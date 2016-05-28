package functionNode;

public class MultNode extends BinaryNode{
	public MultNode(FunctionNode[] args) {
		super(args);
	}

	public MultNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return args[0].compute(variables) * args[1].compute(variables);
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new MultNode(args);
	}
}
