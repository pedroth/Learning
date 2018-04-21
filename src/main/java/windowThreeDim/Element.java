package windowThreeDim;

import algebra.Matrix;
import algebra.TriVector;

import java.awt.*;

public abstract class Element {
    protected TriVector p[];
    protected int nPoints;
    protected Color color;
    /**
     * color associated with a vertice
     */
    protected Color colorPoint[];

    public Element(int nPoints) {
        this.nPoints = nPoints;
    }


    public abstract void draw(PaintMethod visitor);

    public TriVector getNPoint(int i) {
        if (i >= 0 && i < nPoints)
            return p[i];
        else
            return null;
    }

    public Color getColor() {
        if (color == null && colorPoint[0] != null) {
            return colorPoint[0];
        } else {
            return color;
        }
    }

    public void setColor(Color color) {
        this.color = color;
        if (colorPoint == null) {
            colorPoint = new Color[nPoints];
        }
        for (int i = 0; i < colorPoint.length; i++) {
            colorPoint[i] = color;
        }
    }

    public Color getColorPoint(int i) {
        if (i >= 0 && i < nPoints)
            return colorPoint[i];
        else
            return null;
    }

    public void setColorPoint(Color c, int i) {
        if (i >= 0 && i < nPoints)
            colorPoint[i] = c;
    }

    public int getNumOfPoints() {
        return nPoints;
    }

    public TriVector[] getPointsArray() {
        return p;
    }

    public void transform(Matrix m, TriVector v) {
        for (int i = 0; i < p.length; i++) {
            p[i] = TriVector.Transformation(m, p[i]);
            p[i] = TriVector.sum(p[i], v);
        }
    }

    public abstract Element copy();
}
