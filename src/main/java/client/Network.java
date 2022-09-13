package client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import lombok.extern.slf4j.Slf4j;
import model.CloudMessage;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

//класс общих потоков и регистра процессов для форм клиента

@Slf4j
public class Network {
    protected static ObjectDecoderInputStream is;
    protected static ObjectEncoderOutputStream os;
    protected static ProcessorRegistry processorRegistry;

    public Network() {
        try {
            Socket socket = new Socket("localhost", 8189);
            System.out.println("Network created...");
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // read from network
    protected void readLoop() {
        try {
            while (true) {
                CloudMessage message = (CloudMessage) is.readObject();
                log.info("received: {}", message);
                processorRegistry.process(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Optional<ButtonType> showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Register information");
        alert.setContentText(message);
        alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
        return alert.showAndWait();
    }

    protected Optional<ButtonType> showConfirmAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Register information");
        alert.setContentText(message);
        return alert.showAndWait();
    }

    protected void writeToServer(CloudMessage message) {
        try {
            os.writeObject(message);
            os.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected Optional<String> enterFolderNameDialog() {
        TextInputDialog inputDialog = new TextInputDialog();
       // inputDialog.setContentText("Folder name:: ");
        inputDialog.setHeaderText("Enter folder name:");
//        inputDialog.showAndWait();
        return inputDialog.showAndWait();
    }

}
