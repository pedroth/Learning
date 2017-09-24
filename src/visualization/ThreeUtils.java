package visualization;

import algebra.TriVector;
import windowThreeDim.Composite;
import windowThreeDim.Element;
import windowThreeDim.Quad;

import java.awt.*;

public final class ThreeUtils {

    private ThreeUtils() {
        // empty constructor to prohibit instantiation
    }

    public static Composite buildUnitaryCube(Color c) {
        Composite compositeCube = new Composite();
        TriVector[][][] cube = new TriVector[2][2][2];
        for (int i = 0; i < cube.length; i++) {
            for (int j = 0; j < cube.length; j++) {
                for (int k = 0; k < cube.length; k++) {
                    double x = i - 0.5;
                    double y = j - 0.5;
                    double z = k - 0.5;
                    cube[i][j][k] = new TriVector(x, y, z);
                }
            }
        }
        for (int i = 0; i < cube.length; i++) {
            Element e = new Quad(cube[i][0][0], cube[i][1][0], cube[i][1][1], cube[i][0][1]);
            e.setColor(c);
            compositeCube.add(e);
        }
        for (int i = 0; i < cube.length; i++) {
            Element e = new Quad(cube[0][i][0], cube[1][i][0], cube[1][i][1], cube[0][i][1]);
            e.setColor(c);
            compositeCube.add(e);
        }
        for (int i = 0; i < cube.length; i++) {
            Element e = new Quad(cube[0][0][i], cube[1][0][i], cube[1][1][i], cube[0][1][i]);
            e.setColor(c);
            compositeCube.add(e);
        }
        return compositeCube;
    }

    public static Composite buildSphere(double radius, int steps, Color color) {
        double pi = Math.PI;
        double hu = 2 * pi / (steps - 1);
        double hv = pi / (steps - 1);
        TriVector[][] sphere = new TriVector[steps][steps];
        Composite ball = new Composite();
        for (int j = 0; j < steps; j++) {
            for (int i = 0; i < steps; i++) {
                double u = hu * i;
                double v = hv* j;
                double sinU = Math.sin(u);
                double cosU = Math.cos(u);
                double sinV = Math.sin(v);
                double cosV = Math.cos(v);
                // don't understand why there is the need to put a minus in z-coordinate.
                sphere[i][j] = new TriVector(radius * sinV * cosU, radius * sinV * sinU, -radius * cosV);
            }
        }
        for (int j = 0; j < steps - 1; j++) {
            for (int i = 0; i < steps - 1; i++) {
                Quad e = new Quad(sphere[i][j], sphere[i + 1][j], sphere[i + 1][j + 1], sphere[i][j + 1]);
                e.setColor(color);
                ball.add(e);
            }
        }
        return ball;
    }

}
