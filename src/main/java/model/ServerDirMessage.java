package model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Path;

@Data
public class ServerDirMessage implements CloudMessage {

    private final String currentDir;

    public ServerDirMessage(Path dirName) throws IOException {
        currentDir = dirName.toString();
    }

    @Override
    public CommandType getType() {
        return CommandType.SERVER_DIR;
    }
}