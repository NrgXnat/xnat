package org.nrg.automation.runners;

import java.util.Map;

public interface ScriptRunner {

    public static final String DEFAULT_LANGUAGE = "groovy";
    public static final String DEFAULT_VERSION = "2.3.6";

    public abstract String getLanguage();

    public abstract String getLanguageVersion();

    public abstract Object run(final Map<String, Object> parameters);
}
