package model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FilesListMessage implements CloudMessage {

    private final List<String> files;
    private final String path;

    public FilesListMessage(Path path) throws IOException {
        if (path.isAbsolute()) {
            files = Files.list(path)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } else {
            files = new ArrayList<>();
        }
        this.path = path.toString();
    }

    public FilesListMessage(List<String> files, Path dirMask) throws IOException {
        this.files = files;
        path = dirMask.toString();
    }

    @Override
    public CommandType getType() {
        return CommandType.FILES_LIST;
    }
}
