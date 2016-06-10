package comunication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class SimpleChatServer {
    private static final int PORT = 5000;
    ArrayList<PrintWriter> ClientsOutputs;

    public SimpleChatServer() {
        try {
            System.out.println(Inet4Address.getLocalHost().getHostAddress() + ":" + PORT);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SimpleChatServer().go();
    }

    public void go() {
        ClientsOutputs = new ArrayList<>();
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                ClientsOutputs.add(writer);

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
                System.out.println("got connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tellEveryOne(String message) {
        Iterator<PrintWriter> it = ClientsOutputs.iterator();

        while (it.hasNext()) {
            PrintWriter writer = it.next();
            writer.println(message);
            writer.flush();
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
