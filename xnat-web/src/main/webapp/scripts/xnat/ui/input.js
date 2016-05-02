/*!
 * Spawn UI elements using the Spawner service
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
        config = getObject(opts.config || opts.element || {});
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
        'radio', 'button', 'hidden'
    ];
    otherTypes.forEach(function(type){
        input[type] = function(config){
            return setupType(type, type, config);
        }
    });

    // save a list of all available input types
    input.types = [].concat(textTypes, numberTypes, otherTypes);

    // add back items that may have been on
    // a global XNAT.ui.input object or function
    extend(input, input_);

    // this script has loaded
    input.loaded = true;

    return XNAT.ui.input = input;

}));
