package windowThreeDim;

import algebra.Matrix;
import algebra.TriVector;
import window.ImageWindow;

import java.util.LinkedList;
import java.util.List;

public class TriWin {

    private List<Element> l;
    private ImageWindow buffer;
    private PaintMethod method;

    public TriWin() {
        buffer = new ImageWindow(-1, 1, -1, 1);
        l = new LinkedList<>();
    }

    public TriWin(double alpha) {
        setAlpha(alpha);
        l = new LinkedList<>();
    }

    public double getAlpha() {
        final double dy = buffer.getYMax() - buffer.getYMin();
        return 2 * Math.atan(dy / 2);
    }

    /**
     * @param alpha field of view angle for both width and height
     */
    public void setAlpha(double alpha) {
        double windowSize = Math.tan(alpha / 2);
        int windowWidth, windowHeight;
        if (buffer != null) {
            windowWidth  = buffer.getWindowWidth();
            windowHeight = buffer.getWindowHeight();
        } else {
            windowWidth  = 100;
            windowHeight = 100;
        }
        buffer = new ImageWindow(-windowSize, windowSize, -windowSize, windowSize);
        buffer.setWindowSize(windowWidth, windowHeight);
        if (method != null) {
            method.setWindow(buffer);
        }
    }

    public void addtoList(Element e) {
        l.add(e);
    }

    public void removeAllElements() {
        l.removeAll(l);
    }

    public ImageWindow getBuffer() {
        return buffer;
    }

    public void setBuffer(ImageWindow buffer) {
        this.buffer = buffer;
    }

    public PaintMethod getMethod() {
        return method;
    }

    public void setMethod(PaintMethod method) {
        this.method = method;
        method.setWindow(buffer);
    }

    public void setWindowSize(int width, int height) {
        buffer.setWindowSize(width, height);
    }

    public void drawElements() {
        method.init();
        for (Element e : l) {
            e.draw(method);
        }
    }

    public void setCamera(Matrix cameraBasis, TriVector eyePos) {
        method.setCamera(cameraBasis, eyePos);
    }

}
