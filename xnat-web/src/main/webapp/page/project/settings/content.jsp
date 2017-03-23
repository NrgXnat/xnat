<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>


<c:set var="SITE_ROOT" value="${sessionScope.siteRoot}"/>

<c:if test="${not empty param.id}">
    <c:set var="id" value="${param.id}"/>
</c:if>


<div id="page-wrapper">
    <div class="pad">


        <%-- project id must be in query string 'id=fooProject' --%>
        <c:if test="${empty id}">
            <div class="error">Project not specified.</div>
        </c:if>


        <c:if test="${not empty id}">

            <%-- if an 'id' param is passed, use its value to edit specified project data --%>
            <h3 id="project-settings-header" class="hidden">Settings for ${id}</h3>

            <%-- show an error if the project data is not returned from the rest call --%>
            <div id="project-data-error" class="error hidden">Data for "${id}" project not found.</div>

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

            <script src="${SITE_ROOT}/scripts/xnat/app/pluginSettings.js"></script>

            <script>
                (function(){

                    // cache DOM objects
                    var projectSettingsHeader$ = $('#project-settings-header');

                    // render siteSettings tab into specified container
                    var projectSettingsTabs = $('#project-settings-tabs').find('div.content-tabs');

                    // these properties _should_ be set before spawning 'tabs' widgets
                    XNAT.tabs.container = projectSettingsTabs;
                    XNAT.tabs.layout = 'left';

                    // get project data first so we have data to work with
                    XNAT.xhr.getJSON({
                        url: XNAT.url.restUrl('/data/projects/${id}'),
                        success: function(data){
                            // make project id available
                            XNAT.data.projectId = XNAT.data.projectID = '${id}';
                            // make returned project data available for Spawner elements
                            XNAT.data.projectData = data;
                            // show the header since we should have the data
                            projectSettingsHeader$.removeClass('hidden');
                            // render the project settings tabs
                            XNAT.app.pluginSettings.projectSettingsTabs = projectSettingsTabs;
                            XNAT.app.pluginSettings.projectSettings(projectSettingsTabs, function(data){
                                console.log(data);
                                console.log(arguments);
                            });
                        },
                        failure: function(){
                            // if REST call for project data fails,
                            projectSettingsHeader$.addClass('hidden');
                            $('#project-data-error').removeClass('hidden');
                        }
                    })

                }())

            </script>

        </c:if>


    </div>
</div>
