<%@ page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ page import="org.nrg.xdat.security.helpers.UserHelper" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<%--@elvariable id="hibernateSpawnerService" type="org.nrg.xnat.spawner.services.SpawnerService"--%>

<pg:init/>
<pg:jsvars/>

<c:set var="SITE_ROOT" value="${sessionScope.siteRoot}"/>

<c:set var="projectId" value="${fn:escapeXml(not empty param.id ? param.id : (not empty param.project ? param.project : ''))}"/>

<c:set var="userCanEdit" value="false"/>

<%--<div id="breadcrumbs"></div>--%>
<c:if test="${userHelper.canEdit('xnat:subjectData/project', projectId)}">
    <c:set var="userCanEdit" value="true"/>
    <script>window.userCanEdit = true</script>
</c:if>

<c:if test="${userCanEdit == false}">
    <div class="error">No access.</div>
</c:if>

<c:if test="${userCanEdit == true}">

    <%-- if an 'id' param is passed, use its value to edit specified project data --%>
    <header id="project-settings-header" class="hidden">
        <h2 class="pull-left" style="margin:0 0 20px 0;font-size:16px;">
            Settings for <span class="project-link"></span>
        </h2>
        <div class="clearfix"></div>
    </header>

    <div id="page-body">
        <div class="pad">

            <div id="project-settings-page" class="settings-tabs">

                <%-- show an error if the project isn't specified in the query string or url hash --%>
                <div id="project-not-specified" class="error hidden">Project not specified.</div>

                <%-- show an error if the project data is not returned from the rest call --%>
                <div id="project-data-error" class="error hidden">Data for "<span class="project-id"></span>" project not found.</div>

                <div id="project-settings-tabs">
                    <div class="content-tabs xnat-tab-container hidden">

                        <div id="tabs-loading" class="message waiting hidden">Loading...</div>

                        <div class="xnat-nav-tabs side pull-left">
                                <%--<!-- ================== -->--%>
                                <%--<!-- Admin tab flippers -->--%>
                                <%--<!-- ================== -->--%>
                        </div>
                        <div class="xnat-tab-content side pull-right">
                                <%--<!-- ================== -->--%>
                                <%--<!-- Admin tab panes    -->--%>
                                <%--<!-- ================== -->--%>
                        </div>

                    </div>
                </div>

                <script>

                    function returnValue(value){ return value }

                    XNAT.app =
                        getObject(XNAT.app || {});

                    XNAT.app.pluginSettings =
                        getObject(XNAT.app.pluginSettings || {});

                    XNAT.app.pluginSettings.tabConfigs = [];

                    XNAT.app.pluginSettings.addTabConfig = function(config, method){
                        if (!config) return;
                        if (config.hasOwnProperty('projectSettings')) {
                            XNAT.app.pluginSettings.tabConfigs[method](config['projectSettings'])
                        }
                        else if (config.hasOwnProperty('root')) {
                            XNAT.app.pluginSettings.tabConfigs[method](config['root'])
                        }
                    }

                </script>

                <c:catch var="jspError">

                    <%-- any Spawner namespace ending with :projectSettings will get processed --%>

                    <c:forEach items="${hibernateSpawnerService.namespaces}" var="namespace">
                        <%-- only get 'projectSettings' items --%>
                        <script>console.log('namespace: ${namespace}')</script>
                        <c:set var="arrayMethod" value="push"/>
                        <%-- the main 'default' project settings tabs should go first, if present --%>
                        <c:if test="${namespace == 'projectSettings' || namespace == 'xnat:projectSettings'}">
                            <c:set var="arrayMethod" value="unshift"/>
                        </c:if>
                        <c:if test="${fn:endsWith(namespace, 'projectSettings')}">
                            <c:import url="/xapi/spawner/resolve/${namespace}/projectSettings" var="tabsConfig"/>
                            <c:if test="${empty tabsConfig}">
                                <%-- originally 'projectSettings' was the expected name of
                                     the root element, but now 'root' is preferred --%>
                                <script>console.log('(no "projectSettings" property; using "root")')</script>
                                <c:import url="/xapi/spawner/resolve/${namespace}/root" var="tabsConfig"/>
                            </c:if>
                            <script>
                                XNAT.app.pluginSettings.addTabConfig(returnValue(${tabsConfig}), '${arrayMethod}');
                            </script>
                        </c:if>
                    </c:forEach>

                </c:catch>

                <c:if test="${not empty jspError}">
                    <script>
                        console.error('JSP error:');
                        console.error('${jspError}');
                    </script>
                </c:if>

                <script>
                    (function(){

                        var PROJECT_ID =
                                '${projectId}' ||
                                getQueryStringValue('id') ||
                                getQueryStringValue('project') ||
                                getUrlHashValue('#id=') ||
                                getUrlHashValue('#project=');

                        if (!PROJECT_ID) {
                            $('#project-not-specified').hidden(false);
                            return;
                        }

                        // make sure the 'projectId' variable is set for use in spawned elements
                        // (it could be referenced by any of these variables)
                        XNAT.data.context.project = XNAT.data.projectId = XNAT.data.project = PROJECT_ID;

                        function projectLink(cfg, txt){
                            return spawn('a.last', extend(true, {
                                href: '${SITE_ROOT}/data/projects/' + PROJECT_ID + '?format=html',
                                style: { textDecoration: 'underline' },
                                data: { projectId: PROJECT_ID }
                            }, cfg), txt || PROJECT_ID);
                        }

                        // // render the breadcrumb(s) here in case the project id is in the URL hash
                        // $('#breadcrumbs').spawn('span.crumb', [
                        //     projectLink({ id: 'breadcrumb-' + PROJECT_ID })
                        // ]);

                        $('.project-link').each(function(){
                            this.appendChild(projectLink())
                        });

                        $('.project-id').each(function(){
                            this.textContent = PROJECT_ID;
                        });

                        // cache DOM objects
                        var projectSettingsHeader$ = $('#project-settings-header');

                        // render projectSettings tab into specified container
                        var tabContentContainer$ = $('#project-settings-tabs').find('div.content-tabs');

                        // do the tab spawning after fetching the project data (below)
                        function spawnProjectSettingsTabs(callback){

                            var projectSettingsTabs = {};

                            // alias for brevity
                            var tabConfigs = XNAT.app.pluginSettings.tabConfigs;

                            if (tabConfigs.length) {

                                // show the 'Plugin Settings' item in the 'Administer' menu
                                // $('#view-plugin-settings').show().hidden(false);

                                forEach(tabConfigs, function(tabConfig){
                                    // resolve top-level 'contains'/'contents' properties
                                    if (tabConfig.hasOwnProperty('contains')) {
                                        tabConfig.contents = tabConfig[tabConfig.contains];
                                        delete tabConfig[tabConfig.contains];
                                        delete tabConfig.contains;
                                    }
                                    // resolve 'meta.tabGroups'/'tabGroups'/'groups' properties
                                    if (tabConfig.meta && tabConfig.meta.tabGroups) {
                                        tabConfig.groups = tabConfig.meta.tabGroups;
                                        delete tabConfig.meta.tabGroups;
                                    }
                                    else if (tabConfig.tabGroups) {
                                        tabConfig.groups = tabConfig.tabGroups;
                                        delete tabConfig.tabGroups;
                                    }
                                    // merge all configs into a single config object
                                    extend(true, projectSettingsTabs, tabConfig);
                                });

                                // make sure these properties are set correctly
                                projectSettingsTabs.kind      = 'tabs';
                                projectSettingsTabs.name      = 'projectSettingsTabs';
                                projectSettingsTabs.container = tabContentContainer$;
                                projectSettingsTabs.layout    = 'left';

                                // render the tabs
                                XNAT.spawner
                                    .spawn({ projectSettingsTabs: projectSettingsTabs })
                                    .render(tabContentContainer$)
                                    .done(function(spawned){
                                        var selectTab   = getUrlHashValue('tab=');
                                        var tabSelector = selectTab ? 'ul.nav > li[data-tab="' + selectTab + '"]' : 'ul.nav > li[data-tab]';
                                        // XNAT.ui.tab.activate(getUrlHashValue('tab='), spawned);
                                        waitForElement(1, tabSelector, function(){
                                            $('#tabs-loading').remove();
                                            $(tabSelector).first().trigger('click');
                                        });
                                    })
                            }
                        }

                        // get project data first so we have data to work with
                        XNAT.xhr.getJSON({
                            url: XNAT.url.restUrl('/data/projects/' + PROJECT_ID),
                            success: function(data){

                                // make project id available
                                XNAT.data.projectId = XNAT.data.projectID = PROJECT_ID;

                                // make returned project data available for Spawner elements
                                XNAT.data.projectData = data;

                                // show the header since we should have the data
                                projectSettingsHeader$.hidden(false).removeClass('hidden');

                                // render the project settings tabs
                                spawnProjectSettingsTabs(function(){
                                    tabContentContainer$.hidden(false).removeClass('hidden');
                                });

                            },
                            failure: function(){
                                // if REST call for project data fails,
                                projectSettingsHeader$.hidden(true).addClass('hidden');
                                tabContentContainer$.hidden(true).addClass('hidden');
                                $('#project-data-error').hidden(false).removeClass('hidden');
                            }
                        })

                    }())

                </script>

            </div>
        </div>
    </div>

</c:if>