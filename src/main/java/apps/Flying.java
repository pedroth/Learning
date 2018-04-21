package apps;

import algebra.Matrix;
import algebra.TriVector;
import visualization.TextFrame;
import visualization.ThreeUtils;
import windowThreeDim.Composite;
import windowThreeDim.FlatShader;
import windowThreeDim.TriWin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Flying extends JFrame implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
    private final static TextFrame HELP_FRAME = TextFrame.builder()
            .addLine("< left mouse button > : change orientation of camera")
            .addLine("< right mouse button > : zoom in / zoom out")
            .addLine("< [w, s] ans arrows > : move ball")
            .addLine("< t > : toggle space state")
            .addLine("Made by Pedroth")
            .buildWithTitle("Help");
    /**
     * size of the screen
     */
    private int wChanged, hChanged;
    /**
     * 3D Engine
     */
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
        // init
        this.setLayout(null);
        /**
         * begin the engine
         */
        graphics = new TriWin(Math.PI / 2);
        shader = new FlatShader();
        shader.setCullBack(false);
        shader.setPowerDecay(1.0);
        graphics.setMethod(shader);
        graphics.getBuffer().setBackGroundColor(new Color(0.9f, 0.9f, 0.9f));

        //Set default close operation for JFrame
        if (!isApplet) {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        // Set JFrame size
        this.setSize(800, 600);

        wChanged = this.getWidth();
        hChanged = this.getHeight();

        oldTime = (System.currentTimeMillis()) * 1E-03;

        graphics.getBuffer().setWindowSize(wChanged, hChanged);

        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);

        shader.setAmbientLightParameter(0.5);
        shader.setShininess(25);
        shader.addLightPoint(new TriVector(1, 1, 1));
        orbit(theta, phi, 0);

        buildWorld();

        Composite composite = ThreeUtils.buildUnitaryCube(Color.RED);
        Matrix matrix = new Matrix(3, 3);
        matrix.identity();
        matrix.setMatrix(3, 3, 25);
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
        int samples = 10;
        for (int i = 0; i < samples; i++) {
            for (int j = 0; j < samples; j++) {
                for (int k = 0; k < samples; k++) {
                    double x1 = xmin + (xmax - xmin) * (1.0 * i / (samples - 1));
                    double x2 = xmin + (xmax - xmin) * (1.0 * j / (samples - 1));
                    double x3 = xmin + (xmax - xmin) * (1.0 * k / (samples - 1));
                    if (Math.random() < 0.1) {
                        TriVector x = new TriVector(x1, x2, x3);
                        Composite composite = null;
                        if (Math.random() < 0.5) {
                            composite = ThreeUtils.buildUnitaryCube(Color.RED);
                        } else {
                            composite = ThreeUtils.buildSphere(1.0, 10, Color.RED);
                        }
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

    public void paint(Graphics g) {
        if (Math.abs(wChanged - this.getWidth()) > 0 || Math.abs(hChanged - this.getHeight()) > 0) {
            wChanged = this.getWidth();
            hChanged = this.getHeight();
            graphics.setWindowSize(this.getWidth(), this.getHeight());
        }
        update(g);
    }

    public void update(Graphics g) {
        graphics.getBuffer().clearImageWithBackGround();
        currentTime = (System.currentTimeMillis()) * 1E-03;
        double dt = currentTime - oldTime;
        oldTime = currentTime;
        orbit(theta, phi, dt);
        graphics.drawElements();
        graphics.getBuffer().paint(g);
        setTitle("FPS " + Math.floor(1.0 / Math.max(dt, 1E-12)));
        repaint();
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
        if (keyCode == KeyEvent.VK_PLUS) {
            graphics.setAlpha(graphics.getAlpha() + Math.PI / 32);
        }
        if (keyCode == KeyEvent.VK_MINUS) {
            graphics.setAlpha(graphics.getAlpha() - Math.PI / 32);
        }
        eyeThrust = ans;
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        if (arg0.getKeyCode() == KeyEvent.VK_H) {
           HELP_FRAME.setVisible(true);
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
