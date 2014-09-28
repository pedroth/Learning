package apps;

import inputOutput.MyImage;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.TextField;
import java.awt.datatransfer.FlavorTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import visualization.TextFrame;
import window.ImageWindow;
import windowThreeDim.Element;
import windowThreeDim.InterpolativeShader;
import windowThreeDim.LevelSetShader;
import windowThreeDim.Line;
import windowThreeDim.Quad;
import windowThreeDim.SamplingZbuffer;
import windowThreeDim.FlatShader;
import windowThreeDim.StringElement;
import windowThreeDim.TriWin;
import windowThreeDim.WiredPrespective;
import windowThreeDim.ZBufferPrespective;
import windowThreeDim.ZbufferShader;
import algebra.Matrix;
import algebra.TriVector;
import functionNode.CombinationNode;
import functionNode.FunctionNode;
import functions.ExpressionFunction;
import functions.SyntaxErrorException;

public class PartialDifferential extends JFrame implements MouseListener,
		MouseMotionListener, KeyListener, MouseWheelListener {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
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
	 * User Interface(UI) crap
	 */
	private TextField functionString;
	private TextField xMinTxt, xMaxTxt, yMinTxt, yMaxTxt, stepTxt;
	Button drawButton;
	private Choice comboBox;
	private TextField functionVel;
	private TextField functionAcc;
	private Button animate;
	private Button zoomFit;
	/**
	 * to check the need to add UI on the JFrame
	 */
	private boolean init;
	/**
	 * JFrame Graphics object responsible for user interface drawing
	 */
	private ImageWindow frameGraphics;
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
	private double velocity;
	private double acceleration;
	private double oldTime;
	private double currentTime;
	private double thrust;
	/**
	 * where the camera is looking
	 */
	private TriVector focalPoint;
	/**
	 * there is no need in this class i only put here for the case i want to
	 * insert a timer
	 */
	private Euler euler;
	/**
	 * variable to check the need of drawing
	 */
	private boolean drawFunction;
	/**
	 * surface
	 */
	private TriVector[][] surface;
	/**
	 * velocities
	 */
	private double[][] dudt;
	/**
	 * variables describing the domain of the function
	 */
	private double xmin, xmax, ymin, ymax, step;
	/**
	 * computes function from string
	 */
	private ExpressionFunction exprFunction;
	private String[] vars = { "x", "y" };
	/**
	 * PDE functions
	 */
	private ExpressionFunction velEquation;
	private ExpressionFunction accEquation;
	private String[] diffVars = { "x", "y", "t" };
	/**
	 * ZBuffer/WiredFrame
	 */
	private boolean isZBuffer;
	/**
	 * maximum and minimum z-value of the mesh
	 */
	private double maxHeightColor;
	private double minHeightColor;
	/**
	 * 0 -> heatMap color. 1 -> random color. 2 -> ??? color.
	 */
	private int colorState;
	/**
	 * variable to control when to draw axis
	 */
	private boolean drawAxis;
	/**
	 * zoom fit boolean
	 */
	private boolean isZoomFit;
	/**
	 * shader
	 */
	private FlatShader shader;
	/**
	 * is shading
	 */
	private boolean isShading;
	/**
	 * is animating
	 */
	private boolean isAnimating;
	/**
	 * time in seconds
	 */
	private double time;
	/**
	 * fast shading
	 */
	private boolean isfastShading;
	/**
	 * is kakashi image
	 */
	private boolean isKakashi;

	private static String helpText = "< w > : Camera move foward / zoom in \n\n"
			+ "< s > : Camera move backward /zoom out \n\n"
			+ "< z > : Toggle wireframe / zbuffer \n\n"
			+ "< n > : Toggle flat shading \n\n"
			+ "< [1-3] > : change color of surface \n\n"
			+ "< e > : random examples of functions\n\n"
			+ "< 8 > : interpolative colors \n\n"
			+ "< 9 > : level set shader\n\n"
			+ "< Available functions > : sin ,cos, exp, ln, tan, acos, asin, atan, min, adding more \n\n"
			+ "< operators > : +, - , *, ^ \n\n"
			+ "< Available constants > : pi\n\n"
			+ "< a > : draw Axis \n\n"
			+ "< mouse > : rotate camera \n\n"
			+ "< k > : toogle kakashi mode\n\n"
			+ "< Available function on animation part > :  dfx(x,y) (df / dx), dfy(x,y) (df / dy) , dt(x,y) (df / dt), d2fx(x,y)(d2f / dx2)\n\n"
			+ "d2fy(x,y) (d2f / dy2), f(x,y), d2fxy(x,y)\n\n"
			+ "Made by Pedroth";

	private boolean axisAlreadyBuild;

	public PartialDifferential() {
		// Set JFrame title
		super("PDE");

		// init
		this.setLayout(null);
		/**
		 * begin the engine
		 */
		graphics = new TriWin();
		wd = graphics.getBuffer();
		ZBufferPrespective painter = new ZBufferPrespective();
		graphics.setMethod(painter);
		wd.setBackGroundColor(Color.black);
		isZBuffer = true;
		colorState = 0;
		drawAxis = false;
		// Set default close operation for JFrame
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set JFrame size
		setSize(800, 600);

		wChanged = this.getWidth();
		hChanged = this.getHeight();

		wd.setWindowSize(7 * wChanged / 10, hChanged);

		functionString = new TextField();
		xMinTxt = new TextField();
		xMaxTxt = new TextField();
		yMinTxt = new TextField();
		yMaxTxt = new TextField();
		stepTxt = new TextField();
		functionAcc = new TextField();
		functionVel = new TextField();
		comboBox = new Choice();
		comboBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				processLayout();
			}
		});
		comboBox.add("Acceleration");
		comboBox.add("Velocity");
		drawButton = new Button("Draw");
		drawButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				drawFunction = true;
				graphics.removeAllElements();
			}
		});
		animate = new Button("Animate");
		animate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (animate.getLabel() == "Animate") {
					drawFunction = true;
					graphics.removeAllElements();
					isAnimating = true;
					animate.setLabel("Stop Animation");
				} else {
					isAnimating = false;
					animate.setLabel("Animate");
					time = 0;
				}
			}
		});

		zoomFit = new Button("Zoom fit");
		zoomFit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				isZoomFit = true;
			}
		});
		isZoomFit = false;
		init = true;

		raw = 3;
		velocity = 0;
		acceleration = 0;

		oldTime = (System.currentTimeMillis()) * 1E-03;
		thrust = 0;

		drawFunction = true;
		focalPoint = new TriVector();
		euler = new Euler();

		mRotation = 0;

		// Make JFrame visible
		setVisible(true);

		frameGraphics = new ImageWindow(0, 1, 0, 1);
		frameGraphics.setBackGroundColor(Color.black);
		processLayout();

		this.addKeyListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);

		isShading = false;
		shader = new FlatShader();
		shader.setAmbientLightParameter(0.5);
		shader.setShininess(25);
		shader.addLightPoint(new TriVector(raw, raw, raw));

		isAnimating = false;
		time = 0;
		isfastShading = false;
		isKakashi = false;
		axisAlreadyBuild = false;
	}

	/**
	 * order does matter
	 * 
	 * for instance you must draw background ,text, other stuff after the Jframe
	 * components such as buttons textAreas, textFields, checkBoxs, etc...
	 */
	public void processLayout() {
		int border = 70;

		if (init) {
			this.add(functionString);
			this.add(xMinTxt);
			this.add(xMaxTxt);
			this.add(yMinTxt);
			this.add(yMaxTxt);
			this.add(stepTxt);
			this.add(drawButton);
			this.add(comboBox);
			this.add(animate);
			this.add(zoomFit);
			this.add(functionAcc);
			this.add(functionVel);
			functionString.setText("sin( x ^ 2 + y ^ 2)");
			functionAcc.setText("d2x(x,y) + d2y(x,y) - 0.5 * dt(x,y)");
			functionVel
					.setText("(1 / ((1 + dx(x,y)^2 + dy(x,y)^2)^(3/2))) * (d2x(x,y)*(1 + dy(x,y)^2) +2*dx(x,y)*dy(x,y)*d2xy(x,y) +  d2y(x,y)*(1 + dx(x,y)^2))");
			xMinTxt.setText("-1");
			xMaxTxt.setText("1");
			yMinTxt.setText("-1");
			yMaxTxt.setText("1");
			stepTxt.setText("0.25");
			init = false;
		}
		/**
		 * pseudo - canvas
		 */
		wd.setWindowSize(border * wChanged / 100, hChanged);
		frameGraphics.setWindowSize((100 - border) * wChanged / 100, hChanged);

		/**
		 * function text box
		 */
		functionString.setBounds((border + 3) * wChanged / 100, hChanged / 10,
				20 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * xmin
		 */
		xMinTxt.setBounds((border + 3) * wChanged / 100, 23 * hChanged / 100,
				6 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * xmax
		 */
		xMaxTxt.setBounds((border + 18) * wChanged / 100, 23 * hChanged / 100,
				6 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * ymin
		 */
		yMinTxt.setBounds((border + 3) * wChanged / 100, 35 * hChanged / 100,
				6 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * ymax
		 */
		yMaxTxt.setBounds((border + 18) * wChanged / 100, 35 * hChanged / 100,
				6 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * X/Y step
		 */
		stepTxt.setBounds((border + 3) * wChanged / 100, 45 * hChanged / 100,
				6 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * draw Button
		 */
		drawButton.setBounds((border + 13) * wChanged / 100,
				45 * hChanged / 100, 15 * wChanged / 100, 7 * hChanged / 100);
		/**
		 * zoomfit button
		 */
		zoomFit.setBounds((border + 15) * wChanged / 100, 87 * hChanged / 100,
				10 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * Acceleration/Velocity
		 */
		if (comboBox.getSelectedItem() == "Acceleration") {
			functionVel.setVisible(false);
			functionAcc.setVisible(true);
			functionAcc.setBounds((border + 3) * wChanged / 100,
					65 * hChanged / 100, 19 * wChanged / 100,
					5 * hChanged / 100);
		} else {
			functionVel.setVisible(true);
			functionAcc.setVisible(false);
			functionVel.setBounds((border + 3) * wChanged / 100,
					65 * hChanged / 100, 19 * wChanged / 100,
					5 * hChanged / 100);
		}
		/**
		 * animation button
		 */
		animate.setBounds((border + 3) * wChanged / 100, 75 * hChanged / 100,
				15 * wChanged / 100, 7 * hChanged / 100);

		Graphics g = frameGraphics.getGraphics();

		/**
		 * clear panel ,not really necessary
		 */
		frameGraphics.clearImageWithBackGround();
		/**
		 * comboBox
		 */
		comboBox.setBounds((border + 3) * wChanged / 100, 55 * hChanged / 100,
				10 * wChanged / 100, 3 * hChanged / 100);
		/**
		 * Function text
		 */
		frameGraphics.setDrawColor(Color.white);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 12));
		frameGraphics.drawString("F(x, y)", 0.05, 1 - 0.12);
		/**
		 * xmin/xmax and ymin/ymax text
		 */
		frameGraphics.drawString("xmin", 0.05, 1 - 0.25);
		frameGraphics.drawString("xmax", 0.69, 1 - 0.25);
		frameGraphics.drawString("ymin", 0.05, 1 - 0.37);
		frameGraphics.drawString("ymax", 0.69, 1 - 0.37);
		/**
		 * step text
		 */
		frameGraphics.drawString("X/Y step", 0.05, 1 - 0.48);
		/**
		 * PDE text
		 */
		if (comboBox.getSelectedItem() == "Acceleration") {
			frameGraphics
					.drawString(
							"d2F/d2t = G(f(x,y),dx(x,y),dy(x,y),d2x(x,y),d2y(x,y),d2xy(x,y),dt(x,y),t,x,y)",
							0.03, 1 - 0.68);
		} else {
			frameGraphics
					.drawString(
							"dF/dt = G(f(x,y),dx(x,y),dy(x,y),d2x(x,y),d2y(x,y),d2xy(x,y),t,x,y)",
							0.03, 1 - 0.68);
		}
		frameGraphics.paintTranslate(this.getGraphics(), border * wChanged
				/ 100, 0);
	}

	class Euler {

		public void run() {
			currentTime = (System.currentTimeMillis()) * 1E-03;
			double dt = currentTime - oldTime;
			time += dt;
			oldTime = currentTime;
			acceleration = -velocity + thrust;
			velocity += acceleration * dt;
			raw += velocity * dt;
			orbit(theta, phi, focalPoint);
			if (drawAxis) {
				buildAxis();
			}
			if (drawFunction) {
				if (!isKakashi)
					buildFunction();
				else {
					buildKakashi();
				}
			}
			if (isZoomFit) {
				cameraFindGraph();
			}
			if (isAnimating) {
				animate(dt);
			}
			graphics.drawElements();
			repaint();
		}
	}

	public void cameraFindGraph() {
		Double v[] = new Double[3];
		v[0] = (xmax + xmin) / 2;
		v[1] = (ymax + ymin) / 2;
		v[2] = (maxHeightColor + minHeightColor) / 2;
		raw = 3 * xmax;
		focalPoint = new TriVector(v[0], v[1], v[2]);
		isZoomFit = false;
	}

	/**
	 * warning does not accept variables
	 * 
	 * @param s
	 *            string to be read.
	 * @return computation of the expression in s
	 */
	public double numericRead(String s) {
		ExpressionFunction in;
		in = new ExpressionFunction(s, new String[] {});
		in.init();
		return in.compute(new Double[] {});
	}

	private double gauss(int x, int y, int window) {
		double u = x;
		double v = y;
		double w = window / 2.0;
		return 1 / (4 * w * w)
				* Math.exp(-0.25 * ((u - w) * (u - w) + (v - w) * (v - w)));
	}

	private void gaussianFilterToSurface(TriVector[][] s, int window) {
		maxHeightColor = Double.MIN_VALUE;
		minHeightColor = Double.MAX_VALUE;
		double aux = 0;
		for (int j = 0; j < s[0].length + 1; j++) {
			for (int i = 0; i < s.length + 1; i++) {
				for (int k = 0; k < window; k++) {
					for (int l = 0; l < window; l++) {
						int x = Math.max(0, i - window / 2 + k);
						x = Math.min(s.length - 1, x);
						int y = Math.max(0, j - window / 2 + l);
						y = Math.min(s[0].length - 1, y);
						aux = aux + gauss(k, l, window) * s[x][y].getZ();
					}
				}
				int x = Math.min(s.length - 1, i);
				int y = Math.min(s[0].length - 1, j);
				surface[i][j] = new TriVector(s[x][y].getX(), s[x][y].getY(),
						aux);
				double auxZ = surface[i][j].getZ();
				if (!Double.isInfinite(auxZ) && !Double.isNaN(auxZ)) {
					maxHeightColor = Math.max(auxZ, maxHeightColor);
					minHeightColor = Math.min(auxZ, minHeightColor);
				}
				aux = 0.0;
			}
		}
	}

	public void buildKakashi() {
		MyImage kakashi = new MyImage(
				"https://92c3cb5a-a-62cb3a1a-s-sites.googlegroups.com/site/ibplanalto2010/Home/kakashi46-3459488_50_50%5B1%5D.jpg?attachauth=ANoY7cp6kFZ2u7lOyL3KJqDYkzI_jmNGeoLsCE29u25IlE23i8Bgqx-4UsNUTkE4Mh7vBQpKPe107E_-PLAOywT34dv8cW9_r9WV0uOZ8p26uBT4rusztcGEh9wkuZ2QI0f-loBiB4pmzo_3NKMrC0CPbRvHHiwa_vT2wVEjZiWh7fZ9XlUjC6vrCVvNOtnmgsnSd-WjjbZqO-q6jSPBFw1zyyaa8uzcAKExLodMjCR40cjjmDComqp1JMNpKJoE1iTDgXQDWFzU&attredirects=0");
		Matrix v = new Matrix(kakashi.getGrayScale());
		TriVector[][] s = v.matrixToSurface(-1, 1, -1, 1);
		xmin = -1;
		xmax = 1;
		ymin = -1;
		ymax = 1;
		step = 2.0 / s.length;
		surface = new TriVector[s.length + 1][s[0].length + 1];
		dudt = new double[s.length + 1][s[0].length + 1];

		gaussianFilterToSurface(s, 3);

		for (int j = 0; j < s[0].length; j++) {
			for (int i = 0; i < s.length; i++) {
				Element e = new Quad(surface[i][j], surface[i + 1][j],
						surface[i + 1][j + 1], surface[i][j + 1]);
				setElementColor(e);
				graphics.addtoList(e);
			}
		}
		drawFunction = false;
	}

	public void buildFunction() {
		double nx, ny, x, y, z, colorHSB;
		int inx, iny;
		int MaxPoly = 4096;
		Random r = new Random();
		colorHSB = r.nextDouble();
		drawFunction = false;
		xmin = numericRead(xMinTxt.getText());
		xmax = numericRead(xMaxTxt.getText());
		ymin = numericRead(yMinTxt.getText());
		ymax = numericRead(yMaxTxt.getText());
		step = numericRead(stepTxt.getText());
		exprFunction = new ExpressionFunction(functionString.getText(), vars);
		exprFunction.addFunction("C", new CombinationNode());
		maxHeightColor = Double.NEGATIVE_INFINITY;
		minHeightColor = Double.POSITIVE_INFINITY;
		try {
			exprFunction.init();
		} catch (SyntaxErrorException e) {
			JOptionPane
					.showMessageDialog(
							null,
							"there is a syntax error in the formula, pls change the formula."
									+ String.format("%n")
									+ " try to use more brackets, try not to cocatenate 2*x^2 as 2x2."
									+ String.format("%n")
									+ "check also for simple errors like 1/*2.");
		}
		nx = Math.abs(xmax - xmin) / (step);
		ny = Math.abs(ymax - ymin) / (step);

		if (!isZBuffer) {
			MaxPoly = 4096;
		}

		if (nx * ny > MaxPoly) {
			double aux = (nx * ny) * (step * step);
			maxPolyConstraint(aux);
			step = Double.parseDouble(stepTxt.getText());
			nx = Math.abs(xmax - xmin) / (step);
			ny = Math.abs(ymax - ymin) / (step);
		}

		inx = (int) Math.floor(nx);
		iny = (int) Math.floor(ny);
		surface = new TriVector[inx + 1][iny + 1];
		dudt = new double[inx + 1][iny + 1];
		for (int j = 0; j < iny; j++) {
			for (int i = 0; i < inx; i++) {
				if (surface[i][j] == null) {
					x = xmin + i * step;
					y = ymin + j * step;
					Double[] xy = { x, y };
					z = exprFunction.compute(xy);
					surface[i][j] = new TriVector(x, y, z);
					// surface[i][j] = new TriVector(x, y, r.nextDouble());
				}
				if (surface[i + 1][j] == null) {
					x = xmin + (i + 1) * step;
					y = ymin + j * step;
					Double[] xy = { x, y };
					z = exprFunction.compute(xy);
					surface[i + 1][j] = new TriVector(x, y, z);
					// surface[i+1][j] = new TriVector(x, y, r.nextDouble());
				}
				if (surface[i + 1][j + 1] == null) {
					x = xmin + (i + 1) * step;
					y = ymin + (j + 1) * step;
					Double[] xy = { x, y };
					z = exprFunction.compute(xy);
					surface[i + 1][j + 1] = new TriVector(x, y, z);
					// surface[i+1][j+1] = new TriVector(x, y, r.nextDouble());
				}
				if (surface[i][j + 1] == null) {
					x = xmin + (i) * step;
					y = ymin + (j + 1) * step;
					Double[] xy = { x, y };
					z = exprFunction.compute(xy);
					surface[i][j + 1] = new TriVector(x, y, z);
					// surface[i][j+1] = new TriVector(x, y, r.nextDouble());
				}
				double auxZ = surface[i][j].getZ();
				if (!Double.isInfinite(auxZ) && !Double.isNaN(auxZ)) {
					maxHeightColor = Math.max(auxZ, maxHeightColor);
					minHeightColor = Math.min(auxZ, minHeightColor);
				}
			}
		}

		for (int j = 0; j < iny; j++) {
			for (int i = 0; i < inx; i++) {
				Element e = new Quad(surface[i][j], surface[i + 1][j],
						surface[i + 1][j + 1], surface[i][j + 1]);
				setElementColor(e);
				graphics.addtoList(e);
			}
		}
	}

	public void setElementColor(Element e) {
		double red = 0;
		double blue = 240.0 / 360.0;
		double green = 120.0 / 360.0;

		for (int i = 0; i < e.getNumOfPoints(); i++) {
			double z = e.getNPoint(i).getZ();

			double x = -1 + 2 * (z - minHeightColor)
					/ (maxHeightColor - minHeightColor);

			if (colorState == 0) {
				/**
				 * linear interpolation between blue color and red
				 */
				double colorHSB = blue + (red - blue) * 0.5 * (x + 1);
				e.setColorPoint(Color.getHSBColor((float) colorHSB, 1f, 1f), i);
			} else if (colorState == 1) {

			} else if (colorState == 2) {

			}
		}
	}

	public void buildAxis() {
		if (!axisAlreadyBuild) {
			Element e = new Line(new TriVector(0, 0, 0), new TriVector(1, 0, 0));
			e.setColor(Color.white);
			graphics.addtoList(e);
			e = new StringElement(new TriVector(1.1, 0, 0), "X");
			e.setColor(Color.white);
			graphics.addtoList(e);
			e = new Line(new TriVector(0, 0, 0), new TriVector(0, 1, 0));
			e.setColor(Color.white);
			graphics.addtoList(e);
			e = new StringElement(new TriVector(0, 1.1, 0), "Y");
			e.setColor(Color.white);
			graphics.addtoList(e);
			e = new Line(new TriVector(0, 0, 0), new TriVector(0, 0, 1));
			e.setColor(Color.white);
			graphics.addtoList(e);
			e = new StringElement(new TriVector(0, 0, 1.1), "Z");
			e.setColor(Color.white);
			graphics.addtoList(e);
			axisAlreadyBuild = true;
		}
	}

	public void infoColor() {
		double red = 0;
		double blue = 240.0 / 360.0;
		double a, x, colorHSB, divN;
		int n = 10;
		for (int i = 0; i < n + 1; i++) {
			x = -1 + (2.0 / n) * i;
			colorHSB = blue + (red - blue) * 0.5 * (x + 1);
			divN = 1.0 / n;
			wd.setDrawColor(Color.getHSBColor((float) colorHSB, 1f, 1f));
			a = -0.5 + (divN) * i;
			wd.drawFilledRectangle(a, -0.9, divN, 0.05);
		}
		wd.setDrawColor(Color.white);
		wd.drawString("" + minHeightColor, -0.5, -0.95);
		wd.drawString("" + maxHeightColor, 0.5, -0.95);

	}

	public void maxPolyConstraint(double delta) {
		double nextStep = Math.sqrt(delta / 1024);

		if (!isZBuffer) {
			nextStep = Math.sqrt(delta / 1024);
		}

		stepTxt.setText("" + nextStep);
		JOptionPane.showMessageDialog(null,
				"the X/Y step is too low, pls choose a higher one");

	}

	public double randomPointInInterval(double xmin, double xmax) {
		Random r = new Random();

		return xmin + (xmax - xmin) * r.nextDouble();
	}

	public void animate(double dt) {
		graphics.removeAllElements();
		if (comboBox.getSelectedItem() == "Acceleration") {
			accAnimate(dt);
		} else {
			velAnimate(dt);
		}
	}

	public void accAnimate(double dt) {
		if (dt > 0.02) {
			if (isKakashi)
				dt = 0.0001;
			else
				dt = 0.01;
		}
		accEquation = new ExpressionFunction(functionAcc.getText(), diffVars);
		accEquation.addFunction("dx", new Dfdx());
		accEquation.addFunction("dy", new Dfdy());
		accEquation.addFunction("d2x", new D2fdx());
		accEquation.addFunction("d2y", new D2fdy());
		accEquation.addFunction("d2xy", new D2fdxdy());
		accEquation.addFunction("f", new Fxy());
		accEquation.addFunction("dt", new Dfdt());
		accEquation.addFunction("C", new CombinationNode());
		maxHeightColor = Double.NEGATIVE_INFINITY;
		minHeightColor = Double.POSITIVE_INFINITY;
		try {
			accEquation.init();
		} catch (SyntaxErrorException e) {
			JOptionPane
					.showMessageDialog(
							null,
							"there is a syntax error in the formula, pls change the formula."
									+ String.format("%n")
									+ " try to use more brackets, try not to cocatenate 2*x^2 as 2x2."
									+ String.format("%n")
									+ "check also for simple errors like 1/*2.");
		}
		double nx = Math.abs(xmax - xmin) / (step);
		double ny = Math.abs(ymax - ymin) / (step);

		int inx = (int) Math.floor(nx);
		int iny = (int) Math.floor(ny);
		double[][] aux = new double[inx + 1][inx + 1];
		for (int j = 0; j < iny + 1; j++) {
			for (int i = 0; i < inx + 1; i++) {
				double auxZ;
				Double[] x = new Double[3];
				x[0] = xmin + i * step;
				x[1] = ymin + j * step;
				x[2] = time;
				
				double acceleration = accEquation.compute(x);

				dudt[i][j] = dudt[i][j] + acceleration * dt;
				auxZ = surface[i][j].getZ() + dudt[i][j] * dt + 0.5
						* acceleration * dt * dt;
				aux[i][j] = (auxZ);
				if (!Double.isInfinite(auxZ) && !Double.isNaN(auxZ)) {
					maxHeightColor = Math.max(auxZ, maxHeightColor);
					minHeightColor = Math.min(auxZ, minHeightColor);
				}
			}
		}

		for (int j = 0; j < iny; j++) {
			for (int i = 0; i < inx; i++) {
				surface[i][j].setZ(aux[i][j]);
				surface[i + 1][j].setZ(aux[i + 1][j]);
				surface[i + 1][j + 1].setZ(aux[i + 1][j + 1]);
				surface[i][j + 1].setZ(aux[i][j + 1]);
				Element e = new Quad(surface[i][j], surface[i + 1][j],
						surface[i + 1][j + 1], surface[i][j + 1]);
				setElementColor(e);
				graphics.addtoList(e);
			}
		}
	}

	public void velAnimate(double dt) {
		if (dt > 0.02) {
			if (isKakashi)
				dt = 0.0001;
			else
				dt = 0.01;
		}
		velEquation = new ExpressionFunction(functionVel.getText(), diffVars);
		velEquation.addFunction("dx", new Dfdx());
		velEquation.addFunction("dy", new Dfdy());
		velEquation.addFunction("d2x", new D2fdx());
		velEquation.addFunction("d2y", new D2fdy());
		velEquation.addFunction("f", new Fxy());
		velEquation.addFunction("d2xy", new D2fdxdy());
		velEquation.addFunction("C", new CombinationNode());
		maxHeightColor = Double.NEGATIVE_INFINITY;
		minHeightColor = Double.POSITIVE_INFINITY;
		try {
			velEquation.init();
		} catch (SyntaxErrorException e) {
			JOptionPane
					.showMessageDialog(
							null,
							"there is a syntax error in the formula, pls change the formula."
									+ String.format("%n")
									+ " try to use more brackets, try not to cocatenate 2*x^2 as 2x2."
									+ String.format("%n")
									+ "check also for simple errors like 1/*2.");
		}
		double nx = Math.abs(xmax - xmin) / (step);
		double ny = Math.abs(ymax - ymin) / (step);

		int inx = (int) Math.floor(nx);
		int iny = (int) Math.floor(ny);
		double[][] aux = new double[inx + 1][inx + 1];
		for (int j = 0; j < iny + 1; j++) {
			for (int i = 0; i < inx + 1; i++) {
				double auxZ;
				Double[] x = new Double[3];
				x[0] = xmin + i * step;
				x[1] = ymin + j * step;
				x[2] = time;
				auxZ = surface[i][j].getZ() + velEquation.compute(x) * dt;
				aux[i][j] = (auxZ);
				if (!Double.isInfinite(auxZ) && !Double.isNaN(auxZ)) {
					maxHeightColor = Math.max(auxZ, maxHeightColor);
					minHeightColor = Math.min(auxZ, minHeightColor);
				}
			}
		}

		for (int j = 0; j < iny; j++) {
			for (int i = 0; i < inx; i++) {
				surface[i][j].setZ(aux[i][j]);
				surface[i + 1][j].setZ(aux[i + 1][j]);
				surface[i + 1][j + 1].setZ(aux[i + 1][j + 1]);
				surface[i][j + 1].setZ(aux[i][j + 1]);
				Element e = new Quad(surface[i][j], surface[i + 1][j],
						surface[i + 1][j + 1], surface[i][j + 1]);
				setElementColor(e);
				graphics.addtoList(e);
			}
		}
	}

	public class Fxy extends FunctionNode {

		public Fxy() {
			super();
			setnVars(2);
		}

		public Fxy(FunctionNode[] args) {
			super(2, args);
		}

		@Override
		public FunctionNode createNode(FunctionNode[] args) {
			return new Fxy(args);
		}

		@Override
		public Double compute(Double[] variables) {
			int i = (int) ((variables[0] - xmin) / step);
			int j = (int) ((variables[1] - ymin) / step);
			return surface[i][j].getZ();
		}

	}

	public class Dfdx extends FunctionNode {

		public Dfdx() {
			super();
			setnVars(2);
		}

		public Dfdx(FunctionNode[] args) {
			super(2, args);
		}

		@Override
		public FunctionNode createNode(FunctionNode[] args) {
			return new Dfdx(args);
		}

		@Override
		public Double compute(Double[] variables) {
			int i = (int) ((variables[0] - xmin) / step);
			int j = (int) ((variables[1] - ymin) / step);
			int k = Math.min(i + 1, surface.length - 1);
			int l = Math.max(i - 1, 0);
			return (surface[k][j].getZ() - surface[l][j].getZ()) / (2 * step);
		}

	}

	public class Dfdy extends FunctionNode {

		public Dfdy() {
			super();
			setnVars(2);
		}

		public Dfdy(FunctionNode[] args) {
			super(2, args);
		}

		@Override
		public FunctionNode createNode(FunctionNode[] args) {
			return new Dfdy(args);
		}

		@Override
		public Double compute(Double[] variables) {
			int i = (int) ((variables[0] - xmin) / step);
			int j = (int) ((variables[1] - ymin) / step);
			int k = Math.min(j + 1, surface[0].length - 1);
			int l = Math.max(j - 1, 0);
			return (surface[i][k].getZ() - surface[i][l].getZ()) / (2 * step);
		}

	}

	public class D2fdx extends FunctionNode {

		public D2fdx() {
			super();
			setnVars(2);
		}

		public D2fdx(FunctionNode[] args) {
			super(2, args);
		}

		@Override
		public FunctionNode createNode(FunctionNode[] args) {
			return new D2fdx(args);
		}

		@Override
		public Double compute(Double[] variables) {
			int i = (int) ((variables[0] - xmin) / step);
			int j = (int) ((variables[1] - ymin) / step);
			int k = Math.min(i + 1, surface.length - 1);
			int l = Math.max(i - 1, 0);
			return (surface[k][j].getZ() - 2 * surface[i][j].getZ() + surface[l][j]
					.getZ()) / (4 * step * step);
		}

	}

	public class D2fdy extends FunctionNode {

		public D2fdy() {
			super();
			setnVars(2);
		}

		public D2fdy(FunctionNode[] args) {
			super(2, args);
		}

		@Override
		public FunctionNode createNode(FunctionNode[] args) {
			return new D2fdy(args);
		}

		@Override
		public Double compute(Double[] variables) {
			int i = (int) ((variables[0] - xmin) / step);
			int j = (int) ((variables[1] - ymin) / step);
			int k = Math.min(j + 1, surface[0].length - 1);
			int l = Math.max(j - 1, 0);
			return (surface[i][k].getZ() - 2 * surface[i][j].getZ() + surface[i][l]
					.getZ()) / (4 * step * step);
		}

	}

	public class D2fdxdy extends FunctionNode {

		public D2fdxdy() {
			super();
			setnVars(2);
		}

		public D2fdxdy(FunctionNode[] args) {
			super(2, args);
		}

		@Override
		public FunctionNode createNode(FunctionNode[] args) {
			return new D2fdxdy(args);
		}

		@Override
		public Double compute(Double[] variables) {
			int i = (int) ((variables[0] - xmin) / step);
			int j = (int) ((variables[1] - ymin) / step);
			int k = Math.min(j + 1, surface[0].length - 1);
			int l = Math.max(j - 1, 0);
			int m = Math.min(i + 1, surface.length - 1);
			int n = Math.max(i - 1, 0);
			return (surface[m][k].getZ() - surface[m][l].getZ()
					- surface[n][k].getZ() + surface[n][l].getZ())
					/ (4 * step * step);
		}

	}

	public class Dfdt extends FunctionNode {

		public Dfdt() {
			super();
			setnVars(2);
		}

		public Dfdt(FunctionNode[] args) {
			super(2, args);
		}

		@Override
		public FunctionNode createNode(FunctionNode[] args) {
			return new Dfdt(args);
		}

		@Override
		public Double compute(Double[] variables) {
			int i = (int) ((variables[0] - xmin) / step);
			int j = (int) ((variables[1] - ymin) / step);

			return dudt[i][j];
		}

	}

	public void paint(Graphics g) {
		double timeElapse = System.currentTimeMillis() * 1E-3;
		if (Math.abs(wChanged - this.getWidth()) > 0
				|| Math.abs(hChanged - this.getHeight()) > 0) {
			wChanged = this.getWidth();
			hChanged = this.getHeight();

			processLayout();
		}
		update(g);
		// System.out.println(System.currentTimeMillis() * 1E-3 - timeElapse);
	}

	public void update(Graphics g) {
		wd.clearImageWithBackGround();
		euler.run();
		if (drawAxis && colorState == 0)
			infoColor();
		wd.paint(g);
	}

	public static void main(String[] args) {
		new PartialDifferential();
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

		TriVector eye = new TriVector(raw * cosP * cosT + x.getX(), raw * cosP
				* sinT + x.getY(), raw * sinP + x.getZ());

		graphics.setCamera(aux, eye);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_W) {
			thrust = -5;
		} else if (arg0.getKeyCode() == KeyEvent.VK_S) {
			thrust = +5;
		} else if (arg0.getKeyCode() == KeyEvent.VK_F) {
			isfastShading = !isfastShading;
			if (isfastShading) {
				graphics.setMethod(new SamplingZbuffer(3));
			} else {
				graphics.setMethod(new ZBufferPrespective());
			}
			drawFunction = true;
			graphics.removeAllElements();
		} else if (arg0.getKeyCode() == KeyEvent.VK_Z) {
			isZBuffer = !isZBuffer;
			if (isZBuffer) {
				graphics.setMethod(new ZBufferPrespective());
			} else {
				graphics.setMethod(new WiredPrespective());
			}
			drawFunction = true;

			graphics.removeAllElements();
		} else if (arg0.getKeyCode() == KeyEvent.VK_1) {
			colorState = 0;
			graphics.removeAllElements();
			drawFunction = true;
		} else if (arg0.getKeyCode() == KeyEvent.VK_A) {
			drawAxis = !drawAxis;
			axisAlreadyBuild = false;
			graphics.removeAllElements();
			drawFunction = true;
		} else if (arg0.getKeyCode() == KeyEvent.VK_N) {
			isShading = !isShading;
			if (isShading) {
				graphics.setMethod(shader);
				shader.changeNthLight(0, new TriVector(raw, raw, raw));
				graphics.removeAllElements();
				drawFunction = true;
			} else {
				graphics.setMethod(new ZBufferPrespective());
			}
		} else if (arg0.getKeyCode() == KeyEvent.VK_E) {
			Random r = new Random();
			String str = "1 / (1 + 3*exp(-(2 * x - y)))";
			switch (r.nextInt(7) + 1) {
			case 1:
				str = "1 / (1 + 4 * (x ^ 4 +  y ^ 4))";
				break;
			case 2:
				str = "exp(-((x - 1) ^ 2 + y ^ 2)) + 2 * exp(-(x ^ 2 + (y + 1) ^ 2))";
				break;
			case 3:
				str = "1 / (1 + 3 * exp(-3 * (2 * x - y)))";
				break;
			case 4:
				str = "ln((x * x + y * y + 1))";
				break;
			case 5:
				str = "(sin(x) * cos(y) - 0.5) ^ 2 + (sin(x) * sin(y) - 0.1) ^ 2";
				break;
			case 6:
				str = "sin(x * y) - cos(x * y) ";
				break;
			case 7:
				str = "25 / (1 + exp(1 / (1 + exp(-x - (y - 1))) + 1 / (1 + exp(x)) + 1/(1 + exp(y)))) - 5";
			}
			functionString.setText(str);
			graphics.removeAllElements();
			drawFunction = true;
		} else if (arg0.getKeyCode() == KeyEvent.VK_K) {
			isKakashi = !isKakashi;
			graphics.removeAllElements();
			drawFunction = true;
		} else if (arg0.getKeyCode() == KeyEvent.VK_8) {
			graphics.setMethod(new InterpolativeShader());
		} else if (arg0.getKeyCode() == KeyEvent.VK_9) {
			graphics.setMethod(new LevelSetShader());
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

	public void canvasFocus() {
		xMinTxt.transferFocusUpCycle();
		xMaxTxt.transferFocusUpCycle();
		functionString.transferFocusUpCycle();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		canvasFocus();
		newMx = e.getX();
		newMy = e.getY();
		double dx = newMx - mx;
		double dy = newMy - my;
		theta += 2 * Math.PI * (dx / wChanged);
		phi += 2 * Math.PI * (dy / hChanged);

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
		canvasFocus();

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
	public void mousePressed(MouseEvent e) {
		mx = e.getX();
		my = e.getY();

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		mRotation = e.getWheelRotation();
		xMinTxt.setText("" + (xmin - mRotation));
		xMaxTxt.setText("" + (xmax + mRotation));
		yMinTxt.setText("" + (ymin - mRotation));
		yMaxTxt.setText("" + (ymax + mRotation));
		graphics.removeAllElements();
		drawFunction = true;

	}
}
