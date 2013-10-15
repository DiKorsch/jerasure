package de.uni_postdam.hpi.tests.jerasure;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import de.uni_postdam.hpi.jerasure.Buffer;

public class BufferTest {
	Buffer buffer = null;
	int size = 512;

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		this.buffer = new Buffer(size);
		byte[] data = new byte[size];
		for (int i = 0; i < size; i++) {
			data[i] = (byte) i;
		}
		this.buffer.setData(data);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_getter() {
		assertEquals(this.buffer.get(10), 10);
		this.buffer.setStart(10);
		assertEquals(this.buffer.get(0), 10);
	}

	@Test
	public void test_setter() {
		this.buffer.set(10, 0);
		assertEquals(this.buffer.get(10), 0);

		this.buffer.setStart(10);
		this.buffer.set(2, 0);

		this.buffer.reset();
		assertEquals(this.buffer.get(12), 0);

	}

	@Test
	public void test_iterator() {
		int val = 0;
		for (byte b : this.buffer) {
			assertEquals((byte) val++, b);
		}
		assertEquals(size, val);
	}
	
	@Test 
	public void test_range_iterator(){
		
		int start = 20;
		int len = 10;
		
		byte[] data = new byte[len];
		for (int i= 0; i < len; i++) {
			data[i] = (byte) (i + start);
		}
		
		int c = 0;
		this.buffer.setStart(start);
		this.buffer.setEnd(start + len);
		for(byte b: this.buffer){
			assertEquals(data[c++], b);
		}

		assertEquals(c, len);
	}
	
	@Test
	public void test_range_iterator_with_index(){
		int start = 10;
		int len = 10;
		this.buffer.setStart(start);
		this.buffer.setEnd(start + len);
		
		for(int i = 0; i < this.buffer.size(); i++){
			assertEquals(this.buffer.get(i), (byte) (i + start));
		}
	}
	
	@Test
	public void test_range_iterator_over_the_bounds(){
		int start = size;
		int len = 10;
		
		int c = 0;
		this.buffer.setStart(start);
		this.buffer.setEnd(start + len);
		for(byte b: this.buffer){
			assertEquals(0, b);
			c++;
		}

		assertEquals(len, c);		
	}

	@Test
	public void test_size(){
		int start = 10;
		int len = 10;

		assertEquals(this.buffer.size(), size);
		this.buffer.setStart(start);
		this.buffer.setEnd(start + len);
		assertEquals(this.buffer.size(), len);
		this.buffer.reset();
		assertEquals(this.buffer.size(), size);
		
	}

	

}
