package org.nrg.automation.entities;

public class ScriptOutput {
    @SuppressWarnings("unused")
    public ScriptOutput() {

    }
    
    public enum Status { SUCCESS, ERROR, UNKNOWN };

    public ScriptOutput(final Object results, final String output, final String errorOutput, final Status status) {
        _results = results;
        _output = output;
        _errorOutput = errorOutput;
        _status = status;
    }

    public ScriptOutput(final Object results, final String output, final String errorOutput) {
        _results = results;
        _output = output;
        _errorOutput = errorOutput;
        _status = Status.UNKNOWN;
    }

    public ScriptOutput(final Object results, final String output) {
        _results = results;
        _output = output;
        _errorOutput = "";
        _status = Status.UNKNOWN;
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
        _output = output;
    }

    public String getErrorOutput() {
        return _errorOutput;
    }

    public void setErrorOutput(String errorOutput) {
        _errorOutput = errorOutput;
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
