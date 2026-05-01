/*
 * automation: org.nrg.automation.runners.ScriptRunner
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

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
