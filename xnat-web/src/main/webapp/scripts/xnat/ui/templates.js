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


    // ========================================
    // subhead element to segment panels
    template.panelSubhead = function(opts){
        var _templ, _spawn, _html;
        opts = getObject(opts);
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
        opts = getObject(opts);
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
        opts = getObject(opts);
        opts.id = opts.id||toDashed(opts.name);
        opts.label = opts.label||opts.title||opts.name||'';
        return template.panelElement(opts, [
            ['label.element-label|for='+opts.id, opts.label],
            ['div.element-wrapper', [
                element || ['div', {
                    id: opts.id,
                    name: opts.name,
                    className: opts.className||'',
                    size: 25,
                    title: opts.title||opts.name||opts.id,
                    html: opts.value||''
                }],
                ['div.description', opts.description||opts.body||opts.html]
            ]]
        ]);
    };
    // ========================================    

    // ========================================
    // input element for form panels
    template.panelInput = function(opts, element){
        opts = getObject(opts);
        opts.name = opts.name || opts.id || randomID('input-', false);
        opts.id = opts.id||toDashed(opts.name||'');
        opts.label = opts.label||opts.title||opts.name||'';
        opts.element = extend({
            type: opts.type||'text',
            id: opts.id,
            name: opts.name,
            className: opts.className||'',
            size: 25,
            title: opts.title||opts.name||opts.id,
            value: opts.value||''
        }, opts.element);

        opts.data = opts.data || {};
        opts.data.value = opts.data.value || opts.value;

        addDataObjects(opts.element, opts.data||{});

        // use an existing element (passed as the second argument)
        // or spawn a new one
        element = element || spawn('input', opts.element);

        if (/checkbox|radio/i.test(opts.type||'') && opts.checked) {
            element.checked = true;
        }

        var dataGetParts;

        // set the values of form elements
        if (opts.data) {
            if (opts.data.get){
                dataGetParts = opts.data.get.split('|');
                $.ajax({
                    method: dataGetParts[0] || 'GET',
                    url: XNAT.url.restUrl(dataGetParts[1] || ''),
                    success: function(data){
                        // split object path
                        if (dataGetParts[2]) {
                            dataGetParts[2].split('.').forEach(function(part){
                                data = data[part]
                            });
                        }
                        // element.value = data;
                        // changeVal() changes the value and triggers
                        // the 'onchange' event
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
        opts = getObject(opts);
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
                html: prop.label || prop.value || name,
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
        opts = getObject(opts);
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
