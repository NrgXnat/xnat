/**
 * Helper function for creating centered popup windows.
 * Minimum required parameters:
 * popupCentered(url, title, width, height)
 * Optionally specify the ratio for vertical posotion (y)
 * and other parameters for rendering the window:
 * - menubar, toolbar, location, status,
 * - resizable, scrollbars, chrome
 * To override the automatic calculation of left & top,
 * you can include those in the param string: left=100,top=100
 * Likewise with width & height: width=600,height=400
 */

// make sure we've got a .trim() method
if (!String.prototype.trim) {
    String.prototype.trim = function () {
        return this.replace(/^\s+|\s+$/g, '');
    };
}

function popupCentered( /* url, title, w, h, y, params */ ) {

    // expected arguments:
    // url, title, w, h, y, params
    // ('y' is the divisor for vertical position)

    var url    = arguments[0],
        title  = arguments[1] || '',
        w      = arguments[2] || (window.innerWidth - 20),
        h      = arguments[3] || (window.innerHeight - 20),
        y      = arguments[4] || 2,
        params = arguments[5] || {},
        paramsLength=0,
        paramsArray=[];

    // the 'y' argument is optional:
    // if there are only 5 arguments,
    // the 'params' argument will be last
    if (arguments.length === 5 && !$.isNumeric(y)) {
        params = y;
        y = 2;
    }

    // pass a complete params string to explicitly use that
    // then convert the params string to a params object
    if (typeof params == 'string' && params > '') {

        paramsArray = params.replace(/\s/g,'').split(',');
        paramsLength = paramsArray.length;
        params={};

        for (var i=0, par=[]; i < paramsLength; i++){
            par = paramsArray[i].split('=');
            params[par[0]] = par[1]+'';
        }

    }

    // round to 'dec' decimal places
    function roundNumber(num, dec) {
        return Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
    }

    //y = y || 2; // make sure we've got SOME value to work with

    params.width  = params.width  || w ;
    params.height = params.height || h ;
    params.left   = params.left   || roundNumber((screen.width / 2) - (w / 2), 0);
    params.top    = params.top    || roundNumber((screen.height / y) - (h / y), 0);

    paramsArray = []; // reset the array before (re)creating it

    //'scrollbars=yes, resizable=yes, toolbar=no, location=no, directories=no, status=no, copyhistory=yes';
    for (var param in params){
        if (params.hasOwnProperty(param)){
            paramsArray.push(param + '=' + params[param]);
        }
    }

    return window.open( url, title, paramsArray.join(',') );

}

jQuery(function(){
    // set a 'data-popup' attribute on an element to trigger
    // a popup browser window with the parameters passed
    // usage:
    // <button data-popup="/images/image123.jpg ; Image Popup ; 600 ; 400 ; 3 ; status=yes">Open Popup</button>
    // parameters are separated by the semicolon ';' character (space around the semicolon will be stripped)
    var $ = jQuery;
    $(document.body).on('click', '[data-popup]', function(e){
        e.preventDefault();
        var args, url, title, w, h, y, params, delim;
        args = $(this).data('popup');
        // tolerate use of either | or ; for param delimiter
        delim = (args.indexOf('|') > 0) ? '|' : ';';
        args = args.split(delim);
        // split the args
        url    = args[0];
        title  = args[1] ? args[1].trim() : '';
        w      = args[2] ? args[2].trim() : null;
        h      = args[3] ? args[3].trim() : null;
        y      = args[4] ? args[4].trim() : null;
        params = args[5] ? args[5].trim() : null;
        popupCentered( url, title, w, h, y, params );
    });
});

