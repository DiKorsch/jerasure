package de.uni_postdam.hpi.tests.performance;

import static de.uni_postdam.hpi.utils.FileUtils.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_postdam.hpi.jerasure.Decoder;
import de.uni_postdam.hpi.jerasure.Encoder;

public class PerformanceTest {

	static File testDir = new File("performanceTest");

	int k = 3, m = 1, w = 2;
	Long[] files = {
		100 * KB, 
		500 * KB,  
		1 * MB,
		10 * MB,
		100 * MB,
//		1 * GB,
	};

	File getFile(String fileName) {
		return new File(testDir.getAbsolutePath() + File.separator + fileName);
	}
	
	void cleanAndCreateFile(File f, long size){
		cleanDir(testDir);
		assertTrue(createRandomContentFile(f, size));
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
	public void test() {
		long t1,t2;
		for(int i = 0; i < 10; i++){
			for(long size: files){
				String fileName = size + ".test";
				System.out.println("testing " + fileName);
				File f = getFile(fileName);
				cleanAndCreateFile(f, size);
				Encoder enc = new Encoder(k, m, w);
				Decoder dec = new Decoder(f, k, m, w);
				
				t1 = System.currentTimeMillis();
				enc.encode(f);
				t2 = System.currentTimeMillis();
				
				System.out.println("Encoding: " + (t2 - t1));
				
				
				t1 = System.currentTimeMillis();
				dec.decode(size);
				t2 = System.currentTimeMillis();
				
				System.out.println("Decoding: " + (t2 - t1));
				
			}
			System.out.println("\n\n");
		}
	}

}
