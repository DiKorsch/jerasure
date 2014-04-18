package de.uni_postdam.hpi.jerasure.bufferless;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.google.common.io.Files;

import de.uni_postdam.hpi.cauchy.Cauchy;
import de.uni_postdam.hpi.jerasure.Decoder;
import de.uni_postdam.hpi.matrix.BitMatrix;
import de.uni_postdam.hpi.matrix.Matrix;
import de.uni_postdam.hpi.matrix.Schedule;
import de.uni_postdam.hpi.matrix.Schedule.OPERATION;


import static de.uni_postdam.hpi.utils.FileUtils.*;

public class Encoder {
	int k, m, w;

	Matrix matrix = null;
	BitMatrix bitMatrix = null;
	Schedule[] schedules = null;

	private boolean fileEndReached;
	
	byte[][] data = null;
	int numReads = 0;

	public Encoder(int k, int m, int w) {
		this.k = k;
		this.m = m;
		this.w = w;

		this.matrix = Cauchy.good_general_coding_matrix(k, m, w);
		this.bitMatrix = new BitMatrix(matrix, w);
		this.schedules = bitMatrix.toSchedules(k, w);
	}
	
	
	public void encode(File f) throws IOException {

		f = f.getAbsoluteFile();
		FileInputStream fis = new FileInputStream(f);

		this.numReads = (int)(f.length() / k / w + 1);
		
		FileOutputStream[] dataParts = createParts(f.getAbsolutePath(), "k", k);
		FileOutputStream[] codingParts = createParts(f.getAbsolutePath(), "m", m);

		while(!encodeAndWrite(fis, codingParts, dataParts));

		close(dataParts);
		close(codingParts);
		
	}


	private boolean encodeAndWrite(FileInputStream fis, FileOutputStream[] codingParts, FileOutputStream[] dataParts) throws IOException {
		int bytesRead = this.read(fis);
		boolean res = write(codingParts, dataParts, bytesRead);
		return res;
	}


	private boolean write(FileOutputStream[] codingParts, FileOutputStream[] dataParts, int bytesRead) throws IOException {

//		System.out.println(String.format("Run %d: %d/%d", ++run, remains, length));
		
		if(numReads > 0){
			for(int i = 0; i < k; i++)
				dataParts[i].write(data[i]);
	
			for(int i = 0; i < m; i++)
				codingParts[i].write(encode(data)[i]);
			numReads--;
		}
		
		return fileEndReached;
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


	private int read(FileInputStream fis) throws IOException {
		data = new byte[k][w];
		int read = 0;
		for(int id = 0; id < k && !this.fileEndReached; id++){
			read += fis.read(data[id]);
			this.fileEndReached = (read == -1);
		}
		return read;
	}
	
	
	
	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		int k = 2, m = 1, w = 3;
		File f = new File("lorem");
		File orig = new File("orig");
		long size = 256 * BYTE;
		Encoder enc = new Encoder(k, m, w);
		createRandomContentFile(f, size);
		enc.encode(f);
		
		Files.copy(f, orig);
		
		Decoder dec = new Decoder(f, k, m, w);
		dec.decode(size);
		
		if(!getMD5Hash(orig).equals(getMD5Hash(f))){
			System.err.println("en/decoding does not work!");
		} else {
			System.out.println("Done!");
		}
			
		
		System.out.println("ready!");
	}
}
