package apps;

import algebra.Matrix;
import algebra.TriVector;
import functions.ExpressionFunction;
import functions.SyntaxErrorException;
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

public class LinesSurfaces extends JFrame implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
    private static final TextFrame HELP_FRAME = TextFrame.builder()
            .addLine("< w > : Camera move foward / zoom in")
            .addLine("< s > : Camera move backward /zoom out")
            .addLine("< z > : Toggle wireframe / zbuffer")
            .addLine("< n > : Toggle flat shading")
            .addLine("< [1-3] > : change color of surface")
            .addLine("< [4 - 7] > : various surfaces examples")
            .addLine("< l > : rotating light mode")
            .addLine("< 8 > : interpolative colors")
            .addLine("< 9 > : level set shader")
            .addLine("< 0 > : Z-buffer shader")
            .addLine("< Available functions > : sin ,cos, exp, ln, tan, acos, asin, atan, min, adding more")
            .addLine("< operators > : +, - , *, ^ ")
            .addLine("< Available constants > : pi")
            .addLine("< a > : draw Axis")
            .addLine("< mouse > : rotate camera")
            .addLine("---------------------------------------------------")
            .addLine("< g > : generate obj file!!!")
            .addLine("Made by Pedroth")
            .buildWithTitle("Help");

    /*
     * size of the screen
     */
    private int wChanged, hChanged;

    /*
     * 3D Engine
     */
    private ImageWindow wd;
    private TriWin graphics;

    /*
     * User Interface(UI) crap
     */
    private JPanel panel;
    private JPanel surfacePanel;
    private JPanel linePanel;
    private JComboBox comboBoxS;
    private JComboBox comboBoxL;
    private JTextField fxStr;
    private JTextField fyStr;
    private JTextField fzStr;
    private JTextField ftXStr;
    private JTextField ftYStr;
    private JTextField ftZStr;
    private JTextField uMinTxt, uMaxTxt, vMinTxt, vMaxTxt, tMinTxt, tMaxTxt;
    private JTextField samplesTxtS;
    private JTextField samplesTxtL;
    private JButton zoomFitS;
    private JButton zoomFitL;
    private JButton drawButtonS;
    private JButton drawButtonL;

    /*
     * to check the need to add UI on the JFrame
     */
    private boolean init;

    /*
     * spherical coordinates of the camera
     */
    private double theta, phi;

    /*
     * mouse coordinates
     */
    private int mx;
    private int my;
    private int mRotation;

    /*
     * camera dynamics
     */
    private double raw;
    private double velocity;
    private double acceleration;
    private double oldTime;
    private double thrust;

    /*
     * where the camera is looking
     */
    private TriVector focalPoint;

    /*
     * variable to check the need of drawing
     */
    private boolean drawFunction;

    /*
     * variables describing the domain of the surface or line
     */
    private double umin, umax, vmin, vmax, tmin, tmax, samples;

    /*
     * computes function from string
     */
    private ExpressionFunction xFunc;
    private ExpressionFunction yFunc;
    private ExpressionFunction zFunc;
    private ExpressionFunction xTFunc;
    private ExpressionFunction yTFunc;
    private ExpressionFunction zTFunc;
    private String[] vars = {"u", "v"};
    private String[] tVars = {"t"};

    /*
     * ZBuffer/WiredFrame
     */
    private boolean isZBuffer;

    /*
     * maximum and minimum x,y,z values of the mesh
     */
    private double maxX;
    private double minX;
    private double maxY;
    private double minY;
    private double maxZ;
    private double minZ;

    /*
     * colorState = 1 -> z - heatMap, colorState = 2 -> random, colorState = 3
     * -> shading
     */
    private int colorState;

    /*
     * variable to control when to draw axis
     */
    private boolean drawAxis;

    /*
     * zoom fit boolean
     */
    private boolean isZoomFit;

    /*
     * shader Paint method
     */
    private FlatShader shader;

    /*
     * motion light particle
     */
    private TriVector motionLight;

    /*
     * check if light is moving.
     */
    private boolean isMotionLight;

    /*
     * variable control
     */
    private boolean isShading;
    private boolean axisAlreadyBuild;

    /*
     * there is no need in this class i only put here for the case i want to
     * insert a timer
     */
    private Runnable euler = () -> {
        double currentTime = (System.currentTimeMillis()) * 1E-03;
        double dt = currentTime - this.oldTime;
        this.oldTime = currentTime;
        this.acceleration = -this.velocity + this.thrust;
        this.velocity += this.acceleration * dt;
        this.raw += this.velocity * dt;
        orbit(this.theta, this.phi, this.focalPoint);
        if (this.drawAxis) {
            buildAxis();
        }
        if (this.drawFunction) {
            if (this.comboBoxS.getSelectedItem() == "Surface")
                buildSurface();
            else
                buildLine();
        }
        if (isZoomFit) {
            cameraFindGraph();
        }
        updateLight(dt);
        this.graphics.drawElements();
        repaint();
    };

    public LinesSurfaces(boolean isApplet) {
        /*
         * Set JFrame title.
         */
        super("Draw Lines {x(t),y(t),z(t)} and Surfaces {x(u,v),y(u,v),z(u,v)} - Press h for Help");

        this.setLayout(null);

        /*
         * Begin the engine
         */
        this.graphics = new TriWin(Math.PI / 2);
        this.wd = this.graphics.getBuffer();
        this.shader = new FlatShader();
        this.shader.addLightPoint(new TriVector(1, 0, 0));
        this.shader.setShininess(15);
        this.shader.setAmbientLightParameter(0.5);
        this.graphics.setMethod(this.shader);
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
        this.surfacePanel = new JPanel();
        this.linePanel = new JPanel();
        this.fxStr = new JTextField();
        this.fyStr = new JTextField();
        this.fzStr = new JTextField();
        this.ftXStr = new JTextField();
        this.ftYStr = new JTextField();
        this.ftZStr = new JTextField();
        this.uMinTxt = new JTextField();
        this.uMaxTxt = new JTextField();
        this.vMinTxt = new JTextField();
        this.vMaxTxt = new JTextField();
        this.tMinTxt = new JTextField();
        this.tMaxTxt = new JTextField();
        this.samplesTxtS = new JTextField();
        this.samplesTxtL = new JTextField();
        this.comboBoxS = new JComboBox(new String[]{"Surface", "Line"});
        this.comboBoxL = new JComboBox(new String[]{"Surface", "Line"});

        // code repetition sorry!
        this.comboBoxS.addItemListener(this::comboBoxListen);
        this.comboBoxL.addItemListener(this::comboBoxListen);
        this.drawButtonS = new JButton("Draw");
        this.drawButtonL = new JButton("Draw");
        this.drawButtonS.addActionListener(e -> {
            this.drawFunction = true;
            this.graphics.removeAllElements();
        });
        this.drawButtonL.addActionListener(e -> {
            this.drawFunction = true;
            this.graphics.removeAllElements();
        });
        this.zoomFitS = new JButton("Zoom fit");
        this.zoomFitL = new JButton("Zoom fit");
        this.zoomFitS.addActionListener(arg0 -> this.isZoomFit = true);
        this.zoomFitL.addActionListener(arg0 -> this.isZoomFit = true);
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
         *  * Add listeners.
         */
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);

        this.isMotionLight = false;
        this.motionLight = new TriVector(this.raw, this.raw, this.raw);
        this.isShading = true;
        this.axisAlreadyBuild = false;
    }

    public static void main(String[] args) {
        new LinesSurfaces(false);
    }

    private void comboBoxListen(ItemEvent e) {
        if (this.comboBoxL.getSelectedItem() != this.comboBoxS.getSelectedItem()) {
            this.comboBoxL.setSelectedIndex(this.comboBoxS.getSelectedIndex());
        }
        this.graphics.removeAllElements();
        drawFunction = true;
        processLayout();
    }

    /**
     * order does matter
     * <p>
     * for instance you must draw background ,text, other stuff after the Jframe
     * components such as buttons textAreas, textFields, checkBoxs, etc...
     */
    private void processLayout() {
        int border = 70;

        if (this.init) {
            this.panel.setLayout(new GridLayout(1, 1));
            /*
             * surface panel
             */
            this.surfacePanel.setLayout(new GridLayout(10, 1));
            JPanel comboBoxPanel = new JPanel();
            comboBoxPanel.setLayout(new GridLayout(1, 2));
            comboBoxPanel.add(this.comboBoxS);
            this.surfacePanel.add(comboBoxPanel);
            JPanel functionPanel = new JPanel();
            functionPanel.setLayout(new GridLayout(3, 2));
            functionPanel.add(new JLabel("X(u,v)"));
            functionPanel.add(fxStr);
            functionPanel.add(new JLabel("Y(u,v)"));
            functionPanel.add(fyStr);
            functionPanel.add(new JLabel("Z(u,v)"));
            functionPanel.add(fzStr);
            this.surfacePanel.add(functionPanel);
            JPanel intervalPanelX = new JPanel();
            intervalPanelX.setLayout(new GridLayout(1, 2));
            intervalPanelX.add(new JLabel("umin"));
            intervalPanelX.add(new JLabel("umax"));
            this.surfacePanel.add(intervalPanelX);
            JPanel intervalPanelXValue = new JPanel();
            intervalPanelXValue.setLayout(new GridLayout(1, 2));
            intervalPanelXValue.add(uMinTxt);
            intervalPanelXValue.add(uMaxTxt);
            this.surfacePanel.add(intervalPanelXValue);
            JPanel intervalPanelY = new JPanel();
            intervalPanelY.setLayout(new GridLayout(1, 2));
            intervalPanelY.add(new JLabel("vmin"));
            intervalPanelY.add(new JLabel("vmax"));
            this.surfacePanel.add(intervalPanelY);
            JPanel intervalPanelYValue = new JPanel();
            intervalPanelYValue.setLayout(new GridLayout(1, 2));
            intervalPanelYValue.add(vMinTxt);
            intervalPanelYValue.add(vMaxTxt);
            this.surfacePanel.add(intervalPanelYValue);
            JPanel stepPanel = new JPanel();
            stepPanel.setLayout(new GridLayout(1, 2));
            stepPanel.add(new JLabel("U\\V samples"));
            stepPanel.add(this.samplesTxtS);
            this.surfacePanel.add(stepPanel);
            JPanel drawButtonPanel = new JPanel();
            drawButtonPanel.setLayout(new GridLayout(1, 2));
            drawButtonPanel.add(zoomFitS);
            drawButtonPanel.add(drawButtonS);
            this.surfacePanel.add(drawButtonPanel);

            /*
             * line panel
             */
            this.linePanel.setLayout(new GridLayout(10, 1));
            JPanel comboBoxPanelT = new JPanel();
            comboBoxPanelT.setLayout(new GridLayout(1, 2));
            comboBoxPanelT.add(this.comboBoxL);
            this.linePanel.add(comboBoxPanelT);
            JPanel lineFunctionPanel = new JPanel();
            lineFunctionPanel.setLayout(new GridLayout(3, 2));
            lineFunctionPanel.add(new JLabel("X(t)"));
            lineFunctionPanel.add(ftXStr);
            lineFunctionPanel.add(new JLabel("Y(t)"));
            lineFunctionPanel.add(ftYStr);
            lineFunctionPanel.add(new JLabel("Z(t)"));
            lineFunctionPanel.add(ftZStr);
            this.linePanel.add(lineFunctionPanel);
            JPanel intervalPanelT = new JPanel();
            intervalPanelT.setLayout(new GridLayout(1, 2));
            intervalPanelT.add(new JLabel("tmin"));
            intervalPanelT.add(new JLabel("tmax"));
            this.linePanel.add(intervalPanelT);
            JPanel intervalPanelTValue = new JPanel();
            intervalPanelTValue.setLayout(new GridLayout(1, 2));
            intervalPanelTValue.add(tMinTxt);
            intervalPanelTValue.add(tMaxTxt);
            this.linePanel.add(intervalPanelTValue);
            JPanel stepPanelT = new JPanel();
            stepPanelT.setLayout(new GridLayout(1, 2));
            stepPanelT.add(new JLabel("T samples"));
            stepPanelT.add(samplesTxtL);
            this.linePanel.add(stepPanelT);
            JPanel drawButtonPanelT = new JPanel();
            drawButtonPanelT.setLayout(new GridLayout(1, 2));
            drawButtonPanelT.add(zoomFitL);
            drawButtonPanelT.add(drawButtonL);
            this.linePanel.add(drawButtonPanelT);

            this.comboBoxS.setSelectedIndex(0);
            this.comboBoxL.setSelectedIndex(0);
            this.fxStr.setText("(1 + 0.5 * cos(v)) * cos(u)");
            this.fyStr.setText("(1 + 0.5 * cos(v)) * sin(u)");
            this.fzStr.setText("0.5 * sin(v)");
            this.uMinTxt.setText("0");
            this.uMaxTxt.setText("2*pi");
            this.vMinTxt.setText("0");
            this.vMaxTxt.setText("2*pi");
            this.ftXStr.setText("(2 + cos(2 * t))*cos(3 * t)");
            this.ftYStr.setText("(2 + cos(2 * t)) * sin(3 * t)");
            this.ftZStr.setText("sin(4 * t)");
            this.tMinTxt.setText("0");
            this.tMaxTxt.setText("2*pi");
            this.samplesTxtS.setText("33");
            this.samplesTxtL.setText("33");
            this.add(this.panel);
            this.init = false;
        }

        if (this.comboBoxS.getSelectedItem() == "Surface" || this.comboBoxL.getSelectedItem() == "Surface") {
            hideLineUI();
            processSurfaceUI();
        } else {
            hideSurfaceUI();
            processLineUI();
        }
        this.wd.setWindowSize(border * this.wChanged / 100, this.hChanged);
        this.panel.setBounds(border * this.wChanged / 100, 0, (100 - border) * this.wChanged / 100, this.hChanged);
        this.panel.updateUI();
    }

    private void hideLineUI() {
        this.panel.remove(this.linePanel);
    }

    private void hideSurfaceUI() {
        this.panel.remove(this.surfacePanel);
    }

    private void processLineUI() {
        this.panel.add(this.linePanel);
    }

    private void processSurfaceUI() {
        this.panel.add(this.surfacePanel);
    }

    private void cameraFindGraph() {
        Double v[] = new Double[3];
        v[0] = (maxX + minX) / 2;
        v[1] = (maxY + minY) / 2;
        v[2] = (maxZ + minZ) / 2;
        this.raw = 3 * maxX;
        this.focalPoint = new TriVector(v[0], v[1], v[2]);
        isZoomFit = false;
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

    /**
     * build surface bad programming repeating code for xyz i should have an
     * array instead.
     */
    private void buildSurface() {
        int MaxPoly = 25600;
        double colorHSB = Math.random();
        this.drawFunction = false;
        this.umin = numericRead(this.uMinTxt.getText());
        this.umax = numericRead(this.uMaxTxt.getText());
        this.vmin = numericRead(this.vMinTxt.getText());
        this.vmax = numericRead(this.vMaxTxt.getText());
        this.samples = numericRead(this.samplesTxtS.getText());
        this.xFunc = new ExpressionFunction(this.fxStr.getText(), this.vars);
        this.yFunc = new ExpressionFunction(this.fyStr.getText(), this.vars);
        this.zFunc = new ExpressionFunction(this.fzStr.getText(), this.vars);
        this.maxX = Double.NEGATIVE_INFINITY;
        this.minX = Double.POSITIVE_INFINITY;
        this.maxY = Double.NEGATIVE_INFINITY;
        this.minY = Double.POSITIVE_INFINITY;
        this.maxZ = Double.NEGATIVE_INFINITY;
        this.minZ = Double.POSITIVE_INFINITY;
        try {
            this.xFunc.init();
            this.yFunc.init();
            this.zFunc.init();
        } catch (SyntaxErrorException e) {
            JOptionPane.showMessageDialog(null, "there is a syntax error in the formula, pls change the formula." + String.format("%n") + " try to use more brackets, try not to cocatenate 2*x^2 as 2x2." + String.format("%n") + "check also for simple errors like 1/*2.");
        }

        final double samplesSq = this.samples * this.samples;
        if (samplesSq > MaxPoly) {
            maxPolyConstraint(this.samples);
            this.samples = numericRead(this.samplesTxtS.getText());
        }

        int inu = (int) Math.floor(this.samples);
        int inv = (int) Math.floor(this.samples);
        final double stepU = Math.abs(this.umax - this.umin) / (this.samples - 1);
        final double stepV = Math.abs(this.vmax - this.vmin) / (this.samples - 1);
        /*
         * surface
         */
        TriVector[][] surface = new TriVector[inu][inv];
        for (int j = 0; j < inv - 1; j++) {
            for (int i = 0; i < inu - 1; i++) {
                double ubase = this.umin + i * stepU;
                double vbase = this.vmin + j * stepV;
                if (surface[i][j] == null) {
                    Double[] uv = {ubase, vbase};
                    double x = this.xFunc.compute(uv);
                    double y = this.yFunc.compute(uv);
                    double z = this.zFunc.compute(uv);
                    surface[i][j] = new TriVector(x, y, z);
                }
                if (surface[i + 1][j] == null) {
                    Double[] uv = {ubase + stepU, vbase};
                    double x = this.xFunc.compute(uv);
                    double y = this.yFunc.compute(uv);
                    double z = this.zFunc.compute(uv);
                    surface[i + 1][j] = new TriVector(x, y, z);
                }
                if (surface[i + 1][j + 1] == null) {
                    Double[] uv = {ubase + stepU, vbase + stepV};
                    double x = this.xFunc.compute(uv);
                    double y = this.yFunc.compute(uv);
                    double z = this.zFunc.compute(uv);
                    surface[i + 1][j + 1] = new TriVector(x, y, z);
                }
                if (surface[i][j + 1] == null) {
                    Double[] uv = {ubase, vbase + stepV};
                    double x = this.xFunc.compute(uv);
                    double y = this.yFunc.compute(uv);
                    double z = this.zFunc.compute(uv);
                    surface[i][j + 1] = new TriVector(x, y, z);
                }

                double auxX = surface[i][j].getX();
                double auxY = surface[i][j].getY();
                double auxZ = surface[i][j].getZ();
                if (!Double.isInfinite(auxX) && !Double.isNaN(auxX)) {
                    this.maxX = Math.max(auxX, this.maxX);
                    this.minX = Math.min(auxX, this.minX);
                }
                if (!Double.isInfinite(auxY) && !Double.isNaN(auxY)) {
                    this.maxY = Math.max(auxY, this.maxY);
                    this.minY = Math.min(auxY, this.minY);
                }
                if (!Double.isInfinite(auxZ) && !Double.isNaN(auxZ)) {
                    this.maxZ = Math.max(auxZ, this.maxZ);
                    this.minZ = Math.min(auxZ, this.minZ);
                }
            }
        }
        for (int j = 0; j < inv - 1; j++) {
            for (int i = 0; i < inu - 1; i++) {
                Element e = new Quad(surface[i][j], surface[i + 1][j], surface[i + 1][j + 1], surface[i][j + 1]);
                setElementColor(e, colorHSB);
                this.graphics.addtoList(e);
            }
        }
    }

    private void setElementColor(Element e, double colorHSB) {
        double red = 0;
        double blue = 240.0 / 360.0;
        for (int i = 0; i < e.getNumOfPoints(); i++) {
            double z = e.getNPoint(i).getZ();
            double x = -1 + 2 * (z - minZ) / (maxZ - minZ);
            switch (this.colorState) {
                case 0:
                    /*
                     * linear interpolation between blue color and red
                     */
                    colorHSB = blue + (red - blue) * 0.5 * (x + 1);
                    e.setColorPoint(Color.getHSBColor((float) colorHSB, 1f, 1f), i);
                    break;
                case 1:
                    Random r = new Random();
                    e.setColorPoint(Color.getHSBColor((float) colorHSB, r.nextFloat(), 1f), i);
                    break;
                case 2:
                    e.setColorPoint(Color.blue, i);
                    break;
            }
        }
    }

    private void buildAxis() {
        if (!axisAlreadyBuild) {
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

    private void maxPolyConstraint(double delta) {
        int nextSample = (int) Math.floor(Math.sqrt(delta));
        this.samplesTxtS.setText("" + nextSample);
        JOptionPane.showMessageDialog(null, "the X/Y samples are too high, pls choose a lower one");
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
        // shader.changeNthLight(0, eye);
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        Map<Integer, Runnable> keyCode2ActionMap = new HashMap<>();
        keyCode2ActionMap.put(KeyEvent.VK_W, () -> this.thrust = -5);
        keyCode2ActionMap.put(KeyEvent.VK_S, () -> this.thrust = 5);
        keyCode2ActionMap.put(KeyEvent.VK_Z, () -> {
            this.isZBuffer = !this.isZBuffer;
            this.graphics.setMethod(this.isZBuffer ? this.shader : new WiredPrespective());
            this.drawFunction = true;
            this.graphics.removeAllElements();
        });
        keyCode2ActionMap.put(KeyEvent.VK_1, () -> {
            this.colorState = 0;
            this.graphics.removeAllElements();
            this.drawFunction = true;
        });
        keyCode2ActionMap.put(KeyEvent.VK_2, () -> {
            this.colorState = 1;
            this.graphics.removeAllElements();
            this.drawFunction = true;
        });
        keyCode2ActionMap.put(KeyEvent.VK_3, () -> {
            this.colorState = 2;
            this.graphics.removeAllElements();
            this.drawFunction = true;
        });
        keyCode2ActionMap.put(KeyEvent.VK_4, () -> {
            this.comboBoxS.setSelectedIndex(0);
            this.fxStr.setText("cos(u) + v * (cos(u) * cos(0.5 * u))");
            this.fyStr.setText("sin(u) + v * (sin(u) * cos(0.5 * u))");
            this.fzStr.setText("v * sin(0.5 * u)");
            this.uMinTxt.setText("0");
            this.uMaxTxt.setText("2*pi");
            this.vMinTxt.setText("-0.5");
            this.vMaxTxt.setText("0.5");
            this.samplesTxtS.setText("33");
            this.graphics.removeAllElements();
            this.drawFunction = true;
        });
        keyCode2ActionMap.put(KeyEvent.VK_5, () -> {
            this.comboBoxS.setSelectedIndex(0);
            this.fxStr.setText("(1 + 0.5 * cos(v)) * cos(u)");
            this.fyStr.setText("(1 + 0.5 * cos(v)) * sin(u)");
            this.fzStr.setText("0.5 * sin(v)");
            this.uMinTxt.setText("0");
            this.uMaxTxt.setText("2*pi");
            this.vMinTxt.setText("0");
            this.vMaxTxt.setText("2*pi");
            this.samplesTxtS.setText("33");
            this.graphics.removeAllElements();
            this.drawFunction = true;
        });
        keyCode2ActionMap.put(KeyEvent.VK_6, () -> {
            this.comboBoxS.setSelectedIndex(0);
            this.fxStr.setText("0.5 * sin(u) * cos(v)");
            this.fyStr.setText("0.5 * sin(u) * sin(v)");
            this.fzStr.setText("0.5 * cos(u)");
            this.uMinTxt.setText("0");
            this.uMaxTxt.setText("pi");
            this.vMinTxt.setText("0");
            this.vMaxTxt.setText("2*pi");
            this.samplesTxtS.setText("33");
            this.graphics.removeAllElements();
            this.drawFunction = true;
        });
        keyCode2ActionMap.put(KeyEvent.VK_7, () -> {
            this.comboBoxS.setSelectedIndex(0);
            this.fxStr.setText("(4 / 3) ^ v * sin(u) * sin(u) * cos(v)");
            this.fyStr.setText("(4 / 3) ^ v * sin(u) * sin(u) * sin(v) ");
            this.fzStr.setText("(4/3)^ v * sin(u) * cos(u)");
            this.uMinTxt.setText("0");
            this.uMaxTxt.setText("pi");
            this.vMinTxt.setText("-6");
            this.vMaxTxt.setText("1.1*pi");
            this.samplesTxtS.setText("33");
            this.graphics.removeAllElements();
            this.drawFunction = true;
        });
        keyCode2ActionMap.put(KeyEvent.VK_N, () -> {
            this.isShading = !this.isShading;
            this.graphics.setMethod(this.isShading ? this.shader : new SquareZBuffer());
        });
        keyCode2ActionMap.put(KeyEvent.VK_A, () -> {
            this.drawAxis = !this.drawAxis;
            this.axisAlreadyBuild = false;
            this.graphics.removeAllElements();
            this.drawFunction = true;
        });
        keyCode2ActionMap.put(KeyEvent.VK_L, () -> this.isMotionLight = !this.isMotionLight);
        keyCode2ActionMap.put(KeyEvent.VK_8, () -> this.graphics.setMethod(new InterpolativeShader()));
        keyCode2ActionMap.put(KeyEvent.VK_9, () -> this.graphics.setMethod(new LevelSetShader()));
        keyCode2ActionMap.put(KeyEvent.VK_0, () -> this.graphics.setMethod(new ZbufferShader()));
        keyCode2ActionMap.put(KeyEvent.VK_G, this::generateMesh);
        Optional.ofNullable(keyCode2ActionMap.get(arg0.getKeyCode())).ifPresent(Runnable::run);
    }

    private void generateMesh() {
        String s = (String) JOptionPane.showInputDialog(
                this,
                (this.comboBoxS.getSelectedItem() == "Surface") ? "UV samples" : "T samples",
                "Obj file generation",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                this.samples);

        Optional.ofNullable(s).ifPresent(x -> {
            int samples = (int) Math.floor(numericRead(s));
            if ("Surface".equals(this.comboBoxS.getSelectedItem())) {
                generateSurf(samples);
            } else {
                generateLine(samples);
            }
        });
    }

    private void generateLine(int samples) {
        final double stepT = Math.abs(this.tmax - this.tmin) / (this.samples - 1);
        TriVector[] curve = new TriVector[samples];
        for (int i = 0; i < samples; i++) {
            double t = this.tmin + i * stepT;
            Double[] tvar = {t};
            double x = this.xTFunc.compute(tvar);
            double y = this.yTFunc.compute(tvar);
            double z = this.zTFunc.compute(tvar);
            curve[i] = new TriVector(x, y, z);
        }
        StringBuilder objFile = new StringBuilder();
        for (TriVector aCurve : curve) {
            objFile.append("v ").append(aCurve.getX()).append(" ").append(aCurve.getY()).append(" ").append(aCurve.getZ()).append("\n");
        }
        for (int i = 0; i < curve.length - 1; i++) {
            objFile.append("l ").append(i + 1).append(" ").append(i + 2).append("\n");
        }
        new TextFrame("Curve obj file", objFile.toString()).setVisible(true);
    }

    private void generateSurf(int samples) {
        double newStepU = (this.umax - this.umin) / (samples - 1);
        double newStepV = (this.vmax - this.vmin) / (samples - 1);
        TriVector[][] surf = new TriVector[samples][samples];
        for (int i = 0; i < samples; i++) {
            for (int j = 0; j < samples; j++) {
                double u = this.umin + i * newStepU;
                double v = this.vmin + j * newStepV;
                Double[] uv = {u, v};
                double x = this.xFunc.compute(uv);
                double y = this.yFunc.compute(uv);
                double z = this.zFunc.compute(uv);
                surf[i][j] = new TriVector(x, y, z);
            }
        }

        StringBuilder objFile = new StringBuilder();
        for (int i = 0; i < samples; i++) {
            for (int j = 0; j < samples; j++) {
                objFile.append("v ").append(surf[i][j].getX()).append(" ").append(surf[i][j].getY()).append(" ").append(surf[i][j].getZ()).append("\n");
            }
        }
        for (int i = 0; i < samples - 1; i++) {
            for (int j = 0; j < samples - 1; j++) {
                int index = j + samples * i + 1;
                objFile.append("f ").append(index).append(" ").append(index + samples).append(" ").append(index + samples + 1).append("\n");
                objFile.append("f ").append(index).append(" ").append(index + samples + 1).append(" ").append(index + 1).append("\n");
            }
        }
        new TextFrame("Surface obj file", objFile.toString()).setVisible(true);
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
        this.panel.transferFocusUpCycle();
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
        this.mRotation = e.getWheelRotation();
        this.uMinTxt.setText("" + (this.umin - this.mRotation));
        this.uMaxTxt.setText("" + (this.umax + this.mRotation));
        this.vMinTxt.setText("" + (this.vmin - this.mRotation));
        this.vMaxTxt.setText("" + (this.vmax + this.mRotation));
        this.tMinTxt.setText("" + (this.tmin - this.mRotation));
        this.tMaxTxt.setText("" + (this.tmax + this.mRotation));
        this.graphics.removeAllElements();
        this.drawFunction = true;
    }

    /**
     * build the parametric line
     */
    private void buildLine() {
        int MaxPoly = 25600;
        double colorHSB = Math.random();
        this.drawFunction = false;
        this.tmin = numericRead(this.tMinTxt.getText());
        this.tmax = numericRead(this.tMaxTxt.getText());
        this.samples = numericRead(this.samplesTxtL.getText());
        this.xTFunc = new ExpressionFunction(ftXStr.getText(), tVars);
        this.yTFunc = new ExpressionFunction(ftYStr.getText(), tVars);
        this.zTFunc = new ExpressionFunction(ftZStr.getText(), tVars);
        this.maxX = Double.NEGATIVE_INFINITY;
        this.minX = Double.POSITIVE_INFINITY;
        this.maxY = Double.NEGATIVE_INFINITY;
        this.minY = Double.POSITIVE_INFINITY;
        this.maxZ = Double.NEGATIVE_INFINITY;
        this.minZ = Double.POSITIVE_INFINITY;
        try {
            this.xTFunc.init();
            this.yTFunc.init();
            this.zTFunc.init();
        } catch (SyntaxErrorException e) {
            JOptionPane.showMessageDialog(null, "there is a syntax error in the formula, pls change the formula." + String.format("%n") + " try to use more brackets, try not to cocatenate 2*x^2 as 2x2." + String.format("%n") + "check also for simple errors like 1/*2.");
        }
        final double samplesSq = this.samples * this.samples;
        if (samplesSq > MaxPoly) {
            maxPolyConstraint(this.samples);
            this.samples = numericRead(this.samplesTxtL.getText());
        }

        int inT = (int) Math.floor(this.samples);
        final double stepT = Math.abs(this.tmax - this.tmin) / (this.samples - 1);
        /*
         * line
         */
        TriVector[] line = new TriVector[inT];
        for (int i = 0; i < inT - 1; i++) {
            double tbase = this.tmin + i * stepT;
            if (line[i] == null) {
                Double[] tvar = {tbase};
                double x = this.xTFunc.compute(tvar);
                double y = this.yTFunc.compute(tvar);
                double z = this.zTFunc.compute(tvar);
                line[i] = new TriVector(x, y, z);
            }
            if (line[i + 1] == null) {
                Double[] tvar = {tbase + stepT};
                double x = this.xTFunc.compute(tvar);
                double y = this.yTFunc.compute(tvar);
                double z = this.zTFunc.compute(tvar);
                line[i + 1] = new TriVector(x, y, z);
            }
            double auxX = line[i].getX();
            double auxY = line[i].getY();
            double auxZ = line[i].getZ();
            if (!Double.isInfinite(auxX) && !Double.isNaN(auxX)) {
                this.maxX = Math.max(auxX, this.maxX);
                this.minX = Math.min(auxX, this.minX);
            }
            if (!Double.isInfinite(auxY) && !Double.isNaN(auxY)) {
                this.maxY = Math.max(auxY, this.maxY);
                this.minY = Math.min(auxY, this.minY);
            }
            if (!Double.isInfinite(auxZ) && !Double.isNaN(auxZ)) {
                this.maxZ = Math.max(auxZ, this.maxZ);
                this.minZ = Math.min(auxZ, this.minZ);
            }
        }
        for (int i = 0; i < inT - 1; i++) {
            Element e = new Line(line[i], line[i + 1]);
            setElementColor(e, colorHSB);
            this.graphics.addtoList(e);
        }
    }

    /**
     * updateAnimation light
     */
    private void updateLight(double dt) {
        if (this.isMotionLight) {
            TriVector v = new TriVector(-this.motionLight.getY(), this.motionLight.getX(), 0);
            v.multiConstMatrix(dt);
            this.motionLight.sum(v);
            this.shader.changeNthLight(0, this.motionLight);
        } else {
            TriVector aux = TriVector.sum(this.shader.getEyePos(), new TriVector(0, 0, 3));
            this.shader.changeNthLight(0, aux);
        }
    }
}
