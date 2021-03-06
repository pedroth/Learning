package visualization;

import algebra.Matrix;
import algebra.TriVector;
import inputOutput.MyImage;
import window.ImageWindow;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * @author pedro Cristovao
 * <p>
 * dataX, dataY, colorList and states must have the same size
 */
public class Graph2DFrame extends JFrame {
    protected ImageWindow wd;
    private Double[][] hueMatrix;
    private Rectangle matrixSpace;
    private List<double[]> dataX;
    private List<double[]> dataY;
    private List<Color> colorList;
    private boolean isGrayScale;
    private boolean isSmooth;
    private boolean isRepainting;
    /**
     * state 0 -> Curve state 1 -> Points
     */
    private List<Integer> states;
    private Rectangle regions;
    private int wChanged, hChanged;

    public Graph2DFrame(String title) {
        super(title);
        // Set JFrame size
        setSize(800, 550);

        wChanged = this.getWidth();
        hChanged = this.getHeight();

        wd = new ImageWindow(-1, 1, -1, 1);
        wd.setWindowSize(wChanged, hChanged);
        wd.setBackGroundColor(Color.white);
        wd.clearImageWithBackGround();

        // Set default close operation for JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        dataX = new ArrayList<double[]>();
        dataY = new ArrayList<double[]>();
        colorList = new ArrayList<Color>();
        states = new ArrayList<Integer>();
        regions = new Rectangle(0, 0, 0, 0);
        // Make JFrame visible
        setVisible(false);
        setGrayScale(false);
        setSmooth(true);
    }

    public static void test0(Graph2DFrame frame) {
        String str = "sinsinsin";
        double[] x = new double[str.length()];
        double[] y = new double[str.length()];
        for (int i = 0; i < y.length; i++) {
            x[i] = (1.0 * i) / y.length;
            y[i] = str.charAt(i) / (1.0 * Character.MAX_VALUE);
        }
        frame.addCurve(x, y, Color.blue);
        str = "sinsinsinsinsinsin";
        x = new double[str.length()];
        y = new double[str.length()];
        for (int i = 0; i < y.length; i++) {
            x[i] = (1.0 * i) / y.length;
            y[i] = str.charAt(i) / (1.0 * Character.MAX_VALUE);
        }
        frame.addCurve(x, y, Color.red);
        frame.plot();
    }

    public static void test1(Graph2DFrame frame) {
        MyImage immaculata = new MyImage("https://upload.wikimedia.org/wikipedia/commons/thumb/7/72/Miraculous_medal.jpg/140px-Miraculous_medal.jpg");
        /**
         * gray scale matrix
         */
        Matrix v = new Matrix(immaculata.getGrayScale());
        /**
         * add matrix
         */
        frame.setGrayScale(true);
        frame.setSmooth(true);
        frame.setRepainting(false);
        frame.addMatrix(v.getMatrix(), -1, 1, -1, 1);
        frame.plot();
    }

    public static void test2(Graph2DFrame frame) {
        /**
         * matrix function
         */
        int n = 100;
        Matrix v = new Matrix(n, n);
        v.identity();
        v.fillRandomInt(0, 10);
        frame.addMatrix(v.getMatrix(), -1, 1, -1, 1);
        frame.setGrayScale(false);
        frame.setSmooth(true);
        frame.setRepainting(false);
        frame.plot();
    }

