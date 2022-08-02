package client;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import model.*;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;


@Slf4j
public class ClientNetty extends Network implements Initializable, CallbackToClient {

    //client form
    public ListView<String> serverView;
    public ListView<String> clientView;
    public TextField serverPath;
    public TextField clientPath;
    public Button serverFolderUpBtn;
    public Button clientFolderUpBtn;
    public Button uploadBtn;
    public Button downloadBtn;

    private Path clientDir;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            processorRegistry.registerCallback(ClientNetty.this);
            clientDir = Path.of("D:\\");
            //Paths.of(System.getProperty("user.home"));
            clientPath.setText(clientDir.toString());
            updateClientView();
            initClientMouseListeners();
            initClientButtonsListeners();

            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateServerFilesList(List<String> list) {
        Platform.runLater(() -> {
            serverView.getItems().clear();
            serverView.getItems().addAll(list);
        });
    }

    @Override
    public void updateClientView() {
        try {
            clientView.getItems().clear();
            Files.list(clientDir)
                    .map(p -> p.getFileName().toString())
                    .forEach(f -> clientView.getItems().add(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setServerPath(String svrPath) {
        Platform.runLater(() -> serverPath.setText(svrPath));
    }

    @Override
    public Path getClientDir() {
        return clientDir;
    }

//
//    private void initClientKeyListeners() {
//        serverView.setOnKeyPressed(e -> {
//            if (e.getCode() == KeyCode.DELETE) {
//                try {
//                    Path path = Path.of(serverPath.getText())
//                            .resolve(Path.of(serverView.getSelectionModel().getSelectedItem()));
//                    if (Files.isDirectory(path)) {
//                        //todo удалить папку и её содержимое
//                    } else {
//                        os.writeObject(new DeleteServerFileMessage(path));
//                    }
//                    os.flush();
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
//    }

    private void initClientMouseListeners() {
        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Path path = clientDir.resolve(getItem());
                if (Files.isDirectory(path)) {
                    clientDir = path;
//   todo                 processorRegistry.setClientDir(clientDir);
                    Platform.runLater(this::updateClientView);
                    clientPath.setText(clientDir.toString());
                } else {
                    Desktop desktop = Desktop.getDesktop();
                    if (path.toFile().exists()) {
                        try {
                            desktop.open(path.toFile());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        serverView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                try {
                    Path path = Path.of(serverPath.getText())
                            .resolve(Path.of(serverView.getSelectionModel().getSelectedItem()));
                    if (Files.isDirectory(path)) {
                        os.writeObject(new ServerDirMessage(path));
                    } else {
                        Desktop desktop = Desktop.getDesktop();
                        if (path.toFile().exists()) {
                            desktop.open(path.toFile());
                        }
                    }
                    os.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void initClientButtonsListeners() {

        clientFolderUpBtn.setOnAction(e -> {
            Path pathUp = Path.of(clientPath.getText()).getParent();
            if (Files.exists(pathUp)) {
                clientDir = pathUp;
//  todo              processorRegistry.setClientDir(clientDir);
                Platform.runLater(this::updateClientView);
                clientPath.setText(clientDir.toString());
            }
        });
        serverFolderUpBtn.setOnAction(e -> {
            try {
                os.writeObject(new ServerDirMessage(Path.of(serverPath.getText()).getParent()));
                os.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        uploadBtn.setOnMouseClicked(e -> {
            try {
                String fileName = clientView.getSelectionModel().getSelectedItem();
                os.writeObject(new UploadFileMessage(clientDir.resolve(fileName)));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        downloadBtn.setOnMouseClicked(e -> {
            try {
                String currentDir = serverPath.getText();
                String fileName = serverView.getSelectionModel().getSelectedItem();
                Path path = Path.of(currentDir).resolve(Path.of(fileName));
                if (!Files.isDirectory(path)) {
                    os.writeObject(new DownloadFileMessage(path));
                } else {
                    //todo скачать папку
                }
                os.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private String getItem() {
        return clientView.getSelectionModel().getSelectedItem();
    }
}
