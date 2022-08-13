package server;

public class SQLConstants {
    public static final String USER_TABLE = "users";
    public static final String USER_NAME = "user_name";
    public static final String USER_SURNAME = "user_surname";
    public static final String USER_BIRTHDAY = "birth_date";
    public static final String USER_GENDER = "gender";
    public static final String USER_PHONE = "phone_num";
    public static final String USER_EMAIL = "email";
    public static final String USER_LOGIN = "login";
    public static final String USER_PASSWORD = "password";

    public static final String FILE_TABLE = "files";
    public static final String FILE_NAME = "file_name";
    public static final String FILE_PATH = "file_path";
    public static final String FILE_KEY = "file_key";
    public static final String FILE_UPLOAD_DATE = "upload_date";
    public static final String FILE_DELETE_DATE = "delete_date";

    public static final String USER_FILES_TABLE = "user_files";
    public static final String USER_FILES_FILE_ID = "id_file";
    public static final String USER_FILES_USER_OWNER= "id_user_owner";
    public static final String USER_FILES_USER_RECEIVER = "id_user_receiver";
}
