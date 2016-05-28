package functions;

public abstract class DoubleFunction implements Function<Double>{
	private int nVars;
	
	public DoubleFunction(){}
	
	public DoubleFunction(int numVariables){
		nVars = numVariables;
	}
	
	public int getnVars() {
		return nVars;
	}

	public void setnVars(int nVars) {
		this.nVars = nVars;
	}

	@Override
	public abstract Double compute(Double[] variables);

}
