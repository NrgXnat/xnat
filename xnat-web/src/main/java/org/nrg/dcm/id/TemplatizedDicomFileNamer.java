/*
 * org.nrg.dcm.id.TemplatizedDicomFileNamer
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/13/14 5:34 PM
 */
package org.nrg.dcm.id;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.dcm.DicomFileNamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;

public class TemplatizedDicomFileNamer implements DicomFileNamer {

    private static final Pattern VARIABLE_EXTRACTION = Pattern.compile("\\$\\{([A-z0-9]+)\\}");
    private static final String SUFFIX = ".dcm";
    private static final String HASH_PREFIX = "Hash";

    private static final Logger _log = LoggerFactory.getLogger(TemplatizedDicomFileNamer.class);
    public static final String HASH_DELIMITER = "With";

    public TemplatizedDicomFileNamer(final String naming) throws Exception {
        if (_log.isDebugEnabled()) {
            _log.debug("Initializing the templatized DICOM file namer with the template: " + naming);
        }
        _naming = hasExtension(naming) ? naming : naming + SUFFIX;
        _template = initializeTemplate();
        _variables = initializeVariables();
        _hashes = initializeHashes();
        validate();
    }

    /**
     * Makes the file name for the given DICOM object based on the naming template
     * specified during namer initialization.
     * @param dicomObject    The DICOM object for which the name should be calculated.
     * @return The generated file name from the variable values extracted from the DICOM object.
     */
    public String makeFileName(DicomObject dicomObject) {
        Map<String, String> values = new HashMap<>();
        for (final String variable : _variables) {
            if (!variable.startsWith(HASH_PREFIX)) {
                final String tagValue = dicomObject.getString(Tag.forName(variable));
                values.put(variable, tagValue == null ? "no-value-for-" + variable : tagValue);
            }
        }
        return makeFileName(values);
    }

    /**
     * Makes the file name from the given variables.
     * @param values    The various extracted variable values.
     * @return The generated file name from the given variable values.
     */
    public String makeFileName(Map<String, String> values) {
        VelocityContext context = new VelocityContext();
        for (Map.Entry<String, String> value : values.entrySet()) {
            context.put(value.getKey(), value.getValue());
        }
        for (final Map.Entry<String, List<String>> hash : _hashes.entrySet()) {
            context.put(hash.getKey(), calculateHashString(hash.getValue(), values));
        }
        StringWriter writer = new StringWriter();
        try {
            _template.merge(context, writer);
        } catch (Exception exception) {
            throw new RuntimeException("Error trying to resolve naming template", exception);
        }
        return writer.toString();
    }

    /**
     * Calculate a hash for all of the values in the list of variables.
     * @param variables    The variables for which the hash should be calculated.
     * @param values       The map of all values extracted from the DICOM object.
     * @return The calculated hash.
     */
    private String calculateHashString(final List<String> variables, final Map<String,String> values) {
        final int hash = Lists.transform(variables, Functions.forMap(values)).hashCode();
        return Long.toString(hash & 0xffffffffl, 36);
    }

    /**
     * This tells you whether the template has an extension that is separated by the '.'
     * character and consists of one or more alphanumeric characters.
     * @param template  The naming template to be tested.
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
     * Initializes the template for the life of the file namer.
     *
     * @return The initialized Velocity template.
     * @throws Exception
     */
    private Template initializeTemplate() throws Exception {
        RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
        runtimeServices.init();
        StringReader reader = new StringReader(_naming);
        SimpleNode node = runtimeServices.parse(reader, "naming");
        Template template = new Template();
        template.setRuntimeServices(runtimeServices);
        template.setData(node);
        template.initDocument();
        return template;
    }

    /**
     * Extracts all of the variable names from the template.
     * @return All of the variables found in the template.
     */
    private Set<String> initializeVariables() {
        Set<String> variables = new HashSet<>();
        Matcher matcher = VARIABLE_EXTRACTION.matcher(_naming);
        while (matcher.find()) {
            final String variable = matcher.group(1);
            variables.add(variable);
        }
        return variables;
    }

    /**
     * Finds all of the variables that are notated as a hash.
     * @return All of the hashes in the template.
     */
    private Map<String, List<String>> initializeHashes() {
        Map<String, List<String>> hashes = new HashMap<>();
        Set<String> hashedVariables = new HashSet<>();
        for (final String variable : _variables) {
            if (variable.startsWith(HASH_PREFIX)) {
                if (!variable.contains(HASH_DELIMITER)) {
                    throw new RuntimeException("You can't specify a " + HASH_PREFIX + " without specifying at least two DICOM header values joined by the " + HASH_DELIMITER + " delimiter.");
                }
                List<String> variables = Arrays.asList(variable.substring(4).split(HASH_DELIMITER));
                hashes.put(variable, variables);
                hashedVariables.addAll(variables);
            }
        }
        _variables.addAll(hashedVariables);
        return hashes;
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

    private final String _naming;
    private final Template _template;
    private final Set<String> _variables;
    private final Map<String, List<String>> _hashes;
}
