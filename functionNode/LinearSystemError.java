package functionNode;

import algebra.Matrix;

public class LinearSystemError extends BinaryNode {
	private double a11, a21, a12, a22;
	private double z1, z2;

	public LinearSystemError(FunctionNode[] args,double a11, double a21, double a12, double a22,
			double z1, double z2) {
		super(args);
		this.a11 = a11;
		this.a21 = a21;
		this.a12 = a12;
		this.a22 = a22;
		this.z1 = z1;
		this.z2 = z2;
	}
	
	public LinearSystemError(double a11, double a21, double a12, double a22,
			double z1, double z2) {
		super();
		this.a11 = a11;
		this.a21 = a21;
		this.a12 = a12;
		this.a22 = a22;
		this.z1 = z1;
		this.z2 = z2;
	}

	public LinearSystemError() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new LinearSystemError(args,a11,a21,a12,a22,z1,z2);
	}

	@Override
	public Double compute(Double[] variables) {
		double x = args[0].compute(variables);
		double y = args[1].compute(variables);
		double ans = (a11*x + a12*y - z1)*(a11*x + a12*y - z1) + (a21*x+a22*y - z2)*(a21*x+a22*y - z2);
		return ans;
	}
}
