package teste;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

public class ColorFunctionReal extends Applet implements MouseMotionListener,
		KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int brightness;
	private BufferedImage bImg;
	private int wChanged;
	private int hChanged;
	private boolean first = true;
	private boolean random = false;
	private boolean change = false;
	private Random randomNum;
	private long start;
	private long elapsedTime;
	private boolean[][] teste;
	private double lambda = 1;
	private int rt1;
	private double rt2;
	private boolean randomFilled;
	private Thread[] mt;
	private boolean threadActivated;
	private int nCores;
	// private double max;
	// private int rt1Max;
	// private double rt2Max;
	private int nFunction;

	public double colorFunction(double x, double y) {
		switch (nFunction) {
		case 0:
			return Math.cos(-lambda
					* Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)))
					* Math.sin(x * y);
		case 1:
			return 1 / Math.sqrt((lambda * x * x + lambda * y * y));
		case 2:
			return Math.exp(-(lambda)) * Math.sin(Math.sqrt(x * x + y * y));
		case 3:
			return (y * Math.sin(x - lambda) + x * Math.cos(y - lambda)) * 0.125;
		case 4:
			return ((y - lambda) * Math.sin(x * y) - (x - lambda)
					* Math.sin(x * x + y * y)) * 0.125;
		case 5:
			return ((x * x + 2 * y * y - 2) * (2 * x * x + y * y - 2) + lambda) * 0.125;
		default:
			return // Math.exp(-((x-3*Math.sin(lambda))*(x-3*Math.sin(lambda)) +
					// y*y)) + Math.exp(-(x*x +
					// (y-Math.sin(lambda))*(y-Math.sin(lambda))));
					// -1 * Math.exp(-lambda*( x * x + y* y));
					// 1 / Math.sqrt((lambda*x * x + lambda*y *y));
					// Math.exp(-(lambda))*Math.sin(Math.sqrt(x*x+y*y));
					// Math.sin(Math.sqrt((x)*(x) + (y)*(y) ) -lambda);
					// (y*Math.sin(x-lambda) +x*Math.cos(y-lambda))*0.125;
					// (y*y -lambda*x*y + x*x*x)*0.125;
					// -2*(x-lambda)*y /(x*x -lambda*y*(y-lambda));
					// ((y-lambda)*Math.sin(x*y)-(x-lambda)*Math.sin(x*x+y*y))*0.125;
					// Math.pow(Math.sin(x), 3) +
					// Math.cos(lambda)*Math.pow(Math.sin(lambda*y),3)
					// -Math.cos(lambda)*Math.sin(lambda*x)*Math.sin(lambda*y);
					// Math.exp(-((x-3*Math.sin(lambda))*(x-3*Math.sin(lambda))
					// + y*y)) + Math.exp(-(x*x +
					// (y-Math.sin(lambda))*(y-Math.sin(lambda))));
					// ((x*x+2*y*y-2)*(2*x*x + y*y -2)+lambda)*0.125;
					// Math.log((x-lambda)*(x-lambda) +y*y);
					// Math.tan(x*y*lambda);
					// 2*x*y;
					// x*x-y*y + 1;
					// (-1*x + y) + lambda*((x-2)*(x-2) + (y-2)*(y-2));
			((x) * (y) * ((x - 1) + (y - 1)));
			// (-x+y)*((x-lambda)+y)*((y+lambda));
			// (y*Math.sqrt(x)-Math.sin(x));
		}
	}

	public void init() {
		brightness = 100;
		this.addMouseMotionListener(this);
		BufferedImage bImg = new BufferedImage(this.getWidth(),
				this.getHeight(), BufferedImage.TYPE_INT_RGB);
		wChanged = this.getWidth();
		hChanged = this.getHeight();
		this.addKeyListener(this);
		randomNum = new Random();
		teste = new boolean[wChanged][hChanged];
		rt1 = 1;
		rt2 = 1.0;
		randomFilled = false;
		nFunction = 0;
		threadActivated = false;
		nCores = Runtime.getRuntime().availableProcessors();
		mt = new Thread[nCores];
		for (int i = 0; i < nCores; i++) {
			mt[i] = new MyThread();
		}
	}

	// --------------------------------------------------
	private class MyThread extends Thread {

		MyThread() {

		}

		public void run() {
			drawFunctionR(bImg.getGraphics());
		}
	}

	// ----------------------------------------------------
	public void paint(Graphics g) {
		// sempre que paint e chamado ele apaga tudo.
		// g.drawImage(bImg, 0, 0, this);
		update(g);
	}

	public void update(Graphics g) {
		g.drawImage(bImg, 0, 0, this);

		if (Math.abs(wChanged - this.getWidth()) > 0
				|| Math.abs(hChanged - this.getHeight()) > 0 || first || random
				|| change) {
			if (Math.abs(wChanged - this.getWidth()) > 0
					|| Math.abs(hChanged - this.getHeight()) > 0 || first
					|| randomFilled) {
				bImg = new BufferedImage(this.getWidth(), this.getHeight(),
						BufferedImage.TYPE_INT_RGB);
				wChanged = this.getWidth();
				hChanged = this.getHeight();
				teste = new boolean[wChanged][hChanged];
			}
			for (int i = 0; i < wChanged; i++) {
				for (int j = 0; j < hChanged; j++) {
					teste[i][j] = false;
				}
			}
			first = false;
			change = false;
			if (random) {
				start = System.currentTimeMillis();

				if (threadActivated) {
					for (int i = 0; i < nCores - 1; i++) {
						mt[i].start();
					}
				}
				drawFunctionR(g);
				elapsedTime = System.currentTimeMillis() - start;
				Long et = new Long(elapsedTime);
				et = Math.round(1 / (et * 10E-3));
				bImg.getGraphics().setColor(Color.green);

				bImg.getGraphics().drawString(
						"Fps:" + et.toString() + "   thread[t]  "
								+ threadActivated + "  Filled[f]  "
								+ randomFilled, 10, 10);
				System.out.printf("%d \t %f \t %d \n", rt1, rt2, et);
				if (threadActivated) {
					for (int i = 0; i < nCores - 1; i++) {
						try {
							mt[i].join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					for (int i = 0; i < nCores - 1; i++) {
						mt[i] = new MyThread();
					}
				}
			} else {
				start = System.currentTimeMillis();
				drawFunction();
				elapsedTime = System.currentTimeMillis() - start;
				Long et = new Long(elapsedTime);
				et = Math.round(1 / (et / 10E3));
				bImg.getGraphics().setColor(Color.green);
				bImg.getGraphics().drawString("Fps:" + et.toString(), 10, 10);
			}
		}

	}

	public void drawFunction() {
		double f = 0;
		double faux = 0;
		Graphics g1 = bImg.getGraphics();
		for (int i = 0; i < this.getWidth(); i++) {
			for (int j = 0; j < this.getHeight(); j++) {
				f = colorFunction(transX(i), transY(j));
				faux = Math.abs(Math.pow(f, brightness));

				int[] pixels = ((java.awt.image.DataBufferInt) bImg.getRaster().getDataBuffer()).getData();

				 if (Math.abs(f) > 1) {
				 f = 1;
				 faux = 1;
				 }
				 if (f <= 0) {
				Color c = new Color((float) faux, (float) faux,
				 (float) Math.abs(f));
				pixels[j * wChanged + i] = c.getRGB();
				 } else {
				 Color c = new Color((float) Math.abs(f), (float) faux,
				 (float) faux);
				 pixels[j * wChanged + i] = c.getRGB();
				 }
//				f = (240.0 / 360.0) - (240.0 / 360.0)*((f+1.0)/2.0);
//				g1.setColor(Color.getHSBColor((float) f, 1f, 1f));
//				g1.drawLine(i, j, i, j);

			}
		}
	}

	public void drawFunctionR(Graphics g) {
		double f = 0;
		double faux = 0;
		int n = wChanged * hChanged;
		int rw = 0;
		int rh = 0;
		
		int[] pixels = ((java.awt.image.DataBufferInt) bImg.getRaster().getDataBuffer()).getData();
		
		
		// g.drawImage(bImg, 0, 0, this);
		for (int i = 0; i < Math.round(rt2 * n); i++) {

			boolean t = false;
			int niterations = 25;
			do {
				rw = randomNum.nextInt(wChanged);
				rh = randomNum.nextInt(hChanged);
				if (rw < wChanged && rh < hChanged && rw > 0 && rh > 0) {
					t = teste[rw][rh];
				}
				niterations--;
				t = (niterations > 0) ? t : false;
			} while (t == true);
			teste[rw][rh] = true;
			f = colorFunction(transX(rw), transY(rh));
			faux = Math.abs(Math.pow(f, brightness));
			if (Math.abs(f) > 1) {
				f = 1;
				faux = 1;
			}
			if (f <= 0) {
				Color c = new Color((float) faux, (float) faux, (float) Math
						.abs(f));
				pixels[rh * wChanged + rw] = c.getRGB();
				// g1.drawLine(rw + 1, rh, rw, rh);
				// g1.drawLine(rw, rh + 1, rw, rh);
				// g1.drawLine(rw - 1, rh, rw, rh);
				// g1.drawLine(rw, rh - 1, rw, rh);
				// g1.drawLine(rw + 1, rh+1, rw, rh);
				// g1.drawLine(rw, rh - 1, rw, rh);
				// g1.drawLine(rw - 1, rh, rw, rh);
				// g1.drawLine(rw-1, rh - 1, rw, rh);
			} else {
				Color c = new Color((float) Math.abs(f), (float) faux,
						(float) faux);
				pixels[rh * wChanged + rw] = c.getRGB();
				// g1.drawLine(rw + 1, rh, rw, rh);
				// g1.drawLine(rw, rh + 1, rw, rh);
				// g1.drawLine(rw - 1, rh, rw, rh);
				// g1.drawLine(rw, rh - 1, rw, rh);
				// g1.drawLine(rw + 1, rh + 1, rw, rh);
				// g1.drawLine(rw, rh - 1, rw, rh);
				// g1.drawLine(rw - 1, rh, rw, rh);
				// g1.drawLine(rw - 1, rh - 1, rw, rh);
			}
			// if(i==5*(n/6)){
			// g.drawImage(bImg, 0, 0, this);
			// }
		}
	}

	public double transX(int x) {
		return -2 * Math.PI + ((4 * Math.PI) / this.getWidth()) * x;
	}

	public double transY(int y) {
		return 2 * Math.PI - ((4 * Math.PI) / this.getHeight()) * y;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		brightness = (int) Math.round(100 * e.getX() / this.getWidth());
		lambda = transY(e.getY());
		// rt1 = (int) Math.round(((100.0 / this.getWidth())* e.getX()));
		// rt2 = 2.0 / this.getHeight() *e.getY();
		change = true;
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_R) {
			random = !random;
			repaint();
		} else if (arg0.getKeyCode() == KeyEvent.VK_PLUS) {
			rt2 += 0.01;
		} else if (arg0.getKeyCode() == KeyEvent.VK_MINUS) {
			rt2 -= 0.01;
		} else if (arg0.getKeyCode() == KeyEvent.VK_F) {
			randomFilled = !randomFilled;
		} else if (arg0.getKeyCode() == KeyEvent.VK_T) {
			threadActivated = !threadActivated;
			if (threadActivated)
				rt2 = rt2 / nCores;
			else
				rt2 = 1.0;
		} else if (arg0.getKeyCode() == KeyEvent.VK_S) {
			try {
				File outputfile = new File("ColorFunction.png");
				ImageIO.write(bImg, "png", outputfile);
				System.out.println("Image saved");
			} catch (IOException e) {
				// nothing on purpose;
			}
		} else {
			nFunction = (int) arg0.getKeyChar() - '0';
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

}
