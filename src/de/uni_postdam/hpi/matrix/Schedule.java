package de.uni_postdam.hpi.matrix;

public class Schedule {

	int operations[][] = null;

	public Schedule(BitMatrix bitMatrix, int k, int m, int w) {

		this.operations = new int[k * w][m * w];
		int op = 0;
		int i, j;
		int diff[] = new int[m * w];
		int from[] = new int[m * w];
		int flink[] = new int[m * w];
		int blink[] = new int[m * w];

		int no, row, idx;
		int optodo;
		int bestrow = 0, bestdiff, top;

		bestdiff = k * w + 1;
		top = 0;
		idx = 0;
		for (i = 0; i < m * w; i++) {
			no = 0;
			for (j = 0; j < k * w; j++) {
				no += bitMatrix.getWithIdx(idx++);
			}
			diff[i] = no;
			from[i] = -1;
			flink[i] = i + 1;
			blink[i] = i - 1;
			if (no < bestdiff) {
				bestdiff = no;
				bestrow = i;
			}
		}

		flink[m * w - 1] = -1;

		while (top != -1) {
			row = bestrow;
			System.out.println(String.format("Doing row %d - %d from %d\n", row, diff[row], from[row]));

			if (blink[row] == -1) {
				top = flink[row];
				if (top != -1)
					blink[top] = -1;
			} else {
				flink[blink[row]] = flink[row];
				if (flink[row] != -1) {
					blink[flink[row]] = blink[row];
				}
			}

			if (from[row] == -1) {
				optodo = 0;
				for (j = 0; j < k * w; j++) {
					if (bitMatrix.get(row, j) != 0) {
						operations[op] = new int[5];
						operations[op][4] = optodo;
						operations[op][0] = j / w;
						operations[op][1] = j % w;
						operations[op][2] = k + row / w;
						operations[op][3] = row % w;
						optodo = 1;
						op++;
					}
				}
			} else {
				operations[op] = new int[5];
				operations[op][4] = 0;
				operations[op][0] = k + from[row] / w;
				operations[op][1] = from[row] % w;
				operations[op][2] = k + row / w;
				operations[op][3] = row % w;
				op++;
				for (j = 0; j < k * w; j++) {
					if ((bitMatrix.get(row, j) ^ bitMatrix.get(from[row], j)) != 0) {
						operations[op] = new int[5];
						operations[op][4] = 1;
						operations[op][0] = j / w;
						operations[op][1] = j % w;
						operations[op][2] = k + row / w;
						operations[op][3] = row % w;
						optodo = 1;
						op++;
					}
				}
			}
			bestdiff = k * w + 1;
			for (i = top; i != -1; i = flink[i]) {
				no = 1;
				for (j = 0; j < k * w; j++)
					no += (bitMatrix.get(row, j) ^ bitMatrix.get(i, j));
				if (no < diff[i]) {
					from[i] = row;
					diff[i] = no;
				}
				if (diff[i] < bestdiff) {
					bestdiff = diff[i];
					bestrow = i;
				}
			}
		}

		operations[op] = new int[5];
		operations[op][0] = -1;

	}

}
