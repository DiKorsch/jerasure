package de.uni_postdam.hpi.jerasure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import static de.uni_postdam.hpi.utils.CalcUtils.*;
import de.uni_postdam.hpi.utils.FileUtils;

public class Decoder {

	File original = null;
	int k, m, w;
	
	File[] k_parts, m_parts;
	
	boolean[] erasures = null;
	
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
		if(all_k_parts_exist()){
			decodeFromKParts(size);
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
	
}














