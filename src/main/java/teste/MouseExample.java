package teste;

import java.applet.Applet;
import java.awt.Button;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import userGraph.Window;


public class MouseExample extends Applet implements MouseMotionListener,ActionListener,KeyListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int xm,ym;
	double x,y,w,h;
	Window wd;
	Button b;
	boolean drag;
	
	public void init(){
		setLayout(null);// tells the applet that you control the size of buttons,etc..
		drag=false;
		x=1.0;
		y=1.0;
		w=0.5;
		h=0.5;
		wd = new Window(this,this.getGraphics(),Color.white);
		wd.viewWindow(-10.0, 10.0, -10.0, 10.0);
		wd.setDrawColor(Color.red);
		b = new Button("+");
		b.addActionListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		// it seems better to put setBounds here instead of in paint(), but it does not actualize the shape if we change the size of the window.
		this.add(b);
		this.requestFocus();
		this.setFocusable(true);// turn on the keyboard;
		this.setSize(800, 1000);
		//wd.setBkgColor(Color.blue);
	}
	
	public void paint(Graphics g){
		Double r1,r2;
		String s1,s2;
		b.setBounds(wd.getPxlWidth()/2, wd.getPxlHeight()/2, 20, 20);
		if(drag){
			wd.setDrawColor(Color.green);
		}else{
		wd.setDrawColor(Color.red);
		}
		wd.fillRect(x, y, w, h);
		wd.setDrawColor(Color.black);
		r1 = wd.InverseCoordX(xm);
		r2 = wd.InverseCoordY(ym);
		s1 = r1.toString();
		s2 = r2.toString();
		s1 = s1.substring(0, 4);
		s2 = s2.substring(0, 4);
		//works with either g.draw... or wd.drawS.... they are equal.
		g.drawString("("+ s1 +"," + s2 +")", xm, ym);
		//wd.drawString("("+ s1 + "," + s2 + ")" , wd.InverseCoordX(xm), wd.InverseCoordY(ym));
		
	}
	
	
	
	@Override
	public void mouseDragged(MouseEvent e) {
		//wd.setBkgColor(wd.getBkgColor()); // clear screen;
		drag=true;
		x = wd.InverseCoordX(e.getX())-w/2;
		y = wd.InverseCoordY(e.getY())-h/2;
		xm= e.getX();
		ym= e.getY();
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		drag = false;
		xm =e.getX();
		ym= e.getY();
		repaint();
		
	}


	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_PLUS){
			w+=0.1;
			h+=0.1;
			repaint();
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		char c;
		c = e.getKeyChar();
		if(c == '+'){
			w+=0.1;
			h+=0.1;
			repaint();
		}else if( c == '-'){
			w-=0.1;
			h-=0.1;
			repaint();
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == b){
			w+=0.1;
			h+=0.1;
			this.requestFocus();
			this.setFocusable(true);// turn on the keyboard;
			repaint();
		}
		
	}

}
