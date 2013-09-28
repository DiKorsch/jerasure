package de.uni_postdam.hpi.tests.matrix;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uni_postdam.hpi.matrix.BitMatrix;
import de.uni_postdam.hpi.matrix.Matrix;
import de.uni_postdam.hpi.matrix.Schedule;
import de.uni_postdam.hpi.matrix.Schedule.OPERATION;

public class BitMatrixTest {

	@Test
	public void test_creation() {
		Matrix matrix = null;
		BitMatrix bitmatrix = null;
		BitMatrix should = null;
		int w = -1;
		int k = -1;
		
		w = 2;
		k = 2;
		matrix = new Matrix(k,k, new int[]{
				2,3,
				3,2});
		should = new BitMatrix(k, k, w, new int[]{
				0, 1, 1, 1, 
				1, 1, 1, 0, 
				1, 1, 0, 1, 
				1, 0, 1, 1, });
		bitmatrix = new BitMatrix(matrix, w);
		
		assertEquals(should, bitmatrix);
		
		
		w = 3;
		k = 3;
		matrix = new Matrix(k, k, new int[]{
				4,3,2,
				3,4,7,
				2,7,4});
		should = new BitMatrix(k, k, w, new int[]{
				0, 1, 0, 1, 0, 1, 0, 0, 1, 
				0, 1, 1, 1, 1, 1, 1, 0, 1, 
				1, 0, 1, 0, 1, 1, 0, 1, 0, 
				1, 0, 1, 0, 1, 0, 1, 1, 1, 
				1, 1, 1, 0, 1, 1, 1, 0, 0, 
				0, 1, 1, 1, 0, 1, 1, 1, 0, 
				0, 0, 1, 1, 1, 1, 0, 1, 0, 
				1, 0, 1, 1, 0, 0, 0, 1, 1, 
				0, 1, 0, 1, 1, 0, 1, 0, 1 });
		bitmatrix = new BitMatrix(matrix, w);
		
		assertEquals(should, bitmatrix);
		
		

		k = 3;
		w = 5;
		matrix = new Matrix(k, k, new int[]{
				27, 20, 19,
				20, 27, 3,
				19, 3, 27 });
		should = new BitMatrix(k, k, w, new int[]{
				1, 1, 1, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 
				1, 1, 1, 1, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0, 0, 
				0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 0, 0, 1, 1, 0, 
				1, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 
				1, 1, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 1, 
				0, 1, 0, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 1, 
				0, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 0, 0, 
				1, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 1, 0, 1, 
				0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 
				1, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 
				1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 
				1, 1, 1, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0, 
				0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 0, 1, 1, 
				0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 0, 1, 
				1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, });
		bitmatrix = new BitMatrix(matrix, w);
		
		assertEquals(should, bitmatrix);
		
		
	}

	@Test
	public void test_int_to_bit_matrix(){
		BitMatrix bitMatrix = null;
		BitMatrix should = null;
		int w = 0;
		
		w = 2;
		bitMatrix = BitMatrix.numberToBitmatrix(2, w);
		should = new BitMatrix(1, 1, w, new int[]{
				0,1,
				1,1});
		assertEquals(should, bitMatrix);

		bitMatrix = BitMatrix.numberToBitmatrix(3, w);
		should = new BitMatrix(1, 1, w, new int[]{
				1,1,
				1,0});
		assertEquals(should, bitMatrix);

		w = 3;
		bitMatrix = BitMatrix.numberToBitmatrix(4, w);
		should = new BitMatrix(1, 1, w, new int[]{
				0,1,0,
				0,1,1,
				1,0,1});
		assertEquals(should, bitMatrix);

		w = 3;
		bitMatrix = BitMatrix.numberToBitmatrix(2, w);
		should = new BitMatrix(1, 1, w, new int[]{
				0,0,1,
				1,0,1,
				0,1,0});
		assertEquals(should, bitMatrix);
		
		w = 3;
		bitMatrix = BitMatrix.numberToBitmatrix(7, w);
		should = new BitMatrix(1, 1, w, new int[]{
				1,1,1,
				1,0,0,
				1,1,0});
		assertEquals(should, bitMatrix);
		
	}
	
