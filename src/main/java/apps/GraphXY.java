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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class GraphXY extends JFrame implements MouseListener,
        MouseMotionListener, KeyListener, MouseWheelListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final TextFrame HELP_FRAME = TextFrame.builder()
            .addLine("< w > : Camera move foward / zoom in")
            .addLine("< s > : Camera move backward /zoom out")
            .addLine("< z > : Toggle wireframe / zbuffer")
            .addLine("< n > : Toggle flat shading")
            .addLine("< [1-3] > : change color of surface")
            .addLine("< e > : random examples of functions")
            .addLine("< 8 > : interpolative colors")
            .addLine("< 9 > : level set shader")
            .addLine("< Available functions > : sin ,cos, exp, ln, tan, acos, asin, atan, min, adding more")
            .addLine("< operators > : +, - , *, ^")
            .addLine("< Available constants > : pi")
            .addLine("< a > : draw Axis")
            .addLine("< mouse > : rotate camera")
            .addLine("Made by Pedroth")
            .buildWithTitle("Help");
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
    private JTextField xMinTxt, xMaxTxt, yMinTxt, yMaxTxt, samplesTxt;
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
    private int mx;
    private int my;
    private int mRotation;
    /**
     * camera dynamics
     */
    private double raw;
    private double velocity;
    private double acceleration;
    private double oldTime;
    private double thrust;
    /**
     * where the camera is looking
     */
    private TriVector focalPoint;
    /**
     * variable to check the need of drawing
     */
    private boolean drawFunction;
    /**
     * variables describing the domain of the function
     */
    private double xmin, xmax, ymin, ymax, samples;
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

    private Runnable euler = () -> {
        double currentTime = (System.currentTimeMillis()) * 1E-03;
        double dt = currentTime - this.oldTime;
        this.oldTime = currentTime;
        this.acceleration = -velocity + this.thrust;
        this.velocity += this.acceleration * dt;
        this.raw += this.velocity * dt;
        orbit(this.theta, this.phi, this.focalPoint);
        if (this.gradientFlow != 0) gradientFlow(dt);
        if (this.drawAxis) buildAxis();
        if (this.drawFunction) buildFunction();
        if (this.isZoomFit) cameraFindGraph();
        this.graphics.drawElements();
        repaint();
    };

    public GraphXY(boolean isApplet) {
        /*
         * Set JFrame title.
         */
        super("Draw Graph XY - Press h for Help");

        this.setLayout(null);

        /*
         * Begin the engine
         */
        this.graphics = new TriWin();
        this.wd = this.graphics.getBuffer();
        ZBufferPerspective painter = new ZBufferPerspective();
        this.graphics.setMethod(painter);
        this.wd.setBackGroundColor(Color.black);
        this.isZBuffer = true;
        this.colorState = 0;
        this.drawAxis = false;
        /*
         * int global variables
         */
        this.gradientFlow = 0;
        this.optimalPoints = new TriVector[this.numGradPoints];
        buildSphere(0.1);
        this.maxGradientTime = 3.0;


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

        this.wChanged = this.getWidth();
        this.hChanged = this.getHeight();

        this.wd.setWindowSize(7 * this.wChanged / 10, this.hChanged);

        /*
         * UI crap initialization.
         */
        this.panel = new JPanel();
        this.functionString = new JTextField();
        this.xMinTxt = new JTextField();
        this.xMaxTxt = new JTextField();
        this.yMinTxt = new JTextField();
        this.yMaxTxt = new JTextField();
        this.minMaxTxt = new JTextArea();
        this.samplesTxt = new JTextField();
        this.drawButton = new JButton("Draw");
        this.drawButton.addActionListener(e -> {
            this.drawFunction = true;
            this.graphics.removeAllElements();
        });
        this.minButton = new JButton("Min");
        this.minButton.addActionListener(arg0 -> {
            this.gradientFlow = -1.0;
            initGradientFlow();
            this.gradientTime = 0;

        });
        this.maxButton = new JButton("Max");
        this.maxButton.addActionListener(e -> {
            this.gradientFlow = 1.0;
            initGradientFlow();
            this.gradientTime = 0;
        });

        this.clearButton = new JButton("Clear Table");
        this.clearButton.addActionListener(arg0 -> {
            this.txtAreaStr = "\tx \t y \t z\n";
            processLayout();
        });
        this.zoomFit = new JButton("Zoom fit");
        this.zoomFit.addActionListener(arg0 -> this.isZoomFit = true);
        this.isZoomFit = false;
        this.init = true;

        /*
         * init camera values
         */
        this.raw = 3;
        this.velocity = 0;
        this.acceleration = 0;

        this.oldTime = (System.currentTimeMillis()) * 1E-03;
        this.thrust = 0;

        this.drawFunction = true;
        this.focalPoint = new TriVector();
        this.mRotation = 0;

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

        this.isShading = false;
        this.shader = new FlatShader();
        this.shader.setAmbientLightParameter(0.5);
        this.shader.setShininess(25);
        this.shader.addLightPoint(new TriVector(this.raw, this.raw, this.raw));

        this.axisAlreadyBuild = false;
    }

    public static void main(String[] args) {
        new GraphXY(false);
    }

    private void processLayout() {
        int border = 70;

        if (this.init) {
            this.panel.setLayout(new GridLayout(2, 1));
            JPanel upperPanel = new JPanel(new GridLayout(8, 1));
            JPanel functionPanel = new JPanel();
            functionPanel.setLayout(new GridLayout(1, 2));
            functionPanel.add(new JLabel("F(x,y)"));
            functionPanel.add(this.functionString);
            upperPanel.add(functionPanel);
            JPanel intervalPanelX = new JPanel();
            intervalPanelX.setLayout(new GridLayout(1, 2));
            intervalPanelX.add(new JLabel("xmin"));
            intervalPanelX.add(new JLabel("xmax"));
            upperPanel.add(intervalPanelX);
            JPanel intervalPanelXValue = new JPanel();
            intervalPanelXValue.setLayout(new GridLayout(1, 2));
            intervalPanelXValue.add(this.xMinTxt);
            intervalPanelXValue.add(this.xMaxTxt);
            upperPanel.add(intervalPanelXValue);
            JPanel intervalPanelY = new JPanel();
            intervalPanelY.setLayout(new GridLayout(1, 2));
            intervalPanelY.add(new JLabel("ymin"));
            intervalPanelY.add(new JLabel("ymax"));
            upperPanel.add(intervalPanelY);
            JPanel intervalPanelYValue = new JPanel();
            intervalPanelYValue.setLayout(new GridLayout(1, 2));
            intervalPanelYValue.add(this.yMinTxt);
            intervalPanelYValue.add(this.yMaxTxt);
            upperPanel.add(intervalPanelYValue);
            JPanel samplesPanel = new JPanel();
            samplesPanel.setLayout(new GridLayout(1, 2));
            samplesPanel.add(new JLabel("X\\Y Samples"));
            samplesPanel.add(this.samplesTxt);
            upperPanel.add(samplesPanel);
            JPanel drawButtonPanel = new JPanel();
            drawButtonPanel.setLayout(new GridLayout(1, 2));
            drawButtonPanel.add(new JLabel(""));
            drawButtonPanel.add(this.drawButton);
            upperPanel.add(drawButtonPanel);
            JPanel minMaxPanel = new JPanel(new GridLayout(1, 2));
            minMaxPanel.add(this.minButton);
            minMaxPanel.add(this.maxButton);
            upperPanel.add(minMaxPanel);
            this.panel.add(upperPanel);
            JPanel lowerPane = new JPanel(new GridLayout(2, 1));
            JScrollPane areaScrollPane = new JScrollPane(this.minMaxTxt);
            areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            areaScrollPane.setPreferredSize(new Dimension(250, 250));
            lowerPane.add(areaScrollPane);
            JPanel clearZoomPanel = new JPanel(new GridLayout(1, 2));
            clearZoomPanel.add(this.clearButton);
            clearZoomPanel.add(this.zoomFit);
            JPanel dummyPanel = new JPanel(new GridLayout(2, 1));
            dummyPanel.add(clearZoomPanel);
            dummyPanel.add(new JLabel());
            lowerPane.add(dummyPanel);
            this.panel.add(lowerPane);
            this.functionString.setText("sin( x ^ 2 + y ^ 2)");
            this.xMinTxt.setText("-1");
            this.xMaxTxt.setText("1");
            this.yMinTxt.setText("-1");
            this.yMaxTxt.setText("1");
            this.samplesTxt.setText("33");
            this.txtAreaStr = "\tx \t y \t z\n";
            this.add(this.panel);
            this.init = false;
        }
        /*
          pseudo - canvas
         */
        this.wd.setWindowSize(border * this.wChanged / 100, this.hChanged);
        this.panel.setBounds(border * this.wChanged / 100, 0, (100 - border) * this.wChanged / 100, this.hChanged);
        this.minMaxTxt.setText(this.txtAreaStr);
    }

    private void cameraFindGraph() {
        Double v[] = new Double[3];
        v[0] = (this.xmax + this.xmin) / 2;
        v[1] = (this.ymax + this.ymin) / 2;
        v[2] = (this.maxHeightColor + this.minHeightColor) / 2;
        this.raw = 3 * this.xmax;
        this.focalPoint = new TriVector(v[0], v[1], v[2]);
        this.isZoomFit = false;
    }

    /**
     * warning does not accept variables
     *
     * @param s string to be read.
     * @return computation of the expression in s
     */
    private double numericRead(String s) {
        if ("".equals(s))
            return 0;
        ExpressionFunction in;
        in = new ExpressionFunction(s, new String[]{});
        in.init();
        return in.compute(new Double[]{});
    }

    private void buildFunction() {
        double x, y, z, colorHSB;
        int inx, iny;
        int MaxPoly = 25600;
        Random r = new Random();
        colorHSB = r.nextDouble();
        this.drawFunction = false;
        this.xmin = numericRead(this.xMinTxt.getText());
        this.xmax = numericRead(this.xMaxTxt.getText());
        this.ymin = numericRead(this.yMinTxt.getText());
        this.ymax = numericRead(this.yMaxTxt.getText());
        this.samples = numericRead(this.samplesTxt.getText());
        this.exprFunction = new ExpressionFunction(this.functionString.getText(), this.vars);
        String[] dummyVar = {"t"};
        this.exprFunction.addFunction("pedro", new PedroNode(dummyVar, this.exprFunction));
        this.exprFunction.addFunction("linearSystemError", new LinearSystemError(1, -2, 5, -7, 7, -5));
        this.exprFunction.addFunction("C", new CombinationNode());
        this.maxHeightColor = Double.NEGATIVE_INFINITY;
        this.minHeightColor = Double.POSITIVE_INFINITY;
        try {
            this.exprFunction.init();
        } catch (SyntaxErrorException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "there is a syntax error in the formula, pls change the formula."
                            + String.format("%n")
                            + " try to use more brackets, try not to cocatenate 2*x^2 as 2x2."
                            + String.format("%n")
                            + "check also for simple errors like 1/*2."
            );
        }
        final double samplesSq = this.samples * this.samples;
        if (samplesSq > MaxPoly) {
            maxPolyConstraint(samplesSq);
            this.samples = numericRead(this.samplesTxt.getText());
        }

        inx = (int) Math.floor(this.samples);
        iny = (int) Math.floor(this.samples);
        final double stepX = Math.abs(this.xmax - this.xmin) / (this.samples - 1);
        final double stepY = Math.abs(this.ymax - this.ymin) / (this.samples - 1);
        /*
         * surface
         */
        TriVector[][] surface = new TriVector[inx][iny];
        for (int j = 0; j < iny - 1; j++) {
            for (int i = 0; i < inx - 1; i++) {
                final double xbase = this.xmin + i * stepX;
                final double ybase = this.ymin + j * stepY;
                if (surface[i][j] == null) {
                    x = xbase;
                    y = ybase;
                    Double[] xy = {x, y};
                    z = this.exprFunction.compute(xy);
                    surface[i][j] = new TriVector(x, y, z);
                }
                if (surface[i + 1][j] == null) {
                    x = xbase + stepX;
                    y = ybase;
                    Double[] xy = {x, y};
                    z = this.exprFunction.compute(xy);
                    surface[i + 1][j] = new TriVector(x, y, z);
                }
                if (surface[i + 1][j + 1] == null) {
                    x = xbase + stepX;
                    y = ybase + stepY;
                    Double[] xy = {x, y};
                    z = this.exprFunction.compute(xy);
                    surface[i + 1][j + 1] = new TriVector(x, y, z);
                }
                if (surface[i][j + 1] == null) {
                    x = xbase;
                    y = ybase + stepY;
                    Double[] xy = {x, y};
                    z = this.exprFunction.compute(xy);
                    surface[i][j + 1] = new TriVector(x, y, z);
                }
                double auxZ = surface[i][j].getZ();
                if (!Double.isInfinite(auxZ) && !Double.isNaN(auxZ)) {
                    this.maxHeightColor = Math.max(auxZ, this.maxHeightColor);
                    this.minHeightColor = Math.min(auxZ, this.minHeightColor);
                }
            }
        }

        for (int j = 0; j < iny - 1; j++) {
            for (int i = 0; i < inx - 1; i++) {
                Element e = new Quad(surface[i][j], surface[i + 1][j], surface[i + 1][j + 1], surface[i][j + 1]);
                setElementColor(e, colorHSB);
                this.graphics.addtoList(e);
            }
        }
    }

    private void setElementColor(Element e, double colorHSB) {
        double red = 0;
        double blue = 240.0 / 360.0;
        double green = 120.0 / 360.0;
        for (int i = 0; i < e.getNumOfPoints(); i++) {
            double z = e.getNPoint(i).getZ();
            double x = -1 + 2 * (z - this.minHeightColor) / (this.maxHeightColor - this.minHeightColor);
            switch (this.colorState) {
                case 0:
                     /*
                      linear interpolation between blue color and red
                     */
                    colorHSB = blue + (red - blue) * 0.5 * (x + 1);
                    e.setColorPoint(Color.getHSBColor((float) colorHSB, 1f, 1f), i);
                    break;
                case 1:
                    Random r = new Random();
                    e.setColorPoint(Color.getHSBColor((float) colorHSB, r.nextFloat(), 1f), i);
                    break;
                case 2:
                    /*
                      see discrete/finite calculus or Newton series to understand

                      magically it is equal to color state 1 so there is no need
                      for it just here for fun.
                     */
                    double d2s = ((red - green) - (green - blue));
                    double ds = (green - blue);
                    colorHSB = blue + ds * (x + 1) + 0.5 * d2s * (x + 1) * x;
                    e.setColorPoint(Color.getHSBColor((float) colorHSB, 1f, 1f), i);
                    break;
            }
        }
    }

    private void buildAxis() {
        if (!this.axisAlreadyBuild) {
            Element e = new Line(new TriVector(0, 0, 0), new TriVector(1, 0, 0));
            e.setColor(Color.white);
            this.graphics.addtoList(e);
            e = new StringElement(new TriVector(1.1, 0, 0), "X");
            e.setColor(Color.white);
            this.graphics.addtoList(e);
            e = new Line(new TriVector(0, 0, 0), new TriVector(0, 1, 0));
            e.setColor(Color.white);
            this.graphics.addtoList(e);
            e = new StringElement(new TriVector(0, 1.1, 0), "Y");
            e.setColor(Color.white);
            this.graphics.addtoList(e);
            e = new Line(new TriVector(0, 0, 0), new TriVector(0, 0, 1));
            e.setColor(Color.white);
            this.graphics.addtoList(e);
            e = new StringElement(new TriVector(0, 0, 1.1), "Z");
            e.setColor(Color.white);
            this.graphics.addtoList(e);
            this.axisAlreadyBuild = true;
        }
    }

    private void infoColor() {
        double red = 0;
        double blue = 240.0 / 360.0;
        double a, x, colorHSB, divN;
        int n = 10;
        for (int i = 0; i < n + 1; i++) {
            x = -1 + (2.0 / n) * i;
            colorHSB = blue + (red - blue) * 0.5 * (x + 1);
            divN = 1.0 / n;
            this.wd.setDrawColor(Color.getHSBColor((float) colorHSB, 1f, 1f));
            a = -0.5 + (divN) * i;
            this.wd.drawFilledRectangle(a, -0.9, divN, 0.05);
        }
        this.wd.setDrawColor(Color.white);
        this.wd.drawString("" + this.minHeightColor, -0.5, -0.95);
        this.wd.drawString("" + this.maxHeightColor, 0.5, -0.95);

    }

    private void maxPolyConstraint(double delta) {
        double nextSamples = Math.sqrt(delta);
        this.samplesTxt.setText("" + nextSamples);
        JOptionPane.showMessageDialog(null, "the X/Y samples are too high, pls choose a lower one");
    }

    private double randomPointInInterval(double xmin, double xmax) {
        Random r = new Random();
        return xmin + (xmax - xmin) * r.nextDouble();
    }

    private void initGradientFlow() {
        for (int i = 0; i < this.numGradPoints; i++) {
            this.optimalPoints[i] = new TriVector(randomPointInInterval(this.xmin, this.xmax), randomPointInInterval(this.ymin, this.ymax), 0);
        }
    }

    private double dfdx(double x, double y) {
        Double[] vh, v;
        double h = 1E-09;
        v = new Double[2];
        vh = new Double[2];
        v[0] = x;
        v[1] = y;
        vh[0] = x + h;
        vh[1] = y;
        double df = this.exprFunction.compute(vh) - this.exprFunction.compute(v);
        return df / h;
    }

    private double dfdy(double x, double y) {
        Double[] vh, v;
        double h = 1E-09;
        v = new Double[2];
        vh = new Double[2];
        v[0] = x;
        v[1] = y;
        vh[0] = x;
        vh[1] = y + h;
        double df = this.exprFunction.compute(vh) - this.exprFunction.compute(v);
        return df / h;
    }

    private void gradientFlow(double dt) {
        this.graphics.removeAllElements();
        this.gradientTime += dt;

        boolean logic = false;
        for (int i = 0; i < this.numGradPoints; i++) {
            double x = this.optimalPoints[i].getX();
            double y = this.optimalPoints[i].getY();
            double dfdx = dfdx(x, y);
            double dfdy = dfdy(x, y);
            if (Math.abs(dfdx) < 1E-5 && Math.abs(dfdy) < 1E-5) {
                logic = true;
                continue;
            }
            x = x + dt * this.gradientFlow * dfdx;
            y = y + dt * this.gradientFlow * dfdy;
            Double[] v = new Double[2];
            v[0] = x;
            v[1] = y;
            double z = this.exprFunction.compute(v);
            this.optimalPoints[i] = new TriVector(x, y, z);
            Color c = this.gradientFlow < 0 ? Color.red : Color.blue;
            if (this.isZBuffer)
                drawSphere(this.optimalPoints[i], c);
            else {
                Point e = new Point(this.optimalPoints[i]);
                e.setRadius(10);
                e.setColor(c);
                this.graphics.addtoList(e);
            }
        }

        /*
          bad programming
         */
        if (logic || this.gradientTime >= this.maxGradientTime) {
            int indexMinMax = 0;
            double zMinMaz = this.gradientFlow < 0 ? Double.MAX_VALUE : Double.MIN_VALUE;
            for (int i = 0; i < this.numGradPoints; i++) {
                if (this.gradientFlow < 0) {
                    if (zMinMaz > this.optimalPoints[i].getZ()) {
                        indexMinMax = i;
                        zMinMaz = this.optimalPoints[i].getZ();
                    }
                } else {
                    if (zMinMaz < this.optimalPoints[i].getZ()) {
                        indexMinMax = i;
                        zMinMaz = this.optimalPoints[i].getZ();
                    }
                }
            }
            this.txtAreaStr += this.gradientFlow < 0 ? "min	" : "max	";
            this.txtAreaStr += String.format("%.3g", this.optimalPoints[indexMinMax].getX())
                    + "\t"
                    + String.format("%.3g", this.optimalPoints[indexMinMax].getY())
                    + "\t"
                    + String.format("%.3g", this.optimalPoints[indexMinMax].getZ())
                    + "\n";
            this.graphics.removeAllElements();
            this.gradientFlow = 0.0;
            processLayout();
        }

        this.drawFunction = true;

    }

    private void buildSphere(double radius) {
        double pi = Math.PI;
        double step = pi / 2;
        double n = 2 * pi / step;
        int numIte = (int) Math.floor(n);
        this.sphere = new TriVector[numIte][numIte];
        for (int j = 0; j < numIte; j++) {
            for (int i = 0; i < numIte; i++) {
                double u = step * i;
                double v = step * j;
                double sinU = Math.sin(u);
                double cosU = Math.cos(u);
                double sinV = Math.sin(v);
                double cosV = Math.cos(v);
                this.sphere[i][j] = new TriVector(radius * sinU * cosV, radius * sinU * sinV, radius * cosU);
            }
        }
    }

    private void drawSphere(TriVector p, Color c) {
        double pi = Math.PI;
        double step = pi / 2;
        double n = 2 * pi / step;
        int numIte = (int) Math.floor(n);
        for (int j = 0; j < numIte - 1; j++) {
            for (int i = 0; i < numIte - 1; i++) {
                Quad e = new Quad(TriVector.sum(p, this.sphere[i][j]),
                        TriVector.sum(p, this.sphere[i + 1][j]), TriVector.sum(p,
                        this.sphere[i + 1][j + 1]), TriVector.sum(p,
                        this.sphere[i][j + 1]));
                e.setColorPoint(c, 0);
                e.setColorPoint(c, 1);
                e.setColorPoint(c, 2);
                e.setColorPoint(c, 3);
                this.graphics.addtoList(e);
            }
        }
    }

    public void paint(Graphics g) {
        if (Math.abs(this.wChanged - this.getWidth()) > 0
                || Math.abs(this.hChanged - this.getHeight()) > 0) {
            this.wChanged = this.getWidth();
            this.hChanged = this.getHeight();
            processLayout();
        }
        if (this.panel.getGraphics() != null) {
            this.panel.update(this.panel.getGraphics());
        }
        update(g);
    }

    public void update(Graphics g) {
        this.wd.clearImageWithBackGround();
        euler.run();
        if (this.drawAxis && this.colorState == 0)
            infoColor();
        this.wd.paint(g);
    }

    private void orbit(double t, double p, TriVector x) {

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

        TriVector eye = new TriVector(this.raw * cosP * cosT + x.getX(), this.raw * cosP * sinT + x.getY(), this.raw * sinP + x.getZ());

        this.graphics.setCamera(aux, eye);
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        Map<Integer, Runnable> keyActionMap = new HashMap<>();
        keyActionMap.put(KeyEvent.VK_W, () -> this.thrust = -5);
        keyActionMap.put(KeyEvent.VK_S, () -> this.thrust = +5);
        keyActionMap.put(KeyEvent.VK_1, () -> {
            this.colorState = 0;
            this.graphics.removeAllElements();
            this.drawFunction = true;
        });
        keyActionMap.put(KeyEvent.VK_2, () -> {
            this.colorState = 1;
            this.graphics.removeAllElements();
            this.drawFunction = true;
        });
        keyActionMap.put(KeyEvent.VK_3, () -> {
            this.colorState = 2;
            this.graphics.removeAllElements();
            this.drawFunction = true;
        });
        keyActionMap.put(KeyEvent.VK_8, () -> this.graphics.setMethod(new InterpolativeShader()));
        keyActionMap.put(KeyEvent.VK_9, () -> this.graphics.setMethod(new LevelSetShader()));
        keyActionMap.put(KeyEvent.VK_A, () -> {
            this.drawAxis = !this.drawAxis;
            this.axisAlreadyBuild = false;
            this.graphics.removeAllElements();
            this.drawFunction = true;
        });
        keyActionMap.put(KeyEvent.VK_N, () -> {
            this.isShading = !this.isShading;
            if (this.isShading) {
                this.graphics.setMethod(this.shader);
                this.shader.changeNthLight(0, new TriVector(this.raw, this.raw, this.raw));
            } else {
                this.graphics.setMethod(new ZBufferPerspective());
            }
        });
        keyActionMap.put(KeyEvent.VK_E, () -> {
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
            this.functionString.setText(str);
            this.graphics.removeAllElements();
            this.drawFunction = true;
        });
        keyActionMap.put(KeyEvent.VK_Z, () -> {
            this.isZBuffer = !this.isZBuffer;
            if (this.isZBuffer) {
                this.graphics.setMethod(new ZBufferPerspective());
            } else {
                this.graphics.setMethod(new WiredPrespective());
            }
        });
        Optional.ofNullable(keyActionMap.get(arg0.getKeyCode())).ifPresent(Runnable::run);
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        this.thrust = 0;
        if (arg0.getKeyCode() == KeyEvent.VK_H) {
            HELP_FRAME.setVisible(true);
        }
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub
    }

    private void canvasFocus() {
        this.xMinTxt.transferFocusUpCycle();
        this.xMaxTxt.transferFocusUpCycle();
        this.functionString.transferFocusUpCycle();
        this.minMaxTxt.transferFocusUpCycle();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        canvasFocus();
        int newMx = e.getX();
        int newMy = e.getY();
        double dx = newMx - mx;
        double dy = newMy - my;
        this.theta += 2 * Math.PI * (dx / this.wChanged);
        this.phi += 2 * Math.PI * (dy / this.hChanged);

        orbit(this.theta, this.phi, this.focalPoint);
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
        this.mRotation = e.getWheelRotation();
        this.xMinTxt.setText("" + (this.xmin - this.mRotation));
        this.xMaxTxt.setText("" + (this.xmax + this.mRotation));
        this.yMinTxt.setText("" + (this.ymin - this.mRotation));
        this.yMaxTxt.setText("" + (this.ymax + this.mRotation));
        this.graphics.removeAllElements();
        this.drawFunction = true;
    }
}
