package other;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Factorial {
    public static Integer fact(Integer n) {
        if (n == 0)
            return 1;
        else
            return n * fact(n - 1);

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int i;
        while (true) {
            System.out.println("factorial de ?");
            try {
                i = Integer.parseInt(in.readLine());
                break;
            } catch (NumberFormatException | IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(fact(i));
    }

}
