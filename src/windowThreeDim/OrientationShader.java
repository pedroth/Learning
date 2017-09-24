package windowThreeDim;


import algebra.TriVector;

import java.awt.*;

public class OrientationShader extends ZBufferPerspective {

    public Color getFragmentColor(Element e, int x, int y) {
        if (!(e instanceof Triangle)) {
            return Color.black;
        }
        return this.orientation > 0 ? Color.RED : Color.BLUE;
    }
}
