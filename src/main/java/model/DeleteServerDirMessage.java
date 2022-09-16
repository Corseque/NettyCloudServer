package model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Path;

@Data
public class DeleteServerDirMessage implements CloudMessage {
    private final String folderName;
    private final String serverPath;

    public DeleteServerDirMessage(Path path) throws IOException {
        folderName = path.getFileName().toString();
        serverPath = path.getParent().toString();
    }

    @Override
    public CommandType getType() {
        return CommandType.DELETE_SERVER_DIR;
    }
}
