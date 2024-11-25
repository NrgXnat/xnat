/*
 * web: panel.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Functions for creating XNAT tab UI elements
 */

var XNAT = getObject(XNAT || {});

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

    var panel,
        $       = window.jQuery,
        spawn   = window.spawn,
        element = XNAT.element,
        multiform = {
            count: 0,
            errors: 0
        },
        undef, undefined;


    XNAT.ui =
        getObject(XNAT.ui || {});

    function setDisabled(elements, disabled){
        $$(elements).each(function(idx){
            var _disabled = !!disabled;
            var modifyClass = _disabled ? 'addClass' : 'removeClass';
            // this.disabled = _disabled;
            $(this).prop('disabled', _disabled)[modifyClass]('disabled');
        });
    }

    // string that indicates to look for a namespaced object value
    var lookupPrefix = '??';

    function lookupValue(input){
        if (!input) return '';
        if (input.toString().indexOf(lookupPrefix) === 0){
            input = input.split(lookupPrefix)[1].trim();
            return lookupObjectValue(window, input);
        }
        return input;
    }

    function pxSuffix(val){
        if (typeof val === 'number') {
            val = val + 'px'
        }
        return val;
    }

    // replace url params with values from [data-param] attribute (if it exists)
    // <form data-method="put" action="/xapi/theme/[[themeName]]">
    //     <input type="text" name="themeName">
    // </form>
    function urlParams(form, url, params){

        var $form = $$(form);
        var URL = url || $form.attr('action') || $form.data('url');

        // replace items in url string
        if (/{{|{\(/.test(URL)) {
            URL = strReplace(URL);
        }

        // which params are we replacing?
        // /url/path/[[param1]]/[[param2]]
        var urlParams = URL.split('[[').slice(1).map(function(name){
            return name.split(']]')[0].trim();
        });

        // return url if no params to replace
        if (!urlParams.length) {
            return URL;
        }

        // optionally pass an object map with the values
        params = getObject(params);

        forEach(urlParams, function(name){
            var $input = $form.find('[name="' + name + '"]');
            var _str = '[[' + name + ']]';
            var _val = isDefined(params[name]) ? params[name]+'' : '';
            if (!_val && $input.length) {
                _val = $input.filter(':checked').val() || $input[0].value
            }
            URL = URL.replace(_str, _val);
        });

        return URL;

    }

    function loadingDialog(id){
        id = id ? '#'+id : '#loading';
        // make sure xmodal script is loaded
        if (xmodal && xmodal.loading) {
            return {
                open: function(){
                    return xmodal.loading.open(id);
                },
                close: function(){
                    if (id === '#*') {
                        xmodal.loading.closeAll();
                    }
                    else {
                        xmodal.loading.close(id);
                    }
                },
                closeAll: xmodal.loading.closeAll
            }
        } else {
            return {
                open: function(){
                    // Do nothing
                },
                close: function(){
                    // Do nothing
                },
                closeAll: function(){
                    // Do nothing
                }
            }
        }
    }


    function addValidation(config){
        var _validate;
        if (config.validate || config.validation) {
            _validate = (config.validate || config.validation);
            config.data = config.data || {};
            if (typeof _validate === 'string') {
                config.data.validate = _validate;
                if (config.message) {
                    config.data.message = config.message;
                }
            }
            else {
                config.data.validate = _validate.type;
                config.datamessage = _validate.message || '';
            }
            delete config.validate;
            delete config.validation;
            delete config.message;
        }
        return config;
    }


    /**
     * Initialize panel.
     * @param [opts] {Object} Config object
     * @returns {{}}
     */
    panel = function panelInit(opts){

        opts = cloneObject(opts);
        opts.title = opts.title || opts.label || opts.header;

        // attributes for the outer 'panel' element
        opts.panel = extend(true, {
            style: {}
        }, opts.panel);

        // attributes for the inner 'panel-body' element
        opts.element = opts.element || opts.config || {};
        opts.element.style = getObject(opts.element.style);

        if (isDefined(opts.padding)) {
            opts.element.style.padding = pxSuffix(opts.padding);
        }

        if (opts.borderless || opts.border === false) {
            opts.element.style.border = 'none';
            opts.panel.style.border = 'none';
        }

        opts.bodyContent = firstDefined(opts.bodyContent, opts.body, []);

        var _target = spawn('div.panel-body', opts.element, [].concat(opts.bodyContent)),

            hideHeader = (isDefined(opts.header) && (opts.header === false || /^-/.test(opts.title))),

            hideFooter = (isDefined(opts.footer) && (opts.footer === false || /^-/.test(opts.footer))),

            _panel  = spawn('div.panel.panel-default', extend(true, {}, opts.panel), [

                (hideHeader ? ['div.hidden'] : ['div.panel-heading', [
                    ['h3.panel-title', opts.title]
                ]]),

                // target is where the next spawned item will render
                _target,

                (hideFooter ? ['div.hidden'] : ['div.panel-footer', opts.footer])

            ]);

        // add an id to the outer panel element if present
        if (opts.id || opts.element.id) {
            _panel.id = (opts.id || opts.element.id);
        }

        return {
            target: _target,
            element: _panel,
            spawned: _panel,
            get: function(){
                return _panel;
            },
            render: function(container){
                if (!container){
                    return _panel;
                }
                else {
                    $$(container).append(_panel);
                    return _panel;
                }
            }
        }
    };
    panel.init = panel;

    function footerButton(text, type, disabled, classes){
        if (!text) return [''];
        var button = {
            type: type || 'button',
            html: text || 'Submit'
        };
        button.classes = [classes || '', 'btn btn-sm'];
        if (type === 'link') {
            button.classes.push('btn-link')
        }
        else if (/submit|primary/.test(type)) {
            button.classes.push('submit')
        }
        // if (disabled === true) {
        //     button.classes.push('disabled');
        //     button.disabled = true
        // }
        return $.spawn('button', button);
    }

    // populate the data fields if this panel is in the 'active' tab
    // (only getting values for the active tab should cut down on requests)
    // function loadDataNot(form, obj){
    //
    //     obj = cloneObject(obj);
    //
    //     // 'load' is a url, object, or eval string
    //     obj.load = obj.load || obj.url;
    //
    //     // 'onload' is a callback function
    //     if (typeof obj.onload === 'string') {
    //         obj.onload = eval(obj.onload);
    //     }
    //     else {
    //         obj.onload = obj.onload || diddly;
    //     }
    //
    //     // need a form to put the data into!
    //     // and a 'load' property too
    //     if (!form || !obj.load) {
    //         loadingDialog().close();
    //         return;
    //     }
    //
    //     var $form = $$(form);
    //     var _form = $form[0];
    //     var values = {};
    //     var name;
    //     var tmp = '';
    //
    //     obj.load = (obj.load+'').trim();
    //
    //     // if 'load' starts with '#?' execute global (namespaced) function
    //     var fnExe = XNAT.parse.REGEX.fnPrefix.test(obj.load) ? XNAT.parse(obj.load) : null;
    //     if (fnExe && fnExe.done) {
    //         try {
    //             fnExe.done(function(){
    //                 var obj = this;
    //                 if (isPlainObject(obj.result)) {
    //                     $form.setValues(obj.result);
    //                 }
    //             });
    //             return fnExe;
    //         }
    //         catch (e) {
    //             if (jsdebug) console.error(e);
    //             return;
    //         }
    //     }
    //
    //     obj.queryString = '';
    //
    //     // save query string
    //     // if (/.{3}[?]/.test(obj.load)){
    //     //     tmp = obj.load.split('?');
    //     //     obj.queryString = '?' + tmp[tmp.length-1];
    //     //     tmp = '';
    //     // }
    //
    //     // regex for loading returned value to one specific input
    //     var loadInput = /\s*[>:]\s*\[\[\s*(.+)\s*]]\s*/;
    //     var loadParts = [];
    //
    //     // extract name of form element to receive loaded value
    //     // (if raw data is returned instead of JSON with key: value)
    //     // append element name to 'load' value, like:
    //     // /data/projects/foo/quarantine_code > [[quarantine]]
    //     if (loadInput.test(obj.load)){
    //         loadParts = obj.load.split(loadInput);
    //         name = loadParts[1];
    //         obj.load = loadParts[0] + obj.queryString;
    //     }
    //
    //     // if 'load' starts with '$?', '~/', or just '/'
    //     // then values need to load via REST
    //     var ajaxPrefix = /^(\$\?|~\/|\/)/;
    //     var doAjax = ajaxPrefix.test(obj.load);
    //
    //     // if 'load' starts with '!?' do an eval()
    //     var evalPrefix = /^!\?/;
    //
    //     // if 'load' starts with ?? (or NOT evalPrefix or ajaxPrefix), do lookup
    //     var lookupPrefix = /^\?\?/;
    //
    //     // ...BUT...
    //     // if there's an existing property name:
    //     // XNAT.data['/rest/url']
    //     // that matches the URL, then use that
    //     if (doAjax) {
    //         // remove ajax prefix
    //         obj.load = obj.load.replace(ajaxPrefix, '') .trim();
    //         // add serverRoot to load url
    //         obj.load = XNAT.url.rootUrl(obj.load);
    //         // replace url params
    //         obj.load = urlParams($form, obj.load);
    //         // if data is already cached...
    //         if (!obj.reload && XNAT.data && XNAT.data[obj.load]) {
    //             // don't do another request
    //         doAjax = false;
    //         obj.prop = obj.load;
    //         obj.load = '??:XNAT:data'
    //     }
    //     }
    //
    //     if (!doAjax) {
    //
    //         var doLookup = lookupPrefix.test(obj.load);
    //         if (doLookup) {
    //             obj.load = (obj.load.split(lookupPrefix)[1]||'').trim().split('|')[0];
    //             obj.prop = obj.prop || obj.load.split('|')[1] || '';
    //
    //             // get the values
    //             if (name) {
    //                 values[name] = lookupObjectValue(window, obj.load, obj.prop)[name];
    //             }
    //             else {
    //             values = lookupObjectValue(window, obj.load, obj.prop);
    //             }
    //
    //             // set values of form elements
    //             $form.setValues(values);
    //
    //             loadingDialog().close();
    //
    //             obj.onload.call(_form, values);
    //
    //             return _form;
    //
    //         }
    //
    //         var doEval = evalPrefix.test(obj.load);
    //         if (doEval) {
    //             obj.load = (obj.load.split(evalPrefix)[1]||'').trim();
    //         }
    //
    //         // lastly try to eval the 'load' value
    //         try {
    //             if (name) {
    //                 values[name] = (eval(obj.load))[name];
    //             }
    //             else {
    //             values = eval(obj.load);
    //             }
    //             // set values of form elements
    //             $form.setValues(values);
    //         }
    //         catch (e) {
    //             console.log(e);
    //         }
    //
    //         loadingDialog().close();
    //
    //         obj.onload.call(_form, values);
    //
    //         return _form;
    //
    //     }
    //
    //     //////////
    //     // REST
    //     //////////
    //
    //     var ajaxUrl = obj.refresh || '';
    //
    //     // if 'load' starts with $?, do ajax request
    //     //var ajaxPrefix = '$?';
    //     var ajaxProp = '';
    //
    //     // value: $? /path/to/data | obj:prop:name
    //     // value: ~/path/to/data|obj.prop.name
    //     if (doAjax) {
    //         ajaxUrl = (obj.load.split(ajaxPrefix)[2]||'').trim().split('|')[0];
    //         ajaxProp = obj.load.split('|')[1] || '';
    //     }
    //
    //     // need a url to get the data
    //     if (!ajaxUrl || !stringable(ajaxUrl)) {
    //         loadingDialog().close();
    //         return _form;
    //     }
    //
    //     // force GET method
    //     obj.method = 'GET';
    //
    //     // setup the ajax request
    //     // override values with an
    //     // 'ajax' or 'xhr' property
    //     obj.ajax = extend(true, {
    //         method: obj.method,
    //         url: XNAT.url.rootUrl(ajaxUrl)
    //     }, obj.ajax || obj.xhr);
    //
    //     obj.ajax.success = function(data){
    //
    //         if (ajaxProp){
    //             data = data[ajaxProp];
    //         }
    //         $form.dataAttr('status', 'clean');
    //
    //         if (name) {
    //             values[name] = data;
    //         }
    //         else {
    //             values = data;
    //         }
    //
    //         // cache returned data
    //         XNAT.data[obj.ajax.url] = values;
    //
    //         // set values of form elements
    //         $form.setValues(values);
    //
    //         obj.onload.apply(_form, arguments);
    //
    //     };
    //
    //     obj.ajax.error = function(){
    //         $form.dataAttr('status', 'error');
    //     };
    //
    //     obj.ajax.complete = function(){
    //         loadingDialog().close();
    //     };
    //
    //     // return the ajax thing for method chaining
    //     return XNAT.xhr.request(obj.ajax);
    //
    // }

    // creates a panel that's a form that can be submitted
    // TODO: REFACTOR THIS BEAST
    panel.form = function panelForm(opts, callback){

        opts = cloneObject(opts);
        opts.element = opts.element || opts.config || {};
        opts.title = opts.title || opts.label || opts.header;
        opts.name = opts.name || opts.element.name || opts.id || opts.element.id || randomID('form-', false);
        opts.action = firstDefined(opts.action, opts.url);
        opts.load = firstDefined(opts.load, opts.url, opts.action);

        var payloadRegex = /<\s*\[\[(.*)]]$/;
        var parts;

        // to submit raw data from a single input, specify the 'name' at the beginning
        // of the 'action' or 'url' property, like:
        // action: /data/projects/ALI_US_X8W/config/separatePETMR/config?inbody=true&XNAT_CSRF={{csrfToken}} < [[separatePETMR|text/plain]]
        if (payloadRegex.test(opts.action)) {
            parts = opts.action.split(payloadRegex);
            opts.payload = parts[1].trim();
            opts.action = parts[0].trim();
            opts.processData = opts.processData || false;
        }

        var dataObj = {};

        dataObj.panel = toDashed(opts.name);

        if (!/null|false/i.test(opts.method)) {
            dataObj.method = (opts.method || 'POST').toLowerCase();
        }

        if (!/null|false/i.test(opts.load) && isString(opts.load)) {
            dataObj.load = opts.load;
        }

        if (opts.payload) {
            dataObj.payload = opts.payload;
        }

        // data-* attributes to add to panel
        addDataObjects(opts, dataObj);

        // text for 'submit' button
        opts.submit = firstDefined(opts.submit, 'Save');
        // text for 'reset' button
        opts.reset = firstDefined(opts.reset, 'Discard Changes');

        if (opts.action !== undef) {
            opts.action = opts.action.replace(XNAT.parse.REGEX.ajaxPrefix, '');
            opts.action = (opts.action && /^[*~/.]/.test(opts.action)) ? XNAT.url.rootUrl(opts.action) : opts.action || '#!';
        }
        else {
            opts.action = '#!'
        }

        if (opts.onsubmit) {
            opts.element.onsubmit = opts.onsubmit;
        }

        if ('params' in opts){
            if (isPlainObject(opts.params)) {
                forOwn(opts.params, function(key, val){
                    opts.params[key] = strReplace(val);
                });
            }
            else if (Array.isArray(opts.params)) {
                opts.params = opts.params.map(function(param){
                    return strReplace(param);
                })
            }
            else {
                opts.params = strReplace(opts.params)
            }
            opts.action = XNAT.url.updateQueryString(opts.action, opts.params);
        }

        var _target = spawn('div.panel-body', {}, [].concat(opts.body||[])),

            hideHeader = (isDefined(opts.header) && (opts.header === false || /^-/.test(opts.title))),

            hideFooter = (isDefined(opts.footer) && (opts.footer === false || /^-/.test(opts.footer))),

            $saveBtn = footerButton(opts.submit, 'submit', true, 'submit save pull-right'),
            _saveBtn = $saveBtn[0],

            $resetBtn = footerButton(opts.reset, 'reset', true, 'revert pull-right'),
            _resetBtn = $resetBtn[0],

            _footer = [
                _saveBtn,
                ['span.pull-right', '&nbsp;&nbsp;&nbsp;'],
                _resetBtn,
                //['button.btn.btn-sm.btn-link.defaults.pull-left', 'Default Settings'],
                ['div.clear']
            ],

            // TODO: use opts.element for the panel itself
            $formPanel = $.spawn('form.xnat-form-panel.panel.panel-default', extend(true, {
                id: opts.id || opts.element.id || toDashed(opts.name),
                name: opts.name,
                action: opts.action,
                addClass: opts.classes || '',
                data: opts.data
            }, opts.element), [

                (hideHeader ? ['div.hidden'] : ['div.panel-heading', [
                    ['h3.panel-title', opts.title]
                ]]),

                // target is where this form's "contents" will be inserted
                _target,

                (hideFooter ? ['div.hidden'] : ['div.panel-footer', opts.footer || _footer])

            ]);

        // expose the raw form element
        var _formPanel = $formPanel[0];

        // add [method] attribute ONLY for POST or GET
        if (/POST|GET/i.test(opts.method+'')) {
            _formPanel.method = opts.method;
        }

        // if there's a 'validation' (or 'validate') property, add 'validate' class
        if (opts.validation || opts.validate) {
            addClassName(_formPanel, 'validate');
            // addDataObjects()
        }

        // set form element values from an object map
        // HANDLED BY $('form').setValues({name:'value'}) now

        // if (opts.load) {
        //     // loadData(_formPanel, opts)
        //     XNAT.form.setValues($formPanel, opts.load)
        // }

        // 'onload' and 'callback' are the same ('onload' takes priority)
        opts.onload = opts.callback =
                opts.onload || opts.callback || callback;

        if (isString(opts.onload)) {
            opts.onload = lookupObjectValue(opts.onload);
        }

        opts.onload = isFunction(opts.onload) ? opts.onload : diddly;

        // custom event for reloading data (refresh)
        $formPanel.on('reload-data', function reloadData(){

            var form$ = $(this);
            var loadUrl = opts.refresh || opts.load || opts.url || form$.data('load') || form$.attr('action');

            form$.removeClass('ready dirty error valid invalid');
            form$.find('.valid, .invalid').removeClass('valid invalid');
            form$.find('.ready').removeClass('ready');
            form$.find('.dirty').removeClass('dirty');

            // deleting the cached data will force a refresh
            if (XNAT.data && XNAT.data[loadUrl]) {
                delete XNAT.data[loadUrl]
            }

            // prepending '$? *' will force fresh data to be loaded from the server
            var loadUrlX = '$? *' + loadUrl.replace(XNAT.parse.REGEX.ajaxPrefix, '');

            XNAT.form.setValues(form$, loadUrlX);

            // loadData(this, {
            //     load: _load,
            //     reload: true, // force data reload (don't use stale cached data)
            //     onload: function(){
            //         // fire an 'onload' callback, if specified
            //         opts.onload.apply(_formPanel, arguments);
            //         // setDisabled([$saveBtn, $resetBtn], true);
            //     }
            // });
        });

        if (_resetBtn) {
        // click 'Discard Changes' button to reload data
            $resetBtn.on('click', function(e){
                // don't fire default 'reset' event (causes flash of empty inputs)
                e.preventDefault();
                $formPanel.triggerHandler('reload-data');
            });
        }

        // is this form part of a multiForm?
        multiform.parent = $formPanel.closest('form.multi-form');

        if (multiform.parent) {
            multiform.count = $(multiform.parent).find('form').length
        }

        multiform.errors = 0;

        // keep an eye on the inputs but only when they're 'ready'
        // $formPanel.on('focus', ':input.ready', function(){
        //     setDisabled([$saveBtn, $resetBtn], false);
        // });

        // intercept the form submit to do it via REST instead
        $formPanel.on('submit', function(e){
            e.preventDefault();
            e.stopImmediatePropagation();
            return false;
        });

        $formPanel.on('submit-data', function(e){

            var $form = $(this).removeClass('error invalid');
            var form0 = $form[0];
            var silent = $form.hasClass('silent');
            var formAction = $form.attr('action');
            var formSubmit;
            var multiform = {};

            $form.dataAttr('errors', 0);

            function validateForm(){
                var errors = [],
                    errorCount = 0,
                    valid = true;

                // validate inputs before moving on
                $form.find(':input[data-validate]').not('.ignore').each(function(){
                    valid = XNAT.validate(this);
                    if (!valid.check()) {
                        errorCount++;
                        errors.push({
                            field: this.title||this.name||this.id||this.tagName,
                            message: $(this).data('message') || ''
                        });
                    }
                });

                $form.dataAttr('errors', errorCount);

                if (errorCount) {
                    $form.addClass('error');
                    if (!silent) {
                        errors = errors.map(function(error){
                            return '' +
                                '<li>' +
                                '<b>' + error.field + (error.message ? ':</b><br>' + error.message : '</b>') +
                                '</li>'
                        });
                        XNAT.dialog.message({
                            title: 'Validation Error',
                            content: '' +
                            '<p>Please correct the following fields and re-submit the form:</p>' +
                            '<ul>' + errors.join('') + '</ul>' +
                            '<br>',
                            // height: 300,
                            width: 500
                        });
                    }
                    return false;
                }
                return true;
            }

            // execute onsubmit handler for "#?:submitFunction()"
            if (/^#\?/.test(formAction)) {
                formAction = formAction.replace(/^(#\?[:=\s]*)|(\(\))$/g, '');
                formSubmit = lookupObjectValue(formAction);
                if (validateForm() && isFunction(formSubmit)) {
                    return formSubmit.call(form0, $form, e);
                }
                else {
                    return false;
                }
            }

            // don't submit forms with 'action' starting with '#'
            if (/^#/.test(formAction)) {
                return false;
            }

            // only open loading dialog for standard (non-multi) submit
            if (!multiform.count){
                //var saveLoader = xmodal.loading.open('#form-save');
            }

            // var ajaxSubmitOpts = {
            //
            //     target:        '#server-response',  // target element(s) to be updated with server response
            //     beforeSubmit:  function(){},  // pre-submit callback
            //     success:       function(){},  // post-submit callback
            //
            //     // other available options:
            //     url:       '/url/for/submit', // override for form's 'action' attribute
            //     type:      'get or post (or put?)', // 'get' or 'post', override for form's 'method' attribute
            //     dataType:  null,        // 'xml', 'script', or 'json' (expected server response type)
            //     clearForm: true,        // clear all form fields after successful submit
            //     resetForm: true,        // reset the form after successful submit
            //
            //     // $.ajax options can be used here too, for example:
            //     timeout:   3000
            //
            // };

            function formToJSON(form){
                var json = {};
                $$(form).serializeArray().forEach(function(item) {
                    if (!item.name) return;
                    var name = item.name.replace(/^:/,'');
                    var val = firstDefined(item.value+'', '');
                    if (typeof json[name] === 'undefined') {
                        json[name] = val;
                    }
                    else {
                        json[name] = [].concat(json[name], val||[]) ;
                    }
                });
                return json;
            }

            var ajaxConfig = {
                //method: opts.method,
                method: $form.data('method') || opts.method || 'POST',
                processData: firstDefined(opts.processData||undefined, true),
                url: urlParams($form),
                validate: function(){
                    return validateForm();
                },
                success: function(){
                    var obj = {}, callback;
                    // actually, NEVER use returned data...
                    // ALWAYS reload from the server
                    // (prepending '$?' assures that)
                    obj.load = opts.refresh || opts.url || opts.load;
                    obj.reload = true; // force reload after submission
                    if (!silent){
                        XNAT.ui.banner.top(2000, 'Data saved successfully.', 'success');
                        $form.triggerHandler('reload-data');
                        // XNAT.form.setValues($form, obj);
                        // loadData($form, obj);
                    }
                    $form.removeClass('error invalid').dataAttr('errors', 0);
                    $form.find(':input[data-validate]').removeClass('valid invalid');
                    $form.find('.dirty').removeClass('dirty');
                    // fire callback function if specified
                    if (opts.success || opts.callback) {
                        callback = opts.success||opts.callback;
                        try {
                        if (typeof callback === 'string') {
                            callback = window[callback];
                        }
                            callback.apply(this, arguments);
                        }
                        catch(e) {
                            console.log('something is broken: ' + e);
                        }
                    }
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    XNAT.dialog.message({
                        title: textStatus +': ' + errorThrown,
                        content: '' +
                            '<p>' + jqXHR.responseText + '</p>',
                        width: 500
                    });
                }
            };

            // 'payload' property specifies name of element containing submission data
            if (opts.payload || $form.data('payload')) {
                opts.payload = opts.payload || $form.data('payload');
                ajaxConfig.contentType = opts.payload.split('|')[1] || 'text/plain';
                ajaxConfig.processData = opts.processData || false;
                opts.payload = opts.payload.split('|')[0];
                ajaxConfig.data = $form.find('[name="' + opts.payload +'"]').map(function(i, el){
                    if (/radio|checkbox/i.test(this.type) && this.checked) {
                        return this.value || this.checked;
                    }
                    else {
                        return this.value
                    }
                }).get().join(',');
            }

            var inputs = $form.find(':input').not('button, [type="submit"]').toArray();

            if (/json/i.test(opts.contentType||'')){
                // ajaxConfig.data = JSON.stringify(formToJSON(this));
                // ajaxConfig.data = JSON.stringify(form2js(this, /[:\[\]]/));
                ajaxConfig.data = JSON.stringify(form2js(inputs, ':', false));
                ajaxConfig.processData = false;
                if (opts.contentType === 'text/json') {
                    ajaxConfig.contentType = opts.contentType;
                }
                else {
                    ajaxConfig.contentType = 'application/json';
                }
            }

            // submit data with XNAT's AJAX form submit method
            XNAT.xhr.form($form, ajaxConfig);

            return false;

        });

        // 'Save' button triggers a custom 'submit-data' event
        $saveBtn.on('click', function(){
            if (!/^#!/.test($formPanel.attr('action'))){
                $formPanel.triggerHandler('submit-data');
            }
        });


        function loadValues(){
            try {
                if (isPlainObject(opts.loadData || opts.load)) {
                    XNAT.form.setValues($formPanel, (opts.loadData || opts.load));
                }
                else if (opts.load || opts.url) {
                    XNAT.form.setValues($formPanel, ('$?' + (opts.load || opts.url || '').replace(XNAT.parse.REGEX.ajaxPrefix, '')));
                }
            }
            catch (e) {
                console.error(e);
            }
        }

        // this object is returned to the XNAT.spawner() method
        return {
            load: loadValues,
            // setValues: setValues,
            target: _target,
            element: _formPanel,
            spawned: _formPanel,
            get: function(){
                return _formPanel;
            },
            render: function(container){
                if (!container) {
                    return _formPanel;
                }
                else {
                    $$(container).append(_formPanel);
                    return _formPanel;
                }
            }
        }
    };
    panel.form.init = panel.form;

    // creates a panel that submits all forms contained within
    panel.multiForm = function(opts, callback){

        opts = cloneObject(opts);
        opts.element = opts.element || opts.config || {};

        var inner = spawn('div.panel-body', opts.element),

            hideFooter = (isDefined(opts.footer) && (opts.footer === false || /^-/.test(opts.footer))),

            submitBtn = spawn('button', {
                type: 'submit',
                classes: 'btn submit save pull-right',
                html: 'Save All'
            }),

            resetBtn  = spawn('button', {
                type: 'button',
                classes: 'btn revert pull-right',
                html: 'Discard Changes',
                onclick: function(e){
                    e.preventDefault();
                    $(this).closest('form.multi-form').find('form').each(function(){
                        $(this).triggerHandler('reload-data');
                    });
                    return false;
                }
            }),

            defaults = spawn('button', {
                type: 'button',
                classes: 'btn btn-link defaults pull-left',
                html: 'Default Settings'
            }),

            footer = [
                submitBtn,
                ['span.pull-right', '&nbsp;&nbsp;&nbsp;'],
                resetBtn,
                // defaults,
                ['div.clear']
            ],

            multiForm = spawn('form', {
                classes: 'xnat-form-panel multi-form panel panel-default',
                method: opts.method || 'POST',
                action: (opts.action && !/^#/.test(opts.action)) ? XNAT.url.rootUrl(opts.action) : opts.action || '#!',
                onsubmit: function(e){

                    e.preventDefault();
                    var $forms = $(this).find('form');

                    var loader = loadingDialog('multi-save').open();

                    // reset error count on new submission
                    multiform.errors = 0;

                    // how many child forms are there?
                    multiform.count = $forms.length;

                    // submit ALL enclosed forms
                    $forms.each(function(){
                        $(this).addClass('silent').trigger('submit-data');
                    });

                    if (multiform.errors) {
                        xmodal.closeAll();
                        XNAT.dialog.closeAll();
                        XNAT.dialog.message('Error', 'Please correct the highlighted errors and re-submit the form.');
                        return false;
                    }

                    // multiform.errors = 0;
                    // multiform.count = 0;

                    loader.close();

                    // fire the callback function after all forms are submitted error-free


                    XNAT.dialog.message({
                        title: 'Setup Complete',
                        closeBtn: false,
                        content: 'Your XNAT site is ready to use. Click "OK" to continue to the home page.',
                        action: function(){
                            window.location.href = XNAT.url.rootUrl('/setup?init=true');
                            //window.location.href = XNAT.url.rootUrl('/');
                            //$forms.each.triggerHandler('reload-data');
                        }
                    });

                    return false;

                }
            }, [
                ['div.panel-heading', [
                    ['h3.panel-title', opts.title || opts.label]
                ]],


                // 'inner' is where the next spawned item will render
                inner,


                (hideFooter ? ['div.hidden'] : ['div.panel-footer', opts.footer || footer])

            ]);

        // add an id to the outer panel element if present
        if (opts.id || opts.element.id) {
            multiForm.id = opts.id || (opts.element.id);
        }

        return {
            target: inner,
            element: multiForm,
            spawned: multiForm,
            get: function(){
                return multiForm
            }
        }
    };

    // setup a dialog box that contains stuff
    panel.dialogForm = panel.formDialog = function(opts){
        var dialog = new xmodal.Modal
    };

    panel.info = function(opts){};
    panel.info.dialog = {};

    //
    function panelElementInfo(opts){

    }

    // create a single generic panel element
    panel.element = function(opts){

        var _element, _inner = [], _target,
            infoId = '', infoIcon = '', _info = '';

        opts = cloneObject(opts);
        opts.element = opts.element || opts.config || {};

        if (opts.id || opts.element.id) {
            opts.element.id = (opts.id || opts.element.id) + '-element';
        }

        if (opts.name || opts.element.name) {
            opts.element.name = (opts.element.name || opts.name).replace(/^:+/, '')
        }

        addClassName(opts.element, 'panel-element');
        addDataObjects(opts.element, { name: opts.element.name });

        opts.label = opts.label||opts.title||'';

        // add a help info icon if one is specified
        if (opts.info){

            infoId = randomID('i', false);

            // infoIcon = spawn('img', {
            //     src: XNAT.url.rootUrl('/style/icons/icon-qm-48.png'),
            //     width: 16,
            //     height: 16
            // });
            infoIcon = spawn('i.fa.fa-question-circle', {
                style: {
                    'font-size': '16px',
                    'color':'#1A75C2'
                }
            });

            _info = spawn('a.infolink|href=#!', {
                id: infoId,
                style: {
                    position: 'relative',
                    top: '0',
                    right: '8px'
                }
            }, infoIcon);

            panel.info.dialog[infoId] = function(){
                console.log('info for ' + opts.label);
                XNAT.ui.dialog.message({
                    title: opts.label,
                    content: opts.info,
                    nuke: true,
                    // height: 240,
                    footer: false
                });
            };

            //_inner.push(_info);
            // infoContent[infoId++] = {label:opts.label, content:opts.info};
        }

        _inner.push(['div.element-label', [_info, opts.label]]);

        // 'contents' will be inserted into the 'target' element
        _target = spawn('div.element-wrapper', [].concat(opts.body||opts.html||[]));

        // add the target to the content array
        _inner.push(_target);

        // add a description if there is one
        if (opts.description){
            _inner.push(['div.description', opts.description]);
        }

        // add element to clear floats
        _inner.push(['br.clear']);

        _element = spawn('div', opts.element, _inner);

        return {
            target: _target,
            element: _element,
            spawned: _element,
            get: function(){
                return _element
            }
        }

    };
    panel.element.init = panel.element;


    $(function(){
        // delegate 'infolink' click handlers to 'body' element
        $('body').on('click', 'a.infolink', function(){
            panel.info.dialog[this.id].apply(this, arguments);
        });
    });


    panel.subhead = function(opts){
        opts = cloneObject(opts);
        opts.html = opts.html || opts.text || opts.label;
        return XNAT.ui.template.panelSubhead(opts);
    };

    // return a generic panel 'section'
    panel.section = function(opts){

        var _section, _inner = [], _body;

        opts = cloneObject(opts);
        opts.element = opts.element || opts.config || {};
        opts.header = opts.header || opts.label || opts.title || '';
        opts.body = [].concat(opts.body||[]);

        if (opts.header) {
            _inner.push(['header.section-header', opts.header]);
        }

        // this needs to be spawned here to act as
        // the target for this elements 'contents'
        _body = spawn('div.section-body', opts.body);

        _inner.push(_body);

        if (opts.footer) {
            _inner.push(['footer.section-footer', opts.footer]);
        }

        _section = spawn('div.panel-section', opts.element, _inner);

        return {
            target: _body,
            element: _section,
            spawned: _section,
            get: function(){
                return _section;
            },
            render: function(container){
                $$(container).append(_section);
                return _section;
            }
        }

    };

    panel.display = function(opts, contents){
        return XNAT.ui.template.panelDisplay(opts, contents);
    };

    panel.input = function(opts, contents){
        return XNAT.ui.template.panelInput(opts, contents).get();
    };

    panel.input.text = function(opts){
        return XNAT.ui.template.panelInput(opts).get();
    };

    panel.input.list = panel.input.textList = panel.input.arrayList = function(opts){
        opts = cloneObject(opts);
        opts.data = getObject(opts.data);
        addClassName(opts, 'array-list');
        addDataObjects(opts, { delim: opts.delim || opts.delimiter || ',' });
        return XNAT.ui.template.panelInput(opts).get();
    };

    panel.input.number = function panelInputNumber(opts){
        opts = cloneObject(opts);
        opts.type = 'number';
        addClassName(opts, 'input-text number');
        return XNAT.ui.template.panelInput(opts).get();
    };

    panel.input.email = function panelInputEmail(opts){
        opts = cloneObject(opts);
        opts.type = 'text';
        addClassName(opts, 'input-text email');
        return XNAT.ui.template.panelInput(opts).get();
    };

    panel.input.username = function panelInputUsername(opts){
        opts = cloneObject(opts);
        opts.element = getObject(opts.element||{});
        opts.element.autocomplete = 'off';
        addClassName(opts, 'input-text username');
        return XNAT.ui.template.panelInput(opts).get();
    };

    panel.input.password = function panelInputPassword(opts){
        opts = cloneObject(opts);
        var config = getObject(opts.element||{});
        config.id = config.id || opts.id;
        config.name = config.name || opts.name;
        config.className = config.className || opts.className;
        if (opts.validate) {
            config.validate = opts.validate;
        }
        if (opts.message) {
            config.message = opts.message;
        }
        // addClassName(opts, 'input-text password');
        var passwordInput = XNAT.ui.input.password(config);
        // return XNAT.ui.template.panelInput(opts, passwordInput.element).spawned;
        // return panel.display(opts, passwordInput.element).spawned;
        return XNAT.ui.template.panelDisplay(opts, passwordInput.element).get();
    };

    panel.input.date = function panelInputDate(opts){
        opts = cloneObject(opts);
        opts.type = 'date';
        addClassName(opts, 'input-text date');
        return XNAT.ui.template.panelInput(opts).get();
    };

    panel.input.checkbox = panel.checkbox = function panelInputCheckbox(opts){
        opts = cloneObject(opts);
        opts.type = 'checkbox';
        addClassName(opts, 'checkbox');
        return XNAT.ui.template.panelInput(opts).get();
    };

    panel.input.switchbox = panel.switchbox = function panelInputSwitchbox(opts){
        opts = cloneObject(opts);
        var switchbox = XNAT.ui.input.switchbox(opts).get();
        // opts.type = 'checkbox';
        // opts.kind = 'switchbox';
        addClassName(opts, 'panel-switchbox switchbox');
        return XNAT.ui.template.panelDisplay(opts, switchbox).get();
    };

    panel.input.radio = panel.radio = function panelInputRadio(opts){
        opts = cloneObject(opts);
        opts.type = 'radio';
        addClassName(opts, 'radio');
        return XNAT.ui.template.panelInput(opts).get();
    };

    panel.input.radioGroup = panel.radioGroup = function panelRadioGroup(opts){
        opts = cloneObject(opts);
        var radioGroup = XNAT.ui.input.radioGroup(opts);
        addClassName(opts, 'panel-radio-group');
        return XNAT.ui.template.panelDisplay(opts, radioGroup.element).get();
    };

    panel.input.hidden = function panelInputHidden(opts){
        opts = cloneObject(opts);
        opts.element = extend(true, {
            type: 'hidden',
            name: opts.name,
            id: opts.id || toDashed(opts.name),
            value: firstDefined(opts.value+'', '')
        }, opts.element);
        addClassName(opts.element, [opts.className, opts.classes, opts.addClass, 'hidden']);
        if (opts.validation || opts.validate) {
            addValidation(opts);
        }
        // no need to wrap this in panel-specific elements
        return spawn('input', opts.element);
    };


    // add event handlers for setting values of
    // hidden inputs controlled by checkboxes
    // $('body').on('change', 'input.checkbox.controller', function(){
    //     $(this).next('input.hidden').val(this.checked);
    // });

    // basic file input widget with no submission handler
    panel.input.file = function panelInputFile(opts){

        opts = cloneObject(opts);

        opts.id =  opts.id || randomID('upldx', false);

        var uploadForm = ['form', {
            id: opts.id + '-form',
            method: opts.method || 'POST',
            action: (opts.action && !/^#/.test(opts.action)) ? XNAT.url.rootUrl(opts.action) : opts.action || '#!',
            className: addClassName(opts, 'file-upload ignore')
        }, [
            ['input', {
                type: 'file',
                id: opts.id + '-input',
                multiple: true,
                className: addClassName(opts, 'file-upload-input ignore')
            }],
            ['button.btn.btn-sm.submit', {
                type: 'submit',
                id: opts.id +'-submit',
                html: 'Upload'
            }]
        ]];

        return XNAT.ui.template.panelInput(opts, uploadForm).get();
    };


    panel.input.upload = function panelInputUpload(opts){

        opts = cloneObject(opts);

        var config = {};
        config.id = (opts.id||randomID('upldx', false));
        config.input = opts.input || opts.element || opts.config || {};
        config.input.id = config.input.id || config.id;

        config.method = opts.method || 'POST';
        config.url = opts.url || (opts.action && !/^#/.test(opts.action)) ? XNAT.url.rootUrl(opts.action) : opts.action || '#!';

        config.form = extend(true, {
            id: config.id + '-form',
            method: config.method || 'POST',
            action: (opts.action && !/^#/.test(opts.action)) ? XNAT.url.rootUrl(opts.action) : opts.action || '#!',
            className: addClassName(config, 'file-upload ignore')
        }, config.form);

        config.input = extend(true, {
            id: config.id + '-input',
            multiple: true,
            className: addClassName(config, 'file-upload-input ignore')
        }, config.input);

        config.button = extend(true, {
            type: 'submit',
            id: config.id + '-submit',
            html: 'Upload'
        }, config.button);

        var uploadForm = XNAT.ui.input.upload(config).get();

        // var uploadForm = ['form', {
        //     id: opts.id + '-form',
        //     method: opts.method || 'POST',
        //     action: opts.action ? XNAT.url.rootUrl(opts.action) : '#!',
        //     className: addClassName(opts, 'file-upload ignore')
        // }, [
        //     ['input', {
        //         type: 'file',
        //         id: opts.id + '-input',
        //         multiple: true,
        //         className: addClassName(opts, 'file-upload-input ignore')
        //     }],
        //     ['button.btn.btn-sm', {
        //         type: 'submit',
        //         id: opts.id +'-submit',
        //         html: 'Upload'
        //     }]
        // ]];

        return XNAT.ui.template.panelInput(opts, uploadForm).get();
    };

    panel.input.group = function panelInputGroup(opts){
        var _inner = [];
        _inner.push(spawn('div.element-group'));
        if (opts.body) {
            _inner.push(opts.body);
        }
        var _outer = XNAT.ui.template.panelElementGroup(opts, _inner).get();
        return {
            target: _inner,
            element: _outer,
            spawned: _outer,
            get: function(){
                return _outer;
            },
            render: function(container){
                $$(container).append(_outer);
                return _outer;
            }
        }
    };

    panel.textarea = function(opts){
        var textarea = XNAT.ui.input.textarea(opts).get();
        return XNAT.ui.template.panelDisplay(opts, textarea);
    };
    panel.input.textarea = panel.textarea;

    panel.textarea.list = panel.textarea.arrayList = function(opts){
        var textarea = XNAT.ui.input.textarea.list(opts).get();
        return XNAT.ui.template.panelDisplay(opts, textarea);
    };

    //////////////////////////////////////////////////
    // SELECT MENU PANEL ELEMENTS
    //////////////////////////////////////////////////

    panel.select = {};

    panel.select.menu = function panelSelectMenu(opts, multi){

        var _menu;

        opts = cloneObject(opts);

        opts.name = opts.name || opts.id || randomID('select-', false);
        opts.id = opts.id || toDashed(opts.name||'');
        opts.element = extend({
            id: opts.id,
            name: opts.name,
            title: opts.title||opts.name||opts.id||'',
            value: firstDefined(opts.value+'', '')
        }, opts.element);

        addClassName(opts.element, [opts.className, opts.classes, opts.addClass]);

        if (multi) {
            opts.element.multiple = true;
        }

        // set label: false so it's not created in XNAT.ui.select.menu()
        //opts.label = false;

        // use XNAT.ui.select.menu() to normalize rendering
        _menu = XNAT.ui.select.menu(extend({}, opts, { label: false })).get();

        return XNAT.ui.template.panelInput(opts, _menu).get();

    };
    panel.select.init = panel.select.menu;
    panel.select.single = panel.select.menu;

    panel.select.multi = function panelSelectMulti(opts){
        return panel.select.menu(opts, true)
    };
    panel.select.multi.init = panel.select.multi;
    panel.select.multiple   = panel.select.multi;



    //////////////////////////////////////////////////
    // DATA PANELS - RETRIEVE/DISPLAY DATA
    //////////////////////////////////////////////////

    panel.dataTable = function(opts){

        opts = cloneObject(opts);

        // initialize the table
        var dataTable = XNAT.table.dataTable(opts.data||[], opts);

        var panelTable = panel.display({
            label: opts.label||false,
            description: opts.description
        });

        panelTable.target.appendChild(dataTable.table);

        return panelTable;

    };

    panel.dataList = function(opts){
        // initialize the table
        opts = cloneObject(opts);
        opts.element = opts.element || {};
        addClassName(opts.element, 'data-list');
        var dataList = spawn('ul', opts.element);
        XNAT.xhr.get({
            url: XNAT.url.rootUrl(opts.load||opts.url),
            dataType: opts.dataType || 'json',
            success: function(data){

            }
        });


    };

    return XNAT.ui.panel = panel;



    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // STOP EVERYTHING!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // EVERYTHING BELOW HERE IS EFFECTIVELY DISABLED
    // WITH THE return STATEMENT ABOVE
    //
    // IT IS BEING KEPT AROUND TEMPORARILY FOR REFERENCE
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


    // /**
    //  * Panel widget with default 'Submit' and 'Revert' buttons
    //  * @param opts
    //  * @param container
    //  * @returns {*}
    //  */
    // panel.form = function panelForm(opts, container){
    //
    //     var _panel, $panel,
    //         saveBtn, revertBtn,
    //         $saveBtn, $revertBtn;
    //
    //     opts.tag = 'form.xnat-form-panel';
    //
    //     opts.title = opts.label;
    //
    //     opts.body = [];
    //
    //     if (opts.description) {
    //         opts.body.push(element.p(opts.description || ''))
    //     }
    //
    //     if (opts.elements) {
    //         opts.body = opts.body.concat(setupElements(opts.elements))
    //     }
    //
    //     saveBtn   = footerButton('Save', 'submit', true, 'save pull-right');
    //     revertBtn = footerButton('Discard Changes', 'button', true, 'revert pull-right');
    //
    //     opts.footer = [
    //         saveBtn,
    //         spawn('span.pull-right', '&nbsp;&nbsp;&nbsp;'),
    //         revertBtn,
    //         footerButton('Default Settings', 'link', false, 'defaults pull-left'),
    //         spawn('div.clear')
    //     ];
    //
    //     // CREATE THE PANEL
    //     _panel = panel(opts);
    //
    //     $panel = $(_panel.element);
    //     $saveBtn = $(saveBtn);
    //     $revertBtn = $(revertBtn);
    //
    //     // what's the submission method?
    //     var method = opts.method || 'GET';
    //
    //     method = method.toUpperCase();
    //
    //     var url = '#';
    //
    //     if (method === 'GET') {
    //         url = XNAT.url.restUrl(opts.url)
    //     }
    //     else if (/PUT|POST|DELETE/.test(method)) {
    //         // add CSRF token for PUT, POST, or DELETE
    //         url = XNAT.url.csrfUrl(opts.url)
    //     }
    //     else {
    //         // any other 'method' value is ignored
    //     }
    //
    //     // set panel as 'dirty' when stuff changes
    //     $panel.on('change', ':input', function(){
    //         $(this).addClass('dirty');
    //         setDisabled($panel.find('.panel-footer button'), false);
    //     });
    //
    //     $panel.on('submit', function(e){
    //         e.preventDefault();
    //         // submit the data and disable 'Save' and 'Revert' buttons
    //         if (opts.url) {
    //             try {
    //                 XNAT.xhr.request({
    //                     method: method,
    //                     url: url,
    //                     data: $panel,
    //                     success: function(){
    //                         console.log('success!');
    //                         setDisabled([$saveBtn, $revertBtn], true);
    //                     },
    //                     error: function(){
    //                         console.log('error.')
    //                     }
    //                 })
    //             }
    //             catch (e) {
    //                 setDisabled([$saveBtn, $revertBtn], true);
    //                 console.log(e)
    //             }
    //         }
    //     });
    //
    //     _panel.revert = function(){
    //         discardChanges($panel);
    //         setDisabled([$saveBtn, $revertBtn], true);
    //     };
    //
    //     $revertBtn.on('click', _panel.revert);
    //
    //     _panel.panel = _panel.element;
    //
    //     return {
    //         element: _panel,
    //         spawned: _panel,
    //         get: function(){
    //             return _panel;
    //         }
    //     }
    //
    // };

    // return a single 'toggle' element
    // TODO: create toggler widget
    function radioToggle(item){

        var element = extend(true, {}, item),
            radios;

        // don't add these properties to the wrapper
        element.name = null;
        element.label = null;

        //fieldset.tag = 'div.radio-toggle';
        //element.classes = 'show-selected';

        item.name = item.name || randomID();

        radios = item.options.map(function(radio){

            var label       = {},
                button      = spawn('input', {
                    type: 'radio',
                    name: item.name,
                    value: radio.value
                }),
                description = spawn('div.description', {
                    data: {'for': item.value},
                    title: item.value,
                    html: radio.description
                });

            if (button.value === item.value) {
                button.checked = true;
            }

            if (!button.checked) {
                $(description).addClass('hidden');
                //button.disabled = true;
                //label.classes = 'hidden';
            }

            $(button).on('click', function(){
                $(this).closest('.radio-toggle').find('.description').addClass('hidden');
                $(description).removeClass('hidden');
                alert('foo');
            });

            label.append = description;

            return ['label.radio-item', label, [button, ' ' + radio.label]];

        });

        return spawn('div', element, radios);

    }


    function toggleValue(target, source, modifier){

        var $source = $$(source);
        var $target = $$(target);

        var sourceVal = $source.val();
        var targetVal = $target.val();
        var dataVal = $target.data('value');

        $target[0].value = (targetVal === sourceVal) ? dataVal : sourceVal;

        // avoid infinite loop of change triggers
        if ($target[0] !== $$(modifier)[0]) {
            $target.trigger('change.modify');
        }

    }


    function setHidden(elements, hidden){
        [].concat(elements).forEach(function(element){
            var showOrHide, modifyClass;
            if (!!hidden) {
                showOrHide = 'hide';
                modifyClass = 'addClass';
            }
            else {
                showOrHide = 'show';
                modifyClass = 'removeClass';
            }
            $$(element)[showOrHide]()[modifyClass]('hidden');
        });
    }


    // $(function(){
    //
    //     var $body = $('body');
    //
    //     // bind the XNAT.event.toggle() function to elements with 'data-' attributes
    //     $body.on('change.modify', '[data-modify]', function(){
    //
    //         var $this = $(this);
    //         var checked = $this.is(':checked');
    //
    //         // allow multiple states to be passed - separated by semicolons
    //         var states = $this.data('modify').split(';');
    //
    //         states.forEach(function(set){
    //
    //             var parts = set.split(':');
    //
    //             var state = parts[0].trim();
    //
    //             // allow multiple arguments as a comma-separated list
    //             var args = parts[1].split(',');
    //
    //             var _target = args[0].trim();
    //             var _source = (args[1] || '').trim() || _target;
    //
    //             if (args[1]) {
    //                 _source = args[1].trim();
    //             }
    //
    //             switch (state) {
    //
    //                 case 'toggle.disabled' || 'toggle.disable':
    //                     setDisabled(_target, checked);
    //                     break;
    //
    //                 case 'toggle.enabled' || 'toggle.enable':
    //                     setDisabled(_target, !checked);
    //                     break;
    //
    //                 case 'disable' || 'disabled':
    //                     setDisabled(_target, true);
    //                     break;
    //
    //                 case 'enable' || 'enabled':
    //                     setDisabled(_target, false);
    //                     break;
    //
    //                 case 'toggle.hidden' || 'toggle.hide':
    //                     setHidden(_target, checked);
    //                     break;
    //
    //                 case 'toggle.visible' || 'toggle.show':
    //                     setHidden(_target, !checked);
    //                     break;
    //
    //                 case 'hide':
    //                     setHidden(_target, true);
    //                     break;
    //
    //                 case 'show':
    //                     setHidden(_target, false);
    //                     break;
    //
    //                 case 'toggle.value':
    //                     toggleValue(_target, _source, $this[0]);
    //                     break;
    //
    //                 case 'apply.value' || 'set.value':
    //                     setValue(_target, $this);
    //                     break;
    //
    //                 case 'get.value':
    //                     setValue($this, _target);
    //                     break;
    //
    //             }
    //
    //         })
    //
    //     });
    //
    //     // trigger a change on load?
    //     //$('[data-modify]').trigger('change.modify');
    //
    // });

}));
