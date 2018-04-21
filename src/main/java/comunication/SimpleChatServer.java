package comunication;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleChatServer {
    private static final int PORT = 5000;
    private static int nextId = 0;
    ArrayList<PrintWriter> clientsOutputs;
    ArrayList<Socket> clientSockets;

    public SimpleChatServer() {
        try {
            System.out.println(Inet4Address.getLocalHost().getHostAddress() + ":" + PORT);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SimpleChatServer().go();
    }

    public void go() {
        clientsOutputs = new ArrayList<>();
        clientSockets = new ArrayList<>();
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                clientsOutputs.add(writer);
                clientSockets.add(clientSocket);

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
                System.out.println("got connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tellEveryOne(String message) {
        Iterator<PrintWriter> it = clientsOutputs.iterator();

        while (it.hasNext()) {
            PrintWriter writer = it.next();
            writer.println(message);
            writer.flush();
        }

    }

    private void tellEveryOneWebSocket(int message) {
        Iterator<Socket> it = clientSockets.iterator();

        while (it.hasNext()) {
            try {
                OutputStream writer = it.next().getOutputStream();
                writer.write(message);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ClientHandler implements Runnable {
        BufferedReader reader;
        Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    tellEveryOne(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class ClientHandlerWebSocket implements Runnable {
        private int id = nextId++;
        private BufferedReader reader;
        private Socket clientSocket;

        public ClientHandlerWebSocket(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                handshake();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handshake() {
            String data;
            try {
                data = new Scanner(clientSocket.getInputStream(), "UTF-8").useDelimiter("\\r\\n\\r\\n").next();
                Matcher get = Pattern.compile("^GET").matcher(data);

                if (get.find()) {
                    Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                    match.find();
                    byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                            + "Connection: Upgrade\r\n"
                            + "Upgrade: websocket\r\n"
                            + "Sec-WebSocket-Accept: "
                            + DatatypeConverter
                            .printBase64Binary(
                                    MessageDigest
                                            .getInstance("SHA-1")
                                            .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                                    .getBytes("UTF-8")))
                            + "\r\n\r\n")
                            .getBytes("UTF-8");

                    clientSocket.getOutputStream().write(response, 0, response.length);
                }
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        private Character decode(int message) {
            return (char) (message);
        }

        @Override
        public void run() {
            List<Integer> message = new ArrayList<>();
            int str;
            try {
                while ((str = clientSocket.getInputStream().read()) > -1) {
                    System.out.println(id + ":\t" + decode(str));
                }
//                tellEveryOneWebSocket(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
