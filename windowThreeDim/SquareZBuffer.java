package windowThreeDim;

import java.awt.Graphics;

import algebra.TriVector;

public class SquareZBuffer extends ZBufferPrespective {

	@Override
	protected void drawZ(TriVector[] intBuffer, int nPoints, TriVector normal,
			Element element) {
		
		if(isCullBack && normal.getZ() > 0)
			return;
		
		int[][] box;
		double z;
		/**
		 * [0][0] xmin, [0][1] ymin, [1][0] xmax, [1][1] ymax;
		 * 
		 * calculation of the box containing the polygon in integer coordinates
		 */
		box = generateBox(intBuffer);

		/**
		 * z = -(n1/n3 * (x - x0) + n2/n3 * (y - y0)) + z0
		 * 
		 * we can use at (x0,y0,z0) a random point in the polygon
		 */

		double nx = normal.getX() / normal.getZ();

		double ny = normal.getY() / normal.getZ();

		wd.setDrawColor(element.color);
		/**
		 * z = -nx * dx -ny * dy;
		 * 
		 * painting polygon with z-buffer algorithm
		 */
		int w = box[1][0] - box[0][0];
		int h = box[1][1] - box[0][1];

		double dx = box[0][0] + w/2 - intBuffer[0].getX();

		double dy = box[0][1] + h/2 - intBuffer[0].getY();
		
		z = -nx * dx - ny * dy + intBuffer[0].getZ();
		
		int i = Math.min(box[0][0] + w/2, wd.getWindowWidth()-1);
		int j = Math.min(box[0][1] + h/2, wd.getWindowHeight()-1);
		
		i = Math.max(0, i);
		j = Math.max(0,j);
		
		fillZBuffer(box[0][0]+ w/4, box[0][1]+h/4, 3*w/2, 3*h/2, z);
	}
	
	public void fillZBuffer(int x,int y, int w, int h, double z) {
		Graphics g = wd.getGraphics();
//		int k = Math.min(x + w, wd.getWindowWidth()-1);
//		int l = Math.min(y + h, wd.getWindowHeight()-1);
//		for(int i = x ; i < k; i++){
//			for(int j = y; j < l; j++){
//				if( z < zBuffer[i][j] && z > zd ) {
//					g.drawLine(i, j, i, j);
//					zBuffer[i][j] = z;
//				}
//			}
//		}
		g.fillRect(x, y, w, h);
	}

}
