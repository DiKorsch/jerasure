package de.uni_postdam.hpi.tests.matrix;

import static org.junit.Assert.*;

import org.junit.After;
//import org.junit.Before;
import org.junit.Test;

import de.uni_postdam.hpi.matrix.Matrix;

public class MatrixTest {


	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_matrix_dimensions() {
		Matrix m = new Matrix(4,2);

		assertEquals(m.cols(), 4);
		assertEquals(m.rows(), 2);
		
		assertEquals(m.size(), 8);
	}
	
	@Test
	public void test_getter_setter(){
		Matrix m  = new Matrix(4,3);
		assertTrue(m.isEmpty());
		assertEquals(m.get(1, 1), 0);
		m.set(1,1,3);
		assertFalse(m.isEmpty());
		assertEquals(m.get(1,1), 3);
	}
	
	@Test
	public void test_create_empty_matrix() {
		Matrix m = new Matrix(4,2);
		assertTrue(m.isEmpty());
		for(int i = 0; i < 4; i++ ){
			for(int j = 0; j < 2; j++){
				assertEquals(m.get(i, j), 0);
			}
		}
	}

	public void test_create_matrix_with_content(){
		Matrix m = new Matrix(4,2, new int[]{1,2,3,4,5,6,7});
		assertTrue(m.isEmpty());
		int[] content = {1,2,3,4,5,6,7,8};
		m = new Matrix(4,2, content);
		assertFalse(m.isEmpty());
		for(int col = 0; col < 4; col++){
			for(int row = 0; row < 2; row++){
				assertEquals(m.get(col, row), content[col + row*4]);
			}
		}
	}
	
}
