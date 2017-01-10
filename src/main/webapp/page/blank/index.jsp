<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<%--
  ~ web: index.jsp
  ~ XNAT http://www.xnat.org
  ~ Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
  ~ All Rights Reserved
  ~
  ~ Released under the Simplified BSD.
  --%>

<c:set var="pageName" value="blank" scope="request"/>

<c:set var="_headTop">
    <script>console.log('(it is blank)')</script>
</c:set>

<c:set var="_bodyBottom">
    <!-- <h1>I'm at the bottom.</h1> -->
</c:set>

<pg:wrapper>
    <pg:xnat headTop="${_headTop}" bodyBottom="${_bodyBottom}">

        <jsp:include page="content.jsp"/>

    </pg:xnat>
</pg:wrapper>