    public static void test3(Graph2DFrame frame) {
        /**
         * matrix function
         */
        int n = 100;
        Matrix v = new Matrix(n, n);
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++)
                v.setMatrix(i, j, (i - n / 2) * (j - n / 2));
        }

        frame.addMatrix(v.getMatrix(), -1, 1, -1, 1);
        frame.setGrayScale(false);
        frame.setSmooth(true);
        frame.setRepainting(false);
        frame.plot();
    }

    /**
     * http://www.johndcook.com/blog/cpp_phi/
     *
     * @param x
     * @return
     */
    private static double phi(double x) {
        // constants
        double a1 = 0.254829592;
        double a2 = -0.284496736;
        double a3 = 1.421413741;
        double a4 = -1.453152027;
        double a5 = 1.061405429;
        double p = 0.3275911;

        // Save the sign of x
        int sign = 1;
        if (x < 0)
            sign = -1;
        x = Math.abs(x) / Math.sqrt(2.0);

        // A&S formula 7.1.26
        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);

        return 0.5 * (1.0 + sign * y);
    }

    private static double kernel(double x, double myu, double sigma) {
        double normalization = phi((1 - myu) / sigma) - phi(-myu / sigma);
        double gaussian = (1 / (Math.sqrt(2 * Math.PI) * sigma)) * Math.exp(-0.5 * ((x - myu) / sigma) * ((x - myu) / sigma));
        return gaussian / normalization;
    }

    private static void test4(Graph2DFrame frame) {
        int n = 100;
        double[] x = new double[n];
        double[] y = new double[n];
        double t = 0;
        double step = 1.0 / (n - 1);
        for (int i = 0; i < n; i++) {
            x[i] = t;
            y[i] = kernel(t, 0.02, 0.1);
            t += step;
        }
        frame.addCurve(x, y, Color.blue);
        frame.plot();
    }

    private static void test5(Graph2DFrame frame) {
        MyImage immaculatta = new MyImage("https://upload.wikimedia.org/wikipedia/commons/thumb/7/72/Miraculous_medal.jpg/140px-Miraculous_medal.jpg");
        /**
         * hsv matrix
         */
        TriVector[][] k = immaculatta.getHSVImageMatrix();
        Matrix v = new Matrix(k.length, k[0].length);
        for (int i = 1; i <= k.length; i++) {
            for (int j = 1; j <= k[0].length; j++) {
                double x = k[i - 1][j - 1].getX();
                x = x > 0.05 && x < 0.2 ? 0 : 1;
                v.setMatrix(i, j, x);
            }
        }
        /**
         * add matrix
         */
        frame.setGrayScale(true);
        frame.setSmooth(true);
        frame.setRepainting(false);
        frame.addMatrix(v.getMatrix(), -1, 1, -1, 1);
        frame.plot();
    }

    public static void main(String args[]) {
        List<Consumer<Graph2DFrame>> lambdas = Arrays.asList(
                Graph2DFrame::test0,
                Graph2DFrame::test1,
                Graph2DFrame::test2,
                Graph2DFrame::test3,
                Graph2DFrame::test4,
                Graph2DFrame::test5
        );
        IntStream.range(0, lambdas.size()).forEach(i -> lambdas.get(i).accept(new Graph2DFrame(String.format("figure %d", i))));
    }

    public boolean isRepainting() {
        return isRepainting;
    }

    public void setRepainting(boolean isRepainting) {
        this.isRepainting = isRepainting;
    }

    public boolean isGrayScale() {
        return isGrayScale;
    }

    public void setGrayScale(boolean isGrayScale) {
        this.isGrayScale = isGrayScale;
    }

    public boolean isSmooth() {
        return isSmooth;
    }

    public void setSmooth(boolean isSmooth) {
        this.isSmooth = isSmooth;
    }

    public ImageWindow getWd() {
        return wd;
    }

    public void addCurve(double[] x, double[] y, Color c) {
        states.add(0);
        addXYData(x, y, c);
    }

    public void addScatterData(double[] x, double[] y, Color c) {
        states.add(1);
        addXYData(x, y, c);
    }

    /**
     * @param x x-coordinate of data point
     * @param y y-coordinate of data point
     * @param c color
     *          <p>
     *          length of x and y must be equal
     */
    private void addXYData(double[] x, double[] y, Color c) {
        double maxX = 0, minX = 0, maxY = 0, minY = 0;
        if (x.length != y.length) {
            System.out.println("x and y must be equal in length");
            return;
        }
        for (int i = 0; i < x.length; i++) {
            double auxX = x[i];
            double auxY = y[i];
            if (!Double.isInfinite(auxX) && !Double.isNaN(auxX)) {
                maxX = Math.max(auxX, maxX);
                minX = Math.min(auxX, minX);
            }
            if (!Double.isInfinite(auxY) && !Double.isNaN(auxY)) {
                maxY = Math.max(auxY, maxY);
                minY = Math.min(auxY, minY);
            }
        }
        dataX.add(x);
        dataY.add(y);
        regions.max(new Rectangle(minX, minY, maxX, maxY));
        colorList.add(c);
    }

    private void plotCurve(double[] x, double[] y, Color c) {
        wd.setDrawColor(c);
        for (int i = 0; i < x.length - 1; i++) {
            wd.drawLine(x[i], y[i], x[i + 1], y[i + 1]);
        }
    }

    private void plotCloud(double[] x, double[] y, Color c) {
        wd.setDrawColor(c);
        for (int i = 0; i < x.length; i++) {
            wd.drawPoint(x[i], y[i], 5);
        }
    }

    public void plot() {
        setVisible(true);
        Iterator<double[]> xIte = dataX.iterator();
        Iterator<double[]> yIte = dataY.iterator();
        Iterator<Color> cIte = colorList.iterator();
        Iterator<Integer> sIte = states.iterator();
        double deltaX = 0.1 * (regions.getXmax() - regions.getXmin());
        double deltaY = 0.1 * (regions.getYmax() - regions.getYmin());
        wd.setViewWindow(regions.getXmin() - deltaX, regions.getXmax() + deltaX, regions.getYmin() - deltaY, regions.getYmax() + deltaY);
        if (hueMatrix != null)
            drawMatrix();
        while (xIte.hasNext()) {
            Color c = cIte.next();
            double[] x = xIte.next();
            double[] y = yIte.next();
            int state = sIte.next();
            if (state == 0) {
                plotCurve(x, y, c);
            } else if (state == 1) {
                plotCloud(x, y, c);
            }
        }
        drawAxis();
        if (isRepainting) {
            repaint();
        }
    }

    public void addMatrix(double[][] m, double xmin, double xmax, double ymin, double ymax) {
        double red = 0;
        double blue = 240.0 / 360.0;
        if (isGrayScale()) {
            red = 1.0;
            blue = 0.0;
        }
        matrixSpace = new Rectangle(xmin, ymin, xmax, ymax);
        regions.max(matrixSpace);
        double max = Double.MIN_VALUE, min = Double.MAX_VALUE;
        hueMatrix = new Double[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                double auxX = m[i][j];
                if (!Double.isInfinite(auxX) && !Double.isNaN(auxX)) {
                    max = Math.max(auxX, max);
                    min = Math.min(auxX, min);
                }
            }
        }
        double z;
        for (int i = 0; i < hueMatrix.length; i++) {
            for (int j = 0; j < hueMatrix[0].length; j++) {
                z = -1 + 2 * (m[i][j] - min) / (max - min);
                hueMatrix[i][j] = blue + (red - blue) * 0.5 * (z + 1);
            }
        }
    }

    private void drawRectangle(double x, double y, double dx, double dy, double colorI, double colorDx, double colorDy, double colorDxDy) {
        int i = wd.changeCoordX(x);
        int j = wd.changeCoordY(y);
        int w = wd.changeCoordX(x + dx) - wd.changeCoordX(x);
        int h = wd.changeCoordY(y + dy) - wd.changeCoordY(y);
        double df1 = (colorDx - colorI) / w;
        double df2 = (colorDxDy - colorDy) / w;
        Graphics g = wd.getGraphics();
        int[] pixels = ((java.awt.image.DataBufferInt) wd.getImage().getRaster().getDataBuffer()).getData();

        int windowWidth = wd.getWindowWidth();

        Color color;

        if (isSmooth) {
            for (int l = j; l > (j + h); l--) {
                for (int k = i; k < (i + w); k++) {
                    double g1 = colorI + df1 * (k - i);
                    double g2 = colorDy + df2 * (k - i);
                    double hue = g1 + ((g2 - g1) / h) * (l - j);
                    if (isGrayScale()) {
                        color = (new Color((float) hue, (float) hue, (float) hue));
                    } else {
                        color = (Color.getHSBColor((float) hue, 1, 1));
                    }
                    pixels[l * windowWidth + k] = color.getRGB();
                }
            }
        } else {
            if (isGrayScale()) {
                g.setColor(new Color((float) colorDy, (float) colorDy, (float) colorDy));
            } else {
                g.setColor(Color.getHSBColor((float) colorDy, 1, 1));
            }
            g.fillRect(i, j + h, w + 1, Math.abs(h) + 1);
        }

    }

    public void drawMatrix() {
        double xmax, xmin, ymin, ymax;
        xmax = matrixSpace.xmax;
        xmin = matrixSpace.xmin;
        ymin = matrixSpace.ymin;
        ymax = matrixSpace.ymax;
        double dx = (xmax - xmin) / hueMatrix[0].length;
        double dy = (ymax - ymin) / hueMatrix.length;
        for (int i = 0; i < hueMatrix.length; i++) {
            for (int j = 0; j < hueMatrix[0].length; j++) {
                double x = xmin + dx * j;
                double y = ymax - dy * (i + 1);
                drawRectangle(x, y, dx + wd.pxlXStep(), dy + wd.pxlYStep(), hueMatrix[Math.min(i + 1, hueMatrix.length - 1)][j], hueMatrix[Math.min(i + 1, hueMatrix.length - 1)][Math.min(j + 1, hueMatrix[0].length - 1)], hueMatrix[i][j], hueMatrix[i][Math.min(j + 1, hueMatrix[0].length - 1)]);
                // wd.setDrawColor(Color.black);
                // wd.drawLine(x, y, x + dx, y);
                // wd.drawLine(x+dx, y, x + dx, y + dy);
                // wd.drawLine(x+dx, y+dy, x, y + dy);
                // wd.drawLine(x, y+dy, x, y);
            }
        }
    }

    public void drawAxis() {
        wd.setDrawColor(Color.black);
        double deltaX = 0.1 * (regions.getXmax() - regions.getXmin());
        double deltaY = 0.1 * (regions.getYmax() - regions.getYmin());
        wd.drawLine(regions.getXmin(), regions.getYmin() - 0.5 * deltaY, regions.getXmax(), regions.getYmin() - 0.5 * deltaY);
        wd.drawLine(regions.getXmin() - 0.5 * deltaX, regions.getYmin(), regions.getXmin() - 0.5 * deltaX, regions.getYmax());
        for (double x = regions.getXmin(); x < regions.getXmax(); x += (regions.getXmax() - regions.getXmin()) / 10) {
            wd.drawString(String.format("%.2f", x), x, regions.getYmin() - 0.5 * deltaY);
        }
        for (double x = regions.getYmin(); x < regions.getYmax(); x += (regions.getYmax() - regions.getYmin()) / 10) {
            wd.drawString(String.format("%.2f", x), regions.getXmin() - 0.5 * deltaX, x);
        }
    }

    public void clear() {
        wd.clearImageWithBackGround();
        dataX.clear();
        dataY.clear();
        colorList.clear();
        regions = new Rectangle(0, 0, 0, 0);
        repaint();
    }

    public void paint(Graphics g) {
        if (Math.abs(wChanged - this.getWidth()) > 0 || Math.abs(hChanged - this.getHeight()) > 0) {
            wd.setWindowSize(this.getWidth(), this.getHeight());
            wChanged = this.getWidth();
            hChanged = this.getHeight();
        }
        update(g);
    }

    public void update(Graphics g) {
        wd.clearImageWithBackGround();
        plot();
        wd.paint(g);
    }

    public class Rectangle {
        private double xmin, ymax, xmax, ymin;

        public Rectangle(double xmin, double ymin, double xmax, double ymax) {
            super();
            this.xmin = xmin;
            this.ymax = ymax;
            this.xmax = xmax;
            this.ymin = ymin;
        }

        public double getXmin() {
            return xmin;
        }

        public void setXmin(double xmin) {
            this.xmin = xmin;
        }

        public double getYmax() {
            return ymax;
        }

        public void setYmax(double ymax) {
            this.ymax = ymax;
        }

        public double getXmax() {
            return xmax;
        }

        public void setXmax(double xmax) {
            this.xmax = xmax;
        }

        public double getYmin() {
            return ymin;
        }

        public void setYmin(double ymin) {
            this.ymin = ymin;
        }

        /**
         * set this rectangle to a rectangle that is the union of the two
         *
         * @param r
         * @return
         */
        public void max(Rectangle r) {
            this.setXmin(Math.min(this.getXmin(), r.getXmin()));
            this.setXmax(Math.max(this.getXmax(), r.getXmax()));
            this.setYmin(Math.min(this.getYmin(), r.getYmin()));
            this.setYmax(Math.max(this.getYmax(), r.getYmax()));
        }

    }

}
