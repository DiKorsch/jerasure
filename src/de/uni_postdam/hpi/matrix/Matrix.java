package de.uni_postdam.hpi.matrix;

import java.io.PrintStream;

import de.uni_postdam.hpi.galois.Galois;


public class Matrix{

	private int cols;
	private int rows;
	
	protected boolean isEmpty = true;
	
	protected int[][] content = null;
	
	protected int default_value(){
		return -1; 
	}
	
	
	public Matrix(int cols, int rows) {
		this.cols = cols;
		this.rows = rows;
		this.content = new int[cols][rows];
		int default_val = default_value();
		for(int row = 0; row < rows; row++){
			for(int col = 0; col < cols; col++){
				this.content[col][row] = default_val;
			}
		}
	}

	public Matrix(int cols, int rows, int[] content) {
		this(cols, rows);
		this.setContent(content);
	}

	public Matrix(Matrix other) {
		this(other.cols(), other.rows());
		for(int col= 0; col < cols(); col++){
			for(int row = 0; row < rows(); row ++){
				this.set(col, row, other.get(col, row));
			}
		}
		this.isEmpty = other.isEmpty;
	}


	public void setContent(int[] content){
		if(cols*rows != content.length){
			throw new IllegalArgumentException(
					String.format("content does not match the dimensions: cols=%d, rows=%d and content length: %d!",
							cols, rows, content.length));
		}
		this.isEmpty = false;
		for(int col = 0; col < cols; col++){
			for(int row = 0; row < rows; row++){
				this.content[col][row] = content[col + row * cols];
			}
		}
		
	}
	
	
	public int cols() {
		return cols;
	}

	public int rows() {
		return rows;
	}

