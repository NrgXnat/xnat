<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<c:set var="pageName" value="view-page" scope="request"/>

<pg:wrapper>

    <pg:head/>

    <pg:content id="${pageName}" className="xnat app ${pageName}">
        <div id="page-wrapper">
            <div class="pad">
                <jsp:include page="content.jsp"/>
            </div>
        </div>
        <!-- /#page-wrapper -->
    </pg:content>

</pg:wrapper>
