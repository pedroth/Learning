package functionNode;

import functions.ExpressionFunction;

/**
 * 
 * @author pedro
 * 
 *         first input must be a function of the dummy variable plus everything
 *         you like
 * 
 *         ex: sigma(1/2^<dummyVariable>,0, (x - y ) / h);
 * 
 *         how to use in code:
 *         
 *         String[] varTokens = { "u", "x", "y" };
 *         ExpressionFunction foo = new ExpressionFunction("sigma(sigma(u,x,i),x,y)", varTokens);
 *  	   String[] dummyVar ={"i"};
 *		   foo.addFunction("sigma", new Sigma(dummyVar,foo));
 *    	   foo.init();
 *		   Double[] vars = { 3.141592, 1.0, 100.0 };
 *		   System.out.print(foo.compute(vars));
 * 
 * 
 */
public class Sigma extends Functional {
	/**
	 * 
	 * @param args
	 * @param dummyVar
	 *            must be of size 1
	 * @param expr
	 */
	public Sigma(FunctionNode[] args, String[] dummyVar, ExpressionFunction expr) {
		super(dummyVar, expr, 3, args);
	}

	public Sigma(String[] dummyVar, ExpressionFunction expr) {
		super(dummyVar, expr);
		setnVars(3);
	}

	@Override
	public Double compute(Double[] variables) {
		int x = (int) Math.floor(args[1].compute(variables));
		int y = (int) Math.floor(args[2].compute(variables));
		double acm = 0;
		for (int i = x; i <= y; i++) {
			expr.pushDummyVar(this.dummyVarName[0], i);
			acm += args[0].compute(variables);
			expr.popDummyVar(this.dummyVarName[0]);
		}
		return acm;
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new Sigma(args,this.dummyVarName, this.expr);
	}

}
