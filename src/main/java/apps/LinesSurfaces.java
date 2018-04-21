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
import java.util.Random;

public class LinesSurfaces extends JFrame implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
    private static String helpText = "< w > : Camera move foward / zoom in \n\n" + "< s > : Camera move backward /zoom out \n\n" + "< z > : Toggle wireframe / zbuffer \n\n" + "< n > : Toggle flat shading \n\n" + "< [1-3] > : change color of surface \n\n" + "< [4 - 7] > : various surfaces examples \n\n" + "< l > : rotating light mode \n\n" + "< 8 > : interpolative colors \n\n" + "< 9 > : level set shader\n\n" + "< 0 > : Z-buffer shader\n\n" + "< Available functions > : sin ,cos, exp, ln, tan, acos, asin, atan, min, adding more \n\n" + "< operators > : +, - , *, ^ \n\n" + "< Available constants > : pi\n\n" + "< a > : draw Axis \n\n" + "< mouse > : rotate camera\n" + "---------------------------------------------------\n\n" + "< g > : generate obj file!!!\n\n" + "----------------------------------------------------\n\n" + "Made by Pedroth";

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
    private JTextField stepTxtS;
    private JTextField stepTxtL;
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
    private int mx, my, newMx, newMy, mRotation;

    /*
     * camera dynamics
     */
    private double raw;
    private double velocity;
    private double accelaration;
    private double oldTime;
    private double currentTime;
    private double thrust;

    /*
     * where the camera is looking
     */
    private TriVector focalPoint;

    /*
     * there is no need in this class i only put here for the case i want to
     * insert a timer
     */
    private Euler euler;

    /*
     * variable to check the need of drawing
     */
    private boolean drawFunction;

    /*
     * surface
     */
    private TriVector[][] surface;

    /*
     * line
     */
    private TriVector[] line;

    /*
     * variables describing the domain of the surface or line
     */
    private double umin, umax, vmin, vmax, tmin, tmax, step;

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

    public LinesSurfaces(boolean isApplet) {
        /*
         * Set JFrame title.
		 */
        super("Draw Lines {x(t),y(t),z(t)} and Surfaces {x(u,v),y(u,v),z(u,v)} - Press h for Help");

        this.setLayout(null);

        /*
         * Begin the engine
         */
        graphics = new TriWin(Math.PI / 2);
        wd = graphics.getBuffer();
        shader = new FlatShader();
        shader.addLightPoint(new TriVector(1, 0, 0));
        shader.setShininess(15);
        shader.setAmbientLightParameter(0.5);
        graphics.setMethod(shader);
        wd.setBackGroundColor(Color.black);
        isZBuffer = true;
        colorState = 0;
        drawAxis = false;

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
        surfacePanel = new JPanel();
        linePanel = new JPanel();
        fxStr = new JTextField();
        fyStr = new JTextField();
        fzStr = new JTextField();
        ftXStr = new JTextField();
        ftYStr = new JTextField();
        ftZStr = new JTextField();
        uMinTxt = new JTextField();
        uMaxTxt = new JTextField();
        vMinTxt = new JTextField();
        vMaxTxt = new JTextField();
        tMinTxt = new JTextField();
        tMaxTxt = new JTextField();
        stepTxtS = new JTextField();
        stepTxtL = new JTextField();
        comboBoxS = new JComboBox(new String[]{"Surface", "Line"});
        comboBoxL = new JComboBox(new String[]{"Surface", "Line"});

        // code repetition sorry!
        comboBoxS.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if (comboBoxL.getSelectedItem() != comboBoxS.getSelectedItem()) {
                    comboBoxL.setSelectedIndex(comboBoxS.getSelectedIndex());
                }
                graphics.removeAllElements();
                drawFunction = true;
                processLayout();
            }
        });
        comboBoxL.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if (comboBoxL.getSelectedItem() != comboBoxS.getSelectedItem()) {
                    comboBoxS.setSelectedIndex(comboBoxL.getSelectedIndex());
                }
                graphics.removeAllElements();
                drawFunction = true;
                processLayout();
            }
        });
        drawButtonS = new JButton("Draw");
        drawButtonL = new JButton("Draw");
        drawButtonS.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                drawFunction = true;
                graphics.removeAllElements();
            }
        });
        drawButtonL.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                drawFunction = true;
                graphics.removeAllElements();
            }
        });
        zoomFitS = new JButton("Zoom fit");
        zoomFitL = new JButton("Zoom fit");
        zoomFitS.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                isZoomFit = true;
            }
        });
        zoomFitL.addActionListener(new ActionListener() {

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
         *  * Add listeners.
		 */
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);

        isMotionLight = false;
        motionLight = new TriVector(raw, raw, raw);
        isShading = true;
        axisAlreadyBuild = false;
    }

    public static void main(String[] args) {
        new LinesSurfaces(false);
    }

    /**
     * order does matter
     * <p>
     * for instance you must draw background ,text, other stuff after the Jframe
     * components such as buttons textAreas, textFields, checkBoxs, etc...
     */
    public void processLayout() {
        int border = 70;

        if (init) {
            panel.setLayout(new GridLayout(1, 1));
            /*
             * surface panel
             */
            surfacePanel.setLayout(new GridLayout(10, 1));
            JPanel comboBoxPanel = new JPanel();
            comboBoxPanel.setLayout(new GridLayout(1, 2));
            comboBoxPanel.add(comboBoxS);
            surfacePanel.add(comboBoxPanel);
            JPanel functionPanel = new JPanel();
            functionPanel.setLayout(new GridLayout(3, 2));
            functionPanel.add(new JLabel("X(u,v)"));
            functionPanel.add(fxStr);
            functionPanel.add(new JLabel("Y(u,v)"));
            functionPanel.add(fyStr);
            functionPanel.add(new JLabel("Z(u,v)"));
            functionPanel.add(fzStr);
            surfacePanel.add(functionPanel);
            JPanel intervalPanelX = new JPanel();
            intervalPanelX.setLayout(new GridLayout(1, 2));
            intervalPanelX.add(new JLabel("umin"));
            intervalPanelX.add(new JLabel("umax"));
            surfacePanel.add(intervalPanelX);
            JPanel intervalPanelXValue = new JPanel();
            intervalPanelXValue.setLayout(new GridLayout(1, 2));
            intervalPanelXValue.add(uMinTxt);
            intervalPanelXValue.add(uMaxTxt);
            surfacePanel.add(intervalPanelXValue);
            JPanel intervalPanelY = new JPanel();
            intervalPanelY.setLayout(new GridLayout(1, 2));
            intervalPanelY.add(new JLabel("vmin"));
            intervalPanelY.add(new JLabel("vmax"));
            surfacePanel.add(intervalPanelY);
            JPanel intervalPanelYValue = new JPanel();
            intervalPanelYValue.setLayout(new GridLayout(1, 2));
            intervalPanelYValue.add(vMinTxt);
            intervalPanelYValue.add(vMaxTxt);
            surfacePanel.add(intervalPanelYValue);
            JPanel stepPanel = new JPanel();
            stepPanel.setLayout(new GridLayout(1, 2));
            stepPanel.add(new JLabel("U\\V step"));
            stepPanel.add(stepTxtS);
            surfacePanel.add(stepPanel);
            JPanel drawButtonPanel = new JPanel();
            drawButtonPanel.setLayout(new GridLayout(1, 2));
            drawButtonPanel.add(zoomFitS);
            drawButtonPanel.add(drawButtonS);
            surfacePanel.add(drawButtonPanel);

            /*
             * line panel
             */
            linePanel.setLayout(new GridLayout(10, 1));
            JPanel comboBoxPanelT = new JPanel();
            comboBoxPanelT.setLayout(new GridLayout(1, 2));
            comboBoxPanelT.add(comboBoxL);
            linePanel.add(comboBoxPanelT);
            JPanel lineFunctionPanel = new JPanel();
            lineFunctionPanel.setLayout(new GridLayout(3, 2));
            lineFunctionPanel.add(new JLabel("X(t)"));
            lineFunctionPanel.add(ftXStr);
            lineFunctionPanel.add(new JLabel("Y(t)"));
            lineFunctionPanel.add(ftYStr);
            lineFunctionPanel.add(new JLabel("Z(t)"));
            lineFunctionPanel.add(ftZStr);
            linePanel.add(lineFunctionPanel);
            JPanel intervalPanelT = new JPanel();
            intervalPanelT.setLayout(new GridLayout(1, 2));
            intervalPanelT.add(new JLabel("tmin"));
            intervalPanelT.add(new JLabel("tmax"));
            linePanel.add(intervalPanelT);
            JPanel intervalPanelTValue = new JPanel();
            intervalPanelTValue.setLayout(new GridLayout(1, 2));
            intervalPanelTValue.add(tMinTxt);
            intervalPanelTValue.add(tMaxTxt);
            linePanel.add(intervalPanelTValue);
            JPanel stepPanelT = new JPanel();
            stepPanelT.setLayout(new GridLayout(1, 2));
            stepPanelT.add(new JLabel("T step"));
            stepPanelT.add(stepTxtL);
            linePanel.add(stepPanelT);
            JPanel drawButtonPanelT = new JPanel();
            drawButtonPanelT.setLayout(new GridLayout(1, 2));
            drawButtonPanelT.add(zoomFitL);
            drawButtonPanelT.add(drawButtonL);
            linePanel.add(drawButtonPanelT);

            comboBoxS.setSelectedIndex(0);
            comboBoxL.setSelectedIndex(0);
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
            stepTxtS.setText("pi/12");
            stepTxtL.setText("pi/32");
            this.add(panel);
            init = false;
        }

        if (comboBoxS.getSelectedItem() == "Surface" || comboBoxL.getSelectedItem() == "Surface") {
            hideLineUI();
            processSurfaceUI();
        } else {
            hideSurfaceUI();
            processLineUI();
        }
        wd.setWindowSize(border * wChanged / 100, hChanged);
        panel.setBounds(border * wChanged / 100, 0, (100 - border) * wChanged / 100, hChanged);
        panel.updateUI();
    }

    public void hideLineUI() {
        panel.remove(linePanel);
    }

    public void hideSurfaceUI() {
        panel.remove(surfacePanel);
    }

    public void processLineUI() {
        panel.add(linePanel);
    }

    public void processSurfaceUI() {
        panel.add(surfacePanel);
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
     * @param s string to be read.
     * @return computation of the expression in s
     */
    public double numericRead(String s) {
        ExpressionFunction in;
        in = new ExpressionFunction(s, new String[]{});
        in.init();
        return in.compute(new Double[]{});
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
        step = numericRead(stepTxtS.getText());
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
            step = numericRead(stepTxtS.getText());
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
                    Double[] uv = {u, v};
                    x = xFunc.compute(uv);
                    y = yFunc.compute(uv);
                    z = zFunc.compute(uv);
                    surface[i][j] = new TriVector(x, y, z);
                    // surface[i][j] = new TriVector(x, y, r.nextDouble());
                }
                if (surface[i + 1][j] == null) {
                    u = umin + (i + 1) * step;
                    v = vmin + j * step;
                    Double[] uv = {u, v};
                    x = xFunc.compute(uv);
                    y = yFunc.compute(uv);
                    z = zFunc.compute(uv);
                    surface[i + 1][j] = new TriVector(x, y, z);
                    // surface[i+1][j] = new TriVector(x, y, r.nextDouble());
                }
                if (surface[i + 1][j + 1] == null) {
                    u = umin + (i + 1) * step;
                    v = vmin + (j + 1) * step;
                    Double[] uv = {u, v};
                    x = xFunc.compute(uv);
                    y = yFunc.compute(uv);
                    z = zFunc.compute(uv);
                    surface[i + 1][j + 1] = new TriVector(x, y, z);
                    // surface[i+1][j+1] = new TriVector(x, y, r.nextDouble());
                }
                if (surface[i][j + 1] == null) {
                    u = umin + (i) * step;
                    v = vmin + (j + 1) * step;
                    Double[] uv = {u, v};
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

        stepTxtS.setText("" + nextStep);
        JOptionPane.showMessageDialog(null, "the X/Y step is too low, pls choose a higher one");

    }

    public void paint(Graphics g) {
        if (Math.abs(wChanged - this.getWidth()) > 0 || Math.abs(hChanged - this.getHeight()) > 0) {
            wChanged = this.getWidth();
            hChanged = this.getHeight();
            processLayout();
        }
        panel.update(panel.getGraphics());
        update(g);
    }

    public void update(Graphics g) {
        wd.clearImageWithBackGround();
        euler.run();
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
            comboBoxS.setSelectedIndex(0);
            fxStr.setText("cos(u) + v * (cos(u) * cos(0.5 * u))");
            fyStr.setText("sin(u) + v * (sin(u) * cos(0.5 * u))");
            fzStr.setText("v * sin(0.5 * u)");
            uMinTxt.setText("0");
            uMaxTxt.setText("2*pi");
            vMinTxt.setText("-0.5");
            vMaxTxt.setText("0.5");
            stepTxtS.setText("pi/32");
            graphics.removeAllElements();
            drawFunction = true;
        } else if (arg0.getKeyCode() == KeyEvent.VK_5) {
            comboBoxS.setSelectedIndex(0);
            fxStr.setText("(1 + 0.5 * cos(v)) * cos(u)");
            fyStr.setText("(1 + 0.5 * cos(v)) * sin(u)");
            fzStr.setText("0.5 * sin(v)");
            uMinTxt.setText("0");
            uMaxTxt.setText("2*pi");
            vMinTxt.setText("0");
            vMaxTxt.setText("2*pi");
            stepTxtS.setText("pi/24");
            graphics.removeAllElements();
            drawFunction = true;
        } else if (arg0.getKeyCode() == KeyEvent.VK_6) {
            comboBoxS.setSelectedIndex(0);
            fxStr.setText("0.5 * sin(u) * cos(v)");
            fyStr.setText("0.5 * sin(u) * sin(v)");
            fzStr.setText("0.5 * cos(u)");
            uMinTxt.setText("0");
            uMaxTxt.setText("pi");
            vMinTxt.setText("0");
            vMaxTxt.setText("2*pi");
            stepTxtS.setText("pi/24");
            graphics.removeAllElements();
            drawFunction = true;
        } else if (arg0.getKeyCode() == KeyEvent.VK_7) {
            comboBoxS.setSelectedIndex(0);
            fxStr.setText("(4 / 3) ^ v * sin(u) * sin(u) * cos(v)");
            fyStr.setText("(4 / 3) ^ v * sin(u) * sin(u) * sin(v) ");
            fzStr.setText("(4/3)^ v * sin(u) * cos(u)");
            uMinTxt.setText("0");
            uMaxTxt.setText("pi");
            vMinTxt.setText("-6");
            vMaxTxt.setText("1.1*pi");
            stepTxtS.setText("pi/24");
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
        } else if (arg0.getKeyCode() == KeyEvent.VK_0) {
            graphics.setMethod(new ZbufferShader());
        } else if (arg0.getKeyCode() == KeyEvent.VK_G) {
            generateMesh();
        }

    }

    private void generateMesh() {
        String s = (String) JOptionPane.showInputDialog(
                this,
                (comboBoxS.getSelectedItem() == "Surface") ? "UV step" : "T step",
                "Obj file generation",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);


        if (!(s == null)) {
            double step = numericRead(s);
            if (comboBoxS.getSelectedItem() == "Surface") {
                generateSurf(step);
            } else {
                generateLine(step);
            }
        }
    }

    private void generateLine(double step) {
        int numSamples = (int) Math.floor(Math.abs(tmax - tmin) / step);
        double newStep = (tmax - tmin) / (numSamples - 1);
        TriVector[] curve = new TriVector[numSamples];
        for (int i = 0; i < numSamples; i++) {
            double t = tmin + i * newStep;
            Double[] tvar = {t};
            double x = xTFunc.compute(tvar);
            double y = yTFunc.compute(tvar);
            double z = zTFunc.compute(tvar);
            curve[i] = new TriVector(x, y, z);
        }
        String objFile = "";
        for (int i = 0; i < curve.length; i++) {
            objFile += "v" + " " + curve[i].getX() + " " + curve[i].getY() + " " + curve[i].getZ() + "\n";
        }
        for (int i = 0; i < curve.length - 1; i++) {
            objFile += "l" + " " + (i + 1) + " " + (i + 2) + "\n";
        }
        TextFrame frame = new TextFrame("Curve obj file", objFile);
    }

    private void generateSurf(double step) {
        int numSamplesU = (int) Math.floor(Math.abs(umax - umin) / step);
        double newStepU = (umax - umin) / (numSamplesU - 1);
        int numSamplesV = (int) Math.floor(Math.abs(vmax - vmin) / step);
        double newStepV = (vmax - vmin) / (numSamplesV - 1);
        TriVector[][] surf = new TriVector[numSamplesU][numSamplesV];
        for (int i = 0; i < numSamplesU; i++) {
            for (int j = 0; j < numSamplesV; j++) {
                double u = umin + i * newStepU;
                double v = vmin + j * newStepV;
                Double[] uv = {u, v};
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
                objFile += "f" + " " + index + " " + (index + numSamplesV) + " " + (index + numSamplesV + 1) + "\n";
                objFile += "f" + " " + index + " " + (index + numSamplesV + 1) + " " + (index + 1) + "\n";
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
        panel.transferFocusUpCycle();
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
        tMinTxt.setText("" + (tmin - mRotation));
        tMaxTxt.setText("" + (tmax + mRotation));
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
            if (drawAxis) {
                buildAxis();
            }
            if (drawFunction) {
                if (comboBoxS.getSelectedItem() == "Surface")
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
         * updateAnimation light
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
            step = numericRead(stepTxtL.getText());
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
                step = numericRead(stepTxtS.getText());
                nt = Math.abs(tmax - tmin) / (step);
            }

            inT = (int) Math.floor(nt);
            line = new TriVector[inT + 1];
            for (int i = 0; i < inT; i++) {
                if (line[i] == null) {
                    t = tmin + i * step;
                    Double[] tvar = {t};
                    x = xTFunc.compute(tvar);
                    y = yTFunc.compute(tvar);
                    z = zTFunc.compute(tvar);
                    line[i] = new TriVector(x, y, z);
                }
                if (line[i + 1] == null) {
                    t = tmin + (i + 1) * step;
                    Double[] tvar = {t};
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
}