	public int size() {
		return rows*cols;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public int get(int col, int row) {
		if(this.isEmpty) return 0;
		else{
			return this.content[col][row];
		}
	}
	
	public void set(int col, int row, int value){
		if(this.isEmpty) this.isEmpty = false;
		this.content[col][row] = value;
	}
	
	public void rangeSet(int beginCol, int beginRow, Matrix other) {
		if(beginCol + other.cols > this.cols || beginRow + other.rows > this.rows){
			throw new IllegalArgumentException("Destination matrix is too small!");
		}

		int col = beginCol;
		for(int otherCol = 0; otherCol < other.cols; otherCol++){
			int row = beginRow;
			for(int otherRow = 0; otherRow < other.rows; otherRow++){
				this.set(col, row, other.get(otherCol, otherRow));
				row++;
			}
			col++;
		}
	}
	

	public Matrix rangeGet(int beginCol, int beginRow, int cols, int rows) {
		if(beginCol + cols > this.cols || beginRow + rows > this.rows){
			throw new IllegalArgumentException("Source matrix is too small!");
		}
		
		Matrix result = new Matrix(cols, rows);
		for(int col = 0; col < cols; col++){
			for(int row = 0; row < rows; row++){
				result.set(col, row, this.get(col + beginCol, row + beginRow));
			}
		}
		return result;
	}
	
	
	public void print(PrintStream stream){
		stream.print(this.toString());
	}
	
	
	public void setWithIdx(int idx, int value){
		this.set(idx % cols(), idx / cols(), value);
	}
	
	public int getWithIdx(int idx){
		return this.get(this.colFromIdx(idx), this.rowFromIdx(idx));
	}
	
	private int colFromIdx(int idx){
		return idx % cols();
	}
	
	private int rowFromIdx(int idx){
		return idx / cols();
	}
	

	
	
	protected void swap_rows(int fromIdx, int toIdx){
		for(int i = 0; i < this.cols(); i++){
			int fromVal = this.get(i, fromIdx);
			this.set(i, fromIdx, this.get(i, toIdx));
			this.set(i, toIdx, fromVal);
		}
	}
	
	protected void galois_multiply_row(int rowIdx, int factor, int w) {
		for(int i = 0; i < this.cols(); i++) {
			this.set(i, rowIdx, Galois.multiply(this.get(i, rowIdx), factor, w));
		}
	}
	
	protected void galois_divide_row(int rowIdx, int divisor, int w) {
		for(int i = 0; i < this.cols(); i++) {
			this.set(i, rowIdx, Galois.divide(this.get(i, rowIdx), divisor, w));
		}
	}
	
	protected void galois_add_row_to_other(int srcIdx, int destIdx, int w){
		for(int i = 0; i < this.cols(); i++) {
			this.set(i, destIdx, Galois.add(this.get(i, destIdx), this.get(i, srcIdx), w));
		}
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		Matrix other = null;
		if (obj instanceof Matrix) {
			other = (Matrix) obj;
		}else {
			return false;
		}
		if(other.cols() != this.cols() || other.rows() != this.rows()){
			return false;
		}
		for(int col = 0; col < this.cols; col++){
			for(int row = 0; row < this.rows; row++){
				if(this.get(col, row) != other.get(col, row)){
					return false;
				}
			}
		}
		return true;
	}
	
	
	@Override
	public String toString() {
		String result = "";
		for(int row = 0; row < rows; row++){
			for(int col = 0; col < cols; col++){
				result += String.format("%d%s", this.get(col, row), this.delimiter());
			}
			result += "\n";
		}
		return result;
	}

	protected String delimiter(){
		return "\t";
	}
	
	
	
	public Matrix invert(int w) {
		return protectedInvert(new Matrix(this), new Matrix(this), w);
	}
	
	protected Matrix protectedInvert(Matrix self, Matrix inverse, int w){
		if(this.cols() != this.rows()){
			throw new IllegalArgumentException("Matrix have to a square matrix!");
		}
		
		inverse.toIdentity();
		inverse = self.convertToUpperTriangular(inverse, w);
		inverse = self.multiplyDown(inverse, w);
		
		return inverse;
	}
	
	protected Matrix multiplyDown(Matrix inverse, int w) {
		for (int i = rows()-1; i >= 0; i--) {
			for (int j = 0; j < i; j++) {
				if (this.get(i, j) != 0) {
					int tmp = this.get(i, j);
					for (int k = 0; k < cols(); k++) {
						int mult = Galois.multiply(tmp, inverse.get(k, i), w);
						inverse.set(k, j, inverse.get(k, j) ^ mult);
						mult = Galois.multiply(tmp, this.get(k, i), w);
						this.set(k, j, this.get(k, j) ^ mult);
					}
				}
			}
		}
		return inverse;
	}


	private Matrix convertToUpperTriangular(Matrix inverse, int w) {
		for (int col = 0; col < cols(); col++) {
			
			/* Swap rows if we have a zero i,i element.  If we can't swap, then the 
		       matrix was not invertible  */
			if (this.get(col, col) == 0) {
				int row = 0;
				for (row = col+1; row < rows() && this.get(col, row) == 0; row++) ;
				if (row == rows()) {
					throw new RuntimeException("matrix could not be inverted!");
				}
				this.swap_rows(col, row);
				inverse.swap_rows(col, row);
				
				if (this.get(col, col) == 0){
					throw new RuntimeException("something went wrong by swapping!");
				}
			}
			
			/* Multiply the row by 1/element i,i  */
			int val = this.get(col, col);
			if (val != 1) {
				this.galois_divide_row(col, val, w);
				inverse.galois_divide_row(col, val, w);
			}
			
			/* Now for each j>i, add A_ji*Ai to Aj  */
			int k = cols() * col + col;
			for (int j = col+1; j != cols(); j++) {
				k += cols();
				val = this.getWithIdx(k);
				if (val != 0) {
					if (val == 1) {
						this.galois_add_row_to_other(col, j, w);
						inverse.galois_add_row_to_other(col, j, w);
					} else {
						for (int x = 0; x < cols(); x++) {
							int mult = Galois.multiply(val, this.getWithIdx(cols() * col + x), w);
							this.setWithIdx(cols()*j + x, this.getWithIdx(cols()*j + x) ^ mult);
							
							mult = Galois.multiply(val, inverse.getWithIdx(cols() * col + x), w);
							inverse.setWithIdx(cols()*j + x, inverse.getWithIdx(cols()*j + x) ^ mult);
						}
					}
				}
			}
		}
		return inverse;
	}


	private void toIdentity(){
		for(int col = 0; col < cols(); col++){
			for(int row = 0; row < rows(); row++){
				this.set(col, row, col == row ? 1 : 0);
			}
		}
	}
	
}
