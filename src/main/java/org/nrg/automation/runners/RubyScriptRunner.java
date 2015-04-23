package org.nrg.automation.runners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@Component
public class RubyScriptRunner extends AbstractScriptRunner {
    @Override
    public String getLanguage() {
        return "ruby";
    }

    @Override
    public String getLanguageVersion() {
        return "1.7.19";
    }

    @Override
    public ScriptEngine getEngine() {
        if (_engine == null) {
            synchronized (_log) {
                _engine = new ScriptEngineManager().getEngineByName("jruby");
            }
        }
        return _engine;
    }

    private static final Logger _log = LoggerFactory.getLogger(RubyScriptRunner.class);
    private ScriptEngine _engine;
}
