package de.uni_postdam.hpi.cauchy;

import de.uni_postdam.hpi.galois.Galois;
import de.uni_postdam.hpi.matrix.Matrix;

public class Cauchy {


	private static int[] PPs = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1 };

	private static int[][] ONEs = new int[33][33];
	private static int[] NOs = new int[33];

	public static Matrix good_general_coding_matrix(int k, int m, int w) {
		int maxElements = 1 << w;
		if (k + m > maxElements) {
			throw new IllegalArgumentException(
					String.format(
							"k + m is too big! should be <= 2^w, but was: k=%d, m=%d, 2^w=%d",
							k, m, maxElements));
		}
		Matrix matrix = new Matrix(k, m);
		if (m == 2) {
			for (int i = 0; i < k; i++) {
				matrix.setWithIdx(i, 1);
				matrix.setWithIdx(i + k, CauchyBest.get(w)[i]);
			}
		} else {
			matrix = original_coding_matrix(k, m, w);
			matrix = improve_coding_matrix(k, m, w, matrix);

		}

		return matrix;
	}

	private static Matrix original_coding_matrix(int k, int m, int w) {
		Matrix matrix = new Matrix(k, m);
		int index = 0;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < k; j++) {
				matrix.setWithIdx(index, Galois.divide(1, (i ^ (m + j)), w));
				index++;
			}
		}
		return matrix;
	}

	private static Matrix improve_coding_matrix(int k, int m, int w,
			Matrix matrix) {
		int i, j, index, x;
		int tmp;
		int bno, tno, bno_index;

		for (j = 0; j < k; j++) {
			if (matrix.getWithIdx(j) != 1) {
				tmp = Galois.divide(1, matrix.getWithIdx(j), w);
				index = j;
				for (i = 0; i < m; i++) {
					matrix.setWithIdx(index,
							Galois.multiply(matrix.getWithIdx(index), tmp, w));
					index += k;
				}
			}
		}
		for (i = 1; i < m; i++) {
			bno = 0;
			index = i * k;
			for (j = 0; j < k; j++)
				bno += n_ones(matrix.getWithIdx(index + j), w);
			bno_index = -1;
			for (j = 0; j < k; j++) {
				if (matrix.getWithIdx(index + j) != 1) {
					tmp = Galois.divide(1, matrix.getWithIdx(index + j), w);
					tno = 0;
					for (x = 0; x < k; x++) {
						tno += n_ones(Galois.multiply(
								matrix.getWithIdx(index + x), tmp, w), w);
					}
					if (tno < bno) {
						bno = tno;
						bno_index = j;
					}
				}
			}
			if (bno_index != -1) {
				tmp = Galois.divide(1, matrix.getWithIdx(index + bno_index), w);
				for (j = 0; j < k; j++) {
					matrix.setWithIdx(index + j, Galois.multiply(
							matrix.getWithIdx(index + j), tmp, w));
				}
			}
		}
		return matrix;
	}

	public static int n_ones(int number, int w) {
		initPPs(w);
		int oneCount = Integer.bitCount(number & ((1 << w) - 1));
		int cno = oneCount;
		int highbit = (1 << (w - 1));
		
		for (int i = 1; i < w; i++) {
			if ((number & highbit) != 0) {
				number ^= highbit;
				number <<= 1;
				number ^= PPs[w];
				cno--;
				for (int j = 0; j < NOs[w]; j++) {
					cno += ((number & ONEs[w][j]) != 0) ? 1 : -1;
				}
			} else {
				number <<= 1;
			}
			oneCount += cno;
		}
		return oneCount;
	}

	private static void initPPs(int w) {
		if (PPs[w] != -1) 
			return;
		int highbit = (1 << (w - 1));
		int nones = 0;
		PPs[w] = Galois.multiply(highbit, 2, w);
		for (int i = 0; i < w; i++) {
			if ((PPs[w] & (1 << i)) != 0) {
				ONEs[w][nones] = (1 << i);
				nones++;
			}
		}
		NOs[w] = nones;
	}

	public static void printMatrix(Matrix matrix, int k, int m) {
		if (matrix.size() != k * m) {
			System.err.println("The Matrix has invalid size!");
			return;
		}
		for (int row = 0; row < m; row++) {
			for (int col = 0; col < k; col++) {
				System.out.print(String.format("(%d:%d) %d\t", col, row,
						matrix.get(col, row)));
			}
			System.out.println();
		}
	}

}
