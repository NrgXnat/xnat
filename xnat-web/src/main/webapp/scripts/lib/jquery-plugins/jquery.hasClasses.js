/*!
 * jQuery.hasClasses plugin
 *
 * Check if an element has ALL classes
 * or ANY of a number of classes
 *
 * usage:
 * $('#el-id').hasClasses('foo || bar')   <- has EITHER 'foo' or 'bar'
 * $('#el-id').hasClasses('|| foo bar')   <- has EITHER 'foo' or 'bar'
 * $('#el-id').hasClasses('any: foo bar') <- has EITHER 'foo' or 'bar'
 *
 * $('#el-id').hasClasses('foo && bar')   <- must have BOTH 'foo' AND 'bar'
 * $('#el-id').hasClasses('&& foo bar')   <- must have BOTH 'foo' AND 'bar'
 * $('#el-id').hasClasses('all: foo bar') <- must have BOTH 'foo' AND 'bar'
 *
 * or call hasAnyClass() or hasAllClasses directly
 * $('#el-id').hasAnyClass('foo bar') <- has EITHER 'foo' or 'bar'
 * $('#el-id').hasAllClasses('foo bar') <- must have BOTH 'foo' AND 'bar'
 *
 */

(function($){

    // wrap jQuery's .hasClass in a local function
    function hasClassName($element, className) {
        return $element.hasClass(className);
    }

    // remove 'all:', 'any:', delimeters '||' and '&&'
    // and any bogus commas or periods
    function splitClassNames(classNames){
        return classNames.replace(/\.|,|any:|all:|\|\||&&/g,' ').trim().split(/\s+/);
    }

    $.fn.hasAnyClass = function(classNames) {
        var i = -1,
            classes = splitClassNames(classNames),
            len = classes.length;
        while (++i < len) {
            if (hasClassName(this, classes[i])) {
                return true;
            }
        }
        return false;
    };

    $.fn.hasAllClasses = function(classNames) {
        var i = -1,
            classes = splitClassNames(classNames),
            len = classes.length,
            matches = 0;
        while (++i < len) {
            if (hasClassName(this, classes[i])) {
                matches++;
            }
        }
        return matches === len;
    };

    $.fn.hasClasses = function( classNames ){
        var any = false;
        // forgive stupidity if || is included in an 'all:' check
        if (classNames.indexOf('all:') === 0){
            any = false;
        }
        else if (classNames.indexOf('any:') === 0){
            any = true;
        }
        else if (classNames.indexOf('||') > -1){
            any = true;
        }
        // 'all' is default check if no 'any' indicators are present
        return any ? this.hasAnyClass(classNames) : this.hasAllClasses(classNames);
    };

})(jQuery);
