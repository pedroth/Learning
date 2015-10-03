package windowThreeDim;

import java.awt.Color;

public class MaxBaryShader extends ZBufferPrespective {
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
		
		float[] ans = new float[3];
		
		if(barycentric[0] >= barycentric[1]) {
			if(barycentric[0] >= barycentric[2]) {
				ans = e.getColorPoint(0).getRGBColorComponents(null);
			} else {
				ans = e.getColorPoint(2).getRGBColorComponents(null);
			}
		} else {
			if(barycentric[1] >= barycentric[2]) {
				ans = e.getColorPoint(1).getColorComponents(null);
			}else {
				ans = e.getColorPoint(2).getColorComponents(null);
			}
		}
		
		return new Color(ans[0],ans[1],ans[2]);
	}
}	
