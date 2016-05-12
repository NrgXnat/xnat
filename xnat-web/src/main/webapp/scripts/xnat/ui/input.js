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
        var val = '';
        try {
            val = eval(lookup.trim())
        }
        catch (e) {
            val = '';
            console.log(e);
        }
        el.value = val;
        return val;
    }

    function lookupObjectValue(root, objStr){
        var val = '';
        if (!objStr) {
            objStr = root;
            root = window;
        }
        root = root || window;
        objStr.toString().trim().split('.').forEach(function(part, i){
            part = part.trim();
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
    // MAIN FUNCTION
    input = function(type, config){
        // only one argument?
        // could be a config object
        if (!config && typeof type != 'string') {
            config = type;
            type = null; // it MUST contain a 'type' property
        }
        config = getObject(config);
        config.type = type || config.type || 'text';
        // lookup a value if it starts with '??'
        var doLookup = '??';
        if (config.value && config.value.toString().indexOf(doLookup) === 0) {
            config.value = lookupObjectValue(config.value.split(doLookup)[1])
        }
        // lookup a value from a namespaced object
        // if no value is given
        if (!config.value && config.data && config.data.lookup) {
            config.value = lookupObjectValue(config.data.lookup)
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
        opts = getObject(opts);
        var config = getObject(opts.config || opts.element || {});
        config.addClass = className;
        config.data = extend({validate: className}, config.data);
        if (!config.data.validate) delete config.data.validate;
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
            return setupType('text', type, config);
        }
    });

    numberTypes = ['number', 'int', 'integer', 'float'];
    numberTypes.forEach(function(type){
        input[type] = function(config){
            return setupType('number', type, config);
        }
    });

    otherTypes = [
        'password', 'date', 'checkbox',
        'radio', 'button', 'hidden', 'file'
    ];
    otherTypes.forEach(function(type){
        input[type] = function(config){
            return setupType(type, type, config);
        }
    });

    // save a list of all available input types
    input.types = [].concat(textTypes, numberTypes, otherTypes);

    // after the page is finished loading, set empty
    // input values from [data-lookup] attribute
    $(window).load(function(){
        $(':input[data-lookup]').each(function(){
            var $input = $(this);
            var val = lookupObjectValue($input.dataAttr('lookup'));
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
