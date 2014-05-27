package functionNode;

import functions.ExpressionFunction;

public class DummyVariableNode extends FunctionNode {
	String dummyVarName;
	ExpressionFunction expr;

	public DummyVariableNode(String dummyVarName, ExpressionFunction expr) {
		super(0, null);
		this.dummyVarName = dummyVarName;
		this.expr = expr;
	}

	@Override
	public Double compute(Double[] variables) {
		return expr.peekDummyVar(dummyVarName);
	}

	/**
	 * never will be used
	 */
	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
