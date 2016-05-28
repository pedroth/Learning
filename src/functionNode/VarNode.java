package functionNode;

public class VarNode extends FunctionNode {
	int varNum;
	
	public VarNode(int varNum){
		super(0, null);
		this.varNum = varNum;
	}

	@Override
	public Double compute(Double[] variables) {
		return variables[varNum];
	}
	
	/**
	 * never will be used
	 */
	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
