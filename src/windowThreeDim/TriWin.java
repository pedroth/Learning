package windowThreeDim;

import algebra.Matrix;
import algebra.TriVector;
import window.ImageWindow;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TriWin {

    private List<Element> l;
    private ImageWindow buffer;
    private PaintMethod method;

    public TriWin(PaintMethod method) {
        setMethod(method);
        buffer = new ImageWindow(-1, 1, -1, 1);
        l = new ArrayList<Element>();
    }

    public TriWin() {
        buffer = new ImageWindow(-1, 1, -1, 1);
        l = new LinkedList<Element>();
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
