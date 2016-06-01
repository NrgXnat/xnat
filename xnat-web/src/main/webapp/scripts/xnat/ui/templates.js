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
        _templ = [
            'div|data-name='+(opts.name||''),
            { className: opts.className },
            content
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
        opts.label = opts.label||opts.title||opts.name||'';
        
        // pass in an element or create a new 'div' element
        element = 
            element || spawn('div', {
                id: opts.id,
                className: opts.className||'',
                title: opts.title||opts.name||opts.id,
                html: opts.value||opts.html||opts.text||opts.body||''
            });
        
        return template.panelElement(opts, [
            ['label.element-label|for='+element.id||opts.id, opts.label],
            ['div.element-wrapper', [
                
                element ,
                
                ['div.description', opts.description]
            ]]
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
            className: opts.className||'',
            size: opts.size || 25,
            title: opts.title||opts.label||opts.name||opts.id,
            value: opts.value||''
        }, opts.element);

        opts.data = opts.data || {};
        
        if (opts.element.type !== 'password'){
            opts.data.value = opts.data.value || opts.value;
        }

        if (opts.validation || opts.validate) {
            opts.data.validate = opts.validation || opts.validate;
            addClassName(opts.element, opts.data.validate);
        }

        addDataObjects(opts.element, opts.data||{});
        
        if (opts.placeholder) {
            opts.element.placeholder = opts.placeholder;
        }

        // use an existing element (passed as the second argument)
        // or spawn a new one
        element = element || spawn('input', opts.element);

        // cache a jQuery object
        var $element = $(element);

        // set the value of individual form elements
        
        // look up a namespaced object value if the value starts with '??'
        var doLookup = '??';
        if (opts.value && opts.value.toString().indexOf(doLookup) === 0) {
            // element.value = lookupValue(opts.value.split(doLookup)[1].trim());
            $element.val(lookupObjectValue(opts.value.split(doLookup)[1].trim())).change();
        }

        var doEval = '!?';
        if (opts.value && opts.value.toString().indexOf(doEval) === 0) {
            opts.value = (opts.value.split(doEval)[1]||'').trim();
            try {
                $element.val(eval(opts.value)).change();
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
        if (opts.value && opts.value.toString().indexOf(ajaxPrefix) === 0) {
            ajaxUrl = (opts.value.split(ajaxPrefix)[1]||'').split('|')[0];
            ajaxProp = opts.value.split('|')[1] || '';
            ajaxValue(element, ajaxUrl.trim(), ajaxProp.trim());
        }

        // trigger an 'onchange' event
        $element.change();

        // add value to [data-value] attribute
        // (except for textareas - that could get ugly)
        if (isArray(element.value) || stringable(element.value)) {
            $element.not('textarea').dataAttr('value', element.value);
        }

        var inner = [element];

        var hiddenInput;

        // check buttons if value is true
        if (/checkbox|radio/i.test(element.type||'')) {

            element.checked = /true|checked/i.test((opts.checked || element.value).toString());

            // add a hidden input to capture the checkbox/radio value
            hiddenInput = spawn('input', {
                type: 'hidden',
                name: element.name,
                value: element.checked
            });

            // change the value of the hidden input onclick
            element.onclick = function(){
                hiddenInput.value = this.checked.toString();
            };
            
            // remove name of checkbox/radio to avoid conflicts
            element.name = '';

            // add a class for easy selection
            addClassName(element, 'controller');

            // add the hidden input
            inner.push(hiddenInput);

        }

        // add the description after the input
        inner.push(['div.description', opts.description||opts.body||opts.html]);

        return template.panelElement(opts, [
            ['label.element-label|for='+element.id||opts.id, opts.label],
            ['div.element-wrapper', inner]
        ]);
    };
    // ========================================


    // ========================================
    // select element for form panels
    // template.panelSelect = function(opts){
    //    
    //     opts = cloneObject(opts);
    //    
    //     opts.name = opts.name || opts.id || randomID('select-', false);
    //     opts.id = opts.id || toDashed(opts.name||'');
    //     opts.element = extend({
    //         id: opts.id,
    //         name: opts.name,
    //         className: opts.className||'',
    //         //size: 25,
    //         title: opts.title||opts.name||opts.id||'',
    //         value: opts.value||''
    //     }, opts.element);
    //
    //     var _select = spawn('select', opts.element, [['option', 'Select']]);
    //    
    //     // add the options
    //     $.each(opts.options||{}, function(name, prop){
    //         var _option = spawn('option', extend(true, {
    //             html: prop.html || prop.text || prop.label || prop.value || prop,
    //             value: prop.value || name
    //         }, prop.element));
    //         // select the option if it's the select element's value
    //         if (prop.value === opts.value){
    //             _option.selected = true;
    //         }
    //         _select.appendChild(_option)    
    //     });
    //    
    //     return template.panelInput(opts, _select);
    //
    // };
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
