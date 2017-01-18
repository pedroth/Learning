package windowThreeDim;

import algebra.Matrix;
import algebra.TriVector;
import window.ImageWindow;

public class WiredPrespective implements PaintMethod {
    private Matrix cameraBasis;
    private Matrix inverseCameraBasis;
    private TriVector eyePos;
    private ImageWindow wd;
    private boolean isInsideFustrum;
    /**
     * distance to drawing window/pane
     */
    private double d;

    private double zd;

    public WiredPrespective() {
        d = 1;
        zd = d / 256;
        isInsideFustrum = true;
    }

    /**
     * does not change p reference
     *
     * @param p : point being projected
     * @return projected point
     */
    public TriVector Projection(TriVector p) {
        Matrix m = TriVector.subMatrix(p, eyePos);
        TriVector res = new TriVector(0, 0, 0);
        res.setXYZMat(m);
        res.Transformation(inverseCameraBasis);

        if (res.getZ() < zd)
            isInsideFustrum = false;
        else
            isInsideFustrum = true;

		/* x = (x * d) / z */
        double x = (res.selMatrix(1, 1) * d) / res.selMatrix(3, 1);
        /* y = (y * d) / z */
        double y = (res.selMatrix(2, 1) * d) / res.selMatrix(3, 1);
        res.setMatrix(1, 1, x);
        res.setMatrix(2, 1, y);
        return res;
    }

    @Override
    public void paintQuad(Quad element) {
        paintPoly(element);
    }

    @Override
    public void paintTriangle(Triangle element) {
        paintPoly(element);
    }

    @Override
    public void paintLine(Line element) {
        paintPoly(element);
    }

    private void paintPoly(Element element) {
        TriVector[] p = new TriVector[element.nPoints];
        p[0] = element.getNPoint(0);
        p[0] = Projection(p[0]);

        if (!isInsideFustrum)
            return;

        for (int i = 0; i < element.nPoints; i++) {
            if (p[(i + 1) % element.nPoints] == null) {
                p[i + 1] = element.getNPoint(i + 1);
                p[i + 1] = Projection(p[i + 1]);

                if (!isInsideFustrum)
                    return;
            }
            double x1 = p[i].getX();
            double y1 = p[i].getY();
            double x2 = p[(i + 1) % element.nPoints].getX();
            double y2 = p[(i + 1) % element.nPoints].getY();
            wd.setDrawColor(element.getColor());
            wd.drawLine(x1, y1, x2, y2);
        }
    }

    public Matrix getCameraBasis() {
        return cameraBasis;
    }

    public void setCameraBasis(Matrix cameraBasis) {
        this.cameraBasis = cameraBasis;
        this.inverseCameraBasis = Matrix.transpose(this.cameraBasis);
    }

    public TriVector getEyePos() {
        return eyePos;
    }

    public void setEyePos(TriVector eyePos) {
        this.eyePos = eyePos;
    }

    public ImageWindow getWindow() {
        return wd;
    }

    @Override
    public void setWindow(ImageWindow wd) {
        this.wd = wd;

    }

    @Override
    public void setCamera(Matrix cameraBasis, TriVector eyePos) {
        setCameraBasis(cameraBasis);
        setEyePos(eyePos);
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub

    }

    @Override
    public void paintPoint(Point element) {
        TriVector p = element.getNPoint(0);
        p = Projection(p);

        if (!isInsideFustrum)
            return;

        wd.setDrawColor(element.color);
        int radius = element.getRadius();
        int x = wd.changeCoordX(p.getX());
        int y = wd.changeCoordY(p.getY());
        wd.drawPoint(p.getX(), p.getY(), radius);
    }

    @Override
    public void paintStringElement(StringElement element) {
        TriVector p = element.getNPoint(0);
        p = Projection(p);

        if (!isInsideFustrum)
            return;

        wd.setDrawColor(element.color);
        wd.drawString(element.getString(), p.getX(), p.getY());
    }

}
