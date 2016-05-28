package windowThreeDim;

import java.awt.Color;

public class LevelSetShader extends ZBufferPrespective {

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
		
		float rgb;
		float ans = 0;
		
		rgb = (float) (barycentric[0] * e.getNPoint(0).getZ());
		ans += rgb;
		
		rgb = (float) (barycentric[1] * e.getNPoint(1).getZ());
		ans += rgb;
		
		rgb = (float) (barycentric[2] * e.getNPoint(2).getZ());
		ans += rgb;
		
		double freq = 10;
		ans = (float) (Math.sin(freq * ans) * Math.sin(freq * ans));
		
		return new Color(ans * 1.0f,ans * 1.0f,ans * 1.0f);
	}
}
