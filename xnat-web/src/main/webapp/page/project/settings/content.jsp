<%@ page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<pg:init/>
<pg:jsvars/>

<c:set var="SITE_ROOT" value="${sessionScope.siteRoot}"/>

<c:if test="${not empty param.id}">
    <c:set var="id" value="${fn:escapeXml(param.id)}"/>
</c:if>

<div id="breadcrumbs" style="padding-left:0;">
    <span class="crumb project-link">
        <%-- project link rendered here --%>
    </span>
</div>

<div id="page-body">
    <div class="pad">

        <div id="project-settings-page" class="settings-tabs">

            <div id="project-not-specified" class="error hidden">Project not specified.</div>

            <%-- show an error if the project data is not returned from the rest call --%>
            <div id="project-data-error" class="error hidden">Data for "<span class="project-id"></span>" project not found.</div>

            <%-- if an 'id' param is passed, use its value to edit specified project data --%>
            <header id="project-settings-header" class="hidden">
                <h2 class="pull-left">Settings for <span class="project-id"></span></h2>
                <div class="clearfix"></div>
            </header>

            <div id="project-settings-tabs">
                <div class="content-tabs xnat-tab-container">

                    <%--<div class="xnat-nav-tabs side pull-left">--%>
                    <%--<!-- ================== -->--%>
                    <%--<!-- Admin tab flippers -->--%>
                    <%--<!-- ================== -->--%>
                    <%--</div>--%>
                    <%--<div class="xnat-tab-content side pull-right">--%>
                    <%--<!-- ================== -->--%>
                    <%--<!-- Admin tab panes    -->--%>
                    <%--<!-- ================== -->--%>
                    <%--</div>--%>

                </div>
            </div>

        <script>
            (function(){

                var PROJECT_ID =
                    '${id}' ||
                    getQueryStringValue('id') ||
                    getQueryStringValue('project') ||
                    getUrlHashValue('#id=') ||
                    getUrlHashValue('#project=');

                if (!PROJECT_ID) {
                    $('#project-not-specified').hidden(false);
                    return;
                }

                $('span.project-id').html('<a href="/app/action/DisplayItemAction/search_element/xnat:projectData/search_field/xnat:projectData.ID/search_value/'+PROJECT_ID+'">'+PROJECT_ID+'</a>');

                // cache DOM objects
                var projectSettingsHeader$ = $('#project-settings-header');

                // render siteSettings tab into specified container
                var projectSettingsTabs = $('#project-settings-tabs').find('div.content-tabs');

                // these properties _should_ be set before spawning 'tabs' widgets
                XNAT.tabs.container = projectSettingsTabs;
                XNAT.tabs.layout = 'left';

                // get project data first so we have data to work with
                XNAT.xhr.getJSON({
                    url: XNAT.url.restUrl('/data/projects/' + PROJECT_ID),
                    success: function(data){
                        // make project id available
                        XNAT.data.projectId = XNAT.data.projectID = PROJECT_ID;
                        // make returned project data available for Spawner elements
                        XNAT.data.projectData = data;
                        // show the header since we should have the data
                        projectSettingsHeader$.removeClass('hidden');
                        // render the project settings tabs
                        XNAT.app.pluginSettings.showTabs = true;
                        XNAT.app.pluginSettings.projectSettingsTabs = projectSettingsTabs;
                        XNAT.app.pluginSettings.projectSettings(projectSettingsTabs, function(data){
                            console.log(data);
                            console.log(arguments);
                            XNAT.tab.activate(XNAT.tab.active, projectSettingsTabs);
                        });
                    },
                    failure: function(){
                        // if REST call for project data fails,
                        projectSettingsHeader$.hidden();
                        $('#project-data-error').hidden(false);
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

                    $('.project-link').each(function(){
                        this.appendChild(projectLink())
                    });

                    $('.project-id').each(function(){
                        this.textContent = PROJECT_ID;
                    });

                    // cache DOM objects
                    var projectSettingsHeader$ = $('#project-settings-header');

                    // render siteSettings tab into specified container
                    var projectSettingsTabs = $('#project-settings-tabs').find('div.content-tabs');

                    // these properties _should_ be set before spawning 'tabs' widgets
                    XNAT.tabs.container = projectSettingsTabs;
                    XNAT.tabs.layout    = 'left';

                    // get project data first so we have data to work with
                    XNAT.xhr.getJSON({
                        url: XNAT.url.restUrl('/data/projects/' + PROJECT_ID),
                        success: function(data){
                            // make project id available
                            XNAT.data.projectId   = XNAT.data.projectID = PROJECT_ID;
                            // make returned project data available for Spawner elements
                            XNAT.data.projectData = data;
                            // show the header since we should have the data
                            projectSettingsHeader$.removeClass('hidden');
                            // render the project settings tabs
                            XNAT.app.pluginSettings.showTabs            = true;
                            XNAT.app.pluginSettings.projectSettingsTabs = projectSettingsTabs;
                            XNAT.app.pluginSettings.projectSettings(projectSettingsTabs, function(data){
                                console.log(data);
                                console.log(arguments);
                                XNAT.tab.activate(XNAT.tab.active, projectSettingsTabs);
                            });
                        },
                        failure: function(){
                            // if REST call for project data fails,
                            projectSettingsHeader$.hidden();
                            $('#project-data-error').hidden(false);
                        }
                    })

                }())

            </script>

        </div>
    </div>
</div>