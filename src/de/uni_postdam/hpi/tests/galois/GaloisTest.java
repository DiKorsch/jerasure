package de.uni_postdam.hpi.tests.galois;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uni_postdam.hpi.galois.Galois;

public class GaloisTest {

	@Test
	public void test_divide(){
		assertEquals(1, 		Galois.divide(3, 3, 2));
		assertEquals(3, 		Galois.divide(2, 3, 2));
		assertEquals(2, 		Galois.divide(3, 2, 2));

		assertEquals(6, 		Galois.divide(1, 3, 3));
		assertEquals(120, 		Galois.divide(1, 3, 7));
		
		
	}
	
	@Test
	public void test_GF4(){
		int first = 0;
		int second = 0;
		int degree = 2;
		
		assertEquals(0, 		Galois.multiply(first, second++, degree));
		assertEquals(0, 		Galois.multiply(first, second++, degree));
		assertEquals(0, 		Galois.multiply(first, second++, degree));
		assertEquals(0, 		Galois.multiply(first, second++, degree));
		first++; second = 0;
		
		assertEquals(0, 		Galois.multiply(first, second++, degree));
		assertEquals(1, 		Galois.multiply(first, second++, degree));
		assertEquals(2, 		Galois.multiply(first, second++, degree));
		assertEquals(3, 		Galois.multiply(first, second++, degree));
		first++; second = 0;

		assertEquals(0, 		Galois.multiply(first, second++, degree));
		assertEquals(2, 		Galois.multiply(first, second++, degree));
		assertEquals(3, 		Galois.multiply(first, second++, degree));
		assertEquals(1, 		Galois.multiply(first, second++, degree));
		first++; second = 0;
		
		assertEquals(0, 		Galois.multiply(first, second++, degree));
		assertEquals(3, 		Galois.multiply(first, second++, degree));
		assertEquals(1, 		Galois.multiply(first, second++, degree));
		assertEquals(2, 		Galois.multiply(first, second++, degree));
	}
	


}
