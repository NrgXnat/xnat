<%@ tag description="Document Skeleton" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<%@ attribute name="page" %>
<%@ attribute name="title" %>
<%@ attribute name="headTop" %>
<%@ attribute name="headBottom" %>
<%@ attribute name="bodyTop" %>
<%@ attribute name="bodyBottom" %>

<head>

    <c:if test="${empty hasInit}">
        <pg:init>
            <c:if test="${empty hasVars}">
                <pg:jsvars/>
            </c:if>
        </pg:init>
    </c:if>

    ${headTop}

    <title>${empty title ? 'XNAT' : title}</title>

    <c:set var="SITE_ROOT" value="${sessionScope.siteRoot}"/>
    <%--<c:set var="_scripts" value="${SITE_ROOT}/scripts"/>--%>
    <%--<c:set var="_scriptsLib" value="${SITE_ROOT}/scripts/lib"/>--%>
    <c:set var="csrfToken" value="${sessionScope.csrfToken}"/>
    <c:set var="_user" value="${sessionScope.username}"/>
    <c:set var="versionString" value="v=1.7.0a2"/>

    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="cache-control" content="max-age=0">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="-1">
    <meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT">

    <!-- load polyfills before ANY other JavaScript -->
    <script src="${SITE_ROOT}/scripts/polyfills.js"></script>

    <!-- set global vars that are used often -->
    <script type="text/javascript">

        var XNAT = {};
        var serverRoot = '${SITE_ROOT}';
        var csrfToken = '${csrfToken}';
        //var showReason = typeof false != 'undefined' ? false : null;
        //var requireReason = typeof false != 'undefined' ? false : null;

    </script>

    <!-- XNAT global functions (no dependencies) -->
    <script src="${SITE_ROOT}/scripts/globals.js"></script>

    <!-- required libraries -->
    <script src="${SITE_ROOT}/scripts/lib/loadjs/loadjs.js"></script>
    <script src="${SITE_ROOT}/scripts/lib/jquery/jquery.min.js"></script>
    <script src="${SITE_ROOT}/scripts/lib/jquery/jquery-migrate-1.2.1.min.js"></script>
    <script type="text/javascript">
        // use 'jq' to avoid _possible_ conflicts with Velocity
        var jq = jQuery;
    </script>

    <!-- jQuery plugins -->
    <link rel="stylesheet" type="text/css" href="${SITE_ROOT}/scripts/lib/jquery-plugins/chosen/chosen.min.css?${versionString}">
    <script src="${SITE_ROOT}/scripts/lib/jquery-plugins/chosen/chosen.jquery.min.js"></script>
    <script src="${SITE_ROOT}/scripts/lib/jquery-plugins/jquery.maskedinput.min.js"></script>
    <script src="${SITE_ROOT}/scripts/lib/jquery-plugins/jquery.hasClasses.js"></script>
    <script src="${SITE_ROOT}/scripts/lib/jquery-plugins/jquery.dataAttr.js"></script>
    <script src="${SITE_ROOT}/scripts/lib/jquery-plugins/jquery.form.js"></script>

    <%-- probably not going to use the jquery spawner --%>
    <%--<script src="${SITE_ROOT}/scripts/lib/jquery-plugins/jquery.spawn.js"></script>--%>

    <!-- other libraries -->
    <script src="${SITE_ROOT}/scripts/lib/spawn/spawn.js"></script>
    <script src="${SITE_ROOT}/scripts/lib/js.cookie.js"></script>
    <script src="${SITE_ROOT}/scripts/lib/yamljs/dist/yaml.js"></script>


    <%--<script src="${SITE_ROOT}/scripts/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/event/event-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/container/container-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/menu/menu-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/element/element-beta-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/button/button-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/connection/connection-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/treeview/treeview-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/cookie/cookie-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/tabview/tabview-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/datasource/datasource-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/resize/resize-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/dragdrop/dragdrop-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/datatable/datatable-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/paginator/paginator-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/json/json-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/xnat_loader.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/LeftBarTreeView.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/justification/justification.js"></script>--%>

    <!-- XNAT utility functions -->
    <script src="${SITE_ROOT}/scripts/utils.js"></script>

    <script type="text/javascript">

        /*
         * XNAT global namespace object, which will not be overwriten if
         * already defined. Also define some other top level namespaces.
         */
        extend(XNAT, {
            /*
             * Parent namespace that templates can use to put their
             * own namespace
             */
            app: {
                displayNames: {
                    singular: {
                        project: "Project",
                        subject: "Subject",
                        imageSession: "Session",
                        mrSession: "MR Session"
                    },
                    plural: {
                        project: "Projects",
                        subject: "Subjects",
                        imageSession: "Sessions",
                        mrSession: "MR Sessions"
                    }
                },
                siteId: "XNAT"
            },
            data: {
                context: {}
            }
        });

    </script>
    <script type="text/javascript">
        // initialize "Chosen" menus on DOM load
        // all <select class="chosen-menu"> elements
        // will be converted
        // putting this here to be at the top of
        // the jQuery DOM-ready queue
        jq(function(){
            chosenInit()
        });
    </script>

    <script type="text/javascript">

        XNAT.dom = getObject(XNAT.dom || {});
        XNAT.dom.addFormCSRF = function($form){
            $form = $$($form || 'form');
            $form.each(function(form){
                var form$ = $(form);
                // don't add the hidden input twice
                if (!form$.has('input[name="XNAT_CSRF"]')) {
                    form$.append('<input type="hidden" name="XNAT_CSRF" value="' + csrfToken + '">');
                }
            });
        };

        jq(function(){
            // add hidden input with CSRF data
            // to all forms on page load
            XNAT.dom.addFormCSRF();
        });

    </script>

    <!-- YUI css -->
    <%--<link rel="stylesheet" type="text/css" href="${SITE_ROOT}/scripts/yui/build/assets/skins/sam/skin.css?v=1.7.0a1">--%>

    <!-- xdat.css and xnat.css loaded last to override YUI styles -->
    <link rel="stylesheet" type="text/css" href="${SITE_ROOT}/style/app.css?${versionString}">

    <%-- styles for tabbed interface --%>
    <%-- TODO: rename and move file or integrate it into app.css --%>
    <link rel="stylesheet" type="text/css" href="${SITE_ROOT}/page/admin/style.css?${versionString}">


    <!-- legacy XNAT scripts -->
    <link rel="stylesheet" type="text/css" href="${SITE_ROOT}/scripts/xmodal-v1/xmodal.css?${versionString}">
    <script src="${SITE_ROOT}/scripts/xmodal-v1/xmodal.js"></script>
    <script src="${SITE_ROOT}/scripts/xmodal-v1/xmodal-migrate.js"></script>

    <%--<link rel="stylesheet" type="text/css" href="${SITE_ROOT}/scripts/tabWrangler/tabWrangler.css?${versionString}1">--%>
    <%--<script src="${SITE_ROOT}/scripts/tabWrangler/tabWrangler.js"></script>--%>

    <!-- date input stuff -->
    <%--<link type="text/css" rel="stylesheet" href="${SITE_ROOT}/scripts/yui/build/calendar/assets/skins/sam/calendar.css?${versionString}">--%>
    <%--<script src="${SITE_ROOT}/scripts/yui/build/calendar/calendar-min.js"></script>--%>
    <%--<script src="${SITE_ROOT}/scripts/ezCalendar.js"></script>--%>

    <!-- XNAT JLAPI scripts -->
    <script src="${SITE_ROOT}/scripts/xnat/url.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/xhr.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/event.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/element.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/ui/templates.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/ui/input.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/ui/select.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/ui/table.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/ui/panel.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/ui/tabs.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/ui/popup.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/ui/dialog.js"></script>

    <script src="${SITE_ROOT}/scripts/xnat/spawner.js"></script>

    <script src="${SITE_ROOT}/scripts/timeLeft.js"></script>

    ${headBottom}

