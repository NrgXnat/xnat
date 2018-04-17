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

    XNAT.app =
        getObject(XNAT.app || {});

    XNAT.app.customPage =
        getObject(XNAT.app.customPage || {});

    XNAT.theme =
        getObject(XNAT.theme || {});

    // add resolved theme name to XNAT.* namespace
    XNAT.themeName = XNAT.theme.name =
        XNAT.themeName || XNAT.theme.name || '';

    var themeName = XNAT.themeName;

    var customPage = XNAT.app.customPage;

    function isNoneTheme(name){
        var _theme = name || themeName;
        return /^none$/i.test(_theme);
    }

    // get page name for CURRENT page /page/#/page-name
    customPage.getPageName = function(url, end){
        var loc      = url || window.location.href;
        var urlParts = loc.split(/\/page\/#\/|#view=/);
        var pageName = '';
        if (urlParts.length > 1) {
            pageName = urlParts[1].split(end || /\/#|#/)[0];
        }
        return customPage.pageName =
            escapeHtml(pageName.replace(/^\/|\/$/g, ''));
    };

    customPage.getName = function(end){
        var name =
                getQueryStringValue('view') ||
                getUrlHashValue('#view=', end) ||
                getUrlHashValue('#/', end);
        return customPage.name = escapeHtml(name);
    };

    // cache name of current page on load
    customPage.getName();

    customPage.getPage = function(name, container){

        // save current page name for later comparison
        var currentPage = customPage.name;
        var pagePaths   = [];
        var end         = /\/#|#/;

        // special handling if using the 'none' theme
        var noneTheme = isNoneTheme(themeName);

        // use an array for the name param
        // to specify a start AND end for the page string
        if (Array.isArray(name)) {
            end  = name[1] || end;
            name = name[0] || '';
        }

        // if (name && name === currentPage) {
        //     ///// RETURN /////
        //     return currentPage;
        // }

        name = name || customPage.getName(end);

        // return if there's no name or it's '!'
        if (!name || name === '!') {
            ///// RETURN /////
            return currentPage;
        }

        var $container = $$(container || customPage.container || '#view-page').html('loading...');

        function setPaths(pg){

            // remove leading and trailing slashes
            var _pg   = pg.replace(/^\/+|\/+$/g, '');
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
            paths.push('/pages/view/' + _pg + '/content.jsp');
            paths.push('/pages/view/' + _pg + '/content.html');

            return paths;

        }

        pagePaths = setPaths(name);

        try {
            debugLog(pagePaths);
        }
        catch (e) {
            console.warn(e);
        }

        function getPage(path){
            $container.html('');
            return XNAT.xhr.get({
                url: XNAT.url.rootUrl(path + window.location.search),
                dataType: 'html',
                success: function(content){
                    $container.html(content)
                }
            })
        }

        function lookForPage(i){
            var not_found = 'page not found';
            if (i === pagePaths.length) {
                not_found = '<b>"' + customPage.getName() + '"</b> page not found';
                $container.html(not_found);
                return false;
            }
            // recursively try to get pages in possible locations
            getPage(pagePaths[i]).fail(function(){
                lookForPage(++i)
            });
        }

        // do the stuff
        lookForPage(0);

    };

    // update the page if necessary on hash change
    $(window).on('hashchange', function(e){
        var currentPage = customPage.name;
        var newPage = customPage.getName();
        //only get a new page if the page part has changed
        if (newPage && newPage !== '!' && newPage !== currentPage) {
            e.preventDefault();
            customPage.getPage(newPage);
        }
    });

    return XNAT.app.customPage = customPage;

}));
