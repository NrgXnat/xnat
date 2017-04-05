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
            ajaxDataType = _value.split('$:')[1] || 'text';
            // console.log(ajaxDataType);
            _value = '';
            return XNAT.xhr.get({
                url: XNAT.url.restUrl(ajaxUrl),
                dataType: ajaxDataType,
                success: function(val, status, xhr){
                    // console.log('ajaxValue');
                    // console.log(val);
                    // $input.val(val);
                    // _value = xhr.responseText;
                    _value = val;
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

        if (Array.isArray(value)) {
            _value = value.join(', ');
            $input.addClass('array-list')
        }
        else {
            _value = stringable(value) ? value+'' : JSON.stringify(value);
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
            }
        }
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

    input.fileUpload = function(config){

        config = cloneObject(config);

        if (!config.url) {
            throw new Error("The 'url' property is required.")
        }

        // submission method defaults to 'POST'
        config.method = config.method || 'POST';
        config.contentType = config.contentType || 'multipart/form-data';

        // adding 'ignore' class to prevent submitting with parent form
        var fileInput = spawn('input.ignore|type=file|multiple', config.input);
        var uploadBtn = spawn('button.upload.btn.btn-sm|type=submit', config.button, 'Upload');
        var fileForm  = spawn('form.file-upload.ignore', config.form, [fileInput, uploadBtn]);

        var fileTypes = config.fileTypes ? config.fileTypes.split(/[,\s]+/) : null;
        var paramName = config.name || config.param || 'fileUpload';

        // function called when 'Upload' button is clicked
        function doUpload(){
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
            XHR.open(config.method, config.url, true);
            XHR.onload = function(){

            };
            XHR.send(formData);
        }

        $(uploadBtn).on('click', doUpload);

        return fileForm;

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

    input.switchbox = function(config){
        config = cloneObject(config);
        addClassName(config, 'switchbox');
        return spawn('label.switchbox', [
            input('checkbox', config).spawned,
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

    // // not *technically* an <input> element, but a form input nonetheless
    // input.textarea = function(config){
    //
    // };

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
