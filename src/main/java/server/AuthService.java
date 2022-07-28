package server;

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

    /**
     * Получить никнейм по логину и паролю
     * @param login - логин пользователя
     * @param password - пароль пользователя
     * @return никнейм найден = никнейм, не найден = null
     */
    Optional<String> getNickByLoginAndPass(String login, String password);
}
