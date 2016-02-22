package org.nrg.automation.runners;

import org.nrg.automation.entities.ScriptOutput;

import javax.script.ScriptEngine;
import java.io.Writer;
import java.util.Map;

public interface ScriptRunner {

    String DEFAULT_LANGUAGE = "groovy";

    String getLanguage();

    void setConsole(Writer console);
    Writer getConsole();

    void setErrorConsole(Writer errorConsole);
    Writer getErrorConsole();

    ScriptOutput run(final Map<String, Object> parameters);
    
    ScriptOutput run(final Map<String, Object> parameters, boolean exceptionOnError);

    ScriptEngine getEngine();
}
