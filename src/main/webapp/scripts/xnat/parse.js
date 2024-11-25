/*!
 * Methods for parsing 'special' strings
 * and working with the associated data
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

    var undef;

    XNAT.data = getObject(XNAT.data);

    // use object to store REGEX values
    var REGEX = {};

    // does the string start with one of these...?
    // ??  !?  #?  $?  {{  ((
    REGEX.parseable = /^(\?\?|!\?|#\?|\$\?|{{|\(\()/;

    // search for a value at a specific object path location in returned data object
    // value: '$? /data/stuff/thing | :ResultSet:Result:0:contents'   // use lookupObjectValue()
    // value: '$? /data/stuff/thing | /ResultSet/Result/[1]/contents' // use XPath syntax (DefiantJS)
    // value: '$? /data/stuff/thing | $.ResultSet.Result[0].contents'  // use JSONpath syntax
    REGEX.useLookup = /^(\|*\s*:)/;       //   | :
    REGEX.useXPath = /^(\|*\s*\/)/;       //   | /
    REGEX.useJSONpath = /^(\|*\s*\$)/;    //   | $

    // $? = do REST call and use returned value
    // value: '$? /data/stuff/thing'
    REGEX.ajaxPrefix = /^(\$\?[:=]?\s*[*~]*)/;

    // value: '$? */data/stuff/thing'  // ALWAYS reload data
    REGEX.ajaxRefresh = /^(\$\?[:=]?\s*[*~])/;

    // $: = specify expected data type for ajax request
    // value: '$? /data/stuff/thing $:json'
    REGEX.ajaxDataType = /\$:/;

    // ?? = lookup value from variable in global scope
    // (optionally use colons instead of dots for object path)
    // value: '?? :NAMESPACE:var:name'
    REGEX.lookupPrefix = /^\?\?[:=]?\s*/;

    // {{ NAMESPACE:var:name }} - get value from global variable
    REGEX.lookupTest = /^{{.+}}$/;
    REGEX.lookupTrim = /^{{\s*|\s*}}$/g;
    // REGEX.lookupReplace = /{{\s*(.+)\s*}}/;

    // [[ propertyName ]] - property name to use in returned data object
    REGEX.useNameTest = /^\[\[.+]]$/;
    REGEX.useNameTrim = /^\[\[\s*|\s*]]$/g;
    // REGEX.useNameReplace = /\[\[\s*(.+)\s*]]/;

    // #? = execute function by name
    // value: "#? NS.func.name()"
    REGEX.fnPrefix = /^#\?[:=]?\s*/;

    REGEX.fnTest = /^#\?.+(\(\))*$/;
    REGEX.fnTrim = /^(#\?[:=]?\s*)|(\(\))$/g;
    // REGEX.fnReplace = /^#\?[:=]?\s*(.+)\s*\(\)$/;

    // !? = use result of JS eval() from the supplied string
    // value: "!? (function(){ return $('#thing').val() })()"
    REGEX.evalPrefix = /^!\?[:=]?\s*/;

    // (( evalString )) - return result of eval() using enclosed string
    // {( evalString )} - return result of eval() using enclosed string
    REGEX.evalTest = /^[{(]?[(].+[)][)}]?$/;
    REGEX.evalTrim = /^([{(]?[(])|(;*[)][)}]?)$/g;
    // REGEX.evalReplace = /[{(]?[(]\s*(.+)\s*[)][)}]?/;

    // special syntax to get/set form input values
    // value: '$? /data/thing $:text > [[yerInput]]'
    // value: '$? /data/stuff | :ResultSet:Result:0 > [[yerInput]]'
    // action: '/data/thing < [[yerInput]]'
    REGEX.setInputValue = />\s*\[\[(.+)]]/;
    REGEX.getInputValue = /<\s*\[\[(.+)]]/;
    REGEX.inputValueTest = /[><]+\s*\[\[.+]]/;
    REGEX.inputValueSplit = /\s*[><]\s*/;
    REGEX.inputValueTrim = /^(\s*\[\[\s*)|(\s*]]\s*)$/g;

    // @? = copy value from another element (on submit)
    REGEX.copyValue = /^(@\?)/;



    /**
     * Is [value] a 'special' string that can be further parsed?
     * @param value {String} - string to test
     * @returns {Boolean}
     */
    function parseable(value){
        if (!value) return false;
        var VAL = (value + '').trim();
        // a 'parseable' string MUST start with
        // one of these: ??  !?  #?  $?  ~/  {{  ((
        return REGEX.parseable.test(VAL);
    }



    //////////////////////////////////////////
    // MAIN CONSTRUCTOR           ////////////
    //////////////////////////////////////////
    function Parser(input){
        // NEVER transform this.input
        this.input = input;
        // ALWAYS transform this.value
        this.value = input;
        // result defaults to undefined
        this.result = undef;
        // collect callbacks
        this.doneCallbacks = [];
        this.failCallbacks = [];
    }

    //////////////////////////////////////////



    // alias 'prototype' to 'fn' for brevity
    Parser.fn = Parser.prototype;



    /**
     * Is [it] parseable?
     * @param [it] {String|*} - value to test
     * @returns {Boolean}
     */
    Parser.fn.parseable = function(it){
        this.input = it || this.input;
        return parseable(this.input);
    };



    /**
     * Helper for executing callback functions
     * @param fn {Function}
     * @param [status] {String} - check against this.status
     */
    function doCallback(fn, status){
        // always call this function with .call(this, ...)
        if (status === 'always' || this.status === status) {
            this.result = firstDefined(this.result, this.value);
            if (typeof fn === 'function') {
                fn.call(this, this.result);
            }
        }
    }



    /**
     * Always execute this callback despite 'status' value
     * @param fn {Function}
     * @returns {Parser}
     */
    Parser.fn.always = function(fn){
        doCallback.call(this, fn, 'always');
        return this;
    };
    Parser.fn.complete = Parser.fn.always;



    /**
     * Execute [fn] callback on successful parse/ajax
     * @param fn {Function}
     * @returns {Parser}
     */
    Parser.fn.done = function(fn){
        this.doneCallbacks.push(fn);
        // obj.result = obj.value;
        doCallback.call(this, fn, 'success');
        return this;
    };
    Parser.fn.success = Parser.fn.done;



    /**
     * Execute [fn] callback for failure of parse/ajax
     * @param fn {Function}
     * @param [e] {String} - Error message
     * @returns {Parser}
     */
    Parser.fn.fail = function(fn, e){
        this.failCallbacks.push(fn);
        // obj.result = obj.value;
        doCallback.call(this, fn, 'failure');
        if (e) console.error(e);
        return this;
    };
    Parser.fn.failure = Parser.fn.fail;



    /**
     * Should we lookup value for [val] in a global (namespaced) variable?
     * @param value {String|*}
     * @param success {Function}
     * @param failure {Function}
     */
    function doLookup(value, success, failure){

        var obj = this;

        // lookup value from a global variable?
        // ?? varname
        // ?? OBJ.item | //*[contents]
        // ?? OBJ.item | $..contents.*
        // ?? OBJ['item.name']
        // ?? :OBJ:item.name
        // {{ :OBJ:item.name }}
        // {{ varName }}
        // {{ varName | //*[contents] }}

        if (REGEX.lookupPrefix.test(value) || REGEX.lookupTest.test(value)) {

            if (jsdebug) console.log('===== doLookup =====');

            obj.value = value.replace(REGEX.lookupPrefix, '')
                             .replace(REGEX.lookupTrim, '')
                             .split('|')[0].trim();

            obj.value = obj.value.replace(/^:+/, ':');

            obj.path = (value.split('|')[1] || '').trim();

            try {
                obj.result = lookupObjectValue(window, obj.value);

                if (obj.result && obj.path) {
                    // optionally use XPath syntax
                    if (REGEX.useXPath.test(obj.path)) {
                        obj.result = JSON.search.call(JSON, obj.result, obj.path);
                    }
                    // or JSONPath syntax
                    else if (REGEX.useJSONpath.test(obj.path)) {
                        obj.result = jsonPath.call(window, obj.result, obj.path);
                    }
                    else {
                        obj.result = lookupObjectValue(obj.result, obj.path);
                    }
                }
                obj.status = 'success';
                // call the [success] callback and return 'success/done' method for chaining
                obj.done(success);
            }
            catch(e) {
                if (window.jsdebug) console.error(e);
                obj.result = obj.value;
                obj.status = 'failure';
                // call the [failure] callback and return 'failure/fail' method for chaining
                obj.fail(failure);
            }
            // --- LOOKUP RETURN --- //
            return obj;
        }
        return undef;
    }

    Parser.fn.doLookup = doLookup;



    /**
     * Should we retrieve the value via ajax and fire callbacks?
     * @param val {String} - url string
     * @param success {Function}
     * @param failure {Function}
     * @returns {*}
     */
    function doAjax(val, success, failure){

        var obj = this;

        // skip if string is *only* '$?' since this will end up
        // returning the current page's content (bad)
        if ((val + '').trim() === '$?') {
            // --- RETURN --- //
            return undef;
        }

        // lookup value using XHR?
        // $? */path/to/data  <-- ALWAYS load fresh data
        // $? ~/path/to/data
        // $? /path/to/data
        // $? /path/to/stuff | :ResultSet:Result:0
        // $? /path/to/stuff | $.ResultSet.Result[0]
        // $? /path/to/other $:xml

        if (REGEX.ajaxPrefix.test(val)) {

            if (jsdebug) console.log('===== doAjax =====');

            // always reload from url string starting with '*' or '~'
            obj.reloadData = obj.reload || REGEX.ajaxRefresh.test(val);

            obj.url = val.replace(REGEX.ajaxPrefix, '');
            obj.url = obj.url.split('|')[0];
            obj.url = obj.url.split(REGEX.ajaxDataType)[0];
            obj.url = obj.url.trim();
            obj.url = strReplace(obj.url);

            // return early if url is empty
            if (!obj.url) {
                // --- RETURN --- //
                return undef;
            }

            // string to use for XNAT.data.* cache
            obj.cacheUrl = XNAT.url.rootUrl(obj.url);

            if (obj.url.charAt(0) === '/' && obj.url.substr(0, 2) !== '//') {
                obj.url = obj.reloadData ? XNAT.url.restUrl(obj.url) : obj.cacheUrl;
            }

            // if using an object path, json dataType will be assumed
            obj.path = (val.split('|')[1] || '').trim();

            if (obj.path) {
                // obj.path = '$.' + obj.path;
                // defaults to built-in object lookup
                obj.lookupValue = lookupObjectValue;
                // optionally use XPath syntax
                if (REGEX.useXPath.test(obj.path)) {
                    obj.lookupValue = function(data, path){
                        // JSON.search is from DefiantJS, NOT a native method
                        return JSON.search.call(JSON, data, path);
                    };
                }
                // or JSONPath syntax
                else if (REGEX.useJSONpath.test(obj.path)) {
                    obj.lookupValue = function(data, path){
                        return jsonPath.call(window, data, path);
                    };
                }
                obj.dataType = 'json';
            }
            else {
                obj.dataType = (val.split(REGEX.ajaxDataType)[1] || 'text').trim();
            }

            var doneCallback = obj.done || obj.success;
            var failCallback = obj.fail || obj.failure;

            // RETURN CACHED DATA?
            if (!obj.reloadData && XNAT.data[obj.cacheUrl]) {
                // reformat to syntax for object lookup
                obj.value = '?? :XNAT:data:' + (obj.path ? obj.cacheUrl + ' | ' + obj.path : obj.cacheUrl);
                // --- RETURN CACHED DATA --- //
                return doLookup.call(obj, obj.value, doneCallback, failCallback);
            }

            // do XHR
            obj.request = XNAT.xhr.get({
                // dataType: obj.dataType,
                url: obj.url,
                success: function(data, status, xhrObj){
                    if (!data || /no.*content/i.test(status) || xhrObj.status === 204 || !obj.url || obj.url === XNAT.url.rootUrl()) {
                        return;
                    }
                    XNAT.data[obj.cacheUrl] = data;
                    if (jsdebug) debugLog(XNAT.data[obj.cacheUrl]);
                }
            });

            obj.request.always(function(){
                if (jsdebug) console.log(arguments)
            });

            obj.done = obj.success = function(callback){
                // wait until the request is done to
                // fire the success method(s)
                obj.request.done(function(VAL, status, xhrObj){
                    if (!VAL || /no.*content/i.test(status) || xhrObj.status === 204 || !obj.url || obj.url === XNAT.url.rootUrl()) {
                        return;
                    }
                    obj.result = obj.path ? obj.lookupValue(VAL, obj.path) : VAL;
                    obj.status = 'success';
                    doneCallback.call(obj, callback);
                });
                return obj;
            };
            if (isFunction(success)) {
                obj.success(success);
            }

            obj.fail = obj.failure = function(callback){
                obj.request.fail(function(statusText){
                    obj.result = statusText;
                    obj.status = 'failure';
                    failCallback.call(obj, callback, arguments);
                });
                return obj;
            };
            if (isFunction(failure)) {
                obj.failure(failure);
            }

            // --- AJAX RETURN --- //
            return obj;
        }

        return undef;
    }

    Parser.fn.doAjax = doAjax;



    /**
     * Should we get the value by executing a function by name?
     * @param value {*}
     * @param success {Function}
     * @param failure {Function}
     * @returns {*}
     */
    function doFn(value, success, failure){
        var obj = this;
        var val;
        // execute a function by name to get the value
        // #?:NS.func.name()
        // #? NS.func.name   --  with or without colon and trailing ()
        if (REGEX.fnTest.test(value)) {

            if (jsdebug) console.log('===== doFn =====');

            val = value.replace(REGEX.fnTrim, '')
                       .trim();

            try {
                // --- execute the function --- //
                obj.func = lookupObjectValue(val);
                if (obj.func === undef) {
                    obj.result = val;
                }
                if (isFunction(obj.func)) {
                    obj.result = obj.func.call(obj);
                }
                obj.status = 'success';
                // call the [success] callback and return 'success/done' method for chaining
                obj.done(success);
            }
            catch(e) {
                if (jsdebug) console.error(e);
                obj.result = val;
                obj.status = 'failure';
                // call the [failure] callback and return 'failure/fail' method for chaining
                obj.fail(failure);
            }
            // --- EVAL RETURN --- //
            return obj;
        }
        return undef;
    }

    Parser.fn.doFn = doFn;



    /**
     * Should we get the value using a js eval()?
     * @param value {*}
     * @param success {Function}
     * @param failure {Function}
     * @returns {*}
     */
    function doEval(value, success, failure){
        var obj = this;
        var val;
        // use eval() to get the value
        // !? XNAT.data.context.project.toLowerCase();
        // (( XNAT.data.context.project.toLowerCase(); ))
        if (REGEX.evalPrefix.test(value) || REGEX.evalTest.test(value)) {

            if (jsdebug) console.log('===== doEval =====');

            val = value.replace(REGEX.evalPrefix, '')
                       .replace(REGEX.evalTrim, '')
                       .trim();
            try {
                // --- DO THE eval() --- //
                obj.result = JSON.parse(val);
                if (obj.result === undef) {
                    obj.result = val;
                }
                obj.status = 'success';
                // call the [success] callback and return 'success/done' method for chaining
                obj.done(success);
            }
            catch(e) {
                if (jsdebug) console.error(e);
                obj.result = val;
                obj.status = 'failure';
                // call the [failure] callback and return 'failure/fail' method for chaining
                obj.fail(failure);
            }
            // --- EVAL RETURN --- //
            return obj;
        }
        return undef;
    }

    Parser.fn.doEval = doEval;



    /**
     * Parse [value] string and execute [success] using result
     * @param value - string to parse
     * @param [success] - success callback
     * @param [failure] - failure callback
     * @returns {Object}
     */
    Parser.fn.parseValue = function parseValue(value, success, failure){

        // object to return
        var obj = this;

        // coerce [value] to a string and trim whitespace
        obj.value = ((obj.input || obj.value || value) + '').trim();

        // return [value] if it can't be used as a string or starts with '@?'
        if (!parseable(obj.value)) {
            // --- NOT PARSEABLE RETURN --- //
            return obj;
        }

        // is there a name of an input element we're targeting?
        if (REGEX.inputValueTest.test(obj.value)) {
            obj.inputName = obj.value.split(REGEX.inputValueSplit)[1].replace(REGEX.inputValueTrim, '');
            obj.value = obj.value.split(REGEX.inputValueSplit)[0];
        }

        // all retrieval methods use the same arguments
        var args = [obj.value, success, failure];

        // return the first result, or [obj]
        return firstDefined(
            doLookup.apply(obj, args),
            doAjax.apply(obj, args),
            doFn.apply(obj, args),
            doEval.apply(obj, args),
            obj // if not lookup, ajax, or eval...
        );
    };



    /**
     * Main method - parses [value] string and defines lookup method based on syntax:
     * '??' - lookup value in global/namespaced variable
     * '$?' - lookup value via ajax/REST
     * '#?' - Execute function by name
     * '!?' - Run js eval() on [value] string
     * @param [value] {String|*}
     * @returns {*}
     */
    XNAT.parse = function(value){
        var newParser = new Parser(value);
        return value ? newParser.parseValue(value) : newParser;
    };

    // expose parseable() function as a static method
    XNAT.parse.parseable = parseable;

    // expose RegExp items via XNAT.parse namespace
    XNAT.parse.REGEX = REGEX;

    // this script has loaded
    XNAT.parse.loaded = true;

    return XNAT.parse;

}));
