package server;

import lombok.extern.slf4j.Slf4j;
import model.NewUserMessage;

import java.nio.file.Path;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static server.SQLConstants.*;

@Slf4j
public class MySQLAuthService extends SQLConfig {

    private static Connection connection;
    private static Statement statement;


    public void start() {
        try {
            log.info("Auth server started...");
            connect();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String connectionStr = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        connection = DriverManager.getConnection(connectionStr, DB_USER, DB_PASSWORD);
        log.info("Connected to MySQL...");
        statement = connection.createStatement();
    }

    public void stop() {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        log.info("Auth server stopped...");
    }

    private int countOfEntries(String select){
        try (ResultSet rs = statement.executeQuery(select)) {
            if (rs.next()) {
                return rs.getInt("count");
            } else {
                return 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private int countOfUsers() {
        String select = "SELECT COUNT(*) AS count from " + USER_TABLE;
        return countOfEntries(select);
    }

    private int countOfFiles() {
        String select = "SELECT COUNT(*) AS count from " + FILE_TABLE;
        return countOfEntries(select);
    }


    public void addNewUser(NewUserMessage cloudMessage) {
        String insert;
        if (countOfUsers() == 0) {
            insert = "INSERT INTO " + USER_TABLE + " (id, " + USER_NAME + ", " + USER_SURNAME + ", " + USER_BIRTHDAY
                    + ", " + USER_GENDER + ", " + USER_PHONE + ", " + USER_EMAIL + ", " + USER_LOGIN + ", " + USER_PASSWORD
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement ps = connection.prepareStatement(insert)) {
                ps.setInt(1, 1);
                ps.setString(2, cloudMessage.getUserName());
                ps.setString(3, cloudMessage.getUserSurname());
                if (cloudMessage.getUserBirthDate().equals("")) {
                    ps.setString(4, "0001-01-01");
                } else {
                    ps.setString(4, cloudMessage.getUserBirthDate());
                }
                ps.setString(5, cloudMessage.getUserGender());
                if (cloudMessage.getUserPhoneNum().equals("")) {
                    ps.setString(6, "null");
                } else {
                    ps.setString(6, cloudMessage.getUserPhoneNum());
                }
                ps.setString(7, cloudMessage.getUserEmail());
                ps.setString(8, cloudMessage.getUserLogin());
                ps.setString(9, cloudMessage.getUserPassword());
                ps.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            insert = "INSERT INTO " + USER_TABLE + " (" + USER_NAME + ", " + USER_SURNAME + ", " + USER_BIRTHDAY
                    + ", " + USER_GENDER + ", " + USER_PHONE + ", " + USER_EMAIL + ", " + USER_LOGIN + ", " + USER_PASSWORD
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement ps = connection.prepareStatement(insert)) {
                ps.setString(1, cloudMessage.getUserName());
                ps.setString(2, cloudMessage.getUserSurname());
                if (cloudMessage.getUserBirthDate().equals("")) {
                    ps.setString(3, "0001-01-01");
                } else {
                    ps.setString(3, cloudMessage.getUserBirthDate());
                }
                ps.setString(4, cloudMessage.getUserGender());
                if (cloudMessage.getUserPhoneNum().equals("")) {
                    ps.setString(5, "null");
                } else {
                    ps.setString(5, cloudMessage.getUserPhoneNum());
                }
                ps.setString(6, cloudMessage.getUserEmail());
                ps.setString(7, cloudMessage.getUserLogin());
                ps.setString(8, cloudMessage.getUserPassword());
                ps.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean isEntryExists(String select) {
        try (ResultSet rs = statement.executeQuery(select)) {
            if (rs.next()) {
                return rs.getInt("count") != 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public boolean isUserRegistered(String userLogin) {
        String select = "SELECT COUNT(*) AS count from " + USER_TABLE +
                " WHERE " +
                USER_LOGIN + " = '" + userLogin + "';";
        return isEntryExists(select);
    }


    public boolean isLoginSuccess(String userLogin, String userPassword) {
        String select = "SELECT COUNT(*) AS count from " + USER_TABLE +
                " WHERE " +
                USER_LOGIN + " = '" + userLogin + "' AND " +
                USER_PASSWORD + " = '" + userPassword + "';";
        return isEntryExists(select);
    }

    public boolean isLoginAndEmailBusy(String userLogin, String userEmail) {
        String select = "SELECT COUNT(*) AS count from " + USER_TABLE +
                " WHERE " +
                USER_LOGIN + " = '" + userLogin + "' AND " +
                USER_EMAIL + " = '" + userEmail + "';";
        return isEntryExists(select);
    }

    public boolean isLoginBusy(String userLogin, String userEmail) {
        String select = "SELECT COUNT(*) AS count from " + USER_TABLE +
                " WHERE " +
                USER_LOGIN + " = '" + userLogin + "' AND " +
                USER_EMAIL + " != '" + userEmail + "';";
        return isEntryExists(select);
    }

    public boolean isEmailBusy(String userLogin, String userEmail) {
        String select = "SELECT COUNT(*) AS count from " + USER_TABLE +
                " WHERE " +
                USER_LOGIN + " != '" + userLogin + "' AND " +
                USER_EMAIL + " = '" + userEmail + "';";
        return isEntryExists(select);
    }

    public boolean isFileExists(String fileKey){
        String select = "SELECT COUNT(*) AS count from " + FILE_TABLE +
                " WHERE " +
                FILE_KEY + " = '" + fileKey + "';";
        return isEntryExists(select);
    }

    public void addFile(String fileName, String filePath, String fileKey, String userLogin) {
        String insert;
        if (countOfFiles() == 0) {
            insert = "INSERT INTO " + FILE_TABLE + " (id, " + FILE_NAME + ", " + FILE_PATH + ", " + FILE_KEY
                    + ", " + FILE_UPLOAD_DATE + ", " + FILE_DELETE_DATE
                    + ") VALUES (?, ?, ?, ?, ?, ?);";
            try (PreparedStatement ps = connection.prepareStatement(insert)) {
                ps.setInt(1, 1);
                ps.setString(2, fileName);
                ps.setString(3, filePath);
                ps.setString(4, fileKey);
                ps.setString(5, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                ps.setString(6, "9999-01-01");
                ps.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            insert = "INSERT INTO " + FILE_TABLE + " (" + FILE_NAME + ", " + FILE_PATH + ", " + FILE_KEY
                    + ", " + FILE_UPLOAD_DATE + ", " + FILE_DELETE_DATE
                    + ") VALUES (?, ?, ?, ?, ?);";
            try (PreparedStatement ps = connection.prepareStatement(insert)) {
                ps.setString(1, fileName);
                ps.setString(2, filePath);
                ps.setString(3, fileKey);
                ps.setString(4, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                ps.setString(5, "9999-01-01");
                ps.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        int userID = selectUserID(userLogin);
        int fileID = selectFileID(fileKey);
        if (userID !=0 && fileID != 0) {
            insert = "INSERT INTO " + USER_FILES_TABLE + " (" + USER_FILES_FILE_ID + ", " + USER_FILES_USER_OWNER
                    + ", " + USER_FILES_USER_RECEIVER
                    + ") VALUES (?, ?, ?);";
            try (PreparedStatement ps = connection.prepareStatement(insert)) {
                ps.setInt(1, fileID);
                ps.setInt(2, userID);
                ps.setInt(3, 0);
                ps.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            //todo сообщение об ошибке?
        }
    }

    private int selectUserID(String userLogin) {
        String select = "SELECT id FROM " + USER_TABLE + " WHERE " + USER_LOGIN + " = '" + userLogin + "';";
        return selectEntryID(select);
    }

    private int selectFileID(String fileKey) {
        String select = "SELECT id FROM " + FILE_TABLE + " WHERE " + FILE_KEY + " = '" + fileKey + "';";
        return selectEntryID(select);
    }

    private int selectEntryID(String select) {
        try (ResultSet rs = statement.executeQuery(select)) {
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                return 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public List<String> userFiles(String userLogin, Path currentDir) {
        List<String> files = new ArrayList<>();
        int userID = selectUserID(userLogin);

        String select = "SELECT " + FILE_NAME + ", " + FILE_PATH + " FROM " + USER_FILES_TABLE
                + " LEFT JOIN " + FILE_TABLE
                + " ON " + USER_FILES_FILE_ID + " = " + "id"
                + " WHERE "
                + USER_FILES_USER_OWNER + " = '" + userID + "';";
        try (ResultSet rs = statement.executeQuery(select)) {
            while (rs.next()) {
                if (Path.of(rs.getString(FILE_PATH)).compareTo(currentDir) == 0) {
                    files.add(rs.getString(FILE_NAME));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return files;
    }


}
