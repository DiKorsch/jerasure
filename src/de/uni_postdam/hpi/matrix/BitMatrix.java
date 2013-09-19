package de.uni_postdam.hpi.matrix;

import java.util.ArrayList;
import java.util.List;

import de.uni_postdam.hpi.galois.Galois;

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
	
	public BitMatrix(int cols, int rows, int w, int[] content) {
		super(cols * w, rows * w, content);
		this.w = w;
	}
	
	private BitMatrix(int cols, int rows, int w){
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
			boolean op = false;
			for(int col = 0; col < this.cols(); col++){
				if(this.get(col, row) == 1){
					ops.add(new Schedule(op, 
							col / w, col % w, 
							k + row / w, row % w));
					op = true;
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
}
