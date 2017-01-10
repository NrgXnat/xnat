/*
 * web: tab.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Functions for creating XNAT tab UI elements
 */

var XNAT = getObject(XNAT||{});

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

    var tab;

    // just one tab
    XNAT.ui.tab = tab =
        getObject(XNAT.ui.tab || {});



}));
