<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<%--
  ~ web: content.jsp
  ~ XNAT http://www.xnat.org
  ~ Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
  ~ All Rights Reserved
  ~
  ~ Released under the Simplified BSD.
  --%>

<c:set var="redirect">
    <div class="error">Not authorized. Redirecting...</div>
    <script> window.location.href = '<c:url value="/"/>' </script>
</c:set>

<pg:restricted msg="${redirect}">

    <c:set var="SITE_ROOT" value="${sessionScope.siteRoot}"/>

    <div id="page-body">
        <div class="pad">

            <div id="admin-page">
                <header id="content-header">
                    <h2 class="pull-left">Event Service Administration</h2>
                    <div class="clearfix"></div>
                </header>

                <!-- Task Settings tab container -->
                <div id="event-service-admin-tabs" class="xnat-tab-container"></div>
                <script type="text/javascript" src="${SITE_ROOT}/scripts/xnat/admin/eventServiceUi.js"></script>
                <script type="text/javascript" src="${SITE_ROOT}/scripts/xnat/admin/eventServiceHistory.js"></script>
                <link type="text/css" rel="stylesheet" href="${SITE_ROOT}/scripts/xnat/admin/eventServiceAdmin.css?v=event-service-1.0" />
                <link type="text/css" rel="stylesheet" href="${SITE_ROOT}/scripts/xnat/app/scanTable/scanTable.css?v=event-service-1.0" />
                <script>
                    $(document).ready(function(){
                        XNAT.admin.eventServicePanel.init();
                    });
                </script>
            </div>

        </div>
    </div>
    <!-- /#page-body -->

    <div id="xnat-scripts"></div>

</pg:restricted>

