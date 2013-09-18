package de.uni_postdam.hpi.matrix;

import de.uni_postdam.hpi.galois.Galois;

public class BitMatrix extends Matrix{

	final static int w = 3;
	public BitMatrix(Matrix matrix) {
		super(matrix.cols() * w, matrix.rows() * w);
		int rowindex = 0, colindex, mat_value;
		
		for(int row = 0; row < matrix.rows(); row++){
			colindex = rowindex;
			for(int col = 0; col < matrix.cols(); col++){
				mat_value = matrix.get(col, row);
				for(int x = 0; x < w; x++){
					for(int l = 0; l < w; l++){
						int value = ((mat_value & (1 << l))!=0 ? 1 : 0);
						this.setWithIdx(colindex+x+l*this.rows(), value);
					}
					mat_value = Galois.multiply(mat_value, 2, w);
				}
				colindex += w;
			}
			rowindex += w;
		}		
	}
	
	@Override
	protected int default_value() {
		return -2;
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