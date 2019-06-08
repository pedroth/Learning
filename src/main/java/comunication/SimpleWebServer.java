package comunication;


import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleWebServer {
    private int port = 8080;
    private static int nextID = 0;
    private Map<Integer, Socket> clientById;

    private SimpleWebServer() throws UnknownHostException {
        System.out.println(Inet4Address.getLocalHost().getHostAddress() + ":" + port);
        clientById = new HashMap<>(1);
    }

    private SimpleWebServer(int port) throws UnknownHostException {
        this.port = port;
        System.out.println(Inet4Address.getLocalHost().getHostAddress() + ":" + port);
        clientById = new HashMap<>(1);
    }

    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                final String regex = "[0-9]*";
                final Pattern pattern = Pattern.compile(regex);
                final Matcher matcher = pattern.matcher(args[0]);
                SimpleWebServer simpleWebServer = new SimpleWebServer(matcher.find() ? Integer.valueOf(args[0]) : 8080);
                simpleWebServer.go(simpleWebServer.new ComplexHttpHandler());
            } else {
                SimpleWebServer simpleWebServer = new SimpleWebServer();
                simpleWebServer.go(simpleWebServer.new ComplexHttpHandler());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void go(ClientHandler clientHandler) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            clientById.put(++nextID, clientSocket);
            clientHandler.setClientSocket(clientSocket);
            Thread t = new Thread(clientHandler);
            t.start();
            System.out.println("got connection: " + clientSocket.getInetAddress().toString());
        }
    }

    public abstract class ClientHandler implements Runnable {
        BufferedReader reader;
        Socket clientSocket;

        ClientHandler() {
            // do nothing
        }

        public ClientHandler(Socket clientSocket) {
            this.setClientSocket(clientSocket);
        }

        public Socket getClientSocket() {
            return clientSocket;
        }

        void setClientSocket(Socket clientSocket) {
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
                    System.out.println("In:\t" + message);
                    action(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public abstract void action(String message) throws IOException;
    }

    public class SimpleHttpHandler extends ClientHandler {

        public SimpleHttpHandler() {
            super();
        }

        @Override
        public void action(String message) {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println("\r\n");
                out.println("<iframe width=\"420\" height=\"315\" src=\"https://www.youtube.com/embed/bQC8w0fns6A?autoplay=1\" frameborder=\"0\" allowfullscreen></iframe>");
                out.println("<p>Hello world </p>");
                out.flush();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ComplexHttpHandler extends ClientHandler {

        ComplexHttpHandler() {
            super();
        }

        private void sendErrorMessage(BufferedOutputStream out, int code, String message) throws IOException {
            sendHeader(out, code, "text/html", -1, System.currentTimeMillis());
            out.write(("<h1> " + message + " </h1>").getBytes());
        }

        private void sendHeader(BufferedOutputStream out, int code, String contentType, long contentLength, long lastModified) throws IOException {
            String text = "HTTP/1.0 " + code + " OK\r\n" +
                    "Date: " + new Date().toString() + "\r\n" +
                    "Server: PedrothServer/1.0\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    ((contentLength != -1) ? "Content-Length: " + contentLength + "\r\n" : "") +
                    "Last-modified: " + new Date(lastModified).toString() + "\r\n" +
                    "\r\n";
            System.out.println("Out:\t" + text);
            out.write(text.getBytes());
        }

        private void sendFile(BufferedOutputStream out, File file) {
            BufferedInputStream fileReader;
            try {
                fileReader = new BufferedInputStream(new FileInputStream(file));
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileReader.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void action(String message) throws IOException {
            BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());
            try {
                String[] split = message.split(" ");
                clientSocket.setSoTimeout(30000);

                if (split.length > 1 && "GET".equals(split[0]) && !split[1].substring(1).isEmpty()) {
                    String address = split[1].substring(1);
                    File file = new File(address);

                    String fileName = file.getName();
                    System.out.println("Out:\t" + fileName);
                    sendHeader(out, 200, "", file.length(), System.currentTimeMillis());
                    sendFile(out, file);
                } else {
                    sendErrorMessage(out, 404, "There is nothing here!!");
                }
            } catch (Exception e) {
                sendErrorMessage(out, 404, e.getMessage());
            }
            out.flush();
            clientSocket.close();
        }
    }

}
