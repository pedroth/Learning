package apps;

import visualization.TextFrame;
import window.ImageWindow;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class RandomCurve extends JFrame implements MouseListener, KeyListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final TextFrame HELP_FRAME = TextFrame.builder()
            .addLine("< mouse > : initial position of curve")
            .addLine("< [+, -] > : increase/decrease speed of curve")
            .addLine("< s > : save png image(does not work in applet)")
            .addLine("Made by Pedroth")
            .buildWithTitle("Help");
    private Random rand;
    ImageWindow wd;
    private int wChanged;
    private int hChanged;
    private double time;
    private double oldTime;
    private double omega;
    private double alfa;
    private double theta;
    private double speed;
    private boolean speedDisplay;
    private double maxSpeed;
    /**
     * lineVector[i][0] = x-coordinate of point/vector i; lineVector[i][1] =
     * y-coordinate of point/vector i;
     */
    private double[][] lineVector;

    public RandomCurve(boolean isApplet) {

        // Set JFrame title
        super("Random Curve - Press h for Help");

        // init
        lineVector = new double[2][2];
        rand = new Random();
        wd = new ImageWindow(-5, 5, -5, 5);
        this.addMouseListener(this);
        time = 0.0;
        /**
         * time in seconds
         */
        oldTime = ((double) System.currentTimeMillis()) * 1E-03;
        lineVector[0][0] = 10 * rand.nextDouble() - 5.0;
        lineVector[0][1] = 10 * rand.nextDouble() - 5.0;

        if (!isApplet) {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        // Set JFrame size
        setSize(800, 550);

        wChanged = this.getWidth();
        hChanged = this.getHeight();
        wd.setWindowSize(wChanged, hChanged);
        wd.setBackGroundColor(Color.black);
        wd.clearImageWithBackGround();

        // Make JFrame visible
        setVisible(true);

        speed = 1.0;
        maxSpeed = 20;
        theta = 2 * Math.PI * rand.nextDouble() - Math.PI;

        this.addKeyListener(this);
    }

    public static void main(String[] args) {
        new RandomCurve(false);
    }

    public void run() {
        double initX;
        double initY;
        double dxdt;
        double dydt;
        double currentTimeSec;
        double dt;

        currentTimeSec = (System.currentTimeMillis()) * 1E-03;
        dt = currentTimeSec - oldTime;
        oldTime = currentTimeSec;
        time += dt;
        if (lineVector[0][0] > 5)
            lineVector[0][0] = -5;
        else if (lineVector[0][0] < -5)
            lineVector[0][0] = 5;
        else if (lineVector[0][1] > 5)
            lineVector[0][1] = -5;
        else if (lineVector[0][1] < -5)
            lineVector[0][1] = 5;

        initX = lineVector[0][0];
        initY = lineVector[0][1];

        alfa = 100 * rand.nextDouble() - 50 - omega;
        omega = omega + speed * alfa * dt;
        theta = theta + speed * omega * dt;

        dxdt = ComputeDxdt();
        dydt = ComputeDydt();

        // drawCurveOrientation(theta,omega);

        /**
         * x(n+1) = x(n) + dx/dt * dt | where dx/dt = er(theta); er(theta) =
         * {cos(theta),sin(theta)}; theta is solution of alfa = 100 * X - 50
         * - omega; alfa is second derivative of theta and omega is the
         * first derivative.
         *
         * X ~ Uniform(0,1);
         */
        lineVector[1][0] = initX + speed * dt * dxdt;
        lineVector[1][1] = initY + speed * dt * dydt;

//			System.out.println("theta :  " + theta + "  omega :  " + omega
//					+ "  alfa :  " + alfa + " speed : " + speed);

        // wd.setDrawColor(Color.red);
        // wd.drawLine(lineVector[0][0], lineVector[0][1], lineVector[0][0]
        // + dxdt, lineVector[0][1] + dydt);

        wd.setDrawColor(ComputeColor(time));
        wd.drawLine(lineVector[0][0], lineVector[0][1], lineVector[1][0],
                lineVector[1][1]);

        lineVector[0][0] = lineVector[1][0];
        lineVector[0][1] = lineVector[1][1];

        repaint();
    }

    private double ComputeDxdt() {
        return Math.cos(theta);
        /**
         * + (1 / (lineVector[0][1] - 5)) + (1 / (lineVector[0][1] + 5));
         */
    }

    private double ComputeDydt() {
        return Math.sin(theta);
        /**
         * + (1 / (lineVector[0][1] - 5)) + (1 / (lineVector[0][1] + 5));
         */
    }

    private Color ComputeColor(double t) {
        // System.out.println(Math.cos(t) * Math.cos(t));
        return Color.getHSBColor((float) (Math.cos(t) * Math.cos(t)), 1f,
                1f);
    }

    private void drawCurveOrientation(double theta, double omega) {
        wd.clearImageWithBackGround();
        wd.setDrawColor(Color.red);
        wd.drawLine(0, 0, Math.cos(theta), Math.sin(theta));
        wd.setDrawColor(Color.blue);
        wd.drawLine(0, 0, Math.cos(omega), Math.sin(omega));
    }

    public void paint(Graphics g) {
        if (Math.abs(wChanged - this.getWidth()) > 0
                || Math.abs(hChanged - this.getHeight()) > 0) {
            wd.setWindowSize(this.getWidth(), this.getHeight());
            wChanged = this.getWidth();
            hChanged = this.getHeight();
        }
        update(g);
    }

    public void update(Graphics g) {
        displaySpeed();
        run();
        wd.paint(g);
    }

    public void displaySpeed() {
        // wd.drawString(""+speed/maxSpeed, 4.5, 3);
        // wd.setDrawColor(Color.white);
        // wd.drawLine(4.5, 3, 5, 3);
        // wd.drawLine(4.5, 3, 4.5, -3);
        // wd.drawLine(4.5, -3, 5, -3);
        // wd.setDrawColor(Color.blue);
        // wd.drawFilledRectangle(4.6, -2.9, 0.4, speed/maxSpeed * 6);
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
        lineVector[0][0] = wd.InverseCoordX(arg0.getX());
        lineVector[0][1] = wd.InverseCoordY(arg0.getY());
        alfa = 10 * rand.nextDouble() - 5;
        omega = 2 * Math.PI * rand.nextDouble() - Math.PI;
        theta = 2 * Math.PI * rand.nextDouble() - Math.PI;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_H) {
            HELP_FRAME.setVisible(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_PLUS) {
            speed = Math.min(speed + 1, maxSpeed);
        } else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
            speed = Math.max(speed - 1, 1);
        } else {
            try {
                File outputfile = new File("RandomCurve.png");
                ImageIO.write(wd.getImage(), "png", outputfile);
                System.out.println("Image saved");
            } catch (IOException e1) {
                // nothing on purpose;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

}
