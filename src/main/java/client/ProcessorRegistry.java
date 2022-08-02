package client;


import javafx.application.Platform;
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

    private CallbackToClient callbackClient;
    private CallbackToLoginForm callbackLogin;

    public void registerCallback(CallbackToClient callback) {
        this.callbackClient = callback;
    }

    public void registerCallback(CallbackToLoginForm callback) {
        this.callbackLogin = callback;
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
            Path path = callbackClient.getClientDir().resolve(message.getFileName());
            Files.write(path, message.getBytes());
            Platform.runLater(() -> callbackClient.updateClientView());

        });
//        map.put(CommandType.DOWNLOAD_FILES, msg -> {});
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
        map.put(CommandType.DELETE_SERVER_FILE, msg -> {
            DeleteServerFileMessage message = (DeleteServerFileMessage) msg;
        });
//        map.put(CommandType.SHARE_SERVER_DIR, msg -> {});
//        map.put(CommandType.SHARE_SERVER_FILE, msg -> {});
        map.put(CommandType.NEW_USER, msg -> {
            NewUserMessage message = (NewUserMessage) msg;
            if (message.isUserAlreadyExists()) {
                //todo вернуть на форму регистрации и подсветить поля, для изменения
                if (message.isLoginBusy() && message.isEmailBusy()) {

                } else {
                    if (message.isLoginBusy()) {

                    }
                    if (message.isEmailBusy()) {

                    }
                }
            } else {
                //todo скрыть форму регистрации, открыть форму логина (?->) с предзаполненым логином
            }
        });
        map.put(CommandType.LOGIN, msg -> {
//            if (((LoginMessage) msg).isLoginSuccess()) {
            LoginMessage message = (LoginMessage) msg;
            Platform.runLater(() -> callbackLogin.loginAccept(Path.of(message.getRootDir())));

                //todo отправить пользовательскую дирректорию
//            } else {
//                callbackLogin.invalidLoginOrPassword();
//            }
        });
    }

    public void process(CloudMessage msg) throws IOException {
        map.get(msg.getType()).processMessage(msg);
        log.info("Get message on client: " + msg.getType().toString());
    }

}
