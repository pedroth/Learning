package functionNode;

public class NegNode extends UnaryNode{
	
	public NegNode(FunctionNode[] args) {
		super(args);
	}

	public NegNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return - args[0].compute(variables);
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new NegNode(args);
	}
}