</head>
<body id="xnat-app" class="xnat app">

${bodyTop}

<div id="user_bar">
    <div class="inner">

        <c:if test="${_user != '-'}">

            <img id="attention_icon" src="${SITE_ROOT}/images/attention.png" style="display:none;" alt="attention needed - click for more info" title="attention needed - click for more info">
            <span id="user_info">Logged in as: &nbsp;<a href="${SITE_ROOT}/app/template/XDATScreen_UpdateUser.vm">${_user}</a> <b>|</b>
                <span class="tip_icon" style="margin-right:3px;left:2px;top:3px;">
                    <span class="tip shadowed" style="top:20px;z-index:10000;white-space:normal;left:-150px;width:300px;background-color:#ffc;">
                        Your XNAT session will auto-logout after a certain period of inactivity.
                        You can reset the timer without reloading thepage by clicking "renew."
                    </span>
                </span>
                Auto-logout in:
                <b id="timeLeft">-:--:--</b> -
                <a id="timeLeftRenew" href="#!">renew</a>
                <b>|</b>
                <a id="logout_user" href="${SITE_ROOT}/app/action/LogoutUser">Logout</a>
            </span>
            <%--<script type="text/javascript">--%>
            <%--$('#timeLeftRenew').click(XNAT.app.timeout.handleOk);--%>
            <%--Cookies.set('guest', 'false', {path: '/'});--%>
            <%--</script>--%>

        </c:if>

        <div class="clear"></div>
    </div>
