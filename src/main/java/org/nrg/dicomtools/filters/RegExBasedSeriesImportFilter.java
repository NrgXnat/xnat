/*
 * org.nrg.xnat.utils.SeriesImportFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/3/14 9:54 AM
 */
package org.nrg.dicomtools.filters;

import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegExBasedSeriesImportFilter extends AbstractSeriesImportFilter {

    public RegExBasedSeriesImportFilter(final String json) throws IOException {
        super(json);
    }

    public RegExBasedSeriesImportFilter(final LinkedHashMap<String, String> map) {
        super(map);
    }

    public RegExBasedSeriesImportFilter(final String contents, final SeriesImportFilterMode mode, final boolean enabled) {
        this(contents, "", mode, enabled);
    }

    public RegExBasedSeriesImportFilter(final String contents, final String projectId, final SeriesImportFilterMode mode, final boolean enabled) {
        super(projectId, mode, enabled);
        _filters = parsePersistedFilters(contents);
        compileFilterList(_filters);
    }

    @Override
    public String findModality(final DicomObject dicomObject) {
        return "";
    }

    @Override
    public String findModality(final Map<String, String> headers) {
        return "";
    }

    @Override
    public boolean shouldIncludeDicomObject(final DicomObject dicomObject) {
        final String seriesDescription = dicomObject.getString(Tag.SeriesDescription);
        return shouldIncludeDicomObject(new LinkedHashMap<String, String>() {{
            put(KEY_SERIES_DESCRIPTION, seriesDescription);
        }});
    }

    @Override
    public boolean shouldIncludeDicomObject(final DicomObject dicomObject, final String targetModality) {
        return shouldIncludeDicomObject(dicomObject);
    }

    @Override
    public boolean shouldIncludeDicomObject(final Map<String,String> headers) {
        final String seriesDescription = headers.get(KEY_SERIES_DESCRIPTION);
        for (String filter : _filters) {
            // Finding a match is insufficient, we need to check the mode.
            if (StringUtils.isNotEmpty(filter) && StringUtils.isNotBlank(seriesDescription) && seriesDescription.matches(filter)) {
                // So if we matched, then this should be included if this is a whitelist. If
                // it's a blacklist, this will return false and indicate that this DicomObject
                // should not be included.
                return getMode() == SeriesImportFilterMode.Whitelist;
            }
        }

        // We didn't match anything. That means that, if this is a blacklist, we should include
        // this DicomObject, but if it's a whitelist, we should not.
        return getMode() == SeriesImportFilterMode.Blacklist;
    }

    @Override
    public boolean shouldIncludeDicomObject(final Map<String, String> headers, final String targetModality) {
        return shouldIncludeDicomObject(headers);
    }

    @Override
    protected Map<String, String> getImplementationProperties() {
        return new HashMap<String, String>() {{ put(KEY_LIST, Joiner.on("\\n").join(getFilters())); }};
    }

    @Override
    protected void initialize(final Map<String, String> values) {
        final String list = values.get(KEY_LIST);
        _filters = parsePersistedFilters(list);
        compileFilterList(_filters);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RegExBasedSeriesImportFilter)) {
            return false;
        }

        final RegExBasedSeriesImportFilter that = (RegExBasedSeriesImportFilter) o;

        return isEnabled() == that.isEnabled() &&
                getProjectId().equals(that.getProjectId()) &&
                _filters.equals(that._filters);
    }

    @Override
    public int hashCode() {
        int result = _filters.hashCode();
        result = 31 * result + (isEnabled() ? 1 : 0);
        result = 31 * result + getProjectId().hashCode();
        return result;
    }

    public static List<Pattern> compileFilterList(final List<String> candidates) {
        final List<String> failed = new ArrayList<>();
        final List<Pattern> patterns = new ArrayList<>();
        for (String candidate : candidates) {
            try {
                patterns.add(Pattern.compile(candidate));
            } catch (PatternSyntaxException ignored) {
                failed.add(candidate);
            }
        }
        if (failed.size() > 0) {
            String failedPatterns;
            if (failed.size() == 1) {
                failedPatterns = failed.get(0);
            } else {
                failedPatterns = Joiner.on(", ").join(failed);
            }
            throw new NrgServiceRuntimeException(NrgServiceError.UnsupportedFeature, "The series import filter contains the following invalid pattern(s): " + failedPatterns);
        }
        return patterns;
    }

    private List<String> getFilters() {
        return _filters;
    }

    private List<String> _filters;
}
