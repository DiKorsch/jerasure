package de.uni_postdam.hpi.utils;

import java.util.Arrays;

import de.uni_postdam.hpi.jerasure.Buffer;
import de.uni_postdam.hpi.matrix.Schedule;

public class CodingUtils {

	static byte[] prepareCoding(byte[] data, int packetSize, int k, int m, int w) {
		int newSize = (int) CalcUtils.calcNewSize(data.length, k, m);
		byte[] result = new byte[newSize];
		for (int i = 0; i < k * packetSize * w; i++) {
			result[i] = data[i];
		}
		for (int i = k * packetSize * w; i < newSize; i++) {
			result[i] = 0x00; // coding part or part for restore
		}
		return result;
	}

	static byte[] createCoding(int dataLen, int k, int m){
		return new byte[dataLen / k * m];
	}

	public static byte[] addPaddingIfNeeded(byte[] data, int blockSize) {
		if (data.length < blockSize) {
			data = Arrays.copyOf(data, blockSize);
		}
		return data;
	}
	public static byte[] enOrDecode(byte[] data, Schedule[] schedules, int k, int m, int w, int packetSize){
		int blockSize = CalcUtils.calcBlockSize(k, w, packetSize);
		byte[] coding = CodingUtils.createCoding(blockSize, k, m);
		return Schedule.do_scheduled_operations(data, coding, schedules, data.length, packetSize, w); 
	}
	
	public static byte[] enOrDecode(Buffer data, Schedule[] schedules, int k,
			int m, int w, int packetSize) {
		
		data.setEnd(CalcUtils.calcBlockSize(k, w, packetSize));
		byte[] coding = CodingUtils.createCoding(data.size(), k, m);
		return Schedule.do_scheduled_operations(data, coding, schedules, packetSize, w); 
	}

}
