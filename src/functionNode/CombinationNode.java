package functionNode;

public class CombinationNode extends BinaryNode {
	
	public CombinationNode(FunctionNode[] args) {
		super(args);
	}

	public CombinationNode() {
		super();
	}
	
	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new CombinationNode(args);
	}
	
	private int combinationsDp(int n, int k) {
		if(n == k || k == 0)
			return 1;
		else if(k > n)
			return 0;
		
		int[][] c = new int[n+1][k+1];
		for(int i = 0; i < n + 1; i++) {
			c[i][0] = 1;
		}
		
		for(int i = 1;i < k + 1; i++) {
			c[i][i] = 1;
		}
		
		for(int i = 2; i < n + 1 ; i++) {
			for(int j = Math.min(i,k); j > 0; j--) {
				c[i][j] = c[i-1][j-1] + c[i-1][j]; 
			}
		}
		
		return c[n][k];
	}

	@Override
	public Double compute(Double[] variables) {
		int n = (int) Math.floor(args[0].compute(variables));
		int k = (int) Math.floor(args[1].compute(variables));
		
		return (double) combinationsDp(n, k);
	}

}
