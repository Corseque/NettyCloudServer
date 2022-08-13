package model;

public enum CommandType {
    FILES_LIST,                 //Command: update file list
    UPLOAD_FILE,                //Command: send file from client to server
    UPLOAD_FILES,               //Command: send files from client to server
    DOWNLOAD_FILE,              //Command: send file from server to client
    DOWNLOAD_FILES,             //Command: send files from server to client
    SERVER_DIR,                 //Command: send path of the current server directory
    SERVER_FILE,                //Command: send path of the current server directory
    OPEN_SERVER_FILE,           //Command: open directory or file from server
    RENAME_SERVER_DIR,
    RENAME_SERVER_FILE,
    COPY_SERVER_DIR,
    COPY_SERVER_FILE,
    DELETE_SERVER_DIR,
    DELETE_SERVER_FILE,
    SHARE_SERVER_DIR,
    SHARE_SERVER_FILE,
    NEW_USER,                    //Command: new user registered
    NEW_USER_REGISTERED,          //Command: new user registered confirmation
    LOGIN,
    ALERT
}

