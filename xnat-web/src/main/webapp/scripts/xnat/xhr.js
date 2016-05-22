/*!
 * XNAT AJAX functions:
 * Wraps jQuery and/or YUI AJAX functions
 * in XNAT.xhr methods. Allows flexibility
 * to change AJAX library behind the scenes
 * while keeping consistent calls to
 * XNAT.xhr methods.
 */

var XNAT = getObject(XNAT||{}),
    YAHOO = typeof YAHOO != 'undefined' ? YAHOO : null;

(function(XNAT, $, yui){

    var xhr, url,
        root = this,
        undefined;

    XNAT.xhr = xhr = getObject(XNAT.xhr||{});
    XNAT.url = url = getObject(XNAT.url||{});

    function diddly(){} // cute little noop()

    // Use first value from argument list that's defined.
    // Arguments must be declared variables or explicit values.
    // Set var equal to itself before passing to the function
    // if you don't know if it's defined or not.
    // Set last argument to a known valid value to serve
    // as a default.
    // var foo=foo, bar=bar;
    // var name = firstDefined(foo, bar, 'Bob');
    function firstDefined() {
        var undefined, i = -1;
        while (++i < arguments.length) {
            if (arguments[i] !== undefined) {
                return arguments[i];
            }
        }
        return undefined;
    }

    // list of available AJAX methods we'll be using
    xhr.methods = [
        'GET',
        //'HEAD', // ???
        'POST',
        'PUT',
        'DELETE'
    ];

    // do not cache AJAX requests by default
    xhr.cache = firstDefined(xhr.cache||undefined, false);

    // urlencode query string params by default
    xhr.encode = url.encode = firstDefined(xhr.encode||undefined, url.encode||undefined, true);

    //xhr.setup = function(opts){
    //    opts = $.extend( opts || {}, {
    //        cache: true // disable jQuery's aggressive cache busting
    //    });
    //    $.ajaxSetup(opts);
    //};
    ////
    //xhr.setup();

    //////////////////////////////////////////////////
    // URL METHODS MOVED TO xnat/url.js
    //////////////////////////////////////////////////

    // add XNAT.url methods to XNAT.xhr
    // No, don't do that.
    //extend(xhr, url);

    xhr.$ = getObject(xhr.$||{});
    // adding shortcut methods: put and delete AJAX calls for clarity
    $.each(["put", "delete"], function(i, method) {
        $[method] = function(url, data, callback, type) {
            if ($.isFunction(data)) {
                type = type || callback;
                callback = data;
                data = undefined;
            }
            return $.ajax({
                url: url,
                type: method,
                dataType: type,
                data: data,
                success: callback
            });
        };
    });
    // Direct maps to jQuery's AJAX methods.
    // Why use these instead of jQuery directly?
    // For flexibility to allow XNAT's AJAX
    // library to be changed in the future.
    xhr.$.ajax      = xhr.ajax$      = $.ajax;
    xhr.$.get       = xhr.get$       = $.get;
    xhr.$.post      = xhr.post$      = $.post;
    xhr.$.put       = xhr.put$       = $.put;
    xhr.$.delete    = xhr.delete$    = $.delete;
    xhr.$.getJSON   = xhr.getJSON$   = $.getJSON;
    xhr.$.getScript = xhr.getScript$ = $.getScript;
    xhr.$.load = xhr.load$ = function(selector, url, data, success){
        $$(selector).load(url, data, success);
    };

    // private config object constructor
    // no AJAX is done, just setup
    function RequestOfType( method, argsArray /* [ 1:url, 2:data, 3:opts, 4:callback ] */){

        var args = argsArray,  // make a local var
            len = args.length, // argsArray.length won't change
            url = '',
            data = null,
            opts = {},
            callback = null,
            arg1 = args[0],
            arg2 = args[1],
            arg3 = args[2],
            arg4 = args[3];

        if (isPlainObject(arg1)){
            // ( opts )
            opts = arg1;
        }
        else {
            // ( url )
            url = arg1;

            if (len === 2){
                // ( url, callback )
                if (isFunction(arg2)){
                    callback = arg2;
                }
                // ( url, opts )
                else if (isPlainObject(arg2)){
                    opts = arg2;
                }
            }
            else if (len === 3){
                // ( url, data, opts )
                if (isPlainObject(arg3)){
                    data = arg2;
                    opts = arg3;
                }
                // ( url, data, callback )
                else if (isFunction(arg3)){
                    data = arg2;
                    callback = arg3;
                }
            }
            else if (len === 4){
                // not really sure we need this one
                // ( url, data, opts, callback )
                data = arg2;
                opts = arg3;
                callback = arg4;
            }
        }

        this.method = method;
        this.url = url;
        this.data = data;
        this.success = callback;

        // copy any leftover opts to {this}
        extendDeep(this, opts);

    }


    /////////////////////////////////////////////////////////////////
    // main XHR function
    // nothing too special
    // put all options in 'opts' argument
    // for most straightforward usage
    // argument options:
    // (url) // url string
    // (opts) // config object
    // (url, success) // url string and success callback
    // (url, opts) // url string and config object
    // (url, data, success) // url string, data object, success callback
    // (url, data, opts) // url string, data object, config object
    // (url, data, opts, success) // url string, data object, config object, success callback
    xhr.request = xhr.req = xhr.ajax = function( /* url/opts, data/opts/callback, opts/callback, callback */ ){

        var opts = {}, $ajax;

        if (arguments[0] instanceof RequestOfType){
            opts = arguments[0];
        }
        else {
            opts = new RequestOfType(null, arguments);
        }

        // accept 'type' or 'method' names for
        // request type (prefer 'method')
        // defaults to 'GET'
        opts.method = opts.type =
            opts.method || opts.type || 'GET';

        if (opts.beforeSend || opts.start){
            opts.beforeSend = opts.beforeSend || opts.start || diddly;
        }

        // accept 'success' or 'done' names for callback method
        opts.success = opts.success || opts.done || diddly;

        // accept either 'error', 'failure', or 'fail' method names
        opts.error = opts.error || opts.failure || opts.fail || diddly;

        // accept either 'complete', or 'always' for 'complete' method names
        opts.complete = opts.complete || opts.always || diddly;

        opts.params = getObject(opts.params||{});

        // 'format' is an XNAT-specific property and
        // is both the dataType for the XHR request
        // as well as the 'format' query string parameter
        if (opts.format){
            opts.params.format = opts.format;
            delete opts.format;
        }

        // or maybe just add params to the url as a query string?
        // as a way to separate query string params from data to submit
        if (!isEmptyObject(opts.params)){

            opts.url = XNAT.url.addQueryString(opts.url, opts.params);

            // if there's a 'format' param, set the dataType to that
            // if dataType isn't already specified
            if (opts.params.format && !opts.dataType){
                opts.dataType = opts.params.format;
            }

        }

        // use XNAT's cache-busting
        // query string parameter
        // instead of jQuery's
        opts.cache = isDefined(opts.cache||undefined) ? opts.cache : true;

        if (isDefined(opts.context||opts.scope||undefined)){
            opts.context = opts.scope =
                opts.context || opts.scope || root;
        }

        // if no 'yui' property exists, or a
        // specific 'jquery' property exists,
        // just do jQuery $.ajax() call
        if (!opts.yui || opts.jquery){

            $ajax = $.ajax(opts);
            
            // save jQuery's fail method
            $ajax.$fail = $ajax.fail;
            
            // remap the arguments for consistency with .done()
            $ajax.fail = function(callback){
                return $ajax.$fail(function(jqXHR, textStatus, errorThrown) {
                    callback(errorThrown, textStatus, jqXHR);
                    return $ajax;
                });
            };

            // reset XNAT.xhr.cache to false
            xhr.cache = false;

            return $ajax;

        }

        // ---------------------------------------------
        // Most requests will use $.ajax() and stop here
        // ---------------------------------------------
        // YUI 'asyncRequest' will run if there is a
        // 'yui' property set to boolean true, or is
        // an object with a map of YUI params
        // ---------------------------------------------

        // if no YUI library, return null
        if (!yui){ return null }

        // go ahead and use YUI. Ugh.

        // map to yui property names:
        opts.start = opts.beforeSend;
        opts.failure = opts.error;

        // default data is null
        opts.data = opts.data || null;

        // serialize data object to url query string
        // for non POST or PUT requests
        if (opts.data && (/^(PUT|POST)$/i).test(opts.method)){
            opts.url = XNAT.url.addQueryString(opts.url, opts.data);
        }

        opts.yui = (isTrue(opts.yui) || !isPlainObject(opts.yui)) ? {} : opts.yui;
        // maybe it's called 'callback' or 'callbacks'?
        extendDeep(opts.yui, opts.callbacks, opts.callback);
        opts.yui.success  = opts.yui.success  || opts.success  || diddly;
        opts.yui.failure  = opts.yui.failure  || opts.error    || diddly;
        opts.yui.complete = opts.yui.complete || opts.complete || diddly;

        // override cache property since a
        // random query string should've
        // already been added
        opts.yui.cache = true;

        // reset XNAT.xhr.cache to false
        xhr.cache = false;

        return yui.util.Connect.asyncRequest(
            opts.method,
            opts.url,
            opts.yui, // object - contains YUI callback methods and properties
            opts.data // POST data
            // on some 'asyncRequest' calls that are currently
            // in XNAT, there are more than 4 arguments - they
            // will not be used here in XNAT.xhr.request()
        );

    };
    /////////////////////////////////////////////////////////////////

    // call RequestOfType with
    // XNAT.xhr.requestOfType('GET', '/data/etc/', success);
    xhr.requestOfType = function( /* method, url, data, opts, callback */ ){

        var args, method;

        // must have AT LEAST 2 args (method and url)
        if (arguments.length < 2) { return null }

        args = toArray(arguments);
        method = args.shift();

        return xhr.request(new RequestOfType(method, args));

    };

    // same arguments as YAHOO.util.connect.asyncRequest()
    // some properties/methods will work differently though,
    // since jQuery is doing the AJAX request rather than YUI.
    xhr.asyncRequest = function(method, url, callbackObject, data){
        var opts = {
            method: method,
            url:    url,
            data:   data
        };
        return xhr.request(extendDeep(opts, callbackObject));
    };

    // setup shorthand method options
    xhr.shorthands = {
        'get' : { method: 'GET' },
        'post' : { method: 'POST' },
        'put' : { method: 'PUT' },
        'delete' : { method: 'DELETE' },
        'getJSON' : { method: 'GET', dataType: 'json', format: 'json' },
        'getHTML' : { method: 'GET', dataType: 'html', format: 'html' },
        'getXML' : { method: 'GET', dataType: 'xml', format: 'xml' },
        'getText' : { method: 'GET', dataType: 'text', format: 'text' },
        'putJSON' : { method: 'PUT', contentType: 'application/json', processData: false },
        'postJSON' : { method: 'POST', contentType: 'application/json', processData: false }
    };

    xhr.shorthands._delete = xhr.shorthands.delete_ = xhr.shorthands['delete'];

    // create shorthand methods:
    // XNAT.xhr.get()
    // XNAT.xhr.post()
    // XNAT.xhr.put()
    // XNAT.xhr.delete() || XNAT.xhr._delete() || XNAT.xhr.delete_()
    // XNAT.xhr.getJSON()
    // XNAT.xhr.getHTML()
    // XNAT.xhr.getXML()
    // XNAT.xhr.getText()
    // XNAT.xhr.putJSON()
    // XNAT.xhr.postJSON()
    // >>>
    forOwn(xhr.shorthands, function(type, opts){
        xhr[type] = function(/* url, data/null, opts_or_callback, callback */){
            var req = new RequestOfType(opts.method, arguments);
            return xhr.request(extendDeep(req, opts));
        };
    });

    // only do JSON.stringify on Arrays or Objects
    function safeStringify(val){
        if ($.isArray(val) || $.isPlainObject(val)) {
            return JSON.stringify(val);
        }
        return '';
    }

    function processJSON(data, stringify){
        var output = {};
        forEach(data, function(item){
            var prop = item.name;
            var val  = item.value;
            if (typeof output[prop] == 'undefined') {
                output[prop] = val;
            }
            else {
                output[prop] = [].concat(output[prop], val) ;
            }
        });
        if (stringify) {
            return safeStringify(output);
        }
        return output;
    }

    function formToJSON(form, stringify){
        return processJSON($$(form).serializeArray(), stringify);
    }

    // expose to
    // XNAT.xhr.formToJSON(form, true)
    xhr.formToJSON = formToJSON;

    $.fn.formToJSON = $.fn.toJSON = function(stringify){
        return formToJSON(this, stringify);
    };

    // helper function to fire $.fn.changeVal() if available
    function changeValue(el, val){
        var $el = $$(el);
        if ($.isFunction($.fn.changeVal)) {
            $el.changeVal(val);
        }
        else {
            $el.val(val).trigger('change');
        }
        return $el;
    }
    
    // set form element values from an object map
    function setValues(form, dataObj){
        // cache and check if form exists
        var $form = $$(form);
        if (!$form.length) return;
        // find all input and select elements with a name attribute
        $form.find('input[name], select[name]').each(function(){
            var val = '';
            if (Array.isArray(dataObj)) {
                val = dataObj.join(', ');
            }
            else {
                val = stringable(dataObj) ? dataObj+'' : dataObj[this.name] || '';
            }
            changeValue(this, val);
        });
        // set textarea innerText from a 'value' property
        $form.find('textarea[name]').each(function(){
            var $textarea = $(this);
            $textarea.innerText =  (function(){
                var val = dataObj[this.name];
                return stringable(val) ? val+'' : safeStringify(val);
            })();
        });
        return $form;
    }

    // this could be a handy jQuery method
    $.fn.setValues = function(dataObj){
        setValues(this, dataObj);
        return this;
    };

    xhr.form = function(form, opts){

        var $form = $$(form),
            _form = $form[0], // raw DOM element
            validation = true,
            callback = diddly;

        opts = cloneObject(opts);
        opts.url = XNAT.url.rootUrl(opts.url || $form.attr('action'));
        opts.method = opts.method || _form.method || 'GET';

        if ($.isFunction(opts.validate)) {
            validation = opts.validate.call(_form, opts);
            if (!validation) {
                $form.removeClass('valid').addClass('invalid');
                return validation;
            }    
            else {
                $form.removeClass('invalid').addClass('valid');
            }
        }
        
        // set opts.callback:false to prevent the
        // 'standard' method callback from running
        if (opts.callback !== false) {
            callback = opts.success || opts.done || diddly;
        }
        // don't pass 'callback' property into the AJAX request
        delete opts.callback;

        if (/POST|PUT/i.test(opts.method)) {
            if ($form.hasClass('json') || /json/i.test(opts.contentType||'')){
                opts.data = formToJSON($form, true);
                opts.processData = false;
                opts.contentType = 'application/json';
            }
            opts.success = function(data){
                callback.apply($form, arguments);
                // repopulate 'real' data after success
                // DON'T TRUST RETURNED DATA
                //setValues($form, data);
            }
        }
        // populate form fields from returned
        // json data for 'GET' method
        else if (/GET/i.test(opts.method)){
            opts.success = function(data){
                callback.apply($form, arguments);
                // DON'T TRUST RETURNED DATA
                //setValues($form, data);
            };
        }

        // return the ajax thing for method chaining
        return xhr.request(opts);

    };

    // $('form.foo').submitJSON();
    $.fn.submitJSON = function(opts){
        $(this).addClass('json');
        return xhr.form(this, extend(true, {
            method: this.method || 'POST',
            processData: false,
            contentType: 'application/json'
        }, opts))
    };

    // intercept form submissions with 'ajax' or 'json' class
    // using namespaced event handler submit.json
    $('body').on('submit.json, submit.ajax', 'form.ajax, form.json', function(){
        return xhr.form(this);
    });

    // special case for YUI 'GET' request
    xhr.get.yui = function( /* url, data/null, opts_or_callback, callback */ ){
        var req = new RequestOfType('GET', arguments);
        req.yui = req.yui || true; // preserve 'yui' object if it exists
        return xhr.request(req);
    };

    // get the remote HTML
    xhr.loadHTML = function( $container, url, data, callback ){

        var obj = {
                method: 'GET',
                dataType: 'html'
            },
            success = null;

        // need $container and url arguments at the very least
        if (arguments.length < 2){
            return new Error('XNAT.xhr.loadHTML() requires the $container and url arguments.');
        }

        if (isPlainObject(url)){
            obj = extend(true, {}, url, obj);
        }
        else {
            obj.url = url;
            obj.data = data;
        }

        // if there's a 'success' property, call it
        // after appending the HTML
        if (isFunction(obj.success)){
            success = obj.success;
        }

        obj.success = function(html){
            $$($container).empty().append(html);
            if (success){
                success.apply(this, arguments);
            }
            if (isFunction(callback)){
                callback.apply(this, arguments);
            }
        };

        return xhr.request(obj);

        // make sure we've got a jQuery object and pass to jQuery's .load() method
        //$$($container).load(url, data, callback);

    };

    xhr.getScript = function( /* url, data, opts_or_callback, callback */ ){
        var req = new RequestOfType('GET', arguments);
        req.dataType = 'script';
        req.async = firstDefined(req.async||undefined, true);
        return xhr.request(req);
    };

    // check for existence of script
    // or script name before getting script
    xhr.loadScript = function( url, name, opts ){
        // if there's a <script> with "url" as the
        // src attribute, we don't need to load it
        if ($('script[src="' + url + '"]').length) {
            return false
        }
        // if it's already loaded (by url),
        // don't load it again
        if (XNAT.scripts.indexOf(url) !== -1){
            return false
        }

        // if it's not loaded, do the stuff...

        name = name || null;

        if (XNAT[name]) return;

        //name.loaded = true;

        var _opts = $.extend( {}, opts || {}, {
            async: false,
            success: function(){
                XNAT.scripts.push(url);
            }
        });
        return xhr.getScript( url, _opts );
    };

    // XNAT.xhr.loaded
    xhr.loaded = true;

})(XNAT, jQuery, YAHOO);

