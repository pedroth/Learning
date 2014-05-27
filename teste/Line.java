package teste;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Line extends Applet implements MouseListener {
	private int _mouseX, _mouseY, _mouseClicks;
	private boolean _drawLine;
	private int[][] _line;

	public void drawPoint(int x, int y, Color c, Graphics g) {
		g.setColor(c);
		g.fillOval(x, y, 1, 1);
	}

	public void drawLine(int x1, int y1, int x2, int y2, Graphics g) {
		int imin = 0, jmin = 0, fmin = Integer.MAX_VALUE;
		int x = x1, y = y1, oldi = 0, oldj = 0;
		drawPoint(x1, y1, Color.red, g);
		drawPoint(x2, y2, Color.red, g);

		while (x != x2 || y != y2) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {
					if ((i == 0 && j == 0) || (i == oldi && j == oldj)) {
						continue;
					} else {
						int res = function(x + i, y + j);
						if (fmin > res) {
							fmin = res;
							imin = i;
							jmin = j;
						}
					}
				}
			}
			fmin = Integer.MAX_VALUE;
			x += imin;
			y += jmin;
			oldi = -imin;
			oldj = -jmin;
			drawPoint(x, y, Color.red, g);
		}
	}

	public int function(int x, int y) {
		int dx = _line[1][0] - _line[0][0];
		int dy = _line[1][1] - _line[0][1];

		return Math.abs(-dy * (x - _line[0][0]) + dx * (y - _line[0][1]))
				+ Math.abs((x - _line[1][0])) + Math.abs((y - _line[1][1]));
	}

	public void init() {
		_line = new int[2][2];
		_mouseClicks = 0;
		_drawLine = false;
		this.addMouseListener(this);
	}

	public void paint(Graphics g) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		_mouseX = e.getX();
		_mouseY = e.getY();
		_line[_mouseClicks % 2][0] = _mouseX;
		_line[_mouseClicks % 2][1] = _mouseY;
		_mouseClicks++;
		if (_mouseClicks % 2 == 0) {
			// _drawLine = true;
			drawLine(_line[0][0], _line[0][1], _line[1][0], _line[1][1],
					this.getGraphics());
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
	}

}
