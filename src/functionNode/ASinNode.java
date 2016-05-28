package functionNode;

public class ASinNode extends UnaryNode {
	
	public ASinNode(FunctionNode[] args) {
		super(args);
	}

	public ASinNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return Math.asin(args[0].compute(variables));
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new ASinNode(args);
	}
	
}
