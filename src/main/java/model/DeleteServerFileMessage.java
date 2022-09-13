package model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Path;

@Data
public class DeleteServerFileMessage  implements CloudMessage {
    private final String fileName;
    private final String serverPath;

    public DeleteServerFileMessage(Path path) throws IOException {
        fileName = path.getFileName().toString();
        serverPath = path.getParent().toString();
    }

    @Override
    public CommandType getType() {
        return CommandType.DELETE_SERVER_FILE;
    }
}
