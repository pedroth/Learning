package functionNode;

public class AddNode extends BinaryNode {

	public AddNode(FunctionNode[] args) {
		super(args);
	}

	public AddNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return args[0].compute(variables) + args[1].compute(variables);
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new AddNode(args);
	}

}
