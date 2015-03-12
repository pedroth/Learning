package apps;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

public class ManifoldFunctions {
	private TextArea text;
	private Button button;
	private JFrame input;

	public ManifoldFunctions(boolean isApplet) {
		input = new JFrame("Input obj file");
		input.setLayout(new GridLayout(1, 3));
		input.setResizable(false);
		input.setSize(500, 100);
		
		if(!isApplet) {
			input.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		} 
		text = new TextArea();
		button = new Button("Load");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		input.add(text);
		input.add(button);
		input.setVisible(true);
	}

	public static void main(String[] args) {
		new ManifoldFunctions(false);
	}
}
