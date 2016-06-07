/*!
 * Functions for creating XNAT tab UI elements
 */

var XNAT = getObject(XNAT || {});

(function(XNAT, $, window, undefined){

    var panel,
        spawn   = window.spawn,
        element = XNAT.element,
        multiform = {
            count: 0,
            errors: 0
        };


    XNAT.ui =
        getObject(XNAT.ui || {});

    XNAT.ui.panel = panel =
        getObject(XNAT.ui.panel || {});

    // add new element class without destroying existing class
    function addClassName(el, newClass){
        el.className = [].concat(el.className||[], newClass).join(' ').trim();
        return el.className;
    }

    // add new data object item to be used for [data-] attribute(s)
    function addDataObjects(obj, attrs){
        obj.data = obj.data || {};
        forOwn(attrs, function(name, prop){
            obj.data[name] = prop;
        });
        return obj.data;
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

    /**
     * Initialize panel.
     * @param [opts] {Object} Config object
     * @returns {{}}
     */
    panel.init = function panelInit(opts){

        opts = cloneObject(opts);
        opts.element = opts.element || opts.config || {};
        opts.title = opts.title || opts.label || opts.header;

        var _target = spawn('div.panel-body', opts.element),
                
            hideHeader = (isDefined(opts.header) && (opts.header === false || /^-/.test(opts.title))),

            hideFooter = (isDefined(opts.footer) && (opts.footer === false || /^-/.test(opts.footer))),

            _panel  = spawn('div.panel.panel-default', [

                (hideHeader ? ['div.hidden'] : ['div.panel-heading', [
                    ['h3.panel-title', opts.title]
                ]]),

                // target is where the next spawned item will render
                _target,

                (hideFooter ? ['div.hidden'] : ['div.panel-footer', opts.footer])

            ]);

        // add an id to the outer panel element if present
        if (opts.id || opts.element.id) {
            _panel.id = (opts.id || opts.element.id) + '-panel';
        }

        return {
            target: _target,
            element: _panel,
            spawned: _panel,
            get: function(){
                return _panel;
            }
        }
    };

    // creates a panel that's a form that can be submitted
    panel.form = function panelForm(opts, callback){

        opts = cloneObject(opts);
        opts.element = opts.element || opts.config || {};
        opts.title = opts.title || opts.label || opts.header;
        opts.name = opts.name || opts.element.name || opts.id || opts.element.id || randomID('form-', false);

        var _target = spawn('div.panel-body', opts.element),

            hideHeader = (isDefined(opts.header) && (opts.header === false || /^-/.test(opts.title))),

            hideFooter = (isDefined(opts.footer) && (opts.footer === false || /^-/.test(opts.footer))),

            _resetBtn = spawn('button.btn.btn-sm.revert.pull-right|type=button', 'Discard Changes'),

            _footer = [
                ['button.btn.btn-sm.submit.save.pull-right|type=submit', 'Submit'],
                ['span.pull-right', '&nbsp;&nbsp;&nbsp;'],
                _resetBtn,
                //['button.btn.btn-sm.btn-link.defaults.pull-left', 'Default Settings'],
                ['div.clear']
            ],

            _formPanel = spawn('form.validate.xnat-form-panel.panel.panel-default', {
                name: opts.name,
                method: opts.method || 'POST',
                action: opts.action ? XNAT.url.rootUrl(opts.action) : '#!',
                addClass: opts.classes || ''
            }, [

                (hideHeader ? ['div.hidden'] : ['div.panel-heading', [
                    ['h3.panel-title', opts.title]
                ]]),

                // target is where this form's "contents" will be inserted
                _target,

                
                (hideFooter ? ['div.hidden'] : ['div.panel-footer', opts.footer || _footer])

            ]);

        // add an id to the outer panel element if present
        if (opts.id || opts.element.id) {
            _formPanel.id = (opts.id || opts.element.id) + '-panel';
        }

        // cache a jQuery-wrapped element
        var $formPanel = $(_formPanel);

        // set form element values from an object map
        function setValues(form, dataObj){
            // find all form inputs with a name attribute
            $$(form).find(':input').each(function(){

                var val = dataObj[this.name||this.title];

                if (!val) return;

                if (Array.isArray(val)) {
                    val = val.join(', ');
                }
                else {
                    val = stringable(val) ? val : JSON.stringify(val);
                }

                $(this).changeVal(val);

                if (/checkbox|radio/i.test(this.type)) {
                    this.checked = !!this.value;
                }

            });
            if (xmodal && xmodal.loading && xmodal.loading.closeAll){
                xmodal.loading.closeAll();
            }
        }


        // populate the data fields if this panel is in the 'active' tab
        // (only getting values for the active tab should cut down on requests)
        function loadData(form, obj){

            obj = cloneObject(obj);

            // need a form to put the data into!
            // and a 'load' property too
            if (!form || !obj.load) {
                xmodal.loading.close('#load-data');
                return;
            }

            xmodal.loading.open('#load-data');

            obj.load = (obj.load+'').trim();

            // if 'load' starts with '$?', '~/', or just '/'
            // then values need to load via REST
            var ajaxPrefix = /^(\$\?|~\/|\/)/;
            var doAjax = ajaxPrefix.test(obj.load);

            // if 'load' starts with '!?' do an eval()
            var evalPrefix = '!?';

            // if 'load' starts with ?? (or NOT evalPrefix or ajaxPrefix), do lookup
            var lookupPrefix = '??';

            if (!doAjax) {

                var doLookup = obj.load.indexOf(lookupPrefix) === 0;
                if (doLookup) {
                    obj.load = (obj.load.split(lookupPrefix)[1]||'').trim().split('|')[0];
                    obj.prop = obj.prop || obj.load.split('|')[1] || '';
                    setValues(form, lookupObjectValue(window, obj.load, obj.prop));
                    xmodal.loading.close('#load-data');
                    return form;
                }

                var doEval = obj.load.indexOf(evalPrefix) === 0;
                if (doEval) {
                    obj.load = (obj.load.split(evalPrefix)[1]||'').trim();
                }

                // lastly try to eval the 'load' value
                try {
                    setValues(form, eval(obj.load));
                }
                catch (e) {
                    console.log(e);
                }

                xmodal.loading.close('#load-data');
                return form;
                
            }

            //////////
            // REST
            //////////

            var ajaxUrl = obj.refresh || '';

            // if 'load' starts with $?, do ajax request
            //var ajaxPrefix = '$?';
            var ajaxProp = '';

            // value: $? /path/to/data | obj:prop:name
            // value: ~/path/to/data|obj.prop.name
            if (doAjax) {
                ajaxUrl = (obj.load.split(ajaxPrefix)[2]||'').trim().split('|')[0];
                ajaxProp = ajaxUrl.split('|')[1] || '';
            }

            // need a url to get the data
            if (!ajaxUrl || !stringable(ajaxUrl)) {
                xmodal.loading.close('#load-data');
                return form;
            }

            // force GET method
            obj.method = 'GET';

            // setup the ajax request
            // override values with an
            // 'ajax' or 'xhr' property
            obj.ajax = extend(true, {
                method: obj.method,
                url: XNAT.url.rootUrl(ajaxUrl)
            }, obj.ajax || obj.xhr);

            obj.ajax.success = function(data){
                if (ajaxProp){
                    data = data[ajaxProp];
                }
                $(form).dataAttr('status', 'clean');
                setValues(form, data);
            };

            obj.ajax.error = function(){
                $(form).dataAttr('status', 'error');
            };


            obj.ajax.complete = function(){
                xmodal.loading.close('#load-data');
            };

            // return the ajax thing for method chaining
            return XNAT.xhr.request(obj.ajax);

        }

        // if (opts.load) {
        //     loadData(_formPanel, opts)
        // }

        // keep an eye on the inputs
        $formPanel.find(':input').on('change', function(){
            $formPanel.dataAttr('status', 'dirty');
        });

        opts.onload = opts.onload || callback;

        // custom event for reloading data (refresh)
        $formPanel.on('reload-data', function(){
            loadData(this, {
                refresh: opts.refresh || opts.load || opts.url
            });
        });

        // click 'Discard Changes' button to reload data
        _resetBtn.onclick = function(){
            if (!/^#/.test($formPanel.attr('action'))){
                $formPanel.triggerHandler('reload-data');
            }
        };

        opts.callback = opts.callback || callback || diddly;

        // is this form part of a multiForm?
        multiform.parent = $formPanel.closest('form.multi-form');

        if (multiform.parent) {
            multiform.count = $(multiform.parent).find('form').length
        }

        multiform.errors = 0;

        // intercept the form submit to do it via REST instead
        $formPanel.on('submit', function(e){

            e.preventDefault();

            var $form = $(this).removeClass('error'),
                errors = 0,
                valid = true,
                silent = $form.hasClass('silent'),
                multiform = {};

            // don't submit forms with 'action' starting with '#'
            if (/^#/.test($form.attr('action'))) {
                return false;
            }

            $form.dataAttr('errors', 0);

            // validate inputs before moving on
            $form.find(':input.required').each(function(){
                $(this).removeClass('invalid');
                if (this.value.toString() === '') {
                    errors++;
                    valid = false;
                    $(this).addClass('invalid');
                }
            });

            $form.dataAttr('errors', errors);

            if (!valid) {
                $form.addClass('error');
                if (!silent) {
                    xmodal.message('Error','Please enter values for the required items and re-submit the form.');
                }
                return false;
            }

            // only open loading dialog for standard (non-multi) submit
            if (!multiform.count){
                var saveLoader = xmodal.loading.open('#form-save');
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
                    if (typeof json[item.name] == 'undefined') {
                        json[item.name] = item.value || '';
                    }
                    else {
                        json[item.name] = [].concat(json[item.name], item.value||[]) ;
                    }
                });
                return json;
            }

            var ajaxConfig = {
                method: opts.method,
                url: this.action,
                success: function(){
                    var obj = {};
                    // actually, NEVER use returned data...
                    // ALWAYS reload from the server
                    obj.refresh = opts.refresh || opts.reload || opts.url || opts.load;
                    if (!silent){
                        xmodal.loading.close(saveLoader.$modal);
                        XNAT.ui.banner.top(2000, 'Data saved successfully.', 'success');
                        loadData($form, obj);
                        // xmodal.message('Data saved successfully.', {
                        //     action: function(){
                        //         loadData($form, obj);
                        //     }
                        // });
                    }
                }
            };

            if (/json/i.test(opts.contentType||'')){
                ajaxConfig.data = JSON.stringify(formToJSON(this));
                ajaxConfig.processData = false;
                ajaxConfig.contentType = 'application/json';
                $.ajax(ajaxConfig);
            }
            else {
                $(this).ajaxSubmit(ajaxConfig);
            }

            return false;

        });

        // this object is returned to the XNAT.spawner() method
        return {
            load: function(){
                loadData(_formPanel, opts)
            },
            // setValues: setValues,
            target: _target,
            element: _formPanel,
            spawned: _formPanel,
            get: function(){
                return _formPanel;
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
                action: opts.action ? XNAT.url.rootUrl(opts.action) : '#!',
                onsubmit: function(e){

                    e.preventDefault();
                    var $forms = $(this).find('form');

                    var loader = xmodal.loading.open('#multi-save');

                    // reset error count on new submission
                    multiform.errors = 0;

                    // how many child forms are there?
                    multiform.count = $forms.length;

                    // submit ALL enclosed forms
                    $forms.each(function(){
                        $(this).addClass('silent').trigger('submit');
                    });

                    if (multiform.errors) {
                        xmodal.closeAll();
                        xmodal.message('Error', 'Please correct the highlighted errors and re-submit the form.');
                        return false;
                    }

                    // multiform.errors = 0;
                    // multiform.count = 0;

                    xmodal.loading.close(loader.$modal);

                    // fire the callback function after all forms are submitted error-free


                    xmodal.message({
                        title: 'Setup Complete',
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
            multiForm.id = opts.id || (opts.element.id + '-panel');
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

    // create a single generic panel element
    panel.element = function(opts){

        var _element, _inner = [], _target;
        opts = cloneObject(opts);
        opts.element = opts.element || opts.config || {};
        if (opts.id || opts.element.id) {
            opts.element.id = (opts.id || opts.element.id) + '-element';
        }
        addClassName(opts.element, 'panel-element');
        addDataObjects(opts.element, { name: opts.name||'' });
        opts.label = opts.label||opts.title||opts.name||'';

        _inner.push(['div.element-label', opts.label]);

        // 'contents' will be inserted into the 'target' element
        _target = spawn('div.element-wrapper');

        // add the target to the content array
        _inner.push(_target);

        // add a description if there is one
        if (opts.description){
            _inner.push(['div.description', opts.description||opts.body||opts.html]);
        }

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
    
    panel.subhead = function(opts){
        opts = cloneObject(opts);
        opts.html = opts.html || opts.text || opts.label;
        return XNAT.ui.template.panelSubhead(opts).spawned;
    };

    // return a generic panel 'section'
    panel.section = function(opts){

        var _section, _inner = [], _body;

        opts = cloneObject(opts);
        opts.element = opts.element || opts.config || {};
        opts.header = opts.header || opts.label || opts.title || '';

        if (opts.header) {
            _inner.push(['header.section-header', opts.header]);
        }

        // this needs to be spawned here to act as
        // the target for this elements 'contents'
        _body = spawn('div.section-body');

        _inner.push(_body);

        if (opts.footer) {
            _inner.push(['footer.section-footer'], opts.footer);
        }

        _section = spawn('div.panel-section', opts.element, _inner);

        return {
            target: _body,
            element: _section,
            spawned: _section,
            get: function(){
                return _section;
            }
        }

    };

    panel.input = {};

    panel.display = function(opts){
        return XNAT.ui.template.panelDisplay(opts).spawned;
    };

    panel.input.text = function(opts){
        return XNAT.ui.template.panelInput(opts).spawned;
    };

    panel.input.number = function panelInputNumber(opts){
        opts = cloneObject(opts);
        opts.type = 'number';
        return XNAT.ui.template.panelInput(opts).spawned;
    };

    panel.input.email = function panelInputEmail(opts){
        opts = cloneObject(opts);
        opts.type = 'text';
        addClassName(opts, 'email');
        return XNAT.ui.template.panelInput(opts).spawned;
    };

    panel.input.password = function panelInputPassword(opts){
        opts = cloneObject(opts);
        opts.type = 'password';
        addClassName(opts, 'password');
        return XNAT.ui.template.panelInput(opts).spawned;
    };

    panel.input.date = function panelInputPassword(opts){
        opts = cloneObject(opts);
        opts.type = 'date';
        addClassName(opts, 'date');
        return XNAT.ui.template.panelInput(opts).spawned;
    };

    panel.input.checkbox = function panelInputCheckbox(opts){
        opts = cloneObject(opts);
        opts.type = 'checkbox';
        return XNAT.ui.template.panelInput(opts).spawned;
    };

    panel.input.upload = function panelInputUpload(opts){
        opts = cloneObject(opts);
        opts.id = (opts.id||randomID('upload-', false));
        opts.element = opts.element || opts.config || {};
        opts.element.id = opts.id;
        var form = ['form', {
            id: opts.id + '-form',
            method: opts.method || 'POST',
            action: opts.action ? XNAT.url.rootUrl(opts.action) : '#!',
            className: addClassName(opts, 'file-upload')
        }, [
            ['input', {
                type: 'file',
                id: opts.id + '-input',
                multiple: true,
                className: addClassName(opts, 'file-upload-input')
            }],
            ['button', {
                type: 'submit',
                id: opts.id +'-button',
                html: 'Upload'
            }]
        ]];
        return XNAT.ui.template.panelInput(opts, form).spawned;
    };

    panel.input.group = function panelInputGroup(obj){
        var _inner = spawn('div.element-group');
        var _outer = XNAT.ui.template.panelElementGroup(obj, [_inner]).spawned;
        return {
            target: _inner,
            element: _outer,
            spawned: _outer,
            get: function(){
                return _outer;
            }
        }
    };

    panel.textarea = function(opts){
        opts = cloneObject(opts);
        opts.element = opts.element || opts.config || {};
        if (opts.id) opts.element.id = opts.id;
        if (opts.name) opts.element.name = opts.name;
        opts.element.html =
            opts.element.html ||
            opts.element.value ||
            opts.value ||
            opts.text ||
            opts.html || '';
    
        opts.element.html = lookupValue(opts.element.html);

        opts.element.rows = 6;
        
        var textarea = spawn('textarea', opts.element);
        return XNAT.ui.template.panelDisplay(opts, textarea).spawned;
    };
    panel.input.textarea = panel.textarea;

    //////////////////////////////////////////////////
    // SELECT MENU PANEL ELEMENTS
    //////////////////////////////////////////////////
    
    panel.select = {};

    panel.select.menu = function panelSelectSingle(opts, multi){

        var _menu;

        opts = cloneObject(opts);

        opts.name = opts.name || opts.id || randomID('select-', false);
        opts.id = opts.id || toDashed(opts.name||'');
        opts.element = extend({
            id: opts.id,
            name: opts.name,
            className: opts.className||'',
            title: opts.title||opts.name||opts.id||'',
            value: opts.value||''
        }, opts.element);
        
        if (multi) {
            opts.element.multiple = true;
        }

        _menu = spawn('select', opts.element, [['option', 'Select']]);

        if (opts.options){
            forOwn(opts.options, function(name, prop){
                _menu.appendChild(spawn('option', {
                    html: prop.html || prop.text || prop.label || prop.value || prop,
                    value: prop.value || name,
                    selected: prop.selected || (prop.value === opts.value)
                }));
            });
        }

        return XNAT.ui.template.panelInput(opts, _menu).spawned;

    };
    panel.select.init = panel.select.menu;
    panel.select.single = panel.select.menu;

    panel.select.multi = function panelSelectMulti(opts){
        return panel.select.menu(opts, true)
    };
    panel.select.multi.init = panel.select.multi;

    function footerButton(text, type, disabled, classes){
        var button = {
            type: type || 'button',
            html: text || 'Submit'
        };
        button.classes = [classes || '', 'btn btn-sm'];
        if (type === 'link') {
            button.classes.push('btn-link')
        }
        else if (/submit|primary/.test(type)) {
            button.classes.push('btn-primary')
        }
        else {
            button.classes.push('btn-default')
        }
        if (disabled) {
            button.classes.push('disabled');
            button.disabled = 'disabled'
        }
        return spawn('button', button);
    }


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



    /**
     * Panel widget with default 'Submit' and 'Revert' buttons
     * @param opts
     * @param container
     * @returns {*}
     */
    panel.form = function panelForm(opts, container){

        var _panel, $panel,
            saveBtn, revertBtn,
            $saveBtn, $revertBtn;

        opts.tag = 'form.xnat-form-panel';

        opts.title = opts.label;

        opts.body = [];

        if (opts.description) {
            opts.body.push(element.p(opts.description || ''))
        }

        if (opts.elements) {
            opts.body = opts.body.concat(setupElements(opts.elements))
        }

        saveBtn   = footerButton('Save', 'submit', true, 'save pull-right');
        revertBtn = footerButton('Discard Changes', 'button', true, 'revert pull-right');

        opts.footer = [
            saveBtn,
            spawn('span.pull-right', '&nbsp;&nbsp;&nbsp;'),
            revertBtn,
            footerButton('Default Settings', 'link', false, 'defaults pull-left'),
            spawn('div.clear')
        ];

        // CREATE THE PANEL
        _panel = panel(opts);

        $panel = $(_panel.element);
        $saveBtn = $(saveBtn);
        $revertBtn = $(revertBtn);

        // what's the submission method?
        var method = opts.method || 'GET';

        method = method.toUpperCase();

        var url = '#';

        if (method === 'GET') {
            url = XNAT.url.restUrl(opts.url)
        }
        else if (/PUT|POST|DELETE/.test(method)) {
            // add CSRF token for PUT, POST, or DELETE
            url = XNAT.url.csrfUrl(opts.url)
        }
        else {
            // any other 'method' value is ignored
        }

        // set panel as 'dirty' when stuff changes
        $panel.on('change', ':input', function(){
            $(this).addClass('dirty');
            setDisabled($panel.find('.panel-footer button'), false);
        });

        $panel.on('submit', function(e){
            e.preventDefault();
            // submit the data and disable 'Save' and 'Revert' buttons
            if (opts.url) {
                try {
                    XNAT.xhr.request({
                        method: method,
                        url: url,
                        data: $panel,
                        success: function(){
                            console.log('success!');
                            setDisabled([$saveBtn, $revertBtn], true);
                        },
                        error: function(){
                            console.log('error.')
                        }
                    })
                }
                catch (e) {
                    setDisabled([$saveBtn, $revertBtn], true);
                    console.log(e)
                }
            }
        });

        _panel.revert = function(){
            discardChanges($panel);
            setDisabled([$saveBtn, $revertBtn], true);
        };

        $revertBtn.on('click', _panel.revert);

        _panel.panel = _panel.element;

        return {
            element: _panel,
            spawned: _panel,
            get: function(){
                return _panel;
            }
        }

    };

    function footerButton(text, type, disabled, classes){
        var button = {
            type: type || 'button',
            html: text || 'Submit'
        };
        button.classes = [classes || '', 'btn btn-sm'];
        if (type === 'link') {
            button.classes.push('btn-link')
        }
        else if (/submit|primary/.test(type)) {
            button.classes.push('btn-primary')
        }
        else {
            button.classes.push('btn-default')
        }
        if (disabled) {
            button.classes.push('disabled');
            button.disabled = 'disabled'
        }
        return spawn('button', button);
    }

    // return a single 'toggle' element
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
            //button.onchange = ;

            label.append = description;

            return ['label.radio-item', label, [button, ' ' + radio.label]];

        });

        return spawn('div', element, radios);

    }

    // return a single panel element
    function panelElement(item){

        var elements = [],
            element, tag,
            children = '',
            before   = [],
            after    = [],
            kind     = item.kind || '',
            obj      = {};

        // input (or other) element
        tag = item.tag || item.kind || 'div';

        if (kind === 'element-group' && item.elements.length) {
            element = groupElements(item.elements);
            //element = spawn(tag, [radioToggle(item)]);
        }
        else {
            if (item.name) {
                obj.name = item.name
            }

            if (item.type) {
                obj.type = item.type;
            }
        }

        if (item.id) {
            obj.id = item.id;
        }

        if (tag === 'input' && !item.type) {
            obj.type = 'text';
        }

        // 'checkbox' kind
        if (kind === 'checkbox') {
            tag = 'input';
            obj.type = 'checkbox';
        }

        // set a default 'size' value for text inputs
        if (tag === 'input' && /text|email|url/.test(item.type || obj.type || '')) {
            obj.size = '25';
        }

        if (item.label) {
            obj.title = item.label;
        }

        obj.data = item.data ? extend(true, {}, item.data) : {};

        if (item.value) {
            obj.value = item.value;
            obj.data.value = item.value;
        }

        if (item.checked) {
            obj.checked = true;
            obj.data.state = 'checked';
        }

        if (item.info) {
            obj.data.info = item.info;
        }

        if (item.attr || item.attributes) {
            obj.attr = item.attr || item.attributes || {};
        }

        if (/form-table|inputTable|input-table/i.test(kind)) {
            element = XNAT.ui.inputTable(item.tableData).get();
        }
        else {
            obj.innerHTML = [].concat(item.innerHTML || item.html || []).join('\n');
        }

        if (item.before) {
            console.log('before');
            before = item.before;
            //elements.push(spawn('span.before', item.before))
        }

        if (item.after) {
            console.log('after');
            after = item.after;
            //elements.push(spawn('span.after', item.after))
        }

        if (kind !== 'hidden') {
            // enable the 'Save' and 'Discard Changes' buttons on change
            obj.onchange = function(){
                var $panel = $(this).closest('.panel');
                setDisabled([$panel.find('.save'), $panel.find('.revert')], false);
            };
        }

        if (kind === 'select' && item.options) {
            children = item.options.map(function(option){
                var obj = {};
                obj.value = option.value;
                obj.html = option.label;
                if (isDefined(item.value)) {
                    if (item.value === obj.value) {
                        obj.selected = true;
                    }
                }
                return ['option', obj]
            });
        }

        element = element || spawn(tag, obj, children);

        if (!elements.length) {
            elements = [].concat(before, element, after);
        }
        // add a description if present
        if (item.description) {
            elements.push(spawn('div.description', item.description))
        }

        // element setup
        return elements;

    }

    function elementLabel(label, id){
        var obj = {
            innerHTML: label || ''
        };
        if (id) {
            obj.attr = {
                'for': id
            }
        }
        return spawn('label.element-label', obj);
    }

    // create elements that are part of an 'element-group'
    // returns Spawn arguments array
    function groupElements(items){
        return items.map(function(item){
            var label = '';
            var tag = item.kind === 'hidden' ? 'div.hidden' : 'div.group-item';
            if (item.label) {
                label = elementLabel(item.label, item.id)
            }
            tag += '|data-name=' + item.name;
            return [tag, [].concat(label, panelElement(item))];
        });
    }

    // create elements from the 'elements' array
    // returns Spawn arguments array
    function setupElements(items){
        return items.map(function(item){
            var label = '';
            var tag = 'div.panel-element';
            switch (item.kind) {
                case 'hidden':
                    tag = 'div.hidden';
                    break;
                case 'element-group':
                    tag += '.element-group';
                    break;
            }
            if (item.label) {
                label = elementLabel(item.label, item.id)
            }
            tag += '|data-name=' + item.name;
            return [tag, [label, ['div.element-wrapper', panelElement(item)]]];
        });
    }

    function discardChanges(form){

        var $form = $$(form);

        // reset all checkboxes and radio buttons
        $form.find(':checkbox, :radio').each(function(){
            var $this = $(this);
            if ($this.hasClass('dirty')) {
                if ($this.data('state') !== 'checked') {
                    if ($this.is(':checked')) {
                        $this.trigger('click')
                    }
                    else {
                        // nevermind.
                    }
                }
                $this.removeClass('dirty');
            }
        });

        // reset text inputs based on [data-value] attribute
        $form.find(':input').not(':checkbox, :radio').val(function(){
            return $(this).data('value');
        }).trigger('change').removeClass('dirty');

        // simulate click on items that were initially checked
        //$form.find(':input[data-state="checked"]').trigger('click');

    }

    function setValue(target, source){
        $$(target).val($$(source).val());
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


    function setDisabled(elements, disabled){
        [].concat(elements).forEach(function(element){
            var _disabled = !!disabled;
            var modifyClass = _disabled ? 'addClass' : 'removeClass';
            $$(element).prop('disabled', _disabled)[modifyClass]('disabled');
        });
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


    XNAT.ui = getObject(XNAT.ui || {});
    XNAT.ui.panel = panel; // temporarily use the 'form' panel kind
    XNAT.ui.panelForm = XNAT.ui.formPanel = panel.form;


    $(function(){

        var $body = $('body');

        // bind the XNAT.event.toggle() function to elements with 'data-' attributes
        $body.on('change.modify', '[data-modify]', function(){

            var $this = $(this);
            var checked = $this.is(':checked');

            // allow multiple states to be passed - separated by semicolons
            var states = $this.data('modify').split(';');

            states.forEach(function(set){

                var parts = set.split(':');

                var state = parts[0].trim();

                // allow multiple arguments as a comma-separated list
                var args = parts[1].split(',');

                var _target = args[0].trim();
                var _source = (args[1] || '').trim() || _target;

                if (args[1]) {
                    _source = args[1].trim();
                }

                switch (state) {

                    case 'toggle.disabled' || 'toggle.disable':
                        setDisabled(_target, checked);
                        break;

                    case 'toggle.enabled' || 'toggle.enable':
                        setDisabled(_target, !checked);
                        break;

                    case 'disable' || 'disabled':
                        setDisabled(_target, true);
                        break;

                    case 'enable' || 'enabled':
                        setDisabled(_target, false);
                        break;

                    case 'toggle.hidden' || 'toggle.hide':
                        setHidden(_target, checked);
                        break;

                    case 'toggle.visible' || 'toggle.show':
                        setHidden(_target, !checked);
                        break;

                    case 'hide':
                        setHidden(_target, true);
                        break;

                    case 'show':
                        setHidden(_target, false);
                        break;

                    case 'toggle.value':
                        toggleValue(_target, _source, $this[0]);
                        break;

                    case 'apply.value' || 'set.value':
                        setValue(_target, $this);
                        break;

                    case 'get.value':
                        setValue($this, _target);
                        break;

                }

            })

        });

        // trigger a change on load?
        //$('[data-modify]').trigger('change.modify');

    });

})(XNAT, jQuery, window);
