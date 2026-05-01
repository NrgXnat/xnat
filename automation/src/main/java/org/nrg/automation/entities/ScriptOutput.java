/*
 * automation: org.nrg.automation.entities.ScriptOutput
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.entities;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("unused")
public class ScriptOutput {
    @SuppressWarnings("unused")
    public ScriptOutput() {

    }

    public enum Status {SUCCESS, ERROR, UNKNOWN, DISABLED}

    public ScriptOutput(final Object results, final String output) {
        this(results, output, "", Status.UNKNOWN);
    }

    public ScriptOutput(final Object results, final String output, final String errorOutput) {
        this(results, output, errorOutput, Status.UNKNOWN);
    }

    public ScriptOutput(final Object results, final String output, final String errorOutput, final Status status) {
        setResults(results);
        setOutput(output);
        setErrorOutput(errorOutput);
        setStatus(status);
    }

    public static ScriptOutput getDisabledOutput() {
        return new ScriptOutput(null, "", "", Status.DISABLED);
    }

    public Object getResults() {
        return _results;
    }

    public void setResults(Object results) {
        _results = results;
    }

    public String getOutput() {
        return _output;
    }

    public void setOutput(String output) {
        _output = StringUtils.trim(output);
    }

    public String getErrorOutput() {
        return _errorOutput;
    }

    public void setErrorOutput(String errorOutput) {
        _errorOutput = StringUtils.trim(errorOutput);
    }

    public Status getStatus() {
        return _status;
    }

    public void setStatus(Status status) {
        _status = status;
    }

    public String toString() {
        return _results == null ? null : _results.toString();
    }

    private Object _results;
    private String _output;
    private String _errorOutput;
    private Status _status;
}
