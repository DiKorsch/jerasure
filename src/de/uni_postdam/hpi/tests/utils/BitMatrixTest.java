package de.uni_postdam.hpi.tests.utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.uni_postdam.hpi.util.BitMatrix;
import de.uni_postdam.hpi.util.Matrix;

public class BitMatrixTest {

	
	Matrix matrix = new Matrix(4,2);
	@Before
	public void setUp() throws Exception {
		matrix.setContent(new int[]{1,2,3,4,5,6,7,8});
	}
	
	@Test
	public void test_creation() {
		BitMatrix bm = new BitMatrix(matrix);
		bm.print(System.out);
	}

}
