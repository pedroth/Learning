package other;

import algebra.TriVector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Stack;

public class PolygonFill extends JFrame implements MouseListener, KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    int[][] points; // point[i][0], x-coordinate of point i. point[i][1]
    // y-coordinate of point i.
    int[][] normals; // normal vector for each pair of points(points[0][0] and
    // points[1][1], points[1][1] and points[2][2], etc
    // ...).
    int nClicks;
    boolean drawP;
    int nVertex;
    int orientation;
    private BufferedImage bImg;
    private boolean recursive;
    private boolean isBruteForce;
    private int wChanged, hChanged;
    private double oldTime;

    public PolygonFill() {
        // Set JFrame title
        super("Draw A PolygonFill In JFrame");

        // Set default close operation for JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set JFrame size
        setSize(400, 400);

        // Make JFrame visible
        setVisible(true);

        nVertex = 4;
        bImg = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        wChanged = this.getWidth();
        hChanged = this.getHeight();
        points = new int[nVertex][2];
        normals = new int[nVertex][2];
        nClicks = 0;
        drawP = false;
        isBruteForce = false;
        Graphics g = bImg.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, wChanged, hChanged);
        this.addMouseListener(this);
        this.addKeyListener(this);
        recursive = false;
        oldTime = System.currentTimeMillis() * 1E-3;
    }

    public static void main(String[] args) {
        // starting app
        new PolygonFill();
    }

    public void paint(Graphics g) {
        if (Math.abs(wChanged - this.getWidth()) > 0 || Math.abs(hChanged - this.getHeight()) > 0) {
            bImg = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics gimg = bImg.getGraphics();
            gimg.setColor(Color.white);
            wChanged = this.getWidth();
            hChanged = this.getHeight();
            gimg.fillRect(0, 0, wChanged, hChanged);
        }
        update(g);
    }

    public void update(Graphics g) {
        if (recursive) {
            floodFill(bImg);
        } else {
            paintComponent(bImg.getGraphics());
        }
        g.drawImage(bImg, 0, 0, wChanged, hChanged, null);
    }

    // not over writing
    public void paintComponent(Graphics g) {
        int[][] box;
        boolean inOut = false;
        int state = 0;
        if (drawP) {
            oldTime = System.currentTimeMillis() * 1E-3;
            box = generateBox();
            calculateNormals();

            // [0][0] xmin, [0][1] ymin, [1][0] xmax, [1][1] ymax;
            for (int i = box[0][0]; i < box[1][0]; i++) {
                for (int j = box[0][1]; j < box[1][1]; j++) {
                    if (isBruteForce) {
                        g.setColor(Color.blue);
                        if (isOnSet(i, j)) {
                            g.drawLine(i, j, i, j);
                            state = 1;
                        } else {
                            if (state == 1) {
                                break;
                            }
                            g.setColor(Color.green);
                            g.drawLine(i, j, i, j);
                        }
                    } else {
                        if (state == 0) {
                            int step = computeMinBoundaryStep(i, j);
                            if (step < 1)
                                state = 1;
                            else {
                                g.setColor(Color.green);
                                g.drawLine(i, j, i, j);
                                j += step - 1;
                            }
                        }
                        if (state == 1) {
                            g.setColor(Color.blue);
                            if (isOnSet(i, j)) {
                                g.drawLine(i, j, i, j);
                                inOut = true;
                            } else {
                                if (inOut)
                                    break;
                                g.setColor(Color.green);
                                g.drawLine(i, j, i, j);
                            }
                        }
                    }
                }
                this.getGraphics().drawImage(bImg, 0, 0, wChanged, hChanged, null);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                state = 0;
                inOut = false;
            }
            double currentTime = System.currentTimeMillis() * 1E-3;
            double timeElapse = currentTime - oldTime;
            double oldTime = currentTime;
            System.out.println("time :" + timeElapse);
        }
        drawP = false;
    }

    /**
     * compute minimum y step to find polygon.
     */
    public int computeMinBoundaryStep(int x, int y) {
        Graphics g = bImg.getGraphics();
        int index = 0;
        for (int i = 0; i < nVertex; i++) {
            int f = -orientation * normals[i][0] * (x - points[i][0]) - orientation * normals[i][1] * (y - points[i][1]);
            if (f > 0)
                index = i;
            else if (f == 0)
                return 0;
        }
        // g.setColor(Color.orange);
        // g.fillRect(points[index][0], points[index][1], 10, 10);
        // g.drawLine(points[index][0], points[index][1], points[index][0]
        // - orientation * normals[index][0], points[index][1]
        // - orientation * normals[index][1]);
        double dist = Math.sqrt(normals[index][0] * normals[index][0] + normals[index][1] * normals[index][1]);
        double nx = normals[index][0] / dist;
        double ny = normals[index][1] / dist;
        double dot = -orientation * nx * (x - points[index][0]) - orientation * ny * (y - points[index][1]);
        double boundaryStep = orientation * ny * dot;
        int ret = (int) Math.floor(boundaryStep);
        return ret;
    }

    public boolean isOnSet(int x, int y) {
        boolean result = true;
        for (int i = 0; i < nVertex; i++) {
            result = result && (orientation * normals[i][0] * (x - points[i][0]) + orientation * normals[i][1] * (y - points[i][1]) >= 0);
        }
        return result;
    }

    // return points of the largest square which has the polygon, and draws the
    // square.
    public int[][] generateBox() {
        int[][] aux = new int[2][2];
        int xmin = points[0][0], xmax = points[0][0], ymin = points[0][1], ymax = points[0][1];
        for (int i = 0; i < nVertex; i++) {
            xmin = smaller(xmin, points[i][0]);
            ymin = smaller(ymin, points[i][1]);
            xmax = bigger(xmax, points[i][0]);
            ymax = bigger(ymax, points[i][1]);
        }
        aux[0][0] = Math.max(xmin, 0);
        aux[0][1] = Math.max(ymin, 0);
        aux[1][0] = Math.min(xmax, wChanged);
        aux[1][1] = Math.min(ymax, hChanged);
        Graphics g = bImg.getGraphics();
        g.setColor(Color.green);
        g.drawRect(xmin, ymin, xmax - xmin, ymax - ymin);

        return aux;
    }

    public int bigger(int x, int y) {
        if (x >= y)
            return x;
        else
            return y;
    }

    public int smaller(int x, int y) {
        if (x <= y)
            return x;
        else
            return y;
    }

    public void calculateNormals() {
        int v1, v2;
        Graphics g = bImg.getGraphics();
        TriVector u = new TriVector(0, 0, 0);
        TriVector v = new TriVector(0, 0, 0);

        for (int i = 0; i < nVertex; i++) {
            v1 = points[(i + 1) % nVertex][0] - points[i][0];
            v2 = points[(i + 1) % nVertex][1] - points[i][1];
            normals[i][0] = v2;
            normals[i][1] = -v1;
        }
        v1 = points[1][0] - points[0][0];
        v2 = points[1][1] - points[0][1];
        u.setX(v1);
        u.setY(v2);
        v1 = points[nVertex - 1][0] - points[0][0];
        v2 = points[nVertex - 1][1] - points[0][1];
        v.setX(v1);
        v.setY(v2);
        double n = TriVector.vectorProduct(u, v).getZ();

        /**
         * don't know why if n < 0 then orientation > 0, but it works
         */
        if (n < 0)
            orientation = 1;
        else
            orientation = -1;

        // for (int i = 0; i < nVertex; i++) {
        // g.setColor(Color.black);
        // g.drawLine(points[i][0], points[i][1],
        // (points[i][0] - orientation * normals[i][0]), (points[i][1] -
        // orientation * normals[i][1]));
        // }
    }

    public void floodFill(BufferedImage img) {
        Graphics g = img.getGraphics();
        if (drawP) {
            oldTime = System.currentTimeMillis() * 1E-3;
            int rx = 0;
            int ry = 0;
            Color c = new Color(0f, 0f, 1f);
            g.setColor(c);
            // draw frontier
            for (int i = 0; i < nVertex; i++) {
                g.drawLine(points[i][0], points[i][1], points[(i + 1) % nVertex][0], points[(i + 1) % nVertex][1]);
                // center of polygon
                rx += points[i][0];
                ry += points[i][1];
            }
            Stack<Integer> sx = new Stack<Integer>();
            Stack<Integer> sy = new Stack<Integer>();
            // center of polygon
            rx /= nVertex;
            ry /= nVertex;
            sx.push(rx);
            sy.push(ry);
            while (!sx.isEmpty()) {
                rx = sx.pop();
                ry = sy.pop();
                if (img.getRGB(rx, ry) != c.getRGB()) {
                    img.setRGB(rx, ry, c.getRGB());
                    sx.push(rx + 1);
                    sy.push(ry);
                    sx.push(rx - 1);
                    sy.push(ry);
                    sx.push(rx);
                    sy.push(ry + 1);
                    sx.push(rx);
                    sy.push(ry - 1);
                }
                this.getGraphics().drawImage(bImg, 0, 0, wChanged, hChanged, null);
            }
            double currentTime = System.currentTimeMillis() * 1E-3;
            double timeElapse = currentTime - oldTime;
            double oldTime = currentTime;
            System.out.println("time :" + timeElapse);
        }
        drawP = false;
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {

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
        points[nClicks % nVertex][0] = e.getX();
        points[nClicks % nVertex][1] = e.getY();
        nClicks++;
        if (nClicks % nVertex == 0) {
            drawP = true;
        } else {
            drawP = false;
        }
        Graphics g = bImg.getGraphics();
        g.setColor(Color.red);
        g.fillOval(e.getX(), e.getY(), 6, 6);
        if (SwingUtilities.isRightMouseButton(e)) {
            recursive = !recursive;
            System.out.println("isRecursive: " + recursive);
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        isBruteForce = !isBruteForce;
        System.out.println("isBruteForce :" + isBruteForce);
       if (e.getKeyCode() == KeyEvent.VK_PLUS) {
            nVertex++;
        } else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
            nVertex--;
        }
        points = new int[nVertex][2];
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }
}
