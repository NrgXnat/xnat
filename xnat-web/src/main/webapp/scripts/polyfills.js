/*
 * web: polyfills.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * Polyfills for older browsers (IE8).
 */

// Shortcut for element.appendChild()
Element.prototype.append = Element.prototype.appendChild;

// Add an element as a first child of another element
Element.prototype.prepend = function(childNode){
    if (this.firstChild) {
        this.insertBefore(childNode, this.firstChild);
    }
    else {
        this.appendChild(childNode);
    }
};

// Source: https://github.com/Alhadis/Snippets/blob/master/js/polyfills/IE8-child-elements.js
if(!("lastElementChild" in document.documentElement)){
    Object.defineProperty(Element.prototype, "lastElementChild", {
        get: function(){
            for(var nodes = this.children, n, i = nodes.length - 1; i >= 0; --i)
                if(n = nodes[i], 1 === n.nodeType) return n;
            return null;
        }
    });
}

// String.trim() polyfill (IE8)
if (!String.prototype.trim) {
    (function() {
        // Make sure we trim BOM and NBSP
        var rtrim = /^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g;
        String.prototype.trim = function() {
            return this.replace(rtrim, '');
        };
    })();
}

if (!Array.isArray) {
    Array.isArray = function(arg) {
        return Object.prototype.toString.call(arg) === '[object Array]';
    };
}

// Array.indexOf() polyfill (IE8)
if (!Array.prototype.indexOf) {
    Array.prototype.indexOf = function (searchElement /*, fromIndex */ ) {
        "use strict";
        if (this == null) {
            throw new TypeError();
        }
        var t = Object(this);
        var len = t.length >>> 0;
        if (len === 0) {
            return -1;
        }
        var n = 0;
        if (arguments.length > 1) {
            n = Number(arguments[1]);
            if (n != n) { // shortcut for verifying if it's NaN
                n = 0;
            } else if (n != 0 && n != Infinity && n != -Infinity) {
                n = (n > 0 || -1) * Math.floor(Math.abs(n));
            }
        }
        if (n >= len) {
            return -1;
        }
        var k = n >= 0 ? n : Math.max(len - Math.abs(n), 0);
        for (; k < len; k++) {
            if (k in t && t[k] === searchElement) {
                return k;
            }
        }
        return -1;
    }
}

// Array.map() polyfill (IE8)
// Production steps of ECMA-262, Edition 5, 15.4.4.19
// Reference: http://es5.github.com/#x15.4.4.19
if (!Array.prototype.map) {
    Array.prototype.map = function(callback, thisArg) {
        var T, A, k;
        if (this == null) {
            throw new TypeError(" this is null or not defined");
        }
        var O = Object(this);
        var len = O.length >>> 0;
        if (typeof callback !== "function") {
            throw new TypeError(callback + " is not a function");
        }
        if (thisArg) {
            T = thisArg;
        }
        A = new Array(len);
        k = 0;
        while(k < len) {
            var kValue, mappedValue;
            if (k in O) {
                kValue = O[ k ];
                mappedValue = callback.call(T, kValue, k, O);
                A[ k ] = mappedValue;
            }
            k++;
        }
        return A;
    };
}

// Array.forEach() polyfill (IE8)
// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/forEach
// Production steps of ECMA-262, Edition 5, 15.4.4.18
// Reference: http://es5.github.io/#x15.4.4.18
if (!Array.prototype.forEach) {
    Array.prototype.forEach = function(callback, thisArg) {
        var T, k;
        if (this == null) {
            throw new TypeError(' this is null or not defined');
        }
        var O = Object(this);
        var len = O.length >>> 0;
        if (typeof callback !== "function") {
            throw new TypeError(callback + ' is not a function');
        }
        if (arguments.length > 1) {
            T = thisArg;
        }
        k = 0;
        while (k < len) {
            var kValue;
            if (k in O) {
                kValue = O[k];
                callback.call(T, kValue, k, O);
            }
            k++;
        }
    };
}

