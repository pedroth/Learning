package functionNode;

public class CosNode extends UnaryNode {
	
	public CosNode(FunctionNode[] args) {
		super(args);
	}

	public CosNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return Math.cos(args[0].compute(variables));
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new CosNode(args);
	}

}
