<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<pg:restricted>

    <div id="page-body">
        <div class="pad">

            <div id="admin-page">
                <header id="content-header">
                    <h2 class="pull-left">Site Administration</h2>
                    <div class="clearfix"></div>
                </header>

                <!-- Admin tab container -->
                <div id="admin-config-tabs" class="content-tabs xnat-tab-container">

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

                </div>

                <script src="${sessionScope.siteRoot}/scripts/xnat/ui/templates.js"></script>
                <script src="${sessionScope.siteRoot}/page/admin/tabs.js"></script>

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
