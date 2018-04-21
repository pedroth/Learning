package windowThreeDim;

import algebra.TriVector;

import java.awt.*;

public class SamplingZbuffer extends ZBufferPerspective {
    protected int radius;

    public SamplingZbuffer(int radius) {
        super();
        this.radius = radius;
    }

    @Override
    public void init() {
        int width = wd.getWindowWidth() / radius;
        int height = wd.getWindowHeight() / radius;
        zBuffer = new double[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                zBuffer[i][j] = Double.MAX_VALUE;
    }

    protected void drawZ(TriVector[] intBuffer, int nPoints, TriVector normal,
                         Element element) {

        if (isCullBack && normal.getZ() > 0)
            return;

        int[][] polyNormals;
        int[][] box;
        Graphics g = wd.getGraphics();
        double z;
        double z0;

        /**
         * [0][0] xmin, [0][1] ymin, [1][0] xmax, [1][1] ymax;
         *
         * calculation of the box containing the polygon in integer coordinates
         */
        box = generateBox(intBuffer);

        polyNormals = calculateNormals(intBuffer);

        /**
         * z = -(n1/n3 * (x - x0) + n2/n3 * (y - y0)) + z0
         *
         * we can use at (x0,y0,z0) a random point in the polygon
         */

        double nx = normal.getX() / normal.getZ();

        double ny = normal.getY() / normal.getZ();

        double dx = box[0][0] - intBuffer[0].getX();

        double dy = box[0][1] - intBuffer[0].getY();

        z = -nx * dx - ny * dy + intBuffer[0].getZ();

        z0 = z;

        wd.setDrawColor(element.color);
        /**
         * z = -nx * dx -ny * dy;
         *
         * painting polygon with z-buffer algorithm
         */

        int incr = radius;
        for (int i = box[0][0]; i < box[1][0]; i += incr) {
            for (int j = box[0][1]; j < box[1][1]; j += incr) {
                int k = Math.min(i / radius, wd.getWindowWidth() / radius - 1);
                int l = Math.min(j / radius, wd.getWindowHeight() / radius - 1);
                if (isOnSet(i + radius / 2, j + radius / 2, polyNormals,
                        intBuffer) && zCheckWithError(k, l, z) && z > zd) {
                    g.fillRect(i, j, 2 * radius, 2 * radius);
                    zBuffer[k][l] = z;
                }
                z += -radius * ny;
            }
            z0 += -radius * nx;
            z = z0;
        }
    }

    public boolean zCheckWithError(int x, int y, double z) {
        int area = 9;
        int quorum = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int k = Math.min(x + i, wd.getWindowWidth() / radius - 1);
                int l = Math.min(y + j, wd.getWindowHeight() / radius - 1);
                k = Math.max(0, k);
                l = Math.max(0, l);
                if (z < zBuffer[k][l])
                    quorum++;
            }
        }
        return quorum > area / 2;
    }

    @Override
    public void paintLine(Line element) {
        // TODO Auto-generated method stub
    }

    @Override
    public void paintPoint(Point element) {
        // TODO Auto-generated method stub
    }
}
