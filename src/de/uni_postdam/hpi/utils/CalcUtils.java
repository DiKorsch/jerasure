package de.uni_postdam.hpi.utils;

import static de.uni_postdam.hpi.utils.FileUtils.*;

public class CalcUtils {
	
	static final long maxBufferSize = 5 * MB;

	public static int calcPacketSize(int k, int w, long filesize) {
		int packetsize = (int) (filesize / (k * w * 256));
		packetsize = packetsize + 1;
		return packetsize;
	}

	public static long calcNewSize(long origSize, int k, int m) {
		return (origSize + (origSize * m / k));
	}

	public static int calcBufferSize(int k, int w, int packetSize, long size) {
		int blockSize = calcBlockSize(k, w, packetSize);
		int factor = (int) (size / blockSize);
		int calcFactor = factor;
		int i;
		for (i = factor - 1; i > 1; i--) {
			if (factor % i == 0) {
				calcFactor = i;
				break;
			}
		}
		if (calcFactor == 0) {
			return (int) Math.min(size, maxBufferSize);
		}
		return (int) Math.min(calcFactor * blockSize, maxBufferSize);
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
