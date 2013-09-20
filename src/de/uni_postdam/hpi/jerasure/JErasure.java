package de.uni_postdam.hpi.jerasure;

import de.uni_postdam.hpi.matrix.Schedule;

public class JErasure {

	private static int calc_packetsize(int k, int w, int filesize){
	    int packetsize = filesize / (k * w * 256);
	    packetsize = packetsize + (2 - packetsize % 2);
	    return packetsize;
	}

	private static int getNewSize(long origSize, int k, int m){
		return (int) (origSize + (origSize * m / k));
	}

	public static byte[] encode(int k, int m, int w, Schedule[] schedules,
			byte[] data, int packetSize) {
		if(data.length != k * w * packetSize){
			throw new IllegalArgumentException(
					String.format("data array has not valid size! data.lenght=%d, should(k * w * packetSize): %d",
					data.length, k * w * packetSize));
		}

		byte[] dataAndCoding = new byte[getNewSize(data.length, k, m)];
		
		for(int i = 0; i < k * packetSize * w; i++) {
			dataAndCoding[i] = data[i];
		}
		for(int i = 0; i < m * packetSize * w; i++) {
			dataAndCoding[i + k * packetSize * w] = 0x00;	// coding part
		}
		
		for (int done = 0; done < data.length; done += packetSize * w) {
			for(Schedule sched: schedules){
				dataAndCoding = sched.operate(dataAndCoding, packetSize, w);
			}
		}
		
		return dataAndCoding;
	}
	
}
