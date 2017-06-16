/*!
 * Helper methods for working with browser cookies,
 * with default settings for use in XNAT.
 *
 * Uses js-cookie library:
 * https://github.com/js-cookie/js-cookie/tree/v2.1.4
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

    var undef, cookie;

    XNAT.cookie = cookie = getObject(XNAT.cookie || {});

    function getDomain(){
        if (window.location.hostname) {
            return window.location.hostname;
        }
        if (document.domain) {
            return document.domain
        }
    }

    function getContext(){
        return (window.serverRoot + '/').replace(/\/*$/, '') || '/';
    }

    var DOMAIN = getDomain();
    var SITE_ROOT = getContext();

    function CookieObject(name, value, opts){
        this.name = name;
        this.value = value !== undef ? value : '';
        this.opts = extend({
            path: SITE_ROOT,
            domain: DOMAIN
        }, getObject(opts));
    }

    function setCookie(name, value, opts){
        var obj;
        if (!name) {
            return null;
        }
        else {
            obj = new CookieObject(name, value, opts);
        }
        // set the cookie
        Cookies.set(obj.name, obj.value, obj.opts);
        // return the obj
        return obj;
    }

    function getCookie(name){
        if (!name) {
            // get all cookies as JSON
            // if no name is specified
            return Cookies.getJSON();
        }
        else {
            // return value of requested cookie
            return Cookies.get()[name];
        }
    }

    function cookieExists(name){
        return getCookie(name) !== undef;
    }

    function resetCookie(name, opts){
        return setCookie(name, '', opts);
    }

    function removeCookie(name, opts){
        var obj;
        if (!name) {
            return null;
        }
        else {
            obj = new CookieObject(name, '', opts);
        }
        // set the cookie
        Cookies.remove(obj.name, obj.opts);
        // return the obj
        return obj;
    }

    cookie.get = getCookie;
    cookie.set = setCookie;
    cookie.exists = cookieExists;

    // pass an array for 'names' argument
    // to RESET multiple cookies with one call
    cookie.reset = function(names, opts){
        [].concat(names).forEach(function(name){
            resetCookie(name, opts);
        });
    };

    // RESETS all cookies for current or specified domain and path
    cookie.resetAll = function(opts){
        forOwn(Cookies.get(), function(name){
            resetCookie(name, opts);
        });
    };

    // pass an array for 'names' argument
    // to REMOVE multiple cookies with one call
    cookie.remove = function(names, opts){
        [].concat(names).forEach(function(name){
            removeCookie(name, opts);
        });
    };

    // REMOVES all cookies for current or specified domain and path
    cookie.removeAll = function(opts){
        forOwn(Cookies.get(), function(name){
            removeCookie(name, opts);
        });
    };

    // this script has loaded
    cookie.loaded = true;

    XNAT.cookie = cookie;
    // alias to XNAT.cookies (plural) also
    XNAT.cookies = XNAT.cookie;

    return XNAT.cookie;

}));
