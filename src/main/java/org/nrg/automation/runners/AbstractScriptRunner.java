package org.nrg.automation.runners;

import org.nrg.automation.services.ScriptProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractScriptRunner implements ScriptRunner {

    @Override
    public Object run(final Map<String, Object> properties) {
        final String scriptId = (String) properties.get(ScriptProperty.ScriptId.key());
        if (_log.isDebugEnabled()) {
            _log.debug("Running script {} with engine ", scriptId, getEngine().getClass().getName());
        }
        Bindings bindings = getEngine().createBindings();
        for (final String key : properties.keySet()) {
            bindings.put(key, properties.get(key));
        }
        try {
            final CompiledScript script = getScript(properties);
            final Object result = script.eval(bindings);

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

    protected CompiledScript getScript(final Map<String, Object> properties) throws ScriptException {
        final String scriptId = (String) properties.get(ScriptProperty.ScriptId.key());
        final String source = (String) properties.remove(ScriptProperty.Script.key());
        if (!_scripts.containsKey(scriptId) || !_sources.containsKey(scriptId) || _sources.get(scriptId) != source.hashCode()) {
            if (_log.isInfoEnabled()) {
                _log.info("Creating new entry for script ID {}:\n{}", scriptId, source);
            }
            _scripts.put(scriptId, ((Compilable) getEngine()).compile(source));
            _sources.put(scriptId, source.hashCode());
        }
        return _scripts.get(scriptId);
    }

    private static final Logger _log = LoggerFactory.getLogger(AbstractScriptRunner.class);
    private Map<String, CompiledScript> _scripts = new HashMap<>();

    private Map<String, Integer> _sources = new HashMap<>();
}
