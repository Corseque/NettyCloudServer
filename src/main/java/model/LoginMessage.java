package model;

import lombok.Data;

@Data
public class LoginMessage implements CloudMessage{

    private final String userLogin;
    private final String userPassword;
    private boolean loginSuccess;
    private String rootDir;

    public LoginMessage(String userLogin, String userPassword) {
        this.userLogin = userLogin;
        this.userPassword = userPassword;
        loginSuccess = false;
        rootDir = "something went wrong...";
    }

    public LoginMessage(LoginMessage message) {
        this.userLogin = message.userLogin;
        this.userPassword = message.userPassword;
        this.loginSuccess = message.isLoginSuccess();
        this.rootDir = message.getRootDir();
    }


    @Override
    public CommandType getType() {
        return CommandType.LOGIN;
    }
}
