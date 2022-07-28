package model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class UploadFileMessage implements CloudMessage {

    private final String fileName;
    private final byte[] bytes;

    public UploadFileMessage(Path path) throws IOException {
        fileName = path.getFileName().toString();
        bytes = Files.readAllBytes(path);
    }

    @Override
    public CommandType getType() {
        return CommandType.UPLOAD_FILE;
    }
}
