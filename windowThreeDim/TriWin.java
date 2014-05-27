package windowThreeDim;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import window.ImageWindow;
import algebra.Matrix;
import algebra.TriVector;

/**
 * to perform animation with this engine you must not use timers because timer
 * will generate a thread that will call methods of TriWin, when the other
 * thread hasn't finished.
 * 
 * method that will cause damage to your program when using a timer in animation
 * will be deleting element when other tread is still using them
 */
public class TriWin {

	private List<Element> l;
//	private double zMin, zMax, xMin, xMax, yMin, yMax;
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
