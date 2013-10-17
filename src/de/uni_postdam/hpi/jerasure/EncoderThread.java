package de.uni_postdam.hpi.jerasure;

public class EncoderThread extends Thread {

	byte[] data = null;
	byte[] coding = null;
	
	int startData, startCoding;
	
	int packetSize, w;
	Encoder enc = null;
	
	public EncoderThread(Buffer data, Buffer coding, Encoder encoder) {
		this.data = data.getData();
		this.coding = coding.getData();
		
		this.startData = data.getStart();
		this.startCoding = coding.getStart();
		
		this.enc = encoder;
	}
	
	@Override
	public void run() {
		enc.encode(data, startData, coding, startCoding);
	}
}
