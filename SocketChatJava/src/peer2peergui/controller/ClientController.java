package peer2peergui.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ClientController {

    private static ArrayList<Observer> observers = new ArrayList<>();
    private Socket clientSocket;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;

    public ClientController(String host, int clientPort) {
        try {
            clientSocket = new Socket(host, clientPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendChatRequest() {
        Thread requestThread = new Thread(() -> {
            String serverResponse = "";
            String chatRequest = "Snakke Oliver";
            try {
                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                outToServer.writeBytes(chatRequest + '\n');
                while (serverResponse.isEmpty()) {
                    serverResponse = inFromServer.readLine();
                }
                notifyObservers("Message -Anden person: " + serverResponse);
            } catch (IOException e) {
                System.out.println(e);
            }
            if (serverResponse.contains("Ja")) {
                notifyObservers("ConnectAccept(true) -Modtager har accepteret din forespørgsel \nDu kan nu sende den første besked \n");
            } else if (serverResponse.contains("Nej")){
                notifyObservers("ConnectAccept(false) -Modtager vil ikke snakke");
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        });
        requestThread.start();
    }
    public void handleReceiverMessages() {
        Thread messageThread = new Thread(() -> {
            try {
                while (true) {
                    String serverResponse = "";
                    while (serverResponse.isEmpty()) {
                        serverResponse = inFromServer.readLine();
                    }
                    notifyObservers("Message -Anden person: " + serverResponse);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        messageThread.start();
    }

    public void handleSenderMessage(String senderMessage, boolean send) throws IOException {
        if (send) {
            outToServer.writeBytes(senderMessage + '\n');
        }

    }

    public static void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void notifyObservers(String contentOfUpdate) {
        for (Observer ob: observers) {
            ob.update(contentOfUpdate);
        }
    }
}
