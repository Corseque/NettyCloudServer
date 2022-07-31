package client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import model.*;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class ClientNetty implements Initializable, Callback {

    //login form
    public TextField login;
    public PasswordField password;
    public Button loginBtn;
    public Button registerBtn;

    //register form
    public TextField userName;
    public TextField userSurname;
    public RadioButton userMale;
    public RadioButton userFemale;
    private ToggleGroup genderGroup = new ToggleGroup();
    public DatePicker userBirthDate;
    public TextField userPhoneNum;
    public TextField userEmail;
    public TextField userLogin;
    public PasswordField userPassword;
    public PasswordField confirmPassword;
    public Button userRegisterBtn;

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
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;

//    public ClientNetty(ProcessorRegistry processorRegistry) {
//        this.processorRegistry = processorRegistry;
//    }

    private ProcessorRegistry processorRegistry;

    // read from network
    private void readLoop() {
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

    @Override
    public void updateFilesList(List<String> list) {
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        initLoginForm();

        initClient();
    }


    private void initLoginForm() {
        //todo сюда нужно перенести открытие порта и потоков
        initLoginFormButtonsListeners();
    }

    private void initLoginFormButtonsListeners() {
        loginBtn.setOnAction(e -> {
            //временная заглушка начало
            loginBtn.getScene().getWindow().hide();
            try {
                Parent parent = FXMLLoader.load(getClass().getResource("client-view.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(parent));
                stage.show();
                initClient();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            //временная заглушка конец
            try {
                os.writeObject(new LoginMessage(login.getText(), password.getText()));
                os.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        registerBtn.setOnAction(e -> {
            registerBtn.getScene().getWindow().hide();
            try {
                Parent parent = FXMLLoader.load(getClass().getResource("register-view.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(parent));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            //todo скрыть форму логина, открыть форму регистрации
        });
    }


    private void initClient() {
        try {
            processorRegistry = new ProcessorRegistry(
                    //clientDir, clientView, serverView, serverPath
                    );
            processorRegistry.registerCallback(ClientNetty.this);
            Socket socket = new Socket("localhost", 8189);
            System.out.println("Network created...");
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

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
//                    processorRegistry.setClientDir(clientDir);
                    Platform.runLater(() -> updateClientView());
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
//                    String currentDir = serverPath.getText();
//                    String pathDown =  serverView.getSelectionModel().getSelectedItem();
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
        //client form
        clientFolderUpBtn.setOnAction(e -> {
            Path pathUp = Path.of(clientPath.getText()).getParent();
            if (Files.exists(pathUp)) {
                clientDir = pathUp;
//                processorRegistry.setClientDir(clientDir);
                Platform.runLater(() -> updateClientView());
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


    private void initRegisterForm() {
        userMale.setToggleGroup(genderGroup);
        userFemale.setToggleGroup(genderGroup);
        userMale.setSelected(true);
        initRegisterFormButtonsListeners();
        initRegisterFormFieldListeners();
    }

    private void initRegisterFormFieldListeners() {
        confirmPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(userPassword.toString())) {
                userPassword.setStyle("-fx-text-inner-color: red;");
                confirmPassword.setStyle("-fx-text-inner-color: red;");
            } else {
                userPassword.setStyle("-fx-text-inner-color: black;");
                confirmPassword.setStyle("-fx-text-inner-color: black;");
            }
        });
    }

    private void initRegisterFormButtonsListeners() {
        userRegisterBtn.setOnAction(e -> {
            if (!userName.getText().trim().isEmpty() && !userSurname.getText().trim().isEmpty()
                    && !userEmail.getText().trim().isEmpty() && !userLogin.getText().trim().isEmpty()
                    && !userPassword.getText().trim().isEmpty() && !confirmPassword.getText().trim().isEmpty()) {
                if (userPassword.getText().trim().equals(confirmPassword.getText().trim())) {
                    //отправка NewUserMessage
                    String gender = userMale.isSelected() ? "male" : "female";
                    try {
                        os.writeObject(new NewUserMessage(userName.getText(), userSurname.getText(),
                                gender, userBirthDate.toString(), userPhoneNum.getText(),
                                userEmail.getText(), userLogin.getText(), userPassword.getText()));
                        os.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    //сообщение о том, что пароли не совпадают
                    Alert passwordAlert = new Alert(Alert.AlertType.INFORMATION);
//                    passwordAlert.setTitle(titleTxt);
//                    passwordAlert.setHeaderText("Information Alert");
                    passwordAlert.setContentText("Password doesn't match.");
                    passwordAlert.show();
                }
            } else {
                //собщение о необходимости заполнить поля со *
                Alert fillInRequiredFieldsAlert = new Alert(Alert.AlertType.INFORMATION);
                StringBuilder alert = new StringBuilder("Fill in following fields:");
                alert = userName.getText().trim().isEmpty() ? alert.append("\n- Name") : alert;
                alert = userSurname.getText().trim().isEmpty() ? alert.append("\n- Surname") : alert;
                alert = userEmail.getText().trim().isEmpty() ? alert.append("\n- Email") : alert;
                alert = userLogin.getText().trim().isEmpty() ? alert.append("\n- Login") : alert;
                alert = userPassword.getText().trim().isEmpty() ? alert.append("\n- Password") : alert;
                alert = confirmPassword.getText().trim().isEmpty() ? alert.append("\n- Confirm password") : alert;
                fillInRequiredFieldsAlert.setContentText(alert.toString());
                fillInRequiredFieldsAlert.show();
            }
        });
    }

}
