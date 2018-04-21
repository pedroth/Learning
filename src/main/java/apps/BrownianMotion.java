package apps;

import visualization.TextFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * @author pedro
 *         <p>
 *         this is a simple demo for graphics in java.
 *         <p>
 *         this may not be the best way to do it.
 *         <p>
 *         You may encapsulate some of these function in a class.
 */
public class BrownianMotion extends JFrame implements KeyListener {
    private static String helpText = "< q > : add particle \n\n < a > : remove particle \n\n < w > : increase collision damping \n\n < s > : decrease collision damping\n\n" + "< arrows > : add force to all particles \n\n" + "< + > : increase radius of particles \n\n" + "< - > : decrease radius of particles\n\n" + "Made by Pedroth";
    /**
     * diameter of circles
     */
    int r = 20;
    double damping = 0.25;
    double reflecDump = 0.25;
    /**
     * record the size of the window
     */
    private int widthChanged, heightChanged;
    /**
     * basically is a image where you will draw things
     */
    private BufferedImage canvas;
    /**
     * object that has functions to draw things on the canvas this is the
     * graphics object belonging to canvas variable
     */
    private Graphics canvasGraphics;
    /**
     * time handling variables
     * <p>
     * time is in seconds
     */
    private double oldTime;
    private double time;
    /**
     * coordinates of the circle
     */
    private double[] x, y;
    /**
     * velocity
     */
    private double[] vx, vy;
    private int numParticles;
    private double thrustx, thrusty;
    private double[] historyX, historyY;
    private int historyIndex, numHistory = 1000;

    public BrownianMotion(boolean isApplet) {
        // Set JFrame title
        super("Pseudo - Brownian Motion - Press h for Help");

        // Set default close operation for JFrame
        if (!isApplet) {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        // Set JFrame size
        setSize(400, 400);

        widthChanged = 400;
        heightChanged = 400;

        /**
         * creation of the canvas the image that will be draw on the window
         */
        canvas = new BufferedImage(widthChanged, heightChanged, BufferedImage.TYPE_INT_RGB);
        canvasGraphics = canvas.getGraphics();

        // Make JFrame visible
        setVisible(true);

        this.addKeyListener(this);

        /**
         * time in seconds
         */
        oldTime = (System.currentTimeMillis()) * 1E-03;
        clearCanvasWithBackground();
        numParticles = 200;
        initPositions();
    }

    public static void main(String[] args) {
        new BrownianMotion(false);
    }

    private void initPositions() {
        /**
         * init position of oval
         */
        x = new double[numParticles];
        y = new double[numParticles];
        vx = new double[numParticles];
        vy = new double[numParticles];
        Random r = new Random();
        for (int i = 0; i < numParticles; i++) {
            x[i] = r.nextDouble() * widthChanged;
            y[i] = r.nextDouble() * heightChanged;
        }

        historyX = new double[numHistory];
        historyY = new double[numHistory];

        historyIndex = 0;

        for (int i = 0; i < numHistory; i++) {
            historyX[i] = x[0];
            historyY[i] = y[0];
        }
    }

    private void clearCanvasWithBackground() {
        canvasGraphics.setColor(Color.white);
        canvasGraphics.fillRect(0, 0, widthChanged, heightChanged);
    }

    /**
     * function to handle the resize of the window
     */
    private void reShape() {
        if (Math.abs(widthChanged - this.getWidth()) > 0 || Math.abs(heightChanged - this.getHeight()) > 0) {

            canvas = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);

            canvasGraphics = canvas.getGraphics();
            /**
             * getting the actual size of the window and updating the variables.
             */
            widthChanged = this.getWidth();
            heightChanged = this.getHeight();

            clearCanvasWithBackground();
        }
    }

    private void updateTime(double dt) {
        time += dt;
    }

    /**
     * @return time in seconds between to calls of this function;
     */
    private double getTimeDifferential() {
        double currentTime = (System.currentTimeMillis()) * 1E-03;
        double dt = currentTime - oldTime;
        oldTime = currentTime;
        return dt;
    }

    /**
     * this method is automatically called every time the window needs to be
     * painted
     * <p>
     * this is the main of a graphics application
     * <p>
     * g is the graphics object of the window(not the canvas image)
     * <p>
     * this is a override of the JFrame class
     */
    public void paint(Graphics g) {
        reShape();
        update(g);
    }

    /**
     * g is the graphics object of the window(not the canvas image)
     * <p>
     * this is a override of the JFrame class
     * <p>
     * this is where the actual drawing will be made.
     */
    public void update(Graphics g) {
        double dt = getTimeDifferential();
        updateTime(dt);
        clearCanvasWithBackground();
        /**
         * animation and drawing the next frame
         */
        canvasGraphics.setColor(Color.blue);

        updateHistory();
        updateOvalPos(dt);
        canvasGraphics.drawString("time(s) : " + time, 50, 80);
        canvasGraphics.drawString("damping : " + damping, 50, 65);

        /**
         * draw canvas on the window/JFrame
         *
         * image is drawn from the (0,0) coordinate
         */
        g.drawImage(canvas, 0, 0, null);
        repaint();
    }

