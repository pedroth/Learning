package algebra;

import java.awt.Color;
import java.util.Random;

public class Matrix {
	private double[][] matrix;
	private int lines;
	private int columns;

	public Matrix(int l, int col) {
		if (l == 0 || col == 0) {
			System.out.println("lines or columns must be higher than zero\n");
			System.exit(-1);
		} else {
			matrix = new double[l][col];
			lines = l;
			columns = col;
			this.fillZeros();
		}
	}

	public Matrix(double[][] m) {
		matrix = m;
		lines = m.length;
		columns = m[0].length;
	}

	public Matrix(double[] v) {
		lines = 1;
		columns = v.length;
		matrix = new double[lines][columns];
		for (int i = 0; i < columns; i++) {
			matrix[0][i] = v[i];
		}
	}

	public double selMatrix(int l, int col) {
		double r;
		r = matrix[l - 1][col - 1]; // the vectors in java starts with zero and
									// in a math matrix the coordinates of the
									// matrix starts with 1;ex: size matrix 3x3
									// ; i want to select the number on the
									// first line first column, (in math) i
									// would select matrix[1][1];
		return r;
	}

	public void setMatrix(int l, int col, double n) {
		matrix[l - 1][col - 1] = n;
	}

	public int getLines() {
		return lines;
	}

	public int getColumns() {
		return columns;
	}

	public double[][] getMatrix() {
		return matrix;
	}

	public static Matrix sumMatrix(Matrix a, Matrix b) {// return a different
														// matrix from a and b
		int i, j;
		Matrix c = null;
		double r = 0;
		if (a.getLines() == b.getLines() && a.getColumns() == b.getColumns()) {
			c = new Matrix(a.getLines(), a.getColumns());
			for (j = 1; j <= a.getColumns(); j++) {
				for (i = 1; i <= a.getLines(); i++) {
					r = a.selMatrix(i, j) + b.selMatrix(i, j);
					c.setMatrix(i, j, r);
				}
			}
		} else {
			System.out.printf("the size of the two matrix must be equal\n ");
			return c;
		}
		return c;
	}

	public static Matrix subMatrix(Matrix a, Matrix b) {
		Matrix r;
		r = Matrix.multiConsMatrix(-1, b);
		r = Matrix.sumMatrix(a, r);
		return r;
	}

	public static Matrix multiMatrix(Matrix a, Matrix b) {// return a different
															// matrix from a and
															// b
		Matrix c = null;
		double sumIdentity, mulIdentity;
		sumIdentity = 0;// identity of sum;
		mulIdentity = 1;// identity of multiplication;

		if (a.getColumns() == b.getLines()) {
			c = new Matrix(a.getLines(), b.getColumns());
			for (int j = 1; j <= a.getLines(); j++) {
				for (int k = 1; k <= b.getColumns(); k++) {
					sumIdentity = 0;
					for (int i = 1; i <= a.getColumns(); i++) {
						mulIdentity = a.selMatrix(j, i) * b.selMatrix(i, k);
						sumIdentity = sumIdentity + mulIdentity;
					}
					c.setMatrix(j, k, sumIdentity);
				}
			}
		} else {
			System.out
					.printf("the number of columns of the first matrix must be equal to the number of lines of the second one\n");
		}
		return c;
	}

	public static Matrix multiConsMatrix(double f, Matrix a) {
		Matrix r = a.copy();
		r.multiConstMatrix(f);
		return r;
	}

	public Matrix copy() {// returns a new matrix equal to the object which
							// calls this method
		Matrix r = new Matrix(this.lines, this.columns);
		for (int i = 1; i <= lines; i++) {
			for (int j = 1; j <= columns; j++) {
				r.setMatrix(i, j, this.selMatrix(i, j));
			}
		}
		return r;
	}

	public void multiConstMatrix(double f) {
		int i, j;
		double r;
		for (i = 1; i <= lines; i++) {
			for (j = 1; j <= columns; j++) {
				r = f * this.selMatrix(i, j);
				this.setMatrix(i, j, r);
			}
		}
	}

	public void printMatrix() {
		for (int i = 1; i <= lines; i++) {
			for (int j = 1; j <= columns; j++) {
				System.out.printf(" %.10f\t", this.selMatrix(i, j));
			}
			System.out.println();
		}
		System.out.println();
	}

	public String toString() {
		String s = new String();
		for (int i = 1; i <= lines; i++) {
			for (int j = 1; j <= columns; j++) {
				s = s + String.format(" %.10f\t", this.selMatrix(i, j));
			}
			s += "\n";
		}
		s += "\n";
		return s;
	}

	public void fillRandom(double xmin, double xmax) {
		int i, j;
		Random r = new Random();
		for (i = 1; i <= this.getLines(); i++) {
			for (j = 1; j <= this.getColumns(); j++) {
				double x = xmin + (xmax - xmin) * r.nextDouble();
				this.setMatrix(i, j, x);
			}
		}
	}

