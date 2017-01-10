/*
 * automation: org.nrg.automation.runners.AbstractScriptRunner
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.runners;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.nrg.automation.annotations.Supports;
import org.nrg.automation.entities.ScriptOutput;
import org.nrg.automation.services.ScriptProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractScriptRunner implements ScriptRunner {

    @Override
    public String getLanguage() {
        return getClass().getAnnotation(Supports.class).value();
    }

    @Override
    public ScriptOutput run(final Map<String, Object> properties) {
    	return run(properties, true);
    }
    
    @Override
    public ScriptOutput run(final Map<String, Object> properties, boolean exceptionOnError) {
        final String scriptId = (String) properties.get(ScriptProperty.ScriptId.key());
        if (_log.isDebugEnabled()) {
            _log.debug("Running script {} with engine ", scriptId, getEngine().getClass().getName());
        }

        Bindings bindings = getEngine().createBindings();
        for (final String key : properties.keySet()) {
            bindings.put(key, properties.get(key));
        }
        
        if (_console == null) {
             _console = new StringWriter();
        }
        if (_errorConsole == null) {
            _errorConsole = new StringWriter();
        }

        ScriptContext context = new SimpleScriptContext();
         
        context.setWriter(_console);
        context.setErrorWriter(_errorConsole);
        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        try {

            final CompiledScript script = getScript(properties);
            final Object result = script.eval(context);

            if (_log.isDebugEnabled()) {
                if (result == null) {
                    _log.debug("Got a null results object running script: " + scriptId);
                } else {
                    _log.debug("Ran script " + scriptId + ", got a results object of type: " + result.getClass().getName());
                    _log.debug("A simple toString yields: " + result.toString());
                }
            }
            _console.flush();
            _errorConsole.flush();
            return new ScriptOutput(result, _console.toString(), _errorConsole.toString(), ScriptOutput.Status.SUCCESS);
            
        } catch (Throwable e) {
        	
            final String message = "Found an error while running a " + properties.get(ScriptProperty.Language.key()) + " " + properties.get(ScriptProperty.LanguageVersion.key()) + " script with ID " + scriptId;
            _log.error(message, e);
            if (exceptionOnError) {
   	          	throw new RuntimeException(message, e);
           	} else {
            	try {	
       	           	_console.flush();
     	          	_errorConsole.flush();
       	           	return new ScriptOutput(e, _console.toString(), _errorConsole.toString(), ScriptOutput.Status.ERROR);
           	    } catch (Exception e2) {
       	          	throw new RuntimeException(message, e);
           	    }
           	}
            
        }
        
    }

    @Override
    @JsonIgnore
    public Writer getConsole() {
        return _console;
    }

    @Override
    public void setConsole(final Writer console) {
        _console = console;
    }

    @Override
    @JsonIgnore
    public Writer getErrorConsole() {
        return _errorConsole;
    }

    @Override
    public void setErrorConsole(final Writer errorConsole) {
        _errorConsole = errorConsole;
    }

    @Override
    @JsonIgnore
    public ScriptEngine getEngine() {
        if (_engine == null) {
            synchronized (_log) {
                _engine = getEngineByName();
            }
        }
        return _engine;
    }

    /**
     * This gets the engine associated with the name corresponding to the value returned by the
     * {@link #getLanguage()} method in the concrete class implementation. This should not be
     * invoked directly, but only called through the {@link #getEngine()} method, which provides
     * protection from concurrent writes. This method is provided for cases where the concrete
     * implementing class needs to retrieve its particular implementation of the {@link ScriptEngine}
     * interface in a non-standard way.
     * @return An instance of the script engine.
     */
    protected ScriptEngine getEngineByName() {
        return new ScriptEngineManager().getEngineByName(getLanguage());
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

    private ScriptEngine _engine;
    private Map<String, CompiledScript> _scripts = new HashMap<>();
    private Map<String, Integer> _sources = new HashMap<>();
    private Writer _console;
    private Writer _errorConsole;
}
