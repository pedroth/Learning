package apps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import visualization.TextFrame;
import window.ImageWindow;
import windowThreeDim.Composite;
import windowThreeDim.Element;
import windowThreeDim.PaintMethod;
import windowThreeDim.Point;
import windowThreeDim.Quad;
import windowThreeDim.FlatShader;
import windowThreeDim.TriWin;
import algebra.Matrix;
import algebra.TriVector;

/**
 * 
 * @author pedro
 * 
 */
public class Robot extends JFrame implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
	/**
	 * size of the screen
	 */
	private int wChanged, hChanged;
	/**
	 * 3D Engine
	 */
	private ImageWindow wd;
	private TriWin graphics;
	/**
	 * spherical coordinates of the camera
	 */
	private double theta, phi;
	/**
	 * mouse coordinates
	 */
	private int mx, my, newMx, newMy, mRotation;
	/**
	 * camera dynamics
	 */
	private double raw;
	/**
	 * time
	 */
	private double oldTime;
	private double currentTime;
	/**
	 * where the camera is looking
	 */
	private TriVector focalPoint;
	/**
	 * degrees of freedom
	 */
	private Transformation[] dOF;
	/**
	 * scene graph sort of
	 */
	private Node scene;
	private FlatShader shader;
	/**
	 * ball position in spherical coordinates
	 */
	private TriVector ballPos;
	/**
	 * ball Velocity in spherical coordinates
	 */
	private TriVector ballVel;
	/**
	 * ball Thrust in spherical coordinates
	 */
	private TriVector thrust;
	/**
	 * ball
	 */
	private Composite ball;
	Transformation ballTransform;
	private boolean showStateSpace;

	private double robotPieceScale = 2;

	private static String helpText = "< left mouse button > : change orientation of camera \n\n" + "< right mouse button > : zoom in / zoom out \n\n" + "< [w, s] ans arrows > : move ball \n\n" + "< t > : toggle space state \n\n" + "Made by Pedroth";

	public Robot(boolean isApplet) {
		// Set JFrame title
		super("Robot");

		// init
		this.setLayout(null);
		/**
		 * begin the engine
		 */
		graphics = new TriWin();
		wd = graphics.getBuffer();
		shader = new FlatShader();
		graphics.setMethod(shader);
		wd.setBackGroundColor(new Color(0.9f, 0.9f, 0.9f));

		raw = 10;
		focalPoint = new TriVector();

		 //Set default close operation for JFrame
		 if(!isApplet) {
			 setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 }

		// Set JFrame size
		setSize(800, 600);

		wChanged = this.getWidth();
		hChanged = this.getHeight();

		oldTime = (System.currentTimeMillis()) * 1E-03;

		wd.setWindowSize(wChanged, hChanged);

		this.addKeyListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);

		shader.setAmbientLightParameter(0.5);
		shader.setShininess(25);
		shader.addLightPoint(new TriVector(3, 3, 3));
		orbit(theta, phi, focalPoint);
		ballPos = new TriVector(5, Math.PI / 4, Math.PI / 12);
		ballVel = new TriVector();
		thrust = new TriVector();
		buildSphere(0.5);
		createRobot();
		showStateSpace = false;
		euler(0.005);
		/**
		 * Make JFrame visible
		 */
		setVisible(true);
	}

	public void buildSphere(double radius) {
		double pi = Math.PI;
		double step = pi / 16;
		double nV = 2 * pi / step;
		double nU = pi / step;
		int numIteV = (int) Math.floor(nV);
		int numIteU = (int) Math.floor(nU);
		TriVector[][] sphere = new TriVector[numIteU][numIteV];
		ball = new Composite();
		for (int j = 0; j < numIteV; j++) {
			for (int i = 0; i < numIteU; i++) {
				double u = step * i;
				double v = step * j;
				double sinU = Math.sin(u);
				double cosU = Math.cos(u);
				double sinV = Math.sin(v);
				double cosV = Math.cos(v);
				sphere[i][j] = new TriVector(radius * sinU * cosV, radius * sinU * sinV, radius * cosU);
			}
		}
		TriVector p = new TriVector();
		for (int j = 0; j < numIteV - 1; j++) {
			for (int i = 0; i < numIteU - 1; i++) {
				Quad e = new Quad(TriVector.sum(p, sphere[i][j]), TriVector.sum(p, sphere[i + 1][j]), TriVector.sum(p, sphere[i + 1][j + 1]), TriVector.sum(p, sphere[i][j + 1]));
				e.setColor(Color.red);
				ball.add(e);
			}
		}
	}

	private abstract class Node {
		private List<Node> nodeList;

		public Node() {
			nodeList = new ArrayList<Node>();
		}

		public void addNode(Node node) {
			nodeList.add(node);
		}

		public void childIterate(Stack<Matrix> transformStack, Stack<TriVector> translationStack) {
			Iterator<Node> ite = nodeList.iterator();
			while (ite.hasNext()) {
				ite.next().action(transformStack, translationStack);
			}
		}

		public abstract void action(Stack<Matrix> transformStack, Stack<TriVector> translationStack);
	}

	private class Transformation extends Node {
		private String axisOfRotation;
		private Double theta;
		/**
		 * translation vector
		 */
		private TriVector v;

		public Transformation(Double theta, String axisOfRotation, TriVector v) {
			this.theta = theta;
			this.v = v;
			this.axisOfRotation = axisOfRotation;
		}

		public Matrix rotateXMatrix(double angle) {
			Matrix v = new Matrix(3, 3);
			double cosT = Math.cos(angle);
			double sinT = Math.sin(angle);
			v.setMatrix(2, 2, cosT);
			v.setMatrix(2, 3, -sinT);
			v.setMatrix(3, 2, sinT);
			v.setMatrix(3, 3, cosT);
			v.setMatrix(1, 1, 1.0f);
			return v;
		}

		public Matrix rotateYMatrix(double angle) {
			Matrix v = new Matrix(3, 3);
			double cosT = Math.cos(angle);
			double sinT = Math.sin(angle);
			v.setMatrix(1, 1, cosT);
			v.setMatrix(1, 3, sinT);
			v.setMatrix(3, 1, -sinT);
			v.setMatrix(3, 3, cosT);
			v.setMatrix(2, 2, 1.0f);
			return v;
		}

		public Matrix rotateZMatrix(double angle) {
			Matrix v = new Matrix(3, 3);
			double cosT = Math.cos(angle);
			double sinT = Math.sin(angle);
			v.setMatrix(1, 1, cosT);
			v.setMatrix(1, 2, -sinT);
			v.setMatrix(2, 1, sinT);
			v.setMatrix(2, 2, cosT);
			v.setMatrix(3, 3, 1.0f);
			return v;
		}

		@Override
		public void action(Stack<Matrix> transformStack, Stack<TriVector> translationStack) {
			Matrix rot = null;
			TriVector translate = null;
			if (axisOfRotation == "x") {
				rot = rotateXMatrix(theta);
			} else if (axisOfRotation == "y") {
				rot = rotateYMatrix(theta);
			} else {
				rot = rotateZMatrix(theta);
			}
			if (transformStack.empty() || translationStack.empty()) {
				transformStack.push(rot);
				translationStack.push(v);
			} else {
				Matrix topRot = transformStack.peek();
				TriVector topV = translationStack.peek();
				rot = Matrix.multiMatrix(topRot, rot);
				translate = v.copy();
				translate.Transformation(topRot);
				translate = TriVector.sum(translate, topV);
				transformStack.push(rot);
				translationStack.push(translate);
			}

			childIterate(transformStack, translationStack);

			transformStack.pop();
			translationStack.pop();
		}

	}

	private class Geometry extends Node {
		private Composite element;
		private PaintMethod painter;

		public Geometry(Composite element, PaintMethod painter) {
			super();
			this.element = element;
			this.painter = painter;
		}

		@Override
		public void action(Stack<Matrix> transformStack, Stack<TriVector> translationStack) {
			Element e = element.copy();
			e.transform(transformStack.peek(), translationStack.peek());
			e.draw(painter);
		}
	}

	private class InitNode extends Node {
		private PaintMethod painter;

		public InitNode(PaintMethod painter) {
			super();
			this.painter = painter;
		}

		@Override
		public void action(Stack<Matrix> transformStack, Stack<TriVector> translationStack) {
			painter.init();
			childIterate(transformStack, translationStack);
		}
	}

	private void createRobot() {
		int numberDOF = 4;
		Composite[] box = new Composite[numberDOF];
		for (int i = 0; i < box.length; i++) {
			float c = 0.1f + (0.4f / box.length) * i;
			box[i] = buildUnitaryCube(new Color(c, c, c));
		}
		/**
		 * initial transformation of the cube
		 */
		Matrix t = new Matrix(3, 3);
		t.identity();
		t.setMatrix(3, 3, robotPieceScale);
		box[1].transform(t, new TriVector(0, 0, robotPieceScale / 2));
		box[2].transform(t, new TriVector(0, 0, robotPieceScale / 2));
		box[3].transform(t, new TriVector(0, 0, robotPieceScale / 2));
		/**
		 * initial node of the scene
		 */
		scene = new InitNode(shader);
		Transformation originFrame = new Transformation(0.0, "x", new TriVector());
		ballTransform = new Transformation(0.0, "x", ballPos);
		ballTransform.addNode(new Geometry(ball, shader));
		originFrame.addNode(ballTransform);
		scene.addNode(originFrame);

		/**
		 * setting other nodes;
		 */
		Geometry[] obj = new Geometry[numberDOF];
		Transformation[] transform = new Transformation[numberDOF];
		String[] axisOfRotation = { "z", "y", "y", "x" };
		TriVector[] v = new TriVector[numberDOF];
		v[0] = new TriVector(0.0, 0.0, 0.0);
		v[1] = new TriVector(0, 0, robotPieceScale / 4);
		v[2] = new TriVector(0, 0, robotPieceScale);
		v[3] = new TriVector(0.0, 0.0, robotPieceScale);
		for (int i = 0; i < obj.length; i++) {
			obj[i] = new Geometry(box[i], shader);
			transform[i] = new Transformation(Math.PI / 12, axisOfRotation[i], v[i]);
		}
		transform[3].addNode(obj[3]);
		transform[2].addNode(transform[3]);
		transform[2].addNode(obj[2]);
		transform[1].addNode(transform[2]);
		transform[1].addNode(obj[1]);
		transform[0].addNode(transform[1]);
		transform[0].addNode(obj[0]);
		originFrame.addNode(transform[0]);

		dOF = transform;
	}

	private Composite buildUnitaryCube(Color c) {
		Composite compositeCube = new Composite();
		TriVector[][][] cube = new TriVector[2][2][2];
		for (int i = 0; i < cube.length; i++) {
			for (int j = 0; j < cube.length; j++) {
				for (int k = 0; k < cube.length; k++) {
					double x = i - 0.5;
					double y = j - 0.5;
					double z = k - 0.5;
					cube[i][j][k] = new TriVector(x, y, z);
				}
			}
		}
		for (int i = 0; i < cube.length; i++) {
			Element e = new Quad(cube[i][0][0], cube[i][1][0], cube[i][1][1], cube[i][0][1]);
			e.setColor(c);
			compositeCube.add(e);
		}
		for (int i = 0; i < cube.length; i++) {
			Element e = new Quad(cube[0][i][0], cube[1][i][0], cube[1][i][1], cube[0][i][1]);
			e.setColor(c);
			compositeCube.add(e);
		}
		for (int i = 0; i < cube.length; i++) {
			Element e = new Quad(cube[0][0][i], cube[1][0][i], cube[1][1][i], cube[0][1][i]);
			e.setColor(c);
			compositeCube.add(e);
		}
		return compositeCube;
	}

	public void paint(Graphics g) {
		if (Math.abs(wChanged - this.getWidth()) > 0 || Math.abs(hChanged - this.getHeight()) > 0) {
			wChanged = this.getWidth();
			hChanged = this.getHeight();
			wd.setWindowSize(this.getWidth(), this.getHeight());
		}
		update(g);
	}

	public void update(Graphics g) {
		wd.clearImageWithBackGround();
		currentTime = (System.currentTimeMillis()) * 1E-03;
		double dt = currentTime - oldTime;
		oldTime = currentTime;
		scene.action(new Stack<Matrix>(), new Stack<TriVector>());
		if (showStateSpace)
			graphics.drawElements();
//		System.out.println(dt);
		if (dt > 0.05) {
			euler(0.005);
		} else {
			euler(dt);
		}
		wd.paint(g);
	}
	
	public double clamp(double x,double xmin,double xmax) {
		double ans = x; 
		if (x < xmin) {
	        ans = xmin;
		} else if (x > xmax){
	        ans = xmax;
		}
		 return ans;
	}

	public void euler(double dt) {
		double[] theta = new double[dOF.length];
		for (int i = 0; i < dOF.length; i++) {
			theta[i] = dOF[i].theta;
		}

		ballVel = TriVector.sum(ballVel, TriVector.multConst(dt, TriVector.sub(thrust, TriVector.multConst(0.5, ballVel))));
		ballPos = TriVector.sum(TriVector.sum(ballPos, TriVector.multConst(dt, ballVel)), TriVector.multConst(0.5 * dt * dt, TriVector.sub(thrust, TriVector.multConst(0.5, ballVel))));
		double cosP = Math.cos(ballPos.getY());
		double cosT = Math.cos(ballPos.getZ());
		double sinP = Math.sin(ballPos.getY());
		double sinT = Math.sin(ballPos.getZ());
		TriVector positionXYZ = new TriVector();
		positionXYZ.setX(ballPos.getX() * sinP * cosT);
		positionXYZ.setY(ballPos.getX() * sinP * sinT);
		positionXYZ.setZ(ballPos.getX() * cosP);
		ballTransform.v = positionXYZ;

		for (int i = 0; i < dOF.length; i++) {
			double max = 0.01;
			double vel = clamp((computePartialDerivative(theta, i, positionXYZ) + computeRestriction(theta, i)) * 0.5 * dt,-max,max);
			dOF[i].theta -= vel;
			// System.out.println(i + "  " + theta[i] + "\t");
		}
		// System.out.print(computeCostFunction(theta, positionXYZ));
		// System.out.println();
		Element e = new Point(new TriVector(theta[0], theta[1], theta[2]));
		e.setColor(Color.blue);
		graphics.addtoList(e);
		repaint();
	}

	public double computeRestriction(double[] theta, int i) {
		if (i == 0) {
			return 0;
		} else if (i == 1) {
			// return 0;
			return 1 / ((Math.PI / 2) - theta[i]) - 1 / theta[i];
		} else if (i == 2) {
			// return 0;
			return 1 / ((Math.PI / 2) - theta[i]) - 1 / theta[i];
		} else if (i == 3) {
			// return 0;
			return 1 / ((Math.PI / 2) - theta[i]) - 1 / ((Math.PI / 2) + theta[i]);
		} else {
			return 0;
		}
	}

	public double computeCostFunction(double[] theta, TriVector x) {
		TriVector k = new TriVector(0.0, 0.0, robotPieceScale);
		Matrix rot = new Matrix(3, 3);
		rot.identity();
		TriVector acm = new TriVector();
		for (int i = 0; i < dOF.length; i++) {
			acm.sum(TriVector.Transformation(rot, dOF[i].v.copy()));
			if (dOF[i].axisOfRotation == "x") {
				rot = Matrix.multiMatrix(rot, dOF[i].rotateXMatrix(theta[i]));
			} else if (dOF[i].axisOfRotation == "y") {
				rot = Matrix.multiMatrix(rot, dOF[i].rotateYMatrix(theta[i]));
			} else {
				rot = Matrix.multiMatrix(rot, dOF[i].rotateZMatrix(theta[i]));
			}
		}
		k.Transformation(rot);
		acm.sum(k);
		acm = TriVector.sub(acm, x);
		return TriVector.vInnerProduct(acm, acm);
	}

	public double computePartialDerivative(double[] theta, int i, TriVector x) {
		double dx = 1E-3;
		double[] thetaPlusH = new double[theta.length];
		double[] thetaMinusH = new double[theta.length];
		for (int j = 0; j < thetaMinusH.length; j++) {
			thetaPlusH[j] = theta[j];
			thetaMinusH[j] = theta[j];
		}
		thetaPlusH[i] += dx;
		thetaMinusH[i] -= dx;
		double df = (computeCostFunction(thetaPlusH, x) - computeCostFunction(thetaMinusH, x)) / (2 * dx);
		return df;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Robot(false);
	}

	public void orbit(double t, double p, TriVector x) {

		Matrix aux = new Matrix(3, 3);
		double cosP = Math.cos(p);
		double cosT = Math.cos(t);
		double sinP = Math.sin(p);
		double sinT = Math.sin(t);

		// z - axis
		aux.setMatrix(1, 3, -cosP * cosT);
		aux.setMatrix(2, 3, -cosP * sinT);
		aux.setMatrix(3, 3, -sinP);
		// y - axis
		aux.setMatrix(1, 2, -sinP * cosT);
		aux.setMatrix(2, 2, -sinP * sinT);
		aux.setMatrix(3, 2, cosP);
		// x -axis
		aux.setMatrix(1, 1, -sinT);
		aux.setMatrix(2, 1, cosT);
		aux.setMatrix(3, 1, 0);

		TriVector eye = new TriVector(raw * cosP * cosT + x.getX(), raw * cosP * sinT + x.getY(), raw * sinP + x.getZ());

		graphics.setCamera(aux, eye);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		double pi = Math.PI;
		if (arg0.getKeyCode() == KeyEvent.VK_RIGHT) {
			thrust.setZ(-pi / 12);
		}
		if (arg0.getKeyCode() == KeyEvent.VK_LEFT) {
			thrust.setZ(pi / 12);
		}
		if (arg0.getKeyCode() == KeyEvent.VK_UP) {
			thrust.setY(pi / 12);
		}
		if (arg0.getKeyCode() == KeyEvent.VK_DOWN) {
			thrust.setY(-pi / 12);
		}
		if (arg0.getKeyCode() == KeyEvent.VK_W) {
			thrust.setX(10);
		}
		if (arg0.getKeyCode() == KeyEvent.VK_S) {
			thrust.setX(-10);
		}
		if (arg0.getKeyCode() == KeyEvent.VK_T) {
			showStateSpace = !showStateSpace;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		thrust = new TriVector();
		if (arg0.getKeyCode() == KeyEvent.VK_H) {
			new TextFrame("help", helpText);
		}

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		newMx = arg0.getX();
		newMy = arg0.getY();
		double dx = newMx - mx;
		double dy = newMy - my;

		if (SwingUtilities.isLeftMouseButton(arg0)) {
			theta += 2 * Math.PI * (dx / wChanged);
			phi += 2 * Math.PI * (dy / hChanged);
		} else {
			raw = raw + 2 * ((dx / wChanged) + (dy / hChanged));
		}

		orbit(theta, phi, focalPoint);
		repaint();

		mx = newMx;
		my = newMy;
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		mx = arg0.getX();
		my = arg0.getY();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

}
