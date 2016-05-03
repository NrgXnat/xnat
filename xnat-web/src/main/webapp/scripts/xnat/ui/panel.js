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

    panel.init = function(config){
        var _target = spawn('div.panel-body'),
            _panel  = spawn('div.panel.panel-default', [
                ['div.panel-heading', [
                    ['h3.panel-title', config.title || config.label]
                ]],
                _target,
                ['div.panel-footer', config.footer]
            ]);
        return {
            target: _target,
            element: _panel,
            spawned: _panel,
            get: function(){
                return _panel;
            }
        }
    };

    panel.form = function(config){
        var _target = spawn('div.panel-body', config.config),
            _footer = [
                ['button.btn.btn-sm.btn-primary.save.pull-right|type=submit', 'Submit'],
                ['span.pull-right', '&nbsp;&nbsp;&nbsp;'],
                ['button.btn.btn-sm.btn-default.revert.pull-right|type=button', 'Discard Changes'],
                ['button.btn.btn-sm.btn-link.defaults.pull-left', 'Default Settings'],
                ['div.clear']
            ],
            _panel = spawn('form.xnat-form-panel.panel.panel-default', [
                ['div.panel-heading', [
                    ['h3.panel-title', config.title || config.label]
                ]],
                
                _target,
                
                ['div.panel-footer', config.footer || _footer]
            ]);
        return {
            target: _target,
            element: _panel,
            spawned: _panel,
            get: function(){
                return _panel;
            }
        }
    };

    panel.input = {};

    panel.input.text = function(opts){
        return XNAT.ui.templates.panelInput(opts).spawned;
    };

    panel.input.email = function(opts){
        opts.type = 'text';
        opts.className = [].concat(opts.className||[], 'email').join(' ').trim();
        return XNAT.ui.templates.panelInput(opts).spawned;
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
