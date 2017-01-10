/*
 * web: select.js
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

    var undefined, select;

    XNAT.ui = getObject(XNAT.ui || {});

    // if XNAT.ui.input is already defined,
    // save it and its properties to add later
    // as methods and properties to the function
    select = getObject(XNAT.ui.select || {});

    function addOption(el, opt, sel){
        var val, txt;
        if (typeof opt === 'string') {
            val = opt;
            txt = opt;
        }
        else {
            val = opt.value !== undefined ? opt.value : '';
            txt = opt.html || opt.text || opt.label || val;
        }
        var option = spawn('option', extend(true, {
            value: val,
            html: txt,
            selected: val === sel || [].concat(sel).indexOf(val) > -1 || opt.selected || false
        }, opt.element, {
            //selected: val === sel || [].concat(sel).indexOf(val) > -1 || opt.selected || false
        }));
        el.appendChild(option);
    }
    
    // generate JUST the options
    // opts is an array of option objects
    select.options = function(opts){
        return opts.map(function(opt){
            return spawn('option', opt);    
        });      
    };


    // ADD options to a menu
    select.addOptions = function(menu, options){
        $$(menu).append(select.options(options));
    };
    
    
    // ========================================
    // MAIN FUNCTION
    select.menu = function(config){

        var frag = document.createDocumentFragment(),
            $menu, menu, label;

        config = cloneObject(config);

        // show the label on the left by default
        config.layout = firstDefined(config.layout, 'left');

        config.id = config.id || toDashed(config.name) || randomID('menu-', false);
        config.name = config.name || '';
        config.value = config.value !== undefined ? config.value : '';

        config.element = extend(true, {
            id: config.id,
            name: config.name,
            value: config.value,
            title: config.title || ''
        }, config.element);

        $menu = $.spawn('select', config.element);
        menu = $menu[0];

        // DO NOT add default 'Select' option
        //addOption(menu, { html: 'Select' });
        
        if (config.options){
            if (Array.isArray(config.options)) {
                forEach(config.options, function(opt){
                    addOption(menu, opt, config.value);
                })    
            }
            else {
                forOwn(config.options, function(val, txt){
                    var opt = {};
                    if (stringable(txt)) {
                        opt.value = val;
                        opt.html = txt;
                    }
                    else {
                        opt = txt;
                    }
                    addOption(menu, opt, config.value);
                });
            }
        }
        
        // force menu change event to select 'selected' option
        $menu.changeVal(config.value);
        // $menu.find('[value="' + config.value + '"]').prop('selected', true);

        // if there's no label, wrap the 
        // <select> inside a <label> element
        if (!config.label) {
            //frag = XNAT.element().label(menu.get());
            frag = spawn('label', [menu]);
        } 
        else {
            label = spawn('label', {
                attr: { "for": config.id }
            }, config.label);
            
            if (config.layout !== 'right') {
                frag.appendChild(label);
            }

            frag.appendChild(menu.get());

            // seems redundant, but... it's not!
            if (config.layout === 'right') {
                frag.appendChild(label);
            }
        }

        return {
            target: menu,
            element: menu,
            spawned: frag,
            get: function(){
                return frag;
            }
        }
    };
    // ========================================
    select.single = select.menu;
    
    
    select.multiple = function(opts){
        opts = cloneObject(opts);
        opts.element = opts.element || {};
        opts.element.multiple = true;
        return select.menu(opts);
    };


    // load data and add it to a container
    //select.loadMenu = function(opts){
    //
    //};
    
    
    // this script has loaded
    select.loaded = true;

    return XNAT.ui.select = select;

}));
