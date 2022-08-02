package client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.NewUserMessage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class RegisterForm implements Initializable {

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

    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;
    private ProcessorRegistry processorRegistry;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
