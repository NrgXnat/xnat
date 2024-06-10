<%@ page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
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

<pg:init/>
<pg:jsvars/>

<div id="page-wrapper">
    <div class="pad">
        <div id="page-content"></div>
    </div>
</div>

<script>

    (function(){

        var sampleUrl = '/page/#/foo/#tab=bar/#panel=baz';

        // load initial page content
        XNAT.app.customPage.container = $('#page-content');
        XNAT.app.customPage.getPage();

    })();

</script>
