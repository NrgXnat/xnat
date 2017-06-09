package org.nrg.xnat.eventservice.model.xnat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public class XnatFile extends XnatModelObject {
    private static final Logger log = LoggerFactory.getLogger(XnatFile.class);
    private String name;
    private String path;
    private List<String> tags;
    private String format;
    private String content;
    private File file;

    public XnatFile() {}

    public XnatFile(final String parentUri,
                    final String name,
                    final String path,
                    final String tagsCsv,
                    final String format,
                    final String content,
                    final File file) {
        if (parentUri == null) {
            log.error("Cannot construct a file URI. Parent URI is null.");
        } else {
            this.uri = parentUri + "/files/" + name;
        }
        this.name = name;
        this.path = path;
        this.tags = Arrays.asList(tagsCsv.split(","));
        this.format = format;
        this.content = content;
        this.file = file;
    }

    public Project getProject(final UserI userI) {
        // I don't think there is any way to get the project from this.
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(final List<String> tags) {
        this.tags = tags;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(final String format) {
        this.format = format;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public File getFile() {
        return file;
    }

    public void setFile(final File file) {
        this.file = file;
    }

    @Override
    public XFTItem getXftItem(final UserI userI) {
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final XnatFile that = (XnatFile) o;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.path, that.path) &&
                Objects.equals(this.tags, that.tags) &&
                Objects.equals(this.format, that.format) &&
                Objects.equals(this.content, that.content) &&
                Objects.equals(this.file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, path, tags, format, content, file);
    }

    @Override
    public String toString() {
        return name;
    }
}
