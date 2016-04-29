<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<c:set var="pageName" value="admin" scope="request"/>

<pg:wrapper>
    <pg:xnat>

        <c:set var="view" value="${param.view}"/>

        <c:if test="${empty view}">
            <c:set var="view" value="content"/>
        </c:if>

        <jsp:include page="${view}.jsp"/>

    </pg:xnat>
</pg:wrapper>
