package apps;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class FrameApplet extends JApplet implements ComponentListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    JFrame frame;
    JLabel text = new JLabel();
    boolean started = false;

    public void init() {
        addComponentListener(this);
    }

    void showFrame() {
        if (frame == null) {
            started = true;
            frame = new Tetra(true);
            this.add(text);
            repaint();
        }
    }

    public void paint(Graphics g) {
        String s = "Applet is open in a separate window.";
        if (!started)
            s = "Applet is starting.";
        else if (frame == null)
            s = "Applet is finished.";
        else
            frame.repaint();
        text.setText(s);
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
        showFrame();
    }

    public void componentResized(ComponentEvent e) {
    }

    public void destroy() {
        if (frame != null)
            frame.dispose();
        frame = null;
        repaint();
    }
}
