package server;

import model.NewUserMessage;

import java.util.Optional;

/**
 * Сервис аутентификации
 */
public interface AuthService {

    /**
     * Запустить сервис
     */
    void start();

    /**
     * Отключить сервис
     */
    void stop();

    boolean isUserExists(NewUserMessage cloudMessage);
}
