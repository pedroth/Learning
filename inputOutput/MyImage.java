package inputOutput;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import algebra.TriVector;

public class MyImage {
	private BufferedImage img;

	public MyImage() {

	}

	public MyImage(BufferedImage img) {
		this.img = img;
	}

	/**
	 * 
	 * @param file
	 *            could be a web address or a file system address
	 */
	public MyImage(String file) {
		String[] aux = file.split("http");
		if (aux.length > 1)
			loadImageFromWeb(file);
		else
			loadImageFromFileName(file);
	}

	public BufferedImage getImg() {
		return img;
	}

	public int getWidth() {
		return img.getWidth();
	}

	public int getHeight() {
		return img.getHeight();
	}

	public void setImg(BufferedImage img) {
		this.img = img;

	}

	public void loadImageFromFileName(String s) {
		try {
			img = ImageIO.read(new File(s));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loadImageFromWeb(String s) {
		try {
			img = ImageIO.read(new URL(s));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return matrix of RGB , x coord is red, y is green and z is blue. RGB are
	 *         normalized values between [0,1]
	 */
	public TriVector[][] getRGBImageMatrix() {
		int rows = img.getWidth();
		int collumns = img.getHeight();
		TriVector[][] rgb = new TriVector[collumns][rows];
		int[] intRGB = img.getRGB(0, 0, rows, collumns, null, 0, rows);

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < collumns; j++) {
				Color c = new Color(intRGB[j * rows + i]);
				rgb[j][i] = new TriVector(c.getRed() / 255.0,
						c.getGreen() / 255.0, c.getBlue() / 255.0);
			}
		}
		return rgb;
	}

	public TriVector[] getRGBImageVector() {
		int rows = img.getWidth();
		int collumns = img.getHeight();
		TriVector[] rgb = new TriVector[rows * collumns];
		int[] intRGB = img.getRGB(0, 0, rows, collumns, null, 0, rows);

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < collumns; j++) {
				Color c = new Color(intRGB[i * collumns + j]);
				rgb[i * collumns + j] = new TriVector(c.getRed() / 255.0,
						c.getGreen() / 255.0, c.getBlue() / 255.0);
			}
		}
		return rgb;
	}

	/**
	 * 
	 * @return matrix of RGB , x coord is red, y is green and z is blue. RGB are
	 *         normalized values between [0,1]
	 */
	public TriVector[][] getHSVImageMatrix() {
		int rows = img.getWidth();
		int collumns = img.getHeight();
		TriVector[][] rgb = new TriVector[collumns][rows];
		int[] intRGB = img.getRGB(0, 0, rows, collumns, null, 0, rows);

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < collumns; j++) {
				Color c = new Color(intRGB[j * rows + i]);
				float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(),
						c.getBlue(), null);
				rgb[j][i] = new TriVector(hsv[0], hsv[1], hsv[2]);
			}
		}
		return rgb;
	}

	public TriVector[] getHSVImageVector() {
		int rows = img.getWidth();
		int collumns = img.getHeight();
		TriVector[] rgb = new TriVector[rows * collumns];
		int[] intRGB = img.getRGB(0, 0, rows, collumns, null, 0, rows);

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < collumns; j++) {
				Color c = new Color(intRGB[i * collumns + j]);
				float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(),
						c.getBlue(), null);
				rgb[i * collumns + j] = new TriVector(hsv[0], hsv[1], hsv[2]);
			}
		}
		return rgb;
	}

	public double[][] getGrayScale() {
		TriVector[][] image = getRGBImageMatrix();
		int n = image.length;
		int m = image[0].length;
		double[][] ret = new double[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				ret[i][j] = 0.2989 * image[i][j].getX() + 0.5870
						* image[i][j].getY() + 0.1140 * image[i][j].getZ();
			}
		}
		return ret;
	}

}
