<%@ tag description="Document Skeleton" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<%@ attribute name="msg" %>

<%-- restricts access to only admin users --%>

<c:if test="${empty requestScope.hasInit}">
    <pg:init>
        <c:if test="${empty requestScope.hasVars}">
            <pg:jsvars/>
        </c:if>
    </pg:init>
</c:if>

<c:choose>
    <c:when test="${sessionScope.isAdmin == true}">

        <jsp:doBody/>

    </c:when>
    <c:otherwise>

        <c:choose>
            <c:when test="${not empty msg}">
                ${msg}
            </c:when>
            <c:otherwise>
                <p>(not authorized)</p>
            </c:otherwise>
        </c:choose>

    </c:otherwise>
</c:choose>
