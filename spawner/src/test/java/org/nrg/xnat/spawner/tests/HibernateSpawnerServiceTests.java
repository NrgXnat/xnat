/*
 * spawner: org.nrg.xnat.spawner.tests.HibernateSpawnerServiceTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.tests;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xnat.spawner.configuration.HibernateSpawnerServiceTestConfiguration;
import org.nrg.xnat.spawner.entities.SpawnerElement;
import org.nrg.xnat.spawner.exceptions.CircularReferenceException;
import org.nrg.xnat.spawner.exceptions.InvalidElementIdException;
import org.nrg.xnat.spawner.preferences.SpawnerPreferences;
import org.nrg.xnat.spawner.services.SpawnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = HibernateSpawnerServiceTestConfiguration.class)
public class HibernateSpawnerServiceTests {
    @Autowired
    public void setSpawnerService(final SpawnerService service) {
        _service = service;
    }

    @Autowired
    public void setSpawnerPreferences(final SpawnerPreferences preferences) {
        _preferences = preferences;
    }

    @Before
    public void initialize() {
        _service.initialize();
    }

    @Test
    public void testSpawnerElementInitialization() throws InvalidElementIdException, CircularReferenceException {
        final List<String> namespaces = _service.getNamespaces();
        assertThat(namespaces).isNotNull().isNotEmpty().hasSize(2).containsExactlyInAnyOrder(SpawnerElement.DEFAULT_NAMESPACE, "siteAdmin");

        final List<String> defaultElementIds = _service.getDefaultElementIds();
        assertThat(defaultElementIds).isNotNull().isNotEmpty().hasSize(DEFAULT_ELEMENT_IDS.size())
                                     .containsExactlyInAnyOrderElementsOf(DEFAULT_ELEMENT_IDS);

        final List<String> namespaceDefaultElementIds   = _service.getNamespacedElementIds(SpawnerElement.DEFAULT_NAMESPACE);
        final List<String> namespaceSiteAdminElementIds = _service.getNamespacedElementIds("siteAdmin");

        assertThat(namespaceDefaultElementIds).containsExactlyInAnyOrderElementsOf(defaultElementIds);
        assertThat(namespaceSiteAdminElementIds).containsExactlyInAnyOrderElementsOf(namespaceDefaultElementIds);

        final SpawnerElement defaultSiteAdminElement            = _service.retrieve("siteAdmin");
        final SpawnerElement namespaceDefaultSiteAdminElement   = _service.retrieve(SpawnerElement.DEFAULT_NAMESPACE, "siteAdmin");
        final SpawnerElement namespaceSiteAdminSiteAdminElement = _service.retrieve("siteAdmin", "siteAdmin");

        assertThat(defaultSiteAdminElement).isNotNull().isEqualTo(namespaceDefaultSiteAdminElement)
                                           .hasFieldOrPropertyWithValue("namespace", SpawnerElement.DEFAULT_NAMESPACE)
                                           .hasFieldOrPropertyWithValue("label", "Administer XNAT")
                                           .hasFieldOrPropertyWithValue("description", "Administrative functions to configuring and managing XNAT.");

        assertThat(namespaceDefaultSiteAdminElement).isNotNull().isNotEqualTo(namespaceSiteAdminSiteAdminElement)
                                                    .hasFieldOrPropertyWithValue("namespace", SpawnerElement.DEFAULT_NAMESPACE)
                                                    .hasFieldOrPropertyWithValue("label", "Administer XNAT")
                                                    .hasFieldOrPropertyWithValue("description", "Administrative functions to configuring and managing XNAT.");

        assertThat(namespaceSiteAdminSiteAdminElement).isNotNull().isNotEqualTo(defaultSiteAdminElement)
                                                      .hasFieldOrPropertyWithValue("namespace", "siteAdmin")
                                                      .hasFieldOrPropertyWithValue("label", "Administer XNAT")
                                                      .hasFieldOrPropertyWithValue("description", "Administrative functions to configuring and managing XNAT.");

        final String defaultSiteAdminYaml = _service.resolve("siteAdmin");
        assertThat(defaultSiteAdminYaml).isNotNull().isInstanceOf(String.class).isEqualToIgnoringWhitespace(SITE_ADMIN_YAML);

        final String namespacedSiteAdminYaml = _service.resolve("siteAdmin", "siteAdmin");
        assertThat(namespacedSiteAdminYaml).isNotNull().isInstanceOf(String.class).isEqualToIgnoringWhitespace(SITE_ADMIN_YAML);
    }

    @Test
    public void testSpawnerPreferences() {
        assertThat(_preferences.getPurgeAndRefreshOnStartup()).isTrue();
        _preferences.setPurgeAndRefreshOnStartup(false);
        assertThat(_preferences.getPurgeAndRefreshOnStartup()).isFalse();
        _preferences.setPurgeAndRefreshOnStartup(true);
        assertThat(_preferences.getPurgeAndRefreshOnStartup()).isTrue();
    }

    private static final String       SITE_ADMIN_YAML     = "siteAdmin:\n" + "    label: Administer XNAT\n" + "    description: Administrative functions to configuring and managing XNAT.\n" + "    type: page\n" + "    contains: tabs\n" + "    contents:\n" + "        xnatSetup:\n" + "            label: XNAT Setup\n" + "            type: tabGroup\n" + "            tabs:\n" + "                siteSetup:\n" + "                    label: Site Setup\n" + "                    type: tab\n" + "                    contents:\n" + "                        siteInfo:\n" + "                            label: Site Information\n" + "                            type: panel\n" + "                            controls:\n" + "                               siteId:\n" + "                                   label: Site ID\n" + "                                   type: text\n" + "                                   id: site-id\n" + "                                   description: Identifies your XNAT site.\n" + "                                   placeholder: Enter your XNAT site ID...\n" + "                                   url: /xapi/services/prefs/siteId/{siteId}\n" + "                                   default: XNAT\n" + "                                   validation:\n" + "                                       required: true\n" + "                                       type: xnat-id\n" + "                               siteUrl:\n" + "                                   label: Site Url\n" + "                                   type: url\n" + "                                   description: The root URL for the site. This is passed to external services.\n" + "                                   placeholder: Enter the URL for your XNAT site...\n" + "                                   value: https://cnda.wustl.edu\n" + "                                   validation:\n" + "                                       required: true\n" + "                               siteDescription:\n" + "                                   label: Site Description\n" + "                                   type: composite\n" + "                                   selection: radio\n" + "                                   url: /data/services/prefs/{desc}\n" + "                                   children:\n" + "                                       siteDescriptionPage:\n" + "                                           id: desc-page\n" + "                                           label: Page\n" + "                                           type: site.path\n" + "                                           description: The page to display for the site description, e.g. /screens/site_description.vm.\n" + "                                           value: /screens/site_description.vm\n" + "                                           placeholder: Enter a page for your site description...\n" + "                                           validation:\n" + "                                               required: true\n" + "                                       siteDescriptionMarkdown:\n" + "                                           id: desc-markdown\n" + "                                           label: Text (Markdown)\n" + "                                           type: markdown\n" + "                                           tooltip: XNAT allows you to use GitHub-flavored Markdown to create and format your own site description. [&gt;&gt; View Tutorial](http://foobar)\n" + "                                           description: Compose a site description without referencing a template or site page.\n" + "                                           validation:\n" + "                                               required: true\n" + "                               landingPage:\n" + "                                   label: Landing Page\n" + "                                   type: site.path\n" + "                                   description: The page to display when the user logs in.\n" + "                                   value: /screens/QuickSearch.vm\n" + "                                   placeholder: Enter the default landing page...\n" + "                                   overrides:\n" + "                                       target: homePage\n" + "                                       type: checkbox\n" + "                                       position: right\n" + "                                       description: Use this as my home page.\n" + "                                       hideTarget: false\n" + "                                   validation:\n" + "                                       required: true\n" + "                               homePage:\n" + "                                   label: Home Page\n" + "                                   type: site.path\n" + "                                   description: The page to display when the user clicks the home link.\n" + "                                   value: /screens/AdminUsers.vm\n" + "                                   placeholder: Enter the default home page...\n" + "                                   validation:\n" + "                                       required: true\n" + "                        adminInfo:\n" + "                            label: Admin Information\n" + "                            type: panel\n" + "                            controls:\n" + "                                siteAdminEmail:\n" + "                                    label: Site Admin Email\n" + "                                    type: email\n" + "                                    placeholder: Administrator email address...\n" + "                                    url: /xapi/services/prefs/siteAdminEmail/{siteAdminEmail}\n" + "                                    validation:\n" + "                                        required: true\n" + "\n";
    private static final List<String> DEFAULT_ELEMENT_IDS = Arrays.asList("adminInfo", "homePage", "landingPage", "siteAdmin", "siteAdminEmail", "siteDescription", "siteDescriptionMarkdown", "siteDescriptionPage", "siteId", "siteInfo", "siteSetup", "siteUrl", "xnatSetup");

    private SpawnerService     _service;
    private SpawnerPreferences _preferences;
}
