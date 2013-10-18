package de.uni_postdam.hpi.jerasure;

import java.util.ArrayList;
import java.util.List;

public class EncoderThread extends Thread {

	byte[] data = null;
	byte[] coding = null;
	
//	int startData, startCoding;
	
	int packetSize, w;
	Encoder enc = null;
	
	List<Integer[]> starts = new ArrayList<>();
	
	public EncoderThread(Buffer data, Buffer coding, Encoder encoder) {
		this.data = data.getData();
		this.coding = coding.getData();
		this.enc = encoder;
		
		this.addRange(data.getStart(), coding.getStart());
		
	}
	
	@Override
	public void run() {
		for(Integer[] start: starts){
			enc.encode(data, start[0], coding, start[1]);
		}
	}

	public void addRange(int startData, int startCoding) {

		starts.add(new Integer[]{startData, startCoding});
	}
}
