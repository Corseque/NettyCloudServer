package model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class DownloadFileMessage implements CloudMessage {

    private final String fileName;
    private final byte[] bytes;
    private final String serverPath;
    private final String clientPath;

    public DownloadFileMessage(Path serverPath, Path clientPath) throws IOException {
        fileName = serverPath.getFileName().toString();
        if (serverPath.isAbsolute()) {
            bytes = Files.readAllBytes(serverPath);
        } else {
            bytes = null;
        }
        this.serverPath = serverPath.getParent().toString();
        this.clientPath = clientPath.toString();
    }

    public DownloadFileMessage(Path serverPath, String fileName, Path clientPath) throws IOException {
        this.fileName = fileName;
        if (serverPath.isAbsolute()) {
            bytes = Files.readAllBytes(serverPath);
        } else {
            bytes = null;
        }
        this.serverPath = serverPath.toString();
        this.clientPath = clientPath.toString();
    }


    @Override
    public CommandType getType() {
        return CommandType.DOWNLOAD_FILE;
    }
}
