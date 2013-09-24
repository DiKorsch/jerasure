package de.uni_postdam.hpi.jerasure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import de.uni_postdam.hpi.matrix.Schedule;
import de.uni_postdam.hpi.utils.FileUtils;

public class Encoder {
	
	public static byte[] encode(int k, int m, int w, Schedule[] schedules,
			byte[] data, int packetSize) {
		if (data.length < k * w * packetSize) {
			data = Arrays.copyOf(data, k * w * packetSize);
		}

		byte[] dataAndCoding = new byte[(int) CalcUtils.calcNewSize(data.length, k, m)];

		for (int i = 0; i < k * packetSize * w; i++) {
			dataAndCoding[i] = data[i];
		}
		for (int i = 0; i < m * packetSize * w; i++) {
			dataAndCoding[i + k * packetSize * w] = 0x00; // coding part
		}

		for (int done = 0; done < data.length; done += packetSize * w) {
			for (Schedule sched : schedules) {
				dataAndCoding = sched.operate(dataAndCoding, packetSize, w);
			}
		}

		return dataAndCoding;
	}

	public static void encode(File file, int k, int m, int w) {
		System.out.println("Encoding " + file.getAbsolutePath());
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
			int packetSize = CalcUtils.calcPacketSize(k, w, size);
			int bufferSize = CalcUtils.calcBufferSize(k, w, packetSize, size);
			int blockSize = k * w * packetSize;
			k_parts = FileUtils.createParts(file.getAbsolutePath(), "k", k);
			m_parts = FileUtils.createParts(file.getAbsolutePath(), "m", m);

//			System.out.println("Packet size: " + packetSize);
//			System.out.println("Buffer size: " + bufferSize);

			byte[] buffer = new byte[bufferSize];
			int numRead = 0;

			Schedule[] schedules = Schedule.generate(k, m, w);
			while ((numRead = fis.read(buffer)) >= 0) {
				if (buffer.length != numRead) {
					buffer = Arrays.copyOfRange(buffer, 0, numRead);
					performLastReadEncoding(buffer, blockSize, packetSize,
							k_parts, m_parts, w, schedules);
				} else {
					performNormalEncoding(buffer, blockSize, packetSize,
							k_parts, m_parts, w, schedules);
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

	private static void performLastReadEncoding(byte[] buffer, int blockSize,
			int packetSize, FileOutputStream[] k_parts,
			FileOutputStream[] m_parts, int w, Schedule[] schedules)
			throws IOException {

		performNormalEncoding(buffer, blockSize, packetSize, k_parts, m_parts,
				w, schedules);
		int start = buffer.length / blockSize;
		int length = buffer.length % blockSize;
		encodeAndWrite(Arrays.copyOfRange(buffer, start, length), schedules,
				packetSize, w, k_parts, m_parts);

	}

	private static void performNormalEncoding(byte[] buffer, int blockSize,
			int packetSize, FileOutputStream[] k_parts,
			FileOutputStream[] m_parts, int w, Schedule[] schedules)
			throws IOException {

		for (int i = 0; i < buffer.length / blockSize; i++) {
			int start = i * blockSize;
			encodeAndWrite(
					Arrays.copyOfRange(buffer, start, start + blockSize),
					schedules, packetSize, w, k_parts, m_parts);
		}

	}

	private static void encodeAndWrite(byte[] data, Schedule[] schedules,
			int packetSize, int w, FileOutputStream[] k_parts,
			FileOutputStream[] m_parts) throws IOException {
		int k = k_parts.length, m = m_parts.length;
		byte[] dataAndCoding = Encoder.encode(k, m, w, schedules, data,
				packetSize);
		FileUtils.writeParts(dataAndCoding, k_parts, m_parts, w, packetSize);
	}

}
