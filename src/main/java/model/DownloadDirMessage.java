package model;

import lombok.Data;

import java.nio.file.Path;
import java.util.List;

@Data
public class DownloadDirMessage implements CloudMessage {

    private final String folderName;
    private final String serverPath;
    private final List<String> files;
    private final String clientPath;

    public DownloadDirMessage(Path serverPath, List<String> files, Path clientPath) {
        this.folderName = serverPath.getFileName().toString();
        this.serverPath = serverPath.getParent().toString();
        this.files = files;
        this.clientPath = clientPath.toString();
    }

    @Override
    public CommandType getType() {
        return CommandType.DOWNLOAD_DIR;
    }
}
