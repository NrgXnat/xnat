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
        var cookiePath = XNAT.url.rootUrl();
        if (/[*/~]/.test(cookiesParam.split('|')[1] || '')) {
            cookiePath = '/';
        }
        if (/^reset/i.test(cookiesParam)) {
            XNAT.cookie.resetAll({ path: cookiePath })
        }
        if (/^remove/i.test(cookiesParam)) {
            XNAT.cookie.removeAll({ path: cookiePath });
        }

    })();


    // add build info to page elements
    $(function(){

        console.log('buildInfo');

        // add version to title attribute of XNAT logos
        if (window.top.loggedIn !== undef && window.top.loggedIn === true) {

            XNAT.cookie.set('SESSION_ACTIVE', 'true');

            XNAT.xhr.get(serverRoot + '/xapi/siteConfig/buildInfo', function(data){

                var version = XNAT.version = XNAT.data.version = data.version;
                var version_string = version + ', build: ' + data.buildNumber;
                var version_title = 'XNAT version ' + version_string;
                var isNonRelease = /SNAPSHOT|BETA|RC/ig.test(version);
                var build_string = ' ';

                if (isNonRelease) {
                    version_string += ' (' + data.commit + ')';
                    build_string += '<br>' + data.buildDate;
                }

                $('#xnat_power').find('a')
                                .attr('title', version_title)
                                .after('<small>version ' + version_string + build_string + '</small>');

                $('#header_logo').attr('title', version_title);

                XNAT.app.version = version_string;

            });
        }
        else {
            XNAT.cookie.set('SESSION_ACTIVE', 'false');
        }

        var clicker = XNAT.event.click('#header_logo, #xnat_power > a');

        // shift-click the header or footer XNAT logo to TOGGLE debug mode on/off
        clicker.shiftKey(function(e){
            e.preventDefault();
            if (XNAT.cookie.get('debug') === 'on') {
                XNAT.cookie.set('debug', 'off');
                window.location.hash = 'debug=off';
            }
            else {
                XNAT.cookie.set('debug', 'on');
                window.location.hash = 'debug=on';
            }
            window.location.reload();
        });

        // alt-shift-click to open the Swagger page in a new window
        clicker.altShift(function(e){
            e.preventDefault();
            XNAT.ui.popup(XNAT.url.rootUrl('/xapi/swagger-ui.html'));
        });

    });


    // this script has loaded
    init.loaded = true;

    return XNAT.init = init;

}));
