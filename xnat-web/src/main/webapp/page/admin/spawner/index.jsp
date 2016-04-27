<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<c:set var="pageName" value="spawner" scope="request"/>

<pg:wrapper>
    <pg:xnat title="Manage Spawner">

        <jsp:include page="content.jsp"/>

    </pg:xnat>
</pg:wrapper>
