package de.uni_postdam.hpi.matrix;

import de.uni_postdam.hpi.jerasure.Buffer;

public class COPYSchedule extends Schedule {

	public COPYSchedule(int srcId, int srcBit, int destId,
			int destBit) {
		super(srcId, srcBit, destId, destBit);
		this.operation = OPERATION.COPY;
	}

	

	@Override
	public byte[] operate(Buffer data, byte[] coding, int packetSize, int w) {
	
		int srcStart = computeSrcIdx(packetSize, w);
		int destStart = computeDestIdx(packetSize, w);
		for(int i = 0; i < packetSize; i++){
			coding[destStart + i] = data.get(srcStart + i); 
		}
		copyCount++;
		return coding;
	}	

	@Override
	public byte[] operate(byte[] data, byte[] coding, int packetSize, int w) {

		int srcStart = computeSrcIdx(packetSize, w);
		int destStart = computeDestIdx(packetSize, w);
		for(int i = 0; i < packetSize; i++){
			coding[destStart + i] = data[srcStart + i]; 
		}
		copyCount++;
		return coding;
	}
	
	@Override
	public void operate(Buffer data, Buffer coding, int packetSize, int w) {
		
		int srcStart = computeSrcIdx(packetSize, w);
		int destStart = computeDestIdx(packetSize, w);
		for(int i = 0; i < packetSize; i++){
			coding.set(destStart + i, data.get(srcStart + i)); 
		}
		copyCount++;

	}



	@Override
	public void operate(byte[] data, int startData, byte[] coding,
			int startCoding, int packetSize, int w) {
		int srcStart = computeSrcIdx(packetSize, w);
		int destStart = computeDestIdx(packetSize, w);

		for(int i = 0; i < packetSize; i++){
			coding[destStart + i + startCoding] = data[srcStart + i + startData]; 
		}
		copyCount++;		
	}
}
