package client;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import model.FilesListMessage;
import model.LoginMessage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

@Slf4j
public class LoginForm extends Network implements Initializable, CallbackToLoginForm {

    public TextField login;
    public PasswordField password;
    public Button loginBtn;
    public Button registerBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        processorRegistry = new ProcessorRegistry();
        processorRegistry.registerCallback(LoginForm.this);
        initLoginFormButtonsListeners();
        initLoginFormKeyListeners();
    }

    private void tryToLogin() {
        if (!login.getText().isEmpty() && !password.getText().isEmpty()) {
            try {
                new Network();
                Thread readThread = new Thread(this::readLoop);
                readThread.setDaemon(true);
                readThread.start();

                os.writeObject(new LoginMessage(login.getText(), password.getText()));
                os.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (login.getText().isEmpty()) {
            //todo alert
        } else if (password.getText().isEmpty()) {
            //todo alert
        } else {
            //todo alert
        }
    }

    private void initLoginFormKeyListeners() {
        password.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                tryToLogin();
            }
        });
    }

    private void initLoginFormButtonsListeners() {
        loginBtn.setOnAction(e -> tryToLogin());
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
        });
    }

    @Override
    public void loginAccept(String rootDir) {
        loginBtn.getScene().getWindow().hide();
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("client-view.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(parent));
            stage.setTitle("Cloud storage");
            stage.show();
            os.writeObject(new FilesListMessage(Path.of(rootDir)));
            os.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void invalidLoginOrPassword() {
        showAlert("Invalid login or password.");
//        Alert invalidLoginOrPasswordAlert = new Alert(Alert.AlertType.NONE);
//        invalidLoginOrPasswordAlert.setTitle("Invalid data");
//        invalidLoginOrPasswordAlert.setContentText("Invalid login or password.");
//        invalidLoginOrPasswordAlert.getDialogPane().getButtonTypes().add(ButtonType.OK);
//        invalidLoginOrPasswordAlert.show();
    }
}
