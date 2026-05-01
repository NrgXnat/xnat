/*
 * dicomtools: org.nrg.dicomtools.filters.ModalityMapSeriesImportFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ModalityMapSeriesImportFilter extends AbstractSeriesImportFilter {

    public ModalityMapSeriesImportFilter() throws IOException {
        super("");
        _engine = new ScriptEngineManager().getEngineByName("JavaScript");
        setMode(SeriesImportFilterMode.ModalityMap);
        setEnabled(true);
        log.debug("Creating a default instance of the {} class.", getClass().getName());
    }

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

    @SuppressWarnings("unused")
    public ModalityMapSeriesImportFilter(final String contents, final boolean enabled) {
        this(contents, "", enabled);
    }

    public ModalityMapSeriesImportFilter(final String contents, final String projectId, final boolean enabled) {
        super(projectId, SeriesImportFilterMode.ModalityMap, enabled);
        _engine      = new ScriptEngineManager().getEngineByName("JavaScript");
        _modalityMap = processModalityMap(contents);
    }

    @Override
    public void setMode(final SeriesImportFilterMode mode) {
        // TODO: This doesn't make sense in this context. You can change the mode of a regex filter, but not a modality map filter. Still, we need to call the super
        // to set this to the "immutable" value, since it can be called from the abstract base.
        log.info("Tried to set the mode of a {} instance. The mode for this implementation is immutable.", ModalityMapSeriesImportFilter.class.getName());
        super.setMode(SeriesImportFilterMode.ModalityMap);
    }

    @Override
    public String findModality(final DicomObject dicomObject) {
        return findModality(convertDicomObjectToMap(dicomObject));
    }

    @Override
    public String findModality(final Map<String, String> headers) {
        if (isExcluded(headers)) {
            return "";
        }
        for (final Map.Entry<String, String> modalityEntry : _modalityMap.entrySet()) {
            final String modality = modalityEntry.getKey();
            if (modality.equals(KEY_MODE) || modality.equals(KEY_DEFAULT_MODALITY)) {
                continue;
            }
            final String script = modalityEntry.getValue();
            if (evaluate(script, getScriptParameters(script), headers)) {
                return modality;
            }
        }
        return _modalityMap.getOrDefault(KEY_DEFAULT_MODALITY, DEFAULT_MODALITY);
    }

    @Override
    public boolean shouldIncludeDicomObject(final DicomObject dicomObject) {
        return shouldIncludeDicomObject(convertDicomObjectToMap(dicomObject), null);
    }

    @Override
    public boolean shouldIncludeDicomObject(final DicomObject dicomObject, final String targetModality) {
        return shouldIncludeDicomObject(convertDicomObjectToMap(dicomObject), targetModality);
    }

    @Override
    public boolean shouldIncludeDicomObject(final Map<String, String> headers) {
        return shouldIncludeDicomObject(headers, null);
    }

    @Override
    public boolean shouldIncludeDicomObject(final Map<String, String> headers, final String targetModality) {
        // If this filter isn't enabled, then it should always return true because it's not filtering anything out.
        if (!isEnabled()) {
            return true;
        }
        if (isExcluded(headers)) {
            return false;
        }
        final String modality = StringUtils.isBlank(targetModality) ? getModality() : targetModality;
        if (StringUtils.isNotBlank(modality)) {
            final String script = _modalityMap.get(modality);
            return evaluate(script, getScriptParameters(script), headers);
        } else {
            // If there is no specified modality, then assume it matches.
            return true;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModalityMapSeriesImportFilter)) {
            return false;
        }

        final ModalityMapSeriesImportFilter that = (ModalityMapSeriesImportFilter) o;

        return isStrict() == that.isStrict() &&
               isEnabled() == that.isEnabled() &&
               getProjectId().equals(that.getProjectId()) &&
               _modalityMap.equals(that._modalityMap);
    }

    @Override
    public int hashCode() {
        int result = _modalityMap.hashCode();
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
     *
     * @param strict Whether strict parameter representation should be active or not.
     *
     * @see #isStrict()
     */
    public void setStrict(boolean strict) {
        _strict = strict;
    }

    /**
     * Gets the modality filters as a map keyed by modality.
     *
     * @return A map of all recognized modalities and their corresponding expressions.
     */
    @SuppressWarnings("unused")
    @JsonProperty("modalities")
    public LinkedHashMap<String, String> getModalityMap() {
        return _modalityMap;
    }

    /**
     * Sets the modality filters as a map keyed by modality.
     *
     * @param modalityMap A map of all recognized modalities and their corresponding expressions.
     */
    @SuppressWarnings("unused")
    public void setModalityMap(final Map<String, String> modalityMap) {
        if (_modalityMap == null) {
            _modalityMap = new LinkedHashMap<>();
        } else {
            _modalityMap.clear();
        }
        _modalityMap.putAll(modalityMap);
    }

    /**
     * Gets the filter for the indicated modality. This method returns <b>null</b> if there is no
     * filter for that modality.
     *
     * @param modality The modality to retrieve.
     *
     * @return The filter expression associated with the indicated modality.
     */
    @JsonIgnore
    public String getModalityFilter(final String modality) {
        return _modalityMap.get(modality);
    }

    /**
     * Sets the filter for the indicated modality. If the modality already exists, the filter is replaced.
     * If the modality doesn't already exist, the filter is entered as a new filter expression.
     *
     * @param modality The modality to set.
     * @param filter   The filter to set with the modality.
     */
    @JsonIgnore
    public void setModalityFilter(final String modality, final String filter) {
        _modalityMap.put(modality, filter);
    }

    /**
     * {@inheritDoc}
     *
     * @param values The initialization values.
     */
    @Override
    protected void initialize(final Map<String, String> values) {
        setModalityMap(values);
        _modalityMap.remove(KEY_ENABLED);
        _modalityMap.remove(KEY_MODE);
        _modalityMap.remove(KEY_PROJECT_ID);
        if (values.containsKey(KEY_STRICT)) {
            setStrict(Boolean.parseBoolean(values.get(KEY_STRICT)));
            _modalityMap.remove(KEY_STRICT);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<Integer> getFilterTagsImpl() {
        return _modalityMap.values().stream().map(this::getScriptParameters).flatMap(Collection::stream).distinct().map(Tag::toTag).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     *
     * @return A map with the unique properties and values.
     */
    @Override
    protected Map<String, String> getImplementationProperties() {
        return _modalityMap;
    }

    @Override
    protected LinkedHashMap<String, String> toQualifiedMap(final String prefix) {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(prefix(prefix, KEY_MODE), getMode().getValue());
        map.put(prefix(prefix, KEY_ENABLED), Boolean.toString(isEnabled()));
        if (StringUtils.isNotBlank(getProjectId())) {
            map.put(prefix(prefix, KEY_PROJECT_ID), getProjectId());
        }
        final StringBuilder list = new StringBuilder();
        for (final String key : _modalityMap.keySet()) {
            list.append(key).append(": ").append(_modalityMap.get(key)).append("\n");
        }
        map.put(prefix(prefix, "list"), list.toString());
        return map;
    }

    private LinkedHashMap<String, String> processModalityMap(final String contents) {
        final List<String>                  filters = AbstractSeriesImportFilter.parsePersistedFilters(contents);
        final LinkedHashMap<String, String> map     = new LinkedHashMap<>(filters.size());
        for (final String filter : filters) {
            final String[] atoms = filter.split(":", 2);
            if (!isReservedKey(atoms[0])) {
                map.put(atoms[0], atoms[1]);
            }
        }
        return map;
    }

    private boolean isReservedKey(final String key) {
        return StringUtils.isNotBlank(key) && RESERVED_KEYS.contains(key.toLowerCase());
    }

    private Map<String, String> getDicomHeadersAsMap(final Set<String> parameters, final DicomObject dicomObject) {
        final Map<String, String> values = new HashMap<>();
        for (final String parameter : parameters) {
            final int tag = Tag.forName(parameter);
            if (dicomObject.contains(tag)) {
                final DicomElement element = dicomObject.get(tag);
                if (!element.hasItems()) {
                    final String value = element.getValueAsString(dicomObject.getSpecificCharacterSet(), 0);
                    if (StringUtils.isNotBlank(value)) {
                        values.put(parameter, value);
                    }
                } else {
                    if (isStrict()) {
                        throw new RuntimeException("You are trying to run a script, but the specified DICOM header " + parameter + " specifies a sequence or embedded DICOM object.");
                    }
                }
            } else {
                values.put(parameter, "");
            }
        }
        return values;
    }

    private boolean evaluate(final String script, final Set<String> parameters, final Map<String, String> values) {
        final String processed = instantiate(script, parameters, values);
        log.debug("Processing script: {}", processed);
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

    @SuppressWarnings("unused")
    private boolean isExcluded(final DicomObject dicomObject) {
        return _modalityMap.containsKey(KEY_EXCLUDE) && isExcluded(getDicomHeadersAsMap(getScriptParameters(_modalityMap.get(KEY_EXCLUDE)), dicomObject));
    }

    private boolean isExcluded(final Map<String, String> headers) {
        if (_modalityMap.containsKey(KEY_EXCLUDE)) {
            final String script = _modalityMap.get(KEY_EXCLUDE);
            return evaluate(script, getScriptParameters(script), headers);
        }
        return false;
    }

    private Set<String> getScriptParameters(final String script) {
        final Matcher     matcher    = PATTERN_SCRIPT_PARAMETERS.matcher(script);
        final Set<String> parameters = new HashSet<>();
        while (matcher.find()) {
            parameters.add(matcher.group(1));
        }
        return parameters;
    }

    private String instantiate(final String script, final Set<String> parameters, final Map<String, String> values) {
        final Set<String> missing = checkParameters(parameters, values);
        if (missing.size() > 0) {
            if (isStrict()) {
                throw new RuntimeException("You are trying to run a script, but are missing values for the following parameters: " + String.join(", ", missing));
            }
            for (final String blank : missing) {
                values.put(blank, "");
            }
        }

        final Pattern pattern = Pattern.compile("#(" + StringUtils.join(parameters, "|") + ")#");
        final Matcher matcher = pattern.matcher(script);

        final StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, values.get(matcher.group(1)));
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    private Set<String> checkParameters(final Set<String> parameters, final Map<String, String> values) {
        final Set<String> missing = new HashSet<>();
        for (final String parameter : parameters) {
            if (!values.containsKey(parameter)) {
                missing.add(parameter);
            }
        }
        return missing;
    }

    private static final Pattern      PATTERN_SCRIPT_PARAMETERS = Pattern.compile("#(.*?)#");
    private static final List<String> RESERVED_KEYS             = Arrays.asList(KEY_ENABLED, KEY_STRICT, KEY_PROJECT_ID, KEY_DEFAULT_MODALITY, KEY_MODE);

    private final ScriptEngine _engine;

    private LinkedHashMap<String, String> _modalityMap;

    private boolean _strict = false;
}
