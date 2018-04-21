package comunication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SimpleChatClient extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int PORT = 5000;
    private int wChanged, hChanged;
    private TextArea in, out;
    private TextField userName;
    private BufferedReader reader;
    private PrintWriter writer;
    private Socket sock;
    private NetworkSetUp networkSetUp;

    public SimpleChatClient() {
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
        userName = new TextField();

        in.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    writer.println(userName.getText() + " : " + in.getText());
                    writer.flush();
                    in.setText("");
                    in.requestFocus();
                }
            }
        });
        this.add(in);
        this.add(out);
        this.add(userName);

        wChanged = this.getWidth();
        hChanged = this.getHeight();

        in.setBounds(wChanged / 10, (6 * hChanged) / 10, (7 * wChanged) / 10,
                (3 * hChanged) / 10);
        out.setBounds(wChanged / 10, (hChanged) / 10, (75 * wChanged) / 100,
                (4 * hChanged) / 10);
        userName.setBounds(7 * wChanged / 10, (3 * hChanged) / 100,
                (15 * wChanged) / 100, (5 * hChanged) / 100);

        userName.setText("User Name X");
    }

    public static void main(String args[]) {
        String option = "";
        if (args.length > 0) {
            option += args[0];
        }
        NetworkSetUp networkSetUp;
        SimpleChatClient chat = new SimpleChatClient();
        switch (option) {
            case "home":
                networkSetUp = chat.new HomeSetUp();
                break;
            case "tecnico":
                networkSetUp = chat.new TecnicoSetup();
                break;
            default:
                networkSetUp = chat.new WorldSetUp();
                break;
        }
        chat.go(networkSetUp);
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
            userName.setBounds(7 * wChanged / 10, (3 * hChanged) / 100,
                    (15 * wChanged) / 100, (5 * hChanged) / 100);

        }
        update(g);
    }

    public void update(Graphics g) {
        g.clearRect(0, 0, wChanged, hChanged);
        g.drawString("chat", wChanged / 10, (12 * hChanged) / 100);
        g.drawString("input", wChanged / 10, (63 * hChanged) / 100);
    }

    public void go(NetworkSetUp networkSetUp) {
        networkSetUp.setUpNetwork();
        Thread t = new Thread(new ServerReader());
        t.start();
    }

    private interface NetworkSetUp {
        void setUpNetwork();
    }

    public class ServerReader implements Runnable {

        public ServerReader() {
        }

        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    out.append(message + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private class TecnicoSetup implements NetworkSetUp {

        @Override
        public void setUpNetwork() {
            boolean foundConnect = false;
            int lastIPNumber = 100;
            out.setText("loading connection\n");
            while (!foundConnect) {
                try {
                    String ip = "193.136.128." + lastIPNumber;
                    sock = new Socket(ip, PORT);
                    reader = new BufferedReader(new InputStreamReader(
                            sock.getInputStream()));
                    writer = new PrintWriter(sock.getOutputStream());
                    foundConnect = true;
                    out.setText("found connection\n");

                } catch (Exception e) {
                    e.printStackTrace();
                    lastIPNumber++;
                }
            }
        }
    }

    private class WorldSetUp implements NetworkSetUp {

        @Override
        public void setUpNetwork() {
            out.setText("loading connection\n");
            try {
                String ip = "pedroth.ddns.net";
                System.out.println(ip);
                sock = new Socket(ip, PORT);
                reader = new BufferedReader(new InputStreamReader(
                        sock.getInputStream()));
                writer = new PrintWriter(sock.getOutputStream());
                out.setText("found connection\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class HomeSetUp implements NetworkSetUp {
        private static final int MAX_IP = 255;
        private static final int MAX_IP_SQUARE = 65025;

        @Override
        public void setUpNetwork() {
            boolean foundConnect = false;
            out.setText("loading connection\n");
            int lastIP = 1;
            int lastLastIP = 1;
            while (!foundConnect) {
                try {
                    int maxIpPlusOne = MAX_IP + 1;
                    int llStr = (lastLastIP % maxIpPlusOne) == 0 ? 1 : lastLastIP % maxIpPlusOne;
                    int lStr = (lastIP % maxIpPlusOne) == 0 ? 1 : lastIP % maxIpPlusOne;
                    String ip = "192.168." + llStr + "." + lStr;
                    System.out.println(ip);
                    sock = new Socket(ip, PORT);
                    reader = new BufferedReader(new InputStreamReader(
                            sock.getInputStream()));
                    writer = new PrintWriter(sock.getOutputStream());
                    foundConnect = true;
                    out.setText("found connection\n");
                } catch (Exception e) {
                    if (lastIP > MAX_IP_SQUARE) {
                        out.setText("No Ip found");
                        break;
                    }
                    lastIP++;
                    int ratio = lastIP / MAX_IP;
                    lastLastIP = ratio == 0 ? 1 : ratio;
                    e.printStackTrace();
                }
            }
        }
    }
}