<%--@elvariable id="siteConfigPreferences" type="org.nrg.xdat.preferences.SiteConfigPreferences"--%>
<%--@elvariable id="themeService" type="org.nrg.xdat.services.impl.ThemeServiceImpl"--%>
<%@ tag description="Document Skeleton" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<%--
  ~ web: xnat.tag
  ~ XNAT http://www.xnat.org
  ~ Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
  ~ All Rights Reserved
  ~
  ~ Released under the Simplified BSD.
  --%>

<%@ attribute name="page" %>
<%@ attribute name="title" %>
<%@ attribute name="headTop" %>
<%@ attribute name="headBottom" %>
<%@ attribute name="bodyTop" %>
<%@ attribute name="bodyBottom" %>

<head>

    <c:if test="${empty requestScope.hasInit}">
        <pg:init>
            <c:if test="${empty requestScope.hasVars}">
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
    <c:set var="versionString" value="v=1.7.3a"/>

    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="cache-control" content="max-age=0">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="-1">
    <meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT">

    <!-- load polyfills before ANY other JavaScript -->
    <script src="${SITE_ROOT}/scripts/polyfills.js"></script>

    <!-- XNAT global functions (no dependencies) -->
    <script src="${SITE_ROOT}/scripts/globals.js"></script>

    <!-- set global vars that are used often -->
    <script type="text/javascript">

        var XNAT = {};
        var serverRoot = '${SITE_ROOT}';
        var csrfToken = '${csrfToken}';
        //var showReason = typeof false != 'undefined' ? false : null;
        //var requireReason = typeof false != 'undefined' ? false : null;

        window.loggedIn = realValue(${sessionScope.loggedIn});

        XNAT.theme = {};
        XNAT.theme.name = '${themeService.theme.name}';
        XNAT.theme.path = '${themeService.theme.path}';
        XNAT.themeName = XNAT.theme.name;
        XNAT.themePath = XNAT.theme.path;

    </script>

    <!-- required libraries -->
    <script src="${SITE_ROOT}/scripts/lib/loadjs/loadjs.js"></script>
    <script src="${SITE_ROOT}/scripts/lib/jquery/jquery.js"></script>
    <script src="${SITE_ROOT}/scripts/lib/jquery/jquery-migrate.js"></script>
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

    <!-- other libraries -->
    <script src="${SITE_ROOT}/scripts/lib/spawn/spawn.js"></script>
    <script src="${SITE_ROOT}/scripts/lib/js.cookie.js"></script>
    <script src="${SITE_ROOT}/scripts/lib/yamljs/dist/yaml.js"></script>
    <script src="${SITE_ROOT}/scripts/lib/form2js/src/form2js.js"></script>
    <script src="${SITE_ROOT}/scripts/lib/ace/ace.js"></script>

    <!-- XNAT utility functions -->
    <script src="${SITE_ROOT}/scripts/utils.js"></script>

    <script type="text/javascript">

        if (window.loggedIn) {
            Cookies.set('guest', 'false', { path: '/' });
        }

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
        // all <select class="xnat-menu"> elements
        // will be converted
        // putting this here to be at the top of
        // the jQuery DOM-ready queue
        jq(function(){
            menuInit()
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

    <!-- Icon sets -->
    <link rel="stylesheet" type="text/css" href="${SITE_ROOT}/style/icons.css?${versionString}">

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
    <link type="text/css" rel="stylesheet" href="${SITE_ROOT}/scripts/lib/dateTimePicker/jquery.datetimepicker.min.css?${versionString}">
    <script src="${SITE_ROOT}/scripts/lib/dateTimePicker/jquery.datetimepicker.full.min.js"></script>

    <!-- XNAT JLAPI scripts -->
    <script src="${SITE_ROOT}/scripts/xnat/validate.js"></script>
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
    <script src="${SITE_ROOT}/scripts/xnat/ui/banner.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/ui/popup.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/ui/dialog.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/app/codeEditor.js"></script>

    <script src="${SITE_ROOT}/scripts/xnat/spawner.js"></script>

    ${headBottom}

</head>
<body id="xnat-app" class="xnat app">

${bodyTop}

<div id="user_bar">
    <div class="inner">

        <c:if test="${_user != '-' || sessionScope.isGuest}">

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
            <script src="${SITE_ROOT}/scripts/xnat/app/timeout.js"></script>

        </c:if>

        <div class="clear"></div>
    </div>
</div><!-- /user_bar -->

<c:if test="${_user != '-' && page != 'setup'}">

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
                <!-- Sequence: 11 -->
                <li class="hidden"><a id="browse" title="Browse" href="#Browse">Browse</a>
                    <ul class="" style="display:none; min-width: 120px;">
                        <!-- Browse/Default -->
                        <li class="hidden"><a href="#BrowseProjects">Projects</a>
                            <ul id="browse-projects">
                                <!-- Sequence: 10 -->
                            </ul>
                        </li>
                        <li class="hidden"><a href="#FavoriteProjects">Favorite Projects</a>
                            <ul id="favorite-projects">
                                <!-- Sequence: 20 -->
                            </ul>
                        </li>
                        <li class="hidden"><a href="#BrowseData">Data</a>
                            <ul id="browse-data">
                                <!-- Sequence: 30 -->
                            </ul>
                        </li>
                    </ul>
                </li>
                <!-- Sequence: 20 -->
                <li class="more"><a href="#new">New</a>
                    <ul class="" style="display: none;">
                        <!-- Sequence: 10 -->
                        <c:set var="hidden_li">
                            <li class="hidden">&nbsp;</li>
                        </c:set>
                            <%-- only allow admins to create projects for now --%>
                        <pg:restricted msg="${hidden_li}">
                            <li id="top-new-project">
                                <a href="${SITE_ROOT}/app/template/XDATScreen_add_xnat_projectData.vm">Project</a>
                            </li>
                        </pg:restricted>
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
                        <li><a href="${SITE_ROOT}/app/template/UploadOptions.vm">Images</a>
                            <ul>
                                <!-- Sequence: 10 -->
                                <!-- Images -->
                                <li><a href="${SITE_ROOT}/app/template/UploadAssistantPage.vm">Upload Assistant</a></li>
                                <li><a href="${SITE_ROOT}/app/template/CompressedUploaderPage.vm">Compressed Uploader</a></li>
                                <li><a href="${SITE_ROOT}/app/template/DICOMSCPPage.vm">DICOM SCP</a></li>
                            </ul>
                        </li>
                        <li><a href="${SITE_ROOT}/app/template/XMLUpload.vm">XML</a></li>
                        <li><a href="${SITE_ROOT}/app/template/XDATScreen_uploadCSV.vm">Spreadsheet</a></li>
                        <li><a href="${SITE_ROOT}/app/template/XDATScreen_prearchives.vm">Go to prearchive</a></li>
                    </ul>
                </li>

                <pg:restricted msg="<!-- non-admin -->">

                    <!-- Sequence: 40 -->
                    <li class="more"><a href="#adminbox">Administer</a>
                        <ul>
                            <!-- Sequence: 10 -->
                            <li><a href="${SITE_ROOT}/page/admin/">Site Administration</a></li>
                            <li><a href="${SITE_ROOT}/app/template/XDATScreen_admin.vm">Users</a></li>
                            <li><a href="${SITE_ROOT}/app/template/XDATScreen_groups.vm">Groups</a></li>
                            <li><a href="${SITE_ROOT}/app/template/XDATScreen_dataTypes.vm">Data Types</a></li>
                            <li><a href="${SITE_ROOT}/app/template/XDATScreen_email.vm">Email</a></li>
                            <li><a href="${SITE_ROOT}/app/template/XDATScreen_manage_pipeline.vm">Pipelines</a></li>
                            <li><a href="${SITE_ROOT}/app/template/Scripts.vm">Automation</a></li>
                            <li><a href="${SITE_ROOT}/app/template/XDATScreen_admin_options.vm">More...</a></li>
                        </ul>
                    </li>

                </pg:restricted>

                <!-- Title: Tools -->
                <!-- Sequence: 50 -->
                <!-- allowGuest: true -->

                <li class="more"><a href="#tools">Tools</a>
                    <ul>
                        <!-- Sequence: 10 -->
                        <!-- allowGuest: true -->
                        <li>
                            <a href="https://wiki.xnat.org/display/XTOOLS/XNAT+Desktop" target="_blank">XNAT Desktop (XND)</a>
                        </li>
                        <li>
                            <a href="http://nrg.wustl.edu/software/dicom-browser/" target="_blank">DICOM Browser</a>
                        </li>
                        <li>
                            <a href="https://wiki.xnat.org/display/XTOOLS" target="_blank">Command Prompt Tools</a>
                        </li>
                    </ul>
                </li>
                <!-- Sequence: 60 -->
                <li class="more"><a href="#help">Help</a>
                    <ul class="" style="display: none;">
                        <!-- Sequence: 10 -->
                        <!-- Home/Default -->
                        <li><a href="${SITE_ROOT}/app/template/ReportIssue.vm">Report a Problem</a></li>
                        <li><a href="http://wiki.xnat.org" target="_blank">Documentation</a></li>
                    </ul>
                </li>
            </ul>

            <!-- search script -->
            <script type="text/javascript">
                function submitQuickSearch(){
                    if ($('#searchValue').val() != "") {
                        $('#quickSearchForm').submit();
                    }
                }
            </script>
            <!-- end search script -->

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
                <input id="xnat_csrf" name="XNAT_CSRF" type="hidden" value="">
                <button type="button" id="search_btn" class="btn2" onclick="submitQuickSearch();">Go</button>

                <script>
                    var searchField = $('#searchValue');
                    searchField.keyup(function(event){
                        if (event.which == 13) {
                            submitQuickSearch();
                        }
                    });

                    $('#xnat_csrf').val(window.csrfToken);

                    searchField.each(function(){
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
                </script>
            </form>

            <!-- main_nav interactions -->
            <script type="text/javascript">

                (function(){

                    // cache it
                    var main_nav$ = jq('#main_nav ul.nav');

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
                                    var offsetL = sub$.closest('li').width();
                                    sub$.css({'left': offsetL});
                                });
                            }
                    ).on('mouseout',
                            function(){
                                var li$ = $(this);
                                fadeOutNav(li$);
                            }
                    );

                    // clicking the "Logout" link sets the warning bar cookie to 'OPEN' so it's available if needed on next login
                    jq('#logout_user').click(function(){
                        Cookies.set('WARNING_BAR', 'OPEN', {path: '/'});
                        Cookies.set('NOTIFICATION_MESSAGE', 'OPEN', {path: '/'});
                    });

                })();
            </script>
            <!-- end main_nav interactions -->

        </div>
        <!-- /.inner -->

    </div>
    <!-- /#main_nav -->

    <script src="${SITE_ROOT}/scripts/xnat/app/topnav-browse.js"></script>

    <script>

        function submitQuickSearch(){
            if ($('#searchValue').val() != "") {
                $('#quickSearchForm').submit();
            }
        }

        function loadMainNav(){

            // get Velocity-generated menu
            $('#main_nav').load('${SITE_ROOT}/app/template/Page.vm #main_nav > .inner', function(){


                $('#nav-home').css({
                    width: '30px',
                    backgroundImage: "url('${SITE_ROOT}/images/xnat-nav-logo-white-lg.png')",
                    backgroundRepeat: 'no-repeat',
                    backgroundSize: '32px',
                    backgroundPosition: 'center'
                });


                var searchField = $('#searchValue');
                searchField.keyup(function(event){
                    if (event.which == 13) {
                        submitQuickSearch();
                    }
                });

                $('#xnat_csrf').val(window.csrfToken);

                searchField.each(function(){
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


                // cache it
                var main_nav$ = jq('#main_nav ul.nav');

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
                                var offsetL = sub$.closest('li').width();
                                sub$.css({'left': offsetL});
                            });
                        }
                ).on('mouseout',
                        function(){
                            var li$ = $(this);
                            fadeOutNav(li$);
                        }
                );

                // clicking the "Logout" link sets the warning bar cookie to 'OPEN' so it's available if needed on next login
                jq('#logout_user').click(function(){
                    Cookies.set('WARNING_BAR', 'OPEN', {path: '/'});
                    Cookies.set('NOTIFICATION_MESSAGE', 'OPEN', {path: '/'});
                });

                // load js files from elements with 'data-loadjs' attributes
                $('[data-loadjs]').each(function(){
                    var jsAttr = this.dataset ? this.dataset.loadjs : this.getAttribute('data-loadjs');
                    var jsUrl = XNAT.url.rootUrl('/scripts/' + jsAttr + '.js');
                    loadjs(jsUrl);
                })

            });

        }

        loadMainNav();

    </script>

