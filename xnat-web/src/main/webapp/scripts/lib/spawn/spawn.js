/*!
 * DOM element spawner with *optional* jQuery functionality
 *
 * EXAMPLES:
 * var p1 = spawn('p#p1', 'Text for paragraph 1.');
 * var div2 = spawn('div.div2', ['Text for div2.', p1]) // inserts text and puts p1 inside div2
 * var ul1 = spawn('ul', [['li', 'Content for <li> 1.'], ['li', 'Content for the next <li>.']]);
 * div2.appendChild(ul1); // add ul1 to div2
 */

(function(window, doc){

    var undefined,
        UNDEFINED = 'undefined',
        hasConsole = console && console.log;

    // which HTML elements are
    // self-closing "void" elements?
    var voidElements = [
        'area',
        'base',
        'br',
        'col',
        'command',
        'embed',
        'hr',
        'img',
        'input',
        'keygen',
        'link',
        'meta',
        'param',
        'source',
        'track',
        'wbr'
    ];

    // boolean element attributes
    var boolAttrs = [
        'checked',
        'selected',
        'disabled',
        'hidden',
        'multiple',
        'readonly',
        'async',
        'autofocus',
        'autoplay',
        'controls',
        'defer',
        'ismap',
        'loop',
        'open',
        'required',
        'scoped'
    ];

    // which "type" values create an <input> element?
    var inputTypes = [
        'text',
        'password',
        'number',
        'email',
        'date',
        'url',
        'checkbox',
        'radio',
        'hidden',
        'file'
    ];

    // use these as a shortcut to create <input> elements:
    // spawn.html('input|text')
    //
    var inputTags = inputTypes.map(function(type){
        return 'input|' + type;
    }).concat(inputTypes.map(function(type){
        return 'input:' + type;
    }));

    // can the value be reasonably used as a string?
    function stringable(val){
        return /string|number|boolean/.test(typeof val);
    }


    function parseAttrs(el, attrs){
        // allow 'attrs' to be a string or array
        if (typeof attrs == 'string'){
            // use '|' for attribute delimiter
            attrs = attrs.split('|');
        }
        attrs.forEach(function(att, i){
            if (!att) return;
            // use '=' for key/value separator
            var sep = '=';
            // remove quotes around values
            var quotes = /^['"]+|['"]+$/g;
            var key = att.split(sep)[0].trim();
            var val = (att.split(sep)[1]||'').trim().replace(quotes, '') || key;
            // allow use of 'class', but (secretly) use 'className'
            if (key === 'class') {
                el.className = val;
                return;
            }
            try {
                el.setAttribute(key, val);
            }
            catch (e) {
                console.warn(e);
            }
        });
    }


    function hasClassName(el, className){
        var elClasses = (el.className||'').split(/\s+/); // existing classes
        return elClasses.indexOf(className.trim()) > -1;
    }


    // add new element class without destroying existing class
    function addClassName(el, newClass){
        var classes = (el.className||'').split(/\s+/); // existing classes
        var newClasses = [].concat(newClass||[]).join(' ').split(/\s+/);
        // don't add duplicate classes
        newClasses.forEach(function(cls){
            if (!cls) return;
            if (!hasClassName(el, cls)) {
                classes.push(cls);
            }
        });
        classes = classes.join(' ').trim();
        // set the className and return the string
        if (classes) {
            el.className = classes;
        }
        return classes;
    }


    // add new data object item to be used for [data-] attribute(s)
    function addDataObjects(el, attrs){
        el.data = el.data || {};
        forOwn(attrs, function(name, prop){
            el.data[name] = prop;
        });
        // set the data attributes and return the new data object
        return el.data;
    }


    function appendChildren(el, children, fn){
        [].concat(children).forEach(function(child){
            var appended = false;
            if (!child) return;
            // each 'child' can be an array of
            // spawn arrays...
            if (Array.isArray(child)){
                el.appendChild(fn.apply(el, child));
            }
            // ...or an HTML string (or number)...
            else if (stringable(child)){
                el.innerHTML += child;
            }
            // ...or a spawn() config object...
            else if (isPlainObject(child) && !child.jquery) {
                if (child.hasOwnProperty('tag')) {
                    child.html = child.html || child.content || '';
                    el.appendChild(fn.call(el, child.tag, child))
                }
                else {
                    try {
                        forOwn(child, function(key, val){
                            if (!appended && val.tag) {
                                val.html = val.html || val.content || '';
                                el.appendChild(fn.call(el, val.tag, val));
                                appended = true;
                            }
                        })
                    }
                    catch(e) {
                        console.log(e)
                    }
                }
            }
            // ...or 'appendable' nodes
            else {
                try {
                    el.appendChild(child.jquery ? child[0] : child);
                }
                catch (e) {
                    console.error(e);
                    console.log(child);
                    // try appending with jQuery
                    // if native .appendChild() fails
                    if (window.jQuery) {
                        jQuery(el).append(child);
                    }
                }
            }
        });
    }


    /**
     * Fairly lean and fast element spawner that's
     * a little more robust than spawn.element().
     * @param tag {String} element's tagName
     * @param [opts] {Object|Array|String} element
     *        properties/attributes -or- array of
     *        children -or- HTML string
     * @param [children] {Array|String}
     *        array of child element 'spawn' arg arrays
     *        or elements or HTML string
     * @returns {Element|*}
     */
    function spawn(tag, opts, children){

        var el, $el, parts, id, classes, tagParts, attrs, isVoid,
            // property names to skip later
            skip = [
                'innerHTML', 'html', 'attr', 'config',
                'kind', 'tag', 'tagName', 'on',
                'prepend', 'append', 'appendTo',
                'classes', 'className', 'addClass',
                'style', 'data', 'fn', 'label', 'done'
            ],
            errors = []; // collect errors

        // handle passing an array with the arguments
        if (Array.isArray(tag)){
            children = tag[2];
            opts = tag[1];
            tag = tag[0];
        }

        // handle passing a config object as the only argument
        if (!children && !opts && typeof tag !== 'string') {
            opts = tag;
            tag = null;
        }

        tag = tag || opts.tag || opts.tagName || 'span';

        if (tag === '!'){
            el = doc.createDocumentFragment();
            appendChildren(el, opts||'', spawn);
            return el;
        }

        try {
            parts = tag.split('|');
        }
        catch(e){
            if (hasConsole) console.log(e);
            parts = ['div.bogus'];
        }

        tag = parts.shift().trim();

        // also allow 'tag' to use selector syntax for id and class
        // spawn.plus('div#foo.bar', 'Foo');
        // -> '<div id="foo" class="bar">Foo</div>'
        // split classes first
        classes = tag.split('.');

        // first item will ALWAYS be tag name
        // wich MAY also have an id
        tag = classes.shift();

        tagParts = tag.split('#');

        tag = tagParts.shift();

        isVoid = voidElements.indexOf(tag) > -1;

        el = doc.createElement(tag||'div');

        if (tagParts.length){
            id = tagParts[0];
        }

        if (id){
            el.id = id;
        }

        if (classes.length){
            el.className = classes.join(' ').trim();
        }

        if (parts.length){
            // pass element attributes in 'tag' string, like:
            // spawn('a|id="foo-link"|href="foo"|class="bar"');
            // or (without quotes),:
            // spawn('input|type=checkbox|id=foo-ckbx');
            parseAttrs(el, parts);
        }

        if (!opts && !children){
            // return early for
            // basic element creation
            return el;
        }

        opts = opts || {};
        children = children || null;

        // make sure [type] attribute is applied FIRST for <input> elements
        if (/input/i.test(el.tagName) && opts.type) {
            el.type = opts.type;
        }

        if (!isVoid){
            // if 'opts' is a string,
            // set el's innerHTML and
            // return the element
            if (stringable(opts)){
                el.innerHTML += opts;
                return el;
            }

            // if 'children' arg is not present
            // and 'opts' is really an array
            if (!children && Array.isArray(opts)){
                children = opts;
                opts = {};
            }
            // or if 'children' is a string
            // set THAT to the innerHTML
            else if (stringable(children)){
                el.innerHTML += children;
                children = null;
            }
        }

        // allow use of 'classes' property for classNames
        addClassName(el, [].concat(opts.className||[], opts.classes||[], opts.addClass||[]));

        // IE *REALLY* hates method="PUT" on forms
        var methodPut = (opts.method && /put/i.test(opts.method));

        //////////////////////////////////////////////////
        // add attributes and properties to element
        forOwn(opts, function(prop, val){
            // only add if NOT in 'skip' array
            if (skip.indexOf(prop) === -1 || !methodPut){
                el[prop] = val;
            }
        });
        //////////////////////////////////////////////////


        // explicitly add element attributes
        if (opts.attr){
            forOwn(opts.attr, function(name, val){
                el.setAttribute(name, val);
            });
        }

        // explicitly add 'data-' attributes
        if (opts.data){
            forOwn(opts.data, function(name, val){
                setElementData(el, name, [].concat(val).join(''));
            });
        }

        // handle style object
        if (opts.style){
            // it's either a string
            if (typeof opts.style == 'string') {
                el.style = opts.style;
            }
            // or an object
            else {
                forOwn(opts.style, function(prop, val){
                    el.style[prop] = val;
                });
            }
        }

        // only add innerHTML and children for non-void elements
        if (!isVoid){

            // special handling of 'prepend' property
            if (opts.prepend){
                try {
                    appendChildren(el, opts.prepend, spawn)
                }
                catch(e){
                    // write error to console
                    if (hasConsole) console.log(e);
                    //errors.push('Error appending: ' + e);
                }
            }

            // add innerHTML now, if present
            el.innerHTML += (opts.innerHTML||opts.html||'');

            // append any spawned children
            if (children){
                try {
                    appendChildren(el, children, spawn)
                }
                catch(e){
                    // write error to console
                    if (hasConsole) console.log(e);
                    //errors.push('Error appending: ' + e);
                }
            }

            // special handling of 'append' property
            if (opts.append || opts.insert){
                try {
                    appendChildren(el, opts.append || opts.insert, spawn)
                }
                catch(e){
                    // write error to console
                    if (hasConsole) console.log(e);
                    //errors.push('Error appending: ' + e);
                }
            }
        }

        // shortcut for jQuery's .appendTo() method
        // set the 'appendTo' property to a selector,
        // element or jQuery object
        if (opts.appendTo && window.jQuery){
            // using brackets to clarify that we're
            // using the .appendTo() jQuery method
            jQuery(el)['appendTo'](opts.appendTo);
        }

        // execute element methods last...
        // attach object or array of methods
        // to 'fn' property - this can be an
        // array in case you want to run the
        // same method(s) more than once
        if (opts.fn){
            [].concat(opts.fn).forEach(function(fn){
                forOwn(fn, function(f, args){
                    el[f].apply(el, [].concat(args));
                });
            });
        }

        // function for recursively chaining
        // nested objects for jQuery methods
        function chain$($el, obj){
            forOwn(obj, function(method, args){
                if (isPlainObject(args)) {
                    chain$($el, args);
                }
                else {
                    $el[method].apply($el, [].concat(args));
                }
            });
        }

        // put event listeners in 'on' property as a 2-D array or array of objects
        // var events = [];
        // events.push([ 'click', function(){ alert('foo') } ]);
        // events.push({ mouseover: function(){ alert('bar') } }); // objects are preferred for clarity
        //
        // to handle a SINGLE event
        // on: [ 'click', '.child-element', function(){alert('click')} ]
        // --or--
        // on: { click: function(){alert('click')} }
        //
        // to handle MULTIPLE events
        // on: {
        //    click: function(){ alert('foo') },
        //    mouseover: function(){ console.log('bar') }
        // };
        // --or--
        // on: [
        //     ['mouseover', function(){ console.log('over') }],
        //     ['click', '.child-element', function(){ console.log('child-click') }],
        //     ['click', function(){ console.log('element-click') }]
        // ];

        // stuff that uses jQuery - event handlers and $ methods
        if ((opts.on || opts.$) && window.jQuery) {
            $el = jQuery(el);
            if (opts.on) {
                [].concat(opts.on).forEach(function(handler){
                    // handles ['click', function(){ alert('foo') }]
                    // and ['click', '.child-element', function(){}]
                    if (Array.isArray(handler)) {
                        $el.on.apply($el, handler);
                    }
                    // handles {mouseover: function(){ alert('bar') }}
                    else {
                        forOwn(handler, function(event, args){
                            $el.on.apply($el, [].concat(event, args));
                        });
                    }
                })
            }
            // execute jQuery methods from the '$' property
            if (opts.$){
                // use a 2-D array or array of objects
                // to call the same method MORE THAN ONCE
                // $: [['addClass', 'foo'], ['addClass', 'bar']]
                // $: [{ addClass: 'foo' }, { addClass: 'bar' }]
                if (Array.isArray(opts.$)){
                    forEach(opts.$, function(item){
                        if (Array.isArray(item)) {
                            $el[item[0]].apply($el, [].concat(item[1]))
                        }
                        else {
                            forOwn(item, function(method, args){
                                $el[method].apply($el, [].concat(args));
                            });
                        }
                    });
                }
                // use an object map to call seperate methods ONCE
                // $: { addClass: 'foo', fadeIn: 200 }
                else {
                    forOwn(opts.$, function(method, args){
                        $el[method].apply($el, [].concat(args));
                    });
                }
            }
        }

        var frag = document.createDocumentFragment();

        if (opts.before){
            appendChildren(frag, opts.before, spawn);
            frag.appendChild(el);
            el = frag;
        }

        if (opts.after){
            // don't append element twice if
            // there's 'before' AND 'after'
            if (!opts.before) {
                appendChildren(frag, opts.after, spawn);
                frag.appendChild(el);
            }
            el = frag;
        }

        if (errors.length){
            if (hasConsole) console.log(errors);
        }

        // alias for convenience
        el.html = el.outerHTML;

        el.element = el;

        el.get = function(callback){
            if (typeof callback == 'function') {
                callback.call(el);
            }
            return el;
        };

        if (typeof opts.done == 'function') {
            opts.done.call(el);
        }

        return el;

    }
    // aliases
    spawn.plus = spawn;
    spawn.lite = spawn;
    spawn.alt = spawn;

    /**
     * Leanest and fastest element spawner
     * @param tag {String|Object} tag name or config object
     * @param [opts] {Object|String|Array} config object, HTML content, or array of Elements
     * @param [content] {String|Array} HTML content or array of Elements
     * @returns {Element|*}
     */
    spawn.element = function spawnElement(tag, opts, content){

        var el, $el, id, classes, tagParts;

        if (typeof tag != 'string'){
            // if 'tag' isn't a string,
            // it MUST be a config object
            opts = tag;
            // and it MUST have a 'tag'
            // or 'tagName' property
            tag = opts.tag || opts.tagName || 'div';
        }

        // also allow 'tag' to use selector syntax for id and class
        // spawn.plus('div#foo.bar', 'Foo');
        // -> '<div id="foo" class="bar">Foo</div>'
        // split classes first
        classes = tag.split('.');

        // first item will ALWAYS be tag name
        // wich MAY also have an id
        tag = classes.shift();

        tagParts = tag.split('#');

        tag = tagParts[0];

        el = doc.createElement(tag||'div');

        if (tagParts.length > 1){
            id = tagParts[1];
        }

        if (id){
            el.id = id;
        }

        if (classes.length){
            el.className = classes.join(' ').trim();
        }

        // return early for basic usage
        if (!content && !opts && typeof tag == 'string') {
            return el;
        }

        // allow use of only 2 arguments
        // with the HTML text being the second
        if (stringable(opts)){
            el.innerHTML += (opts+'');
            return el;
        }
        else if (Array.isArray(opts)){
            content = opts;
            opts = {};
        }

        if (opts.classes || opts.className || opts.addClass){
            el.className = [].concat(el.className||[], opts.classes||[], opts.className||[], opts.addClass||[]).join(' ');
        }

        if (opts.html){
            opts.innerHTML += opts.html+'';
        }

        // add attributes and properties to element
        forOwn(opts, function(prop, val){
            if (/^(tag|html|classes|addClass|attr|append|appendChild|data|fn|$)$/.test(prop)) return;
            el[prop] = val;
        });

        // explicitly add element attributes
        if (opts.attr){
            forOwn(opts.attr, function(name, val){
                el.setAttribute(name, val);
            });
        }

        // explicitly add 'data-' attributes
        if (opts.data){
            forOwn(opts.data, function(name, val){
                setElementData(el, name, [].concat(val).join(''));
            });
        }

        // add any HTML content or child elements
        if (content){
            appendChildren(el, content, spawnElement);
        }

        // special handling of 'append' and 'appendChild'
        if (opts.appendChild){
            appendChildren(el, opts.appendChild, spawnElement);
        }
        if (opts.append){
            appendChildren(el, opts.append, spawnElement);
        }

        // call any element methods
        if (opts.fn){
            [].concat(opts.fn).forEach(function(fn){
                forOwn(fn, function(f, args){
                    el[f].apply(el, [].concat(args));
                });
            });
        }

        // execute jQuery methods from the `$` property
        if (opts.$ && window.jQuery){
            $el = jQuery(el);
            // use an array to call the same method more than once
            if (Array.isArray(opts.$)){
                forEach(opts.$, function(item){
                    $el[item[0]].apply($el, [].concat(item[1]))
                });
            }
            else {
                forOwn(opts.$, function(method, args){
                    $el[method].apply($el, [].concat(args));
                });
            }
        }

        return el;

    };

    /**
     * Spawn an HTML string using input parameters
     * Simple and fast but only generates HTML
     * @param tag {String} tag name for HTML element
     * @param [attrs] {Object|Array|String} element attributes
     * @param [content] {String|Array} string or array of strings for HTML content
     * @returns {String} HTML string
     */
    spawn.html = function spawnHTML(tag, attrs, content){
        // the 'html' method can be useful
        // for easily churning out plain old HTML
        // no event handlers or other methods

        tag = tag || 'div';
        attrs = attrs || null;
        content = content || [];

        var output = {};
        output.inner = '';
        output.attrs = '';

        if (inputTags.indexOf(tag) > -1){
            tag = tag.split('|');
            output.attrs += (' type="' + tag[1] +'"');
            tag = tag[0];
            // maybe set 'content' as the value?
            output.attrs += (' value="' + content + '"');
            // add content to [data-content] attribute?
            //output.attrs += (' data-content="' + content + '"');
        }

        var isVoid = voidElements.indexOf(tag) > -1;

        if (isVoid){
            output.open = '<' + tag;
            output.close = '>';
        }
        else {
            output.open = '<' + tag;
            output.inner = '>' + [].concat(content).map(function(child){
                    if (Array.isArray(child)){
                        return spawnHTML.apply(null, child)
                    }
                    else {
                        return child+''
                    }
                }).join('');
            output.close = '</' + tag + '>';
        }

        // process the attributes;
        if (attrs){
            if (isPlainObject(attrs)){
                forOwn(attrs, function(attr, val){
                    if (boolAttrs.indexOf(attr) > -1){
                        if (attr){
                            // boolean attributes don't need a value
                            output.attrs += (' ' + attr);
                        }
                    }
                    else {
                        output.attrs += (' ' + attr + '="' + val + '"');
                    }
                });
            }
            else {
                output.attrs += [''].concat(attrs).join(' ');
            }
        }

        return output.open + output.attrs + output.inner + output.close;

    };

    /**
     * Full-featured (but slowest) element spawner
     * @param tag {String|Object} tag name or config object
     * @param [opts] {Object|String|Array} config object, HTML string or array of 'appendable' items
     * @param [inner] {String|Array} HTML string or array of 'appendable' items
     * @returns {Element|*}
     */
    spawn.extreme = spawn.xt = function(tag, opts, inner){

        var el, parts, attrs, use$, $el, children,
            DIV        = doc.createElement('div'),
            classArray = [],
            contents   = '',
            $opts      = {},
            toDelete   = ['tag', 'tagName'];

        if (typeof tag == UNDEFINED) {
            return doc.createDocumentFragment();
        }

        // handle cases where 'tag' is already an element
        if (isElement(tag) || isFragment(tag)) {
            //return tag;
            el = tag;
            tag = el.tagName; // will this create a new element?
        }

        tag = typeof tag == 'string' ? tag.trim() : tag;

        if (arguments.length === 1) {
            if (Array.isArray(tag)) {
                children = tag;
                tag = '#html';
            }
            else if (tag === '!') {
                return doc.createDocumentFragment();
            }
            else if (typeof tag == 'string' && tag !== '' && !(/^(#text|#html|!)|\|/gi.test(tag))) {
                return doc.createElement(tag || 'span')
            }
        }

        // make sure opts is defined
        //opts = opts || {};

        if (arguments.length === 3) {
            contents = inner;
        }

        if (Array.isArray(opts) || typeof opts == 'string' || typeof opts == 'function') {
            contents = opts;
        }
        else {
            opts = opts || {};
        }

        if (isPlainObject(tag)) {
            opts = tag;
            tag = opts.tag || opts.tagName || '#html';
        }

        // NOW make sure opts is an Object
        opts = getObject(opts);

        if (typeof contents == 'number') {
            contents = contents + '';
        }

        if (typeof contents == 'function'){
            try {
                contents = contents();
            }
            catch(e){
                if (hasConsole) console.log(e);
                contents = [];
            }
        }

        // combine 'content', 'contents', and 'children' respectively
        contents = [].concat(contents||[], opts.content||[], opts.contents||[], opts.children||[], children||[]);

        // add contents/children properties to list of properties to be deleted
        toDelete.push('content', 'contents', 'children');

        // trim outer white space and remove any trailing
        // semicolons or commas from 'tag'
        // (shortcut for adding attributes)
        parts = tag.trim().replace(/(;|,)$/, '').split('|');

        tag = parts[0].trim();

        if (el && (isElement(el) || isFragment(el))) {
            // don't do anything if
            // el is already an element
        }
        else {
            // pass '!' as first argument to create fragment
            //if (tag === '!'){
            //    el = doc.createDocumentFragment();
            //    //el.appendChild(contents);
            //    if (!contents.length){
            //        return el;
            //    }
            //}
            // pass empty string '', '#text', or '#html' as first argument
            // to create a textNode
            if (tag === '' || /^(#text|#html|!)|\|/gi.test(tag)) {
                el = doc.createDocumentFragment();
                //el.appendChild(doc.createTextNode(contents));
                //return el;
            }
            else {
                try {
                    el = doc.createElement(tag || 'span');
                }
                catch(e) {
                    if (hasConsole) console.log(e);
                    el = doc.createDocumentFragment();
                    el.appendChild(doc.createTextNode(tag || ''));
                }
            }
        }

        // pass element attributes in 'tag' string, like:
        // spawn('a|id="foo-link";href="foo";class="bar"');
        // or (colons for separators, commas for delimeters, no quotes),:
        // spawn('input|type:checkbox,id:foo-ckbx');
        // allow ';' or ',' for attribute delimeter
        attrs = parts[1] ? parts[1].split(/;|,/) || [] : [];

        forEach(attrs, function(att){
            if (!att) return;
            var sep = /:|=/; // allow ':' or '=' for key/value separator
            var quotes = /^('|")|('|")$/g;
            var key = att.split(sep)[0].trim();
            var val = (att.split(sep)[1] || '').trim().replace(quotes, '') || key;
            // allow use of 'class', but (secretly) use 'className'
            if (key === 'class') {
                el.className = val;
                return;
            }
            el.setAttribute(key, val);
        });

        // 'attr' property (object) to EXPLICITLY set attribute=value
        opts.attr = opts.attr || opts.attrs || opts.attributes;
        if (opts.attr) {
            forOwn(opts.attr, function(name, val){
                // if a 'data' object snuck in 'attr'
                if (name.data) {
                    opts.data = name.data;
                    delete name.data;
                    return;
                }
                el.setAttribute(name, val);
            });
        }

        // any 'data-' attributes?
        if (opts.data) {
            forOwn(opts.data, function(name, val){
                setElementData(el, name, val);
            });
        }

        toDelete.push('data', 'attr');

        //opts = isPlainObject(opts) ? opts : {};

        // Are we using jQuery later?
        // jQuery stuff needs to be in a property named $, jq, jQuery, or jquery
        opts.$ = opts.$ || opts.jq || opts.jQuery || opts.jquery;

        use$ = isDefined(opts.$ || undefined);

        if (use$) {
            // copy to new object so we can delete from {opts}
            forOwn(opts.$, function(method, args){
                $opts[method] = args;
            });
        }

        // delete these before adding stuff to the element
        toDelete.push('$', 'jq', 'jQuery', 'jquery');

        // allow use of 'classes', 'classNames', 'className', and 'addClass'
        // as a space-separated string or array of strings
        opts.className = [].concat(opts.classes||[], opts.classNames||[], opts.className||[], opts.addClass||[]);

        // delete bogus 'class' properties later
        toDelete.push('classes', 'classNames', 'addClass');

        forEach(opts.className.join(' ').split(/\s+/), function(name){
            if (classArray.indexOf(name) === -1) {
                classArray.push(name)
            }
        });

        // apply sanitized className string back to opts.className
        opts.className = classArray.join(' ').trim();

        // if no className, delete property
        if (!opts.className) toDelete.push('className');

        // contents MUST be an array before being processed later
        // add 'prepend' and 'append' properties
        contents = [].concat(opts.prepend||[], contents, opts.append||[]);

        toDelete.push('prepend', 'append');

        // DELETE PROPERTIES THAT AREN'T VALID *ELEMENT* ATTRIBUTES OR PROPERTIES
        forEach(toDelete, function(prop){
            delete opts[prop];
        });

        // add remaining properties and attributes to element
        // (there should only be legal attributes left)
        if (isPlainObject(opts)) {
            forOwn(opts, function(attr, val){
                el[attr] = val;
            });
        }

        forEach(contents, function(part){
            try {
                if (typeof part == 'string') {
                    DIV = doc.createElement('div');
                    DIV.innerHTML = part;
                    while (DIV.firstChild){
                        el.appendChild(DIV.firstChild);
                    }
                }
                else if (isElement(part) || isFragment(part)) {
                    el.appendChild(part);
                }
                else {
                    el.appendChild(spawn.apply(null, [].concat(part)))
                }
            }
            catch(e) {
                if (hasConsole) console.log(e);
            }
        });
        // that's it... 'contents' HAS to be one of the following
        // - text or HTML string
        // - array of spawn() compatible arrays: ['div', '{divOpts}']
        // - element or fragment

        // OPTIONALLY do some jQuery stuff, if specified (and available)
        if (use$ && isDefined(window.jQuery || undefined)) {
            $el = window.jQuery(el);
            forOwn($opts, function(method, args){
                // accept on/off event handlers with varying
                // number of arguments
                if (/^(on|off)$/i.test(method)) {
                    forOwn(args, function(evt, fn){
                        try {
                            $el[method].apply($el, [].concat(evt, fn));
                        }
                        catch(e) {
                            if (hasConsole) console.log(e);
                        }
                    });
                    return;
                }
                $el[method].apply($el, [].concat(args))
            });
            //return $el;
        }

        return el;

    };

    // convenience alias
    spawn.fragment = spawn.lite.fragment = function(el){
        var frag = doc.createDocumentFragment();
        if (el){
            frag.appendChild(el);
        }
        return frag;
    };

    // test spawning speed
    spawn.speed = function(tag, count, method){
        tag = tag || 'div';
        count = count || 1000;
        var i = -1,
            time = Date.now(),
            span = spawn.element('span'),
            output = [],
            fn = method ? spawn[method] : spawn,
            el;
        while (++i < count){
            el = fn.apply(null, [].concat(tag));
            output.push(el);
            //if (typeof el == 'string'){
            //    span.innerHTML += el;
            //}
            //else {
            //    span.appendChild(el);
            //}
        }
        time = ((Date.now() - time)/1000);
        return {
            time: time + 's',
            output: output
        }
    };

    // compare performance of different spawn methods
    spawn.speed.compare = function(tag, count){
        tag = tag || 'div';
        count = count || 1000;
        return {
            spawn: spawn.speed(tag, count, '').time,
            element: spawn.speed(tag, count, 'element').time,
            plus: spawn.speed(tag, count, 'plus').time,
            html: spawn.speed(tag, count, 'html').time
        }
    };

    // export to the global window object
    window.spawn = spawn;

    // if jQuery is present, add it to the jQuery
    // prototype for chaining and the jQuery object
    // to get a jQuery-wrapped spawned element
    if (window.jQuery){

        jQuery.fn.spawn = function(){
            this.append(spawn.apply(this, arguments));
            return this;
        };

        jQuery.spawn = function(){
            return jQuery(spawn.apply(null, arguments));
        }

    }

    //
    // utility functions:
    //

    function isElement(it){
        return it.nodeType && it.nodeType === 1;
    }

    function isFragment(it){
        return it.nodeType && it.nodeType === 11;
    }

    function isDefined(it){
        return typeof it != 'undefined';
    }

    function isNumeric(num){
        return !Array.isArray(num) && (num - parseFloat(num) + 1) >= 0;
    }

    // returns first defined argument
    // useful for retrieving 'falsey' values
    function firstDefined(){
        var undefined, i = -1;
        while (++i < arguments.length) {
            if (arguments[i] !== undefined) {
                return arguments[i];
            }
        }
        return undefined;
    }

    function isPlainObject(obj){
        return Object.prototype.toString.call(obj) === '[object Object]';
    }

    function getObject(obj){
        return isPlainObject(obj) ? obj : {};
    }

    function forEach(arr, fn){
        if (!arr) return;
        var i = -1, len = arr.length;
        while (++i < len) {
            fn(arr[i], i);
        }
    }

    function forOwn(obj, fn){
        if (!obj) return;
        var keys = [],
            key;
        for (key in obj) {
            if (obj.hasOwnProperty(key)) {
                keys.push(key);
                if (typeof fn != 'function') continue;
                fn(key, obj[key]);
            }
        }
        return keys;
    }

    function setElementData(element, name, val){
        if (document.head && document.head.dataset) {
            name = camelize(name);
            element.dataset[name] = val;
        }
        else {
            name = hyphenize(name);
            element.setAttribute('data-' + name, val);
        }
    }

    function getElementData(element, name){
        if (document.head && document.head.dataset) {
            name = camelize(name);
            return realValue(element.dataset[name]);
        }
        else {
            name = hyphenize(name);
            return realValue(element.getAttribute('data-' + name));
        }
    }

    // returns real boolean for boolean string
    // returns real number for numeric string
    // returns null and undefined for those strings
    // (or returns original value if none of those)
    // useful for pulling 'real' values from
    // a string used in [data-] attributes
    function realValue(val, bool){
        var undefined;
        // only evaluate strings
        if (typeof val != 'string') return val;
        if (bool) {
            if (val === '0') {
                return false;
            }
            if (val === '1') {
                return true;
            }
        }
        if (isNumeric(val)) {
            return +val;
        }
        switch(val) {
            case 'true':
                return true;
            case 'false':
                return false;
            case 'undefined':
                return undefined;
            case 'null':
                return null;
            default:
                return val;
        }
    }

    function hyphenize(name){
        return name.replace(/([A-Z])/g, function(u){
            return '-' + u.toLowerCase();
        });
    }

    // set 'forceLower' === true (or omit argument)
    // to ensure *only* 'cameled' letters are uppercase
    function camelize(name, forceLower){
        if (firstDefined(forceLower, false)) {
            name = name.toLowerCase();
        }
        return name.replace(/\-./g, function(u){
            return u.substr(1).toUpperCase();
        });
    }

})(this, document);