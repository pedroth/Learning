package functionNode;

import java.awt.Color;
import java.util.Random;

import visualization.Graph2DFrame;
import functions.ExpressionFunction;

/**
 * 
 * @author pedro
 * 
 *         first input must be a function of the dummy variable plus everything
 *         you like
 * 
 *         ex: pedro(cos(<dummyVariable>),sin(<dummyVariable>),0,2*pi,x,y);
 * 
 *         works better on regular curves, for the rest doesn't really works
 * 
 * 
 */
public class PedroNode extends Functional {
	private Double[] tempVariables;
	private double minDummyDomain, maxDummyDomain;

	public PedroNode(FunctionNode[] args, String[] dummyVar,
			ExpressionFunction expr) {
		super(dummyVar, expr, 6, args);
	}

	/**
	 * 
	 * @param args
	 * @param dummyVar
	 *            must be of size 1
	 * @param expr
	 */
	public PedroNode(String[] dummyVar, ExpressionFunction expr) {
		super(dummyVar, expr);
		setnVars(6);
	}

	private double[] u(double t) {
		double[] ret = new double[2];
		expr.pushDummyVar(this.dummyVarName[0], t);
		ret[0] = args[0].compute(tempVariables);
		ret[1] = args[1].compute(tempVariables);
		expr.popDummyVar(this.dummyVarName[0]);
		return ret;
	}

	private double[] du(double t) {
		double[] ret;
		double dt = 1E-9;
		double[] dh = u(t + dt);
		double[] d = u(t);
		ret = diff(dh, d);
		ret[0] *= (1 / dt);
		ret[1] *= (1 / dt);
		return ret;
	}

	private double derivativeNorm(double t) {
		double[] dx = du(t);
		return Math.sqrt(dx[0] * dx[0] + dx[1] * dx[1]);
	}

	private double[] diff(double[] x, double[] y) {
		double[] ret = new double[2];
		ret[0] = x[0] - y[0];
		ret[1] = x[1] - y[1];
		return ret;
	}

	private double dot(double[] u, double[] v) {
		return u[0] * v[0] + u[1] * v[1];
	}

	private double findArgMin(double x, double y) {
		double oldTime = System.currentTimeMillis();
		double time = 0;
		double t = randomPointInInterval(minDummyDomain, maxDummyDomain);
		double[] xy = new double[2];
		xy[0] = x;
		xy[1] = y;
		double grad = 1E3;
		while (Math.abs(grad) > 1E-3 && time < 0.01) {
			grad = dot(diff(u(t), xy), du(t));
			t = t - 0.1 * grad;
			time += (System.currentTimeMillis() - oldTime) * 1E-03;
			oldTime = System.currentTimeMillis();
		}

		return t;
	}

	private double findArgMinGauss(double x, double y, double myu) {
		double oldTime = System.currentTimeMillis();
		double time = 0;
		double t = randomGaussPoint(myu,
				Math.abs((maxDummyDomain - minDummyDomain) / 16));
		double[] xy = new double[2];
		xy[0] = x;
		xy[1] = y;
		double grad = 1E3;
		while (Math.abs(grad) > 1E-3 && time < 0.01) {
			grad = dot(diff(u(t), xy), du(t));
			t = t - 0.1 * grad;
			time += (System.currentTimeMillis() - oldTime) * 1E-03;
			oldTime = System.currentTimeMillis();
		}

		return t;
	}

	private double[] normalizeDerivative(double t) {
		double[] dv = du(t);
		double norm = derivativeNorm(t);
		dv[0] *= 1 / norm;
		dv[1] *= 1 / norm;
		return dv;
	}

	private double[] rotatePIOverTwoLeft(double[] u) {
		double temp = u[0];
		u[0] = -u[1];
		u[1] = temp;
		return u;
	}

	private double randomPointInInterval(double xmin, double xmax) {
		Random r = new Random();

		return xmin + (xmax - xmin) * r.nextDouble();
	}

	private double randomGaussPoint(double myu, double sigma) {
		Random r = new Random();

		return (r.nextGaussian() + myu) * sigma;
	}

	public double distanceFunction(double t, double x, double y) {
		double[] xy = { x, y };
		double[] dv = diff(u(t), xy);
		return dot(dv, dv);
	}

