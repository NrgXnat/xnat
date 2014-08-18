package org.nrg.automation.runners;

import java.util.Map;

public interface ScriptRunner {
    public abstract String getLanguage();

    public abstract String getLanguageVersion();

    public abstract Object run(final Map<String, Object> parameters);
}
