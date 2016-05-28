package teste;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import functions.ExpressionFunction;
/**
 * 
 * @author pedro
 * too slow. just works until 4th derivative more less
 */
public class DerivativeTest {
	ExpressionFunction f;
	public static double dx = 1E-6;

	public DerivativeTest(ExpressionFunction f) {
		this.f = f;
	}

	public double f(double x) {
		Double[] v = new Double[1];
		v[0] = x;
		return f.compute(v);
	}

	/**
	 * 
	 * @param x
	 *            middle point
	 * @param h
	 *            interval
	 * @param n
	 *            exponential factor
	 * @return integral[(f(t)(t - x) ^ n) * dt](x - h,x + h)
	 */
	public double beta(double x, double h, int n) {
		double numIterations = (2 * h) / dx;
		double a = x - h;
		double acm = 0;
		double fi;
		double deltaFi;
		for (int i = 0; i < numIterations; i++) {
			fi = fPoly(a + i * dx, x, n);
			deltaFi = fPoly(a + (i + 1) * dx, x, n) - fi;
			acm += (fi + 0.5 * deltaFi) * dx;
		}
		return acm;
	}

	/**
	 * 
	 * @param h
	 *            interval
	 * @param n
	 *            exponential factor
	 * @return integral[((t - x) ^ n) * dt](x - h,x + h) = 0 if(odd(n)), (2 * h
	 *         ^ (n + 1)) / (n + 1) otherwise
	 */
	public double gamma(double h, int n) {
		if (n % 2 == 0)
			return (2 * Math.pow(h, n + 1)) / (n + 1);
		else
			return 0;
	}

	public double poly(double x, double x0, int n) {
		return Math.pow(x - x0, n);
	}

	public double fPoly(double x, double x0, int n) {
		return f(x) * poly(x, x0, n);
	}

	public double S(double x, double h, int n) {
		double acm = 0;
		for (int i = 0; i < n; i++) {
			acm += approximateDerivative(x, h, i) * gamma(h, n + i);
		}
		return acm;
//		return f(x)*gamma(h, n);
	}

	public double approximateDerivative(double x, double h, int n) {
		if (n == 0) {
			return f(x);
		} else if (n > 0) {
			double gamma2n = gamma(h, 2 * n);
			double beta = beta(x, h, n);
			double delta = (beta - S(x, h, n));
			double res = delta / gamma2n;
			return res;
		} else {
			return 1;
		}
	}

	public void run(double x) {
		System.out.println("computing ...");
		double h = 0.01;
		for (int i = 1; i < 11; i++) {
			System.out.println("" + i + "  " + approximateDerivative(x, h, i));
		}
	}

	public static void main(String[] args) {
		String func = null;
		double a = 0;
		String[] var = { "x" };
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("introduce function f(x)");
		try {
			func = in.readLine();
			System.out.println("introduce x0");
			a = Double.parseDouble(in.readLine());

			ExpressionFunction foo = new ExpressionFunction(func, var);
			foo.init();

			DerivativeTest t = new DerivativeTest(foo);
			t.run(a);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
