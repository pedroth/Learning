package visualization;

import algebra.Matrix;
import algebra.TriVector;
import inputOutput.MyImage;
import inputOutput.TextIO;
import window.ImageWindow;
import windowThreeDim.Composite;
import windowThreeDim.*;
import windowThreeDim.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class Graph3DFrame extends JFrame implements MouseListener,
        MouseMotionListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private static final MyImage IMMACULATA = new MyImage("https://upload.wikimedia.org/wikipedia/commons/thumb/7/72/Miraculous_medal.jpg/140px-Miraculous_medal.jpg");
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
     * mouse coordinates
     */
    private int mx, my, newMx, newMy;
    /**
     * camera dynamics
     */
    /**
     * raw is 3-dim vector where 1st coordinate is distance to focalPoint 2nd
     * coordinate is theta 3rd coordinate is phi
     */
    private TriVector raw;
    /**
     * velRaw is the velocity of raw
     */
    private TriVector velRaw;
    /**
     * where the camera is looking
     */
    private TriVector focalPoint;
    /**
     * shader
     */
    private PaintMethod shader;
    private WiredPrespective wiredShader;
    /**
     * time variables
     */
    private double oldTime;
    private double currentTime;
    /**
     * thrust on (raw,theta,phi);
     */
    private TriVector thrust;
    /**
     * sphere surface
     */
    private TriVector[][] sphere;
    /**
     * frame counter
     */
    private FrameCounter fps;
    /**
     * title
     */
    private String title;
    /**
     * draw pxl instead of spheres in scatter plot
     */
    private boolean isScatterAsPxl;
    /**
     * drawAxis
     */
    private boolean isDrawAxis;
    private boolean axisAlreadyBuild;

    Graph3DFrame(String title) {
        super(title);
        this.title = title;
        /**
         * begin the engine
         */
        graphics = new TriWin(Math.PI / 64);
        wd = graphics.getBuffer();
        wiredShader = new WiredPrespective();
        FlatShader flatShader = new FlatShader();
        flatShader.setAmbientLightParameter(0.5);
        flatShader.setShininess(25);
        flatShader.addLightPoint(new TriVector(3, 3, 3));
        flatShader.setCullBack(false);
        shader = flatShader;
        graphics.setMethod(shader);
        wd.setBackGroundColor(Color.BLACK);

        // Set default close operation for JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set JFrame size
        setSize(800, 600);

        wChanged = this.getWidth();
        hChanged = this.getHeight();

        wd.setWindowSize(wChanged, hChanged);

        oldTime = (System.currentTimeMillis()) * 1E-03;
        thrust = new TriVector();
        raw = new TriVector();
        velRaw = new TriVector();

        focalPoint = new TriVector();
        // Make JFrame invisible
        setVisible(false);
        buildSphere(1);

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(this);

        Timer timer = new Timer();
        fps = new FrameCounter();
        timer.schedule(fps, 0, 1000);
        axisAlreadyBuild = false;
    }

    private static void test1(Graph3DFrame frame) {
        // IMACULATA as scatter point
        TriVector[] s = IMMACULATA.getRGBImageVector();

        frame.setScatterAsPxl(true);
        frame.addScatterData(s, Color.blue, 0.01);
        frame.raw.setX(30.0);
    }

    private static void test2(Graph3DFrame frame) {
        // IMACULATA as rgb surface
        TriVector[][] s = IMMACULATA.getRGBImageMatrix();
        frame.addSurface(s);
        frame.raw.setX(30.0);
    }

    private static void test3(Graph3DFrame frame) {
        // IMACULATA gray scale matrix
        Matrix v = new Matrix(IMMACULATA.getGrayScale());
        frame.addMatrix(v.getMatrix(), -1, 1, -1, 1);
    }

    private static void test4(Graph3DFrame frame) {
        // random points
        int n = 200;
        Random r = new Random();
        TriVector[] v = new TriVector[n];
        for (int i = 0; i < n; i++) {
            v[i] = new TriVector(r.nextDouble(), r.nextDouble(), r.nextDouble());
        }
        frame.addScatterData(v, Color.blue, 0.01);
        frame.addCurve(v, Color.red);
    }

    private static void test5(Graph3DFrame frame) {
        // tests
        frame.setShader(new InterpolativeShader());
        Triangle tri = new Triangle(new TriVector(1, 0, 0), new TriVector(0, 1, 0), new TriVector(0, 0, 1));
        tri.setColorPoint(Color.red, 0);
        tri.setColorPoint(Color.green, 1);
        tri.setColorPoint(Color.blue, 2);
        frame.raw.setX(2.0);
        frame.addElement(tri);
    }

    private static void test6(Graph3DFrame frame) {
        final List<ObjParser> objParsers = Arrays.asList(
                new ObjParser("http://graphics.stanford.edu/~mdfisher/Data/Meshes/bunny.obj")
        );
        int index = 0;
        Composite c = objParsers.get(index).parse();
        double scale = 1;
        double[][] m = {{scale, 0, 0}, {0, scale, 0}, {0, 0, scale}};
        c.transform(new Matrix(m), new TriVector());
        frame.addElement(c);
        frame.raw.setX(1.0);
        frame.focalPoint = c.centroid();
        FlatShader shader = new FlatShader();
        shader.setCullBack(true);
        shader.addLightPoint(new TriVector(-3, 3, -3));
        frame.setShader(shader);
    }

    private static void test7(Graph3DFrame frame) {
        int n = 100;
        TriVector[] p = new TriVector[n];
        for (int i = 0; i < p.length; i++) {
            p[i] = new TriVector();
            p[i].fillRandom(-1, 1);
            p[i].normalize();
        }
        frame.addScatterData(p, Color.blue, 0.01);

        TriVector[] v = new TriVector[n];
        TriVector normal = new TriVector(1, 1, 1);
        normal.normalize();
        for (int i = 0; i < p.length; i++) {
            Matrix aux = Matrix.subMatrix(p[i], TriVector.multiConsMatrix(TriVector.vInnerProduct(p[i], normal), normal));
            v[i] = new TriVector(aux.selMatrix(1, 1), aux.selMatrix(2, 1), aux.selMatrix(3, 1));
        }
        frame.addScatterData(v, Color.red, 0.01);

        TriVector[] r = new TriVector[n];
        TriVector normal2 = TriVector.vectorProduct(normal, new TriVector(1, 0, 0));
        normal2.normalize();
        for (int i = 0; i < p.length; i++) {
            Matrix aux = Matrix.subMatrix(v[i], TriVector.multiConsMatrix(TriVector.vInnerProduct(v[i], normal2), normal2));
            r[i] = new TriVector(aux.selMatrix(1, 1), aux.selMatrix(2, 1), aux.selMatrix(3, 1));
        }
        frame.addScatterData(r, Color.green, 0.01);
    }


    private static void test8(Graph3DFrame frame) {
        TextIO textIO = new TextIO("src/main/java/visualization/resource/data.txt");
        String text = textIO.getText();
        String[] split = text.split("\n");
        List<String[]> points = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            points.add(split[i].split("\t"));
        }
        TriVector[] points3D = new TriVector[points.get(0).length];
        for (int i = 0; i < points.get(0).length; i++) {
            points3D[i] = new TriVector(Double.parseDouble(points.get(0)[i]), Double.parseDouble(points.get(1)[i]), Double.parseDouble(points.get(2)[i]));
        }
        frame.addScatterData(points3D, Color.BLUE, 0.001);
    }

    public static void main(String[] args) {
        final List<Consumer<Graph3DFrame>> visualExperiments = Arrays.asList(
                Graph3DFrame::test1,
                Graph3DFrame::test2,
                Graph3DFrame::test3,
                Graph3DFrame::test4,
                Graph3DFrame::test5,
                Graph3DFrame::test6,
                Graph3DFrame::test7,
                Graph3DFrame::test8
        );
        final int EXPERIMENT = args.length > 0 ? Integer.parseInt(args[0]) : 5;
        Graph3DFrame frame = new Graph3DFrame(String.format("figure %d", EXPERIMENT));
        visualExperiments.get(EXPERIMENT).accept(frame);
        frame.plot();
    }

    public boolean isScatterAsPxl() {
        return isScatterAsPxl;
    }

    public void setScatterAsPxl(boolean isScatterAsPxl) {
        this.isScatterAsPxl = isScatterAsPxl;
    }

    public boolean isDrawAxis() {
        return isDrawAxis;
    }

    public void setDrawAxis(boolean isDrawAxis) {
        this.isDrawAxis = isDrawAxis;
    }

    public void plot() {
        setVisible(true);
        repaint();
    }

    public void addCurve(TriVector[] x, Color c) {
        for (int i = 0; i < x.length - 1; i++) {
            Element e = new Line(x[i], x[i + 1]);
            e.setColor(c);
            graphics.addtoList(e);
        }
    }

    /**
     * @param x      center position of the sphere
     * @param c      color of the sphere
     * @param radius >= 0;
     */
    public void addScatterData(TriVector[] x, Color c, double radius) {
        double maxX = Double.MIN_VALUE, minX = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE, minY = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE, minZ = Double.MAX_VALUE;
        for (int i = 0; i < x.length; i++) {
            maxX = Math.max(maxX, x[i].getX());
            maxY = Math.max(maxY, x[i].getY());
            maxZ = Math.max(maxZ, x[i].getZ());
            minX = Math.min(maxX, x[i].getX());
            minY = Math.min(maxY, x[i].getY());
            minZ = Math.min(maxZ, x[i].getZ());
            drawSphere(x[i], c, radius);
        }
        Double v[] = new Double[3];
        v[0] = (maxX + minX) / 2;
        v[1] = (maxY + minY) / 2;
        v[2] = (maxZ + minZ) / 2;
        focalPoint = TriVector.multConst(0.5,
                TriVector.sum(focalPoint, new TriVector(v[0], v[1], v[2])));
        raw.setX(Math.max(raw.getX(), 3 * maxX));
    }

    public void addSurface(TriVector[][] surface) {
        double maxX = Double.MIN_VALUE, minX = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE, minY = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE, minZ = Double.MAX_VALUE;
        int n = surface.length;
        int m = surface[0].length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
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
        for (int j = 0; j < m - 1; j++) {
            for (int i = 0; i < n - 1; i++) {
                Element e = new Quad(surface[i][j], surface[i + 1][j],
                        surface[i + 1][j + 1], surface[i][j + 1]);
                /**
                 * stand by solution
                 */

                double colorHSB;
                for (int k = 0; k < e.getNumOfPoints(); k++) {
                    double z = e.getNPoint(k).getZ();
                    colorHSB = heatColor(z, minZ, maxZ);
                    e.setColorPoint(Color.getHSBColor((float) colorHSB, 1f, 1f), k);
                }
                graphics.addtoList(e);
            }
        }

        Double v[] = new Double[3];
        v[0] = (maxX + minX) / 2;
        v[1] = (maxY + minY) / 2;
        v[2] = (maxZ + minZ) / 2;
        focalPoint = TriVector.multConst(0.5,
                TriVector.sum(focalPoint, new TriVector(v[0], v[1], v[2])));
        raw.setX(Math.max(raw.getX(), 3 * maxX));
    }

    public void addMatrix(double[][] m, double xmin, double xmax, double ymin,
                          double ymax) {

        Matrix v = new Matrix(m);

        TriVector[][] s = v.matrixToSurface(xmin, xmax, ymin, ymax);
        addSurface(s);
    }

    public double heatColor(double z, double min, double max) {
        double red = 0;
        double blue = 240.0 / 360.0;
        z = -1 + 2 * (z - min) / (max - min);
        return blue + (red - blue) * 0.5 * (z + 1);
    }

    public void buildSphere(double radius) {
        double pi = Math.PI;
        double step = pi / 2;
        double n = (2 * pi) / step;
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

    public void drawSphere(TriVector p, Color c, double radius) {
        if (!isScatterAsPxl) {
            double pi = Math.PI;
            double step = pi / 2;
            double n = (2 * pi) / step;
            int numIte = (int) Math.floor(n);
            for (int j = 0; j < numIte - 1; j++) {
                for (int i = 0; i < numIte - 1; i++) {
                    Quad e = new Quad(TriVector.sum(p,
                            TriVector.multConst(radius, sphere[i][j])),
                            TriVector.sum(p, TriVector.multConst(radius,
                                    sphere[i + 1][j])), TriVector.sum(p,
                            TriVector.multConst(radius,
                                    sphere[i + 1][j + 1])),
                            TriVector.sum(p, TriVector.multConst(radius,
                                    sphere[i][j + 1])));
                    e.setColor(c);
                    graphics.addtoList(e);

                }
            }
        } else {
            Point e = new Point(p);
            e.setColor(c);
            graphics.addtoList(e);
        }
    }

    public void paint(Graphics g) {
        if (Math.abs(wChanged - this.getWidth()) > 0
                || Math.abs(hChanged - this.getHeight()) > 0) {
            wd.setWindowSize(this.getWidth(), this.getHeight());
            wChanged = this.getWidth();
            hChanged = this.getHeight();
        }
        update(g);
    }

    public void update(Graphics g) {
        wd.clearImageWithBackGround();
        currentTime = (System.currentTimeMillis()) * 1E-03;
        double dt = currentTime - oldTime;
        oldTime = currentTime;
        euler(dt);
        if (isDrawAxis)
            buildAxis();
        graphics.drawElements();
        wd.paint(g);
        fps.count();
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

    private void euler(double dt) {
        dt = Math.min(dt, 0.05);
        double accX = (thrust.getX() - velRaw.getX());
        double accY = (thrust.getY() - velRaw.getY());
        double accZ = (thrust.getZ() - velRaw.getZ());
        velRaw.setX(velRaw.getX() + accX * dt);
        velRaw.setY(velRaw.getY() + accY * dt);
        velRaw.setZ(velRaw.getZ() + accZ * dt);
        raw.setX(raw.getX() + velRaw.getX() * dt + 0.5 * accX * dt * dt);
        raw.setY(raw.getY() + velRaw.getY() * dt + 0.5 * accY * dt * dt);
        raw.setZ(raw.getZ() + velRaw.getZ() * dt + 0.5 * accZ * dt * dt);
        orbit();
        repaint();
    }

    public void orbit() {

        double t = raw.getY();
        double p = raw.getZ();

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

        double r = raw.getX();
        TriVector eye = new TriVector(r * cosP * cosT + focalPoint.getX(), r
                * cosP * sinT + focalPoint.getY(), r * sinP + focalPoint.getZ());

        graphics.setCamera(aux, eye);
    }

    public void setShader(PaintMethod shader) {
        graphics.setMethod(shader);
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        if (arg0.getKeyCode() == KeyEvent.VK_Z)
            graphics.setMethod(wiredShader);
        if (arg0.getKeyCode() == KeyEvent.VK_A) {
            isDrawAxis = !isDrawAxis;
            axisAlreadyBuild = false;
        }
        if (arg0.getKeyCode() == KeyEvent.VK_S) {
            ZBufferPerspective s = new InterpolativeShader();
            s.setCullBack(true);
            graphics.setMethod(s);
        }
        if (arg0.getKeyCode() == KeyEvent.VK_F)
            graphics.setMethod(shader);
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub
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
            raw.setY(raw.getY() + 2 * Math.PI * (dx / wChanged));
            raw.setZ(raw.getZ() + 2 * Math.PI * (dy / hChanged));
            thrust.setY(40 * 2 * Math.PI * (dx / wChanged));
            thrust.setZ(40 * 2 * Math.PI * (dy / hChanged));
        } else {
            raw.setX(raw.getX() + (dx / wChanged) + (dy / hChanged));
            thrust.setX(90 * ((dx / wChanged) + (dy / hChanged)));
        }
        orbit();

        mx = newMx;
        my = newMy;

        repaint();
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
        thrust.setX(0);
        thrust.setY(0);
        thrust.setZ(0);
    }

    public void addElement(Element e) {
        graphics.addtoList(e);
    }

    public class FrameCounter extends TimerTask {
        private int nFrames;

        public FrameCounter() {
            nFrames = 0;
        }

        @Override
        public void run() {
            String s = title + "	 FPS : " + nFrames;
            Graph3DFrame.this.setTitle(s);
            nFrames = 0;
        }

        public void count() {
            nFrames++;
        }
    }
}
