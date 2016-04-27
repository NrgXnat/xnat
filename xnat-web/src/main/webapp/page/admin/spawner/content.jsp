<!-- BEGIN views/spawner/elements.jsp -->
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%--<%@ taglib prefix="sp" tagdir="/WEB-INF/tags/spawner" %>--%>
<%--<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="elements" scope="request" type="java.util.List"/>

<table>
    <c:forEach var="element" items="${elements}">
        <tr class="highlight events-list">
            <td class="site-event-id">
                <a href="elements/${element.elementId}" class="edit-event" data-action="editEvent"
                   title="edit existing event">${element.label}</a>
            </td>
            <td class="site-event-label">
                    ${element.description}
            </td>
            <td class="actions" style="text-align:center;white-space:nowrap;">
                <a href="#!" class="delete-event" data-action="deleteEvent"
                   title="delete existing event">delete</a>
            </td>
        </tr>
    </c:forEach>
</table>
