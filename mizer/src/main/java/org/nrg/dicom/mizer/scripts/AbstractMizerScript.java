/*
 * mizer: org.nrg.dicom.mizer.scripts.AnonScriptAbstractBase
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.scripts;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import org.apache.commons.lang3.StringUtils;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.service.VersionString;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract base implementation of MizerScript.
 *
 * This implementation stores the script as a list of String _statements.
 */
public abstract class AbstractMizerScript implements MizerScript {
    public AbstractMizerScript() {
        _log.debug("Creating a Mizer script instance");
    }

    public AbstractMizerScript(final String script) throws MizerException {
        this(new StringReader(script));
    }

    public AbstractMizerScript(final InputStream input) throws MizerException {
        this(new InputStreamReader(input));
    }

    public AbstractMizerScript(final List<String> statements) throws MizerException {
        this();
        _statements.addAll(statements);
    }

    public AbstractMizerScript(final Reader reader) throws MizerException {
        this();
        try {
            _statements.addAll(CharStreams.readLines(reader));
        } catch (IOException e) {
            throw new MizerException(e);
        }
    }

    @Override
    public Set<TagPath> getReferencedTags() {
        if (!_initialized) {
            initialize();
        }
        return _referencedTags;
    }

    @Override
    public Map<String, Variable> getVariables() {
        if (!_initialized) {
            initialize();
        }
        return ImmutableMap.copyOf(_variables);
    }

    @Override
    public Map<String, Variable> getExternalVariables() {
        if (!_initialized) {
            initialize();
        }
        return ImmutableMap.copyOf(_externalVariables);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VersionString getVersionString() {
        if (_versionString == null) {
            _versionString = parseVersionString(_statements);
        }
        return _versionString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream() throws UnsupportedEncodingException {
        return new ByteArrayInputStream(asString().getBytes("UTF-8"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asString() {
        return _statements.stream()
                .map(this::mustEndWithNewline)
                .collect(Collectors.joining(""));
    }

    private String mustEndWithNewline(String line) {
        return line == null ? "\n" : StringUtils.appendIfMissing(line, "\n");
    }

    public List<String> getStatements() {
        return _statements;
    }

    public void addStatement( String statement) {
        _statements.add(statement);
    }

    public void addStatements( List<String> statements) {
        _statements.addAll( statements);
    }

    /**
     * Parse the version string out of the script file.
     *
     * This method is called on the List of string _statements.
     * Assumes the script file contains a line with format 'version "some-string"'. The version string is some-string
     * with the double-quotes removed.
     *
     * @return the version string or the empty string if it is not found.
     */
    public static VersionString parseVersionString(final List<String> statements) {
        try {
            for (final String statement : statements) {
                if (statement.startsWith("version")) {
                    final String[] tokens = statement.split("\\s");
                    return new VersionString(tokens[1].replaceAll("\"", ""));
                }
            }
        } catch (IllegalArgumentException e) {
            _log.error(e.getMessage());
        }
        return null;
    }

    protected abstract void initialize();

    protected void setEntities(final Map<String, Variable> variables, final Map<String, Variable> externalVariables, final Set<TagPath> referencedTags) {
        _variables.putAll(variables);
        _externalVariables.putAll(externalVariables);
        _referencedTags.addAll( referencedTags);
        _initialized = true;
    }

    private static final Logger _log = LoggerFactory.getLogger(AbstractMizerScript.class);

    private final List<String>          _statements        = Lists.newArrayList();
    private final Set<TagPath>          _referencedTags    = Sets.newHashSet();
    private final Map<String, Variable> _variables         = Maps.newHashMap();
    private final Map<String, Variable> _externalVariables = Maps.newHashMap();

    private VersionString _versionString;
    private boolean       _initialized;
}
