package server;

import model.NewUserMessage;

import java.util.Optional;

/**
 * ������ ��������������
 */
public interface AuthService {

    /**
     * ��������� ������
     */
    void start();

    /**
     * ��������� ������
     */
    void stop();

    boolean isUserExists(NewUserMessage cloudMessage);
}
