package windowThreeDim;

import algebra.Matrix;
import algebra.TriVector;
import window.ImageWindow;

public interface PaintMethod {

    void paintQuad(Quad element);

    void paintTriangle(Triangle element);

    void paintLine(Line element);

    void setWindow(ImageWindow wd);

    void setCamera(Matrix cameraBasis, TriVector eyePos);

    Matrix getCameraBasis();

    TriVector getEyePos();

    void init();

    void paintPoint(Point element);

    void paintStringElement(StringElement stringElement);
}
