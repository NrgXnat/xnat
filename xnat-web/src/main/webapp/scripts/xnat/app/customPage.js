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
    //var customPage = setExtendedObject(XNAT, NS);

    var customPage = getObject(XNAT.app.customPage||{});
    
    customPage.getName = function(){
        var name = getQueryStringValue('view');
        name = name || getUrlHashValue('#view=');
        name = name || getUrlHashString();
        return name;
    };

    customPage.name = customPage.getName();

    customPage.getPage = function(name, container){

        var pagePaths = [],
            themePaths = [];

        name = name || customPage.getName();

        // don't even bother if there's no name
        if (!name) return;

        var container$ = customPage.container || $$(container);

        function getPage(path){
            return XNAT.xhr.get({
                url: XNAT.url.rootUrl(path),
                dataType: 'html',
                success: function(content){
                    container$.html(content);
                }
            })
        }

        var setPaths = function(pg, prefixes){
            var paths = [];
            [].concat(prefixes).forEach(function(prefix){
                paths.push(prefix + '/' + pg + '.jsp');
                paths.push(prefix + '/' + pg + '.html');
            });
            return paths;
        };

        pagePaths = setPaths(name, ['/pages', '/page']);

        // if we're using a theme, check that theme's folder
        if (XNAT.theme){
            themePaths = setPaths(name, [
                '/themes/' + XNAT.theme,
                '/themes/' + XNAT.theme + '/pages',
                '/themes/' + XNAT.theme + '/page'
            ]);
            pagePaths = themePaths.concat(pagePaths);
        }

        function lookForPage(i) {
            if (i === pagePaths.length){
                console.log("couldn't do it");
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

    return customPage;

}));
