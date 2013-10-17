package de.uni_postdam.hpi.matrix;

import de.uni_postdam.hpi.cauchy.Cauchy;
import de.uni_postdam.hpi.jerasure.Buffer;

public class Schedule {

	public static long xorCount = 0;
	public static long copyCount = 0;

	public enum OPERATION {
		COPY, XOR
	}

	/** FALSE for copy and TRUE for XOR */
	OPERATION operation;

	int sourceId;
	int sourceBit;

	int destinationId;
	int destinationBit;

	public Schedule(OPERATION operation, int srcId, int srcBit, int destId,
			int destBit) {
		this.operation = operation;
		this.sourceId = srcId;
		this.sourceBit = srcBit;
		this.destinationId = destId;
		this.destinationBit = destBit;
	}

	@Override
	public String toString() {

		return String.format("<%d, %d, %d, %d, %d>",
				operation == OPERATION.XOR ? 1 : 0, sourceId, sourceBit,
				destinationId, destinationBit);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Schedule) {
			Schedule other = (Schedule) obj;
			return this.operation == other.operation
					&& this.sourceId == other.sourceId
					&& this.sourceBit == other.sourceBit
					&& this.destinationId == other.destinationId
					&& this.destinationBit == other.destinationBit;
		}
		return false;
	}

	private void operate(Buffer data, Buffer coding, int packetSize, int w) {
		int srcStart = computeSrcIdx(packetSize, w);
		int destStart = computeDestIdx(packetSize, w);
		if (operation == OPERATION.XOR) {
			xor(data, coding, srcStart, destStart, packetSize);
		} else {
			copy(data, coding, srcStart, destStart, packetSize);
		}
	}

	private void operate(byte[] data, int startData, byte[] coding, int startCoding, int packetSize, int w) {
		int srcStart = computeSrcIdx(packetSize, w);
		int destStart = computeDestIdx(packetSize, w);
		if (operation == OPERATION.XOR) {
			xor(data, startData, coding, startCoding, srcStart, destStart, packetSize);
		} else {
			copy(data, startData, coding, startCoding, srcStart, destStart, packetSize);
		}

	}


	public byte[] operate(byte[] data, byte[] coding, int packetSize, int w) {
		int srcStart = computeSrcIdx(packetSize, w);
		int destStart = computeDestIdx(packetSize, w);
		if (operation == OPERATION.XOR) {
			return xor(data, coding, srcStart, destStart, packetSize);
		} else {
			return copy(data, coding, srcStart, destStart, packetSize);
		}
	}

	public byte[] operate(Buffer data, byte[] coding, int packetSize, int w) {
		int srcStart = computeSrcIdx(packetSize, w);
		int destStart = computeDestIdx(packetSize, w);
		if (operation == OPERATION.XOR) {
			return xor(data, coding, srcStart, destStart, packetSize);
		} else {
			return copy(data, coding, srcStart, destStart, packetSize);
		}
	}
	

	private void copy(byte[] data, int startData, byte[] coding,
			int startCoding, int srcStart, int destStart, int packetSize) {
		for (int i = 0; i < packetSize; i++) {
			coding[destStart + i + startCoding] = data[srcStart + i + startData];
		}
		
	}

	private void xor(byte[] data, int startData, byte[] coding,
			int startCoding, int srcStart, int destStart, int packetSize) {
		for (int i = 0; i < packetSize; i++) {
			coding[destStart + i + startCoding] ^= data[srcStart + i + startData];
		}
	}

	private void copy(Buffer data, Buffer coding, int srcStart, int destStart,
			int packetSize) {
		for (int i = 0; i < packetSize; i++) {
			coding.set(destStart + i, data.get(srcStart + i));
		}
		copyCount++;
	}

	private void xor(Buffer data, Buffer coding, int srcStart, int destStart,
			int packetSize) {
		for (int i = 0; i < packetSize; i++) {
			coding.xor(destStart + i, data.get(srcStart + i));
		}
		xorCount++;
	}

	private byte[] copy(Buffer data, byte[] coding, int srcStart,
			int destStart, int packetSize) {
		for (int i = 0; i < packetSize; i++) {
			coding[destStart + i] = data.get(srcStart + i);
		}
		return coding;
	}

	private byte[] xor(Buffer data, byte[] coding, int srcStart, int destStart,
			int packetSize) {
		for (int i = 0; i < packetSize; i++) {
			coding[destStart + i] ^= data.get(srcStart + i);
		}
		return coding;
	}

	private byte[] copy(byte[] data, byte[] coding, int srcStart,
			int destStart, int packetSize) {

		for (int i = 0; i < packetSize; i++) {
			coding[destStart + i] = data[srcStart + i];
		}
		return coding;

	}

	private byte[] xor(byte[] data, byte[] coding, int srcStart, int destStart,
			int packetSize) {

		for (int i = 0; i < packetSize; i++) {
			coding[destStart + i] ^= data[srcStart + i];
		}
		return coding;
	}

	private int computeSrcIdx(int packetSize, int w) {
		return (this.sourceId * w + this.sourceBit) * packetSize;
	}

	private int computeDestIdx(int packetSize, int w) {
		return (this.destinationId * w + this.destinationBit) * packetSize;
	}

	public static Schedule[] generate(int k, int m, int w) {
		BitMatrix matrix = new BitMatrix(Cauchy.good_general_coding_matrix(k,
				m, w), w);
		return matrix.toSchedules(k, w);
	}

	public static byte[] do_scheduled_operations(byte[] data, byte[] coding,
			Schedule[] schedules, int packetSize, int w) {
		for (Schedule sched : schedules) {
			coding = sched.operate(data, coding, packetSize, w);
		}
		return coding;
	}

	public static byte[] do_scheduled_operations(Buffer data, byte[] coding,
			Schedule[] schedules, int packetSize, int w) {
		for (Schedule sched : schedules) {
			coding = sched.operate(data, coding, packetSize, w);
		}
		return coding;
	}

	public static void do_scheduled_operations(Buffer data, Buffer coding,
			Schedule[] schedules, int packetSize, int w) {
		for (Schedule sched : schedules) {
			sched.operate(data, coding, packetSize, w);
		}
	}

	public static void do_scheduled_operations(byte[] data, int startData,
			byte[] coding, int startCoding, Schedule[] schedules,
			int packetSize, int w) {
		for (Schedule sched : schedules) {
			sched.operate(data, startData, coding, startCoding, packetSize, w);
		}

	}

}
