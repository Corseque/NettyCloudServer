package client;

import java.nio.file.Path;

public interface CallbackToLoginForm {
    void invalidLoginOrPassword();
    void loginAccept(String rootDir);
}
