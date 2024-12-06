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

    var undef, textTypes,
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
            val = window[(lookup || '').trim()] || '';
        }
        catch(e) {
            val = '';
            console.log(e);
        }
        el.value = val;
        return val;
    }

    function endsWith(inputStr, endStr){
        return inputStr.lastIndexOf(endStr) === inputStr.length - endStr.length;
    }

    // helper function to fire $.fn.changeVal() if available
    function changeValue(el, val){
        var $el = $$(el);
        if ($.isFunction($.fn.changeVal)) {
            $el.changeVal(val);
        }
        else {
            if ($el.val() !== val) {
                $el.val(val).trigger('change');
            }
        }
        return $el;
    }

    // // set value of a SINGLE element
    // function setValueX(input, value){
    //
    //     var $input = $$(input);
    //     var inputs = $input.toArray();
    //     var _input = $input[0];
    //     var _value = firstDefined(value, $input.val() || '');
    //
    //     _value = strReplace(_value);
    //
    //     // don't set values of inputs with EXISTING
    //     // values that start with "@?"
    //     // -- those get parsed on submission
    //     if (stringable(_value) && /^(@\?)/.test(_value+'')) {
    //         return;
    //     }
    //
    //     // lookup a value if it starts with '??'
    //     // tolerate '??=' or '??:' syntax
    //     var lookupPrefix = /^\?\?[:=\s]*/;
    //     if (_value && lookupPrefix.test(_value+'')) {
    //         _value = _value.replace(lookupPrefix, '').trim();
    //         _value = lookupObjectValue(window, _value);
    //         inputs.forEach(function(_input){
    //             // $(_input).changeVal(_value);
    //             changeValue(_input, _value)
    //         });
    //     }
    //
    //     // try to get value from XHR
    //     // $? /path/to/json/data $:json
    //     // $? /path/to/text/data $:text
    //     var ajaxPrefix = /^\$\?[:=\s]*/;
    //     var ajaxUrl = '';
    //     var ajaxDataType = '';
    //     if (ajaxPrefix.test(_value)) {
    //         ajaxUrl = _value.replace(ajaxPrefix, '').split('$:')[0].trim();
    //         ajaxDataType = (_value.split('$:')[1] || 'text').trim();
    //         // console.log(ajaxDataType);
    //         _value = '';
    //         return XNAT.xhr.get({
    //             url: XNAT.url.rootUrl(ajaxUrl),
    //             dataType: ajaxDataType,
    //             success: function(val, status, xhr){
    //                 // _value = xhr.responseText;
    //                 _value = val;
    //                 // format JSON
    //                 if (/json/i.test(ajaxDataType)) {
    //                     if (typeof val === 'string') {
    //                         val = JSON.parse(val);
    //                     }
    //                     _value = isArray(val) ? val.join(', ') : JSON.stringify(val, null, 2);
    //                 }
    //                 // $input.val('');
    //                 // console.log(_input);
    //                 inputs.forEach(function(_input){
    //                     changeValue(_input, _value)
    //                 })
    //             },
    //             error: function(){
    //                 console.error(arguments[0]);
    //                 console.error(arguments[1]);
    //                 console.error(arguments[2]);
    //             }
    //         });
    //     }
    //
    //     // get value with js eval
    //     // !? XNAT.data.context.projectId.toLowerCase();
    //     var evalPrefix = /^!\?[:=\s]*/;
    //     var evalString = '';
    //     if (evalPrefix.test(_value)) {
    //         evalString = _value.replace(evalPrefix, '').trim();
    //         _value = eval(evalString);
    //         inputs.forEach(function(_input){
    //             // $input.changeVal(_value);
    //             changeValue(_input, _value)
    //         });
    //         // setValue(_input, eval('(' + evalString + ')'));
    //     }
    //
    //     if (Array.isArray(_value)) {
    //         _value = _value.join(', ');
    //         $input.addClass('array-list')
    //     }
    //     else {
    //         _value = stringable(_value) ? _value+'' : JSON.stringify(_value);
    //     }
    //
    //     // _value = realValue((_value+'').replace(/^("|')?|("|')?$/g, '').trim());
    //
    //     if (/checkbox/i.test(_input.type)) {
    //         // allow values other than 'true' or 'false'
    //         _input.checked = (_input.value && _value && isEqual(_input.value, _value)) ? true : _value;
    //         if (_input.value === '') {
    //             _input.value = _value;
    //         }
    //         // changeValue($input, _value);
    //     }
    //     else if (/radio/i.test(_input.type)) {
    //         _input.checked = isEqual(_input.value, _value);
    //         if (_input.checked) {
    //             $input.trigger('change');
    //         }
    //     }
    //     else {
    //         // console.log('changeValue');
    //         changeValue($input, _value);
    //     }
    //
    //     // add value to [data-value] attribute
    //     // (except for textareas - that could get ugly)
    //     if (!/textarea/i.test(_input.tagName) && !/password/i.test(_input.type)){
    //         if (isArray(_value) || stringable(_value)) {
    //             $input.dataAttr('value', _value);
    //         }
    //     }
    //
    //     // console.log('_value');
    //     // console.log(_value);
    //
    //     return _value;
    //
    // }


    // set value(s) of specified input(s)
    // function setValues(inputs, values){
    //     var $inputs = $$(inputs);
    //     setValue($inputs, values);
    // }

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
                config.data.message = _validate.message || '';
            }
            delete config.validate;
            delete config.validation;
            delete config.message;
        }
        return config;
    }


    // ========================================
    // MAIN FUNCTION
    input = function inputElement(type, config){

        var _input, _label, labelText, descText, _validate, _layout;

        // only one argument?
        // could be a config object
        if (!config && typeof type !== 'string') {
            config = type;
            type = null; // it MUST contain a 'type' property
        }

        // bring 'element' properties into 'config'
        config = extend(true, {}, config || {}, config.element || {});

        // don't pass 'element' into spawn() function
        delete config.element;

        config.type = type || config.type || 'text';
        config.data = getObject(config.data || {});

        var isHidden = /hidden/i.test(config.kind);

        // addClassName(config, config.type);

        // add validation [data-*] attributes
        addValidation(config);

        _label = isHidden ? spawn('label.hidden') : spawn('label');
        // _label.style.marginBottom = '10px';

        if (!isHidden) {

            if (!/switchbox/i.test(config.kind || '')) {
                if (config.label) {
                    labelText = spawn('span.label-text', config.label);
                    delete config.label;
                }

                if (config.layout) {
                    _layout = config.layout;
                    _label.style.display = /block/i.test(_layout) ? 'block' : 'inline';
                    delete config.layout;
                }
            }

            if (config.description) {
                descText = spawn('span.description.desc-text', config.description);
                descText.style.paddingLeft = '6px';
                delete config.description;
            }

        }

        // value should at least be an empty string
        config.value = firstDefined(config.value, '') + '';

        _input = spawn('input', config);

        if (labelText) {
            if (/left/i.test(_layout)) {
                labelText.style.paddingRight = '6px';
                _label.appendChild(labelText);
                _label.appendChild(_input);
            }
            else {
                labelText.style.paddingLeft = '6px';
                _label.appendChild(_input);
                _label.appendChild(labelText);
            }
        }
        else {
            _label.appendChild(_input);
        }

        if (descText) {
            _label.appendChild(descText);
        }

        // should setting the value be a separate action???
        // ...probably...
        // if (config.value !== '' && config.value !== undef) {
        //     XNAT.form.setValue(_input, config.value);
        // }

        function setInputValue(){
            // set literal value for all-caps __VALUE__ or VALUE property
            if (config.__VALUE__ !== undef || config.VALUE !== undef) {
                _input.value = firstDefined(config.__VALUE__, config.VALUE);
                return;
            }
            if (config.value !== '' && config.value !== undef) {
                XNAT.form.setValue(_input, config.value);
            }
        }

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
        // if (config.value === undef && config.data.lookup) {
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
            spawned: _label,
            load: function(){
                setInputValue()
            },
            get: function(){
                setInputValue();
                return _label;
            },
            render: function(container){
                $$(container).append(_label);
                setInputValue();
            }
        }
    };
    input.init = function(){
        return input.apply(null, arguments);
    };
    // ========================================


    // alias XNAT.form.setValue to XNAT.input.setValue
    input.setValue = XNAT.form.setValue;


    function setupType(type, className, opts){
        var config = extend(true, {}, opts, opts.element, {
            data: {},
            $: { addClass: className }
        });
        config.data.validate = opts.validation || opts.validate;
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
            config.size = config.size || 30;
            addClassName(config, type);
            return input('text', config);
        }
    });

    numberTypes = [
        'number', 'int', 'integer', 'float'
    ];
    numberTypes.forEach(function(type){
        input[type] = function(config){
            config.size = config.size || 30;
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

    input.list = input.textList = input.arrayList = function(opts){
        opts = cloneObject(opts);
        opts.element = opts.element || {};
        opts.element.data = opts.element.data || {};
        var delim =
            opts.element.data.delim ||
            opts.element.data.delimiter ||
            opts.delim || opts.delimiter ||
            ',';
        addClassName(opts.element, 'array-list');
        addDataObjects(opts.element, { delim: delim });
        return input.text(opts);
    };

    // self-contained form for file uploads
    // with custom XHR functionality
    var fileUploadConfigModel = {
        // REQUIRED - url for data submission
        url: '/data/projects/{{project_id}}/resources/upload/[[file_input]]?format=[[data_format]]',
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

        console.log('upload');

        // if (!config.url) {
        //     throw new Error("The 'url' property is required.")
        // }

        // submission method defaults to 'POST'
        config.method = config.method || 'POST';
        config.contentType = config.contentType || config.enctype || 'multipart/form-data';

        var fileTypes = config.fileTypes ? config.fileTypes.split(/[,\s]+/) : null;

        config.input = extend(true, {
            id: config.id || config.name || '',
            name: config.name || config.id || '',
            accept: config.accept || fileTypes,
            attr: {}
        }, config.input);

        if (config.multiple) {
            config.input.attr.multiple = "multiple";
            config.input.multiple = config.multiple;
        }

        config.button = extend(true, {
            html: 'Upload'
        }, config.button);

        config.afterUploadSuccess = config.afterUploadSuccess || diddly;
        config.removeFiles = config.removeFiles || diddly;

        // adding 'ignore' class to prevent submitting with parent form
        let fileInput = spawn('input.file-upload-input.ignore|type=file', config.input);
        let uploadBtn;
        let dragdropdiv;
        let uploadProg;

        const formChildren = [];
        if (config.dragdrop) {
            const msg = config.multiple ? 'Drag file(s) here or click to browse' : 'Drag file here or click to browse';
            $(fileInput).addClass('dropzone')
            dragdropdiv = spawn('div.dropzone-wrapper', [
                spawn('div.dropzone-desc', [
                    spawn('i.fa.fa-download'),
                    spawn('p', msg)
                ]), fileInput]);
            formChildren.push(dragdropdiv);
        } else {
            formChildren.push(fileInput);
        }
        if (config.automatic) {
            uploadProg = spawn('div.upload-prog', [
                spawn('span.canceled.text-error.hidden', 'Canceled'),
                spawn('div.pull-progress-div.hidden', [
                    spawn('div.pull-progress-bar'),
                    spawn('span.remove', [spawn('i.fa.fa-times-circle')])
                ])
            ]);
            formChildren.push(uploadProg);
        } else {
            uploadBtn = spawn('button.upload.btn.btn1.btn-sm|type=button', config.button);
            formChildren.push(uploadBtn);
        }
        var fileForm = spawn('div.file-upload-form.ignore', formChildren);

        var paramName = config.name || config.input.name || config.param || 'fileUpload';

        var URL = config.url || fileForm.getAttribute('data-url') || fileForm.getAttribute('action');

        // function called when 'Upload' button is clicked
        function doUpload(e) {
            e.preventDefault();
            if (!fileInput.files || !fileInput.files.length) {
                if (!config.automatic) {
                    XNAT.dialog.message('Error', 'No files selected.');
                }
                return false;
            }
            handleFiles(fileInput.files);
        }

        function handleFiles(files) {
            let waitElement;
            let waitElementProg;
            function clearProgress() {
                waitElement.addClass('hidden');
                waitElementProg
                    .css("width", "0")
                    .text("");
            }
            if (config.automatic) {
                if (!config.multiple) {
                    $(fileForm).find('.upload-complete').remove();
                }
                waitElement = $(uploadProg).find('.pull-progress-div');
                $(uploadProg).find('span.canceled').addClass('hidden');
                waitElement.removeClass('hidden');
                waitElementProg = waitElement.find('.pull-progress-bar');
            } else {
                waitElement = XNAT.dialog.static('<div class="message waiting">Uploading...</div>').open();
            }

            var formData = new FormData();
            var XHR = new XMLHttpRequest();
            let filenames = [];
            forEach(files, function(file){
                if (fileTypes) {
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
                filenames.push(file.name);
            });
            if (!config.multiple && config.appendFilename) {
                URL += filenames[0];
            }
            if (config.automatic) {
                waitElement.find('.remove').click(function(){
                    XHR.abort();
                    clearProgress();
                    $(uploadProg).find('span.canceled').removeClass('hidden');
                    $(fileInput).val(null);
                });
            }
            XNAT.app.timeout.maintainLogin = true;
            XHR.open(config.method, XNAT.url.csrfUrl(URL), true);
            // XHR.setRequestHeader('Content-Type', config.contentType);
            XHR.onload = function(){
                XNAT.app.timeout.maintainLogin = false;
                if (XHR.status !== 200) {
                    console.error(XHR.statusText);
                    console.error(XHR.responseText);
                    if (config.automatic) {
                        clearProgress();
                    }
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
                else {
                    config.afterUploadSuccess(filenames);
                    if (config.automatic) {
                        if (config.dragdrop) {
                            let uploadList = [];
                            filenames.forEach(function(name) {
                                uploadList.push('<div class="text-success">' +
                                    '<i class="fa fa-check-circle"></i>' +
                                    '<span class="upload-complete-txt"> ' + name + ' ' +
                                    '   <span class="remove" data-filenames="' + [name] + '">' +
                                    '       <i class="fa fa-times-circle"></i>' +
                                    '   </span>' +
                                    '</span>' +
                                    '</div>');
                            });
                            $(dragdropdiv).after(spawn('div.upload-complete', uploadList));
                        } else {
                            $(fileInput).after('<span class="text-success upload-complete">' +
                                '<i class="fa fa-check-circle"></i>' +
                                '<span class="upload-complete-txt"> Uploaded' +
                                '<span class="remove" data-filenames="' + filenames + '"><i class="fa fa-times-circle"></i></span>'
                                + '</span></span>');
                        }
                        clearProgress();
                        $(fileForm).find('.upload-complete .remove').click(function() {
                            const removed = $(this).data('filenames');
                            config.removeFiles(removed);
                            $(this).parent().parent().remove();
                        });
                    } else {
                        waitElement.close().destroy();
                        XNAT.ui.banner.top(3000, 'Upload complete.', 'success');
                    }
                }
            };
            if (config.automatic) {
                XHR.upload.addEventListener('progress', function(event) {
                    var percent = 0;
                    var position = event.loaded || event.position; /*event.position is deprecated*/
                    var total = event.total;
                    if (event.lengthComputable) {
                        percent = Math.ceil(position / total * 100);
                        waitElementProg
                            .css("width", percent + "%")
                            .text(percent + "%");
                    } else {
                        waitElementProg
                            .css("width", "100%")
                            .text("Waiting...");
                    }
                }, false);
                XHR.upload.addEventListener('loadend', function(event) {
                    waitElementProg
                        .css("width", "100%")
                        .text("Saving...");
                }, false);
            }
            XHR.send(formData);
        }

        if (config.dragdrop) {
            const $div = $(dragdropdiv)
            $div.on('dragover dragenter', function(e) {
                e.preventDefault();
                e.stopPropagation();
                $(this).addClass('dragover');
            });
            $div.on('dragleave drop', function(e) {
                e.preventDefault();
                e.stopPropagation();
                $(this).removeClass('dragover');
            });
            $div.on('drop', function(e) {
                handleFiles(e.originalEvent.dataTransfer.files);
            });
        }
        if (config.automatic) {
            $(fileInput).change(doUpload);
        } else {
            $(uploadBtn).on('click', doUpload);
        }

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
        config.size = config.size || 30;
        config.autocomplete = 'off';
        addClassName(config, 'username');
        delete config.element;
        return input('text', config);
    };
    otherTypes.push('username');

    // TODO: HANDLE PASSWORD VALUES IN A SAFER WAY
    input.password = function(config){
        config = extend(true, {}, config, config.element);
        config.size = config.size || 30;
        config.value = ''; // set initial value to empty string
        config.placeholder = '********';
        config.autocomplete = firstDefined(config.autocomplete, 'new-password') + '';
        addClassName(config, 'password');
        // should be safe to delete 'element' property
        delete config.element;
        return input('password', config);
    };
    otherTypes.push('password');

    // checkboxes are special
    input.checkbox = function(config){

        config = extend(true, {}, config, config.element);
        config.kind = config.kind || 'input.checkbox';

        var chkbox = input('checkbox', config).element;

        var NAME = chkbox.name || chkbox.title || chkbox.id;
        var VALUES = (config.values || config.options || 'true|false');
        var proxyId = randomID('prx', false);

        addClassName(chkbox, 'controller');

        addDataAttrs(chkbox, {
            name: NAME,
            values: VALUES//,
            // proxy: proxyId
        });

        // var proxy = spawn('input.hidden.proxy', {
        //     type: 'hidden',
        //     name: NAME,
        //     id: proxyId,
        //     value: config.value || ''
        // });

        // skip the proxy
        var proxy = '';

        var spawned = spawn('label', [chkbox, proxy]);

        return {
            spawned: spawned,
            element: spawned,
            get: function(){
                return spawned
            },
            render: function(container){
                $$(container).append(spawned);
                return spawned;
            },
            checkbox: chkbox,
            proxy: proxy
        }

    };
    otherTypes.push('checkbox');

    // allow use of an existing checkbox as a second argument
    input.switchbox = function(config){

        config = cloneObject(config);
        config.kind = 'input.switchbox';

        // use XNAT.ui.input.checkbox() for consistency (?)
        var CKBX = input.checkbox(config);

        var chkbox = CKBX.checkbox;

        var NAME = chkbox.name || chkbox.title || chkbox.id;
        var VALUES = config.values || config.options || 'true|false';

        chkbox.title = chkbox.title || (NAME || '') + ': ' + VALUES;
        // chkbox.name = '';

        addClassName(chkbox, 'switchbox');

        if (config.onValue) {
            addDataAttrs(chkbox, {
                checkedval: config.onValue
            });
        }

        var proxy = CKBX.proxy;

        var swboxParts = [
            chkbox,
            // proxy,
            ['span.switchbox-outer', [['span.switchbox-inner']]],
            ['span.switchbox-on', config.onText || ''],
            ['span.switchbox-off', config.offText || ''],
            ''
        ];

        var SWBX = spawn('label.switchbox', {
            title: NAME ? NAME + ': ' + VALUES : ''
        }, swboxParts);

        return {
            spawned: SWBX,
            element: SWBX,
            get: function(){
                return SWBX
            },
            render: function(container){
                $$(container).append(SWBX);
                return SWBX;
            },
            checkbox: chkbox,
            proxy: proxy
        }

    };
    otherTypes.push('switchbox');

    // radio buttons are special too
    input.radio = function(config){
        var _config = extend(true, {}, config, config.element);
        return input('radio', _config);
    };
    otherTypes.push('radio');

    // radio button group with easily configurable options
    input.radioGroup = function(config){

        if (jsdebug) console.log('input.radioGroup');

        config = extend(true, {}, config, config.element);

        var selectedValue = config.value || '';
        var radioGroupName = config.name;

        var radioGroupOptions = spawn('div.radio-group.table-display', {
            classes: [].concat(config.className || [], config.classes || []).join(' ')
        });

        var radios = [];

        forOwn(config.items || config.options, function(name, item){
            var id = item.id || randomID('xrg', false);
            radioGroupName = radioGroupName || item.name || name;
            var radio = spawn('input.radio-control', {
                type: 'radio',
                id: id,
                name: radioGroupName,
                checked: config.value === item.value,
                value: item.value
            });
            radios.push(radio);
            var label = item.label ? spawn('b.label', item.label || '') : '';
            var radioCell = spawn('div.radio.nowrap.table-cell', [radio, label]);
            var descCell = spawn('div.description.table-cell', [['p', item.description]]);
            var optionRow = spawn('label.option.table-row', {
                classes: [].concat(item.className || [], item.classes || []).join(' ')
            }, [radioCell, descCell]);
            radioGroupOptions.appendChild(optionRow);
        });

        var radioGroupContainer = spawn('div.radio-group-container', {
            on: [
                ['click', '.radio-control', function(e){
                    console.log(this.value);
                }]
            ]
        }, [radioGroupOptions]);

        var tmp = {};
        tmp[radioGroupName] = selectedValue;

        if (jsdebug) console.log('/////  SET RADIO GROUP VALUE  /////');

        // XNAT.form.setValues(radios, tmp);

        return {
            element: radioGroupContainer,
            spawned: radioGroupContainer,
            load: function(){
                XNAT.form.setValues(radios, tmp);
            },
            get: function(){
                return radioGroupContainer
            },
            render: function(container){
                $$(container).append(radioGroupContainer);
                XNAT.form.setValues(radios, tmp);
            }
        }
    };

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

        opts.element.title = opts.element.title || opts.title || opts.label;

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
            // open code editor on double-click
            // (actually don't - it's annoying)
            // opts.element.title = 'Double-click to open in code editor.';
            // opts.element.ondblclick = function(){
            //     var panelTextarea = XNAT.app.codeEditor.init(this, { language: opts.code || 'html' });
            //     panelTextarea.openEditor();
            // };
        }

        opts.element.rows = opts.rows || opts.element.rows || 10;

        opts.element.validate = '' ||
            opts.element.validate || opts.validate ||
            opts.element.validation || opts.validation;

        addValidation(opts.element);

        var textarea = spawn('textarea', opts.element);

        XNAT.form.setValue(textarea, _val);

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

    // add 'array-list' class to textarea.list elements
    input.textarea.list = function(opts){
        opts = cloneObject(opts);
        opts.element = opts.element || {};
        opts.element.data = opts.element.data || {};
        var delim =
            opts.element.data.delim ||
            opts.element.data.delimiter ||
            opts.delim || opts.delimiter ||
            ',';
        addClassName(opts.element, 'array-list');
        addDataObjects(opts.element, { delim: delim });
        return input.textarea(opts);
    };

    // alias to XNAT.ui.textarea
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
