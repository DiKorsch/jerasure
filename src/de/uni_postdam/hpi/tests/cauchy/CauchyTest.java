package de.uni_postdam.hpi.tests.cauchy;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uni_postdam.hpi.cauchy.Cauchy;
import de.uni_postdam.hpi.matrix.Matrix;

public class CauchyTest {

	@Test
	public void test_n_ones() {

		int[] ws = { 2, 3, 4, 5, 6, 7 };
		int[][] shoulds = {
		/*w:     2  3  4   5   6   7  */
		/* 0 */{ 0, 0, 0,  0,  0,  0  },
		/* 1 */{ 2, 3, 4,  5,  6,  7  },
		/* 2 */{ 3, 4, 5,  6,  7,  8  },
		/* 3 */{ 3, 7, 9,  11, 13, 15 },
		/* 4 */{ 0, 5, 6,  7,  8,  9  },
		/* 5 */{ 2, 4, 10, 12, 14, 16 },
		/* 6 */{ 3, 7, 9,  13, 13, 17 },
		/* 7 */{ 3, 6, 13, 18, 19, 24 },
		/* 8 */{ 0, 0, 7,  8,  9,  10 },
		/* 9 */{ 2, 3, 5,  7,  15, 17 }};
		
		for (int n = 0; n < shoulds.length; n++) {
			for (int wIdx = 0; wIdx < shoulds[n].length; wIdx++) {
				int w = ws[wIdx];
				int should = shoulds[n][wIdx];
				int was = Cauchy.n_ones(n, w);
				assertEquals(
						String.format("n_ones for n=%d and w=%d failed!", n, w),
						should, was);
			}
		}
	}

	@Test
	public void test_matrix_generation(){
		Matrix cauchy = null;
		Matrix should = null;
		
		cauchy = Cauchy.good_general_coding_matrix(5, 2, 3);
		should = new Matrix(5, 2, new int[]{
				1,1,1,1,1,
				1,2,5,4,7});
		assertEquals(should, cauchy);
		
		cauchy = Cauchy.good_general_coding_matrix(5, 2, 4);
		should = new Matrix(5, 2, new int[]{
				1,1,1,1,1,
				1,2,9,4,8});
		assertEquals(should, cauchy);
		
		cauchy = Cauchy.good_general_coding_matrix(5, 2, 7);
		should = new Matrix(5, 2, new int[]{
				1,1,1,1,1,
				1,2,68,4,34});
		assertEquals(should, cauchy);
		
		
		cauchy = Cauchy.good_general_coding_matrix(5, 3, 7);
		should = new Matrix(5, 3, new int[]{
				1,1,1,1,1,
				103,25,16,1,11,
				1,80,107,68,72});
		assertEquals(should, cauchy);
		
		cauchy = Cauchy.good_general_coding_matrix(3, 4, 5);
		should = new Matrix(3, 4, new int[]{
				1,1,1,
				22,8,13,
				22,13,1,
				5,1,22});
		assertEquals(should, cauchy);
		
	}
	
}
