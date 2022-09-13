package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import model.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

//обработчик входящих сообщений от клиента

@Slf4j
public class CloudServerHandler extends SimpleChannelInboundHandler<CloudMessage> {
    private final Path rootDir;
    private final MySQLAuthService mySQL;
    private String userLogin = "";

//    private Path currentDir;

    // ChannelHandlerContext ctx;

    public CloudServerHandler(CloudServer server) {
        mySQL = server.getAuthService();
        rootDir = server.getRootDir();
    }

    public void channelActive(ChannelHandlerContext ctx) {
//        currentDir = rootDir;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        //this.ctx = ctx;
        log.info("Get message on server: " + cloudMessage.getType().toString());
        switch (cloudMessage.getType()) {
            case FILES_LIST:
                if (userLogin.equals("")) {
                    userLogin = ((FilesListMessage) cloudMessage).getPath();
                }
                sendList(ctx, Path.of(((FilesListMessage) cloudMessage).getPath()));
                break;
            case DOWNLOAD_FILE:
                processDownloadFileMessage((DownloadFileMessage) cloudMessage, ctx);
                break;
            case UPLOAD_FILE:
                processUploadFileMessage((UploadFileMessage) cloudMessage, ctx);
                break;
            case REPLACE_FILE:
                processReplaceFileMessage((ReplaceFileMessage) cloudMessage, ctx);
                break;
            case DELETE_SERVER_FILE:
                processDeleteServerFileMessage((DeleteServerFileMessage) cloudMessage, ctx);
                break;
            case CREATE_SERVER_DIR:
                processCreateServerDirMessage((CreateServerDirMessage) cloudMessage, ctx);
                break;
            case SERVER_DIR:
                processServerDirMessage(Path.of(((ServerDirMessage) cloudMessage).getCurrentDir()), ctx);
                break;
            case NEW_USER:
                processNewUserMessage((NewUserMessage) cloudMessage, ctx);
                break;
            case LOGIN:
                processLoginMessage((LoginMessage) cloudMessage, ctx);
                break;
        }
    }

    private void sendList(ChannelHandlerContext ctx, Path serverPathMask) throws IOException {
        ctx.writeAndFlush(new ServerDirMessage(serverPathMask));
        ctx.writeAndFlush(new FilesListMessage(mySQL.userFiles(userLogin, serverPathMask), serverPathMask));
    }

    private void processDownloadFileMessage(DownloadFileMessage cloudMessage, ChannelHandlerContext ctx) throws IOException {
        String fileName = cloudMessage.getFileName();
        String fileKey = mySQL.findFileKey(userLogin, Path.of(cloudMessage.getServerPath()).resolve(Path.of(cloudMessage.getFileName())));
        if (fileKey != null) {
            Path path = rootDir.resolve(fileKey);
            ctx.writeAndFlush(new DownloadFileMessage(path, fileName));
        } else {
            //todo сообщение пользователю, что файл не найден
            //todo сделать корректный обработчик на стороне клиента
        }
    }

    private void processUploadFileMessage(UploadFileMessage cloudMessage, ChannelHandlerContext ctx) throws IOException {
        String fileKey = DigestUtils.md5Hex(userLogin + cloudMessage.getServerPath() + cloudMessage.getFileName());
        if (!mySQL.isFileExists(fileKey)) {
            Files.write(rootDir.resolve(fileKey), cloudMessage.getBytes());
            mySQL.addFile(cloudMessage.getFileName(), cloudMessage.getServerPath(), fileKey, userLogin, true);
            sendList(ctx, Path.of(cloudMessage.getServerPath()));
        } else {
            ctx.writeAndFlush(new ReplaceFileAlertMessage("The file already exists. Do you want to replace the file?"));
        }
    }

