/*
 * spawner: org.nrg.xnat.spawner.services.impl.constants.TestSpawnerService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.services.impl.constants;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.nrg.framework.ajax.hibernate.HibernatePaginatedRequest;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.QueryBuilder;
import org.nrg.framework.utilities.TreeNode;
import org.nrg.xnat.spawner.components.SpawnerWorker;
import org.nrg.xnat.spawner.entities.SpawnerElement;
import org.nrg.xnat.spawner.exceptions.CircularReferenceException;
import org.nrg.xnat.spawner.exceptions.InvalidElementIdException;
import org.nrg.xnat.spawner.services.SpawnerService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class TestSpawnerService implements SpawnerService {
    public static final String SIMPLE_YAML_RESOLVED    = "This isn't really YAML, just a string with references:\n" +
                                                         " * This is from SIMPLE_YAML_ONE\n" +
                                                         " * This is from SIMPLE_YAML_TWO\n" +
                                                         " * This is from SIMPLE_YAML_THREE\n";
    public static final String SIMPLE_ID_ROOT          = "SIMPLE_ROOT";
    public static final String COMPLEX_YAML_RESOLVED   = "This isn't really YAML, just a string with references:\n" +
                                                         " * This is from COMPLEX_YAML_ONE, it includes COMPLEX_ONE_ONE, COMPLEX_ONE_TWO, and COMPLEX_ONE_THREE\n" +
                                                         " * This is from COMPLEX_YAML_TWO, it includes COMPLEX_TWO_ONE, COMPLEX_TWO_TWO, and COMPLEX_TWO_THREE\n" +
                                                         " * This is from COMPLEX_YAML_THREE, it includes COMPLEX_THREE_ONE, COMPLEX_THREE_TWO, and COMPLEX_THREE_THREE\n";
    public static final String COMPLEX_ID_ROOT         = "COMPLEX_ROOT";
    public static final String SITE_INFO_YAML_RESOLVED = "siteInfo:\n" + "    label: Site Information\n" + "    type: panel\n" + "    controls:\n" + "       siteId:\n" + "           label: Site ID\n" + "           type: text\n" + "           id: site-id\n" + "           description: Identifies your XNAT site.\n" + "           placeholder: Enter your XNAT site ID...\n" + "           url: /xapi/services/prefs/siteId/{siteId}\n" + "           default: XNAT\n" + "           validation:\n" + "               required: true\n" + "               type: xnat-id\n" + "       siteUrl:\n" + "           label: Site Url\n" + "           type: url\n" + "           description: The root URL for the site. This is passed to external services.\n" + "           placeholder: Enter the URL for your XNAT site...\n" + "           value: https://cnda.wustl.edu\n" + "           validation:\n" + "               required: true\n" + "       siteDescription:\n" + "           label: Site Description\n" + "           type: composite\n" + "           selection: radio\n" + "           url: /data/services/prefs/{desc}\n" + "           children:\n" + "               siteDescriptionPage:\n" + "                   id: desc-page\n" + "                   label: Page\n" + "                   type: site.path\n" + "                   description: The page to display for the site description, e.g. /screens/site_description.vm.\n" + "                   value: /screens/site_description.vm\n" + "                   placeholder: Enter a page for your site description...\n" + "                   validation:\n" + "                       required: true\n" + "               siteDescriptionMarkdown:\n" + "                   id: desc-markdown\n" + "                   label: Text (Markdown)\n" + "                   type: markdown\n" + "                   tooltip: XNAT allows you to use GitHub-flavored Markdown to create and format your own site description. [&gt;&gt; View Tutorial](http://foobar)\n" + "                   description: Compose a site description without referencing a template or site page.\n" + "                   validation:\n" + "                       required: true\n" + "       landingPage:\n" + "           label: Landing Page\n" + "           type: site.path\n" + "           description: The page to display when the user logs in.\n" + "           value: /screens/QuickSearch.vm\n" + "           placeholder: Enter the default landing page...\n" + "           overrides:\n" + "               target: homePage\n" + "               type: checkbox\n" + "               position: right\n" + "               description: Use this as my home page.\n" + "               hideTarget: false\n" + "           validation:\n" + "               required: true\n" + "       homePage:\n" + "           label: Home Page\n" + "           type: site.path\n" + "           description: The page to display when the user clicks the home link.\n" + "           value: /screens/AdminUsers.vm\n" + "           placeholder: Enter the default home page...\n" + "           validation:\n" + "               required: true\n";
    public static final String SITE_ADMIN_ID_SITE_INFO = "siteInfo";

    @Autowired
    public TestSpawnerService(final SpawnerWorker worker) {
        _worker = worker.spawnerService(this);
    }

    @Override
    public void initialize(final boolean forcePurge) throws BeansException {
        log.debug("Initializing the service if required... forcePurge set to {}", forcePurge);
    }

    @Override
    public List<String> getNamespaces() {
        return Collections.singletonList(SpawnerElement.DEFAULT_NAMESPACE);
    }

    @Override
    public List<Object[]> getNamespacesAndResrictions() {
        String[] row = new String[]{SpawnerElement.DEFAULT_NAMESPACE, null};
        return Collections.singletonList(row);
    }


    /**
     * Locates the element definition file for the namespace and reloads all elements defined in that element definition
     * file. This will not update or delete elements that are not in the current version of the element definition file.
     * This includes elements that were added to the namespace separately from the element definition file or, more
     * importantly, elements that were removed from the element definition file.
     *
     * @param namespace The namespace to be refreshed.
     */
    @Override
    public void refreshNamespace(final String namespace) {
        log.debug("Refreshing the namespace {}", namespace);
    }

    /**
     * Deletes all the elements in the indicated namespace, locates the element definition file for the namespace and
     * reloads all elements defined in that element definition file. Note the difference between this method and the
     * {@link #refreshNamespace(String)} method. This method basically combines the functions of the {@link
     * #deleteNamespace(String)} method and the {@link #refreshNamespace(String)} method.
     *
     * @param namespace The namespace to be purged and refreshed.
     */
    @Override
    public void purgeAndRefreshNamespace(final String namespace) {
        log.debug("Purging and refreshing the namespace {}", namespace);
    }

    /**
     * Deletes all the elements in the indicated namespace. This is effectively deleting the namespace itself.
     *
     * @param namespace The namespace to be deleted.
     */
    @Override
    public void deleteNamespace(final String namespace) {
        log.debug("Deleting the namespace {}", namespace);
    }

    @Override
    public List<String> getDefaultElementIds() {
        return CONSTANT_ELEMENT_IDS;
    }

    @Override
    public List<String> getNamespacedElementIds(final String namespace) {
        return getDefaultElementIds();
    }

    @Override
    public List<SpawnerElement> getDefaultElements() {
        return CONSTANT_ELEMENTS;
    }

    @Override
    public List<SpawnerElement> getNamespacedElements(final String namespace) {
        return getDefaultElements();
    }

    @Override
    public SpawnerElement retrieve(final String elementId) {
        return CONSTANT_ELEMENT_MAP.get(elementId);
    }

    @Override
    public SpawnerElement retrieve(final String namespace, final String elementId) {
        return retrieve(elementId);
    }

    @Override
    public String resolve(final String elementId) throws InvalidElementIdException, CircularReferenceException {
        final SpawnerElement root = retrieve(elementId);
        if (root == null) {
            return null;
        }
        final TreeNode<SpawnerElement> tree = _worker.resolve(root);
        return _worker.compose(tree);
    }

    @Override
    public String resolve(final String namespace, final String elementId) throws CircularReferenceException, InvalidElementIdException {
        return resolve(elementId);
    }

    @Override
    public SpawnerElement parse(final String yaml) throws InvalidElementIdException, IOException {
        return _worker.parse(yaml);
    }

    @Override
    public QueryBuilder<SpawnerElement> newQueryBuilder() {
        return null;
    }

    @Override
    public SpawnerElement newEntity(final Object... parameters) {
        switch (parameters.length) {
            case 0:
                return new SpawnerElement();
            case 2:
                return new SpawnerElement((String) parameters[0], (String) parameters[1]);
            case 3:
                return new SpawnerElement((String) parameters[0], (String) parameters[1], (String) parameters[2]);
            default:
                throw new NrgServiceRuntimeException("Invalid parameters for creating new Spawner element: " + Arrays.stream(parameters).map(parameter -> parameter.getClass().getName()).collect(Collectors.joining(", ")));
        }
    }

    @Override
    public SpawnerElement create(final SpawnerElement entity) {
        entity.setId(RandomUtils.nextLong());
        return entity;
    }

    @Override
    public SpawnerElement create(final Object... parameters) {
        final SpawnerElement element = newEntity(parameters);
        element.setId(RandomUtils.nextLong());
        return element;
    }

    @Override
    public SpawnerElement retrieve(final long id) {
        return null;
    }

    @Override
    public SpawnerElement get(final long id) {
        return null;
    }

    @Override
    public void update(final SpawnerElement entity) {

    }

    @Override
    public void saveOrUpdate(final SpawnerElement entity) {

    }

    @Override
    public void delete(final long id) {

    }

    @Override
    public void delete(final SpawnerElement entity) {

    }

    @Override
    public List<SpawnerElement> getAll() {
        return null;
    }

    @Override
    public List<SpawnerElement> getPaginated(final HibernatePaginatedRequest request) {
        return null;
    }

    @Override
    public List<SpawnerElement> getAllWithDisabled() {
        return null;
    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public long getCount(final String property, final Object value) {
        return 0;
    }

    @Override
    public long getCount(final Map<String, Object> properties) {
        return 0;
    }

    @Override
    public long getCountWithDisabled() {
        return 0;
    }

    @Override
    public boolean exists(final String property, final Object value) {
        return false;
    }

    @Override
    public boolean exists(final String property1, final Object value1, final String property2, final Object value2) {
        return false;
    }

    @Override
    public boolean exists(final String property1, final Object value1, final String property2, final Object value2, final String property3, final Object value3) {
        return false;
    }

    @Override
    public boolean exists(final String property1, final Object value1, final String property2, final Object value2, final String property3, final Object value3, final String property4, final Object value4) {
        return false;
    }

    @Override
    public boolean exists(final Map<String, Object> parameters) {
        return false;
    }

    @Override
    public <T> Set<T> distinct(final Class<T> columnType, final String columnName) {
        return null;
    }

    @Override
    public void refresh(final SpawnerElement entity) {

    }

    @Override
    public void refresh(final List<SpawnerElement> entities) {

    }

    @Override
    public void refresh(final boolean initialize, final SpawnerElement entity) {

    }

    @Override
    public void refresh(final boolean initialize, final List<SpawnerElement> entities) {

    }

    @Override
    public void flush() {

    }

    @Override
    public List<Number> getRevisions(final long id) {
        return null;
    }

    @Override
    public SpawnerElement getRevision(final long id, final Number revision) {
        return null;
    }

    @Override
    public String validate(final SpawnerElement entity) {
        return null;
    }

    @Override
    public boolean getInitialize() {
        return false;
    }

    @Override
    public void setInitialize(final boolean initialize) {

    }

    private static final String                      SIMPLE_ID_ONE              = "SIMPLE_ONE";
    private static final String                      SIMPLE_ID_TWO              = "SIMPLE_TWO";
    private static final String                      SIMPLE_ID_THREE            = "SIMPLE_THREE";
    private static final String                      SIMPLE_YAML_ROOT           = "This isn't really YAML, just a string with references:\n" +
                                                                                  " * ${SIMPLE_ONE}\n" +
                                                                                  " * ${SIMPLE_TWO}\n" +
                                                                                  " * ${SIMPLE_THREE}\n";
    private static final String                      SIMPLE_YAML_ONE            = "This is from SIMPLE_YAML_ONE";
    private static final String                      SIMPLE_YAML_TWO            = "This is from SIMPLE_YAML_TWO";
    private static final String                      SIMPLE_YAML_THREE          = "This is from SIMPLE_YAML_THREE";
    private static final String                      COMPLEX_ID_ONE             = "COMPLEX_ONE";
    private static final String                      COMPLEX_ID_TWO             = "COMPLEX_TWO";
    private static final String                      COMPLEX_ID_THREE           = "COMPLEX_THREE";
    private static final String                      COMPLEX_ID_ONE_ONE         = "COMPLEX_ONE_ONE";
    private static final String                      COMPLEX_ID_ONE_TWO         = "COMPLEX_ONE_TWO";
    private static final String                      COMPLEX_ID_ONE_THREE       = "COMPLEX_ONE_THREE";
    private static final String                      COMPLEX_ID_TWO_ONE         = "COMPLEX_TWO_ONE";
    private static final String                      COMPLEX_ID_TWO_TWO         = "COMPLEX_TWO_TWO";
    private static final String                      COMPLEX_ID_TWO_THREE       = "COMPLEX_TWO_THREE";
    private static final String                      COMPLEX_ID_THREE_ONE       = "COMPLEX_THREE_ONE";
    private static final String                      COMPLEX_ID_THREE_TWO       = "COMPLEX_THREE_TWO";
    private static final String                      COMPLEX_ID_THREE_THREE     = "COMPLEX_THREE_THREE";
    private static final String                      COMPLEX_YAML_ROOT          = "This isn't really YAML, just a string with references:\n" +
                                                                                  " * ${COMPLEX_ONE}\n" +
                                                                                  " * ${COMPLEX_TWO}\n" +
                                                                                  " * ${COMPLEX_THREE}\n";
    private static final String                      COMPLEX_YAML_ONE           = "This is from COMPLEX_YAML_ONE, it includes ${COMPLEX_ONE_ONE}, ${COMPLEX_ONE_TWO}, and ${COMPLEX_ONE_THREE}";
    private static final String                      COMPLEX_YAML_TWO           = "This is from COMPLEX_YAML_TWO, it includes ${COMPLEX_TWO_ONE}, ${COMPLEX_TWO_TWO}, and ${COMPLEX_TWO_THREE}";
    private static final String                      COMPLEX_YAML_THREE         = "This is from COMPLEX_YAML_THREE, it includes ${COMPLEX_THREE_ONE}, ${COMPLEX_THREE_TWO}, and ${COMPLEX_THREE_THREE}";
    private static final String                      COMPLEX_YAML_ONE_ONE       = "COMPLEX_ONE_ONE";
    private static final String                      COMPLEX_YAML_ONE_TWO       = "COMPLEX_ONE_TWO";
    private static final String                      COMPLEX_YAML_ONE_THREE     = "COMPLEX_ONE_THREE";
    private static final String                      COMPLEX_YAML_TWO_ONE       = "COMPLEX_TWO_ONE";
    private static final String                      COMPLEX_YAML_TWO_TWO       = "COMPLEX_TWO_TWO";
    private static final String                      COMPLEX_YAML_TWO_THREE     = "COMPLEX_TWO_THREE";
    private static final String                      COMPLEX_YAML_THREE_ONE     = "COMPLEX_THREE_ONE";
    private static final String                      COMPLEX_YAML_THREE_TWO     = "COMPLEX_THREE_TWO";
    private static final String                      COMPLEX_YAML_THREE_THREE   = "COMPLEX_THREE_THREE";
    private static final String                      SITE_ADMIN_ID_SITE_ID      = "siteId";
    private static final String                      SITE_ADMIN_ID_SITE_URL     = "siteUrl";
    private static final String                      SITE_ADMIN_ID_SITE_DESC    = "siteDescription";
    private static final String                      SITE_ADMIN_ID_SITE_LANDING = "landingPage";
    private static final String                      SITE_ADMIN_ID_SITE_HOME    = "homePage";
    private static final String                      SITE_ADMIN_SITE_ID         = "siteId:\n" + "    label: Site ID\n" + "    type: text\n" + "    id: site-id\n" + "    description: Identifies your XNAT site.\n" + "    placeholder: Enter your XNAT site ID...\n" + "    url: /xapi/services/prefs/siteId/{siteId}\n" + "    default: XNAT\n" + "    validation:\n" + "        required: true\n" + "        type: xnat-id\n";
    private static final String                      SITE_ADMIN_SITE_URL        = "siteUrl:\n" + "    label: Site Url\n" + "    type: url\n" + "    description: The root URL for the site. This is passed to external services.\n" + "    placeholder: Enter the URL for your XNAT site...\n" + "    value: https://cnda.wustl.edu\n" + "    validation:\n" + "        required: true\n";
    private static final String                      SITE_ADMIN_SITE_DESC       = "siteDescription:\n" + "    label: Site Description\n" + "    type: composite\n" + "    selection: radio\n" + "    url: /data/services/prefs/{desc}\n" + "    children:\n" + "        siteDescriptionPage:\n" + "            id: desc-page\n" + "            label: Page\n" + "            type: site.path\n" + "            description: The page to display for the site description, e.g. /screens/site_description.vm.\n" + "            value: /screens/site_description.vm\n" + "            placeholder: Enter a page for your site description...\n" + "            validation:\n" + "                required: true\n" + "        siteDescriptionMarkdown:\n" + "            id: desc-markdown\n" + "            label: Text (Markdown)\n" + "            type: markdown\n" + "            tooltip: XNAT allows you to use GitHub-flavored Markdown to create and format your own site description. [&gt;&gt; View Tutorial](http://foobar)\n" + "            description: Compose a site description without referencing a template or site page.\n" + "            validation:\n" + "                required: true\n";
    private static final String                      SITE_ADMIN_SITE_LANDING    = "landingPage:\n" + "    label: Landing Page\n" + "    type: site.path\n" + "    description: The page to display when the user logs in.\n" + "    value: /screens/QuickSearch.vm\n" + "    placeholder: Enter the default landing page...\n" + "    overrides:\n" + "        target: homePage\n" + "        type: checkbox\n" + "        position: right\n" + "        description: Use this as my home page.\n" + "        hideTarget: false\n" + "    validation:\n" + "        required: true\n";
    private static final String                      SITE_ADMIN_SITE_HOME       = "homePage:\n" + "    label: Home Page\n" + "    type: site.path\n" + "    description: The page to display when the user clicks the home link.\n" + "    value: /screens/AdminUsers.vm\n" + "    placeholder: Enter the default home page...\n" + "    validation:\n" + "        required: true\n";
    private static final String                      SITE_ADMIN_SITE_INFO       = "siteInfo:\n" + "    label: Site Information\n" + "    type: panel\n" + "    controls:\n" + "       ${siteId}\n" + "       ${siteUrl}\n" + "       ${siteDescription}\n" + "       ${landingPage}\n" + "       ${homePage}\n";
    private static final Map<String, SpawnerElement> CONSTANT_ELEMENT_MAP       = Stream.of(new SpawnerElement(SIMPLE_ID_ROOT, SIMPLE_YAML_ROOT),
                                                                                            new SpawnerElement(SIMPLE_ID_ONE, SIMPLE_YAML_ONE),
                                                                                            new SpawnerElement(SIMPLE_ID_TWO, SIMPLE_YAML_TWO),
                                                                                            new SpawnerElement(SIMPLE_ID_THREE, SIMPLE_YAML_THREE),
                                                                                            new SpawnerElement(COMPLEX_ID_ROOT, COMPLEX_YAML_ROOT),
                                                                                            new SpawnerElement(COMPLEX_ID_ONE, COMPLEX_YAML_ONE),
                                                                                            new SpawnerElement(COMPLEX_ID_TWO, COMPLEX_YAML_TWO),
                                                                                            new SpawnerElement(COMPLEX_ID_THREE, COMPLEX_YAML_THREE),
                                                                                            new SpawnerElement(COMPLEX_ID_ONE_ONE, COMPLEX_YAML_ONE_ONE),
                                                                                            new SpawnerElement(COMPLEX_ID_ONE_TWO, COMPLEX_YAML_ONE_TWO),
                                                                                            new SpawnerElement(COMPLEX_ID_ONE_THREE, COMPLEX_YAML_ONE_THREE),
                                                                                            new SpawnerElement(COMPLEX_ID_TWO_ONE, COMPLEX_YAML_TWO_ONE),
                                                                                            new SpawnerElement(COMPLEX_ID_TWO_TWO, COMPLEX_YAML_TWO_TWO),
                                                                                            new SpawnerElement(COMPLEX_ID_TWO_THREE, COMPLEX_YAML_TWO_THREE),
                                                                                            new SpawnerElement(COMPLEX_ID_THREE_ONE, COMPLEX_YAML_THREE_ONE),
                                                                                            new SpawnerElement(COMPLEX_ID_THREE_TWO, COMPLEX_YAML_THREE_TWO),
                                                                                            new SpawnerElement(COMPLEX_ID_THREE_THREE, COMPLEX_YAML_THREE_THREE),
                                                                                            new SpawnerElement(SITE_ADMIN_ID_SITE_ID, SITE_ADMIN_SITE_ID),
                                                                                            new SpawnerElement(SITE_ADMIN_ID_SITE_URL, SITE_ADMIN_SITE_URL),
                                                                                            new SpawnerElement(SITE_ADMIN_ID_SITE_DESC, SITE_ADMIN_SITE_DESC),
                                                                                            new SpawnerElement(SITE_ADMIN_ID_SITE_LANDING, SITE_ADMIN_SITE_LANDING),
                                                                                            new SpawnerElement(SITE_ADMIN_ID_SITE_HOME, SITE_ADMIN_SITE_HOME),
                                                                                            new SpawnerElement(SITE_ADMIN_ID_SITE_INFO, SITE_ADMIN_SITE_INFO)).collect(Collectors.toMap(SpawnerElement::getElementId, Function.identity()));
    private static final List<String>                CONSTANT_ELEMENT_IDS       = CONSTANT_ELEMENT_MAP.keySet().stream().sorted().collect(Collectors.toList());
    private static final List<SpawnerElement>        CONSTANT_ELEMENTS          = CONSTANT_ELEMENT_MAP.values().stream().sorted().collect(Collectors.toList());

    private final SpawnerWorker _worker;
}
