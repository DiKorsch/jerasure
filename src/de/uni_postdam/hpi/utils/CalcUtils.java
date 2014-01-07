package de.uni_postdam.hpi.utils;

import static de.uni_postdam.hpi.utils.FileUtils.*;

public class CalcUtils {
	
	public static final long maxBufferSize = 10 * MB;

	public static int calcPacketSize(int k, int w, long filesize) {
		int packetsize = (int) (filesize / (k * w * 128));
		packetsize = packetsize + 1;
		return packetsize;
	}

	public static long calcNewSize(long origSize, int k, int m) {
		return (origSize + (origSize * m / k));
	}

	public static int calcBufferSize(int k, int w, int packetSize, long size, long maxSize){
		int blockSize = calcBlockSize(k, w, packetSize);
		long bufferSize = Math.min(maxSize, size);
		bufferSize = (bufferSize / blockSize + 1) * blockSize;
		return (int) bufferSize;
		
	}
	
	public static int calcBufferSize(int k, int w, int packetSize, long size) {
		return calcBufferSize(k, w, packetSize, size, maxBufferSize);
	}
	
	public static int calcBlockSize(int k, int w, int packetSize){
		return k * w * packetSize;
	}
	
	public static int calcCodingBlockSize(int m, int w, int packetSize){
		return m * w * packetSize;
	}

	public static int calcOverHead(long size, int k, int w){
		int bufferSize, packetSize, blockSize;
		
		packetSize = calcPacketSize(k, w, size);
		bufferSize = calcBufferSize(k, w, packetSize, size);
		blockSize = calcBlockSize(k, w, packetSize);
		
		// last bytes, which have to be padded with zeros
		int rest = (int) ((size % bufferSize) % blockSize);
		
		return blockSize - rest;
	}

}
