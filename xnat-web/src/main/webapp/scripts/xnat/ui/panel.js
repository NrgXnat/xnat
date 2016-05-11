/*!
 * Functions for creating XNAT tab UI elements
 */

var XNAT = getObject(XNAT || {});

(function(XNAT, $, window, undefined){

    var panel,
        spawn   = window.spawn,
        element = XNAT.element;


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

    panel.init = function panelInit(opts){

        opts = getObject(opts);
        opts.element = opts.element || opts.config || {};

        var _target = spawn('div.panel-body', opts.element),

            hideFooter = (isDefined(opts.footer) && (opts.footer === false || /^-/.test(opts.footer))),

            _panel  = spawn('div.panel.panel-default', [
                ['div.panel-heading', [
                    ['h3.panel-title', opts.title || opts.label]
                ]],

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
    panel.form = function panelForm(opts){

        opts = getObject(opts);
        opts.element = opts.element || opts.config || {};

        var _target = spawn('div.panel-body', opts.element),

            hideFooter = (isDefined(opts.footer) && (opts.footer === false || /^-/.test(opts.footer))),

            _resetBtn = spawn('button.btn.btn-sm.btn-default.revert.pull-right|type=button', 'Discard Changes'),

            _footer = [
                ['button.btn.btn-sm.btn-primary.save.pull-right|type=submit', 'Submit'],
                ['span.pull-right', '&nbsp;&nbsp;&nbsp;'],
                _resetBtn,
                ['button.btn.btn-sm.btn-link.defaults.pull-left', 'Default Settings'],
                ['div.clear']
            ],

            _formPanel = spawn('form.xnat-form-panel.panel.panel-default', {
                method: opts.method || 'POST',
                action: XNAT.url.rootUrl(opts.action || '') || '#'
            }, [
                ['div.panel-heading', [
                    ['h3.panel-title', opts.title || opts.label]
                ]],

                // target is where the next spawned item will render
                _target,

                (hideFooter ? ['div.hidden'] : ['div.panel-footer', opts.footer || _footer])

            ]);

        // add an id to the outer panel element if present
        if (opts.id || opts.element.id) {
            _formPanel.id = (opts.id || opts.element.id) + '-panel';
        }

        // set form element values from an object map
        function setValues(form, dataObj){
            // pass a single argument to work with this form
            if (!dataObj) {
                dataObj = form;
                form = _formPanel;
            }
            // find all form inputs with a name attribute
            $$(form).find(':input[name]').each(function(){
                var val = '';
                if (Array.isArray(dataObj)) {
                    val = dataObj.join(', ');
                }
                else {
                    val = /string|number/i.test(typeof dataObj) ? dataObj : dataObj[this.name] || '';
                }
                $(this).changeVal(val);
            });
            if (xmodal && xmodal.loading && xmodal.loading.close){
                xmodal.loading.close();
            }
        }

        // populate the data fields if this panel is in the 'active' tab
        // (only getting values for the active tab should cut down on requests)
        function loadData(obj){

            if (!obj) {
                obj = opts.load || {};
            }

            obj = extend(true, {}, obj);

            obj.form = obj.form || obj.target || obj.element || _formPanel;

            // need a form to put the data into
            if (!obj.form) return;

            // // if there's a 'refresh' url, make that obj.url
            // if (obj.refresh) obj.url = obj.refresh;

            // if we pass data in a 'lookup' property, just use that
            // to avoid doing a server request

            if (obj.lookup && !obj.url) {
                if (Array.isArray(obj.lookup)) {
                    obj.lookup = obj.lookup[0];
                }
                else {
                    try {
                        obj.lookup = eval(obj.lookup);
                    }
                    catch (e) {
                        if (console && console.log) console.log(e);
                        obj.lookup = ''
                    }
                }
                setValues(obj.form, obj.lookup);
                return obj.form;
            }

            // otherwise try to get the data values via ajax

            // need a url to get the data
            if (!obj.url) return obj.form;

            obj.method = obj.method || 'GET';

            // setup the ajax request
            // override values with an
            // 'ajax' or 'xhr' property
            obj.ajax = extend(true, {
                method: obj.method,
                url: XNAT.url.restUrl(obj.url)
            }, obj.ajax || obj.xhr);

            // allow use of 'prop' or 'root' for the root property name
            obj.prop = obj.prop || obj.root;

            obj.ajax.success = function(data){
                var prop = data;
                // if there's a property to target,
                // specify the 'prop' property
                if (obj.prop){
                    obj.prop.split('.').forEach(function(part){
                        prop = prop[part];
                    });
                }
                setValues(prop);
            };

            // return the ajax thing for method chaining
            return XNAT.xhr.request(obj.ajax);

        }

        //if (opts.load){
        //    loadData(opts.load);
        //}

        $(_formPanel).on('reload-data', function(){
            xmodal.loading.open();
            opts.load.url = opts.load.url || opts.load.refresh;
            loadData(opts.load);
        });

        // click 'Discard Changes' button to reload data
        _resetBtn.onclick = function(){
            $(_formPanel).triggerHandler('reload-data');
        };

        // intercept the form submit to do it via REST instead
        $(_formPanel).on('submit', function(e){

            e.preventDefault();

            xmodal.loading.open();

            var ajaxSubmitOpts = {

                target:        '#server-response',  // target element(s) to be updated with server response
                beforeSubmit:  function(){},  // pre-submit callback
                success:       function(){},  // post-submit callback

                // other available options:
                url:       '/url/for/submit', // override for form's 'action' attribute
                type:      'get or post (or put?)', // 'get' or 'post', override for form's 'method' attribute
                dataType:  null,        // 'xml', 'script', or 'json' (expected server response type)
                clearForm: true,        // clear all form fields after successful submit
                resetForm: true,        // reset the form after successful submit

                // $.ajax options can be used here too, for example:
                timeout:   3000

            };

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
                success: function(data){
                    var obj = {};
                    // if a data object is returned,
                    // just use that
                    if (data) {
                        // wrap the returned data in an array so the
                        // loadData() function handles it properly
                        obj.lookup = [data];
                    }
                    else {
                        obj.url = opts.refresh;
                    }
                    xmodal.loading.close();
                    xmodal.message('Data saved successfully.', {
                        action: function(){
                            xmodal.closeAll();
                            loadData(obj);
                        }
                    });
                }
            };

            // if (!/form/i.test(opts.contentType||'')) {
            //     ajaxConfig.contentType = opts.contentType;
            // }

            if (/json/i.test(opts.contentType||'')){
                ajaxConfig.data = JSON.stringify(formToJSON(this));
                ajaxConfig.processData = false;
                ajaxConfig.contentType = 'application/json';
                $.ajax(ajaxConfig);
            }
            else {
                // ajaxConfig.data =  $(this).serialize();
                // $.ajax(ajaxConfig);
                $(this).ajaxSubmit(ajaxConfig);
            }

            // $.ajax(ajaxConfig);

            return false;

        });

        // this object is returned to the XNAT.spawner() method
        return {
            load: loadData,
            setValues: setValues,
            target: _target,
            element: _formPanel,
            spawned: _formPanel,
            get: function(){
                return _formPanel;
            }
        }
    };
    
    // creates a panel that submits all forms contained within
    panel.multiForm = function(opts){

        opts = getObject(opts);
        opts.element = opts.element || opts.config || {};

        var inner = spawn('div.panel-body', opts.element),

            hideFooter = (isDefined(opts.footer) && (opts.footer === false || /^-/.test(opts.footer))),

            submitBtn = spawn('button', {
                type: 'submit',
                classes: 'btn btn-sm btn-primary save pull-right',
                html: 'Save All'
            }),
            
            resetBtn  = spawn('button', {
                type: 'button',
                classes: 'btn btn-sm btn-default revert pull-right',
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
                classes: 'btn btn-sm btn-link defaults pull-left',
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
                action: opts.action || '#',
                onsubmit: function(e){
                    e.preventDefault();
                    // submit all enclosed forms
                    $(this).find('form').each(function(){
                        xmodal.closeAll();
                        $(this).trigger('submit');
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
        opts = getObject(opts);
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

    panel.subhead = function(opts){
        opts = getObject(opts);
        opts.html = opts.html || opts.text || opts.label;
        return XNAT.ui.template.panelSubhead(opts).spawned;
    };

    // return a generic panel 'section'
    panel.section = function(opts){

        var _section, _inner = [], _body;

        opts = getObject(opts);
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
        opts = getObject(opts);
        opts.type = 'number';
        return XNAT.ui.template.panelInput(opts).spawned;
    };

    panel.input.email = function panelInputEmail(opts){
        opts = getObject(opts);
        opts.type = 'text';
        addClassName(opts, 'email');
        return XNAT.ui.template.panelInput(opts).spawned;
    };

    panel.input.password = function panelInputPassword(opts){
        opts = getObject(opts);
        opts.type = 'password';
        addClassName(opts, 'password');
        return XNAT.ui.template.panelInput(opts).spawned;
    };

    panel.input.checkbox = function panelInputCheckbox(opts){
        opts = getObject(opts);
        opts.type = 'checkbox';
        //addClassName(opts, 'checkbox');
        return XNAT.ui.template.panelInput(opts).spawned;
    };

    panel.input.upload = function panelInputUpload(opts){
        opts = getObject(opts);
        opts.id = (opts.id||randomID('upload-', false));
        opts.element = opts.element || opts.config || {};
        opts.element.id = opts.id;
        var form = ['form', {
            id: opts.id + '-form',
            method: opts.method || 'POST',
            action: opts.action || '#',
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

    panel.select = {};

    panel.select.menu = function panelSelectSingle(opts, multi){

        var _menu;

        opts = getObject(opts);
        opts.element = opts.element || opts.config || {};
        opts.element.name = opts.element.name || opts.name || '';
        opts.element.id = opts.element.id || opts.id || toDashed(opts.element.name);
        if (multi) {
            opts.element.multiple = true;
        }
        _menu = spawn('select', opts.element, [['option|value=!', 'Select']]);

        if (opts.options){
            forOwn(opts.options, function(name, prop){
                _menu.appendChild(spawn('option', {
                    value: prop.value,
                    selected: prop.selected || (prop.value === opts.value)
                }, prop.label));
            });
        }
        return XNAT.ui.template.panelInput({
            label: opts.label,
            name: opts.name
        }, _menu).spawned;
    };
    panel.select.init = panel.select.menu;
    panel.select.single = panel.select.menu;

    panel.select.multi = function panelSelectMulti(opts){
        return panel.select.menu(opts, true)
    };

    panel.selectMenu = function panelSelectMenu(opts){
        opts = getObject(opts);
        return XNAT.ui.template.panelSelect(opts).spawned;
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
     * Initialize panel and optionally render it.
     * If 'opts' argument is passed, 'setup' method will run.
     * If 'container' argument is passed, the 'render' method will run.
     * So if both args are passed, 'setup' and 'render' do NOT need to be called.
     * @param [opts] {Object} Config object
     * @param [container] {Element} Container for panel
     * @returns {{}}
     */
    function panel(opts, container){

        // `this` object
        var _panel = {};

        /**
         * Standard panel widget
         */
        function newPanel(){

            var sections = [
                ['div.panel-heading', [
                    ['h3.panel-title', _panel.opts.title]
                ]],
                ['div.panel-body', _panel.opts.body]
            ];

            if (_panel.opts.footer) {
                sections.push(['div.panel-footer', _panel.opts.footer])
            }

            return spawn((_panel.opts.tag) + '.panel.panel-default', _panel.opts.attr, sections);
            //return $(spawn('div.panel.panel-default')).append(content);
        }

        /**
         * Sets up elements before rendering to the page
         * @param _opts Config object
         * @returns {{}}
         */
        _panel.setup = function _panelSetup(_opts){

            _panel.opts = extend(true, {}, _opts);

            _panel.opts.tag = _panel.opts.tag || 'div';
            _panel.opts.title = _panel.opts.title || _panel.opts.header || '';
            _panel.opts.body = _panel.opts.body || _panel.opts.content || '';
            _panel.opts.footer = _panel.opts.footer || '';

            _panel.opts.attr = _panel.opts.attr || {};

            if (_panel.opts.id) {
                _panel.opts.attr.id = _panel.opts.id
            }

            if (_panel.opts.name) {
                _panel.opts.attr.data = getObject(_panel.opts.attr.data);
                _panel.opts.attr.data.name = _panel.opts.name;
            }

            _panel.panel = _panel.element = newPanel();

            return _panel;
        };

        // if 'opts' arg is passed to .panel(), call .setup()
        if (opts) {
            _panel.setup(opts);
        }

        // render the panel and append to 'container'
        _panel.render = function _panelRender(container){
            $$(container).append(_panel.panel);
            return _panel;
        };

        // render immediately if 'container' is specified
        if (container) {
            _panel.render(container);
        }

        _panel.get = function _panelGet(){
            return _panel.element;
        };

        return _panel;

    }


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

        saveBtn = footerButton('Save', 'submit', true, 'save pull-right');
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
