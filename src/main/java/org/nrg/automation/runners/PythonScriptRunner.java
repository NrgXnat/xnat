package org.nrg.automation.runners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@Component
public class PythonScriptRunner extends AbstractScriptRunner {

    @Override
    public String getLanguage() {
        return "python";
    }

    @Override
    public String getLanguageVersion() {
        return "2.7";
    }

    @Override
    public ScriptEngine getEngine() {
        if (_engine == null) {
            synchronized (_log) {
                _engine = new ScriptEngineManager().getEngineByName("python");
            }
        }
        return _engine;
    }

    private static final Logger _log = LoggerFactory.getLogger(PythonScriptRunner.class);
    private ScriptEngine _engine;
}
