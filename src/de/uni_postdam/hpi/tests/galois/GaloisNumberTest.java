package de.uni_postdam.hpi.tests.galois;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uni_postdam.hpi.galois.GaloisField;
import de.uni_postdam.hpi.galois.GaloisNumber;

public class GaloisNumberTest {

	
	private GaloisField newField(int degree){
		return new GaloisField(degree);
	}
	
	@Test
	public void test_creation() {
		GaloisField gf = newField(2);
		GaloisNumber num1 = gf.number(3);
		assertNotNull(num1);
		
		GaloisNumber num2 = new GaloisNumber(gf, 4);
		assertNotNull(num2);

		assertEquals(3, num1.value());
		assertEquals(0, num2.value());
	}
	
	@Test
	public void test_equality() {
		GaloisField gf = newField(2);
		assertEquals(gf.number(3), new GaloisNumber(gf, 3));

		assertEquals(gf.number(3), gf.number(7));
		
		assertNotSame(gf.number(3), gf.number(5));
	}
	
	@Test
	public void test_comparabilty(){
		GaloisField gf = newField(2);

		
		GaloisNumber num0 = gf.number(0);
		GaloisNumber num1 = gf.number(1);
		GaloisNumber num2 = gf.number(2);
		GaloisNumber num3 = gf.number(3);
		GaloisNumber num0_4 = gf.number(4);
		GaloisNumber num1_5 = gf.number(5);
		GaloisNumber num2_6 = gf.number(6);
		GaloisNumber num3_7 = gf.number(7);

		assertTrue(num0.compareTo(num1) < 0);
		assertTrue(num1.compareTo(num2) < 0);
		assertTrue(num2.compareTo(num3) < 0);

		assertTrue(num0_4.compareTo(num1_5) < 0);
		assertTrue(num1_5.compareTo(num2_6) < 0);
		assertTrue(num2_6.compareTo(num3_7) < 0);
		
		assertTrue(num0_4.compareTo(num1) < 0);
		assertTrue(num1.compareTo(num2_6) < 0);
	}

}