    private void processReplaceFileMessage(ReplaceFileMessage cloudMessage, ChannelHandlerContext ctx) {
        String fileKey = DigestUtils.md5Hex(userLogin + cloudMessage.getServerPath() + cloudMessage.getFileName());
        String replacedFileKey = mySQL.markFileReplaced(fileKey);
        Path path = rootDir.resolve(fileKey);
        try {
            Files.move(path, path.resolveSibling(replacedFileKey), StandardCopyOption.REPLACE_EXISTING);
            Files.write(path, cloudMessage.getBytes());
            mySQL.addFile(cloudMessage.getFileName(), cloudMessage.getServerPath(), fileKey, userLogin, true);
            sendList(ctx, Path.of(cloudMessage.getServerPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processDeleteServerFileMessage(DeleteServerFileMessage cloudMessage, ChannelHandlerContext ctx) throws IOException {
        String fileKey = mySQL.findFileKey(userLogin, Path.of(cloudMessage.getServerPath()).resolve(Path.of(cloudMessage.getFileName())));
        if (fileKey != null) {
            String deletedFileKey = mySQL.markFileDeleted(fileKey);
            Path path = rootDir.resolve(fileKey);
            if (!mySQL.isFolder(fileKey)) {
                Files.move(path, path.resolveSibling(deletedFileKey), StandardCopyOption.REPLACE_EXISTING);
            } else {
                //todo написать обработку удаления директории
                // написать проверку наличия файлов в директории, если есть, то спросить пользователя: удалить папку с файлами?
            }
            sendList(ctx, Path.of(cloudMessage.getServerPath()));
        }
    }

    private void processCreateServerDirMessage(CreateServerDirMessage cloudMessage, ChannelHandlerContext ctx) throws IOException {
        String folderKey = DigestUtils.md5Hex(userLogin + cloudMessage.getServerPath() + cloudMessage.getFolderName());
        if (!mySQL.isFileExists(folderKey)) {
            mySQL.addFile(cloudMessage.getFolderName(), cloudMessage.getServerPath(), folderKey, userLogin, false);
            sendList(ctx, Path.of(cloudMessage.getServerPath()));
        } else {
            ctx.writeAndFlush(new CreateServerDirAlertMessage("The folder with this name already exists. Specify another folder name?"));
        }
    }

    private void processServerDirMessage(Path serverPathMask, ChannelHandlerContext ctx) throws IOException {
        //todo нужно написать отправку содержимого папки клиенту, если это папка
        String folderKey = mySQL.findFileKey(userLogin, serverPathMask);
        if (folderKey != null || serverPathMask.compareTo(Path.of(userLogin)) == 0) {
            if (mySQL.isFolder(folderKey) || serverPathMask.compareTo(Path.of(userLogin)) == 0) {
                sendList(ctx, serverPathMask);
            } else {
                //todo 3.2. вопрос клиенту: вы хотите скачать (?и открыть?) файл?
            }
        }
    }

    private void processLoginMessage(LoginMessage cloudMessage, ChannelHandlerContext ctx) {
        if (mySQL.isUserRegistered(cloudMessage.getUserLogin())) {
            if (mySQL.isLoginSuccess(cloudMessage.getUserLogin(), cloudMessage.getUserPassword())) {
                cloudMessage.setLoginSuccess(true);
                userLogin = cloudMessage.getUserLogin();
                cloudMessage.setRootDir(userLogin);
            } else {
                cloudMessage.setLoginSuccess(false);
            }
        }
        ctx.writeAndFlush(new LoginMessage(cloudMessage));
    }

    private void processNewUserMessage(NewUserMessage cloudMessage, ChannelHandlerContext ctx) {
        if (mySQL.isLoginAndEmailBusy(cloudMessage.getUserLogin(), cloudMessage.getUserEmail())) {
            cloudMessage.setLoginBusy(true);
            cloudMessage.setEmailBusy(true);
        } else if (mySQL.isLoginBusy(cloudMessage.getUserLogin(), cloudMessage.getUserEmail())) {
            cloudMessage.setLoginBusy(true);
        } else if (mySQL.isEmailBusy(cloudMessage.getUserLogin(), cloudMessage.getUserEmail())) {
            cloudMessage.setEmailBusy(true);
        } else {
            mySQL.addNewUser(cloudMessage);
        }
        ctx.writeAndFlush(new NewUserMessage(cloudMessage));
    }

}
