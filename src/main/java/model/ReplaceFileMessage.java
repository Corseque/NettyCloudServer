package model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class ReplaceFileMessage implements CloudMessage {

    private final String fileName;
    private final String path;
    private final byte[] bytes;


    public ReplaceFileMessage(Path path) throws IOException {
        fileName = path.getFileName().toString();
        this.path = path.getParent().toString();
        bytes = Files.readAllBytes(path);
    }

    @Override
    public CommandType getType() {
        return CommandType.REPLACE_FILE;
    }
}
