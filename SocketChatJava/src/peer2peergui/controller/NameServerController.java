package peer2peergui.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class NameServerController {
    private static ArrayList<Observer> observers = new ArrayList<>();
    private Socket clientSocket;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;

    public NameServerController(String host, int clientPort) {
        try {
            clientSocket = new Socket(host, clientPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToNameServer(String name, String host, int port) {
        String[] valueArray = {host, String.valueOf(port), name};

        Thread nameServerThread = new Thread(() -> {
            try {
                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String[] serverInfo = {"localhost", valueArray[1], valueArray[2]};
                outToServer.writeBytes("subscribe-" + serverInfo[0] + "," + serverInfo[1] + "," + serverInfo[2] + "\n");
                outToServer.close();
                inFromServer.close();
                clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        nameServerThread.start();
    }

}
