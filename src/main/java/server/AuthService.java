package server;

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

    /**
     * �������� ������� �� ������ � ������
     * @param login - ����� ������������
     * @param password - ������ ������������
     * @return ������� ������ = �������, �� ������ = null
     */
    Optional<String> getNickByLoginAndPass(String login, String password);
}
