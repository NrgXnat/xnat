/*!
 * Create HTML elements from a config object
 * https://gist.github.com/Error601/7a86fa6e34fbc98b31a6
 */

if (typeof jQuery == 'undefined') {
    throw  new Error('jquery.spawn.js requires jQuery');
}

(function($, undefined){

    function isElement(it){
        return it.nodeType && it.nodeType === 1;
    }

    function isFragment(it){
        return it.nodeType && it.nodeType === 11;
    }

    // returns first defined argument
    // useful for retrieving 'falsey' values
    function firstDefined() {
        var i = -1;
        while (++i < arguments.length) {
            if (arguments[i] !== undefined) {
                return arguments[i];
            }
        }
        return undefined;
    }

    // attach to global jQuery $ object
    // (since it uses jQuery)
    // usage:
    // var $div1 = $.spawn('div|id:div1', {addClass: 'foo'}, {name: 'bar'}, [$p1, $p2, $p3]);
    // var $div2 = $.spawn('div'), {}, {id:'div2'}, "Div2's HTML content")
    /**
     * Create a jQuery-wrapped DOM object
     * @param tag {String} HTML tag name
     * @param [opts] {Object|String} jQuery methods / child element(s) / HTML
     * @param [attr] {Object} native DOM methods, properties, and attributes
     * @param [content] {String|Array|Object|Element} child element(s) / HTML
     * @returns {*|HTMLElement}
     */
    $.spawn = function(tag, opts, attr, content){

        var el, $el, parts, _opts={}, attrs, _attr={};

        // return jQuery-wrapped fragment
        // if called with no arguments
        if (typeof tag === 'undefined') {
            return $(document.createDocumentFragment());
        }

        // stop and return if it's
        // already a jQuery object
        if (tag && tag.jquery) {
            return tag;
        }

        if ($.isPlainObject(tag)) {
            opts = tag;
            tag = firstDefined(opts.tag||undefined, '');
        }

        // 'tag' could be an array of child content in recursive spawns
        if ($.isArray(tag)){
            _opts.children = tag;
        }

        // trim outer white space and remove any trailing semicolons or commas
        parts = tag.trim().replace(/(;|,)$/,'').split('|');

        tag = parts[0].trim();

        // pass empty string or #text as
        // first argument to create fragment
        if (tag === '' || /^(#text|#html)/i.test(tag)){
            el = document.createDocumentFragment();
            // the second argument MUST be
            // text or HTML content
            el.innerHTML = opts;
        }
        else {
            try {
                el = document.createElement(tag||'span');
            }
            catch(e){
                if (console && console.log) console.log(e);
                el = document.createDocumentFragment();
                el = el.appendChild(document.createTextNode(tag||''));
            }
        }

        // pass element attributes in 'tag' string, like:
        // $.spawn('a|id="foo-link";href="foo";class="bar"');
        // or (colons for separators, commas for delimeters, no quotes),:
        // $.spawn('input|type:checkbox,id:foo-ckbx');

        attrs = (parts[1]||'').split(/;|,/) || []; // allow ';' or ',' for attribute delimeter

        $.each(attrs, function(i, att){
            if (!att) return;
            var sep = /:|=/; // allow ':' or '=' for key/value separator
            var quotes = /^('|")|('|")$/g;
            var key = att.split(sep)[0].trim();
            var val = (att.split(sep)[1]||'').trim().replace(quotes, '') || key;
            // allow use of 'class', but (secretly) use 'className'
            //if (key === 'class') { key = 'className' }
            // add each attribute/property directly to DOM element
            //el[key] = val;
            el.setAttribute(key, val);
        });

        $el = $(el);

        if (attr) {
            if ($.isPlainObject(attr)){
                // pull out the 'prop' properties
                if (attr.prop){
                    $el.prop.apply($el, [].concat(attr.prop));
                    delete attr.prop;
                }
                $.each(attr, function(name, prop){
                    el[name] = prop;
                });
            }
            else {
                try {
                    // could be an object map of multiple attributes
                    // or could be an array for a single attribute - ['name','foo']
                    $el.attr.apply($el, [].concat(attr));
                }
                catch(e){
                    if (console && console.log) console.log(e);
                }
            }
        }

        if (typeof opts == 'undefined') {
            return $el;
        }

        opts = opts || {};

        // just append an HTML string, jQuery object, element, or fragment
        if (typeof opts == 'string' || typeof opts == 'function' || opts.jquery || isElement(opts) || isFragment(opts)){
            content = opts;
            //return $el.append(opts);
        }
        // if 'opts' is an array, they
        // will be child elements
        else if ($.isArray(opts)) {
            _opts.children = opts;
        }
        // otherwise it's a config object
        else if ($.isPlainObject(opts)) {
            _opts = $.extend(true, {}, opts);
        }

        // a fourth argument can contain additional content
        if (content){
            if (typeof content == 'function'){
                try {
                    content = content();
                }
                catch(e){
                    if (console && console.log) console.log(e);
                    content = [];
                }
            }
            _opts.content = [].concat(_opts.content||[], content);
        }

        $.each(_opts, function(prop, val){

            prop = prop.trim();

            // skip 'tag' property
            if (prop === 'tag') return;

            // special handling for 'id' property
            if (prop === 'id'){
                el.id = val;
                return;
            }

            // special handling for 'class', 'className', or 'classes' property
            // accepts a className string or array of separate class names
            if (/^(class|classes|className|classNames)$/.test(prop)){
                el.className = [].concat(val).join(' ').replace(/\s+/g, ' ').trim();
                return;
            }

            // 'element' (or 'el') property contains
            // items to attach natively
            // to the new element
            // (without jQuery)
            if (/^(element|el)$/.test(prop)){
                $.each([].concat(val), function(name, value){
                    el[name] = value;
                });
                return;
            }

            // 'children' property is
            // an array of elements
            // to be spawned
            if (/^(children|content|contents)$/.test(prop)) {
                val = [].concat(val);
                if (val.length === 1){
                    return $el.append(val);
                }
                $.each(val, function(i, child){
                    try {
                        // recursively append spawns as needed
                        //$el.append(child); // each child must be an 'appendable' item
                        //$.spawn(child).appendTo($el);
                        $el.append($.spawn.apply(null, [].concat(child)));
                    }
                    catch (e) {
                        if (console && console.log) console.log(e);
                    }
                });
                return;
            }

            // accept event handlers with varying
            // number of arguments
            if (/^(on|off)$/.test(prop)){
                $.each(val, function(evt, args){
                    try {
                        $el[prop].apply($el, [evt].concat(args));
                    }
                    catch(e){
                        if (console && console.log) console.log(e);
                    }
                });
                return;
            }

            // otherwise root property names will be
            // treated as jQuery methods
            try {
                $el[prop].apply($el, [].concat(val));
            }
            catch (e) {
                if (console && console.log) console.log(e);
            }
        });

        // raw element available in the
        // 'element' property of returned
        // jQuery object
        $el.element = el;

        return $el;

    };

    /**
     * Leaner and faster jQuery element spawner
     * @param tag {String|Object} tag name or jQuery object
     * @param [$opts] {Object|String} jQuery options or 'appendable' content
     * @param [opts] {Object|String} element options or 'appendable' content
     * @param [content] {String|Element} 'appendable' content
     * @returns {*|HTMLElement}
     */
    $.spawn.element = function(tag, $opts, opts, content){

        var el, $el, argLen = arguments.length;

        if (argLen === 0){
            return $(document.createDocumentFragment());
        }

        // 'tag' arg is required but can be either
        // a string for the tag name or a jQuery object
        el = tag.jquery ? tag[0] : document.createElement(tag);
        $el = $(el);

        if (argLen === 1){
            return $el;
        }
        else if (argLen === 2){
            if (/(string|number)/.test(typeof $opts)){
                el.innerHTML += $opts+'';
                return $el;
            }
        }
        else if (argLen === 3){
            if (/(string|number)/.test(typeof opts)){
                el.innerHTML += opts+'';
                content = null;
                opts = null;
            }
        }

        if (content){
            $el.append([].concat(content));
        }

        if (opts){
            forOwn(opts, function(prop, val){
                el[prop] = val;
            });
        }

        if ($opts){
            forOwn($opts, function(prop, val){
                $el[prop].apply($el, [].concat(val));
            });
            //forOwn($opts||{}, function(prop, val){
            //    $el[prop] = val;
            //});
        }

        return $el;

    };

    $.spawn.trial = $.spawn.time = function(tag, count){
        tag = tag || 'div';
        count = count || 1000;
        var i = -1,
            time = Date.now(),
            frag$ = $.spawn();
        while (++i < count){
            frag$.append($.spawn.apply(null, [].concat(tag)));
        }
        console.log('time: ' + ((Date.now() - time) / 1000 ) + 's');
        return frag$;
    };


})(jQuery);
