package comunication;

import java.awt.Graphics;
import java.awt.TextArea;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;

public class TcpClient extends JFrame implements KeyListener {

	private static final long serialVersionUID = 1L;
	private int wChanged, hChanged;
	public static boolean finished = false;
	private static TextArea in, out;
	public static boolean stringReady = false;


	public TcpClient() {
		super("Client");

		// Set default close operation for JFrame
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set JFrame size
		setSize(500, 500);

		// Make JFrame visible
		setVisible(true);

		this.setLayout(null);

		in = new TextArea();
		out = new TextArea();

		in.setBounds(500 / 10, (6 * 500) / 10, (7 * 500) / 10, (3 * 500) / 10);
		out.setBounds(500 / 10, (1 * 500) / 10, (75 * 500) / 100,
				(4 * 500) / 10);

		in.addKeyListener(this);
		this.add(in);
		this.add(out);

		wChanged = this.getWidth();
		hChanged = this.getHeight();
	}

	public class Client {
		private DataOutputStream outServer;
		private Socket clientSocket;
		private BufferedReader inFromServer;
		private String ServerName;
		
		public Client(String server){
			ServerName = server;
		}
		
		public void connect() {
			try {
				clientSocket = new Socket(ServerName, 8000);
				inFromServer = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
				outServer = new DataOutputStream(clientSocket.getOutputStream());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			out.setText("you may start writing" + String.format("%n"));

		}
		
		public void writeServer(String s){
			try {
				outServer.writeBytes(s);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public String readServer(){
			String s = null;
			do{
				try {
					s = inFromServer.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}while(s==null || s==" " || s=="");
			return s;
			
		}
		
		public void close() {
			try {
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void paint(Graphics g) {
		
		if (Math.abs(wChanged - this.getWidth()) > 0
				|| Math.abs(hChanged - this.getHeight()) > 0) {
			wChanged = this.getWidth();
			hChanged = this.getHeight();

			in.setBounds(wChanged / 10, (6 * hChanged) / 10,
					(7 * wChanged) / 10, (3 * hChanged) / 10);
			out.setBounds(wChanged / 10, (hChanged) / 10,
					(75 * wChanged) / 100, (4 * hChanged) / 10);
			// button.setBounds((85 * wChanged) / 100, (65 * hChanged) / 100,
			// wChanged / 10, (20 * hChanged) / 100);
		}
		update(g);
	}

	public void update(Graphics g) {
		g.clearRect(0, 0, wChanged, hChanged);
		g.drawString("chat", wChanged / 10, (12 * hChanged) / 100);
		g.drawString("input", wChanged / 10, (63 * hChanged) / 100);
	}

	public static void main(String args[]) {
		String s = null;
		int state = 0; // 0 - sending , 1 - reading

		// change "pedro-pc" to the computer you want to connect.

		// to see your computer name type in command line "hostname"
		
		
		TcpClient frame = new TcpClient();
		Client client = frame.new Client("pedro-pc");
		client.connect();
		while (!finished) {
			while (state == 0) {
				System.err.println();
				if(stringReady){
					client.writeServer(in.getText()+ String.format("%n"));
					out.setText(out.getText() + "Tiago send: " + in.getText() + String.format("%n"));
					in.setText("");
					state = (state + 1) % 2;
					stringReady = false;
					out.setText(out.getText() + "Pedro is writing... " + String.format("%n"));
					s=null;
				}
			}

			while (state == 1) {
				s = client.readServer();
				out.setText(out.getText() + "Pedro send: " + s
						+ String.format("%n"));
				state = (state + 1) % 2;
				s=null;
			}
		}

		client.close();
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			stringReady = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}
}
