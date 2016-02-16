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

    var undefined;

    function isElement(it){
        return it.nodeType && it.nodeType === 1;
    }

    function isFragment(it){
        return it.nodeType && it.nodeType === 11;
    }

    // main factory function
    function spawn(tag, opts, inner){

        var el, parts, attrs, classArray=[], contents='', children,
            use$, $el, $opts={}, toDelete=['tag', 'tagName'];

        if (!isDefined(tag)) {
            return doc.createDocumentFragment();
        }

        // handle cases where 'tag' is already an element
        if (isElement(tag) || isFragment(tag)){
            return tag;
            //el = tag;
            //tag = el.tagName; // will this create a new element?
        }

        tag = typeof tag == 'string' ? tag.trim() : tag;

        if (arguments.length === 1){
            if (Array.isArray(tag)){
                children = tag;
                tag = '#html';
            }
            else if (tag === '!'){
                return doc.createDocumentFragment();
            }
            else if (typeof tag == 'string' && tag !== '' &&
                !(/^(#text|#html|!)|\|/gi.test(tag))
            ){
                return doc.createElement(tag||'span')
            }
        }

        // make sure opts is defined
        opts = opts || {};

        if (arguments.length === 3){
            contents = inner;
        }

        if (Array.isArray(opts) || typeof opts == 'string'){
            contents = opts;
        }

        if (isPlainObject(tag)){
            opts = tag;
            tag = firstDefined(opts.tag||opts.tagName||undefined, '#html');
        }

        // NOW make sure opts is an Object
        opts = getObject(opts);

        if (typeof contents == 'number'){
            contents = contents+'';
        }

        // combine 'content', 'contents', and 'children' respectively
        contents = [].concat(contents||[], opts.content||[], opts.contents||[], opts.children||[], children||[]);

        // add contents/children properties to list of properties to be deleted
        toDelete.push('content', 'contents', 'children');

        // trim outer white space and remove any trailing
        // semicolons or commas from 'tag'
        // (shortcut for adding attributes)
        parts = tag.trim().replace(/(;|,)$/,'').split('|');

        tag = parts[0].trim();

        if (el && (isElement(el) || isFragment(el))){
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
            if (tag === '' || /^(#text|#html|!)|\|/gi.test(tag)){
                el = doc.createDocumentFragment();
                //el.appendChild(doc.createTextNode(contents));
                //return el;
            }
            else {
                try {
                    el = doc.createElement(tag||'span');
                }
                catch(e){
                    if (console && console.log) console.log(e);
                    el = doc.createDocumentFragment();
                    el.appendChild(doc.createTextNode(tag||''));
                }
            }
        }

        // pass element attributes in 'tag' string, like:
        // spawn('a|id="foo-link";href="foo";class="bar"');
        // or (colons for separators, commas for delimeters, no quotes),:
        // spawn('input|type:checkbox,id:foo-ckbx');
        // allow ';' or ',' for attribute delimeter
        attrs = parts[1] ? parts[1].split(/;|,/) || null : null;

        forEach(attrs, function(att){
            if (!att) return;
            var sep = /:|=/; // allow ':' or '=' for key/value separator
            var quotes = /^('|")|('|")$/g;
            var key = att.split(sep)[0].trim();
            var val = (att.split(sep)[1]||'').trim().replace(quotes, '') || key;
            // add each attribute/property directly to DOM element
            //el[key] = val;
            el.setAttribute(key, val);
        });

        // any 'data-' attributes?
        if (opts.data) {
            forOwn(opts.data, function(name, val){
                el.setAttribute('data-'+name, val);
            });
        }

        // 'attr' property (object) to EXPLICITLY set attribute=value
        if (opts.attr){
            forOwn(opts.attr, function(name, val){
                el.setAttribute(name, val);
            });
        }

        toDelete.push('data', 'attr');

        //opts = isPlainObject(opts) ? opts : {};

        // Are we using jQuery later?
        // jQuery stuff needs to be in a property named $, jq, jQuery, or jquery
        opts.$ = opts.$ || opts.jq || opts.jQuery || opts.jquery;

        use$ = isDefined(opts.$||undefined);

        if (use$){
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
            if (classArray.indexOf(name) === -1){
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
        if (isPlainObject(opts)){
            forOwn(opts, function(attr, val){
                el[attr] = val;
            });
        }

        forEach(contents, function(part){
            try {
                if (typeof part == 'string'){
                    el.innerHTML += part;
                }
                else if (isElement(part) || isFragment(part)){
                    el.appendChild(part);
                }
                else {
                    el.appendChild(spawn.apply(null, [].concat(part)))
                }
            }
            catch(e){
                if (console && console.log) console.log(e);
            }
        });
        // that's it... 'contents' HAS to be one of the following
        // - text or HTML string
        // - array of spawn() compatible arrays: ['div', '{divOpts}']
        // - element or fragment

        // OPTIONALLY do some jQuery stuff, if specified (and available)
        if (use$ && isDefined(window.jQuery||undefined)){
            $el = window.jQuery(el);
            forOwn($opts, function(method, args){
                method = method.toLowerCase();
                // accept on/off event handlers with varying
                // number of arguments
                if (/^(on|off)$/.test(method)){
                    forOwn(args, function(evt, fn){
                        try {
                            $el[method].apply($el, [].concat(evt, fn));
                        }
                        catch(e){
                            if (console && console.log) console.log(e);
                        }
                    });
                    return;
                }
                $el[method].apply($el, [].concat(args))
            });
            //return $el;
        }
        
        return el;

    }

    // export to the global window object
    window.spawn = spawn;

    //
    // utility functions:
    //
    function isDefined(it){
        return typeof it != 'undefined';
    }

    // returns first defined argument
    // useful for retrieving 'falsey' values
    function firstDefined() {
        var undefined, i = -1;
        while (++i < arguments.length) {
            if (arguments[i] !== undefined) {
                return arguments[i];
            }
        }
        return undefined;
    }

    function isPlainObject( obj ){
        return Object.prototype.toString.call(obj) === '[object Object]';
    }

    function getObject(obj){
        return isPlainObject(obj) ? obj : {};
    }

    function forEach( arr, fn ){
        if (!arr) return;
        var i = -1, len = arr.length;
        while (++i < len){
            fn(arr[i], i);
        }
    }

    function forOwn( obj, fn ){
        if (!obj) return;
        var key;
        for ( key in obj ){
            if (obj.hasOwnProperty(key)) {
                fn(key, obj[key]);
            }
        }
    }

})(this, document);