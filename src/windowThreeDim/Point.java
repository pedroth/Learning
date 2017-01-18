package windowThreeDim;

import algebra.TriVector;

import java.awt.*;

public class Point extends Element {
    private int radius;

    public Point(TriVector point) {
        this.p = new TriVector[1];
        colorPoint = new Color[1];
        this.p[0] = point;
        radius = 1;
        nPoints = 1;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public void draw(PaintMethod visitor) {
        visitor.paintPoint(this);
    }

    @Override
    public Element copy() {
        // TODO Auto-generated method stub
        return null;
    }

}
