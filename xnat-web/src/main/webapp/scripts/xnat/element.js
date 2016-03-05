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
        return spawn.element(tag, setup[0], setup[1]);
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

    // return root element and all children
    Element.p.get = function(){
        if (this.isFragment){
            if (this.rootElement.childNodes.length){
                return this.rootElement.childNodes;
            }
        }
        return this.rootElement;
    };

    Element.p.get$ = function(){
        return $(this.get())
    };

    // return last element in the chain
    Element.p.getLast = function(){
        if (this.isFragment){
            return this.rootElement.lastElementChild || this.rootElement;
        }
        return this.lastElement;
    };

    Element.p.getLast$ = function(){
        return $(this.getLast())
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

    // chainable spawner
    // XNAT.element('div').p()._b('Bold text. ')._i('Italic text.');
    // -> <div><p><b>Bold text. </b><i>Italic text.</i></p></div>
    XNAT.element = XNAT.el = element = function(tag, opts, content){
        return new Element(tag, opts, content);
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
    'select option optgroup ' +
    'img map area embed object script' +
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

        //// add siblings
        //Element.p['_'+tag] = function(opts, content){
        //    var el = setupElement(tag, opts, content);
        //    this.lastParent = this.lastElement.parentNode;
        //    this.lastParent.appendChild(el);
        //    this.parent = el;
        //    this.lastElement = el;
        //    return this;
        //};

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
            return spawn.element(tag, args[0], args[1]);
        }

    });

    // TODO: make element methods chainable:
    // XNAT.element.div({id:'foo'}).p({className:'bar'}, 'Foo Bar');
    // --> <div id="foo"><p class="bar">Foo Bar</p></div>

})(XNAT);
