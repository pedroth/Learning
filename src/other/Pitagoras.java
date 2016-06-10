package other;
import java.util.Scanner;


public class Pitagoras {
	public static void main(String[] args){
		Double x,y,z;
		Scanner s = new Scanner(System.in);
		System.out.println("introduz 1� cateto");
		x = s.nextDouble(); // neste computador, um double � da forma <digitos>,<digitos>
		System.out.println("introduz 2� cateto");
		y = s.nextDouble();
		z= Math.sqrt(x*x + y*y);
		System.out.println("hipotnusa =" + z);
	}
}