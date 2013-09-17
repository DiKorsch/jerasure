package de.uni_postdam.hpi.galois;

/*
 * Parts are from:
 * */

public class GaloisField {

	private int elements = 0;
	private int degree = 0;

	private int[] expTable = null;
	private int[] logTable = null;

	private boolean initialized = false;

	private final static int prim_poly[] = { 0,
	/* 1 */1,
	/* 2 */07,
	/* 3 */013,
	/* 4 */023,
	/* 5 */045,
	/* 6 */0103,
	/* 7 */0211,
	/* 8 */0435,
	/* 9 */01021,
	/* 10 */02011,
	/* 11 */04005,
	/* 12 */010123,
	/* 13 */020033,
	/* 14 */042103,
	/* 15 */0100003,
	/* 16 */0210013,
	/* 17 */0400011,
	/* 18 */01000201,
	/* 19 */02000047,
	/* 20 */04000011,
	/* 21 */010000005,
	/* 22 */020000003,
	/* 23 */040000041,
	/* 24 */0100000207,
	/* 25 */0200000011,
	/* 26 */0400000107,
	/* 27 */01000000047,
	/* 28 */02000000011,
	/* 29 */04000000005,
	/* 30 */010040000007,
	/* 31 */020000000011,
	/* 32 */00020000007 }; /*
							 * Really 40020000007, but we're omitting the high
							 * order bit
							 */

	public static int calcElements(int degree) {
		return 1 << degree; // 2^degree
	}

	public GaloisField(int degree) {
		this.degree = degree;
		this.elements = calcElements(degree);
		initialize();
	}

	private void initialize() {
		if (initialized)
			return;

		int size = this.elements;
		expTable = new int[size];
		logTable = new int[size];
		int x = 1;
		for (int i = 0; i < size; i++) {
			expTable[i] = x;
			x <<= 1; // x = x * 2; we're assuming the generator alpha is 2
			if (x >= size) {
				x ^= prim_poly[this.degree];
				x &= size - 1;
			}
		}
		for (int i = 0; i < size - 1; i++) {
			logTable[expTable[i]] = i;
		}
		initialized = true;

	}

	public int elements() {
		return this.elements;
	}

	public GaloisNumber number(int value) {
		return new GaloisNumber(this, value);
	}

	@Override
	public boolean equals(Object arg0) {
		GaloisField other = null;
		if (arg0 instanceof GaloisField) {
			other = (GaloisField) arg0;
		} else {
			return false;
		}
		return other != null && (this.elements == other.elements);
	}

	public GaloisNumber add(int x, int y) {

		return this.number(this.number(x).value() ^ this.number(y).value());
	}

	public GaloisNumber subtract(int x, int y) {

		return this.add(x, y);
	}

	public GaloisNumber divide(int x, int y) {
		int a = this.number(x).value();
		int b = this.number(y).value();
		if (b == 0) {
			throw new IllegalArgumentException("0 is not allowed!");
		}
		if (a == 0) {
			return this.number(0);
		}
		int idx = log(a) - log(b);
		if (idx < 0)
			idx = idx + this.elements - 1;
		return this.number(exp(idx));
	}

	/* ------------------------------------------------------------------------
	 * following code was copied from
	 * https://code.google.com/p/zxing/source/browse
	 * /trunk/core/src/com/google/zxing/common/reedsolomon/GenericGF.java
	 * ========================================================================
	 * Copyright 2007 ZXing authors
	 * 
	 * Licensed under the Apache License, Version 2.0 (the "License"); you may
	 * not use this file except in compliance with the License. You may obtain a
	 * copy of the License at
	 * 
	 * http://www.apache.org/licenses/LICENSE-2.0
	 * 
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	 * License for the specific language governing permissions and limitations
	 * under the License.
	 * ------------------------------------------------------------------------
	 */
	
	/**
	 * @return 2 to the power of a in GF(elements)
	 */
	int exp(int a) {
		return expTable[a];
	}

	/**
	 * @return base 2 log of a in GF(elements)
	 */
	int log(int a) {
		if (a == 0) {
			throw new IllegalArgumentException("0 is not allowed!");
		}
		return logTable[a];
	}

	/**
	 * @return multiplicative inverse of a
	 */
	int inverse(int a) {
		if (a == 0) {
			throw new ArithmeticException("0 is not allowed!");
		}
		return exp(this.elements - log(a) - 1);
	}

	public GaloisNumber multiply(int x, int y) {
		int a = this.number(x).value();
		int b = this.number(y).value();
		if (a == 0 || b == 0) {
			return this.number(0);
		}
		return this.number(exp((log(a) + log(b)) % (this.elements - 1)));
	}
}
