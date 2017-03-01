/*
 * web: timeout.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Session timeout warning dialog
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

    var timeout, undefined;
    var $timeLeftDisplay = $('#timeLeft');

    XNAT.app = getObject(XNAT.app || {});

    XNAT.app.timeout = timeout =
        getObject(XNAT.app.timeout || {});

    function dateString(ms, strip){
        var str = (new Date(ms)).toString();
        if (strip !== false) {
            str = str.replace(/\s+/g,'-');
        }
        return str;
    }

    // timeout polling interval
    timeout.interval = 1000;

    // when do we show the dialog?
    timeout.showTime = 60;

    timeout.cancelled = false;

    function timeoutCookie(name){

        var undefined;
        
        function CookieFn(){
            this.cookieExists = false;
            this.name = name !== undefined ? name : this.name;
            this.value = '';
            this.opts = { path: '/' };
        }

        var fn = CookieFn.prototype;

        fn.exists = function(name){
            this.name = name !== undefined ? name : this.name;
            this.cookieExists = Cookies.get()[this.name] !== undefined;
            return this.cookieExists;
        };
        // go ahead and check if this cookie exists
        fn.exists();

        // reset values after each call
        fn.reset = function(){
            //this.cookieExists = false;
            this.name = name !== undefined ? name : this.name;
            this.value = '';
            this.opts = { path: '/' };
            return this;
        };

        fn.set = function(name, value){
            if (value === undefined) {
                value = name;
                name = undefined;
            }
            this.name = name !== undefined ? name : this.name;
            this.value = value !== undefined ? value : this.value;
            Cookies.set(this.name, this.value, this.opts);
            //this.reset(); // reset to prevent passing of values to chained methods
            return this;
        };

        // 'value' is a default value to set if the cookie doesn't exist
        fn.get = function(value){
            value = value !== undefined ? value : '';
            this.value = this.exists() ? Cookies.get()[this.name] : value;
            return this;
        };

        fn.is = function(value) {
            return (this.get().value||'').toString() === value.toString();
        };

        fn.cookie = function(name) {
            this.name = name;
            this.exists();
            return this;
        };

        return new CookieFn();

    }

    var cookie = {};

    // store cookie value from server as a js var
    cookie.SESSION_EXPIRATION_TIME = timeoutCookie('SESSION_EXPIRATION_TIME').get();

    // is the dialog displayed?
    cookie.SESSION_DIALOG_OPEN = timeoutCookie('SESSION_DIALOG_OPEN').set('false');

    // has it been cancelled?
    cookie.SESSION_DIALOG_CANCELLED = timeoutCookie('SESSION_DIALOG_CANCELLED').set('false');

    // is the session still active? (could have been logged out in another window)
    cookie.SESSION_ACTIVE = timeoutCookie('SESSION_ACTIVE').set(window.loggedIn);

    // has the session timed out?
    cookie.SESSION_TIMED_OUT = timeoutCookie('SESSION_TIMED_OUT').get();

    // the time, in ms, that the session will end or has ended
    cookie.SESSION_TIMEOUT_TIME = timeoutCookie('SESSION_TIMEOUT_TIME');

    // the date and time, as a string, that the session will end or has ended
    // cookie.SESSION_TIMEOUT_STRING = timeoutCookie('SESSION_TIMEOUT_STRING');

    // has the user been redirected after timout?
    cookie.SESSION_LOGOUT_REDIRECT = timeoutCookie('SESSION_LOGOUT_REDIRECT').get();

    // the time, in ms, that the session started
    cookie.SESSION_LAST_LOGIN = timeoutCookie('SESSION_LAST_LOGIN');

    // what was the last page visited?
    cookie.SESSION_LAST_PAGE = timeoutCookie('SESSION_LAST_PAGE').get();
    
    timeout.expCookie = '';

    timeout.startTime = Date.now();

    // parse the timeout values
    timeout.getValues = function(){
        var expCookie = cookie.SESSION_EXPIRATION_TIME.get().value;
        if (timeout.expCookie && timeout.expCookie === expCookie) return;
        timeout.expCookie = expCookie; // save it for next time
        expCookie = (expCookie||'').replace(/"/g, '').split(',');
        //timeout.startTime = +expCookie[0].trim() + 100;
        timeout.startTime = Date.now();
        timeout.duration = +(expCookie[1]||'').trim();
        timeout.endTime = timeout.startTime + timeout.duration;
        return {
            startTime: timeout.startTime,
            duration: timeout.duration,
            endTime: timeout.endTime
        }
    };


    // set SESSION_TIMEOUT_* cookies on load
    cookie.SESSION_TIMEOUT_TIME.set(timeout.endTime);
    // cookie.SESSION_TIMEOUT_STRING.set(dateString(timeout.endTime));


    function parseTimestamp(time) {
        time = time || timeout.endTime;
        var timeLeft = time - Date.now();
        var secondsLeft = Math.floor(timeLeft / 1000);
        var minutesLeft = Math.floor(secondsLeft / 60);
        var secondsPart = secondsLeft % 60;
        var hoursPart = Math.floor(minutesLeft / 60);
        var minutesPart = minutesLeft % 60;
        return {
            time: time,
            timeLeft: timeLeft,
            secondsLeft: secondsLeft,
            seconds: secondsPart,
            minutes: minutesPart,
            hours: hoursPart
        };
    }


    // these things need to wait for the DOM to load
    $(function(){

        // create the dialog but don't render until DOM load
        // and don't show it until needed
        function timeoutDialog(){

            var z = 99999;

            var dialog = xmodal.open({
                id: 'session-timeout-warning',
                classes: 'keep static',
                width: 300,
                height: 200,
                title: false,
                content: 'Your ' + XNAT.app.siteId + ' session will expire in: <br><br>' +
                '<b class="mono timeout-hours"></b> hours ' +
                '<b class="mono timeout-minutes"></b> minutes ' +
                '<b class="mono timeout-seconds"></b> seconds.' +
                '</br></br>Click "Renew" to reset session timer.',
                okLabel: 'Renew',
                okClose: false,
                okAction: function(){
                    timeout.handleOk();
                    dialog.hide();
                },
                cancelClose: false, // don't destroy the dialog
                cancelAction: function(){
                    timeout.handleCancel();
                    dialog.hide();
                }
            });

            dialog.$mask.hide().css('z-index', z-1);
            dialog.$modal.hide().css('z-index', z);

            dialog.hours   = dialog.$modal.find('b.timeout-hours');
            dialog.minutes = dialog.$modal.find('b.timeout-minutes');
            dialog.seconds = dialog.$modal.find('b.timeout-seconds');

            dialog.show = function(){
                // DON'T SHOW IF ALREADY SHOWING
                // set this as a js var so it's window-independend
                if (timeout.dialogIsOpen) return;
                // ONLY SHOW THE DIALOG IF NOT CANCELLED
                if (cookie.SESSION_DIALOG_CANCELLED.is('false')) {
                    dialog.$mask.show();
                    dialog.$modal.show();
                    cookie.SESSION_DIALOG_OPEN.set('true');
                    timeout.dialogIsOpen = true;
                }
            };

            dialog.hide = function(){
                if (timeout.dialogIsOpen) {
                    dialog.$modal.hide();
                    dialog.$mask.hide();
                    cookie.SESSION_DIALOG_OPEN.set('false');
                }
                timeout.dialogIsOpen = false;
            };

            return dialog;

        }


        timeout.dialog = timeoutDialog();


        function redirectToLogin() {
            var NOW = Date.now();
            timeout.redirecting = true;
            if (!window.top.debug) {
                xmodal.loading.open('#redirecting');
            }
            timeout.dialog.hide();
            cookie.SESSION_DIALOG_OPEN.set('false');
            cookie.SESSION_DIALOG_CANCELLED.set('false');
            cookie.SESSION_TIMED_OUT.set('true');
            cookie.SESSION_TIMEOUT_TIME.set(NOW);
            // cookie.SESSION_TIMEOUT_STRING.set(dateString(NOW));
            cookie.SESSION_LOGOUT_REDIRECT.set('true');
            cookie.SESSION_LAST_PAGE.set(window.location.href);
            timeoutCookie('WARNING_BAR').set('OPEN');
            timeoutCookie('guest').set('true');
            // need to wait a little longer before reloading
            setTimeout(function(){
                window.location.reload();
            }, 2000);
        }


        function renewSession(){
            // redirect if trying to renew an inactive session
            if (cookie.SESSION_ACTIVE.is('false')) {
                redirectToLogin();
            }
            timeout.dialog.hide();
            cookie.SESSION_EXPIRATION_TIME.get();
            timeout.getValues();
            cookie.SESSION_DIALOG_OPEN.set('false');
            cookie.SESSION_DIALOG_CANCELLED.set('false');
            cookie.SESSION_TIMED_OUT.set('false');
            cookie.SESSION_TIMEOUT_TIME.set(timeout.endTime);
            // cookie.SESSION_TIMEOUT_STRING.set(dateString(timeout.endTime));
            if (console && console.log) {
                console.log('Session ends: ' + dateString(timeout.endTime, false));
            }
            cookie.SESSION_LOGOUT_REDIRECT.set('false');
            timeout.cancelled = false;
        }


        timeout.touch = function(opts){
            return XNAT.xhr.get(extend(true, {
                url: XNAT.url.restUrl('/xapi/siteConfig/buildInfo')
            }, opts || {} ));
        };


        timeout.handleOk = function(){

            var touch = timeout.touch();

            touch.done(function(data){
                cookie.SESSION_DIALOG_CANCELLED.set('true');
                // an object is returned if session is still valid
                timeout.sessionExpired = !isPlainObject(data);
                if (timeout.sessionExpired) {
                    redirectToLogin();
                }
                else {
                    renewSession();
                }
            });

        };


        // fire this once when the script loads
        timeout.handleOk();


        timeout.handleCancel = function(){
            timeout.dialog.hide();
            timeout.cancelled = true;
            cookie.SESSION_DIALOG_CANCELLED.set('true');
            cookie.SESSION_DIALOG_OPEN.set('false');
        };


        // check every second to see if our timeout time has been reached
        timeout.check = function(){

            timeout.getValues();

            var NOW = Date.now();

            // redirect if TIMED_OUT cookie is true
            if (!timeout.redirecting && cookie.SESSION_TIMED_OUT.is('true')) {
                redirectToLogin();
                return false;
            }

            // redirect if time has run out
            if (timeout.endTime <= NOW) {
                cookie.SESSION_TIMED_OUT.set('true');
                //redirectToLogin();
                return false;
            }

            // redirect if logged out from another window
            if (cookie.SESSION_ACTIVE.is('false')) {
                redirectToLogin();
                return false;
            }

            // close dialog if closed from another window
            if (cookie.SESSION_DIALOG_OPEN.is('false')) {
                timeout.dialog.hide();
            }

            // if endTime minus showTime is less than now
            if (timeout.endTime - (timeout.showTime*1000) <= NOW) {
                //don't do anything if the dialog has already been cancelled
                if (cookie.SESSION_DIALOG_CANCELLED.is('true')) {
                    //timeout.handleCancel();
                    return false;
                }
                cookie.SESSION_DIALOG_CANCELLED.set('false');
                timeout.dialog.show();
                return false;
            }

            return true;

        };


        timeout.sessionCountdown = function() {

            var timeLeft = parseTimestamp();

            var hours = timeLeft.hours;
            var mins  = zeroPad(timeLeft.minutes);
            var secs  = zeroPad(timeLeft.seconds);

            $timeLeftDisplay.text(hours + ":" + mins + ":" + secs);

            if (cookie.SESSION_TIMED_OUT.is('true')) {
                $timeLeftDisplay.text("Session Expired");
                hours = mins = secs = '--';
            }

            // Update the text in the dialog too so it's always in synch
            timeout.dialog.hours.text(hours);
            timeout.dialog.minutes.text(mins);
            timeout.dialog.seconds.text(secs);

        };


        timeout.running = false;


        timeout.init = function(){
            if (!timeout.running) {
                timeout.running = true;
                setInterval(
                    function(){
                        timeout.check();
                        timeout.sessionCountdown();
                    },
                    timeout.interval
                );
            }
        };


        // only run the timer if *not* a guest user (if an authenticated user)
        if ((!!Cookies.get('guest')) && (Cookies.get('guest') === 'false')) {
            timeout.init();
        }


        // attach event handler to elements with 'renew-session' class
        $('body').on('click', '#timeLeftRenew, .renew-session', function(){
            timeout.handleOk();
        });


    });


    // this script has loaded
    timeout.loaded = true;


    return XNAT.app.timeout = timeout;


}));
