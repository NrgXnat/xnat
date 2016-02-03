/*!
 * JavaScript utility functions, some of which
 * depend on jQuery. (load AFTER jQuery)
 */


// return REST url with common parts pre-defined
// restUrl('/data/projects', ['format=json'])
// returns: '/data/projects?format=json&XNAT_CSRF=8493920104-csrfstring-3858939248'
// it's ok to just keep the query string in the 'url' arg, but if you want to
// prevent the XNAT_CSRF param from getting added, you'll need to pass
// an empty array as the second argument and false as the third
function restUrl( url, params, csrf ){
    // 'params' = array of query string params: ['format=json','sort=asc']
    params = params || [];
    if ( isUndefined(csrf) || isTrue(csrf) ) {
        params.push('XNAT_CSRF=' + csrfToken);
    }
    url = (serverRoot || '') + '/' + url.replace(/^\//, '');
    url += (params.length) ? '?' + params.join('&') : '';
    return url;
}


function cleanBadChars( val, what ){
    var newVal = val.replace( /\W/g, '_' );
    // 'what' arg might be something like "session label"
    // or XNAT.app.displayNames.singular.imageSession.toLowerCase()
    if ( what && newVal != val ){
        xmodal.message('Removing invalid characters' +
            (what ? ' in ' + what : '') + '.',{footer:false});
    }
    return newVal;
}


// replace items in 'str' with 'replacements' object map
function replaceMultiple(str, replacements, regex_params){
    forOwn(replacements, function(to_replace, replacement){
        var regex = new RegExp(to_replace, regex_params||'g');
        str = str.replace(regex, replacement)
    });
    return str;
}


// make sure the ajax calls are NOT cached
//$.ajaxSetup({cache:false});

// checks for email address in format 'name@domain.com'
function validEmailFormat(email) {
    var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
}

jQuery.loadScript = function (url, arg1, arg2) {
    var cache = false, callback = null;
    //arg1 and arg2 can be interchangable
    if ($.isFunction(arg1)){
        callback = arg1;
        cache = arg2 || cache;
    } else {
        cache = arg1 || cache;
        callback = arg2 || callback;
    }

    var load = true;
    //check all existing script tags in the page for the url
    jQuery('script')
        .each(function () {
            return load = (url != $(this).attr('src'));
        });
    if (load){
        //didn't find it in the page, so load it
        jQuery.ajax({
            type: 'GET',
            url: url,
            success: callback,
            dataType: 'script',
            cache: cache
        });
    } else {
        //already loaded so just call the callback
        if (jQuery.isFunction(callback)) {
            callback.call(this);
        }
    }
};


// set the value of a form element, then fire the
// 'onchange' event ONLY if the value actually changed
// (works on hidden inputs too)
// usage:
// $('#element').changeVal('foo');
// sets '#element' to 'foo' then triggers
// 'onchange' event if it's different than before
$.fn.changeVal = function(){
    var prev;
    if ( arguments.length > 0 ){
        prev = $.fn.val.apply(this, []);
    }
    var result = $.fn.val.apply(this, arguments);
    if ( arguments.length > 0 && prev != $.fn.val.apply(this, []) ){
        $(this).trigger('change');
    }
    return result;
};


// Make elements draggable.
// usage:
// $('#element_id').drags();  // <- drag the element that's clicked on
// $('#element_id').drags({handle:'.drag_handle'});
$.fn.drags = function (opt) {

    opt = $.extend({handle: '', cursor: 'move'}, opt);

    var $el;

    if (opt.handle === '') {
        $el = this;
    }
    else {
        $el = this.find(opt.handle);
    }

    return $el.css('cursor', opt.cursor).on('mousedown', function (e) {
        var $drag;
        if (opt.handle === '') {
            $drag = $(this).addClass('draggable');
        }
        else {
            $drag = $(this).addClass('active-handle').parent().addClass('draggable');
        }
        var z_idx = $drag.css('z-index')-0,
            drg_h = $drag.outerHeight(),
            drg_w = $drag.outerWidth(),
            pos_y = $drag.offset().top + drg_h - e.pageY,
            pos_x = $drag.offset().left + drg_w - e.pageX;
        $drag.parents().on('mousemove', function (e) {
            $('.draggable').css({ 'right': 'auto', 'bottom': 'auto' }).offset({
                top: e.pageY + pos_y - drg_h,
                left: e.pageX + pos_x - drg_w
            }).on('mouseup', function () {
                $(this).removeClass('draggable')/*.css('z-index', z_idx)*/;
            });
        });
        e.preventDefault(); // disable selection
    }).on('mouseup', function () {
        if (opt.handle === "") {
            $(this).removeClass('draggable');
        }
        else {
            $(this).removeClass('active-handle').parent().removeClass('draggable');
        }
    });
};
// end draggable


/**
 * jQuery.fn.sortElements
 * --------------
 * @author James Padolsey (http://james.padolsey.com)
 * @version 0.11
 * @updated 18-MAR-2010
 * --------------
 * @param Function comparator:
 *   Exactly the same behaviour as [1,2,3].sort(comparator)
 *
 * @param Function getSortable
 *   A function that should return the element that is
 *   to be sorted. The comparator will run on the
 *   current collection, but you may want the actual
 *   resulting sort to occur on a parent or another
 *   associated element.
 *
 *   E.g. $('td').sortElements(comparator, function(){
 *      return this.parentNode;
 *   })
 *
 *   The <td>'s parent (<tr>) will be sorted instead
 *   of the <td> itself.
 */
jQuery.fn.sortElements = (function(){

    var sort = [].sort;

    return function(comparator, getSortable) {

        getSortable = getSortable || function(){return this;};

        var placements = this.map(function(){

            var sortElement = getSortable.call(this),
                parentNode = sortElement.parentNode,

            // Since the element itself will change position, we have
            // to have some way of storing it's original position in
            // the DOM. The easiest way is to have a 'flag' node:
                nextSibling = parentNode.insertBefore(
                    document.createTextNode(''),
                    sortElement.nextSibling
                );

            return function() {

                if (parentNode === this) {
                    throw new Error(
                        "You can't sort elements if any one is a descendant of another."
                    );
                }

                // Insert before flag:
                parentNode.insertBefore(this, nextSibling);
                // Remove flag:
                parentNode.removeChild(nextSibling);

            };

        });

        return sort.call(this, comparator).each(function(i){
            placements[i].call(getSortable.call(this));
        });

    };

})();


// http://stackoverflow.com/questions/3160277/jquery-table-sort
jQuery.fn.tableSort = function(){
    var $table = this;
    $table.find('th.sort').
        wrapInner('<a href="#" class="nolink" title="click to sort on this column"/>').
        each(function(){
            var th = $(this),
                thIndex = th.index(),
                inverse = false;
            th.click(function(){
                $table.find('td').filter(function(){
                    return $(this).index() === thIndex;
                }).sortElements(function(a, b){
                    return $.text([a]) > $.text([b]) ?
                        inverse ? -1 : 1 : inverse ? 1 : -1;
                }, function(){
                    // parentNode is the element we want to move
                    return this.parentNode;
                });
                inverse = !inverse;
            });
        });
};


// alphabetically (but not numerically)
// sort an array of objects ('objects')
// by a specific property ('prop')
function sortObjects( objects, prop ){
    return objects.sort( function ( _a, _b ) {
        var a = _a[prop].toUpperCase();
        var b = _b[prop].toUpperCase();
        return (a < b) ? -1 : (a > b) ? 1 : 0;
    });
}


// utility for sorting DOM elements
// by their 'title' attribute
// usage: sortElements('ul#list','li');
// 'child' must be DIRECT descendent of 'parent'
function sortElements( _parent, _child ){
    //console.log('sorting...');
    var $mylist = jqObj(_parent);
    var listitems = $mylist.children(_child).get();
    listitems.sort(function( _a, _b ) {
        var a = $(_a).attr('title').toUpperCase();
        var b = $(_b).attr('title').toUpperCase();
        return (a < b) ? -1 : (a > b) ? 1 : 0;
    });
    $mylist.html('');
    $.each(listitems, function( idx, itm ) { $mylist.append(itm) });
}


// force a jQuery object and allow use of
// non-standard id names with special prefix:
// $$('#=weird:id/that.XNAT.will[create]').addClass('cray-cray');
function $$( el, id_prefix ){
    // can't decide on a prefix for selection by id
    // use ONE of these:
    // id= | id: | @= | @: | @ | #= | #: | #/
    var ID_PREFIX = /^(id=|id:|@=|@:|@|#=|#:|#\/)/;
    if (el.jquery){
        return el;
    }
    id_prefix = id_prefix || ID_PREFIX;
    if (el.search(id_prefix) === 0){
        return $(document.getElementById(el.replace(id_prefix,'')));
    }
    return $(el);
}


function jqById(id){
    return $(document.getElementById(id));
}


// this will make sure we've got a jQuery object
function jqObj(el){
    if (!el) { return false }
    var $el = el;
    if (!$el.jquery){
        $el = $(el);
        // if there's not a matching DOM element
        // then it's PROBABLY just an id string
        if (!$el.length){
            $el = $('#'+el);
        }
        // if there's STILL not a matching DOM element
        // after trying to find an element with that ID,
        // then set $el to null
        if (!$el.length){
            $el = null;
        }
    }
    return $el;
}


// global function for initializing "Chosen" menus
// options: http://harvesthq.github.io/chosen/options.html
function chosenInit(select, opts, width){
    var $select = $$(select||'select.chosen-menu'),
        defaults = {
            disable_search_threshold: 7,
            placeholder_text_single: 'Select...',
            placeholder_text_multiple: 'Select...',
            search_contains: true
        };
    if (width) { defaults.width = (width + 'px').replace(/pxpx$/,'px') }
    $select.each(function(){
        var $this = $(this),
            dataChosenOpts = {};
        // trigger an update if it's already been initialized
        if ($this.hasClass('chosen-ready')){
            $this.trigger('chosen:updated');
            return;
        }
        if ($this.data('chosen-opts')){
            parseOptions(dataChosenOpts, $this.data('chosen-opts'));
        }
        var _opts = $.extend(defaults, dataChosenOpts, opts||{});
        // turn menus into "Chosen" menus
        $this.chosen(_opts).addClass('chosen-ready');
    });
    // return the jQuery object/collection
    return $select;
}

function chosenUpdate(select){
    return $$(select||'select.chosen-menu').trigger('chosen:updated');
}


// feed this function a date (and optionally a format) and
// it'll spit out month number and name (full or abbreviated), day, and year
function SplitDate(_date, _format, _mos) {

    var mm_pos, dd_pos, yyyy_pos, example;

    this.val = _date = (typeof _date != 'undefined' && ('' + _date).length) ? _date : '0000-00-00'; // save it to a variable before removing the spaces

    // removing spaces and underscores - probably a better regex for this
    _date = _date.replace(/\s+/g,'').replace(/_/g,'');

    _format = (_format) ? _format.toLowerCase() : '' ;

    // pass in an object containing long and short month names to override
    // default (English) names (don't forget '13' for 'invalid' values)
    var months = _mos || {
        '01': ['January', 'Jan'], '02': ['February', 'Feb'], '03': ['March', 'Mar'], '04': ['April', 'Apr'], '05': ['May', 'May'], '06': ['June', 'Jun'], '07': ['July', 'Jul'],
        '08': ['August', 'Aug'], '09': ['September', 'Sep'], '10': ['October', 'Oct'], '11': ['November', 'Nov'], '12': ['December', 'Dec'], '13': ['invalid', 'invalid']
    };

    // accepts either dashes, slashes or periods as a delimeter
    // but there MUST be one of these as a delimeter
    if (_date.indexOf('-') !== -1) {
        this.arr = _date.split('-');
    }
    else if (_date.indexOf('/') !== -1) {
        this.arr = _date.split('/');
    }
    else if (_date.indexOf('.') !== -1) {
        this.arr = _date.split('.');
    }
    else {
        this.arr = null;
    }

    // we can't do anything if we don't have the date elements saved in an array
    // or if we're passed a bogus value for _date
    if (this.arr !== null && allNumeric(this.arr)) {
        try {
            // accepts either single-digit or double-digit for month or day
            if (this.arr[0].length === 1 || this.arr[0].length === 2) { // it's probably US format, but could MAYBE be Euro format
                var first_num = parseInt(this.arr[0], 10);
                var second_num = parseInt(this.arr[1], 10);
                if (first_num > 12 && first_num < 32 && second_num < 13) _format = 'eu'; // if the first number is higher than 12 but less than 32, it's *probably* Euro format?
                if (_format === 'eu' || _format === 'euro') { // if it's Euro
                    dd_pos = 0;
                    mm_pos = 1;
                    yyyy_pos = 2;
                    example = '31/01/2001';
                    this.format = 'eu';
                }
                else {
                    mm_pos = 0;
                    dd_pos = 1;
                    yyyy_pos = 2;
                    example = '01/31/2001';
                    this.format = 'us';
                }
            }
            else if (this.arr[0].length === 4 || _format === 'iso') { // it's probably ISO format
                yyyy_pos = 0;
                mm_pos = 1;
                dd_pos = 2;
                example = '2001-01-31';
                this.format = 'iso';
            }

            this.m = this.arr[mm_pos];
            this.mm = zeroPad(this.m);
            if (this.m === '' || parseInt(this.m, 10) < 1 || parseInt(this.m, 10) > 12) this.mm = '13';
            //if (this.mm+'' !== '00'){
            this.month = months[this.mm + ''][0]; // set the full month name
            this.mo = months[this.mm + ''][1]; // set the month abbreviation
            //}
            this.d = this.arr[dd_pos];
            this.dd = zeroPad(this.d);
            if (this.d === '' || parseInt(this.d, 10) > 31) this.dd = '32';
            this.yyyy = this.arr[yyyy_pos];
            this.year = (this.yyyy === '0000') ? 0 : this.yyyy ;
            this.example = example;

            this.ISO = this.iso = this.yyyy + '-' + this.mm + '-' + this.dd;
            this.US = this.us = this.mm + '/' + this.dd + '/' + this.yyyy;
            this.EU = this.eu = this.EURO = this.euro = this.dd + '/' + this.mm + '/' + this.yyyy;

            this.date_string = this.yyyy + this.mm + this.dd;
            this.date_num = parseInt(this.date_string, 10);
            this.ms = Date.parse(this.iso);

            this.val = this[this.format] || null;

        }
        catch (e) {
            if (console.log) console.log('Error: ' + e);
            this.val = _date;
            this.format = _format;
        }
    }
    else {
        this.val = _date;
        this.format = _format;
    }
}

/*
 // examples of using the SplitDate function
 var split_date = new SplitDate(XNAT.data.todaysDate.iso);
 if (console.log) console.log('The date is ' + split_date.mo + ' ' + split_date.dd + ', ' + split_date.yyyy + '.');
 //
 var split_date2 = new SplitDate(XNAT.data.todaysDate.us);
 if (console.log) console.log('The date is ' + split_date2.mo + ' ' + split_date2.dd + ', ' + split_date2.yyyy + '.');
 //
 var split_date3 = new SplitDate('22-11-2011','euro');
 if (console.log) console.log('The date is ' + split_date3.mo + ' ' + split_date3.dd + ', ' + split_date3.yyyy + '.');
 //
 var split_date4 = new SplitDate('2001.11.11');
 if (console.log) console.log('The date is ' + split_date4.mo + ' ' + split_date4.dd + ', ' + split_date4.yyyy + '.');
 //
 var split_date5 = new SplitDate('9999-44-55');
 if (console.log) console.log('The date is ' + split_date5.mo + ' ' + split_date5.dd + ', ' + split_date5.yyyy + '.');
 //
 */


/*!
 * Convert an object to a string, for whatever reason.
 * This can be useful for heavy-handed comparison
 * (as in compareByText() function below),
 * or printing out JavaScript objects or functions.
 *
 * Original source:
 * http://stackoverflow.com/questions/5612787/converting-an-object-to-a-string
 * ('extra' comma issue has been resolved in the code below)
 *
 * https://gist.github.com/Error601/d9623b836458b5b6fe86
 */

function convertToText( obj, spaces ){

    if ( obj === window ) {
        // don't be cray-cray
        return false
    }

    var string = '',
        part = [],
        prop,
        len = 0,
        i = -1;

    // is string - skip all other tests
    if ( typeof obj == 'string' ) {
        string += obj;
    }
    // is object
    else if ( Object.prototype.toString.call(obj) === '[object Object]' ) {
        for ( prop in obj ) {
            // should we filter the properties?
            //if ( obj.hasOwnProperty(prop) ){
            part.push(prop + ': ' + convertToText(obj[prop], spaces));
            //}
        }
        string += ' { ' + part.join(', ') + ' } ';
    }
    // is array
    else if ( Array.isArray(obj) ) {
        len = obj.length;
        while ( ++i < len ) {
            part.push(convertToText(obj[i], spaces));
        }
        string += ' [ ' + part.join(', ') + ' ]';
    }
    // is function
    else if ( typeof obj == 'function' ) {
        string += obj.toString();
    }
    //all other values can be done with JSON.stringify
    else {
        string += JSON.stringify(obj);
    }

    // strip spaces by default and also if (spaces !== true)
    if ( typeof spaces == 'undefined' || spaces.toString().toLowerCase() !== 'true' ) {
        return string.replace(/\s/g, '');
    }
    // only keep spaces if (spaces === true)
    else {
        return string;
    }
}

// fairly heavy-handed approach for comparison
function compareByText( obj1, obj2 ){
    return convertToText(obj1) === convertToText(obj2);
}

