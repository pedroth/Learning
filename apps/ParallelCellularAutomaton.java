package apps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;

import visualization.TextFrame;

/**
 * Parallel Asynchronous cellular automaton
 * 
 * for every pixel calculates update function of the automaton
 * 
 * I need another algorithms to update the automaton one that is O(n) n = number
 * of alive cells and not O(A/nCores) where A is area of pixels
 * 
 * @author pedro
 * 
 */
public class ParallelCellularAutomaton extends JFrame implements MouseMotionListener, KeyListener {

	private static final long serialVersionUID = 1L;

	private BufferedImage buffer;
	private int wChanged, hChanged;
	private boolean[][] space;
	private Graphics gimg;
	private boolean timerStarted;
	private ThreadManager threadManager;

	private static String helpText = "< mouse > : draw initial state \n\n" + "<space> : starts animation \n" + "<r> : reset \n" + "\n" + "Made by Pedroth";

	class CellularAutomaton implements Runnable {
		private int discreteTime;
		private int begin, end;
		/**
		 * change rules where according with
		 * http://psoup.math.wisc.edu/mcell/rullex_life.html
		 */
		private int[] survive;
		private int[] born;

		public CellularAutomaton(int begin, int end, int type) {
			discreteTime = 0;
			this.begin = begin;
			this.end = end;
			if (type == 0) {
				int[] sAux = { 1, 2, 3, 4, 5 };
				int[] bAux = { 3 };
				survive = Arrays.copyOf(sAux, sAux.length);
				born = Arrays.copyOf(bAux, bAux.length);
			} else if (type == 1) {
				int[] sAux = { 3, 4 };
				int[] bAux = { 3, 4 };
				survive = Arrays.copyOf(sAux, sAux.length);
				born = Arrays.copyOf(bAux, bAux.length);
			} else if (type == 2) {
				int[] sAux = { 0, 2, 3, 6 };
				int[] bAux = { 1 };
				survive = Arrays.copyOf(sAux, sAux.length);
				born = Arrays.copyOf(bAux, bAux.length);
			} else if (type == 3) {
				int[] sAux = { 1, 3, 5, 7, 8 };
				int[] bAux = { 3, 5, 7 };
				survive = Arrays.copyOf(sAux, sAux.length);
				born = Arrays.copyOf(bAux, bAux.length);
			} else if (type == 4) {
				int[] sAux = { 4, 5, 6, 7, 8 };
				int[] bAux = { 3 };
				survive = Arrays.copyOf(sAux, sAux.length);
				born = Arrays.copyOf(bAux, bAux.length);
			} else {
				int[] sAux = { 2, 3, 5, 6, 7, 8 };
				int[] bAux = { 3, 7, 8 };
				survive = Arrays.copyOf(sAux, sAux.length);
				born = Arrays.copyOf(bAux, bAux.length);
			}
		}

		boolean cellularFunction(int x, int y, int t) {
			int acm = 0;
			/**
			 * check neighbors cells.
			 */
			boolean isAlive = space[x][y];
			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {
					int k = Math.max(x + i, 0);
					int l = Math.max(y + j, 0);
					k = Math.min(k, wChanged - 1);
					l = Math.min(l, hChanged - 1);
					if (space[k][l])
						acm++;
				}
			}
			/**
			 * rules
			 */
			/**
			 * born rule
			 */
			if (!isAlive && bornRule(acm))
				return true;
			/**
			 * survive rule
			 */
			else if (isAlive && surviveRule(acm))
				return true;
			else
				return false;
		}

		public boolean bornRule(int acm) {
			boolean orIden = false;
			for (int v : born)
				orIden = orIden || (acm == v);
			return orIden;
		}

		public boolean surviveRule(int acm) {
			boolean orIden = false;
			for (int v : survive)
				orIden = orIden || (acm == v);
			return orIden;
		}

