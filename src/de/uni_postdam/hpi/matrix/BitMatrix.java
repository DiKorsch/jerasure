package de.uni_postdam.hpi.matrix;

import java.util.ArrayList;
import java.util.List;

import de.uni_postdam.hpi.galois.Galois;
import de.uni_postdam.hpi.matrix.Schedule.OPERATION;

public class BitMatrix extends Matrix{

	int w = -1;
	public BitMatrix(Matrix matrix, int w) {
		this(matrix.cols(), matrix.rows(), w);
		
		for(int row = 0; row < matrix.rows(); row++){
			for(int col = 0; col < matrix.cols(); col++){
				BitMatrix small = numberToBitmatrix(matrix.get(col, row), w);
				this.rangeSet(col * w, row * w, small);
			}
		}		
	}
	
	public BitMatrix(BitMatrix other){
		super(other);
		this.w = other.w;
	}
	
	public BitMatrix(int cols, int rows, int w, int[] content) {
		super(cols * w, rows * w, content);
		this.w = w;
	}
	
	public BitMatrix(int cols, int rows, int w){
		super(cols * w, rows * w);
		this.w = w;
	}

	public static BitMatrix numberToBitmatrix(int number, int w){
		BitMatrix result = new BitMatrix(1, 1, w);
		
		for(int col = 0; col < w; col++){
			for(int row = 0; row < w; row++){
				int value = ((number & (1 << row))!=0 ? 1 : 0);
				result.set(col, row, value);
			}
			number = Galois.multiply(number, 2, w);
		}
		
		return result;
	}
	
	
	public Schedule[] toSchedules(int k, int w){
		
		List<Schedule> ops = new ArrayList<>();
		
		for(int row = 0; row < this.rows(); row++){
			OPERATION op = OPERATION.COPY;
			for(int col = 0; col < this.cols(); col++){
				if(this.get(col, row) == 1){
					ops.add(new Schedule(op, 
							col / w, col % w, 
							k + row / w, row % w));
					op = OPERATION.XOR;
				}
			}
		}
		
		return ops.toArray(new Schedule[ops.size()]);
	}
	
	
	@Override
	protected int default_value() {
		return -2;
	}
	
	@Override
	protected String delimiter() {
		return "  ";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BitMatrix) {
			BitMatrix other = (BitMatrix) obj;
			if(other.w != this.w){
				return false;
			}
		}
		return super.equals(obj);
	}

	
	public void copy_withIdx(int start, BitMatrix src, int srcStart, int length) {
		for(int i = 0; i < length; i++){
			this.setWithIdx(start + i, src.getWithIdx(srcStart + i));
		}
		
	}

	public void zero(int start, int len) {
		for(int i = start; i < start + len; i++){
			this.setWithIdx(i, 0);
		}
	}
	
	
	
	public BitMatrix invert(int w) {
		if(this.cols() != this.rows()){
			throw new IllegalArgumentException("Matrix have to a square matrix!");
		}
		BitMatrix self = new BitMatrix(this);
		BitMatrix inverse = new BitMatrix(this);

		inverse.toIdentity();
		inverse = self.convertToUpperTriangular(inverse, w);
		
		/* Now the matrix is upper triangular.  Start at the top and multiply down  */
		
		for (int i = rows()-1; i >= 0; i--) {
			int row_start = i*cols();
			for (int j = 0; j < i; j++) {
				int rs2 = j*cols();
				if (self.getWithIdx(rs2+i) != 0) {
					int tmp = self.getWithIdx(rs2+i);
					self.setWithIdx(rs2 + i, 0); 
					for (int k = 0; k < cols(); k++) {
						int mult = Galois.multiply(tmp, inverse.getWithIdx(row_start + k), w);
						inverse.setWithIdx(rs2 + k, inverse.getWithIdx(rs2+k) ^ mult);
					}
				}
			}
		}
		
		return inverse;
	}


	@Override
	protected BitMatrix convertToUpperTriangular(Matrix inverse, int w) {
		return (BitMatrix)super.convertToUpperTriangular(inverse, w);
	}
}
