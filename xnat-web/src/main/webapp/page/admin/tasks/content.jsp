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
    <script> window.location.href = XNAT.url.rootUrl('/') </script>
</c:set>

<pg:restricted msg="${redirect}">

    <div id="page-body">
        <div class="pad">

            <div id="admin-page">
                <header id="content-header">
                    <h2 class="pull-left"></h2>
                    <div class="clearfix"></div>
                </header>

                <!-- Task Settings tab container -->
                <div id="task-settings-tabs">
                </div>
                <script>
			XNAT.admin.xnatTaskSettings.populateDisplay();
                </script>

            </div>

        </div>
    </div>
    <!-- /#page-body -->

    <div id="xnat-scripts"></div>

</pg:restricted>