		@Override
		public void run() {
			for (int i = begin; i < end; i++) {
				for (int j = 0; j < hChanged; j++) {
					boolean ans = cellularFunction(i, j, discreteTime);
					if (ans) {
						drawPoint(i, j, Color.green);
					} else {
						drawPoint(i, j, Color.black);
					}
				}
			}
			for (int i = begin; i < end; i++) {
				for (int j = 0; j < hChanged; j++) {
					if (buffer.getRGB(i, j) == Color.green.getRGB()) {
						space[i][j] = true;
					} else {
						space[i][j] = false;
					}
				}
			}

			discreteTime++;
		}

	}

	class ThreadManager {
		private Thread[] threads;
		private int nCores;
		private CellularAutomaton[] cells;

		ThreadManager() {
			nCores = Runtime.getRuntime().availableProcessors();
			threads = new Thread[nCores];
			cells = new CellularAutomaton[nCores];
			Random random = new Random();
			int type = random.nextInt(6);
			for (int i = 0; i < nCores; i++) {
				cells[i] = new CellularAutomaton(i * (wChanged / nCores), (i + 1) * (wChanged / nCores), type);
			}
		}

		public void run() {
			for (int i = 0; i < nCores; i++) {
				threads[i] = new Thread(cells[i]);
				threads[i].start();
			}
			for (int i = 0; i < nCores; i++) {
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			repaint();
		}
	}

	/* synchronized */public void drawPoint(int x, int y, Color c) {
		int rgbColor = c.getRGB();
		int[] pixels = ((java.awt.image.DataBufferInt) buffer.getRaster().getDataBuffer()).getData();

		int w = wChanged;
		pixels[y * w + x] = rgbColor;
	}

	public ParallelCellularAutomaton(boolean isApplet) {
		this.setLayout(null);
		// Set default close operation for JFrame
		if (!isApplet) {
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		this.setSize(500, 500);
		this.addMouseMotionListener(this);
		buffer = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
		space = new boolean[this.getWidth()][this.getHeight()];
		gimg = buffer.getGraphics();
		gimg.setColor(Color.black);
		wChanged = this.getWidth();
		hChanged = this.getHeight();
		gimg.fillRect(0, 0, wChanged, hChanged);
		threadManager = new ThreadManager();
		this.addKeyListener(this);
		this.setVisible(true);
	}

	public void myInit() {
		this.setSize(500, 500);
		buffer = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
		space = new boolean[this.getWidth()][this.getHeight()];
		gimg = buffer.getGraphics();
		gimg.setColor(Color.black);
		wChanged = this.getWidth();
		hChanged = this.getHeight();
		gimg.fillRect(0, 0, wChanged, hChanged);
		threadManager = new ThreadManager();
	}

	public void paint(Graphics g) {
		if (Math.abs(wChanged - this.getWidth()) > 0 || Math.abs(hChanged - this.getHeight()) > 0) {
			buffer = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
			space = new boolean[this.getWidth()][this.getHeight()];
			gimg = buffer.getGraphics();
			gimg.setColor(Color.black);
			wChanged = this.getWidth();
			hChanged = this.getHeight();
			gimg.fillRect(0, 0, wChanged, hChanged);
		}
		update(g);
	}

	public void update(Graphics g) {
		if (timerStarted) {
			threadManager.run();
		}
		buffer.getGraphics().setColor(Color.green);
		g.drawImage(buffer, 0, 0, wChanged, hChanged, null);
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int k = Math.max(e.getX(), 0);
		int l = Math.max(e.getY(), 0);
		k = Math.min(k, wChanged - 1);
		l = Math.min(l, hChanged - 1);
		space[k][l] = true;
		if (!timerStarted) {
			gimg.setColor(Color.green);
			gimg.drawLine(k, l, k, l);
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_H) {
			new TextFrame("help", helpText);
		} else if (e.getKeyCode() == KeyEvent.VK_R) {
			timerStarted = false;
			myInit();
		} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			timerStarted = true;
			repaint();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		new ParallelCellularAutomaton(false);
	}
}
