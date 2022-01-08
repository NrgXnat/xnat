/*
 * dicomtools: org.nrg.dicomtools.filters.AbstractSeriesImportFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.filters;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractSeriesImportFilter implements SeriesImportFilter {

    public AbstractSeriesImportFilter(final String json) throws IOException {
        // TODO: For some reason, if I call the code in the getContentsAsMap() directly in here, it wants to return an Object instead of a LinkedHashMap.
        this(DicomFilterService.getContentsAsMap(json));
    }

    public AbstractSeriesImportFilter(final LinkedHashMap<String, String> values) {
        setProjectId(values.getOrDefault(KEY_PROJECT_ID, ""));
        setEnabled(!values.containsKey(KEY_ENABLED) || Boolean.parseBoolean(values.get(KEY_ENABLED)));
        if (values.containsKey(KEY_MODE)) {
            setMode(SeriesImportFilterMode.mode(values.get(KEY_MODE)));
        }
        initialize(values);
    }

    /**
     * Constructor that subclasses can use to initialize filter metadata. This does not call the subclass's {@link #initialize(Map)}
     * method, so the implementation-specific operations must be done directly. Specifying a project ID creates a project-scoped filter
     * instance. To create a site-wide filter, just pass an empty string or null in place of the project ID.
     *
     * @param projectId The project ID for the filter.
     * @param mode      The mode of the filter.
     * @param enabled   Whether the filter is enabled.
     */
    protected AbstractSeriesImportFilter(final String projectId, final SeriesImportFilterMode mode, final boolean enabled) {
        setProjectId(projectId);
        setMode(mode);
        setEnabled(enabled);
    }

    public static List<String> parsePersistedFilters(final String list) {
        return Arrays.stream(list.split((list.contains("\\n") ? "\\\\n" : "\\n"), -1)).map(String::trim).collect(Collectors.toList());
    }

    // TODO: Eventually this can be replaced with a lambda that tells the target methods how to get tags from maps and DicomObjects.
    public static Map<String, String> convertDicomObjectToMap(final DicomObject dicomObject) {
        final Map<String, String> values = new HashMap<>();
        for (final String parameter : DICOM_TAG_NAMES) {
            final int tag = Tag.forName(parameter);
            if (dicomObject.contains(tag)) {
                final DicomElement element = dicomObject.get(tag);
                if (!element.hasItems()) {
                    final String value = element.getValueAsString(dicomObject.getSpecificCharacterSet(), 0);
                    if (StringUtils.isNotBlank(value)) {
                        values.put(parameter, value);
                    }
                } else {
                    log.info("The specified DICOM header {} specifies a sequence or embedded DICOM object, which isn't currently supported.", parameter);
                }
            }
        }
        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getFilterTags() {
        return getFilterTagsImpl().stream().distinct().sorted().collect(Collectors.toList());
    }

    /**
     * Passes a map of values onto the concrete implementation to use as necessary.
     *
     * @param values The initialization values.
     */
    abstract protected void initialize(final Map<String, String> values);

    /**
     * Gets all the properties that are unique to each particular filter implementation.
     *
     * @return A map with the unique properties and values.
     */
    abstract protected Map<String, String> getImplementationProperties();

    /**
     * Gets all the properties that are unique to each particular filter implementation.
     *
     * @return A map with the unique properties and values.
     */
    abstract protected Collection<Integer> getFilterTagsImpl();

    @Override
    public String getProjectId() {
        return _projectId;
    }

    @Override
    public void setProjectId(final String projectId) {
        _projectId = projectId;
    }

    @Override
    public boolean isEnabled() {
        return _enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    @Override
    public SeriesImportFilterMode getMode() {
        return _mode;
    }

    @Override
    public void setMode(final SeriesImportFilterMode mode) {
        _mode = mode;
    }

    @Override
    public String getModality() {
        return _modality;
    }

    @Override
    public void setModality(final String modality) {
        _modality = modality;
    }

    @Override
    public LinkedHashMap<String, String> toMap() {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(KEY_MODE, _mode.getValue());
        map.put(KEY_ENABLED, Boolean.toString(_enabled));
        if (StringUtils.isNotBlank(_projectId)) {
            map.put(KEY_PROJECT_ID, _projectId);
        }
        map.putAll(getImplementationProperties());
        return map;
    }

    @Override
    public LinkedHashMap<String, String> toQualifiedMap() {
        return toQualifiedMap("seriesImportFilter");
    }

    @SuppressWarnings("SameParameterValue")
    protected LinkedHashMap<String, String> toQualifiedMap(final String prefix) {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(prefix(prefix, KEY_MODE), _mode.getValue());
        map.put(prefix(prefix, KEY_ENABLED), Boolean.toString(_enabled));
        if (StringUtils.isNotBlank(_projectId)) {
            map.put(prefix(prefix, KEY_PROJECT_ID), _projectId);
        }
        final Map<String, String> properties = getImplementationProperties();
        for (final String key : properties.keySet()) {
            map.put(prefix(prefix, key), properties.get(key));
        }
        return map;
    }

    protected String prefix(final String prefix, final String key) {
        return prefix + StringUtils.capitalize(key);
    }

    @Data
    @RequiredArgsConstructor
    private static class DicomTag implements Comparable<DicomTag> {
        private final int    value;
        private final String tag;

        @Override
        public int compareTo(final AbstractSeriesImportFilter.DicomTag other) {
            return Integer.compare(getValue(), other.getValue());
        }
    }

    private static final TreeSet<DicomTag> DICOM_TAGS = Arrays.stream(Tag.class.getFields()).map(field -> {
        try {
            final int value = field.getInt(null);
            return value >= 0
                   ? new DicomTag(value, field.getName())
                   : null;
        } catch (IllegalAccessException e) {
            // Ignore this, if we're not allowed to see it, we don't care about it.
            return null;
        }
    }).filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new));

    private static final List<String> DICOM_TAG_NAMES = DICOM_TAGS.stream().map(DicomTag::getTag).collect(Collectors.toList());

    private String                 _projectId;
    private boolean                _enabled;
    private SeriesImportFilterMode _mode;
    private String                 _modality;
}