</div><!-- /user_bar -->

<c:if test="${_user != '-' && page != 'setup'}">

    <div id="main_nav">
        <div class="inner">

            <ul class="nav">
                <!-- Sequence: 10 -->
                <!-- allowGuest: true -->
                <li>
                    <a id="nav-home" title="Home" href="${SITE_ROOT}/">&nbsp;</a>
                    <script>
                        $('#nav-home').css({
                            width: '30px',
                            backgroundImage: "url('${SITE_ROOT}/images/xnat-nav-logo-white-lg.png')",
                            backgroundRepeat: 'no-repeat',
                            backgroundSize: '32px',
                            backgroundPosition: 'center'
                        });
                    </script>
                </li>
                <!-- Sequence: 20 -->
                <li class="more"><a href="#new">New</a>
                    <ul class="" style="display: none;">
                        <!-- Sequence: 10 -->
                        <li><a href="${SITE_ROOT}/app/template/XDATScreen_add_xnat_projectData.vm">Project</a></li>
                        <li>
                            <a href="${SITE_ROOT}/app/action/XDATActionRouter/xdataction/edit/search_element/xnat:subjectData">Subject</a>
                        </li>
                        <li><a href="${SITE_ROOT}/app/template/XDATScreen_add_experiment.vm">Experiment</a></li>
                    </ul>
                </li>
                <!-- Sequence: 30 -->
                <li class="more"><a href="#upload">Upload</a>
                    <ul>
                        <!-- Sequence: 10 -->
                        <!-- Upload/Default -->
                        <li><a href="${SITE_ROOT}/app/template/LaunchUploadApplet.vm">Images</a></li>
                        <li><a href="${SITE_ROOT}/app/template/XMLUpload.vm">XML</a></li>
                        <li><a href="${SITE_ROOT}/app/template/XDATScreen_uploadCSV.vm">Spreadsheet</a></li>
                        <li><a href="${SITE_ROOT}/app/template/XDATScreen_prearchives.vm">Go to prearchive</a></li>
                    </ul>
                </li>


                <c:if test="${isAdmin == true}">
                    <!-- Sequence: 40 -->
                    <li class="more"><a href="#adminbox">Administer</a>
                        <ul>
                            <!-- Sequence: 10 -->
                            <li><a href="${SITE_ROOT}/app/template/XDATScreen_admin.vm">Users</a></li>
                            <li><a href="${SITE_ROOT}/app/template/XDATScreen_groups.vm">Groups</a></li>
                            <li><a href="${SITE_ROOT}/app/template/XDATScreen_dataTypes.vm">Data Types</a></li>
                            <li><a href="${SITE_ROOT}/app/template/XDATScreen_email.vm">Email</a></li>
                            <li><a href="${SITE_ROOT}/app/template/XDATScreen_manage_pipeline.vm">Pipelines</a></li>
                            <li><a href="${SITE_ROOT}/page/admin/">Configuration</a></li>
                            <li><a href="${SITE_ROOT}/app/template/Scripts.vm">Automation</a></li>
                            <li><a href="${SITE_ROOT}/app/template/XDATScreen_admin_options.vm">More...</a></li>
                        </ul>
                    </li>
                </c:if>


                <!-- Title: Tools -->
                <!-- Sequence: 50 -->
                <!-- allowGuest: true -->

                <li class="more"><a href="#tools">Tools</a>
                    <ul>
                        <!-- Sequence: 10 -->
                        <!-- allowGuest: true -->
                        <li>
                            <a href="https://wiki.xnat.org/display/XNAT16/XNAT+Desktop" target="_blank">XNAT Desktop (XND)</a>
                        </li>
                        <li>
                            <a href="http://nrg.wustl.edu/projects/DICOM/DicomBrowser.jsp" target="_blank">DICOM Browser</a>
                        </li>
                        <li>
                            <a href="https://wiki.xnat.org/display/XNAT16/XNAT+Client+Tools" target="_blank">Command Prompt Tools</a>
                        </li>
                    </ul>
                </li>
                <!-- Sequence: 60 -->
                <li class="more"><a href="#help">Help</a>
                    <ul class="" style="display: none;">
                        <!-- Sequence: 10 -->
                        <!-- Home/Default -->
                        <li><a href="${SITE_ROOT}/app/template/ReportIssue.vm">Report a Problem</a></li>
                        <li><a href="http://wiki.xnat.org/display/XNAT16/Home" target="_blank">Documentation</a></li>
                    </ul>
                </li>
            </ul>

            <!-- search script -->
            <script type="text/javascript">
                <!--
                function DefaultEnterKey(e, button){
                    var keynum, keychar, numcheck;

                    if (window.event) // IE
                    {
                        keynum = e.keyCode;
                        if (keynum == 13) {
                            submitQuickSearch();
                            return true;
                        }
                    }
                    else if (e) // Netscape/Firefox/Opera
                    {
                        keynum = e.which;
                        if (keynum == 13) {
                            submitQuickSearch();
                            return false;
                        }
                    }
                    return true;
                }

                function submitQuickSearch(){
                    concealContent();
                    if (document.getElementById('quickSearchForm').value != "")
                        document.getElementById('quickSearchForm').submit();
                }

                //-->
            </script>
            <!-- end search script -->

            <style type="text/css">
                #quickSearchForm .chosen-results {
                    max-height: 500px;
                }

                #quickSearchForm .chosen-results li {
                    padding-right: 20px;
                    white-space: nowrap;
                }

                #quickSearchForm .chosen-container .chosen-drop {
                    width: auto;
                    min-width: 180px;
                    max-width: 360px;
                }

                #quickSearchForm .chosen-container .chosen-drop .divider {
                    padding: 0;
                    overflow: hidden;
                }
            </style>

            <form id="quickSearchForm" method="post" action="${SITE_ROOT}/app/action/QuickSearchAction">
                <select id="stored-searches" data-placeholder="Stored Searches" style="display: none;">
                    <option></option>
                    <optgroup>
                        <option value="${SITE_ROOT}/app/template/XDATScreen_search_wizard1.vm">Advanced Search…</option>
                    </optgroup>
                    <optgroup class="stored-search-list">
                        <option disabled="">(no stored searches)</option>
                        <!-- stored searches will show up here -->
                    </optgroup>
                </select>
                <input id="searchValue" class="clean" name="searchValue" type="text" maxlength="40" size="20" value="">
                <button type="button" id="search_btn" class="btn2" onclick="submitQuickSearch();">Go</button>

                <script>

                    $('#searchValue').each(function(){
                        var _this = this;
                        _this.value = _this.value || 'search';
                        $(_this).focus(function(){
                            $(_this).removeClass('clean');
                            if (!_this.value || _this.value === 'search') {
                                _this.value = '';
                            }
                        })
                    });

                    $('#stored-searches').on('change', function(){
                        if (this.value) {
                            window.location.href = this.value;
                        }
                    }).chosen({
                        width: '150px',
                        disable_search_threshold: 9,
                        inherit_select_classes: true,
                        placeholder_text_single: 'Stored Searches',
                        search_contains: true
                    });


                    window.logged_in = true;

                </script>
            </form>

            <!-- main_nav interactions -->
            <script type="text/javascript">

                (function(){

                    // cache it
                    var main_nav$ = jq('#main_nav ul.nav');

                    var body$ = jq('body');

                    var cover_up_count = 1;

                    function coverApplet(el$){
                        var cover_up_id = 'cover_up' + cover_up_count++;
                        var jqObjPos = el$.offset(),
                                jqObjLeft = jqObjPos.left,
                                jqObjTop = jqObjPos.top,
                                jqObjMarginTop = el$.css('margin-top'),
                                jqObjWidth = el$.outerWidth() + 4,
                                jqObjHeight = el$.outerHeight() + 2;

                        el$.before('<iframe id="' + cover_up_id + '" class="applet_cover_up" src="about:blank" width="' + jqObjWidth + '" height="' + jqObjHeight + '"></iframe>');

                        jq('#' + cover_up_id).css({
                            display: 'block',
                            position: 'fixed',
                            width: jqObjWidth,
                            height: jqObjHeight,
                            marginTop: jqObjMarginTop,
                            left: jqObjLeft,
                            top: jqObjTop,
                            background: 'transparent',
                            border: 'none',
                            outline: 'none'
                        });
                    }

                    function unCoverApplets(el$){
                        el$.prev('iframe.applet_cover_up').detach();
                    }

                    function fadeInNav(el$){
//            el$.stop('clearQueue','gotoEnd');
                        el$.find('> ul').show().addClass('open');
                    }

                    function fadeOutNav(el$){
//            el$.stop('clearQueue','gotoEnd');
                        el$.find('> ul').hide().removeClass('open');
                    }

                    // give menus with submenus a class of 'more'
                    main_nav$.find('li ul, li li ul').closest('li').addClass('more');
                    main_nav$.find('li li ul').addClass('subnav');

                    // no fancy fades on hover
                    main_nav$.find('li.more').on('mouseover',
                            function(){
                                var li$ = $(this);
                                fadeInNav(li$);
                                //jq('#main_nav li').removeClass('open');
                                li$.find('ul.subnav').each(function(){
                                    var sub$ = $(this);
                                    var offsetL = sub$.closest('ul').outerWidth();
                                    sub$.css({ 'left': offsetL + -25 })
                                });
                                if (body$.hasClass('applet')) {
                                    coverApplet(li$.find('> ul'));
                                }
                            }
                    ).on('mouseout',
                            function(){
                                var li$ = $(this);
                                fadeOutNav(li$);
                                if (body$.hasClass('applet')) {
                                    unCoverApplets(li$.find('> ul'));
                                }
                            }
                    );

                    // clicking the "Logout" link sets the warning bar cookie to 'OPEN' so it's available if needed on next login
                    jq('#logout_user').click(function(){
                        Cookies.set('WARNING_BAR', 'OPEN', { path: '/' });
                        Cookies.set('NOTIFICATION_MESSAGE', 'OPEN', { path: '/' });
                    });

                })();
            </script>
            <!-- end main_nav interactions -->

        </div>
        <!-- /.inner -->

    </div>
    <!-- /#main_nav -->

