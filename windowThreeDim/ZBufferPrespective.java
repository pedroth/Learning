package windowThreeDim;

import java.awt.Color;
import java.awt.Graphics;

import window.ImageWindow;
import algebra.Matrix;
import algebra.TriVector;

public class ZBufferPrespective implements PaintMethod {
	protected Matrix cameraBasis;
	protected Matrix inverseCameraBasis;
	protected TriVector eyePos;
	protected ImageWindow wd;
	protected double[][] zBuffer;
	protected int orientation;
	protected boolean isCullBack;
	protected TriVector[] intBuffer;
	/**
	 * distance to drawing window/pane
	 */
	protected double d;

	protected double zd;
	/**
	 * interpolated z , changing for each element and pxl
	 */
	protected double z;

	public ZBufferPrespective() {
		d = 1;
		zd = d / 2;
		isCullBack = false;
	}

	@Override
	public void init() {
		zBuffer = new double[wd.getWindowWidth()][wd.getWindowHeight()];
		for (int i = 0; i < wd.getWindowWidth(); i++)
			for (int j = 0; j < wd.getWindowHeight(); j++)
				zBuffer[i][j] = Double.MAX_VALUE;
	}

	public boolean isCullBack() {
		return isCullBack;
	}

	/**
	 * 
	 * @param isCullBack
	 *            if true cull back face is on
	 */
	public void setCullBack(boolean isCullBack) {
		this.isCullBack = isCullBack;
	}

	/**
	 * does not change p reference
	 * 
	 * @param p
	 *            : point being transform to camera coordinate
	 * @return transformed point
	 */
	public TriVector transform(TriVector p) {
		Matrix m = TriVector.subMatrix(p, eyePos);
		TriVector res = new TriVector(0, 0, 0);
		res.setXYZMat(m);
		res.Transformation(inverseCameraBasis);
		return res;
	}

	/**
	 * does not change p reference
	 * 
	 * @param p
	 *            : point being project
	 * @return Projected point
	 */
	public TriVector project(TriVector p) {
		TriVector res = new TriVector(0, 0, 0);
		res.setXYZMat(p);
		/* x = (x * d) / z */
		double x = (res.selMatrix(1, 1) * d) / res.selMatrix(3, 1);
		/* y = (y * d) / z */
		double y = (res.selMatrix(2, 1) * d) / res.selMatrix(3, 1);
		res.setMatrix(1, 1, x);
		res.setMatrix(2, 1, y);
		return res;
	}

	public Matrix getCameraBasis() {
		return cameraBasis;
	}

	public void setCameraBasis(Matrix cameraBasis) {
		this.cameraBasis = cameraBasis;
		this.inverseCameraBasis = Matrix.transpose(this.cameraBasis);
	}

	public TriVector getEyePos() {
		return eyePos;
	}

	public void setEyePos(TriVector eyePos) {
		this.eyePos = eyePos;
	}

	public ImageWindow getWindow() {
		return wd;
	}

	@Override
	public void setWindow(ImageWindow wd) {
		this.wd = wd;
		zBuffer = new double[wd.getWindowWidth()][wd.getWindowHeight()];
	}

	@Override
	public void setCamera(Matrix cameraBasis, TriVector eyePos) {
		setCameraBasis(cameraBasis);
		setEyePos(eyePos);
	}

	@Override
	public void paintQuad(Quad element) {
		Triangle q1 = new Triangle(element.getNPoint(0), element.getNPoint(1),
				element.getNPoint(2));
		Triangle q2 = new Triangle(element.getNPoint(2), element.getNPoint(3),
				element.getNPoint(0));
		
		for (int i = 0; i < element.nPoints - 1; i++) {
			q1.setColorPoint(element.getColorPoint(i), i);
			int j = ((i + 2) % element.nPoints);
			q2.setColorPoint(element.getColorPoint(j), i);
		}
		
		q1.setColor(element.getColor());
		q2.setColor(element.getColor());

		paintTriangle(q1);
		paintTriangle(q2);
	}

