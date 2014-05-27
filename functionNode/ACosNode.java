package functionNode;

public class ACosNode extends UnaryNode {
	public ACosNode(FunctionNode[] args) {
		super(args);
	}

	public ACosNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return Math.acos(args[0].compute(variables));
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new ACosNode(args);
	}
}
