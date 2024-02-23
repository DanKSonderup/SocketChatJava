package peer2peergui.controller;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerController {
    private static ArrayList<Observer> observers = new ArrayList<>();
    private ServerSocket serverSocket;
    private Socket connectionSocket;
    private Thread serverThread;
    private DataOutputStream outToClient;
    private BufferedReader inFromClient;
    private String receiverName;
    private DataOutputStream outToServer;

    public ServerController(int serverPort) {
        try {
            serverPort = serverPort;
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToNameServer(String name, String host, int port) {
        String[] valueArray = {host, String.valueOf(port), name};

        Thread nameServerThread = new Thread(() -> {
        try {
            connectionSocket = new Socket("localhost", 6972);
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
            String[] serverInfo = {"localhost", valueArray[1], valueArray[2]};
            outToServer.writeBytes("subscribe-" + serverInfo[0] + "," + serverInfo[1] + "," + serverInfo[2] + "\n");
            // outToServer.close();
            // inFromServer.close();
            // clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        });
        nameServerThread.start();
    }
    public void startServer() {
        serverThread = new Thread(() -> {
            try {
                connectionSocket = serverSocket.accept();
                System.out.println("Forbindelse modtaget");
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                String clientRequest = inFromClient.readLine();
                receiverName = clientRequest.split(" ")[1];
                notifyObservers("ConnectRequestMessage -" + clientRequest);
                notifyObservers("ConnectRequest -En modtager vil snakke med dig, vil du acceptere? (Ja/Nej)");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        serverThread.start();
    }
    public static void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void notifyObservers(String contentOfUpdate) {
        for (Observer ob: observers) {
            ob.update(contentOfUpdate);
        }
    }
    public void acceptRequest(String answer) {
        Thread requestThread = new Thread(() -> {
            try {
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                // String clientResponse = inFromClient.readLine();
                System.out.println("Kommer forbi");
                outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                // receiverName = clientResponse.split(" ")[1];
                if (answer.equals("Ja")) {
                    outToClient.writeBytes("Ja" + "\n");
                    notifyObservers("ConnectAccept(true) -Svar sendt, du kan nu begynde at chatte \n");
                } else if (answer.equals("Nej")){
                    notifyObservers("ConnectAccept(false) -Anmodning afvist");
                    outToClient.writeBytes("Nej" + "\n");
                } else {
                    notifyObservers("ConnectAccept(false) -Ugyldig kommando, anmodning afvist");
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        });
        requestThread.start();
    }

    public void handleReceiverMessages() {
        Thread messageThread = new Thread(() -> {
            try {
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                while (true) {
                    String clientResponse = "";
                    while (clientResponse.isEmpty()) {
                        clientResponse = inFromClient.readLine();
                    }
                    notifyObservers("Message -" + receiverName + ": " + clientResponse);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        messageThread.start();
    }

    public void handleSenderMessage(String senderMessage, boolean send) throws IOException {
        outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        if (send) {
        outToClient.writeBytes(senderMessage + "\n");
        }
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }
}
