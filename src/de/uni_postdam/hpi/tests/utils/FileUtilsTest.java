package de.uni_postdam.hpi.tests.utils;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_postdam.hpi.jerasure.Buffer;
import de.uni_postdam.hpi.utils.FileUtils;

public class FileUtilsTest {

	File[] k_files = new File[2];
	File[] m_files = new File[1];
	
	FileOutputStream[] k_parts = new FileOutputStream[2];
	FileOutputStream[] m_parts = new FileOutputStream[1];

	byte[][] rawParts = new byte[2][256];
	byte[] rawData = new byte[512];
 	byte[] rawCoding = new byte[256];

 	int w = 8, packetSize = 1;
	@Before
	public void setUp() throws IOException{
		int c1 = 0, c2 = 0;
		boolean first = true;
		for(int i = 0; i < 512; i++){
			if(i != 0 && (i % (w * packetSize)) == 0){
				first = !first;
			}
			if(first){
				rawParts[0][c1++] = (byte)i;
			}else{
				rawParts[1][c2++] = (byte)i;
			}
		}
		for(int i = 0; i < rawData.length; i++){
			rawData[i] = (byte)(i);
		}
		for(int i = 0; i < rawCoding.length; i++){
			rawCoding[i] = (byte)i;
		}
	}
	
	@After
	public void tearDown(){
		for(File[] parts: new File[][]{k_files, m_files}){
			for(File part: parts){
				part.delete();
			}
		}
	}
	
	private void createParts(File[] files, FileOutputStream[] streams, String suffix) throws IOException{
		for(int i = 0; i < streams.length; i++){
			File f = FileUtils.createFile(String.format("part_%s%d", suffix, i), new byte[]{});
			files[i] = f;
			streams[i] = new FileOutputStream(f);
		}
	}
	
	@Test
	public void test_write_parts_from_buffers() throws IOException {
		createParts(k_files, k_parts, "k");
		createParts(m_files, m_parts, "m");
		
		Buffer data = new Buffer(rawData);		
		Buffer coding = new Buffer(rawCoding);
		
		FileUtils.writeParts(data, coding, k_parts, m_parts, w, packetSize);

		FileUtils.close(k_parts);
		FileUtils.close(m_parts);

		assertTrue(FileUtils.checkFileContent(k_files[0], rawParts[0]));
		assertTrue(FileUtils.checkFileContent(k_files[1], rawParts[1]));

		assertTrue(FileUtils.checkFileContent(m_files[0], rawCoding));
	}

	@Test
	public void test_read_parts_to_buffer() throws IOException{
		createParts(k_files, k_parts, "k");
		createParts(m_files, m_parts, "m");

		Buffer data = new Buffer(rawData);		
		Buffer coding = new Buffer(rawCoding);
		
		FileUtils.writeParts(data, coding, k_parts, m_parts, w, packetSize);

		FileUtils.close(k_parts);
		FileUtils.close(m_parts);
		
		data = new Buffer(512);
		coding = new Buffer(256);
		
		SortedMap<Integer, FileInputStream> parts = new TreeMap<>();
		int c = 0;
		for(File f: k_files){
			parts.put(c++, new FileInputStream(f));
		}
		
		FileUtils.readParts(data, parts, w, packetSize);
		
		assertArrayEquals(rawData, data.getData());
	}
}





