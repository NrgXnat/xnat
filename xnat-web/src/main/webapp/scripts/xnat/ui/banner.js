/*
 * web: banner.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * Display a banner in the header when stuff happens
 */

/*
*
* <div id="success-banner" class="success" style="top: 0px; position: absolute; left: 0px; right: 0px; bottom: auto; display: none;">Save complete.</div>
*
*/
var XNAT = getObject(XNAT || {});

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

    var banner, undefined;

    XNAT.ui =
        getObject(XNAT.ui || {});

    XNAT.ui.banner = banner =
        getObject(XNAT.banner || {});

    // set a universal default container
    banner.container = banner.container || '#header-banner';

    function Banner(opts){

        // set default values
        this.type = 'message';
        this.container = 'body';

        // allow passing properties to the div via
        // spawn.js config object named 'element'
        this.element = {};

        this.effect = 'fade';
        this.speed = 200;
        this.hold = 2000;

        // 'opts' overrides defaults
        extend(true, this, opts);

        this.speedIn = this.speedIn || this.speed;
        this.speedOut = this.speedOut || this.speed;

        this.content = this.content || this.html || this.text || '&nbsp;';

        // spawn the div
        this.newBanner = spawn('div.banner', this.element, this.content);
        this.banner = this.newBanner;

        // wrap it in jQuery
        this.$newBanner = $$(this.newBanner).hide();
        this.$banner = this.$newBanner.addClass(this.type);

        if (this.container) {
            // wrap up the container
            this.$container = $$(this.container);
            // put the banner in the container
            // maybe not?
            // this.$container.append(this.newBanner);
        }
    }

    Banner.fn = Banner.prototype;

    Banner.fn.remove = function(){
        this.$newBanner.remove();
    };

    // hide the banner, using a transition, if specified
    // Banner.fn.hide = function(effect, speed){
    //
    // };

    Banner.fn.reveal = function(hold, type, speedIn, speedOut){
        var _this = this,
            _in = 'show',
            _out = 'hide';
        hold = hold || this.hold || 2000;
        type = type || this.effect;
        speedIn = speedIn || this.speedIn;
        speedOut = speedOut || this.speedOut || 500;
        if (type === 'fade') {
            _in = 'fadeIn';
            _out = 'fadeOut';
        }
        else if (type === 'slide') {
            _in = 'slideDown';
            _out = 'slideUp';
        }
        else {
            speedIn = 0;
            speedOut = 0;
        }
        if (hold === true){
            this.$newBanner[_in](speedIn);
        }
        else {
            this.$newBanner[_in](speedIn).delay(hold)[_out](speedOut, function(){
                _this.$banner.remove();
            });
        }
        return this;
    };
    Banner.fn.animate = Banner.fn.reveal;

    Banner.fn.fade = function(hold, speedIn, speedOut){
        this.effect = 'fade';
        this.animate(hold, this.effect, speedIn, speedOut);
        return this;
    };

    Banner.fn.slide = function(hold, speedIn, speedOut){
        this.effect = 'slide';
        this.animate(hold, this.effect, speedIn, speedOut);
        return this;
    };

    Banner.fn.show = function(hold) {
        this.$newBanner.show();
        if (hold && hold !== true) {
            this.$newBanner.delay(hold||2000).hide();
        }
        return this;
    };

    // retrieve the <div.banner> element
    Banner.fn.get = function(){
        return this.newBanner;
    };
    // retrieve jQuery-wrapped element
    Banner.fn.get$ = Banner.fn.$get = function(){
        return this.$newBanner;
    };

    Banner.fn.render = function(container, method){
        this.$container = container !== undefined ? $$(container) : this.$container;
        this.$container[method||'append'](this.newBanner);
        this.reveal();
        return this;
    };

    banner.sampleConfig = {
        content: 'The text for the banner',
        //'text/html': '(aliases for content)',
        speed: 200, // how fast do we want to fade/slide in/out
        hold: 1000, // how long should it show before hiding?
        //delay': '(alias for hold)',
        //duration: '(alias for hold)',
        effect: false, // 'fade', 'slide', false - false does show()
        //transition: '(alias for effect)',
        container: '#banner-container' // selector, jQuery object, or DOM element that will contain the banner
    };

    banner.init = function(opts){
        var bnr = new Banner(opts);
        return bnr.reveal();
    };

    banner.notify = function(text, opts){
        opts = getObject(opts);
        opts = extend(true, {
            type: 'message',
            content: text||'Notified.',
            effect: 'slide',
            speedIn: 200,
            hold: 2000,
            speedOut: 500,
            element: {
                style: { width: opts.width || '400px' }
            }
        }, opts);
        var bnr = new Banner(opts);
        return bnr.reveal();
    };

    banner.top = function(hold, text, type, width){
        var opts = {
            type: type || 'message',
            content: text || 'Hello.',
            effect: 'slide',
            hold: hold || 0,
            container: 'body',
            element: {
                addClass: 'top-banner',
                style: {
                    position: 'fixed',
                    top: '5px',
                    left: 0,
                    right: 0,
                    bottom: 'auto',
                    width: width||'400px',
                    margin: '0 auto',
                    zIndex: 99999
                }
            }
        };
        var bnr = new Banner(opts);
        bnr.$banner.click(function(){ $(this).slideUp(100) });
        return bnr.render(opts.container);
    };

    banner.success = function(text, opts){
        opts = extend(true, {
            type: 'success',
            content: text||'Success.',
            effect: 'fade',
            speedIn: 200,
            hold: 2000,
            speedOut: 500
        }, opts);
        var bnr = new Banner(opts);
        return bnr.reveal();
    };

    banner.headerBanner = function(hold, text, type){
        var opts = {
            type: type || 'message',
            content: text || 'Hello.',
            effect: 'fade',
            hold: hold || 0,
            container: '#page_wrapper',
            addClass: 'header-banner'
        };
        var bnr = new Banner(opts);
        return bnr.render(opts.container, 'prepend');
    };
    
    banner.saved = function(hold, text){
        return banner.headerBanner(hold||2000, text||'Saved.', 'success');
    };

    // 'preset' for save success banner
    banner.onSuccess = function(hold, text){
        if (!text) {
            text = hold;
            hold = 2000
        }
        return banner.headerBanner(hold||2000, text||'Saved.', 'success');
    };


    return XNAT.ui.banner = banner;

}));

