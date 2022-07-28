package server;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@Slf4j
public class MySQLAuthService implements AuthService {

    private static Connection connection;
    private static Statement statement;

    @Override
    public void start() {
        try {
            connect();
            createDatabase();
            createTables();
            //insertUsers();
            //dropTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws SQLException {
        connection =  DriverManager.getConnection("jdbc:mysql://localhost:3306/","root", "root");
        log.info("Connected to MySQL...");
        statement = connection.createStatement();

    }

    private void createDatabase() throws SQLException {
        statement.executeUpdate("CREATE DATABASE IF NOT EXISTS `cloud_storage`;");
        log.info("Database created ...");
    }

    private void createTables() throws SQLException {
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS `cloud_storage`.`users` (\n" +
                "  `id` INT(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT,\n" +
                "  `user_name` VARCHAR(45) NOT NULL,\n" +
                "  `user_surname` VARCHAR(45) NOT NULL,\n" +
                "  `birth_date` DATE DEFAULT NULL,\n" +
                "  `gender` VARCHAR(45) NOT NULL,\n" +
                "  `phone_num` VARCHAR(45) NULL,\n" +
                "  `email` VARCHAR(45) NOT NULL,\n" +
                "  `login` VARCHAR(45) NOT NULL,\n" +
                "  `password` VARCHAR(45) NOT NULL,\n" +
                "   PRIMARY KEY (`id`),\n" +
                "   UNIQUE KEY `id_UNIQUE` (`id`),\n" +
                "   UNIQUE KEY `idx_login` (`login`),\n" +
                "   UNIQUE KEY `idx_email` (`email`),\n" +
                "   UNIQUE KEY `phone_num_UNIQUE` (`phone_num`)\n" +
                "   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;");

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS `cloud_storage`.`files` (\n" +
                "  `id` INT(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT,\n" +
                "  `file_name` VARCHAR(45) NOT NULL,\n" +
                "  `file_path` VARCHAR(255) NOT NULL,\n" +
                "  `upload_date` DATETIME NOT NULL,\n" +
                "  `delete_date` DATETIME NOT NULL,\n" +
                "   PRIMARY KEY (`id`),\n" +
                "   UNIQUE KEY `id_UNIQUE` (`id`),\n" +
                "   UNIQUE KEY `idx_path_to_file` (`file_name`,`file_path`)\n" +
                "   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;");

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS `cloud_storage`.`user_files` (\n" +
                "  `id_user_owner` INT(10) UNSIGNED ZEROFILL NOT NULL,\n" +
                "  `id_user_receiver` INT(10) UNSIGNED ZEROFILL NOT NULL,\n" +
                "  `id_file` INT(10) UNSIGNED ZEROFILL NOT NULL,\n" +
                "   KEY `fk_user_files_file_idx` (`id_file`),\n" +
                "   KEY `fk_user_files_user_owner_idx` (`id_user_owner`),\n" +
                "   KEY `fk_user_files_user_receiver_idx` (`id_user_receiver`),\n" +
                "   CONSTRAINT `fk_user_files_file` FOREIGN KEY (`id_file`) REFERENCES `files` (`id`),\n" +
                "   CONSTRAINT `fk_user_files_user_owner` FOREIGN KEY (`id_user_owner`) REFERENCES `users` (`id`),\n" +
                "   CONSTRAINT `fk_user_files_user_receiver` FOREIGN KEY (`id_user_receiver`) REFERENCES `users` (`id`)\n" +
                "   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;");
    }

    @Override
    public void stop() {

    }

    @Override
    public Optional<String> getNickByLoginAndPass(String login, String password) {
        return Optional.empty();
    }
}
