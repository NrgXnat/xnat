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

window.XNAT   = getObject(window.XNAT || {});
window.xmodal = getObject(window.xmodal);

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

    // get the highest z-index value for dialogs
    // created with either xmodal or XNAT.dialog
    dialog.zIndexTop = function(x){
        var xmodalZ = window.xmodal.topZ || dialog.zIndex;
        var dialogZ = dialog.zIndex;
        var _topZ = dialogZ > xmodalZ ? dialogZ : xmodalZ;
        return window.xmodal.topZ =
            dialog.zIndex =
                (_topZ + 1) + (x || 0);
    };

    // keep track of dialog objects
    dialog.dialogs = {};

    // keep track of special loading dialog objects
    dialog.loaders = {};

    // keep track of just uids
    dialog.uids = [];

    // keep track of OPEN dialogs
    dialog.openDialogs = [];

    // which dialog is on top?
    dialog.topUID = null;

    dialog.updateUIDs = function(){
        // save keys from dialog.dialogs
        dialog.uids = [];
        dialog.openDialogs = [];
        forOwn(dialog.dialogs).forEach(function(uid){
            if (!/loading/i.test(uid)) {
                dialog.uids.push(uid);
            }
            if (dialog.dialogs[uid].isOpen){
                dialog.openDialogs.push(uid);
            }
        });
    };

    dialog.bodyPosition = window.scrollY;

    dialog.getPosition = function(){
        var dialogPosition = dialog.bodyPosition;
        var yPosition = window.scrollY || dialogPosition;
        if (dialogPosition !== yPosition) {
            dialogPosition = yPosition;
        }
        return dialog.bodyPosition = dialogPosition;
    };

    // update <body> className and window scroll position
    dialog.updateWindow = function(isModal){
        // only change scroll and position for modal dialogs
        if (!firstDefined(isModal, false)) return;
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
        opts.html = _btn.label || _btn.html || _btn.text || 'OK';
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
                cls.push('button btn');
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
            var action = _btn.action || _btn.handler || _btn.onclick || diddly;
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

        this.delay = this.delay || 0;

        this.zIndex = {};
        // this.zIndex.container = dialog.zIndexTop();
        this.zIndex.mask      = dialog.zIndexTop();
        this.zIndex.dialog    = dialog.zIndexTop();

        this.maxxed = !!this.maxxed;

        this.id = this.id || null;

        // use an outer container for correct positioning
        // this.container$ = $.spawn('div.xnat-dialog-container', {
        //     id: (this.id || this.uid) + '-container',
        //     style: {
        //         display: 'none',
        //         zIndex: this.zIndex.container
        //     },
        //     data: {
        //         uid: this.uid,
        //         count: this.count
        //     }
        // });

        // will this dialog be 'modal' (with a mask behind it)
        this.isModal = firstDefined(this.isModal, this.mask, this.modal, true);

        // mask div
        this.mask$ = this.isModal ? $.spawn('div.xnat-dialog-mask', {
                style: { zIndex: this.zIndex.mask },
                id: (this.id || this.uid) + '-mask' // append 'mask' to given id
            }) : frag();
        this.$mask = this.__mask = this.mask$;

        // set up style object for dialog element
        this.dialogStyle = extend(true, {}, this.dialogStyle || this.style || {}, {
            zIndex: this.zIndex.dialog
        });

        // set content: false to prevent an actual dialog from opening
        // (will just render the container and mask with a 'shell' div)
        if (this.content === false) {

            this.dialogStyle.width = pxSuffix(this.width || '100%');

            this.dialog$ = $.spawn('div.xnat-dialog-shell', {
                id: this.id || (this.uid + '-shell'),
                style: this.dialogStyle
            });

        }
        else {

            this.dialogStyle.width = pxSuffix(this.width || 600);

            if (this.top) this.dialogStyle.top = this.top;
            if (this.bottom) this.dialogStyle.bottom = this.bottom;
            if (this.right) this.dialogStyle.right = this.right;
            if (this.left) this.dialogStyle.left = this.left;

            // outermost dialog <div>
            this.dialog$ = $.spawn('div.xnat-dialog', {
                id: this.id || this.uid, // use given id as-is or use uid-dialog
                attr: { tabindex: '0' },
                style: this.dialogStyle,
                on: {
                    mousedown: function(){
                        // only bring non-modal dialogs to top onclick
                        _this.toTop(false);
                    }
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
                        }, '&times;').on('click', function(e){
                            _this.close();
                            // option-click to close all open dialogs
                            if (e.altKey) {
                                dialog.closeAll();
                            }
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
            this.content = this.content || '<span>&nbsp;</span>';

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
                    $.spawn('span.content', [].concat(this.footerContent));

                // add default buttons if not defined
                if (this.buttons === undef) {
                    this.buttons = [];
                    // default 'ok' button
                    this.buttons.push({
                        id: this.okId || this.id + '-btn-ok',
                        label: this.okLabel || 'OK',
                        className: 'ok',
                        isDefault: true,
                        action: this.okAction || diddly,
                        close: this.okClose || true
                    });
                    // default 'cancel' button
                    this.buttons.push({
                        id: this.cancelId || this.id + '-btn-cancel',
                        label: this.cancelLabel || 'Cancel',
                        className: 'cancel',
                        isDefault: false,
                        action: this.cancelAction || diddly,
                        close: this.cancelClose || true
                    });
                }

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
        this.style.top = this.style.top || this.top || '3%';

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
        // this.container$.append([
        //     this.mask$,
        //     this.dialog$
        // ]);

        // add the container to the DOM (at the end of the <body>)
        if (window.body$.length) {
            window.body$.append([this.mask$, this.dialog$]);
            // window.body$.append(this.container$);
        }

        // save a reference to this instance
        // (unless it's 'protected')
        if (this.protected === true) {
            // this.container$.addClass('protected');
            this.mask$.addClass('protected');
            this.dialog$.addClass('protected');
        }
        else {
            dialog.dialogs[this.uid] = this;
        }

        dialog.updateUIDs();

    }

    Dialog.fn = Dialog.prototype;

    // add all of the content - title, body, footer, buttons, etc.
    // Dialog.fn.render = function(opts){};

    Dialog.fn.setUID = Dialog.fn.setUid = function(uid){
        this.uid = uid || this.uid || randomID('dlgx', false);
        dialog.dialogs[this.uid] = this;
        dialog.updateUIDs();
        return this;
    };

    // re-calculate height of modal body if window.innerHeight has changed
    Dialog.fn.setHeight = function(scale){

        // console.log('dialog-resize');

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
    Dialog.fn.toTop = function(topMask){
        // passing -1 to dialog.zIndexTop(-1)
        // just returns the topmost z-index
        // without changing the values
        var _topZ  = dialog.zIndexTop(-1);
        var _thisZ = this.zIndex.dialog;
        // return if already on top
        if (_thisZ >= _topZ) return this;
        // make sure this dialog is on top
        // otherwise...
        // remove 'top' class from existing dialogs
        forOwn(dialog.dialogs, function(uid, dlg){
            // dlg.container$.removeClass('top');
            dlg.mask$.removeClass('top');
            dlg.dialog$.removeClass('top');
        });
        // this.zIndex.container = dialog.zIndexTop();
        // this.container$.addClass('top').css('z-index', this.zIndex.container);

        // set topMask argument to false to prevent bringing mask with the dialog
        if (firstDefined(topMask, true)){
            this.zIndex.mask   = dialog.zIndexTop();
            this.mask$.addClass('top').css('z-index', this.zIndex.mask);
        }

        this.zIndex.dialog = dialog.zIndexTop();
        this.dialog$.addClass('top').css('z-index', this.zIndex.dialog);

        this.focus();

        dialog.topUID = this.uid;

        return this;
    };

    // accepts the same arguments as jQuery's .show() method
    // http://api.jquery.com/show/
    Dialog.fn.show = function(duration, callback){

        var _this = this;

        this.setSpeed(firstDefined(duration, 0));

        if (this.isOpen || this.speed < 0) {
            return this;
        }

        // if (!dialog.openDialogs.length) {
        //     dialog.bodyPosition = dialog.bodyPosition || window.scrollY;
        //     this.dialog$.css('top', dialog.getPosition() + (this.top || 0));
        // }

        if (isFunction(this.beforeShow)) {
            this.beforeShowResult = this.beforeShow.call(this, this);
            if (this.beforeShowResult === false) {
                return this;
            }
        }

        if (isFunction(this.onShow)) {
            this.onShowResult = this.onShow.call(this, this);
            if (this.onShowResult === false) {
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

        // this.container$[this.showMethod](this.speed * 0.3, function(){
        //     _this.container$.addClass('open')
        // });

        this.mask$[this.showMethod](this.speed * 0.6, function(){
            _this.mask$.addClass('open');
        });
        this.dialog$[this.showMethod](this.speed, function(){
            _this.dialog$.addClass('open');
            _this.focus(showCallback);
        });

        this.isOpen = true;
        this.isHidden = !this.isOpen;

        dialog.updateUIDs();
        dialog.getPosition();

        if (this.isModal) {
            dialog.updateWindow(this.isModal);
            window.html$.addClass('xnat-dialog-open');
            window.body$
                .addClass('xnat-dialog-open')
                .css('top', -dialog.bodyPosition);
        }

        // if (!dialog.openDialogs.length) {
        //     window.scrollTo(0, 0);
        // }

        return this;
    };

    // accepts the same arguments as jQuery's .hide() method
    // http://api.jquery.com/hide/
    Dialog.fn.hide = function(duration, callback){

        var _this = this;

        this.setSpeed(firstDefined(duration, 0));

        if (isFunction(this.beforeHide)) {
            this.beforeHideResult = this.beforeHide.call(this, this);
            if (this.beforeHideResult === false) {
                return this;
            }
        }

        if (isFunction(this.onHide)) {
            this.onHideResult = this.onHide.call(this, this);
            // return false from onHide to stop dialog from hiding
            if (this.onHideResult === false) {
                return this;
            }
        }

        function hideCallback(){
            if (isFunction(_this.afterHide)) {
                _this.afterHideResult = _this.afterHide.call(_this, _this);
            }
            if (isFunction(callback)) {
                _this.hideCallbackResult = callback.call(_this, _this);
            }
        }

        this.hideMethod = (/^(0|-1)$/.test(this.speed+'') ? 'hide' : this.hideMethod || 'fadeOut');

        this.mask$[this.hideMethod](this.speed * 0.6, function(){
            _this.mask$.removeClass('open top');
        });

        // TODO: figure out why the first argument would be an object?
        if (isPlainObject(arguments[0])) {
            // this.container$[this.hideMethod].apply(this.container$, arguments[0]);
            this.dialog$[this.hideMethod].apply(this.dialog$, arguments[0]);
        }
        else {
            // this.container$[this.hideMethod](this.speed, function(){
            //     hideCallback();
            //     _this.container$.removeClass('open top');
            // });
            this.dialog$[this.hideMethod](this.speed * 0.3, function(){
                hideCallback();
                _this.dialog$.removeClass('open top');
            });
        }

        this.isHidden = true;
        this.isOpen = !this.isHidden;

        dialog.updateUIDs();

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
        this.hideMethod = 'fadeOut';
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
        if (this.template$ && this.templateContent) {
            this.templateContent.detach();
            this.template$.empty().append(this.templateContent);
        }
        // this.container$.remove();
        this.mask$.remove();
        this.dialog$.remove();
        delete dialog.dialogs[this.uid];
        dialog.updateUIDs();
        dialog.updateWindow(this.isModal);
        return dialog.dialogs;
    };

    // render the elements and show the dialog immediately (on top)
    Dialog.fn.open = function(duration, callback){
        var _this = this;
        // use -1 to suppress opening
        this.setSpeed(firstDefined(duration, 0));
        this.toTop();
        if (this.isOpen || this.speed < 0) {
            return this;
        }
        if (this.delay){
            window.setTimeout(function(){
                try {
                    _this.show(_this.speed , callback);
                }
                catch(e) {
                    console.log(e);
                }
            }, _this.delay||1);
        }
        else {
            this.show(this.speed, callback);
        }
        return this;
    };

    // hide the dialog optionally removing ALL elements
    // putting back the template HTML, if used
    Dialog.fn.close = function(destroy, duration, callback){

        // destroy by default when calling .close() method
        var _destroy = firstDefined(destroy, true);

        if (isFunction(this.onClose)) {
            this.onCloseResult = this.onClose.call(this, this);
            if (this.onCloseResult === false) {
                return this;
            }
        }

        this.setSpeed(firstDefined(duration||undef, 0));

        this.hide(this.speed, callback);

        if (isFunction(this.afterClose)) {
            this.afterCloseResult = this.afterClose.call(this, this);
            if (this.afterCloseResult === false) {
                return this;
            }
        }

        // TODO: to destroy or not to destroy?
        // TODO: ANSER - ALWAYS DESTROY ON CLOSE
        if (this.nuke || this.destroyOnClose || _destroy) {
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

    // global function to toggle visibility of a dialog...
    // 'method' arg can be one of the following:
    // 'open', 'close', 'show', 'hide', 'fadeIn', 'fadeOut'
    // or... any jQuery method that toggles visibility
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
        else {
            return null;
        }
        if (!method && DLG.isOpen) {
            method = DLG.hideMethod || 'hide';
        }
        else {
            method = DLG.showMethod || 'show';
        }
        if (/^(open|close|show|hide|fadeIn|fadeOut)$/i.test(method)) {
            DLG[method]();
        }
        else {
            // DLG.container$[method]();
            DLG.mask$[method]();
            DLG.dialog$[method]();
        }
        return DLG;
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

    // global function to destroy an existing dialog
    dialog.destroy = function(dlg){
        var DLG = dialog.toggle(dlg, 'close');
        var UID;
        if (!DLG) return null;
        UID = DLG.uid;
        DLG.destroy();
        // return uid of destroyed dialog
        return UID;
    };

    // destroy all existing dialogs, whether they're visible or not
    dialog.destroyAll = function(){
        forOwn(dialog.dialogs, function(uid, dlg){
            dlg.destroy();
        })
    };

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
                opts = arg5 || {};  // fifth is a config object
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

    // use XNAT.ui.dialog.message the same as xmodal.message
    dialog.message = function(title, msg, okLabel, okAction, obj){
        var opts = message.apply(null, arguments);
        return dialog.init(opts).open();
    };

    // 'alert' dialog has no title text or title bar buttons
    dialog.alert = function(msg, okLabel, okAction, obj){
        return dialog.message(false, msg, okLabel, okAction, obj);
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
        // DO NOT render a 'close' button in the title bar
        // we want the user to explicitly confirm or cancel
        opts.closeBtn = false;
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

    var loadingBarCounter = 0;

    dialog.loading = function(delay, callback){
        // only one loadingBar?
        if (dialog.loadingBar instanceof Dialog) {
            // console.log('loadingBar');
            return dialog.loadingBar.toTop();
        }
        var LDG = dialog.shell({
            id: 'loadingbar' + (loadingBarCounter += 1),
            width: 240,
            height: 'auto',
            top: 60,
            delay: +delay,
            protected: true, // prevents being tracked in dialogs object
            destroyOnClose: false
        });
        LDG.dialog$
           .addClass('loader loading')
           .append(spawn('img', {
               src: XNAT.url.rootUrl('/images/loading_bar.gif'),
               width: 220,
               height: 19
           }));
        if (isFunction(callback)) {
            LDG.afterShow = callback;
        }
        dialog.loaders[LDG.uid] = LDG;
        return LDG;
    };

    dialog.loading.open = function(id, delay, callback){
        var LDG = dialog.loading(delay, callback);
        if (id) LDG.uid = LDG.id = id;
        dialog.loaders[LDG.uid] = LDG;
        return LDG.open();
    };

    // close a specific 'loading' dialog by UID
    dialog.loading.close = function(dlg, destroy){
        var ldg = isString(dlg) ? dialog.loaders[dlg] : dlg;
        var LDG = dialog.toggle(ldg, 'close');
        if (!LDG) return null;
        if (LDG.nuke || LDG.destroyOnClose || destroy) {
            LDG.destroy();
        }
        return LDG;
    };

    dialog.loading.closeAll = function(){
        forOwn(dialog.loaders, function(uid, dlg){
            dlg.close(true);
        })
    };

    $(function(){
        // generate the loadingBar on DOM ready
        window.body$ = $('body');
        dialog.loadingbar = dialog.loadingBar = dialog.loading().hide();
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
