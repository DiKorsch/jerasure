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

import de.uni_postdam.hpi.jerasure.Buffer;
import de.uni_postdam.hpi.jerasure.Decoder;
import de.uni_postdam.hpi.jerasure.Encoder;
import de.uni_postdam.hpi.matrix.BitMatrix;
import de.uni_postdam.hpi.utils.FileUtils;
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
	public void test_validator() throws NoSuchAlgorithmException, IOException {
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
	public void test_decoding_packet() throws NoSuchAlgorithmException, IOException{
		byte[] data = null;
		byte[] should = null;
		byte[] missing = null;
		int packetSize = 0;
		k = 3; m = 1; 
		File f = getFile("someFile");
		m_of_k_parts_missing(f);

		data = new byte[]{
				// m1 < k1 is missing!
				0x70, 0x47, 0x39, (byte) 0xb7, 
				// k2 
				0x52, (byte) 0xf6, 0x09, (byte) 0x85, 
				// k3 
				0x22, (byte) 0x97, 0x2e, 0x15, };
		
		should = new byte[]{ 
				// k1 < restored values
				0x00, 0x26, 0x1e, 0x27,  
				};
		
		w = 2; 
		packetSize = 2;
		missing = new Decoder(f, k, m, w).decode(data, packetSize);
		
		assertArrayEquals(should, missing);
		
		
		w = 4; 
		packetSize = 2;
		data = new byte[]{
				// m1
				0x0a, 0x63, 0x47, (byte) 0xa6,
				0x64, (byte) 0x86, 0x1a, 0x5c,
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
		};

		missing = new Decoder(f, k, m, w).decode(data, packetSize);
		
		assertArrayEquals(should, missing);
		
	}
	
	@Test
	public void test_decoding_packet_as_buffer() throws NoSuchAlgorithmException, IOException{
		Buffer data = null;
		byte[] should = null;
		byte[] missing = null;
		int packetSize = 0;
		k = 3; m = 1; 
		File f = getFile("someFile");
		m_of_k_parts_missing(f);

		data = new Buffer(new byte[]{
				// m1 < k1 is missing!
				0x70, 0x47, 0x39, (byte) 0xb7, 
				// k2 
				0x52, (byte) 0xf6, 0x09, (byte) 0x85, 
				// k3 
				0x22, (byte) 0x97, 0x2e, 0x15});
		
		should = new byte[]{ 
				// k1 < restored values
				0x00, 0x26, 0x1e, 0x27,  
				};
		
		w = 2; 
		packetSize = 2;
		missing = new Decoder(f, k, m, w).decode(data, packetSize);
		
		assertArrayEquals(should, missing);
		
		
		w = 4; 
		packetSize = 2;
		data = new Buffer(new byte[]{
				// m1
				0x0a, 0x63, 0x47, (byte) 0xa6,
				0x64, (byte) 0x86, 0x1a, 0x5c,
				// k2
				0x22, (byte) 0x97, 0x2e, 0x15,
				0x20, (byte) 0xad, 0x7e, 0x1d,
				// k3				
				0x28, (byte) 0xd2, 0x77, (byte) 0x94,
				0x16, (byte) 0xdd, 0x6d, (byte) 0xc4,
		});
		should = new byte[]{
				// k1
				0x00, 0x26, 0x1e, 0x27,
				0x52, (byte) 0xf6, 0x09, (byte) 0x85,
		};

		missing = new Decoder(f, k, m, w).decode(data, packetSize);
		
		assertArrayEquals(should, missing);
		
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

	@Test 
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
	
	@Test
	public void test_decode_256_bytes(){

		int k,m,w;
		File original = null;

		File[] k_files = null;
		File[] m_files = null;
		
		try {
			k = 2; m = 1; w = 4;
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
			

			deleteSomeFiles(k_files, m);
			assertTrue(original.delete());
			assertFalse(original.exists());
			new Decoder(original, k, m, w).decode(size);
			assertTrue(original.exists());

			assertTrue(checkFileContent(k_files[0], content_k1));
			assertTrue(checkFileContent(k_files[1], content_k2));

			assertTrue(checkFileContent(m_files[0], content_m1));
			
			assertTrue(checkFileContent(original, content));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test_decode_64_bytes() throws NoSuchAlgorithmException{

		int k,m,w;
		File original = null;

		File[] k_files = null;
		File[] m_files = null;
		
		try {
			k = 3; m = 2; w = 7;
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
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test_decoding_with_k_and_m_missing() throws NoSuchAlgorithmException, IOException{
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
	public void test_decoding_restores_m_of_k_missing_parts() throws NoSuchAlgorithmException, IOException{
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
	public void test_generate_decoding_bitmatrix_m_of_k_parts_missing() throws NoSuchAlgorithmException, IOException{
		
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
	
	private String[] m_of_k_parts_missing(File f) throws NoSuchAlgorithmException, IOException{
		cleanAndCreateFile(f);
		Encoder enc = new Encoder(k, m, w);
		enc.encode(f);
		String[] hashes = getHashes(collectFiles(f.getAbsolutePath(), "k", k));
		
		deleteSomeFiles(collectFiles(f.getAbsolutePath(), "k", k), m);

		
		return hashes;
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
	
	private String[] getHashes(File[] parts) throws NoSuchAlgorithmException, IOException{
		int c = 0;
		String[] hashes = new String[parts.length];
		for(File part: parts){
			hashes[c++] = FileUtils.getMD5Hash(part);
		}
		return hashes;
	}
	

}
