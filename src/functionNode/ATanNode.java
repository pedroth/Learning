package functionNode;

public class ATanNode extends UnaryNode {
	public ATanNode(FunctionNode[] args) {
		super(args);
	}

	public ATanNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return Math.atan(args[0].compute(variables));
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new ATanNode(args);
	}
}
