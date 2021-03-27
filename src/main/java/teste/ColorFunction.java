package teste;

import javax.swing.*;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ColorFunction extends JFrame {

    ColorFunction() {
        // Set JFrame title
        super("Color function");

        // Set default close operation for JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set JFrame size
        setSize(400, 400);

        // Make JFrame visible
        setVisible(true);
    }

    public double colorFunction(int x, int y) {
        return Math.sin(Math.pow(
                this.getWidth() * Math.PI * (x - this.getWidth() / 2), 2)
                + Math.pow(this.getHeight() * Math.PI
                * (y - this.getHeight() / 2), 2));
    }

    public void paint(Graphics g) {
        BufferedImage bImg = new BufferedImage(this.getWidth(),
                this.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g1 = bImg.getGraphics();
        for (int i = 0; i < this.getWidth(); i++) {
            for (int j = 0; j < this.getHeight(); j++) {
                double f = colorFunction(i, j);
                if (f <= 0) {
                    g1.setColor(new Color((float) Math.abs(f), 0.0f, 0.0f));
                    g1.drawLine(i, j, i, j);
                } else {
                    g1.setColor(new Color(0.0f, 0.0f, (float) Math.abs(f)));
                    g1.drawLine(i, j, i, j);
                }
            }
        }
        g.drawImage(bImg, 0, 0, this);
    }

    public static void main(String[] args) {
        new ColorFunction();
    }

}
