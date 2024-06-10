<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
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

    <script type="text/javascript" charset="utf8" src="//cdn.datatables.net/1.10.19/js/jquery.dataTables.min.js"></script>
    <script type="text/javascript" src="https://code.jquery.com/ui/1.9.2/jquery-ui.min.js"></script>
    <script src="${SITE_ROOT}/scripts/project/projEventService.js"></script>
    <script src="${SITE_ROOT}/scripts/project/projEventHistory.js"></script>
    <link type="text/css" rel="stylesheet" href="${SITE_ROOT}/scripts/xnat/admin/eventServiceAdmin.css?v=event-service-1.0" />
    <link type="text/css" rel="stylesheet" href="${SITE_ROOT}/scripts/xnat/app/scanTable/scanTable.css?v=event-service-1.0" />

    <%-- if an 'id' param is passed, use its value to edit specified project data --%>
    <header id="project-eventservice-header">
        <h2 class="pull-left" style="margin:0 0 20px 0;font-size:16px;">
        Event Service Management for <span class="project-link"></span>
        </h2>
        <div class="clearfix"></div>
    </header>

    <div id="page-body">
        <div class="pad">

            <div id="project-eventservice-page" class="settings-tabs">

                <%-- show an error if the project isn't specified in the query string or url hash --%>
                <div id="project-not-specified" class="error hidden">Project not specified.</div>

                <%-- show an error if the project data is not returned from the rest call --%>
                <div id="project-data-error" class="error hidden">Data for "<span class="project-id"></span>" project not found.</div>

                    <div id="project-eventservice-tabs" class="xnat-tab-container"></div>

                </div>

                <script>
                    $(document).ready(function(){
                        XNAT.admin.projEventServicePanel.init();
                    });
                </script>

            </div>
        </div>
    </div>
    <!-- /#page-body -->

    <div id="xnat-scripts"></div>
</c:if>


