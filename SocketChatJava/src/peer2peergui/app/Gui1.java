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
import peer2peergui.controller.Observer;
import peer2peergui.controller.ServerController;

import java.io.IOException;

public class Gui1 extends Application implements Observer {

    private final ServerController serverController = new ServerController(6969);

    private ClientController clientController;

    @Override
    public void start(Stage stage) {
        stage.setTitle("App1 - Peer2Peer Chat");
        GridPane pane = new GridPane();
        this.initContent(pane);

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void init() {
        ClientController.addObserver(this);
        ServerController.addObserver(this);
        serverController.startServer();
    }
    private final TextArea chatWindow = new TextArea();
    private final TextField txfInput = new TextField();
    private final Button btnSend = new Button("Send");
    private boolean isClient = false;

    // ConnectRequest, ConnectAccept, CanSend
    private boolean[] actionState = {false, false, false};

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

        String welcome = "Velkommen til chatprogrammet " +
                "\nHvis du ønsker at starte en chat samtale tast 'c'" +
                "\nHvis du vil have en besked fra en anden, " +
                "så afvent en forespørgsel\n";
        chatWindow.setText(welcome);
    }

    @Override
    public void update(String content) {
        String currentText = chatWindow.getText();
        String[] controllerAnswer = content.split("-");
        String typeOfAction = controllerAnswer[0];
        String message = controllerAnswer[1];

        if(clientController != null) {
            if (typeOfAction.contains("ConnectAccept")) {
                if (typeOfAction.contains("true")) {
                    actionState[1] = true;
                    actionState[2] = true;
                }
                chatWindow.setText(currentText + message + "\n");
                clientController.handleReceiverMessages();
            }
            if (typeOfAction.contains("Message")) {
                chatWindow.setText(currentText + message + "\n");
                actionState[2] = true;
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
                actionState[2] = true;
            }
            chatWindow.setText(currentText + message + "\n");
            serverController.handleReceiverMessages();
        }
        if (typeOfAction.contains("Message")) {
            chatWindow.setText(currentText + message + "\n");
            actionState[2] = true;
        }
        chatWindow.appendText("");
    }

    public void sendAction() {
        String inputText = txfInput.getText();
        String currentText = chatWindow.getText();
        if (inputText.equals("c")) {
            chatWindow.setText(currentText + "Forespørger modtager om forbindelse" + "\n");
            clientController = new ClientController("localhost", 6970);
            clientController.sendChatRequest();
            txfInput.clear();
            chatWindow.appendText("");
            return;
        }

        if(actionState[0]) {
            try {
                serverController.acceptRequest(inputText);
                serverController.handleSenderMessage("", actionState[2]);
                actionState[0] = false;
            } catch (IOException e) {
                chatWindow.setText(currentText + "En fejl opstod");
            }
        } else if (actionState[2]) {
            String message = txfInput.getText();
            try {
                if (clientController != null) {
                    clientController.handleSenderMessage(message, actionState[2]);
                } else {
                    serverController.handleSenderMessage(message, actionState[2]);
                }
                chatWindow.setText(currentText + "Mig: " + inputText + "\n");
            } catch (IOException e) {
                chatWindow.setText(currentText + "En fejl opstod");
            }
            actionState[2] = false;
        }
        chatWindow.appendText("");
        txfInput.clear();
    }
}
