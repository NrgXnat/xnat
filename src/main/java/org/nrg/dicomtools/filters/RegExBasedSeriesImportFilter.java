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
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;
import org.nrg.dicomtools.utilities.DicomUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public RegExBasedSeriesImportFilter(final String contents, final SeriesImportFilterMode mode, final boolean enabled) throws IOException {
        this(contents, "", mode, enabled);
    }

    public RegExBasedSeriesImportFilter(final String contents, final String projectId, final SeriesImportFilterMode mode, final boolean enabled) throws IOException {
        super(createMap(contents, projectId, mode, enabled));
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
        final Map<Integer, String> values = new LinkedHashMap<>();
        final SpecificCharacterSet characterSet = dicomObject.getSpecificCharacterSet();
        for (final int header : getFilters().keySet()) {
            if (dicomObject.contains(header)) {
                final DicomElement dicomElement = dicomObject.get(header);
                final String value = dicomElement.getValueAsString(characterSet, 300);
                values.put(header, StringUtils.isNotBlank(value) ? value : "");
            } else {
                values.put(header, null);
            }
        }
        return shouldIncludeDicomObjectImpl(values);
    }

    @Override
    public boolean shouldIncludeDicomObject(final DicomObject dicomObject, final String targetModality) {
        return shouldIncludeDicomObject(dicomObject);
    }

    @Override
    public boolean shouldIncludeDicomObject(final Map<String, String> headers, final String targetModality) {
        return shouldIncludeDicomObject(headers);
    }

    @Override
    public boolean shouldIncludeDicomObject(final Map<String, String> headers) {
        final Map<Integer, String> converted = new LinkedHashMap<>(headers.size());
        for (final String header : headers.keySet()) {
            final int tag = DicomUtils.parseDicomHeaderId(header);
            converted.put(tag, headers.get(header));
        }
        return shouldIncludeDicomObjectImpl(converted);
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
                StringUtils.equals(_contents, that._contents);
    }

    @Override
    public int hashCode() {
        int result = _contents.hashCode();
        result = 31 * result + (isEnabled() ? 1 : 0);
        result = 31 * result + getProjectId().hashCode();
        return result;
    }

    @Override
    protected Map<String, String> getImplementationProperties() {
        return new HashMap<String, String>() {{
            put(KEY_LIST, _contents); }};
    }

    @Override
    protected void initialize(final Map<String, String> values) {
        _contents = values.get(KEY_LIST);
        getFilters().putAll(createFilterConfiguration(parsePersistedFilters(_contents)));
    }

    private boolean shouldIncludeDicomObjectImpl(final Map<Integer, String> headers) {
        for (final int header : getFilters().keySet()) {
            final String value = headers.get(header);

            final List<Pattern> patterns = getFilters().get(header);
            for (final Pattern filter : patterns) {
                // Check for EXISTS pattern.
                if (filter.pattern().equals("EXISTS") && value != null) {
                    // As long as the value is not null, the header exists.
                    return getMode() == SeriesImportFilterMode.Whitelist;
                }
                if (filter.pattern().equals("!EXISTS") && value == null) {
                    return getMode() == SeriesImportFilterMode.Whitelist;
                }
                if (value == null) {
                    continue;
                }
                // Finding a match is insufficient, we need to check the mode.
                if (filter.matcher(value).find()) {
                    if (_log.isDebugEnabled()) {
                        _log.debug("Matched  " + DicomUtils.getDicomAttribute(header) + " tag value " + value + " against filter, " + filter.toString() + (getMode() == SeriesImportFilterMode.Whitelist ? "accepting" : "rejecting"));
                    }
                    // So if we matched, then this should be included if this is a whitelist. If
                    // it's a blacklist, this will return false and indicate that this DicomObject
                    // should not be included.
                    return getMode() == SeriesImportFilterMode.Whitelist;
                } else if (_log.isDebugEnabled()) {
                    _log.debug("Didn't match " + DicomUtils.getDicomAttribute(header) + " tag value " + value + " against filter " + filter.toString());
                }
            }
        }

        if (_log.isDebugEnabled()) {
            _log.debug("Didn't match any headers, " + (getMode() == SeriesImportFilterMode.Blacklist ? "rejecting" : "accepting"));
        }

        return getMode() == SeriesImportFilterMode.Blacklist;
    }

    private Map<Integer, List<Pattern>> getFilters() {
        if (_filters == null) {
            _filters = new HashMap<>();
        }
        return _filters;
    }

    private String getFilterList() {
        final List<String> lines = new ArrayList<>();
        if (getFilters().size() == 1 && getFilters().containsKey(Tag.SeriesDescription)) {
            for (final Pattern expression : getFilters().get(Tag.SeriesDescription)) {
                lines.add(expression.toString());
            }
        } else {
            for (final int key : getFilters().keySet()) {
                lines.add("[" + DicomUtils.getDicomAttribute(key) + "]");
                for (final Pattern expression : getFilters().get(key)) {
                    lines.add(expression.toString());
                }
            }
        }
        return Joiner.on("\n").join(lines);
    }

    private static Map<Integer, List<Pattern>> createFilterConfiguration(final List<String> strings) {
        final Map<Integer, List<Pattern>> filters = new HashMap<>();
        final Map<String, List<String>> failed = new HashMap<>();

        int currentField = DEFAULT_FIELD;
        filters.put(currentField, new ArrayList<Pattern>());

        for (final String current : strings) {
            // A blank line or comment is ignored.
            if (StringUtils.isBlank(current) || current.startsWith("#")) {
                continue;
            }

            // Trim off white space and work with that.
            final String trimmed = current.trim();

            // Check for the config header delimiters, e.g. [BurnedInAnnotations]
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                currentField = DicomUtils.parseDicomHeaderId(trimmed.substring(1, trimmed.length() - 1));
                if (!filters.containsKey(currentField)) {
                    filters.put(currentField, new ArrayList<Pattern>());
                }
            } else {
                try {
                    filters.get(currentField).add(Pattern.compile(trimmed));
                } catch (PatternSyntaxException ignored) {
                    final String attribute = DicomUtils.getDicomAttribute(currentField);
                    if (!failed.containsKey(attribute)) {
                        failed.put(attribute, new ArrayList<String>());
                    }
                    failed.get(current).add(trimmed);
                }
            }
        }

        if (failed.size() > 0) {
            final List<String> failedSections = new ArrayList<>(failed.size());
            for (final String failedSection : failed.keySet()) {
                final List<String> failedPatterns = failed.get(failedSection);
                failedSections.add(failedPatterns.size() == 1 ? failedSection + ": " + failedPatterns.get(0) : failedSection + ": " + Joiner.on(", ").join(failedPatterns));
            }
            final StringBuilder buffer = new StringBuilder("The series import filter contains the following invalid pattern(s):\n");
            for (final String failedSection : failedSections) {
                buffer.append(" * ").append(failedSection).append("\n");
            }
            throw new NrgServiceRuntimeException(NrgServiceError.UnsupportedFeature, buffer.toString());
        }

        if (filters.get(DEFAULT_FIELD).isEmpty()) {
            filters.remove(DEFAULT_FIELD);
        }

        return filters;
    }

    private static LinkedHashMap<String, String> createMap(final String contents, final String projectId, final SeriesImportFilterMode mode, final boolean enabled) {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(KEY_LIST, contents);
        if (StringUtils.isNotBlank(projectId)) {
            map.put(KEY_PROJECT_ID, projectId);
        }
        map.put(KEY_MODE, mode.getValue());
        map.put(KEY_ENABLED, Boolean.toString(enabled));
        return map;
    }

    private static final Logger _log = LoggerFactory.getLogger(RegExBasedSeriesImportFilter.class);

    private static final int DEFAULT_FIELD = Tag.SeriesDescription;

    private Map<Integer, List<Pattern>> _filters;
    private String _contents;
}
