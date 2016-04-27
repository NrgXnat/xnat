<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<c:if test="${empty hasInit}">
    <pg:init/>
</c:if>

<c:if test="${not empty hasInit}">
    <!-- init done -->
</c:if>

<c:if test="${empty hasVars}">
    <pg:jsvars/>
</c:if>

<div id="page-wrapper">
    <div class="pad">
        <div id="page-content">loading...</div>
    </div>
</div>


<script>
    (function () {

        function getUrlHash(key) {
            var hash = window.location.hash.split(key || '#')[1] || '';
            return hash.split('/#')[0];
        }

        function getContent() {
            var hash = getUrlHash('#/') || 'home';
            XNAT.xhr.get({
                dataType: 'html',
                url: PAGE.pageRoot + '/' + (hash.replace(/\/+$/, '')) + '/content.jsp',
                success: function (html) {
                    $('#page-content').html(html);
                },
                failure: function () {
                    $('#page-content').html('page not found');
                }
            })
        }

        getContent();

        window.onhashchange = function () {
            getContent();
        }

    })();
</script>