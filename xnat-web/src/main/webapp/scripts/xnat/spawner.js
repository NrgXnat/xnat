/*
 * web: spawner.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

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

    spawner.counter = 0;

    // keep track of items that spawned
    spawner.spawnedElements = [];

    // keep track of items that didn't spawn
    spawner.notSpawned = [];

    function setRoot(url){
        url = url.replace(/^([*~.]\/*)/, '/');
        return XNAT.url.rootUrl(url)
    }

    // ==================================================
    // MAIN FUNCTION
    spawner.spawn = spawner.init = function spawnerInit(obj){

        var frag  = document.createDocumentFragment(),
            $frag = $(frag),
            callbacks = [],
            undefined;

        if (firstDefined(obj.kind || false, undefined) === null) {
            return null;
        }

        spawner.counter++;

        forOwn(obj, function(item, prop){

            var kind, element, method, spawnedElement, $spawnedElement;

            // 'prop' can be a new or existing DOM element
            if (prop instanceof Element) {
                element = prop;
                prop = {
                    kind: 'element',
                    element: element
                };
            }
            else {
                // save the config properties in a new object
                prop = cloneObject(prop);
            }

            // add this for proper handling in 'universal' widgets
            prop.spawnerElement = true;

            prop.element = prop.element || prop.config || {};

            // use 'name' property in element or config
            // then look for 'name' at object root
            // lastly use the object's own name
            prop.name = prop.name || item;

            // auto-generate IDs if not specified
            // I really don't like doing this here.
            prop.id = prop.id || prop.element.id || toDashed(prop.name);

            // accept 'kind' or 'type' property name
            // but 'kind' will take priority
            // with a fallback to a generic div
            kind = prop.kind || prop.type || null;

            // make 'href' 'src' and 'action' properties
            // start at the site root if starting with '/'
            if (prop.element.href) {
                prop.element.href = setRoot(prop.element.href)
            }
            if (prop.element.src) {
                prop.element.src = setRoot(prop.element.src)
            }
            if (prop.element.action) {
                prop.element.action = setRoot(prop.element.action)
            }

            // do a raw spawn() if 'kind' is 'element'
            // or if there's a tag property
            if (kind === 'element' || prop.tag || prop.element.tag) {

                // pass 'content' (not contentS) property to add
                // stuff directly to spawned element
                prop.content = prop.content || prop.children || '';

                try {
                    // if setting up Spawner elements in JS, allow a
                    // DOM element to be passed in the 'element' property
                    if (prop.element instanceof Element) {
                        spawnedElement = prop.element;
                    }
                    else {
                        spawnedElement = spawn(prop.tag || prop.element.tag || 'span', prop.element, prop.content);
                    }

                    // convert relative URIs for href, src, and action attributes
                    if (spawnedElement.href) {
                        spawnedElement.href = setRoot(spawnedElement.getAttribute('href'))
                    }
                    if (spawnedElement.src) {
                        spawnedElement.src = setRoot(spawnedElement.getAttribute('src'))
                    }
                    if (spawnedElement.action) {
                        spawnedElement.action = setRoot(spawnedElement.getAttribute('action'))
                    }

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
            else if (/^(text|html)$/i.test(kind)) {
                $frag.append(prop.content||prop.html||prop.text)
            }
            else {

                // check for a matching XNAT.ui method to call:
                method =

                    // XNAT.kind.init()
                    lookupObjectValue(XNAT, kind + '.init') ||

                    // XNAT.kind()
                    lookupObjectValue(XNAT, kind) ||

                    // XNAT.ui.kind.init()
                    lookupObjectValue(NAMESPACE + '.' + kind + '.init') ||

                    // XNAT.ui.kind()
                    lookupObjectValue(NAMESPACE + '.' + kind) ||

                    // XNAT.element.kind()
                    lookupObjectValue(XNAT, 'element.' + kind) ||

                    // kind.init()
                    lookupObjectValue(kind + '.init') ||

                    // kind()
                    lookupObjectValue(kind) ||

                    null;

                // only spawn elements with defined methods
                if (isFunction(method)) {

                    // 'spawnedElement' item will be an
                    // object with an 'element' property
                    // or a .get() method that will retrieve
                    // the spawned item
                    spawnedElement = method(prop);

                    // add spawnedElement to the master frag
                    $frag.append(spawnedElement.element||spawnedElement.get());

                    // save a reference to spawnedElement
                    spawner.spawnedElements.push(spawnedElement.element||spawnedElement.get());

                }
                else {
                    if (hasConsole) {
                        console.log('not spawned: ');
                        console.log(prop);
                    }
                    spawner.notSpawned.push(prop);
                }
            }

            // give up if no spawnedElement
            if (!spawnedElement) return;

            // spawn child elements from...
            // 'contents' or 'content' or 'children' or
            // a property matching the value of either 'contains' or 'kind'
            if (prop.contains || prop.contents || prop[prop.kind]) {
                prop.contents = prop[prop.contains] || prop.contents || prop[prop.kind];
                // if there's a 'target' property, put contents in there
                if (spawnedElement.target || spawnedElement.inner) {
                    $spawnedElement = $(spawnedElement.target || spawnedElement.inner);
                }
                else {
                    $spawnedElement = $(spawnedElement.element||spawnedElement.get());
                }

                // if a string, number, or boolean is passed as 'contents'
                // just append that as-is (as a string)
                if (stringable(prop.contents)) {
                    $spawnedElement.append(prop.contents+'');
                }
                else {
                    $spawnedElement.append(spawnerInit(prop.contents).get());
                }
            }

            // Treat 'before' and 'after' just like 'contents'
            // but insert the items 'before' or 'after' the main
            // spawned (outer) element. This may have unintended
            // consequences depending on the HTML structure of the
            // spawned widget that has things 'before' or 'after' it.

            if (prop.after) {
                if (stringable(prop.after) || Array.isArray(prop.after)) {
                    $frag.append(prop.after)
                }
                else if (isPlainObject(prop.after)) {
                    $frag.append(spawnerInit(prop.after).get())
                }
            }

            if (prop.before) {
                if (stringable(prop.before) || Array.isArray(prop.before)) {
                    $frag.prepend(prop.before)
                }
                else if (isPlainObject(prop.before)) {
                    $frag.prepend(spawnerInit(prop.before).get())
                }
            }

            // if there's a .load() method, fire that
            if (isFunction(spawnedElement.load||null)) {
                spawnedElement.load.call(spawnedElement);
            }

            // if there's an .onRender() method, queue it
            if (isFunction(spawnedElement.onRender||null)) {
                callbacks.push({
                    onRender: spawnedElement.onRender,
                    spawned: spawnedElement,
                    $element: $spawnedElement
                });
            }

        });

        spawnerInit.spawned = frag;

        spawnerInit.element = frag;

        spawnerInit.children = frag.children;

        spawnerInit.get = function(){
            return frag;
        };

        spawnerInit.getContents = function(){
            return $frag.contents();
        };

        spawnerInit.done = function(callback){
            if (isFunction(callback)) {
                callback(spawnerInit, frag, $frag)
            }
            return spawnerInit;
        };

        spawnerInit.render = function(container, wait, callback){

            var $container = $$(container).hide();

            wait = wait !== undefined ? wait : 100;

            $container.append(frag);
            $container.fadeIn(wait);

            // fire collected callbacks
            callbacks.forEach(function(obj){
                try {
                    obj.onRender.call(obj.spawned, obj.$element, obj);
                }
                catch(e) {
                    console.log(e)
                }
            });

            setTimeout(function(){
                if (isFunction(callback)) {
                    callback.call(spawnerInit, obj);
                }
            }, wait/2);

            return spawnerInit;

        };

        spawnerInit.foo = '(spawn.foo)';

        return spawnerInit;

    };
    // ==================================================


    // given a container and spawner object,
    // spawn the elements into the container
    spawner.render = function(container, obj){
        return spawner.spawn(obj).render($$(container))
    };


    // spawn elements with only the namespace/element path,
    // container/selector, and an optional AJAX config object
    // XNAT.spawner.resolve('siteAdmin/adminPage').render('#page-container');
    // or assign it to a variable and render later.
    // var adminPage = XNAT.spawner.resolve('siteAdmin/adminPage');
    // adminPage.render('#page-container');
    // and methods from the AJAX request will be in .get.done(), .get.fail(), etc.
    spawner.resolve = function(nsPath, opts) {

            // you can pass a config object as the only argument
        opts = cloneObject(firstDefined(opts, getObject(nsPath)));

        var url = opts.url || XNAT.url.restUrl('/xapi/spawner/resolve/' + nsPath);

        var request = XNAT.xhr.getJSON(extend(true, {
            url: url
        }, opts));

        function spawnRender(){
            var renderArgs = arguments;
            return request.done(function(obj){
                    spawner.spawn(obj).render.apply(request, renderArgs);
                return request;
            });
        }

        return {
            // returned 'ok' method only fires with 200 response
            // and returns with 'this' as Spawner instance
            ok: function(callback){
                return request.done(function(obj, txtStatus, xhr){
                    var spawnerInstance = spawner.spawn(obj);
                    if (xhr.status === 200) {
                        if (isFunction(callback)){
                            callback.call(spawnerInstance, obj, txtStatus, xhr)
                        }
                    }
                })
            },
            done: request.done,
            fail: request.fail,
            always: request.always,
            spawn: spawnRender,
            render: spawnRender
        };

    };

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
