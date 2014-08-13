package teste;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import visualization.Graph2DFrame;

import functions.ExpressionFunction;
/**
 * 
 * @author pedro
 * 
 * works fine until 4th derivative
 * 
 * the best method yet
 *
 */
public class DerivativeTest2 {
	private ExpressionFunction f;
	private double dx = 1E-09;
	private double r = 0.75;

	public DerivativeTest2(ExpressionFunction f) {
		this.f = f;
	}

	public double exp(double x, int n) {
		if (n == 0) {
			return 1;
		} else if (n == 1) {
			return x;
		} else if ((n % 2) == 0) {
			return exp(x * x, n / 2);
		} else if ((n % 2) != 0) {
			return x * exp(x * x, n / 2);
		} else {
			return exp(1 / x, -n);
		}
	}

	public double derivative(double x0, int n, int k) {
		double exp = 1.0 / (k * k);
		if (n == 0)
			return f(x0);
		else
			return (derivative(x0 + exp, n - 1, k) - derivative(x0 - exp, n - 1, k)) / (2 * exp);
	}

	public double f(double x) {
		Double[] v = new Double[1];
		v[0] = x;
		return f.compute(v);
	}

	public double computeNthDerivative(double x0, int n) {
		int k = 1;
		ArrayList<Double> v = new ArrayList<Double>();
		double df;
		double dfPlusOne;
		do {
			dfPlusOne = derivative(x0, n, k + 1);
			df = derivative(x0, n, k);
			v.add(df);
			k++;
		} while (Math.abs(dfPlusOne - df) > dx);
		int size = v.size();
		double[] x = new double[size];
		double[] y = new double[size];
		for(int i = 0; i < size; i++) {
			x[i] = i + 1;
			y[i] = v.get(i);
		}
		Graph2DFrame plot = new Graph2DFrame("k vs derivative");
		plot.addCurve(x, y, Color.blue);
		plot.plot();
		return df;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String func = null;
		int n = 0;
		double x0 = 0;
		String[] var = { "x" };
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("introduce function f(x)");
		try {
			func = in.readLine();
			System.out.println("introduce x0");
			x0 = Double.parseDouble(in.readLine());
			System.out.println("introduce n");
			n = Integer.parseInt(in.readLine());
			ExpressionFunction foo = new ExpressionFunction(func, var);
			foo.init();
			DerivativeTest2 t = new DerivativeTest2(foo);
			System.out.println(t.computeNthDerivative(x0, n));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
