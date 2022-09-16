package client;


import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//обработчик входящих сообщений от сервера

@Slf4j
public class ProcessorRegistry {

    private CallbackToClientForm callbackClient;
    private CallbackToLoginForm callbackLogin;
    private CallbackToRegisterForm callbackRegister;

    public void registerCallback(CallbackToClientForm callback) {
        this.callbackClient = callback;
    }

    public void registerCallback(CallbackToLoginForm callback) {
        this.callbackLogin = callback;
    }

    public void registerCallback(CallbackToRegisterForm callback) {
        this.callbackRegister = callback;
    }

    private final Map<CommandType, MessageProcessor> map;


    public ProcessorRegistry() {

        map = new HashMap<>();

        map.put(CommandType.FILES_LIST, msg -> {
            FilesListMessage message = (FilesListMessage) msg;
            callbackClient.updateServerFilesList(message.getFiles());
        });
        map.put(CommandType.UPLOAD_FILE, msg -> {
            UploadFileMessage message = (UploadFileMessage) msg;
        });
//        map.put(CommandType.UPLOAD_FILES, msg -> {});
        map.put(CommandType.DOWNLOAD_FILE, msg -> {
            DownloadFileMessage message = (DownloadFileMessage) msg;
            Path path = Path.of(message.getClientPath()).resolve(message.getFileName());
            Files.write(path, message.getBytes());
            Platform.runLater(() -> callbackClient.updateClientView());

        });
        map.put(CommandType.DOWNLOAD_DIR, msg -> {
            DownloadDirMessage message = (DownloadDirMessage) msg;
//            callbackClient.downloadFolderFiles(message.getServerPath(), message.getFolderName(), message.getFiles());
            try {
                Path cPath = Path.of(message.getClientPath()).resolve(Path.of(message.getFolderName()));
                if (Files.exists(cPath)) {
                    Path path;
                    int n = 0;
                    do {
                        n++;
                        path = cPath.getParent().resolve(Path.of(cPath.getFileName().toString() + " (" + n + ")"));
                    } while (Files.exists(path));
                    cPath = path;
                }
                Path sPath = Path.of(message.getServerPath()).resolve(message.getFolderName());
                Files.createDirectory(cPath);
                Platform.runLater(() -> callbackClient.updateClientView());
                for (String s : message.getFiles()) {
                    callbackClient.downloadFolderFiles(sPath.resolve(s), cPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        map.put(CommandType.SERVER_DIR, msg -> {
            ServerDirMessage message = (ServerDirMessage) msg;
            callbackClient.setServerPath(message.getCurrentDir());
        });
//        map.put(CommandType.OPEN_SERVER_FILE, msg -> {});
//        map.put(CommandType.RENAME_SERVER_DIR, msg -> {});
//        map.put(CommandType.RENAME_SERVER_FILE, msg -> {});
//        map.put(CommandType.COPY_SERVER_DIR, msg -> {});
//        map.put(CommandType.COPY_SERVER_FILE, msg -> {});
//        map.put(CommandType.DELETE_SERVER_DIR, msg -> {});
//        map.put(CommandType.DELETE_SERVER_FILE, msg -> {});
//        map.put(CommandType.SHARE_SERVER_DIR, msg -> {});
//        map.put(CommandType.SHARE_SERVER_FILE, msg -> {});
        map.put(CommandType.NEW_USER, msg -> {
            NewUserMessage message = (NewUserMessage) msg;
                if (message.isLoginBusy() && message.isEmailBusy()) {
                    Platform.runLater(() -> callbackRegister.userExists(message.getUserLogin(), message.getUserEmail()));
                } else if (message.isLoginBusy() && !message.isEmailBusy()) {
                        Platform.runLater(() -> callbackRegister.loginBusy(message.getUserLogin()));
                } else if (!message.isLoginBusy() && message.isEmailBusy()) {
                        Platform.runLater(() -> callbackRegister.emailBusy(message.getUserEmail()));
                } else {
                Platform.runLater(() -> callbackRegister.registerSuccess());
            }
        });
        map.put(CommandType.LOGIN, msg -> {
            LoginMessage message = (LoginMessage) msg;
            if (message.isLoginSuccess()) {
                Platform.runLater(() -> callbackLogin.loginAccept(message.getRootDir()));
            } else {
                Platform.runLater(() -> callbackLogin.invalidLoginOrPassword());
            }
        });
        map.put(CommandType.ALERT_REPLACE_FILE, msg -> {
            ReplaceFileAlertMessage message = (ReplaceFileAlertMessage) msg;
            Platform.runLater(() -> callbackClient.processReplaceFileAlert(message.getAlert()));
        });
        map.put(CommandType.ALERT_CREATE_SERVER_DIR, msg -> {
            CreateServerDirAlertMessage message = (CreateServerDirAlertMessage) msg;
            Platform.runLater(() -> callbackClient.processCreateServerDirAlert(message.getAlert()));
        });
        map.put(CommandType.ALERT_DELETE_SERVER_DIR, msg -> {
            DeleteServerDirAlertMessage message = (DeleteServerDirAlertMessage) msg;
            Platform.runLater(() -> callbackClient.processDeleteServerDirAlert(message.getAlert()));
        });
    }

    public void process(CloudMessage msg) throws IOException {
        map.get(msg.getType()).processMessage(msg);
        log.info("Get message on client: " + msg.getType().toString());
    }

}
