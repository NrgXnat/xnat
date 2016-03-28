/*!
 * DOM element spawner with *optional* jQuery functionality
 *
 * EXAMPLES:
 * var p1 = spawn('p|id:p1', 'Text for paragraph 1.');
 * var div2 = spawn('div|class=div2', ['Text for div2.', p1]) // inserts text and puts p1 inside div2
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
        'disabled',
        'selected',
        'checked',
        'multiple'
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
        'hidden'
    ];

    // use these as a shortcut to create <input> elements:
    // spawn.html('input|text')
    //
    var inputTags = inputTypes.map(function(type){
        return 'input|' + type;
    });


    function parseAttrs(el, attrs){
        // allow ';' or ',' for attribute delimeter
        (attrs.split(/;|,/) || []).forEach(function(att, i){
            if (!att) return;
            // allow ':' or '=' for key/value separator
            var sep = /:|=/;
            // tolerate quotes around values
            var quotes = /^['"]+|['"]+$/g;
            var key = att.split(sep)[0].trim();
            var val = (att.split(sep)[1]||'').trim().replace(quotes, '') || key;
            // allow use of 'class', but (secretly) use 'className'
            if (key === 'class') {
                el.className = val;
                return;
            }
            el.setAttribute(key, val);
        });
    }


    function appendChildren(el, children, fn){
        [].concat(children).forEach(function(child){
            // each 'child' can be an array of
            // spawn arrays...
            if (Array.isArray(child)){
                el.appendChild(fn.apply(el, child));
            }
            // ...or an HTML string (or number)...
            else if (/(string|number)/.test(typeof child)){
                el.innerHTML += child;
            }
            // ...or 'appendable' nodes
            else {
                el.appendChild(child);
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
            skip = ['innerHTML', 'html', 'append',  // properties to skip later
                'classes', 'attr', 'data', 'fn'],
            errors = []; // collect errors

        if (tag === '!'){
            el = doc.createDocumentFragment();
            appendChildren(el, opts, spawn);
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
            // spawn('a|id="foo-link";href="foo";class="bar"');
            // or (colons for separators, commas for delimeters, no quotes),:
            // spawn('input|type:checkbox,id:foo-ckbx');
            parseAttrs(el, parts[0]||'');
        }

        if (!opts && !children){
            // return early for
            // basic element creation
            return el;
        }

        opts = opts || {};
        children = children || null;

        if (!isVoid){
            // if 'opts' is a string,
            // set el's innerHTML and
            // return the element
            if (/(string|number)/.test(typeof opts)){
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
            else if (/(string|number)/.test(typeof children)){
                el.innerHTML += children;
                children = null;
            }
        }

        // allow use of 'classes' property for classNames
        if (opts.className || opts.classes){
            el.className = [].concat(opts.className||[], opts.classes||[]).join(' ').trim();
        }

        // add attributes and properties to element
        forOwn(opts, function(prop, val){
            // only add if NOT in 'skip' array
            if (skip.indexOf(prop) === -1){
                el[prop] = val;
            }
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
            if (opts.append){
                try {
                    appendChildren(el, opts.append, spawn)
                }
                catch(e){
                    // write error to console
                    if (hasConsole) console.log(e);
                    //errors.push('Error appending: ' + e);
                }
            }
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

        // execute jQuery methods from the `$` property
        if (opts.$ && window.$){
            $el = $(el);
            forOwn(opts.$, function(method, args){
                $el[method].apply($el, [].concat(args));
            });
        }

        if (errors.length){
            if (hasConsole) console.log(errors);
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
        if (/(string|number)/.test(typeof opts)){
            el.innerHTML += (opts+'');
            return el;
        }
        else if (Array.isArray(opts)){
            content = opts;
            opts = {};
        }

        if (opts.classes || opts.className){
            el.className = [].concat(el.className||[], opts.classes||[], opts.className||[]).join(' ');
        }

        if (opts.html){
            opts.innerHTML += opts.html+'';
        }

        // add attributes and properties to element
        forOwn(opts, function(prop, val){
            if (/^(tag|html|classes|attr|data|$)$/.test(prop)) return;
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

        if (opts.$){
            $el = $(el);
            forOwn(opts.$, function(method, args){
                $el[method].apply($el, [].concat(args));
            });
            //delete opts.$;
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