package de.uni_postdam.hpi.jerasure.bufferless;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class Reader {

	int k,m,w;
	
	ReaderThread thread = null;

	private Deque<byte[][]> buffer = new ArrayDeque<byte[][]>();
	public boolean ready = false;

	public Reader(int k, int m, int w, FileInputStream fis) {
		this.k = k;
		this.m = m;
		this.w = w;
		
		this.thread = new ReaderThread(fis, this);
		this.thread.start();
		this.ready = false;
	}
	
	@Override
	protected void finalize() throws Throwable {
		this.thread.work = false;
		this.thread.join(10 * 1000);
		super.finalize();
	
	}
	
	
	class ReaderThread extends Thread {
		
		private FileInputStream fis;
		private Reader parent;
		private boolean fileEndReached;
//		private int bytesRead = 0;
		boolean work = true;
		
		public FileInputStream getStream(){
			return this.fis;
		}
		
		public ReaderThread(FileInputStream fis, Reader parent) {
			this.fis = fis;
			this.parent = parent;
		}
		
		@Override
		public void run() {
			try {
				while(this.work && !this.fileEndReached){
					this.parent.add(this.read());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(this.fileEndReached){
				this.parent.threadIsReady();
			}
		}

		
		private byte[][] read() throws IOException{
			int currRead = 0;
			byte[][] result = new byte[this.parent.k][this.parent.w];
			for(int id = 0; id < k && !this.fileEndReached; id++){
				currRead = fis.read(result[id]);
				this.fileEndReached = (currRead == -1);
			}
			return result;
		}
	}
	
	
	public boolean isFull() {
		return this.buffer.size() >= 5;
	}

	public void threadIsReady() {
		ready = true;
	}
	
	public boolean next(){
		return !(ready && buffer.isEmpty());
	}

	synchronized public void add(byte[][] data){
		
		if(this.isFull()){
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		buffer.addFirst(data);
		this.notify();
	}
	
	synchronized public byte[][] get(){
		
		if(buffer.isEmpty()) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		byte[][] res = buffer.pollLast();
		this.notify();
		return res;
	}
}
