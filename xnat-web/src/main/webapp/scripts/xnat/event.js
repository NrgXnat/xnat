/*
 * web: event.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Helper functions for events.
 */

var XNAT = getObject(XNAT||{});

(function(XNAT){

    XNAT.event = getObject(XNAT.event||{});

    ////////////////////////////////////////////////////////////
    // CLICK EVENT HELPERS
    // - makes easy binding of clicks with modifier keys
    ////////////////////////////////////////////////////////////
    // USAGE
    // XNAT.event.click('#div1', doSomething).alt(doSomethingElse);
    // XNAT.event.click('#nav').shiftClick(shiftClickAction);
    // NOTE: event.preventDefault() is only called on modified clicks
    //       or links starting with '#'
    function Click(selector, fn){
        this.selector = selector;
        this.el$ = this.element$ = $$(selector); // support XNAT selector syntax with $$
        this.el = this.element = this.el$[0];
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

    Click.fn.ctrlAlt = Click.fn.altCtrl = function(fn){
        this.ctrlAltAction = fn;
        return this;
    };

    Click.fn.metaShift = Click.fn.shiftMeta = function(fn){
        this.metaShiftAction = fn;
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

    Click.fn.shift = Click.fn.shiftKey = function(fn){
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
        click.el$.click(function(e){

            var action = 'clickAction';

            // alt-shift click
            if (e.shiftKey && e.altKey){
                action = 'altShiftAction';
            }
            // ctrl-shift click
            else if (e.shiftKey && e.ctrlKey) {
                // prevent context menu on Macs?
                click.el$.on('contextmenu', function(e){
                    e.preventDefault();
                });
                action = 'ctrlShiftAction';
            }
            // ctrl-alt click
            else if (e.ctrlKey && e.altKey) {
                action = 'ctrlAltAction';
            }
            // shift-command/windows(meta) click
            else if (e.shiftKey && e.metaKey) {
                action = 'metaShiftAction';
            }
            // alt-click
            else if (e.altKey) {
                action = 'altAction';
            }
            // ctrl-click
            else if (e.ctrlKey) {
                // prevent context menu on Macs?
                click.el$.on('contextmenu', function(e){
                    e.preventDefault();
                });
                action = 'ctrlAction';
            }
            // shift-click
            else if (e.shiftKey) {
                action = 'shiftAction';
            }
            // command-click or windows(meta)-click
            else if (e.metaKey) {
                action = 'metaAction';
            }
            else {
                // 'clickAction' is the default value
                //action = 'clickAction';
            }

            // only prevent default for standard click handler on links
            // starting with '#' or elements with no 'href' attribute
            // (always prevent default on click events with modifier keys)
            if (action !== 'clickAction' && (!click.el.href || click.el.href.indexOf('#') === 0)){
                e.preventDefault();
            }

            // just one try
            try {
                click[action].call(click.el, e);
            }
            catch(e) {
                // fail silently
                //if (console && console.log) console.log(e);
            }

        });

        return click;
    };
    XNAT.click = XNAT.click$ =
        XNAT.event.click$ = XNAT.event.click;
    // add $ to hint we're using jQuery for events
    ////////////////////////////////////////////////////////////

})(XNAT);
