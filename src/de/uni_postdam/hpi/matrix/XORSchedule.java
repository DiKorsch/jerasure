package de.uni_postdam.hpi.matrix;

import de.uni_postdam.hpi.jerasure.Buffer;

public class XORSchedule extends Schedule {

	public XORSchedule(int srcId, int srcBit, int destId,
			int destBit) {
		super(srcId, srcBit, destId, destBit);
		this.operation = OPERATION.XOR;
	}
	
	@Override
	public byte[] operate(Buffer data, byte[] coding, int packetSize, int w) {
	
		int srcStart = computeSrcIdx(packetSize, w);
		int destStart = computeDestIdx(packetSize, w);
		for(int i = 0; i < packetSize; i++){
			coding[destStart + i] ^= data.get(srcStart + i); 
		}
		xorCount++;
		return coding;
	}	

	@Override
	public byte[] operate(byte[] data, byte[] coding, int packetSize, int w) {

		int srcStart = computeSrcIdx(packetSize, w);
		int destStart = computeDestIdx(packetSize, w);
		for(int i = 0; i < packetSize; i++){
			coding[destStart + i] ^= data[srcStart + i]; 
		}
		xorCount++;
		return coding;
	}
	
	@Override
	public void operate(Buffer data, Buffer coding, int packetSize, int w) {
		
		int srcStart = computeSrcIdx(packetSize, w);
		int destStart = computeDestIdx(packetSize, w);
		for(int i = 0; i < packetSize; i++){
			coding.xor(destStart + i, data.get(srcStart + i)); 
		}
		xorCount++;
	}

}
