package client;


import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

//���������� �������� ��������� �� �������

@Slf4j
public class ProcessorRegistry {

    private Callback callback;

    public void registerCallback(Callback callback) {
        this.callback = callback;
    }

    private final Map<CommandType, MessageProcessor> map;
//    private Path clientDir;
//    public ListView<String> clientView;
//    public ListView<String> serverView;
//    public TextField serverPath;

    public ProcessorRegistry(
//            Path clientDir, ListView<String> clientView, ListView<String> serverView, TextField serverPath
    ) {
//        this.clientDir = clientDir;
//        this.clientView = clientView;
//        this.serverView = serverView;
//        this.serverPath = serverPath;
        map = new HashMap<>();

        map.put(CommandType.FILES_LIST, msg -> {
            FilesListMessage message = (FilesListMessage) msg;
            callback.updateFilesList(message.getFiles());
////            Platform.runLater(() -> {
////                serverView.getItems().clear();
////                serverView.getItems().addAll(message.getFiles());
////            });
        });
        map.put(CommandType.UPLOAD_FILE, msg -> {
            UploadFileMessage message = (UploadFileMessage) msg;
        });
//        map.put(CommandType.UPLOAD_FILES, msg -> {});
        map.put(CommandType.DOWNLOAD_FILE, msg -> {
            DownloadFileMessage message = (DownloadFileMessage) msg;
            Path path = callback.getClientDir().resolve(message.getFileName());
            Files.write(path, message.getBytes());
            Platform.runLater(() -> callback.updateClientView());

        });
//        map.put(CommandType.DOWNLOAD_FILES, msg -> {});
        map.put(CommandType.SERVER_DIR, msg -> {
            ServerDirMessage message = (ServerDirMessage) msg;
            callback.setServerPath(message.getCurrentDir());
////            serverPath.setText(message.getCurrentDir());
        });
//        map.put(CommandType.OPEN_SERVER_FILE, msg -> {});
//        map.put(CommandType.RENAME_SERVER_DIR, msg -> {});
//        map.put(CommandType.RENAME_SERVER_FILE, msg -> {});
//        map.put(CommandType.COPY_SERVER_DIR, msg -> {});
//        map.put(CommandType.COPY_SERVER_FILE, msg -> {});
//        map.put(CommandType.DELETE_SERVER_DIR, msg -> {});
        map.put(CommandType.DELETE_SERVER_FILE, msg -> {
            DeleteServerFileMessage message = (DeleteServerFileMessage) msg;
        });
//        map.put(CommandType.SHARE_SERVER_DIR, msg -> {});
//        map.put(CommandType.SHARE_SERVER_FILE, msg -> {});
        map.put(CommandType.NEW_USER, msg -> {
            NewUserMessage message = (NewUserMessage) msg;
            if (message.isUserAlreadyExists()) {
                //todo ������� �� ����� ����������� � ���������� ����, ��� ���������
                if (message.isLoginBusy() && message.isEmailBusy()) {

                } else {
                    if (message.isLoginBusy()) {

                    }
                    if (message.isEmailBusy()) {

                    }
                }
            } else {
                //todo ������ ����� �����������, ������� ����� ������ (?->) � �������������� �������
            }
        });
        map.put(CommandType.LOGIN, msg -> {
            if (((LoginMessage) msg).isLoginSuccess()) {
                //todo ������� ������ ��� ���������� �������������
            } else {
                //todo ������� ���������, ��� ������ �������� ����� ��� ������
            }
        });
    }

    public void process(CloudMessage msg) throws IOException {
        map.get(msg.getType()).processMessage(msg);
        log.info("Get message on client: " + msg.getType().toString());
    }

//    public void setClientDir(Path clientDir) {
//        this.clientDir = clientDir;
//    }

//    public Path getClientDir() {
//        return clientDir;
//    }

//    private void updateClientView() {
//        try {
//            clientView.getItems().clear();
//            Files.list(clientDir)
//                    .map(p -> p.getFileName().toString())
//                    .forEach(f -> clientView.getItems().add(f));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
