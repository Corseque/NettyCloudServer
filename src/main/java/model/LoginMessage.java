package model;

import lombok.Data;

@Data
public class LoginMessage implements CloudMessage{

    private final String userLogin;
    private final String userPassword;
    private final boolean loginSuccess;

    public LoginMessage(String userLogin, String userPassword) {
        this.userLogin = userLogin;
        this.userPassword = userPassword;
        loginSuccess = true;
    }

    public LoginMessage(LoginMessage message, boolean loginSuccess) {
        this.userLogin = message.userLogin;
        this.userPassword = message.userPassword;
        this.loginSuccess = loginSuccess;
    }

    @Override
    public CommandType getType() {
        return CommandType.LOGIN;
    }
}
