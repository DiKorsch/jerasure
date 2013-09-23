package de.uni_postdam.hpi.jerasure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import de.uni_postdam.hpi.matrix.Schedule;
import de.uni_postdam.hpi.utils.FileUtils;

public class JErasure {

	private static int calcPacketSize(int k, int w, long filesize) {
		int packetsize = (int) (filesize / (k * w * 256));
		packetsize = packetsize + 1;
		return packetsize;
	}

	private static long calcNewSize(long origSize, int k, int m) {
		return (origSize + (origSize * m / k));
	}

	private static int calcBufferSize(int k, int w, int packetSize, long size) {
		int factor = (int) (size / (k * w * packetSize));
		int calcFactor = factor;
		int i;
		for (i = factor - 1; i > 1; i--) {
			if (factor % i == 0) {
				calcFactor = i;
				break;
			}
		}
		if (calcFactor == 0) {
			return (int) size;
		}
		return calcFactor * k * w * packetSize;
	}

	public static byte[] encode(int k, int m, int w, Schedule[] schedules,
			byte[] data, int packetSize) {
		if (data.length < k * w * packetSize) {
			data = Arrays.copyOf(data, k * w * packetSize);
		}

		byte[] dataAndCoding = new byte[(int) calcNewSize(data.length, k, m)];

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
			int packetSize = calcPacketSize(k, w, size);
			int bufferSize = calcBufferSize(k, w, packetSize, size);
			int blockSize = k * w * packetSize;
			k_parts = createParts(file.getAbsolutePath(), "k", k);
			m_parts = createParts(file.getAbsolutePath(), "m", m);

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
			close(k_parts);
			close(m_parts);
		}

	}

	public static int calcOverHead(long size, int k, int w){
		int bufferSize, packetSize, blockSize;
		
		packetSize = calcPacketSize(k, w, size);
		bufferSize = calcBufferSize(k, w, packetSize, size);
		blockSize = k * w * packetSize;
		
		// last bytes, which have to be padded with zeros
		int rest = (int) ((size % bufferSize) % blockSize);
		
		return blockSize - rest;
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
		byte[] dataAndCoding = JErasure.encode(k, m, w, schedules, data,
				packetSize);
		writeParts(dataAndCoding, k_parts, m_parts, w, packetSize);
	}

	private static void writeParts(byte[] dataAndCoding,
			FileOutputStream[] k_parts, FileOutputStream[] m_parts, int w,
			int packetSize) throws IOException {
		if (k_parts == null || m_parts == null) {
			throw new IllegalArgumentException(
					"one of the parts(or both) arrays was null: k=" + k_parts
							+ " m=" + m_parts);
		}

		int k = k_parts.length;
		int m = m_parts.length;

		for (int i = 0; i < k; i++) {
			write(i, k_parts[i], dataAndCoding, w, packetSize);
		}

		for (int i = 0; i < m; i++) {
			write(i + k, m_parts[i], dataAndCoding, w, packetSize);
		}
	}

	private static void write(int idx, FileOutputStream destenation,
			byte[] dataAndCoding, int w, int packetSize) throws IOException {
		int start = idx * w * packetSize;
		destenation.write(dataAndCoding, start, w * packetSize);
	}

	private static void close(FileOutputStream[] parts) {
		if (parts == null)
			return;
		for (FileOutputStream fos : parts) {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.err.println("output stream was null!");
			}
		}
	}

	private static FileOutputStream[] createParts(String filePath,
			String suffix, int numParts) {
		FileOutputStream[] result = new FileOutputStream[numParts];
		for (int i = 0; i < numParts; i++) {
			String partName = FileUtils.generatePartPath(filePath, suffix, i+1);
			File part = new File(partName);
			try {
				if (part.exists()) {
					part.delete();
				}
				if (part.createNewFile()) {
					result[i] = new FileOutputStream(part);
				} else {
					throw new RuntimeException("part " + part.getAbsolutePath()
							+ " could not be created!");
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("an error occured!");
			}
		}
		return result;
	}

}