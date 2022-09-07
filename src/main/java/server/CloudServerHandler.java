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

    private Path currentDir;

    // ChannelHandlerContext ctx;

    public CloudServerHandler(CloudServer server) {
        mySQL = server.getAuthService();
        rootDir = server.getRootDir();
    }

    public void channelActive(ChannelHandlerContext ctx) {
        currentDir = rootDir;
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

    private Path revealPathMask(Path mask) {
        Path dir;
        int nameCount = mask.getNameCount();
        if (nameCount > 1) {
            dir = mask.subpath(1, nameCount);
            return rootDir.resolve(dir);
        } else {
            return rootDir;
        }
    }

    private void sendList(ChannelHandlerContext ctx, Path dirMask) throws IOException {
        ctx.writeAndFlush(new ServerDirMessage(dirMask));
        currentDir = revealPathMask(dirMask);
        ctx.writeAndFlush(new FilesListMessage(mySQL.userFiles(userLogin, currentDir), dirMask));
    }

    private void processUploadFileMessage(UploadFileMessage cloudMessage, ChannelHandlerContext ctx) throws IOException {
        String fileKey = DigestUtils.md5Hex(userLogin + cloudMessage.getFileName());
        if (!mySQL.isFileExists(fileKey)) {
            Files.write(currentDir.resolve(fileKey), cloudMessage.getBytes());
            ctx.writeAndFlush(new FilesListMessage(currentDir));
            mySQL.addFile(cloudMessage.getFileName(), currentDir.toString(), fileKey, userLogin);
            sendList(ctx, Path.of(cloudMessage.getPath()));
        } else {
            ctx.writeAndFlush(new AlertMessage("The file already exists. Do you want to replace the file?"));
        }
    }


    private void processReplaceFileMessage(ReplaceFileMessage cloudMessage, ChannelHandlerContext ctx) {
        String fileKey = DigestUtils.md5Hex(userLogin + cloudMessage.getFileName());
        String replacedFileKey = mySQL.markFileReplaced(fileKey);
        Path path = currentDir.resolve(fileKey);
        try {
            Files.move(path, path.resolveSibling(replacedFileKey), StandardCopyOption.REPLACE_EXISTING);
            Files.write(path, cloudMessage.getBytes());
            ctx.writeAndFlush(new FilesListMessage(currentDir));
            mySQL.addFile(cloudMessage.getFileName(), currentDir.toString(), fileKey, userLogin);
            sendList(ctx, Path.of(cloudMessage.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processDownloadFileMessage(DownloadFileMessage cloudMessage, ChannelHandlerContext ctx) throws IOException {
        String fileName = cloudMessage.getFileName();
        Path path = currentDir.resolve(mySQL.findFileKey(userLogin, fileName));
        ctx.writeAndFlush(new DownloadFileMessage(path, fileName));
    }

    private void processServerDirMessage(Path dirMask, ChannelHandlerContext ctx) throws IOException {
        currentDir = revealPathMask(dirMask);
        if (Files.isDirectory(currentDir)) {
            if (rootDir.compareTo(currentDir) == 0 || rootDir.compareTo(currentDir) > 0) {
                currentDir = rootDir;
                sendList(ctx, dirMask);
            } else {
                sendList(ctx, dirMask);
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
