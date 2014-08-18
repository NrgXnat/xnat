package org.nrg.automation.runners;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.nrg.automation.services.ScriptProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GroovyScriptRunner implements ScriptRunner {

    @Override
    public String getLanguage() {
        return "groovy";
    }

    @Override
    public String getLanguageVersion() {
        return "2.3.6";
    }

    @Override
    public Object run(final Map<String, Object> properties) {
        if (_log.isDebugEnabled()) {
            _log.debug("Running script " + properties.get(ScriptProperty.ScriptId.key()));
        }
        final String script = (String) properties.remove(ScriptProperty.Script.key());
        final Binding binding = new Binding();
        for (final String key : properties.keySet()) {
            binding.setVariable(key, properties.get(key));
        }
        final GroovyShell shell = new GroovyShell(binding);
        return shell.evaluate(script);
    }

    private static final Logger _log = LoggerFactory.getLogger(GroovyScriptRunner.class);
}
