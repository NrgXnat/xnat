/*!
 * Get or set [data-] attributes and
 * keep jQuery's .data object in sync
 * with those attributes with special
 * handling for non-string data and
 * proper retrieval of functions stored
 * in jQuery's .data object.
 */


/**
 * Sets value for name of [data-] attribute. Also adds value
 * to name prop in jQuery object that .dataAttr() is called on.
 * Returns a new object containing properties from the element's
 * .dataset property and jQuery .data object.
 * jQuery().data().name values override
 * element.dataset.name properties.
 * @param {string} name - Name of [data-] attribute
 * @param {string|array|object|function} [value] - The value to assign to name
 * @return {object}
 * Returns object map of data attributes and values
 * OR the jQuery object .dataAttr() was called on
 */
$.fn.dataAttr = function(name, value){

    var $this = this,
        el = this[0],
        attrVal = value,
        fnName;

    if (!arguments.length){
        // return all [data-] attrs merged
        // with jQuery's .data object
        // if no args are passed
        return $.extend(true, {}, el.dataset, $this.data());
    }

    function processAttrs(){
        // future function to consolidate
        // .dataAttr and .dataAttrs
        // into one function
    }

    if ($.isPlainObject(name) || $.isArray(name)){
        return $this.dataAttrs(name);
    }

    // TODO: separate vars - attrName, dataName, attrVal, dataVal

    // strip data prefix from name
    name = name.replace(/^(data\-+|data)/,'');

    // ensure [data-] attribute names are hy-phen-at-ed
    // and data: {} property names are camelCased
    var dataAttrName = 'data-' + toDashed(name),
        dataPropName = toCamelCase(name),
        // events start with 'on'
        isEvent = dataPropName.indexOf('on') === 0,
        // event name is the [data-] attribute name WITHOUT 'on'
        EVENT_NAME = isEvent ? dataPropName.toLowerCase().replace(/^on/,'') : null;

    if (arguments.length === 1) {
        // get the value from the element attribute
        // instead of the data object???
        if ($.isFunction($this.data(dataPropName))){
            // unless it's a function
            return function(/* arguments */){
                $this.data(dataPropName).apply(el, arguments);
                return $this;
            }
        }
        try {
            return JSON.parse($this.attr(dataAttrName));
        }
        catch(e){
            //console.log(e);
            try {
                return $this.attr(dataAttrName);
            }
            catch(ee){
                console.log(ee);
            }
        }
    }
    else if (value === null) {
        // remove jQuery data and element's [data-] attribute
        // if value is null
        $this.removeData(dataPropName).removeAttr(dataAttrName);
        if (EVENT_NAME){
            $this.off(EVENT_NAME);
        }
    }
    else {
        // if it's a function, it will be available via jQuery's .data() method
        if ($.isFunction(attrVal)) {
            //fnName = "function(){return $('" + $this.selector + "').dataAttr('" + dataPropName + "'))";
            fnName = "$('" + $this.selector + "').dataAttr('" + dataPropName + "')";
            // (escape quotes?) and store the function name
            //attrVal = fnName.replace(/'/g,"\\'").replace(/"/g,'\\"');
            attrVal = fnName;
            //attrVal = "fn:"+dataPropName; // store the function name
            //attrVal = '()';
            //attrVal = 'function:' + dataPropName;
            if (console && console.debug){
                console.debug(
                    'To invoke the function stored in jQuery\'s .data ' +
                    'object for this element, call ' + fnName + '();'
                );
            }
            // then to call it, just do:
            // var func = $('#el').data('foo');
            // func();
        }
        attrVal = (typeof attrVal === 'string') ? attrVal : JSON.stringify(attrVal);
        $this.data(dataPropName, value).attr(dataAttrName, attrVal);
        if (EVENT_NAME){
            $(document.body).on(EVENT_NAME, $this.selector, $this.data(dataPropName));
        }
    }

    function toDashed(str){
        return str.replace(/[A-Z]/g, function(u) {
            return '-' + u;
        }).replace(/[A-Z]-/g, function(c){
            return c.replace(/-$/, '');
        }).toLowerCase().replace(/\W+|_+/g, '-').replace(/^-*|-*$/g, '');
    }

    function toCamelCase(str) {
        // 'sanitize' by running str through toDashed()
        return toDashed(str).replace(/-./g, function(u){
            return u.substr(1).toUpperCase();
        });
    }

    // return the jQuery object for chaining
    return $this;

};

// handle multiple [data-] attributes
// as an object map
// property names must be camelCase 'attrName'
// and will map to hyphenated 'data-attr-name'
// (do NOT include 'data' in property names)
$.fn.dataAttrs = function(obj){

    var $this = this,
        name, val,
        datas = {},
        i = -1;

    if (!arguments.length){
        return $this.dataAttr();
    }

    if ($.isPlainObject(obj)){
        for (name in obj){
            if (obj.hasOwnProperty(name)){
                val = obj[name];
                $this.dataAttr(name, val);
            }
        }
    }
    // return an object map of name: value
    // if an array of [data-] attribute names
    // is passed
    else if ($.isArray(obj)){
        while (++i < obj.length){
            name = obj[i];
            datas[name] = $this.dataAttr(name);
        }
        return datas;
    }
    else {
        return $this.dataAttr.apply($this, arguments);
    }

    return $this;

};

// remove [data-] attributes and jQuery .data() properties
// pass multiple names or array of names as argument(s)
// to remove more than one [data-] attribute
$.fn.removeDataAttrs = $.fn.removeDataAttr = function(/* name(s) */){

    var $this = this,
        names = [].concat(arguments[0]||null),
        i = -1,
        name;

    if (arguments.length > 1){
        names = $.makeArray(arguments);
    }

    while (++i < names.length){
        name = names[i];
        if (name && typeof name == 'string'){
            $this.dataAttr(name, null);
        }
    }

    return $this;
};
