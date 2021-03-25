package apps;

import algebra.Matrix;
import algebra.TriVector;
import visualization.ObjParser;
import visualization.TextFrame;
import visualization.ThreeUtils;
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
    private static final TextFrame HELP_FRAME = TextFrame.builder()
            .addLine("< w > : Camera move foward / zoom in")
            .addLine("< s > : Camera move backward /zoom out")
            .addLine("< z > : Toggle wireframe / zbuffer")
            .addLine("< [1-6] > : various geometries")
            .addLine("< 7 > : sphere flow")
            .addLine("< 8 > : distance to camera shader")
            .addLine("< 9 > : random planet")
            .addLine("< mouse > : rotate camera")
            .addLine("< +,- > : increase angle of view , decrease angle of view")
            .addLine("Made by Pedroth")
            .buildWithTitle("Help");
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
            buildRockerArm();
        } else if (arg0.getKeyCode() == KeyEvent.VK_6) {
            graphics.removeAllElements();
            buildSpot();
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
        } else if (arg0.getKeyCode() == KeyEvent.VK_9) {
            graphics.removeAllElements();
            BuildWorld();
        }
        orbit(theta, phi);
    }

    double powInt(double x, int n) {
        if (n == 0) {
            return 1;
        } else if (n == 1) {
            return x;
        } else {
            if (n % 2 == 0) {
                return powInt(x * x, n / 2);
            } else {
                return x * powInt(x * x, n / 2);
            }
        }
    }


    double fourier3D(TriVector x, double[] fourierCoeff) {
        int n = (int) Math.floor(Math.sqrt(fourierCoeff.length));
        double acc = 0;
        final double y = x.getY();
        final double xx = x.getX();
        double theta = Math.atan2(y, xx);
        double alpha = Math.atan2(y * y + xx * xx, x.getZ());
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                final int p = n * i + j;
                double w = i + j;
                acc += powInt(0.85, p) * fourierCoeff[p] * Math.sin(w * theta) * Math.sin(w * alpha);
            }
        }
        return acc;
    }

    private void BuildWorld() {
        int numOfFrequencies = 50;
        double[] fourierCoeff = new double[numOfFrequencies];
        for (int i = 0; i < fourierCoeff.length; i++) {
            fourierCoeff[i] = -0.25 + 0.5 * Math.random();
        }
        Composite sphere = ThreeUtils.buildSphere(1.0, 50, Color.RED);
        Composite water = ThreeUtils.buildSphere(0.75 + 0.5 * Math.random(), 20, Color.BLUE);
        sphere.forEach(element -> {
            final int numOfPoints = element.getNumOfPoints();
            final TriVector[] pointsArray = element.getPointsArray();
            for (int i = 0; i < numOfPoints; i++) {
                pointsArray[i] = TriVector.sum(pointsArray[i], TriVector.multConst(fourier3D(pointsArray[i], fourierCoeff), pointsArray[i]));
            }
        });
        final Double maxNorm = sphere.stream().map(e -> {
            final TriVector[] pointsArray = e.getPointsArray();
            final int numOfPoints = e.getNumOfPoints();
            double max = -1;
            for (int i = 0; i < numOfPoints; i++) {
                final double norm = pointsArray[i].norm();
                max = Math.max(max, norm);
            }
            return max;
        }).reduce(-1.0, Math::max);
        final Double minNorm = sphere.stream().map(e -> {
            final TriVector[] pointsArray = e.getPointsArray();
            final int numOfPoints = e.getNumOfPoints();
            double min = Double.MAX_VALUE;
            for (int i = 0; i < numOfPoints; i++) {
                final double norm = pointsArray[i].norm();
                min = Math.min(min, norm);
            }
            return min;
        }).reduce(Double.MAX_VALUE, Math::min);
        sphere.forEach(element -> {
            final int numOfPoints = element.getNumOfPoints();
            final TriVector[] pointsArray = element.getPointsArray();
            for (int i = 0; i < numOfPoints; i++) {
                final double norm = pointsArray[i].norm();
                element.setColorPoint(getWorldColor(norm, minNorm, maxNorm), i);
            }
        });
        figure = Optional.of(sphere);
        graphics.addtoList(sphere);
        graphics.addtoList(water);
        graphics.setMethod(new InterpolativeShader());
//        addFlatShader();
    }

    private Color getWorldColor(double x, Double minNorm, Double maxNorm) {
        final double z = (x - minNorm) / (maxNorm - minNorm);
        final TriVector color = linearInterpolate(z, new double[]{0, 0.75, 1}, new TriVector[]{new TriVector(0, 1, 0), new TriVector(0.54509807, 0.33333334, 0.11764706), new TriVector(1, 1, 1)});
        return new Color((float) color.getX(), (float) color.getY(), (float) color.getZ());
    }

    /**
     * @param x0
     * @param x  assumes it is a order array
     * @param y
     * @return
     */
    private TriVector linearInterpolate(double x0, double[] x, TriVector[] y) {
        assert x.length == y.length;
        assert x0 >= x[0] && x0 <= x[x.length - 1];
        int lower = 0;
        int upper = x.length - 1;
        int len = upper - lower;
        while(len > 1) {
            if(x[len / 2] >= x0) {
                upper = len / 2;
            }else {
                lower = len / 2;
            }
            len = upper - lower;
        }
        return TriVector.sum(TriVector.multConst((x[upper] - x0) / (x[upper] - x[lower]), y[lower]), TriVector.multConst((x0 - x[lower]) / (x[upper] - x[lower]), y[upper]));
    }

    private void buildSpot() {
        ObjParser obj = new ObjParser("https://raw.githubusercontent.com/alecjacobson/common-3d-test-models/master/data/spot.obj");
        final Composite composite = obj.parse();
        figure = Optional.of(composite);
        double scale = 1;
        double[][] m = {{scale, 0, 0}, {0, scale, 0}, {0, 0, scale}};
        composite.transform(new Matrix(m), TriVector.multConst(-1, composite.centroid()));
        graphics.addtoList(composite);
        addFlatShader();
        raw = 3;
    }

    private void addFlatShader() {
        FlatShader shader = new FlatShader();
        shader.setCullBack(true);
        shader.addLightPoint(new TriVector(3, 3, 3));
        graphics.setMethod(shader);
    }

    private void buildRockerArm() {
        ObjParser obj = new ObjParser("https://raw.githubusercontent.com/alecjacobson/common-3d-test-models/master/data/rocker-arm.obj");
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
            HELP_FRAME.setVisible(true);
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
                final int numOfPoints = x.getNumOfPoints();
                for (int i = 0; i < numOfPoints; i++) {
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
