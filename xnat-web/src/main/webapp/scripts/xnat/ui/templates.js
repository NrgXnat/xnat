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
        var val = '';
        try {
            val = eval(lookup.trim());
        }
        catch (e) {
            val = '';
            console.log(e);
        }
        // changeVal() changes the value and triggers
        // the 'onchange' event
        $(el).changeVal(val).dataAttr('value', val);
        return val;
    }

    // another way to do this without using eval()
    // is to loop over object string using dot notation:
    // var myVal = lookupObjectValue(XNAT, 'data.siteConfig.siteId');
    // --> myVal == 'myXnatSiteId'
    function lookupObjectValue(root, objStr){
        var val = '';
        if (!objStr) {
            objStr = root;
            root = window;
        }
        root = root || window;
        objStr.toString().trim().split('.').forEach(function(part, i){
            // start at the root object
            if (i === 0) {
                val = root[part] || {};
            }
            else {
                val = val[part];
            }
        });
        return val;
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
            title: opts.title||opts.name||opts.id,
            value: opts.value||''
        }, opts.element);

        opts.data = opts.data || {};
        
        if (opts.element.type !== 'password'){
            opts.data.value = opts.data.value || opts.value;
        }

        addDataObjects(opts.element, opts.data||{});
        
        if (opts.placeholder) {
            opts.element.placeholder = opts.placeholder;
        }

        // use an existing element (passed as the second argument)
        // or spawn a new one
        element = element || spawn('input', opts.element);

        if (/checkbox|radio/i.test(opts.type||'') && opts.checked) {
            element.checked = true;
        }

        // set the value of individual form elements
        
        // look up a namespaced object value if the value starts with '??'
        var doLookup = '??';
        if (opts.value && opts.value.toString().indexOf(doLookup) === 0) {
            element.value = lookupObjectValue(opts.value.split(doLookup)[1]);
        }
        
        if (opts.load) {
            if (opts.load.lookup) {
                lookupValue(element, opts.load.lookup);
            }
            else if (opts.load.url){
                $.ajax({
                    method: opts.load.method || 'GET',
                    url: XNAT.url.restUrl(opts.load.url),
                    success: function(data){
                        // get value from specific object path
                        if (opts.load.prop) {
                            opts.load.prop.split('.').forEach(function(part){
                                data = data[part] || {};
                            });
                            // data = lookupObjectValue(opts.load.prop);
                        }
                        $(element).changeVal(data).dataAttr('value', data);
                    }
                })
            }
        }

        return template.panelElement(opts, [
            ['label.element-label|for='+element.id||opts.id, opts.label],
            ['div.element-wrapper', [

                element,

                ['div.description', opts.description||opts.body||opts.html]
            ]]
        ]);
    };
    // ========================================


    // ========================================
    // select element for form panels
    template.panelSelect = function(opts){
        opts = cloneObject(opts);
        opts.name = opts.name || opts.id || randomID('select-', false);
        opts.id = opts.id || toDashed(opts.name||'');
        opts.element = extend({
            id: opts.id,
            name: opts.name,
            className: opts.className||'',
            //size: 25,
            title: opts.title||opts.name||opts.id||'',
            value: opts.value||''
        }, opts.element);

        var _select = spawn('select', opts.element, [['option|value=!', 'Select']]);
        
        // add the options
        $.each(opts.options||{}, function(name, prop){
            var _option = spawn('option', extend(true, {
                html: prop.label || prop.value || prop,
                value: prop.value || name
            }, prop.element));
            // select the option if it's the select element's value
            if (prop.value === opts.value){
                _option.selected = true;
            }
            _select.appendChild(_option)    
        });
        return template.panelInput(opts, _select);
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
