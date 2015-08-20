package org.nrg.automation.entities;

public class ScriptOutput {
    @SuppressWarnings("unused")
    public ScriptOutput() {

    }

    public ScriptOutput(final Object results, final String output) {
        _results = results;
        _output = output;
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

    public String toString() {
        return _results == null ? null : _results.toString();
    }

    private Object _results;
    private String _output;
}
