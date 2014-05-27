package windowThreeDim;

import java.awt.Color;

import algebra.Matrix;
import algebra.TriVector;

public class Triangle extends Element{
	
	public Triangle(TriVector p1, TriVector p2, TriVector p3) {
		nPoints = 3;
		p = new TriVector[nPoints];
		p[0] = p1;
		p[1] = p2;
		p[2] = p3;
		colorPoint = new Color[nPoints];
	}
	
	@Override
	public void draw(PaintMethod visitor) {
		visitor.paintTriangle(this);
		
	}
	/**
	 * 
	 * @param p : must be a 3 - dim array of TriVector;
	 * @return : return normalized normal;
	 */
	public static TriVector calcNormal(TriVector[] p) {
		TriVector v1 = new TriVector();
		TriVector v2 = new TriVector();
		Matrix m = TriVector.subMatrix(p[1], p[0]);
		Matrix m2 = TriVector.subMatrix(p[2], p[0]);
		v1.setXYZMat(m);
		v2.setXYZMat(m2);
		v1 = TriVector.vectorProduct(v1, v2);
		if(v1.getX() == 0.0 && v1.getY() == 0.0 && v1.getZ() == 0.0)
			return v1;
		else
			v1.normalize();
		return v1;
	}

	@Override
	public Element copy() {
		TriVector[] pCopy = new TriVector[this.nPoints];
		for (int i = 0; i < pCopy.length; i++) {
			pCopy[i] = p[i].copy();
		}
		Element ret = new Triangle(pCopy[0], pCopy[1], pCopy[2]);
		ret.setColor(this.getColor());
		return ret;
	}
}
