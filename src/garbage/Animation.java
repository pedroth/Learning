package garbage;

import java.applet.Applet;
import java.awt.Graphics;
import java.util.Random;

public class Animation extends Applet {
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
}
