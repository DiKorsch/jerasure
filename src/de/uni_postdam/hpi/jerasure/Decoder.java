package de.uni_postdam.hpi.jerasure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

import static de.uni_postdam.hpi.utils.CalcUtils.*;
import de.uni_postdam.hpi.cauchy.Cauchy;
import de.uni_postdam.hpi.matrix.*;
import de.uni_postdam.hpi.utils.CodingUtils;
import de.uni_postdam.hpi.utils.FileUtils;

public class Decoder {

	File original = null;
	int k, m, w;
	
	File[] k_parts, m_parts;
	
	boolean[] erasures = null;
	int[] row_to_device_id = null;
	int[] device_id_to_row = null;
	
	int dataFailed = 0, codingFailed = 0;
	
	Schedule[] schedules = null;
	
	int packetSize = 0, bufferSize = 0, blockSize = 0;
	
	long originalFileSize = 0;
	
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
	
	
	public void decode(long size) {
		if(!isValid()){
			throw new RuntimeException("Not enough parts to decode the original!");
		}
		this.originalFileSize = size;
		calcSizes();
		if(!all_k_parts_exist()){
			generateSchedules();
			restoreKParts();
		}
		decodeFromKParts();
	}
	

	public byte[] decode(byte[] data, int packetSize){
		if(schedules == null){
			generateSchedules();
		}
		return CodingUtils.enOrDecode(data, schedules, k, m, w, packetSize);
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

	private void generateSchedules(){
		BitMatrix decMatrix = this.generate_decoding_bitmatrix();
		schedules = decMatrix.toSchedules(k, w);
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
	
	private void calcSizes(){
		packetSize = calcPacketSize(k, w, originalFileSize);
		blockSize = calcBlockSize(k, w, packetSize);
		bufferSize = calcBufferSize(k, w, packetSize, originalFileSize);
	}
	

	private void restoreKParts() {
		SortedMap<Integer, FileInputStream> parts = null;
		FileOutputStream[] missing_parts = null;
		try {
			parts = getExistingParts();
			missing_parts = getMissingParts();
			int bytesWritten = 0;
			while (bytesWritten < originalFileSize) {
				int bytesToWrite = (int) Math.min(bufferSize, originalFileSize - bytesWritten);
				byte[] buffer = null;
				if(bytesToWrite != bufferSize){
					buffer = read_from_parts(parts, blockSize); 
				} else {
					buffer = read_from_parts(parts, bufferSize); 
				}
				performDecoding(buffer, missing_parts);
				bytesWritten += bytesToWrite;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeStreams(parts);
			FileUtils.close(missing_parts);
		}
		
	}
	
	private void decodeFromKParts(){
		
		RandomAccessFile f = null;
		byte[] buffer = null;
		SortedMap<Integer, FileInputStream> parts = null;
		try {
			parts = orderParts(k_parts, "k");
			f = new RandomAccessFile(original, "rw");
			f.setLength(originalFileSize);
			int bytesWritten = 0;
			while (bytesWritten < originalFileSize) {
				int bytesToWrite = (int) Math.min(bufferSize, originalFileSize - bytesWritten);
				buffer = read_from_parts(parts, bytesToWrite);
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
				closeStreams(parts);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	private void performDecoding(byte[] buffer,
			FileOutputStream[] missing_parts) throws IOException {
		for (int i = 0; i < buffer.length / blockSize; i++) {
			decodeAndWrite(Arrays.copyOfRange(buffer, i * blockSize, (i+1) * blockSize), missing_parts);
		}
	}
	
	private void decodeAndWrite(byte[] data, 
			FileOutputStream[] missing_parts) throws IOException{
		byte[] restored = decode(data, packetSize);
		FileUtils.writeRestored(restored, missing_parts, k, w, packetSize);
	}
	
	private SortedMap<Integer, FileInputStream> orderParts(File[] parts, String partSuffix) throws FileNotFoundException{
		SortedMap<Integer, FileInputStream> result = new TreeMap<Integer, FileInputStream>();
		for(File part: parts){
			result.put(FileUtils.extractNum(part.getName(), partSuffix), new FileInputStream(part));
		}
		return result;
	}
	
	private SortedMap<Integer, FileInputStream> getExistingParts() throws FileNotFoundException {
		SortedMap<Integer, FileInputStream> allParts = new TreeMap<Integer, FileInputStream>();
		int c = 0;
		for(int deviceId : row_to_device_id){
			File currFile = deviceId < k ? k_parts[deviceId] : m_parts[deviceId - k];
			if(!currFile.exists()) 
				continue;
			allParts.put(c++, new FileInputStream(currFile));
		}
		return allParts;
	}

	private FileOutputStream[] getMissingParts() throws FileNotFoundException {
		FileOutputStream[] missing = new FileOutputStream[dataFailed];
		int c = 0;
		for(int deviceId : row_to_device_id){
			File currFile = deviceId < k ? k_parts[deviceId] : m_parts[deviceId - k];
			if(currFile.exists()) 
				continue;
			missing[c++] = new FileOutputStream(currFile);
			if(c == dataFailed){
				break;
			}
		}
		return missing;
	}

	private void closeStreams(SortedMap<Integer, FileInputStream> parts) {
		try {
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


	private byte[] read_from_parts(SortedMap<Integer, FileInputStream> parts, int bytesToRead) throws IOException {
		
		if(bytesToRead < blockSize){
			return read_last_bytes(parts, bytesToRead);
		}
		
		byte[] result = new byte[bytesToRead];
		int c = 0;
		for(int i = 0; i < bytesToRead / blockSize; i++){
			
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
	
	private byte[] read_last_bytes(SortedMap<Integer, FileInputStream> parts, int bytesToRead) throws IOException{
		byte[] result = new byte[bytesToRead];
		int c = 0;
		
		for(FileInputStream part_fos: parts.values()){
			byte[] packet = new byte[packetSize * w];
			if(part_fos.read(packet) != -1){
				for(byte b: packet){
					result[c++] = b;
					if(c == bytesToRead) 
						return result;
				}
			}
		}
		return result;
	}

	public BitMatrix generate_decoding_bitmatrix(){
		
		updateErasures();
		update_erased_ids();
		
		BitMatrix encodingMatrix = new BitMatrix(Cauchy.good_general_coding_matrix(k, m, w), w);
		BitMatrix result = new BitMatrix(k, codingFailed + dataFailed, w);
		
		if(dataFailed > 0){
			BitMatrix decoding_matrix = new BitMatrix(k, k, w);
			decoding_matrix.toIdentity();
			for (int dataDeviceId = 0; dataDeviceId < k; dataDeviceId++) {
				if (!deviceOK(dataDeviceId)) {
					decoding_matrix.copyRows(dataDeviceId * w, encodingMatrix, row_to_coding_id(dataDeviceId) * w, w);
				}
			}
			
			BitMatrix inverse = decoding_matrix.invert(w);

			for(int deviceId = 0; deviceId < dataFailed; deviceId++){
				result.copyRows(deviceId * w, inverse, row_to_device_id[deviceId + k] * w, w);
			}
		}
		
		
		for (int x = dataFailed; x < codingFailed + dataFailed; x++) {
		    int codingId = row_to_coding_id(x + k);
		    int currRow = x * w;
			result.copyRows(currRow, encodingMatrix, codingId * w, w);

			for (int dataDeviceId = 0; dataDeviceId < k; dataDeviceId++) {
				if (!deviceOK(dataDeviceId)) {
					result.zero(dataDeviceId * w, currRow, w, w);
				}  
			}

		    /* There's the yucky part */
		    for (int dataId = 0; dataId < k; dataId++) {
		    	if (deviceOK(dataId)) { continue; }
		    	result.do_yucky_decoding_stuff(encodingMatrix, currRow, device_id_to_row[dataId] - k, dataId, codingId);
	    	}  
	    }
		
		return result;
	}
	
	

	private int row_to_coding_id(int i){
		return row_to_device_id[i] - k;
	}
	
	private boolean deviceOK(int i){
		return row_to_device_id[i] == i;
	}
	
	private void update_erased_ids(){
		row_to_device_id = new int[k+m];
		device_id_to_row = new int[k+m];
		codingFailed = 0;
		dataFailed = 0;
		
		int j = k, x = k;
		for (int i = 0; i < k; i++) {
			if (!erasures[i]) {
				row_to_device_id[i] = i;
				device_id_to_row[i] = i;
			} else {
				while (erasures[j]){
					if(++j == erasures.length){
						throw new RuntimeException("Not enough redundant parts!");
					}
				}
				dataFailed++;
				row_to_device_id[i] = j;
				device_id_to_row[j] = i;
				j++;
				device_id_to_row[i] = x;
				row_to_device_id[x] = i;
				x++;
			}
		}
		for (int i = k; i < k+m; i++) {
			if (erasures[i]) {
				codingFailed++;
				row_to_device_id[x] = i;
				device_id_to_row[i] = x;
				x++;
			}
		}
	}
}
