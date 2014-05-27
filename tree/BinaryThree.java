package tree;

public class BinaryThree {
	private ThreeNode root;
	
	public BinaryThree(){
		root=null;
	}
	
	public void insert(double x1){
		ThreeNode s1,s2;
		double aux;
		s1 = new ThreeNode(x1);
		if(root==null){
			root=s1;
		}else{
			s2= root;
			while(true){
				aux = s2.getThreeReal();
				if(x1>=aux){
					if(s2.getRight()==null){
						s2.setRight(s1);
						break;
					}else{
						s2=s2.getRight();
					}
				}else{
					if(s2.getLeft()==null){
						s2.setLeft(s1);
						break;
					}else{
						s2=s2.getLeft();
					}
				}
			}
		}
	}
	
	public void  printThree(){
		if(root==null){
			System.out.printf("[end]\n");
		}else{
			root.printThreeNodes();
		}	
	}
	
	
	public ThreeNode getRoot(){
		return root;
	}
}