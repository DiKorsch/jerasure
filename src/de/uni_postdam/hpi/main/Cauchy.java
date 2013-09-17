package de.uni_postdam.hpi.main;

import de.uni_postdam.hpi.Settings;
import de.uni_postdam.hpi.util.Matrix;
import de.uni_postdam.hpi.galois.Galois;

public class Cauchy {

	static Integer[] best_07 = { 1, 2, 68, 4, 34, 8, 17, 16, 76, 32, 38, 3, 64,
			69, 5, 19, 35, 70, 6, 9, 18, 102, 10, 36, 85, 12, 21, 42, 51, 72,
			77, 84, 20, 25, 33, 50, 78, 98, 24, 39, 49, 100, 110, 48, 65, 93,
			40, 66, 71, 92, 7, 46, 55, 87, 96, 103, 106, 11, 23, 37, 54, 81,
			86, 108, 13, 22, 27, 43, 53, 73, 80, 14, 26, 52, 74, 79, 99, 119,
			44, 95, 101, 104, 111, 118, 29, 59, 89, 94, 117, 28, 41, 58, 67,
			88, 115, 116, 47, 57, 83, 97, 107, 114, 127, 56, 82, 109, 113, 126,
			112, 125, 15, 63, 75, 123, 124, 31, 45, 62, 91, 105, 122, 30, 61,
			90, 121, 60, 120 };

	private static int[] PPs = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1 };

	private static int[][] ONEs = new int[33][33];
	private static int[] NOs = new int[33];

	static Matrix good_general_coding_matrix(int k, int m, int w) {
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
				matrix.setWithIdx(i + k, Cauchy.best_07[i]);
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

	private static int n_ones(int n, int w) {

		int no;
		int cno;
		int nones;
		int i, j;
		int highbit;

		highbit = (1 << (Settings.w - 1));

		if (PPs[Settings.w] == -1) {
			nones = 0;
			PPs[Settings.w] = Galois.multiply(highbit, 2, w);
			for (i = 0; i < Settings.w; i++) {
				if (PPs[Settings.w] != 0 & (1 << i) != 0) {
					ONEs[Settings.w][nones] = (1 << i);
					nones++;
				}
			}
			NOs[Settings.w] = nones;
		}

		no = 0;
		for (i = 0; i < Settings.w; i++)
			if ((n & (1 << i)) != 0)
				no++;
		cno = no;
		for (i = 1; i < Settings.w; i++) {
			if ((n & highbit) != 0) {
				n ^= highbit;
				n <<= 1;
				n ^= PPs[Settings.w];
				cno--;
				for (j = 0; j < NOs[Settings.w]; j++) {
					cno += ((n & ONEs[Settings.w][j]) != 0) ? 1 : -1;
				}
			} else {
				n <<= 1;
			}
			no += cno;
		}
		return no;
	}

	static void printMatrix(Matrix matrix, int k, int m) {
		if (matrix.size() != k * m) {
			System.err.println("Die Matrix hat die falsche Größe!");
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

	public static void main(String[] args) {

		int k = 5;
		int m = 3;
		Matrix matrix = Cauchy.good_general_coding_matrix(k, m, Settings.w);
		if (matrix == null) {
			System.err.println("Matrix was null!");
		} else {
			matrix.print(System.out);
		}
	}

}
