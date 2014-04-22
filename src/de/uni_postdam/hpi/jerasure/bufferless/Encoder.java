package de.uni_postdam.hpi.jerasure.bufferless;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.google.common.io.Files;

import de.uni_postdam.hpi.cauchy.Cauchy;
import de.uni_postdam.hpi.jerasure.bufferless.Decoder;
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

	int numReads = 0;

	public Encoder(int k, int m, int w) {
		this.k = k;
		this.m = m;
		this.w = w;

		this.matrix = Cauchy.good_general_coding_matrix(k, m, w);
		this.bitMatrix = new BitMatrix(matrix, w);
		this.schedules = bitMatrix.toSchedules(k, w);
	}
	
	
	public void encode(File f) throws IOException, InterruptedException {

		f = f.getAbsoluteFile();
		FileInputStream fis = new FileInputStream(f);

		this.numReads = (int)(f.length() / k / w + 1);
		
		long t1 = 0, read = 0, encoding = 0, write = 0;

		Reader reader = new Reader(k, m, w, fis);
		Writer writer = new Writer(k, m, w, f);
		byte[][] data;
		while(reader.next()){
			t1 = System.currentTimeMillis();
			data = reader.get();
			read += System.currentTimeMillis() - t1;
			
			
			t1 = System.currentTimeMillis();
			byte[][] coding = encode(data);
			encoding += System.currentTimeMillis() - t1;
			
			if(numReads-- <= 0){ break; }

			t1 = System.currentTimeMillis();
			writer.write(data, coding);
			
			write += System.currentTimeMillis() - t1;
		}
		
		t1 = System.currentTimeMillis();
		writer.join();
		write += System.currentTimeMillis() - t1;
		
		fis.close();
		
		
		System.out.println(String.format("%d\t%d\t%d\t%d", f.length(), read, encoding, write));
		
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


	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InterruptedException {
		int k = 2, m = 1, w = 3;
		File f = new File("lorem");
		Encoder enc = new Encoder(k, m, w);
		
		for(int i = 1; i < 11; i++){
			createRandomContentFile(f, i * 1 * MB);
			enc.encode(f);
		}
		
//		Files.copy(f, orig);
		
//		Decoder dec = new Decoder(f, k, m, w);
//		dec.decode(size);
		
//		if(!getMD5Hash(orig).equals(getMD5Hash(f))){
//			System.err.println("en/decoding does not work!");
//		} else {
//			System.out.println("Done!");
//		}
			
		
		System.out.println("ready!");
	}
}
