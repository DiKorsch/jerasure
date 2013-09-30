package de.uni_postdam.hpi.utils;

import java.util.Arrays;

import de.uni_postdam.hpi.matrix.Schedule;

public class CodingUtils {

	public static byte[] prepareData(byte[] data, int packetSize, int k, int m, int w) {
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

	public static byte[] addPaddingIfNeeded(byte[] data, int blockSize) {
		if (data.length < blockSize) {
			data = Arrays.copyOf(data, blockSize);
		}
		return data;
	}
	
	public static byte[] enOrDecode(byte[] data, Schedule[] schedules, int k, int m, int w, int packetSize){

		int blockSize = CalcUtils.calcBlockSize(k, w, packetSize);
		data = CodingUtils.addPaddingIfNeeded(data, blockSize);
		byte[] dataAndCoding = CodingUtils.prepareData(data, packetSize, k, m, w);
		return Schedule.do_scheduled_operations(dataAndCoding, schedules, data.length, packetSize, w); 
	}

}
