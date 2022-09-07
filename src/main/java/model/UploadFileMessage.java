package model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class UploadFileMessage implements CloudMessage {

    private final String fileName;
    private final String folder;
    private final byte[] bytes;


    public UploadFileMessage(Path folder) throws IOException {
        fileName = folder.getFileName().toString();
        this.folder = folder.getParent().toString();
        bytes = Files.readAllBytes(folder);
    }

    @Override
    public CommandType getType() {
        return CommandType.UPLOAD_FILE;
    }
}
