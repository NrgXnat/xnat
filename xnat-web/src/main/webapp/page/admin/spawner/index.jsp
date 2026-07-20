<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
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

<c:set var="pageName" value="spawner" scope="request"/>

<pg:wrapper>
    <pg:xnat title="Manage Spawner">

        <xnat:import url="content.jsp"/>

    </pg:xnat>
</pg:wrapper>
