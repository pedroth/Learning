package functionNode;

public class SubNode extends BinaryNode {

	public SubNode(FunctionNode[] args) {
		super(args);
	}

	public SubNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return args[0].compute(variables) - args[1].compute(variables);
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new SubNode(args);
	}

}