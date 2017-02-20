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

                        var adminTabs = XNAT.spawner.resolve('siteAdmin/adminPage');
                        adminTabs.render(XNAT.tabs.container, 200, function(){
                            //initInfoLinks();
                            // SAVE THE UI JSON
                            XNAT.app.adminTabs = adminTabs;
                        });

                    })();

//                    function initInfoLinks(){
//                        $('.infolink').click(function(e){
//                            var idx = this.id.substr(9);
//                            var help = infoContent[idx];
//                            xmodal.message(help.title, help.content);
//                        });
//                    }
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

