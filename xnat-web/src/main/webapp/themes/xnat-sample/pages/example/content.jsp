<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<div id="page-wrapper">
    <div class="pad">

        <pg:restricted msg="Members Only">
            <div class="admin-content">
                <h1>Administrator Access</h1>
                <p>If you can read this, you are a site administrator.</p>
            </div>
        </pg:restricted>
        <br>
        <small>loaded from: '/themes/xnat-sample/pages/example/content.jsp'</small>
        <br>
        <small>The '*.jsp' page takes priority over the '*.html' page.</small>

    </div>
</div>
<!-- /#page-wrapper -->

<script>
    console.log(window.location.href.split(serverRoot)[1]);
    console.log('/themes/xnat-sample/pages/example/content.jsp');
</script>
