package functionNode;

public class MinNode extends BinaryNode{
	public MinNode(FunctionNode[] args) {
		super(args);
	}

	public MinNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return Math.min(args[0].compute(variables),args[1].compute(variables));
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new MinNode(args);
	}
}
