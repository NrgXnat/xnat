package org.nrg.automation.runners;

import javax.script.ScriptEngine;
import java.util.Map;

public interface ScriptRunner {

    String DEFAULT_LANGUAGE = "groovy";
    String DEFAULT_VERSION = "2.3.6";

    String getLanguage();

    String getLanguageVersion();

    Object run(final Map<String, Object> parameters);

    ScriptEngine getEngine();
}
