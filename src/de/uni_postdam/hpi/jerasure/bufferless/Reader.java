package de.uni_postdam.hpi.jerasure.bufferless;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class Reader {

	int k,m,w;
	
//	ReaderThread thread = null;

	private Deque<byte[][]> buffer_old = new ArrayDeque<byte[][]>();
	private byte[] buffer = null;
	private final int BUFFERSIZE = 1000;
	private int bufferPos = 0;
	public boolean ready = false;
	
	private FileInputStream fis = null;

	private boolean eof_reached;

	public Reader(int k, int m, int w, FileInputStream fis) {
		this.k = k;
		this.m = m;
		this.w = w;
		
		this.fis = fis;
		this.ready = false;
	}
	
	
	
	public boolean isFullyRead() {
		return bufferPos >= BUFFERSIZE;
	}

	public void threadIsReady() {
		ready = true;
	}
	
	public boolean next(){
		return !(this.eof_reached && isFullyRead());
	}

	synchronized public void add(byte[][] data){
		
		if(this.isFullyRead()){
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		buffer_old.addFirst(data);
		this.notify();
	}
	
	public byte[][] get() throws IOException{
		
		this.read();
		byte[][] res = new byte[k][w];
		for(int i = 0; i < k; i++)
			for(int j = 0; j < w; j++)
				res[i][j] = buffer[bufferPos * k * w + i * w + j];
		
		bufferPos += 1;
		return res;
	}

	private void read() throws IOException {
		if(this.buffer == null) {
			this.buffer = new byte[k * w * BUFFERSIZE];
		} else if(bufferPos < BUFFERSIZE) return;
		
		
//		System.out.println("Reading " + BUFFERSIZE + " bytes");
		this.eof_reached = this.fis.read(buffer) == -1;
		bufferPos = 0;
	}
}
