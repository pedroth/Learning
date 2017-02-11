package apps;

import algebra.Matrix;
import algebra.TriVector;
import visualization.TextFrame;
import window.ImageWindow;
import windowThreeDim.Composite;
import windowThreeDim.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Flying extends JFrame implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
    private static String helpText = "< left mouse button > : change orientation of camera \n\n" + "< right mouse button > : zoom in / zoom out \n\n" + "< [w, s] ans arrows > : move ball \n\n" + "< t > : toggle space state \n\n" + "Made by Pedroth";
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
    private TriVector dEye = new TriVector();
    private TriVector eye = new TriVector(5, 0, 0);
    private TriVector eyeThrust = new TriVector();

    /**
     * mouse coordinates
     */
    private int mx, my, newMx, newMy, mRotation;

    /**
     * time
     */
    private double oldTime;
    private double currentTime;

    private FlatShader shader;


    public Flying(boolean isApplet) {
        // Set JFrame title
        super("Robot - Press h for Help");

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

        //Set default close operation for JFrame
        if (!isApplet) {
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
        shader.addLightPoint(new TriVector(10, 3, 3));
        orbit(theta, phi, 0);

        buildWorld();

        Composite composite = buildUnitaryCube(Color.RED);
        Matrix matrix = new Matrix(3, 3);
        matrix.identity();
        matrix.setMatrix(3,3, 25);
        composite.transform(matrix, new TriVector());
        graphics.addtoList(composite);

        /**
         * Make JFrame visible
         */
        setVisible(true);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new Flying(false);
    }

    private void buildWorld() {
        double xmin = -7;
        double xmax = 7;
        int samples = 20;
        for (int i = 0; i < samples; i++) {
            for (int j = 0; j < samples; j++) {
                for (int k = 0; k < samples; k++) {
                    double x1 = xmin + (xmax - xmin) * (1.0 * i / (samples - 1));
                    double x2 = xmin + (xmax - xmin) * (1.0 * j / (samples - 1));
                    double x3 = xmin + (xmax - xmin) * (1.0 * k / (samples - 1));
                    if (Math.random() < 0.1) {
                        TriVector x = new TriVector(x1, x2, x3);
                        Composite composite = buildUnitaryCube(Color.RED);
                        Matrix matrix = new Matrix(3, 3);
                        matrix.identity();
                        matrix.multiConstMatrix(0.5);
                        composite.transform(matrix, x);
                        graphics.addtoList(composite);
                    }
                }
            }
        }
    }

    public Composite buildSphere(double radius) {
        double pi = Math.PI;
        double step = pi / 16;
        double nV = 2 * pi / step;
        double nU = pi / step;
        int numIteV = (int) Math.floor(nV);
        int numIteU = (int) Math.floor(nU);
        TriVector[][] sphere = new TriVector[numIteU][numIteV];
        Composite ball = new Composite();
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
        return ball;
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
        orbit(theta, phi, dt);
        graphics.drawElements();
        wd.paint(g);
        repaint();
    }

    public double clamp(double x, double xmin, double xmax) {
        double ans = x;
        if (x < xmin) {
            ans = xmin;
        } else if (x > xmax) {
            ans = xmax;
        }
        return ans;
    }

    public void orbit(double t, double p, double dt) {

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

        TriVector thrustWorldCoord = TriVector.Transformation(aux, eyeThrust);

        eye = TriVector.sum(eye, TriVector.multConst(dt, dEye));
        dEye = TriVector.sum(dEye, TriVector.multConst(dt, TriVector.sub(thrustWorldCoord, dEye)));
        graphics.setCamera(aux, eye);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        int keyCode = arg0.getKeyCode();
        TriVector ans = new TriVector();
        double thrust = 10;
        if (keyCode == KeyEvent.VK_W) {
            ans = TriVector.sum(ans, new TriVector(0, 0, thrust));
        }
        if (keyCode == KeyEvent.VK_S) {
            ans = TriVector.sum(ans, new TriVector(0, 0, -thrust));
        }
        if (keyCode == KeyEvent.VK_A) {
            ans = TriVector.sum(ans, new TriVector(-thrust, 0, 0));
        }
        if (keyCode == KeyEvent.VK_D) {
            ans = TriVector.sum(ans, new TriVector(thrust, 0, 0));
        }
        eyeThrust = ans;
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        if (arg0.getKeyCode() == KeyEvent.VK_H) {
            new TextFrame("help", helpText);
        }
        eyeThrust = new TriVector();
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
            theta = theta - 2 * Math.PI * (dx / wChanged);
            phi = phi + 2 * Math.PI * (dy / hChanged);
        }

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
