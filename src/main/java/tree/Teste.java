package tree;

import java.util.Random;


public class Teste {
    public static void main(String[] args) {
        int i;
        double aux;
        Random r = new Random();
        BinaryThree bt = new BinaryThree();
        for (i = 0; i < 100; i++) {
            aux = Math.floor(r.nextInt(500));
            bt.insert(aux);
        }
        bt.printThree();
    }
}
