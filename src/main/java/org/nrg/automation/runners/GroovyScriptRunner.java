package org.nrg.automation.runners;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.nrg.automation.services.ScriptProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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
        final String scriptId = (String) properties.get(ScriptProperty.ScriptId.key());
        if (_log.isDebugEnabled()) {
            _log.debug("Running script " + scriptId);
        }
        final Binding binding = new Binding();
        for (final String key : properties.keySet()) {
            binding.setVariable(key, properties.get(key));
        }
        try {
            final Script script = getScript(properties);
            script.setBinding(binding);
            final Object result = script.run();

            if (_log.isDebugEnabled()) {
                if (result == null) {
                    _log.debug("Got a null results object running script: " + scriptId);
                } else {
                    _log.debug("Ran script " + scriptId + ", got a results object of type: " + result.getClass().getName());
                    _log.debug("A simple toString yields: " + result.toString());
                }
            }
            return result;
        } catch (Throwable e) {
            final String message = "Found an error while running a " + properties.get(ScriptProperty.Language.key()) + " " + properties.get(ScriptProperty.LanguageVersion.key()) + " script with ID " + scriptId;
            _log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    private Script getScript(final Map<String, Object> properties) {
        final String scriptId = (String) properties.get(ScriptProperty.ScriptId.key());
        final String source = (String) properties.remove(ScriptProperty.Script.key());
        if (!_scripts.containsKey(scriptId) || !_sources.containsKey(scriptId) || _sources.get(scriptId) != source.hashCode()) {
            _scripts.put(scriptId, _shell.parse(source));
            _sources.put(scriptId, source.hashCode());
        }
        return _scripts.get(scriptId);
    }

    private static final Logger _log = LoggerFactory.getLogger(GroovyScriptRunner.class);
    private static final GroovyShell _shell = new GroovyShell();
    private Map<String, Script> _scripts = new HashMap<String, Script>();
    private Map<String, Integer> _sources = new HashMap<String, Integer>();
}
