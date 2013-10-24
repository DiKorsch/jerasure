package de.uni_postdam.hpi.tests.performance;

import static de.uni_postdam.hpi.utils.FileUtils.cleanDir;
import static de.uni_postdam.hpi.utils.FileUtils.createRandomContentFile;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class BasePerfTest {


	static File testDir = new File("performanceTest");
	protected File getFile(String fileName) {
		return new File(testDir.getAbsolutePath() + File.separator + fileName);
	}
	
	protected void cleanAndCreateFile(File f, long size){
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
}
