package de.uni_postdam.hpi.jerasure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

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
	
	int blockSize, bufferSize, packetSize;
	
	public Encoder(int k, int m, int w) {
		this.k = k;
		this.m = m;
		this.w = w;
		
		this.matrix = Cauchy.good_general_coding_matrix(k, m, w);
		this.bitMatrix = new BitMatrix(matrix, w);
		this.schedules = bitMatrix.toSchedules(k, w);
	}
	
	public byte[] encode(byte[] data, int packetSize) {
		return CodingUtils.enOrDecode(data, schedules, k, m, w, packetSize); 
	}
	
	private void calcSizes(long size){
		packetSize = CalcUtils.calcPacketSize(k, w, size);
		bufferSize = CalcUtils.calcBufferSize(k, w, packetSize, size);
		blockSize = CalcUtils.calcBlockSize(k, w, packetSize);
	}

	public void encode(File file) {
		if (!file.exists()) {
			System.err.println("File " + file.getAbsolutePath()
					+ " does not exist!");
			return;
		}
		FileInputStream fis = null;
		FileOutputStream[] k_parts = null;
		FileOutputStream[] m_parts = null;

		try {
			fis = new FileInputStream(file);
			long size = file.length();
			calcSizes(size);
			k_parts = FileUtils.createParts(file.getAbsolutePath(), "k", k);
			m_parts = FileUtils.createParts(file.getAbsolutePath(), "m", m);

			byte[] buffer = new byte[bufferSize];
			int numRead = 0;

			Schedule[] schedules = Schedule.generate(k, m, w);
			while ((numRead = fis.read(buffer)) >= 0) {
				if (bufferSize != numRead) {
					buffer = Arrays.copyOfRange(buffer, 0, numRead);
					performLastReadEncoding(buffer, k_parts, m_parts, w, schedules);
				} else {
					performNormalEncoding(buffer, k_parts, m_parts, w, schedules);
				}
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

	private void performLastReadEncoding(byte[] buffer, FileOutputStream[] k_parts,
			FileOutputStream[] m_parts, int w, Schedule[] schedules)
			throws IOException {

		performNormalEncoding(buffer, k_parts, m_parts,	w, schedules);
		int start = buffer.length / blockSize;
		int length = buffer.length % blockSize;
		encodeAndWrite(Arrays.copyOfRange(buffer, start, length), k_parts, m_parts);

	}

	private void performNormalEncoding(byte[] buffer, FileOutputStream[] k_parts,
			FileOutputStream[] m_parts, int w, Schedule[] schedules)
			throws IOException {

		for (int i = 0; i < buffer.length / blockSize; i++) {
			encodeAndWrite(
					Arrays.copyOfRange(buffer, i * blockSize, (i+1)*blockSize), k_parts, m_parts);
		}

	}

	private void encodeAndWrite(byte[] data, FileOutputStream[] k_parts,
			FileOutputStream[] m_parts) throws IOException {
		byte[] dataAndCoding = encode(data, packetSize);
		FileUtils.writeParts(dataAndCoding, k_parts, m_parts, w, packetSize);
	}

}
