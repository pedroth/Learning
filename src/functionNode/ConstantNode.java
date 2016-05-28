package functionNode;

public class ConstantNode extends FunctionNode {
	private Double value;
	
	public ConstantNode(Double value) {
		super(0, null);
		this.value = value;
	}

	@Override
	public Double compute(Double[] variables) {
		return value;
	}

	/**
	 * never will be used
	 */
	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return null;
	}

}
