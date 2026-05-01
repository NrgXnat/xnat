package org.nrg.framework.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SanitizeUtils {
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("(^|/|\\\\)\\.\\.($|/|\\\\)");

    private SanitizeUtils() {
    }

    public static boolean containsPathTraversal(String inputPath) {
        if (inputPath == null) {
            return false;
        }
        Matcher matcher = PATH_TRAVERSAL_PATTERN.matcher(inputPath);
        return matcher.find();
    }

    public static String sanitizeFilePath(String inputPath) {
        if (inputPath == null || containsPathTraversal(inputPath)) {
            return null;
        }
        return inputPath;
    }
}
