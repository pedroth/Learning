package functionNode;

public class TanNode extends UnaryNode {
	
	public TanNode(FunctionNode[] args) {
		super(args);
	}

	public TanNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return Math.tan(args[0].compute(variables));
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new TanNode(args);
	}

}