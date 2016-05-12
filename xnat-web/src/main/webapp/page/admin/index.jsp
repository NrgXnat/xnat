<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<c:set var="pageName" value="admin" scope="request"/>

<pg:wrapper>
    <pg:xnat>

        <jsp:include page="${not empty param.view ? param.view : 'content'}.jsp"/>

    </pg:xnat>
</pg:wrapper>
