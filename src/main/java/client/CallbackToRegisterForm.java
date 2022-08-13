package client;

public interface CallbackToRegisterForm {
    void registerSuccess();
    void userExists(String login, String email);
    void loginBusy(String login);
    void emailBusy(String email);
}
