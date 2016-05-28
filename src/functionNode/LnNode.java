package functionNode;

public class LnNode extends UnaryNode {
	
	public LnNode(FunctionNode[] args) {
		super(args);
	}

	public LnNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return Math.log(args[0].compute(variables));
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new LnNode(args);
	}

}