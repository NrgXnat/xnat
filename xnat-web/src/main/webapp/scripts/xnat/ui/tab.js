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
