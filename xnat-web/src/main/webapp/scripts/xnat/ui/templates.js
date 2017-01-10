/*
 * web: templates.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Templates for creating UI elements with spawn.js
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

    var undefined, template,
        $ = jQuery || null; // check and localize

    XNAT.ui = getObject(XNAT.ui || {});

    XNAT.ui.template = template =
        XNAT.ui.template || {};

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

    // retrieve value via REST and put it in the element
    function ajaxValue(el, url, prop){
        var opts = {
            url: XNAT.url.rootUrl(url),
            success: function(data){
                if (prop && isPlainObject(data)) {
                    data = lookupObjectValue(data, prop.trim());
                    // data = lookupValue(prop.trim());
                }
                $$(el).val(data).change();
            }
        };
        return $.get(opts);
    }


    // ========================================
    // subhead element to segment panels
    template.panelSubhead = function(opts){
        var _templ, _spawn, _html;
        opts = cloneObject(opts);
        _templ = ['h4.panel-subhead', opts];
        _spawn = function(){
            return spawn.apply(null, _templ);
        };
        _html = _spawn().outerHTML;
        return {
            template: _templ,
            spawned: _spawn(),
            spawn: _spawn,
            html: _html,
            get: _spawn
        }
    };
    // ========================================


    // ========================================
    // generic panel element
    template.panelElement = function(opts, content){
        var _templ, _spawn, _html;
        opts = cloneObject(opts);
        addClassName(opts, 'panel-element');
        opts.name = (opts.name||'').replace(/^:*/,'');
        _templ = [
            'div|data-name='+opts.name,
            { className: opts.className },
            [].concat(content, spawn('br.clear'))
        ];
        _spawn = function(){
            return spawn.apply(null, _templ);
        };
        _html = _spawn().outerHTML;
        return {
            template: _templ, // the raw template (Spawn array)
            spawned: _spawn(), // pre-spawned
            spawn: _spawn, // call to make a fresh spawn
            html: _html, // pre-spawned HTML
            get: _spawn,
            getHTML: function(){ // call to get fresh HTML
                return _spawn().outerHTML;
            }
        }
    };
    // ========================================


    // ========================================
    // display only element for form panels
    template.panelDisplay = function(opts, element){

        opts = cloneObject(opts);
        opts.id = opts.id||toDashed(opts.name||'');
        opts.label = opts.label||'';

        // pass in an element or create a new 'div' element
        element =
            element || spawn('div', extend(true, {
                id: opts.id,
                title: opts.title||opts.name||opts.id,
                html: opts.value||opts.html||opts.text||opts.body||''
            }, opts.element));

        if (opts.className || opts.classes || opts.addClass) {
            addClassName(element, [opts.className, opts.classes, opts.addClass]);
        }

        return template.panelElement(opts, [

            // only add a label if specified
            (opts.label ? ['label.element-label|for='+element.id||opts.id, opts.label] : ''),

            ['div.element-wrapper', [].concat(

                (opts.beforeElement ? opts.beforeElement : []),

                element ,

                (opts.afterElement ? opts.afterElement : []),

                spawn('div.description', opts.description||'')

            )]
        ]);
    };
    // ========================================


    // ========================================
    // input element for form panels
    template.panelInput = function(opts, element){
        opts = cloneObject(opts);
        opts.name = opts.name || opts.id || randomID('input-', false);
        opts.id = opts.id||toDashed(opts.name||'');
        opts.label = opts.label||opts.title||opts.name||'';
        opts.element = extend({
            type: opts.type||'text',
            id: opts.id,
            name: opts.name,
            size: opts.size || 25,
            title: opts.title||opts.label||opts.name||opts.id,
            value: opts.value||''
        }, opts.element);

        if (opts.className || opts.classes || opts.addClass) {
            addClassName(opts.element, [opts.className, opts.classes, opts.addClass]);
        }

        opts.data = opts.data || {};

        if (opts.element.type !== 'password'){
            opts.data.value = opts.data.value || opts.value;
        }
        else {
            opts.data.value = '!';
            opts.element.autocomplete = 'new-password';
        }

        if (opts.validation || opts.validate) {
            opts.data.validate = opts.validation || opts.validate;
            addClassName(opts.element, opts.data.validate);
        }

        if (opts.message) {
            opts.data.message = opts.message;
        }

        addDataObjects(opts.element, opts.data);

        if (opts.placeholder) {
            opts.element.placeholder = opts.placeholder;
        }

        // use an existing element (passed as the second argument)
        // or spawn a new one
        element = element || spawn('input', opts.element);

        // cache a jQuery object
        var $element = $(element);

        // set the value of individual form elements
        var hasValue = isDefined(opts.value);

        // look up a namespaced object value if the value starts with '??'
        var doLookup = '??';
        if (hasValue && opts.value.toString().indexOf(doLookup) === 0) {
            // element.value = lookupValue(opts.value.split(doLookup)[1].trim());
            $element.val(lookupObjectValue(opts.value.split(doLookup)[1].trim()));
        }

        var doEval = '!?';
        if (hasValue && opts.value.toString().indexOf(doEval) === 0) {
            opts.value = (opts.value.split(doEval)[1]||'').trim();
            try {
                $element.changeVal(eval(opts.value));
            }
            catch (e) {
                $element.val('').change();
            }
        }

        // get value via REST/ajax if value starts with $?
        // value: $? /path/to/data | obj:prop:name
        var ajaxPrefix = '$?';
        var ajaxUrl = '';
        var ajaxProp = '';
        if (hasValue && opts.value.toString().indexOf(ajaxPrefix) === 0) {
            ajaxUrl = (opts.value.split(ajaxPrefix)[1]||'').split('|')[0];
            ajaxProp = opts.value.split('|')[1] || '';
            ajaxValue(element, ajaxUrl.trim(), ajaxProp.trim());
        }

        // trigger an 'onchange' event
        $element.change();

        // add value to [data-value] attribute
        // (except for textareas - that could get ugly)
        if (!/textarea/i.test(element.tagName) && !/password/i.test(element.type)){
            if (isArray(element.value) || stringable(element.value)) {
                $element.dataAttr('value', element.value);
            }
        }

        var inner = [];

        // add 'before' content before the core element
        if (opts.beforeElement) {
            opts.beforeElement = stringable(opts.beforeElement) ? opts.beforeElement : '';
            inner.push(opts.beforeElement);
        }


        // special stuff for switchbox elements
        if (/switchbox/i.test(opts.kind)) {
            inner.push(spawn('label.switchbox', [
                element,
                ['span.switchbox-outer', [['span.switchbox-inner']]],
                ['span.switchbox-on', opts.onText||''],
                ['span.switchbox-off', opts.offText||'']
            ]))
        }
        else {
            inner.push(element);
        }


        // add 'after' content after the core element
        if (opts.afterElement) {
            opts.afterElement = stringable(opts.afterElement) ? opts.afterElement : '';
            inner.push(opts.afterElement);
        }

        var $hiddenInput, hiddenInput;

        // check buttons if value is true
        if (/checkbox/i.test(element.type||'')) {

            element.checked = /^(true|checked|1)$/gi.test((opts.checked||element.value||'').trim());

            // add a hidden input to capture the checkbox/radio value
            $hiddenInput = $.spawn('input.proxy', {
                type: 'hidden',
                name: element.name,
                value: element.checked ? (element.value || opts.value || element.checked || true) : false
            });

            hiddenInput = $hiddenInput[0];

            // add [data-value] attribute
            $hiddenInput.dataAttr('value', hiddenInput.value);

            $element.on('click', function(e){
                var _val = this.value+'';
                // shift-click a checkbox (or switchbox) to
                // switch between boolean true/false and 1/0
                if (e.shiftKey) {
                    if (/^(1|0)$/g.test(_val)) {
                        _val = _val === '1' ? 'true' : 'false';
                    }
                    else if (/^(true|false)$/gi.test(_val)) {
                        _val = _val === 'true' ? '1' : '0';
                    }
                }
                this.value = _val;
            });

            // change the value of the hidden input
            // when 'controller' input changes
            $element.on('change', function(e){
                var _val = this.value+'';
                if (/^(1|0)$/g.test(_val)) {
                    _val = this.checked ? '1' : '0';
                }
                else if (/^(true|false)$/gi.test(_val)) {
                    _val = this.checked ? 'true' : 'false';
                }
                this.value = hiddenInput.value = _val;
                $hiddenInput.toggleClass('dirty');
            });

            // copy name to title
            element.title = element.name;

            // remove name of checkbox/radio to avoid conflicts
            element.name = '';

            // add a class for easy selection
            addClassName(element, 'controller');
            addClassName(opts, 'controller');

            // add the hidden input
            inner.push(hiddenInput);

        }

        // add the description after the input
        inner.push(spawn('div.description', opts.description||opts.body||opts.html));

        return template.panelElement(opts, [
            ['label.element-label|for='+element.id||opts.id, opts.label],
            ['div.element-wrapper', inner]
        ]);
    };
    // ========================================


    template.panelElementGroup = function(opts, elements){
        opts = cloneObject(opts);
        return template.panelElement(opts, [
            ['label.element-label|for='+opts.id, opts.label||opts.title||opts.name],
            ['div.element-wrapper', elements]
        ]);
    };


    template.codeEditor = function(opts, contents){
        // options for the 'div.editor-content' element
        opts = extend(true, opts, {
            style: {
                width: '100%', height: '100%',
                position: 'absolute',
                top: 0, right: 0,
                bottom: 0, left: 0,
                border: '1px solid #ccc'
            }
        });
        // don't pass 'before' and 'after' into the editor
        var before = opts.before || '';
        var after = opts.after || '';
        delete opts.before;
        delete opts.after;
        var content = spawn('div.editor-content', opts, contents||'');
        var _tmpl = ['form.code-editor', [
            before,
            ['div.editor-wrapper', {
                style: {
                    width:  opts.width  || '840px',
                    height: opts.height || '482px',
                    position: 'relative'
                }
            }, [content]],
            after
        ]];
        var _spawned = spawn.apply(null, _tmpl);
        var _html = _spawned.outerHTML;
        return {
            template: _tmpl, // the raw template (Spawn array)
            spawned: _spawned, // pre-spawned
            editor: content, // easy-to-remember name for the editor div
            target: content, // for inserting content dynamically
            inner: content,
            html: _html, // pre-spawned HTML
            outerHTML: _html,
            get: function(){
                return _spawned;
            },
            getHTML: function(){ // call to get the HTML
                return _html;
            }
        }
    };

    return XNAT.ui.templates = XNAT.ui.template = template;

}));
