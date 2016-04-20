/*!
 * Retrieve custom pages via AJAX
 * Used in: /xnat-templates/screens/Page.vm
 */
(function(NS, factory){
    if (typeof define === 'function' && define.amd) {
        define(factory);
    }
    else if (typeof exports === 'object') {
        module.exports = factory();
    }
    else {
        return factory(NS);
    }
}('app.customPage', function(NS, undefined){

    // setExtendedObject() hasn't been tested yet
    // var customPage = setExtendedObject(XNAT, NS);

    var customPage = getObject(eval('XNAT.'+NS)||{});

    customPage.getName = function(){
        var name = getQueryStringValue('view');
        name = name || getUrlHashValue('#view=');
        name = name || getUrlHash();
        return customPage.name = name;
    };

    customPage.getName();

    customPage.getPage = function(name, container){

        var pagePaths = [],
            themePaths = [];

        name = name || customPage.getName();

        // don't even bother if there's no name
        if (!name) return;

        var $container = customPage.container || $$(container);

        function getPage(path){
            return XNAT.xhr.get({
                url: XNAT.url.rootUrl(path),
                dataType: 'html',
                success: function(content){
                    $container.html(content)
                }
            })
        }

        var setPaths = function(pg, prefixes){
            var paths = [];
            pg = pg.replace(/^\/+|\/+$/g, ''); // remove leading and trailing slashes
            [].concat(prefixes).forEach(function(prefix){
                paths.push(prefix + '/' + pg + '/content.jsp');
                paths.push(prefix + '/' + pg + '.jsp');
                paths.push(prefix + '/' + pg + '/content.html');
                paths.push(prefix + '/' + pg + '.html');
                // paths.push(prefix + '/' + pg + '/'); // that could be dangerous
            });
            return paths;
        };

        pagePaths = setPaths(name, ['/page', '/pages']);

        // if we're using a theme, check that theme's folder
        if (XNAT.theme){
            themePaths = setPaths(name, [
                '/themes/' + XNAT.theme,
                '/themes/' + XNAT.theme + '/page',
                '/themes/' + XNAT.theme + '/pages'
            ]);
            pagePaths = themePaths.concat(pagePaths);
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
