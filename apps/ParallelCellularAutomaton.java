package apps;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;

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
public class ParallelCellularAutomaton extends Applet implements
		MouseMotionListener, KeyListener {

	private static final long serialVersionUID = 1L;
	private BufferedImage buffer;
	private int wChanged, hChanged;
	private boolean[][] space;
	private Graphics gimg;
	private Timer timer;
	private JButton button;
	private boolean timerStarted;
	private ThreadManager threadManager;
	
	private static String helpText = "< mouse > : draw initial state \n\n" +
			"<any button > : starts animation \n\n" +
			"Made by Pedroth";

	class CellularAutomaton implements Runnable {
		private int discreteTime;
		private int begin, end;
		/**
		 * change rules where according with
		 * http://psoup.math.wisc.edu/mcell/rullex_life.html
		 */
		private int[] survive = { 2, 3 };
		private int[] born = { 3 };

		public CellularAutomaton(int begin, int end) {
			discreteTime = 0;
			this.begin = begin;
			this.end = end;
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

	class ThreadManager extends TimerTask {
		private Thread[] threads;
		private int nCores;

		ThreadManager() {
			nCores = Runtime.getRuntime().availableProcessors();
			threads = new Thread[nCores];
		}

		@Override
		public void run() {
			for (int i = 0; i < nCores; i++) {
				threads[i] = new Thread(new CellularAutomaton(i
						* (wChanged / nCores), (i + 1) * (wChanged / nCores)));
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
		int[] pixels = ((java.awt.image.DataBufferInt) buffer.getRaster()
				.getDataBuffer()).getData();

		int w = wChanged;
		pixels[y * w + x] = rgbColor;
	}

	public void init() {
		this.setLayout(null);

		threadManager = new ThreadManager();
		button = new JButton();
		// button.setBorder(BorderFactory.createEmptyBorder());
		// button.setContentAreaFilled(false);
		this.setSize(500, 250);
		this.addMouseMotionListener(this);
		buffer = new BufferedImage(this.getWidth(), this.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		space = new boolean[this.getWidth()][this.getHeight()];
		gimg = buffer.getGraphics();
		gimg.setColor(Color.black);
		wChanged = this.getWidth();
		hChanged = this.getHeight();
		gimg.fillRect(0, 0, wChanged, hChanged);
		timer = new Timer();
		timerStarted = false;
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				timer.schedule(threadManager, 0, 1);
				timerStarted = true;
			}

		});
		button.setBounds(0, 0, 10, 10);
		this.add(button);
		this.addKeyListener(this);
	}

	public void paint(Graphics g) {
		if (Math.abs(wChanged - this.getWidth()) > 0
				|| Math.abs(hChanged - this.getHeight()) > 0) {
			buffer = new BufferedImage(this.getWidth(), this.getHeight(),
					BufferedImage.TYPE_INT_RGB);
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
		g.drawImage(buffer, 0, 0, wChanged, hChanged, null);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		timer.schedule(threadManager, 0, 10);
		timerStarted = true;
		if (e.getKeyCode() == KeyEvent.VK_H) {
			new TextFrame("help", helpText);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}
