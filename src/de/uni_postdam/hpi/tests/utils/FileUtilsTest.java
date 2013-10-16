package de.uni_postdam.hpi.tests.utils;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

	byte[] rawData = new byte[512];
	byte[] rawCoding = new byte[256];

	@Before
	public void setUp() throws IOException{
		for(int i = 0; i < rawData.length; i++){
			rawData[i] = (byte)i;
		}
		for(int i = 0; i < rawCoding.length; i++){
			rawCoding[i] = (byte)i;
		}

		for(int i = 0; i < k_parts.length; i++){
			File f = FileUtils.createFile(String.format("part_k%d", i), new byte[]{});
			k_files[i] = f;
			k_parts[i] = new FileOutputStream(f);
		}
		
		for(int i = 0; i < m_parts.length; i++){
			File f = FileUtils.createFile(String.format("part_m%d", i), new byte[]{});
			m_files[i] = f;
			m_parts[i] = new FileOutputStream(f);
		}
	}
	
	@Test
	public void test_write_parts_from_buffers() throws IOException {
		
		int w = 8, packetSize = 1;
		
		Buffer data = new Buffer(rawData);
		Buffer coding = new Buffer(rawCoding);

		
		FileUtils.writeParts(data, coding, k_parts, m_parts, w, packetSize);

		FileUtils.close(k_parts);
		FileUtils.close(m_parts);

		byte[] k1_content = new byte[256];
		byte[] k2_content = new byte[256];

		boolean first = true;
		int c1 = 0, c2 = 0; 
		for(int i = 0; i < rawData.length; i++){
			int rest = i % (w*packetSize);
			if(i != 0 && rest == 0){
				first = !first;
			}

			if(first){
				k1_content[c1++] = (byte)i;
			}else{
				k2_content[c2++] = (byte)i;
			}
		}

		assertTrue(FileUtils.checkFileContent(k_files[0], k1_content));
		assertTrue(FileUtils.checkFileContent(k_files[1], k2_content));

		assertTrue(FileUtils.checkFileContent(m_files[0], rawCoding));
	}

}
