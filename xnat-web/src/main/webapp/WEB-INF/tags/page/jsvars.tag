<%@ tag description="Initialize Variables" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
  ~ web: jsvars.tag
  ~ XNAT http://www.xnat.org
  ~ Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
  ~ All Rights Reserved
  ~
  ~ Released under the Simplified BSD.
  --%>

<script>
    var PAGE = {};
    PAGE.siteRoot = '${sessionScope.siteRoot}';
    PAGE.themeName = '${sessionScope.themeName}';
    PAGE.themeRoot = '${sessionScope.themeRoot}';
    PAGE.pageRoot = '${sessionScope.pageRoot}';
    PAGE.username = '${sessionScope.username}';
    PAGE.isDialog = '${sessionScope.isDialog}';
    PAGE.isModal = '${sessionScope.isModal}';
    PAGE.session = {
        username: '${sessionScope.username}',
        token: '${sessionScope.csrfToken}',
        expiration: '${sessionScope.sessionExpiration}'
    };
    PAGE.session.startTime = ('${sessionScope.sessionExpiration}'.split(',')[0]) * 1;
    PAGE.session.startString = new Date(PAGE.session.startTime).toString();
    PAGE.session.duration = ('${sessionScope.sessionExpiration}'.split(',')[1]) * 1;
    PAGE.session.endTime = (PAGE.session.startTime + PAGE.session.duration);
    PAGE.session.endString = new Date(PAGE.session.endTime).toString();
    PAGE.session.timeout = PAGE.session.endTime;
    console.log(PAGE);
</script>

<c:set var="hasVars" value="true" scope="request"/>
