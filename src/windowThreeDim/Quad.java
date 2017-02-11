package windowThreeDim;

import algebra.TriVector;

import java.awt.*;

public class Quad extends Element {

    public Quad(TriVector p1, TriVector p2, TriVector p3, TriVector p4) {
        super(4);
        p = new TriVector[nPoints];
        p[0] = p1;
        p[1] = p2;
        p[2] = p3;
        p[3] = p4;
        colorPoint = new Color[nPoints];
    }

    @Override
    public void draw(PaintMethod visitor) {
        visitor.paintQuad(this);
    }

    @Override
    public Element copy() {
        TriVector[] pCopy = new TriVector[this.nPoints];
        for (int i = 0; i < pCopy.length; i++) {
            pCopy[i] = p[i].copy();
        }
        Element ret = new Quad(pCopy[0], pCopy[1], pCopy[2], pCopy[3]);
        ret.setColor(this.getColor());
        return ret;
    }
}