    public void updateHistory() {
        historyX[historyIndex] = x[0];
        historyY[historyIndex] = y[0];
        canvasGraphics.setColor(Color.red);
        for (int i = 0; i < numHistory; i++) {
            canvasGraphics.fillOval((int) historyX[i], (int) historyY[i], r, r);
        }
        historyIndex++;
        historyIndex = historyIndex % numHistory;
    }

    public void updateOvalPos(double dt) {
        for (int i = 0; i < numParticles; i++) {
            if ((x[i] + r) > widthChanged || (x[i] - r) < 0) {
                vx[i] = -vx[i];
                x[i] = (((x[i] - r) < 0) ? r : 0) + (((x[i] + r) > widthChanged) ? widthChanged - r : 0);
            }
            if ((y[i] - r) < 0 || (y[i] + r) > heightChanged) {
                vy[i] = -vy[i];
                y[i] = (((y[i] - r) < 0) ? 1.01 * r : 0) + (((y[i] + r) > heightChanged) ? heightChanged - 1.01 * r : 0);
            }
            collisionHandle(i);
            double ax = thrustx;
            double ay = thrusty;
            vx[i] = vx[i] + ax * dt;
            vy[i] = vy[i] + ay * dt;
            x[i] = x[i] + dt * vx[i] + 0.5 * ax * dt * dt;
            y[i] = y[i] + dt * vy[i] + 0.5 * ay * dt * dt;
            canvasGraphics.setColor(Color.blue);
            canvasGraphics.fillOval((int) x[i], (int) y[i], r, r);
            // canvasGraphics.drawLine((int) x[0], (int) y[0],(int) (x[0] +
            // vx[0]), (int) (y[0] + vy[0]));
        }
    }

    public void collisionHandle(int i) {
        for (int j = 0; j < numParticles; j++) {
            if (i == j)
                continue;
            else if (isContact(i, j)) {
                constraintUpdate(i, j);
                collisionVelocity(i, j);
                collisionVelocity(j, i);
            }
        }
    }

    public void constraintUpdate(int i, int j) {
        double s = 0.5;
        double nx = x[j] - x[i];
        double ny = y[j] - y[i];
        double d = distance(i, j);
        nx = nx / d;
        ny = ny / d;
        double constx = (x[i] + nx * r * s) - (x[j] - nx * r * s);
        double consty = (y[i] + ny * r * s) - (y[j] - ny * r * s);
        double factor = Math.sqrt(constx * constx + consty * consty);
        x[i] = x[i] - nx * factor * s;
        y[i] = y[i] - ny * factor * s;
        x[j] = x[j] + nx * factor * s;
        y[j] = y[j] + ny * factor * s;
    }

    /**
     * @param i index of the particle which is colliding
     * @param j index of a particle which is the obstacle
     *          <p>
     *          changes velocity o particle i
     */
    public void collisionVelocity(int i, int j) {
        double nx = x[j] - x[i];
        double ny = y[j] - y[i];
        double d = distance(i, j);
        nx = nx / d;
        ny = ny / d;
        double dot = nx * vx[i] + ny * vy[i];
        double dot2 = -nx * vx[j] - ny * vy[j];
        vx[i] = vx[i] - (2 - reflecDump) * dot * nx - damping * dot2 * nx;
        vy[i] = vy[i] - (2 - reflecDump) * dot * ny - damping * dot2 * ny;
    }

    public double distance(int i, int j) {
        return Math.sqrt((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j]) * (y[i] - y[j]));
    }

    public boolean isContact(int i, int j) {

        double d = distance(i, j);
        return d < r;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            thrustx = 200;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            thrustx = -200;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            thrusty = -200;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            thrusty = 200;
        }
        if (e.getKeyCode() == KeyEvent.VK_PLUS) {
            r += 2;
        }
        if (e.getKeyCode() == KeyEvent.VK_MINUS) {
            r -= 2;
        }
        if (e.getKeyCode() == KeyEvent.VK_W) {
            reflecDump += 0.01;
            damping += 0.01;
            reflecDump = Math.min(0.98, reflecDump);
            damping = Math.min(0.98, damping);
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            reflecDump -= 0.01;
            damping -= 0.01;
            reflecDump = Math.max(0.0, reflecDump);
            damping = Math.max(0.0, damping);
        }
        if (e.getKeyCode() == KeyEvent.VK_Q) {
            numParticles++;
            initPositions();
        }
        if (e.getKeyCode() == KeyEvent.VK_A) {
            numParticles--;
            initPositions();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
            thrustx = 0;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
            thrusty = 0;
        }
        if (e.getKeyCode() == KeyEvent.VK_H) {
            new TextFrame("help", helpText);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }
}
