package window;

import java.awt.Color;
import java.awt.Graphics;

import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public class ImageWindow extends Window2D {
	private BufferedImage img;
	/**
	 * width pixels of img
	 */
	private int widthPxl;
	/**
	 * height pixels of img
	 */
	private int heightPxl;
	/**
	 * graphics of BufferedImage img
	 */
	private Graphics g;
	/**
	 * background color
	 */
	private Color back;

	public ImageWindow() {
	}

	public ImageWindow(double xMin, double xMax, double yMin, double yMax) {
		super(xMin, xMax, yMin, yMax);
		back = Color.WHITE;
	}

	public BufferedImage getImage() {
		return img;
	}

	public Graphics getGraphics() {
		return g;
	}

	public Color getDrawColor() {
		return g.getColor();
	}

	public void setDrawColor(Color c) {
		g.setColor(c);
	}

	public Color getBackGroundColor() {
		return back;
	}

	public void setBackGroundColor(Color c) {
		back = c;
	}

	public int getWindowHeight() {
		return heightPxl;
	}

	public int getWindowWidth() {
		return widthPxl;
	}

	public void setWindowSize(int width, int height) {
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = img.getGraphics();
		widthPxl = width;
		heightPxl = height;
	}

	public void clearImageWithBackGround() {
		Color c = this.getDrawColor();
		setDrawColor(this.getBackGroundColor());
		g.fillRect(0, 0, this.getWindowWidth(), this.getWindowHeight());
		setDrawColor(c);
	}

	@Override
	public void drawLine(double x1, double y1, double x2, double y2) {
		int lambda1, lambda2, lambda3, lambda4;
		lambda1 = this.changeCoordX(x1);
		lambda2 = this.changeCoordY(y1);
		lambda3 = this.changeCoordX(x2);
		lambda4 = this.changeCoordY(y2);
		g.drawLine(lambda1, lambda2, lambda3, lambda4);
	}

	@Override
	public void drawFilledRectangle(double x, double y, double width,
			double height) {
		this.drawFilledTriangle(x, y, x + width, y, x + width, y + height);
		this.drawFilledTriangle(x + width, y + height, x, y + height, x, y);

	}

	@Override
	public void drawFilledParallelogram(double x0, double y0, double x1,
			double y1, double x2, double y2) {
		this.drawFilledTriangle(x0, y0, x1, y1, x2 + x1, y2 + y1);
		this.drawFilledTriangle(x0, y0, x2, y2, x1 + x2, y2 + y2);
	}

	@Override
	public void drawFilledTriangle(double x0, double y0, double x1, double y1,
			double x2, double y2) {
		int[] lambda1, lambda2;
		lambda1 = new int[3];
		lambda2 = new int[3];
		lambda1[0] = this.changeCoordX(x0);
		lambda2[0] = this.changeCoordY(y0);
		lambda1[1] = this.changeCoordX(x1);
		lambda2[1] = this.changeCoordY(y1);
		lambda1[2] = this.changeCoordX(x2);
		lambda2[2] = this.changeCoordY(y2);
		g.fillPolygon(lambda1, lambda2, 3);
	}

	@Override
	public void drawString(String s, double x1, double y1) {
		int lambda1, lambda2;
		lambda1 = this.changeCoordX(x1);
		lambda2 = this.changeCoordY(y1);
		g.drawString(s, lambda1, lambda2);

	}

	public void paint(Graphics g1, ImageObserver imgObs) {
		g1.drawImage(img, 0, 0, imgObs);
	}

	/**
	 * 
	 * @param g1
	 *            : graphics of the object in which you will draw the
	 *            ImageBuffer img
	 */
	public void paint(Graphics g1) {
		paint(g1, null);
	}
	
	public void paintTranslate(Graphics g1, int x, int y) {
		g1.drawImage(img, x, y, null);
	}

	// ---------------------------------------------------------

	public int changeCoordX(double x) {
		int r;
		r = (int) Math.floor(((this.getWindowWidth()) * (x - (x_min)))
				/ (Math.abs(x_max - x_min)));
		return r;
	}

	public int changeCoordY(double y) {
		int r;
		r = (int) Math.floor((-(this.getWindowHeight()) * (y - (y_max)))
				/ (Math.abs(y_max - y_min)));
		return r;
	}

	public double InverseCoordY(int y) {
		double aux, y1;
		y1 = (double) y;
		aux = y1 * (-Math.abs(y_max - y_min) / this.getWindowHeight()) + y_max;
		return aux;
	}

	public double InverseCoordX(int x) {
		double aux, x1;
		x1 = (double) x;
		aux = x1 * (Math.abs(x_max - x_min) / this.getWindowWidth()) + x_min;
		return aux;
	}

	public double pxlXStep() {
		double r1;
		r1 = (double) (Math.abs(x_max - x_min)) / (this.getWindowWidth());
		return r1;
	}

	public double pxlYStep() {
		double r2;
		r2 = (double) (Math.abs(y_max - y_min)) / (this.getWindowHeight());
		return r2;
	}

	@Override
	public void drawFilledQuadrilateral(double x0, double y0, double x1,
			double y1, double x2, double y2, double x3, double y3) {
		drawFilledTriangle(x0, y0, x1, y1, x2, y2);
		drawFilledTriangle(x2, y2, x3, y3, x0, y0);

	}

	@Override
	public void drawPoint(double x1, double y1, int pxlRadius) {
		int x0 = changeCoordX(x1);
		int y0 = changeCoordY(y1);
		if(pxlRadius > 1)
			g.fillOval(x0, y0, pxlRadius, pxlRadius);
		else
			drawLine(x1,y1,x1,y1);
	}

}
