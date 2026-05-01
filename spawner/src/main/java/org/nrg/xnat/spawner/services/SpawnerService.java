/*
 * spawner: org.nrg.xnat.spawner.services.SpawnerService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.spawner.entities.SpawnerElement;
import org.nrg.xnat.spawner.exceptions.CircularReferenceException;
import org.nrg.xnat.spawner.exceptions.InvalidElementIdException;
import org.nrg.xnat.spawner.preferences.SpawnerPreferences;
import org.springframework.beans.BeansException;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface SpawnerService extends BaseHibernateService<SpawnerElement> {
    Pattern REGEX_NEW_ELEMENT               = Pattern.compile("^([A-z0-9_-]+):[\\s]*$");
    Pattern REGEX_COMMENT                   = Pattern.compile("^[\\s]*#.*$");
    Pattern REGEX_RESOURCE_NAMESPACE        = Pattern.compile("^.*META-INF/xnat/spawner/(?<nselement>.+?)(?<suffix>-elements)?.yaml$");
    String  DEFAULT_SPAWNER_LOCATOR_PATTERN = "classpath*:META-INF/xnat/spawner/**/*.yaml";

    /**
     * Initializes the system's spawner entities, including purging the system if {@link SpawnerPreferences#getPurgeAndRefreshOnStartup()}
     * is <b>true</b>. You can force a purge by calling {@link #initialize(boolean)} with the <b>forcePurge</b>
     * parameter set to <b>true</b>.
     *
     * In previous versions of Spawner, this method was annotated with <b>@PostConstruct</b> so that it was invoked
     * automatically on start-up, but now must be explicitly invoked so that the system is only initialized once on
     * multi-node systems.
     *
     * @throws BeansException When an error occurs initializing the spawner entities.
     */
    @Transactional
    default void initialize() throws BeansException {
        initialize(false);
    }

    /**
     * Initializes the system's spawner entities, including purging the system if {@link SpawnerPreferences#getPurgeAndRefreshOnStartup()}
     * is <b>true</b> <i>or</i> if the <b>forcePurge</b> parameter is set to <b>true</b>.
     *
     * @throws BeansException When an error occurs initializing the spawner entities.
     */
    void initialize(final boolean forcePurge) throws BeansException;

    /**
     * This retrieves the ID of all available spawner namespaces defined in the system.
     *
     * @return A list containing all of the available spawner namespaces.
     */
    List<String> getNamespaces();

    /**
     * This retrieves the ID of all available spawner namespaces defined in the system and their restrictions.
     *
     * @return A list containing all of the available spawner namespaces as key and restrictions as value.
     */
    List<Object[]> getNamespacesAndResrictions();


    /**
     * Locates the element definition file for the namespace and reloads all elements defined in that element definition
     * file. This will not update or delete elements that are not in the current version of the element definition file.
     * This includes elements that were added to the namespace separately from the element definition file or, more
     * importantly, elements that were removed from the element definition file.
     *
     * @param namespace The namespace to be refreshed.
     *
     * @throws InvalidElementIdException When one of the elements in the namespace has an invalid element ID.
     */
    void refreshNamespace(final String namespace) throws InvalidElementIdException;

    /**
     * Deletes all of the elements in the indicated namespace, locates the element definition file for the namespace and
     * reloads all elements defined in that element definition file. Note the difference between this method and the
     * {@link #refreshNamespace(String)} method. This method basically combines the functions of the {@link
     * #deleteNamespace(String)} method and the {@link #refreshNamespace(String)} method.
     *
     * @param namespace The namespace to be purged and refreshed.
     *
     * @throws InvalidElementIdException When one of the elements in the namespace has an invalid element ID.
     */
    void purgeAndRefreshNamespace(final String namespace) throws InvalidElementIdException;

    /**
     * Deletes all of the elements in the indicated namespace. This is effectively deleting the namespace itself.
     *
     * @param namespace The namespace to be deleted.
     */
    void deleteNamespace(final String namespace);

    /**
     * This retrieves the ID of all available spawner elements defined for the default namespace.
     *
     * @return A list containing the IDs of the available spawner elements in the default namespace.
     */
    List<String> getDefaultElementIds();

    /**
     * This retrieves the ID of all available spawner elements defined for the indicated namespace.
     *
     * @param namespace The namespace for which elements should be retrieved.
     *
     * @return A list containing the IDs of the available spawner elements in the indicated namespace.
     */
    List<String> getNamespacedElementIds(final String namespace);

    /**
     * This retrieves all available spawner elements defined for the default namespace.
     *
     * @return A list containing all of the available spawner elements in the default namespace.
     */
    List<SpawnerElement> getDefaultElements();

    /**
     * This retrieves all available spawner elements defined for the indicated namespace.
     *
     * @param namespace The namespace for which elements should be retrieved.
     *
     * @return A list containing all of the available spawner elements for the indicated namespace.
     */
    List<SpawnerElement> getNamespacedElements(final String namespace);

    /**
     * Returns the element with the indicated ID.
     *
     * @param elementId The ID of the element to retrieve.
     *
     * @return The requested element if it exists, null otherwise.
     */
    SpawnerElement retrieve(final String elementId);

    /**
     * Returns the element with the indicated ID.
     *
     * @param namespace The namespace to search.
     * @param elementId The ID of the element to retrieve.
     *
     * @return The requested element if it exists, null otherwise.
     */
    SpawnerElement retrieve(final String namespace, final String elementId);

    /**
     * Fetches a fully resolved spawner element from the system. Fetching an element differs from simply {@link
     * #retrieve(String) retrieving} an element, since all references to dependent elements are resolved, meaning that
     * fetching an element implies fetching the requested element <em>and</em> all elements referenced by the requested
     * element <em>and</em> all elements referenced by those elements and so on, until the UI element is fully resolved.
     * Circular references result in a {@link CircularReferenceException}.
     *
     * @param elementId The ID of the element to resolve and return.
     *
     * @return The fully resolved spawner element.
     *
     * @throws InvalidElementIdException  When the ID specified for the element is invalid.
     * @throws CircularReferenceException When an element is referenced more than once within the same branch.
     */
    String resolve(final String elementId) throws CircularReferenceException, InvalidElementIdException;

    /**
     * Fetches a fully resolved spawner element from the system. Fetching an element differs from simply {@link
     * #retrieve(String) retrieving} an element, since all references to dependent elements are resolved, meaning that
     * fetching an element implies fetching the requested element <em>and</em> all elements referenced by the requested
     * element <em>and</em> all elements referenced by those elements and so on, until the UI element is fully resolved.
     * Circular references result in a {@link CircularReferenceException}.
     *
     * @param namespace The namespace to search.
     * @param elementId The ID of the element to resolve and return.
     *
     * @return The fully resolved spawner element.
     *
     * @throws InvalidElementIdException  When the ID specified for the element is invalid.
     * @throws CircularReferenceException When an element is referenced more than once within the same branch.
     */
    String resolve(final String namespace, final String elementId) throws CircularReferenceException, InvalidElementIdException;

    /**
     * <p>Creates a new {@link SpawnerElement} from the submitted text:</p>
     * <ul>
     * <li>The element ID is extracted from well-formed text by taking the first non-blank line and, if that line
     * starts with text at the first character and is terminated by the ':' character (ignoring whitespace), using
     * the text from that line. If no ID is found, the element is still parsed and set as the {@link
     * SpawnerElement#setYaml(String) YAML} for the element, but the ID is not set.</li>
     * <li>The {@link SpawnerElement#getNamespace() element's namespace property} will not be set! The namespace
     * property is used to make it easier to group and manage spawner elements in the system and are not rendered at
     * all in the element's YAML. If your element requires a namespace setting, you should set that explicitly
     * before persisting the element object, because...</li>
     * <li>The resulting object is <em>not</em> persisted! You must {@link #create(Object...) create} the object
     * explicitly if you want it saved to the system.</li>
     * </ul>
     *
     * @param text The text to be parsed as YAML.
     *
     * @return The populated {@link SpawnerElement spawner element object}.
     *
     * @throws InvalidElementIdException When the ID specified for the element is invalid.
     * @throws IOException               When there's an I/O error attempting to parse the text.
     */
    SpawnerElement parse(final String text) throws InvalidElementIdException, IOException;
}
