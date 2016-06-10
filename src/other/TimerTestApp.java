package other;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.TextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

public class TimerTestApp extends JFrame {
	private Timer timer;
	private TextField txt;
	private int width, height;
	private double y;

	private TimerTask task;

	class Action extends TimerTask {
		private double a;
		private double v;

		Action() {
			v = 0;
		}

		public void run() {
			txt.setVisible(true);
			double dt = 0.01;
			a = -(0.05 * height) * (y - height / 3) - v;
			v += a * dt;
			y += v * dt;
			txt.setBounds(width / 3, (int) y, width / 3, 25);
			txt.setText("" + y);
			if (Math.abs(y - height / 3) < 0.01)
				timer.cancel();
			repaint();
		}
	};

	public TimerTestApp() {
		// Set JFrame title
		super("applet test");
		timer = new Timer();
		txt = new TextField();
		txt.setVisible(false);

		setLayout(null);

		// Set default close operation for JFrame
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set JFrame size
		setSize(500, 700);

		width = this.getWidth();
		height = this.getHeight();
		y = 0;
		txt.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					txt.setBounds(width / 3, 0, width / 3, 25);
					y = 0;
					timer = new Timer();
					timer.schedule(new Action(), 0, 1);
				}

			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		this.add(txt);
		task = new Action();
		timer.schedule(task, 100, 1);
		// Make JFrame visible
		setVisible(true);
	}

	public void paint(Graphics g) {
		update(g);

	}

	public void update(Graphics g) {
		g.clearRect(0, 0, width, height);
		g.setColor(Color.black);
		g.fillRect(width / 3, (int) y, width / 2, height / 4);
	}

	public static void main(String args[]) {
		new TimerTestApp();
	}
}
