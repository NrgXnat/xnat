<%@ tag description="Body Header" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
  ~ web: header.tag
  ~ XNAT http://www.xnat.org
  ~ Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
  ~ All Rights Reserved
  ~
  ~ Released under the Simplified BSD.
  --%>

<div id="old-ie">
    <!--[if lt IE 7]><p class="browsehappy">You are using an extremely
    <strong>outdated</strong> browser that is not supported. Please
    <a href="http://browsehappy.com/">upgrade your browser</a> for complete functionality.</p><![endif]-->
    <!--[if IE 8]><p class="browsehappy">Internet Explorer 8 and earlier are not officially supported. Please
    <a href="http://browsehappy.com/">upgrade your browser</a> to ensure proper functionality.</p><![endif]-->
</div>
<!-- /#old-ie -->

<nav id="top-bar" class="navbar navbar-inverse navbar-fixed-top" role="navigation">

    <div id="user-bar" style="">

        <div class="inner" style="width:960px;margin:0 auto;color:#fff !important;">
            <span id="user-last-login" class="pull-left">Last login: 06/16/2014 16:22:53</span>
            <span id="user-login-info" class="pull-right">
                Logged in as: <a href="#" class="u">username</a> <b>|</b>
                <span id="about-user-timeout" class="hover-tooltip glyphicon glyphicon-info-sign"
                      data-toggle="tooltip"
                      data-placement="bottom"
                      title='Your XNAT session will auto-logout after a certain period of inactivity. You can reset that timer without reloading the page by clicking "Renew."'></span> &nbsp;
                Auto-logout in: <span class="mono"> 0:15:00</span>
                <b>|</b> <a href="#" class="u">Renew</a>
                <b>|</b> <a href="#" class="u">Logout</a>
            </span>
        </div>

    </div>

    <div class="container-fluid">

        <ul class="nav navbar-nav">

            <li class="active"><a href="#" class="glyphicon glyphicon-home"></a></li>

            <li id="nav-tools" class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Tools <b class="caret"></b></a>
                <ul class="dropdown-menu" role="menu">
                    <li><a href="#">XNAT Desktop (XND)</a></li>
                    <li><a href="#">Dicom Browser</a></li>
                    <li><a href="#">Command-line Tools</a></li>
                    <li class="divider"></li>
                    <li><a href="#">More...</a></li>
                </ul>
            </li>

            <li id="nav-admin" class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Admin <b class="caret"></b></a>
                <ul class="dropdown-menu" role="menu">
                    <li><a href="#">Site Administration</a></li>
                    <li><a href="#">Users</a></li>
                    <li><a href="#">Data Types</a></li>
                    <li><a href="#">Pipelines</a></li>
                    <li><a href="#">Email</a></li>
                    <li class="divider"></li>
                    <li><a href="#">More...</a></li>
                </ul>
            </li>

            <li id="nav-help" class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                    Help <b class="caret"></b>
                    <!-- <span class="glyphicon glyphicon-question-sign"></span> -->
                </a>
                <ul class="dropdown-menu" role="menu">
                    <li><a href="#">Report a Problem</a></li>
                    <li><a href="#">Documentation</a></li>
                </ul>
            </li>

        </ul>

        <script>
            (function(){

                function joinClasses(classes){
                    var i=-1, len = arguments.length,
                            newClass=[];
                    while (++i < len){
                        newClass = newClass.concat(arguments[i]);
                    }
                    // remove multiple spaces and trim
                    // leading and trailing spaces
                    return newClass.join(' ').split(/\s+/).join(' ').trim();
                }

                // master constructor
                function Nav(opts){

                    var ul = {
                        tag: 'ul',
                        className: joinClasses('nav navbar-nav', opts.className||'')
                    };

                    this.container = this.ul =
                            spawn.element(extend(true, {}, opts, ul));

                }

                Nav.container = function(id, className, opts){
                    opts = opts || {};
                    if (id) opts.id = id;
                    if (className) opts.className = className;
                    var nav = new Nav(opts);
                    return nav.container;
                };


                function subNav(items){
                    var ul = spawn('ul');
                    ul.className = 'dropdown-menu';
                    ul.setAttribute('role', 'menu');
                }


                // spawn top-level nav <li> elements
                Nav.topItem = function(label, href, id, className, subNav){

                    var li, a;

                    li = spawn.element('li', {
                        id: id || 'nav-' + toDashed(label),
                        className: 'dropdown'
                    });

                    a = spawn.element('a', {
                        href: href || '#!',
                        className: 'dropdown-toggle',
                        attr: { 'data-toggle': 'dropdown' }
                    }, label);

                    if (subNav){
                        a.appendChild(spawn.element('b.caret'));
                        li.appendChild(Nav.subNav(subNav))
                    }
                };

                Nav.navBar = function(items, opts){

                    var nav = new Nav(opts);

                    [].concat(items).forEach(function(item, i){

                        nav.appendChild()

                    });

                }


            })();
        </script>



        <form class="navbar-form navbar-right" role="search">

            <div class="btn-group">
                <button
                        type="button"
                        class="btn btn-info btn-sm dropdown-toggle"
                        data-toggle="dropdown"
                        style="padding:2px 8px 2px 6px;">
                    <a href="#" id="saved-searches" class="glyphicon glyphicon-folder-open"></a>
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu" role="menu">
                    <li><a href="#">Search 1</a></li>
                    <li><a href="#">Search 2</a></li>
                    <li><a href="#">Search 3</a></li>
                    <li><a href="#">Search 4</a></li>
                    <li><a href="#">Search 5</a></li>
                    <li><a href="#">Search 6</a></li>
                    <li class="divider"></li>
                    <li><a href="#">More...</a></li>
                </ul>
            </div>

            <div class="form-group" style="padding:0 5px;">
                <input type="text" class="form-control" placeholder="search">
            </div>

            <button type="submit" class="btn btn-default" style="padding:3px 8px 4px;">Go</button>

        </form>

    </div>

</nav>
