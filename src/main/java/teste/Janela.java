package teste;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import userGraph.Window;

import javax.swing.*;

public class Janela extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Janela(){
		// Set JFrame title
		super("Janela");
		// Set default close operation for JFrame
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Set JFrame size
		setSize(400, 400);
		// Make JFrame visible
		setVisible(true);
	}

	public void paint(Graphics g) {
		Window wd;
		int i;
		Double r, r1;
		String str;
		Random random = new Random();
		Color c = new Color(random.nextInt(255), random.nextInt(255),
				random.nextInt(255));
		wd = new Window(this, g, c);
		wd.viewWindow(-5, 5, -5, 5);
		wd.setXYDisplacement(0, 0);
		wd.drawLine(-5, 0, 5, 0);
		wd.drawLine(0, -5, 0, 5);
		for (i = 0; i <= 10; i++) {
			r = wd.getXmin() + 10 / 9 * i;
			str = r.toString();
			wd.drawString(str, r, 0);
		}
		wd.drawLine(-5, -3.14, 5, 3.2);
		wd.fillTriangle(1, 1, 2, 1, 2, 2);
		wd.fillRect(1.0, 1.0, 0.1, 0.1);
		r = wd.pxl_xstep();
		r1 = wd.pxl_ystep();
		// wd.drawLine(1, 1, 1+r, 1+r1);
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			//
		}
		wd.setBkgColor(c); //erase all the screen content

	}

	public static void main(String[] args) {
		new Janela();
	}
}
