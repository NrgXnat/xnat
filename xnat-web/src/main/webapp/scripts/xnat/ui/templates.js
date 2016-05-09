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


    // ========================================
    // generic panel element
    template.panelElement = function(opts, content){
        var _templ, _spawn, _html;
        opts.className = [].concat(opts.className||[], 'panel-element').join(' ').trim();
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
                return spawn(_templ).outerHTML;
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
        opts.id = opts.id||toDashed(opts.name);
        opts.label = opts.label||opts.title||opts.name||'';
        return template.panelElement(opts, [
            ['label.element-label|for='+opts.id, opts.label],
            ['div.element-wrapper', [
                element || ['input', {
                    type: opts.type||'text',
                    id: opts.id,
                    name: opts.name,
                    className: opts.className||'',
                    size: 25,
                    title: opts.title||opts.name||opts.id,
                    value: opts.value||''
                }],
                ['div.description', opts.description||opts.body||opts.html]
            ]]
        ]);
    };
    // ========================================


    // ========================================
    // select element for form panels
    template.panelSelect = function(opts){
        opts = getObject(opts);
        opts.name = opts.name || opts.id;
        opts.id = opts.id || toDashed(opts.name);

        var _select = spawn('select', {
            id: opts.id,
            name: opts.name,
            className: opts.className||'',
            //size: 25,
            title: opts.title||opts.name||opts.id||'',
            value: opts.value||''
        });
        // add the options
        $.each(opts.options||{}, function(name, prop){
            var _option = spawn('option', {
                html: prop.label || prop.text,
                value: prop.value
            });
            // select the option if it's the select element's value
            if (prop.value === opts.value){
                _option.selected = true;
            }
            _select.appendChild(_option)
        });
        return template.panelElement(opts, [
            ['label.element-label|for='+opts.id, opts.label||opts.title||opts.name],
            ['div.element-wrapper', [
                _select,
                ['div.description', opts.description||opts.body||opts.html]
            ]]
        ]);
    };
    // ========================================


    template.panelElementGroup = function(opts, elements){
        opts = getObject(opts);
        return template.panelElement(opts, [
            ['label.element-label|for='+opts.id, opts.label||opts.title||opts.name],
            ['div.element-wrapper', [

                elements

            ]]
        ]);
    };


    return XNAT.ui.templates = XNAT.ui.template = template;

}));
