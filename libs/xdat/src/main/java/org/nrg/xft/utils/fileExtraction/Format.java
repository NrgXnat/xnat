package org.nrg.xft.utils.fileExtraction;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Format {
    TAR("^(?<filename>.*)\\.(?<extension>[tx]ar)$"),
    TGZ("^(?<filename>.*)\\.(?<extension>t(ar\\.)?gz)$"),
    ZIP("^(?<filename>.*)\\.(?<extension>z(ip|ar))$"),
    GZ("^(?!.*\\.tar\\.gz$)^(?<filename>.*)\\.(?<extension>gz)$"),
    UNKNOWN("^$");

    Format(final String regex) {
        _pattern = Pattern.compile(regex);
    }

    /**
     * Return the compression format based on filename. This method search the available formats and tries to match
     * the filename to the regular expression for that format.
     *
     * @param filename The name of the file to check
     *
     * @return The compression format indicated by the filename.
     */
    public static Format getFormat(final String filename) {
        return Arrays.stream(values()).filter(format -> format._pattern.matcher(filename).matches()).findAny().orElse(UNKNOWN);
    }

    /**
     * Return the compression format based on filename. This method search the available formats and tries to match
     * the filename to the regular expression for that format.
     *
     * @param path The path to the file to check
     *
     * @return The compression format indicated by the filename.
     */
    public static Format getFormat(final Path path) {
        return getFormat(path.getFileName().toString());
    }

    /**
     * Return filename without suffix.
     *
     * @param filename The filename to be stripped.
     *
     * @return The filename stripped of its suffix as long as the suffix matches one of the format patterns.
     */
    public String getFileName(final String filename) {
        if (this == UNKNOWN) {
            return filename;
        }
        final Matcher matcher = _pattern.matcher(filename);
        return matcher.find() ? matcher.group("filename") : filename;
    }

    private final Pattern _pattern;
}
