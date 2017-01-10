/*
 * web: element.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Methods for generating DOM elements on-the-fly
 * Uses spawn.js behind the scenes.
 */

var XNAT = getObject(XNAT||{});

(function(XNAT){

    var element, undefined;

    // tolerate passing 'opts' and 'content'
    // arguments in reverse order
    function setOpts(opts, content){
        // if there is only one argument, 
        // it could be content OR opts
        if (!content) {
            content = opts;
            opts = {};
        }
        // if 'content' is an object, put it first
        if (isPlainObject(content)){
            return [content, '']
        }
        return [opts, content];
    }

    function setupElement(tag, opts, content){
        var setup = setOpts(opts, content);
        return spawn(tag, setup[0], setup[1]);
    }

    function Element(tag, opts, content){
        //this.element = this;
        if (!tag) {
            this.element = spawn.fragment();
            this.isFragment = true;
        }
        else {
            this.element = setupElement(tag, opts, content);
        }
        this.rootElement = this.element;
        this.parent = this.element;
    }

    Element.p = Element.prototype;

    Element.p.content = Element.p.html = function(content){
        this.lastElement = this.lastElement || this.parent || this.rootElement;
        this.lastElement.innerHTML = [
            this.lastElement.innerHTML,
            content
        ].join(' ');
        return this;
    };

    var $methods = [
        'attr', 'prepend', 'append',
        'before', 'after',
        'addClass', 'removeClass'
    ];

    // hijack some methods from jQuery
    $methods.forEach(function(method){
        Element.p[method] = function(){
            var $el = this.getLast$();
            $el[method].apply($el, arguments);
            return this;
        }
    });

    // uses jquery.dataAttr.js
    Element.p.data = function(name, value){
        this.lastElement = this.lastElement || this.parent || this.rootElement;
        $(this.lastElement).dataAttr(name, value);
        return this;
    };

    // return root element and all children
    Element.p.get = function(i){
        if (this.isFragment){
            if (this.rootElement.childNodes.length){
                return this.rootElement.childNodes[i||0];
            }
        }
        return this.rootElement;
    };

    Element.p.get$ = function(i){
        this.$element = this.element$ = $(this.get(i));
        return this.$element;
    };

    // return last element in the chain
    Element.p.getLast = function(){
        if (this.isFragment){
            return this.rootElement.lastElementChild || this.rootElement;
        }
        return this.lastElement;
    };

    Element.p.getLast$ = function(){
        this.$last = this.last$ = $(this.getLast());
        return this.$last;
    };

    Element.p.$ = function($opts){
        var $last = this.getLast$();
        if (arguments.length === 2){
            $last[arguments[0]].apply($last, [].concat(arguments[1]));
        }
        else if (Array.isArray($opts)){
            $last[$opts[0]].apply($last, [].concat($opts[1]));
        }
        else {
            forOwn($opts, function($opt, args){
                $last[$opt].apply($last, [].concat(args));
            });
        }
        return this;
    };

    Element.p.upTo = Element.p.up = function(tag){
        // don't go past the root element
        if (this.lastElement === this.rootElement){
            this.parent = this.rootElement;
            return this;
        }
        // go up one right away
        this.parent = this.lastElement = this.lastElement.parentNode;
        // return early for simple usage
        if (!tag) return this;
        // keep going if 'tag' is specified
        var parentTag = this.parent.tagName.toLowerCase();
        tag = tag ? tag.toLowerCase() : parentTag;
        if (tag !== parentTag){
            this.upTo(tag);
        }
        return this;
    };

    Element.p.closest = function(selector){
        this.parent = this.lastElement =
            $(this.lastElement).closest(selector)[0];
        return this;
    };

    //////////////////////////////////////////////////////////////////////
    // chainable spawner
    // XNAT.element('div').p()._b('Bold text. ')._i('Italic text.');
    // -> <div><p><b>Bold text. </b><i>Italic text.</i></p></div>
    element = function(tag, opts, content){
        return new Element(tag, opts, content);
    };
    //////////////////////////////////////////////////////////////////////
    
    // copy value from 'target' to 'source'
    element.copyValue = function(target, source){
        var sourceValue = $$(source).val();
        $$(target).val(sourceValue).dataAttr('value', sourceValue);
    };

    // copy value from 'source' to last element in the chain
    Element.p.copyValue = function(source){
        element.copyValue(this.getLast$(), source);
        return this;
    };

    // set 'name' property with 'value' on 'elements'
    element.setProp = function(elements, name, value){
        [].concat(elements).forEach(function(element){
            $$(element).prop(name, value);
        });
    };

    // set properties and classes to 'disable' an element
    element.setDisabled = function(elements, disabled){
        disabled = firstDefined(disabled, true);
        [].concat(elements).forEach(function(element){
            var _disabled = !!disabled;
            var modifyClass = _disabled ? 'addClass' : 'removeClass';
            $$(element).prop('disabled', _disabled)[modifyClass]('disabled');
        });
    };

    // 'disable' last element in the chain
    Element.p.disabled = Element.p.setDisabled = function(disabled){
        element.setDisabled(this.getLast$(), disabled);
        return this;
    };

    element.checked = function(elements, checked){
        [].concat(elements).forEach(function(element){
            var _checked = !!checked;
            var modifyClass = _checked ? 'addClass' : 'removeClass';
            $$(element).prop('checked', _checked)[modifyClass]('checked');
        });
    };

    Element.p.checked = function(checked){

    };

    // space-separated list of elements
    // for auto-generated functions
    // like:
    // XNAT.element.div('Foo') -> <div>Foo</div>
    // XNAT.element.br() -> <br>
    // full list of Elements:
    // https://developer.mozilla.org/en-US/docs/Web/HTML/Element
    var tagNames = ('' +
    'div span p q a h1 h2 h3 h4 h5 h6 main ' +
    'header footer nav section hgroup article ' +
    'table thead tr th tbody td tfoot col colgroup ' +
    'ul ol li dl dt dd hr br iframe ' +
    's small sub sup u b i em strong pre ' +
    'form fieldset button input textarea ' +
    'label select option optgroup ' +
    'img map area embed object' +
    '').split(/\s+/);

    tagNames.forEach(function(tag, i){

        // don't process empty 'tag'
        if (!tag) return;

        // don't overwrite existing functions
        if (isFunction(element[tag])) return;

        // add siblings after
        Element.p['_'+tag] = function(opts, content){
            var el = setupElement(tag, opts, content);
            this.parent.appendChild(el);
            this.lastElement = el;
            return this;
        };

        Element.p['_'+tag+'$'] = function(opts, content){
            this.last$ = this.$last =
                this['_'+tag]().$(opts).get$().append(content);
            return this;
        };

        // add generators to prototype for chaining
        Element.p[tag] = function(opts, content){
            var el = setupElement(tag, opts, content);
            this.parent.appendChild(el);
            // set parent to THIS element
            // for creating child elements
            this.parent = el;
            this.lastElement = el;
            return this;
        };

        // generate tag functions to call
        // without calling XNAT.element() first
        // XNAT.element.div('Foo')
        // -> <div>Foo</div>
        element[tag] = function(opts, content){
            var args = setOpts(opts, content);
            return spawn(tag, args[0], args[1]);
        }

    });
    
    
    // insert items into <head> element
    element.head = {
        append: function(opts){
            opts = cloneObject(opts);
            opts.tagName = opts.tagName || opts.tag;
            if (!opts.tagName) return;
            delete opts.tag;
            var el = spawn(opts.tagName, opts);
            document.head.appendChild(el);
            return {
                element: el,
                spawned: el
            }
        }
    };
    element.head.insert = element.head.append;
    

    // special handling of <script> elements
    // add them to the <head>
    element.script = function(opts){
        opts = cloneObject(opts);
        var el;
        if (opts.src) {
            opts.html = '';
        }
        el = spawn('script', opts);
        document.head.appendChild(el);
        return {
            element: el,
            spawned: el
        }
    };


    //////////////////////////////////////////////////////////////////////
    // chainable spawner
    // XNAT.element('div').p()._b('Bold text. ')._i('Italic text.');
    // -> <div><p><b>Bold text. </b><i>Italic text.</i></p></div>
    XNAT.element = XNAT.el = element;
    // also add to XNAT.ui namespace
    XNAT.ui = getObject(XNAT.ui||{});
    XNAT.ui.element = XNAT.element;
    //////////////////////////////////////////////////////////////////////


})(XNAT);
