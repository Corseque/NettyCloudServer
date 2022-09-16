package client;

import java.nio.file.Path;
import java.util.List;

public interface CallbackToClientForm {
    void updateServerFilesList(List<String> list); // FILES_LIST
    void updateClientView();  // DOWNLOAD_FILE
    void setServerPath(String svrPath); // SERVER_DIR
    void processReplaceFileAlert(String alert);
    void processCreateServerDirAlert(String alert);
    void processDeleteServerDirAlert(String alert);
//    void downloadFolderFiles(String sPath, String folderName, List<String> files);
    void downloadFolderFiles(Path sPath, Path cPath);
}
