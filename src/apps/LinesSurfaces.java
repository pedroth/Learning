package apps;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.TextField;
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
import windowThreeDim.FlatShader;
import windowThreeDim.InterpolativeShader;
import windowThreeDim.LevelSetShader;
import windowThreeDim.Line;
import windowThreeDim.Quad;
import windowThreeDim.SquareZBuffer;
import windowThreeDim.StringElement;
import windowThreeDim.TriWin;
import windowThreeDim.WiredPrespective;
import algebra.Matrix;
import algebra.TriVector;
import functions.ExpressionFunction;
import functions.SyntaxErrorException;

public class LinesSurfaces extends JFrame implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
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
	private Choice comboBox;
	private TextField fxStr;
	private TextField fyStr;
	private TextField fzStr;
	private TextField ftXStr;
	private TextField ftYStr;
	private TextField ftZStr;
	private TextField uMinTxt, uMaxTxt, vMinTxt, vMaxTxt, stepTxt, tMinTxt, tMaxTxt;
	Button drawButton;
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
	private double accelaration;
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
	 * line
	 */
	private TriVector[] line;
	/**
	 * variables describing the domain of the surface or line
	 */
	private double umin, umax, vmin, vmax, tmin, tmax, step;
	/**
	 * computes function from string
	 */
	private ExpressionFunction xFunc;
	private ExpressionFunction yFunc;
	private ExpressionFunction zFunc;
	private ExpressionFunction xTFunc;
	private ExpressionFunction yTFunc;
	private ExpressionFunction zTFunc;
	private String[] vars = { "u", "v" };
	private String[] tVars = { "t" };
	/**
	 * ZBuffer/WiredFrame
	 */
	private boolean isZBuffer;
	/**
	 * maximum and minimum x,y,z values of the mesh
	 */
	private double maxX;
	private double minX;
	private double maxY;
	private double minY;
	private double maxZ;
	private double minZ;
	/**
	 * colorState = 1 -> z - heatMap, colorState = 2 -> random, colorState = 3
	 * -> shading
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
	 * shader Paint method
	 */
	private FlatShader shader;
	/**
	 * motion light particle
	 */
	private TriVector motionLight;
	/**
	 * check if light is moving.
	 */
	private boolean isMotionLight;
	/**
	 * variable control
	 */
	private boolean isShading;

	private static String helpText = "< w > : Camera move foward / zoom in \n\n" + "< s > : Camera move backward /zoom out \n\n" + "< z > : Toggle wireframe / zbuffer \n\n" + "< n > : Toggle flat shading \n\n" + "< [1-3] > : change color of surface \n\n" + "< [4 - 7] > : various surfaces examples \n\n" + "< l > : rotating light mode \n\n" + "< 8 > : interpolative colors \n\n" + "< 9 > : level set shader\n\n" + "< Available functions > : sin ,cos, exp, ln, tan, acos, asin, atan, min, adding more \n\n" + "< operators > : +, - , *, ^ \n\n" + "< Available constants > : pi\n\n" + "< a > : draw Axis \n\n" + "< mouse > : rotate camera\n" + "---------------------------------------------------\n\n" + "< g > : generate obj file!!!\n\n" + "----------------------------------------------------\n\n" + "Made by Pedroth";

	private boolean axisAlreadyBuild;

	public LinesSurfaces(boolean isApplet) {
		// Set JFrame title
		super("Draw Lines {x(t),y(t),z(t)} and Surfaces {x(u,v),y(u,v),z(u,v)}");

		// init
		this.setLayout(null);
		/**
		 * begin the engine
		 */
		graphics = new TriWin();
		wd = graphics.getBuffer();
		shader = new FlatShader();
		shader.addLightPoint(new TriVector(1, 0, 0));
		shader.setShininess(15);
		shader.setAmbientLightParameter(0.5);
		graphics.setMethod(shader);
		// shader.setCullBack(true);
		wd.setBackGroundColor(Color.black);
		isZBuffer = true;
		colorState = 0;
		drawAxis = false;

		// Set default close operation for JFrame
		if (!isApplet) {
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		// Set JFrame size
		setSize(800, 600);

		wChanged = this.getWidth();
		hChanged = this.getHeight();

		wd.setWindowSize(7 * wChanged / 10, hChanged);

		fxStr = new TextField();
		fyStr = new TextField();
		fzStr = new TextField();
		ftXStr = new TextField();
		ftYStr = new TextField();
		ftZStr = new TextField();
		uMinTxt = new TextField();
		uMaxTxt = new TextField();
		vMinTxt = new TextField();
		vMaxTxt = new TextField();
		tMinTxt = new TextField();
		tMaxTxt = new TextField();
		stepTxt = new TextField();
		comboBox = new Choice();
		comboBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				graphics.removeAllElements();
				drawFunction = true;
				processLayout();
			}
		});
		comboBox.add("Surface");
		comboBox.add("Line");
		drawButton = new Button("Draw");
		drawButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				drawFunction = true;
				graphics.removeAllElements();
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
		accelaration = 0;

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

		isMotionLight = false;
		motionLight = new TriVector(raw, raw, raw);
		isShading = true;
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
			this.add(comboBox);
			this.add(fxStr);
			this.add(fyStr);
			this.add(fzStr);
			this.add(ftXStr);
			this.add(ftYStr);
			this.add(ftZStr);
			this.add(uMinTxt);
			this.add(uMaxTxt);
			this.add(vMinTxt);
			this.add(vMaxTxt);
			this.add(tMaxTxt);
			this.add(tMinTxt);
			this.add(stepTxt);
			this.add(drawButton);
			this.add(zoomFit);
			comboBox.select(0);
			fxStr.setText("(1 + 0.5 * cos(v)) * cos(u)");
			fyStr.setText("(1 + 0.5 * cos(v)) * sin(u)");
			fzStr.setText("0.5 * sin(v)");
			uMinTxt.setText("0");
			uMaxTxt.setText("2*pi");
			vMinTxt.setText("0");
			vMaxTxt.setText("2*pi");
			ftXStr.setText("(2 + cos(2 * t))*cos(3 * t)");
			ftYStr.setText("(2 + cos(2 * t)) * sin(3 * t)");
			ftZStr.setText("sin(4 * t)");
			tMinTxt.setText("0");
			tMaxTxt.setText("2*pi");
			stepTxt.setText("pi/12");
			init = false;
		}
		/**
		 * pseudo - canvas
		 */
		wd.setWindowSize((border + 2) * wChanged / 100, hChanged);
		frameGraphics.setWindowSize((100 - border) * wChanged / 100, hChanged);

		if (comboBox.getSelectedItem() == "Surface") {
			hideLineUI();
			processSurfaceUI();
		} else {
			hideSurfaceUI();
			processLineUI();
		}
		frameGraphics.paintTranslate(this.getGraphics(), border * wChanged / 100, 0);
	}

	public void hideLineUI() {
		fxStr.setVisible(true);
		fyStr.setVisible(true);
		fzStr.setVisible(true);
		uMinTxt.setVisible(true);
		uMaxTxt.setVisible(true);
		vMinTxt.setVisible(true);
		vMaxTxt.setVisible(true);
		ftXStr.setVisible(false);
		ftYStr.setVisible(false);
		ftZStr.setVisible(false);
		tMaxTxt.setVisible(false);
		tMinTxt.setVisible(false);
	}

	public void hideSurfaceUI() {
		fxStr.setVisible(false);
		fyStr.setVisible(false);
		fzStr.setVisible(false);
		uMinTxt.setVisible(false);
		uMaxTxt.setVisible(false);
		vMinTxt.setVisible(false);
		vMaxTxt.setVisible(false);
		ftXStr.setVisible(true);
		ftYStr.setVisible(true);
		ftZStr.setVisible(true);
		tMaxTxt.setVisible(true);
		tMinTxt.setVisible(true);
	}

	public void processLineUI() {
		int border = 70;

		/**
		 * comboBox
		 */
		comboBox.setBounds((border + 7) * wChanged / 100, 1 * hChanged / 100, 10 * wChanged / 100, 3 * hChanged / 100);
		/**
		 * function text box
		 */
		ftXStr.setBounds((border + 7) * wChanged / 100, hChanged / 10, 20 * wChanged / 100, 5 * hChanged / 100);
		ftYStr.setBounds((border + 7) * wChanged / 100, 2 * hChanged / 10, 20 * wChanged / 100, 5 * hChanged / 100);
		ftZStr.setBounds((border + 7) * wChanged / 100, 3 * hChanged / 10, 20 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * tmin
		 */
		tMinTxt.setBounds((border + 5) * wChanged / 100, 43 * hChanged / 100, 6 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * tmax
		 */
		tMaxTxt.setBounds((border + 18) * wChanged / 100, 43 * hChanged / 100, 6 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * t step
		 */
		stepTxt.setBounds((border + 5) * wChanged / 100, 55 * hChanged / 100, 6 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * draw Button
		 */
		drawButton.setBounds((border + 13) * wChanged / 100, 55 * hChanged / 100, 15 * wChanged / 100, 7 * hChanged / 100);
		/**
		 * zoom fit button
		 */
		zoomFit.setBounds((border + 13) * wChanged / 100, 65 * hChanged / 100, 10 * wChanged / 100, 5 * hChanged / 100);

		Graphics g = frameGraphics.getGraphics();
		/**
		 * clear panel
		 */
		frameGraphics.clearImageWithBackGround();
		/**
		 * Function text
		 */
		frameGraphics.setDrawColor(Color.white);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		frameGraphics.drawString("X(t)", 0.12, 1 - 0.18);
		frameGraphics.drawString("Y(t)", 0.12, 1 - 0.28);
		frameGraphics.drawString("Z(t)", 0.12, 1 - 0.38);
		/**
		 * tmin/tmax text
		 */
		frameGraphics.drawString("tmin", 0.12, 1 - 0.46);
		frameGraphics.drawString("tmax", 0.65, 1 - 0.46);
		/**
		 * step text
		 */
		frameGraphics.drawString("t step", 0.12, 1 - 0.58);
	}

	public void processSurfaceUI() {
		int border = 70;

		/**
		 * comboBox
		 */
		comboBox.setBounds((border + 7) * wChanged / 100, 1 * hChanged / 100, 10 * wChanged / 100, 3 * hChanged / 100);
		/**
		 * function text box
		 */
		fxStr.setBounds((border + 7) * wChanged / 100, hChanged / 10, 20 * wChanged / 100, 5 * hChanged / 100);
		fyStr.setBounds((border + 7) * wChanged / 100, 2 * hChanged / 10, 20 * wChanged / 100, 5 * hChanged / 100);
		fzStr.setBounds((border + 7) * wChanged / 100, 3 * hChanged / 10, 20 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * umin
		 */
		uMinTxt.setBounds((border + 5) * wChanged / 100, 43 * hChanged / 100, 6 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * umax
		 */
		uMaxTxt.setBounds((border + 18) * wChanged / 100, 43 * hChanged / 100, 6 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * vmin
		 */
		vMinTxt.setBounds((border + 5) * wChanged / 100, 55 * hChanged / 100, 6 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * vmax
		 */
		vMaxTxt.setBounds((border + 18) * wChanged / 100, 55 * hChanged / 100, 6 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * U/V step
		 */
		stepTxt.setBounds((border + 5) * wChanged / 100, 65 * hChanged / 100, 6 * wChanged / 100, 5 * hChanged / 100);
		/**
		 * draw Button
		 */
		drawButton.setBounds((border + 13) * wChanged / 100, 65 * hChanged / 100, 15 * wChanged / 100, 7 * hChanged / 100);
		/**
		 * zoom fit button
		 */
		zoomFit.setBounds((border + 13) * wChanged / 100, 75 * hChanged / 100, 10 * wChanged / 100, 5 * hChanged / 100);

		Graphics g = frameGraphics.getGraphics();
		/**
		 * clear panel
		 */
		frameGraphics.clearImageWithBackGround();
		/**
		 * Function text
		 */
		frameGraphics.setDrawColor(Color.white);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		frameGraphics.drawString("X(u, v)", 0.08, 1 - 0.18);
		frameGraphics.drawString("Y(u, v)", 0.08, 1 - 0.28);
		frameGraphics.drawString("Z(u, v)", 0.08, 1 - 0.38);
		/**
		 * umin/umax and vmin/vmax text
		 */
		frameGraphics.drawString("umin", 0.08, 1 - 0.46);
		frameGraphics.drawString("umax", 0.65, 1 - 0.46);
		frameGraphics.drawString("vmin", 0.08, 1 - 0.58);
		frameGraphics.drawString("vmax", 0.65, 1 - 0.58);
		/**
		 * step text
		 */
		frameGraphics.drawString("U/V step", 0.05, 1 - 0.68);

	}

	class Euler {

		public void run() {
			currentTime = (System.currentTimeMillis()) * 1E-03;
			double dt = currentTime - oldTime;
			oldTime = currentTime;
			accelaration = -velocity + thrust;
			velocity += accelaration * dt;
			raw += velocity * dt;
			orbit(theta, phi, focalPoint);
			if (drawAxis) {
				buildAxis();
			}
			if (drawFunction) {
				if (comboBox.getSelectedItem() == "Surface")
					buildSurface();
				else
					buildLine();
			}
			if (isZoomFit)
				cameraFindGraph();

			updateLight(dt);

			graphics.drawElements();
			repaint();
		}

		/**
		 * update light
		 */
		public void updateLight(double dt) {
			if (isMotionLight) {
				TriVector v = new TriVector(-motionLight.getY(), motionLight.getX(), 0);
				v.multiConstMatrix(dt);
				motionLight.sum(v);
				shader.changeNthLight(0, motionLight);
			} else {
				TriVector aux = TriVector.sum(shader.getEyePos(), new TriVector(0, 0, 3));
				shader.changeNthLight(0, aux);
			}
		}

		/**
		 * build the parametric line
		 */
		public void buildLine() {
			double nt, x, y, z, t, colorHSB;
			int inT;
			int MaxPoly = 25600;
			Random r = new Random();
			colorHSB = r.nextDouble();
			drawFunction = false;
			tmin = numericRead(tMinTxt.getText());
			tmax = numericRead(tMaxTxt.getText());
			step = numericRead(stepTxt.getText());
			xTFunc = new ExpressionFunction(ftXStr.getText(), tVars);
			yTFunc = new ExpressionFunction(ftYStr.getText(), tVars);
			zTFunc = new ExpressionFunction(ftZStr.getText(), tVars);
			maxX = Double.NEGATIVE_INFINITY;
			minX = Double.POSITIVE_INFINITY;
			maxY = Double.NEGATIVE_INFINITY;
			minY = Double.POSITIVE_INFINITY;
			maxZ = Double.NEGATIVE_INFINITY;
			minZ = Double.POSITIVE_INFINITY;
			try {
				xTFunc.init();
				yTFunc.init();
				zTFunc.init();
			} catch (SyntaxErrorException e) {
				JOptionPane.showMessageDialog(null, "there is a syntax error in the formula, pls change the formula." + String.format("%n") + " try to use more brackets, try not to cocatenate 2*x^2 as 2x2." + String.format("%n") + "check also for simple errors like 1/*2.");
			}
			nt = Math.abs(tmax - tmin) / (step);
			if (!isZBuffer) {
				MaxPoly = 50000;
			}

			if (nt > MaxPoly) {
				double aux = nt * step;
				maxPolyConstraint(aux);
				step = numericRead(stepTxt.getText());
				nt = Math.abs(tmax - tmin) / (step);
			}

			inT = (int) Math.floor(nt);
			line = new TriVector[inT + 1];
			for (int i = 0; i < inT; i++) {
				if (line[i] == null) {
					t = tmin + i * step;
					Double[] tvar = { t };
					x = xTFunc.compute(tvar);
					y = yTFunc.compute(tvar);
					z = zTFunc.compute(tvar);
					line[i] = new TriVector(x, y, z);
				}
				if (line[i + 1] == null) {
					t = tmin + (i + 1) * step;
					Double[] tvar = { t };
					x = xTFunc.compute(tvar);
					y = yTFunc.compute(tvar);
					z = zTFunc.compute(tvar);
					line[i + 1] = new TriVector(x, y, z);
				}
				double auxX = line[i].getX();
				double auxY = line[i].getY();
				double auxZ = line[i].getZ();
				if (!Double.isInfinite(auxX) && !Double.isNaN(auxX)) {
					maxX = Math.max(auxX, maxX);
					minX = Math.min(auxX, minX);
				}
				if (!Double.isInfinite(auxY) && !Double.isNaN(auxY)) {
					maxY = Math.max(auxY, maxY);
					minY = Math.min(auxY, minY);
				}
				if (!Double.isInfinite(auxZ) && !Double.isNaN(auxZ)) {
					maxZ = Math.max(auxZ, maxZ);
					minZ = Math.min(auxZ, minZ);
				}
			}
			for (int i = 0; i < inT; i++) {
				Element e = new Line(line[i], line[i + 1]);
				setElementColor(e, colorHSB);
				graphics.addtoList(e);
			}
		}
	}

	public void cameraFindGraph() {
		Double v[] = new Double[3];
		v[0] = (maxX + minX) / 2;
		v[1] = (maxY + minY) / 2;
		v[2] = (maxZ + minZ) / 2;
		raw = 3 * maxX;
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

	/**
	 * build surface bad programming repeating code for xyz i should have an
	 * array instead.
	 */
	public void buildSurface() {
		double nu, nv, x, y, z, u, v, colorHSB;
		int inu, inv;
		int MaxPoly = 25600;
		Random r = new Random();
		colorHSB = r.nextDouble();
		drawFunction = false;
		umin = numericRead(uMinTxt.getText());
		umax = numericRead(uMaxTxt.getText());
		vmin = numericRead(vMinTxt.getText());
		vmax = numericRead(vMaxTxt.getText());
		step = numericRead(stepTxt.getText());
		xFunc = new ExpressionFunction(fxStr.getText(), vars);
		yFunc = new ExpressionFunction(fyStr.getText(), vars);
		zFunc = new ExpressionFunction(fzStr.getText(), vars);
		maxX = Double.NEGATIVE_INFINITY;
		minX = Double.POSITIVE_INFINITY;
		maxY = Double.NEGATIVE_INFINITY;
		minY = Double.POSITIVE_INFINITY;
		maxZ = Double.NEGATIVE_INFINITY;
		minZ = Double.POSITIVE_INFINITY;
		try {
			xFunc.init();
			yFunc.init();
			zFunc.init();
		} catch (SyntaxErrorException e) {
			JOptionPane.showMessageDialog(null, "there is a syntax error in the formula, pls change the formula." + String.format("%n") + " try to use more brackets, try not to cocatenate 2*x^2 as 2x2." + String.format("%n") + "check also for simple errors like 1/*2.");
		}
		nu = Math.abs(umax - umin) / (step);
		nv = Math.abs(vmax - vmin) / (step);

		if (!isZBuffer) {
			MaxPoly = 50000;
		}

		if (nu * nv > MaxPoly) {
			double aux = (nu * nv) * (step * step);
			maxPolyConstraint(aux);
			step = numericRead(stepTxt.getText());
			nu = Math.abs(umax - umin) / (step);
			nv = Math.abs(vmax - vmin) / (step);
		}

		inu = (int) Math.floor(nu);
		inv = (int) Math.floor(nv);
		surface = new TriVector[inu + 1][inv + 1];
		for (int j = 0; j < inv; j++) {
			for (int i = 0; i < inu; i++) {
				if (surface[i][j] == null) {
					u = umin + i * step;
					v = vmin + j * step;
					Double[] uv = { u, v };
					x = xFunc.compute(uv);
					y = yFunc.compute(uv);
					z = zFunc.compute(uv);
					surface[i][j] = new TriVector(x, y, z);
					// surface[i][j] = new TriVector(x, y, r.nextDouble());
				}
				if (surface[i + 1][j] == null) {
					u = umin + (i + 1) * step;
					v = vmin + j * step;
					Double[] uv = { u, v };
					x = xFunc.compute(uv);
					y = yFunc.compute(uv);
					z = zFunc.compute(uv);
					surface[i + 1][j] = new TriVector(x, y, z);
					// surface[i+1][j] = new TriVector(x, y, r.nextDouble());
				}
				if (surface[i + 1][j + 1] == null) {
					u = umin + (i + 1) * step;
					v = vmin + (j + 1) * step;
					Double[] uv = { u, v };
					x = xFunc.compute(uv);
					y = yFunc.compute(uv);
					z = zFunc.compute(uv);
					surface[i + 1][j + 1] = new TriVector(x, y, z);
					// surface[i+1][j+1] = new TriVector(x, y, r.nextDouble());
				}
				if (surface[i][j + 1] == null) {
					u = umin + (i) * step;
					v = vmin + (j + 1) * step;
					Double[] uv = { u, v };
					x = xFunc.compute(uv);
					y = yFunc.compute(uv);
					z = zFunc.compute(uv);
					surface[i][j + 1] = new TriVector(x, y, z);
					// surface[i][j+1] = new TriVector(x, y, r.nextDouble());
				}
				double auxX = surface[i][j].getX();
				double auxY = surface[i][j].getY();
				double auxZ = surface[i][j].getZ();
				if (!Double.isInfinite(auxX) && !Double.isNaN(auxX)) {
					maxX = Math.max(auxX, maxX);
					minX = Math.min(auxX, minX);
				}
				if (!Double.isInfinite(auxY) && !Double.isNaN(auxY)) {
					maxY = Math.max(auxY, maxY);
					minY = Math.min(auxY, minY);
				}
				if (!Double.isInfinite(auxZ) && !Double.isNaN(auxZ)) {
					maxZ = Math.max(auxZ, maxZ);
					minZ = Math.min(auxZ, minZ);
				}
			}
		}

		for (int j = 0; j < inv; j++) {
			for (int i = 0; i < inu; i++) {
				Element e = new Quad(surface[i][j], surface[i + 1][j], surface[i + 1][j + 1], surface[i][j + 1]);
				setElementColor(e, colorHSB);
				graphics.addtoList(e);
			}
		}
	}

	public void setElementColor(Element e, double colorHSB) {
		double red = 0;
		double blue = 240.0 / 360.0;
		double green = 120.0 / 360.0;

		for (int i = 0; i < e.getNumOfPoints(); i++) {
			double z = e.getNPoint(i).getZ();
			double x = -1 + 2 * (z - minZ) / (maxZ - minZ);

			if (colorState == 0) {
				/**
				 * linear interpolation between blue color and red
				 */
				colorHSB = blue + (red - blue) * 0.5 * (x + 1);
				e.setColorPoint(Color.getHSBColor((float) colorHSB, 1f, 1f), i);
			} else if (colorState == 1) {
				Random r = new Random();
				e.setColorPoint(Color.getHSBColor((float) colorHSB, r.nextFloat(), 1f), i);
			} else if (colorState == 2) {
				e.setColorPoint(Color.blue, i);
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

	public void maxPolyConstraint(double delta) {
		double nextStep = Math.sqrt(delta / 10E3);

		if (!isZBuffer) {
			nextStep = Math.sqrt(delta / 10000);
		}

		stepTxt.setText("" + nextStep);
		JOptionPane.showMessageDialog(null, "the X/Y step is too low, pls choose a higher one");

	}

	public void paint(Graphics g) {
		if (Math.abs(wChanged - this.getWidth()) > 0 || Math.abs(hChanged - this.getHeight()) > 0) {
			wChanged = this.getWidth();
			hChanged = this.getHeight();
		}
		processLayout();
		update(g);
	}

	public void update(Graphics g) {
		wd.clearImageWithBackGround();
		euler.run();
		wd.paint(g);
	}

	public static void main(String[] args) {
		new LinesSurfaces(false);
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
		// shader.changeNthLight(0, eye);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_W) {
			thrust = -5;
		} else if (arg0.getKeyCode() == KeyEvent.VK_S) {
			thrust = +5;
		} else if (arg0.getKeyCode() == KeyEvent.VK_Z) {
			isZBuffer = !isZBuffer;
			if (isZBuffer) {
				graphics.setMethod(shader);
			} else {
				graphics.setMethod(new WiredPrespective());
			}
			drawFunction = true;

			graphics.removeAllElements();
		} else if (arg0.getKeyCode() == KeyEvent.VK_1) {
			colorState = 0;
			graphics.removeAllElements();
			drawFunction = true;
		} else if (arg0.getKeyCode() == KeyEvent.VK_2) {
			colorState = 1;
			graphics.removeAllElements();
			drawFunction = true;
		} else if (arg0.getKeyCode() == KeyEvent.VK_3) {
			colorState = 2;
			graphics.removeAllElements();
			drawFunction = true;
		} else if (arg0.getKeyCode() == KeyEvent.VK_4) {
			comboBox.select(0);
			fxStr.setText("cos(u) + v * (cos(u) * cos(0.5 * u))");
			fyStr.setText("sin(u) + v * (sin(u) * cos(0.5 * u))");
			fzStr.setText("v * sin(0.5 * u)");
			uMinTxt.setText("0");
			uMaxTxt.setText("2*pi");
			vMinTxt.setText("-0.5");
			vMaxTxt.setText("0.5");
			stepTxt.setText("pi/32");
			graphics.removeAllElements();
			drawFunction = true;
		} else if (arg0.getKeyCode() == KeyEvent.VK_5) {
			comboBox.select(0);
			fxStr.setText("(1 + 0.5 * cos(v)) * cos(u)");
			fyStr.setText("(1 + 0.5 * cos(v)) * sin(u)");
			fzStr.setText("0.5 * sin(v)");
			uMinTxt.setText("0");
			uMaxTxt.setText("2*pi");
			vMinTxt.setText("0");
			vMaxTxt.setText("2*pi");
			stepTxt.setText("pi/24");
			graphics.removeAllElements();
			drawFunction = true;
		} else if (arg0.getKeyCode() == KeyEvent.VK_6) {
			comboBox.select(0);
			fxStr.setText("0.5 * sin(u) * cos(v)");
			fyStr.setText("0.5 * sin(u) * sin(v)");
			fzStr.setText("0.5 * cos(u)");
			uMinTxt.setText("0");
			uMaxTxt.setText("pi");
			vMinTxt.setText("0");
			vMaxTxt.setText("2*pi");
			stepTxt.setText("pi/24");
			graphics.removeAllElements();
			drawFunction = true;
		} else if (arg0.getKeyCode() == KeyEvent.VK_7) {
			comboBox.select(0);
			fxStr.setText("(4 / 3) ^ v * sin(u) * sin(u) * cos(v)");
			fyStr.setText("(4 / 3) ^ v * sin(u) * sin(u) * sin(v) ");
			fzStr.setText("(4/3)^ v * sin(u) * cos(u)");
			uMinTxt.setText("0");
			uMaxTxt.setText("pi");
			vMinTxt.setText("-6");
			vMaxTxt.setText("1.1*pi");
			stepTxt.setText("pi/24");
			graphics.removeAllElements();
			drawFunction = true;
		} else if (arg0.getKeyCode() == KeyEvent.VK_N) {
			isShading = !isShading;
			if (isShading) {
				graphics.setMethod(shader);
			} else
				graphics.setMethod(new SquareZBuffer());
		} else if (arg0.getKeyCode() == KeyEvent.VK_A) {
			drawAxis = !drawAxis;
			axisAlreadyBuild = false;
			graphics.removeAllElements();
			drawFunction = true;
		} else if (arg0.getKeyCode() == KeyEvent.VK_L) {
			isMotionLight = !isMotionLight;
		} else if (arg0.getKeyCode() == KeyEvent.VK_8) {
			graphics.setMethod(new InterpolativeShader());
		} else if (arg0.getKeyCode() == KeyEvent.VK_9) {
			graphics.setMethod(new LevelSetShader());
		} else if (arg0.getKeyCode() == KeyEvent.VK_G) {
			generateMesh();
		}

	}

	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	private void generateMesh() {
		String s = (String)JOptionPane.showInputDialog(
		                    this,
		                    (comboBox.getSelectedItem() == "Surface")?"UV step":"T step",
		                    "Obj file generation",
		                    JOptionPane.PLAIN_MESSAGE,
		                    null,
		                    null,
		                    null);


		if(!(s == null)) {
			double step = numericRead(s);
			if(comboBox.getSelectedItem() == "Surface") {
				generateSurf(step);
			} else {
				generateLine(step);
			}
		}
	}

	private void generateLine(double step) {
		int numSamples = (int) Math.floor(Math.abs(tmax - tmin) / step);
		double newStep = (tmax - tmin) / (numSamples-1);
		TriVector[] curve = new TriVector[numSamples];
		for (int i = 0; i < numSamples; i++) {
			double t = tmin + i * newStep;
			Double[] tvar = { t };
			double x = xTFunc.compute(tvar);
			double y = yTFunc.compute(tvar);
			double z = zTFunc.compute(tvar);
			curve[i] = new TriVector(x,y,z);
		}
		String objFile = "";
		for (int i = 0; i < curve.length; i++) {
			objFile += "v" + " " + curve[i].getX() + " " + curve[i].getY() + " " + curve[i].getZ() + "\n";
		}
		for (int i = 0; i < curve.length - 1; i++) {
			objFile += "l" + " " + (i+1) + " " + (i+2) + "\n";
		}
		TextFrame frame = new TextFrame("Curve obj file", objFile);
	}

	private void generateSurf(double step) {
		int numSamplesU = (int) Math.floor(Math.abs(umax - umin) / step);
		double newStepU = (umax - umin) / (numSamplesU-1);
		int numSamplesV = (int) Math.floor(Math.abs(vmax - vmin) / step);
		double newStepV = (vmax - vmin) / (numSamplesV-1);
		TriVector[][] surf = new TriVector[numSamplesU][numSamplesV];
		for (int i = 0; i < numSamplesU; i++) {
			for (int j = 0; j < numSamplesV; j++) {
				double u = umin + i * newStepU;
				double v = vmin + j * newStepV;
				Double[] uv = { u, v };
				double x = xFunc.compute(uv);
				double y = yFunc.compute(uv);
				double z = zFunc.compute(uv);
				surf[i][j] = new TriVector(x, y, z);
			}
		}

		String objFile = "";
		for (int i = 0; i < numSamplesU; i++) {
			for (int j = 0; j < numSamplesV; j++) {
				objFile += "v" + " " + surf[i][j].getX() + " " + surf[i][j].getY() + " " + surf[i][j].getZ() + "\n";
			}
		}
		for (int i = 0; i < numSamplesU - 1; i++) {
			for (int j = 0; j < numSamplesV - 1; j++) {
				int index = j + numSamplesV * i + 1;
				objFile += "f" + " " + index + " " + (index + numSamplesV) + " " + (index + numSamplesV + 1)+"\n";
				objFile += "f" + " " + index + " " + (index + numSamplesV + 1) + " " + (index + 1)+"\n";
			}
		}
		TextFrame frame = new TextFrame("Surface obj file", objFile);
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
		uMinTxt.transferFocusUpCycle();
		uMaxTxt.transferFocusUpCycle();
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
		uMinTxt.setText("" + (umin - mRotation));
		uMaxTxt.setText("" + (umax + mRotation));
		vMinTxt.setText("" + (vmin - mRotation));
		vMaxTxt.setText("" + (vmax + mRotation));
		graphics.removeAllElements();
		drawFunction = true;

	}
}
