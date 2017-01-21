/*
 * web: xmodal.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * xmodal jQuery plugin
 * written for XNAT (xnat.org)
 * to ease the transition away from YUI dialogs
 * by Mark M. Florida (floridam@wustl.edu)
 */

var xmodal = xmodal || {};
xmodal.version = '1.0.3';

if (typeof jQuery == 'undefined') {
    throw new Error('jQuery is required for the xmodal plugin.');
}

(function() {

    var $ = jQuery;
    var _xmodal = {}, $html, $body, $mask, $modal, undefined;


    _xmodal.topZ = 10000;

    // polyfill for string.trim(); method
    if (!String.prototype.trim) {
        String.prototype.trim = function () {
            return this.replace(/^[\s\xA0]+|[\s\xA0]+$/g, '');
        };
    }


    // force a jQuery object and allow use of
    // non-standard id names with special prefix:
    // $$('@#weird:id/that.XNAT.will[create]').addClass('cray-cray');
    function $$( el, id_prefix ){
        // can't decide on a prefix for selection by id
        // use ONE of these:
        // id= | id: | @id= | @# | @= | @: | @ | #= | #: | #/
        var ALL_PREFIX = /^!\*/,  // $$('!*div.foo') --> return raw 'div.foo' elements as an array
            RAW_ID     = /^!#/,   // $$('!#foo')     --> return (one) raw element with id 'foo'
            NAME_RAW   = /^!\?/,  // $$('!?foo')     --> return raw elements with [name="foo"]
            NAME_$     = /^\?/,   // $$('?foo')      --> return wrapped elements with [name="foo"]
            RAW_PREFIX = /^!/,    // $$('!div.foo')  --> return FIRST raw 'div.foo' element
            ID_PREFIX  = /^(id=|id:|@id=|@#|@=|@:|@|#=|#:|#\/)/;
        if (!el || el.jquery){
            return el;
        }
        if (typeof el == 'string'){
            if (el.search(ALL_PREFIX) === 0){
                return document.querySelectorAll(el.replace(ALL_PREFIX, ''));
            }
            // pass empty string or null as the second argument
            // to get the bare element by id (no jQuery)
            if (id_prefix === '' || id_prefix === null || el.search(RAW_ID) === 0){
                return document.getElementById(el.replace(RAW_ID,''));
            }
            if (el.search(NAME_RAW) === 0){
                return document.getElementsByName(el.replace(NAME_RAW, ''));
            }
            if (el.search(NAME_$) === 0){
                return $(document.getElementsByName(el.replace(NAME_$, '')));
            }
            if (el.search(RAW_PREFIX) === 0){
                return document.querySelector(el.replace(RAW_PREFIX,''));
            }
            id_prefix = id_prefix || ID_PREFIX;
            if (el.search(id_prefix) === 0){
                return $(document.getElementById(el.replace(id_prefix,'')));
            }
        }
        return $(el);
    }


    function getScriptDir() {
        var src, path;
        src = $('script[src]').last().attr('src');
        if (src.indexOf('/') !== -1) {
            path = src.split('/');
            path.splice(path.length - 1, 1);
            return path.join('/') + '/';
        }
        else {
            return '';
        }
    }


    function appendCSS(_path, _filename) {
        _path = (_path !== null) ? _path : getScriptDir();
        if (!$('link[href*="' + _filename + '"]').length) {
            $('head').append('<link type="text/css" rel="stylesheet" href="' + _path + _filename + '">');
        }
    }


    function appendScript(_path, _filename) {
        _path = (_path !== null) ? _path : getScriptDir();
        if (!$('script[src*="' + _filename + '"]').length) {
            $('head').append('<script type="text/javascript" src="' + _path + _filename + '"></script>');
        }
    }


    function getObject(obj){
        return $.isPlainObject(obj) ? obj : {};
    }


    function isValue( a, b ){
        if (arguments.length !== 2) return; // need exactly TWO args
        if (typeof a != 'undefined'){
            return (a.toString() === b.toString());
        }
    }


    function isFalse( val ){
        return isValue( val, false );
    }


    function isTrue( val ){
        return isValue( val, true );
    }


    function isDefined( x ){
        return typeof x != 'undefined';
    }


    // make the modal draggable
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
                //xmodal.topZ = z_idx+1;
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


    _xmodal.path = getScriptDir();


    _xmodal.count = 0;
    _xmodal.presets = {};
    _xmodal.modals = {};
    _xmodal.modals._ids = [];


    // commonly re-used HTML class names - set here as a convenience
    _xmodal.strings = {};
    _xmodal.strings.base = 'xmodal';
    _xmodal.strings.mask = 'xmodal-mask';
    _xmodal.strings.version = 'v1'; // this is here to deal with the 'old' xModal


    // these properties are added to 'xmodal.dialog' object
    _xmodal.dialog = (function( strings ){

        var dialog={};

        dialog.element = 'div';
        dialog.v       = strings.version;
        dialog.box     = dialog.element + '.' + strings.base + '.' + dialog.v;
        dialog.mask    = dialog.element + '.' + strings.mask + '.' + dialog.v;
        dialog.open    = dialog.box + '.' + 'open';
        dialog.top     = dialog.box + '.' + 'top';

        return dialog;

    })( _xmodal.strings );


    _xmodal.bodyClassName = _xmodal.strings.base + ' ' + _xmodal.strings.base + '-' + _xmodal.strings.version;

    // override local '_xmodal' properties with
    // properties attached to global 'xmodal' object
    xmodal = $.extend(true, _xmodal, xmodal);


    //////////////////////////////////////////////////
    // after DOM load (just do everything then)
    //////////////////////////////////////////////////
    $(function () {

        $html = $('html');
        $body = $(document.body);
        $body.addClass(xmodal.bodyClassName);
        appendCSS(xmodal.path, 'xmodal.css');
        //$body.on('click','div.xmodal .close',function(){xmodal.close(this)});


        // add 'loading_bar.gif' to the page (but off-canvas) to preload it
        // so it's displayed immediately when opening a loading dialog
        $body.append('' +
            '<div id="xmodal-loading" style="position:fixed;left:-9999px;top:-9999px;">' +
            '<img src="' + xmodal.path + 'loading_bar.gif" alt="loading">' +
            '</div>' +
            '');


        $body.on('keydown', function (e) {
            var keyCode = (window.event) ? e.which : e.keyCode;
            //if (keyCode !== 27 || keyCode !== 13) { return }
            var $open = $(xmodal.dialog.open);
            if (!$open.length) { return }
            var $top_modal = $(xmodal.dialog.top).last();
            if (keyCode === 27) {  // key 27 = 'esc'
                if ($top_modal.hasClass('esc')) {
                    if ($top_modal.find('.buttons .cancel').length) {
                        $top_modal.find('.buttons .cancel').not('.disabled').trigger('click');
                    }
                    else {
                        xmodal.close($top_modal);
                    }
                    //$top_modal.find('.title .close').trigger('click');
                }
            }
            else if (keyCode === 13) {  // key 13 = 'enter'
                if ($top_modal.hasClass('enter') &&
                        ($top_modal.has(document.activeElement).length ||
                            $top_modal.is(document.activeElement))) {
                    //e.preventDefault();
                    $top_modal.find('.buttons .default').not('.disabled').trigger('click');
                    //xmodal.closeModal = false;
                }
            }
        });


        $body.on('focus', 'a, button, :input, [tabindex]', function(e){
            if (!xmodal.modals._ids.length || !$(xmodal.dialog.mask).length) {
                return
            }
            var $top = $(xmodal.dialog.top);
            // if (!$top) { return }
            if (!$(this).closest($top).length){
                if (!e) { e = window.event }
                if (e.stopPropagation){
                    e.stopPropagation();
                }
                else {
                    e.cancelBubble = true;
                }
                setTimeout(function(){
                    $top.focus();
                }, 10);
            }
        });


        $body.on('mousedown', 'div.xmodal.open:not(.top)', function(e){
            // click anywhere in an xmodal to move it to the front
            // if there is more than one open
            var $open = $(xmodal.dialog.open);
            if ( $open.length ) {
                $open.removeClass('top');
                //xmodal.topZ = $modal.css('z-index')+10;
                $(this).not('.static').css('z-index',++xmodal.topZ).addClass('top');
            }
        });


        $body.on('click', 'div.xmodal.open .maximize', function(){
            var $modal = $(this).closest(xmodal.dialog.box);
            $modal.toggleClass('maxxed');
            xmodal.resize($modal);
        });


        // any <a class="xmodal-close"> will close the dialog
        $body.on('click', 'div.xmodal.open .xmodal-close', function(e){
            e.preventDefault();
            var $modal = $(this).closest(xmodal.dialog.box);
            xmodal.close($modal);
        });


        // option-click on close button to close all modals
        // with a delay so users can see what's going on
        $body.on('click', 'div.xmodal.open .title .close', function(e){
            if (e.altKey) {
                xmodal.closeAll();
            }
        });




        //////////////////////////////////////////////////
        // get xmodal config opts from data attribute
        //////////////////////////////////////////////////
        // usage:
        // <div data-xmodal-opts="size:large|speed:2000|mask:false"><div>
        xmodal.getOptsFromData = function( el, opts, delim, sep ){
            // el = DOM element (jQuery object, selector, or id)
            // opts = config object we're modifying, or empty object {}
            if ( !el || !opts ) { return } // need both args
            delim = delim || '|'; // delimiter between properties
            sep   = sep   || ':'; // separator between key:value
            var $el = $$(el);
            var data = $el.data('xmodal-opts') || $el.data('xmodal-options');
            if (data){
                var dataOpts = data.split(delim);
                $.each( dataOpts, function(i, v) {
                    var opt = v.split(sep);
                    opts[opt[0].trim()] = opt[1].trim();
                });
            }
            return opts;
        };
        //////////////////////////////////////////////////





        //////////////////////////////////////////////////
        // get xmodal object from xmodal.modals
        //////////////////////////////////////////////////
        // usage:
        // var modalObj = xmodal.getModalObject(id);
        // modalObj.__modal.find('form').submit();
        // __modal is jQuery DOM object for the modal
        //////////////////////////////////////////////////
        xmodal.getModalObject = function( id ){
            if ( !id ) { return false } // need the id
            return xmodal.modals[id];
        };
        //////////////////////////////////////////////////




        //////////////////////////////////////////////////
        // delete the 'xmodal.modals[_id]' object
        //////////////////////////////////////////////////
        xmodal.deleteModal = function(_id){

            try {
                delete xmodal.modals[_id];
            }
            catch(e) {
                // fail silently
            }

            var i = xmodal.modals._ids.indexOf(_id);
            xmodal.modals._ids.splice( i, 1 );

        };
        //////////////////////////////////////////////////




        //////////////////////////////////////////////////
        // set the xmodal body element's size
        //////////////////////////////////////////////////
        xmodal.resize = function ($modal, modal) {

            function resizeBody(_$modal, _modal) {
                var $title = _$modal.find('.title');
                var modal_height  = _$modal.height();
                var title_height  = $title.height();
                var title_width   = $title.width();
                var footer_height = _$modal.find('.footer').height();
                var body_height   = 200; // set an initial value

                if ( !isFalse(_modal.footer) && $.isPlainObject(_modal.footer) ) {
                    body_height = Math.round(modal_height - title_height - footer_height - 1);
                }
                else {
                    body_height = Math.round(modal_height - title_height);
                }

                _$modal.find('div.body').css({
                    'height': body_height - 1
                });

                if ( !isFalse(modal.closeBtn) ){
                    title_width = title_width - 48;
                    $title.find('.inner').css({
                        'width': title_width
                    });
                }

                if ( !isFalse(modal.maximize) ){
                    title_width = title_width - 34;
                    $title.find('.inner').css({
                        'width': title_width
                    });
                }

            }

            modal = modal || xmodal.modals[$modal.attr('id')];

            resizeBody($modal, modal);

            $(window).off('resize').on('resize', function(){
                resizeBody($modal, modal);
            });

        };
        //////////////////////////////////////////////////




        //////////////////////////////////////////////////
        // define some default buttons
        //////////////////////////////////////////////////
        xmodal.defaultButtons = function(opts){

            opts = getObject(opts);

            var buttons = {};

            if ( isFalse(opts.ok) || opts.ok === 'hide') {
                buttons.ok = false
            }
            else {
                buttons.ok = {};
                buttons.ok.label = opts.okLabel || 'OK';
                buttons.ok.action = opts.okAction || $.noop;
                buttons.ok.close = ( !isFalse(opts.okClose) );
                buttons.ok.isDefault = true;
            }

            if ( isFalse(opts.cancel) || opts.cancel === 'hide') {
                buttons.cancel = false
            }
            else {
                buttons.cancel = {};
                buttons.cancel.label = opts.cancelLabel || 'Cancel';
                buttons.cancel.action = opts.cancelAction || $.noop;
                buttons.cancel.close = ( !isFalse(opts.cancelClose) );
            }

            return buttons;

        };
        //xmodal.defaultButtons = xmodal.defaultButtonSetup();
        //////////////////////////////////////////////////




        //////////////////////////////////////////////////
        // HTML output factory
        //////////////////////////////////////////////////
        xmodal.output = function (_opts) {

            // make sure we've AT LEAST got an empty object
            var modal = $.extend(true, {}, _opts);

            function renderButtons() {

                var html = '';

                modal.buttons = ($.isPlainObject(modal.buttons)) ? modal.buttons : {};

                $.each(modal.buttons, function (_prop, _value) {
                    // we NEED an object for the button value for proper configuration
                    // use false, null or '' to remove a default 'ok' or 'cancel' button
                    if (!$.isPlainObject(_value)) return;
                    var button = this;
                    var disabled = '';

                    button.classNames = button.classNames || button.className || button.classes;
                    var button_id = modal.id + '-' + _prop + '-button';
                    // 'button' and the name of this button's property
                    // are automatically added to the class attribute
                    var classNames = [_prop];

                    if ( button.classNames ) { classNames.push(button.classNames) }
                    if ( isTrue(button.close) ) { classNames.push('close') }
                    if ( isTrue(button.isDefault) || isTrue(button['default']) ) { classNames.push('default') }
                    if ( isTrue(button.disabled) ){
                        classNames.push('disabled');
                        disabled = 'disabled ';
                    }

                    if (button.link) {
                        classNames.push('link');
                        html += '' +
                            '<a tabindex="0" id="' + button_id + '" href="javascript:"' +
                            ' class="' + classNames.join(' ') + '">' + button.label + '</a> ';
                    }
                    else {
                        classNames.push('button');
                        html += '' +
                            '<button ' + disabled + 'tabindex="0" id="' + button_id + '" ' +
                            'class="' + classNames.join(' ') + '">' + button.label + '</button> ';
                    }

                    var button_ = modal.buttons[_prop];
                    var button_action = button_.action;

                    var click_btn = 'click.' + button_id;

                    // unbind any existing onclick events
                    $body.off(click_btn, '#' + button_id);

                    // bind onclick events to THESE buttons
                    $body.on(click_btn, '#' + button_id, function (e) {
                        // stop if it's disabled
                        if ($(this).hasClass('disabled')){
                            e.stopImmediatePropagation();
                            return false;
                        }
                        var thisModal = xmodal.getModalObject(modal.id);
                        if ($.isFunction(button_action)) {
                            button_action(thisModal);
                        }
                        if ($(this).hasClass('close')) {
                            xmodal.close(thisModal.$modal);
                        }
                    });
                });

                return html;
            }
            //
            var buttons = renderButtons();

            // create object to collect the output
            var output={};

            output.container={};

            // HTML to put BEFORE the xmodal stuff
            output.container._pre = '';

            // HTML for the mask
            output.container._pre +=
                // only generate the mask if not false (true is default)
                ( !isFalse(modal.mask) ) ?
                    '<div class="' + [xmodal.strings.mask, xmodal.strings.version, modal.classNames].join(' ') + '"' +
                        ' id="' + modal.id + '-mask" data-xmodal-id="' + modal.id + '"' +
                        ' data-xmodal-x="' + modal.count + '"></div>' :
                    '';

            // HTML for the modal container
            output.container._pre +=
                '<div class="' + [xmodal.strings.base, xmodal.strings.version, modal.classNames].join(' ') + '"' +
                    ' id="' + modal.id + '" data-xmodal-x="' + modal.count + '">';

            output.container.post_ = '</div>';

            if ( !isFalse(modal.title) ){
                modal.titleBarStyle = '';
            }
            else {
                modal.titleBarStyle = ' style="height:15px;"';
                modal.title = '';
                modal.closeBtn = false;
            }
            output.title  = '<div title="' + modal.title + '" class="title"' + modal.titleBarStyle + '>';
            output.title += '<span class="inner"';
            if (modal.titleStyle){
                output.title += (' style="' + modal.titleStyle + '"');
            }
            output.title += '>' + modal.title + '</span>';

            if (isTrue(modal.maximize)){
                output.title +=
                    '<b class="maximize" title="maximize/minimize this dialog">' +
                    '&ndash;' +
                    '</b>' ;
            }

            if (isTrue(modal.closeBtn)){
                output.title +=
                    '<b class="close" title="click to close (alt-click to close all modals)">' +
                    '&times;' +
                    '</b>' ;
            }

            output.title += '</div>';

            output.body =
                '<div class="body content">' +
                '' + '<div class="inner">' + modal.content + '</div>' +
                '</div>';


            if ( !isFalse(modal.footer) ) {
                output.footerStyle =
                    'height:' + modal.footer.height + 'px;' +
                    'background-color:' + modal.footer.background + ';' +
                    'border-color:' + modal.footer.border + ';' +
                    '';
                output.footer =
                    '<div class="footer" style="' + output.footerStyle + '">' +
                    '<div class="inner">' +
                    '<span class="content">' + modal.footer.content + '</span>' +
                    '<span class="buttons">' + buttons + '</span>' +
                    '</div>' + // end .inner
                    '</div>'; // end .footer
            }
            else {
                output.footer = '';
            }

            // if we want to TOTALLY overwrite the HTML,
            // or force any other property, put those
            // properties in a 'custom' property of
            // the object passed to the 'xmodal.Modal' constructor
            var custom = (_opts && _opts.custom) ? _opts.custom : {};
            $.extend(true, output, custom);

            output.html =
                output.html ||
                output.container._pre + output.title + output.body + output.footer + output.container.post_;
            output.opts = _opts; // pass along the original _opts if needed later
            output.config = modal; // the combined config object

            // assign the 'close' onclick handler to THIS modal
            $body.on('click', '#' + modal.id + ' .close', function () {
                xmodal.close(modal.id);
            });

            return output;

        };
        //////////////////////////////////////////////////




        //////////////////////////////////////////////////
        // xmodal.Modal constructor - magic starts here
        //////////////////////////////////////////////////
        xmodal.Modal = function (_opts) {

            // extending _opts with an empty object SHOULD make sure
            // we're working with an object, even if it's empty
            _opts = getObject(_opts);

            var _this = this;

            //xmodal.count++;
            this.count = _opts.count || ++xmodal.count;
            this.id = _opts.id =
                (_opts.id) ? _opts.id : 'xmodal' + this.count;

            _opts.classNames = _opts.classNames || _opts.className || _opts.classes || '' ;

            // don't allow dismissal of dialog with 'esc' or 'enter' keys
            // if the corresponding property is false;
            if ( !isFalse(_opts.enter) ) { _opts.classNames += ' enter' }
            if ( !isFalse(_opts.esc) ) { _opts.classNames += ' esc' }

            // we can use 'className', 'classNames' or 'classes'
            this.classNames = this.className = this.classes = _opts.classNames ;

            this.top = firstDefined(_opts.top||undefined, '22px');

            this.width = 600;
            //this.minWidth = 'inherit';
            //this.maxWidth = 'inherit';

            this.height = 400;
            //this.minHeight = 'inherit';
            //this.maxHeight = 'inherit';

            // support for 'preset' sizes
            if (_opts.kind || _opts.size){
                (function(){
                    var kinds = {
                        dialog:  [600, 400],
                        message: [400, 200],
                        max:     ['98%', '98%'],
                        full:    ['98%', '98%'],
                        large:   ['80%', '80%'],
                        med:     ['60%', '60%'],
                        small:   ['40%', '40%'],
                        xsmall:  ['30%', '20%']
                    };
                    $.each(kinds, function( kind, dims ){
                        if (_opts.kind === kind || _opts.size === kind){
                            _this.width = dims[0];
                            _this.height = dims[1];
                            return false; // exits $.each()
                        }
                    });
                })();
            }

            this.position = false; // top: css property - false is vertically centered
            this.animation = 'fade'; // false, 'fade' or 'slide'
            this.duration = _opts.speed || _opts.duration || 100; // if we're passing duration as 'speed'
            this.speed = this.duration;
            this.modal = (typeof _opts.modal != 'undefined') ? _opts.modal : true ;
            this.mask = _opts.mask || this.modal ; // is this dialog modal? defaults to 'true' so mask shows
            this.title = 'Message';
            this.closeBtn = true;
            this.maximize = false;
            //this.padding = _opts.padding || 20; // amount of padding - in pixels
            this.template = false; // don't use a template by default (set a value if a condition below is met)

            if (_opts.template){
                this.template = this.$template = _opts.template = $$(_opts.template);
                // there's a 'data-xmodal-opts' attribute on the
                // template element, extract options from that
                if (this.template.data('xmodal-opts') || this.template.data('xmodal-options')){
                    // options in data-xmodal-opts/options attribute overrides config object
                    // this makes it easier to customize on-the-fly
                    xmodal.getOptsFromData(this.template, _opts);
                }
            }

            // what is the mess in the value of this.content?
            // 1) set this.content and _opts.content to _opts.content or "(no content)" if we're NOT using a template
            // 2) BUT... if we ARE using a template (if there's a value set for _opts.template), grab the HTML from that
            this.content = _opts.content =
                ( !isFalse(this.template) ) ?
                    this.template.html() :
                    _opts.content || '(no content)';
            this.scroll = true;
            this.footer = { // this.footer = false -- will not render footer
                //show: true, // probably don't need this - just pass "false" to "footer" property to not render footer
                content:    _opts.footerContent || '',
                height:     _opts.footerHeight || 50,
                background: _opts.footerBackground || '#f0f0f0',
                border:     _opts.footerBorder || '#e0e0e0',
                buttons:    _opts.footerButtons || true
            };
            // okLabel, okAction, okClose, cancelLabel, cancelAction included for easy config of default buttons

            this.buttons = ($.isPlainObject(_opts.buttons)) ? _opts.buttons : xmodal.defaultButtons(_opts);

            this.isDraggable = _opts.draggable || true;

            if ( !isFalse(this.template) && this.template.jquery ) {
                this.template.empty()
            }

            this.original = this.inputObject = _opts;

            // merge defaults with config object
            // properties in '_opts' override 'this'
            $.extend(true, this, _opts);

            // private property to let xmodal.open() know if
            // this constructor has been called yet
            //this.newModal = true;
            // (now checks instanceof xmodal.Modal)

        };
        //////////////////////////////////////////////////





        //////////////////////////////////////////////////
        // xmodal GO!
        //////////////////////////////////////////////////
        xmodal.open = function (obj) {

            var modal, output, modal_id;
            // set xmodal.modals.max before calling this script
            // to set a different value for max number of xmodal modals
            var max = xmodal.modals.max || 100;
            // limit to 100 open modals
            // if there are more than that,
            // there is likely a problem somewhere else
            if ($(xmodal.dialog.box).length > (max-1)) { return }

            // also limit to 200 total modals (open or not)
            //if ($(xmodal.dialog.box).length > 199) { return }

            // if 'obj' has ( newModal: true ) then 'obj' was
            // created with ( new xmodal.Modal() ) constructor
            // and we can pass obj as-is
            if (obj instanceof xmodal.Modal){
                modal = obj;
            }
            // otherwise pass 'obj' to xmodal.Modal(obj) constructor
            else {
                modal = new xmodal.Modal(obj);
            }

            output = xmodal.output(modal);
            modal.html = output.html;

            // put the html on the page, but don't show it yet
            $body.append(modal.html);

            modal_id = '#' + modal.id;
            modal.$modal = $modal = $(modal_id);

            if ( !isFalse(modal.mask) ){
                modal.$mask = $mask = $(modal_id + '-mask');
                modal.$mask.css({
                    'z-index': ++xmodal.topZ
                });
                modal.$mask.show().addClass('open');
            }

            // copy to Velocity-safe var names
            // to be available to callback methods
            modal.__mask  = modal.$mask;
            modal.__modal = modal.$modal;
            // also access '$modal' object via '$dialog' or '__dialog'
            modal.__dialog = modal.$dialog = modal.$modal;

            var top_  = modal.top  || 0,
                left_ = modal.left || 0;

            if ($.isPlainObject(modal.position)) {
                top_  = modal.position.top  || top_;
                left_ = modal.position.left || left_;
            }

            // adjust height if there's no text in the title bar
            // or DON'T because it breaks with % values
            //if (modal.title === false) {
            //    modal.height = modal.height-10
            //}

            var _css = {
                'top': top_,
                'bottom': (top_ !== 0) ? modal.bottom || 'auto' : 0,
                'left': left_,
                'right': (left_ !== 0) ? 'auto' : 0,
                'width': modal.width,
                'height': modal.height,
                'z-index': ++xmodal.topZ
            };

            // only set min/max if defined
            ['minWidth', 'maxWidth', 'minHeight', 'maxHeight'].forEach(function(prop){
                if (modal[prop]) {
                    _css[prop] = modal[prop]
                }
            });

            modal.$modal.css(_css);

            if ( isTrue(modal.scroll) ) {
                modal.$modal.find('> .body').addClass('scroll');
            }

            if ( isDefined(modal.overflow||undefined) ){
                if ( isTrue(modal.overflow) ){
                    modal.overflow = 'visible'
                }
                if ( isFalse(modal.overflow) ){
                    modal.overflow = 'hidden'
                }
                // set the overflow css property
                // on the entire dialog as well
                // as the .body and .inner elements
                modal.$modal.css('overflow', modal.overflow).
                    find('> .body').css('overflow', modal.overflow).
                    find('> .inner').css('overflow', modal.overflow);
            }

            if ( isDefined(modal.padding||undefined) ) {
                modal.style = getObject(modal.style||{});
                modal.style.padding = modal.padding + 'px';
                modal.$modal.find('> .body').css(modal.style);
            }
            if ( isDefined(modal.style||undefined) ) {
                modal.css = getObject(modal.css||{});
                $.extend(modal.css, modal.style);
            }
            if ( isDefined(modal.css||undefined) ) {
                modal.$modal.find('> .body > .inner').css(modal.css);
            }

            if ($.isFunction(modal.beforeShow)) {
                modal.beforeShow(modal);
            }

            modal.$modal.find('> .body > .inner').fadeIn(modal.speed);

            function afterRender($modal) {

                xmodal.resize($modal, modal);

                $('body').addClass('open');
                $('html').addClass('noscroll');

                $modal.attr('tabindex',0).focus();

                if ($.isFunction(modal.afterShow)) {
                    modal.afterShow(modal);
                }

                xmodal.modals._ids.push(modal.id);

            }

            if (modal.animation === 'fade') {
                modal.$modal.fadeIn(+modal.speed, function() {
                    afterRender($(this));
                });
            }
            else if (modal.animation === 'slide') {
                modal.$modal.slideDown(+modal.speed, function() {
                    afterRender($(this));
                });
            }
            else {
                if (modal.animation !== 'hide'){
                    modal.$modal.show();
                    afterRender($modal);
                }
            }

            $(xmodal.dialog.box).removeClass('top'); // strip 'top' class from ALL xmodal modals
            modal.$modal.addClass('open top'); // add 'top' (and 'open') class to only top one

            if ( !isFalse(modal.isDraggable) ) {
                if (typeof $.fn.drags != 'undefined') {
                    modal.$modal.drags({ handle: '.title' });
                }
            }

            // add close method to modal object itself
            modal.closeModal = modal.close = function(){
                try {
                    xmodal.close(modal.$modal);
                }
                catch(e){
                    //
                }
            };

            // save a reference to this modal
            xmodal.modals[modal.id] = modal;

            return modal;

        };
        //////////////////////////////////////////////////
        xmodal.show = xmodal.open;
        // preference is to call xmodal.open(),
        // but xmodal.show() should work too





        //////////////////////////////////////////////////
        // close the xmodal and delete its object
        //////////////////////////////////////////////////
        xmodal.close = function (_id, fade) {

            var $top, $modal, modal_id, close_modal=true;

            fade = fade || 0;

            // if there is no argument passed,
            // we'll use the last (top) open modal
            if (typeof _id == 'undefined'){
                $top = $(xmodal.dialog.top);
                $modal = ($top.length) ? $top.last() : $(xmodal.dialog.open).last() ;
                modal_id = $modal.attr('id');
            }
            else {
                // if a jQuery DOM object is passed in,
                // use it as-is, then get the id
                if (_id.jquery){
                    $modal = _id;
                    modal_id = $modal.attr('id');
                }
                // the only other option is to pass
                // the element's id
                else {
                    $modal = $('#'+_id);
                    modal_id = _id;
                }
            }

            var $mask = $('#' + modal_id + '-mask');

            // fade out then remove the modal
            $modal.fadeOut(fade, function(){

                // then if there's a mask that goes with it
                // get rid of that too
                if ($mask.length){
                    $mask.hide();
                }
                // don't remove 'static' modals we want to 'keep'
                if (!/keep|static/i.test($modal[0].className)) {
                    $modal.remove();
                    $mask.remove();
                }

                if (!$(xmodal.dialog.open).length) {
                    $body.removeClass('open');
                    $html.removeClass('noscroll');
                    return ; // be done if there are no more open modals
                }
                $(xmodal.dialog.open).last().addClass('top').focus();
            });

            // NONE OF THE CODE BELOW THIS WILL EXECUTE
            // IF NO MATCHING 'xmodal.modals[id]' OBJECT IS FOUND
            // (it's for your own good, kid)
            if (!xmodal.modals[modal_id]) { return } // should prevent errors if this xmodal.modals object doesn't exist

            // localize the object for this modal
            var modal = xmodal.modals[modal_id];

            if ($.isFunction(modal.onClose)) {
                close_modal = modal.onClose(modal);
            }

            // Return the template content to its original location
            // (if we used a template)
            //
            // IMPORTANT NOTE:
            // Any event listeners attached to template elements
            // will be removed because the original DOM elements
            // do not exist anymore - there are NEW elements that
            // match the old ones EXACTLY, but they're not the same.
            //
            // Attach listeners with jQuery's 'on' method, like:
            // $('body').on('click', '#element-id', function(e){ doSomething() });
            //
            // of course the lovely 'onclick' attributes will work too
            // :-/
            if (modal.$template) {
                modal.$template.html(modal.original.content);
            }

            // not sure why we'd want to delete an xmodal.modals[modal] object
            // for a modal that isn't the current one, but we can here.
            // I guess you could override the .deleteModal() method to not delete
            // anything at all - but why?
            if ( !isFalse(modal.deleteModal) ){

                // one last chance to do something with
                // this modal's object
                if ($.isFunction(modal.onDelete)){
                    modal.onDelete(modal);
                }

                xmodal.deleteModal(modal.id);

            }

            if ($.isFunction(modal.afterClose)) {
                modal.afterClose(modal);
            }

            // set the local 'modal' object to xmodal.closed
            // if we need a reference to the one we JUST closed
            return xmodal.closed = modal;

        };
        //////////////////////////////////////////////////




        //////////////////////////////////////////////////
        // close all open xmodal modals (except $not)
        //////////////////////////////////////////////////
        xmodal.closeAll = function( $modals, $not, _delete ){

            $modals = $$($modals) || $(xmodal.dialog.open);
            $modals = $modals.not($not);

            var timeout = 0;

            $($modals.get().reverse()).each(function(){

                var $modal = $(this);
                var id = $modal.attr('id');

                if (isTrue(_delete) || isFalse(_delete)) {
                    xmodal.modals[id].deleteModal = _delete;
                }

                setTimeout(function(){
                    xmodal.close($modal);
                }, (timeout += 10) );

            });
        };
        //////////////////////////////////////////////////




        //////////////////////////////////////////////////
        // maximize this modal
        //////////////////////////////////////////////////
        xmodal.maximize = function($modal){
            if (!$modal) { return }
            $modal = $$($modal);
            $modal.toggleClass('maxxed');
        };
        //////////////////////////////////////////////////




        //////////////////////////////////////////////////
        // preset for simple messages with one button
        //////////////////////////////////////////////////
        xmodal.alerts={};
        //xmodal.alerts.count = 0;
        //
        xmodal.presets.Message = function (opts) {
            opts = opts || {};
            //xmodal.alerts.count = ++xmodal.count;
            this.count = opts.count || ++xmodal.count;
            this.kind = 'alert';
            this.id = 'xmodal' + this.count + '-message';
            this.width = 400;
            this.height = 200;
            this.animation = 'fade';
            this.speed = 100;
            this.title = false;
            this.content = opts.content || opts.message || '(no message - must set value for "content" property)';
            this.buttons = {};
            this.buttons.ok = {
                label: opts.label || opts.button || opts.okLabel || opts.buttonLabel || 'OK',
                action: opts.action || opts.okAction || $.noop,
                close: !isFalse(opts.okClose),
                isDefault: true
            };
            //this.buttons.cancel = false; // prevents default "Cancel" button from rendering
            //this.closeModal = xmodal.closeModal = opts.closeModal || true ; // alert messages should be dismissed by pressing the enter key
            $.extend(true, this, opts);
        };
        //
        // xmodal.message() arguments:
        // (1 arg)  content/{opts} //-- message content OR config object
        // (2 args) title/content, content/{opts} //-- title and content OR content and config object
        // (3 args) title, content, buttonLabel/{opts} //-- title, content, and button label OR title, content, and config object
        // (4 args) title, content, buttonLabel, {opts} //-- title, content, button label, and config object
        // SEE switch STATEMENT FOR FURTHER EXPLANATION
        xmodal.message = function( /* accepts 1, 2, 3, or 4 args - see above */ ) {

            var opts = {};
            var arg1 = arguments[0];
            var arg2 = arguments[1];
            var arg3 = arguments[2];
            var arg4 = arguments[3];

            if (arguments.length === 0){
                throw new Error('Message text and/or configuration object required.');
            }

            // figure out what to do based on the number of arguments that are passed
            switch (arguments.length){
                //
                case 1:
                    // if there's one argument and it's not an object
                    // it must be a string for the message text
                    if (!$.isPlainObject(arg1)){
                        opts.content = arg1;
                    }
                    // if it's a plain object, set 'opts' to that
                    else {
                        opts = arg1;
                    }
                    break;
                //
                case 2:
                    // if there are two arguments,
                    // and the second one is NOT an object
                    // the first must be the title
                    // and the second the message text
                    if (!$.isPlainObject(arg2)){
                        opts.title   = arg1;
                        opts.content = arg2;
                    }
                    // if the second arg is an object,
                    // the first must be the message text
                    // and the second a config object
                    else {
                        opts = arg2;
                        opts.content = arg1;
                    }
                    break;
                //
                case 3:
                    // if there are three arguments,
                    // and the third is NOT an object,
                    // the third must be a custom button label
                    if (!$.isPlainObject(arg3)){
                        opts.buttonLabel = arg3;
                    }
                    // if the third arg is an object,
                    // it must be a config object
                    else {
                        opts = arg3;
                    }
                    // in all cases when there three args,
                    // the first must be the title,
                    // and the second must be the message text
                    opts.title = arg1;
                    opts.content = arg2;
                    break;
                //
                default:
                    // if all four arguments are present...
                    opts             = arg4;  // fourth is a config object
                    opts.title       = arg1;  // first is the title
                    opts.content     = arg2;  // second is the message content
                    opts.buttonLabel = arg3;  // third is a custom button label
            }

            // seems redundant to pass _opts to the constructor
            // as well as merging with the alert var
            var msg = new xmodal.presets.Message(opts);

            // or maybe not since we alias some properties (like okLabel)
            opts = $.extend(true, {}, msg, opts);

            opts.classNames = [(opts.classNames || opts.className || opts.classes || ''), 'xmodal-message'].join(' ');

            var modal = xmodal.open(opts);

            return { opts: opts, modal: modal };

        };
        // xmodal.alert('message text') may be more intuitive to call
        // as a replacement to the standard JavaScript alert() function.
        xmodal.alert = xmodal.message;
        // maybe "alert" is more descriptive than "message"?
        // but either will work
        //
        //sample usage:
        //    xmodal.alert('This is an alert.');
        //    xmodal.alert({
        //         title: 'Error',
        //         content: 'There was an error. Please fix it.',
        //         label: 'OK',
        //         action: function(){ console.log('There was an error with the data') },
        //         scroll: true
        //    });
        //////////////////////////////////////////////////




        xmodal.confirm = function(_opts){
            // pretty much the same as the Alert/Message, but with a cancel button.

            if (!$.isPlainObject(_opts)) {
                throw new Error('Please pass a configuration object as the argument for the xmodal.confirm(opts) function.');
            }

            _opts = _opts || {};

            var confirm={};
            confirm.count = _opts.count || ++xmodal.count;
            confirm.id = 'xmodal' + confirm.count + '-confirm';
            confirm.title = 'Confirmation';
            confirm.buttons = {};
            confirm.buttons.cancel = {
                // the 'ok' button is defined in xmodal.presets.Message
                label: _opts.cancelLabel || 'Cancel',
                action: _opts.cancelAction || $.noop,
                close: true
            };

            var opts = $.extend(true, {}, confirm, _opts);

            return xmodal.message(opts);

        };




        //////////////////////////////////////////////////
        // preset for simple 'loading' dialog
        //////////////////////////////////////////////////
        xmodal.loading={};
        xmodal.loading.count = 0;
        //
        xmodal.loading.open = function( /* {opts} || title, {opts} || title, id, {opts} */ ){

            var opts = {};
            opts.title = false;
            opts.id = 'xmodal-loading-' + (++xmodal.loading.count);
            opts.width = 260;
            opts.height = 90;
            opts.content = $('#xmodal-loading').html();
            opts.animation = false;
            opts.closeBtn = false;
            opts.footer = false;
            opts.classNames = [(opts.classNames || opts.className || opts.classes || ''), 'loader loading'].join(' ');

            var args = arguments.length;

            var arg1 = arguments[0],
                arg2 = arguments[1],
                arg3 = arguments[2];

            if (args === 1){
                if ($.isPlainObject(arg1)){
                    $.extend(true, opts, arg1);
                }
                else if (typeof arg1 == 'string'){
                    if (/^#/.test(arg1)) {
                        opts.id = arg1.split('#')[1] || '';
                    }
                    else {
                        opts.title = arg1;
                    }
                }
            }
            else if (args === 2){
                if ($.isPlainObject(arg2)){
                    $.extend(true, opts, arg2);
                }
                else if (typeof arg2 == 'string'){
                    opts.id = arg2;
                }
                opts.title = arg2.title || arg1;
            }
            else if (args === 3){
                $.extend(true, opts, arg3);
                opts.title = arg3.title || arg1;
                opts.id = arg3.id || arg2;
            }

            // don't open a second loader with the same id
            if (xmodal.modals._ids.indexOf(opts.id) > 0) {
                return false;
            }

            return xmodal.open(opts);

        };
        //
        xmodal.loading.close = function(_id){
            //var $loaders = $('div.xmodal.loading.open');
            //if (!$loaders.length) { return } // stop if there are no loading modals
            //var $top_loader = $loaders.last();
            //var id = _id || $top_loader.attr('id');
            //xmodal.loading.count--;
            var $loader = (_id) ? $$(_id) : $('div.xmodal.loading.open.top');
            xmodal.close($loader);
        };
        //
        xmodal.loading.closeAll = function(){
            xmodal.closeAll('div.xmodal.loading.open');
        };
        //////////////////////////////////////////////////




        //////////////////////////////////////////////////
        // preset for 'large' modal dialog
        //////////////////////////////////////////////////
        xmodal.large = function( _opts ){

            _opts = $.isPlainObject(_opts) ? _opts : {};

            // these properties cannot be overridden
            var dialog = {
                size: 'large',
                isDraggable: false,
                maximize: true,
                modal: true
            };

            var opts = $.extend(true, {}, _opts, dialog);

            return xmodal.open(opts);

        };




        //////////////////////////////////////////////////
        // preset for 'full-screen' modal dialog
        //////////////////////////////////////////////////
        xmodal.fullScreen = function( _opts ){

            _opts = $.isPlainObject(_opts) ? _opts : {};

            // these properties cannot be overridden
            var dialog = {
                size: 'max',
                isDraggable: false,
                modal: true
            };

            var opts = $.extend(true, {}, _opts, dialog);

            return xmodal.open(opts);

        };
        xmodal.max = xmodal.full = xmodal.fullScreen;




        //////////////////////////////////////////////////
        // preset for dialog containing an iframe
        //////////////////////////////////////////////////
        xmodal.iframe = function( url, opts ){

            // allow using a config object argument
            // with a 'url' or 'src' property
            if ($.isPlainObject(url)){
                opts = $.extend(true, {}, url);
                url = opts.url || opts.src || '';
            }

            opts = $.isPlainObject(opts) ? opts : {};

            var iframe = {
                    open: '<iframe ',
                    attrs: [],
                    close: '></iframe>'
                },
                dialog = {
                    size: 'med',
                    padding: '0',
                    maximize: true
                };

            iframe.attrs.push('src="' + url + '"');
            if (opts.name) {
                iframe.attrs.push('name="' + opts.name + '"');
            }
            iframe.attrs.push('seamless');
            iframe.attrs.push('style="width:100%;height:100%;position:absolute;border:none;"');

            opts.content = url ? (iframe.open+iframe.attrs.join(' ')+iframe.close) : '(no url specified)';

            return xmodal.open($.extend(true, {}, dialog, opts));

        };




    });
    // end $(function(){ });
    //////////////////////////////////////////////////




})();
