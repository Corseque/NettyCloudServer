package client;

import java.nio.file.Path;
import java.util.List;

public interface Callback {
    void updateFilesList(List<String> list); // FILES_LIST
    void updateClientView();  // DOWNLOAD_FILE
    void setServerPath(String svrPath); // SERVER_DIR
    Path getClientDir();

}
