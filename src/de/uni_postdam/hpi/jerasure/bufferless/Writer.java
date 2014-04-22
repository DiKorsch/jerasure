package de.uni_postdam.hpi.jerasure.bufferless;

import static de.uni_postdam.hpi.utils.FileUtils.close;
import static de.uni_postdam.hpi.utils.FileUtils.createParts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class Writer {
	int k = 0, m = 0, w = 0;
	File original = null;


	Deque<byte[][]> dataBuffer2 = new ArrayDeque<byte[][]>();
	Deque<byte[][]> codingBuffer2 = new ArrayDeque<byte[][]>();

	byte[][] dataBuffer = null;
	byte[][] codingBuffer = null;
	
	private int dataBufferPos = 0, codingBufferPos = 0;
	
	final private int BUFFERSIZE = 1000;
	FileOutputStream[] dataParts = null;
	FileOutputStream[] codingParts = null;
	
	public Writer(int k, int m, int w, File original) {
		this.k = k;
		this.m = m;
		this.w = w;
		
		this.original = original;

		this.dataBuffer = new byte[k][w * this.BUFFERSIZE];
		this.codingBuffer = new byte[m][w * this.BUFFERSIZE];
		
		this.dataParts = createParts(original.getAbsolutePath(), "k", k);
		this.codingParts = createParts(original.getAbsolutePath(), "m", m);
	
	}
	
	public void write(byte[][] data, byte[][] coding) throws InterruptedException, IOException {

		for(int i = 0; i < k; i++){
			for(int j = 0; j < w; j++){
				this.dataBuffer[i][dataBufferPos + j] = data[i][j];
			}
		}
		
		for(int i = 0; i < m; i++){
			for(int j = 0; j < w; j++){
				this.codingBuffer[i][codingBufferPos + j] = coding[i][j];
			}
		}

		dataBufferPos += w;
		codingBufferPos += w;
		
		if(!this.isFull()) return;
		
		this.writeOut();

	}
	
	private void writeOut() throws IOException {

		for(int i = 0; i < k; i++){
			this.dataParts[i].write(this.dataBuffer[i]);
		}
		
		for(int i = 0; i < m; i++){
			this.codingParts[i].write(this.codingBuffer[i]);
		}

		dataBufferPos = 0;
		codingBufferPos = 0;
	}

	private boolean isFull() {
		return this.dataBufferPos >= this.BUFFERSIZE;
	}


	
	public void join() throws IOException {
		for(int i = 0; i < k; i++){
			this.dataParts[i].write(Arrays.copyOf(this.dataBuffer[i], dataBufferPos));
		}
		
		for(int i = 0; i < m; i++){
			this.codingParts[i].write(Arrays.copyOf(this.codingBuffer[i], codingBufferPos));
		}

		close(dataParts);
		close(codingParts);
	}

	
	
}
