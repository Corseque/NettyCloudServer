package model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FilesListMessage implements CloudMessage {

    private final List<String> files;
  //  private final Path path;

    public FilesListMessage(Path path) throws IOException {
        files = Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    //    this.path = path;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILES_LIST;
    }
}
