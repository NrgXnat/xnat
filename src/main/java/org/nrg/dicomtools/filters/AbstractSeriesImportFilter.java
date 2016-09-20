/*
 * org.nrg.dicomtools.filters.AbstractSeriesImportFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.filters;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public abstract class AbstractSeriesImportFilter implements SeriesImportFilter {

    public AbstractSeriesImportFilter(final String json) throws IOException {
        // TODO: For some reason, if I call the code in the getContentsAsMap() directly in here, it wants to return an Object instead of a LinkedHashMap.
        this(DicomFilterService.getContentsAsMap(json));
    }

    public AbstractSeriesImportFilter(final LinkedHashMap<String, String> values) {
        setProjectId(values.containsKey(KEY_PROJECT_ID) ? values.get(KEY_PROJECT_ID) : "");
        setEnabled(!values.containsKey(KEY_ENABLED) || Boolean.parseBoolean(values.get(KEY_ENABLED)));
        if (values.containsKey(KEY_MODE)) {
            setMode(SeriesImportFilterMode.mode(values.get(KEY_MODE)));
        }
        initialize(values);
    }

    /**
     * Constructor that subclasses can use to initialize filter metadata. This does not call the subclass's {@link #initialize(Map)}
     * method, so the implementation-specific operations must be done directly. Specifying a project ID creates a project-scoped filter
     * instance. To create a site-wide filter, just pass an empty string or null in place of the project ID..
     * @param projectId  The project ID for the filter.
     * @param mode       The mode of the filter.
     * @param enabled    Whether the filter is enabled.
     */
    protected AbstractSeriesImportFilter(final String projectId, final SeriesImportFilterMode mode, final boolean enabled) {
        setProjectId(projectId);
        setMode(mode);
        setEnabled(enabled);
    }

    public static List<String> parsePersistedFilters(final String list) {
        return Arrays.asList(list.split((list.contains("\\n") ? "\\\\n" : "\\n"), -1));
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
                    _log.info("The specified DICOM header " + parameter + " specifies a sequence or embedded DICOM object, which isn't currently supported.");
                }
            }
        }
        return values;
    }

    /**
     * Passes a map of values onto the concrete implementation to use as necessary.
     * @param values    The initialization values.
     */
    abstract protected void initialize(final Map<String, String> values);

    /**
     * Gets all of the properties that are unique to each particular filter implementation.
     * @return A map with the unique properties and values.
     */
    abstract protected Map<String, String> getImplementationProperties();

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
        if (mode != _mode) {
            _mode = mode;
        }
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
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
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

    protected LinkedHashMap<String, String> toQualifiedMap(final String prefix) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
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

    private static class DicomTag {
        private final String _tag;
        private final int _value;

        public DicomTag(int value, String tag) {
            _value = value;
            _tag = tag;
        }
    }

    private static class DicomTagComparator implements Comparator<DicomTag> {
        @Override
        public int compare(final DicomTag first, final DicomTag second) {
            if (first._value < second._value) {
                return -1;
            }
            if (first._value > second._value) {
                return 1;
            }
            return 0;
        }
    }

    private static TreeSet<DicomTag> DICOM_TAGS = new TreeSet<DicomTag>(new DicomTagComparator()) {{
        final Field[] fields = Tag.class.getFields();
        for (final Field field : fields) {
            try {
                final int value = field.getInt(null);
                if (value >= 0) {
                    add(new DicomTag(value, field.getName()));
                }
            } catch (IllegalAccessException ignored) {
                // Ignore this, if we're not allowed to see it, we don't care about it.
            }
        }
    }};

    private static List<String> DICOM_TAG_NAMES = new ArrayList<String>() {{
        for (final DicomTag tag : DICOM_TAGS) {
            add(tag._tag);
        }
    }};

    private static final Logger _log = LoggerFactory.getLogger(AbstractSeriesImportFilter.class);

    private String _projectId;
    private boolean _enabled;
    private SeriesImportFilterMode _mode;
    private String _modality;
}
