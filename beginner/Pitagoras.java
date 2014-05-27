package beginner;
import java.util.Scanner;


public class Pitagoras {
	public static void main(String[] args){
		Double x,y,z;
		Scanner s = new Scanner(System.in);
		System.out.println("introduz 1º cateto");
		x = s.nextDouble(); // neste computador, um double é da forma <digitos>,<digitos>
		System.out.println("introduz 2º cateto");
		y = s.nextDouble();
		z= Math.sqrt(x*x + y*y);
		System.out.println("hipotnusa =" + z);
	}
}