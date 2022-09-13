package model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class UploadFileMessage implements CloudMessage {

    private final String fileName;
    private final byte[] bytes;
    private final String serverPath;


    public UploadFileMessage(Path userPath, Path serverFolder) throws IOException {
        fileName = userPath.getFileName().toString();
        bytes = Files.readAllBytes(userPath);
        this.serverPath = serverFolder.toString();
    }

    @Override
    public CommandType getType() {
        return CommandType.UPLOAD_FILE;
    }
}
