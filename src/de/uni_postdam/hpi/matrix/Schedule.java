package de.uni_postdam.hpi.matrix;

import de.uni_postdam.hpi.cauchy.Cauchy;
import de.uni_postdam.hpi.jerasure.Buffer;

public abstract class Schedule {

	public static long xorCount = 0;
	public static long copyCount = 0;

	public enum OPERATION {
		COPY, XOR
	}
	public OPERATION operation;

	public int sourceId;
	public int sourceBit;

	public int destinationId;
	public int destinationBit;
	
	public static Schedule create(OPERATION operation, int srcId, int srcBit, int destId, int destBit) {
		if(operation == OPERATION.XOR){
			return new XORSchedule(srcId, srcBit, destId, destBit);
		}else{
			return new COPYSchedule(srcId, srcBit, destId, destBit);
		}
	}
	
	protected Schedule(int srcId, int srcBit, int destId, int destBit){
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


	public abstract void operate(Buffer data, Buffer coding, int packetSize, int w);
	public abstract void operate(byte[] data, int startData, byte[] coding, int startCoding, int packetSize, int w);
	public abstract byte[] operate(byte[] data, byte[] coding, int packetSize, int w);
	public abstract byte[] operate(Buffer data, byte[] coding, int packetSize, int w);
	
	public abstract byte[][] operate(byte[][] data, byte[][] coding, int w);


	protected int computeSrcIdx(int packetSize, int w){
		return (this.sourceId * w + this.sourceBit) * packetSize;
	}
	
	protected int computeDestIdx(int packetSize, int w){
		return (this.destinationId * w + this.destinationBit) * packetSize;
	}
	
	
	public static Schedule[] generate(int k, int m, int w){
		BitMatrix matrix = new BitMatrix(Cauchy.good_general_coding_matrix(k, m, w), w);
		return matrix.toSchedules(k, w);
	}
	
	
	public static byte[] do_scheduled_operations(byte[] data, byte[] coding, Schedule[] schedules, int packetSize, int w){
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
