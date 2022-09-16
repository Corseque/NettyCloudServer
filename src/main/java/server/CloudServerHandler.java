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
import java.util.Iterator;


//���������� �������� ��������� �� �������

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
            case DELETE_SERVER_DIR:
                processDeleteServerDirMessage((DeleteServerDirMessage) cloudMessage, ctx);
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
        Path cloudMessagePath = Path.of(cloudMessage.getServerPath()).resolve(Path.of(cloudMessage.getFileName()));
        String fileKey = mySQL.findFileKey(userLogin, cloudMessagePath);
        if (fileKey != null) {
            if (!mySQL.isFolder(fileKey)) {
                ctx.writeAndFlush(new DownloadFileMessage(rootDir.resolve(fileKey), fileName, Path.of(cloudMessage.getClientPath())));
            } else {
                ctx.writeAndFlush(new DownloadDirMessage(cloudMessagePath, mySQL.userFiles(userLogin, cloudMessagePath), Path.of(cloudMessage.getClientPath())));
            }
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
        Path cloudMessagePath = Path.of(cloudMessage.getServerPath()).resolve(Path.of(cloudMessage.getFileName()));
        String fileKey = mySQL.findFileKey(userLogin, cloudMessagePath);
        if (fileKey != null) {
            if (!mySQL.isFolder(fileKey)) {
                Path rootPath = rootDir.resolve(fileKey);
                String deletedFileKey = mySQL.markFileDeleted(fileKey);
                Files.move(rootPath, rootPath.resolveSibling(deletedFileKey), StandardCopyOption.REPLACE_EXISTING);
            } else {
                ctx.writeAndFlush(new DeleteServerDirAlertMessage("A you sure you want to delete folder: " + cloudMessage.getFileName() + "?"));
            }
            sendList(ctx, Path.of(cloudMessage.getServerPath()));
        }
    }

    private void processDeleteServerDirMessage(DeleteServerDirMessage cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path cloudMessagePath = Path.of(cloudMessage.getServerPath()).resolve(Path.of(cloudMessage.getFolderName()));
        String fileKey = mySQL.findFileKey(userLogin, cloudMessagePath);
        mySQL.markFileDeleted(fileKey);
        for (String s : mySQL.userFileKeys(userLogin, cloudMessagePath)) {
            fileKey = s;
            String deletedFileKey = mySQL.markFileDeleted(fileKey);
            Path rootPath = rootDir.resolve(fileKey);
            Files.move(rootPath, rootPath.resolveSibling(deletedFileKey), StandardCopyOption.REPLACE_EXISTING);
        }
        sendList(ctx, Path.of(cloudMessage.getServerPath()));
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
        String folderKey = mySQL.findFileKey(userLogin, serverPathMask);
        if (folderKey != null || serverPathMask.compareTo(Path.of(userLogin)) == 0) {
            if (mySQL.isFolder(folderKey) || serverPathMask.compareTo(Path.of(userLogin)) == 0) {
                sendList(ctx, serverPathMask);
            } else {
                //todo ������ �������: �� ������ ������� (?� �������?) ����?
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
