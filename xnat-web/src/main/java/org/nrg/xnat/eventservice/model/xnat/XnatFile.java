package org.nrg.xnat.eventservice.model.xnat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@JsonInclude(Include.NON_NULL)
public class XnatFile extends XnatModelObject {
    private String name;
    private String path;
    private List<String> tags;
    private String format;
    private String content;
    private Long size;
    private String checksum;

    public XnatFile() {}

    public XnatFile(final String parentUri,
                    final String name,
                    final String path,
                    final String tagsCsv,
                    final String format,
                    final String content,
                    final Long size,
                    final String checksum) {
        if (parentUri == null) {
            log.debug("Cannot construct a file URI. Parent URI is null.");
        } else {
            this.uri = parentUri + "/files/" + name;
        }

        this.label = name;
        this.name = name;
        this.path = path;
        this.tags = Arrays.asList(tagsCsv.split(","));
        this.format = format;
        this.content = content;
        this.size = size;
        this.checksum = checksum;
}
    public static XnatFile populateSample() {
        XnatFile xnatFile = new XnatFile();
        xnatFile.setFormat("DICOM");
        return xnatFile;
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

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getChecksum() { return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
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
                Objects.equals(this.size, that.size) &&
                Objects.equals(this.checksum, that.checksum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, path, tags, format, content, size, checksum);
    }

    @Override
    public String toString() {
        return name;
    }
}
