package de.uni_postdam.hpi.jerasure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.SortedMap;
import java.util.TreeMap;

import static de.uni_postdam.hpi.utils.CalcUtils.*;
import de.uni_postdam.hpi.cauchy.Cauchy;
import de.uni_postdam.hpi.matrix.*;
import de.uni_postdam.hpi.utils.CodingUtils;
import de.uni_postdam.hpi.utils.FileUtils;

public class Decoder {

	File original = null;
	int k, m, w;

	static int numThreads = Runtime.getRuntime().availableProcessors();//*/20;
	File[] k_parts, m_parts;

	boolean[] erasures = null;
	int[] row_to_device_id = null;
	int[] device_id_to_row = null;

	int dataFailed = 0, codingFailed = 0;

	Schedule[] schedules = null;

	int packetSize = 0, bufferSize = 0, blockSize = 0, codingBlockSize = 0;

	long originalFileSize = 0;

	DecoderThread[] threads;

	public Decoder(File file, int k, int m, int w) {
		this.original = file;
		this.k = k;
		this.m = m;
		this.w = w;

		this.k_parts = FileUtils.collectFiles(file.getAbsolutePath(), "k", k);
		this.m_parts = FileUtils.collectFiles(file.getAbsolutePath(), "m", m);
		this.erasures = new boolean[k + m];

		updateErasures();

		this.threads = new DecoderThread[numThreads];
	}

	public void decode(byte[] data, int startData, byte[] coding, int startCoding) {
		Schedule.do_scheduled_operations(data, startData, coding, startCoding,
				schedules, packetSize, w);
	}

	public void decode(long size) {
		if (!isValid()) {
			throw new RuntimeException(
					"Not enough parts to decode the original!");
		}
		this.originalFileSize = size;
		calcSizes();
		if (!all_k_parts_exist()) {
			generateSchedules();
			restoreKParts();
		}
		decodeFromKParts();
	}

	public byte[] decode(byte[] data, int packetSize) {
		if (schedules == null) {
			generateSchedules();
		}
		return CodingUtils.enOrDecode(data, schedules, k, m, w, packetSize);
	}

	public byte[] decode(Buffer data, int packetSize) {
		if (schedules == null) {
			generateSchedules();
		}
		return CodingUtils.enOrDecode(data, schedules, k, m, w, packetSize);
	}

	private void updateErasures() {
		if (erasures == null)
			throw new RuntimeException("erasures was null!");

		int c = 0;
		for (File part : k_parts) {
			erasures[c++] = !part.exists();
		}
		for (File part : m_parts) {
			erasures[c++] = !part.exists();
		}
	}

	private void generateSchedules() {
		BitMatrix decMatrix = this.generate_decoding_bitmatrix();
		schedules = decMatrix.toSchedules(k, w);
	}

	public boolean isValid() {
		updateErasures();
		int c = 0;
		for (boolean erased : erasures) {
			if (erased)
				c++;
		}

		return c <= this.m;
	}

	private boolean all_k_parts_exist() {
		for (File part : k_parts) {
			if (!part.exists())
				return false;
		}
		return true;
	}

	private void calcSizes() {
		packetSize = calcPacketSize(k, w, originalFileSize);
		blockSize = calcBlockSize(k, w, packetSize);
		codingBlockSize = calcBlockSize(m, w, packetSize);
		bufferSize = calcBufferSize(k, w, packetSize, originalFileSize);
	}

	private void restoreKParts() {
		SortedMap<Integer, FileInputStream> parts = null;
		FileOutputStream[] missing_parts = null;

		try {
			parts = getExistingParts();
			missing_parts = getMissingParts();
			int bytesWritten = 0;
			Buffer data = new Buffer(bufferSize);
			Buffer coding = new Buffer(bufferSize / k * m);
			int currRead = 0;
			do {
				coding.reset();
				data.reset();

				currRead = FileUtils.readParts(data, parts, w, packetSize);

				performDecoding(data, coding, missing_parts);
				
				coding.setStart(0);
				FileUtils.writeParts(coding, missing_parts, w, packetSize);
				
				if(currRead == -1) break;
				bytesWritten += currRead;
			} while(bytesWritten < originalFileSize);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeStreams(parts);
			FileUtils.close(missing_parts);
		}

	}

	private void decode(Buffer data, Buffer coding, int packetSize) {
		data.setLen(k * packetSize * w);
		coding.setLen(m * packetSize * w);
		Schedule.do_scheduled_operations(data, coding, schedules, packetSize, w);
	}

	private void decode(Buffer data, Buffer coding) {
		decode(data, coding, packetSize);
	}

	private void performDecoding(Buffer data, Buffer coding,
			FileOutputStream[] missing_parts) throws IOException {
		int bufferSize = data.size();
		int blocks = bufferSize / blockSize;
		if (blocks == 1) {
			decode(data, coding);
		} else {
			int stepsProThread = blocks / numThreads;
			if (blocks % numThreads != 0)
				stepsProThread++;
			int c = 0;
			for (int blockId = 0; blockId < blocks; blockId++) {
				data.setStart(blockId * k * w * packetSize);
				coding.setStart(blockId * m * w * packetSize);

				if (blockId % stepsProThread == 0) {
					threads[c++] = new DecoderThread(data, coding, this);
				} else {
					threads[c-1].addRange(data.getStart(), coding.getStart());
				}
			}
			while (c < threads.length) {
				threads[c++] = null;
			}
			start(threads);
			wait_(threads);
		}
	}

