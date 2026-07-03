package windowThreeDim;

import algebra.Matrix;
import algebra.TriVector;

import java.awt.*;

public class Triangle extends Element {

    public Triangle(TriVector p1, TriVector p2, TriVector p3) {
        super(3);
        p = new TriVector[nPoints];
        p[0] = p1;
        p[1] = p2;
        p[2] = p3;
        colorPoint = new Color[nPoints];
    }

    /**
     * @param p : must be a 3 - dim array of TriVector;
     * @return : return normalized normal;
     */
    public static TriVector calcNormal(TriVector[] p) {
        TriVector v1 = TriVector.sub(p[1], p[0]);
        TriVector v2 = TriVector.sub(p[2], p[0]);
        TriVector normal = TriVector.vectorProduct(v1, v2);
        if (normal.getX() == 0.0 && normal.getY() == 0.0 && normal.getZ() == 0.0)
            return normal;
        else
            normal.normalize();
        return normal;
    }

    @Override
    public void draw(PaintMethod visitor) {
        visitor.paintTriangle(this);

    }

    @Override
    public Element copy() {
        TriVector[] pCopy = new TriVector[this.nPoints];
        for (int i = 0; i < pCopy.length; i++) {
            pCopy[i] = p[i].copy();
        }
        Element ret = new Triangle(pCopy[0], pCopy[1], pCopy[2]);
        ret.setColor(this.getColor());
        return ret;
    }

    public void setPoints(TriVector vertex1, TriVector vertex2, TriVector vertex3) {
        this.p = new TriVector[]{vertex1, vertex2, vertex3};
    }
}
