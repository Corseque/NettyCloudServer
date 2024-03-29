package model;


import lombok.Data;

@Data
public class NewUserMessage implements CloudMessage{

    private final String userName;
    private final String userSurname;
    private final String userGender;
    private final String userBirthDate;
    private final String userPhoneNum;
    private final String userEmail;
    private final String userLogin;
    private final String userPassword;
    private boolean loginBusy;
    private boolean emailBusy;

    public NewUserMessage(String userName, String userSurname, String userGender, String userBirthDate, String userPhoneNum, String userEmail, String userLogin, String userPassword) {
        this.userName = userName;
        this.userSurname = userSurname;
        this.userGender = userGender;
        this.userBirthDate = userBirthDate;
        this.userPhoneNum = userPhoneNum;
        this.userEmail = userEmail;
        this.userLogin = userLogin;
        this.userPassword = userPassword;
        loginBusy = false;
        emailBusy = false;
    }


    public NewUserMessage(NewUserMessage message) {
        this.userName = message.userName;
        this.userSurname = message.userSurname;
        this.userGender = message.userGender;
        this.userBirthDate = message.userBirthDate;
        this.userPhoneNum = message.userPhoneNum;
        this.userEmail = message.userEmail;
        this.userLogin = message.userLogin;
        this.userPassword = message.userPassword;
        this.loginBusy = message.loginBusy;
        this.emailBusy = message.emailBusy;
    }

    @Override
    public CommandType getType() {
        return CommandType.NEW_USER;
    }
}
