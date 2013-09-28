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
	
	@Override
	public BitMatrix getRows(int beginRow, int rows) {
		return (BitMatrix) super.getRows(beginRow, rows);
	}
	
	@Override
	public BitMatrix rangeGet(int beginCol, int beginRow, int cols, int rows) {
		return (BitMatrix) super.rangeGet(beginCol, beginRow, cols, rows, new BitMatrix(cols / this.w, rows / this.w , this.w));
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

	
	public void copyWithIdx(int start, BitMatrix src, int srcStart, int length) {
		for(int i = 0; i < length; i++){
			this.setWithIdx(start + i, src.getWithIdx(srcStart + i));
		}
	}
	
	public void copyRows(int rowIdx, BitMatrix src, int srcRowIdx, int numRows){
		if(src.cols() != this.cols()){
			throw new IllegalArgumentException("both matrices must have same col number!");
		}
		BitMatrix rows = src.getRows(srcRowIdx, numRows);
		for (int row = 0; row < numRows; row++) {
			for(int col = 0; col < this.cols(); col++){
				this.set(col, row + rowIdx, rows.get(col, row));
			}
		}
	}

	public void zero(int start, int len) {
		for(int i = start; i < start + len; i++){
			this.setWithIdx(i, 0);
		}
	}
	
	
	@Override
	public BitMatrix invert(int w) {
		return (BitMatrix) invert(new BitMatrix(this), new BitMatrix(this), w);
	}
	
	@Override
	protected BitMatrix multiplyDown(Matrix inverse, int w) {
		
		for (int i = rows()-1; i >= 0; i--) {
			for (int j = 0; j < i; j++) {
				if (this.get(i, j) != 0) {
					this.galois_add_row_to_other(i, j, w);
					inverse.galois_add_row_to_other(i, j, w);
				}
			}
		}
		return (BitMatrix) inverse;
	}
	
}
