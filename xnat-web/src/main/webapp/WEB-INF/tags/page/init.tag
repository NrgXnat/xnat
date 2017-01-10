<%@ tag description="Initialize Variables" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%--
  ~ web: init.tag
  ~ XNAT http://www.xnat.org
  ~ Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
  ~ All Rights Reserved
  ~
  ~ Released under the Simplified BSD.
  --%>

<c:if test="${empty requestScope.hasInit}">

    <%-- set empty user info --%>
    <c:set var="loggedIn" value="false" scope="session"/>
    <c:set var="username" value="-" scope="session"/>
    <c:set var="isAdmin" value="false" scope="session"/>
    <c:set var="isGuest" value="false" scope="session"/>

    <%-- set vars for user --%>
    <sec:authorize access="isAuthenticated()">
        <c:set var="loggedIn" value="true" scope="session"/>
        <c:set var="username" value="${pageContext.request.userPrincipal.name}" scope="session"/>
    </sec:authorize>

    <%-- REDIRECT IF NOT LOGGED IN (username will be '-') --%>
    <c:if test="${username == '-'}">
        <%--<c:redirect url="/app/template/Login.vm"/>--%>
    </c:if>

    <sec:authorize access="hasAnyRole('ADMIN')">
        <c:set var="isAdmin" value="true" scope="session"/>
    </sec:authorize>

    <sec:authorize access="hasAnyRole('ANONYMOUS')">
        <c:set var="isGuest" value="true" scope="session"/>
    </sec:authorize>

    <c:set var="themeName" value="${cookie.THEME_NAME.value}" scope="session"/>

    <%-- if there's a theme specified in the request, use that --%>
    <c:if test="${empty themeName && not empty param.theme}">
        <c:set var="themeName" value="${param.theme}" scope="session"/>
    </c:if>

    <%-- set 'siteRoot' to the root of your web app --%>
    <c:set var="siteRoot" value="${pageContext.request.contextPath}" scope="session"/>

    <%-- add a leading slash if siteRoot is not empty and doesn't already start with a slash --%>
    <c:if test="${siteRoot != '' && !fn:startsWith(siteRoot,'/')}">
        <c:set var="siteRoot" value="/${pageContext.request.contextPath}" scope="session"/>
    </c:if>

    <c:set var="themeRoot" value="${siteRoot}/themes/${themeName}" scope="session"/>
    <c:set var="pageRoot" value="${themeRoot}/page" scope="session"/>

    <%-- if no themeName is found, set vars to use root items --%>
    <c:if test="${empty themeName}">
        <c:set var="themeRoot" value="${siteRoot}" scope="session"/>
        <c:set var="pageRoot" value="${siteRoot}/page" scope="session"/>
    </c:if>

    <%-- get session expiration time --%>
    <c:set var="sessionExpiration" value="${cookie.SESSION_EXPIRATION_TIME.value}" scope="session"/>

    <c:set var="csrfToken" value="0" scope="session"/>
    <c:set var="landingPage" value="${pageRoot}/login/#!" scope="session"/>

    <c:if test="${loggedIn == true}">
        <c:set var="csrfToken" value="${sessionScope.XNAT_CSRF}" scope="session"/>
        <c:set var="landingPage" value="${pageRoot}/home/#!" scope="session"/>
    </c:if>

    <%-- is this page in a modal/dialog box --%>
    <c:set var="isModal" value="${not empty param.modal && param.modal == 'true'}"/>
    <c:set var="isDialog" value="${not empty param.dialog && param.dialog == 'true'}"/>

    <c:set var="hasInit" value="true" scope="request"/>

</c:if>

<%-- inject content on init--%>
<jsp:doBody/>
