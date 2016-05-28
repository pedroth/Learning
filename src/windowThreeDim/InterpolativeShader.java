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
		float alfa = 1.0f * (uy * px - py * ux) / det;
		float beta = 1.0f * (-vy * px + vx * py) / det;
		float gama = 1.0f - alfa - beta;
		
		barycentric[0] = gama;
		barycentric[1] = alfa;
		barycentric[2] = beta;
		
		float[] rgb;
		float[] ans = new float[3];
		
		Color colorPoint = e.getColorPoint(0);
		rgb = colorPoint.getRGBComponents(null);
		rgb[0] = barycentric[0] * rgb[0];
		rgb[1] = barycentric[0] * rgb[1];
		rgb[2] = barycentric[0] * rgb[2];
		ans[0] += rgb[0];
		ans[1] += rgb[1];
		ans[2] += rgb[2];
		
		colorPoint = e.getColorPoint(1);
		rgb = colorPoint.getRGBComponents(null);
		rgb[0] = barycentric[1] * rgb[0];
		rgb[1] = barycentric[1] * rgb[1];
		rgb[2] = barycentric[1] * rgb[2];
		ans[0] += rgb[0];
		ans[1] += rgb[1];
		ans[2] += rgb[2];
		
		colorPoint = e.getColorPoint(2);
		rgb = colorPoint.getRGBComponents(null);
		rgb[0] = barycentric[2] * rgb[0];
		rgb[1] = barycentric[2] * rgb[1];
		rgb[2] = barycentric[2] * rgb[2];
		ans[0] += rgb[0];
		ans[1] += rgb[1];
		ans[2] += rgb[2];
		
		ans[0] = Math.min(ans[0], 1.0f);
		ans[1] = Math.min(ans[1], 1.0f);
		ans[2] = Math.min(ans[2], 1.0f);
		
		ans[0] = Math.max(ans[0], 0.0f);
		ans[1] = Math.max(ans[1], 0.0f);
		ans[2] = Math.max(ans[2], 0.0f);
		
		return new Color(ans[0],ans[1],ans[2]);
	}
}
