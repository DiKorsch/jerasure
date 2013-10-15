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

	public byte[] encode(Buffer data, int packetSize) {
		return CodingUtils.enOrDecode(data, schedules, k, m, w, packetSize);
	}

	private void calcSizes(long size) {
		packetSize = CalcUtils.calcPacketSize(k, w, size);
		bufferSize = CalcUtils.calcBufferSize(k, w, packetSize, size);
		blockSize = CalcUtils.calcBlockSize(k, w, packetSize);
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

//			Buffer newBuffer = new Buffer(bufferSize);
			byte[] buffer = new byte[bufferSize];
			int numRead = 0;

			Schedule[] schedules = Schedule.generate(k, m, w);
			while ((numRead = fis.read(buffer)/*newBuffer.readFromStream(fis)*/) >= 0) {
				if (bufferSize != numRead) {
//					newBuffer.setEnd(numRead);
					buffer = Arrays.copyOf(buffer, numRead);
					performLastReadEncoding(buffer, k_parts, m_parts, w,
							schedules);
				} else {
					performEncoding(buffer, k_parts, m_parts, w, schedules);
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

	private void performEncoding(byte[] buffer, FileOutputStream[] k_parts,
			FileOutputStream[] m_parts, int w2, Schedule[] schedules2) throws IOException {
		for (int i = 0; i < buffer.length / blockSize; i++) {
			encodeAndWrite(Arrays.copyOfRange(buffer, i * blockSize, (i + 1) * blockSize), k_parts, m_parts);
		}		
	}


	private void performLastReadEncoding(byte[] buffer,
			FileOutputStream[] k_parts, FileOutputStream[] m_parts, int w2,
			Schedule[] schedules2) throws IOException {
			performEncoding(buffer, k_parts, m_parts, w, schedules);
			int start = (buffer.length / blockSize) * blockSize;
			int end = start + buffer.length % blockSize;
			if (start == 0) {
				encodeAndWrite(buffer, k_parts, m_parts);
			} else {
				encodeAndWrite(Arrays.copyOfRange(buffer, start, end), k_parts, m_parts);
			}		
	}

	private void performEncoding(Buffer buffer, FileOutputStream[] k_parts,
			FileOutputStream[] m_parts, int w, Schedule[] schedules) throws IOException {
		for (int i = 0; i < buffer.size() / blockSize; i++) {
			buffer.setStart(i * blockSize);
			buffer.setEnd((i + 1) * blockSize);
			encodeAndWrite(buffer, k_parts, m_parts);
		}

	}

	private void performLastReadEncoding(Buffer buffer,
			FileOutputStream[] k_parts, FileOutputStream[] m_parts, int w,
			Schedule[] schedules) throws IOException {
		performEncoding(buffer, k_parts, m_parts, w, schedules);
		int start = (buffer.size() / blockSize) * blockSize;
		int end = start + buffer.size() % blockSize;
		if (start == 0) {
			encodeAndWrite(buffer, k_parts, m_parts);
		} else {
			buffer.setStart(start);
			buffer.setEnd(end);
			encodeAndWrite(buffer, k_parts, m_parts);
		}
	}

	private void encodeAndWrite(Buffer data, FileOutputStream[] k_parts,
			FileOutputStream[] m_parts) throws IOException {
		byte[] coding = encode(data, packetSize);
		FileUtils.writeParts(data, coding, k_parts, m_parts, w, packetSize);
	}

	private void encodeAndWrite(byte[] data, FileOutputStream[] k_parts,
			FileOutputStream[] m_parts) throws IOException {

		int blockSize = CalcUtils.calcBlockSize(k, w, packetSize);
		data = CodingUtils.addPaddingIfNeeded(data, blockSize);
		byte[] coding = encode(data, packetSize);
		FileUtils.writeParts(data, coding, k_parts, m_parts, w, packetSize);

	}
}
