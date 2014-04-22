package de.uni_postdam.hpi.tests.jerasure;

import static de.uni_postdam.hpi.utils.FileUtils.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Files;

import de.uni_postdam.hpi.jerasure.bufferless.Decoder;
import de.uni_postdam.hpi.jerasure.bufferless.Encoder;
import de.uni_postdam.hpi.matrix.BitMatrix;
import de.uni_postdam.hpi.utils.FileUtils;

public class BufferlessDecoderTest {

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
	public void test_validator() throws NoSuchAlgorithmException, IOException, InterruptedException {
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
	public void test_decoding_with_all_k_parts() throws NoSuchAlgorithmException, IOException, InterruptedException{
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

	@Test 
	public void test_decoding_with_m_of_k_parts_missing() throws NoSuchAlgorithmException, IOException, InterruptedException{
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

	@Test
	public void test_decode_256_bytes() throws InterruptedException, IOException{

		int k,m,w;
		File original = null;

		File[] k_files = null;
		File[] m_files = null;
		
		k = 2; m = 1; w = 7;
		byte[] content = new byte[256];
		for(int i = 0; i < 256; i++){
			content[i] = (byte) i;
		}
		original = createFile("original", content);
		long size = original.length();
		new Encoder(k, m, w).encode(original);

		k_files = collectFiles(original.getAbsolutePath(), "k", k);
		m_files = collectFiles(original.getAbsolutePath(), "m", m);
		
		for(File f: k_files){
			assertTrue(String.format("%s does not exist!", f.getAbsolutePath()), f.exists());
		}
		
		for(File f: m_files){
			assertTrue(String.format("%s does not exist!", f.getAbsolutePath()), f.exists());
		}

		deleteSomeFiles(k_files, m);
		assertTrue(original.delete());
		assertFalse(original.exists());
		new Decoder(original, k, m, w).decode(size);
		assertTrue(original.exists());

		assertTrue(checkFileContent(original, content));
			
	}
	

	@Test
	public void test_decode_big_file() throws IOException, InterruptedException{

		int k,m,w;
		File original = null;

		File[] k_files = null;
		File[] m_files = null;
		
		long numBytes = 16 * MB;
		k = 2; m = 1; w = 7;
		byte[] content = new byte[(int) numBytes];
		for(int i = 0; i < numBytes; i++){
			content[i] = (byte) i;
		}
		original = createFile("original", content);
		long size = original.length();
		new Encoder(k, m, w).encode(original);

		k_files = collectFiles(original.getAbsolutePath(), "k", k);
		m_files = collectFiles(original.getAbsolutePath(), "m", m);
		
		for(File f: k_files){
			assertTrue(String.format("%s does not exist!", f.getAbsolutePath()), f.exists());
		}
		
		for(File f: m_files){
			assertTrue(String.format("%s does not exist!", f.getAbsolutePath()), f.exists());
		}

		deleteSomeFiles(k_files, m);
		assertTrue(original.delete());
		assertFalse(original.exists());
		new Decoder(original, k, m, w).decode(size);
		assertTrue(original.exists());

		assertTrue(checkFileContent(original, content));
			
	}
	
	@Test
	public void test_decode_64_bytes() throws NoSuchAlgorithmException, IOException, InterruptedException{

		int k,m,w;
		File original = null;

		File[] k_files = null;
		File[] m_files = null;
		
		k = 3; m = 2; w = 6;
		int len = 64;
		byte[] content = new byte[len];
		for(int i = 0; i < len; i++){
			content[i] = (byte) i;
		}
		original = createFile("original", content);
		long size = original.length();
		new Encoder(k, m, w).encode(original);

		k_files = collectFiles(original.getAbsolutePath(), "k", k);
		m_files = collectFiles(original.getAbsolutePath(), "m", m);
		
		
		for(File f: k_files){
			assertTrue(String.format("%s does not exist!", f.getAbsolutePath()), f.exists());
		}
		
		for(File f: m_files){
			assertTrue(String.format("%s does not exist!", f.getAbsolutePath()), f.exists());
		}

		String[] k_hashes = getHashes(k_files);
		String[] m_hashes = getHashes(m_files);

		deleteSomeFiles(k_files, m);
		assertTrue(original.delete());
		assertFalse(original.exists());
		new Decoder(original, k, m, w).decode(size);
		assertTrue(original.exists());
		
		for(int i = 0; i < k; i++){
			assertEquals(k_hashes[i], FileUtils.getMD5Hash(k_files[i]));
		}
		
		for(int i = 0; i < m; i++){
			assertEquals(m_hashes[i], FileUtils.getMD5Hash(m_files[i]));
		}
		
		assertTrue(checkFileContent(original, content));
			
	}
	
	@Test
	public void test_decoding_with_k_and_m_missing() throws NoSuchAlgorithmException, IOException, InterruptedException{
		k = 3; m = 2; w = 3;
		File f = getFile("someFile");
		Decoder dec = new Decoder(f, k, m, w);
		
		k_and_m_missing(f, 1, 1);
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
	
	@Test
	public void test_decoding_restores_m_of_k_missing_parts() throws NoSuchAlgorithmException, IOException, InterruptedException{
		k = 3; m = 2; w = 3;
		File f = getFile("someFile");
		Decoder dec = new Decoder(f, k, m, w);
		
		String[] partHashes = m_of_k_parts_missing(f);
		long size = f.length();

		dec.decode(size);
		
		int c = 0;
		for(File part: collectFiles(f.getAbsolutePath(), "k", k)){
			assertTrue(part.exists());
			assertEquals(part.getName(), partHashes[c++], FileUtils.getMD5Hash(part));
		}
		
	}
	
	@Test
	public void test_generate_decoding_bitmatrix_k_and_m_missing() throws IOException, InterruptedException{
				
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
	public void test_generate_decoding_bitmatrix_m_of_k_parts_missing() throws NoSuchAlgorithmException, IOException, InterruptedException{
		
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
	public void test_generate_decoding_bitmatrix_m_parts_missing() throws IOException, InterruptedException{
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
	private void all_parts_exist(File f) throws IOException, InterruptedException {
		cleanAndCreateFile(f);
		Encoder enc = new Encoder(k, m, w);
		enc.encode(f);
	}

	private void m_parts_missing(File f) throws IOException, InterruptedException {
		cleanAndCreateFile(f);
		Encoder enc = new Encoder(k, m, w);
		enc.encode(f);
		deleteFiles(collectFiles(f.getAbsolutePath(), "m", m));
	}
	
	private String[] m_of_k_parts_missing(File f) throws NoSuchAlgorithmException, IOException, InterruptedException{
		cleanAndCreateFile(f);
		Encoder enc = new Encoder(k, m, w);
		enc.encode(f);
		String[] hashes = getHashes(collectFiles(f.getAbsolutePath(), "k", k));
		
		deleteSomeFiles(collectFiles(f.getAbsolutePath(), "k", k), m);

		
		return hashes;
	}

	private void to_many_parts_missing(File f) throws IOException, InterruptedException {
		m_parts_missing(f);
		assertTrue(collectFiles(f.getAbsolutePath(), "k", k)[0].delete());
		
	}
	
	private void k_and_m_missing(File f, int k_missing, int m_missing) throws IOException, InterruptedException{
		cleanAndCreateFile(f);
		Encoder enc = new Encoder(k, m, w);
		enc.encode(f);

		deleteSomeFiles(collectFiles(f.getAbsolutePath(), "k", k), k_missing);
		deleteSomeFiles(collectFiles(f.getAbsolutePath(), "m", m), m_missing);

		
	}
	
	
	private String[] getHashes(File[] parts) throws NoSuchAlgorithmException, IOException{
		int c = 0;
		String[] hashes = new String[parts.length];
		for(File part: parts){
			hashes[c++] = FileUtils.getMD5Hash(part);
		}
		return hashes;
	}
	

}
