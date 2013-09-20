package de.uni_postdam.hpi.tests.jerasure;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import de.uni_postdam.hpi.jerasure.JErasure;
import de.uni_postdam.hpi.matrix.Schedule;

public class JErasureTest {

//	@Test
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
		
		coding = JErasure.encode(k, m, w, schedules, data, packetSize);
		
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

		coding = JErasure.encode(k, m, w, schedules, data, packetSize);
		
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

		coding = JErasure.encode(k, m, w, schedules, data, packetSize);
		
		assertArrayEquals(should, coding);
	}


	private File[] collectFiles(String filePath, String partPrefix, int numFiles){
		File[] result = new File[numFiles];
		for(int i = 1; i <= numFiles; i++){
			result[i-1] = new File(String.format("%s_%s%d", filePath, partPrefix, i));
		}
		return result;
	}
	
	@Test
	public void test_encode_file(){
		int k,m,w;
		File original = null;
		FileOutputStream fos = null;
		File[] k_files = null;
		File[] m_files = null;
		try {
			k = 3; m = 1; w = 4;
			original = File.createTempFile("original", "");
			fos = new FileOutputStream(original);
			fos.write(new byte[]{
					0x00, 0x26, 0x1e, 0x27, 
					0x52, (byte) 0xf6, 0x09, (byte) 0x85, 
					0x22, (byte) 0x97, 0x2e, 0x15, 
			});
			fos.close();
			
			JErasure.encode(original, k, m, w);

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
		} finally{
			if(original != null){
				original.deleteOnExit();
			}

			deleteFiles(k_files);
			deleteFiles(m_files);
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
}
