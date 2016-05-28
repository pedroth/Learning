package tree;

public class ThreeNode {
	private double x;
	private ThreeNode left,right;
	
	ThreeNode(double a){
		x=a;
		left=null;
		right=null;
	}
	
	public void setThreeReal(double x1){
		x=x1;
	}
	
	public void setLeft(ThreeNode t){
		left=t;
	}
	public void setRight(ThreeNode t){
		right=t;
	}
	
	public double getThreeReal(){
		return x;
	}
	
	public ThreeNode getLeft(){
		return left;
	}
	
	public ThreeNode getRight(){
		return right;
	}
	
	public void printThreeNodes(){
		ThreeNode t1,t2;
		System.out.print(this.getThreeReal() + "\t");
		t1 = this.getLeft();
		t2 = this.getRight();
		if(t1==null){
			System.out.printf("[end]\n"); 
		}else{
			t1.printThreeNodes();
		}
		if(t2==null){
			System.out.printf("[end]\n");
		}else{
			t2.printThreeNodes();
		}
	}
}
