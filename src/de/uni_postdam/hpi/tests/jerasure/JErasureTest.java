package de.uni_postdam.hpi.tests.jerasure;

import static org.junit.Assert.*;
import static de.uni_postdam.hpi.utils.FileUtils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Test;

import de.uni_postdam.hpi.jerasure.Encoder;
import de.uni_postdam.hpi.matrix.Schedule;
import de.uni_postdam.hpi.utils.CalcUtils;

public class JErasureTest {

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
	public void test_encoding_packet() {

		int k, m, w, packetSize;
		byte[] data = null;
		byte[] should = null;
		byte[] coding = null;
		Schedule[] schedules = null;

		k = 3; m = 1; w = 2; packetSize = 2;
		schedules = Schedule.generate(k, m, w);
		data = new byte[]{
				// k1
				0x00, 0x26, 0x1e, 0x27, 
				// k2 
				0x52, (byte) 0xf6, 0x09, (byte) 0x85, 
				// k3 
				0x22, (byte) 0x97, 0x2e, 0x15, };
		should = new byte[]{ 
				// k1
				0x00, 0x26, 0x1e, 0x27, 
				// k2
				0x52, (byte) 0xf6, 0x09, (byte) 0x85, 
				// k3
				0x22, (byte) 0x97, 0x2e, 0x15, 
				// m1
				0x70, 0x47, 0x39, (byte) 0xb7 };
		
		coding = Encoder.encode(k, m, w, schedules, data, packetSize);
		
		assertArrayEquals(should, coding);
		
		
		k = 3; m = 1; w = 4; packetSize = 2;
		schedules = Schedule.generate(k, m, w);
		data = new byte[]{
				// k1
				0x00, 0x26, 0x1e, 0x27,
				0x52, (byte) 0xf6, 0x09, (byte) 0x85,
				// k2
				0x22, (byte) 0x97, 0x2e, 0x15,
				0x20, (byte) 0xad, 0x7e, 0x1d,
				// k3				
				0x28, (byte) 0xd2, 0x77, (byte) 0x94,
				0x16, (byte) 0xdd, 0x6d, (byte) 0xc4,
		};
		should = new byte[]{
				// k1
				0x00, 0x26, 0x1e, 0x27,
				0x52, (byte) 0xf6, 0x09, (byte) 0x85,
				// k2
				0x22, (byte) 0x97, 0x2e, 0x15,
				0x20, (byte) 0xad, 0x7e, 0x1d,
				// k3				
				0x28, (byte) 0xd2, 0x77, (byte) 0x94,
				0x16, (byte) 0xdd, 0x6d, (byte) 0xc4,
				// m1
				0x0a, 0x63, 0x47, (byte) 0xa6,
				0x64, (byte) 0x86, 0x1a, 0x5c,
		};

		coding = Encoder.encode(k, m, w, schedules, data, packetSize);
		
		assertArrayEquals(should, coding);
		

		k = 3; m = 2; w = 4; packetSize = 2;
		schedules = Schedule.generate(k, m, w);
		data = new byte[]{
				// k1
				0x00, 0x26, 0x1e, 0x27,
				0x52, (byte) 0xf6, 0x09, (byte) 0x85,
				// k2
				0x22, (byte) 0x97, 0x2e, 0x15,
				0x20, (byte) 0xad, 0x7e, 0x1d,
				// k3
				0x28, (byte) 0xd2, 0x77, (byte) 0x94,
				0x16, (byte) 0xdd, 0x6d, (byte) 0xc4,
		};
		
		should = new byte[]{
				// k1
				0x00, 0x26, 0x1e, 0x27,
				0x52, (byte) 0xf6, 0x09, (byte) 0x85,
				// k2
				0x22, (byte) 0x97, 0x2e, 0x15,
				0x20, (byte) 0xad, 0x7e, 0x1d,
				// k3
				0x28, (byte) 0xd2, 0x77, (byte) 0x94,
				0x16, (byte) 0xdd, 0x6d, (byte) 0xc4,
				// m1
				0x0a, 0x63, 0x47, (byte) 0xa6,
				0x64, (byte) 0x86, 0x1a, 0x5c,
				// m2
				0x21, 0x7d, 0x54, 0x70, 
				0x11, 0x27, 0x01, (byte) 0xfa
		};

		coding = Encoder.encode(k, m, w, schedules, data, packetSize);
		
		assertArrayEquals(should, coding);
		
		
		k = 2; m = 1; w = 4; packetSize = 1;
		schedules = Schedule.generate(k, m, w);
		data = new byte[]{
				// k1
				0,1,2,3,
				// k2
				4,5,6,7,
		};
		
		should = new byte[]{
				// k1
				0,1,2,3,
				// k2
				4,5,6,7,
				// m1
				4,4,4,4,
		};

		coding = Encoder.encode(k, m, w, schedules, data, packetSize);
		
		assertArrayEquals(should, coding);
	}

