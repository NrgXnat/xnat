<%@ page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
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
    <script> window.location.href = '<c:url value="/"/>' </script>
</c:set>

<pg:restricted msg="${redirect}">

    <c:set var="SITE_ROOT" value="${sessionScope.siteRoot}"/>

    <div id="page-body">
        <div class="pad">

            <div id="admin-page">
                <header id="content-header">
                    <h2 class="pull-left">Site Administration</h2>
                    <div class="clearfix"></div>
                </header>

                <!-- Admin tab container -->
                <div id="admin-config-tabs">

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

                <c:import url="/xapi/siteConfig" var="siteConfig"/>
                <c:import url="/xapi/notifications" var="notifications"/>

                <script>
                    (function(){

                        XNAT.data = extend(true, {
                            siteConfig: {},
                            notifications: {}
                        }, XNAT.data||{});

                        <%-- safety check --%>
                        <c:if test="${not empty siteConfig}">
                            XNAT.data.siteConfig = ${siteConfig};
                            // get rid of the 'targetSource' property
                            delete XNAT.data.siteConfig.targetSource;
                            XNAT.data['/xapi/siteConfig'] = XNAT.data.siteConfig;
                        </c:if>

                        <%-- can't use empty/undefined object --%>
                        <c:if test="${not empty notifications}">
                            XNAT.data.notifications = ${notifications};
                            XNAT.data['/xapi/notifications'] = XNAT.data.notifications;
                        </c:if>

                        // these properties MUST be set before spawning 'tabs' widgets
                        XNAT.tabs.container = $('#admin-config-tabs').find('div.content-tabs');
                        XNAT.tabs.layout = 'left';

                        var gotTabs = false;
                        var spawnerIds = ['root', 'adminPage'];

                        function findAdminTabs(idIndex){
                            if (gotTabs || idIndex >= spawnerIds.length) return;
                            var spawnerNS = 'siteAdmin/' + spawnerIds[idIndex];
                            XNAT.spawner
                                .resolve(spawnerNS)
                                .ok(function(){
                                    gotTabs = true;
                                    this.render(XNAT.tabs.container, 200, function(){
                                        //initInfoLinks();
                                    });
                                    this.done(function(){
                                        XNAT.tab.activate(XNAT.tab.active, XNAT.tabs.container);
                                    })
                                })
                                .fail(function(){
                                    findAdminTabs(idIndex += 1)
                                })
                        }

                        findAdminTabs(0);

                    })();

                </script>

            </div>

        </div>
    </div>
    <!-- /#page-body -->

    <div id="xnat-scripts">
        <script>
            //        $(window).load(function(){
            //            // any values that start with '@|' will be set to
            //            // the value of the element with matching selector
            //            $('[value^="@|"]').each(function(){
            //                var selector = $(this).val().split('@|')[1];
            //                var value = $$(selector).val();
            //                $(this).val(value).dataAttr('value',value);
            //            });
            //        });
        </script>
    </div>

</pg:restricted>

