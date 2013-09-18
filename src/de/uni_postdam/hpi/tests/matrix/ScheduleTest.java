package de.uni_postdam.hpi.tests.matrix;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uni_postdam.hpi.cauchy.Cauchy;
import de.uni_postdam.hpi.matrix.*;

public class ScheduleTest {

	@Test
	public void test() {
		
		int k = 3, m = 1, w = 2;
		Matrix mat = Cauchy.good_general_coding_matrix(k, m, 2);
		BitMatrix bitMatrix = new BitMatrix(mat, w);
		Schedule sch = new Schedule(bitMatrix, k, m, w);
	}

}
