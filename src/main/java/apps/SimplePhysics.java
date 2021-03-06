package apps;

import algebra.Matrix;
import visualization.TextFrame;
import window.ImageWindow;
import window.Window2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimplePhysics extends JFrame implements MouseListener, KeyListener, MouseWheelListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static double dx = 1E-09;
    private static final TextFrame HELP_FRAME = TextFrame.builder()
            .addLine("< arrows > : move square")
            .addLine("< c > : follow square")
            .addLine("< [0-9] >,<space> : change map")
            .addLine("< + > : add squares")
            .addLine("Made By Pedroth")
            .buildWithTitle("Help");
    private ImageWindow wd;
    private List<Square> squares;
    private Square square;
    private Random rand;
    private int wChanged;
    private int hChanged;
    private double time;
    private double oldTime;
    private int rot;
    private int nmap = 0;
    private boolean graph = false;
    private int k;
    private double xThrust;
    private double yThrust;
    private double vRanGauss1;
    private double vRanGauss2;
    private double ranAmp1;
    private double ranAmp2;
    private boolean camera;
    private boolean isDebug = false;

    public SimplePhysics(boolean isApplet) {
        super("Simple Physics - Press h for Help");
        wd = new ImageWindow(-5, 5, -5, 5);
        this.setSize(500, 500);
        wChanged = this.getWidth();
        hChanged = this.getHeight();
        // Set default close operation for JFrame
        if (!isApplet) {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        wd.setWindowSize(wChanged, hChanged);
        time = 0.0;
        wd.setBackGroundColor(Color.white);
        wd.clearImageWithBackGround();
        squares = new ArrayList<>();
        squares.add(new Square(0, 4.9, 0.1));
        square = squares.get(0);
        rot = 0;
        rand = new Random();
        oldTime = System.currentTimeMillis();
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
        k = 0;
        xThrust = 0.0;
        yThrust = 0.0;
        this.requestFocus();
        this.setFocusable(true);
        camera = false;
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new SimplePhysics(false);
    }

    public void paint(Graphics g) {
        if (Math.abs(wChanged - this.getWidth()) > 0 || Math.abs(hChanged - this.getHeight()) > 0) {
            wd.setWindowSize(this.getWidth(), this.getHeight());
            wChanged = this.getWidth();
            hChanged = this.getHeight();
        }
        update(g);
    }

    public void update(Graphics g) {
        double dt = (System.currentTimeMillis() - oldTime) * 1E-3;
        oldTime = System.currentTimeMillis();
        updateAnimation(dt);
        squares.forEach(x -> run(x, dt));
        time += dt;
        wd.paint(g);
    }

    public void updateAnimation(double dt) {
        if (graph) {
            int oldk = k;
            k = (int) (time / 30);
            if (oldk != k) {
                wd.clearImageWithBackGround();
            }
            wd.setViewWindow(30 * k, 30 + 30 * k, -7, 7);
            wd.drawPoint(time, square.angle, 1);
        } else {
            wd.clearImageWithBackGround();
            if (camera) {
                wd.setViewWindow(square.xCenter + square.velXCenter * 0.01 - 5, square.xCenter + square.velXCenter * 0.01 + 5, square.yCenter + square.velYCenter * 0.01 - 5, square.yCenter + square.velYCenter * 0.01 + 5);
            }
            this.drawLandScape();
            wd.setDrawColor(Color.red);
            wd.drawLine(square.getXCenter(), square.getYCenter(), square.getXCenter() + square.getXCenterAceleration(), square.getYCenter() + square.getYCenterAceleration());
            wd.setDrawColor(Color.blue);
            wd.drawLine(square.getXCenter(), square.getYCenter(), square.getXCenter() + square.getXCenterVelocity(), square.getYCenter() + square.getYCenterVelocity());
            squares.forEach(x -> x.draw(wd));
        }
    }

    public void drawLandScape() {
        double xmin = wd.getXMin();
        double xmax = wd.getXMax();
        double h = minimalStepFunction(xmin);
        while (xmin < xmax) {
            wd.setDrawColor(Color.green);
            final double y = function(xmin, time);
            final double yh = function(xmin + h, time);
            wd.drawLine(xmin, y, xmin + h, yh);
            if (isDebug) {
                wd.setDrawColor(Color.red);
                wd.drawFilledRectangle(xmin, y, 0.1, 0.1);
            }
            wd.setDrawColor(Color.green);
            if (wd.getYMin() < y) {
                wd.drawFilledQuadrilateral(xmin, wd.getYMin(), xmin + h, wd.getYMin(), xmin + h, yh, xmin, y);
            }
            xmin = xmin + h;
            h = minimalStepFunction(xmin);
        }
    }

    public double function(double x, double t) {
        switch (nmap) {
            case 0:
                return Math.sin(2 * x) * Math.cos(1.5 * x);
            case 1:
                return 3 * Math.exp(-Math.pow(x - Math.sin(t), 2)) + Math.exp(-Math.pow(x - 3 * Math.sin(t), 2)) + Math.exp(-Math.pow(x - 2 * Math.cos(t), 2)) - 3;
            case 2:
                return 0.25 * x;
            case 3:
                return 0.0;
            case 4:
                return Math.cos(x) * Math.sin(x) - Math.sin(x);
            case 5:
                return Math.sin(x) * Math.sin(t);
            case 6:
                return Math.exp(Math.cos(x - 0.5 * t)) * Math.sin(x - t);
            case 7:
                return Math.sin(x + t);
            case 8:
                return 3 * Math.exp(-Math.pow(x, 2)) + Math.exp(-Math.pow(x - 1.5, 2)) + Math.exp(-Math.pow(x - 2, 2)) - 3;
            case 9:
                return Math.sin(vRanGauss1 * x - vRanGauss2 * t) + Math.cos(vRanGauss2 * x - vRanGauss1 * t);
            default:
                return ((-2 + 4 * ranAmp1) * Math.cos(t)) * Math.exp(-(x - (Math.sin(5 * vRanGauss1 * t))) * (x - Math.sin(5 * vRanGauss1 * t))) + ((-2 + 4 * ranAmp2) * Math.cos(t)) * Math.exp(-(x - (Math.sin(5 * vRanGauss2 * t))) * (x - Math.sin(5 * vRanGauss2 * t)));
        }
    }

    public double nthDerivativeFunction(double x0, int n) {
        if (n == 0)
            return function(x0, time);
        else
            return (nthDerivativeFunction(x0 + dx * Math.pow(10, n + 1), n - 1) - nthDerivativeFunction(x0 - dx * Math.pow(10, n + 1), n - 1)) / (2 * dx * Math.pow(10, n + 1));
    }

    public double minimalStepFunction(double x0) {
        final double min = 0.01;
        double df = 1.0;
        double epsilon = 0.001;
        boolean logic = false;

        /**
         * DISCRETIZE TO MUCH
         */
        for (int i = 2; i < 7 && !logic; i++) {
            df = Math.abs(nthDerivativeFunction(x0, i));
            if (df > min) {
                logic = true;
                df = Math.pow((factorial(i) * epsilon) / Math.abs(df), 1.0 / (i));
            }
        }
        if (logic) {
            if (df >= wd.pxlXStep())
                return df;
            else
                return wd.pxlXStep();
        } else
            return 1;

        // for (int i = 1; i < 5 && !logic; i = 2 * i + 1) {
        //
        // df = Math.abs(nthDerivativeFunction(x0, i + 1));
        // if (df > min) {
        // logic = true;
        // df = Math.pow((factorial(i + 2) * epsilon) / (2 * df),
        // 1.0 / (i + 2));
        // }
        // }
        //
        // if (logic) {
        // if (df >= wd.pxlXStep())
        // return df;
        // else
        // return wd.pxlXStep();
        // } else
        // return 1;

    }

    double factorial(int x) {
        int acc = 1;
        for (int i = x; i > 0; i--)
            acc = acc * i;
        return (double) acc;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() >= '0' && e.getKeyChar() <= '9') {
            nmap = e.getKeyChar() - '0';
            vRanGauss1 = rand.nextDouble();
            vRanGauss2 = rand.nextDouble();
            ranAmp1 = rand.nextDouble();
            ranAmp2 = rand.nextDouble();
        } else if (e.getKeyChar() == 'g') {
            graph = !graph;
            wd.clearImageWithBackGround();
            wd.setViewWindow(-5, 5, -5, 5);
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            nmap = 10;
            vRanGauss1 = rand.nextDouble();
            vRanGauss2 = rand.nextDouble();
            ranAmp1 = rand.nextDouble();
            ranAmp2 = rand.nextDouble();
        } else if (e.getKeyCode() == KeyEvent.VK_C) {
            camera = !camera;
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            yThrust = 20.0;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            yThrust = -10.0;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            xThrust = -10.0;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            xThrust = 10.0;
        } else if (e.getKeyCode() == KeyEvent.VK_PLUS) {
            squares.add(new Square(square.getXCenter(), square.getYCenter(), square.radius));
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            this.isDebug = !isDebug;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
            xThrust = 0;
            yThrust = 0;
        }
        if (e.getKeyCode() == KeyEvent.VK_H) {
            HELP_FRAME.setVisible(true);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Matrix vertex = square.getVertexs();
        double x0 = wd.InverseCoordX(e.getX());
        double y0 = wd.InverseCoordY(e.getY());
        square.setXCenter(x0);
        square.setYCenter(y0);
        // square.setXCenterVelocity(-wd.InverseCoordY(e.getY()));
        // square.setYCenterVelocity(wd.InverseCoordX(e.getX()));
        vertex.setMatrix(1, 1, x0 + square.getWidth());
        vertex.setMatrix(2, 1, y0 + square.getWidth());
        vertex.setMatrix(1, 2, x0 - square.getWidth());
        vertex.setMatrix(2, 2, y0 + square.getWidth());
        vertex.setMatrix(1, 3, x0 - square.getWidth());
        vertex.setMatrix(2, 3, y0 - square.getWidth());
        vertex.setMatrix(1, 4, x0 + square.getWidth());
        vertex.setMatrix(2, 4, y0 - square.getWidth());
        square.setXCenterVelocity(0.0);
        square.setYCenterVelocity(0.0);
        square.setMomentum(0.0);
        square.setAngle(0.0);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent arg0) {
        rot += arg0.getWheelRotation();
        wd = new ImageWindow(-5 - rot, 5 + rot, -5 - rot, 5 + rot);
        wd.setWindowSize(wChanged, hChanged);
        wd.setBackGroundColor(Color.white);
        wd.clearImageWithBackGround();
    }

    public void run(Square square, double dt) {
        double contactXForce;
        double contactYForce;
        double r1;
        double r2;
        double ax;
        double ay;
        double torque = 0.0;
        double omega;
        double dx;
        double dy;
        double ds;
        double dot;
        double minGetOut = 0;
        double dfdt;
        boolean logic = false;
        double epsilon = 10;
        Matrix ai = new Matrix(2, 4);
        Matrix vertex = square.getVertexs();

        for (int i = 1; i <= 4; i++) {
            // check contact with some precision
            dfdt = 1 / 1E-09 * (function(vertex.selMatrix(1, i), time + 1E-09) - function(vertex.selMatrix(1, i), time));
            if ((vertex.selMatrix(2, i)) + square.velYCenter * 0.01 - (function(vertex.selMatrix(1, i), time) + dfdt * 0.01) < 1E-02) {
                logic = true;

                // figure out max distance between floor and vertex
                double aux = vertex.selMatrix(2, i) - function(vertex.selMatrix(1, i), time);
                if (minGetOut > aux) {
                    minGetOut = aux;
                }

                // contact force on vertex[i]
                dx = -nthDerivativeFunction(vertex.selMatrix(1, i), 1);
                dy = 1;
                ds = Math.sqrt(dx * dx + dy * dy);
                dx = (1 / ds) * dx;
                dy = (1 / ds) * dy;
                dot = -(dx * ax(vertex.selMatrix(1, i), vertex.selMatrix(2, i), time) + dy * ay(vertex.selMatrix(1, i), vertex.selMatrix(2, i), time));
                contactXForce = 1 * dot * dx;
                contactYForce = 1 * dot * dy;

            } else {
                contactXForce = 0.0;
                contactYForce = 0.0;
            }

            ai.setMatrix(1, i, ax(vertex.selMatrix(1, i), vertex.selMatrix(2, i), time) + contactXForce + xThrust);
            ai.setMatrix(2, i, ay(vertex.selMatrix(1, i), vertex.selMatrix(2, i), time) + contactYForce + yThrust);
            // wd.drawLine(vertex.selMatrix(1, i), vertex.selMatrix(2, i),
            // ai.selMatrix(1, i)+vertex.selMatrix(1, i), ai.selMatrix(2,
            // i)+vertex.selMatrix(2, i));

        }

        contactXForce = 0;
        contactYForce = 0;

        if (logic) {
            // floor velocity
            if ((dfdt = 1 / 1E-09 * (function(square.xCenter, time + 1E-09) - function(square.xCenter, time))) < 0.0)
                dfdt = 0.0;
            // collision velocity
            dx = -nthDerivativeFunction(square.getXCenter(), 1);
            dy = 1;
            ds = Math.sqrt(dx * dx + dy * dy);
            dx = (1 / ds) * dx;
            dy = (1 / ds) * dy;
            dot = -(dx * square.getXCenterVelocity() + dy * square.getYCenterVelocity());
            square.setXCenterVelocity(1.5 * dot * dx + square.getXCenterVelocity());
            square.setYCenterVelocity(1.5 * dot * dy + square.getYCenterVelocity());

            // get out of floor speed
            dot = -epsilon * (dy * minGetOut);
            square.setXCenterVelocity(square.getXCenterVelocity() + dot * dx);
            square.setYCenterVelocity(square.getYCenterVelocity() + dot * dy);

            // floor movement velocity
            dot = (dy * dfdt);
            square.setXCenterVelocity(square.getXCenterVelocity() + dot * dx);
            square.setYCenterVelocity(square.getYCenterVelocity() + dot * dy);

            // contact force calculation
            dot = -(dx * ax(square.xCenter, square.yCenter, time) + dy * ay(square.xCenter, square.yCenter, time));
            contactXForce = 1 * dot * dx;
            contactYForce = 1 * dot * dy;

            // wd.drawLine(square.xCenter, square.yCenter, square.xCenter +
            // dot*dx, square.yCenter + dot*dy);

            // floor movement acceleration
            if ((dfdt = (1 / (1E-04 * 1E-04)) * (function(square.xCenter, time + 2 * 1E-04) - function(square.xCenter, time))) < 0.0)
                dfdt = 0.0;
            dot = (dy * dfdt);
            contactXForce += 0.01 * dot * dx;
            contactYForce += 0.01 * dot * dy;

        }
        ax = ax(square.xCenter, square.yCenter, time) + contactXForce + xThrust;
        ay = ay(square.xCenter, square.yCenter, time) + contactYForce + yThrust;

        square.setXCenterAceleration(ax);
        square.setYCenterAceleration(ay);

        double vx = square.getXCenterVelocity() + square.getXCenterAceleration() * dt;
        double vy = square.getYCenterVelocity() + square.getYCenterAceleration() * dt;
        double x = square.getXCenter() + vx * dt;
        double y = square.getYCenter() + vy * dt;

        square.setXCenter(x);
        square.setYCenter(y);
        square.setXCenterVelocity(vx);
        square.setYCenterVelocity(vy);

        Matrix aux = new Matrix(2, 4);
        Matrix aux2 = new Matrix(2, 4);
        for (int i = 1; i <= 4; i++) {
            r1 = square.getVertexs().selMatrix(1, i) - square.getXCenter();
            r2 = square.getVertexs().selMatrix(2, i) - square.getYCenter();
            ax = ai.selMatrix(1, i);
            ay = ai.selMatrix(2, i);
            aux.setMatrix(1, i, square.getXCenter());
            aux.setMatrix(2, i, square.getYCenter());
            aux2.setMatrix(1, i, vx * dt);
            aux2.setMatrix(2, i, vy * dt);
            torque += (r1 * ay - r2 * ax);
        }
        square.setTorque(1 * torque);

        if (logic) {
            square.setMomentum(3 * torque);
        }

        omega = square.getMomentum() + square.getTorque() * dt;
        double theta = square.getAngle() + omega * dt;

        // System.out.printf("%.3f\t %.3f\t %.3f\t %.3f\t \n", time, torque,
        // omega, theta);
        vertex.setMatrix(1, 1, x + square.getWidth());
        vertex.setMatrix(2, 1, y + square.getWidth());
        vertex.setMatrix(1, 2, x - square.getWidth());
        vertex.setMatrix(2, 2, y + square.getWidth());
        vertex.setMatrix(1, 3, x - square.getWidth());
        vertex.setMatrix(2, 3, y - square.getWidth());
        vertex.setMatrix(1, 4, x + square.getWidth());
        vertex.setMatrix(2, 4, y - square.getWidth());

        Matrix rot = new Matrix(2, 2);
        rot.setMatrix(1, 1, Math.cos(theta));
        rot.setMatrix(1, 2, -Math.sin(theta));
        rot.setMatrix(2, 1, Math.sin(theta));
        rot.setMatrix(2, 2, Math.cos(theta));
        Matrix result = Matrix.subMatrix(square.getVertexs(), aux);
        result = Matrix.multiMatrix(rot, result);
        aux = Matrix.sumMatrix(aux, aux2);
        result = Matrix.sumMatrix(aux, result);

        square.setVertex(result);

        square.setMomentum(omega);
        square.setAngle(theta);

        repaint();
    }

    public double ax(double x, double y, double t) {
        return
                // -x*Math.exp(-t);
                // -x;
                0;
        // -(x-1);
        // -y;
        // -(y-3)/((x-3) + (y-3));
        // -100*(y - square.yCenter);
    }

    public double ay(double x, double y, double t) {
        return
                // -y*Math.exp(-t);
                -9.8;
        // 1 / ((y - function(x, t)) * (y - function(x, t)));
        // -1.0;
        // -(y-1);
        // x;
        // (x-3)/((x-3) + (y-3));
        // 100*(x - square.xCenter) - 1;
    }


    public class Square {
        private double xCenter;
        private double yCenter;
        private double radius;
        private double velXCenter;
        private double velYCenter;
        private double aceXCenter;
        private double aceYCenter;
        private Matrix vertex;
        private double torque;
        private double momentum;
        private double angle;

        Square(double x0, double y0, double _radius) {
            xCenter = x0;
            yCenter = y0;
            radius = _radius;
            velXCenter = 0.0;
            velYCenter = 0.0;
            aceXCenter = 0.0;
            aceYCenter = 0.0;
            vertex = new Matrix(2, 4);
            vertex.setMatrix(1, 1, x0 + radius);
            vertex.setMatrix(2, 1, y0 + radius);
            vertex.setMatrix(1, 2, x0 - radius);
            vertex.setMatrix(2, 2, y0 + radius);
            vertex.setMatrix(1, 3, x0 - radius);
            vertex.setMatrix(2, 3, y0 - radius);
            vertex.setMatrix(1, 4, x0 + radius);
            vertex.setMatrix(2, 4, y0 - radius);
            torque = 0.0;
            momentum = 0.0;
            angle = 0.0;
        }

        public double getXCenter() {
            return xCenter;
        }

        public void setXCenter(double x) {
            xCenter = x;

        }

        public double getYCenter() {
            return yCenter;
        }

        public void setYCenter(double y) {
            yCenter = y;
        }

        public double getXCenterVelocity() {
            return velXCenter;
        }

        public void setXCenterVelocity(double vx) {
            velXCenter = vx;
        }

        public double getYCenterVelocity() {
            return velYCenter;
        }

        public void setYCenterVelocity(double vy) {
            velYCenter = vy;
        }

        public double getXCenterAceleration() {
            return aceXCenter;
        }

        public void setXCenterAceleration(double ax) {
            aceXCenter = ax;
        }

        public double getYCenterAceleration() {
            return aceYCenter;
        }

        public void setYCenterAceleration(double ay) {
            aceYCenter = ay;
        }

        public double getAngle() {
            return angle;
        }

        public void setAngle(double a) {
            angle = a;
        }

        public double getMomentum() {
            return momentum;
        }

        public void setMomentum(double m) {
            momentum = m;
        }

        public double getTorque() {
            return torque;
        }

        public void setTorque(double t) {
            torque = t;
        }

        public double getWidth() {
            return radius;
        }

        public void setWidth(double w) {
            radius = w;
        }

        public Matrix getVertexs() {
            return vertex;
        }

        public void setVertex(Matrix v) {
            vertex = v;
        }

        public void draw(Window2D w) {
            w.drawFilledQuadrilateral(vertex.selMatrix(1, 1), vertex.selMatrix(2, 1), vertex.selMatrix(1, 2), vertex.selMatrix(2, 2), vertex.selMatrix(1, 3), vertex.selMatrix(2, 3), vertex.selMatrix(1, 4), vertex.selMatrix(2, 4));
        }
    }
}