	public void fillRandomInt(int xmin, int xmax) {
		int i, j;
		Random r = new Random();
		for (i = 1; i <= this.getLines(); i++) {
			for (j = 1; j <= this.getColumns(); j++) {
				int x = xmin + r.nextInt(Math.abs(xmax - xmin) + 1);
				this.setMatrix(i, j, Math.ceil(x));
			}
		}
	}

	public void fillZeros() {
		int i, j;
		for (i = 1; i <= this.getLines(); i++) {
			for (j = 1; j <= this.getColumns(); j++) {
				this.setMatrix(i, j, 0.0);
			}
		}
	}

	public void transpose() {
		int i = 0, j = 0;
		Matrix m;
		m = new Matrix(this.getColumns(), this.getLines());
		for (i = 1; i <= this.lines; i++) {
			for (j = 1; j <= this.columns; j++) {
				m.setMatrix(j, i, this.selMatrix(i, j));
			}
		}
		matrix = m.matrix;
		lines = m.getLines();
		columns = m.getColumns();
	}

	/**
	 * transpose a matrix
	 * 
	 * @param a
	 *            : matrix to be transposed
	 * @return transposed matrix of matrix a
	 */
	public static Matrix transpose(Matrix a) {
		int i = 0, j = 0;
		Matrix aux = new Matrix(a.getColumns(), a.getLines());
		for (i = 1; i <= a.lines; i++) {
			for (j = 1; j <= a.columns; j++) {
				aux.setMatrix(j, i, a.selMatrix(i, j));
			}
		}
		return aux;
	}

	/**
	 * 
	 * @param n
	 *            number of column
	 * @return return a l x 1 matrix where l is number of lines of this matrix.
	 *         return null if n > number of columns
	 * 
	 */
	public Matrix getVectorCol(int n) {
		int l = this.getLines();
		Matrix a = new Matrix(l, 1);
		if (n <= this.getColumns()) {
			for (int i = 1; i <= l; i++)
				a.setMatrix(i, 1, this.selMatrix(i, n));
			return a;
		}
		return null;
	}

	/**
	 * 
	 * @param v
	 *            : is a matrix {number of lines} x 1;
	 * @param n
	 *            : number of column
	 */
	public void setVectorCol(Matrix v, int n) {
		int l = lines;
		if (n <= this.getColumns()) {
			for (int i = 1; i <= l; i++)
				this.setMatrix(i, n, v.selMatrix(i, 1));
		}
	}

	public double[] vectorToDoubleArray() {
		if (this.getColumns() > 1)
			throw new Error("must be a n x 1 matrix");
		else {
			double[] res = new double[this.getLines()];
			for (int i = 1; i <= this.getLines(); i++) {
				res[i - 1] = this.selMatrix(i, 1);
			}
			return res;
		}
	}

	/**
	 * 
	 * @param v1
	 *            : dimension must be n x 1
	 * @param v2
	 *            : dimension must be n x 1
	 * @return
	 */
	public static double vInnerProduct(Matrix v1, Matrix v2) {
		v1 = Matrix.transpose(v1);
		v2 = Matrix.multiMatrix(v1, v2);
		return v2.selMatrix(1, 1);
	}

	/**
	 * 
	 * @param xmin
	 *            first element of the returning vector
	 * @param step
	 * @param xmax
	 *            last element of the returning vector
	 * @return a vector v which dimensions are n * 1, of the form v[i] = xmin +
	 *         i * step, 0 <= i < n, n = floor((xmax - xmin) / step)
	 */
	public static Matrix vectorStep(double xmin, double step, double xmax) {
		int n = (int) Math.ceil((xmax - xmin) / step);
		n++;
		Matrix v = new Matrix(n, 1);
		double x = xmin;
		for (int i = 1; i <= n; i++) {
			v.setMatrix(i, 1, x);
			x += step;
		}
		return v;
	}

	public void identity() {
		this.fillZeros();
		for (int i = 1; i <= this.getLines(); i++) {
			this.setMatrix(i, i, 1.0f);
		}
	}

	public TriVector[][] matrixToSurface(double xmin, double xmax, double ymin,
			double ymax) {
		double dx = (xmax - xmin) / matrix[0].length;
		double dy = (ymax - ymin) / matrix.length;
		TriVector[][] ret = new TriVector[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				double x = xmin + dx * j;
				double y = ymax - dy * i;
				ret[i][j] = new TriVector(x, y, matrix[i][j]);
			}
		}
		return ret;
	}

	public static void main(String[] args) {
		for (int i = 3; i <= 3; i++) {
			double oldTime = System.currentTimeMillis();
			int size = i;
			Matrix a = new Matrix(size, size);
			Matrix b = new Matrix(size, size);
			a.fillRandom(-1, 1);
			b.fillRandom(-1, 1);
			Matrix.multiMatrix(a, b);
			double time = (System.currentTimeMillis() - oldTime) * 1E-03;
			System.out.println(size + "\t" + time);
		}
	}

}
