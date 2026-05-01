package org.nrg.dicom.dicomedit;

import org.apache.commons.lang3.StringUtils;
import org.nrg.dicom.mizer.exceptions.MizerException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This processor strips all end-line comments out of the script.
 * Scan for comment-start string "//".
 * Lines can contain URLs. If the character before "//" is ":" then don't match.
 * Comments in strings (text between double-quotes) are retained.
 * Strings are not nested, ""// comment"" parses as string-comment-string.
 */
public class ScriptProcessorRemoveEndLineComments implements ScriptDirectiveProcessor {
    public ScriptProcessorRemoveEndLineComments() {
    }

    @Override
    public DE6Script process(DE6Script script) {
        return null;
    }

    /**
     * strips all end-line comments
     *
     * @param lines list of lines to strip.
     * @return list of stripped lines.
     */
    @Override
    public List<String> process(List<String> lines) throws MizerException {
        return lines.stream().map(line -> {
            if (line.trim().matches(".*\"[^\"]*(?<!https?:)//[^\"]*\".*")) {
                return removeComments(line);
            } else {
                return line.replaceAll("(?<!https?:)//.*$", "");
            }
        }).collect(Collectors.toList());
    }

    private static String removeComments(String line) {
        boolean       inString = false;
        int           length   = line.length();
        StringBuilder result   = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inString = !inString;
            }

            if (!inString && i < length - 1 && c == '/' && line.charAt(i + 1) == '/') {
                if (i > 0 && line.charAt(i - 1) == ':') {
                    result.append("/");
                    continue;
                }
                break;
            }

            result.append(c);
        }

        return StringUtils.endsWith(line, "\n") ? StringUtils.appendIfMissing(result.toString(), "\n") : result.toString();
    }
}
