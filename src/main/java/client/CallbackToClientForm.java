package client;

import java.nio.file.Path;
import java.util.List;

public interface CallbackToClientForm {
    void updateServerFilesList(List<String> list); // FILES_LIST
    void updateClientView();  // DOWNLOAD_FILE
    void setServerPath(String svrPath); // SERVER_DIR
    Path getClientDir();
    void processAlert(String alert);

}
