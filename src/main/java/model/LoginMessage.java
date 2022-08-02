package model;

import lombok.Data;

import java.nio.file.Path;

@Data
public class LoginMessage implements CloudMessage{

    private final String userLogin;
    private final String userPassword;
    private final boolean loginSuccess;
    private final String rootDir;

    public LoginMessage(String userLogin, String userPassword) {
        this.userLogin = userLogin;
        this.userPassword = userPassword;
        loginSuccess = false;
        rootDir = "";
    }

    public LoginMessage(LoginMessage message, boolean loginSuccess, String rootDir) {
        this.userLogin = message.userLogin;
        this.userPassword = message.userPassword;
        this.loginSuccess = loginSuccess;
        this.rootDir = rootDir;
    }

    @Override
    public CommandType getType() {
        return CommandType.LOGIN;
    }
}
