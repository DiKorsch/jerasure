package de.uni_postdam.hpi.tests.performance;

import java.io.File;

import org.junit.Test;

import de.uni_postdam.hpi.jerasure.Decoder;
import de.uni_postdam.hpi.jerasure.Encoder;

import static de.uni_postdam.hpi.utils.FileUtils.*;

public class GrowingKTest extends BasePerfTest{

	String fileName = "100mb";
	long fileSize = 100 * MB;
	
	int min_k = 3, max_k = 15;
	int m = 2, w = 8;
	
	@Test
	public void test() {

		File f = this.getFile(fileName);
		cleanAndCreateFile(f, fileSize);
		long t1, t2, size = f.length();
		
		for(int k = min_k; k <= max_k; k++) {
			String out = "";
			out += String.format("CR[%d:%d], w=%d:\t", k,m,w);
			Encoder enc = new Encoder(k, m, w);
			Decoder dec = new Decoder(f, k, m, w);
			
			t1 = System.currentTimeMillis();
			enc.encode(f);
			t2 = System.currentTimeMillis();
			
			out += String.format("Encoding: %d\t", t2 - t1);
			
			t1 = System.currentTimeMillis();
			dec.decode(size);
			t2 = System.currentTimeMillis();
			
			out += String.format("Decoding: %d", t2 - t1);
			
			System.out.println(out);
		}
	
	}

}
