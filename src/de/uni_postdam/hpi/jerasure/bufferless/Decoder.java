package de.uni_postdam.hpi.jerasure.bufferless;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.SortedMap;
import java.util.TreeMap;





import de.uni_postdam.hpi.cauchy.Cauchy;
import de.uni_postdam.hpi.jerasure.Buffer;
import de.uni_postdam.hpi.matrix.BitMatrix;
import de.uni_postdam.hpi.matrix.Schedule;
import de.uni_postdam.hpi.matrix.Schedule.OPERATION;
import de.uni_postdam.hpi.utils.FileUtils;

public class Decoder {

	int k,m,w;
	File destFile;
	File[] k_parts, m_parts;
	boolean[] erasures;
	long originalFileSize;
	private Schedule[] schedules;
	private int[] row_to_device_id;
	private int[] device_id_to_row;
	private int codingFailed;
	private int dataFailed;
	
	private byte[][] data;
	private boolean fileEndReached;
	
	public Decoder(File f, int k, int m, int w){
		this.destFile = f;
		this.k = k;
		this.m = m;
		this.w = w;
		
		this.k_parts = FileUtils.collectFiles(f.getAbsolutePath(), "k", k);
		this.m_parts = FileUtils.collectFiles(f.getAbsolutePath(), "m", m);
		
		this.data = new byte[k][w];

		updateErasures();
	}

	private void updateErasures() {
		this.erasures = new boolean[k + m];

		int c = 0;
		for (File part : k_parts) {
			erasures[c++] = !part.exists();
		}
		for (File part : m_parts) {
			erasures[c++] = !part.exists();
		}
		
	}

	public void decode(long origSize){

		this.originalFileSize = origSize;

		if (!all_k_parts_exist()) {
//			throw new RuntimeException("not implemented yet!");
			generateSchedules();
			restoreKParts();
		}
		decodeFromKParts();
	}

	private void decodeFromKParts() {

		RandomAccessFile f = null;
		SortedMap<Integer, FileInputStream> parts = null;
		try {
			parts = orderParts(k_parts, "k");
			f = new RandomAccessFile(this.destFile, "rw");
			f.setLength(originalFileSize);
			long bytesWritten = 0;
			int toWrite = 0;
			while (!fileEndReached) {
				this.read(parts);
				for(int id = 0; id < k; id ++){
					toWrite = (int) Math.min(data[id].length, originalFileSize - bytesWritten);
					f.write(data[id], 0, toWrite);
					bytesWritten += toWrite;
				}
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

	private int read(SortedMap<Integer, FileInputStream> parts) throws IOException {
		int read = 0, currRead;
		for(int id = 0; id < k && !this.fileEndReached; id++){
			currRead = parts.get(id).read(data[id]);
			this.fileEndReached = (currRead == -1);
			if(!this.fileEndReached) read += currRead;
		}
		return read;
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


	private void restoreKParts() {
		SortedMap<Integer, FileInputStream> parts = null;
		FileOutputStream[] missing_parts = null;

		try {
			parts = getExistingParts();
			missing_parts = getMissingParts();
			int bytesWritten = 0;
			int currRead = 0;
			
			do {
				int i = 0;
				for(FileInputStream fis: parts.values()){
					currRead = fis.read(data[i++]);

					if(currRead == -1) break;
					bytesWritten += currRead;
				}
				
				i = 0;
				for(FileOutputStream fos: missing_parts){
					if(fos == null) continue;
					fos.write(encode(data)[i++]);
				}

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
	

	private byte[][] encode(byte[][] data) {
		byte[][] coding = new byte[m][w];
		for(Schedule sched: this.schedules){
			if(sched.operation == OPERATION.COPY) {
				coding[sched.destinationId][sched.destinationBit] = data[sched.sourceId][sched.sourceBit];
			} else {
				coding[sched.destinationId][sched.destinationBit] ^= data[sched.sourceId][sched.sourceBit];
			}
		}
		return coding;
	}

	private void generateSchedules() {
		BitMatrix decMatrix = this.generate_decoding_bitmatrix();
		this.schedules = decMatrix.toSchedules(k, w);
		
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

	private boolean deviceOK(int i) {
		return row_to_device_id[i] == i;
	}

	private int row_to_coding_id(int i) {
		return row_to_device_id[i] - k;
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
		

	private boolean all_k_parts_exist() {
		for (File part : k_parts) {
			if (!part.exists())
				return false;
		}
		return true;
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


}
