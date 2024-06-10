/*
 * web: popup.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * XNAT pop-up windows
 */

var XNAT = getObject(XNAT||{});

(function(XNAT){

    var popup,
        window = this;
    
    // base popup function
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

        return window.top.open( url, title, paramsArray.join(',') );

    }
    window.popupCentered = popupCentered;
    window.centeredPopup = popupCentered;


    popup = function(/* url, title, w, h, y, params */){
        return popupCentered.apply(null, arguments);
    };


    // view XML pages in a centered browser popup window
    popup.viewXML = popup.xml = function(url, opts){
        opts = isString(opts) ? opts : 'status=yes,resizable=yes,scrollbars=yes,toolbar=no';
        return popupCentered(url, '', 960, 640, 4, opts)
    };


    // is the current window or 'loc' an XNAT popup?
    function isPopup(loc){
        loc = XNAT.url.splitUrl(loc || window.location.href);
        return (loc.base.indexOf('popup/true') > -1 || XNAT.url.getQueryStringValue('popup') === 'true')
    }
    // save popup status for global usage
    window.isPopup = isPopup();


    // process a url to add the 'popup' parts
    function setupUrl(fullUrl){

        var urlParts = XNAT.url.splitUrl(fullUrl),
            newUrl = XNAT.url.updateBase(fullUrl, urlParts.base.replace(/(\/popup\/(false|true))|(\/+$)/g,'') + '/popup/true');

        return XNAT.url.addQueryString(newUrl, '?popup=true')

    }
    popup.setupUrl = setupUrl;


    // set specified <a> links in popup windows to
    // open in 'popup' mode
    popup.setLink = function($a, loc){

        $a = $$($a||'a.popup');

        if (isPopup(loc)){
            $a.each(function(){
                var href = $(this).attr('href');
                $(this).attr('href', setupUrl(href));
            });
        }

    };


    // setup popup pages with forms to stay in 'popup' mode on submit
    // (also used in iframe dialogs)
    popup.setForm = function($form, loc){

        $form = $$($form||'form');

        if (isPopup(loc)){
            $form.each(function(){
                var _form = this;
                _form.action = setupUrl(_form.action);
            });
        }

    };


    XNAT.ui = getObject(XNAT.ui||{});
    XNAT.ui.popup = popup;


    $(function(){
        // process all <a> and <form> elements on DOM load
        if (isPopup()){
            popup.setLink('a.popup');
            popup.setForm('form');
        }
    });


})(XNAT);
