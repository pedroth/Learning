package window;

public abstract class Window2D {
    protected double x_min;
    protected double x_max;
    protected double y_min;
    protected double y_max;

    public Window2D() {
    }

    public Window2D(double xMin, double xMax, double yMin, double yMax) {
        x_min = xMin;
        x_max = xMax;
        y_min = yMin;
        y_max = yMax;
    }

    public abstract void drawLine(double x1, double y1, double x2, double y2);

    public abstract void drawFilledRectangle(double x, double y, double width,
                                             double height);

    public abstract void drawFilledParallelogram(double x0, double y0,
                                                 double x1, double y1, double x2, double y2); // (x0,y0) is a
    // starting point,
    // (x1,y1) and
    // (x2,y2) are two
    // vectors, the
    // parallelogram has
    // the following
    // vertices (x0,y0),
    // (x1,y1), (x2,y2),
    // (x1+x2,y1+y2)

    public abstract void drawFilledTriangle(double x0, double y0, double x1,
                                            double y1, double x2, double y2); // triangle with the following
    // vertices (x0,y0), (x1,y1),
    // (x2,y2);

    public abstract void drawString(String s, double x1, double y1);

    public abstract void drawPoint(double x1, double y1, int pxlRadius);

    public abstract void drawFilledQuadrilateral(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3);

    public double getXMin() {
        return x_min;
    }

    public double getXMax() {
        return x_max;
    }

    public double getYMin() {
        return y_min;
    }

    public double getYMax() {
        return y_max;
    }

    public void setViewWindow(double xmin, double xmax, double ymin, double ymax) {
        x_min = xmin;
        x_max = xmax;
        y_min = ymin;
        y_max = ymax;
    }
}