// Array.every() polyfill (IE8)
// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/every
// syntax: arrayVar.some(callback[, thisArg])
if (!Array.prototype.every) {
    Array.prototype.every = function(callbackfn, thisArg) {
        'use strict';
        var T, k;
        if (this == null) {
            throw new TypeError('this is null or not defined');
        }
        var O = Object(this);
        var len = O.length >>> 0;
        if (typeof callbackfn !== 'function') {
            throw new TypeError();
        }
        if (arguments.length > 1) {
            T = thisArg;
        }
        k = 0;
        while (k < len) {
            var kValue;
            if (k in O) {
                kValue = O[k];
                var testResult = callbackfn.call(T, kValue, k, O);
                if (!testResult) {
                    return false;
                }
            }
            k++;
        }
        return true;
    };
}

// Array.some() polyfill (IE8)
// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/some
// Production steps of ECMA-262, Edition 5, 15.4.4.17
// Reference: http://es5.github.io/#x15.4.4.17
if (!Array.prototype.some) {
    Array.prototype.some = function(fun /*, thisArg*/) {
        'use strict';
        if (this == null) {
            throw new TypeError('Array.prototype.some called on null or undefined');
        }
        if (typeof fun !== 'function') {
            throw new TypeError();
        }
        var t = Object(this);
        var len = t.length >>> 0;
        var thisArg = arguments.length >= 2 ? arguments[1] : void 0;
        for (var i = 0; i < len; i++) {
            if (i in t && fun.call(thisArg, t[i], i, t)) {
                return true;
            }
        }
        return false;
    };
}

// Array.reduce() polyfill (IE8)
// Production steps of ECMA-262, Edition 5, 15.4.4.21
// Reference: http://es5.github.io/#x15.4.4.21
if (!Array.prototype.reduce) {
    Array.prototype.reduce = function(callback /*, initialValue*/) {
        'use strict';
        if (this == null) {
            throw new TypeError('Array.prototype.reduce called on null or undefined');
        }
        if (typeof callback !== 'function') {
            throw new TypeError(callback + ' is not a function');
        }
        var t = Object(this), len = t.length >>> 0, k = 0, value;
        if (arguments.length == 2) {
            value = arguments[1];
        } else {
            while (k < len && ! k in t) {
                k++;
            }
            if (k >= len) {
                throw new TypeError('Reduce of empty array with no initial value');
            }
            value = t[k++];
        }
        for (; k < len; k++) {
            if (k in t) {
                value = callback(value, t[k], k, t);
            }
        }
        return value;
    };
}

if (!Array.prototype.filter) {
    Array.prototype.filter = function(fun/*, thisArg*/) {
        'use strict';
        if (this === void 0 || this === null) {
            throw new TypeError();
        }
        var t = Object(this);
        var len = t.length >>> 0;
        if (typeof fun !== 'function') {
            throw new TypeError();
        }
        var res = [];
        var thisArg = arguments.length >= 2 ? arguments[1] : void 0;
        for (var i = 0; i < len; i++) {
            if (i in t) {
                var val = t[i];
                if (fun.call(thisArg, val, i, t)) {
                    res.push(val);
                }
            }
        }
        return res;
    };
}

// trim spaces from all string items in an array
// optionally removing empty strings from the array
// and optionally purging non-string items
if (!Array.prototype.trim) {
    Array.prototype.trim = function(strip, purge) {
        'use strict';
        if (this == null) {
            throw new TypeError('Array.prototype.trim called on null or undefined');
        }
        var i = -1;
        var len = this.length;
        var outputArray = [];
        var item;
        while (++i < len) {
            item = this[i];
            // return non-string items as-is
            if (typeof item !== 'string') {
                // if not purging them
                if (purge !== true) {
                    outputArray.push(item);
                }
                continue;
            }
            // trim whitespace from string
            item = item.trim();
            // only push empty item if not stripping them
            if (item === '') {
                if (!strip) {
                    outputArray.push(item);
                }
            }
            else {
                // always push non-empty strings
                outputArray.push(item);
            }
        }
        return outputArray;
    }
}

