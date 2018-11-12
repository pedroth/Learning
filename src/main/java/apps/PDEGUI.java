package apps;

import algebra.Matrix;
import algebra.TriVector;
import functionNode.CombinationNode;
import functionNode.FunctionNode;
import functions.ExpressionFunction;
import functions.SyntaxErrorException;
import inputOutput.MyImage;
import visualization.TextFrame;
import window.ImageWindow;
import windowThreeDim.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class PDEGUI extends JFrame implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
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
            .addLine("< operators > : +, - , *, ^ ")
            .addLine("< Available constants > : pi")
            .addLine("< a > : draw Axis")
            .addLine("< mouse > : rotate camera")
            .addLine("< k > : toogle kakashi mode")
            .addLine("< Available function on animation part > :  dfx(x,y) (df / dx), dfy(x,y) (df / dy) , dt(x,y) (df / dt), d2fx(x,y)(d2f / dx2)")
            .addLine("d2fy(x,y) (d2f / dy2), f(x,y), d2fxy(x,y)")
            .addLine("Made by Pedroth")
            .buildWithTitle("Help");

    private JButton drawButton;
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
    private JTextField xMinTxt, xMaxTxt, yMinTxt, yMaxTxt, samplesText;
    private JComboBox<String> comboBox;
    private JTextField functionVel;
    private JTextField functionAcc;
    private JButton animate;
    private JButton zoomFit;
    private JPanel panel;
    private JLabel accVelTextLabel;
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
    private double currentTime;
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
    private double xmin, xmax, ymin, ymax, samples;
    private String[] vars = {"x", "y"};
    private String[] diffVars = {"x", "y", "t"};
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
    private boolean axisAlreadyBuild;

    private Runnable euler = () -> {
        this.currentTime = (System.currentTimeMillis()) * 1E-03;
        double dt = this.currentTime - this.oldTime;
        this.time += dt;
        this.oldTime = this.currentTime;
        this.acceleration = -this.velocity + this.thrust;
        this.velocity += this.acceleration * dt;
        this.raw += this.velocity * dt;
        orbit(this.theta, this.phi, this.focalPoint);
        if (this.drawAxis) {
            buildAxis();
        }
        if (this.drawFunction) {
            if (!this.isKakashi)
                buildFunction();
            else {
                buildKakashi();
            }
        }
        if (this.isZoomFit) {
            cameraFindGraph();
        }
        if (this.isAnimating) {
            animate(dt);
        }
        this.graphics.drawElements();
        repaint();
    };

    public PDEGUI(boolean isApplet) {
        /*
         * Set JFrame title.
         */
        super("PDE - Press h for Help");

        this.setLayout(null);

        /*
         * Begin Engine.
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
        this.samplesText = new JTextField();
        this.functionAcc = new JTextField();
        this.functionVel = new JTextField();
        this.comboBox = new JComboBox(new String[]{"Acceleration", "Velocity"});
        this.accVelTextLabel = new JLabel();
        this.comboBox.addItemListener(e -> processLayout());
        this.drawButton = new JButton("Draw");
        this.drawButton.addActionListener(e -> {
            this.drawFunction = true;
            this.graphics.removeAllElements();
        });
        this.animate = new JButton("Animate");
        this.animate.addActionListener(e -> {
            if ("Animate".equals(this.animate.getText())) {
                this.drawFunction = true;
                this.graphics.removeAllElements();
                this.isAnimating = true;
                this.animate.setText("Stop Animation");
            } else {
                this.isAnimating = false;
                this.animate.setText("Animate");
                this.time = 0;
            }
        });
        this.zoomFit = new JButton("Zoom fit");
        this.zoomFit.addActionListener(e -> this.isZoomFit = true);
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

        this.isShading = false;
        this.shader = new FlatShader();
        this.shader.setAmbientLightParameter(0.5);
        this.shader.setShininess(25);
        this.shader.addLightPoint(new TriVector(this.raw, this.raw, this.raw));

        this.isAnimating = false;
        this.time = 0;
        this.isfastShading = false;
        this.isKakashi = false;
        this.axisAlreadyBuild = false;
    }

    public static void main(String[] args) {
        new PDEGUI(false);
    }

    public static int positiveMod(int x, int y) {
        if (x < 0) {
            return y + (x % y);
        } else {
            return x % y;
        }
    }

    private void processLayout() {
        int border = 70;
        if (this.init) {
            this.panel.setLayout(new GridLayout(20, 1));
            JPanel functionPanel = new JPanel();
            functionPanel.setLayout(new GridLayout(1, 2));
            functionPanel.add(new JLabel("F(x,y)"));
            functionPanel.add(this.functionString);
            this.panel.add(functionPanel);
            JPanel intervalPanelX = new JPanel();
            intervalPanelX.setLayout(new GridLayout(1, 2));
            intervalPanelX.add(new JLabel("xmin"));
            intervalPanelX.add(new JLabel("xmax"));
            this.panel.add(intervalPanelX);
            JPanel intervalPanelXValue = new JPanel();
            intervalPanelXValue.setLayout(new GridLayout(1, 2));
            intervalPanelXValue.add(this.xMinTxt);
            intervalPanelXValue.add(this.xMaxTxt);
            this.panel.add(intervalPanelXValue);
            JPanel intervalPanelY = new JPanel();
            intervalPanelY.setLayout(new GridLayout(1, 2));
            intervalPanelY.add(new JLabel("ymin"));
            intervalPanelY.add(new JLabel("ymax"));
            this.panel.add(intervalPanelY);
            JPanel intervalPanelYValue = new JPanel();
            intervalPanelYValue.setLayout(new GridLayout(1, 2));
            intervalPanelYValue.add(this.yMinTxt);
            intervalPanelYValue.add(this.yMaxTxt);
            this.panel.add(intervalPanelYValue);
            JPanel samplesPanel = new JPanel();
            samplesPanel.setLayout(new GridLayout(1, 2));
            samplesPanel.add(new JLabel("X\\Y samplesText"));
            samplesPanel.add(this.samplesText);
            this.panel.add(samplesPanel);
            JPanel drawButtonPanel = new JPanel();
            drawButtonPanel.setLayout(new GridLayout(1, 2));
            drawButtonPanel.add(new JLabel(""));
            drawButtonPanel.add(this.drawButton);
            this.panel.add(drawButtonPanel);
            JPanel comboBoxPanel = new JPanel();
            comboBoxPanel.setLayout(new GridLayout(1, 1));
            comboBoxPanel.add(this.comboBox);
            this.panel.add(comboBoxPanel);
            JPanel paux = new JPanel();
            this.panel.add(paux.add(this.accVelTextLabel));
            this.panel.add(this.functionAcc);
            this.panel.add(this.functionVel);
            JPanel animationButtonPanel = new JPanel();
            animationButtonPanel.setLayout(new GridLayout(1, 2));
            animationButtonPanel.add(this.animate);
            animationButtonPanel.add(this.zoomFit);
            this.panel.add(animationButtonPanel);
            this.functionString.setText("sin( x ^ 2 + y ^ 2)");
            this.functionAcc.setText("d2x(x,y) + d2y(x,y) - 0.5 * dt(x,y)");
            this.functionVel.setText("0.1*(d2x(x,y) + d2y(x,y))");
            this.xMinTxt.setText("-1");
            this.xMaxTxt.setText("1");
            this.yMinTxt.setText("-1");
            this.yMaxTxt.setText("1");
            this.samplesText.setText("33");
            this.add(this.panel);
            this.init = false;
        }
        /*
         * pseudo - canvas
         */
        this.wd.setWindowSize(border * this.wChanged / 100, this.hChanged);
        this.panel.setBounds(border * this.wChanged / 100, 0, (100 - border) * this.wChanged / 100, this.hChanged);

        /*
         * Acceleration/Velocity
         */
        if (this.comboBox.getSelectedItem() == "Acceleration") {
            this.functionVel.setVisible(false);
            this.functionAcc.setVisible(true);
        } else {
            this.functionVel.setVisible(true);
            this.functionAcc.setVisible(false);
        }
        /*
         * PDE text
         */
        if (this.comboBox.getSelectedItem() == "Acceleration") {
            this.accVelTextLabel.setText("d2F/d2t = G(f(x,y),dx(x,y),dy(x,y),d2x(x,y),d2y(x,y),d2xy(x,y),dt(x,y),t,x,y)");
        } else {
            this.accVelTextLabel.setText("dF/dt = G(f(x,y),dx(x,y),dy(x,y),d2x(x,y),d2y(x,y),d2xy(x,y),t,x,y)");
        }

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
        ExpressionFunction in;
        in = new ExpressionFunction(s, new String[]{});
        in.init();
        return in.compute(new Double[]{});
    }

    private double gauss(int x, int y, int window) {
        double u = (double) x;
        double v = (double) y;
        double w = window / 2.0;
        return 1 / (4 * w * w) * Math.exp(-0.25 * ((u - w) * (u - w) + (v - w) * (v - w)));
    }

    private void gaussianFilterToSurface(TriVector[][] s, int window) {
        this.maxHeightColor = Double.MIN_VALUE;
        this.minHeightColor = Double.MAX_VALUE;
        double aux = 0;
        for (int j = 0; j < s[0].length; j++) {
            for (int i = 0; i < s.length; i++) {
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
                this.surface[i][j] = new TriVector(s[x][y].getX(), s[x][y].getY(), aux);
                double auxZ = this.surface[i][j].getZ();
                if (!Double.isInfinite(auxZ) && !Double.isNaN(auxZ)) {
                    this.maxHeightColor = Math.max(auxZ, this.maxHeightColor);
                    this.minHeightColor = Math.min(auxZ, this.minHeightColor);
                }
                aux = 0.0;
            }
        }
    }

    private void buildKakashi() {
        MyImage kakashi = new MyImage("https://92c3cb5a-a-62cb3a1a-s-sites.googlegroups.com/site/ibplanalto2010/Home/kakashi46-3459488_50_50%5B1%5D.jpg?attachauth=ANoY7cp6kFZ2u7lOyL3KJqDYkzI_jmNGeoLsCE29u25IlE23i8Bgqx-4UsNUTkE4Mh7vBQpKPe107E_-PLAOywT34dv8cW9_r9WV0uOZ8p26uBT4rusztcGEh9wkuZ2QI0f-loBiB4pmzo_3NKMrC0CPbRvHHiwa_vT2wVEjZiWh7fZ9XlUjC6vrCVvNOtnmgsnSd-WjjbZqO-q6jSPBFw1zyyaa8uzcAKExLodMjCR40cjjmDComqp1JMNpKJoE1iTDgXQDWFzU&attredirects=0");
        Matrix v = new Matrix(kakashi.getGrayScale());
        TriVector[][] s = v.matrixToSurface(-1, 1, -1, 1);
        this.xmin = -1;
        this.xmax = 1;
        this.ymin = -1;
        this.ymax = 1;
        this.samples = s.length;
        this.surface = new TriVector[s.length][s[0].length];
        this.dudt = new double[s.length][s[0].length];

        gaussianFilterToSurface(s, 3);

        for (int j = 0; j < s[0].length - 1; j++) {
            for (int i = 0; i < s.length - 1; i++) {
                Element e = new Quad(this.surface[i][j], this.surface[i + 1][j], this.surface[i + 1][j + 1], this.surface[i][j + 1]);
                setElementColor(e);
                this.graphics.addtoList(e);
            }
        }
        this.drawFunction = false;
    }

    private void buildFunction() {
        int maxPoly = 5000;
        this.drawFunction = false;
        this.xmin = numericRead(this.xMinTxt.getText());
        this.xmax = numericRead(this.xMaxTxt.getText());
        this.ymin = numericRead(this.yMinTxt.getText());
        this.ymax = numericRead(this.yMaxTxt.getText());
        this.samples = numericRead(this.samplesText.getText());
        /*
         * computes function from string
         */
        ExpressionFunction exprFunction = new ExpressionFunction(this.functionString.getText(), vars);
        exprFunction.addFunction("C", new CombinationNode());
        this.maxHeightColor = Double.NEGATIVE_INFINITY;
        this.minHeightColor = Double.POSITIVE_INFINITY;
        try {
            exprFunction.init();
        } catch (SyntaxErrorException e) {
            JOptionPane.showMessageDialog(null, "there is a syntax error in the formula, pls change the formula." + String.format("%n") + " try to use more brackets, try not to concatenate 2*x^2 as 2x2." + String.format("%n") + "check also for simple errors like 1/*2.");
        }

        final double samplesSquare = this.samples * this.samples;
        if (samplesSquare > maxPoly) {
            maxPolyConstraint(maxPoly);
            this.samples = Double.parseDouble(this.samplesText.getText());
        }
        final int inx = (int) Math.floor(this.samples);
        final int iny = (int) Math.floor(this.samples);
        this.surface = new TriVector[inx][iny];
        this.dudt = new double[inx][iny];

        final double stepX = Math.abs(this.xmax - this.xmin) / (this.samples - 1);
        final double stepY = Math.abs(this.ymax - this.ymin) / (this.samples - 1);

        for (int j = 0; j < iny - 1; j++) {
            for (int i = 0; i < inx - 1; i++) {
                final double xbase = this.xmin + i * stepX;
                final double ybase = this.ymin + j * stepY;
                double x;
                double y;
                double z;
                if (this.surface[i][j] == null) {
                    x = xbase;
                    y = ybase;
                    Double[] xy = {x, y};
                    z = exprFunction.compute(xy);
                    this.surface[i][j] = new TriVector(x, y, z);
                }
                if (this.surface[i + 1][j] == null) {
                    x = xbase + stepX;
                    y = ybase;
                    Double[] xy = {x, y};
                    z = exprFunction.compute(xy);
                    this.surface[i + 1][j] = new TriVector(x, y, z);
                }
                if (this.surface[i + 1][j + 1] == null) {
                    x = xbase + stepX;
                    y = ybase + stepY;
                    Double[] xy = {x, y};
                    z = exprFunction.compute(xy);
                    this.surface[i + 1][j + 1] = new TriVector(x, y, z);
                }
                if (this.surface[i][j + 1] == null) {
                    x = xbase;
                    y = ybase + stepY;
                    Double[] xy = {x, y};
                    z = exprFunction.compute(xy);
                    this.surface[i][j + 1] = new TriVector(x, y, z);
                }
                double auxZ = this.surface[i][j].getZ();
                if (!Double.isInfinite(auxZ) && !Double.isNaN(auxZ)) {
                    this.maxHeightColor = Math.max(auxZ, this.maxHeightColor);
                    this.minHeightColor = Math.min(auxZ, this.minHeightColor);
                }
            }
        }

        for (int j = 0; j < iny - 1; j++) {
            for (int i = 0; i < inx - 1; i++) {
                Element e = new Quad(this.surface[i][j], this.surface[i + 1][j], this.surface[i + 1][j + 1], this.surface[i][j + 1]);
                setElementColor(e);
                this.graphics.addtoList(e);
            }
        }
    }

    private void setElementColor(Element e) {
        double red = 0;
        double blue = 240.0 / 360.0;
        for (int i = 0; i < e.getNumOfPoints(); i++) {
            double z = e.getNPoint(i).getZ();
            double x = -1 + 2 * (z - this.minHeightColor) / (this.maxHeightColor - this.minHeightColor);
            double p = (z - this.minHeightColor) / (this.maxHeightColor - this.minHeightColor);
            switch (this.colorState) {
                case 0:
                    /*
                     * linear interpolation between blue color and red
                     */
                    double colorHSB = blue + (red - blue) * 0.5 * (x + 1);
                    e.setColorPoint(Color.getHSBColor((float) colorHSB, 1f, 1f), i);
                    break;
                case 1:
                    float cRed = (float) Math.min(Math.max(10.0 / 4.0 * p, 0), 1);
                    float cGreen = (float) Math.min(Math.max(10.0 / 4.0 * (p - 0.4), 0), 1);
                    float cBlue = (float) Math.min(Math.max(5 * (p - 0.8), 0), 1);
                    e.setColorPoint(new Color(cRed, cGreen, cBlue), i);
                    break;
                default:
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
            a = -0.5 + divN * i;
            this.wd.drawFilledRectangle(a, -0.9, divN, 0.05);
        }
        this.wd.setDrawColor(Color.white);
        this.wd.drawString("" + this.minHeightColor, -0.5, -0.95);
        this.wd.drawString("" + this.maxHeightColor, 0.5, -0.95);
    }

    private void maxPolyConstraint(double delta) {
        double nextStep = Math.floor(Math.sqrt(delta) / 2);
        this.samplesText.setText("" + nextStep);
        JOptionPane.showMessageDialog(null, "the X/Y samples are too high, pls choose a smaller one");
    }

    private void animate(double dt) {
        this.graphics.removeAllElements();
        if (this.comboBox.getSelectedItem() == "Acceleration") {
            accAnimate(dt);
        } else {
            velAnimate(dt);
        }
    }

    private void accAnimate(double dt) {
        if (dt > 0.02) {
            dt = this.isKakashi ? 0.0001 : 0.01;
        }
        ExpressionFunction accEquation = new ExpressionFunction(this.functionAcc.getText(), this.diffVars);
        accEquation.addFunction("dx", new Dfdx());
        accEquation.addFunction("dy", new Dfdy());
        accEquation.addFunction("d2x", new D2fdx());
        accEquation.addFunction("d2y", new D2fdy());
        accEquation.addFunction("d2xy", new D2fdxdy());
        accEquation.addFunction("f", new Fxy());
        accEquation.addFunction("dt", new Dfdt());
        accEquation.addFunction("C", new CombinationNode());
        this.maxHeightColor = Double.NEGATIVE_INFINITY;
        this.minHeightColor = Double.POSITIVE_INFINITY;
        try {
            accEquation.init();
        } catch (SyntaxErrorException e) {
            JOptionPane.showMessageDialog(null, "there is a syntax error in the formula, pls change the formula." + String.format("%n") + " try to use more brackets, try not to cocatenate 2*x^2 as 2x2." + String.format("%n") + "check also for simple errors like 1/*2.");
        }
        int inx = (int) Math.floor(this.samples);
        int iny = (int) Math.floor(this.samples);
        double[][] aux = new double[inx][inx];

        final double stepX = Math.abs(this.xmax - this.xmin) / (this.samples - 1);
        final double stepY = Math.abs(this.ymax - this.ymin) / (this.samples - 1);

        for (int j = 0; j < iny; j++) {
            for (int i = 0; i < inx; i++) {
                Double[] x = new Double[3];
                x[0] = this.xmin + i * stepX;
                x[1] = this.ymin + j * stepY;
                x[2] = this.time;
                double acceleration = accEquation.compute(x);
                this.dudt[i][j] = this.dudt[i][j] + acceleration * dt;
                double auxZ = this.surface[i][j].getZ() + this.dudt[i][j] * dt + 0.5 * acceleration * dt * dt;
                aux[i][j] = auxZ;
                if (!Double.isInfinite(auxZ) && !Double.isNaN(auxZ)) {
                    this.maxHeightColor = Math.max(auxZ, this.maxHeightColor);
                    this.minHeightColor = Math.min(auxZ, this.minHeightColor);
                }
            }
        }

        for (int j = 0; j < iny - 1; j++) {
            for (int i = 0; i < inx - 1; i++) {
                this.surface[i][j].setZ(aux[i][j]);
                this.surface[i + 1][j].setZ(aux[i + 1][j]);
                this.surface[i + 1][j + 1].setZ(aux[i + 1][j + 1]);
                this.surface[i][j + 1].setZ(aux[i][j + 1]);
                Element e = new Quad(this.surface[i][j], this.surface[i + 1][j], this.surface[i + 1][j + 1], this.surface[i][j + 1]);
                setElementColor(e);
                this.graphics.addtoList(e);
            }
        }
    }

    private void velAnimate(double dt) {
        if (dt > 0.02) {
            dt = this.isKakashi ? 0.0001 : 0.01;
        }
        /*
         * PDE functions
         */
        ExpressionFunction velEquation = new ExpressionFunction(this.functionVel.getText(), this.diffVars);
        velEquation.addFunction("dx", new Dfdx());
        velEquation.addFunction("dy", new Dfdy());
        velEquation.addFunction("d2x", new D2fdx());
        velEquation.addFunction("d2y", new D2fdy());
        velEquation.addFunction("f", new Fxy());
        velEquation.addFunction("d2xy", new D2fdxdy());
        velEquation.addFunction("C", new CombinationNode());
        this.maxHeightColor = Double.NEGATIVE_INFINITY;
        this.minHeightColor = Double.POSITIVE_INFINITY;
        try {
            velEquation.init();
        } catch (SyntaxErrorException e) {
            JOptionPane.showMessageDialog(null, "there is a syntax error in the formula, pls change the formula." + String.format("%n") + " try to use more brackets, try not to concatenate 2*x^2 as 2x2." + String.format("%n") + "check also for simple errors like 1/*2.");
        }

        int inx = (int) Math.floor(this.samples);
        int iny = (int) Math.floor(this.samples);
        double[][] aux = new double[inx][inx];

        final double stepX = Math.abs(this.xmax - this.xmin) / (this.samples - 1);
        final double stepY = Math.abs(this.ymax - this.ymin) / (this.samples - 1);

        for (int j = 0; j < iny; j++) {
            for (int i = 0; i < inx; i++) {
                double auxZ;
                Double[] x = new Double[3];
                x[0] = this.xmin + i * stepX;
                x[1] = this.ymin + j * stepY;
                x[2] = this.time;
                auxZ = this.surface[i][j].getZ() + velEquation.compute(x) * dt;
                aux[i][j] = (auxZ);
                if (!Double.isInfinite(auxZ) && !Double.isNaN(auxZ)) {
                    this.maxHeightColor = Math.max(auxZ, this.maxHeightColor);
                    this.minHeightColor = Math.min(auxZ, this.minHeightColor);
                }
            }
        }

        for (int j = 0; j < iny - 1; j++) {
            for (int i = 0; i < inx - 1; i++) {
                this.surface[i][j].setZ(aux[i][j]);
                this.surface[i + 1][j].setZ(aux[i + 1][j]);
                this.surface[i + 1][j + 1].setZ(aux[i + 1][j + 1]);
                this.surface[i][j + 1].setZ(aux[i][j + 1]);
                Element e = new Quad(this.surface[i][j], this.surface[i + 1][j], this.surface[i + 1][j + 1], this.surface[i][j + 1]);
                setElementColor(e);
                this.graphics.addtoList(e);
            }
        }
    }

    public void paint(Graphics g) {
        if (Math.abs(this.wChanged - this.getWidth()) > 0 || Math.abs(this.hChanged - this.getHeight()) > 0) {
            this.wChanged = this.getWidth();
            this.hChanged = this.getHeight();
            processLayout();
        }
        this.panel.update(this.panel.getGraphics());
        update(g);
    }

    public void update(Graphics g) {
        this.wd.clearImageWithBackGround();
        this.euler.run();
        if (this.drawAxis && colorState == 0)
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
        keyActionMap.put(KeyEvent.VK_S, () -> this.thrust = 5);
        keyActionMap.put(KeyEvent.VK_F, () -> {
            this.isfastShading = !this.isfastShading;
            if (this.isfastShading) {
                this.graphics.setMethod(new SamplingZbuffer(3));
            } else {
                this.graphics.setMethod(new ZBufferPerspective());
            }
            this.drawFunction = true;
            this.graphics.removeAllElements();
        });
        keyActionMap.put(KeyEvent.VK_Z, () -> {
            this.isZBuffer = !this.isZBuffer;
            if (this.isZBuffer) {
                this.graphics.setMethod(new ZBufferPerspective());
            } else {
                this.graphics.setMethod(new WiredPrespective());
            }
            this.drawFunction = true;
            this.graphics.removeAllElements();
        });
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
                this.graphics.removeAllElements();
                this.drawFunction = true;
            } else {
                this.graphics.setMethod(new ZBufferPerspective());
            }
        });
        keyActionMap.put(KeyEvent.VK_E, () -> {
            Random r = new Random();
            String str = "1 / (1 + 3*exp(-(2 * x - y)))";
            switch (r.nextInt(7)) {
                case 0:
                    str = "1 / (1 + 4 * (x ^ 4 +  y ^ 4))";
                    break;
                case 1:
                    str = "exp(-((x - 1) ^ 2 + y ^ 2)) + 2 * exp(-(x ^ 2 + (y + 1) ^ 2))";
                    break;
                case 2:
                    str = "1 / (1 + 3 * exp(-3 * (2 * x - y)))";
                    break;
                case 3:
                    str = "ln((x * x + y * y + 1))";
                    break;
                case 4:
                    str = "(sin(x) * cos(y) - 0.5) ^ 2 + (sin(x) * sin(y) - 0.1) ^ 2";
                    break;
                case 5:
                    str = "sin(x * y) - cos(x * y) ";
                    break;
                case 6:
                    str = "25 / (1 + exp(1 / (1 + exp(-x - (y - 1))) + 1 / (1 + exp(x)) + 1/(1 + exp(y)))) - 5";
                    break;
            }
            this.functionString.setText(str);
            this.graphics.removeAllElements();
            this.drawFunction = true;
        });
        keyActionMap.put(KeyEvent.VK_K, () -> {
            isKakashi = !isKakashi;
            graphics.removeAllElements();
            drawFunction = true;
        });
        keyActionMap.put(KeyEvent.VK_8, () -> {
            graphics.setMethod(new InterpolativeShader());
        });
        keyActionMap.put(KeyEvent.VK_9, () -> {
            graphics.setMethod(new LevelSetShader());
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

    public void canvasFocus() {
        this.xMinTxt.transferFocusUpCycle();
        this.xMaxTxt.transferFocusUpCycle();
        this.functionString.transferFocusUpCycle();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        canvasFocus();
        int newMx = e.getX();
        int newMy = e.getY();
        double dx = newMx - this.mx;
        double dy = newMy - this.my;
        this.theta += 2 * Math.PI * (dx / this.wChanged);
        this.phi += 2 * Math.PI * (dy / this.hChanged);
        orbit(this.theta, this.phi, this.focalPoint);
        this.mx = newMx;
        this.my = newMy;
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
        this.mx = e.getX();
        this.my = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mRotation = e.getWheelRotation();
        this.xMinTxt.setText("" + (this.xmin - mRotation));
        this.xMaxTxt.setText("" + (this.xmax + mRotation));
        this.yMinTxt.setText("" + (this.ymin - mRotation));
        this.yMaxTxt.setText("" + (this.ymax + mRotation));
        this.graphics.removeAllElements();
        this.drawFunction = true;
    }

    public class Fxy extends FunctionNode {

        Fxy() {
            super();
            setnVars(2);
        }

        Fxy(FunctionNode[] args) {
            super(2, args);
        }

        @Override
        public FunctionNode createNode(FunctionNode[] args) {
            return new Fxy(args);
        }

        @Override
        public Double compute(Double[] variables) {
            int i = (int) ((samples - 1) * ((variables[0] - xmin) / (xmax - xmin)));
            int j = (int) ((samples - 1) * ((variables[1] - ymin) / (ymax - ymin)));
            return surface[i][j].getZ();
        }
    }

    public class Dfdx extends FunctionNode {

        Dfdx() {
            super();
            setnVars(2);
        }

        Dfdx(FunctionNode[] args) {
            super(2, args);
        }

        @Override
        public FunctionNode createNode(FunctionNode[] args) {
            return new Dfdx(args);
        }

        @Override
        public Double compute(Double[] variables) {
            int i = (int) ((samples - 1) * ((variables[0] - xmin) / (xmax - xmin)));
            int j = (int) ((samples - 1) * ((variables[1] - ymin) / (ymax - ymin)));
            final double stepX = Math.abs(xmax - xmin) / (samples - 1);
            if (i + 1 > surface.length - 1)
                return (surface[i - 1][j].getZ() - surface[i][j].getZ()) / stepX;
            if (i - 1 < 0)
                return (surface[i + 1][j].getZ() - surface[i][j].getZ()) / stepX;
            return (surface[i + 1][j].getZ() - surface[i][j].getZ()) / stepX;
        }
    }

    public class Dfdy extends FunctionNode {

        Dfdy() {
            super();
            setnVars(2);
        }

        Dfdy(FunctionNode[] args) {
            super(2, args);
        }

        @Override
        public FunctionNode createNode(FunctionNode[] args) {
            return new Dfdy(args);
        }

        @Override
        public Double compute(Double[] variables) {
            int i = (int) ((samples - 1) * ((variables[0] - xmin) / (xmax - xmin)));
            int j = (int) ((samples - 1) * ((variables[1] - ymin) / (ymax - ymin)));
            final double stepY = Math.abs(ymax - ymin) / (samples - 1);
            if (j + 1 > surface[0].length - 1)
                return (surface[i][j - 1].getZ() - surface[i][j].getZ()) / stepY;
            if (j - 1 < 0)
                return (surface[i][j + 1].getZ() - surface[i][j].getZ()) / stepY;
            return (surface[i][j + 1].getZ() - surface[i][j].getZ()) / stepY;
        }
    }

    public class D2fdx extends FunctionNode {

        D2fdx() {
            super();
            setnVars(2);
        }

        D2fdx(FunctionNode[] args) {
            super(2, args);
        }

        @Override
        public FunctionNode createNode(FunctionNode[] args) {
            return new D2fdx(args);
        }

        @Override
        public Double compute(Double[] variables) {
            int i = (int) ((samples - 1) * ((variables[0] - xmin) / (xmax - xmin)));
            int j = (int) ((samples - 1) * ((variables[1] - ymin) / (ymax - ymin)));
            final double stepX = Math.abs(xmax - xmin) / (samples - 1);
            if (i + 1 > surface.length - 1)
                return (surface[i - 1][j].getZ() - surface[i][j].getZ()) / stepX;
            if (i - 1 < 0)
                return (surface[i + 1][j].getZ() - surface[i][j].getZ()) / stepX;
            return (surface[i + 1][j].getZ() - 2 * surface[i][j].getZ() + surface[i - 1][j].getZ()) / (stepX * stepX);
        }
    }

    public class D2fdy extends FunctionNode {

        D2fdy() {
            super();
            setnVars(2);
        }

        D2fdy(FunctionNode[] args) {
            super(2, args);
        }

        @Override
        public FunctionNode createNode(FunctionNode[] args) {
            return new D2fdy(args);
        }

        @Override
        public Double compute(Double[] variables) {
            int i = (int) ((samples - 1) * ((variables[0] - xmin) / (xmax - xmin)));
            int j = (int) ((samples - 1) * ((variables[1] - ymin) / (ymax - ymin)));
            final double stepY = Math.abs(ymax - ymin) / (samples - 1);
            if (j + 1 > surface[0].length - 1)
                return (surface[i][j - 1].getZ() - surface[i][j].getZ()) / stepY;
            if (j - 1 < 0)
                return (surface[i][j + 1].getZ() - surface[i][j].getZ()) / stepY;
            return (surface[i][j + 1].getZ() - 2 * surface[i][j].getZ() + surface[i][j - 1].getZ()) / (stepY * stepY);
        }
    }

    public class D2fdxdy extends FunctionNode {

        D2fdxdy() {
            super();
            setnVars(2);
        }

        D2fdxdy(FunctionNode[] args) {
            super(2, args);
        }

        @Override
        public FunctionNode createNode(FunctionNode[] args) {
            return new D2fdxdy(args);
        }

        @Override
        public Double compute(Double[] variables) {
            int i = (int) ((samples - 1) * ((variables[0] - xmin) / (xmax - xmin)));
            int j = (int) ((samples - 1) * ((variables[1] - ymin) / (ymax - ymin)));
            final double stepX = Math.abs(xmax - xmin) / (samples - 1);
            final double stepY = Math.abs(ymax - ymin) / (samples - 1);

            if (i + 1 > surface.length - 1) return (surface[i - 1][j].getZ() - surface[i][j].getZ()) / stepX;
            if (i - 1 < 0) return (surface[i + 1][j].getZ() - surface[i][j].getZ()) / stepX;
            if (j + 1 > surface[0].length - 1) return (surface[i][j - 1].getZ() - surface[i][j].getZ()) / stepY;
            if (j - 1 < 0) return (surface[i][j + 1].getZ() - surface[i][j].getZ()) / stepY;
            return (surface[i + 1][j + 1].getZ() - surface[i + 1][j - 1].getZ() - surface[i - 1][j + 1].getZ() + surface[i - 1][j - 1].getZ()) / (4 * stepX * stepY);
        }
    }

    public class Dfdt extends FunctionNode {

        Dfdt() {
            super();
            setnVars(2);
        }

        Dfdt(FunctionNode[] args) {
            super(2, args);
        }

        @Override
        public FunctionNode createNode(FunctionNode[] args) {
            return new Dfdt(args);
        }

        @Override
        public Double compute(Double[] variables) {
            int i = (int) ((samples - 1) * ((variables[0] - xmin) / (xmax - xmin)));
            int j = (int) ((samples - 1) * ((variables[1] - ymin) / (ymax - ymin)));
            return dudt[i][j];
        }
    }
}

