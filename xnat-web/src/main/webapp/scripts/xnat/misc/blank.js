/*!
 * Empty starter JavaScript file
 */

var XNAT = getObject(XNAT);

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

    var undefined, blank;

    XNAT.misc = getObject(XNAT.misc || {});
    
    XNAT.misc.blank = blank = 
        getObject(XNAT.misc.blank || {});
    
    
    // init function for XNAT.misc.blank
    blank.init = function(){
        
    };
    
    
    //////////////////////////////////////////////////
    
    
    
    
    
    // CODE GOES HERE
    
    
    
    
    
    //////////////////////////////////////////////////
    
    
    
    
    
    // this script has loaded
    blank.loaded = true;

    return XNAT.misc.blank = XNAT.blank = blank;
    
}));
