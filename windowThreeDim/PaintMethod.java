package windowThreeDim;

import algebra.Matrix;
import algebra.TriVector;
import window.ImageWindow;

public interface PaintMethod {
	
	public void paintQuad(Quad element);
	public void paintTriangle(Triangle element);
	public void paintLine(Line element);
	public void setWindow(ImageWindow wd);
	public void setCamera(Matrix cameraBasis, TriVector eyePos);
	public Matrix getCameraBasis();
	public TriVector getEyePos();
	public void init();
	public void paintPoint(Point element);
	public void paintStringElement(StringElement stringElement);
}
