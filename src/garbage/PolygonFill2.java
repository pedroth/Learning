package garbage;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import java.util.Random;

public class PolygonFill2 extends JFrame implements MouseListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage bImg;
	private int wChanged, hChanged;
	/**
	 * points[i][0] is the x-coordinate of i point
	 * points[i][1] is the y-coordinate of i point
	 */
	int[][] points; 
	/**
	 * normals[i][0] is the x-coordinate of i normal
	 * normals[i][1] is the y-coordinate of i normal
	 * 
	 * if there are n points then there is n-1 normals
	 */
	int[][] normals;
	/**
	 * tangent 
	 */
	int[][] tagents;
	int nClicks;
	boolean drawP;
	int nVertex;

	public PolygonFill2() {
		// Set JFrame title
		super("Draw A PolygonFill In JFrame");

		// Set default close operation for JFrame
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set JFrame size
		setSize(400, 400);

		// Make JFrame visible
		setVisible(true);

		nVertex = 5;
		bImg = new BufferedImage(this.getWidth(), this.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		wChanged = this.getWidth();
		hChanged = this.getHeight();
		points = new int[nVertex][2];
		nClicks = 0;
		drawP = false;
		Graphics g = bImg.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, wChanged, hChanged);
		this.addMouseListener(this);
		this.addKeyListener(this);
	}

	public void paint(Graphics g) {
		if (Math.abs(wChanged - this.getWidth()) > 0
				|| Math.abs(hChanged - this.getHeight()) > 0) {
			bImg = new BufferedImage(this.getWidth(), this.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			Graphics gimg = bImg.getGraphics();
			gimg.setColor(Color.white);
			wChanged = this.getWidth();
			hChanged = this.getHeight();
			gimg.fillRect(0, 0, wChanged, hChanged);

		}
		update(g);
	}

	public void update(Graphics g) {
		paintComponent(bImg.getGraphics());
		g.drawImage(bImg, 0, 0, wChanged, hChanged, null);
	}

	// not over writing
	public void paintComponent(Graphics g) {

		if (drawP) {
			double oldTime = System.currentTimeMillis() * 1E-3;
			computeNormals();
			int area = computeArea();
			int root =(int) Math.floor(Math.sqrt(area));
			for(int i = 0; i < area;  i++) { 
				g.setColor(Color.blue);
//				int[] v = RandomPointOnPoly2();
				int[] v = RandomPointOnPoly();
//				int[] v = pointOnDistribution(i % root,i / root, root);
//				int[] v = RandomPointOnPoly3();
				g.drawLine(v[0], v[1], v[0], v[1]);
			}
			double currentTime = System.currentTimeMillis() * 1E-3;
			double timeElapse = currentTime - oldTime;
			oldTime = currentTime;
			System.out.println("time :" + timeElapse);
		}
		drawP = false;
	}
	/*
	 * only works with 3 vertices
	 */
	private int[] pointOnDistribution(int i, int j, int root) {
		int ret[] = new int[2];
		double id = i, jd = j;
		double lambda = 1.0 * (id / root);
		double myu = 1.0 * (jd / root);
		double x = lambda;
		double y = myu;
		double z = 1 - lambda - myu;
		ret[0] =(int) (x * points[0][0] + y * points[1][0] + z * points[2][0]);
		ret[1] =(int) (x * points[0][1] + y * points[1][1] + z * points[2][1]);
		return ret;
	}

	public int[] RandomPointOnPoly() {
		int ret[] = new int[2];
		Random r = new Random();
		double[] randomVector = new double[nVertex];
		double acm = 0;
		for(int i = 0; i < nVertex; i++) {
			randomVector[i] = r.nextDouble();
			acm += randomVector[i];
		}
		double x = 0, y = 0;
		for(int i = 0; i < nVertex; i++) {
			randomVector[i] *= 1.0 / acm;
			x += points[i][0] * randomVector[i];
			y += points[i][1] * randomVector[i];
		}
		ret[0] = (int) x;
		ret[1] = (int) y;
		return ret;
	}
	
	public int[] RandomPointOnPoly2() {
		int ret[] = new int[2];
		double[] randomVector = new double[nVertex];
		double acm = 0;
		for(int i = 0; i < nVertex - 1; i++) {
			randomVector[i] = randomPointOnIterval(0, 1 - acm);
			acm += randomVector[i];
		}
		randomVector[nVertex - 1] = 1 - acm;
		double x = 0, y = 0;
		for(int i = 0; i < nVertex; i++) {
			x += points[i][0] * randomVector[i];
			y += points[i][1] * randomVector[i];
		}
		ret[0] = (int) x;
		ret[1] = (int) y;
		return ret;
	}
	
	/*
	 * only works with 3 vertex
	 */
	public int[] RandomPointOnPoly3() {
		int ret[] = new int[2];
		double[] randomVector = new double[nVertex];
		double r1 = randomPointOnIterval(0, 1);
		double r2 = randomPointOnIterval(0, 1);
		randomVector[0] =  (1 - Math.sqrt(r1));
		randomVector[1] =  (Math.sqrt(r1) * (1 - r2));
		randomVector[2] = (Math.sqrt(r1) * r2);
		double x = 0, y = 0;
		for(int i = 0; i < nVertex; i++) {
			x += points[i][0] * randomVector[i];
			y += points[i][1] * randomVector[i];
		}
		ret[0] = (int) x;
		ret[1] = (int) y;
		return ret;
	}
	
	public double randomPointOnIterval(double xmin, double xmax) {
		Random r = new Random();
		return xmin + (xmax - xmin) * r.nextDouble();
	}
	
	public void computeNormals() { 
		int v1, v2;
		normals = new int[nVertex][2];
		tagents = new int[nVertex][2];
		for (int i = 0; i < nVertex; i++) {
			v1 = points[(i + 1) % nVertex][0] - points[i][0];
			v2 = points[(i + 1) % nVertex][1] - points[i][1];
			tagents[i][0] = v1;
			tagents[i][1] = v2;
			normals[i][0] = v2;
			normals[i][1] = -v1;
		}	
	}
	
	public int computeArea() {
		int acm = 0;
		for(int i = 0; i < nVertex; i++) {
			acm += dot(points[i],normals[i]); 
		}
		return Math.abs(acm / 2);
	}
	
	public int dot(int[] v, int[] u) {
		int acm = 0;
		for(int i = 0; i < v.length; i++) {
			acm += v[i] * u[i];
		}
		return acm;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

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
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		points[nClicks % nVertex][0] = e.getX();
		points[nClicks % nVertex][1] = e.getY();
		nClicks++;
		if (nClicks % nVertex == 0) {
			drawP = true;
		} else {
			drawP = false;
		}
		Graphics g = bImg.getGraphics();
		g.setColor(Color.red);
		g.fillOval(e.getX(), e.getY(), 6, 6);
		repaint();
	}

	public static void main(String[] args) {
		// starting app
		new PolygonFill2();
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}
