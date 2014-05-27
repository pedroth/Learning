package apps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.timer.TimerMBean;
import javax.swing.JFrame;

import window.ImageWindow;
import windowThreeDim.Element;
import windowThreeDim.PaintMethod;
import windowThreeDim.Quad;
import windowThreeDim.SamplingZbuffer;
import windowThreeDim.FlatShader;
import windowThreeDim.SquareZBuffer;
import windowThreeDim.TriWin;
import windowThreeDim.Triangle;
import windowThreeDim.WiredPrespective;
import windowThreeDim.ZBufferPrespective;
import algebra.Matrix;
import algebra.TriVector;

public class Tetra extends JFrame implements MouseListener,
		MouseMotionListener, KeyListener {
	/**
* 
*/
	private static final long serialVersionUID = 1L;
	private int wChanged, hChanged;
	private TriWin graphics;
	private ImageWindow wd;
	private double theta, phi;
	private int mx, my, newMx, newMy;
	private double raw;
	private double velocity;
	private double accelaration;
	private double oldTime;
	private double currentTime;
	private Timer timer;
	private double thrust;
	private boolean zBufferOn;
	private PaintMethod paint;
	private FrameCounter fps;

	public Tetra() {
		// Set JFrame title
		super("Draw Tetra");

		// init
		zBufferOn = true;
		graphics = new TriWin();
		wd = graphics.getBuffer();
		paint = new ZBufferPrespective();
		graphics.setMethod(paint);
		buildTetra();
		wd.setBackGroundColor(Color.white);
		theta = 0;
		phi = 0;

		/**
		 * there is no need for the instruction below
		 */
		// // Set default close operation for JFrame
		 setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set JFrame size
		setSize(800, 550);

		wChanged = this.getWidth();
		hChanged = this.getHeight();

		wd.setWindowSize(wChanged, hChanged);

		// Make JFrame visible
		setVisible(true);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		raw = 3;
		velocity = 0;
		accelaration = 0;
		oldTime = (System.currentTimeMillis()) * 1E-03;
		timer = new Timer();
		thrust = 0;
		timer.schedule(new Euler(), 0, 1);
		fps = new FrameCounter();
		timer.schedule(fps, 0,1000);
		orbit(theta, phi);
	}
	
	public class FrameCounter extends TimerTask {
		private int nFrames;
		public FrameCounter() {
			nFrames = 0;
		}
		@Override
		public void run() {
			String s = "Tetra FPS : " + nFrames;
			Tetra.this.setTitle(s);
			nFrames = 0;
		}
		
		public void count(){
			nFrames++;
		}
		
	}

	private void buildTetra() {
		TriVector p1 = new TriVector(0.5, -0.5, -0.5);
		TriVector p2 = new TriVector(0.5, 0.5, -0.5);
		TriVector p3 = new TriVector(0, 0, 1);
		Element e = new Triangle(p1, p2, p3);
		e.setColor(Color.black);
		graphics.addtoList(e);
		p1 = new TriVector(0.5, 0.5, -0.5);
		p2 = new TriVector(-0.5, 0.5, -0.5);
		p3 = new TriVector(0, 0, 1);
		e = new Triangle(p1, p2, p3);
		e.setColor(Color.blue);
		graphics.addtoList(e);
		p1 = new TriVector(-0.5, 0.5, -0.5);
		p2 = new TriVector(-0.5, -0.5, -0.5);
		p3 = new TriVector(0, 0, 1);
		e = new Triangle(p1, p2, p3);
		e.setColor(Color.red);
		graphics.addtoList(e);
		p1 = new TriVector(-0.5, -0.5, -0.5);
		p2 = new TriVector(0.5, -0.5, -0.5);
		p3 = new TriVector(0, 0, 1);
		e = new Triangle(p1, p2, p3);
		e.setColor(Color.green);
		graphics.addtoList(e);
	}

	private void buildTri() {
		Random r = new Random();
		int n = 10;
		for(int i = 0; i < n; i++){
			TriVector p1 = RandomPointInCube();
			TriVector p2 = RandomPointInCube();
			TriVector p3 = RandomPointInCube();
			Element e = new Triangle(p1, p2, p3);
			e.setColor(new Color(r.nextInt(255),r.nextInt(255),r.nextInt(255)));
			graphics.addtoList(e);
		}
	}

	private void buildCube() {
		TriVector p1 = new TriVector(0.5, -0.5, -0.5);
		TriVector p2 = new TriVector(0.5, 0.5, -0.5);
		TriVector p3 = new TriVector(0.5, 0.5, 0.5);
		TriVector p4 = new TriVector(0.5, -0.5, 0.5);
		Element e = new Quad(p1, p2, p3, p4);
		e.setColor(Color.black);
		graphics.addtoList(e);
		p1 = new TriVector(0.5, 0.5, -0.5);
		p2 = new TriVector(-0.5, 0.5, -0.5);
		p3 = new TriVector(-0.5, 0.5, 0.5);
		p4 = new TriVector(0.5, 0.5, 0.5);
		e = new Quad(p1, p2, p3, p4);
		e.setColor(Color.blue);
		graphics.addtoList(e);
		p1 = new TriVector(-0.5, 0.5, -0.5);
		p2 = new TriVector(-0.5, 0.5, 0.5);
		p3 = new TriVector(-0.5, -0.5, 0.5);
		p4 = new TriVector(-0.5, -0.5, -0.5);
		e = new Quad(p1, p2, p3, p4);
		e.setColor(Color.red);
		graphics.addtoList(e);
		p1 = new TriVector(-0.5, -0.5, -0.5);
		p2 = new TriVector(-0.5, -0.5, 0.5);
		p3 = new TriVector(0.5, -0.5, 0.5);
		p4 = new TriVector(0.5, -0.5, -0.5);
		e = new Quad(p1, p2, p3, p4);
		e.setColor(Color.green);
		graphics.addtoList(e);
		p1 = new TriVector(0.5, -0.5, -0.5);
		p2 = new TriVector(0.5, 0.5, -0.5);
		p3 = new TriVector(-0.5, 0.5, -0.5);
		p4 = new TriVector(-0.5, -0.5, -0.5);
		e = new Quad(p1, p2, p3, p4);
		e.setColor(Color.gray);
		graphics.addtoList(e);
		p1 = new TriVector(0.5, -0.5, 0.5);
		p2 = new TriVector(0.5, 0.5, 0.5);
		p3 = new TriVector(-0.5, 0.5, 0.5);
		p4 = new TriVector(-0.5, -0.5, 0.5);
		e = new Quad(p1, p2, p3, p4);
		e.setColor(Color.orange);
		graphics.addtoList(e);
	}

	private TriVector RandomPointInCube() {
		Random r = new Random();
		double x[] = new double[3];
		for (int i = 0; i < 3; i++) {
			x[i] = -1 + 2*r.nextDouble();
		}
		return new TriVector(x[0], x[1], x[2]);
	}

	class Euler extends TimerTask {

		@Override
		public void run() {
			currentTime = (System.currentTimeMillis()) * 1E-03;
			double dt = currentTime - oldTime;
			oldTime = currentTime;
			accelaration = -velocity + thrust;
			velocity += accelaration * dt;
			raw += velocity * dt;
			orbit(theta, phi);
			repaint();
		}
	}

	public void paint(Graphics g) {
		if (Math.abs(wChanged - this.getWidth()) > 0
				|| Math.abs(hChanged - this.getHeight()) > 0) {
			wd.setWindowSize(this.getWidth(), this.getHeight());
			wChanged = this.getWidth();
			hChanged = this.getHeight();
		}
		update(g);
	}

	public void update(Graphics g) {
		wd.clearImageWithBackGround();
		graphics.drawElements();
//		System.out.println("" + raw);
		wd.paint(g);
		fps.count();
	}

	public static void main(String[] args) {
		new Tetra();
	}

	public void orbit(double t, double p) {

		Matrix aux = new Matrix(3, 3);
		// z - axis
		aux.setMatrix(1, 3, -Math.cos(p) * Math.cos(t));
		aux.setMatrix(2, 3, -Math.cos(p) * Math.sin(t));
		aux.setMatrix(3, 3, -Math.sin(p));
		// y - axis
		aux.setMatrix(1, 2, -Math.sin(p) * Math.cos(t));
		aux.setMatrix(2, 2, -Math.sin(p) * Math.sin(t));
		aux.setMatrix(3, 2, Math.cos(p));
		// x -axis
		aux.setMatrix(1, 1, -Math.sin(t));
		aux.setMatrix(2, 1, Math.cos(t));
		aux.setMatrix(3, 1, 0);
		TriVector eye = new TriVector(raw * Math.cos(p) * Math.cos(t), raw
				* Math.cos(p) * Math.sin(t), raw * Math.sin(p));

		graphics.setCamera(aux, eye);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		newMx = e.getX();
		newMy = e.getY();
		double dx = newMx - mx;
		double dy = newMy - my;
		theta += 2 * Math.PI * (dx / wChanged);
		phi += 2 * Math.PI * (dy / hChanged);

		orbit(theta, phi);
		repaint();

		mx = newMx;
		my = newMy;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_W) {
			thrust = -5;
		} else if (arg0.getKeyCode() == KeyEvent.VK_S) {
			thrust = +5;
		} else if (arg0.getKeyCode() == KeyEvent.VK_1) {
			graphics.removeAllElements();
			buildTetra();
		} else if (arg0.getKeyCode() == KeyEvent.VK_2) {
			graphics.removeAllElements();
			buildTri();
		} else if (arg0.getKeyCode() == KeyEvent.VK_3) {
			graphics.removeAllElements();
			buildCube();
		} else if (arg0.getKeyCode() == KeyEvent.VK_Z) {
			zBufferOn = !zBufferOn;
			if(zBufferOn){
				graphics.setMethod(new ZBufferPrespective());
			}else {
				graphics.setMethod(new WiredPrespective());
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		thrust = 0;
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}
}
