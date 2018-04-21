package teste;

import userGraph.Window;

import java.applet.Applet;
import java.awt.*;


public class ParametricTest extends Applet {
    private static final long serialVersionUID = 1L;
    static public double h1 = 1 * Math.pow(10, -9);
    static public double h2 = 1 * Math.pow(10, -4.5);

    public static double x(double t) {
        double R = 3;
        double r = 1;
        double d = 2;
        return (R - r) * Math.cos(t) + d * Math.cos(((R - r) / r) * t);
    }

    public static double y(double t) {
        double R = 3;
        double r = 1;
        double d = 2;
        return (R - r) * Math.sin(t) - d * Math.sin(((R - r) / r) * t);
    }

    public static double dxDt(double t) {
        return (x(t + h1) - x(t)) / h1;
    }

    public static double dyDt(double t) {
        return (y(t + h1) - y(t)) / h1;
    }

    public static double d2xD2t(double t) {
        // return (dxDt(t+h2)-dxDt(t))/h2; menos precisao
        return (x(t + 2 * h2) - 2 * x(t + h2) + x(t)) / (Math.pow(h2, 2));
    }

    public static double d2yD2t(double t) {
        // return (dyDt(t+h2)-dyDt(t))/h2; menos precisao
        return (y(t + 2 * h2) - 2 * y(t + h2) + y(t)) / (Math.pow(h2, 2));
    }

    public void paint(Graphics g) {
        Window wd;
        double t, x1, x2, x3, x4, r = 1;
        int i;
        Color c = new Color((float) 1.0, (float) 1.0, (float) 1.0);
        wd = new Window(this, g, c);
        wd.viewWindow(-5, 5, -5, 5);
        wd.setPercHeight(1);
        wd.setPercWidth(1);
        wd.setXYDisplacement(0, 0);
        /*
         * while(true){
		 */
        for (i = 0; i < 48; i++) {
            t = (Math.PI / 24) * i;
            x1 = r * x(t);
            x2 = r * y(t);
            t = (Math.PI / 24) * (i + 1);
            x3 = r * x(t);
            x4 = r * y(t);
            wd.drawLine(x1, x2, x3, x4);
        }
        wd.setDrawColor(Color.BLUE);
        for (i = 0; i < 48; i++) {
            t = (Math.PI / 24) * i;
            x1 = r * x(t);
            x2 = r * y(t);
            x3 = x1 + dxDt(t);
            x4 = x2 + dyDt(t);
            wd.drawLine(x1, x2, x3, x4);
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
        wd.setDrawColor(Color.red);
        for (i = 0; i < 48; i++) {
            t = (Math.PI / 24) * i;
            x1 = r * x(t);
            x2 = r * y(t);
            x3 = x1 + d2xD2t(t);
            x4 = x2 + d2yD2t(t);
            wd.drawLine(x1, x2, x3, x4);
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }

    }
}