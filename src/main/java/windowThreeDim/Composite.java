package windowThreeDim;

import algebra.Matrix;
import algebra.TriVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Composite extends Element {
    private List<Element> elementList;

    public Composite() {
        super(0);
        elementList = new ArrayList<Element>();
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

    public void forEach(Consumer<Element> lambda) {
        elementList.forEach(lambda);
    }

    public Stream<Element> stream() {
        return elementList.stream();
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

    public double getDistanceStandardDeviation() {
        final TriVector centroid = this.centroid();
        final double reduce = this.stream().flatMap(x -> Arrays.stream(x.getPointsArray())).mapToDouble(x -> TriVector.sub(x, centroid).norm()).reduce(0, (a, b) -> a + b);
        return reduce / this.getNumOfPoints();
    }

    public List<Element> getElementList() {
        return elementList;
    }
}
