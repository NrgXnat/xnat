package org.nrg.automation.runners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@Component
public class JavaScriptScriptRunner extends AbstractScriptRunner {
    @Override
    public String getLanguage() {
        return "JavaScript";
    }

    @Override
    public String getLanguageVersion() {
        return "1.6";
    }

    @Override
    public ScriptEngine getEngine() {
        if (_engine == null) {
            synchronized (_log) {
                _engine = new ScriptEngineManager().getEngineByName("JavaScript");
            }
        }
        return _engine;
    }

    private static final Logger _log = LoggerFactory.getLogger(JavaScriptScriptRunner.class);
    private ScriptEngine _engine;
}
