package other;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.util.Random;

public class Animation extends JFrame {
    Animation() {
        // Set JFrame title
        super("Animation");

        // Set default close operation for JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set JFrame size
        setSize(500, 500);

        // Make JFrame visible
        setVisible(true);
    }
    public void paint(Graphics g) {
        Random r = new Random();
        int a, b, c, d;
        while (true) {
            a = r.nextInt(500);
            b = r.nextInt(500);
            c = r.nextInt(500);
            d = r.nextInt(500);
            g.drawRect(a, b, c, d);
            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
        }
    }

    public static void main(String[] args) {
        new Animation();
    }
}
