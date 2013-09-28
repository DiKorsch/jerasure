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
			Matrix encMat = Cauchy.good_general_coding_matrix(k, m, w);
			BitMatrix encBitMat = new BitMatrix(encMat, w);
			BitMatrix decMatrix = generate_decoding_bitmatrix(encBitMat);
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

	private BitMatrix generate_decoding_bitmatrix(BitMatrix mat){
		
		BitMatrix test = null;
		int[] content = null;
		
		update_erased_ids();
		int cdf = 0, ddf = 0;
		for (int i = 0; i < erasures.length; i++) {
			if (erasures[i]){
				if(i < k) ddf++; 
				else cdf++;
			}
		}
		
		BitMatrix real_decoding_matrix = new BitMatrix(k, cdf+ddf, w);
		
		if(ddf > 0){
			BitMatrix decoding_matrix = new BitMatrix(k,k,w);
			int ptr = 0;
			for (int i = 0; i < k; i++) {
				if (row_to_id[i] == i) {
					// diagonale mit 1 setzen, den rest mit 0        
					decoding_matrix.zero(ptr, k*w*w); // bzero(ptr, k*w*w*sizeof(int));
					for (int x = 0; x < w; x++) {
						decoding_matrix.setWithIdx(ptr + x+i*w+x*k*w, 1);
					} 
				} else {
					// memcpy(ptr, bitmatrix+k*w*w*(row_ids[i]-k), k*w*w*sizeof(int));
					decoding_matrix.copy_withIdx(ptr, mat, k*w*w*(row_to_id[i]-k), k*w*w);
				}
				ptr += (k*w*w);
			}
			
			
			if(k == 3 && m== 2 && w == 3){
				content = new int[]{
					1, 0, 0, 1, 0, 0, 1, 0, 0, 
					0, 1, 0, 0, 1, 0, 0, 1, 0, 
					0, 0, 1, 0, 0, 1, 0, 0, 1, 

					1, 0, 0, 0, 0, 1, 1, 1, 0, 
					0, 1, 0, 1, 0, 1, 0, 0, 1, 
					0, 0, 1, 0, 1, 0, 1, 0, 0, 

					0, 0, 0, 0, 0, 0, 1, 0, 0, 
					0, 0, 0, 0, 0, 0, 0, 1, 0, 
					0, 0, 0, 0, 0, 0, 0, 0, 1,
				};
				test = new BitMatrix(k,k,w, content);
				assert decoding_matrix.equals(test);
			}
			
			BitMatrix inverse = decoding_matrix.invert(w);
			
			
			if(k == 3 && m== 2 && w == 3){
				content = new int[]{
					1, 1, 1, 0, 1, 1, 0, 1, 0, 
					1, 0, 0, 1, 1, 0, 0, 1, 1, 
					1, 1, 0, 1, 1, 1, 1, 0, 1, 

					0, 1, 1, 0, 1, 1, 1, 1, 0, 
					1, 1, 0, 1, 1, 0, 0, 0, 1, 
					1, 1, 1, 1, 1, 1, 1, 0, 0, 

					0, 0, 0, 0, 0, 0, 1, 0, 0, 
					0, 0, 0, 0, 0, 0, 0, 1, 0, 
					0, 0, 0, 0, 0, 0, 0, 0, 1, 

				};
				test = new BitMatrix(k,k,w, content);
				assert inverse.equals(test);
			}
			
			ptr = 0;
			for(int i = 0; i < ddf; i++){
				real_decoding_matrix.copy_withIdx(ptr, inverse, k*w*w*row_to_id[k+i], k*w*w);
				ptr += (k*w*w);
			}
		}
		
		
		for (int x = 0; x < cdf; x++) {
		    int drive = row_to_id[x+ddf+k]-k;
		    int ptr = k*w*w*(ddf+x);
		    // memcpy(ptr, bitmatrix+drive*k*w*w, sizeof(int)*k*w*w);
		    real_decoding_matrix.copy_withIdx(ptr, mat, drive*k*w*w, k*w*w);

		    for (int i = 0; i < k; i++) {
		      if (row_to_id[i] != i) {
		        for (int j = 0; j < w; j++) {
		        	real_decoding_matrix.zero(ptr+j*k*w+i*w, w); // bzero(ptr+j*k*w+i*w, sizeof(int)*w);
		        }
		      }  
		    }

		    /* There's the yucky part */

		    int index = drive*k*w*w;
		    for (int i = 0; i < k; i++) {
		    	if (row_to_id[i] != i) {
		    		int b1 =(id_to_row[i]-k)*k*w*w;
		    		for (int j = 0; j < w; j++) {
		    			int b2 = ptr + j*k*w;
		    			for (int y = 0; y < w; y++) {
		    				if (mat.getWithIdx(index+j*k*w+i*w+y) != 0) {
		    					for (int z = 0; z < k*w; z++) {
		    						// b2[z] = b2[z] ^ b1[z+y*k*w];
		    						int val = real_decoding_matrix.getWithIdx(b2 + z) ^ real_decoding_matrix.getWithIdx(b1 + z+y*k*w);
		    						real_decoding_matrix.setWithIdx(b2 + z, val);
		    					}
		    				}
		    			}
		    		}
		    	}  
		    }
	    }
		
		if(k == 3 && m == 2 && w == 3){
			content = new int[]{
				1, 1, 1, 0, 1, 1, 0, 1, 0, 
				1, 0, 0, 1, 1, 0, 0, 1, 1, 
				1, 1, 0, 1, 1, 1, 1, 0, 1, 
	
				0, 1, 1, 0, 1, 1, 1, 1, 0, 
				1, 1, 0, 1, 1, 0, 0, 0, 1, 
				1, 1, 1, 1, 1, 1, 1, 0, 0, 
			};
			test = new BitMatrix(k,m,w, content);
			assert real_decoding_matrix.equals(test);
		}
		
		return real_decoding_matrix;
	}
	
	private void update_erased_ids(){
		row_to_id = new int[k+m];
		id_to_row = new int[k+m];
		
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
				row_to_id[i] = j;
				id_to_row[j] = i;
				j++;
				row_to_id[x] = i;
				id_to_row[i] = x;
				x++;
			}
		}
		for (int i = k; i < k+m; i++) {
			if (erasures[i]) {
				row_to_id[x] = i;
				id_to_row[i] = x;
				x++;
			}
		}
	}
}














