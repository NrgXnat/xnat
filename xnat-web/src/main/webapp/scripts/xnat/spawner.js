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

    var undefined,
        ui, spawner,
        NAMESPACE  = 'XNAT.ui',
        $          = jQuery || null, // check and localize
        hasConsole = console && console.log;

    XNAT.ui =
        getObject(XNAT.ui || {});
    XNAT.spawner = spawner =
        getObject(XNAT.spawner || {});

    // keep track of items that spawned
    spawner.spawnedElements = [];

    // keep track of items that didn't spawn
    spawner.notSpawned = [];


    // ==================================================
    // MAIN FUNCTION
    spawner.spawn = function _spawn(obj){

        var frag  = document.createDocumentFragment(),
            $frag = $(frag);

        forOwn(obj, function(item, prop){

            var kind, method, spawnedElement, $spawnedElement;
            
            // save the config properties in a new object
            prop = getObject(prop);

            prop.config = prop.config || prop.element || {};

            // use 'name' property in element or config
            // then look for 'name' at object root
            // lastly use the object's own name
            prop.name = prop.name || item;

            // accept 'kind' or 'type' property name
            // but 'kind' will take priority
            // with a fallback to a generic div
            kind = prop.kind || prop.type || 'div.spawned';

            // do a raw spawn() if 'kind' is 'element'
            // or if there's a tag property
            if (kind === 'element' || prop.tag) {
                try {
                    spawnedElement =
                        spawn(prop.tag || prop.config.tag || 'div', prop.config);
                    // jQuery's .append() method is
                    // MUCH more robust and forgiving
                    // than element.appendChild()
                    $frag.append(spawnedElement);
                    spawner.spawnedElements.push(spawnedElement);
                }
                catch (e) {
                    if (hasConsole) console.log(e);
                    spawner.notSpawned.push(prop);
                }
            }
            else {
                // check for a matching XNAT.ui method to call:
                method =
                    // XNAT.ui.kind.init()
                    eval(NAMESPACE + '.' + kind + '.init') ||

                    // XNAT.ui.kind()
                    eval(NAMESPACE + '.' + kind) ||

                    // XNAT.kind.init()
                    eval('XNAT.' + kind + '.init') ||

                    // XNAT.kind()
                    eval('XNAT.' + kind) ||

                    // kind.init()
                    eval(kind + '.init') ||

                    // kind()
                    eval(kind);


                // only spawn elements with defined methods
                if (isFunction(method)) {

                    // 'spawnedElement' item will be an
                    // object with a .get() method that
                    // will retrieve the spawned item
                    spawnedElement = method(prop);

                    // add spawnedElement to the master frag
                    $frag.append(spawnedElement.element);

                    // save a reference to spawnedElement
                    spawner.spawnedElements.push(spawnedElement.element);

                }
                else {
                    spawner.notSpawned.push(item);
                }
            }

            // spawn child elements from...
            // 'contents' or 'content' or 'children' or
            // a property matching the value of either 'contains' or 'kind'
            if (prop.contains || prop.contents || prop.content || prop.children || prop[prop.kind]) {
                prop.contents = prop[prop.contains] || prop.contents || prop.content || prop.children || prop[prop.kind];
                // if there's a 'target' property, put contents in there
                if (spawnedElement.target) {
                    $spawnedElement = $(spawnedElement.target);
                }
                else {
                    $spawnedElement = $(spawnedElement.element);
                }
                $spawnedElement.append(_spawn(prop.contents).get());
            }
            
            if (prop.after) {
                $frag.append(prop.after)
            }
            
            if (prop.before) {
                $frag.prepend(prop.before)
            }

        });

        _spawn.spawned = frag;
        
        _spawn.element = frag;

        _spawn.children = frag.children;

        _spawn.get = function(){
            return frag;
        };
        
        _spawn.getContents = function(){
            return $frag.contents();    
        };

        _spawn.render = function(element, empty){
            var $el = $$(element);
            // empty the container element before spawning?
            if (empty) {
                $el.empty();
            }
            $el.append(frag);
            return spawn;
        };

        _spawn.foo = '(spawn.foo)';

        return _spawn;

    };
    // ==================================================


    spawner.testSpawn = function(){
        var jsonUrl =
                XNAT.url.rootUrl('/page/admin/data/config/site-admin-sample-new.json');
        return $.getJSON({
            url: jsonUrl,
            success: function(data){
                spawner.spawn(data);
            }
        });
    };


    // this script has loaded
    spawner.loaded = true;

    return XNAT.spawner = spawner;

}));
