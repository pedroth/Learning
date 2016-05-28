package algebra;

public class TriVector extends Matrix {
	
	public TriVector() {
		super(3,1);
		new TriVector(0,0,0);
	}
	
	public TriVector(double x, double y, double z) {
		super(3, 1);
		super.setMatrix(1, 1, x);
		super.setMatrix(2, 1, y);
		super.setMatrix(3, 1, z);
	}
	
	public void setX(double a) {
		this.setMatrix(1, 1, a);
	}

	public void setY(double a) {
		this.setMatrix(2, 1, a);
	}

	public void setZ(double a) {
		this.setMatrix(3, 1, a);
	}

	public double getX() {
		return selMatrix(1, 1);
	}

	public double getY() {
		return selMatrix(2, 1);
	}

	public double getZ() {
		return selMatrix(3, 1);
	}
	
	public static TriVector vectorProduct(TriVector v1, TriVector v2) {
		TriVector aux;
		aux = new TriVector(v1.getY() * v2.getZ() - v1.getZ() * v2.getY(),
				v1.getZ() * v2.getX() - v1.getX() * v2.getZ(), v1.getX()
						* v2.getY() - v1.getY() * v2.getX());
		return aux;
	}

	public void normalize() {
		double norm;
		norm = Math.sqrt(this.getX() * this.getX() + this.getY() * this.getY()
				+ this.getZ() * this.getZ());
		this.setX((1 / norm) * this.getX());
		this.setY((1 / norm) * this.getY());
		this.setZ((1 / norm) * this.getZ());
	}

	public void Transformation(Matrix m) {
		Matrix aux;
		aux = this;
		aux = Matrix.multiMatrix(m, aux);
		this.setXYZMat(aux);
	}

	public void setXYZMat(Matrix m) {
		this.setX(m.selMatrix(1, 1));
		this.setY(m.selMatrix(2, 1));
		this.setZ(m.selMatrix(3, 1));
	}

	public void sum(TriVector v1) {
		Matrix a = v1;
		Matrix b = this;
		a = Matrix.sumMatrix(a, b);
		this.setXYZMat(a);
	}
	
	public TriVector copy() {
		TriVector res = new TriVector();
		Matrix aux = super.copy();
		res.setXYZMat(aux);
		return res;
	}
	
	public double getLength() {
		return Math.sqrt(Math.pow(this.getX(), 2) + Math.pow(this.getY(), 2)
				+ Math.pow(this.getZ(), 2));
	}
	
	public static TriVector sum(TriVector u, TriVector v) {
		return new TriVector(u.getX() + v.getX(), u.getY() + v.getY(), u.getZ() + v.getZ());
	}
	
	public static TriVector sub(TriVector u, TriVector v) {
		return new TriVector(u.getX() - v.getX(), u.getY() - v.getY(), u.getZ() - v.getZ());
	}
	
	public void multConst(double x) {
		Matrix aux = TriVector.multiConsMatrix(x, this);
		this.setXYZMat(aux);
	}
	
	public static TriVector multConst(double x, TriVector v) {
		return new TriVector(v.getX() * x, v.getY() * x, v.getZ() * x);
	}
	
	public static TriVector Transformation(Matrix m, TriVector v) {
		TriVector ret = new TriVector();
		Matrix aux;
		aux = v;
		aux = Matrix.multiMatrix(m, aux);
		ret.setXYZMat(aux);
		return ret;
	}
}
