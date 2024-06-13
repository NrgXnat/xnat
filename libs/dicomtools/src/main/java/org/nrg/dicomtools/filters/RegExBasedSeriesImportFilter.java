/*
 * dicomtools: org.nrg.dicomtools.filters.RegExBasedSeriesImportFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.filters;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;
import org.nrg.dicomtools.utilities.DicomUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Slf4j
public class RegExBasedSeriesImportFilter extends AbstractSeriesImportFilter {
    public RegExBasedSeriesImportFilter(final String json) throws IOException {
        super(json);
    }

    public RegExBasedSeriesImportFilter(final LinkedHashMap<String, String> map) {
        super(map);
    }

    @SuppressWarnings("unused")
    public RegExBasedSeriesImportFilter(final String contents, final SeriesImportFilterMode mode, final boolean enabled) {
        this(contents, "", mode, enabled);
    }

    public RegExBasedSeriesImportFilter(final String contents, final String projectId, final SeriesImportFilterMode mode, final boolean enabled) {
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
        // If this filter isn't enabled, then it should always return true because it's not filtering anything out.
        if (!isEnabled()) {
            return true;
        }
        final Map<Integer, String> values       = new LinkedHashMap<>();
        final SpecificCharacterSet characterSet = dicomObject.getSpecificCharacterSet();
        for (final int header : getFilters().keySet()) {
            if (dicomObject.contains(header)) {
                final DicomElement dicomElement = dicomObject.get(header);
                final String       value        = dicomElement.getValueAsString(characterSet, 300);
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
        // If this filter isn't enabled, then it should always return true because it's not filtering anything out.
        if (!isEnabled()) {
            return true;
        }
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
               getMode() == that.getMode() &&
               StringUtils.equals(getModality(), that.getModality()) &&
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<Integer> getFilterTagsImpl() {
        return getFilters().keySet();
    }

    @Override
    protected Map<String, String> getImplementationProperties() {
        return ImmutableMap.of(KEY_LIST, _contents);
    }

    @Override
    protected void initialize(final Map<String, String> values) {
        _contents = values.get(KEY_LIST);
        getFilters().putAll(createFilterConfiguration(parsePersistedFilters(_contents)));
    }

    private boolean shouldIncludeDicomObjectImpl(final Map<Integer, String> headers) {
        // If this filter isn't enabled, then it should always return true because it's not filtering anything out.
        if (!isEnabled()) {
            return true;
        }
        for (final int header : getFilters().keySet()) {
            final String        value    = headers.get(header);
            final List<Pattern> patterns = getFilters().get(header);
            for (final Pattern filter : patterns) {
                if (testValueAgainstFilter(header, value, filter)) {
                    log.debug("Matched {} tag with value \"{}\" against filter \"{}\". Mode is {} so object is {}", DicomUtils.getDicomAttribute(header), value, filter.pattern(), getMode().getValue(), getMode() == SeriesImportFilterMode.Whitelist ? "accepted" : "rejected");
                    return getMode() == SeriesImportFilterMode.Whitelist;
                }
            }
        }

        log.debug("Didn't match any headers. Mode is {} so object is {}", getMode().getValue(), getMode() == SeriesImportFilterMode.Blacklist ? "accepted" : "rejected");
        return getMode() == SeriesImportFilterMode.Blacklist;
    }

    private boolean testValueAgainstFilter(final int header, final String value, final Pattern filter) {
        // Check for EXISTS pattern. As long as the value isn't null, then this header exists, so the filter pattern matches.
        if (filter.pattern().equals("EXISTS") && value != null) {
            // As long as the value is not null, the header exists.
            log.debug("{} matched value \"{}\", returning true", getFilterDescription(header, filter), value);
            return true;
        }
        // Check for !EXISTS pattern. If the value is null, then this header does not exist, so the filter pattern matches.
        if (filter.pattern().equals("!EXISTS") && value == null) {
            log.debug("{} matched null (i.e. header doesn't exist), returning true", getFilterDescription(header, filter));
            return true;
        }
        if (value == null) {
            log.debug("Header {} is null, can't match anything other than !EXISTS so returning false", DicomUtils.getDicomAttribute(header));
            return false;
        }
        // Finding a match is insufficient, we need to check the mode.
        if (filter.matcher(value).find()) {
            log.debug("{} matched value \"{}\", returning true", getFilterDescription(header, filter), value);
            return true;
        }
        log.debug("{} didn't match value \"{}\", returning false", getFilterDescription(header, filter), value);
        return false;
    }

    private String getFilterDescription(final int header, final Pattern filter) {
        final StringBuilder buffer = new StringBuilder("Filter ");
        if (StringUtils.isNotBlank(getProjectId())) {
            buffer.append(getProjectId());
        } else {
            buffer.append("site");
        }
        buffer.append("/").append(DicomUtils.getDicomAttribute(header)).append("/").append(filter.pattern());
        return buffer.toString();
    }

    private Map<Integer, List<Pattern>> getFilters() {
        if (_filters == null) {
            _filters = new HashMap<>();
        }
        return _filters;
    }

    private static Map<Integer, List<Pattern>> createFilterConfiguration(final List<String> strings) {
        final Map<Integer, List<Pattern>> filters = new HashMap<>();
        final Map<String, List<String>>   failed  = new HashMap<>();

        int currentField = DEFAULT_FIELD;
        filters.put(currentField, new ArrayList<>());

        for (final String current : strings) {
            // A blank line or comment is ignored.
            if (StringUtils.isBlank(current) || current.startsWith("#")) {
                continue;
            }

            // Trim white space and work with that.
            final String trimmed = current.trim();

            // Check for the config header delimiters, e.g. [BurnedInAnnotations]
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                currentField = DicomUtils.parseDicomHeaderId(trimmed.substring(1, trimmed.length() - 1));
                if (!filters.containsKey(currentField)) {
                    filters.put(currentField, new ArrayList<>());
                }
            } else {
                try {
                    filters.get(currentField).add(Pattern.compile(trimmed, Pattern.CASE_INSENSITIVE));
                } catch (PatternSyntaxException ignored) {
                    final String attribute = DicomUtils.getDicomAttribute(currentField);
                    if (!failed.containsKey(attribute)) {
                        failed.put(attribute, new ArrayList<>());
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

    private static final int DEFAULT_FIELD = Tag.SeriesDescription;

    private Map<Integer, List<Pattern>> _filters;
    private String                      _contents;
}
