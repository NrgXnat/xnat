<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<%--
  ~ web: content.jsp
  ~ XNAT http://www.xnat.org
  ~ Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
  ~ All Rights Reserved
  ~
  ~ Released under the Simplified BSD.
  --%>

<c:if test="${empty requestScope.hasInit}">
    <pg:init>
        <c:if test="${empty requestScope.hasVars}">
            <pg:jsvars/>
        </c:if>
    </pg:init>
</c:if>

<div id="page-wrapper">
    <div class="pad">
        <div id="page-content"></div>
    </div>
</div>

<script>
    (function(){

        var customPage = XNAT.app.customPage;
        var $pageContent = $('#page-content').html('loading...');

        customPage.getPage(null, $pageContent);

        //        window.onhashchange = function(){
        //            customPage.getPage('', $pageContent);
        //        }

    })();
</script>
