package teste;
/**
 * author Guilherme
 */
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
// for the main JFrame design
// for the GUI stuff
// for the event handling
// for reading from a file
// for writing to a file
public class NotePad extends JFrame implements ActionListener {

    public static void main(String[] args){
        NotePad app = new NotePad();
        app.setVisible(true);
    }
        private TextArea textArea = new TextArea("", 0,0, TextArea.SCROLLBARS_VERTICAL_ONLY);
        private MenuBar menuBar = new MenuBar(); // first, create a MenuBar item
        private Menu file = new Menu(); // File menu
        private MenuItem openFile = new MenuItem();  // an open option
        private MenuItem saveFile = new MenuItem(); // a save option
        private MenuItem close = new MenuItem(); // and a close option!

        public NotePad() {
            this.setSize(500, 300); // set the initial size of the window
            this.setTitle("Java Notepad Tutorial"); // set the title of the window
            setDefaultCloseOperation(EXIT_ON_CLOSE); // set the default close operation
            this.textArea.setFont(new Font("Century Gothic", Font.BOLD, 12)); // set a default font for the TextArea
            this.getContentPane().setLayout(new BorderLayout());
            this.getContentPane().add(textArea);

            // add our menu bar into the GUI
            this.setMenuBar(this.menuBar);
            this.menuBar.add(this.file);

            this.file.setLabel("File");

            this.openFile.setLabel("Open"); // set the label of the menu item
            this.openFile.addActionListener(this); // add an action listener (so we know when it's been clicked
            this.openFile.setShortcut(new MenuShortcut(KeyEvent.VK_O, false)); // set a keyboard shortcut
            this.file.add(this.openFile); // add it to the "File" menu

            this.saveFile.setLabel("Save");
            this.saveFile.addActionListener(this);
            this.saveFile.setShortcut(new MenuShortcut(KeyEvent.VK_S, false));
            this.file.add(this.saveFile);

            this.close.setLabel("Close");
            this.close.setShortcut(new MenuShortcut(KeyEvent.VK_F4, false));
            this.close.addActionListener(this);
            this.file.add(this.close);
        }

        public void actionPerformed (ActionEvent e) {
	// if the source of the event was our "close" option
	if (e.getSource() == this.close)
		this.dispose(); // dispose all resources and close the application

	// if the source was the "open" option
	else if (e.getSource() == this.openFile) {
		JFileChooser open = new JFileChooser(); // open up a file chooser (a dialog for the user to browse files to open)
		int option = open.showOpenDialog(this); // get the option that the user selected (approve or cancel)
		// NOTE: because we are OPENing a file, we call showOpenDialog~
		// if the user clicked OK, we have "APPROVE_OPTION"
		// so we want to open the file
		if (option == JFileChooser.APPROVE_OPTION) {
			this.textArea.setText(""); // clear the TextArea before applying the file contents
			try {
				// create a scanner to read the file (getSelectedFile().getPath() will get the path to the file)
				Scanner scan = new Scanner(new FileReader(open.getSelectedFile().getPath()));
				while (scan.hasNext()) // while there's still something to read
					this.textArea.append(scan.nextLine() + "\n"); // append the line to the TextArea
			} catch (Exception ex) { // catch any exceptions, and...
				// ...write to the debug console
				System.out.println(ex.getMessage());
			}
		}
	}

	// and lastly, if the source of the event was the "save" option
	else if (e.getSource() == this.saveFile) {
		JFileChooser save = new JFileChooser(); // again, open a file chooser
		int option = save.showSaveDialog(this); // similar to the open file, only this time we call
		// showSaveDialog instead of showOpenDialog
		// if the user clicked OK (and not cancel)
		if (option == JFileChooser.APPROVE_OPTION) {
			try {
				// create a buffered writer to write to a file
				BufferedWriter out = new BufferedWriter(new FileWriter(save.getSelectedFile().getPath()));
				out.write(this.textArea.getText()); // write the contents of the TextArea to the file
				out.close(); // close the file stream
			} catch (Exception ex) { // again, catch any exceptions and...
				// ...write to the debug console
				System.out.println(ex.getMessage());
			}
		}
	}
}



}