	@Test
	public void test_encode_small_file(){
		int k,m,w;
		File original = null;
		try {
			k = 3; m = 1; w = 4;
			original = createFile("original", new byte[]{
					0x00, 0x26, 0x1e, 0x27, 
					0x52, (byte) 0xf6, 0x09, (byte) 0x85, 
					0x22, (byte) 0x97, 0x2e, 0x15, 
			});
			
			Encoder.encode(original, k, m, w);

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
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
		
	@Test
	public void test_encode_middle_file(){

		int k,m,w;
		File original = null;
		try {
			k = 2; m = 1; w = 4;
			byte[] content = new byte[256];
			for(int i = 0; i < 256; i++){
				content[i] = (byte) i;
			}
			original = createFile("original", content);
			
			Encoder.encode(original, k, m, w);

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
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Test
	public void test_encode_with_smallest_overhead(){
		try {
			int len = 1024;
			byte[] content = new byte[len];
			for(int i = 0; i < len; i++){
				content[i] = 1;
			}
			File original = createFile("original", content);
			assertEquals(original.length(), len);
			int k = 3, m = 2, w = 7;
			Encoder.encode(original, k, m, w);

			k_files = collectFiles(original.getAbsolutePath(), "k", k);
			m_files = collectFiles(original.getAbsolutePath(), "m", m);
			
			int part_len = 0;
			for(File part: k_files){
				part_len += part.length();
			}
			
			assertEquals(part_len, len + CalcUtils.calcOverHead(original.length(), k, w));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test_file_extension_is_kept_as_it_was(){
		
		try {
			byte[] content = new byte[512];
			String name = "original", ext = "test";
			File original = createFile(name, "." + ext, content);
			
			assertEquals(ext, getExtension(original.getName()));

			int k = 3, m = 2, w = 7;
			
			Encoder.encode(original, k, m, w);

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
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private boolean checkFileContent(File f, byte[] should) throws IOException {
		
		FileInputStream fis = new FileInputStream(f);
		byte[] content = new byte[should.length];
		fis.read(content);
		fis.close();
		for(int i = 0; i < should.length; i++){
			if(content[i] != should[i])
				return false;
		}
		return true;
	}

	private void deleteFiles(File[] files) {
		if(files == null){
			System.err.println("Files was null!");
			return;
		}
		for(File f: files){
			if(f != null && !f.delete()){
				System.err.println("Could not delete: " + f.getAbsolutePath());
			}else if(f == null){
				System.err.println("File was null!");
			}
		}
		
	}

	
	private File createFile(String fileName, String extension, byte[] content) throws IOException{
		File result = File.createTempFile(fileName, extension);
		result.deleteOnExit();
		FileOutputStream fos = null;
		fos = new FileOutputStream(result);
		fos.write(content);
		fos.close();
		return result;
		
	}
	private File createFile(String fileName, byte[] content) throws IOException{
		return createFile(fileName, "", content);
	}
	
}