	@Test
	public void test_bit_matrix_to_schedule(){

		int k = 3, m = 1, w = 5;
		BitMatrix bitMatrix = new BitMatrix(k, m, w, new int[]{
			1,0,0,0,0, 0,1,0,0,0, 0,0,1,0,0,
			0,1,0,0,0, 0,0,1,0,0, 0,0,0,1,0,
			0,0,1,0,0, 0,0,1,1,0, 0,0,0,0,1,
			0,0,0,1,0, 0,0,0,0,1, 1,0,0,0,0,
			0,0,0,0,1, 1,0,0,0,0, 1,1,0,0,0,
				
		});
		Schedule[] schedules = bitMatrix.toSchedules(k, w);
		Schedule[] should = {
				new Schedule(OPERATION.COPY, 0, 0, 3, 0), new Schedule(OPERATION.XOR,  1, 1, 3, 0), new Schedule(OPERATION.XOR,  2, 2, 3, 0),
				new Schedule(OPERATION.COPY, 0, 1, 3, 1), new Schedule(OPERATION.XOR,  1, 2, 3, 1), new Schedule(OPERATION.XOR,  2, 3, 3, 1),
				new Schedule(OPERATION.COPY, 0, 2, 3, 2), new Schedule(OPERATION.XOR,  1, 2, 3, 2), new Schedule(OPERATION.XOR,  1, 3, 3, 2), new Schedule(OPERATION.XOR,  2, 4, 3, 2),
				new Schedule(OPERATION.COPY, 0, 3, 3, 3), new Schedule(OPERATION.XOR,  1, 4, 3, 3), new Schedule(OPERATION.XOR,  2, 0, 3, 3),
				new Schedule(OPERATION.COPY, 0, 4, 3, 4), new Schedule(OPERATION.XOR,  1, 0, 3, 4), new Schedule(OPERATION.XOR,  2, 0, 3, 4), new Schedule(OPERATION.XOR,  2, 1, 3, 4),
		}; 
		
		assertArrayEquals(should, schedules);
	}

	@Test
	public void test_invert(){
		BitMatrix 
			orig = null, 
			should = null;
		int[] content = null;
		int k = 0, w = 0;
		k = 2; w = 2;
		content = new int[]{
				0, 1, 1, 1, 
				1, 1, 1, 0, 
				1, 1, 0, 1, 
				1, 0, 1, 1, 
		};
		orig = new BitMatrix(k, k, w, content);
		content = new int[]{
				0, 1, 1, 1, 
				1, 1, 1, 0, 
				1, 1, 0, 1, 
				1, 0, 1, 1,  
		};
		should = new BitMatrix(k,k,w, content);
		orig = orig.invert(w);
		assertEquals(should, orig);
		
		
		k = 3; w = 3;
		content = new int[]{
				0, 1, 0, 1, 0, 1, 0, 0, 1, 
				0, 1, 1, 1, 1, 1, 1, 0, 1, 
				1, 0, 1, 0, 1, 1, 0, 1, 0, 
				1, 0, 1, 0, 1, 0, 1, 1, 1, 
				1, 1, 1, 0, 1, 1, 1, 0, 0, 
				0, 1, 1, 1, 0, 1, 1, 1, 0, 
				0, 0, 1, 1, 1, 1, 0, 1, 0, 
				1, 0, 1, 1, 0, 0, 0, 1, 1, 
				0, 1, 0, 1, 1, 0, 1, 0, 1, 
		};
		orig = new BitMatrix(k, k, w, content);
		content = new int[]{
				0, 1, 0, 0, 1, 1, 1, 0, 1, 
				0, 1, 1, 1, 1, 0, 1, 1, 1, 
				1, 0, 1, 1, 1, 1, 0, 1, 1, 
				0, 1, 1, 0, 1, 1, 0, 0, 1, 
				1, 1, 0, 1, 1, 0, 1, 0, 1, 
				1, 1, 1, 1, 1, 1, 0, 1, 0, 
				1, 0, 1, 0, 0, 1, 1, 1, 0, 
				1, 1, 1, 1, 0, 1, 0, 0, 1, 
				0, 1, 1, 0, 1, 0, 1, 0, 0, 
		};
		should = new BitMatrix(k,k,w, content);
		assertEquals(should, orig.invert(w));

	
	}
}
