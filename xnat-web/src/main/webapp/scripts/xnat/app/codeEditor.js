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
                method: method || _this.opts.submitMethod || _this.opts.method,
                url: url || _this.opts.submitUrl || _this.opts.url,
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

        var modal = {};
        modal.width = 880;
        modal.height = 580;
        modal.scroll = false;
        modal.content = '';

        opts = cloneObject(opts);

        // insert additional content above editor
        if (opts.before) {
            modal.content += '<div class="before-editor">' + opts.before + '</div>';
            delete opts.before; // don't pass this to xmodal.open()
        }
        
        // div container for code editor
        modal.content += '<div class="code-editor" style="width:840px;height:440px;position:relative;"></div>';
        
        // insert additional content BELOW editor
        if (opts.after) {
            modal.content += '<div class="after-editor">' + opts.after + '</div>';
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

        // the 'beforeShow' and 'afterShow' methods
        // get an extra argument - the Editor instance

        var _beforeShow = opts.beforeShow;

        fn.beforeShow = function(dialog){
            _this.$editor = this.$modal.find('div.code-editor');
            _this.load();
            if (isFunction(_beforeShow)) {
                // '_this' is the Editor instance
                _beforeShow.call(this, dialog, _this)
            }
        };

        var _afterShow = opts.afterShow;

        fn.afterShow = function(dialog){
            if (isFunction(_afterShow)) {
                // '_this' is the Editor instance
                _afterShow.call(this, dialog, _this)
            }
            _this.aceEditor.focus();
        };

        modal.buttons = {
            save: {
                label: _this.isUrl ? 'Submit Changes' : 'Apply Changes',
                action: function(){
                    _this.save();
                },
                isDefault: true,
                close: false
            },
            close: {
                label: 'Cancel'
            }
        };
        
        // override modal options with {opts}
        this.dialog = xmodal.open(extend({}, modal, opts, fn));
        
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
    $('body').on('dblclick', '[data-code-editor]', function(){

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
