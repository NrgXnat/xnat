package org.nrg.xnat.snapshot;

import java.io.File;
import java.nio.file.Path;

/**
 * Class to bundle related properties of File Resources.
 */
public class FileResource {
    private Path _path;
    private String _content;
    private String _format;

    public FileResource( Path path, String content, String format) {
        this._path = path;
        this._content = content;
        this._format = format;
    }

    public String getName() {
        return _path.getFileName().toString();
    }

    public File getFile() {
        return _path.toFile();
    }

    public Path getRoot() {
        return _path.getRoot();
    }

    public Path getPath() {
        return _path;
    }

    public String getContent() {
        return _content;
    }

    public String getFormat() {
        return _format;
    }

}
