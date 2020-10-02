/*
 * web: url.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Methods for creating and working with URLs in XNAT
 */

var XNAT = getObject(XNAT||{});

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

    var app, url, xhr,
        root = this,
        undefined;

    XNAT.app = app = getObject(XNAT.app||{});
    XNAT.url = url = getObject(XNAT.url||{});
    XNAT.xhr = xhr = getObject(XNAT.xhr||{});

    // urlencode query string params by default
    url.encode = xhr.encode = firstDefined(url.encode||undefined, xhr.encode||undefined, true);

    // site context without trailing a slash
    url.context = url.siteContext = (window.serverRoot + '/').replace(/\/*$/, '') || '/';

    // don't cache AJAX requests
    xhr.cache = firstDefined(xhr.cache||undefined, false);

    // trim leading AND trailing slashes
    function trimSlashes(str){
        return str.replace(/^\/+|\/+$/g,'');
    }

    // chop off LEADING slashes
    function chopSlashes(str){
        return str.replace(/^\/+/,'');
    }

    // try to make sure url includes a SINGLE app context string
    function fixRoot(root, url){
        // remove slashes from both sides of 'root'
        var rootTemp = trimSlashes(root) + '/';
        // remove slash and 'root' chars from beginning of 'url'
        var urlTemp  = chopSlashes(url.replace(/^[~*]/, ''));
        // uh...
        var newUrl = ((root ? rootTemp : '') + (url ? urlTemp : ''));
        var rootRegex = new RegExp('^(' + rootTemp + ')+', 'g');
        newUrl = newUrl.replace(rootRegex, rootTemp);
        return ('/' + newUrl).replace(/\/+/g, '/');
    }

    // make sure the serverRoot string (and only ONE serverRoot string)
    // is at the beginning of a url
    function rootUrl(url){
        return fixRoot((window.serverRoot || XNAT.serverRoot || ''), url || '')
    }
    url.rootUrl = rootUrl;

    url.getProtocol = function(URL){
        var docUrl = URL || document.URL;
        if (!URL && window.location.protocol) {
            return window.location.protocol;
        }
        return /^(https:)/.test(docUrl) ? 'https:' : 'http:'
    };
    url.protocol = url.getProtocol();

    url.getDomain = function(){
        if (window.location.hostname) {
            return window.location.hostname;
        }
        if (document.domain) {
            return document.domain
        }
    };

    url.domain = url.getDomain();

    // return the current location's port number
    // (if present), optionally prefixed with [sep]
    url.getPort = function(sep){
        var port = window.location.port;
        return port ? (sep || '') + port : '';
    };

    url.fullUrl = function(_url){
        return window.location.origin ? window.location.origin + rootUrl(_url) : urlWithProtocol(url.getProtocol(), _url);
    };

    // use this to generate a download protocol for the XNAT Desktop Client
    function xnatUrl(_url){
        return urlWithProtocol(url.getProtocol() === 'https:' ? 'xnats:' : 'xnat:', _url);
    }
    url.xnatUrl = xnatUrl;

    function urlWithProtocol(protocol, path) {
        // Create URL object then convert back to string. This handles converting https://domain:443 to https://domain
        // and http://domain:80 to http://domain (i.e. eliminating explicit inclusion of default port for protocol).
        return new URL(protocol + '//' + url.getDomain() + url.getPort(':') + rootUrl(path)).toString()
    }
    url.urlWithProtocol = urlWithProtocol;

    // better encodeURIComponent() that catches
    // these additional characters: !'()*
    url.encodeURIComponent = function(str) {
        return encodeURIComponent(str).replace(/[!'()*]/g, function(c){
            return '%' + c.charCodeAt(0).toString(16);
        });
    };

    // more forgiving URI component decoder
    // http://unixpapa.com/js/querystring.html
    url.decodeURIComponent = function( str ){
        str = str.replace(/%([EF][0-9A-F])%([89AB][0-9A-F])%([89AB][0-9A-F])/gi,
            function( code, hex1, hex2, hex3 ){
                var n1 = parseInt(hex1, 16) - 0xE0;
                var n2 = parseInt(hex2, 16) - 0x80;
                if (n1 == 0 && n2 < 32) return code;
                var n3 = parseInt(hex3, 16) - 0x80;
                var n = (n1 << 12) + (n2 << 6) + n3;
                if (n > 0xFFFF) return code;
                return String.fromCharCode(n);
            });
        str = str.replace(/%([CD][0-9A-F])%([89AB][0-9A-F])/gi,
            function( code, hex1, hex2 ){
                var n1 = parseInt(hex1, 16) - 0xC0;
                if (n1 < 2) return code;
                var n2 = parseInt(hex2, 16) - 0x80;
                return String.fromCharCode((n1 << 6) + n2);
            });
        str = str.replace(/%([0-7][0-9A-F])/gi,
            function( code, hex ){
                return String.fromCharCode(parseInt(hex, 16));
            });
        return str;
    };

    // uri encode (key): (value)
    // optionally returning an array
    url.processQueryObject = function(query, arr, encode){

        var queryObject = {},
            qsArray = [];

        encode = (encode !== undefined) ? encode : XNAT.url.encode;

        forOwn(query, function(key, val){
            if (encode){
                key = XNAT.url.encodeURIComponent(key);
                val = XNAT.url.encodeURIComponent(val);
            }
            queryObject[key] = val;
            qsArray.push(key + '=' + val);
        });

        // reset url.encode to true
        // probably don't want to do this here
        // it would be reset every time XNAT.url.processQueryObject() is run
        //XNAT.url.encode = true;

        // pass an array as the second (arr) argument
        // to add the query string params to an
        // existing params array, OR the string 'array'
        // to return a new params array INSTEAD of an
        // object - confusing? maybe. sorry.
        if (arr){
            if (stringLower(arr) === 'array'){
                return qsArray
            }
            else if (isArray(arr)){
                return [].concat(arr, qsArray);
            }
        }
        // else:
        return queryObject;
    };


    url.toQueryObject = function(query, arr, encode){

        var qsArray = [],
            qsObject = {},
            qsOutput = isArray(arr) ? arr : [];

        encode = (encode !== undefined) ? encode : XNAT.url.encode;

        // passing 'query' as a {key: value}
        // object will be the most reliable
        // { format: 'json', sort: 'asc' }
        if (isPlainObject(query)){
            // returns unencoded data as query string params array
            qsArray = XNAT.url.processQueryObject(query, [], encode);
        }
        // an array of parameters
        // ['format=json','sort=asc']
        else if (isArray(query)){
            qsArray = query;
        }
        // handle passing of query string directly
        // (which should already be url encoded, if necessary)
        // this will not work properly if the query string
        // params or values contain '?', '&', or '#' characters
        else if (isString(query) && query.charAt(0) === '?'){
            // make sure a pesky url hash doesn't sneak in there
            query = query.split('#')[0];
            qsArray = (query.split('?')[1]||'').split('&');
        }

        // add qsArray to qsOutput
        qsOutput = qsOutput.concat(qsArray);

        // split name and value, which should
        // already be url encoded if passing
        // a string or array for 'query'
        forEach(qsOutput, function(qs){
            var _query = qs.split('='),
            // using shift to pull out the first
            // item in the _query array then
            // joining any remaining array items
            // should allow tolerance of any '='
            // chars in the query string values
                name = _query.shift(),
                val  = _query.join('');
            // putting values in an object
            // should eliminate duplicate
            // query string params
            if (name){
                qsObject[name] = val;
            }
        });

        return qsObject;

    };

    // normalize query string values
    // to an array in the format:
    // ['foo=bar','black=white','up=down']
    url.toQueryArray = function(query, arr){

        var qsOutput = [],
            qsObject = XNAT.url.toQueryObject(query, arr);

        // build up output array
        forOwn(qsObject, function(key, val){
            qsOutput.push(key + '=' + val);
        });

        return qsOutput;

    };


    // take a query object, or url encoded
    // string or array of query params
    // and convert to a full query string
    url.toQueryString = function(query, arr, encode){
        XNAT.url.encode = encode || XNAT.url.encode;
        var output = XNAT.url.toQueryArray(query, arr).join('&');
        return (output) ? output.replace(/^[?]*/, '?') : '';
    };


    // return url parts as an object
    url.splitUrl = function(URL){
        var _hash = URL.split('#');
        // account for multiple '#'
        // by shifting the first
        // array item, which should
        // be the base url
        var _base = _hash.shift();
        var parts = {
            hash: _hash.join('#'),
            query: _base.split('?')[1] || '',
            base: _base.split('?')[0]
        };
        // alias 'base' to 'url'
        parts.url = parts.path = parts.base;
        if (/^http/i.test(URL)){
            parts.protocol = parts.base.split(/(http[s]*)/)[1] || '';
            parts.urlParts = parts.base.split(parts.protocol + '://')[1].split('/');
            parts.siteParts = parts.urlParts.shift().split(':');
            parts.domain = parts.siteParts[0];
            parts.port = parts.siteParts[1] || '';
            parts.site = parts.protocol + '://' + parts.domain + (parts.port ? (':' + parts.port) : '');
            parts.path = '/' + parts.urlParts.join('/');
        }
        parts.paramsArray = [];
        parts.params = {};
        if (parts.query){
            parts.paramsArray = parts.query.split('&');
            parts.params = XNAT.url.toQueryObject({}, parts.paramsArray);
            //forEach(parts.paramsArray, function(part){
            //    if (!part) { return }
            //    var pieces = part.split('=');
            //    parts.params[pieces[0]] = pieces[1];
            //});
        }
        return parts;
    };


    // add query string to url, making sure
    // of proper placement of '?' and '&'
    // and preserving any existing query string
    // and url hash
    url.addQueryString = function(url, query){

        var _url = XNAT.url.splitUrl(url),
            params, output;

        // the url, minus any params and "bonus" slashes
        //url = urlParts[0].replace(/^\/+|\/+$/g, '');
        // maybe only *trailing* slashes?
        //url = urlBase.replace(/\/+$/, '');

        output = _url.base;

        if (isPlainObject(query)){
            query = extendCopy(_url.params, query);
        }

        //params = XNAT.url.toQueryString(query);
        params = XNAT.url.toQueryString(query, _url.paramsArray);

        if (params){
            output += (params.replace(/\?+/,'?'));
        }

        if (_url.hash){
            output += ('#' + _url.hash);
        }

        return output;

    };


    // return the value of a query string parameter,
    // either from a provided url string or the current
    // page's location if only 1 argument is passed
    url.getQueryStringValue = url.getParamValue = function(url, parameter){
        if (arguments.length === 1){
            parameter = url;
            url = window.location.href;
        }
        return XNAT.url.splitUrl(url).params[parameter] || null;
    };


    // change the baseURL without affecting the hash or query string
    url.updateBase = url.updateBaseUrl = function(url, newUrl){
        var _url = XNAT.url.splitUrl(url);
        if (_url.query){
            newUrl += ('?' + _url.query);
        }
        if (_url.hash){
            newUrl += ('#' + _url.hash);
        }
        return newUrl ;
    };


    // change the url hash without affecting the base url or query string
    url.updateHash = function(url, newHash){
        var _url = XNAT.url.splitUrl(url),
            newUrl = _url.base;
        if (_url.query){
            newUrl += ('?' + _url.query);
        }
        if (newHash){
            // newUrl += ('#' + newHash).replace(/^#+/,'#');
            newUrl += newHash.replace(/^#*/,'#');
        }
        return newUrl ;
    };


    // replace ANY part of the url hash with another value
    url.updateHashPart = function(hash, key, value, delim){

        var oldPart, newPart, undefined;

        hash = hash || window.location.hash || '#';
        hash = '#' + hash.split('#').slice(1).join('#');

        // both key and value are REQUIRED
        if (!key || value === undefined) return hash;

        if (hash.indexOf(key) === -1) {
            hash = (hash + key + value);
        }
        else {
            delim = delim !== undefined ? delim : '/#';
            oldPart = key + hash.split(key)[1].split(delim)[0];
            newPart = key + value;
            hash = hash.replace(oldPart, newPart);
        }

        hash = hash.replace(/^#*/,'#'); // only one '#' at the beginning, please

        return hash

    };


    // update parameter(s) stored in the url hash in the format
    // #foo=bar
    url.updateHashQuery = function(hash, key, value, delim){
        // make sure key starts with '#' and ends with '='
        key = key.replace(/^#*/, '#').replace(/=*$/, '=');
        return XNAT.url.updateHashPart(hash, key, value, delim);
    };


    // trigger a custom 'reload-hash' event on hash change
    $(window).on('reload-hash', function(e, callback, args){
        callback.apply(e, [].concat(args));
    });


    url.reloadHash = function(key, value, delim, callback){
        var newHash = XNAT.url.updateHashPart('', key, value, delim);
        //window.location.replace(newHash);
        //window.location.reload();
        $(window).trigger('reload-hash', [callback, arguments]);
    };


    // update parameters in the url query string without affecting
    // the other query string parameters or base url or url hash
    url.updateQueryString = function(url, newQuery, doReplace){

        var _url = XNAT.url.splitUrl(url),
            newUrl = _url.base;

        url = (doReplace) ? _url.base : url;

        if (newQuery){
            newUrl = XNAT.url.addQueryString(url, newQuery);
        }

        if (_url.hash){
            newUrl += ('#' + _url.hash);
        }

        return newUrl ;
    };


    // change the url query string without affecting the base url or url hash
    url.replaceQueryString = function(url, newQuery){
        return XNAT.url.updateQueryString(url, newQuery, true);
    };


    // return REST url with common parts pre-defined
    // restUrl('/data/projects', ['format=json'])
    // -or-
    // restUrl('/data/projects', {format: 'json'});
    // returns: '/data/projects?format=json&XNAT_CSRF=8493920104-csrfstring-3858939248'
    // it's ok to just keep the query string in the 'url' arg, but if you want to
    // prevent the XNAT_CSRF param from getting added, you'll need to pass
    // an empty array as the second argument and false as the third
    url.restUrl = function( url, params, cacheParam, csrf ){
        // 'params' = object or array of query string params:
        // ['format=json','sort=asc'], or
        // { format: 'json', sort: 'asc' }

        var urlParts = XNAT.url.splitUrl(url);

        // need to get a query object first
        // so we can add the XNAT_CSRF and
        // XNAT_XHR params to the end
        params = extend(urlParts.params, XNAT.url.toQueryObject(params,[],false));

        if ((window.csrfToken || XNAT.csrfToken) && (isTrue(csrf))) {
            params.XNAT_CSRF = (window.csrfToken || XNAT.csrfToken);
        }

        // add random string to query if
        // XNAT.xhr.cache === false (the default)
        if (xhr && !xhr.cache) {
            if (isUndefined(cacheParam) || isTrue(cacheParam)){
                // params.XNAT_XHR = randomID('x', false) + '-' + Date.now();
                // params.x = Date.now() + 'x' + (randomID('', '') + randomID('', '')).toUpperCase();
                params.x = randomID('xhr', false);
            }
        }

        return urlSetup(urlParts.base, '', params, urlParts.hash);

    };


    // automatically add CSRF param to URL string
    url.csrfUrl = function( URL, params, cacheParam ){
        return url.restUrl(URL, params, cacheParam, true);
    };


    // makes sure we've got serverRoot
    // and a properly formed url
    function urlSetup( base, parts, query, hash ){

        var pathArray = [],
            newUrl = '';

        if (arguments.length === 1 || isUndefined(parts)){
            parts = base;
            base = null;
        }

        if (isArray(parts)){
            pathArray = parts;
        }
        else if (isPlainObject(parts)){
            forOwn(parts, function(name, val){
                //pathArray = pathArray.concat(name, val);
                pathArray.push(name);
                pathArray.push(val);
            });
        }
        else if (isString(parts)) {
            pathArray = parts.split('/');
        }
        else {
            // 'parts' must be array, object, or string
            //return newUrl;
        }

        if (base && isString(base)){
            pathArray.unshift(base);
        }

        newUrl += pathArray.join('/');

        if (query){
            newUrl += XNAT.url.toQueryString(query);
        }

        if (hash){
            newUrl += '#' + hash.replace(/^#+/,'');
        }

        // remove multiple slashes and remove '/' in front of '?'
        return rootUrl(newUrl).replace(/\/+\?/g, '?');

    }
    url.buildUrl = url.setup = urlSetup;


    // replace parseable URL parts while preserving 'special' prefix
    url.parse = function( URL ){
        if (!URL) return '';
        var urlTemp = URL;
        // preserve 'special' syntax prefixes
        var parts = URL.split(/^(\$*[*?~]?\s*[:=]*\s*)/);
        // if there's only one part, there's no prefix
        if (parts.length === 1) {
            urlTemp = strReplace(URL);
        }
        else {
            urlTemp = strReplace(parts[2])
        }
        return (parts[1] || '') + rootUrl(urlTemp);
    };
    url.replace = url.parse;


    // build url path from object, array, or argument sequence
    // ({ projects: 'foo', subject: 'bar' })
    // -or-
    // (['projects', 'foo', 'subject', 'bar'])
    // -or-
    // ('projects', 'foo', 'subject', 'bar')
    // produces url:
    // '/data/projects/foo/subject/bar'
    //url.dataUrl = function(parts){
    //    var args = toArray(arguments);
    //    if (args.length > 1){
    //        parts = args;
    //    }
    //    return url.buildUrl('data', parts);
    //};
    //
    //url.templateUrl = function(parts){
    //    var args = toArray(arguments);
    //    if (args.length > 1){
    //        parts = args;
    //    }
    //    return url.buildUrl('app/template', parts);
    //};

    var urlGenerators = {
        'scriptUrl':      'scripts',
        'dataUrl':        'data',
        'templateUrl':    'app/template',
        'actionUrl':      'app/action/XDATActionRouter/xdataction',
        'displayItemUrl': 'app/action/DisplayItemAction'
    };

    forOwn(urlGenerators, function(name, base){
        XNAT.url[name] = function(parts){
            if (arguments.length > 1){
                parts = toArray(arguments);
            }
            return urlSetup(base, parts);
        };
    });
    // the above object loop will output:
    // url.dataUrl()
    // url.templateUrl()
    // url.actionUrl()
    // url.displayItemUrl()


    // produces:
    // /app/XDATScreen_report_xnat_projectData.vm/search_element/xnat:projectData/search_field/xnat:projectData.ID/search_value/prj001/
    url.searchUrl = function(/* obj (or) base, element, field, value, query */){

        var args = toArray(arguments),
            arg1 = args[0],
            obj = {};

        if (isPlainObject(arg1)) {
            obj = arg1;
        }
        else {
            if (isArray(arg1)){
                args = arg1;
            }
            if (args.length > 1){
                obj = {
                    base: args.shift(),
                    element: args.shift(),
                    field: args.shift(),
                    value: args.shift(),
                    query: args.shift()||''
                };
            }
            else {
                // ???
            }
        }

        var urlParts = [
            //'app',
            //obj.base,
            'search_element', obj.element || obj.search_element,
            'search_field', obj.field || obj.search_field,
            'search_value', obj.value || obj.search_value,
            XNAT.url.toQueryString(obj.query)
        ].concat(args); // pick up any extra args?

        return urlSetup(obj.base, urlParts);

    };


    // base/search_element/element/search_field/field/search_value/value
    url.searchUrl['doNotExecute'] = function(){
        return XNAT.url.searchUrl(
            'app/template/XDATScreen_report_xnat_projectData.vm', // base
            'xnat:projectData', // element
            'xnat:projectData.ID', // field
            'prj001' // value
        );
    };

    // this script has loaded
    url.loaded = true;

    return XNAT.url = url;

}));

// $(window).on('load', function(){
    // re-write links that start with '/'
    // to make sure they go to the site root
    // ...or don't...
    // $('a[href^="/"]').attr('href', function(){
    //     return XNAT.url.rootUrl($(this).attr('href'));
    // });
// });
