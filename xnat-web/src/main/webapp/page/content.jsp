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

        var sampleUrl = '/page/#/foo/#tab=bar/#panel=baz';

        // save the value for the initial page that's loaded
        var page = getUrlHashValue('#/');

        var $pageContent = $('#page-content').html('loading...');

//        var pageName = XNAT.app.customPage.getPageName();
//        console.log('pageName: "' + pageName + '"');

        XNAT.app.customPage.container = $pageContent;
        XNAT.app.customPage.getPage(page);

        $(window).on('hashchange', function(e){
            e.preventDefault();
            var newPage = getUrlHashValue('#/');
            // only get a new page if the page part has changed
            if (newPage !== page) {
                XNAT.app.customPage.getPage(newPage, $pageContent);
            }
        });

    })();

    $(function(){
        $(document).on('click', '[href^="#"], [href^="@!"]', function(e){
            e.preventDefault();
        });
    });

</script>
