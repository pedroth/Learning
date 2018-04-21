package functionNode;

import functions.DoubleFunction;

public abstract class FunctionNode extends DoubleFunction {
	protected FunctionNode[] args;

	public FunctionNode() {
	}

	public FunctionNode(int nArgs, FunctionNode[] args) {
		super(nArgs);
		this.args = args;
	}
	/**
	 * 
	 * @param args
	 * @return create functionNode object with the args;
	 */
	public abstract FunctionNode createNode(FunctionNode[] args);
}
