/*
 * web: input.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
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

    var undefined, textTypes,
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
            val = eval((lookup||'').trim()) || ''
        }
        catch (e) {
            val = '';
            console.log(e);
        }
        el.value = val;
        return val;
    }


    // ========================================
    // MAIN FUNCTION
    input = function(type, config){
        // only one argument?
        // could be a config object
        if (!config && typeof type != 'string') {
            config = type;
            type = null; // it MUST contain a 'type' property
        }
        config = cloneObject(config);
        config.type = type || config.type || 'text';
        // lookup a value if it starts with '??'
        var doLookup = '??';
        if (config.value && (config.value+'').indexOf(doLookup) === 0) {
            config.value = lookupValue(config.value.split(doLookup)[1])
        }
        // lookup a value from a namespaced object
        // if no value is given
        if (config.value === undefined && config.data && config.data.lookup) {
            config.value = lookupValue(config.data.lookup)
        }
        var spawned = spawn('input', config);
        return {
            element: spawned,
            spawned: spawned,
            get: function(){
                return spawned;
            }
        }
    };
    // ========================================


    function setupType(type, className, opts){
        var config = extend(true, {}, opts, opts.element, {
            data: {},
            $: { addClass: className }
        });
        config.data.validate = opts.validation||opts.validate;
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
            config.size = config.size || 25;
            return setupType('text', type, config);
        }
    });

    numberTypes = [
        'number', 'int', 'integer', 'float'
    ];
    numberTypes.forEach(function(type){
        input[type] = function(config){
            config.size = config.size || 25;
            return setupType('number', type, config);
        }
    });

    otherTypes = [
        'date', 'file', 'button', 'hidden'
    ];
    otherTypes.forEach(function(type){
        input[type] = function(config){
            return setupType(type, type, config);
        }
    });

    input.username = function(config){
        config = extend(true, {}, config, config.element);
        config.size = config.size || 25;
        config.autocomplete = 'off';
        return setupType('text', 'username', config);
    };
    otherTypes.push('username');

    // TODO: HANDLE PASSWORD VALUES IN A SAFER WAY
    input.password = function(config){
        config = extend(true, {}, config, config.element);
        config.size = config.size || 25;
        config.value = '';
        config.placeholder = '********';
        // config.value = config.value ? '********' : '';
        // config.data = { value: '!' };
        // config.onfocus = function(){
        //     this.select();
        //     (config.onfocus||diddly).call();
        // };
        config.autocomplete = 'new-password';
        // config.autocomplete = 'off';
        return setupType('password', 'password', config);
    };
    otherTypes.push('password');

    // checkboxes are special
    input.checkbox = function(config){
        config = extend(true, {}, config, config.element);
        // config.onchange = function(){
        //     this.value = this.checked || $(this).data('uncheckedValue') || '';
        // };
        return setupType('checkbox', '', config);
    };
    otherTypes.push('checkbox');

    input.switchbox = function(config){
        addClassName(config, 'switchbox');
        return spawn('label.switchbox', [
            input.checkbox(config).get(),
            ['span.switchbox-outer', [['span.switchbox-inner']]],
            ['span.switchbox-on', config.onText||''],
            ['span.switchbox-off', config.offText||'']
        ])
    };
    otherTypes.push('switchbox');

    // radio buttons are special too
    input.radio = function(config){
        config = extend(true, {}, config, config.element);
        return setupType('radio', '', config);
    };
    otherTypes.push('radio');

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

    // // not *technically* an <input> element, but a form input nonetheless
    // input.textarea = function(config){
    //
    // };

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
