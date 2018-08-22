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

    var DOMAIN = getDomain();
    var SITE_ROOT = XNAT.url.rootUrl();

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
            // get all cookies as an object
            // if no name is specified
            return Cookies.get();
        }
        else {
            // return value of requested cookie
            return Cookies.get()[name];
        }
    }

    function getSubCookie(name, prop){
        var cookieObject = Cookies.getJSON(name);
        if (isPlainObject(cookieObject)) {
            return cookieObject[prop];
        }
        else {
            return cookieObject;
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

    cookie.subCookie = getSubCookie;
    cookie.getValue = getSubCookie;

    // update JSON cookie with [obj]
    cookie.update = function(name, obj, opts) {
        var cookieObject = Cookies.getJSON(name);
        // just set the value if not an object
        if (!isPlainObject(cookieObject)) {
            return setCookie(name, obj, opts);
        }
        forOwn(obj, function(key, val){
            cookieObject[key] = val;
        });
        return setCookie(name, cookieObject, opts);
    };

    // force setting of object without trampling on other properties
    cookie.setValue = function(name, prop, val, opts){
        var cookieObject = getObject(Cookies.getJSON(name));
        cookieObject[prop] = val;
        return setCookie(name, cookieObject, opts);
    };

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
