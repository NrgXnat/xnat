/*
 * web: dialog.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * XNAT dialogs (replaces xmodal functions)
 */

var XNAT = getObject(XNAT || {});

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

    var undef, ui, dialog;
    var counter = 0;
    var NBSP = '&nbsp;';
    var diddly = function(){};

    window.body$ = $('body');
    window.html$ = $('html');

    XNAT.ui =
        getObject(XNAT.ui || {});

    XNAT.ui.dialog = dialog =
        getObject(XNAT.ui.dialog || {});

    dialog.count = 0;
    dialog.zIndex = 9000;

    // keep track of dialog objects
    dialog.dialogs = {};

    // keep track of just uids
    dialog.uids = [];

    // which dialog is on top?
    dialog.topUID = null;

    dialog.updateUIDs = function(){
        // save keys from dialog.dialogs
        dialog.uids = forOwn(dialog.dialogs);
    };

    dialog.bodyPosition = window.scrollY;

    // update <body> className and window scroll position
    dialog.updateWindow = function(){
        if (!window.body$.find('div.xnat-dialog.open').length) {
            window.html$.removeClass('xnat-dialog-open open');
            window.body$.removeClass('xnat-dialog-open open');
            window.body$.css('top', 0);
            window.scrollTo(0, dialog.bodyPosition);
        }
    };

    function frag(){
        return $(document.createDocumentFragment());
    }

    function pxSuffix(val){
        if (typeof val === 'number') {
            val = val + 'px'
        }
        return val;
    }

    function $footerButton(btn, i){

        var _this = this;

        var _btn = cloneObject(btn);
        var btnId = _btn.id || (_this.id || 'dialog-' + _this.count) + '-btn-' + (i + 1);

        // setup object for spawning the button/link
        var opts = {};

        // opts.tag = 'button';
        opts.id = btnId;
        opts.tabindex = '0';
        opts.html = _btn.label || _btn.html || _btn.text || '(no label)';
        opts.attr = _btn.attr || {};
        opts.attr.tabindex = 0;
        opts.className = (function(){
            var cls = [];
            if (_btn.className) {
                cls.push(_btn.className)
            }
            if (_btn.link || _btn.isLink) {
                cls.push('link');
                opts.tag = 'a';
                opts.href = '#!';
            }
            else {
                cls.push('button btn btn-sm');
            }
            if (_btn.isDefault || btn['default']) {
                cls.push('default')
            }
            if (_btn.close === true || _btn.close === undef) {
                cls.push('close');
                _btn.close = true;
            }
            return cls.join(' ');
        })();

        var btn$ = $.spawn('button', opts);

        btn$.on('click', function(e){
            e.preventDefault();
            var action = _btn.action || _btn.onclick || diddly;
            action.call(this, _this);
            if (_btn.close) {
                _this.close(_btn.destroy);
            }
        });

        return btn$;

    }

    function Dialog(opts){

        extend(true, this, opts);

        var _this = this;

        window.body$ = $('body');
        window.html$ = $('html');

        // unique internal id for keeping track of dialogs
        // this can be pre-defined, but it MUST be unique
        this.uid = this.uid || randomID('dlgx', false);

        this.count = ++dialog.count;

        this.zIndex = {};
        this.zIndex.container = ++dialog.zIndex;
        // this.zIndex.mask = ++dialog.zIndex;
        // this.zIndex.dialog = ++dialog.zIndex;

        this.maxxed = !!this.maxxed;

        this.id = this.id || null;

        // use an outer container for correct positioning
        this.container$ = $.spawn('div.xnat-dialog-container', {
            id: this.uid,
            style: {
                display: 'none',
                zIndex: this.zIndex.container
            },
            data: {
                uid: this.uid,
                count: this.count
            }
        });

        // will this dialog be 'modal' (with a mask behind it)
        this.isModal = firstDefined(this.isModal, this.mask, this.modal, true);

        // mask div
        this.mask$ = this.isModal ? $.spawn('div.xnat-dialog-mask', {
                // style: { zIndex: this.zIndex.mask },
                id: (this.id || this.uid) + '-mask' // append 'mask' to given id
            }) : frag();
        this.$mask = this.__mask = this.mask$;

        // set content: false to prevent an actual dialog from opening
        // (will just render the container and mask with a 'shell' div)
        if (this.content === false) {

            this.dialog$ = $.spawn('div.xnat-dialog-shell', {
                id: this.id || (this.uid + '-shell'),
                style: {
                    // zIndex: this.zIndex.dialog,
                    width: pxSuffix(this.width || '100%')
                }
            });

        }
        else {

            // outermost dialog <div>
            this.dialog$ = $.spawn('div.xnat-dialog', {
                id: this.id || (this.uid + '-dialog'), // use given id as-is or use uid-dialog
                attr: { tabindex: '0' },
                style: {
                    // zIndex: this.zIndex.dialog,
                    width: pxSuffix(this.width || 600)
                }
            });
            this.$modal = this.__modal = this.dialog$;

            if (this.header !== false) {

                // header (title) bar
                this.header$ =
                    $.spawn('div.xnat-dialog-header.title');

                // set title: false to render just a 'handle'
                if (this.title !== false) {

                    // title content
                    this.title = this.title || '';

                    // title container (inner)
                    this.title$ =
                        $.spawn('div.xnat-dialog-title.inner').append(this.title);

                    // is there a 'close' button in the header?
                    this.closeBtn = (this.closeBtn !== undef) ? this.closeBtn : true;

                    // is there a 'maximize' button in the header?
                    // defaults to NO maximize button
                    this.maxBtn = firstDefined(this.maxBtn || undef, false);

                    // header buttons (close, maximize)
                    this.headerButtons = {};

                    this.headerButtons.close$ = (this.closeBtn) ? $.spawn('b.close', {
                            title: 'click to close (alt-click to close all modals)'
                        }, '&times;').on('click', function(){
                            _this.close();
                        }) : frag();

                    this.headerButtons.max$ = (this.maxBtn) ?
                        $.spawn('b.maximize', {
                            title: 'maximize/minimize this dialog'
                        }, '&ndash;').click(function(){
                            _this.toggleMaximize();
                        }) : frag();

                    // add buttons to the header
                    this.headerContent = [
                        this.title$,
                        this.headerButtons.max$,
                        this.headerButtons.close$
                    ];

                }
                else {

                    this.headerContent = spawn('div.xnat-dialog-handle');
                    this.headerHeight = 20;
                    this.header$.css({ height: '20px' });

                }

                this.header$.append(this.headerContent);

            }

            // body content (text, html, DOM nodes, or jQuery-wrapped elements
            this.content = this.content || '';

            // if a template is specified, and no content, grab the template
            if (this.template && !this.content) {
                this.template$ = $$(this.template);
                this.templateContent = this.template$.clone(true, true);
                this.content = this.templateContent;
                // this.templateHTML = this.template$.html(); // do we NEED the HTML?
            }

            // body content (inner)
            this.content$ = $.spawn('div.inner.xnat-dialog-content', {
                style: { margin: pxSuffix(this.padding || 20) }
            }).append(this.content);

            // make sure we have a footerHeight to calculate bodyHeight
            this.footerHeight = this.footerHeight || 50;

            this.windowHeight = window.innerHeight;

            // calculate dialog body max-height from window height
            this.bodyHeight = (window.innerHeight * 0.9) - this.footerHeight - 40 - 2;

            // body container
            this.dialogBody$ = this.body$ = $.spawn('div.body.content.xnat-dialog-body', {
                style: { maxHeight: pxSuffix(this.bodyHeight) }
            }).append(this.content$);

            // footer (where the footer content and buttons go)
            if (this.footer !== false) {

                this.hasFooter = true;

                this.footer = getObject(this.footer);

                // reconcile footer stuff
                this.footerHeight = this.footerHeight || this.footer.height || 50;
                this.footerContent = this.footerContent || this.footer.content || '';

                // set default footer height (in pixels)
                this.footerHeightPx = pxSuffix(this.footerHeight);

                // footer content (on the left side)
                this.footerContent$ =
                    $.spawn('span.content', this.footerContent);

                // footer buttons (on the right side)
                this.footerButtons$ =
                    $.spawn('span.buttons', (this.buttons || []).map(function(btn, i){
                        // spawn a <button> element for each item in the 'buttons' array
                        return $footerButton.apply(_this, arguments);
                    }));

                this.footerInner$ = $.spawn('div.inner').append([
                    this.footerContent$,
                    this.footerButtons$
                ]);

                this.footer$ = $.spawn('div.footer.xnat-dialog-footer', {
                    style: { height: this.footerHeightPx }
                }).append(this.footerInner$);

            }
            else {
                this.footer$ = frag();
            }

            // add the elements to the dialog <div>
            this.dialog$.append([
                this.header$,
                this.body$
            ]);

            if (this.hasFooter) {
                // insert an element to help with sizing with a footer
                this.dialog$.spawn('div.footer-pad', {
                    style: { height: this.footerHeightPx }
                }, NBSP).append(this.footer$)
            }

            // enable dragging unless explicitly setting draggable: false
            if (this.draggable !== false) {
                this.dialog$.drags({ handle: this.header$ });
            }

        }

        // directly specify css for dialog element
        this.style = this.style || this.css || {};
        this.style.height = this.style.height || this.height || 'auto';
        this.style.top = this.style.top || this.top || 0;

        // only set min/max if defined
        ['minWidth', 'maxWidth', 'minHeight', 'maxHeight'].forEach(function(prop){
            if (_this[prop]) {
                _this.style[prop] = _this[prop]
            }
        });

        // add styles to the dialog
        this.dialog$.css(this.style);

        // add data-attributes
        addDataAttrs(this.dialog$[0], {
            uid: this.uid,
            count: this.count
        });

        // add the mask and the dialog box to the container
        this.container$.append([
            this.mask$,
            this.dialog$
        ]);

        console.log(window.body$.length);

        // add the container to the DOM (at the end of the <body>)
        if (window.body$.length) {
            window.body$.append(this.container$);
        }

        // save a reference to this instance
        // (unless it's 'protected')
        if (this.protected !== true) {
            dialog.dialogs[this.uid] = this;
        }

        dialog.updateUIDs();

    }

    Dialog.fn = Dialog.prototype;

    // add all of the content - title, body, footer, buttons, etc.
    // Dialog.fn.render = function(opts){};

    // re-calculate height of modal body if window.innerHeight has changed
    Dialog.fn.setHeight = function(scale){

        console.log('dialog-resize');

        var winHt = window.innerHeight;
        var ftrHt = this.footerHeight || 50;
        var hdrHt = this.headerHeight || 40;

        // no need to set height if there's no content
        if (this.content === false) {
            return this;
        }

        scale = scale || this.maxxed ? 0.98 : 0.9;

        this.bodyHeight = (winHt * scale) - ftrHt - hdrHt - 2;
        this.body$.css('maxHeight', this.bodyHeight);
        this.windowHeight = winHt;

        return this;
    };

    Dialog.fn.setSpeed = function(speed){
        this.speed = realValue(speed+'' || this.speed+'' || 0);
        return this;
    };

    // focus on the dialog with optional callback
    Dialog.fn.focus = function(callback){
        this.dialog$.focus();
        if (isFunction(this.onFocus)) {
            this.onFocusResult = this.onFocus.call(this);
        }
        if (isFunction(callback)) {
            this.focusCallbackResult = callback.call(this);
        }
        return this;
    };

    // bring the dialog to the top
    Dialog.fn.toTop = function(){
        // return early if already top
        if (dialog.topUID === this.uid) {
            return this;
        }
        // otherwise...
        // remove 'top' class from existing dialogs
        forOwn(dialog.dialogs, function(uid, dlg){
            dlg.container$.removeClass('top');
        });
        // and put it at the top
        this.zIndex.container = ++dialog.zIndex;
        this.container$.addClass('top').css('z-index', this.zIndex.container);
        this.mask$.addClass('top');
        this.dialog$.addClass('top');
        dialog.topUID = this.uid;
        return this;
    };

    // accepts the same arguments as jQuery's .show() method
    // http://api.jquery.com/show/
    Dialog.fn.show = function(duration, callback){

        var _this = this;

        this.setSpeed(firstDefined(duration, 0));

        if (this.speed === -1) {
            return this;
        }

        dialog.bodyPosition = window.scrollY;

        this.dialog$.css('top', dialog.bodyPosition + (this.top || 0));

        if (isFunction(this.beforeShow)) {
            this.beforeShowResult = this.beforeShow.call(this, this);
            if (this.beforeShowResult === false) {
                return this;
            }
        }

        function showCallback(){
            if (isFunction(_this.afterShow)) {
                _this.afterShowResult = _this.afterShow.call(_this, arguments);
            }
            if (isFunction(callback)) {
                _this.showCallbackResult = callback.call(_this, arguments);
            }
        }

        this.showMethod = (/^(0|-1)$/.test(this.speed+'') ? 'show' : this.showMethod || 'fadeIn');

        this.container$[this.showMethod](this.speed, function(){
            _this.container$.addClass('open')
        });

        this.mask$[this.showMethod](this.speed * 0.3, function(){
            _this.mask$.addClass('open');
        });
        this.dialog$[this.showMethod](this.speed * 0.6, function(){
            _this.dialog$.addClass('open');
            _this.focus(showCallback);
        });

        window.html$.addClass('xnat-dialog-open');
        window.body$.addClass('xnat-dialog-open').css('top', -dialog.bodyPosition);

        window.scrollTo(0, 0);

        return this;
    };

    // accepts the same arguments as jQuery's .hide() method
    // http://api.jquery.com/hide/
    Dialog.fn.hide = function(duration, callback){

        var _this = this;

        this.setSpeed(firstDefined(duration, 0));

        if (isFunction(this.onHide)) {
            this.onHideResult = this.onHide.call(this, this);
            // return false from onHide to stop dialog from hiding
            if (this.onHideResult === false) {
                return this;
            }
        }

        this.hideMethod = (/^(0|-1)$/.test(this.speed+'') ? 'hide' : this.hideMethod || 'fadeOut');

        this.dialog$[this.hideMethod](this.speed * 0.3, function(){
            _this.dialog$.removeClass('open top');
        });
        this.mask$[this.hideMethod](this.speed * 0.6, function(){
            _this.mask$.removeClass('open top');
        });

        function hideCallback(){
            if (isFunction(_this.afterHide)) {
                _this.afterHideResult = _this.afterHide.call(_this, _this);
            }
            if (isFunction(callback)) {
                _this.hideCallbackResult = callback.call(_this, _this);
            }
        }

        if (isPlainObject(arguments[0])) {
            this.container$[this.hideMethod].apply(this.container$, arguments[0]);
        }
        else {
            this.container$[this.hideMethod](this.speed, function(){
                hideCallback();
                _this.container$.removeClass('open top');
            });
        }

        // update classes on <html> and <body> elements
        dialog.updateWindow();

        return this;
    };

    Dialog.fn.fadeIn = function(duration, callback){
        this.showMethod = 'fadeIn';
        this.setSpeed(firstDefined(duration, 200));
        this.show(this.speed, callback);
        return this;
    };

    Dialog.fn.fadeOut = function(duration, callback){
        this.showMethod = 'fadeOut';
        this.setSpeed(firstDefined(duration, 200));
        this.hide(this.speed, callback);
        return this;
    };

    // clear out dialog body content
    Dialog.fn.empty = function(){
        this.content$.empty();
        return this;
    };

    // replace body with new content, optionally
    // destructively deleting previous content
    Dialog.fn.replaceContent = Dialog.fn.replace = function(empty, newContent){
        // remove the content from the dialog
        // but keep it in memory...
        // unless empty === true
        if (empty === true) {
            this.empty()
        }
        else {
            this.content$.children().detach();
        }
        this.content$.append(newContent);
        return this;
    };

    // create and setup the dialog
    // Dialog.fn.setup = function(opts){};

    // remove the dialog and all its events from the DOM
    Dialog.fn.destroy = function(){
        if (this.template$) {
            this.template$.empty().append(this.templateContent);
        }
        this.container$.remove();
        delete dialog.dialogs[this.uid];
        dialog.updateUIDs();
        dialog.updateWindow();
        return dialog.dialogs;
    };

    // render the elements and show the dialog immediately (on top)
    Dialog.fn.open = function(duration, callback){
        // use -1 to suppress opening
        this.setSpeed(firstDefined(duration, 0));
        if (this.speed === -1) {
            return this;
        }
        this.toTop();
        this.show(this.speed , callback);
        return this;
    };

    // hide the dialog optionally removing ALL elements
    // putting back the template HTML, if used
    Dialog.fn.close = function(destroy, duration, callback){

        if (isFunction(this.onClose)) {
            this.onCloseResult = this.onClose.call(this, this);
            if (this.onCloseResult === false) {
                return this;
            }
        }

        this.setSpeed(firstDefined(duration, 0));

        this.hide(this.speed, callback);

        if (isFunction(this.afterClose)) {
            this.afterCloseResult = this.afterClose.call(this, this);
            if (this.afterCloseResult === false) {
                return this;
            }
        }

        // TODO: to destroy or not to destroy?
        if (this.nuke || this.destroyOnClose || destroy === true) {
            this.destroy();
        }
        return this;
    };

    // un-maximize the dialog
    Dialog.fn.restore = function(){
        this.dialog$.removeClass('maxxed');
        this.maxxed = false;
        this.setHeight();
        return this;
    };

    // resize the dialog to the maximum size (98%)
    Dialog.fn.maximize = function(bool){
        if (bool === false) {
            this.restore();
            return this;
        }
        this.dialog$.addClass('maxxed');
        this.maxxed = true;
        this.setHeight(0.98);
        return this;
    };

    // toggle max/restore
    Dialog.fn.toggleMaximize = function(force){
        if (force !== undef) {
            this.maximize(force);
            return this;
        }
        this.dialog$.toggleClass('maxxed');
        this.maxxed = !this.maxxed;
        this.setHeight();
        return this;
    };

    // main function to initialize the dialog
    // WITHOUT showing it
    dialog.init = function(opts){
        var newDialog = new Dialog(opts);
        var resizeTimer = window.setTimeout(null, 60 * 60 * 1000);
        $(window).on('resize', function(){
            // console.log('window-resize');
            window.clearTimeout(resizeTimer);
            resizeTimer = window.setTimeout(function(){
                newDialog.setHeight();
            }, 200);
        });
        return newDialog;
    };

    // global function to toggle visibility of a dialog
    dialog.toggle = function(dlg, method){
        var DLG = null;
        if (dlg instanceof Dialog) {
            DLG = dlg;
        }
        else if (dialog.dialogs[dlg]) {
            DLG = dialog.dialogs[dlg];
        }
        else if (dialog.dialogs[dialog.topUID]) {
            DLG = dialog.dialogs[dialog.topUID];
        }
        return DLG ? DLG[method || 'show']() : null;
    };

    // global function to show an existing dialog
    dialog.show = function(dlg){
        return dialog.toggle(dlg, 'show');
    };

    // global function to hide ANY dialog
    dialog.hide = function(dlg){
        return dialog.toggle(dlg, 'hide');
    };


    ////////////////////////////////////////////////////////////
    // initialize AND open the dialog
    dialog.open = function(opts){
        return dialog.init(opts).open();
    };
    ////////////////////////////////////////////////////////////


    // global function to close and optionally destroy ANY dialog
    dialog.close = function(dlg, destroy){
        var DLG = dialog.toggle(dlg, 'close');
        if (!DLG) return null;
        if (DLG.nuke || DLG.destroyOnClose || destroy) {
            DLG.destroy();
        }
        return DLG;
    };

    // close ALL open dialogs
    dialog.closeAll = function(destroy){
        forOwn(dialog.dialogs, function(uid, dlg){
            dlg.close(destroy);
        })
    };

    // return the container, mask, and an empty <div>
    dialog.shell = dialog.shade = function(obj){
        var opts = cloneObject(obj);
        opts.content = false;
        return new Dialog(opts);
    };

    // set up a config object with defaults for a 'message' dialog
    function message(title, msg, btnLabel, action, obj){

        var opts = {};
        var arg1 = arguments[0];
        var arg2 = arguments[1];
        var arg3 = arguments[2];
        var arg4 = arguments[3];
        var arg5 = arguments[4];

        if (arguments.length === 0) {
            throw new Error('Message text or configuration object required.');
        }

        switch(arguments.length) {

            case 1:
                if (!isPlainObject(arg1)) {
                    opts.content = arg1;
                }
                else {
                    opts = arg1;
                }
                break;

            case 2:
                if (!isPlainObject(arg2)) {
                    opts.title = arg1;
                    opts.content = arg2;
                }
                else {
                    opts = arg2;
                    opts.content = arg1;
                }
                break;

            case 3:
                if (!isPlainObject(arg3)) {
                    opts.buttonLabel = arg3;
                }
                else {
                    opts = arg3;
                }
                opts.title = arg1;
                opts.content = arg2;
                break;

            case 4:
                if (isFunction(arg4)) {
                    opts.okAction = arg4;
                }
                else {
                    opts = arg4;
                }
                opts.title = arg1;
                opts.content = arg2;
                opts.buttonLabel = arg3;
                break;

            default:
                opts = arg5;  // fifth is a config object
                opts.title = arg1;  // first is the title
                opts.content = arg2;  // second is the message content
                opts.buttonLabel = arg3;  // third is a custom button label
                opts.okAction = arg4;  // fourth is the 'OK' callback
        }

        // add properties to 'this'
        opts = extend(true, {
            width: 400,
            //height: 200,
            title: NBSP,
            content: NBSP,
            maxBtn: false,
            nuke: true, // destroy on close?
            buttons: [{
                label: opts.buttonLabel || opts.okLabel || 'OK',
                close: firstDefined(opts.okClose || undef, true),
                isDefault: true,
                action: opts.okAction || diddly
            }]
        }, opts);

        return opts;
        //return dialog.init(opts).open();

    }

    dialog.message = function(title, msg, okLabel, okAction, obj){
        var opts = message.apply(null, arguments);
        return dialog.init(opts).open();
    };

    // simple confirmation dialog with 'Cancel' and 'OK' buttons
    dialog.confirm = function(title, msg, obj){
        var opts = message.apply(null, arguments);
        // add a default 'Cancel' button
        if (opts.buttons.length === 1) {
            opts.buttons.push({
                label: opts.cancelLabel || 'Cancel',
                close: firstDefined(opts.cancelClose || undef, true),
                action: opts.cancelAction || diddly
            })
        }
        return dialog.init(opts).open();
    };

    // loading image
    //dialog.loading = {};
    //dialog.loading.count = 0;
    //dialog.loading.init = function(open){
    //    var _loading = dialog.shell({
    //        width: 328,
    //        height: 'auto',
    //        top: 0
    //    });
    //    _loading.dialog$.addClass('loader loading');
    //    _loading.dialog$.append(spawn('img', {
    //        src: XNAT.url.rootUrl('/images/loading_bar.gif'),
    //        width: 300,
    //        height: 19
    //    }));
    //    window.setTimeout(function(){
    //        if (open) {
    //            _loading.open();
    //        }
    //    }, 1);
    //    return _loading;
    //};

    dialog.loading = function(){
        // only one loadingBar
        // if (dialog.loadingBar instanceof Dialog) {
        //     console.log('loadingBar present');
        //     return dialog.loadingBar;
        // }
        var LDG = dialog.shell({
            uid: 'loadingbar',
            width: 328,
            height: 'auto',
            top: 0,
            protected: true, // prevents being tracked in dialogs object
            destroyOnClose: false
        });
        LDG.dialog$
            .addClass('loader loading')
            .append(spawn('img', {
                src: XNAT.url.rootUrl('/images/loading_bar.gif'),
                width: 300,
                height: 19
            }));
        return LDG;
    };

    $(function(){
        // generate the loadingBar on DOM ready
        window.body$ = $('body');
        dialog.loadingbar = dialog.loadingBar = dialog.loading();
    });

    // open a dialog from the 'top' window
    // (useful for iframe dialogs)
    dialog.top = function(method, obj){
        if (isPlainObject(method)) {
            obj = cloneObject(method);
            method = 'open';
        }
        return window.top.XNAT.ui.dialog[method](obj);
    };

    // iframe 'popup' with sensible defaults
    dialog.iframe = function(url, title, width, height, opts){

        var config = {
            title: '',
            width: 800,
            height: 600,
            //mask: false,
            footer: false
        };

        if (isPlainObject(url)) {
            extendDeep(config, url);
        }
        else if (isPlainObject(title)) {
            config.src = url;
            extendDeep(config, title);
        }
        else {
            extendDeep(config, {
                src: url,
                title: title,
                width: width,
                height: height
            }, getObject(opts))
        }

        return xmodal.iframe(config);

    };

    return XNAT.ui.dialog = XNAT.dialog = dialog;

}));
