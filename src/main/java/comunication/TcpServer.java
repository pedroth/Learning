package comunication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpServer extends JFrame implements KeyListener {

    private static final long serialVersionUID = 1L;
    public static boolean stringReady;
    public static boolean finished = false;
    private static TextArea in, out;
    private int wChanged, hChanged;

    public TcpServer() {
        super("Server");

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
        add(in);
        add(out);
        stringReady = false;
        wChanged = this.getWidth();
        hChanged = this.getHeight();
    }

    public static void main(String args[]) {
        String s = null;
        int state = 1; // 0 - sending , 1 - reading
        TcpServer frame = new TcpServer();
        Server server = frame.new Server();
        server.connect();
        while (!finished) {
            while (state == 0) {
                if (stringReady) {
                    server.writeClient(in.getText() + String.format("%n"));
                    out.setText(out.getText() + "Pedro send: " + in.getText()
                            + String.format("%n"));
                    in.setText("");
                    state = (state + 1) % 2;
                    stringReady = false;
                    out.setText(out.getText() + "Tiago is writing... "
                            + String.format("%n"));
                    s = null;
                }
            }
            while (state == 1) {
                s = server.readClient();
                System.out.print((int) s.charAt(0));
                out.setText(out.getText() + "Tiago send: " + s
                        + String.format("%n"));
                s = null;
                state = (state + 1) % 2;
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

    @Override
    public void keyPressed(KeyEvent arg0) {
        if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
            stringReady = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    public class Server {
        private DataOutputStream outClient;
        private ServerSocket serverSocket;
        private Socket connectionSocket;
        private BufferedReader inFromClient;

        Server() {
        }

        public void connect() {
            out.setText("A conectar-se " + String.format("%n"));
            try {
                serverSocket = new ServerSocket(8000);

                connectionSocket = serverSocket.accept();

                out.setText(out.getText() + "Conectado com Tiago"
                        + String.format("%n"));

                inFromClient = new BufferedReader(new InputStreamReader(
                        connectionSocket.getInputStream()));

                outClient = new DataOutputStream(
                        connectionSocket.getOutputStream());

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void writeClient(String s) {
            try {
                outClient.writeBytes(s);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public String readClient() {
            String s = null;
            do {
                try {
                    s = inFromClient.readLine();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } while (s == null || s == " " || s == "");
            return s;
        }

    }
}
