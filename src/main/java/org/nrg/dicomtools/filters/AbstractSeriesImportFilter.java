package org.nrg.dicomtools.filters;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;

public abstract class AbstractSeriesImportFilter implements SeriesImportFilter {

    public AbstractSeriesImportFilter(final String json) throws IOException {
        // TODO: For some reason, if I call the code in the getContentsAsMap() directly in here, it wants to return an Object instead of a LinkedHashMap.
        this(DicomFilterService.getContentsAsMap(json));
    }

    public AbstractSeriesImportFilter(final LinkedHashMap<String, String> values) {
        setProjectId(values.containsKey(KEY_PROJECT_ID) ? values.get(KEY_PROJECT_ID) : "");
        setEnabled(!values.containsKey(KEY_ENABLED) || Boolean.parseBoolean(values.get(KEY_ENABLED)));
        setMode(SeriesImportFilterMode.mode(values.get(KEY_MODE)));
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
        return Arrays.asList(list.split((list.contains("\\n") ? "\\\\n+" : "\\n+")));
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

    private LinkedHashMap<String, String> toQualifiedMap(final String prefix) {
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

    private String prefix(final String prefix, final String key) {
        return prefix + StringUtils.capitalize(key);
    }

    private String _projectId;
    private boolean _enabled;
    private SeriesImportFilterMode _mode;
    private String _modality;
}
