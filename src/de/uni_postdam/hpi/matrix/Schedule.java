package de.uni_postdam.hpi.matrix;

import de.uni_postdam.hpi.cauchy.Cauchy;
import de.uni_postdam.hpi.jerasure.Buffer;

public class Schedule {
	
	public enum OPERATION{
		COPY,
		XOR
	}
	
	/** FALSE for copy and TRUE for XOR*/
	OPERATION operation;
	
	int sourceId;
	int sourceBit;
	
	int destinationId;
	int destinationBit;
	
	public Schedule(OPERATION operation, int srcId, int srcBit, int destId, int destBit) {
		this.operation = operation;
		this.sourceId = srcId;
		this.sourceBit = srcBit;
		this.destinationId = destId;
		this.destinationBit = destBit;
	}
	
	@Override
	public String toString() {
		
		return String.format("<%d, %d, %d, %d, %d>",
				operation == OPERATION.XOR ? 1 : 0, 
				sourceId, sourceBit,
				destinationId, destinationBit);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Schedule) {
			Schedule other = (Schedule) obj;
			return 
				this.operation == other.operation 
				&& this.sourceId == other.sourceId
				&& this.sourceBit == other.sourceBit
				&& this.destinationId == other.destinationId
				&& this.destinationBit == other.destinationBit;
		}
		return false;
	}



	public byte[] operate(Buffer data, byte[] coding, int packetSize, int w) {
		int srcStart = computeSrcIdx(packetSize, w);
		int destStart = computeDestIdx(packetSize, w);
		if(operation == OPERATION.XOR){
			return xor(data, coding, srcStart, destStart, packetSize);
		}else{
			return copy(data, coding, srcStart, destStart, packetSize);
		}
	}
	
	private byte[] copy(Buffer data, byte[] coding, int srcStart, int destStart, int packetSize) {
		for(int i = 0; i < packetSize; i++){
			coding[destStart + i] = data.get(srcStart + i); 
		}
		return coding;
	}

	private byte[] xor(Buffer data, byte[] coding, int srcStart, int destStart, int packetSize) {
		for(int i = 0; i < packetSize; i++){
			coding[destStart + i] ^= data.get(srcStart + i); 
		}
		return coding;
	}


	public byte[] operate(byte[] data, byte[] coding, int packetSize, int w) {
		int srcStart = computeSrcIdx(packetSize, w);
		int destStart = computeDestIdx(packetSize, w);
		if(operation == OPERATION.XOR){
			return xor(data, coding, srcStart, destStart, packetSize);
		}else{
			return copy(data, coding, srcStart, destStart, packetSize);
		}
	}
	
	private byte[] copy(byte[] data, byte[] coding, int srcStart, int destStart, int packetSize) {
		
		for(int i = 0; i < packetSize; i++){
			coding[destStart + i] = data[srcStart + i]; 
		}
		return coding;

	}

	private byte[] xor(byte[] data, byte[] coding, int srcStart, int destStart, int packetSize) {
		
		for(int i = 0; i < packetSize; i++){
			coding[destStart + i] ^= data[srcStart + i]; 
		}
		return coding;
	}

	private int computeSrcIdx(int packetSize, int w){
		return this.sourceId * packetSize * w + this.sourceBit * packetSize;
	}
	
	private int computeDestIdx(int packetSize, int w){
		return this.destinationId * packetSize * w + this.destinationBit * packetSize;
	}
	
	public static Schedule[] generate(int k, int m, int w){
		BitMatrix matrix = new BitMatrix(Cauchy.good_general_coding_matrix(k, m, w), w);
		return matrix.toSchedules(k, w);
	}

	public static byte[] do_scheduled_operations(byte[] data, byte[] coding, Schedule[] schedules, long dataSize, int packetSize, int w){
		for (int done = 0; done < dataSize; done += packetSize * w) {
			for (Schedule sched : schedules) {
				coding = sched.operate(data, coding, packetSize, w);
			}
		}
		return coding;
	}
	

	public static byte[] do_scheduled_operations(Buffer data,
			byte[] coding, Schedule[] schedules, int packetSize, int w) {
		for (int done = 0; done < data.size(); done += packetSize * w) {
			for (Schedule sched : schedules) {
				coding = sched.operate(data, coding, packetSize, w);
			}
		}
		return coding;
	}

}































