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

                <c:import url="/xapi/plugins" var="plugins"/>

                <script>
                    (function(){

                        XNAT.xapi = getObject(XNAT.xapi);

                        <c:if test="${not empty plugins}">
                            XNAT.xapi.plugins = ${plugins};
                            XNAT.data['/xapi/plugins'] = XNAT.xapi.plugins;
                        </c:if>

                        XNAT.data = extend(true, {
                            plugins: XNAT.xapi.plugins
                        }, XNAT.data||{});

                        // these properties MUST be set before spawning 'tabs' widgets
                        XNAT.tabs.container = $('#plugin-settings-tabs').find('div.content-tabs');
                        XNAT.tabs.layout = 'left';

                        function pluginTabs(name){
                            var tabSpawn = XNAT.spawner.resolve(name + '/siteSettings');
                            tabSpawn.render(XNAT.tabs.container, 200)
                                    .fail(function(){
                                        console.log('"' + name + '" site settings tabs not found');
                                    });
                            return tabSpawn;
                        }

                        // try to get plugin tab Spawner objects
                        // from url:
                        // /xapi/spawner/resolve/pluginName/siteSettings
                        forOwn(XNAT.xapi.plugins, function(name, obj){
                            // first try to load admin js file at
                            // /scripts/xnat-plugins/pluginName/admin.js
                            //loadjs(XNAT.url.rootUrl('/scripts/xnat-plugins/' + name + '/admin.js'), name);
                            pluginTabs(name);
                        });

                    })();
                </script>

            </div>

        </div>
    </div>
    <!-- /#page-body -->

    <div id="xnat-scripts"></div>

</pg:restricted>

