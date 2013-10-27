package de.uni_postdam.hpi.jerasure;

import java.util.ArrayList;
import java.util.List;

public class DecoderThread extends Thread {

	byte[] data = null;
	byte[] coding = null;
	
	Decoder dec = null;
	
	List<Integer[]> starts = new ArrayList<>();
	
	public DecoderThread(Buffer data, Buffer coding, Decoder decoder) {

		this.data = data.getData();
		this.coding = coding.getData();
		this.dec = decoder;
		
		this.addRange(data.getStart(), coding.getStart());
	}

	@Override
	public void run() {
		for(Integer[] start: starts){
			dec.decode(data, start[0], coding, start[1]);
		}
	}

	public void addRange(int startData, int startCoding) {

		starts.add(new Integer[]{startData, startCoding});
	}
		
}
