/*
 * web: org.nrg.xnat.services.archive.impl.legacy.DownloadArchiveOptions
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.archive.impl.legacy;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Helper to parse and
 */
public class DownloadArchiveOptions {
    public enum Option {
        ProjectIncludedInPath("projectIncludedInPath"),
        SubjectIncludedInPath("subjectIncludedInPath"),
        Simplified("simplified");

        Option(final String value) {
            _value = value;
        }

        @Override
        public String toString() {
            return _value;
        }

        private final String _value;
    }

    public static DownloadArchiveOptions getOptions(final String description) {
        return new DownloadArchiveOptions(description);
    }

    public static DownloadArchiveOptions getOptions(final List<String> options) {
        return new DownloadArchiveOptions(options);
    }

    private DownloadArchiveOptions(final List<String> options) {
        this(options == null ? "" : Joiner.on(", ").join(options));
    }

    private DownloadArchiveOptions(final String description) {
        _description = StringUtils.defaultIfBlank(description, "");
        _projectIncludedInPath = _description.contains("projectIncludedInPath");
        _subjectIncludedInPath = _description.contains("subjectIncludedInPath");
        _simplified = _description.contains("simplified");
    }

    public String getDescription() {
        return _description;
    }

    public boolean isProjectIncludedInPath() {
        return _projectIncludedInPath;
    }

    public boolean isSubjectIncludedInPath() {
        return _subjectIncludedInPath;
    }

    public boolean isSimplified() {
        return _simplified;
    }

    private final String  _description;
    private final boolean _projectIncludedInPath;
    private final boolean _subjectIncludedInPath;
    private final boolean _simplified;
}