	@Override
	public Double compute(Double[] variables) {
		tempVariables = variables;
		minDummyDomain = args[2].compute(variables);
		maxDummyDomain = args[3].compute(variables);
		double x = args[4].compute(variables);
		double y = args[5].compute(variables);
		double[] xy = { x, y };
		double min = Double.MAX_VALUE;
		int n = 100;
		double tMin = 0;
		for(int i = 0; i < n; i++) {
			double tAux = findArgMin(x, y);
			double f = distanceFunction(tAux, x, y);
			if(f < min) {
				min = f;
				tMin = tAux;
			}
		}
		
		// int NUM = 1000;
		// double tx[] = new double[NUM];
		// double ty[] = new double[NUM];
		// for(int i = 0; i < NUM; i++) {
		// tx[i] = minDummyDomain + ((maxDummyDomain-minDummyDomain) / NUM) * i;
		// ty[i] = dot(diff(u(tx[i]),xy),diff(u(tx[i]),xy));
		// }
		// Graph2DFrame frame = new Graph2DFrame("t vs f" + xy[0] + "  " +
		// xy[1]);
		// frame.addCurve(tx, ty, Color.blue);
		// frame.setRepainting(false);
		// frame.plot();
		return dot(rotatePIOverTwoLeft(normalizeDerivative(tMin)),
				diff(u(tMin), xy));
	}

	// private double minimizeDistanceFunction(double x, double y) {
	// double tMin, min, tLevel, minLevel, epsilon;
	// int maxIte = 7;
	// min = Double.MAX_VALUE;
	// minLevel = Double.MAX_VALUE;
	// epsilon = 1E-2;
	// tLevel = randomPointInInterval(minDummyDomain, maxDummyDomain);
	// tMin = 0;
	// for(int i = 0; i < maxIte; i++) {
	// double t = findArgMin(x, y, tLevel);
	// double newMin = distanceFunction(t, x, y);
	// tLevel = findLevelSet(x, y, min);
	// if(min > newMin) {
	// tMin = t;
	// min = newMin;
	// }
	// }
	// return tMin;
	// }
	//
	// private double findLevelSet(double x, double y, double z) {
	// double tMin = 0;
	// double min = Double.MAX_VALUE;
	// int maxIte = 7;
	// for (int i = 0; i < maxIte; i++) {
	// double t = findLevelSetArg(x, y, z);
	// double cost = levelSetCost(t, x, y, z);
	// if (t<minDummyDomain || t > maxDummyDomain) {
	// continue;
	// }
	// if (min > cost) {
	// tMin = t;
	// min = cost;
	// }
	// }
	// return tMin;
	// }
	//
	// private double findLevelSetArg(double x, double y, double z) {
	// double oldTime = System.currentTimeMillis();
	// double time = 0;
	// double t = randomPointInInterval(minDummyDomain, maxDummyDomain);
	// double[] xy = new double[2];
	// xy[0] = x;
	// xy[1] = y;
	// double grad = 1E3;
	// while (Math.abs(grad) > 1E-3 && time < 0.01) {
	// grad = dot(diff(u(t), xy), du(t)) * (distanceFunction(t, x, y) - z);
	// t = t - 0.1 * grad;
	// time += (System.currentTimeMillis() - oldTime) * 1E-03;
	// oldTime = System.currentTimeMillis();
	// }
	//
	// return t;
	// }
	//
	// private double levelSetCost(double t, double x, double y, double z) {
	// return (distanceFunction(t, x, y) - z)
	// * (distanceFunction(t, x, y) - z);
	// }
	//
	// private double findArgMin(double x, double y, double tInit) {
	// double oldTime = System.currentTimeMillis();
	// double time = 0;
	// double t = tInit;
	// double[] xy = new double[2];
	// xy[0] = x;
	// xy[1] = y;
	// double grad = 1E3;
	// while (Math.abs(grad) > 1E-3 && time < 0.01) {
	// grad = dot(diff(u(t), xy), du(t));
	// t = t - 0.1 * grad;
	// time += (System.currentTimeMillis() - oldTime) * 1E-03;
	// oldTime = System.currentTimeMillis();
	// }
	//
	// return t;
	// }

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new PedroNode(args, this.dummyVarName, this.expr);
	}

}