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

    public boolean hasSiteScript(final String scriptId);

    public boolean hasSiteScript(final String scriptId, final String path);

    public boolean hasScopedScript(final Scope scope, final String entityId, final String scriptId);

    public boolean hasScopedScript(final Scope scope, final String entityId, final String scriptId, final String path);

    public boolean hasScript(final Scope scope, final String entityId, final String scriptId);

    public boolean hasScript(final Scope scope, final String entityId, final String scriptId, final String path);

    public Properties getSiteScript(final String scriptId);

    public Properties getSiteScript(final String scriptId, final String path);

    public Properties getScopedScript(final Scope scope, final String entityId, final String scriptId);

    public Properties getScopedScript(final Scope scope, final String entityId, final String scriptId, final String path);

    public Properties getScript(final Scope scope, final String entityId, final String scriptId);

    public Properties getScript(final Scope scope, final String entityId, final String scriptId, final String path);

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

    /**
     * This attempts to run a script using the indicated scope and entity ID. If no script exists with that scope and
     * entity ID, it will fail over and try to run the script with the same script ID but with the next higher scope.
     *
     * For now, this effectively means it will try the specific scope and entity ID, then fail over to a site-wide
     * script with the indicated script ID. Full hierarchical fail-over has performance and relational concerns (e.g.
     * failing over from subject to project requires retrieving the subject and then the project).
     *
     * @param user        The user requesting to run the script.
     * @param scope       The scope for the script.
     * @param entityId    The associated entity for the script.
     * @param scriptId    The ID of the script to run.
     * @return The results of the script execution.
     */
    public Object runScript(final String user, final Scope scope, final String entityId, final String scriptId);

    /**
     * This attempts to run a script using the indicated scope and entity ID, passing along the indicated parameters. If
     * no script exists with that scope and entity ID, it will fail over and try to run the script with the same script
     * ID but with the next higher scope.
     *
     * For now, this effectively means it will try the specific scope and entity ID, then fail over to a site-wide
     * script with the indicated script ID. Full hierarchical fail-over has performance and relational concerns (e.g.
     * failing over from subject to project requires retrieving the subject and then the project).
     *
     * @param user        The user requesting to run the script.
     * @param scope       The scope for the script.
     * @param entityId    The associated entity for the script.
     * @param scriptId    The ID of the script to run.
     * @param parameters  The parameters to pass to the script.
     * @return The results of the script execution.
     */
    public Object runScript(final String user, final Scope scope, final String entityId, final String scriptId, Map<String, Object> parameters);

    /**
     * This attempts to run a script using the indicated scope and entity ID. If no script exists with that scope and
     * entity ID, it will fail over and try to run the script with the same script ID and path but with the next higher
     * scope.
     *
     * For now, this effectively means it will try the specific scope and entity ID, then fail over to a site-wide
     * script with the indicated script ID and path. Full hierarchical fail-over has performance and relational concerns
     * (e.g. failing over from subject to project requires retrieving the subject and then the project).
     *
     * @param user        The user requesting to run the script.
     * @param scope       The scope for the script.
     * @param entityId    The associated entity for the script.
     * @param scriptId    The ID of the script to run.
     * @param path        The path info of the script to run.
     * @return The results of the script execution.
     */
    public Object runScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path);

    /**
     * This attempts to run a script using the indicated scope and entity ID, passing along the indicated parameters. If
     * no script exists with that scope and entity ID, it will fail over and try to run the script with the same script
     * ID and path but with the next higher scope.
     *
     * For now, this effectively means it will try the specific scope and entity ID, then fail over to a site-wide
     * script with the indicated script ID and path. Full hierarchical fail-over has performance and relational concerns
     * (e.g. failing over from subject to project requires retrieving the subject and then the project).
     *
     * @param user        The user requesting to run the script.
     * @param scope       The scope for the script.
     * @param entityId    The associated entity for the script.
     * @param scriptId    The ID of the script to run.
     * @param path        The path info of the script to run.
     * @param parameters  The parameters to pass to the script.
     * @return The results of the script execution.
     */
    public Object runScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path, Map<String, Object> parameters);

    @Autowired
    void setRunners(List<ScriptRunner> runners);

    boolean hasRunner(String language, String version);

    ScriptRunner getRunner(String language, String version);

    void addRunner(ScriptRunner runner);

    void addRunners(List<ScriptRunner> runners);
}
