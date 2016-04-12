<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="sp" tagdir="/WEB-INF/tags/spawner" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="elementId" scope="request" type="java.lang.String"/>

<c:set var="title" value="Spawner Element: ${elementId}"/>
<sp:layout title="${title}">
    <h3 style="margin:0 0 15px 0;">${title}</h3>

    <div style="margin:15px 0;max-width:696px;min-width:400px;">
        Make your desired changes to the spawner element and click <b>Save</b> when completed.
    </div>

<script type="text/javascript">
    var spawnerElement = $.ajax({
        type : "GET",
        url:serverRoot+"/xapi/spawner/${elementId}?XNAT_CSRF=" + window.csrfToken,
        cache: false,
        async: true,
        context: this,
        dataType: "yaml"
    });
    spawnerElement.done( function( data, textStatus, jqXHR ) {
        if (typeof data.ResultSet !== "undefined" && typeof data.ResultSet.Result !== "undefined") {
            alert(data);
        }
    });

</script>
</sp:layout>