</c:if>

<div id="page_wrapper">

    <div id="header" class="main_header">
        <div class="pad">
            <a id="header_logo" href="${SITE_ROOT}/" style="display: none;" title="XNAT version Unknown">
                <img class="logo_img" src="${SITE_ROOT}/images/logo.png" style="border:none;">
            </a>
        </div>
    </div>
    <!-- /header -->

    <script type="text/javascript">

        (function(){

            var header_logo$ = $('#header_logo');

            // adjust height of header if logo is taller than 65px
            var hdr_logo_height = header_logo$.height();
            if (hdr_logo_height > 65) {
                jq('.main_header').height(hdr_logo_height + 10);
            }

            // adjust width of main nav if logo is wider than 175px
            var hdr_logo_width = header_logo$.width();
            if (hdr_logo_width > 175) {
                jq('#main_nav').width(932 - hdr_logo_width - 20);
            }

            //
            //var recent_proj_height = jq('#min_projects_list > div').height();
            var recent_proj_height = 67;
            //jq('#min_projects_list, #min_expt_list').height(recent_proj_height * 5).css({'min-width':349,'overflow-y':'scroll'});

        })();

        // initialize the advanced search method toggler
        XNAT.app.searchMethodToggler = function(parent$){

            parent$ = $$(parent$ || 'body');

            var INPUTS = 'input, select, textarea, :input',
                    SEARCH_METHOD_CKBOXES = 'input.search-method',
                    searchGroups$ = parent$.find('div.search-group'),
                    searchMethodInputs$ = parent$.find(SEARCH_METHOD_CKBOXES);

            // disable 'by-id' search groups by default
            searchGroups$.filter('.by-id').addClass('disabled').find(INPUTS).not(SEARCH_METHOD_CKBOXES).changeVal('')
                         .prop('disabled', true).addClass('disabled');

            // enable 'by-criteria' search groups by default
            searchGroups$.filter('.by-criteria').removeClass('disabled').find(INPUTS).prop('disabled', false)
                         .removeClass('disabled');

            // check 'by-criteria' checkboxes
            searchMethodInputs$.filter('.by-criteria').prop('checked', true);

            // don't add multiple click handlers
            searchMethodInputs$.off('click');

            // toggle the search groups
            searchMethodInputs$.on('click', function(){

                var method = this.value,
                        isChecked = this.checked;

                searchGroups$.addClass('disabled').find(INPUTS).not(SEARCH_METHOD_CKBOXES).changeVal('')
                             .prop('disabled', true).addClass('disabled');

                searchGroups$.filter('.' + method).removeClass('disabled').find(INPUTS).prop('disabled', false)
                             .removeClass('disabled');

                // update the radio buttons/checkboxes
                searchMethodInputs$.prop('checked', false);
                searchMethodInputs$.filter('.' + method).prop('checked', true);
                chosenUpdate();
            });
        };

    </script>

    <div id="tp_fm"></div>

    <div id="breadcrumbs"></div>
    <script src="${SITE_ROOT}/scripts/xnat/ui/breadcrumbs.js"></script>
    <script language="javascript">
        window.isProjectPage = (XNAT.data.context.xsiType === 'xnat:projectData');
        // wrap it up to keep things
        // out of global scope
        (function(){
            var crumbs = [];
            XNAT.ui.breadcrumbs.render('#breadcrumbs', crumbs);
        })();
    </script>

    <div id="layout_content2" style="display:none;">Loading...</div>
    <div id="layout_content">
        <!--BEGIN SCREEN CONTENT -->
        <!-- start xnat-templates/screens/Page.vm -->
        <script src="${SITE_ROOT}/scripts/xnat/app/customPage.js"></script>

        <div id="view-page">
            <!--   BODY START   -->


            <jsp:doBody/>


            <!--  BODY END  -->
        </div>

        <!-- end xnat-templates/screens/Page.vm -->
        <!--END SCREEN CONTENT -->
    </div>

    <div id="mylogger"></div>
