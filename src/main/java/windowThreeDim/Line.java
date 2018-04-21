package windowThreeDim;

import algebra.TriVector;

import java.awt.*;

public class Line extends Element {

    public Line(TriVector p1, TriVector p2) {
        super(2);
        p = new TriVector[nPoints];
        p[0] = p1;
        p[1] = p2;
        colorPoint = new Color[nPoints];
    }

    @Override
    public void draw(PaintMethod visitor) {
        visitor.paintLine(this);
    }

    @Override
    public Element copy() {
        return new Line(this.getNPoint(0), this.getNPoint(1));
    }
}
