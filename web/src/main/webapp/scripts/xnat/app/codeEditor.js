/*
 * web: codeEditor.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * functions for XNAT generic code editor
 * xnat-templates/screens/Scripts.vm
 */

var XNAT = getObject(XNAT || {});

(function(XNAT){

    var codeEditor,
        xhr = XNAT.xhr;

    console.log('codeEditor.js');

    var csrfParam = {
        XNAT_CSRF: csrfToken
    };

    XNAT.app = getObject(XNAT.app || {});

    XNAT.app.codeEditor =
        codeEditor = getObject(XNAT.app.codeEditor || {});

    /**
     * Main Editor constructor
     * @param source String/Element - CSS selector, DOM object or jQuery object
     * @param opts Object - configuration object
     * @constructor
     */
    function Editor(source, opts){

        var _this = this;

        this.opts = cloneObject(opts);

        this.source = source;
        this.$source = $$(this.source) || {};

        // this will be defined when the dialog opens
        this.$editor = null;

        this.isInput = this.$source.is ? (function(){ return _this.$source.is(':input') })() : false;

        this.isUrl = !this.source && (this.opts.loadUrl || this.opts.load || this.opts.url);

        this.loadUrl = this.isUrl ? (this.opts.loadUrl || this.opts.load || this.opts.url) : null;

        // set default language for editor
        // add [data-code-language="javascript"] to source code element
        // for correct syntax highlighting
        this.language = this.opts.language || (this.$source.attr ? this.$source.attr('data-code-language') : '');

        this.getSourceCode = function(){
            if (this.isUrl){
                // set source to null or empty string
                // and opts.url = '/url/to/data' to
                // pull code from a REST call
                return XNAT.xhr.get(this.loadUrl);
            }
            else {
                // extract code from the source
                this.code = this.isInput ? this.$source.val() : this.$source.html ? this.$source.html() : '';
            }
            return this.code;
            // return {
            //     done: function(callback){
            //         callback.call(_this, _this.code);
            //     }
            // }
        };

        //
        this.getSourceCode();

    }

    Editor.fn = Editor.prototype;

    Editor.fn.getValue = function(editor){
        this.$editor = editor || this.$editor;
        this.code = this.$editor ? this.aceEditor.getValue() : this.getSourceCode();
        return this;
    };

    Editor.fn.save = function(method, url, opts){

        var _this = this;

        // call this on save to make sure we have the latest edits
        this.getValue();

        if (this.isUrl){
            // save via ajax
            return xhr.request(extend(true, {
                method: method || _this.opts.submitMethod || _this.opts.method || 'POST',
                url: url || _this.opts.submitUrl || _this.opts.url,
                // processData: false,
                data: this.code,
                success: function(){
                    _this.dialog.close()
                }
            }, opts))
        }
        else {
            // otherwise put the modified code
            // back where it came from
            if (this.isInput) {
                this.$source.val(this.code);
            }
            else {
                this.$source.text(this.code);
            }
            this.dialog.close()
        }
        
        return this;
        
    };

    Editor.fn.load = function(){

        var _this = this;

        _this.code = _this.getSourceCode();

        if (/json/i.test(_this.language)) {
            if (typeof _this.code === 'string') {
                _this.code = JSON.parse(_this.code);
            }
            _this.code = JSON.stringify(_this.code, null, 2);
        }

        var editor = spawn('div', {
            className: 'editor-content',
            html: '',
            style: {
                position: 'absolute',
                top: 0, right: 0, bottom: 0, left: 0,
                border: '1px solid #ccc'
            },
            done: function(){
                _this.aceEditor = ace.edit(this); // {this} is the <div> being spawned here
                _this.aceEditor.setTheme('ace/theme/eclipse');
                _this.aceEditor.getSession().setMode('ace/mode/' + stringLower(_this.language||''));
                _this.aceEditor.session.setValue(_this.code);
                // _this.aceEditor.setReadOnly(_this.readonly);
            }
        });

        // put the new editor div in the wrapper
        _this.$editor.empty().append(editor);

        return this;

    };

    Editor.fn.revert = function(){
        // TODO: reload original content
    };

    Editor.fn.closeEditor = function(){
        this.dialog.close();
        return this;
    };

    // open code in a dialog for editing
    Editor.fn.openEditor = function(opts){

        var _this = this,
            fn = {};

        var cfg = cloneObject(opts);

        var modal = {};
        modal.width = cfg.width ? Number((cfg.width + '').replace(/[a-z]+$/i, '')) : 880;
        modal.height = cfg.height ? Number((cfg.height + '').replace(/[a-z]+$/i, '')) : 580;
        modal.scroll = false;

        modal.content = spawn('div.code-editor-container', [
            cfg.before ? ['div.before-editor', [cfg.before]] : '',
            ['div.code-editor', {
                style: {
                    width: (modal.width - 40) + 'px',
                    height: (modal.height - 140) + 'px',
                    position: 'relative',
                    opacity: '0.01'
                }
            }],
            cfg.after ? ['div.after-editor', [cfg.after]] : ''
        ]).outerHTML;

        if (opts.before) {
            delete opts.before; // don't pass this to xmodal.open()
        }

        if (opts.after) {
            delete opts.after; // don't pass this to xmodal.open()
        }

        modal.title = 'XNAT Code Editor';
        modal.title += (_this.language) ? ' - ' + _this.language : '';
        // modal.closeBtn = false;
        // modal.maximize = true;
        modal.esc = false; // prevent closing on 'esc'
        modal.enter = false; // prevents modal closing on 'enter' keypress
        modal.footerContent = '<span style="color:#555;">' +
            (_this.isUrl ?
                'Changes will be submitted on save.' :
                'Changes are not submitted automatically.<br>The containing form will need to be submitted to save.') +
            '</span>';

        // the code editor 'beforeShow' and 'afterShow' methods
        // get an extra argument - the Editor instance

        var _beforeShow = opts.beforeShow;

        fn.beforeShow = function(obj){
            _this.$editor = obj.$modal.find('div.code-editor');
            if (isFunction(_beforeShow)) {
                // '_this' is the Editor instance
                _beforeShow.call(this, obj, _this)
            }
        };

        var _afterShow = opts.afterShow;

        fn.afterShow = function(obj){

            // try to adjust editor area height based on 'before' and/or 'after' content
            _this.$editor.css({
                height: _this.$editor.height() - obj.$modal.find('div.before-editor').outerHeight(true) - obj.$modal.find('div.after-editor').outerHeight(true)
            });

            // load after editor height has been set
            _this.load();
            _this.$editor.css('opacity', 1);

            if (isFunction(_afterShow)) {
                // '_this' is the Editor instance
                _afterShow.call(this, obj, _this)
            }

            _this.aceEditor.focus();
        };

        modal.buttons = [
            {
                label: _this.isUrl ? 'Submit Changes' : 'Apply Changes',
                action: function(){
                    _this.save();
                },
                isDefault: true,
                close: false
            },
            {
                label: 'Cancel'
            }
        ];
        
        // override modal options with {opts}
        this.dialog = XNAT.dialog.open(extend({}, modal, opts, fn));
        
        return this;

    };

    /**
     * Open a code editor dialog and apply edits to source. If source is a url, submit changes on close.
     * @param source String/Element - CSS selector string or DOM element that contains the source code to edit
     * @param opts - Object - config object
     * @returns {Editor}
     */
    codeEditor.init = function(source, opts){
        return new Editor(source, opts);
    };

    // bind codeEditor to elements with [data-code-editor] attribute
    // <textarea name="foo" data-code-editor="language:html;" data-code-dialog="title:Edit The Code;width:500;height:300;"></textarea>
    $(document).on('dblclick', '[data-code-editor]', function(){

        var $source = $(this),
            opts = parseOptions($source.dataAttr('codeEditor')),
            dialog = parseOptions($source.dataAttr('codeDialog'));

        var editor = codeEditor.init(this, opts);

        // if there's no title specified in [data-code-dialog]
        // and there IS a [title] on the source element,
        // use that title for the dialog
        if (!dialog.title && opts.title) {
            dialog.title = opts.title;
        }

        editor.openEditor(dialog);

    });

})(XNAT);
