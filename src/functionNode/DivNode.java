package functionNode;

public class DivNode extends BinaryNode{
	public DivNode(FunctionNode[] args) {
		super(args);
	}

	public DivNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return args[0].compute(variables) / args[1].compute(variables);
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new DivNode(args);
	}
}
