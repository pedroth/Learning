package windowThreeDim;

import algebra.Matrix;
import algebra.TriVector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Composite extends Element {
    private List<Element> elementList;

    public Composite() {
        elementList = new ArrayList<Element>();
        nPoints = 0;
    }

    public void add(Element element) {
        nPoints += element.nPoints;
        elementList.add(element);
    }

    @Override
    public void draw(PaintMethod visitor) {
        Iterator<Element> ite = elementList.iterator();
        while (ite.hasNext()) {
            Element e = ite.next();
            e.draw(visitor);
        }
    }

    @Override
    public void transform(Matrix m, TriVector v) {
        Iterator<Element> ite = elementList.iterator();
        while (ite.hasNext()) {
            Element e = ite.next();
            e.transform(m, v);
        }
    }

    @Override
    public Element copy() {
        Composite ret = new Composite();
        Iterator<Element> ite = elementList.iterator();
        while (ite.hasNext()) {
            Element e = ite.next().copy();
            ret.add(e);
        }
        return ret;
    }

    public TriVector centroid() {
        int count = 0;
        TriVector acm = new TriVector();
        for (Element obj : elementList) {
            for (int i = 0; i < obj.getNumOfPoints(); i++) {
                acm = TriVector.sum(acm, obj.getNPoint(i));
                count++;
            }
        }
        acm = TriVector.multConst(1.0 / count, acm);
        return acm;
    }

}
