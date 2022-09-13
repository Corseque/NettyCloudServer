package model;

import lombok.Data;

import java.nio.file.Path;

@Data
public class CreateServerDirMessage implements CloudMessage{

    private final String folderName;
    private final String serverPath;

    public CreateServerDirMessage(Path path) {
        this.folderName = path.getFileName().toString();
        this.serverPath = path.getParent().toString();
    }

    @Override
    public CommandType getType() {
        return CommandType.CREATE_SERVER_DIR;
    }
}
