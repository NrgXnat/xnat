/*
 * web: input.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Spawn form input elements
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

    var undefined, textTypes,
        numberTypes, otherTypes,
        $ = jQuery || null, // check and localize
        input_, input;

    XNAT.ui = getObject(XNAT.ui || {});

    // if XNAT.ui.input is already defined,
    // save it and its properties to add later
    // as methods and properties to the function
    input_ = XNAT.ui.input || {};

    function lookupValue(el, lookup){
        if (!lookup) {
            lookup = el;
            el = {};
        }
        var val = '';
        try {
            val = eval((lookup||'').trim()) || ''
        }
        catch (e) {
            val = '';
            console.log(e);
        }
        el.value = val;
        return val;
    }

    function endsWith(inputStr, endStr) {
        return inputStr.lastIndexOf(endStr) === inputStr.length - endStr.length;
    }

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

    // set value of a SINGLE element
    function setValue(input, value){

        var $input = $$(input);
        var _input = $input[0];
        var _value = firstDefined(value, _input.value || '');

        // don't set values of inputs with EXISTING
        // values that start with "@?"
        // -- those get parsed on submission
        if (stringable(_value) && /^(@\?)/.test(_value+'')) {
            return;
        }

        // lookup a value if it starts with '??'
        // tolerate '??=' or '??:' syntax
        var lookupPrefix = /^\?\?[:=\s]*/;
        if (_value && lookupPrefix.test(_value+'')) {
            _value = _value.replace(lookupPrefix, '').trim();
            _value = lookupObjectValue(window, _value);
            $input.val(_value);
        }

        // try to get value from XHR
        // $? /path/to/json/data $:json
        // $? /path/to/text/data $:text
        var ajaxPrefix = /^\$\?[:=\s]*/;
        var ajaxUrl = '';
        var ajaxDataType = '';
        if (ajaxPrefix.test(_value)) {
            ajaxUrl = _value.replace(ajaxPrefix, '').split('$:')[0].trim();
            ajaxDataType = (_value.split('$:')[1] || 'text').trim();
            // console.log(ajaxDataType);
            _value = '';
            return XNAT.xhr.get({
                url: XNAT.url.restUrl(ajaxUrl),
                dataType: ajaxDataType,
                success: function(val, status, xhr){
                    // _value = xhr.responseText;
                    _value = val;
                    // format JSON
                    if (/json/i.test(ajaxDataType)) {
                        if (typeof val === 'string') {
                            val = JSON.parse(val);
                        }
                        _value = JSON.stringify(val, null, 2);
                    }
                    _input.value = '';
                    // console.log(_input);
                    setValue(_input, _value)
                },
                error: function(){
                    console.error(arguments[0]);
                    console.error(arguments[1]);
                    console.error(arguments[2]);
                }
            });
        }

        // get value with js eval
        // !? XNAT.data.context.projectId.toLowerCase();
        var evalPrefix = /^!\?[:=\s]*/;
        var evalString = '';
        if (evalPrefix.test(_value)) {
            evalString = _value.replace(evalPrefix, '').trim();
            _value = eval('(' + evalString + ')');
            $input.val(_value);
            // setValue(_input, eval('(' + evalString + ')'));
        }

        if (Array.isArray(_value)) {
            _value = _value.join(', ');
            $input.addClass('array-list')
        }
        else {
            _value = stringable(_value) ? _value+'' : JSON.stringify(_value);
        }

        // _value = realValue((_value+'').replace(/^("|')?|("|')?$/g, '').trim());

        if (/checkbox/i.test(_input.type)) {
            // allow values other than 'true' or 'false'
            _input.checked = (_input.value && _value && isEqual(_input.value, _value)) ? true : _value;
            if (_input.value === '') {
                _input.value = _value;
            }
            //changeValue($this, val);
        }
        else if (/radio/i.test(_input.type)) {
            _input.checked = isEqual(_input.value, _value);
            if (_input.checked) {
                $input.trigger('change');
            }
        }
        else {
            // console.log('changeValue');
            changeValue($input, _value);
        }

        // add value to [data-value] attribute
        // (except for textareas - that could get ugly)
        if (!/textarea/i.test(_input.tagName) && !/password/i.test(_input.type)){
            if (isArray(_value) || stringable(_value)) {
                $input.dataAttr('value', _value);
            }
        }

        // console.log('_value');
        // console.log(_value);

        return _value;

    }

    // set value(s) of specified input(s)
    function setValues(inputs, values){

    }


    // ========================================
    // MAIN FUNCTION
    input = function inputElement(type, config){

        // only one argument?
        // could be a config object
        if (!config && typeof type != 'string') {
            config = type;
            type = null; // it MUST contain a 'type' property
        }

        // bring 'element' properties into 'config'
        config = extend(true, {}, config||{}, config.element||{});

        // don't pass 'element' into spawn() function
        delete config.element;

        config.type = type || config.type || 'text';
        config.data = getObject(config.data || {});

        // addClassName(config, config.type);

        // add validation [data-*] attributes
        if (config.validate || config.validation) {
            config.data.validate = config.validate || config.validation;
            delete config.validation; // don't pass these to the spawn() function
            delete config.validate;   // ^^
        }

        // add validation error message
        if (config.message) {
            config.data.message = config.message;
            delete config.message;
        }

        // value should at least be an empty string
        config.value = config.value || '';

        var _input = spawn('input', config);

        setValue(_input, config.value);

        // // copy value to [data-*] attribute for non-password inputs
        // config.data.value = (!/password/i.test(config.type)) ? config.value : '!';
        //
        // // lookup a value if it starts with '??'
        // // tolerate '??=' or '??:' syntax
        // var lookupPrefix = /^\?\?[:=\s]*/;
        // if (config.value && lookupPrefix.test(config.value+'')) {
        //     config.value = config.value.replace(lookupPrefix, '').trim();
        //     config.value = lookupObjectValue(window, config.value)
        // }
        //
        // // lookup a value from a namespaced object
        // // if no value is given
        // if (config.value === undefined && config.data.lookup) {
        //     config.value = lookupObjectValue(window, config.data.lookup)
        // }
        //
        //
        // // try to get value from XHR
        // var ajaxPrefix = /^\$\?[:=\s]*/;
        // var ajaxUrl = '';
        // if (ajaxPrefix.test(config.value)) {
        //     ajaxUrl = config.value.replace(ajaxPrefix, '').trim();
        //     XNAT.xhr.get({
        //         url: XNAT.url.restUrl(ajaxUrl),
        //         success: function(val){
        //             _input.value = '';
        //             setValue(_input, val)
        //         }
        //     });
        // }
        //
        // var evalPrefix = /^!\?[:=\s]*/;
        // var evalString = '';
        // if (evalPrefix.test(config.value)) {
        //     evalString = config.value.replace(evalPrefix, '').trim();
        //     setValue(_input, eval('(' + evalString + ')'));
        // }

        return {
            element: _input,
            spawned: _input,
            get: function(){
                return _input;
            },
            render: function(container){
                $$(container).append(_input);
            }
        }
    };
    input.init = function(){
        return input.apply(null, arguments);
    };
    // ========================================


    // expose the 'setValue' function as a method of XNAT.input
    input.setValue = setValue;


    function setupType(type, className, opts){
        var config = extend(true, {}, opts, opts.element, {
            data: {},
            $: { addClass: className }
        });
        config.data.validate = opts.validation||opts.validate;
        if (!config.data.validate) delete config.data.validate;
        delete config.validation; // don't pass these to the spawn() function
        delete config.validate;   // ^^
        return input(type, config);
    }

    // methods for direct creation of specific input types
    // some are 'real' element types, others are XNAT-specific
    textTypes = [
        'text', 'email', 'url', 'strict',
        'id', 'alpha', 'alphanum'
    ];
    textTypes.forEach(function(type){
        input[type] = function(config){
            config.size = config.size || 25;
            addClassName(config, type);
            return input('text', config);
        }
    });

    numberTypes = [
        'number', 'int', 'integer', 'float'
    ];
    numberTypes.forEach(function(type){
        input[type] = function(config){
            config.size = config.size || 25;
            addClassName(config, type);
            return input('number', config);
        }
    });

    otherTypes = [
        'date', 'file', 'button', 'hidden'
    ];
    otherTypes.forEach(function(type){
        input[type] = function(config){
            return input(type, config);
        }
    });

    // self-contained form for file uploads
    // with custom XHR functionality
    var fileUploadConfigModel = {
        // REQUIRED - url for data submission
        url: '/data/projects/{{project_id}}/resources/upload/{{file_input}}?format={{data_format}}',
        // submission method - defaults to 'POST'
        method: 'POST', // (POST or PUT)
        // data contentType (this probably shouldn't be changed)
        contentType: 'multipart/form-data',
        // parameter name expected on the back-end
        name: 'fileUpload',
        // space- or comma-separated list
        // of acceptable file extensions
        fileTypes: 'zip gz json xml',
        form: {
            // properties for <form> element
        },
        input: {
            // properties for <input type="file"> element
        },
        button: {
            // properties for <button> element
        }
    };

    input.fileUpload = input.upload = function(config){

        config = cloneObject(config);

        if (!config.url) {
            throw new Error("The 'url' property is required.")
        }

        // submission method defaults to 'POST'
        config.method = config.method || 'POST';
        config.contentType = config.contentType || config.enctype || 'multipart/form-data';

        config.form = extend(true, {
            method: config.method,
            action: config.action ? XNAT.url.rootUrl(config.action) : '#!',
            attr: {
                'content-type': config.contentType,
                'enctype': config.enctype || config.contentType
            }
        }, config.form);

        var fileTypes = config.fileTypes ? config.fileTypes.split(/[,\s]+/) : null;

        config.input = extend(true, {
            id: config.id || config.name || '',
            name: config.name || config.id || '',
            accept: config.accept || fileTypes
        }, config.input);

        config.button = extend(true, {
            html: 'Upload'
        }, config.button);

        // adding 'ignore' class to prevent submitting with parent form
        var fileInput = spawn('input.ignore|type=file|multiple', config.input);
        var uploadBtn = spawn('button.upload.btn.btn-sm|type=button', config.button);
        var fileForm  = spawn('form.file-upload.ignore', config.form, [fileInput, uploadBtn]);

        var paramName = config.name || config.param || 'fileUpload';

        var URL = XNAT.url.rootUrl(config.url || config.action);

        // function called when 'Upload' button is clicked
        function doUpload(e){
            e.preventDefault();
            var formData = new FormData();
            var XHR = new XMLHttpRequest();
            forEach(fileInput.files, function(file){
                if (fileTypes){
                    // check each extension and only add
                    // matching files to the list
                    forEach(fileTypes, function(type){
                        if (endsWith(file.name, type)) {
                            formData.append(paramName, file);
                        }
                    });
                }
                else {
                    formData.append(paramName, file);
                }
            });
            XHR.open(config.method, URL, true);
            XHR.setRequestHeader('Content-Type', config.contentType);
            XHR.onload = function(){
                if (XHR.status !== 200) {
                    console.error(XHR.statusText);
                    console.error(XHR.responseText);
                    XNAT.ui.dialog.message({
                        title: 'Upload Error',
                        content: '' +
                            'There was a problem uploading the selected file.' +
                            '<br>' +
                            'Server responded with: ' +
                            '<br>' +
                            '<b>' + XHR.statusText + '</b>'

                    });
                }
            };
            XHR.send(formData);
        }

        $(uploadBtn).on('click', doUpload);

        return {
            element: fileForm,
            spawned: fileForm,
            get: function(){
                return fileForm
            },
            render: function(container){
                $$(container).append(fileForm);
                return fileForm;
            }
        };

        // TODO: FINISH THIS


        // var uploaded = false;
        // for (var i = 0; i < files.length; i++) {
        //     var file = files[i];
        //     if (!file.type.match('zip.*')) {
        //         continue;
        //     }
        //     formData.append('themePackage', file, file.name); // formData.append('themes[]', file, file.name);
        //     var xhr = new XMLHttpRequest();
        //     xhr.open('POST', themeUploadForm.action, true);
        //     xhr.onload = function(){
        //         if (xhr.status !== 200) {
        //             console.log(xhr.statusText);
        //             console.log(xhr.responseText);
        //             xmodal.message('Upload Error', 'There was a problem uploading your theme package.<br>Server responded with: ' + xhr.statusText);
        //         }
        //         $(themeUploadSubmit).text('Upload');
        //         $(themeUploadSubmit).removeAttr('disabled');
        //         var newThemeOptions = $.parseJSON(xhr.responseText);
        //         var selected;
        //         if (newThemeOptions[0]) {
        //             selected = newThemeOptions[0].value;
        //         }
        //         addThemeOptions(newThemeOptions, selected);
        //     };
        //     xhr.send(formData);
        //     uploaded = true;
        // }

    };

    input.username = function(config){
        config = extend(true, {}, config, config.element);
        config.size = config.size || 25;
        config.autocomplete = 'off';
        addClassName(config, 'username');
        delete config.element;
        return input('text', config);
    };
    otherTypes.push('username');

    // TODO: HANDLE PASSWORD VALUES IN A SAFER WAY
    input.password = function(config){
        config = extend(true, {}, config, config.element);
        config.size = config.size || 25;
        config.value = ''; // set initial value to empty string
        config.placeholder = '********';
        config.autocomplete = 'new-password';
        addClassName(config, 'password');
        // should be safe to delete 'element' property
        delete config.element;
        return input('password', config);
    };
    otherTypes.push('password');

    // checkboxes are special
    input.checkbox = function(config){
        config = extend(true, {}, config, config.element);
        // config.onchange = function(){
        //     this.value = this.checked || $(this).data('uncheckedValue') || '';
        // };
        return input('checkbox', config);
    };
    otherTypes.push('checkbox');

    // allow use of an existing checkbox as a second argument
    input.switchbox = function(config, ckbx){
        config = cloneObject(config);
        addClassName(config, 'switchbox');
        return spawn('label.switchbox', [
            ckbx || input('checkbox', config).spawned,
            ['span.switchbox-outer', [['span.switchbox-inner']]],
            ['span.switchbox-on', config.onText||''],
            ['span.switchbox-off', config.offText||'']
        ])
    };
    otherTypes.push('switchbox');

    // radio buttons are special too
    input.radio = function(config){
        config = extend(true, {}, config, config.element);
        return input('radio', config);
    };
    otherTypes.push('radio');

    // save a list of all available input types
    input.types = [].concat(textTypes, numberTypes, otherTypes);

    // create display: block versions of ALL input types
    input.types.forEach(function(type, i){
        input[type]['block'] = function(config){
            config = extend(true, {}, config, config.element, {
                style: { display: 'block' }
            });
            addClassName(config, 'display-block');
            return input[type](config);
        }
    });

    // not *technically* an <input> element, but a form input nonetheless
    input.textarea = function(opts){

        opts = cloneObject(opts);
        opts.element = opts.element || opts.config || {};

        if (opts.id) opts.element.id = opts.id;
        if (opts.name) opts.element.name = opts.name;

        var val1 = opts.element.value;
        var val2 = opts.value;
        var _val = firstDefined(val1, val2, '');

        // opts.element.value = firstDefined(val1, val2, '');

        // opts.element.html = firstDefined(
        //     opts.element.html+'',
        //     opts.element.value+'',
        //     opts.text+'',
        //     opts.html+'',
        //     '');

        if (opts.code || opts.codeLanguage) {
            opts.code = opts.code || opts.codeLanguage;
            addDataObjects(opts.element, {
                codeEditor: opts.code,
                codeLanguage: opts.codeLanguage || opts.code
            });
            addClassName(opts.element, 'code mono');
            opts.element.title = 'Double-click to open in code editor.';
            // open code editor on double-click
            // opts.element.ondblclick = function(){
            //     var panelTextarea = XNAT.app.codeEditor.init(this, { language: opts.code || 'html' });
            //     panelTextarea.openEditor();
            // };
        }

        opts.element.rows = opts.rows || opts.element.rows || 10;

        var textarea = spawn('textarea', opts.element);

        setValue(textarea, _val);

        return {
            element: textarea,
            spawned: textarea,
            get: function(){
                return textarea;
            },
            render: function(container){
                $$(container).append(textarea);
            }
        };

    };
    XNAT.ui.textarea = input.textarea;

    // after the page is finished loading, set empty
    // input values from [data-lookup] attribute
    $(window).on('load', function(){
        $(':input[data-lookup]').each(function(){
            var $input = $(this);
            var val = lookupValue($input.dataAttr('lookup'));
            $input.changeVal(val);
        });
    });

    // add back items that may have been on
    // a global XNAT.ui.input object or function
    extend(input, input_);

    // this script has loaded
    input.loaded = true;

    return XNAT.ui.input = input;

}));
