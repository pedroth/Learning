package windowThreeDim;

import java.awt.*;

public class InterpolativeShader extends ZBufferPerspective {

    public Color getFragmentColor(Element e, int x, int y) {
        /*
         * very ugly code but it is faster unfortunately
         */
        int vx = (int) (intBuffer[1].getX() - intBuffer[0].getX());
        int vy = (int) (intBuffer[1].getY() - intBuffer[0].getY());
        int ux = (int) (intBuffer[2].getX() - intBuffer[0].getX());
        int uy = (int) (intBuffer[2].getY() - intBuffer[0].getY());
        int px = (int) (x - intBuffer[0].getX());
        int py = (int) (y - intBuffer[0].getY());
        /*
         * det > 0, since x,y are always inside the triangle
         */
        int det = vx * uy - vy * ux;
        float alfa = (1.0f * (uy * px - py * ux)) / det;
        float beta = (1.0f * (-vy * px + vx * py)) / det;
        float gama = 1.0f - alfa - beta;

        int rgb0 = e.getColorPoint(0).getRGB();
        int rgb1 = e.getColorPoint(1).getRGB();
        int rgb2 = e.getColorPoint(2).getRGB();

        final float inv255 = 1f / 255f;
        float r0 = ((rgb0 >> 16) & 0xFF) * inv255, g0 = ((rgb0 >> 8) & 0xFF) * inv255, b0 = (rgb0 & 0xFF) * inv255;
        float r1 = ((rgb1 >> 16) & 0xFF) * inv255, g1 = ((rgb1 >> 8) & 0xFF) * inv255, b1 = (rgb1 & 0xFF) * inv255;
        float r2 = ((rgb2 >> 16) & 0xFF) * inv255, g2 = ((rgb2 >> 8) & 0xFF) * inv255, b2 = (rgb2 & 0xFF) * inv255;

        float r = Math.max(0f, Math.min(1f, r0 * gama + r1 * alfa + r2 * beta));
        float g = Math.max(0f, Math.min(1f, g0 * gama + g1 * alfa + g2 * beta));
        float b = Math.max(0f, Math.min(1f, b0 * gama + b1 * alfa + b2 * beta));

        return new Color(r, g, b);
    }
}
