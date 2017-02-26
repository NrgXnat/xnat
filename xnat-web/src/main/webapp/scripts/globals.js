/*
 * web: globals.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Global general-purpose JavaScript convenience
 * utlity helper functions (verbosity intentional)
 *
 * Some of these functions are taken straight from
 * other libraries and put in the global scope here.
 * Is that a good idea? Maybe, maybe not.
 */

// Avoid console errors in browsers that lack a console.
(function(){
    var method;
    var noop = function(){};
    var methods = [
        'assert', 'clear', 'count', 'debug', 'dir', 'dirxml', 'error',
        'exception', 'group', 'groupCollapsed', 'groupEnd', 'info', 'log',
        'markTimeline', 'profile', 'profileEnd', 'table', 'time', 'timeEnd',
        'timeStamp', 'trace', 'warn'
    ];
    var length = methods.length;
    var console = (window.console = window.console || {});
    var i = 0;
    while ( length-- ) {
        method = methods[i++];
        // Only stub undefined methods.
        if ( !console[method] ) {
            console[method] = noop;
        }
    }
}());

function diddly(){}

// utility for getting URL query string value
function getQueryStringValue( param ){
    var search = window.location.search;
    if (!param || !search) { return '' }
    if (search.indexOf(param) === -1) { return '' }
    var val = search.
        split(param+'=')[1].
        split('&')[0].
        split('#')[0].
        replace(/\/*$/,''); // remove any 'bonus' trailing slashes
    return decodeURIComponent(val);
}

function getParameterByName( name ){
    return getQueryStringValue(name)
}

// get the url hash string without the '#'
function getUrlHash(){
    return window.location.hash.split('#')[1] || '';
}

// simplest function for getting
// a value from the url hash
function getUrlHashValue(start, end){
    var part = '',
        hash = window.location.hash;
    if (!hash) { return '' }
    part = hash.split(start||'#')[1]||'';
    part = part.split(end||'/')[0]||'';
    return part;
}

function firstDefined() {
    var undef, i = -1, val;
    while (++i < arguments.length) {
        val = arguments[i];
        if (val !== undef && val !== 'undefined') {
            return val;
        }
    }
    return undef;
}
function isDefined( x ){
    return typeof x != 'undefined'
}
function isUndefined( x ){
    return typeof x == 'undefined'
}
function isString( str ){
    return typeof str === 'string';
}
function stringLower( str ){
    return str.toString().toLowerCase();
}
function stringUpper( str ){
    return str.toString().toUpperCase();
}
function capitalize(str){
    return str.charAt(0).toUpperCase() + str.slice(1);
}
function sentenceCase(str) {
    return capitalize(str);
}
function titleCase(str){
    return str.split(/\s+/).map(function(word){
        return capitalize(word);
    }).join(' ');
}
function truncateText(text, len){
    len = len || 30; // default length is 30 chars
    if (text.length <= len){
        return text;
    }
    else {
        return text.substring(0, len) + '...';
    }
}
function isTrue( val ){
    return stringLower(val||'') === 'true';
}
function isFalse( val ){
    return stringLower(val||'') === 'false';
}
function isEqual( a, b ){
    // heavy-handed comparison of 2 values as strings
    if (arguments.length === 2 && typeof a != 'undefined'){
        return (a.toString() === b.toString());
    }
    else {
        return undefined;
    }
}
function isEqualLower(a, b){
    return isEqual(stringLower(a||''), stringLower(b||''));
}
function isFunction( func ){
    return typeof func == 'function';
}
function isObject( obj ){
    // returns true for objects, arrays, and null
    return typeof obj == 'object';
}
function isPlainObject( obj ){
    return Object.prototype.toString.call(obj) === '[object Object]';
}
function isEmptyObject( obj ){
    var name;
    for ( name in obj ) {
        return false;
    }
    return true;
}
function getObject( obj ){
    return (isPlainObject(obj) || isFunction(obj)) ? obj : {};
}
function isArray( arr ){
    if ( Array.isArray ) {
        return Array.isArray(arr);
    }
    else {
        return Object.prototype.toString.call(arr) === '[object Array]';
    }
}
function isEmptyArray( arr ){
    return isArray(arr) && arr.length === 0;
}
function isEmpty( x, args ){
    if (typeof x === 'boolean') {
        return false;
    }
    if (isNumeric(x)) {
        return false;
    }
    if (isString(x)){
        return x === '';
    }
    if (isPlainObject(x)){
        return isEmptyObject(x);
    }
    if (isArray(x)){
        return !x.length;
    }
    // does a function return an 'empty' value?
    if (isFunction(x)){
        return isEmpty(x.apply(null, [].concat(args)));
    }
    return (x === null || isUndefined(x) || !isFunction(x));
}
function isNumber( num ){
    return (typeof num == 'number' && !isNaN(num));
}

// copy of jQuery's $.isNumeric() method
function isNumeric( num ) {
    // parseFloat NaNs numeric-cast false positives (null|true|false|"")
    // ...but misinterprets leading-number strings, particularly hex literals ("0x...")
    // subtraction forces infinities to NaN
    // adding 1 corrects loss of precision from parseFloat (jQuery issue #15100)
    return !isArray( num ) && (num - parseFloat( num ) + 1) >= 0;
}

// can the value be reasonably used as a string?
function stringable(val){
    return /string|number|boolean/.test(typeof val);
}

// copy an object
function objectCopy(obj){

    var output = {},
        prop, val;

    for (prop in obj){
        val = output[prop] = obj[prop];
        if (isPlainObject(val)) {
            // prevent infinite loop
            if (val[prop] !== val){
                output[prop] = objectCopy(val);
            }
            continue;
        }
        if (isArray(val)) {
            output[prop] = toArray(val);
        }
    }

    return output;
}

// alternative to Object.assign
function objectAssign(/* deep, target, obj2, obj3, etc */) {

    var undef, target, source, prop, val,
        args = arguments,
        deep = args[0],
        len = arguments.length,
        i = 1;

    if (typeof deep !== 'boolean') {
        deep = false;
        i = 0;
    }

    target = args[i];

    if (target === undef || target === null) {
        throw new TypeError('Cannot convert undefined or null to object');
    }

    var output = Object(target);

    while (++i < len) {
        source = args[i];
        if (source !== undef && source !== null) {
            for (prop in source) {
                if (source.hasOwnProperty(prop)) {
                    val = output[prop] = source[prop];
                    if (deep && isPlainObject(val)) {
                        // prevent infinite loop
                        if (val[prop] !== val){
                            output[prop] = objectAssign(deep, {}, val);
                            // output[prop] = val;
                        }
                    }
                }
            }
        }
    }
    return output;
}

// copy of jQuery's $.extend() method
function extend(){
    var src, copyIsArray, copy, name, options, clone,
        target = arguments[0] || {},
        i = 1,
        length = arguments.length,
        deep = false;
    // Handle a deep copy situation
    if ( typeof target === "boolean" ) {
        deep = target;

        // skip the boolean and the target
        target = arguments[ i ] || {};
        i++;
    }
    // Handle case when target is a string or something (possible in deep copy)
    if ( typeof target !== "object" && !isFunction(target) ) {
        target = {};
    }
    // Copy object if only one argument is passed.
    // jQuery extends its own object, but since this
    // isn't jQuery, 'this' is the global object (bad)
    if ( i === length ) {
        target = {};
        i--;
    }
    for ( ; i < length; i++ ) {
        // Only deal with non-null/undefined values
        if ( (options = arguments[ i ]) != null ) {
            // Extend the base object
            for ( name in options ) {
                // don't check for this - extend everything
                //if ( !options.hasOwnProperty(name) ) {
                //    continue;
                //}
                src = target[ name ];
                copy = options[ name ];
                // Prevent never-ending loop
                if ( target === copy ) {
                    continue;
                }
                // Recurse if we're merging plain objects or arrays
                if ( deep && copy && ( isPlainObject(copy) || (copyIsArray = isArray(copy)) ) ) {
                    if ( copyIsArray ) {
                        copyIsArray = false;
                        clone = src && isArray(src) ? src : [];
                    }
                    else {
                        clone = src && isPlainObject(src) ? src : {};
                    }
                    // Never move original objects, clone them
                    target[ name ] = extend(deep, clone, copy);
                    // Don't bring in undefined values
                }
                else if ( copy !== undefined ) {
                    target[ name ] = copy;
                }
            }
        }
    }
    // Return the modified object
    return target;
}

// default deep extend
function extendDeep(){
    var args = toArray(arguments);
    return extend.apply(null, [true].concat(args));
}

// clone and extend
function extendCopy(){
    var args = toArray(arguments);
    return extend.apply(null, [{}].concat(args));
}

// clone and deep extend
function extendCopyDeep(){
    var args = toArray(arguments);
    return extend.apply(null, [true, {}].concat(args));
}

// return a cloned copy of a single 'obj'
function cloneObject(obj){
    return extend(true, {}, obj || {});
}

// add child objects to 'obj' object from string
// OVERWRITES PROPERTIES WITH MATCHING NAMES
// setObject(foo, 'bar.baz', 123456)
// -> foo: { bar: { baz: 123456 } }
function setObject(obj, str, val) {
    var parts, part;
    if (typeof str != 'string' || !str.length) {
        return {};
    }
    obj = getObject(obj);
    parts = str.split('.');
    while (parts.length > 1) {
        part = parts.shift();
        obj = getObject(obj);
        if (!obj[part]) {
            obj[part] = {};
        }
        obj = obj[part];
    }
    obj[parts[0]] = val || {};
    return obj;
}

// add child objects to 'obj' object
// OVERWRITES PROPERTIES WITH MATCHING NAMES
function setExtendedObject(obj, str, val){
    var newObj = {};
    setObject(newObj, str, val);
    newObj = extend(true, {}, obj, newObj);
    return newObj;
}

// loops over object string using quasi-dot notation (with colons):
// we use colons because property names can contain periods (which is dumb)
// var myVal = lookupObjectValue(XNAT, ':data:siteConfig:siteId');
// --> myVal == 'myXnatSiteId'
function lookupObjectValue(root, objStr, prop){

    var val = '',
        delim = '.',
        brackets = /[\]\[]/,
        hasBrackets = false,
        parts = [],
        undef;

    if (!objStr) {
        objStr = root+'';
        root = window;
    }

    if (!objStr) return '';

    root = root || window;

    // if 'objStr' contains brackets, use bracket notation
    if (objStr.indexOf('[') > -1) {
        delim = brackets;
        // remove leading and trailing brackets
        //objStr = objStr.replace(/^\[|]$/g, '')
        hasBrackets = true;
    }
    // if 'objStr' STARTS WITH a colon, use those as the path delimiter
    else if (/^:/.test(objStr)) {
        delim = ':';
    }
    // otherwise we're probably using dot notation

    objStr.toString().trim().split(delim).forEach(function(part, i){
        part = (part+'').trim();
        // if using brackets, trim quotes
        if (hasBrackets) {
            part = part.replace(/^[\['"]|['"\]]$/g, '');
        }
        // only push non-empty parts
        // this should filter items that
        // start or end with a delimiter
        if (part > '') parts.push(part);
    });

    parts.forEach(function(part, i){
        // start at the root object
        if (i === 0) {
            val = (root[part] !== undef) ? root[part] : '';
        }
        else {
            if (val === undef) return false;
            val = val[part];
        }
    });

    // explicitly set a final property name to look for
    if (prop) {
        val = (val[prop] !== undef) ? val[prop] : val;
    }

    return val;

}

// return the last item in an array-like object
function getLast(arr){
    if (!arr) { return null }
    if (!arr.length) { return arr }
    return arr[arr.length-1];
}

// make sure we only run a function one time
function once(func, args) {
    func = func || function(){};
    if (func.called) { return }
    func.apply(null, args);
    func.called = true;
}

// execute a function on each item in an
// array(-like) object with a length property
// works like native Array.forEach();
function forEach( arr, fn, context ){
    var i = -1, len;
    if (!arr || !arr.length) { return }
    len = arr.length;
    if (isFunction(fn)) {
        while (++i < len){
            fn.call(context||arr[i], arr[i], i);
        }
    }
}

function forIn(obj, fn){
    var keys = [],
        key;
    if (!isPlainObject(obj)) { return }
    for (key in obj) {
        keys.push(key);
        if (!isFunction(fn)) continue;
        fn(key, obj[key]);
    }
    return keys;
}

// execute a function on each
// of an object's own properties
// works like jQuery's $.each()
// but only for objects
// returns array of property names
function forOwn(obj, fn){
    var keys = [],
        key;
    if (!isPlainObject(obj)) { return }
    for (key in obj) {
        if (obj.hasOwnProperty(key)) {
            keys.push(key);
            if (!fn || !isFunction(fn)) continue;
            fn(key, obj[key]);
        }
    }
    return keys;
}

// convert array-like object or arguments to a real array
// (twice as fast as Array.prototype.slice.call(arguments))
function toArray(arr) {
    var i = -1,
        len = arr.length,
        newArray = new Array(len);
    while (++i < len) {
        newArray[i] = arr[i];
    }
    return newArray;
}

// check if 'item' is in 'arr' array
function inArray(item, arr){
    var i = -1,
        len = arr.length;
    if (!len) {
        return false;
    }
    while (++i < len){
        if (arr[i] === item){
            return true;
        }
    }
    return false;
}

// return new array with duplicates removed
function dedupeArray(arr){
    var out = [],
        i   = -1,
        len = arr.length,
        item;
    while (++i < len) {
        item = arr[i];
        if (!inArray(item, out)){
            out.push(item);
        }
    }
    return out;
}

// break an array into a 2-D array of smaller 'chunks'
function chunkArray(arr, len) {
    var chunks = [],
        i = 0,
        n = arr.length;
    while (i < n) {
        chunks.push(arr.slice(i, i += len));
    }
    return chunks;
}

// set default values for object
function setDefaults(obj, props){
    //obj = getObject(obj);
    //forOwn(props, function(name, val){
    //    obj[name] = val;
    //});
    //return obj;
    return extendCopyDeep(obj, props)
}

// converts properly formatted numeric string
// to a number, or if not a proper numeric string,
// just returns the value passed, unless
// 'force' === true, then use parseFloat()
// to try to return a number
function toNumber( val, strip, force, dec ){

    var deci = /\./g,
        thou = /,/g,
        num;

    // only do additional processing if more than one argument
    if (arguments.length > 1){

        // do not strip non-numeric characters by default
        strip = (strip === 'strip') ? true : strip === true;

        // do not force number conversion by default
        // (returns 'sanitized' number)
        force = (force === 'force') ? true : force === true;

        // if comma is used for decimal separator
        // adjust thousands and decimal separators for JS
        if (dec === ','){
            deci = /,/g;
            thou = /\./g;
        }

        // strip thousands separators and make sure
        // period is used for the decimal separator
        val = (val+'').replace(thou,'').replace(deci,'.');

        // strip non-numeric characters (besides decimal)
        if (strip){
            val = val.replace(/[^0-9\.]/g,'');
        }

        // chop off after 2nd decimal, if present
        val = val.split('.');
        val = (val.length === 1) ? val[0] : val[0]+ '.' +val[1];

    }

    // finally create the number
    num = parseFloat(val);

    if (num === +val){
        return num;
    }
    else {
        return force ? num : val;
    }
}

// return a 'clean' number
// remove non-numeric characters
// and truncate past 2nd decimal,
// if present
// examples:
// cleanNumber('123,456.789.001') -> 123456.789
// cleanNumber('abc123456xyz789.001a') -> 123456789.001
// cleanNumber('123.456,001abc', ',') -> 123456.001
function cleanNumber( val, dec ){
    return toNumber(val, true, true, dec);
}

// pass an array of values to make sure ALL of them are numbers
// 'numeric' argument indicates allowing numeric _string_: '1'
function allNumbers( arr, numeric ){
    var len = arr.length,
        i = -1,
        checkNumber = (numeric) ? isNumeric : isNumber;
    if ( !isArray(arr) ) { return false }
    while ( ++i < len ) {
        if ( !checkNumber(arr[i]) ) {
            return false;
        }
    }
    return true;
}

// pass an array of values to make sure they're ALL numeric
function allNumeric( arr ){
    return allNumbers( arr, true );
}

// feed an array of values to check for at least one number
// 'numeric' argument indicates allowing numeric string
function hasNumber( arr, numeric ){
    var numbers = 0,
        len = arr.length,
        i = -1,
        checkNumber = (numeric) ? isNumeric : isNumber;
    if ( !isArray(arr) ) { return false }
    while ( ++i < len ) {
        if ( checkNumber(arr[i]) ) {
            numbers += 1;
        }
    }
    return numbers > 0;
}

// returns number as a string with leading zeros (or other character)
// thanks to - http://stackoverflow.com/a/10073788
// revised here - http://jsfiddle.net/rj0rf5hg/2/
// padNumber( 5 )           //=> '05'
// padNumber( 55, 4 )       //=> '0055'
// padNumber( 555, 6, 'X' ) //=> 'XXX555'
function padNumber( num, size, fill ) {
    // only whole numbers
    if (parseInt(num, 10) !== +num) { return num+'' }
    num = num+''; // make sure 'num' is a string
    // make sure 'size' is a whole number
    // defaults to 2 digits
    size = (typeof size != 'undefined') ? parseInt(size, 10) : 2;
    fill = fill || '0'; // default fill character is '0'
    return (num.length >= size) ? num : new Array(size - num.length + 1).join(fill) + num;
}
function zeroPad( num, size, fill ){
    return padNumber(num, size, fill || '0');
}

// add commas to numbers
function addCommas( nStr ){
    nStr += '';
    var
        x = nStr.split('.'),
        x1 = x[0],
        x2 = x.length > 1 ? '.' + x[1] : ''
        ;
    var rgx = /(\d+)(\d{3})/;
    while ( rgx.test(x1) ) {
        x1 = x1.replace(rgx, '$1' + ',' + '$2');
    }
    return x1 + x2;
}

function roundNumber( num, dec ){
    return Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
}

// convert number to file size in KB, MB, GB
// rounded to 'round' decimal places
function sizeFormat( size, round ){
    var KB = 1024,
        MB = KB * KB,
        GB = MB * KB,
        TB = GB * KB;
    // round to 2 decimal places by default
    round = round || 2;
    if ( size >= TB ) {
        return roundNumber(size / TB, round) + ' TB';
    }
    if ( size >= GB ) {
        return roundNumber(size / GB, round) + ' GB';
    }
    if ( size >= MB ) {
        return roundNumber(size / MB, round) + ' MB';
    }
    if ( size >= KB ) {
        return roundNumber(size / KB, round) + ' KB';
    }
    return size + ' B';
}

function randomID(prefix, seq) {
    window.autoIDcount = window.autoIDcount || 0;
    window.autoIDs     = window.autoIDs     || [];
    var pre = (isDefined(prefix)) ? prefix : 'i-' ;
    var i = (isUndefined(seq) || isTrue(seq)) ? padNumber( ++window.autoIDcount, 4 ) + '-' : '';
    var newID = pre + i + (Math.random() + 1).toString(36).substr(2,8);
    window.autoIDs.push(newID);
    window.randomIDcount = window.autoIDcount;
    window.randomIDs     = window.autoIDs;
    return newID;
}
autoID = randomID;

// set 'forceLower' === true (or omit argument)
// to ensure output is lowercase
function toDashed(str){
    var undefined;
    str = str !== undefined ? str+'' : '';
    return str.replace(/[A-Z]/g, function(u) {
        return '-' + u;
    }).replace(/[A-Z]-/g, function(c){
        return c.replace(/-$/, '');
    }).toLowerCase().replace(/[\W_-]+/g, '-').replace(/^-*|-*$/g, '');
}
//hyphenate = toDashed;
//dashify   = toDashed;

// like toDashed() but with underscores instead of hyphens
function toUnderscore(str){
    return toDashed(str).replace(/-+/g, '_');
}

// set 'forceLower' === true (or omit argument)
// to ensure *only* 'cameled' letters are uppercase
function toCamelCase(str) {
    // 'sanitize' by running str through toDashed()
    return toDashed(str).replace(/-./g, function(u){
        return u.substr(1).toUpperCase();
    });
}
//toCamel     = toCamelCase;
//camelCase   = toCamelCase;
//camelize    = toCamelCase;
//camelify    = toCamelCase;
//camelfy     = toCamelCase;

// put on the String prototype just for kicks
// or don't
//String.prototype.toDashed = function(forceLower){
//    return toDashed(this, forceLower);
//};
//
//String.prototype.toCamel = function(forceLower){
//    return toCamel(this, forceLower);
//};
//
//String.prototype.toCamelLower = function(){
//    return toCamelLower(this);
//};

// enhanced encodeURIComponent() that
// replaces more non-word characters
function encodeURIComponentAll(str) {
    return encodeURIComponent(str).replace(/[!'()*]/g, function(c){
        return '%' + c.charCodeAt(0).toString(16);
    });
}

function setElementData(element, name, val){
    if (document.head && document.head.dataset) {
        name = toCamelCase(name);
        element.dataset[name] = val;
    }
    else {
        name = toDashed(name);
        element.setAttribute('data-' + name, val);
    }
}

function getElementData(element, name){
    if (document.head && document.head.dataset) {
        name = toCamelCase(name);
        return realValue(element.dataset[name]);
    }
    else {
        name = toDashed(name);
        return realValue(element.getAttribute('data-' + name));
    }
}

// returns real boolean for boolean string
// returns real number for numeric string
// returns null and undefined for those strings
// (or returns original value if none of those)
// useful for pulling 'real' values from
// a string used in [data-] attributes
function realValue(val, bool){
    var undefined;
    // only evaluate strings
    if (!isString(val)) return val;
    if (bool){
        if (val === '0'){
            return false;
        }
        if (val === '1'){
            return true;
        }
    }
    if (isNumeric(val)){
        return +val;
    }
    switch(val) {
        case 'true': return true;
        case 'false': return false;
        case 'undefined': return undefined;
        case 'null': return null;
        default: return val;
    }
}

// return an object from a string with pipe-separated
// (or custom 'delim' separated) values
// 'obj_or_str' can be an existing object you want to modify
function parseOptions(obj_or_str, str, delim, sep){

    var obj = {};

    // handle simplest case of just passing an options string
    if (arguments.length === 1){
        str = obj_or_str;
    }

    if (isPlainObject(obj_or_str)){
        obj = obj_or_str;
    }

    delim = delim || /[,;|]/; // default delimiters ( , ; | - comma or semicolon or pipe)
    sep   = sep   || /[:=]/; // default key:value separators ( : = - colon or equals)

    var parts = isString(str) ? str.split(delim) : [];

    forEach(parts, function(part){

        var prop = part.split(sep)[0],
            val = part.split(sep)[1];

        obj[prop] = realValue(val);

    });

    return obj;
}

// Return the directory path for the script executing this function.
// This works ONLY with scripts hard coded onto the page or loaded
// with document.write() or insertScripts().
// DO NOT wait for DOM to load to run this - that will just return
// the last <script> (with "src") on the page
function getScriptDir() {
    var scripts, src, path;
    scripts = document.querySelectorAll('script[src]');
    src = scripts[scripts.length - 1].src;
    if (src.indexOf('/') > -1) {
        path = src.split('/');
        path.splice(path.length - 1, 1);
        return path.join('/') + '/';
    }
    else {
        return '';
    }
}

// keep track of scripts loaded with loadScript();
window.loadedScripts = [];

// did we load the page in 'debug' mode?
// add ?jsdebug=true or #jsdebug to url
// this is used to load non-minified scripts if true
function debugMode(){
    var hash = window.location.hash.toLowerCase();
    var debug = (getQueryStringValue('jsdebug') ||
            getQueryStringValue('debug') ||
            getQueryStringValue('js') ||
            '').toLowerCase();
    if (/(debug=off|debug=false)/.test(hash)) return false;
    if (/(debug|true|on)/.test(debug)) return true;
    if (/(debug)/.test(hash)) return true;
    return false;
}

window.jsdebug = window.debug = debugMode();

// return passed 'min' string if in jsdebug mode
// ?jsdebug=true or #jsdebug
function setMin(min){
    return debugMode() ? '' : min || '';
}

function scriptUrl(url, min){
    var parts = url.split('|');
    url = parts[0].trim();
    min = (min || parts[1] || '').replace(/([!~\*])+/g,'').trim();
    return serverRoot + '/scripts/' + (url.replace(/\.js$/i,'')) + setMin(min) + '.js';
}

function getScriptElements(){
    var scripts = document.querySelectorAll('script[src]'),
        scriptsArray = window.loadedScripts.slice() || [],
        len = scripts.length,
        i = -1,
        src;
    while (++i < len){
        src = scripts[i].getAttribute('src');
        if (scriptsArray.indexOf(src) === -1){
            scriptsArray.push(src);
        }
    }
    if (window.jsdebug){
        console.log(scriptsArray);
    }
    return window.loadedScripts = scriptsArray;
}
//getScriptElements(); // gather scripts thus far?

function hasScript( url ){
    // fastest check?
    if (window.loadedScripts.indexOf(url) > -1){
        return true;
    }
    //// see if it's in the DOM already
    //else {
    //    if (getScriptElements().indexOf(url) > -1){
    //        return true;
    //    }
    //    // if not, push it to window.loadedScripts array
    //    else {
    //        // and tell the closure it's not there (yet)???
    //        //window.loadedScripts.push(url);
    //        return false;
    //    }
    //}
    //var scriptLength = document.querySelectorAll('script[src="' + url + '.js"]').length;
    //console.log('count: ' + url + ' - ' + scriptLength);
    //return scriptLength;
    return getScriptElements().indexOf(url) > -1;
}

// split params passed as a pipe-separated string
// and return object with property names for params
function scriptParams( script ){
    var obj = { url: '', min: '', name: '' };
    if (isString(script)){
        script = script.split('|');
        obj.url    = script[0] ? script[0].trim() : '';
        obj.min    = script[1] ? script[1].replace(/\*/,'').trim() : '';
        obj.name   = script[2] ? script[2].replace(/\*/,'').trim() : '';
        obj.parent = script[3] ? script[3].replace(/\*/,'').trim() : '';
    }
    else if (isPlainObject(script)) {
        script.url = script.src =
            script.url || script.src; // tolerate use of 'src' prop name
        extend(obj, script);
    }
    //obj.min = setMin(obj.min);
    obj.src = obj.url = obj.url.replace(/\.js$/i,'') + obj.min + '.js';
    return obj;
}

// returns HTML for <script> element
function scriptHTML( src, name ){
    var script = '';
    if (arguments.length === 0 || src === null){
        return '';
    }
    if (src && !hasScript(src)){
        script += '<script type="text/javascript"';
        script += ' src="' + src + '"';
        script += (name) ? ' data-name="' + name + '"' : '';
        script += '><\/script>';
        //window.loadedScripts.push(_src);
    }
    return script;
}

// kludgy document.write('<script>')
// ONLY WORKS INLINE ON THE PAGE
// call functions that rely on these scripts
// AFTERWARDS in a separate <script> element
// insertScript('/scripts/app/script', '.min', 'app.script');
// DO NOT CALL insertScript() AFTER PAGE LOAD - ONLY ON INITIAL LOAD
function insertScript( url, min, name ){

    var script = scriptParams(url);

    if (hasScript(script.src)) { return }

    try {
        document.write(scriptHTML(script.src, name || script.name));
        //script.element = document.querySelector('script[src="'+script.src+'"]');
        // don't know if these have loaded since
        // they're inserted after this script runs?
        //script.element.onload = function(){
        //    if (!done) {
        //        done = true;
        //        window.loadedScripts.push(script.src);
        //        //callback(this, "ok");
        //    }
        //};
        //script.element.onreadystatechange = function(){
        //    var state;
        //    if (!done) {
        //        state = this.readyState;
        //        if (state === "complete") {
        //            this.onload();
        //        }
        //    }
        //};
        //console.log(getScriptElements());
    }
    catch(e){
        console.log(e);
    }
}

// insertScripts([{url:'/scripts/app/script',name:'app.script',min:'-min'}]);
//function insertScripts( /* scripts (multiple args or array) */ ){
//    var i = -1, scripts;
//    if (isString(arguments[0]) || arguments.length > 1){
//        scripts = toArray(arguments);
//    }
//    else {
//        scripts = arguments[0];
//    }
//    while (++i < scripts.length){
//        if (scripts[i]){ // skip null values
//            insertScript(scripts[i]);
//        }
//    }
//}
//insertScripts.configArraySample = [
//    // string with pipe separating params (spaces ok)
//    // (script url) | (optional min string) | (optional script name)
//    '/scripts/app/foo.js | .min | foo',
//    // or use an object with param properties
//    {
//        url: '/scripts/app/script', // REQUIRED
//        min: '-min', // optional
//        name: 'app.script' // optional
//    },
//    {
//        // 'src' property name works also
//        src: '/scripts/app/utils',
//        min: '.min',
//        name: 'app.utils'
//    }
//];

// returns new <script> DOM ELEMENT
function scriptElement( src, title, body ){
    var script = document.createElement('script');
    script.type = "text/javascript";
    if (title){
        script.title = title;
    }
    if (src){
        script.src = src;
    }
    else {
        script.innerHTML = body || '';
    }
    return script;
}

function writeScript( src, title, body ){
    document.write(scriptElement(src, title, body).outerHTML)
}

function writeScripts(){
    var scripts = arguments[0],
        i = -1,
        len = arguments.length;
    if (len > 1) { scripts = arguments }
    len = scripts.length;
    while (++i < len){
        writeScript.apply(null, [].concat(scripts[i]));
    }
}

// load a script,
// optionally into a specific parent element,
// and/or with a callback (optional)
function loadScript( /* script, parent/callback, callback */ ) {

    var obj, parent, _parent,
        script, scripts, status,
        callback,
        fns = {},
        noop = function(){},
        args = arguments,
        arg2 = args[1],
        arg3 = args[2],
        len  = args.length,
        done = false;

    if (len === 0){
        // no args no run
        return;
    }

    if (len > 3){
        console.log('max 3 arguments allowed');
        return;
    }
    if (len >= 2){
        // parent could be second argument
        // if there are 2 or 3 args
        parent = arg2;
        callback = arg3;
    }
    // but arg2 *could* be a callback instead of the parent
    if (len === 2 && (isFunction(arg2) || isPlainObject(arg2))){
        parent = 'head';
        callback = arg2;
    }

    // process params input in string or object format
    // returns params object
    obj = scriptParams(args[0]);

    obj.callback = obj.success || obj.complete || obj.callback || noop;

    if (isPlainObject(callback)){
        fns = callback;
    }
    else {
        fns.callback = isFunction(callback) ? callback : noop;
    }
    fns.callback = fns.success || fns.complete || fns.callback || noop;

    script = scriptElement(obj.src, obj.name);
    script.url = obj.src;
    script.onload = function(complete){
        status = complete || 'ok';
        if (!done) {
            done = true;
            window.loadedScripts.push(obj.src);
            obj.callback(this, status); // callback function in config object
            fns.callback(this, status); // callback function argument
        }
    };
    script.onreadystatechange = function(){
        if (!done) {
            if (this.readyState === 'complete') {
                this.onload('complete');
            }
        }
    };
    script.onerror = function(){
        status = 'error';
        // prefer to call 'error' callback
        obj.callback = obj.failure || obj.error || obj.callback;
        fns.callback = fns.failure || fns.error || fns.callback;
        if (!done) {
            done = true;
            obj.callback(this, status); // callback function in config object
            fns.callback(this, status);
        }
    };

    // 'parent' param could be a separate argument for this function
    // or a property property on the 'script' arg
    //if (parent === 'before'){
    //    scripts = document.querySelectorAll('script');
    //    scripts[scripts.length-1].insertBefore(script);
    //    return;
    //}
    _parent = document.querySelector(parent||obj.parent||'head');
    //_parent.appendChild(script);
    _parent.insertBefore(script, getLast(document.scripts));

}

// load multiple scripts
// (into the same parent, if specified)
// and run optional callbacks for each script
// and a final callback after all scripts are loaded
function loadScripts( scripts, parent_or_callback, callback ){

    var i = -1, len, _script, _parent,
        _callback = isFunction(callback) ? callback : function(){};

    scripts = scripts.slice();

    len = scripts.length;

    if (len === 0){
        // need args
        return;
    }

    if (isFunction(parent_or_callback)){
        _parent = 'head';
        _callback = parent_or_callback;
    }
    else {
        _parent = parent_or_callback;
    }

    while (++i < len){
        _script = scriptParams(scripts[i]);
        _script.min      = ''; // 'min's been added already
        _script.parent   = _script.parent || _parent;
        if (i === len-1){
            // do callback after *last* script has loaded
            loadScript(_script, _script.parent, _callback);
        }
        else {
            loadScript(_script, _script.parent);
        }
    }
}

function cssUrl( url, min, query ){
    var parts = url.split('|');
    url = parts[0].trim();
    min = (min || parts[1] || '').replace(/([!~\*])+/g,'').trim();
    query = (query || parts[2] || '').trim();
    if (query) {
        query = '?' + query.replace(/^\?/,'');
    }
    return serverRoot +
        '/' + (url.replace(/\.css$/i,'')) +
        setMin(min) + '.css' + query;
}

function cssElement( href ){
    var el = document.createElement('link');
    el.rel = 'stylesheet';
    el.type = 'text/css';
    el.href = cssUrl(href);
    return el;
}

// document.write(css)
function writeCSS( href ){
    document.write(cssElement(href).outerHTML);
}

function loadCSS( url, parent ){
    // use CSS-style selector for 'parent'
    parent = parent ? document.querySelector(parent) : document.querySelector('head');
    parent.appendChild(cssElement(url));
}

// basic indentation formatting only
function formatJSON(json, indent){
    return JSON.stringify(json, null, indent || 4);
}

function prettifyJSON(data, indent) {
    var json;
    if (typeof data != 'string') {
        json = JSON.stringify(data, null, indent||2);
    }
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return '<pre class="json">' + json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        var cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    }) + '</pre>';
}

// call 'test' at 'interval' until it returns true
// then execute 'callback' -- basic but useful
function waitForIt(interval, test, callback){
    var _test = test;
    if (typeof test !== 'function') {
        _test = function(){
            return !!test
        }
    }
    var waiting = setInterval(function(){
        if (_test()) {
            var called = callback();
            clearInterval(waiting);
            return called;
        }
    }, interval || 10);
    return waiting;
}
