// helper functions for transitioning to new xmodal.js script

if (typeof xmodal == 'undefined') {
    throw new Error('The xmodal.js file must be included before this one.');
}

var xModal = xModal || {};

// NOTE: camel case "xModal"
xModal.Modal = function( _opts ){

    this._opts = _opts = _opts || {};

    $.extend( true, this, _opts );

    function isValue( a, b ){
        var undefined;
        if (arguments.length !== 2) return; // need exactly TWO args
        if (a != undefined){
            return (a.toString() === b.toString());
        }
        return undefined;
    }

    // if there's a value that's NOT the 'default'
    // set the value to false
    this.scroll = !(_opts.scroll && _opts.scroll !== 'yes');
    this.closeBtn = !(_opts.closeBtn && _opts.closeBtn !== 'show');

    if (_opts.id && isValue(_opts.content,'static')) {
        this.template = _opts.id
    }

    if (_opts.id && isValue(_opts.content,'existing')){
        this.template = _opts.id;
        this.padding = 0;
        this.footer = _opts.footer = false;
    }

    if (_opts.footer && _opts.footer !== 'show'){
        this.footer = false;
    }
    else {
        this.footer={};
        this.footer.buttons = !(_opts.footerButtons && _opts.footerButtons !== 'show');
        this.footer.content = _opts.footerContent;
        this.footer.background = _opts.footerBackground;
        this.footer.border = _opts.footerBorder;
        if (_opts.footerHeight) { this.footer.height = _opts.footerHeight }
    }

    var opts = $.extend(true, {}, this, _opts);
    this.opts = opts;

    return xmodal.open(opts);

};

function xModalOpenNew(opts){
    return new xModal.Modal(opts);
}
function xModalCloseNew(id){
    xmodal.close(id);
}

var xModalMessageCount = 0 ;

function xModalMessage( title, message, label, options ){
    xModalMessageCount++ ;
    var opts={};
    opts.title = title || ' ';
    opts.content = message || ' ';
    opts.buttonLabel = label || 'OK';
    $.extend(true, opts, options);
    return xmodal.message(opts);
}

function xModalConfirm(opts){
    return xmodal.confirm(opts);
}

function xModalLoadingOpen(opts){
    return xmodal.loading.open(opts.title, opts.id, opts);
}
function xModalLoadingClose(id){
    xmodal.loading.close(id);
}




// replace old XNAT YUI loading dialogs?
// these SHOULD work, but let's wait a bit
//function openModalPanel(id, title, opts){
//    //var opts = $.extend( true, { id: _id, title: _title }, _opts );
//    // 'title' and 'id' are switched (because 'id' can be auto-generated);
//    xmodal.loading.open( title, id, opts );
//}
//function closeModalPanel(id){
//    xmodal.loading.close(id);
//}
