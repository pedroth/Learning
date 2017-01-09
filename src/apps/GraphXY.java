package apps;

import algebra.Matrix;
import algebra.TriVector;
import functionNode.CombinationNode;
import functionNode.LinearSystemError;
import functionNode.PedroNode;
import functions.ExpressionFunction;
import functions.SyntaxErrorException;
import visualization.TextFrame;
import window.ImageWindow;
import windowThreeDim.*;
import windowThreeDim.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class GraphXY extends JFrame implements MouseListener,
        MouseMotionListener, KeyListener, MouseWheelListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
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
            + "< mouse > : rotate camera\n\n" + "Made by Pedroth";
    JButton drawButton;
    JButton minButton;
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
    private JTextField functionString;
    private JTextField xMinTxt, xMaxTxt, yMinTxt, yMaxTxt, stepTxt;
    private JTextArea minMaxTxt;
    private JButton maxButton;
    private String txtAreaStr;
    private JButton clearButton;
    private JButton zoomFit;
    private JPanel panel;
    /**
     * to check the need to add UI on the JFrame
     */
    private boolean init;
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
     * variables describing the domain of the function
     */
    private double xmin, xmax, ymin, ymax, step;
    /**
     * computes function from string
     */
    private ExpressionFunction exprFunction;
    private String[] vars = {"x", "y"};
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
     * if -1 then minimizing a function, if 1 the maximizing function
     */
    private double gradientFlow;
    /**
     * points that flow through gradient flow
     */
    private TriVector[] optimalPoints;
    /**
     * number of points that goes through the gradient flow
     */
    private int numGradPoints = 10;
    /**
     * sphere
     */
    private TriVector[][] sphere;
    /**
     * counts time when gradient flow is called
     */
    private double gradientTime;
    /**
     * max time that gradient flow occurs in seconds
     */
    private double maxGradientTime;
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
    private boolean axisAlreadyBuild;

    public GraphXY(boolean isApplet) {
        /*
         * Set JFrame title.
		 */
        super("Draw Graph XY");

        this.setLayout(null);

        /*
         * Begin the engine
         */
        graphics = new TriWin();
        wd = graphics.getBuffer();
        ZBufferPrespective painter = new ZBufferPrespective();
        graphics.setMethod(painter);
        wd.setBackGroundColor(Color.black);
        isZBuffer = true;
        colorState = 0;
        drawAxis = false;
        /*
         * int global variables
         */
        gradientFlow = 0;
        optimalPoints = new TriVector[numGradPoints];
        buildSphere(0.1);
        maxGradientTime = 3.0;


        /*
         * Set default close operation for JFrame.
		 */
        if (!isApplet) {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        /*
		 * Set JFrame size.
		 */
        setSize(800, 600);

        wChanged = this.getWidth();
        hChanged = this.getHeight();

        wd.setWindowSize(7 * wChanged / 10, hChanged);

        /*
		 * UI crap initialization.
		 */
        panel = new JPanel();
        functionString = new JTextField();
        xMinTxt = new JTextField();
        xMaxTxt = new JTextField();
        yMinTxt = new JTextField();
        yMaxTxt = new JTextField();
        minMaxTxt = new JTextArea();
        stepTxt = new JTextField();
        drawButton = new JButton("Draw");
        drawButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                drawFunction = true;
                graphics.removeAllElements();
            }
        });
        minButton = new JButton("Min");
        minButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                gradientFlow = -1.0;
                initGradientFlow();
                gradientTime = 0;

            }
        });
        maxButton = new JButton("Max");
        maxButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                gradientFlow = 1.0;
                initGradientFlow();
                gradientTime = 0;
            }
        });

        clearButton = new JButton("Clear Table");
        clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                txtAreaStr = "\tx \t y \t z\n";
                processLayout();
            }
        });
        zoomFit = new JButton("Zoom fit");
        zoomFit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                isZoomFit = true;
            }
        });
        isZoomFit = false;
        init = true;

        /*
         * init camera values
         */
        raw = 3;
        velocity = 0;
        accelaration = 0;

        oldTime = (System.currentTimeMillis()) * 1E-03;
        thrust = 0;

        drawFunction = true;
        focalPoint = new TriVector();
        euler = new Euler();

        mRotation = 0;

        processLayout();

		/*
		 * Make JFrame visible.
		 */
        setVisible(true);

        /*
		 * Add listeners.
		 */
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);

        isShading = false;
        shader = new FlatShader();
        shader.setAmbientLightParameter(0.5);
        shader.setShininess(25);
        shader.addLightPoint(new TriVector(raw, raw, raw));

        axisAlreadyBuild = false;
    }

    public static void main(String[] args) {
        new GraphXY(false);
    }

    public void processLayout() {
        int border = 70;

        if (init) {
            panel.setLayout(new GridLayout(2, 1));
            JPanel upperPanel = new JPanel(new GridLayout(8,1));
            JPanel functionPanel = new JPanel();
            functionPanel.setLayout(new GridLayout(1, 2));
            functionPanel.add(new JLabel("F(x,y)"));
            functionPanel.add(functionString);
            upperPanel.add(functionPanel);
            JPanel intervalPanelX = new JPanel();
            intervalPanelX.setLayout(new GridLayout(1, 2));
            intervalPanelX.add(new JLabel("xmin"));
            intervalPanelX.add(new JLabel("xmax"));
            upperPanel.add(intervalPanelX);
            JPanel intervalPanelXValue = new JPanel();
            intervalPanelXValue.setLayout(new GridLayout(1, 2));
            intervalPanelXValue.add(xMinTxt);
            intervalPanelXValue.add(xMaxTxt);
            upperPanel.add(intervalPanelXValue);
            JPanel intervalPanelY = new JPanel();
            intervalPanelY.setLayout(new GridLayout(1, 2));
            intervalPanelY.add(new JLabel("ymin"));
            intervalPanelY.add(new JLabel("ymax"));
            upperPanel.add(intervalPanelY);
            JPanel intervalPanelYValue = new JPanel();
            intervalPanelYValue.setLayout(new GridLayout(1, 2));
            intervalPanelYValue.add(yMinTxt);
            intervalPanelYValue.add(yMaxTxt);
            upperPanel.add(intervalPanelYValue);
            JPanel stepPanel = new JPanel();
            stepPanel.setLayout(new GridLayout(1, 2));
            stepPanel.add(new JLabel("X\\Y step"));
            stepPanel.add(stepTxt);
            upperPanel.add(stepPanel);
            JPanel drawButtonPanel = new JPanel();
            drawButtonPanel.setLayout(new GridLayout(1, 2));
            drawButtonPanel.add(new JLabel(""));
            drawButtonPanel.add(drawButton);
            upperPanel.add(drawButtonPanel);
            JPanel minMaxPanel = new JPanel(new GridLayout(1, 2));
            minMaxPanel.add(minButton);
            minMaxPanel.add(maxButton);
            upperPanel.add(minMaxPanel);
            panel.add(upperPanel);
            JPanel lowerPane = new JPanel(new GridLayout(2,1));
            JScrollPane areaScrollPane = new JScrollPane(minMaxTxt);
            areaScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            areaScrollPane.setPreferredSize(new Dimension(250, 250));
            lowerPane.add(areaScrollPane);
            JPanel clearZoomPanel = new JPanel(new GridLayout(1, 2));
            clearZoomPanel.add(clearButton);
            clearZoomPanel.add(zoomFit);
            JPanel dummyPanel = new JPanel(new GridLayout(2,1));
            dummyPanel.add(clearZoomPanel);
            dummyPanel.add(new JLabel());
            lowerPane.add(dummyPanel);
            panel.add(lowerPane);
            functionString.setText("sin( x ^ 2 + y ^ 2)");
            xMinTxt.setText("-1");
            xMaxTxt.setText("1");
            yMinTxt.setText("-1");
            yMaxTxt.setText("1");
            stepTxt.setText("0.25");
            txtAreaStr = "\tx \t y \t z\n";
            this.add(panel);
            init = false;
        }
        /**
         * pseudo - canvas
         */
        wd.setWindowSize(border * wChanged / 100, hChanged);
        panel.setBounds(border * wChanged / 100, 0, (100 - border) * wChanged / 100, hChanged);
        minMaxTxt.setText(txtAreaStr);
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
     * @param s string to be read.
     * @return computation of the expression in s
     */
    public double numericRead(String s) {
        if ("".equals(s))
            return 0;
        ExpressionFunction in;
        in = new ExpressionFunction(s, new String[]{});
        in.init();
        return in.compute(new Double[]{});
    }

    public void buildFunction() {
        double nx, ny, x, y, z, colorHSB;
        int inx, iny;
        int MaxPoly = 25600;
        Random r = new Random();
        colorHSB = r.nextDouble();
        drawFunction = false;
        xmin = numericRead(xMinTxt.getText());
        xmax = numericRead(xMaxTxt.getText());
        ymin = numericRead(yMinTxt.getText());
        ymax = numericRead(yMaxTxt.getText());
        step = numericRead(stepTxt.getText());
        exprFunction = new ExpressionFunction(functionString.getText(), vars);
        String[] dummyVar = {"t"};
        exprFunction
                .addFunction("pedro", new PedroNode(dummyVar, exprFunction));
        exprFunction.addFunction("linearSystemError", new LinearSystemError(1,
                -2, 5, -7, 7, -5));
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
            MaxPoly = 50000;
        }

        if (nx * ny > MaxPoly) {
            double aux = (nx * ny) * (step * step);
            maxPolyConstraint(aux);
            step = numericRead(stepTxt.getText());
            nx = Math.abs(xmax - xmin) / (step);
            ny = Math.abs(ymax - ymin) / (step);
        }

        inx = (int) Math.floor(nx);
        iny = (int) Math.floor(ny);
        surface = new TriVector[inx + 1][iny + 1];
        for (int j = 0; j < iny; j++) {
            for (int i = 0; i < inx; i++) {
                if (surface[i][j] == null) {
                    x = xmin + i * step;
                    y = ymin + j * step;
                    Double[] xy = {x, y};
                    z = exprFunction.compute(xy);
                    surface[i][j] = new TriVector(x, y, z);
                    // surface[i][j] = new TriVector(x, y, r.nextDouble());
                }
                if (surface[i + 1][j] == null) {
                    x = xmin + (i + 1) * step;
                    y = ymin + j * step;
                    Double[] xy = {x, y};
                    z = exprFunction.compute(xy);
                    surface[i + 1][j] = new TriVector(x, y, z);
                    // surface[i+1][j] = new TriVector(x, y, r.nextDouble());
                }
                if (surface[i + 1][j + 1] == null) {
                    x = xmin + (i + 1) * step;
                    y = ymin + (j + 1) * step;
                    Double[] xy = {x, y};
                    z = exprFunction.compute(xy);
                    surface[i + 1][j + 1] = new TriVector(x, y, z);
                    // surface[i+1][j+1] = new TriVector(x, y, r.nextDouble());
                }
                if (surface[i][j + 1] == null) {
                    x = xmin + (i) * step;
                    y = ymin + (j + 1) * step;
                    Double[] xy = {x, y};
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

            double x = -1 + 2 * (z - minHeightColor)
                    / (maxHeightColor - minHeightColor);

            if (colorState == 0) {
                /**
                 * linear interpolation between blue color and red
                 */
                colorHSB = blue + (red - blue) * 0.5 * (x + 1);
                e.setColorPoint(Color.getHSBColor((float) colorHSB, 1f, 1f), i);
            } else if (colorState == 1) {
                Random r = new Random();
                e.setColorPoint(
                        Color.getHSBColor((float) colorHSB, r.nextFloat(), 1f),
                        i);
            } else if (colorState == 2) {
                /**
                 * see discrete/finite calculus or Newton series to understand
                 *
                 * magically it is equal to color state 1 so there is no need
                 * for it just here for fun.
                 */
                double d2s = ((red - green) - (green - blue));
                double ds = (green - blue);
                colorHSB = blue + ds * (x + 1) + 0.5 * d2s * (x + 1) * x;
                e.setColorPoint(Color.getHSBColor((float) colorHSB, 1f, 1f), i);
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
        double nextStep = Math.sqrt(delta / 10E3);

        if (!isZBuffer) {
            nextStep = Math.sqrt(delta / 10000);
        }

        stepTxt.setText("" + nextStep);
        JOptionPane.showMessageDialog(null,
                "the X/Y step is too low, pls choose a higher one");

    }

    public double randomPointInInterval(double xmin, double xmax) {
        Random r = new Random();

        return xmin + (xmax - xmin) * r.nextDouble();
    }

    public void initGradientFlow() {

        for (int i = 0; i < numGradPoints; i++) {
            optimalPoints[i] = new TriVector(randomPointInInterval(xmin, xmax),
                    randomPointInInterval(ymin, ymax), 0);
        }
    }

    public double dfdx(double x, double y) {
        Double[] vh, v;
        double h = 1E-09;
        v = new Double[2];
        vh = new Double[2];
        v[0] = x;
        v[1] = y;
        vh[0] = x + h;
        vh[1] = y;
        double df = exprFunction.compute(vh) - exprFunction.compute(v);
        return df / h;
    }

    public double dfdy(double x, double y) {
        Double[] vh, v;
        double h = 1E-09;
        v = new Double[2];
        vh = new Double[2];
        v[0] = x;
        v[1] = y;
        vh[0] = x;
        vh[1] = y + h;
        double df = exprFunction.compute(vh) - exprFunction.compute(v);
        return df / h;
    }

    public void gradientFlow(double dt) {
        double x, y, z;
        Color c;
        boolean logic = false;
        graphics.removeAllElements();

        gradientTime += dt;

        for (int i = 0; i < numGradPoints; i++) {
            x = optimalPoints[i].getX();
            y = optimalPoints[i].getY();
            double dfdx = dfdx(x, y);
            double dfdy = dfdy(x, y);
            if (Math.abs(dfdx) < 1E-5 && Math.abs(dfdy) < 1E-5) {
                logic = true;
                continue;
            }
            x = x + dt * gradientFlow * dfdx;
            y = y + dt * gradientFlow * dfdy;
            Double[] v = new Double[2];
            v[0] = x;
            v[1] = y;
            z = exprFunction.compute(v);
            optimalPoints[i] = new TriVector(x, y, z);
            if (gradientFlow < 0)
                c = Color.red;
            else
                c = Color.blue;

            if (isZBuffer)
                drawSphere(optimalPoints[i], c);
            else {
                Point e = new Point(optimalPoints[i]);
                e.setRadius(10);
                e.setColor(c);
                graphics.addtoList(e);
            }
        }

        /**
         * bad programming
         */
        if (logic || gradientTime >= maxGradientTime) {
            double zMinMaz;
            int indexMinMax = 0;
            if (gradientFlow < 0)
                zMinMaz = Double.MAX_VALUE;
            else
                zMinMaz = Double.MIN_VALUE;

            for (int i = 0; i < numGradPoints; i++) {
                if (gradientFlow < 0) {
                    if (zMinMaz > optimalPoints[i].getZ()) {
                        indexMinMax = i;
                        zMinMaz = optimalPoints[i].getZ();
                    }
                } else {
                    if (zMinMaz < optimalPoints[i].getZ()) {
                        indexMinMax = i;
                        zMinMaz = optimalPoints[i].getZ();
                    }
                }
            }

            if (gradientFlow < 0)
                txtAreaStr += "min	";
            else
                txtAreaStr += "max	";

            txtAreaStr += String.format("%.3g",
                    optimalPoints[indexMinMax].getX())
                    + "\t"
                    + String.format("%.3g", optimalPoints[indexMinMax].getY())
                    + "\t"
                    + String.format("%.3g", optimalPoints[indexMinMax].getZ())
                    + "\n";

            graphics.removeAllElements();
            gradientFlow = 0.0;
            processLayout();
        }

        drawFunction = true;

    }

    public void buildSphere(double radius) {
        double pi = Math.PI;
        double step = pi / 2;
        double n = 2 * pi / step;
        int numIte = (int) Math.floor(n);
        sphere = new TriVector[numIte][numIte];
        for (int j = 0; j < numIte; j++) {
            for (int i = 0; i < numIte; i++) {
                double u = step * i;
                double v = step * j;
                double sinU = Math.sin(u);
                double cosU = Math.cos(u);
                double sinV = Math.sin(v);
                double cosV = Math.cos(v);
                sphere[i][j] = new TriVector(radius * sinU * cosV, radius
                        * sinU * sinV, radius * cosU);
            }
        }
    }

    public void drawSphere(TriVector p, Color c) {
        double pi = Math.PI;
        double step = pi / 2;
        double n = 2 * pi / step;
        int numIte = (int) Math.floor(n);
        for (int j = 0; j < numIte - 1; j++) {
            for (int i = 0; i < numIte - 1; i++) {
                Quad e = new Quad(TriVector.sum(p, sphere[i][j]),
                        TriVector.sum(p, sphere[i + 1][j]), TriVector.sum(p,
                        sphere[i + 1][j + 1]), TriVector.sum(p,
                        sphere[i][j + 1]));
                e.setColorPoint(c, 0);
                e.setColorPoint(c, 1);
                e.setColorPoint(c, 2);
                e.setColorPoint(c, 3);
                graphics.addtoList(e);

            }
        }
    }

    public void paint(Graphics g) {
        if (Math.abs(wChanged - this.getWidth()) > 0
                || Math.abs(hChanged - this.getHeight()) > 0) {
            wChanged = this.getWidth();
            hChanged = this.getHeight();
            processLayout();
        }
        if (panel.getGraphics() != null) {
            panel.update(panel.getGraphics());
        }
        update(g);
    }

    public void update(Graphics g) {
        wd.clearImageWithBackGround();
        euler.run();
        if (drawAxis && colorState == 0)
            infoColor();
        wd.paint(g);
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
        } else if (arg0.getKeyCode() == KeyEvent.VK_Z) {
            isZBuffer = !isZBuffer;
            if (isZBuffer) {
                graphics.setMethod(new ZBufferPrespective());
            } else {
                graphics.setMethod(new WiredPrespective());
            }
            // drawFunction = true;
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
        minMaxTxt.transferFocusUpCycle();
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

    class Euler {

        public void run() {
            currentTime = (System.currentTimeMillis()) * 1E-03;
            double dt = currentTime - oldTime;
            oldTime = currentTime;
            accelaration = -velocity + thrust;
            velocity += accelaration * dt;
            raw += velocity * dt;
            orbit(theta, phi, focalPoint);
            if (gradientFlow != 0) {
                gradientFlow(dt);
            }
            if (drawAxis) {
                buildAxis();
            }
            if (drawFunction) {
                buildFunction();
            }
            if (isZoomFit)
                cameraFindGraph();

            graphics.drawElements();
            repaint();
        }
    }
}
