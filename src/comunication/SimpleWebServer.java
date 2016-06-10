package comunication;


import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SimpleWebServer {
    public static final int PORT = 8000;
    ArrayList<PrintWriter> ClientsOutputs;

    public SimpleWebServer() {
        try {
            System.out.println(Inet4Address.getLocalHost().getHostAddress() + ":" + PORT);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SimpleWebServer().go();
    }

    public void go() {
        ClientsOutputs = new ArrayList<>();
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                ClientsOutputs.add(writer);

                Thread t = new Thread(new ComplexHttpHandler(clientSocket));
                t.start();
                System.out.println("got connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract class ClientHandler implements Runnable {
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
                while ((message = reader.readLine()) != null && !clientSocket.isClosed()) {
                    System.out.println(message);
                    action(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public abstract void action(String message);
    }

    public class SimpleHttpHandler extends ClientHandler {

        public SimpleHttpHandler(Socket clientSocket) {
            super(clientSocket);
        }

        @Override
        public void action(String message) {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println("\r\n");
                out.println("<iframe width=\"420\" height=\"315\" src=\"https://www.youtube.com/embed/yfNBxNlZYJg?autoplay=1\" frameborder=\"0\" allowfullscreen></iframe>");
                out.println("<p>Hello world </p>");
                out.flush();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public class ComplexHttpHandler extends ClientHandler {

        public ComplexHttpHandler(Socket clientSocket) {
            super(clientSocket);
        }

        @Override
        public void action(String message) {
            try {
                String[] split = message.split(" ");
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println("\r\n");
                if (split.length > 1 && "/test".equals(split[1].toLowerCase())) {
                    try (BufferedReader br = new BufferedReader(new FileReader("C:/pedro/visualExperiments/index.html"))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            // process the line.
                            out.println(line);
                        }
                    }
                } else {
                    out.println("<h1>There is nothing here!!</h1>");
                }
                out.flush();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
