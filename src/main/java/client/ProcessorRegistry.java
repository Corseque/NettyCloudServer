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

//обработчик входящих сообщений от сервера

@Slf4j
public class ProcessorRegistry {

    private Map<CommandType, MessageProcessor> map;
    private Path clientDir;
    public ListView<String> clientView;
    public ListView<String> serverView;
    public TextField serverPath;

    public ProcessorRegistry(Path clientDir, ListView<String> clientView, ListView<String> serverView, TextField serverPath) {
        this.clientDir = clientDir;
        this.clientView = clientView;
        this.serverView = serverView;
        this.serverPath = serverPath;
        map = new HashMap<>();

        map.put(CommandType.FILES_LIST, msg -> {
            FilesListMessage message = (FilesListMessage) msg;
            Platform.runLater(() -> {
                serverView.getItems().clear();
                serverView.getItems().addAll(message.getFiles());
            });
        });
        map.put(CommandType.UPLOAD_FILE, msg -> {
            UploadFileMessage message = (UploadFileMessage) msg;

        });
//        map.put(CommandType.UPLOAD_FILES, msg -> {});
        map.put(CommandType.DOWNLOAD_FILE, msg -> {
            DownloadFileMessage message = (DownloadFileMessage) msg;
            Path path = getClientDir().resolve(message.getFileName());
            Files.write(path, message.getBytes());
            Platform.runLater(() -> updateClientView());

        });
//        map.put(CommandType.DOWNLOAD_FILES, msg -> {});
        map.put(CommandType.SERVER_DIR, msg -> {
            ServerDirMessage message = (ServerDirMessage) msg;
            serverPath.setText(message.getCurrentDir());
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
            if (((NewUserMessage) msg).isUserAlreadyExists()) {
                //todo вернуть на форму регистрации и подсветить поля, для изменения
            } else {
                //todo переход на форму входа с предзаполненым логином
            }
        });
        map.put(CommandType.LOGIN, msg -> {
            if (((LoginMessage) msg).isLoginSuccess()) {
                //todo открыть клиент под конктреным пользователем
            } else {
                //todo вывести сообщение, что введен неверный логин или пароль
            }
        });
    }

    public void process(CloudMessage msg) throws IOException {
        map.get(msg.getType()).processMessage(msg);
        log.info("Get message on client: " + msg.getType().toString());
    }

    public void setClientDir(Path clientDir) {
        this.clientDir = clientDir;
    }

    public Path getClientDir() {
        return clientDir;
    }

    private void updateClientView() {
        try {
            clientView.getItems().clear();
            Files.list(clientDir)
                    .map(p -> p.getFileName().toString())
                    .forEach(f -> clientView.getItems().add(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
