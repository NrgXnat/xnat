<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<c:set var="redirect">
    <p>Not authorized. Redirecting...</p>
    <script> window.location.href = XNAT.url.rootUrl('/') </script>
</c:set>

<pg:restricted msg="${redirect}">

    <link rel="stylesheet" type="text/css" href="${sessionScope.siteRoot}/page/admin/style.css">

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

                        XNAT.data = getObject(XNAT.data);

                        <%-- safety check --%>
                        <c:if test="${not empty siteConfig}">
                            XNAT.data.siteConfig = ${siteConfig};
                            // get rid of the 'targetSource' property
                            delete XNAT.data.siteConfig.targetSource;
                        </c:if>

                        <%-- can't use empty/undefined object --%>
                        <c:if test="${not empty notifications}">
                            XNAT.data.notifications = ${notifications};
                        </c:if>

                        var jsonUrl = XNAT.url.rootUrl('/xapi/spawner/resolve/siteAdmin/adminPage');

                        $.get({
                            url: jsonUrl,
                            success: function(data){

                                // these properties need to be set before spawning 'tabs' widgets
                                XNAT.tabs.container = $('#admin-config-tabs').find('div.content-tabs');
                                XNAT.tabs.layout = 'left';

                                // SPAWN THE TABS
                                var adminTabs = XNAT.spawner.spawn(data);

                                adminTabs.render(XNAT.tabs.container, 500);

                                // SAVE THE JSON
                                XNAT.app.adminTabs = adminTabs;

                            }
                        });

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

