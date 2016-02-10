/*!
 * Helper functions for events.
 */

var XNAT = getObject(XNAT||{});

(function(XNAT){

    var undefined;

    XNAT.event = getObject(XNAT.event||{});

    ////////////////////////////////////////////////////////////
    // CLICK EVENT HELPERS
    // - makes easy binding of clicks with modifier keys
    ////////////////////////////////////////////////////////////
    // USAGE
    // XNAT.event.click('div1', doSomething).alt(doSomethingElse);
    // .preventDefault() is called on all actions
    function Click(selector, fn){
        this.selector = selector;
        this.$el = this.$element = $(selector);
        this.el = this.element = this.$el[0];
        this.clickAction = fn;
    }

    Click.fn = Click.prototype;

    Click.fn.altShift = Click.fn.shiftAlt = function(fn){
        this.altShiftAction = fn;
        return this;
    };

    Click.fn.ctrlShift = Click.fn.shiftCtrl = function(fn){
        this.ctrlShiftAction = fn;
        return this;
    };

    Click.fn.alt = Click.fn.opt = Click.fn.option = function(fn){
        this.altAction = fn;
        return this;
    };

    Click.fn.ctrl = Click.control = function(fn){
        this.ctrlAction = fn;
        return this;
    };

    Click.fn.shift = function(fn){
        this.shiftAction = fn;
        return this;
    };

    Click.fn.meta = Click.fn.cmd = Click.fn.command = function(fn){
        this.metaAction = fn;
        return this;
    };


    XNAT.event.click = function(selector, action){

        var click = new Click(selector, action);

        // just a single click event handler for all chained methods
        click.$el.click(function(e){

            e.preventDefault();

            if (e.shiftKey && e.altKey){
                try { click.altShiftAction.call(click.el, e) }
                catch(e) { if (console && console.log) console.log(e) }
            }

            else if (e.shiftKey && e.ctrlKey) {
                // prevent context menu on Macs?
                click.$el.on('contextmenu', function(e){
                    e.preventDefault();
                });
                try { click.ctrlShiftAction.call(click.el, e) }
                catch(e) { if (console && console.log) console.log(e) }
            }

            else if (e.altKey) {
                try { click.altAction.call(click.el, e) }
                catch(e) { if (console && console.log) console.log(e) }
            }

            else if (e.ctrlKey) {
                // prevent context menu on Macs?
                click.$el.on('contextmenu', function(e){
                    e.preventDefault();
                });
                try { click.ctrlAction.call(click.el, e) }
                catch(e) { if (console && console.log) console.log(e) }
            }

            else if (e.shiftKey) {
                try { click.shiftAction.call(click.el, e) }
                catch(e) { if (console && console.log) console.log(e) }
            }

            else if (e.metaKey) {
                try { click.metaAction.call(click.el, e) }
                catch(e) { if (console && console.log) console.log(e) }
            }

            else {
                try { click.clickAction.call(click.el, e) }
                catch(e) { if (console && console.log) console.log(e) }
            }

        });
        return click;
    };
    ////////////////////////////////////////////////////////////

})(XNAT);

function isPlainObject( obj ){
    return Object.prototype.toString.call(obj) === '[object Object]';
}
function getObject( obj ){
    return isPlainObject(obj) ? obj : {};
}
