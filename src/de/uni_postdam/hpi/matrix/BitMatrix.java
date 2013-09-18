package de.uni_postdam.hpi.matrix;

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
	
	@Override
	protected int default_value() {
		return -2;
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


/*
 * 
 * 
 * 
 * 
int *jerasure_matrix_to_bitmatrix(int k, int m, int w, int *matrix) 
{
  int *bitmatrix;
  bitmatrix = talloc(int, k*m*w*w);
  if (matrix == NULL) { return NULL; }

  rowelts = k * w;
  rowindex = 0;

  for (i = 0; i < m; i++) {
    colindex = rowindex;
    for (j = 0; j < k; j++) {
      elt = matrix[i*k+j];
      for (x = 0; x < w; x++) {
        for (l = 0; l < w; l++) {
          bitmatrix[colindex+x+l*rowelts] = ((elt & (1 << l)) ? 1 : 0);
        }
        elt = galois_single_multiply(elt, 2, w);
      }
      colindex += w;
    }
    rowindex += rowelts * w;
  }
  return bitmatrix;
 }
  
 */