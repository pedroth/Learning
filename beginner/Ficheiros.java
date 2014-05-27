package beginner;
import java.io.*;

public class Ficheiros {
	public static void main(String[] args){
		double r;
		String s1 ;
		String s = "3.1425545";
		File f1 = new File("c:Users/pedrotiago/Desktop/javaTest");
		r = Double.parseDouble(s);
		s1=Double.toString(r);
		System.out.println(r +" "+ s1);
		if(f1.exists()){
			//f1.mkdir(); it doesn't make any directory if it is already made;
			System.out.println("existe");
		}else{
			f1.mkdir();// creates directory with the f1 name, which is javaTest in the Desktop directory;
			System.out.println("n existe");
		}
		try {
			PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "c:Users/pedrotiago/Desktop/javaTest/teste.txt")));
			out.println("ola sou o pedro");
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
