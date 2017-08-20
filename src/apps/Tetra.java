package apps;

import algebra.Matrix;
import algebra.TriVector;
import visualization.ObjParser;
import visualization.TextFrame;
import windowThreeDim.Composite;
import windowThreeDim.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Tetra extends JFrame implements MouseListener,
        MouseMotionListener, KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String helpText = "< w > : Camera move foward / zoom in \n\n" +
            "< s > : Camera move backward /zoom out \n\n" +
            "< z > : Toggle wireframe / zbuffer \n\n" +
            "< [1-6] > : various geometries \n\n" +
            "< 7 > : sphere flow\n\n" +
            "< 8 > : distance to camera shader\n\n" +
            "< mouse > : rotate camera\n\n" +
            "Made by Pedroth";
    private int wChanged, hChanged;
    private TriWin graphics;
    private double theta, phi;
    private int mx, my, newMx, newMy;
    private double raw;
    private double velocity;
    private double acceleration;
    private double oldTime;
    private double currentTime;
    private Timer timer;
    private double thrust;
    private boolean zBufferOn;
    private PaintMethod paint;
    private FrameCounter fps;
    private Optional<Composite> figure = Optional.empty();
    private boolean isSphereFlow = false;

    public Tetra(boolean isApplet) {
        // Set JFrame title
        super("Draw Tetra");

        // init
        zBufferOn = true;
        graphics = new TriWin();
        paint = new ZBufferPerspective();
        graphics.setMethod(paint);
        buildTetra();
        graphics.getBuffer().setBackGroundColor(Color.white);
        theta = 0;
        phi = 0;

        // Set default close operation for JFrame
        if (!isApplet) {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        // Set JFrame size
        setSize(800, 550);

        wChanged = this.getWidth();
        hChanged = this.getHeight();

        graphics.getBuffer().setWindowSize(wChanged, hChanged);

        // Make JFrame visible
        setVisible(true);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(this);
        raw = 3;
        velocity = 0;
        acceleration = 0;
        oldTime = (System.currentTimeMillis()) * 1E-03;
        timer = new Timer();
        thrust = 0;
        fps = new FrameCounter();
        timer.schedule(fps, 0, 1000);
        orbit(theta, phi);
    }

    public static void main(String[] args) {
        new Tetra(false);
    }

    private void buildTetra() {
        Composite composite = new Composite();
        TriVector p1 = new TriVector(0.5, -0.5, -0.5);
        TriVector p2 = new TriVector(0.5, 0.5, -0.5);
        TriVector p3 = new TriVector(0, 0, 1);
        Element e = new Triangle(p1, p2, p3);
        e.setColor(Color.black);
        composite.add(e);
        p1 = new TriVector(0.5, 0.5, -0.5);
        p2 = new TriVector(-0.5, 0.5, -0.5);
        p3 = new TriVector(0, 0, 1);
        e = new Triangle(p1, p2, p3);
        e.setColor(Color.blue);
        composite.add(e);
        p1 = new TriVector(-0.5, 0.5, -0.5);
        p2 = new TriVector(-0.5, -0.5, -0.5);
        p3 = new TriVector(0, 0, 1);
        e = new Triangle(p1, p2, p3);
        e.setColor(Color.red);
        composite.add(e);
        p1 = new TriVector(-0.5, -0.5, -0.5);
        p2 = new TriVector(0.5, -0.5, -0.5);
        p3 = new TriVector(0, 0, 1);
        e = new Triangle(p1, p2, p3);
        e.setColor(Color.green);
        composite.add(e);
        e = new Quad(new TriVector(-0.5, -0.5, -0.5), new TriVector(0.5, -0.5, -0.5), new TriVector(0.5, 0.5, -0.5), new TriVector(-0.5, 0.5, -0.5));
        e.setColor(Color.YELLOW);
        composite.add(e);
        graphics.addtoList(composite);
        graphics.setMethod(new ZBufferPerspective());
        figure = Optional.of(composite);
        raw = 3.0;
    }

    private void buildTri() {
        Random r = new Random();
        int n = 10;
        Composite composite = new Composite();
        for (int i = 0; i < n; i++) {
            TriVector p1 = RandomPointInCube();
            TriVector p2 = RandomPointInCube();
            TriVector p3 = RandomPointInCube();
            Element e = new Triangle(p1, p2, p3);
            e.setColor(new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
            composite.add(e);
        }
        graphics.addtoList(composite);
        graphics.setMethod(new ZBufferPerspective());
        figure = Optional.of(composite);
        raw = 3;
    }

    private void buildCube() {
        Composite composite = new Composite();
        TriVector p1 = new TriVector(0.5, -0.5, -0.5);
        TriVector p2 = new TriVector(0.5, 0.5, -0.5);
        TriVector p3 = new TriVector(0.5, 0.5, 0.5);
        TriVector p4 = new TriVector(0.5, -0.5, 0.5);
        Element e = new Quad(p1, p2, p3, p4);
        e.setColor(Color.black);
        composite.add(e);
        p1 = new TriVector(0.5, 0.5, -0.5);
        p2 = new TriVector(-0.5, 0.5, -0.5);
        p3 = new TriVector(-0.5, 0.5, 0.5);
        p4 = new TriVector(0.5, 0.5, 0.5);
        e = new Quad(p1, p2, p3, p4);
        e.setColor(Color.blue);
        composite.add(e);
        p1 = new TriVector(-0.5, 0.5, -0.5);
        p2 = new TriVector(-0.5, 0.5, 0.5);
        p3 = new TriVector(-0.5, -0.5, 0.5);
        p4 = new TriVector(-0.5, -0.5, -0.5);
        e = new Quad(p1, p2, p3, p4);
        e.setColor(Color.red);
        composite.add(e);
        p1 = new TriVector(-0.5, -0.5, -0.5);
        p2 = new TriVector(-0.5, -0.5, 0.5);
        p3 = new TriVector(0.5, -0.5, 0.5);
        p4 = new TriVector(0.5, -0.5, -0.5);
        e = new Quad(p1, p2, p3, p4);
        e.setColor(Color.green);
        composite.add(e);
        p1 = new TriVector(0.5, -0.5, -0.5);
        p2 = new TriVector(0.5, 0.5, -0.5);
        p3 = new TriVector(-0.5, 0.5, -0.5);
        p4 = new TriVector(-0.5, -0.5, -0.5);
        e = new Quad(p1, p2, p3, p4);
        e.setColor(Color.gray);
        composite.add(e);
        p1 = new TriVector(0.5, -0.5, 0.5);
        p2 = new TriVector(0.5, 0.5, 0.5);
        p3 = new TriVector(-0.5, 0.5, 0.5);
        p4 = new TriVector(-0.5, -0.5, 0.5);
        e = new Quad(p1, p2, p3, p4);
        e.setColor(Color.orange);
        composite.add(e);
        graphics.addtoList(composite);
        figure = Optional.of(composite);
        graphics.setMethod(new ZBufferPerspective());
        raw = 3.0;
    }

    private TriVector RandomPointInCube() {
        Random r = new Random();
        double x[] = new double[3];
        for (int i = 0; i < 3; i++) {
            x[i] = -1 + 2 * r.nextDouble();
        }
        return new TriVector(x[0], x[1], x[2]);
    }

    public void paint(Graphics g) {
        if (Math.abs(wChanged - this.getWidth()) > 0
                || Math.abs(hChanged - this.getHeight()) > 0) {
            graphics.setWindowSize(this.getWidth(), this.getHeight());
            wChanged = this.getWidth();
            hChanged = this.getHeight();
        }
        update(g);
    }

    public void update(Graphics g) {
        graphics.getBuffer().clearImageWithBackGround();
        graphics.drawElements();
        run();
        graphics.getBuffer().paint(g);
        fps.count();
    }

    public void orbit(double t, double p) {

        Matrix aux = new Matrix(3, 3);
        // z - axis
        aux.setMatrix(1, 3, -Math.cos(p) * Math.cos(t));
        aux.setMatrix(2, 3, -Math.cos(p) * Math.sin(t));
        aux.setMatrix(3, 3, -Math.sin(p));
        // y - axis
        aux.setMatrix(1, 2, -Math.sin(p) * Math.cos(t));
        aux.setMatrix(2, 2, -Math.sin(p) * Math.sin(t));
        aux.setMatrix(3, 2, Math.cos(p));
        // x -axis
        aux.setMatrix(1, 1, -Math.sin(t));
        aux.setMatrix(2, 1, Math.cos(t));
        aux.setMatrix(3, 1, 0);
        TriVector eye = new TriVector(raw * Math.cos(p) * Math.cos(t), raw
                * Math.cos(p) * Math.sin(t), raw * Math.sin(p));

        graphics.setCamera(aux, eye);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        newMx = e.getX();
        newMy = e.getY();
        double dx = newMx - mx;
        double dy = newMy - my;
        theta += 2 * Math.PI * (dx / wChanged);
        phi += 2 * Math.PI * (dy / hChanged);

        orbit(theta, phi);
        mx = newMx;
        my = newMy;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
        mx = e.getX();
        my = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        isSphereFlow = false;
        if (arg0.getKeyCode() == KeyEvent.VK_W) {
            thrust = -5;
        } else if (arg0.getKeyCode() == KeyEvent.VK_S) {
            thrust = +5;
        } else if (arg0.getKeyCode() == KeyEvent.VK_1) {
            graphics.removeAllElements();
            buildTetra();
        } else if (arg0.getKeyCode() == KeyEvent.VK_2) {
            graphics.removeAllElements();
            buildTri();
        } else if (arg0.getKeyCode() == KeyEvent.VK_3) {
            graphics.removeAllElements();
            buildCube();
        } else if (arg0.getKeyCode() == KeyEvent.VK_4) {
            graphics.removeAllElements();
            buildBunny();
        } else if (arg0.getKeyCode() == KeyEvent.VK_5) {
            graphics.removeAllElements();
            buildKakashi();
        } else if (arg0.getKeyCode() == KeyEvent.VK_6) {
            graphics.removeAllElements();
            buildSonic();
        } else if (arg0.getKeyCode() == KeyEvent.VK_7) {
            isSphereFlow = true;
        } else if (arg0.getKeyCode() == KeyEvent.VK_PLUS) {
            graphics.setAlpha(graphics.getAlpha() + Math.PI / 32);
        } else if (arg0.getKeyCode() == KeyEvent.VK_MINUS) {
            graphics.setAlpha(graphics.getAlpha() - Math.PI / 32);
        } else if (arg0.getKeyCode() == KeyEvent.VK_8) {
            final ZbufferShader method = new ZbufferShader();
            method.setCullBack(true);
            graphics.setMethod(method);
        } else if (arg0.getKeyCode() == KeyEvent.VK_Z) {
            zBufferOn = !zBufferOn;
            if (zBufferOn) {
                graphics.setMethod(new ZBufferPerspective());
            } else {
                graphics.setMethod(new WiredPrespective());
            }
        }
        orbit(theta, phi);
    }

    private void buildSonic() {
        ObjParser obj = new ObjParser("https://sites.google.com/site/ibplanalto2010/Home/Sonic.obj?attredirects=0&d=1");
        final Composite composite = obj.parse();
        figure = Optional.of(composite);
        double scale = 1;
        double[][] m = {{scale, 0, 0}, {0, scale, 0}, {0, 0, scale}};
        composite.transform(new Matrix(m), TriVector.multConst(-1, composite.centroid()));
        graphics.addtoList(composite);
        addFlatShader();
        raw = 20;
    }

    private void addFlatShader() {
        FlatShader shader = new FlatShader();
        shader.setCullBack(true);
        shader.addLightPoint(new TriVector(3, 3, 3));
        graphics.setMethod(shader);
    }

    private void buildKakashi() {
        ObjParser obj = new ObjParser("https://sites.google.com/site/ibplanalto2010/Home/Kakashi.obj?attredirects=0&d=1");
        final Composite composite = obj.parse();
        figure = Optional.of(composite);
        double scale = 1;
        double[][] m = {{scale, 0, 0}, {0, scale, 0}, {0, 0, scale}};
        composite.transform(new Matrix(m), TriVector.multConst(-1, composite.centroid()));
        graphics.addtoList(composite);
        addFlatShader();
        raw = 3.0;
    }

    private void buildBunny() {
        ObjParser obj = new ObjParser("http://graphics.stanford.edu/~mdfisher/Data/Meshes/bunny.obj");
        final Composite composite = obj.parse();
        figure = Optional.of(composite);
        composite.forEach(x -> x.setColor(Color.getHSBColor((float) Math.random(), 1.0f, 1.0f)));
        double scale = 10;
        double[][] m = {{scale, 0, 0}, {0, scale, 0}, {0, 0, scale}};
        composite.transform(new Matrix(m), TriVector.multConst(-1, composite.centroid()));
        graphics.addtoList(composite);
        addFlatShader();
        raw = 3.0;
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

    public void run() {
        currentTime = (System.currentTimeMillis()) * 1E-03;
        double dt = currentTime - oldTime;
        oldTime = currentTime;
        acceleration = -velocity + thrust;
        velocity += acceleration * dt;
        raw += velocity * dt;
        if (figure.isPresent() && isSphereFlow) {
            final Composite composite = figure.get();
            final TriVector centroid = composite.centroid();
            final double std = composite.getDistanceStandardDeviation();
            composite.forEach(x -> {
                for (int i = 0; i < 3; i++) {
                    TriVector nPoint = x.getNPoint(i);
                    final TriVector sub = TriVector.sub(centroid, nPoint);
                    final double length = sub.getLength();
                    sub.normalize();
                    final TriVector sum = TriVector.sum(nPoint, TriVector.multConst(dt * (length - std), sub));
                    nPoint.setXYZMat(sum);
                }
            });
        }
        orbit(theta, phi);
        repaint();
    }

    public class FrameCounter extends TimerTask {
        private int nFrames;

        public FrameCounter() {
            nFrames = 0;
        }

        @Override
        public void run() {
            String s = "Tetra FPS : " + nFrames + " - Press h for Help";
            Tetra.this.setTitle(s);
            nFrames = 0;
        }

        public void count() {
            nFrames++;
        }

    }
}
