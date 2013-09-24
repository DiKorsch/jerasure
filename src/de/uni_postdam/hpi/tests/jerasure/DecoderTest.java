package de.uni_postdam.hpi.tests.jerasure;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_postdam.hpi.jerasure.Decoder;
import de.uni_postdam.hpi.jerasure.Encoder;
import static de.uni_postdam.hpi.utils.FileUtils.*;

public class DecoderTest {

	static File testDir = new File("decoderTest");

	int k, m, w;

	File getFile(String fileName) {
		return new File(testDir.getAbsolutePath() + File.separator + fileName);
	}
	
	void cleanAndCreateFile(File f){
		cleanDir(testDir);
		assertTrue(createRandomContentFile(f, MB));
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
		k = 4;
		m = 2;
		w = 7;
		Decoder dec = null;
		File f = getFile("someFile");

		// nothing exists yet
		dec = new Decoder(f, k, m, w);
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
		int c = 0;
		for (File part : collectFiles(f.getAbsolutePath(), "k", k)) {
			if (++c > m)
				break;
			assertTrue(part.delete());
		}
	}

	private void to_many_parts_missing(File f) {
		m_parts_missing(f);
		assertTrue(collectFiles(f.getAbsolutePath(), "k", k)[0].delete());
		
	}
	

}
