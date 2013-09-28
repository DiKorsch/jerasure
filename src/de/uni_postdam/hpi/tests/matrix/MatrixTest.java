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
	
	@Test
	public void test_range_setter(){
		Matrix mBig = new Matrix(3,3);
		Matrix mSmall = new Matrix(2,2, new int[]{1,1,1,1}); 
		
		for(int i = 1; i < 3; i++){
			for(int j = 1; j < 3; j++){
				assertTrue(1 != mBig.get(i, j));
			}
		}
		
		mBig.rangeSet(1,1, mSmall);
		
		for(int i = 1; i < 3; i++){
			for(int j = 1; j < 3; j++){
				assertEquals(1, mBig.get(i, j));
			}
		}
	}
	
	public void test_range_getter(){
		Matrix mBig = new Matrix(3,3, new int[]{
				2,2,2,
				2,3,4,
				2,5,6});
		Matrix mSmall = new Matrix(2,2, new int[]{
				3,4,
				5,6}); 
		
		assertEquals(mSmall, mBig.rangeGet(1, 1, 2, 2));
	}
	
	@Test
	public void test_inverse(){
		Matrix 
			orig = null, 
			should = null;
		int[] content = null;
		int k = 0, w = 0;
		
		
		k = 2; w = 2;
		content = new int[]{2,3,3,2};
		orig = new Matrix(k,k, content);
		// inverse is the same!
		should = new Matrix(k,k, content);
		assertEquals(should, orig.invert(w));

		
		k = 3; w = 3;
		content = new int[]{
				4, 3, 2,
				3, 4, 7,
				2, 7, 4
			};
		orig = new Matrix(k,k, content);
		
		content = new int[]{
				4, 6, 3,
				6, 6, 2,
				3, 2, 5
			};
		should = new Matrix(k,k, content);
		assertEquals(should, orig.invert(w));
		
	}
}
