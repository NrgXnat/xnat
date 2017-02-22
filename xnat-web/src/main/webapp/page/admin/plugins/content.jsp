<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
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

<c:set var="redirect">
    <div class="error">Not authorized. Redirecting...</div>
    <script> window.location.href = XNAT.url.rootUrl('/') </script>
</c:set>

<pg:restricted msg="${redirect}">

    <c:set var="SITE_ROOT" value="${sessionScope.siteRoot}"/>

    <div id="page-body">
        <div class="pad">

            <div id="admin-page">
                <header id="content-header">
                    <h2 class="pull-left">Plugin Settings</h2>
                    <div class="clearfix"></div>
                </header>

                <!-- Plugin Settings tab container -->
                <div id="plugin-settings-tabs">

                    <div class="content-tabs xnat-tab-container">

                        <%--
                        <div class="xnat-nav-tabs side pull-left">
                            <!-- ================== -->
                            <!-- Admin tab flippers -->
                            <!-- ================== -->
                        </div>
                        <div class="xnat-tab-content side pull-right">
                            <!-- ================== -->
                            <!-- Admin tab panes    -->
                            <!-- ================== -->
                        </div>
                        --%>

                    </div>

                </div>

                <c:set var="pluginsUrl" value="/xapi/plugins"/>
                <c:set var="spawnerNamespacesUrl" value="/xapi/spawner/namespaces"/>

                <c:import url="${pluginsUrl}" var="plugins"/>
                <c:import url="${spawnerNamespacesUrl}" var="spawnerNamespaces"/>

                <script src="${SITE_ROOT}/scripts/xnat/app/pluginSettings.js"></script>

                <script>
                    (function(){

                        XNAT.xapi = getObject(XNAT.xapi);

                        <c:if test="${not empty plugins}">
                            XNAT.xapi.plugins = ${plugins};
                            XNAT.data['${pluginsUrl}'] = XNAT.xapi.plugins;
                        </c:if>

                        <c:if test="${not empty spawnerNamespaces}">
                            XNAT.xapi.spawnerNamespaces = ${spawnerNamespaces};
                            XNAT.data['${spawnerNamespacesUrl}'] = XNAT.xapi.spawnerNamespaces;
                        </c:if>

                        XNAT.data = extend(true, {
                            plugins: XNAT.xapi.plugins,
                            spawnerNamespaces: XNAT.xapi.spawnerNamespaces
                        }, XNAT.data||{});

                        // render siteSettings tab into specified container
                        var siteSettingsTabs = $('#plugin-settings-tabs').find('div.content-tabs');
                        XNAT.app.pluginSettings.siteSettings(siteSettingsTabs);

                    })();
                </script>

            </div>

        </div>
    </div>
    <!-- /#page-body -->

    <div id="xnat-scripts"></div>

</pg:restricted>

