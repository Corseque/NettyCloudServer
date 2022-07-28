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
    private final boolean userAlreadyExists;

    public NewUserMessage(String userName, String userSurname, String userGender, String userBirthDate, String userPhoneNum, String userEmail, String userLogin, String userPassword) {
        this.userName = userName;
        this.userSurname = userSurname;
        this.userGender = userGender;
        this.userBirthDate = userBirthDate;
        this.userPhoneNum = userPhoneNum;
        this.userEmail = userEmail;
        this.userLogin = userLogin;
        this.userPassword = userPassword;
        userAlreadyExists = false;
    }

    public NewUserMessage(NewUserMessage message, boolean userAlreadyExists) {
        this.userName = message.userName;
        this.userSurname = message.userSurname;
        this.userGender = message.userGender;
        this.userBirthDate = message.userBirthDate;
        this.userPhoneNum = message.userPhoneNum;
        this.userEmail = message.userEmail;
        this.userLogin = message.userLogin;
        this.userPassword = message.userPassword;
        this.userAlreadyExists = true;
    }

    @Override
    public CommandType getType() {
        return CommandType.NEW_USER;
    }
}
