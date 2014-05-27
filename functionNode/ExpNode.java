package functionNode;

public class ExpNode extends UnaryNode {
	
	public ExpNode(FunctionNode[] args) {
		super(args);
	}

	public ExpNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return Math.exp(args[0].compute(variables));
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new ExpNode(args);
	}

}