<%@ page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %><%@ taglib prefix="xnat" uri="http://www.xnat.org/tags" %>

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

        <xnat:import url="${incl}"/>

    </pg:xnat>
</pg:wrapper>
