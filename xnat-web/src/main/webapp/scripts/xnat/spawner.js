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
        NAMESPACE = 'XNAT.ui',
        $ = jQuery || null, // check and localize
        hasConsole = console && console.log;

    XNAT.ui = 
        getObject(XNAT.ui||{});
    XNAT.spawner = spawner = 
        getObject(XNAT.spawner||{});

    // keep track of items that spawned
    spawner.spawnedElements = [];
    
    // keep track of items that didn't spawn
    spawner.notSpawned = [];


    // ==================================================
    // MAIN FUNCTION
    spawner.spawn = function SPAWN(obj){
        
        var frag = document.createDocumentFragment(),
            $frag = $(frag);

        forOwn(obj, function(item, o){

            var kind, method, spawned,
                // save the config properties in a new object
                config = o.config || o.element || {};

            // use 'name' property in element or config
            // then look for 'name' at object root
            // lastly use the object name
            config.name = config.name || o.name || item;

            // accept 'kind' or 'type' property name
            // but 'kind' will take priority
            // with a fallback to a generic div
            kind = o.kind || o.type || 'div.spawned';

            // do a raw spawn() if 'kind' is 'element'
            // or if there's a tag property
            if (kind === 'element' || o.tag){
                try {
                    spawned = window.spawn(o.tag||config.tag||'div', config);
                    // jQuery's .append() method is
                    // MUCH more robust and forgiving
                    // than element.appendChild()
                    $frag.append(spawned);
                    spawner.spawnedElements.push(spawned);
                }
                catch (e) {
                    if (hasConsole) console.log(e);
                    spawner.notSpawned.push(config);
                }
            }
            else {
                // check for a matching XNAT.ui method to call:
                // XNAT.ui.kind()
                method = eval(NAMESPACE+'.'+kind);

                // only spawn elements with defined methods
                if (isFunction(method)) {

                    // 'spawned' item will be an HTML element
                    spawned = method(config);

                    // add the spawned element to the master frag
                    $frag.append(spawned);

                    // save a reference to the spawned element
                    spawner.spawnedElements.push(spawned);

                }
                else {
                    spawner.notSpawned.push(item);
                }
            }

            // spawn child elements from 'contents'
            if (o.contents || o.content || o.children) {
                o.contents = o.contents || o.content || o.children;
                $frag.append(SPAWN(o.contents).spawned);
            }

        });

        SPAWN.spawned = frag;

        SPAWN.children = $frag.contents();

        SPAWN.get = function(){
            return frag;
        };

        SPAWN.render = function(element, empty){
            var $el = $$(element);
            // empty the container element before spawning?
            if (empty){
                $el.empty();
            }
            $el.append(frag);
            return spawn;
        };

        SPAWN.foo = '(spawn.foo)';

        return SPAWN;

    };
    // ==================================================


    spawner.testSpawn = function(){
        var jsonUrl = 
                XNAT.url.rootUrl('/page/admin/data/config/site-admin-sample-new.json');
        return $.getJSON({
            url: jsonUrl,
            success: function(data){
                spawner.spawn(data);
                console.log('spawnedElements:');
                console.log(spawner.spawnedElements);
                console.log('notSpawned:');
                console.log(spawner.notSpawned);
            }
        });
    };


    // this script has loaded
    spawner.loaded = true;
    
    return XNAT.spawner = spawner;

}));
