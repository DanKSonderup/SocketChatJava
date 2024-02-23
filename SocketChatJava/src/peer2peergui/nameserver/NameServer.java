package peer2peergui.nameserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class NameServer {
    private static HashMap<String, Host> servers = new HashMap<>();
    private static ServerSocket serverSocket;
    public static void main(String[] args) throws IOException {
        try {
            serverSocket = new ServerSocket(6972);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Host testHost = new Host("localhost", 6970);
        servers.put("Daniel", testHost);
        System.out.println("Server is running");
        while (true) {
            new EchoClientHandler(serverSocket.accept()).start();
        }
    }


    private static class EchoClientHandler extends Thread {
        private Socket connectionSocket;
        private BufferedReader inFromClient;
        private DataOutputStream outToClient;

        public EchoClientHandler(Socket socket) {
            this.connectionSocket = socket;
        }

        public void run() {
            System.out.println("Thread running");
            try {
                    System.out.println("Connection established");

                    inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                    String clientRequest = inFromClient.readLine();
                    String[] requestString = clientRequest.split("-");
                    String typeOfAction = requestString[0];
                    System.out.println(typeOfAction);
                    String message = requestString[1];
                    if (typeOfAction.equals("subscribe")) {
                        String[] hostInfo = message.split(",");
                        subscribeToNameServer(hostInfo[0], Integer.parseInt(hostInfo[1]), hostInfo[2]);
                    }
                    if (typeOfAction.equals("serverRequest")) {
                        outToClient.writeBytes(sendHostAndPortToRequester(message) + "\n");
                    }

            } catch (IOException e) {

            }
        }

        private static void subscribeToNameServer(String hostIp, int port, String name) {
            Host host = new Host(hostIp, port);
            servers.put(name, host);
            System.out.println("Server with name: " + name + " ip: " + host.getHostIp() + " and port: " + host.getServerPort() + " added");
        }

        private static String sendHostAndPortToRequester(String requestedServer) {
            Host host = servers.get(requestedServer);
            return "" + host.getHostIp() + "," + host.getServerPort();
        }
    }
}
