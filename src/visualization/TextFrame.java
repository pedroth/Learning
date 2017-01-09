package visualization;

import javax.swing.*;

public class TextFrame extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public TextFrame(String title, String text) {
        super(title);
        // Set JFrame size
        setSize(800, 550);
        // Set default close operation for JFrame
        //setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JTextArea txtArea = new JTextArea(text);
        JScrollPane areaScrollPane = new JScrollPane(txtArea);
        areaScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(areaScrollPane);
        setVisible(true);
    }
}