	@Override
	public void paintTriangle(Triangle element) {
		/**
		 * initialization
		 */
		TriVector[] p = new TriVector[element.nPoints];
		intBuffer = new TriVector[element.nPoints];
		for (int i = 0; i < element.nPoints; i++)
			intBuffer[i] = new TriVector();

		/**
		 * transformation to camera coordinates
		 */
		p[0] = element.getNPoint(0);
		p[0] = transform(p[0]);
		for (int i = 0; i < element.nPoints; i++) {
			if (p[(i + 1) % element.nPoints] == null) {
				p[i + 1] = element.getNPoint(i + 1);
				p[i + 1] = transform(p[i + 1]);
			}
		}
		/**
		 * Calculate of the integer coordinates
		 */
		for (int i = 0; i < element.nPoints; i++) {
			intBuffer[i].setZ(p[i].getZ());
			p[i] = project(p[i]);
			intBuffer[i].setX(wd.changeCoordX(p[i].getX()));
			intBuffer[i].setY(wd.changeCoordY(p[i].getY()));
		}

		/**
		 * calculating normal in integer coordinates
		 */
		TriVector normal = Triangle.calcNormal(intBuffer);
		if (checkIfPointsAreWellProjected(p))
			drawZ(intBuffer, element.nPoints, normal, element);
	}

	/**
	 * 
	 * @param p
	 *            points in projected coordinates
	 * @return check if p belongs to the projected canvas
	 */
	public boolean checkIfPointsAreWellProjected(TriVector[] p) {
		boolean logic = true;
		double border = 3;
		for (int i = 0; i < p.length; i++) {
			logic = logic
					&& (p[i].getX() > -border && p[i].getX() < border
							&& p[i].getY() > -border && p[i].getY() < border && p[i]
							.getZ() > 0);
		}
		return logic;
	}

	/**
	 * 
	 * @param intBuffer
	 *            points in integer coordinates
	 * @param nPoints
	 *            number of points of the polygon
	 * @param normal
	 *            normal vector of the polygon
	 * @param element
	 *            polygon
	 */
	protected void drawZ(TriVector[] intBuffer, int nPoints, TriVector normal,
			Element element) {

		if (isCullBack && normal.getZ() > 0)
			return;
		
		int[][] polyNormals;
		int[][] box;
		Graphics g = wd.getGraphics();
		double z0;
		boolean inOut = false;

		/**
		 * [0][0] xmin, [0][1] ymin, [1][0] xmax, [1][1] ymax;
		 * 
		 * calculation of the box containing the polygon in integer coordinates
		 */
		box = generateBox(intBuffer);

		polyNormals = calculateNormals(intBuffer);

		/**
		 * z = -(n1/n3 * (x - x0) + n2/n3 * (y - y0)) + z0
		 * 
		 * we can use at (x0,y0,z0) a random point in the polygon
		 */

		double nx = normal.getX() / normal.getZ();

		double ny = normal.getY() / normal.getZ();

		double dx = box[0][0] - intBuffer[0].getX();

		double dy = box[0][1] - intBuffer[0].getY();

		z = -nx * dx - ny * dy + intBuffer[0].getZ();

		z0 = z;

		/**
		 * is inside a triangle determination
		 */
		int nVertex = polyNormals.length;
		int[] dot0 = new int[nVertex];
		int[] dot = new int[nVertex];
		for (int i = 0; i < nVertex; i++) {
			dot0[i] = (int) (orientation * polyNormals[i][0]
					* (box[0][0] - intBuffer[i].getX()) + orientation
					* polyNormals[i][1] * (box[0][1] - intBuffer[i].getY()));
			dot[i] = dot0[i];
		}

		int[] pixels = ((java.awt.image.DataBufferInt) wd.getImage()
				.getRaster().getDataBuffer()).getData();

		int w = wd.getWindowWidth();
		/**
		 * z = - nx * dx - ny * dy;
		 * 
		 * painting polygon with z-buffer algorithm
		 */
		for (int i = box[0][0]; i < box[1][0]; i++) {
			for (int j = box[0][1]; j < box[1][1]; j++) {
				if (isPositive(dot) && z < zBuffer[i][j] && z > zd) {
					pixels[j * w + i] = getFragmentColor(element, i, j)
							.getRGB();
					zBuffer[i][j] = z;
					inOut = true;
				} else {
					if (inOut && !isPositive(dot)) {
						break;
					}
				}
				z += -ny;
				for (int k = 0; k < nVertex; k++) {
					dot[k] += orientation * polyNormals[k][1];
				}
			}
			z0 += -nx;
			z = z0;
			for (int k = 0; k < nVertex; k++) {
				dot0[k] += orientation * polyNormals[k][0];
				dot[k] = dot0[k];
			}
			inOut = false;
		}
	}

