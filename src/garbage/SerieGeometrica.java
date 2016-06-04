package garbage;
import java.util.Scanner;

public class SerieGeometrica {
	static double geo(double a, double r,int n){
		return Math.pow(a*r, n); 
	}
	
	public static void main(String [] args){
		int inf,sup,n;
		double sum=0,u1,r1;
		Scanner s = new Scanner(System.in);
		System.out.println("u1 ?");
		u1= s.nextDouble();
		System.out.println("razao ?");
		r1= s.nextDouble();
		System.out.println("limite inferior?");
		inf= s.nextInt();
		System.out.println("limite superior?");
		sup= s.nextInt();
		for(n=inf;n<sup;n++){
			System.out.println(sum + "[" + n + "]");
			sum=sum + geo(u1,r1,n);
		}
	}
}
