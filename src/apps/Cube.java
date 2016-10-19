package apps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Random;
import java.util.TimerTask;

import javax.swing.JFrame;

import visualization.TextFrame;
import window.ImageWindow;
import windowThreeDim.Element;
import windowThreeDim.Point;
import windowThreeDim.Quad;
import windowThreeDim.TriWin;
import windowThreeDim.WiredPrespective;
import algebra.Matrix;
import algebra.TriVector;

public class Cube extends JFrame implements MouseListener, MouseMotionListener,
		KeyListener {
	
	private static final long serialVersionUID = 1L;
	private int wChanged, hChanged;
	private TriWin graphics, graphParticles;
	private ImageWindow wd;
	private double theta, phi;
	private int mx, my, newMx, newMy;
	private double raw;
	private double velocity;
	private double accelaration;
	private double oldTime;
	private double currentTime;
	private double thrust;

	private int stateField;
	private boolean forceOrVelocity;
	private boolean loopOn;
	private TriVector[] points;
	/**
	 * velocities of the particles
	 */
	private TriVector[] velocities;
	private TriVector p, q;
	/**
	 * velocities of the planets
	 */
	private TriVector vp, vq;
	private Euler2 euler2;
	private Euler euler;
	private double time;
	private double dt;
	private static int numParticles = 10000;
	
	private double loopSize = 3;
	
	private static String helpText = "< [1-6] > : various vector fields \n\n" +
			"< w > : Camera move foward / zoom in \n\n" +
			"< s > : Camera move backward /zoom out \n\n" +
			"< l > : loop mode toggle \n\n" +
			"< [+,-] > : increase / decrease loop box size \n\n" +
			"< v > : toggle between velocity field and acceleration field\n\n" +
			"Made by Pedroth";

	public Cube(boolean isApplet) {
		// Set JFrame title
		super("Draw Cube");

		// init

		graphics = new TriWin();
		graphParticles = new TriWin();
		wd = graphics.getBuffer();
		graphParticles.setBuffer(wd);
		euler2 = new Euler2();
		euler = new Euler();
		graphics.setMethod(new WiredPrespective());
		buildCube();
		graphParticles.setMethod(new WiredPrespective());
		wd.setBackGroundColor(Color.black);
		theta = 0;
		phi = 0;
		forceOrVelocity = true;
		loopOn = false;
		stateField = 1;

		// Set default close operation for JFrame
		if(!isApplet) {
			 setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 }
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
		time = 0;
		dt = 0;
		oldTime = (System.currentTimeMillis()) * 1E-03;
		thrust = 0;

		orbit(theta, phi);
		points = new TriVector[numParticles];
		velocities = new TriVector[numParticles];
		buildParticles();

	}

	private void buildCube() {
		TriVector p1 = new TriVector(0.5, -0.5, -0.5);
		TriVector p2 = new TriVector(0.5, 0.5, -0.5);
		TriVector p3 = new TriVector(0.5, 0.5, 0.5);
		TriVector p4 = new TriVector(0.5, -0.5, 0.5);
		Element e = new Quad(p1, p2, p3, p4);
		e.setColor(Color.white);
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
	}

	private void buildParticles() {
		p = RandomPointInCube();
		q = RandomPointInCube();
		vp = RandomPointInCube();
		vq = RandomPointInCube();
		for (int i = 0; i < numParticles; i++) {
			points[i] = RandomPointInCube();
			velocities[i] = new TriVector();
		}
	}

	private TriVector RandomPointInCube() {
		Random r = new Random();
		double x[] = new double[3];
		for (int i = 0; i < 3; i++) {
			x[i] = -0.5 + r.nextDouble();
		}
		return new TriVector(x[0], x[1], x[2]);
	}

	class Euler extends TimerTask {

		@Override
		public void run() {
			currentTime = (System.currentTimeMillis()) * 1E-03;
			dt = currentTime - oldTime;
			time += dt;
			oldTime = currentTime;
			accelaration = -velocity + thrust;
			velocity += accelaration * dt;
			raw += velocity * dt;
			orbit(theta, phi);
			euler2.run();
			repaint();
		}
	}

	class Euler2 extends TimerTask {
		public Euler2() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run() {
			computeParticlesPosition(dt);
		}

		private void computeParticlesPosition(double dt) {
			Element e;
			TriVector a = new TriVector();
			Matrix aux;
			graphParticles.removeAllElements();
			computeGravityPoints(dt);
			for (int i = 0; i < numParticles; i++) {
				if (forceOrVelocity) {
					a = f(points[i]);
					/**
					 * v[i] = a * dt + v[i];
					 */
					a.multiConstMatrix(dt);
					a.sum(velocities[i]);
					/**
					 * dont need to make a.copy because setXYZ copies the
					 * doubles inside matrix a
					 */
					velocities[i].setXYZMat(a);
				} else {
					velocities[i] = f(points[i]);
				}
				/**
				 * p[i] = v[i] * dt + p[i];
				 */
				aux = TriVector.multiConsMatrix(dt, velocities[i]);
				a.setXYZMat(aux);
				a.sum(points[i]);
				points[i].setXYZMat(a);

				if (loopOn)
					loopCheck(points[i]);

				e = new Point(points[i]);
				e.setColor(Color.white);
				graphParticles.addtoList(e);
			}
		}

		private void computeGravityPoints(double dt) {
			double d;
			TriVector alfa;
			Matrix aux;
			alfa = new TriVector();
			d = Math.sqrt((p.getX() - q.getX()) * (p.getX() - q.getX())
					+ (p.getY() - q.getY()) * (p.getY() - q.getY())
					+ (p.getZ() - q.getZ()) * (p.getZ() - q.getZ()));
			/**
			 * a = (1/|q-p|^2)(q - p)
			 * 
			 * v = a * dt + v
			 */
			Matrix v = TriVector.subMatrix(q, p);
			alfa.setXYZMat(v);
			alfa.multiConstMatrix(dt / d * d);
			vp.sum(alfa);
			/**
			 * p = v * dt + p
			 */
			aux = TriVector.multiConsMatrix(dt, vp);
			alfa.setXYZMat(aux);
			p.sum(alfa);

			/**
			 * same thing for q
			 */
			v.multiConstMatrix(-1);
			alfa.setXYZMat(v);
			alfa.multiConstMatrix(dt / d * d);
			vq.sum(alfa);
			aux = TriVector.multiConsMatrix(dt, vq);
			alfa.setXYZMat(aux);
			q.sum(alfa);

			// loopCheck(q);
			// loopCheck(p);
			/**
			 * create points to draw later
			 */
			Point e = new Point(q);
			e.setColor(Color.red);
			e.setRadius(5);
			graphParticles.addtoList(e);
			e = new Point(p);
			e.setColor(Color.red);
			e.setRadius(5);
			graphParticles.addtoList(e);
		}

		private TriVector f(TriVector x) {
			TriVector f = new TriVector();
			double distanceP, distanceQ;
			distanceP = distance((x.getX() - p.getX()),
					(x.getY() - p.getY()), (x.getZ() - p.getZ()));
			distanceQ = distance((x.getX() - q.getX()),
					(x.getY() - q.getY()), (x.getZ() - q.getZ()));
			switch (stateField) {
			case 6:
				f.setX(-(1 / (distanceP * distanceP)) * (x.getY() - p.getY())
						+ (1 / (distanceQ * distanceQ)) * (x.getZ() - q.getZ()));

				f.setY(-(1 / (distanceP * distanceP)) * (x.getX() - p.getX()));

				f.setZ(- (1 / (distanceQ * distanceQ)) * (x.getX() - q.getX()));
				break;
			case 5:
				
				f.setX(-(1 / (distanceP)) * (x.getX() - p.getX())
						+ (1 / (distanceQ)) * (x.getX() - q.getX()));

				f.setY(-(1 / (distanceP)) * (x.getY() - p.getY())
						+ (1 / (distanceQ)) * (x.getY() - q.getY()));

				f.setZ(-(1 / (distanceP)) * (x.getZ() - p.getZ())
						+ (1 / (distanceQ)) * (x.getZ() - q.getZ()));
				break;
			case 4:
				f.setX(10 * (x.getY() - x.getX()));

				f.setY(x.getX() * (13 - x.getZ()) - x.getY());

				f.setZ(x.getX() * x.getY() - (8 / 3) * x.getZ());
				break;
			case 3:
				f.setX(-(x.getY() - p.getY()) + (x.getZ() - q.getZ())
						* (x.getX() - q.getX()));

				f.setY((x.getX() - p.getX()) + (x.getZ() - q.getZ())
						* (x.getY() - q.getY()));

				f.setZ(-Math.sqrt((x.getX() - q.getX()) * (x.getX() - q.getX())
						+ (x.getY() - q.getY()) * (x.getY() - q.getY())));
				break;
			case 2:
				f.setX(p.getX() * (x.getY() - x.getX()));

				f.setY(x.getX() * (13 - x.getZ()) - x.getY());

				f.setZ(x.getX() * x.getY() - (q.getZ()) * x.getZ());
				break;
			case 1:
				f.setX(-(1 / (distanceP * distanceP)) * (x.getX() - p.getX())
						+ (1 / (distanceQ * distanceQ)) * (x.getX() - q.getX()));

				f.setY(-(1 / (distanceP * distanceP)) * (x.getY() - p.getY())
						+ (1 / (distanceQ * distanceQ)) * (x.getY() - q.getY()));

				f.setZ(-(1 / (distanceP * distanceP)) * (x.getZ() - p.getZ())
						+ (1 / (distanceQ * distanceQ)) * (x.getZ() - q.getZ()));
				break;
			}
			return f;
		}

		private double distance(double x, double y, double z) {
			return Math.sqrt(x * x + y * y + z * z);
		}

		private void loopCheck(TriVector x) {
			double min = -loopSize;
			double max = loopSize;
			x.setX(barrierCheck(x.getX(), min, max));
			x.setY(barrierCheck(x.getY(), min, max));
			x.setZ(barrierCheck(x.getZ(), min, max));
		}

		/**
		 * 
		 * @param x
		 *            point in 1d space
		 * @param min
		 *            minimum point in the barrier
		 * @param max
		 *            maximum point in the barrier
		 * @return max if x < min, min if x > max, x if min < x < max
		 */
		private double barrierCheck(double x, double min, double max) {
			if (x < min)
				return max;
			else if (x > max)
				return min;
			else
				return x;
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
		euler.run();
		graphics.drawElements();
		graphParticles.drawElements();
		// System.out.println("" + time);
		wd.paint(g);
	}

	public static void main(String[] args) {
		new Cube(false);

	}

	public void orbit(double t, double p) {

		Matrix aux = new Matrix(3, 3);
		aux.setMatrix(1, 3, -Math.cos(p) * Math.cos(t));
		aux.setMatrix(2, 3, -Math.cos(p) * Math.sin(t));
		aux.setMatrix(3, 3, -Math.sin(p));
		aux.setMatrix(1, 2, Math.sin(p) * Math.cos(t));
		aux.setMatrix(2, 2, Math.sin(p) * Math.sin(t));
		aux.setMatrix(3, 2, -Math.cos(p));
		aux.setMatrix(1, 1, -Math.sin(t));
		aux.setMatrix(2, 1, Math.cos(t));
		aux.setMatrix(3, 1, 0);
		TriVector eye = new TriVector(raw * Math.cos(p) * Math.cos(t), raw
				* Math.cos(p) * Math.sin(t), raw * Math.sin(p));

		graphics.setCamera(aux, eye);
		graphParticles.setCamera(aux, eye);
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
		} else if (arg0.getKeyCode() == KeyEvent.VK_R) {
			buildParticles();
		} else if (arg0.getKeyCode() == KeyEvent.VK_V) {
			forceOrVelocity = !forceOrVelocity;
		} else if (arg0.getKeyCode() == KeyEvent.VK_L) {
			loopOn = !loopOn;
		} else if (arg0.getKeyCode() == KeyEvent.VK_1) {
			stateField = 1;
		} else if (arg0.getKeyCode() == KeyEvent.VK_2) {
			stateField = 2;
		} else if (arg0.getKeyCode() == KeyEvent.VK_3) {
			stateField = 3;
		} else if (arg0.getKeyCode() == KeyEvent.VK_4) {
			stateField = 4;
			forceOrVelocity = false;
		} else if (arg0.getKeyCode() == KeyEvent.VK_5) {
			stateField = 5;
		}else if (arg0.getKeyCode() == KeyEvent.VK_6) {
			stateField = 6;
		}else if (arg0.getKeyCode() == KeyEvent.VK_PLUS) {
			loopSize ++;
		}else if (arg0.getKeyCode() == KeyEvent.VK_MINUS) {
			loopSize--;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		thrust = 0;
		if (arg0.getKeyCode() == KeyEvent.VK_H) {
			new TextFrame("help", helpText);
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}
}
