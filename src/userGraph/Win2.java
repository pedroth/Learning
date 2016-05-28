package userGraph;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;


public class Win2 extends Applet implements MouseMotionListener{
	Window wd1;
	Window wd2;
	double[] p1;
	double[] p2;
	double xm;
	double ym;
	
	public void init(){
		wd1 = new Window(this,this.getGraphics(),Color.white);
		wd2 = new Window(this,this.getGraphics(),Color.white);
		wd1.viewWindow(-5, 5, -5, 5);
		wd2.viewWindow(-5, 5, -5, 5);
		wd1.setPercWidth(0.5);
		wd2.setPercWidth(0.5);
		wd1.setPercHeight(0.5);
		wd2.setPercHeight(1);
		wd1.setBkgColor(Color.white);
		wd2.setXYDisplacement((int)(this.getWidth()/2), 0);
		p1 = new double[4];
		p2 = new double[4]; 
		p1[0] = -1;
		p1[1] =  1;
		p1[2] =  1;
		p1[3] = -1;
		p2[0] = -1;
		p2[1] = -1;
		p2[2] =  1;
		p2[3] =  1;
		this.addMouseMotionListener(this);
	}
	

	public void paint(Graphics g){
		int i;
		wd2.setXYDisplacement((int)(this.getWidth()/2), 0);
		for(i = 0;i<4;i++){
			wd1.drawLine(p1[i%4], p2[i%4], p1[(i+1)%4], p2[(i+1)%4]);
			wd2.setDrawColor(Color.red);
			wd2.drawLine(p1[i%4], p2[i%4], p1[(i+1)%4], p2[(i+1)%4]);
		}
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(e.getX()<=wd1.getPxlWidth() && e.getY()<=wd1.getPxlHeight()){
		p1[0]=  wd1.InverseCoordX(e.getX());
		p2[0] = wd1.InverseCoordY(e.getY());
		xm= e.getX();
		ym= e.getY();
		repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
}
