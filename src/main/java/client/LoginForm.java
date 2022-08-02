package client;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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

    //login form
    public TextField login;
    public PasswordField password;
    public Button loginBtn;
    public Button registerBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        processorRegistry = new ProcessorRegistry();
        processorRegistry.registerCallback(LoginForm.this);
        initLoginFormButtonsListeners();
    }

    private void initLoginFormButtonsListeners() {
        loginBtn.setOnAction(e -> {
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
        });
    }

    @Override
    public void loginAccept(Path rootDir) {
        loginBtn.getScene().getWindow().hide();
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("client-view.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(parent));
            stage.show();
            os.writeObject(new FilesListMessage(rootDir));
            os.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void invalidLoginOrPassword() {
        Alert invalidLoginOrPasswordAlert = new Alert(Alert.AlertType.INFORMATION);
        invalidLoginOrPasswordAlert.setContentText("Invalid login or password ");
        invalidLoginOrPasswordAlert.show();
    }
}
