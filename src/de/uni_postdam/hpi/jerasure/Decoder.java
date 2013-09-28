package de.uni_postdam.hpi.jerasure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.SortedMap;
import java.util.TreeMap;

import static de.uni_postdam.hpi.utils.CalcUtils.*;
import de.uni_postdam.hpi.cauchy.Cauchy;
import de.uni_postdam.hpi.matrix.*;
import de.uni_postdam.hpi.utils.FileUtils;

public class Decoder {

	File original = null;
	int k, m, w;
	
	File[] k_parts, m_parts;
	
	boolean[] erasures = null;
	int[] row_to_id = null;
	int[] id_to_row = null;
	
	int dataFailed = 0, codingFailed = 0;
	
	
	public Decoder(File file, int k, int m, int w) {
		this.original = file;
		this.k = k;
		this.m = m;
		this.w = w;
		
		this.k_parts = FileUtils.collectFiles(file.getAbsolutePath(), "k", k);
		this.m_parts = FileUtils.collectFiles(file.getAbsolutePath(), "m", m);
		this.erasures = new boolean[k+m];
		
		updateErasures();
	}
	
	private void updateErasures(){
		if(erasures == null) 
			throw new RuntimeException("erasures was null!");
		
		int c = 0;
		for(File part: k_parts){
			erasures[c++] = !part.exists();
		}
		for(File part: m_parts){
			erasures[c++] = !part.exists();
		}
	}
	
	public void decode(long size) {
		if(!isValid()){
			System.err.println("Not enough parts to decode the original!");
			return;
		}
		if(all_k_parts_exist()){
			decodeFromKParts(size);
		} else {
			BitMatrix decMatrix = this.generate_decoding_bitmatrix();
//			System.out.println("encMat:");
//			encMat.print(System.out);
//			System.out.println("encBitMat:");
//			encBitMat.print(System.out);
//			System.out.println("decMat:");
//			decMatrix.print(System.out);
		}
	}
	public boolean isValid(){
		updateErasures();
		int c = 0;
		for(boolean erased: erasures){
			if(erased)
				c++;
		}
		
		return c <= this.m;
	}

	private boolean all_k_parts_exist(){
		for(File part: k_parts){
			if(!part.exists())
				return false;
		}
		return true;
	}
	
	private SortedMap<Integer, FileInputStream> orderParts(File[] parts, String partSuffix) throws FileNotFoundException{
		SortedMap<Integer, FileInputStream> result = new TreeMap<Integer, FileInputStream>();
		for(File part: parts){
			result.put(FileUtils.extractNum(part.getName(), partSuffix), new FileInputStream(part));
		}
		return result;
	}

	private void decodeFromKParts(long size){
		int packetSize = calcPacketSize(k, w, size);
		int blockSize = calcBlockSize(k, w, packetSize);
		int bufferSize = calcBufferSize(k, w, packetSize, size);

		RandomAccessFile f = null;
		byte[] buffer = null;
		SortedMap<Integer, FileInputStream> parts = null;
		try {
			parts = orderParts(k_parts, "k");
			f = new RandomAccessFile(original, "rw");
			f.setLength(size);
			int bytesWritten = 0;
			while (bytesWritten < size) {
				int bytesToWrite = (int) Math.min(bufferSize, size - bytesWritten);
				buffer = read_from_k_parts(parts, packetSize, blockSize, bytesToWrite);
				f.write(buffer);
				bytesWritten += bytesToWrite;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(f != null){
					f.close();
				}
				if(parts != null){
					for(FileInputStream part: parts.values()){
						if(part != null)
							part.close();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}


	private byte[] read_from_k_parts(SortedMap<Integer, FileInputStream> parts, int packetSize, int blockSize, int bytesToWrite) throws IOException {
		
		if(bytesToWrite < blockSize){
			return read_last_bytes(parts, packetSize, bytesToWrite);
		}
		
		byte[] result = new byte[bytesToWrite];
		int c = 0;
		for(int i = 0; i < bytesToWrite / blockSize; i++){
			for(FileInputStream part_fos: parts.values()){
				byte[] packet = new byte[packetSize * w];
				part_fos.read(packet);
				for(byte b: packet){
					result[c++] = b;
				}
			}
		}
		return result;
	}
	
	private byte[] read_last_bytes(SortedMap<Integer, FileInputStream> parts, int packetSize, int bytesToWrite) throws IOException{
		byte[] result = new byte[bytesToWrite];
		int c = 0;
		for(FileInputStream part_fos: parts.values()){
			do{
				result[c++] = (byte) part_fos.read();
				if(c == bytesToWrite) return result;
			} while(c % (packetSize * w) != 0);
		}
		return result;
	}

	public BitMatrix generate_decoding_bitmatrix(){
		
		updateErasures();
		update_erased_ids();
		
		BitMatrix mat = new BitMatrix(Cauchy.good_general_coding_matrix(k, m, w), w);
		BitMatrix result = new BitMatrix(k, codingFailed + dataFailed, w);
		
		if(dataFailed > 0){
			BitMatrix decoding_matrix = new BitMatrix(k,k,w);
			decoding_matrix.toIdentity();
			for (int i = 0; i < k; i++) {
				if (!deviceOK(i)) {
					// memcpy(ptr, bitmatrix+k*w*w*(row_ids[i]-k), k*w*w*sizeof(int));
					decoding_matrix.copyRows(i * w, mat, row_to_coding_id(i) * w, w);
				}
			}
			
			BitMatrix inverse = decoding_matrix.invert(w);

			for(int i = 0; i < dataFailed; i++){
				result.copyRows(i * w, inverse, row_to_id[i + k] * w, w);
			}
		}
		
		
		for (int x = dataFailed; x < codingFailed + dataFailed; x++) {
		    int codingId = row_to_coding_id(x + k);
		    int currRow = x * w;
		    // memcpy(ptr, bitmatrix+drive*k*w*w, sizeof(int)*k*w*w);
			result.copyRows(currRow, mat, codingId * w, w);

			for (int i = 0; i < k; i++) {
				if (!deviceOK(i)) {
					result.zero(i * w, currRow, w, w);
				}  
			}

		    /* There's the yucky part */
		    for (int dataId = 0; dataId < k; dataId++) {
		    	if (deviceOK(dataId)) { continue; }
		    	result.do_yucky_decoding_stuff(mat, currRow, id_to_row[dataId] - k, dataId, codingId);
	    	}  
	    }
		
		return result;
	}
	
	

	private int row_to_coding_id(int i){
		return row_to_id[i] - k;
	}
	
	private boolean deviceOK(int i){
		return row_to_id[i] == i;
	}
	
	private void update_erased_ids(){
		row_to_id = new int[k+m];
		id_to_row = new int[k+m];
		codingFailed = 0;
		dataFailed = 0;
		
		int j = k, x = k;
		for (int i = 0; i < k; i++) {
			if (!erasures[i]) {
				row_to_id[i] = i;
				id_to_row[i] = i;
			} else {
				while (erasures[j]){
					if(++j == erasures.length){
						throw new RuntimeException("Not enough redundant parts!");
					}
				}
				dataFailed++;
				row_to_id[i] = j;
				id_to_row[j] = i;
				j++;
				id_to_row[i] = x;
				row_to_id[x] = i;
				x++;
			}
		}
		for (int i = k; i < k+m; i++) {
			if (erasures[i]) {
				codingFailed++;
				row_to_id[x] = i;
				id_to_row[i] = x;
				x++;
			}
		}
		
		System.out.println();
	}
}














