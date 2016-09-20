/*
 * web: dialog.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * XNAT dialogs (uses xmodal.js)
 */

var XNAT = getObject(XNAT||{});

(function(XNAT){

    var ui, dialog;

    XNAT.ui = ui = getObject(XNAT.ui||{});
    XNAT.ui.dialog = dialog = getObject(XNAT.ui.dialog||{});

    //if (!window.top.xmodal){ return }

    dialog.top = function(method, obj){
        if (isPlainObject(method)){
            obj = cloneObject(method);
            method = 'open';
        }
        return window.top.xmodal[method](obj);
    };

    // xmodal.iframe 'popup' with sensible defaults
    dialog.iframe = function(url, title, width, height, opts){

        var config = {
            title: '',
            width: 500,
            height: 400,
            //mask: false,
            footer: false
        };

        if (isPlainObject(url)){
            extendDeep(config, url);
        }
        else if (isPlainObject(title)){
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

})(XNAT);
