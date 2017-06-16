/*!
 * Initialize XNAT settings
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

    var undef, init;

    XNAT.init = init = getObject(XNAT.init || {});

    XNAT.init.debug =
        getQueryStringValue('debug') ||
        getQueryStringValue('jsdebug');

    if (XNAT.init.debug === 'true') {
        console.log('/scripts/xnat/init.js');
    }

    // reset or remove cookies based on query string
    (function(){
        var cookiesParam = getQueryStringValue('cookies');
        var cookiePath = XNAT.url.context;
        if (/[*/~]/.test(cookiesParam.split('|')[1] || '')) {
            cookiePath = '/';
        }
        if (/^reset/i.test(cookiesParam)){
            XNAT.cookie.resetAll({ path: cookiePath })
        }
        if (/^remove/i.test(cookiesParam)){
            XNAT.cookie.removeAll({ path: cookiePath });
        }

    })();


    // this script has loaded
    init.loaded = true;

    return XNAT.init = init;

}));
