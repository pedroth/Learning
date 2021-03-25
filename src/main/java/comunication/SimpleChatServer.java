package comunication;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

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
}
