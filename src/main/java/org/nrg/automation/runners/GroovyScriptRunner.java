package org.nrg.automation.runners;

import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;

@Component
public class GroovyScriptRunner extends AbstractScriptRunner {

    @Override
    public String getLanguage() {
        return "groovy";
    }

    @Override
    public String getLanguageVersion() {
        return "2.3.6";
    }

    @Override
    public ScriptEngine getEngine() {
        if (_engine == null) {
            synchronized (_log) {
                _engine = new GroovyScriptEngineFactory().getScriptEngine();
            }
        }
        return _engine;
    }

    private static final Logger _log = LoggerFactory.getLogger(GroovyScriptRunner.class);
    private ScriptEngine _engine;
}
