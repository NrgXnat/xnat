/*
 * web: org.nrg.dcm.id.TemplatizedDicomFileNamer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import com.google.common.collect.ArrayListMultimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.event.listeners.methods.AbstractXnatPreferenceHandlerMethod;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component("dicomFileNamer")
public class TemplatizedDicomFileNamer extends AbstractXnatPreferenceHandlerMethod implements DicomFileNamer {
    public TemplatizedDicomFileNamer(final SiteConfigPreferences siteConfigPreferences) {
        super("dicomFileNameTemplate");

        final String naming = siteConfigPreferences.getDicomFileNameTemplate();
        log.debug("Initializing the templatized DICOM file namer with the template: {}", naming);
        setNamingPattern(naming);
        validate();
    }

    /**
     * Makes the file name for the given DICOM object based on the naming template
     * specified during namer initialization.
     *
     * @param dicomObject The DICOM object for which the name should be calculated.
     *
     * @return The generated file name from the variable values extracted from the DICOM object.
     */
    @Override
    public String makeFileName(final DicomObject dicomObject) {
        final Map<String, String> values  = new HashMap<>();
        for (final String variable : _variables) {
            if (!variable.startsWith(HASH_PREFIX)) {
                final String value = StringUtils.defaultIfBlank(dicomObject.getString(Tag.forName(variable)), "no-value-for-" + variable);
                values.put(variable, value);
            }
        }
        for (final String key : _hashes.keySet()) {
            values.put(key, calculateHashString(_hashes.get(key), values));
        }
        return new StringSubstitutor(values).replace(_naming);
    }

    /**
     * Handles changes to the specified preferences.
     *
     * @param preference The preference to be handled.
     * @param value      The new value of the preference.
     */
    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        setNamingPattern(value);
    }

    /**
     * Sets the pattern for naming.
     *
     * @param naming The naming pattern.
     */
    private void setNamingPattern(final String naming) {
        _naming = hasExtension(naming) ? naming : naming + SUFFIX;
        initializeVariables();
        initializeHashes();
    }

    /**
     * Calculate a hash for all of the values in the list of variables.
     *
     * @param variables The variables for which the hash should be calculated.
     * @param values    The map of all values extracted from the DICOM object.
     *
     * @return The calculated hash.
     */
    private String calculateHashString(final List<String> variables, final Map<String, String> values) {
        final int hash = variables.stream().map(values::get).collect(Collectors.toList()).hashCode();
        return Long.toString(hash & 0xffffffffL, 36);
    }

    /**
     * This tells you whether the template has an extension that is separated by the '.'
     * character and consists of one or more alphanumeric characters.
     *
     * @param template The naming template to be tested.
     *
     * @return True if the template has an extension, false otherwise.
     */
    private boolean hasExtension(final String template) {
        if (!template.contains(".")) {
            return false;
        }
        final String lastElement = template.substring(template.lastIndexOf("."));
        return !StringUtils.isBlank(lastElement) && lastElement.matches("^\\.[A-z0-9]+");
    }

    /**
     * Extracts all of the variable names from the template.
     */
    private void initializeVariables() {
        _variables.clear();

        final Matcher matcher = VARIABLE_EXTRACTION.matcher(_naming);
        while (matcher.find()) {
            final String variable = matcher.group(1);
            _variables.add(variable);
        }
    }

    /**
     * Finds all of the variables that are embedded with hash variables. For example, a hash variable like <b>HashSOPClassUIDWithSOPInstanceUID</b>
     * would add <b>SOPClassUID</b> and <b>SOPInstanceUID</b> to the list of variables.
     */
    private void initializeHashes() {
        _hashes.clear();

        final Set<String> added = new HashSet<>();
        for (final String variable : _variables) {
            if (variable.startsWith(HASH_PREFIX)) {
                if (!variable.contains(HASH_DELIMITER)) {
                    throw new RuntimeException("You can't specify a " + HASH_PREFIX + " without specifying at least two DICOM header values joined by the " + HASH_DELIMITER + " delimiter.");
                }
                final List<String> variables = Arrays.asList(variable.substring(4).split(HASH_DELIMITER));
                _hashes.putAll(variable, variables);
                added.addAll(variables);
            }
        }
        if (!added.isEmpty()) {
            _variables.addAll(added);
        }
    }

    /**
     * Validates that all specified variables and tokens are valid.
     */
    private void validate() {
        String lastVariable = "";
        try {
            for (final String variable : _variables) {
                if (!variable.startsWith(HASH_PREFIX)) {
                    lastVariable = variable;
                    int header = Tag.forName(variable);
                    if (header == -1) {
                        throw new RuntimeException("That's not, like, a thing.");
                    }
                }
            }
        } catch (IllegalArgumentException exception) {
            throw new RuntimeException("Illegal DICOM header tag specified: " + lastVariable, exception);
        }
    }

    @SuppressWarnings("RegExpRedundantEscape")
    private static final Pattern VARIABLE_EXTRACTION = Pattern.compile("\\$\\{([A-Za-z0-9]+)\\}");
    private static final String  SUFFIX              = ".dcm";
    private static final String  HASH_PREFIX         = "Hash";
    private static final String  HASH_DELIMITER      = "With";

    private final Set<String>                       _variables = new HashSet<>();
    private final ArrayListMultimap<String, String> _hashes    = ArrayListMultimap.create();

    private String   _naming;
}