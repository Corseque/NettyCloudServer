package client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.LoginMessage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class LoginForm implements Initializable {

    //login form
    public TextField login;
    public PasswordField password;
    public Button loginBtn;
    public Button registerBtn;

    private Path clientDir;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;
    private ProcessorRegistry processorRegistry;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initLoginForm();
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
}
