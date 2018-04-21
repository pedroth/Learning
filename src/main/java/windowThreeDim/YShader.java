package windowThreeDim;

import java.awt.*;

public class YShader extends ZBufferPerspective {

    public YShader() {
        super();
    }

    public Color getFragmentColor(Element e, int x, int y) {
        int h = wd.getWindowHeight();
        double red = 0;
        double blue = 240.0 / 360.0;

        double lambda = -1.0 + 2.0 * ((1.0 * y) / h);
        /**
         * linear interpolation between blue color and red
         */
        float colorHSB = (float) (blue + (red - blue) * 0.5 * (lambda + 1));
        return Color.getHSBColor((float) colorHSB, 1f, 1f);
    }
}