	/**
	 * 
	 * @param points
	 *            points in integer coordinate
	 * @return points of the smallest square(not proved) that contains input
	 *         points
	 */
	protected int[][] generateBox(TriVector[] points) {
		int[][] aux = new int[2][2];
		int xmin = (int) points[0].getX(), xmax = (int) points[0].getX(), ymin = (int) points[0]
				.getY(), ymax = (int) points[0].getY();
		for (int i = 0; i < points.length; i++) {
			xmin = Math.min(xmin, (int) points[i].getX());
			ymin = Math.min(ymin, (int) points[i].getY());
			xmax = Math.max(xmax, (int) points[i].getX());
			ymax = Math.max(ymax, (int) points[i].getY());
		}
		aux[0][0] = Math.max(xmin, 0);
		aux[0][1] = Math.max(ymin, 0);
		aux[1][0] = Math.min(xmax, wd.getWindowWidth());
		aux[1][1] = Math.min(ymax, wd.getWindowHeight());

		// wd.getGraphics().drawRect(xmin, ymin, xmax - xmin, ymax - ymin);
		return aux;
	}

	/**
	 * 
	 * @param points
	 *            points of the polygon in 2d integer coordinate
	 * @return set of normal vectors in 2d integer coordinates, where each
	 *         normal is perpendicular to a line of the polygon
	 */
	protected int[][] calculateNormals(TriVector[] points) {
		int v1, v2;
		int normals[][] = new int[points.length][2];
		int nVertex = points.length;

		TriVector u = new TriVector(0, 0, 0);
		TriVector v = new TriVector(0, 0, 0);

		for (int i = 0; i < nVertex; i++) {
			v1 = (int) (points[(i + 1) % nVertex].getX() - points[i].getX());
			v2 = (int) (points[(i + 1) % nVertex].getY() - points[i].getY());
			normals[i][0] = v2;
			normals[i][1] = -v1;
		}

		v1 = (int) (points[1].getX() - points[0].getX());
		v2 = (int) (points[1].getY() - points[0].getY());

		u.setX(v1);
		u.setY(v2);

		v1 = (int) (points[nVertex - 1].getX() - points[0].getX());
		v2 = (int) (points[nVertex - 1].getY() - points[0].getY());

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

		return normals;
	}

	protected boolean isOnSet(int x, int y, int[][] normals, TriVector[] points) {
		boolean result = true;
		int nVertex = normals.length;
		for (int i = 0; i < nVertex; i++) {
			result = result
					&& (orientation * normals[i][0] * (x - points[i].getX())
							+ orientation * normals[i][1]
							* (y - points[i].getY()) >= 0);
		}
		return result;
	}

	boolean isPositive(int[] dot) {
		boolean result = true;
		for (int i = 0; i < dot.length; i++) {
			result = result && (dot[i] >= 0);
		}
		return result;
	}

