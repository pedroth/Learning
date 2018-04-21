package windowThreeDim;

import algebra.TriVector;

import java.awt.image.BufferedImage;

/**
 * @author pedro this is worse than one thread
 */
public class ParallelShader extends FlatShader {
    private double nx, ny;
    private int rgb;
    private int[][] box, polyNormals;
    private TriVector[] intBuffer;

    /**
     * @param intBuffer points in integer coordinates
     * @param nPoints   number of points of the polygon
     * @param normal    normal vector of the polygon
     * @param element   polygon
     */
    protected void drawZ(TriVector[] intBuffer, int nPoints, TriVector normal,
                         Element element) {

        if (isCullBack && normal.getZ() > 0)
            return;

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

        nx = normal.getX() / normal.getZ();

        ny = normal.getY() / normal.getZ();

        double dx = box[0][0] - intBuffer[0].getX();

        double dy = box[0][1] - intBuffer[0].getY();

        double z = -nx * dx - ny * dy + intBuffer[0].getZ();

        this.intBuffer = intBuffer;

        rgb = shade.getRGB();

        int nCores = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[nCores];
        int width = box[1][0] - box[0][0];
        int incr = width / nCores;
        int x = box[0][0];
        for (int i = 0; i < nCores; i++) {
            threads[i] = new Thread(new TriFiller(x, x + incr, z - nx * incr
                    * i));
            threads[i].start();
            x += incr;
        }
        try {
            for (int i = 0; i < nCores; i++)
                threads[i].join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class TriFiller implements Runnable {
        int xi, xf;
        double z;

        public TriFiller(int xi, int xf, double z) {
            super();
            this.xi = xi;
            this.xf = xf;
            this.z = z;
        }

        @Override
        public void run() {

            int color = shade.getRGB();

            int[] pixels = ((java.awt.image.DataBufferInt) wd.getImage()
                    .getRaster().getDataBuffer()).getData();

            int w = wd.getWindowWidth();


            boolean inOut = false;
            double z0 = z;
            BufferedImage image = wd.getImage();
            for (int i = xi; i < xf; i++) {
                for (int j = box[0][1]; j < box[1][1]; j++) {
                    if (isOnSet(i, j, polyNormals, intBuffer)
                            && z < zBuffer[i][j] && z > zd) {
                        pixels[j * w + i] = color;
                        zBuffer[i][j] = z;
                        inOut = true;
                    } else {
                        if (inOut) {
                            break;
                        }
                    }
                    z += -ny;
                }
                z0 += -nx;
                z = z0;
                inOut = false;
            }
        }
    }

}