package de.uni_postdam.hpi.jerasure.bufferless;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.uni_postdam.hpi.cauchy.Cauchy;
import de.uni_postdam.hpi.matrix.BitMatrix;
import de.uni_postdam.hpi.matrix.Matrix;
import de.uni_postdam.hpi.matrix.Schedule;
import de.uni_postdam.hpi.matrix.Schedule.OPERATION;

public class Encoder {
	int k, m, w;

	Matrix matrix = null;
	BitMatrix bitMatrix = null;
	Schedule[] schedules = null;

	private boolean fileEndReached;


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

		FileOutputStream[] dataParts = this.getParts(f, k, "k");
		FileOutputStream[] codingParts = this.getParts(f, m, "m");

		while(!encodeAndWrite(this.read(fis), codingParts, dataParts));

		this.closeParts(dataParts);
		this.closeParts(codingParts);
		
	}

	private void closeParts(FileOutputStream[] parts) throws IOException {
		for(FileOutputStream part: parts) part.close();
	}


	private FileOutputStream[] getParts(File f, int numParts, String suffix) throws FileNotFoundException {
		FileOutputStream[] res = new FileOutputStream[numParts];
		for(int i = 0; i < numParts; i++){
			res[i] = new FileOutputStream(this.getPartName(f, i+1, suffix));
		}
		return res;
	}

	private String getPartName(File f, int num, String suffix){
		String filePath = f.getParentFile().getAbsolutePath();
		String fileName = f.getName();
		return String.format("%s/%s_%s%02d", filePath, fileName, suffix, num);
		
	}
	
	private boolean encodeAndWrite(byte[][] data, FileOutputStream[] codingParts, FileOutputStream[] dataParts) throws IOException {
		
		return write(data, encode(data), codingParts, dataParts);
	}


	private boolean write(byte[][] data, byte[][] coding, FileOutputStream[] codingParts, FileOutputStream[] dataParts) throws IOException {
		for(int i = 0; i < k; i++)
			dataParts[i].write(data[i]);

		for(int i = 0; i < m; i++)
			codingParts[i].write(coding[i]);
		
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


	private byte[][] read(FileInputStream fis) throws IOException {
		byte[][] data = new byte[k][w];
		for(int id = 0; id < k && !this.fileEndReached; id++){
			this.fileEndReached = (fis.read(data[id]) == -1);
		}
		return data;
	}
	
	
	
	
	public static void main(String[] args) {
		Encoder enc = new Encoder(2, 1, 3);
		
		try {
			enc.encode(new File("lorem"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("ready!");
	}
}
