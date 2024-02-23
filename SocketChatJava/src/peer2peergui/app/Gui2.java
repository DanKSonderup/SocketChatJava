package peer2peergui.app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import peer2peergui.controller.ClientController;
import peer2peergui.controller.NameServerController;
import peer2peergui.controller.Observer;
import peer2peergui.controller.ServerController;

import java.io.IOException;

public class Gui2 extends Application implements Observer {

    private final ServerController serverController = new ServerController(6970);
    private ClientController clientController;
    private ClientController nameServerClientController;
    private NameServerController nameServerController;
    private String userName;

    @Override
    public void start(Stage stage) {
        stage.setTitle("App2 - Peer2Peer Chat");
        GridPane pane = new GridPane();
        this.initContent(pane);

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void init() {
        nameServerClientController = new ClientController("localhost", 6972);
        ClientController.addObserver(this);
        ServerController.addObserver(this);
        serverController.startServer();
    }
    private final TextArea chatWindow = new TextArea();
    private final TextField txfInput = new TextField();
    private final Button btnSend = new Button("Send");

    // ConnectRequest, CanSend, Client Requesting Server, Inserting name
    // Some checks have been implemented later, hence the inorder logic
    private boolean[] actionState = {false, false, false, true};

    private void initContent(GridPane pane) {
        // show or hide grid lines
        pane.setGridLinesVisible(false);
        // set padding of the pane
        pane.setPadding(new Insets(20));
        // set horizontal gap between components
        pane.setHgap(10);
        // set vertical gap between components
        pane.setVgap(10);

        Label lblChat = new Label("Chat");

        pane.add(lblChat, 0, 0);
        pane.add(chatWindow, 0, 2);
        pane.add(txfInput, 0, 3);
        pane.add(btnSend, 1, 3);

        // Sætter Enter knappen til at klikke på send button
        txfInput.setOnKeyPressed(event -> {
                    if (event.getCode().equals(KeyCode.ENTER)) {
                        btnSend.fire();
                    }
                }
        );

        btnSend.setOnAction(event -> this.sendAction());

        String typeInName = "Velkommen til chatprogrammet, venligst indtast dit navn: \n";

        chatWindow.setText(typeInName);
    }

    @Override
    public void update(String content) {
        String currentText = chatWindow.getText();
        String[] controllerAnswer = content.split("-");
        String typeOfAction = controllerAnswer[0];
        String message = controllerAnswer[1];


        if (typeOfAction.contains("NameServer")) {
            System.out.println("NameServer responded");
            String hostIP = message.split(",")[0];
            int port = Integer.parseInt(message.split(",")[1]);
            clientController = new ClientController(hostIP,port);
            clientController.sendChatRequest(userName);
        }


        if(clientController != null) {
            if (typeOfAction.contains("ConnectAccept")) {
                if (typeOfAction.contains("true")) {
                    actionState[1] = true;
                }
                chatWindow.setText(currentText + message + "\n");
                clientController.handleReceiverMessages();
            }
            if (typeOfAction.contains("Message")) {
                chatWindow.setText(currentText + message + "\n");
            }

            chatWindow.appendText("");
            return;
        }
        if (typeOfAction.contains("ConnectRequestMessage")) {
            chatWindow.setText(currentText + message + "\n");
        }
        if (typeOfAction.contains("ConnectRequest")) {
            chatWindow.setText(currentText + message + "\n");
            actionState[0] = true;
        }
        if (typeOfAction.contains("ConnectAccept")) {
            if(typeOfAction.contains("true")) {
                actionState[1] = true;
            }
            chatWindow.setText(currentText + message + "\n");
            serverController.handleReceiverMessages();
        }
        if (typeOfAction.contains("Message")) {
            chatWindow.setText(currentText + message + "\n");
        }
        chatWindow.appendText("");
    }

    public void sendAction() {
        String inputText = txfInput.getText();
        String currentText = chatWindow.getText();

        if (actionState[3]) {
            userName = inputText;
            nameServerController = new NameServerController("localhost", 6972);
            String welcome = "Mit navn: " + userName + "\n" + "Hvis du ønsker at starte en chat samtale tast 'c'" +
                    "\nHvis du vil have en besked fra en anden, " +
                    "så afvent en forespørgsel\n";
            chatWindow.setText(currentText + welcome + "\n");
            nameServerController.addToNameServer(inputText, "localhost", 6970);
            actionState[3] = false;
        }
        if (inputText.equals("c")) {
            chatWindow.setText(currentText + "Indtast navnet på den du gerne vil snakke med:" + "\n");
            actionState[2] = true;
            txfInput.clear();
            chatWindow.appendText("");
            return;
        }

        if (actionState[2]) {
            chatWindow.setText(currentText + inputText + "\n");
            if (nameServerClientController == null) {
                nameServerClientController = new ClientController("localhost", 6972);
            }
            nameServerClientController.requestNameServer(inputText);
            actionState[2] = false;
        }

        if(actionState[0]) {
            try {
                serverController.acceptRequest(inputText);
                serverController.handleSenderMessage("", actionState[1]);
                actionState[0] = false;
            } catch (IOException e) {
                chatWindow.setText(currentText + "En fejl opstod");
            }
        } else if (actionState[1]) {
            String message = txfInput.getText();
            try {
                if (clientController != null) {
                    clientController.handleSenderMessage(message, actionState[1]);
                } else {
                    serverController.handleSenderMessage(message, actionState[1]);
                }
                chatWindow.setText(currentText + "Mig: " + inputText + "\n");
            } catch (IOException e) {
                chatWindow.setText(currentText + "En fejl opstod");
            }
        }

        chatWindow.appendText("");
        txfInput.clear();
    }
}
