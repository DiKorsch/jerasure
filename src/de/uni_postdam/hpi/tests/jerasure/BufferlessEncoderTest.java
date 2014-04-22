package de.uni_postdam.hpi.tests.jerasure;

import static de.uni_postdam.hpi.utils.FileUtils.checkFileContent;
import static de.uni_postdam.hpi.utils.FileUtils.collectFiles;
import static de.uni_postdam.hpi.utils.FileUtils.createFile;
import static de.uni_postdam.hpi.utils.FileUtils.deleteFiles;
import static de.uni_postdam.hpi.utils.FileUtils.getExtension;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Test;

import de.uni_postdam.hpi.jerasure.bufferless.Encoder;
import de.uni_postdam.hpi.utils.CalcUtils;

public class BufferlessEncoderTest {

	File[] k_files = null;
	File[] m_files = null;
	
	@After
	public void tearDown(){
		for(File[] parts: new File[][]{k_files, m_files}){
			if(parts != null){
				deleteFiles(parts);
			}
		}
	}
	
	@Test
	public void test_encode_small_file() throws InterruptedException, IOException{
		int k,m,w;
		File original = null;
		k = 3; m = 1; w = 4;
		original = createFile("original", new byte[]{
				0x00, 0x26, 0x1e, 0x27, 
				0x52, (byte) 0xf6, 0x09, (byte) 0x85, 
				0x22, (byte) 0x97, 0x2e, 0x15, 
		});
		
		new Encoder(k, m, w).encode(original);

		k_files = collectFiles(original.getAbsolutePath(), "k", k);
		m_files = collectFiles(original.getAbsolutePath(), "m", m);
		
		for(File f: k_files){
			assertTrue(String.format("%s does not exist!", f.getAbsolutePath()), f.exists());
		}
		
		for(File f: m_files){
			assertTrue(String.format("%s does not exist!", f.getAbsolutePath()), f.exists());
		}

		assertTrue(checkFileContent(k_files[0], new byte[]{0x00, 0x26, 0x1e, 0x27}));
		assertTrue(checkFileContent(k_files[1], new byte[]{0x52, (byte) 0xf6, 0x09, (byte) 0x85}));
		assertTrue(checkFileContent(k_files[2], new byte[]{0x22, (byte) 0x97, 0x2e, 0x15}));
		
		assertTrue(checkFileContent(m_files[0], new byte[]{0x70, 0x47, 0x39, (byte) 0xb7}));
			
	}
		
	@Test
	public void test_encode_middle_file() throws IOException, InterruptedException{

		int k,m,w;
		File original = null;
		k = 2; m = 1; w = 4;
		byte[] content = new byte[256];
		for(int i = 0; i < 256; i++){
			content[i] = (byte) i;
		}
		original = createFile("original", content);

		new Encoder(k, m, w).encode(original);

		k_files = collectFiles(original.getAbsolutePath(), "k", k);
		m_files = collectFiles(original.getAbsolutePath(), "m", m);
		
		for(File f: k_files){
			assertTrue(String.format("%s does not exist!", f.getAbsolutePath()), f.exists());
		}
		
		for(File f: m_files){
			assertTrue(String.format("%s does not exist!", f.getAbsolutePath()), f.exists());
		}

		byte[] content_k1 = new byte[256 / 2];
		byte[] content_k2 = new byte[256 / 2];
		byte[] content_m1 = new byte[256 / 2];
		
		boolean first = false;
		int c1 = 0, c2 = 0;
		for(int i = 0; i < 256; i++){
			if((i % 4) == 0){
				first = !first; // toggle every 4th byte
			}
			
			if(first){
				content_k1[c1++] = (byte)i; 
			} else {
				content_k2[c2++] = (byte)i;
			}
			// fill xored m-file
			if(i < 128) content_m1[i] = 4;
		}
		
		
		assertTrue(checkFileContent(k_files[0], content_k1));
		assertTrue(checkFileContent(k_files[1], content_k2));
		
		assertTrue(checkFileContent(m_files[0], content_m1));
			
	}


	@Test
	public void test_encode_with_smallest_overhead() throws InterruptedException, IOException{
		int len = 1024;
		byte[] content = new byte[len];
		for(int i = 0; i < len; i++){
			content[i] = 1;
		}
		File original = createFile("original", content);
		assertEquals(original.length(), len);
		int k = 3, m = 2, w = 7;
		new Encoder(k, m, w).encode(original);

		k_files = collectFiles(original.getAbsolutePath(), "k", k);
		m_files = collectFiles(original.getAbsolutePath(), "m", m);
		
		int part_len = 0;
		for(File part: k_files){
			part_len += part.length();
		}
		
		assertEquals(len + CalcUtils.calcOverHead(len, k, w), part_len);
		
	}

	@Test
	public void test_file_extension_is_kept_as_it_was() throws IOException, InterruptedException{
		
		byte[] content = new byte[512];
		String name = "original", ext = "test";
		File original = createFile(name, "." + ext, content);
		
		assertEquals(ext, getExtension(original.getName()));
		
		int k = 3, m = 2, w = 7;
		
		new Encoder(k, m, w).encode(original);
		
		k_files = collectFiles(original.getAbsolutePath(), "k", k);
		m_files = collectFiles(original.getAbsolutePath(), "m", m);
		
		assertEquals(k, k_files.length);
		assertEquals(m, m_files.length);
		
		for(File part: k_files){
			assertTrue(part.exists());
			assertEquals(ext, getExtension(part.getName()));
		}
		
		for(File part: m_files){
			assertTrue(part.exists());
			assertEquals(ext, getExtension(part.getName()));
		}
			
	}
}
