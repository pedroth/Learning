package other;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class nthDerivative {
	public static double dx = 1E-9;
	
	public static Double nthDerivativeFunction(Double x0, int n) {
		if (n == 0)
			return f(x0);
		else
			return (nthDerivativeFunction(x0 + dx * Math.pow(10, n + 1), n - 1) - nthDerivativeFunction(
					x0 - dx * Math.pow(10, n + 1), n - 1))
					/ (2 * dx * Math.pow(10, n + 1));
	}
		
	public static String nthDerivativeFunctionString(String x0, int n) {
		if (n == 0){
			return "f("+ x0 + ")";
		}
		else{
			return "((" + nthDerivativeFunctionString(x0 + " + dx",n-1) + " - " + nthDerivativeFunctionString(x0,n-1) + ")" + " / dx )"; 
		}
	}
	
	public static Double f(Double x0){
		return Math.pow(x0, 4);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		Double x;
		
		while (true) {
			System.out.println("derivada de ?");
			try {
				x = Double.parseDouble(in.readLine());
				break;
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
		}
		
		Double y =((((f(x + dx + dx) - f(x + dx)) / dx ) - ((f(x + dx) - f(x)) / dx )) / dx );
		for(int i = 1; i < 11 ; i++) {
			System.out.println(nthDerivativeFunction(x,i));
			System.out.println(nthDerivativeFunctionString("x",i));
		}
	}

}
