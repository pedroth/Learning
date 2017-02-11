package windowThreeDim;

import algebra.TriVector;

import java.awt.*;

public class StringElement extends Element {
    private String str;

    public StringElement(TriVector point, String str) {
        super(1);
        this.p = new TriVector[1];
        colorPoint = new Color[1];
        this.p[0] = point;
        this.str = str;
    }

    public String getString() {
        return str;
    }

    public void setString(String str) {
        this.str = str;
    }

    @Override
    public void draw(PaintMethod visitor) {
        visitor.paintStringElement(this);
    }

    @Override
    public Element copy() {
        // TODO Auto-generated method stub
        return null;
    }

}
