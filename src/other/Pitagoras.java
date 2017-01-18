package other;

import java.util.Scanner;


public class Pitagoras {
    public static void main(String[] args) {
        Double x, y, z;
        Scanner s = new Scanner(System.in);
        System.out.println("introduce 1st side in triangle");
        x = s.nextDouble();
        System.out.println("introduce 2st side in triangle");
        y = s.nextDouble();
        z = Math.sqrt(x * x + y * y);
        System.out.println("hypotenuse =" + z);
    }
}