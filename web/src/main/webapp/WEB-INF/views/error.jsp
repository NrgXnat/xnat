<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<%--
  ~ web: error.jsp
  ~ XNAT http://www.xnat.org
  ~ Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
  ~ All Rights Reserved
  ~
  ~ Released under the Simplified BSD.
  --%>

<jsp:useBean id="status" scope="request" type="org.springframework.http.HttpStatus"/>
<jsp:useBean id="url" scope="request" type="java.lang.String"/>
<jsp:useBean id="message" scope="request" type="java.lang.String"/>
<jsp:useBean id="exception" scope="request" type="java.lang.Throwable"/>

<pg:wrapper>
    <pg:xnat>

        <h3>An error has occurred</h3>
        <table>
            <tr>
                <td><strong>Status:</strong></td>
                <td>${status}</td>
            </tr>
            <tr>
                <td><strong>Request URL:</strong></td>
                <td>${url}</td>
            </tr>
            <tr>
                <td><strong>Message:</strong></td>
                <td>${message}</td>
            </tr>
            <c:if test="${not empty exception}">
                <tr>
                    <td><strong>Exception:</strong></td>
                    <td>${exception.message}</td>
                </tr>
                <tr>
                    <td><strong>Stacktrace:</strong></td>
                    <td>
                        <c:forEach var="element" items="exception.stackTrace">
                            ${element.toString()}<br/>
                        </c:forEach>
                    </td>
                </tr>
            </c:if>
        </table>

    </pg:xnat>
</pg:wrapper>
