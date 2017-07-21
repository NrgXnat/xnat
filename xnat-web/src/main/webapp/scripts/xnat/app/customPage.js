/*
 * web: customPage.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Retrieve custom pages via AJAX
 * Used in: /xnat-templates/screens/Page.vm
 *
 * Page search order:
 *
 * (if a theme is active)
 * /themes/theme-name/pages/page-name.jsp
 * /themes/theme-name/pages/page-name/content.jsp
 * /themes/theme-name/pages/page-name/content.html
 *
 * (if no theme is set or if a page is not found in the theme)
 * /page/page-name/content.jsp
 * /page/page-name/content.html
 *
 */

var XNAT = getObject(XNAT);
XNAT.app = getObject(XNAT.app||{});

(function(factory){
    if (typeof define === 'function' && define.amd) {
        define(factory);
    }
    else if (typeof exports === 'object') {
        module.exports = factory();
    }
    else {
        return factory();
    }
}(function(){

    var customPage = XNAT.app.customPage =
        getObject(XNAT.app.customPage||{});

    XNAT.theme =
        getObject(XNAT.theme||{});

    var noneRegex = /^none$/i;

    var themeName = XNAT.themeName || XNAT.theme.name || '';

    // add resolved theme name to XNAT.* namespace
    XNAT.themeName = XNAT.theme.name = themeName;

    function isNoneTheme(name){
        var _theme = name || themeName;
        return noneRegex.test(_theme);
    }

    // get page name for CURRENT page /page/#/page-name
    customPage.getPageName = function(url, end){
        var loc = url || window.location.href;
        var urlParts = loc.split('/page/#/');
        var pageName = '';
        if (urlParts.length > 1) {
            pageName = urlParts[1].split(end||/\/#|#/)[0];
        }
        return pageName.replace(/^\/|\/$/g, '');
    };

    customPage.getName = function(end){
        var name = getQueryStringValue('view') ||
            getUrlHashValue('#view=', end) ||
            getUrlHashValue('#/', end) ||
            getUrlHash();
        return customPage.name = name;
    };

    customPage.getName();

    customPage.getPage = function(name, container){

        var pagePaths = [];
        var themePaths = [];
        var end = /\/#|#/;

        // special handling if using the 'none' theme
        var noneTheme = isNoneTheme(themeName);

        // use an array for the name param
        // to specify a start AND end for the page string
        if (Array.isArray(name)){
            end  = name[1] || end;
            name = name[0] || '';
        }

        name = name || customPage.getName(end);

        // don't even bother if there's no name
        if (!name) return;

        var $container = customPage.container || $$(container);

        function setPaths(pg){

            // remove leading and trailing slashes
            var _pg = pg.replace(/^\/+|\/+$/g, '');
            var paths = [];

            // if we're using a theme (that's not the default),
            // check that theme's folder
            if (themeName && !noneTheme) {
                // jsp theme files first
                paths.push('/themes/' + themeName + '/pages/' + _pg + '.jsp');
                paths.push('/themes/' + themeName + '/pages/' + _pg + '/content.jsp');
                // paths.push('/themes/' + themeName + '/pages/' + _pg + '.jsp');
                // html theme files next
                paths.push('/themes/' + themeName + '/pages/' + _pg + '/content.html');
                // paths.push('/themes/' + themeName + '/pages/' + _pg + '.html');
            }

            // then core jsp and html files
            paths.push('/page/' + _pg + '/content.jsp');
            paths.push('/page/' + _pg + '/content.html');

            return paths;

        }

        pagePaths = setPaths(name);

        console.log(pagePaths);

        function getPage(path){
            return XNAT.xhr.get({
                url: XNAT.url.rootUrl(path + window.location.search),
                dataType: 'html',
                success: function(content){
                    $container.html(content)
                }
            })
        }

        function lookForPage(i) {
            var not_found = 'page not found';
            if (i === pagePaths.length){
                not_found =  '<b>"' + customPage.getName() + '"</b> page not found';
                $container.html(not_found);
                // console.log("couldn't do it");
                return false;
            }
            // recursively try to get pages at different places
            getPage(pagePaths[i]).fail(function(){
                lookForPage(++i)
            });
        }

        // do the stuff
        lookForPage(0);

    };

    return XNAT.app.customPage = customPage;

}));
