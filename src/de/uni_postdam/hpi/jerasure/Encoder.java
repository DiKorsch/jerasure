package de.uni_postdam.hpi.jerasure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.uni_postdam.hpi.cauchy.Cauchy;
import de.uni_postdam.hpi.matrix.*;
import de.uni_postdam.hpi.utils.CalcUtils;
import de.uni_postdam.hpi.utils.CodingUtils;
import de.uni_postdam.hpi.utils.FileUtils;

public class Encoder {

	int k, m, w;

	Matrix matrix = null;
	BitMatrix bitMatrix = null;
	Schedule[] schedules = null;

	int blockSize, bufferSize, packetSize, codingBlockSize;
	
	static int numThreads = 4;
	EncoderThread[] threads = null;

	public Encoder(int k, int m, int w) {
		this.k = k;
		this.m = m;
		this.w = w;

		this.matrix = Cauchy.good_general_coding_matrix(k, m, w);
		this.bitMatrix = new BitMatrix(matrix, w);
		this.schedules = bitMatrix.toSchedules(k, w);
		
		this.threads = new EncoderThread[numThreads];
	}

	public byte[] encode(byte[] data, int packetSize) {
		return CodingUtils.enOrDecode(data, schedules, k, m, w, packetSize);
	}

	public byte[] encode(Buffer data, int packetSize) {
		return CodingUtils.enOrDecode(data, schedules, k, m, w, packetSize);
	}
	
	
	public void encode(Buffer data, Buffer coding, int packetSize) {
		data.setLen(k * packetSize * w);
		coding.setLen(m * packetSize * w);
		Schedule.do_scheduled_operations(data, coding, schedules, packetSize, w);
	}
	
	public void encode(Buffer data, Buffer coding){
		this.encode(data, coding, packetSize);
	}
	

	public void encode(byte[] data, int startData, byte[] coding, int startCoding) {
		Schedule.do_scheduled_operations(data, startData, coding, startCoding, schedules, packetSize, w);
	}

	private void calcSizes(long size) {
		packetSize = CalcUtils.calcPacketSize(k, w, size);
		bufferSize = CalcUtils.calcBufferSize(k, w, packetSize, size);
		blockSize = CalcUtils.calcBlockSize(k, w, packetSize);
		codingBlockSize = CalcUtils.calcCodingBlockSize(m, w, packetSize);
	}


	
	public void encode(File file) {
		if (!file.exists()) {
			throw new IllegalArgumentException("File " + file.getAbsolutePath()
					+ " does not exist!");
		}
		FileInputStream fis = null;
		FileOutputStream[] k_parts = null;
		FileOutputStream[] m_parts = null;
		
		try {
			fis = new FileInputStream(file);
			calcSizes(file.length());
			k_parts = FileUtils.createParts(file.getAbsolutePath(), "k", k);
			m_parts = FileUtils.createParts(file.getAbsolutePath(), "m", m);
			Buffer data = new Buffer(bufferSize);
			Buffer coding = new Buffer(bufferSize / k * m);
			int numRead = 0;

			while ((numRead = data.readFromStream(fis)) >= 0) {
				data.reset();
				coding.reset();
				
				performEncoding(data, coding);
				// encode last blocks
				if (bufferSize != numRead) {
					performLastReadEncoding(data, coding, numRead);
				}

				data.setStart(0);
				coding.setStart(0);
				
				FileUtils.writeParts(data, coding, k_parts, m_parts, w, packetSize);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			FileUtils.close(k_parts);
			FileUtils.close(m_parts);
		}

	}

	private void performLastReadEncoding(Buffer data, Buffer coding, int numRead) {

		data.setRange(numRead / blockSize * blockSize, numRead % blockSize);
		coding.setRange(numRead / blockSize * codingBlockSize, numRead % blockSize);
		
		encode(data, coding);		
	}

	private void performEncoding(Buffer data, Buffer coding) {
		int steps = bufferSize / blockSize;
		if(steps == 0){return;}
		int stepsProThread = steps / numThreads;
		if ( steps % numThreads != 0) stepsProThread++;
		int c = 0;
		for (int blockId = 0; blockId < steps; blockId++) {
			data.setRange(blockId * blockSize, blockSize);
			coding.setRange(blockId * codingBlockSize, codingBlockSize);
			
			if(blockId % stepsProThread == 0){
				threads[c++] = new EncoderThread(data, coding, this);
			} else {
				threads[c-1].addRange(data.getStart(), coding.getStart());
			}
			
		}
		start(threads);
		wait_(threads);
	}

	private void wait_(EncoderThread[] threads2){
		for(Thread t: threads){
			if(t == null) continue;
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void start(EncoderThread[] threads) {
		for(Thread t: threads){
			if(t != null ){
				t.start();
			}
		}
		
	}

}
