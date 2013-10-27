package de.uni_postdam.hpi.tests.performance.encoding;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import de.uni_postdam.hpi.jerasure.Encoder;
import de.uni_postdam.hpi.matrix.Schedule;
import de.uni_postdam.hpi.tests.performance.BasePerfTest;

import static de.uni_postdam.hpi.utils.FileUtils.*;

public class GrowingKTest extends BasePerfTest{

	String fileName = "100mb";
	long fileSize = 100 * MB;
	
	int min_mult = 1, max_mult = 15;
	int m = 2, w = 8, k = 4;
	File f = null;
	
	@Before
	public void setUp() {
		f = this.getFile(fileName);
		cleanAndCreateFile(f, fileSize);
	}
	
	@Test
	public void test() {
		
		long t1, t2;
		
		for(int mult = min_mult; mult <= max_mult; mult++) {
			String out = "";

			Schedule.copyCount = 0;
			Schedule.xorCount = 0;
			
			out += String.format("CR[%d:%d], w=%d:\t", k * mult, m * mult,w);
			Encoder enc = new Encoder(k * mult, m * mult, w);
			
			t1 = System.currentTimeMillis();
			enc.encode(f);
			t2 = System.currentTimeMillis();
			
			out += String.format("Encoding: %d\t", t2 - t1);
			out += String.format("xor: %d, copy: %d", Schedule.xorCount, Schedule.copyCount);
			
			System.out.println(out);
		}
	
	}

}