</c:if>

<div id="page_wrapper">

    <div id="header" class="main_header">
        <div class="pad">
            <a id="header_logo" href="${SITE_ROOT}/" style="display: none;" title="XNAT version Unknown">
                <img class="logo_img" src="<c:url value="${siteConfigPreferences.siteLogoPath}"/>" style="border:none;">
            </a>
        </div>
    </div>
    <!-- /header -->

    <script type="text/javascript">


        XNAT.app.adjustHeaderAndNavForLogoSize = function(){

            var header_logo$ = $('#header_logo');

            // adjust height of header if logo is taller than 65px
            var hdr_logo_height = header_logo$.height();
            if (hdr_logo_height > 65) {
                jq('.main_header').height(hdr_logo_height + 10);
            }

            //Commented out 2016/09/02 (XNAT-4501).  I don't think we want to do this (See home page when this takes effect)
            // adjust width of main nav if logo is wider than 175px
            //var hdr_logo_width = header_logo$.width();
            //if (hdr_logo_width > 175) {
            //    jq('#main_nav').width(932 - hdr_logo_width - 20);
            //}

            //
            //var recent_proj_height = jq('#min_projects_list > div').height();
            var recent_proj_height = 67;
            //jq('#min_projects_list, #min_expt_list').height(recent_proj_height * 5).css({'min-width':349,'overflow-y':'scroll'});

        };

        // initialize the advanced search method toggler
        XNAT.app.searchMethodToggler = function(parent$){

            parent$ = $$(parent$ || 'body');

            var INPUTS                = 'input, select, textarea, :input',
                SEARCH_METHOD_CKBOXES = 'input.search-method',
                searchGroups$         = parent$.find('div.search-group'),
                searchMethodInputs$   = parent$.find(SEARCH_METHOD_CKBOXES);

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

                var method    = this.value,
                    isChecked = this.checked;

                searchGroups$.addClass('disabled').find(INPUTS).not(SEARCH_METHOD_CKBOXES).changeVal('')
                        .prop('disabled', true).addClass('disabled');

                searchGroups$.filter('.' + method).removeClass('disabled').find(INPUTS).prop('disabled', false)
                        .removeClass('disabled');

                // update the radio buttons/checkboxes
                searchMethodInputs$.prop('checked', false);
                searchMethodInputs$.filter('.' + method).prop('checked', true);
                menuUpdate();
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

    <script>

        XNAT.app.customPage.container = $$('id=view-page');

        var body$ = $(document.body);

        body$.on('click', 'a[href*="#"]', function(e){
            e.preventDefault();
        });

        // navigate to new page when clicking a '/page/#/name/' link
        body$.on('click', 'a[href^="/page/#"]', function(e){
            e.preventDefault();
            var pageLink = this.href.split('page/#')[1];
            var hashPart = getUrlHash();
            if (pageLink !== hashPart) {
//                var viewPage = this.href.split('page/#/')[1].split('/#')[0];
                XNAT.app.customPage.getPage(pageLink, null, function(){
                    loadMainNav();
                    window.location.hash = pageLink;
                });
            }
        });

    </script>

    <div id="mylogger"></div>
</div>
<!-- /page_wrapper -->
<div class="clear"></div>
<div id="xnat_power"></div>

<script type="text/javascript">

    (function(){

        <c:import url="/xapi/siteConfig/buildInfo" var="buildInfo" scope="session"/>

        extend(true, XNAT, {
            data: {
                siteConfig: {
                    buildInfo: ${buildInfo}
                }
            }
        });

        var buildInfo = XNAT.data.siteConfig.buildInfo;

        var buildInfoSample = {
            "Application-Name": "XNAT",
            "Manifest-Version": "1.0",
            buildDate: "Sun Jun 05 12:41:24 CDT 2016",
            buildNumber: "Manual",
            commit: "v275-gd2220fd",
            version: "1.7.0"
        };

        XNAT.version = buildInfo.version;

        // add version to title attribute of XNAT logos
        var version = buildInfo.version + " build: " + buildInfo.buildNumber;

        var isNonRelease = /.*(SNAPSHOT|BETA|RC).*/i.test(buildInfo.version);

        if (isNonRelease) {
            version += " (" + buildInfo.commit + ")";
        }

        $('#xnat_power')
                .spawn('a.xnat-version', {
                    href: 'http://www.xnat.org',
                    target: '_blank',
                    title: 'XNAT version ' + version
                }, [['img|src=${SITE_ROOT}/images/xnat_power_small.png']])
                .spawn('small', 'version ' + version + (isNonRelease ? '<br>' + buildInfo.buildDate : ''));

        $('#header_logo').attr('title', 'XNAT version ' + version);

        XNAT.app.version = version;

        var clicker = XNAT.event.click('#header_logo, #xnat_power > a');

        // shift-click the header or footer XNAT logo to TOGGLE debug mode on/off
        clicker.shiftKey(function(e){
            e.preventDefault();
            if (Cookies.get('debug') === 'on') {
                Cookies.set('debug', 'off');
                window.location.hash = 'debug=off';
            }
            else {
                Cookies.set('debug', 'on');
                window.location.hash = 'debug=on';
            }
            window.location.reload();
        });

        // alt-shift-click to open the Swagger page in a new window
        clicker.altShift(function(e){
            e.preventDefault();
            XNAT.ui.popup(XNAT.url.rootUrl('/xapi/swagger-ui.html'));
        });

    })();

</script>

${bodyBottom}

</body>
