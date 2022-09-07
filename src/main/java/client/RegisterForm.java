package client;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.NewUserMessage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class RegisterForm extends Network implements Initializable, CallbackToRegisterForm {

    public TextField userName;
    public TextField userSurname;
    public RadioButton userMale;
    public RadioButton userFemale;
    private final ToggleGroup genderGroup = new ToggleGroup();
    public DatePicker userBirthDate;
    public TextField userPhoneNum;
    public TextField userEmail;
    public TextField userLogin;
    public PasswordField userPassword;
    public PasswordField confirmPassword;
    public Button userRegisterBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        processorRegistry.registerCallback(RegisterForm.this);

        userMale.setToggleGroup(genderGroup);
        userFemale.setToggleGroup(genderGroup);
        userMale.setSelected(true);
        initRegisterFormButtonsListeners();
        initRegisterFormFieldListeners();

        Thread readThread = new Thread(this::readLoop);
        readThread.setDaemon(true);
        readThread.start();
    }

    private void initRegisterFormFieldListeners() {
        confirmPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(userPassword.getText().trim())) {
                userPassword.setStyle("-fx-text-inner-color: red;");
                confirmPassword.setStyle("-fx-text-inner-color: red;");
            } else {
                userPassword.setStyle("-fx-text-inner-color: black;");
                confirmPassword.setStyle("-fx-text-inner-color: black;");
            }
        });
        userEmail.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue)) {
                userEmail.setStyle("-fx-text-inner-color: red;");
                userEmail.setStyle("-fx-text-inner-color: red;");
            } else {
                userEmail.setStyle("-fx-text-inner-color: black;");
                userEmail.setStyle("-fx-text-inner-color: black;");
            }
        });
        userLogin.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue)) {
                userLogin.setStyle("-fx-text-inner-color: red;");
                userLogin.setStyle("-fx-text-inner-color: red;");
            } else {
                userLogin.setStyle("-fx-text-inner-color: black;");
                userLogin.setStyle("-fx-text-inner-color: black;");
            }
        });
    }

    private void initUserEmailChanges() {
        userEmail.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue)) {
                userEmail.setStyle("-fx-text-inner-color: red;");
                userEmail.setStyle("-fx-text-inner-color: red;");
            } else {
                userEmail.setStyle("-fx-text-inner-color: black;");
                userEmail.setStyle("-fx-text-inner-color: black;");
            }
        });
    }
    private void initUserLoginChanges() {
        userLogin.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue)) {
                userLogin.setStyle("-fx-text-inner-color: red;");
                userLogin.setStyle("-fx-text-inner-color: red;");
            } else {
                userLogin.setStyle("-fx-text-inner-color: black;");
                userLogin.setStyle("-fx-text-inner-color: black;");
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
                    String userBirthday = userBirthDate.getValue() == null ? "0001-01-01" : userBirthDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    writeToServer(new NewUserMessage(userName.getText(), userSurname.getText(),
                            gender, userBirthday, userPhoneNum.getText(),
                            userEmail.getText(), userLogin.getText(), userPassword.getText()));
                } else {
                    //сообщение о том, что пароли не совпадают
                    showInfoAlert("Password doesn't match.");
                }
            } else {
                //собщение о необходимости заполнить поля со *
                StringBuilder alert = new StringBuilder("Fill in following fields:");
                alert = userName.getText().trim().isEmpty() ? alert.append("\n- Name") : alert;
                alert = userSurname.getText().trim().isEmpty() ? alert.append("\n- Surname") : alert;
                alert = userEmail.getText().trim().isEmpty() ? alert.append("\n- Email") : alert;
                alert = userLogin.getText().trim().isEmpty() ? alert.append("\n- Login") : alert;
                alert = userPassword.getText().trim().isEmpty() ? alert.append("\n- Password") : alert;
                alert = confirmPassword.getText().trim().isEmpty() ? alert.append("\n- Confirm password") : alert;
                showInfoAlert(alert.toString());
            }
        });
    }

    @Override
    public void registerSuccess() {
        Optional<ButtonType> result = showInfoAlert("New user registered successfully.");
        if (result.get() == ButtonType.OK) {
            userRegisterBtn.getScene().getWindow().hide();
            try {
                Parent parent = FXMLLoader.load(getClass().getResource("login-view.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Cloud storage");
                stage.setScene(new Scene(parent));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void userExists(String login, String email) {
        Optional<ButtonType> result = showInfoAlert("User with login = " + login + " and email = " + email + " already exists.");
        if (result.get() == ButtonType.OK) {
            userLogin.setStyle("-fx-text-inner-color: red;");
            userEmail.setStyle("-fx-text-inner-color: red;");
        }
    }

    @Override
    public void loginBusy(String login) {
        Optional<ButtonType> result = showInfoAlert("User with login = " + login + " already exists.");
        if (result.get() == ButtonType.OK) {
            userLogin.setStyle("-fx-text-inner-color: red;");
        }
    }

    @Override
    public void emailBusy(String email) {
        Optional<ButtonType> result = showInfoAlert("User with email = " + email + " already exists.");
        if (result.get() == ButtonType.OK) {
            userEmail.setStyle("-fx-text-inner-color: red;");
        }
    }


//    private Optional<ButtonType> showAlert(String message) {
//        Alert alert = new Alert(Alert.AlertType.NONE);
//        alert.setTitle("Register information");
//        alert.setContentText(message);
//        alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
//        return alert.showAndWait();
//    }

}
