/*
 * to use window, you must do this few steps:
 * 
 * 		1- create variable, 
 * 				Window wd;
 * 		2- create an instance of the class,
 * 				wd = new Window(this,this.getGraphics(),Color.white); // "this" means in this example an applet;
 * 		3- set view window,
 * 				wd.viewWindow(-5,5,-5,5); 
 * 		4 - set the percentage of the applet width and height your width;
 * 				wd.setPercWidth(1);
 * 				wd.setPercHeight(1);
 * 		5 - set the xy-displacement of the widow in relation to the left upper corner the displacement is given in pxl;
 * 				wd2.setXYDisplacement((int) wd2.getPxlWidth()/2, 0);
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * */

package userGraph;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;

public class Window {
    private Graphics gWindow;
    private JFrame appWindow;
    private double x_min;
    private double x_max;
    private double y_min;
    private double y_max;
    private double percWidth; // [0,1]
    private double percHeight;// [0,1]
    private int xDisplacement;//x displacement from the left up corner of the applet;
    private int yDisplacement;//y displacement from the left up corner of the applet;


    public Window(JFrame app, Graphics gra) {
        gWindow = gra;
        appWindow = app; //in this applet there is the background color, width pxl's, height pxl's.
        percWidth = 1.0;
        percHeight = 1.0;
        xDisplacement = 0;
        yDisplacement = 0;
    }

    public void setXYDisplacement(int x, int y) {
        xDisplacement = x;
        yDisplacement = y;
    }

    public Graphics getGraphics() {
        return gWindow;
    }

    public void setGraphics(Graphics g) {
        gWindow = g;
    }

    public int getPxlWidth() {
        return (int) Math.floor(percWidth * appWindow.getWidth());
    }

    public int getPxlHeight() {
        return (int) Math.floor(percHeight * appWindow.getHeight());
    }

    public double getXmin() {
        return x_min;
    }

    public double getYmin() {
        return y_min;
    }

    public double getXmax() {
        return x_max;
    }

    public double getYmax() {
        return y_max;
    }

    public JFrame getApplet() {
        return appWindow;
    }

    public void setApplet(JFrame app) {
        appWindow = app;
    }

    public void setHeightPxl(int y) {
        // blank in purpose
    }

    public void setWidth_pxl(int x) {
        // blank in purpose
    }

    public void setPercWidth(double percentWidth) {
        percWidth = percentWidth;
    }

    public void setPercHeight(double percentHeight) {
        percHeight = percentHeight;
    }

    public void viewWindow(double xmin, double xmax, double ymin, double ymax) {
        x_min = xmin;
        y_min = ymin;
        x_max = xmax;
        y_max = ymax;
    }

    public Color getBkgColor() {
        return appWindow.getBackground();
    }

    public void setBkgColor(Color c) {
        appWindow.setBackground(c);
    }

    public Color getDrawColor() {
        return gWindow.getColor();
    }

    public void setDrawColor(Color c) {
        gWindow.setColor(c);
    }

    public void clearScreen(Color c) {
        this.setDrawColor(c);
        gWindow.fillRect(0, 0, this.getPxlWidth(), this.getPxlHeight());
    }

    public int changeCoordX(double x) {
        int r;
        r = (int) Math.floor(((this.getPxlWidth()) * (x - (x_min))) / (Math.abs(x_max - x_min)));
        r += xDisplacement;
        return r;
    }

    public int changeCoordY(double y) {
        int r;
        r = (int) Math.floor((-(this.getPxlHeight()) * (y - (y_max))) / (Math.abs(y_max - y_min)));
        r += yDisplacement;
        return r;
    }

    public double InverseCoordY(int y) {
        double aux, y1;
        y1 = (double) y;
        aux = y1 * (-Math.abs(y_max - y_min) / this.getPxlHeight()) + y_max;
        return aux;
    }

    public double InverseCoordX(int x) {
        double aux, x1;
        x1 = (double) x;
        aux = x1 * (Math.abs(x_max - x_min) / this.getPxlWidth()) + x_min;
        return aux;
    }

    public void drawLine(double x1, double y1, double x2, double y2) {
        int lambda1, lambda2, lambda3, lambda4;
        lambda1 = this.changeCoordX(x1);
        lambda2 = this.changeCoordY(y1);
        lambda3 = this.changeCoordX(x2);
        lambda4 = this.changeCoordY(y2);
        gWindow.drawLine(lambda1, lambda2, lambda3, lambda4);
    }

    public void drawString(String s, double x1, double y1) {
        int lambda1, lambda2;
        lambda1 = this.changeCoordX(x1);
        lambda2 = this.changeCoordY(y1);
        gWindow.drawString(s, lambda1, lambda2);
    }

    public void fillTriangle(double x1, double y1, double x2, double y2, double x3, double y3) {
        int[] lambda1, lambda2;
        lambda1 = new int[3];
        lambda2 = new int[3];
        lambda1[0] = this.changeCoordX(x1);
        lambda2[0] = this.changeCoordY(y1);
        lambda1[1] = this.changeCoordX(x2);
        lambda2[1] = this.changeCoordY(y2);
        lambda1[2] = this.changeCoordX(x3);
        lambda2[2] = this.changeCoordY(y3);
        gWindow.fillPolygon(lambda1, lambda2, 3);
    }

    public void fillRect(double x1, double y1, double width, double height) {
        this.fillTriangle(x1, y1, x1 + width, y1, x1 + width, y1 + height);
        this.fillTriangle(x1 + width, y1 + height, x1, y1 + height, x1, y1);
    }

    public double pxl_xstep() {
        double r1;
        r1 = (double) (Math.abs(x_max - x_min)) / (this.getPxlWidth());
        return r1;
    }


    public double pxl_ystep() {
        double r2;
        r2 = (double) (Math.abs(y_max - y_min)) / (this.getPxlHeight());
        return r2;
    }

}
