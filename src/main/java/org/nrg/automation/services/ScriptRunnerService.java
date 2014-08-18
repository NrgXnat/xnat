package org.nrg.automation.services;

import org.nrg.automation.runners.ScriptRunner;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.services.NrgService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface ScriptRunnerService extends NrgService {
    public Properties getSiteScript(final String scriptId);

    public Properties getSiteScript(final String scriptId, final String path);

    public Properties getScopedScript(final Scope scope, final String entityId, final String scriptId);

    public Properties getScopedScript(final Scope scope, final String entityId, final String scriptId, final String path);

    public void setSiteScript(final String user, final String scriptId, final String script) throws ConfigServiceException;

    public void setSiteScript(final String user, final String scriptId, final String path, final String script) throws ConfigServiceException;

    public void setSiteScript(final String user, final String scriptId, final String script, final Properties properties) throws ConfigServiceException;

    public void setSiteScript(final String user, final String scriptId, final String path, final String script, final Properties properties) throws ConfigServiceException;

    public void setScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String script) throws ConfigServiceException;

    public void setScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path, final String script) throws ConfigServiceException;

    public void setScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String script, final Properties properties) throws ConfigServiceException;

    public void setScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path, final String script, final Properties properties) throws ConfigServiceException;

    public Object runSiteScript(final String user, final String scriptId);

    public Object runSiteScript(final String user, final String scriptId, Map<String, Object> parameters);

    public Object runSiteScript(final String user, final String scriptId, final String path);

    public Object runSiteScript(final String user, final String scriptId, final String path, Map<String, Object> parameters);

    public Object runScopedScript(final String user, final Scope scope, final String entityId, final String scriptId);

    public Object runScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, Map<String, Object> parameters);

    public Object runScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path);

    public Object runScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path, Map<String, Object> parameters);

    @Autowired
    void setRunners(List<ScriptRunner> runners);

    boolean hasRunner(String language, String version);

    ScriptRunner getRunner(String language, String version);

    void addRunner(ScriptRunner runner);

    void addRunners(List<ScriptRunner> runners);
}
