package org.nrg.dicomtools.filters;

import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModalityMapSeriesImportFilter extends AbstractSeriesImportFilter {

    public ModalityMapSeriesImportFilter(final String json) throws IOException {
        super(json);
        _engine = new ScriptEngineManager().getEngineByName("JavaScript");
        setMode(SeriesImportFilterMode.ModalityMap);
    }

    public ModalityMapSeriesImportFilter(final LinkedHashMap<String, String> values) {
        super(values);
        _engine = new ScriptEngineManager().getEngineByName("JavaScript");
        setMode(SeriesImportFilterMode.ModalityMap);
    }

    public ModalityMapSeriesImportFilter(final String contents, final boolean enabled) {
        this(contents, "", enabled);
    }

    public ModalityMapSeriesImportFilter(final String contents, final String projectId, final boolean enabled) {
        super(projectId, SeriesImportFilterMode.ModalityMap, enabled);
        _engine = new ScriptEngineManager().getEngineByName("JavaScript");
        _filterProperties = processModalityMap(contents);
    }

    /**
     * {@inheritDoc}
     * @param values    The initialization values.
     */
    @Override
    protected void initialize(final Map<String, String> values) {
        _filterProperties = new LinkedHashMap<>();
        _filterProperties.putAll(values);
        _filterProperties.remove(KEY_ENABLED);
        _filterProperties.remove(KEY_MODE);
        _filterProperties.remove(KEY_PROJECT_ID);
        if (values.containsKey(KEY_STRICT)) {
            setStrict(Boolean.parseBoolean(values.get(KEY_STRICT)));
            _filterProperties.remove(KEY_STRICT);
        }
    }

    /**
     * {@inheritDoc}
     * @return A map with the unique properties and values.
     */
    @Override
    protected Map<String, String> getImplementationProperties() {
        return _filterProperties;
    }

    @Override
    public void setMode(final SeriesImportFilterMode mode) {
        // TODO: This doesn't make sense in this context. You can change the mode of a regex filter, but not a modality map filter. Still, we need to call the super
        // to set this to the "immutable" value, since it can be called from the abstract base.
        _log.info("Tried to set the mode of a " + ModalityMapSeriesImportFilter.class.getName() + " instance. The mode for this implementation is immutable.");
        super.setMode(SeriesImportFilterMode.ModalityMap);
    }

    @Override
    public String findModality(final DicomObject dicomObject) {
        if (isExcluded(dicomObject)) {
            return "";
        }
        for(final Map.Entry<String, String> modalityEntry : _filterProperties.entrySet()) {
            final String modality = modalityEntry.getKey();
            if(modality.equals(KEY_MODE) || modality.equals(KEY_DEFAULT_MODALITY)) {
                continue;
            }
            if (evaluate(modalityEntry.getValue(), dicomObject)) {
                return modality;
            }
        }
        return _filterProperties.containsKey(KEY_DEFAULT_MODALITY) ? _filterProperties.get(KEY_DEFAULT_MODALITY) : null;
    }

    @Override
    public String findModality(final Map<String, String> headers) {
        if (isExcluded(headers)) {
            return "";
        }
        for(final Map.Entry<String, String> modalityEntry : _filterProperties.entrySet()) {
            final String modality = modalityEntry.getKey();
            if(modality.equals(KEY_MODE) || modality.equals(KEY_DEFAULT_MODALITY)) {
                continue;
            }
            if (evaluate(modalityEntry.getValue(), headers)) {
                return modality;
            }
        }
        return _filterProperties.containsKey(KEY_DEFAULT_MODALITY) ? _filterProperties.get(KEY_DEFAULT_MODALITY) : null;
    }

    @Override
    public boolean shouldIncludeDicomObject(final DicomObject dicomObject) {
        return shouldIncludeDicomObject(dicomObject, null);
    }

    @Override
    public boolean shouldIncludeDicomObject(final DicomObject dicomObject, final String targetModality) {
        return !isExcluded(dicomObject) && evaluate(_filterProperties.get(StringUtils.isBlank(targetModality) ? getModality() : targetModality), dicomObject);
    }

    @Override
    public boolean shouldIncludeDicomObject(final Map<String, String> headers) {
        return shouldIncludeDicomObject(headers, null);
    }

    @Override
    public boolean shouldIncludeDicomObject(final Map<String, String> headers, final String targetModality) {
        return !isExcluded(headers) && evaluate(_filterProperties.get(StringUtils.isBlank(targetModality) ? getModality() : targetModality), headers);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ModalityMapSeriesImportFilter)) return false;

        final ModalityMapSeriesImportFilter that = (ModalityMapSeriesImportFilter) o;

        return isStrict() == that.isStrict() &&
               isEnabled() == that.isEnabled() &&
               getProjectId().equals(that.getProjectId()) &&
               _filterProperties.equals(that._filterProperties);
    }

    @Override
    public int hashCode() {
        int result = _filterProperties.hashCode();
        result = 31 * result + (isStrict() ? 1 : 0);
        result = 31 * result + (isEnabled() ? 1 : 0);
        result = 31 * result + getProjectId().hashCode();
        return result;
    }

    /**
     * Indicates whether the filter has strict parameter representation requirements. When strict parameter representation is active,
     * any parameters referenced in a script <i>must</i> be present in the submitted {@link DicomObject DICOM object} or value map.
     * If any values are missing during evaluation, an exception will be thrown. If strict parameter representation is inactive, any
     * missing parameter values are simply represented as blanks.
     *
     * <b>Note:</b> Strict parameter representation requirements are <b>inactive</b> by default. It can be turned on in persisted
     * filter definitions by including the {@link #KEY_STRICT "strict"} parameter in the definition and setting it to "true".
     *
     * @return <b>true</b> if strict parameter representation is active, <b>false</b> otherwise.
     */
    public boolean isStrict() {
        return _strict;
    }

    /**
     * Sets the filter's strict parameter representation setting.
     * @param strict    Whether strict parameter representation should be active or not.
     * @see #isStrict()
     */
    public void setStrict(boolean strict) {
        _strict = strict;
    }

    private LinkedHashMap<String, String> processModalityMap(final String contents) {
        final List<String> filters = AbstractSeriesImportFilter.parsePersistedFilters(contents);
        final LinkedHashMap<String, String> map = new LinkedHashMap<>(filters.size());
        for (final String filter : filters) {
            final String[] atoms = filter.split(":", 2);
            map.put(atoms[0], atoms[1]);
        }
        return map;
    }

    private boolean evaluate(final String script, final DicomObject dicomObject) {
        final Set<String> parameters = getScriptParameters(script);
        final Map<String, String> values = new HashMap<>();
        for (final String parameter : parameters) {
            values.put(parameter, dicomObject.getString(Tag.forName(parameter))); // TODO: Need to figure out how to handle non-string tags.
        }
        return evaluate(script, parameters, values);
    }

    private boolean evaluate(final String script, final Map<String, String> values) {
        return evaluate(script, getScriptParameters(script), values);
    }

    private boolean evaluate(final String script, final Set<String> parameters, final Map<String, String> values) {
        final String processed = instantiate(script, parameters, values);
        if (_log.isDebugEnabled()) {
            _log.debug("Processing script: " + processed);
        }
        final Object _return;
        try {
            _return = _engine.eval(processed);
        } catch (ScriptException e) {
            throw new RuntimeException("An error occurred trying to process the DICOM object", e);
        }
        if (_return instanceof Boolean) {
            return (boolean) _return;
        }
        throw new RuntimeException("Your script did not return a boolean value, but instead returned a " + (_return != null ? _return.getClass().getName() : "null") + ". The processed script was: " + processed);
    }

    private boolean isExcluded(final DicomObject dicomObject) {
        return _filterProperties.containsKey(KEY_EXCLUDED) && evaluate(_filterProperties.get(KEY_EXCLUDED), dicomObject);
    }

    private boolean isExcluded(final Map<String, String> headers) {
        return _filterProperties.containsKey(KEY_EXCLUDED) && evaluate(_filterProperties.get(KEY_EXCLUDED), headers);
    }

    private Set<String> getScriptParameters(final String script) {
        final Matcher m = PATTERN_SCRIPT_PARAMETERS.matcher(script);
        final Set<String> parameters = new HashSet<>();
        while(m.find()) {
            parameters.add(m.group(1));
        }
        return parameters;
    }

    private String instantiate(final String script, final Set<String> parameters, final Map<String, String> values) {
        final Set<String> missing = checkParameters(parameters, values);
        if (missing.size() > 0) {
            if (isStrict()) {
                throw new RuntimeException("You are trying to run a script, but are missing values for the following parameters: " + Joiner.on(", ").join(missing));
            }
            for (final String blank : missing) {
                values.put(blank, "");
            }
        }

        final Pattern pattern = Pattern.compile("#(" + StringUtils.join(parameters, "|") + ")#");
        final Matcher matcher = pattern.matcher(script);

        final StringBuffer buffer = new StringBuffer();
        while(matcher.find()) {
            matcher.appendReplacement(buffer, values.get(matcher.group(1)));
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    private Set<String> checkParameters(final Set<String> parameters, final Map<String, String> values) {
        final Set<String> missing = new HashSet<>();
        for (final String parameter : parameters) {
            if(!values.containsKey(parameter)) {
                missing.add(parameter);
            }
        }
        return missing;
    }

    private static final Logger _log = LoggerFactory.getLogger(ModalityMapSeriesImportFilter.class);
    private static final Pattern PATTERN_SCRIPT_PARAMETERS = Pattern.compile("#(.*?)#");
    private final ScriptEngine _engine;
    private LinkedHashMap<String, String> _filterProperties;
    private boolean _strict = false;
}
