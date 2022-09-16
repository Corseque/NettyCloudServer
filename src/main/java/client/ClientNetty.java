package client;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import lombok.extern.slf4j.Slf4j;
import model.*;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


@Slf4j
public class ClientNetty extends Network implements Initializable, CallbackToClientForm {

    public ListView<String> serverView;
    public ListView<String> clientView;
    public TextField serverPath;
    public TextField clientPath;
    public Button serverFolderUpBtn;
    public Button clientFolderUpBtn;
    public Button uploadBtn;
    public Button downloadBtn;
    public Button createFolderBtn;
    public Button deleteBtn;
    public ContextMenu contextMenuServer = new ContextMenu();
    public ContextMenu contextMenuClient = new ContextMenu();
    public MenuItem uploadItem = new MenuItem("Upload file");
    public MenuItem downloadItem = new MenuItem("Download file");
    public MenuItem deleteFileItem = new MenuItem("Delete file");
    public MenuItem createFolderItem = new MenuItem("Create folder");
    public MenuItem deleteFolderItem = new MenuItem("Delete folder");


    private Path clientDir;
//    private Path serverRootDir;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            processorRegistry.registerCallback(ClientNetty.this);
            clientDir = Path.of("D:\\");
            //Paths.of(System.getProperty("user.home"));
            clientPath.setText(clientDir.toString());

            contextMenuClient.getItems().addAll(uploadItem);
            contextMenuServer.getItems().addAll(downloadItem, deleteFileItem, createFolderItem, deleteFolderItem);

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
    public void processReplaceFileAlert(String alert) {
        Optional<ButtonType> result = showConfirmAlert(alert);
        ButtonType bt = result.orElse(ButtonType.CANCEL);
        if (bt == ButtonType.OK) {
            try {
                String fileName = clientView.getSelectionModel().getSelectedItem();
                writeToServer(new ReplaceFileMessage(clientDir.resolve(fileName), Path.of(serverPath.getText())));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void processCreateServerDirAlert(String alert) {
        Optional<ButtonType> result = showConfirmAlert(alert);
        ButtonType bt = result.orElse(ButtonType.CANCEL);
        if (bt == ButtonType.OK) {
            createFolder();
        }
    }

    @Override
    public void processDeleteServerDirAlert(String alert) {
        Optional<ButtonType> result = showConfirmAlert(alert);
        ButtonType bt = result.orElse(ButtonType.CANCEL);
        if (bt == ButtonType.OK) {
            try {
                writeToServer(new DeleteServerDirMessage(Path.of(serverPath.getText()).resolve(Path.of(serverView.getSelectionModel().getSelectedItem()))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    @Override
//    public void downloadFolderFiles(String sPath, String folderName, List<String> files) {
//        try {
//            //todo проверка на наличие папки с таким же названием, если есть сделать с номером (1), (2) ...
//            Path cPath = Path.of(clientPath.getText()).resolve(Path.of(folderName));
//            Files.createDirectory(cPath);
//            Platform.runLater(this::updateClientView);
//            Iterator<String> iterator = files.iterator();
//            while (iterator.hasNext()) {
//                Path path = Path.of(sPath).resolve(Path.of(folderName)).resolve(iterator.next());
//                writeToServer(new DownloadFileMessage(path, cPath));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void downloadFolderFiles(Path sPath, Path cPath) {
        try {
            writeToServer(new DownloadFileMessage(sPath, cPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void initClientKeyListeners() {
//        serverView.setOnKeyPressed(e -> {
//            if (e.getCode() == KeyCode.DELETE) {
//                try {
//                    Path path = Path.of(serverPath.getText())
//                            .resolve(Path.of(serverView.getSelectionModel().getSelectedItem()));
//                    if (Files.isDirectory(path)) {
//                        //todo удалить папку и её содержимое
//                    } else {
//                        writeToServer(new DeleteServerFileMessage(path));
//                    }
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
                    writeToServer(new ServerDirMessage(path));

//                    if (Files.isDirectory(path)) {
//                        writeToServer(new ServerDirMessage(path));

//                    } else {
//                        Desktop desktop = Desktop.getDesktop();
//                        if (path.toFile().exists()) {
//                            desktop.open(path.toFile());
//                        }
//                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        serverView.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                try {
                    writeToServer(new DeleteServerFileMessage(Path.of(serverPath.getText()).resolve(Path.of(serverView.getSelectionModel().getSelectedItem()))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        clientView.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                try {
                    Path path = Path.of(clientPath.getText()).resolve(Path.of(clientView.getSelectionModel().getSelectedItem()));
                    Files.deleteIfExists(path);
                    Platform.runLater(this::updateClientView);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        clientView.setOnContextMenuRequested(e -> {
            contextMenuClient.show(clientView, e.getScreenX(), e.getScreenY());
        });
        serverView.setOnContextMenuRequested(e -> {
            contextMenuServer.show(serverView, e.getScreenX(), e.getScreenY());
        });
        uploadItem.setOnAction(e -> {
            uploadFile();
        });
        createFolderItem.setOnAction(event -> {
//            serverView.getItems().add();
        });
    }

    private void initClientButtonsListeners() {
        clientFolderUpBtn.setOnAction(e -> {
            Path pathUp = Path.of(clientPath.getText()).getParent();
            if (Files.exists(pathUp)) {
                clientDir = pathUp;
                Platform.runLater(this::updateClientView);
                clientPath.setText(clientDir.toString());
            }
        });
        serverFolderUpBtn.setOnAction(e -> {
            try {
                if (Path.of(serverPath.getText()).getParent() != null) {
                    Path path = Path.of(serverPath.getText()).getParent();
                    writeToServer(new ServerDirMessage(path));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        uploadBtn.setOnMouseClicked(e -> {
            uploadFile();
//            try {
//                String fileName = clientView.getSelectionModel().getSelectedItem();
//                writeToServer(new UploadFileMessage(clientDir.resolve(fileName)));
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
        });
        downloadBtn.setOnMouseClicked(e -> {
            try {
                Path sPath = Path.of(serverPath.getText()).resolve(Path.of(serverView.getSelectionModel().getSelectedItem()));
                Path cPath = Path.of(clientPath.getText());
                writeToServer(new DownloadFileMessage(sPath, cPath));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        createFolderBtn.setOnMouseClicked(e -> {
            //todo понять как можно указать название папки (через всплывающее окно? или в режиме набора на листвью) и сделать обработку на клиенте и сервере
            createFolder();
        });
        deleteBtn.setOnMouseClicked(e -> {
                try {
                    writeToServer(new DeleteServerFileMessage(Path.of(serverPath.getText()).resolve(Path.of(serverView.getSelectionModel().getSelectedItem()))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
        });
    }

    private void uploadFile() {
        try {
            String fileName = clientView.getSelectionModel().getSelectedItem();
            writeToServer(new UploadFileMessage(clientDir.resolve(fileName), Path.of(serverPath.getText())));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void createFolder() {
        String currentDir = serverPath.getText();
        Optional<String> name = enterFolderNameDialog();
        String folderName = name.orElse("");
        if (!folderName.equals("")) {
            Path path = Path.of(currentDir).resolve(Path.of(folderName));
            writeToServer(new CreateServerDirMessage(path));
        }

    }

    private String getItem() {
        return clientView.getSelectionModel().getSelectedItem();
    }
}