</div>
<!-- /page_wrapper -->

<div id="xnat_power">
    <a target="_blank" href="http://www.xnat.org/" style="" title="XNAT Version 1.7"><img
            src="${SITE_ROOT}/images/xnat_power_small.png"></a>
    <small>version 1.7</small>
</div>

<script type="text/javascript">

    loadjs(scriptUrl('xnat/event.js'), function(){

        // shift-click the header or footer XNAT logo to TOGGLE debug mode on/off
        // alt-shift-click to open the Swagger page in a new window
        XNAT.event.click('#header_logo, #xnat_power > a')
            .shiftKey(function(e){
                e.preventDefault();
                if (Cookies.get('debug') === 'on'){
                    window.location.hash = 'debug=off';
                }
                else {
                    window.location.hash = 'debug=on';
                }
                window.location.reload();
            })
            .altShift(function(e){
                e.preventDefault();
                XNAT.ui.popup(XNAT.url.rootUrl('/xapi/swagger-ui.html'));
            });

    })

</script>
<%--<script src="${SITE_ROOT}/scripts/footer.js"></script>--%>

<div id="xmodal-loading" style="position:fixed;left:-9999px;top:-9999px;">
    <img src="${SITE_ROOT}/scripts/xmodal-v1/loading_bar.gif" alt="loading">
</div>

${bodyBottom}

</body>