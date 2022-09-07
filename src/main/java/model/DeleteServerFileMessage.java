package model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Path;

@Data
public class DeleteServerFileMessage  implements CloudMessage {
    private final String fileName;
    private final boolean isDirectory;
    private final String folder;

    public DeleteServerFileMessage(Path folder) throws IOException {
        fileName = folder.getFileName().toString();
        isDirectory = folder.toFile().isDirectory();
        this.folder = folder.getParent().toString();
    }

    @Override
    public CommandType getType() {
        return CommandType.DELETE_SERVER_FILE;
    }
}
