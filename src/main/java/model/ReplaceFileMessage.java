package model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class ReplaceFileMessage implements CloudMessage {

    private final String fileName;
    private final byte[] bytes;
    private final String serverPath;


    public ReplaceFileMessage(Path path, Path serverFolder) throws IOException {
        fileName = path.getFileName().toString();
        bytes = Files.readAllBytes(path);
        this.serverPath = serverFolder.toString();
    }

    @Override
    public CommandType getType() {
        return CommandType.REPLACE_FILE;
    }
}
