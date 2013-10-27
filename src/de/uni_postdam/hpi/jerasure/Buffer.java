package de.uni_postdam.hpi.jerasure;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class Buffer implements Iterable<Byte> {

	private byte[] data = null;
	private int start = 0;
	private int end = -1;
	
	byte defaultValue = 0;

	public Buffer(int size) {
		this.setData(new byte[size]);
		this.setEnd(size);
	}

	public Buffer(byte[] rawData) {
		this(rawData.length);
		this.setData(rawData);
	}
	
	public byte[] getData() {
		return data;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}
	
	public void setLen(int len){
		this.setEnd(this.getStart() + len);
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public void setDefaultValue(byte val){
		this.defaultValue = val;
	}

	public void reset() {
		this.setStart(0);
		this.setEnd(data.length);
	}

	public boolean isValid() {

		return this.size() > 0;
	}

	public void set(int idx, byte value) {
		data[idx + getStart()] = value;
	}

	public byte get(int idx) {
		return data[idx + getStart()];
	}

	public void set(int idx, int i) {
		this.data[idx + getStart()] = (byte) i;
	}
	
	public int size() {
		return getEnd() - getStart();
	}

	public Iterator<Byte> iterator() {

		return new Iterator<Byte>() {
			private int idx = 0;

			public boolean hasNext() {
				return (idx + getStart()) < end;
			}

			public Byte next() {
				Byte val = 0;
				if (idx + getStart() < data.length){
					val = data[idx + getStart()];
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

	public int readFromStream(FileInputStream fis, int start, int len) throws IOException {
		return fis.read(this.data, start, len);
	}

	public void writeToStream(FileOutputStream destenation, int offset, int len) throws IOException {
		destenation.write(this.data, getStart() + offset, len);
	}

	public void writeToStream(RandomAccessFile destenation, int len) throws IOException {
		destenation.write(this.data, this.start, len);
	}
	
	public void xor(int idx, byte otherValue) {
		this.set(idx, this.get(idx) ^ otherValue);
	}

	public void setRange(int start, int len) {
		this.setStart(start);
		this.setEnd(start + len);
	}


}
