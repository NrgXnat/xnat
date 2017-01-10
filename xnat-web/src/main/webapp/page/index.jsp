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

<pg:wrapper>
    <pg:xnat>

        <c:set var="incl" value="content.jsp"/>

        <c:if test="${not empty param.view}">
            <c:set var="incl" value="/page/${param.view}/content.jsp"/>
        </c:if>

        <jsp:include page="${incl}"/>

    </pg:xnat>
</pg:wrapper>
