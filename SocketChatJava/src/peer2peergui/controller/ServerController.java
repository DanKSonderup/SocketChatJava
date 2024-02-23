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

    public ServerController(int serverPort) {
        try {
            serverPort = serverPort;
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        serverThread = new Thread(() -> {
            try {
                connectionSocket = serverSocket.accept();
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                String clientRequest = inFromClient.readLine();
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
    public void acceptRequest(String answer) throws IOException {

        inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        // String clientResponse = inFromClient.readLine();
        outToClient = new DataOutputStream(connectionSocket.getOutputStream());

        if (answer.equals("Ja")) {
            outToClient.writeBytes("Ja" + '\n');
            notifyObservers("ConnectAccept(true) -Svar sendt, afvent første besked fra forespørger \n");
        } else if (answer.equals("Nej")){
            notifyObservers("ConnectAccept(false) -Anmodning afvist");
            outToClient.writeBytes("Nej" + '\n');
        } else {
            notifyObservers("ConnectAccept(false) -Ugyldig kommando, anmodning afvist");
        }
    }

    public void handleReceiverMessages() {
        Thread messageThread = new Thread(() -> {
            try {
                while (true) {
                    String clientResponse = "";
                    while (clientResponse.isEmpty()) {
                        clientResponse = inFromClient.readLine();
                    }
                    notifyObservers("Message -Anden person: " + clientResponse);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        messageThread.start();
    }

    public void handleSenderMessage(String senderMessage, boolean send) throws IOException {
        if (send) {
        outToClient.writeBytes(senderMessage + '\n');
        }
    }
}
