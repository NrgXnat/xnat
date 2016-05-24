<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>
<%--<%@ taglib prefix="sp" tagdir="/WEB-INF/tags/spawner" %>--%>

<c:set var="MSG">
    No spawning allowed.
    <script> window.location.href = XNAT.url.rootUrl('/') </script>
</c:set>

<pg:restricted msg="${MSG}">

    <c:set var="SITE_ROOT" value="${sessionScope.siteRoot}"/>

    <div class="panel panel-default">

        <div class="panel-heading">
            <h3 class="panel-title">XNAT Spawner Elements</h3>
        </div>

        <div class="panel-body">

            <div data-name="spawnerElements" class="panel-element" style="overflow:visible;">

                    <%--<label class="element-label" for="!?"></label>--%>
                    <%--<div class="element-wrapper">--%>

                <table id="spawner-element-list" class="xnat-table alt1 clean" style="width:100%;border:none;">
                    <!-- list of available namespaces will show here -->
                </table>

                <div class="description" style="margin:20px 5px 0">View and manage XNAT Spawner elements.</div>

                    <%--</div>--%>

            </div>
        </div>

        <div class="hidden"></div>

    </div>

    <!-- button element will be rendered in this span -->
    <span id="view-json"></span>

    <script src="${SITE_ROOT}/page/admin/spawner/spawner-admin.js"></script>

</pg:restricted>
