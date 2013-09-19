package de.uni_postdam.hpi.jerasure;

import de.uni_postdam.hpi.matrix.Schedule;

public class JErasure {

	public static byte[] encode(int k, int m, int w, Schedule[] schedules,
			byte[] data, byte[] coding, int size, int packetSize) {

		int i;
		byte[] dataAndCoding = new byte[data.length + coding.length];
		
		for(i = 0; i < k; i++) dataAndCoding[i] = data[i];
		for(i = 0; i < m; i++) dataAndCoding[i + k] = coding[i];
		
		for (int done = 0; done < size; done += packetSize * w) {
			for(Schedule sched: schedules){
				dataAndCoding = sched.operate(dataAndCoding, packetSize, w);
			}
		}
		
		return dataAndCoding;
	}
	
}
