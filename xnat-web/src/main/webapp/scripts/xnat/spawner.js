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

    var undef,
        ui, spawner,
        NAMESPACE  = 'XNAT.ui',
        $          = jQuery || null, // check and localize
        hasConsole = console && console.log;

    var appendCount = 0;

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
        url = (url+'').trim().replace(/^([*~.]*\/+)/, '/');
        // only modify 'url' values that start with '/'
        if (/^\//.test(url)) {
            return /^\/data\//.test(url) ? XNAT.url.csrfUrl(url) : XNAT.url.rootUrl(url);
        } else {
            return url;
        }
    }

    // process URL for spawner elements
    function spawnerUrl(url){
        var _url = strReplace(url);
        return setRoot(_url);
    }

    // add [prefix] and [suffix] to id
    function rewriteId(el0, prefix, suffix){
        var oldId = el0.id.split('-X-')[1] || el0.id; // watch out for previously rewritten ids
        var newId = (prefix || randomID('idx', false)) + '-X-' + oldId + (suffix ? ('-X-' + suffix) : '');
        return (el0.id = newId);
    }

    // extract original id
    function revertId(el0){
        return (el0.id = el0.id.split('-X-')[1] || el0.id);
    }

    // ==================================================
    // MAIN FUNCTION
    spawner.spawn = spawner.init = function spawneri(obj){

        var frag  = document.createDocumentFragment(),
            $frag = $(frag),
            template$, template$$, tmplId,
            callbacks = [],
            config = getObject(obj),
            undef;

        try {
            if (!obj || firstDefined(obj.kind || false, undef) === null) {
                return null;
            }
        }
        catch (e){
            console.error(e);
        }

        spawner.counter++;

        // tolerate malformed element config (not using a top-level 'parent' property)
        // element config CANNOT be named any of the following:
        var reserved = ['kind', 'tag', 'html', 'before', 'after', 'template', 'page', 'type'];
        var renamed = false;

        // this should fix both malformed element config objects and configs using 'reserved' names
        if (obj.kind || obj.tag || obj.html || obj.before || obj.after || obj.template || obj.page || obj.type) {
            // tolerate elements named with 'reserved' names
            forEachCheck(reserved, function(name){
                if (renamed) return false;
                if (config.hasOwnProperty(name)) {
                    // save element name to the 'name' property if not defined in the config
                    config[name].name = config[name].name || name;
                    config[randomID('spawnerE', false)] = config[name];
                    delete config[name];
                    renamed = true;
                }
            });
            // config[randomID('spawnerE', false)] = obj;
        }

        forOwn(config, function(item, prop){

            // prevent infinite recursion here
            if (config === prop) {
                console.warn('Malformed Spawner element:');
                console.warn(prop);
                return;
            }

            var show, hide, kind, content, element, method, spawnedElement, $spawnedElement, _spwnd;

            // accept 'kind' or 'type' property name
            // but 'kind' will take priority
            // with a fallback to a generic div
            kind = prop.kind || prop.type || null;

            // spawn the item?
            if (prop.show !== undef) {
                show = strReplace(prop.show);
                if (show === prop.show) {
                    XNAT.parse(prop.show).success(function(result){
                        show = result;
                    });
                }
            }
            if (prop.hide !== undef) {
                hide = strReplace(prop.hide);
                if (hide === prop.hide) {
                    XNAT.parse(prop.hide).success(function(result){
                        hide = result;
                    });
                }
                show = !hide;
            }


            // if 'show' evaluates to false, return early (and don't render)
            if (/^false$/i.test(show)) return;


            try {

                if (kind === 'page' || prop.page) {
                    if (prop.url || prop.href || prop.page) {
                        prop.url = XNAT.url.parse(prop.url || prop.href || prop.page);
                        if (prop.container) {
                            return $$(prop.container).load(prop.url)
                        }
                        else {
                            return $frag.load(prop.url);
                        }
                    }
                    return;
                }

                // pick up an existing element
                if (prop.template !== undef) {
                    template$ = $$(prop.template);
                    // append and return if found
                    if (template$.length) {
                        // rewrite template ids to prevent conflicts
                        // and return the custom id
                        tmplId = randomID('tmplx', false);
                        template$.find('[id]').each(function(){
                            rewriteId(this, tmplId)
                        });
                        // clones the template...
                        // http://api.jquery.com/clone/
                        // BEWARE DUPLICATE ID ATTRIBUTE VALUES
                        template$$ = template$.clone(true);
                        // modify ids of all elements with an id
                        $frag.append(template$$.hidden(false).removeClass('html-template'));
                        template$$.find('[id]').each(function(){
                            revertId(this);
                        });
                        return;
                    }
                }

                if (stringable(prop)) {
                    $frag.append(prop);
                    return;
                }

                if (Array.isArray(prop)) {
                    prop.forEach(function(p){
                        if (stringable(p)) {
                            $frag.append(p);
                            return;
                        }
                        if (Array.isArray(p)) {
                            $frag.append(spawn.apply(null, p));
                        }
                    });
                    return;
                }

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

                if (prop.url) {
                    prop.url = spawnerUrl(prop.url)
                }
                if (prop.action) {
                    prop.action = spawnerUrl(prop.action)
                }
                if (isString(prop.load) && !/^([#!?][?])/.test(prop.load)) {
                    prop.load = spawnerUrl(prop.load)
                }

                // make 'href' 'src' and 'action' properties
                // start at the site root if starting with '/'
                if (prop.element.href) {
                    prop.element.href = spawnerUrl(prop.element.href)
                }
                if (prop.element.src) {
                    prop.element.src = spawnerUrl(prop.element.src)
                }
                if (prop.element.action) {
                    prop.element.action = spawnerUrl(prop.element.action)
                }

                // do a raw spawn() if 'kind' is 'element'
                // or if there's a tag property
                if (kind === 'element' || prop.tag || prop.element.tag) {

                    // pass 'content' (not contentS) property to add
                    // stuff directly to spawned element
                    prop.content = prop.content || prop.children || prop.element.content || '';

                    prop.tag = prop.tag || prop.element.tag || 'span';

                    // prevent duplicate elements
                    delete prop.element.tag;
                    delete prop.element.content;

                    try {
                        // if setting up Spawner elements in JS, allow a
                        // DOM element to be passed in the 'element' property
                        if (prop.element instanceof Element) {
                            spawnedElement = prop.element;
                        }
                        else {
                            spawnedElement = spawn(prop.tag, prop.element, [].concat(prop.content));
                        }

                        // convert relative URIs for href, src, and action attributes
                        if (spawnedElement.href) {
                            spawnedElement.href = spawnerUrl(spawnedElement.getAttribute('href'))
                        }
                        if (spawnedElement.src) {
                            spawnedElement.src = spawnerUrl(spawnedElement.getAttribute('src'))
                        }
                        if (spawnedElement.action) {
                            spawnedElement.action = spawnerUrl(spawnedElement.getAttribute('action'))
                        }

                        // jQuery's .append() method is
                        // MUCH more robust and forgiving
                        // than element.appendChild()
                        $frag.append(spawnedElement);
                        spawner.spawnedElements.push(spawnedElement);
                    }
                    catch(e) {
                        if (hasConsole) console.log(e);
                        spawner.notSpawned.push(prop);
                    }
                }
                else if (/^(text|html)$/i.test(kind)) {
                    $frag.append(prop.content || prop.html || prop.text)
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

                        // prefer .get() method over .element property
                        _spwnd = isFunction(spawnedElement.get) ? spawnedElement.get() : spawnedElement.element;

                        // add spawnedElement to the master frag
                        $frag.append(_spwnd);

                        // save a reference to spawnedElement
                        spawner.spawnedElements.push(_spwnd);

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
                // 'contents' or 'children' or
                // a property matching the value of either 'contains' or 'kind'
                if (prop.contains || prop.contents || prop[prop.kind]) {
                    prop.contents = prop[prop.contains] || prop.contents || prop[prop.kind];
                    // if there's a 'target' property, put contents in there
                    if (spawnedElement.target || spawnedElement.inner) {
                        $spawnedElement = $(spawnedElement.target || spawnedElement.inner);
                    }
                    else {
                        $spawnedElement = isFunction(spawnedElement.get) ? $(spawnedElement.get()) : $(spawnedElement.element);
                    }

                    // if a string, number, or boolean is passed as 'contents'
                    // just append that as-is (as a string)
                    if (stringable(prop.contents)) {
                        $spawnedElement.append(prop.contents + '');
                    }
                    else {
                        $spawnedElement.append(spawneri(prop.contents).get());
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
                        $frag.append(spawneri(prop.after).get())
                    }
                }

                if (prop.before) {
                    if (stringable(prop.before) || Array.isArray(prop.before)) {
                        $frag.prepend(prop.before)
                    }
                    else if (isPlainObject(prop.before)) {
                        $frag.prepend(spawneri(prop.before).get())
                    }
                }

                // if there's a .load() method, fire that
                if (isFunction(spawnedElement.load || null)) {
                    // console.log('spawnedElement.load');
                    spawnedElement.load.call(spawnedElement);
                }

                // console.log('load / onRender');

                // if there's an .onRender() method, queue it
                if (isFunction(spawnedElement.onRender || null)) {
                    console.log('spawnedElement.onRender');
                    callbacks.push(function(){
                        return spawnedElement.onRender.call(spawnedElement);
                    });
                }

            }
            catch(e) {
                console.error(e);
            }

        });

        spawneri.spawned = frag;

        spawneri.element = frag;

        spawneri.children = frag.children;

        spawneri.get = function(callback){
            // allow transform callback when calling .get()
            if (isFunction(callback)) {
                callback.call(spawneri, frag, $frag)
            }
            return frag;
        };
        spawneri.getSpawned = spawneri.get;

        spawneri.get$ = function(){
            return $frag;
        };

        spawneri.getContents = function(){
            return $frag.contents();
        };

        spawneri.done = function(callback){
            // console.log('spawneri.done');
            if (isFunction(callback)) {
                // console.log('spawneri.done callback');
                callback.call(spawneri, frag, $frag)
            }
            return spawneri;
        };

        spawneri.render = function(container, callback){

            console.log('spawneri.render');

            var $container = $$(container).hide().append(frag);

            window.setTimeout(function(){

                $container.fadeIn(100, function() {
                    if (isFunction(callback)) {
                        callback.call(spawneri, obj);
                    }
                });

                // fire collected callbacks
                callbacks.forEach(function(fn){
                    try {
                        fn.call(spawneri, obj);
                    }
                    catch(e) {
                        console.log(e)
                    }
                });

            }, 1);

            // $container.fadeIn(wait, function(){
            //     console.log('append ' + (appendCount+=1));
            //     $frag.fadeIn(wait/2, function(){
            //         console.log('$frag fadeIn');
            //         // fire collected callbacks
            //         callbacks.forEach(function(fn){
            //             console.log('callbacks.forEach');
            //             // setTimeout(function(){
            //             try {
            //                 fn.call(spawneri);
            //             }
            //             catch (e) {
            //                 console.log(e)
            //             }
            //             // }, wait * 3)
            //         });
            //     });
            //     // setTimeout(function(){
            //     if (isFunction(callback)) {
            //         console.log('render callback');
            //         callback.call(spawneri, obj);
            //     }
            //     // }, wait * 2);
            // });

            return spawneri;

        };

        spawneri.foo = '(spawn.foo)';

        return spawneri;

    };
    // ==================================================


    // given a container and spawner object,
    // spawn the elements into the container
    spawner.render = function(container, obj){
        return spawner.spawn(obj).render($$(container))
    };


    // only CHECK for the presence of an element - do not render
    spawner.check = function(nsPath){
        var url = XNAT.url.restUrl('/xapi/spawner/resolve/' + nsPath);
        var request = XNAT.xhr.get(url);
        // add an 'ok' method to the request object itself
        request.ok = function(callback){
            request.done(function(obj, txtStatus, xhr){
                // only fire success for 200 status
                if (xhr.status === 200) {
                    if (isFunction(callback)){
                        callback.apply(request, arguments)
                    }
                }
                else {
                    if (jsdebug) console.log(xhr);
                }
            })
        };
        return request;
    };


    // spawn elements with only the namespace/element path,
    // container/selector, and an optional AJAX config object
    // XNAT.spawner.resolve('siteAdmin/root').render('#page-container');
    // or assign it to a variable and render later.
    // var adminPage = XNAT.spawner.resolve('siteAdmin/root');
    // adminPage.render('#page-container');
    // and methods from the AJAX request will be in .get.done(), .get.fail(), etc.
    spawner.resolve = function(nsPath, opts){

        // you can pass a config object as the only argument
        opts = cloneObject(firstDefined(opts, getObject(nsPath)));

        // object to return
        var resolve = {};

        var url = opts.url || XNAT.url.restUrl('/xapi/spawner/resolve/' + nsPath);

        var request = XNAT.xhr.getJSON(extend(true, {
            url: url
        }, opts));

        resolve.url = url;
        resolve.request = request;

        // save reference to generated Spawner instance
        var spawneri;

        function spawnRender(){
            // console.log('spawnRender');
            var args = arguments;
            return request.done(function(data){
                spawneri = spawneri || spawner.spawn(data);
                spawneri.render.apply(request, args);
                return spawneri;
            });
        }

        // returned 'ok' method only fires with 200 response
        // and returns with Spawner instance as 'this'
        resolve.ok = function(success, failure){
            if (jsdebug) console.log('spawner.resolve().ok()');
            request.done(function(data, txtStatus, xhr){
                spawneri = spawneri || spawner.spawn(data);
                if (xhr.status === 200) {
                    if (isFunction(success)) {
                        success.call(spawneri, data, txtStatus, xhr)
                    }
                }
                else {
                    // try something else if element isn't present
                    if (isFunction(failure)) {
                        failure.call(spawneri, data, txtStatus, xhr)
                    }
                }
            });
            return resolve;
        };

        resolve.done = function(callback){
            if (isFunction(callback)) {
                request.done(callback);
            }
            return resolve;
        };

        resolve.fail = function(callback){
            if (jsdebug) console.log('spawner.resolve().fail()');
            request.done(function(data, txtStatus, xhr){
                spawneri = spawneri || spawner.spawn(data);
                if (xhr.status !== 200) {
                    if (isFunction(callback)) {
                        callback.apply(spawneri, arguments)
                    }
                }
            });
            return resolve;
        };

        resolve.always = function(callback){
            if (isFunction(callback)) {
                request.always(callback);
            }
            return resolve;
        };

        resolve.spawn = function(){
            resolve.request = spawnRender.apply(resolve, arguments);
            return resolve;
        };

        resolve.render = function(container, callback){
            resolve.ok(function(data){
                spawneri = spawneri || spawner.spawn(data);
                spawneri.render(container);
                if (isFunction(callback)) {
                    try {
                        spawneri.done.call(resolve, callback)
                    }
                    catch(e) {
                        if (jsdebug) console.error(e);
                    }
                }
            });
            return resolve;
        };

        return resolve;

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
