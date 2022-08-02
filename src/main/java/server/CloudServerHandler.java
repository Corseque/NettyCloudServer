package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

//обработчик входящих сообщений от клиента

@Slf4j
public class CloudServerHandler extends SimpleChannelInboundHandler<CloudMessage> {
    private final Path rootDir = Path.of("C:/Users/Corse/IdeaProjects/NettyCloudServer/data");
    private Path currentDir;
    private final CloudServer server;
   // ChannelHandlerContext ctx;

    public CloudServerHandler(CloudServer server) {
        this.server = server;
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
                sendList(ctx, currentDir);
                break;
            case DOWNLOAD_FILE:
                processDownloadFileMessage((DownloadFileMessage) cloudMessage, ctx);
                break;
            case UPLOAD_FILE:
                processUploadFileMessage((UploadFileMessage) cloudMessage);
                sendList(ctx, currentDir);
                break;
            case SERVER_DIR:
                processServerDirMessage((ServerDirMessage) cloudMessage, ctx);
                break;
            case NEW_USER:
                processNewUserMessage((NewUserMessage) cloudMessage, ctx);
                break;
            case LOGIN:
                processLoginMessage((LoginMessage) cloudMessage, ctx);
                break;
        }
    }

    private void processLoginMessage(LoginMessage cloudMessage, ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new LoginMessage(cloudMessage, true, rootDir.toString()));

        //todo написать обработчик через проверку пользователя в БД
    }

    private void processNewUserMessage(NewUserMessage cloudMessage, ChannelHandlerContext ctx) {
        if (server.getAuthService().isUserExists(cloudMessage)) {
            ctx.writeAndFlush(new NewUserMessage(cloudMessage, true, true, true));
        } else if (server.getAuthService().isLoginBusy(cloudMessage)) {
            ctx.writeAndFlush(new NewUserMessage(cloudMessage, true, true, false));
        } else if (server.getAuthService().isEmailBusy(cloudMessage)) {
            ctx.writeAndFlush(new NewUserMessage(cloudMessage, true, false, true));
        } else {
            ctx.writeAndFlush(new NewUserMessage(cloudMessage, false, false, false));
        }
    }

    private void sendList(ChannelHandlerContext ctx, Path dir) throws IOException {
        ctx.writeAndFlush(new ServerDirMessage(dir));
        ctx.writeAndFlush(new FilesListMessage(dir));
    }

    private void processUploadFileMessage(UploadFileMessage cloudMessage) throws IOException {
        Files.write(currentDir.resolve(cloudMessage.getFileName()), cloudMessage.getBytes());
    }

    private void processDownloadFileMessage(DownloadFileMessage cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path path = currentDir.resolve(cloudMessage.getFileName());
        ctx.writeAndFlush(new DownloadFileMessage(path));
    }

    private void processServerDirMessage(ServerDirMessage cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path path = Path.of(cloudMessage.getCurrentDir());
        if (Files.isDirectory(path)) {
            if (rootDir.compareTo(path) == 0 || rootDir.compareTo(path) > 0) {
                sendList(ctx, rootDir);
            } else {
                currentDir = path;
                sendList(ctx, currentDir);
            }
        }
    }

}
