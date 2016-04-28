<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>
<%--<%@ taglib prefix="sp" tagdir="/WEB-INF/tags/spawner" %>--%>

<c:if test="${empty hasInit}">
    <pg:init>
        <c:if test="${empty hasVars}">
            <pg:jsvars/>
        </c:if>
    </pg:init>
</c:if>

<c:set var="MSG">
    Playground clossed.
</c:set>

<pg:restricted msg="${MSG}">

    <c:set var="_siteRoot" value="${sessionScope.siteRoot}"/>

    <!-- playground for XNAT.spawner methods -->

    <div id="spawn-container"></div>

    <script src="${_siteRoot}/scripts/xnat/spawner.js"></script>

    <script>

    </script>

</pg:restricted>