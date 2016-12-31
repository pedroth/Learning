package windowThreeDim;

import java.awt.Color;

public class InterpolativeShader extends ZBufferPrespective {

	public Color getFragmentColor(Element e, int x, int y) {
		/**
		 * very ugly code but it is faster unfortunately
		 */

		int vx = (int) (intBuffer[1].getX() - intBuffer[0].getX());
		int vy = (int) (intBuffer[1].getY() - intBuffer[0].getY());
		int ux = (int) (intBuffer[2].getX() - intBuffer[0].getX());
		int uy = (int) (intBuffer[2].getY() - intBuffer[0].getY());
		int px = (int) (x - intBuffer[0].getX());
		int py = (int) (y - intBuffer[0].getY());
		/**
		 * det > 0, since x,y are always inside the triangle
		 */
		int det = vx * uy - vy * ux;
		float[] barycentric = new float[e.nPoints];
		float alfa = (1.0f * (uy * px - py * ux)) / det;
		float beta = (1.0f * (-vy * px + vx * py)) / det;
		float gama = 1.0f - alfa - beta;

		barycentric[0] = gama;
		barycentric[1] = alfa;
		barycentric[2] = beta;

		float[] acc = new float[3];
		float[] colorPoint0 = e.getColorPoint(0).getRGBColorComponents(null);
		float[] colorPoint1 = e.getColorPoint(1).getRGBColorComponents(null);
		float[] colorPoint2 = e.getColorPoint(2).getRGBColorComponents(null);

		acc[0] += Math.max(0.0, Math.min(1.0, colorPoint0[0] * barycentric[0] + colorPoint1[0] * barycentric[1] + colorPoint2[0] * barycentric[2]));
		acc[1] += Math.max(0.0, Math.min(1.0, colorPoint0[1] * barycentric[0] + colorPoint1[1] * barycentric[1] + colorPoint2[1] * barycentric[2]));
		acc[2] += Math.max(0.0, Math.min(1.0, colorPoint0[2] * barycentric[0] + colorPoint1[2] * barycentric[1] + colorPoint2[2] * barycentric[2]));


		return new Color(acc[0],acc[1],acc[2]);
	}
}
