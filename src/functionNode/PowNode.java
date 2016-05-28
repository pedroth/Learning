package functionNode;

public class PowNode extends BinaryNode {
	public PowNode(FunctionNode[] args) {
		super(args);
	}

	public PowNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return Math.pow(args[0].compute(variables), args[1].compute(variables));
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new PowNode(args);
	}
}