	private void wait_(DecoderThread[] threads) {
		for (Thread t : threads) {
			if (t == null)
				continue;
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void start(DecoderThread[] threads) {
		for (Thread t : threads) {
			if (t != null) {
				t.start();
			}
		}
	}

	private void decodeFromKParts() {

		RandomAccessFile f = null;
		Buffer data = new Buffer(bufferSize);
		SortedMap<Integer, FileInputStream> parts = null;
		try {
			parts = orderParts(k_parts, "k");
			f = new RandomAccessFile(original, "rw");
			f.setLength(originalFileSize);
			int bytesWritten = 0, read = 0;
			while (bytesWritten < originalFileSize) {
				read = FileUtils.readParts(data, parts, w, packetSize);
				int bytesToWrite = (int) Math.min(data.size(), originalFileSize - bytesWritten);
				data.writeToStream(f, bytesToWrite);
				if(read == -1) break;
				bytesWritten += bytesToWrite;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (f != null) {
					f.close();
				}
				closeStreams(parts);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private SortedMap<Integer, FileInputStream> orderParts(File[] parts,
			String partSuffix) throws FileNotFoundException {
		SortedMap<Integer, FileInputStream> result = new TreeMap<Integer, FileInputStream>();
		for (File part : parts) {
			result.put(FileUtils.extractNum(part.getName(), partSuffix) - 1,
					new FileInputStream(part));
		}
		return result;
	}

	private SortedMap<Integer, FileInputStream> getExistingParts()
			throws FileNotFoundException {
		SortedMap<Integer, FileInputStream> allParts = new TreeMap<Integer, FileInputStream>();
		int c = 0;
		for (int deviceId : row_to_device_id) {
			File currFile = deviceId < k ? k_parts[deviceId] : m_parts[deviceId
					- k];
			if (!currFile.exists())
				continue;
			allParts.put(c++, new FileInputStream(currFile));
		}
		return allParts;
	}

	private FileOutputStream[] getMissingParts() throws FileNotFoundException {
		FileOutputStream[] missing = new FileOutputStream[m];
		int c = 0;
		for (int deviceId : row_to_device_id) {
			File currFile = deviceId < k ? k_parts[deviceId] : m_parts[deviceId
					- k];
			if (currFile.exists())
				continue;
			missing[c++] = new FileOutputStream(currFile);
			if (c == dataFailed) {
				break;
			}
		}
		while (c < m) {
			missing[c++] = null;
		}
		return missing;
	}

	private void closeStreams(SortedMap<Integer, FileInputStream> parts) {
		try {
			if (parts != null) {
				for (FileInputStream part : parts.values()) {
					if (part != null)
						part.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public BitMatrix generate_decoding_bitmatrix() {

		updateErasures();
		update_erased_ids();

		BitMatrix encodingMatrix = new BitMatrix(
				Cauchy.good_general_coding_matrix(k, m, w), w);
		BitMatrix result = new BitMatrix(k, codingFailed + dataFailed, w);

		if (dataFailed > 0) {
			BitMatrix decoding_matrix = new BitMatrix(k, k, w);
			decoding_matrix.toIdentity();
			for (int dataDeviceId = 0; dataDeviceId < k; dataDeviceId++) {
				if (!deviceOK(dataDeviceId)) {
					decoding_matrix.copyRows(dataDeviceId * w, encodingMatrix,
							row_to_coding_id(dataDeviceId) * w, w);
				}
			}

			BitMatrix inverse = decoding_matrix.invert(w);

			for (int deviceId = 0; deviceId < dataFailed; deviceId++) {
				result.copyRows(deviceId * w, inverse,
						row_to_device_id[deviceId + k] * w, w);
			}
		}

		for (int x = dataFailed; x < codingFailed + dataFailed; x++) {
			int codingId = row_to_coding_id(x + k);
			int currRow = x * w;
			result.copyRows(currRow, encodingMatrix, codingId * w, w);

			for (int dataDeviceId = 0; dataDeviceId < k; dataDeviceId++) {
				if (!deviceOK(dataDeviceId)) {
					result.zero(dataDeviceId * w, currRow, w, w);
				}
			}

			/* There's the yucky part */
			for (int dataId = 0; dataId < k; dataId++) {
				if (deviceOK(dataId)) {
					continue;
				}
				result.do_yucky_decoding_stuff(encodingMatrix, currRow,
						device_id_to_row[dataId] - k, dataId, codingId);
			}
		}

		return result;
	}

	private int row_to_coding_id(int i) {
		return row_to_device_id[i] - k;
	}

	private boolean deviceOK(int i) {
		return row_to_device_id[i] == i;
	}

	private void update_erased_ids() {
		row_to_device_id = new int[k + m];
		device_id_to_row = new int[k + m];
		codingFailed = 0;
		dataFailed = 0;

		int j = k, x = k;
		for (int i = 0; i < k; i++) {
			if (!erasures[i]) {
				row_to_device_id[i] = i;
				device_id_to_row[i] = i;
			} else {
				while (erasures[j]) {
					if (++j == erasures.length) {
						throw new RuntimeException(
								"Not enough redundant parts!");
					}
				}
				dataFailed++;
				row_to_device_id[i] = j;
				device_id_to_row[j] = i;
				j++;
				device_id_to_row[i] = x;
				row_to_device_id[x] = i;
				x++;
			}
		}
		for (int i = k; i < k + m; i++) {
			if (erasures[i]) {
				codingFailed++;
				row_to_device_id[x] = i;
				device_id_to_row[i] = x;
				x++;
			}
		}
	}
}
