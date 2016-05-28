package beginner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Factorial {

	public static Integer fact(Integer n){
		if(n==0)
			return 1;
		else
			return n*fact(n-1);
		
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		Integer i;
		
		while (true) {
			System.out.println("factorial de ?");
			try {
				i = Integer.parseInt(in.readLine());
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
		
		System.out.println(fact(i));
	}

}
