<%@ page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
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

            <div id="user-admin-page">
                <header id="content-header">
                    <h2>Manage Stored Searches ("Bundles")
                        <a class="popup pull-right" href="${SITE_ROOT}/app/template/XDATScreen_edit_xdat_stored_search.vm/popup/true">
                            <button class="btn1 btn-sm">New Stored Search</button>
                        </a>
                    </h2>
                    <div class="clearfix"></div>
                </header>

                <div id="stored-searches-container">
                </div>

                <script>

                    XNAT.page = getObject(XNAT.page);
                    XNAT.page.ssAdminPage = true;

                </script>
                <style>
                    .primary-link { font-weight: bold; }
                </style>

                <script src="${SITE_ROOT}/scripts/lib/x2js/xml2json.js"></script>
                <script src="${SITE_ROOT}/scripts/xnat/admin/storedSearches.js"></script>

                <script>
                    XNAT.admin.storedSearches.init();
                </script>

            </div>

        </div>
    </div>
    <!-- /#page-body -->

    <div id="xnat-scripts"></div>

</pg:restricted>

