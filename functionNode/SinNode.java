package functionNode;

public class SinNode extends UnaryNode {
	
	public SinNode(FunctionNode[] args) {
		super(args);
	}

	public SinNode() {
		super();
	}

	@Override
	public Double compute(Double[] variables) {
		return Math.sin(args[0].compute(variables));
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new SinNode(args);
	}

}
