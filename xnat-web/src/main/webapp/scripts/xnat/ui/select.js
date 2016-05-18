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

    function addOption(el, opt){
        el.appendChild(spawn('option', {
            value: opt.value || '',
            html: opt.html || opt.text || opt.value
        }));
    }
    
    // generate JUST the options
    // opts is an array of option objects
    select.options = function(opts){
        return opts.map(function(opt){
            return spawn('option', opt);    
        });      
    };
    
    // ========================================
    // MAIN FUNCTION
    select.menu = function(config){

        var frag = document.createDocumentFragment(),
            menu, label;

        config = getObject(config);

        // show the label on the left by default
        config.layout = config.layout || 'left';

        config.id = config.id || config.name || randomID('menu-', false);

        config.element = extend(true, {
            id: config.id,
            name: config.name || config.id
        }, config.element);

        menu = spawn('select', config.element);

        addOption(menu, { html: 'Select' });
        
        if (config.options){
            if (Array.isArray(config.options)) {
                forEach(config.options, function(opt){
                    addOption(menu, opt);
                })    
            }
            else {
                forOwn(config.options, function(txt, val){
                    var opt = {};
                    if (stringable(val)) {
                        opt.value = val;
                        opt.html = txt;
                    }
                    else {
                        opt = val;
                    }
                    addOption(menu, opt);
                });
            }
        }
        
        // if there's no label, wrap the 
        // <select> inside a <label> element
        if (!config.label) {
            //frag = XNAT.element().label(menu.get());
            frag = spawn('label', [menu]);
        } 
        else {
            label = spawn('label', {
                attr: { for: config.id }
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
        opts = getObject(opts);
        opts.element = opts.element || {};
        opts.element.multiple = true;
        return select.menu(opts);
    };
    
    // this script has loaded
    select.loaded = true;

    return XNAT.ui.select = select;

}));
