package de.uni_postdam.hpi.jerasure.bufferless;

import static de.uni_postdam.hpi.utils.FileUtils.close;
import static de.uni_postdam.hpi.utils.FileUtils.createParts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class Writer {
	int k = 0, m = 0, w = 0;
	File original = null;
	private WriterThread thread;


	Deque<byte[][]> dataBuffer = new ArrayDeque<byte[][]>();
	Deque<byte[][]> codingBuffer = new ArrayDeque<byte[][]>();
	
	
	private class WriterThread extends Thread{
		
		FileOutputStream[] dataParts = null;
		FileOutputStream[] codingParts = null;

		Writer parent = null;
		boolean work = true;
		
		public WriterThread(File original, Writer parent) {

			this.parent = parent;
			this.dataParts = createParts(original.getAbsolutePath(), "k", k);
			this.codingParts = createParts(original.getAbsolutePath(), "m", m);
		}
		
		@Override
		public void run() {
			
			while(this.work || this.parent.hasData()){
				
				try {
					byte[][][] dataAndCoding = this.parent.get();
					int i = 0;
					for (byte[] dataPart: dataAndCoding[0]) {
						this.dataParts[i++].write(dataPart);
					}
					
					i = 0;
					for (byte[] codingPart: dataAndCoding[1]) {
						this.codingParts[i++].write(codingPart);
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
	
		public void closeOutputs() {
			close(dataParts);
			close(codingParts);
		}
	}
	
	public Writer(int k, int m, int w, File original) {
		this.k = k;
		this.m = m;
		this.w = w;
		
		this.original = original;
		
		this.thread = new WriterThread(original, this);
		
		this.thread.start();

	}
	
	protected void finalize() throws Throwable {
		this.thread.join();
	};

	public void write(byte[][] data, byte[][] coding) throws InterruptedException {

		while(this.isFull()){
			synchronized (this) {
				this.wait();
			}
		}
		
		synchronized (this) {
			dataBuffer.addFirst(data);
			codingBuffer.addFirst(coding);
			this.notify();
		}
	}
	
	private boolean isFull() {
		return this.dataBuffer.size() >= 250;
	}

	byte[][][] get() throws InterruptedException{
		while(dataBuffer.isEmpty()) {
			synchronized (this) {
				this.wait();
			}
		}

		synchronized (this) {
			byte[][] res1 = dataBuffer.pollLast();
			byte[][] res2 = codingBuffer.pollLast();
			this.notify();
			return new byte[][][]{res1, res2};
		}
		
	}
	

	
	public boolean hasData(){
		return !(codingBuffer.isEmpty() && dataBuffer.isEmpty());
	}
	
	public void join() {
		try {
			this.thread.work = false;
			this.thread.join();
			this.thread.closeOutputs();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	
}
