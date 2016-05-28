package functionNode;

public class TriAverageNode extends FunctionNode {
	
	public TriAverageNode(FunctionNode[] args) {
		super(3,args);
	}

	public TriAverageNode() {
		super();
		setnVars(3);
	}

	@Override
	public Double compute(Double[] variables) {
		return (args[0].compute(variables) + args[1].compute(variables) + args[2].compute(variables)) / 3;
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new TriAverageNode(args);
	}

}