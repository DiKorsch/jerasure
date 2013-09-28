package de.uni_postdam.hpi.tests.jerasure;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Files;

import de.uni_postdam.hpi.jerasure.Decoder;
import de.uni_postdam.hpi.jerasure.Encoder;
import de.uni_postdam.hpi.matrix.BitMatrix;
import static de.uni_postdam.hpi.utils.FileUtils.*;

public class DecoderTest {

	static File testDir = new File("decoderTest");

	int k = 10, m = 1, w = 7;

	File getFile(String fileName) {
		return new File(testDir.getAbsolutePath() + File.separator + fileName);
	}
	
	void cleanAndCreateFile(File f){
		cleanDir(testDir);
		assertTrue(createRandomContentFile(f, 1 * MB));
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		if (!testDir.isDirectory()) {
			testDir.mkdir();
		}

		cleanDir(testDir);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		cleanDir(testDir, true);
		if (testDir.isDirectory()) {
			testDir.delete();
		}
	}

	@After
	public void tearDown() {
		cleanDir(testDir);
	}

	
	
	@Test
	public void test_validator() {
		File f = getFile("someFile");
		Decoder dec = new Decoder(f, k, m, w);

		// nothing exists yet
		assertFalse(dec.isValid());

		all_parts_exist(f);
		assertTrue(dec.isValid());
		
		m_parts_missing(f);
		assertTrue(dec.isValid());
		
		m_of_k_parts_missing(f);
		assertTrue(dec.isValid());
		
		to_many_parts_missing(f);
		assertFalse(dec.isValid());

	}
	
	
	
	@Test
	public void test_decoding_with_all_k_parts() throws NoSuchAlgorithmException, IOException{
		File f = getFile("someFile");
		Decoder dec = new Decoder(f, k, m, w);
		
		m_parts_missing(f);
		
		long size = f.length();
		String hashShould = getMD5Hash(f);
		Files.copy(f, getFile("original"));
		assertTrue(f.delete());
		assertFalse(f.exists());
		dec.decode(size);
		assertTrue(f.exists());
		
		assertEquals(size, f.length());
		
		assertEquals(hashShould, getMD5Hash(f));
	}

//	@Test 
	public void test_decoding_with_m_of_k_parts_missing() throws NoSuchAlgorithmException, IOException{
		k = 3; m = 2; w = 3;
		File f = getFile("someFile");
		Decoder dec = new Decoder(f, k, m, w);
		
		m_of_k_parts_missing(f);
		long size = f.length();
		
		String hashShould = getMD5Hash(f);
		Files.copy(f, getFile("original"));
		assertTrue(f.delete());
		assertFalse(f.exists());
		dec.decode(size);
		assertTrue("Decoded file does not exist!", f.exists());
		
		assertEquals(size, f.length());
		
		assertEquals(hashShould, getMD5Hash(f));
	}
	
//	@Test
	public void test_decoding_with_k_and_m_missing(){
		k = 3; m = 2; w = 3;
		File f = getFile("someFile");
		Decoder dec = new Decoder(f, k, m, w);
		
		k_and_m_missing(f, 1, 1);
		long size = f.length();
		dec.decode(size);
	}
	
	
	
	@Test
	public void test_generate_decoding_bitmatrix_k_and_m_missing(){
				
		k = 3; m = 2; w = 3;
		File f = getFile("someFile");
		Decoder dec = new Decoder(f, k, m, w);

		k_and_m_missing(f, 1, 1);
		int[] content = {
				1, 0, 0, 0, 0, 1, 1, 1, 0, 
				0, 1, 0, 1, 0, 1, 0, 0, 1, 
				0, 0, 1, 0, 1, 0, 1, 0, 0, 

				1, 0, 0, 1, 0, 1, 0, 1, 0, 
				0, 1, 0, 1, 1, 1, 0, 1, 1, 
				0, 0, 1, 0, 1, 1, 1, 0, 1, 
		};
		BitMatrix should = new BitMatrix(k, m, w, content);
		
		assertEquals(should, dec.generate_decoding_bitmatrix());
	}
	
	@Test
	public void test_generate_decoding_bitmatrix_m_of_k_parts_missing(){
		
		k = 3; m = 2; w = 3;
		File f = getFile("someFile");
		Decoder dec = new Decoder(f, k, m, w);
		
		m_of_k_parts_missing(f);
		int[] content = {
				1, 1, 1, 0, 1, 1, 0, 1, 0, 
				1, 0, 0, 1, 1, 0, 0, 1, 1, 
				1, 1, 0, 1, 1, 1, 1, 0, 1, 
	
				0, 1, 1, 0, 1, 1, 1, 1, 0, 
				1, 1, 0, 1, 1, 0, 0, 0, 1, 
				1, 1, 1, 1, 1, 1, 1, 0, 0, 
		};
		BitMatrix should = new BitMatrix(k, m, w, content);
		
		assertEquals(should, dec.generate_decoding_bitmatrix());
	}
	
	@Test
	public void test_generate_decoding_bitmatrix_m_parts_missing(){
		k = 3; m = 2; w = 3;
		File f = getFile("someFile");
		Decoder dec = new Decoder(f, k, m, w);

		m_parts_missing(f);
		int[] content = {
				1, 0, 0, 1, 0, 0, 1, 0, 0, 
				0, 1, 0, 0, 1, 0, 0, 1, 0, 
				0, 0, 1, 0, 0, 1, 0, 0, 1, 

				1, 0, 0, 0, 0, 1, 1, 1, 0, 
				0, 1, 0, 1, 0, 1, 0, 0, 1, 
				0, 0, 1, 0, 1, 0, 1, 0, 0,  
		};
		BitMatrix should = new BitMatrix(k, m, w, content);
		
		assertEquals(should, dec.generate_decoding_bitmatrix());
	}
	
	
	
	// Scenarios
	private void all_parts_exist(File f) {
		cleanAndCreateFile(f);
		Encoder enc = new Encoder(k, m, w);
		enc.encode(f);
	}

	private void m_parts_missing(File f) {
		cleanAndCreateFile(f);
		Encoder enc = new Encoder(k, m, w);
		enc.encode(f);
		deleteFiles(collectFiles(f.getAbsolutePath(), "m", m));
	}
	
	private void m_of_k_parts_missing(File f){
		cleanAndCreateFile(f);
		Encoder enc = new Encoder(k, m, w);
		enc.encode(f);
		deleteSomeFiles(collectFiles(f.getAbsolutePath(), "k", k), m);

	}

	private void to_many_parts_missing(File f) {
		m_parts_missing(f);
		assertTrue(collectFiles(f.getAbsolutePath(), "k", k)[0].delete());
		
	}
	
	private void k_and_m_missing(File f, int k_missing, int m_missing){
		cleanAndCreateFile(f);
		Encoder enc = new Encoder(k, m, w);
		enc.encode(f);

		deleteSomeFiles(collectFiles(f.getAbsolutePath(), "k", k), k_missing);
		deleteSomeFiles(collectFiles(f.getAbsolutePath(), "m", m), m_missing);

		
	}
	
	private void deleteSomeFiles(File[] files, int toDelete){
		int c = 0;
		for (File part : files) {
			if (++c > toDelete)
				break;
			assertTrue(part.delete());
		}
	}
	
	

}
