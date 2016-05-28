package beginner;

import java.applet.Applet;
import java.awt.Frame;
import java.awt.Graphics;

public class PolygonApplet extends Applet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Frame frame;
	public void init(){
		frame = new PolygonFill();
		this.setSize(100, 50);
	}
	
	public void paint(Graphics g){
		update(g);
	}
	
	public void update(Graphics g){
		g.drawString("Frame is open", 0, 25);
	}
	
}
