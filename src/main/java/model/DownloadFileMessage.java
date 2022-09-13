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

    public DownloadFileMessage(Path serverPath) throws IOException {
        fileName = serverPath.getFileName().toString();
        if (serverPath.isAbsolute()) {
            bytes = Files.readAllBytes(serverPath);
        } else {
            bytes = null;
        }
        this.serverPath = serverPath.toString();
    }

    public DownloadFileMessage(Path serverPath, String fileName) throws IOException {
        this.fileName = fileName;
        if (serverPath.isAbsolute()) {
            bytes = Files.readAllBytes(serverPath);
        } else {
            bytes = null;
        }
        this.serverPath = serverPath.toString();
    }


    @Override
    public CommandType getType() {
        return CommandType.DOWNLOAD_FILE;
    }
}
