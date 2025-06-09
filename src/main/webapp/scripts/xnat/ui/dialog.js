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

window.XNAT = getObject(window.XNAT || {});
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

    window.html$ = $('html');

    XNAT.ui =
        getObject(XNAT.ui || {});

    XNAT.ui.dialog = dialog =
        getObject(XNAT.ui.dialog || {});

    function isElement(it){
        return it && it.nodeType && it.nodeType === Node.ELEMENT_NODE;
    }

    function isFragment(it){
        return it && it.nodeType && it.nodeType === Node.DOCUMENT_FRAGMENT_NODE;
    }

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
    dialog.loader = {};
    dialog.loaders = [];

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
        Object.keys(dialog.dialogs).forEach(function(uid){
            if (!/loading/i.test(uid)) {
                dialog.uids.push(uid);
            }
            if (dialog.dialogs[uid].isOpen) {
                dialog.openDialogs.push(uid);
            }
        });
    };

    var scrollY = window.scrollY;


    function getModals(){
        dialog.openModals = dialog.openDialogs
                                  .filter(function(uid, i){
                                      return /^modal/.test(uid);
                                  })
                                  .map(function(uid, i){
                                      return {
                                          uid: uid,
                                          dialog: dialog.dialogs[uid]
                                      };
                                  });
        return getModals;
    }
    dialog.getModals = getModals();


    function setPosition(reset){
        scrollY = -window.body$.offset().top || window.scrollY;
        if (reset) {
            window.html$.removeClass('xnat-dialog-open');
            window.body$.removeClass('xnat-dialog-open');
            window.body$.css('top', 0);
            window.scrollTo(0, scrollY);
            scrollY = 0;
        }
        else {
            scrollY = window.scrollY;
            window.html$.addClass('xnat-dialog-open');
            window.body$.addClass('xnat-dialog-open');
            window.body$.css('top', -scrollY);
            window.scrollTo(0, 0);
            scrollY = 1;
        }
    }

    // update <body> className and window scroll position
    dialog.updateWindow = function(doUpdate){
        waitForElement(0, 'body', function(){

            window.body$ = window.body$ || $(document.body);

            // don't update anything if there are no 'modal' dialogs
            // or if the `openDialogs` count is more than `openModals`
            // ^^^ that means there are non-modal dialogs open
            if (!doUpdate) return;

            // calling this updates `dialog.openModals`
            getModals();

            if (!dialog.openDialogs.length || dialog.openDialogs.length > dialog.openModals.length) {
                setPosition(!dialog.openModals.length);
                return;
            }
            if (!scrollY && dialog.openModals.length) {
                setPosition(dialog.openModals.length !== 1)
            }
            else {
                setPosition(true);
            }
        });
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

        var isSubmit = _btn.isSubmit || _btn['submit'] || false;
        var isCancel = _btn.isCancel || _btn['cancel'] || false;

        var isDefault = isSubmit || _btn.isDefault || _btn['default'] || false;

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

            if (isDefault) {
                cls.push('default')
            }

            if (isSubmit) {
                cls.push('submit')
            }
            else if (isCancel) {
                cls.push('cancel')
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

        this.isReady = false;

        this.count = ++dialog.count;

        this.delay = this.delay || 0;

        this.speed = firstDefined(this.speed, 0);

        this.showMethod = (/^(0|-1)$/.test(this.speed + '') ? 'show' : this.showMethod || 'fadeIn');
        this.hideMethod = (/^(0|-1)$/.test(this.speed + '') ? 'hide' : this.hideMethod || 'fadeOut');

        this.zIndex = {};
        this.zIndex.mask = dialog.zIndexTop();
        this.zIndex.dialog = dialog.zIndexTop();

        this.maxxed = this.maxed = !!(this.maxxed || this.maxed);

        // will this dialog be 'modal' (with a mask behind it)
        this.isModal = firstDefined(this.isModal, this.mask, this.modal, true);

        // unique internal id for keeping track of dialogs
        // this can be pre-defined, but it MUST be unique
        this.uid = this.uid || (this.isModal ? randomID('modalx', false) : randomID('dialogx', false));

        this.id = this.id || this.uid || null;

        // mask div
        this.maskId = this.id + '-mask'; // append 'mask' to given id
        this.mask$ = this.isModal ? $.spawn('div.xnat-dialog-mask', {
            style: { zIndex: this.zIndex.mask },
            id: this.maskId
        }) : frag();
        this.$mask = this.__mask = this.mask$;
        this.mask0 = this.mask$[0];

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
                id: this.id, // use given id as-is or use uid-dialog
                attr: { tabindex: '0' },
                style: this.dialogStyle,
                on: {
                    mousedown: function(){
                        // only bring non-modal dialogs to top onclick
                        _this.toTop(false);
                    }
                }
            });

            // many aliases
            this.$modal = this.modal$ = this.__modal = this.$dialog = this.__dialog = this.dialog$;

            this.dialog0 = this.dialog$[0];

            if (this.header !== false) {

                // header (title) bar
                this.header$ = $.spawn('div.xnat-dialog-header.title');

                // set title: false to render just a 'handle'
                if (this.title !== false) {

                    // title content
                    this.title = this.title || '';

                    // title container (inner)
                    this.title$ =
                        $.spawn('div.xnat-dialog-title.inner').append(this.title);

                    this.title0 = this.title$[0];

                    // is there a 'close' button in the header?
                    this.closeBtn = (this.closeBtn !== undef) ? this.closeBtn : true;

                    // is there a 'maximize' button in the header?
                    // defaults to NO maximize button
                    this.maxBtn = firstDefined(this.maxBtn, false);

                    // header buttons (close, maximize)
                    this.headerButtons = {};

                    this.headerButtons.close$ = (this.closeBtn) ? $.spawn('b.close', {
                        title: 'click to close (alt-click to close all modals)'
                    }, [['i.fa.fa-close']]).on('click', function(e){
                        _this.close();
                        // option-click to close all open dialogs
                        if (e.altKey) {
                            dialog.closeAll();
                        }
                    }) : frag();

                    this.headerButtons.max$ = (this.maxBtn) ?
                        $.spawn('b.maximize', {
                            title: 'maximize/minimize this dialog'
                        }, [['i.fa.fa-expand']]).on('click', function(){
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

                this.header0 = this.header$[0];

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
                style: {
                    margin: pxSuffix(firstDefined(this.padding, 20))//,
                    //marginBottom: pxSuffix(firstDefined(this.padding, 20) + 10)
                }
            }).append(this.content);

            // make sure we have a footerHeight to calculate bodyHeight
            this.footerHeight = this.footerHeight || 50;

            this.windowHeight = window.innerHeight;

            // calculate dialog body max-height from window height
            this.bodyHeight = (this.windowHeight * 0.9) - this.footerHeight - 40 - 2;

            // [bodyConfig] allows customization of the dialog body <div>
            this.bodyConfig = extend(true, {
                style: { maxHeight: pxSuffix(this.bodyHeight) }
            }, this.bodyConfig || {});

            // body container
            this.dialogBody$ =
                $.spawn('div.body.content.xnat-dialog-body', this.bodyConfig)
                 .append(this.content$);

            this.$body = this.body$ = this.dialogBody$;

            this.body0 = this.body$[0];

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
                        close: firstDefined(this.okClose, true)
                    });
                    // default 'cancel' button
                    this.buttons.push({
                        id: this.cancelId || this.id + '-btn-cancel',
                        label: this.cancelLabel || 'Cancel',
                        className: 'cancel',
                        isDefault: false,
                        action: this.cancelAction || diddly,
                        close: firstDefined(this.cancelClose, true)
                    });
                }

                // tolerate 'buttons' object (instead of array)
                if (isPlainObject(this.buttons)) {
                    _this.btnTemp = [];
                    forOwn(this.buttons, function(name, obj){
                        addClassName(obj, name);
                        _this.btnTemp.push(obj)
                    });
                    this.buttons = _this.btnTemp;
                    delete _this.btnTemp;
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

            this.footer0 = this.footer$[0];

            // add the elements to the dialog <div>
            this.dialog$.append([
                this.header$,
                this.dialogBody$
            ]);

            if (this.hasFooter) {
                // this.content$[0].style.marginBottom = pxSuffix(this.footerHeight - 1);
                // insert an element to help with sizing when there's a footer
                this.dialog$.spawn('div.footer-pad', {
                    style: { height: pxSuffix(this.footerHeight) }
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

        // add styles to the dialog
        this.dialog$.css(this.style);

        // only set min/max if defined as a number or 'px' value
        if (this.minWidth || this.maxWidth || this.minHeight || this.maxHeight) {
            // don't stop rendering of the entire dialog if something goes wrong
            try {
                ['minWidth', 'maxWidth', 'minHeight', 'maxHeight'].forEach(function(prop){
                    var parts, val, unit;
                    if (_this[prop]) {
                        parts = ('' + _this[prop]).split(/[a-z]/i);
                        val = Number(parts[0].trim());
                        unit = (parts[1] || '').trim();
                        // if no 'unit' was found, it's px
                        if (!unit || /px/i.test(unit)) {
                            _this.dialog$.css(prop, pxSuffix(val));
                            _this.dialogBody$.css(prop, pxSuffix(val - _this.headerHeight - _this.footerHeight));
                        }
                    }
                });
            }
            catch(e){
                console.warn(e);
            }
        }

        if (this.maxxed) {
            this.dialog$.addClass('maxxed');
        }

        // add data-attributes
        addDataAttrs(this.dialog$[0], {
            uid: this.uid,
            count: this.count
        });

        addDataAttrs(this.mask$[0], {
            uid: this.uid,
            count: this.count
        });

        // add the container to the DOM (at the end of the <body>)
        waitForElement(1, 'body', function(){
            // maybe only do this on .show() ?
            $(document.body).append([
                _this.mask$,
                _this.dialog$
            ]);
        });

        if (this.classes || this.addClass) {
            this.mask$.addClass(this.classes || this.addClass);
            this.dialog$.addClass(this.classes || this.addClass);
        }

        // save a reference to this instance
        // (unless it's 'protected')
        if (this.protected === true) {
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

    // execute [callback] when dialog is added to the DOM
    Dialog.fn.ready = function(callback){
        debugLog('Dialog.fn.ready');
        var _this = this;
        var counter = 0;
        // set ['isReady'] to null to cancel further execution
        if (this.isReady === null) {
            return this;
        }
        else if (this.isReady) {
            callback.call(_this, _this);
        }
        else {
            waitForElement(0, 'body', function(){
                waitForElement(0, '#' + _this.id, function(){
                    // don't try more than 10,000 times. (10 seconds?)
                    if (++counter > 10000) { return }
                    _this.isReady = true;
                    callback.call(_this, _this);
                })
            });
        }
        return this;
    };

    Dialog.fn.setUID = Dialog.fn.setUid = function(uid){
        debugLog('Dialog.fn.setUID');
        this.uid = uid || this.uid || randomID('dlgx', false);
        dialog.dialogs[this.uid] = this;
        dialog.updateUIDs();
        return this;
    };

    // re-calculate height of modal body if window.innerHeight has changed
    Dialog.fn.setHeight = function(){
        debugLog('Dialog.fn.setHeight');
        return this.ready(function(){

            console.log('dialog#setHeight');

            // no need to set height if there's no content
            if (this.content === false) {
                return this;
            }

            var dlgHt, hdrHt, ftrHt, innerHt;

            if (this.dialog$ && this.dialog$.is(':visible')) {
                dlgHt = this.dialog$.is(':visible') ? this.dialog$.innerHeight() : 500;
                hdrHt = this.header$ && this.header$.length && this.header$.is(':visible') ? this.header$.innerHeight() || 0 : 0;
                ftrHt = this.footer$ && this.footer$.length && !isFragment(this.footer0) ? this.footer$.innerHeight() || 0 : 0;
            }

            innerHt = dlgHt - ftrHt - hdrHt;

            // only set max-height if the content overflows the available vertical space
            if (this.dialogBody$.outerHeight() > innerHt) {
                this.dialogBody$.css('maxHeight', innerHt);
            }

        });
    };

    Dialog.fn.setSpeed = function(speed){
        this.speed = realValue(speed + '' || this.speed + '' || 0);
        return this;
    };

    // focus on the dialog with optional callback
    Dialog.fn.setFocus = function(callback){
        return this.ready(function(){
            // this.dialog$.focus();

            if (isFunction(this.onFocus)) {
                this.onFocusResult = this.onFocus.call(this);
            }
            if (isFunction(callback)) {
                this.focusCallbackResult = callback.call(this);
            }

        });
    };

    // bring the dialog to the top
    Dialog.fn.toTop = function(topMask){
        // passing -1 to dialog.zIndexTop(-1)
        // just returns the topmost z-index
        // without changing the values
        var _topZ = dialog.zIndexTop(-1);
        var _thisZ = this.zIndex.dialog;
        // return if already on top
        // if (_thisZ >= _topZ) return this;
        // make sure this dialog is on top
        // otherwise...

        this.setFocus(function(){

            // remove 'top' class from existing dialogs
            forOwn(dialog.dialogs, function(uid, dlg){
                dlg.mask$.removeClass('top');
                dlg.dialog$.removeClass('top');
            });

            // set topMask argument to false to prevent bringing mask with the dialog
            if (firstDefined(topMask, true)) {
                this.zIndex.mask = dialog.zIndexTop();
                this.mask$.addClass('top').css('z-index', this.zIndex.mask);
            }

            this.zIndex.dialog = dialog.zIndexTop();
            this.dialog$.addClass('top').css('z-index', this.zIndex.dialog);

            dialog.topUID = this.uid;

        });

        return this;
    };

    // accepts the same arguments as jQuery's .show() method
    // http://api.jquery.com/show/
    Dialog.fn.show = function(duration, callback){

        var _this = this;

        this.ready(function(){

            this.setSpeed(firstDefined(duration, 0));

            if (this.isOpen || this.speed < 0) {
                return this;
            }

            if (isFunction(this.beforeShow)) {
                this.beforeShowResult = this.beforeShow.call(this, this);
                if (this.beforeShowResult === false) {
                    this.isReady = null;
                    return this;
                }
            }

            if (isFunction(this.onShow)) {
                this.onShowResult = this.onShow.call(this, this);
                if (this.onShowResult === false) {
                    this.isReady = null;
                    return this;
                }
            }

            function showCallback(){
                if (isFunction(_this.afterShow)) {
                    _this.afterShowResult = _this.afterShow.call(_this, _this, arguments);
                }
                if (isFunction(callback)) {
                    _this.showCallbackResult = callback.call(_this, _this, arguments);
                }
            }

            this.mask$[this.showMethod](this.speed * 0.6, function(){
                _this.mask$.addClass('open');
            });

            this.dialog$[this.showMethod](this.speed, function(){
                _this.dialog$.addClass('open');
                _this.setFocus(showCallback);
            });

            this.isOpen = true;
            this.isHidden = !this.isOpen;
            this.isReady = true;

            dialog.updateUIDs();

            if (this.isModal) {
                dialog.updateWindow(true);
            }

            this.setHeight();

        });

        return this;
    };

    // accepts the same arguments as jQuery's .hide() method
    // http://api.jquery.com/hide/
    Dialog.fn.hide = function(duration, callback){

        var _this = this;

        this.setSpeed(firstDefined(duration, 0));

        // explicitly set `callback` to false to
        // prevent any 'hide' callbacks from firing
        var doCallbacks = callback !== false;

        this.ready(function(){

            if (doCallbacks && isFunction(this.beforeHide)) {
                this.beforeHideResult = this.beforeHide.call(this, this);
                if (this.beforeHideResult === false) {
                    this.isReady = null;
                    return this;
                }
            }

            if (doCallbacks && isFunction(this.onHide)) {
                this.onHideResult = this.onHide.call(this, this);
                // return false from onHide to stop dialog from hiding
                if (this.onHideResult === false) {
                    this.isReady = null;
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

            this.mask$[this.hideMethod](this.speed * 0.6, function(){
                _this.mask$.removeClass('open top');
            });

            this.dialog$[this.hideMethod](this.speed * 0.3, function(){
                if (doCallbacks) hideCallback();
                _this.dialog$.removeClass('open top');
            });

            this.isHidden = true;
            this.isOpen = !this.isHidden;
            this.isReady = true;

            dialog.updateUIDs();

            // update classes on <html> and <body> elements
            if (this.isModal) {
                dialog.updateWindow(true);
            }

        });

        return this;
    };

    Dialog.fn.fadeIn = function(duration, callback){
        this.showMethod = 'fadeIn';
        this.setSpeed(firstDefined(duration, 100));
        this.show(this.speed, callback);
        return this;
    };

    Dialog.fn.fadeOut = function(duration, callback){
        this.hideMethod = 'fadeOut';
        this.setSpeed(firstDefined(duration, 100));
        this.hide(this.speed, callback);
        return this;
    };

    // clear out dialog body content, optionally saving previous contents
    Dialog.fn.empty = function(save){
        if (save === true) {
            this.previousContents = this.content$.children();
        }
        this.content$.empty();
        return this;
    };

    // replace body with new content, optionally
    // destructively deleting previous content
    Dialog.fn.replaceContent = Dialog.fn.update = function(empty, newContent){
        // remove the content from the dialog
        // but keep it in memory...
        this.previousContents = this.content$.children();
        this.allContents = (this.allContents || []).concat([this.previousContents]);
        if (empty === true || newContent === undef) {
            this.empty()
        }
        else {
            this.previousContents.detach();
        }
        newContent = newContent || empty;
        this.content$.append(newContent);
        return this;
    };


    // load the view at the specified views index
    Dialog.fn.loadView = function(idx){
        // TODO: support multiple content 'views'
    };

    // load the previous dialog 'view' content
    Dialog.fn.prevView = function(){
        // TODO: support multiple content 'views'
    };

    // load the next dialog 'view' content
    Dialog.fn.nextView = function(){
        // TODO: support multiple content 'views'
    };

    // create and setup the dialog
    // Dialog.fn.setup = function(opts){};

    // remove the dialog and all its events from the DOM
    Dialog.fn.destroy = function(){
        this.ready(function(){

            // handle cases when destroy is called directly, before hiding
            if (this.isOpen) {
                this.hide(0, false);
            }

            if (this.template$ && this.templateContent) {
                this.templateContent.detach();
                this.template$.empty().append(this.templateContent);
            }

            this.mask$.remove();
            this.dialog$.remove();

            // setting to null instead of deleting could offer more flexibility(?)
            // dialog.dialogs[this.uid] = null;
            delete dialog.dialogs[this.uid];
            dialog.updateUIDs();

        });

        return dialog.dialogs;
    };

    // render the elements and show the dialog immediately (on top)
    Dialog.fn.open = function(duration, callback){
        this.ready(function(){
            // use -1 to suppress opening
            this.setSpeed(firstDefined(duration, 0));
            this.toTop();
            if (this.isOpen || this.speed < 0) {
                return this;
            }
            try {
                this.show(this.speed, callback);
            }
            catch(e) {
                console.log(e);
            }
        });
        return this;
    };

    // hide the dialog optionally removing ALL elements
    // putting back the template HTML, if used
    Dialog.fn.close = function(destroy, duration, callback){
        this.ready(function(){
            // destroy by default when calling .close() method
            var _destroy = firstDefined(destroy, true);
            if (this.nuke === false || this.destroyOnClose === false) {
                // explicit "do not destroy"
                _destroy = false;
            }

            if (isFunction(this.onClose)) {
                this.onCloseResult = this.onClose.call(this, this);
                if (this.onCloseResult === false) {
                    this.isReady = null;
                    return this;
                }
            }

            this.setSpeed(firstDefined(duration || undef, 0));

            this.isReady = true;

            this.hide(this.speed, callback);

            if (isFunction(this.afterClose)) {
                this.afterCloseResult = this.afterClose.call(this, this);
                if (this.afterCloseResult === false) {
                    this.isReady = null;
                    return this;
                }
            }

            // TODO: to destroy or not to destroy?
            // TODO: ANSWER - ALWAYS DESTROY ON CLOSE
            if (_destroy) {
                this.destroy();
            }
        });

        return this;
    };

    // toggle visibility of the dialog
    Dialog.fn.toggle = function(method){
        this.ready(function(){
            if (this.isOpen) {
                method = method || this.hideMethod || 'hide';
            }
            else {
                method = this.showMethod || 'show';
            }
            if (/^(open|close|show|hide|fadeIn|fadeOut)$/i.test(method)) {
                this[method]();
            }
            else {
                this.mask$[method]();
                this.dialog$[method]();
            }
        });
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
        this.setHeight();
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

    // return a dialog instance directly or by uid
    dialog.getDialog = function(dlg){
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
            DLG = null;
        }
        return DLG;
    };

    // global function to toggle visibility of a dialog...
    // 'method' arg can be one of the following:
    // 'open', 'close', 'show', 'hide', 'fadeIn', 'fadeOut'
    // or... any jQuery method that toggles visibility
    dialog.toggle = function(dlg, method){
        var DLG = dialog.getDialog(dlg);
        if (DLG) { DLG.toggle(method) }
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

    // global function to hide ALL dialogs
    dialog.hideAll = function(){
        forOwn(dialog.dialogs, function(uid, dlg){
            dlg.toggle('hide');
        });
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
            buttons: opts.buttons || [{
                label: opts.buttonLabel || opts.okLabel || 'OK',
                close: firstDefined(opts.okClose, true),
                isDefault: true,
                isSubmit: true,
                action: opts.action || opts.okAction || diddly
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
                close: firstDefined(opts.cancelClose, true),
                isCancel: true,
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

    function loadingBarConfig(opts){
        return extend({
            id: 'loadingbar' + (loadingBarCounter += 1),
            width: 240,
            height: 'auto',
            top: 60,
            // delay: +delay,
            // destroyOnClose: false,
            protected: true // prevents being tracked in dialogs object
        }, opts);
    }

    dialog.loading = function(opts){

        var ldg = dialog.shell(loadingBarConfig(opts));

        var imgUrl = XNAT.url.rootUrl('/images/loading_bar.gif');
        var loadingBarImg = spawn('img|width=220|height=19', { src: imgUrl });

        ldg.dialog$.addClass('loader loading').append(loadingBarImg).hide();

        // override .show(), .open(), .hide(), and .close() methods
        // ldg.show = ldg.open = function(){
        //     ldg.toTop();
        //     ldg.mask$.show();
        //     ldg.dialog$.show();
        // };
        //
        // ldg.hide = ldg.close = function(){
        //     ldg.mask$.hide();
        //     ldg.dialog$.hide();
        // };
        //
        // ldg.destroy = ldg.remove = function(){
        //     ldg.mask$.remove();
        //     ldg.dialog$.remove();
        // };

        dialog.loader[ldg.uid] = ldg;
        dialog.loaders.push(ldg);

        return ldg;
    };

    dialog.loading.open = function(){
        return dialog.loading().open();
    };

    // close a specific 'loading' dialog by instance or UID
    dialog.loading.close = function(dlg){
        var ldg = dlg instanceof Dialog ? dlg : dialog.loader[dlg] || dialog.loaders.pop();
        if (ldg) {
            ldg.close();
            if (ldg.destroyOnClose !== false) {
                ldg.destroy();
            }
        }
        return ldg;
    };

    dialog.loading.closeAll = function(){
        forEach(dialog.loaders, function(dlg){
            dlg.close(true);
        })
    };


    // create and save a 'loadingBar' instance
    dialog.loadingBar = dialog.loading({ destroyOnClose: false });


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
            height: '85%',
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

    // load an html template into the dialog via ajax
    dialog.load = function (url, obj){

        if (!arguments.length) {
            console.error('dialog.load() requires at least a url or config object');
            return;
        }

        var tmpl, URL;

        var config = {
            title: '',
            width: 800,
            // height: 600,
            speed: 0,
            maxBtn: true,
            esc: false,
            enter: false,
            nuke: true,
            buttons: [{
                label: 'Close',
                isDefault: true,
                close: true
            }]
        };

        if (isPlainObject(url)) {
            extendDeep(config, url);
            URL = config.url;
        }
        else if (isString(url)) {
            extendDeep(config, obj);
            URL = url;
        }

        if (!url || !URL) {
            console.error('dialog.load() requires a "url" property');
            return;
        }

        config.content$ = $.spawn('div.load-content');
        config.content = config.content$[0];

        var DLG = dialog.init(config);

        config.content$.load(XNAT.url.rootUrl(URL), function(){
            // just wait for the content to load before opening the dialog...
            // ...THEN the heights of dialog elements should be calculated properly
            DLG.open();
        });

        return DLG;

    };

    // render a 'static' dialog with no title bar or footer to block user interraction
    dialog.static = function(message, opts){
        var cfg = {
            width: 300,
            header: false,
            footer: false,
            mask: true,
            padding: '0',
            top: '80px',
            classes: 'static',
            content: message || spawn('div.message.md', 'Please wait...')
        };
        return dialog.init(extend(true, cfg, opts));
    };

    dialog.static.message = function(message, opts){
        var cfg = { mask: false };
        return dialog.static(spawn('div.message.md', message), extend(true, cfg, opts)).open();
    };

    dialog.static.wait = function(message, opts){
        var msg = message || ' Please wait...';
        opts = getObject(opts);
        opts.classes = 'wait static';
        return dialog.static(spawn('div.message.waiting.md', msg), opts).open();
    };

    $(document).ready(function(){
        // generate the loadingBar on DOM ready
        var body$ = window.body$ = $(document.body);
        // elements with a 'data-dialog-load="/url/to/your/template.html" attribute
        // will render a new dialog that loads the specified template
        body$.on('click', '[data-dialog-load]', function(e){
            e.preventDefault();
            var this$ = $(this);
            var dialogOpts = this$.attr('data-dialog-opts');
            var config = dialogOpts ? parseOptions(dialogOpts) : {};
            config.url = this$.attr('data-dialog-load');
            dialog.load(config)
        });

        // body$.on('mousedown', 'div.xnat-dialog *', function(e){
        //     console.log('mousedown');
        //     var uid = $(this).closest('div.xnat-dialog').attr('data-uid');
        //     dialog.dialogs[uid].toTop(false);
        //     e.stopPropagation();
        // });

        // dialog.loadingbar = dialog.loadingBar = dialog.loading().hide();
    });

    return XNAT.ui.dialog = XNAT.dialog = dialog;

}));