// force 'stringable' array elements to string,
// optionally including non-stringable items
if (!Array.prototype.strings) {
    Array.prototype.strings = function(other, recursive, flatten){
        'use strict';
        if (this == null) {
            throw new TypeError('Array.prototype.strings called on null or undefined');
        }
        var stringArray = [];
        this.forEach(function(item){
            var _item = null;
            if (/string|number|boolean/.test(typeof item)) {
                _item = item+'';
            }
            else if (other) {
                if (Array.isArray(item)){
                    if (recursive) {
                        if (flatten) {
                            stringArray = stringArray.concat(item.strings(other, recursive, flatten))
                        }
                        else {
                            _item = item.strings(other, recursive, flatten)
                        }
                    }
                    else {
                        _item = '[' + item.strings(other) + ']';
                    }
                }
                else if (typeof item === 'function') {
                    _item = item.toString().replace(/\s+/, ' ');
                }
                else if (typeof item === 'object') {
                    try {
                        _item = JSON.stringify(item)
                    }
                    catch(e1) {
                        try {
                            _item = item.toString()
                        }
                        catch(e2) {
                            try {
                                _item = item+''
                            }
                            catch(e3) {
                                console.error('Could not stringify. ' + [e1, e2, e3].join(' '));
                            }
                        }
                    }
                }
            }
            if (_item) {
                stringArray.push(_item);
            }
        });
        return stringArray;
    }
}

// simple check for a plain object
if (typeof Object.isObject != 'function') {
    Object.isObject = function(obj) {
        return Object.prototype.toString.call(obj) === '[object Object]';
    };
}

if (typeof Object.assign != 'function') {
    Object.assign = function (target) {
        var undefined;
        'use strict';
        if (target === undefined || target === null) {
            throw new TypeError('Cannot convert undefined or null to object');
        }
        var output = Object(target);
        for (var index = 1; index < arguments.length; index++) {
            var source = arguments[index];
            if (source !== undefined && source !== null) {
                for (var nextKey in source) {
                    if (source.hasOwnProperty(nextKey)) {
                        output[nextKey] = source[nextKey];
                    }
                }
            }
        }
        return output;
    };
}

if (typeof Object.create != 'function') {
    Object.create = (function() {
        var undefined;
        var Temp = function(){};
        return function(prototype, propertiesObject){
            if (prototype !== Object(prototype) && prototype !== null) {
                throw TypeError('Argument must be an object, or null');
            }
            Temp.prototype = prototype || {};
            var result = new Temp();
            Temp.prototype = null;
            if (propertiesObject !== undefined) {
                Object.defineProperties(result, propertiesObject);
            }
            // to imitate the case of Object.create(null)
            if (prototype === null) {
                result.__proto__ = null;
            }
            return result;
        };
    })();
}

// From https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/keys
if (!Object.keys) {
    Object.keys = (function() {
        'use strict';
        var hasOwnProperty = Object.prototype.hasOwnProperty,
            hasDontEnumBug = !({ toString: null }).propertyIsEnumerable('toString'),
            dontEnums = [
                'toString',
                'toLocaleString',
                'valueOf',
                'hasOwnProperty',
                'isPrototypeOf',
                'propertyIsEnumerable',
                'constructor'
            ],
            dontEnumsLength = dontEnums.length;

        return function(obj) {
            if (typeof obj !== 'object' && (typeof obj !== 'function' || obj === null)) {
                throw new TypeError('Object.keys called on non-object');
            }

            var result = [], prop, i;

            for (prop in obj) {
                if (hasOwnProperty.call(obj, prop)) {
                    result.push(prop);
                }
            }

            if (hasDontEnumBug) {
                for (i = 0; i < dontEnumsLength; i++) {
                    if (hasOwnProperty.call(obj, dontEnums[i])) {
                        result.push(dontEnums[i]);
                    }
                }
            }
            return result;
        };
    }());
}
