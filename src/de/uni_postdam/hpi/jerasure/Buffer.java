package de.uni_postdam.hpi.jerasure;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class Buffer implements Iterable<Byte> {

	private byte[] data = null;
	private int start = 0;
	private int end = -1;
	
	byte defaultValue = 0;

	public Buffer(int size) {
		this.data = new byte[size];
		this.end = size;
	}

	public Buffer(byte[] rawData) {
		this(rawData.length);
		this.data = rawData.clone();
	}

	public byte[] getData() {
		return this.data;
	}

	public int getStart() {
		return this.start;
	}

	public int getEnd() {
		return this.end;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public void setDefaultValue(byte val){
		this.defaultValue = val;
	}

	public void reset() {
		this.start = 0;
		this.end = this.data.length;
	}

	public boolean isValid() {

		return this.size() > 0;
	}

	public void set(int idx, byte value) {
		this.data[idx + start] = value;
	}

	public byte get(int idx) {
		return this.data[idx + start];
	}

	public void set(int idx, int i) {
		this.set(idx, (byte) i);
	}
	
	public int size() {
		return end - start;
	}

	public Iterator<Byte> iterator() {

		return new Iterator<Byte>() {
			private int idx = 0;

			public boolean hasNext() {
				return (idx + start) < end;
			}

			public Byte next() {
				Byte val = 0;
				if (idx+start < data.length){
					val = data[idx + start];
				}
				idx++;
				return val;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public int readFromStream(FileInputStream fis) throws IOException {
		return fis.read(this.data);
	}


	public void writeToStream(FileOutputStream destenation, int start2, int len) throws IOException {
		destenation.write(data, start, len);		
	}

}
