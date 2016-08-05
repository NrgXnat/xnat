/**
 * functions for XNAT generic code editor
 * xnat-templates/screens/Scripts.vm
 */

var XNAT = getObject(XNAT || {});

(function(XNAT){

    var codeEditor,
        xhr = XNAT.xhr;

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
        this.$source = $$(this.source);

        // this will be defined when the dialog opens
        this.$editor = null;

        this.isInput = (function(){ return _this.$source.is(':input') })();

        this.isUrl = !this.source && this.opts.url;

        // set default language for editor
        // add [data-code-language="javascript"] to source code element
        // for correct syntax highlighting
        this.language = this.opts.language || this.$source.attr('data-code-language');

        this.getSourceCode = function(){
            if (this.isUrl){
                // set source to null or empty string
                // and opts.url = '/url/to/data' to
                // pull code from a REST call
                this.code = '';
            }
            else {
                // extract code from the source
                this.code = this.isInput ? this.$source.val() : this.$source.html();
            }
            return this.code
        };

        //
        this.getSourceCode();

    };

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
                method: method,
                url: url,
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

        var _this = this;

        var modal = {};
        modal.width = 880;
        modal.height = 580;
        modal.scroll = false;
        modal.content = '';

        opts = cloneObject(opts);

        // insert additional content above editor
        if (opts.before || opts.contentTop) {
            modal.content += opts.before || opts.contentTop;
        }
        
        // div container for code editor
        modal.content += '<div class="code-editor" style="width:840px;height:440px;position:relative;"></div>';
        
        // insert additional content BELOW editor
        if (opts.after || opts.contentBottom) {
            modal.content += opts.after || opts.contentBottom;
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
        modal.beforeShow = function(obj){
            _this.$editor = obj.$modal.find('div.code-editor');
            _this.load();
        };
        modal.afterShow = function(){
            _this.$editor.focus();
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
        extend(modal, opts);

        this.dialog = xmodal.open(modal);
        
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

})(XNAT);

