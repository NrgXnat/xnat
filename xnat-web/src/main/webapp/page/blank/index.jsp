<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<c:set var="pageName" value="blank" scope="request"/>

<c:set var="_headTop">
    <script>alert('(it is blank)')</script>
</c:set>

<c:set var="_bodyBottom">
    <h1>I'm at the bottom.</h1>
</c:set>

<pg:wrapper>
    <pg:xnat headTop="${_headTop}" bodyBottom="${_bodyBottom}">

        <jsp:include page="content.jsp"/>

    </pg:xnat>
</pg:wrapper>