	/**
	 * some code repetition who cares
	 */
	@Override
	public void paintLine(Line element) {
		/**
		 * initialization
		 */
		TriVector[] p = new TriVector[element.nPoints];
		TriVector[] intBuffer = new TriVector[element.nPoints];
		for (int i = 0; i < element.nPoints; i++)
			intBuffer[i] = new TriVector();

		/**
		 * transformation to camera coordinates
		 */
		p[0] = element.getNPoint(0);
		p[0] = transform(p[0]);
		for (int i = 0; i < element.nPoints; i++) {
			if (p[(i + 1) % element.nPoints] == null) {
				p[i + 1] = element.getNPoint(i + 1);
				p[i + 1] = transform(p[i + 1]);
			}
		}
		/**
		 * Calculation of the integer coordinates
		 */
		for (int i = 0; i < element.nPoints; i++) {
			intBuffer[i].setZ(p[i].getZ());
			p[i] = project(p[i]);
			intBuffer[i].setX(wd.changeCoordX(p[i].getX()));
			intBuffer[i].setY(wd.changeCoordY(p[i].getY()));
		}

		/**
		 * calculating normal in integer coordinates
		 */
		if (checkIfPointsAreWellProjected(p))
			drawZLine(intBuffer, element);
	}

	/**
	 * 
	 * @param p
	 *            points of line
	 * @param element
	 *            line element
	 */
	public void drawZLine(TriVector[] p, Element element) {
		int x1 = (int) p[0].getX(), x2 = (int) p[1].getX(), y1 = (int) p[0]
				.getY(), y2 = (int) p[1].getY();

		double z1 = p[0].getZ(), z2 = p[1].getZ(), z;

		int imin = 0, jmin = 0, fmin = Integer.MAX_VALUE;

		int x = x1, y = y1, oldi = 0, oldj = 0;

		int n = (int) Math.floor(Math.sqrt(x1 * x1 + y1 * y1));

		int counter = 0;

		Graphics g = wd.getGraphics();

		g.setColor(element.getColor());

		while (x != x2 || y != y2) {
			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {
					if ((i == 0 && j == 0) || (i == oldi && j == oldj)) {
						continue;
					} else {
						int res = lineFunction(x + i, y + j, p);
						if (fmin > res) {
							fmin = res;
							imin = i;
							jmin = j;
						}
					}
				}
			}

			counter++;
			z = z1 + ((z2 - z1) / n) * counter;

			fmin = Integer.MAX_VALUE;
			x += imin;
			y += jmin;
			oldi = -imin;
			oldj = -jmin;

			if (x < wd.getWindowWidth() && x > -1 && y < wd.getWindowHeight()
					&& y > -1 && z < zBuffer[x][y] && z > zd) {
				zBuffer[x][y] = z;
				g.drawLine(x, y, x, y);
			}
		}
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param p
	 * @return error of x and y from the line
	 */
	public int lineFunction(int x, int y, TriVector[] p) {
		int dx = (int) (p[1].getX() - p[0].getX());
		int dy = (int) (p[1].getY() - p[0].getY());

		return (int) (Math
				.abs(-dy * (x - p[0].getX()) + dx * (y - p[0].getY()))
				+ Math.abs((x - p[1].getX())) + Math.abs((y - p[1].getY())));
	}

	@Override
	public void paintPoint(Point element) {
		TriVector p = element.getNPoint(0);
		p = transform(p);
		p = project(p);

		wd.setDrawColor(element.color);

		int radius = element.getRadius();
		int x = wd.changeCoordX(p.getX());
		int y = wd.changeCoordY(p.getY());
		double z = p.getZ();
		if (x > 0 && x < wd.getWindowWidth() && y > 0
				&& y < wd.getWindowHeight() && z < zBuffer[x][y] && z > 0) {
			wd.drawPoint(p.getX(), p.getY(), radius);

			zBuffer[x][y] = z;
		}
	}

	@Override
	public void paintStringElement(StringElement element) {
		TriVector p = element.getNPoint(0);
		p = transform(p);
		p = project(p);
		wd.setDrawColor(element.color);
		TriVector[] v = new TriVector[1];
		v[0] = p;
		if (checkIfPointsAreWellProjected(v) && p.getZ() > 0) {
			wd.drawString(element.getString(), p.getX(), p.getY());
		}
	}

	public Color getFragmentColor(Element e, int x, int y) {
		return e.getColor();
	}
}
