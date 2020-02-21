/*
 * web: sessionTimer.js
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

    var timeout, timer, siteRoot, undef;
    var $timeLeftDisplay = $('#timeLeft');

    var resetCookies = getQueryStringValue('cookies') === 'reset';

    XNAT.app = getObject(XNAT.app || {});

    XNAT.app.timeout = timeout = timer =
        getObject(XNAT.app.timeout || {});

    XNAT.app.timeout.maintainLogin = false;

    XNAT.app.siteRoot = siteRoot =
        XNAT.app.siteRoot || XNAT.url.rootUrl();

    function dateString(ms, strip){
        var str = (new Date(ms)).toString();
        if (strip !== false) {
            str = str.replace(/\s+/g, '-');
        }
        return str;
    }

    // timeout polling interval
    timeout.interval = timeout.interval || 1000;

    // when do we show the dialog?
    // (how many seconds until the session ends)
    timeout.showTime = timeout.showTime || 60;

    // how long will the screen be grayed out
    // before redirecting?
    timeout.delayTime = timeout.delayTime || 20;

    timeout.cancelled = false;

    function timeoutCookie(name){

        var undefined;

        function CookieFn(){
            this.cookieExists = false;
            this.name = name !== undefined ? name : this.name;
            this.value = '';
            this.opts = {
                path: siteRoot,
                domain: XNAT.url.getDomain()
            };
        }

        var fn = CookieFn.prototype;

        fn.exists = function(name){
            this.name = name !== undefined ? name : this.name;
            this.cookieExists = XNAT.cookie.get(this.name) !== undefined;
            return this.cookieExists;
        };
        // go ahead and check if this cookie exists
        fn.exists();

        // reset values after each call
        fn.reset = function(){
            //this.cookieExists = false;
            this.name = name !== undefined ? name : this.name;
            this.value = '';
            this.opts = {
                path: siteRoot,
                domain: XNAT.url.getDomain()
            };
            return this;
        };

        fn.set = function(name, value, opts){
            if (value === undefined) {
                value = name;
                name = undefined;
            }
            this.name = name !== undefined ? name : this.name;
            this.value = value !== undefined ? value : this.value;
            this.opts = opts !== undefined ? opts : this.opts;
            XNAT.cookie.set(this.name, this.value, this.opts);
            //this.reset(); // reset to prevent passing of values to chained methods
            return this;
        };

        // 'value' is a default value to set if the cookie doesn't exist
        fn.get = function(value){
            value = value !== undefined ? value : '';
            this.value = this.exists() ? XNAT.cookie.get(this.name) : value;
            return this;
        };

        fn.getJSON = function(name){
            this.name = name !== undefined ? name : this.name;
            return getObject(Cookies.getJSON(this.name));
        };

        fn.getValue = function(prop){
            return XNAT.cookie.getValue(this.name, prop);
        };

        fn.is = function(value){
            return (this.get().value || '').toString() === value.toString();
        };

        fn.remove = function(){
            XNAT.cookie.remove(this.name);
            return !this.exists();
        };

        fn.cookie = function(name){
            this.name = name;
            this.exists();
            return this;
        };

        fn.update = function(prop, val){
            prop = prop || '*';
            val = firstDefined(val, '/');
            XNAT.cookie.setValue(this.name, prop, val, this.opts);
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
    // cookie.SESSION_LOGOUT_REDIRECT = timeoutCookie('SESSION_LOGOUT_REDIRECT').get();

    // the time, in ms, that the session started
    cookie.SESSION_LAST_LOGIN = timeoutCookie('SESSION_LAST_LOGIN');

    // the last logged-in user - used for redirect check
    // cookie.SESSION_LAST_USER = timeoutCookie('SESSION_LAST_USER').get();

    // what was the last page visited?
    // cookie.SESSION_LAST_PAGE = timeoutCookie('SESSION_LAST_PAGE').get();

    // remove potentially problematic cookies
    XNAT.cookie.remove('SESSION_LAST_USER', { path: '/' });
    XNAT.cookie.remove('SESSION_LAST_USER');
    XNAT.cookie.remove('SESSION_LAST_PAGE', { path: '/' });
    XNAT.cookie.remove('SESSION_LAST_PAGE');

    timeout.expCookie = '';

    timeout.startTime = Date.now();

    // parse the timeout values
    timeout.getValues = (function(){
        // define the function
        function getTimeoutValues(){
            var expCookie = cookie.SESSION_EXPIRATION_TIME.get().value;
            if (timeout.expCookie && timeout.expCookie === expCookie) return;
            timeout.expCookie = expCookie; // save it for next time
            expCookie = (expCookie || '').replace(/"/g, '').split(',');
            return {
                startTime: (timeout.startTime = +((expCookie[0] || '').trim())),
                duration: (timeout.duration = +((expCookie[1] || '').trim())),
                endTime: (timeout.endTime = timeout.startTime + timeout.duration)
            }
        }
        // call the function to initialize the needed values
        getTimeoutValues();
        // return for later use with timeout.getValues()
        return getTimeoutValues;
    })();

    // set SESSION_TIMEOUT_* cookies on load
    cookie.SESSION_TIMEOUT_TIME.set(timeout.endTime);
    // cookie.SESSION_TIMEOUT_STRING.set(dateString(timeout.endTime));


    function parseTimestamp(time){
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

    var shaded = false;

    var shade = XNAT.ui.dialog.shade({
        protected: true,
        id: 'page-obfuscator',
        beforeShow: function(){
            debugLog('shade.beforeShow');
            return shaded !== true;
        },
        afterShow: function(){
            debugLog('shade.afterShow');
            shaded = true;
        },
        afterHide: function(){
            debugLog('shade.afterHide');
            shaded = false;
        }
    }).hide();

    // custom shade
    shade.mask$.css({
        background: '#aaa',
        opacity: '0.95'
    });

    window.shade = shade;

    // these things need to wait for the DOM to load
    // ...or do they?
    (function(){

        var USERNAME = window.username;
        var LOC_HREF = window.location.href;

        function encodePageInfo(usr, pg){
            var _usr = XNAT.sub64.dlxEnc(usr).encoded;
            var _pg = XNAT.sub64.encode(pg).encoded;
            // var _parts = XNAT.url.splitUrl(_pg);
            return {
                user: _usr,
                page: _pg,
                encoded: _usr + '@' + _pg
            }
        }

        function decodePageInfo(str){
            var parts = str.split('@');
            var _usr = XNAT.sub64.dlxDec(parts[0]).decoded;
            var _pg = XNAT.sub64.decode(parts[1]).decoded;
            return {
                user: _usr,
                page: _pg,
                decoded: _usr + '@' + _pg
            }
        }


        var resumeCounter = 0;

        // if the last visited page is different than the current page,
        // try to want to go to there
        // function resumeSession(user, page){
        //
        //     // prevent infinite callback loop
        //     if ((resumeCounter += 1) > 10) return;
        //
        //     // this makes my brain hurt [*L*]
        //
        //     var pageEnc = encodePageInfo(user, page).encoded;
        //
        //     var logoutRedirected = cookie.SESSION_LOGOUT_REDIRECT.is('true');
        //     var lastPageCookie = cookie.SESSION_LAST_PAGE.get().value;
        //
        //     var lastSessionParts = decodePageInfo(lastPageCookie);
        //     var lastSessionUser = lastSessionParts.user;
        //     var lastSessionPage = lastSessionParts.page;
        //
        //     // only attempt a redirect if user@page is different
        //     if (logoutRedirected && lastPageCookie !== pageEnc) {
        //
        //         cookie.SESSION_LOGOUT_REDIRECT.set('');
        //
        //         if (lastSessionUser === USERNAME) {
        //
        //             cookie.SESSION_LAST_PAGE.set(pageEnc);
        //             // cookie.SESSION_LAST_USER.set(lastSessionUser);
        //
        //             // calling this again should let
        //             // the session resume properly
        //             resumeSession(lastSessionUser, lastSessionPage);
        //         }
        //         // else {
        //         //     cookie.SESSION_LAST_USER.set('');
        //         // }
        //     }
        //     else {
        //         if (LOC_HREF !== page) {
        //             window.location.href = page;
        //         }
        //         console.log('???');
        //         //window.location.href = XNAT.url.rootUrl('/');
        //     }
        // }
        //
        // resumeSession(USERNAME, LOC_HREF);

        var warned = false;

        // create the dialog but don't render until DOM load
        // and don't show it until needed
        function warningDialog(){

            var z = 99999;

            debugLog('warningDialog');

            var warning = XNAT.ui.dialog.init({
                id: 'session-timeout-warning',
                classes: 'keep static',
                width: 360,
                // height: 200,
                // speed: -1,   // do not open
                nuke: false, // do not destroy on close
                protected: true, // do not destroy EVER
                title: false,
                content: '' +
                '<div style="font-size:14px;">' +
                    'Your <b>' + XNAT.app.siteId + '</b> session will expire in: <br><br>' +
                    '<b class="mono timeout-hours"></b> hours ' +
                    '<b class="mono timeout-minutes"></b> minutes ' +
                    '<b class="mono timeout-seconds"></b> seconds.' +
                    '<br><br>Click <b>"Renew"</b> to reset session timer.' +
                '</div>',
                beforeShow: function(){
                    // console.log('beforeShow');
                    // ONLY SHOW THE DIALOG IF NOT OPEN OR CANCELLED
                    return !warned || cookie.SESSION_DIALOG_CANCELLED.is('false');
                    // return !timeout.dialogIsOpen && cookie.SESSION_DIALOG_CANCELLED.is('false');
                },
                afterShow: function(){
                    // console.log('afterShow');
                    // set this as a js var so it's window-independent
                    cookie.SESSION_DIALOG_OPEN.set('true');
                    warned = true;
                    timeout.dialogIsOpen = true;
                    // if (cookie.SESSION_DIALOG_CANCELLED.is('false')) {
                    //     cookie.SESSION_DIALOG_OPEN.set('true');
                    //     timeout.dialogIsOpen = true;
                    // }
                },
                // onHide: function(){
                //     // console.log('onHide');
                //     return timeout.dialogIsOpen;
                // },
                afterHide: function(){
                    // console.log('afterHide');
                    shade.hide();
                    cookie.SESSION_DIALOG_OPEN.set('false');
                    timeout.dialogIsOpen = false;
                },
                buttons: [
                    {
                        label: 'Renew',
                        isDefault: true,
                        close: false,
                        action: function(dialog){
                            timeout.renew();
                            dialog.hide(0);
                        }
                    },
                    {
                        label: 'Close',
                        close: false,
                        action: function(dialog){
                            timeout.handleCancel();
                            dialog.hide(0);
                        }
                    }
                ],
                // okLabel: 'Renew',
                // okClose: false,
                // okAction: function(){
                //     timeout.renew();
                //     warning.hide();
                // },
                // cancelClose: false, // don't destroy the dialog
                // cancelAction: function(){
                //     timeout.handleCancel();
                //     warning.hide();
                // },
                bogus: null

            });

            warning.hide();

            warning.hours = warning.$modal.find('b.timeout-hours');
            warning.minutes = warning.$modal.find('b.timeout-minutes');
            warning.seconds = warning.$modal.find('b.timeout-seconds');

            // warning.show = function(){
            //     // DON'T SHOW IF ALREADY SHOWING
            //     // set this as a js var so it's window-independend
            //     if (timeout.dialogIsOpen) return;
            //     // ONLY SHOW THE DIALOG IF NOT CANCELLED
            //     if (cookie.SESSION_DIALOG_CANCELLED.is('false')) {
            //         warning.$mask.show();
            //         warning.$modal.show();
            //         cookie.SESSION_DIALOG_OPEN.set('true');
            //         timeout.dialogIsOpen = true;
            //     }
            // };
            //
            // warning.hide = function(){
            //     if (timeout.dialogIsOpen) {
            //         warning.$modal.hide();
            //         warning.$mask.hide();
            //         cookie.SESSION_DIALOG_OPEN.set('false');
            //     }
            //     timeout.dialogIsOpen = false;
            // };

            return warning;

        }


        timeout.warningDialog = warningDialog();


        function redirectToLogin(){
            var END = timeout.endTime;
            var NOW = Date.now();
            // var USR = USERNAME;
            // var LOC = LOC_HREF;
            // var lastPageEnc = encodePageInfo(USR, LOC).encoded;
            window.clearInterval(timeout.intervalCheck);
            timeout.redirecting = true;
            timeout.warningDialog.hide();
            if (!shade.isOpen) {
                shade.open();
            }
            XNAT.ui.dialog.message({
                title: false,
                content: '' +
                '<div class="warning" style="font-size:14px;">' +
                'User session ended: ' + (new Date(NOW)).toLocaleString() +
                '</div>',
                // padding: 30,
                okAction: function(){
                    // window.location.href = XNAT.url.rootUrl('/');
                    window.location.reload();
                }
            });
            cookie.SESSION_DIALOG_OPEN.set('false');
            cookie.SESSION_DIALOG_CANCELLED.set('false');
            cookie.SESSION_TIMED_OUT.set('true');
            cookie.SESSION_TIMEOUT_TIME.set(NOW);
            // cookie.SESSION_TIMEOUT_STRING.set(dateString(NOW));
            // cookie.SESSION_LOGOUT_REDIRECT.set('true');
            // cookie.SESSION_LAST_PAGE.set(lastPageEnc);
            timeoutCookie('WARNING_BAR').set('OPEN');
            timeoutCookie('guest').set('true');
            // need to wait a little longer before reloading
            window.setTimeout(function(){
                if (!window.top.debug) {
                    xmodal.loading.open('#redirecting');
                }
                window.setTimeout(function(){
                    // by going to the site root after timing out,
                    // this script will handle redirection
                    // window.location.href = XNAT.url.rootUrl('/');
                    window.location.reload();
                }, 2000);
            }, 20000)
        }


        function renewSession(){
            // redirect if trying to renew an expired session
            if (cookie.SESSION_ACTIVE.is('false')) {
                // resumeSession();
                redirectToLogin();
            }
            timeout.warningDialog.hide();
            shade.hide();
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
            // cookie.SESSION_LOGOUT_REDIRECT.set('false');
            timeout.cancelled = false;
        }


        timeout.touch = function(opts){
            return XNAT.xhr.get(extend(true, {
                url: XNAT.url.restUrl('/xapi/siteConfig/buildInfo')
            }, opts || {}));
        };


        timeout.renew = function(){
            return timeout.touch().done(function(data){
                if (timeout.warningDialog.isOpen) {
                    timeout.warningDialog.hide();
                    cookie.SESSION_DIALOG_CANCELLED.set('true');
                }
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
        // alias for legacy compatibility
        timeout.handleOk = timeout.renew;


        // fire this once when the script loads
        timeout.renew();


        timeout.handleCancel = function(){
            timeout.warningDialog.hide();
            timeout.cancelled = true;
            cookie.SESSION_DIALOG_CANCELLED.set('true');
            cookie.SESSION_DIALOG_OPEN.set('false');
        };


        var renewalCounter = 1;
        // check every second to see if our timeout time has been reached
        timeout.check = function(){

            // debugLog('timeout.check()');

            if (XNAT.app.timeout.maintainLogin) {
                if (renewalCounter === 60) {
                    timeout.renew();
                    renewalCounter = 0;
                } else {
                    renewalCounter++;
                }
            }

            timeout.getValues();

            var NOW = Date.now();

            timeout.redirecting = timeout.redirecting || shade.isOpen;

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
                if (timeout.warningDialog.isOpen) {
                    timeout.warningDialog.hide();
                }
            }

            // if endTime minus showTime is less than now
            if (timeout.endTime - (timeout.showTime * 1000) <= NOW) {
                //don't do anything if the dialog has already been cancelled
                if (cookie.SESSION_DIALOG_CANCELLED.is('true')) {
                    //timeout.handleCancel();
                    return false;
                }
                cookie.SESSION_DIALOG_CANCELLED.set('false');
                timeout.warningDialog.show().toTop();
                return false;
            }

            return true;

        };


        timeout.sessionCountdown = function(){

            var timeLeft = parseTimestamp();

            var hours = timeLeft.hours;
            var mins = zeroPad(timeLeft.minutes);
            var secs = zeroPad(timeLeft.seconds);

            $timeLeftDisplay.text(hours + ":" + mins + ":" + secs);

            if (cookie.SESSION_TIMED_OUT.is('true')) {
                $timeLeftDisplay.text("Session Expired");
                hours = mins = secs = '--';
            }

            // Update the text in the dialog too so it's always in synch
            timeout.warningDialog.hours.text(hours);
            timeout.warningDialog.minutes.text(mins);
            timeout.warningDialog.seconds.text(secs);

        };


        timeout.running = false;


        timeout.init = function(){
            if (!timeout.running) {
                timeout.running = true;
                timeout.intervalCheck = window.setInterval(
                    function(){
                        timeout.check();
                        timeout.sessionCountdown();
                    },
                    timeout.interval
                );
            }
        };

    })();

    // don't start the timer until the page is finished loading
    $(window).on('load', function(){
        // only run the timer if *not* a guest user (if an authenticated user)
        if ((!!XNAT.cookie.get('guest')) && (XNAT.cookie.get('guest') === 'false')) {
            timeout.init();
        }
    });

    $(window).on('beforeunload', function(){
        // var lastPageEnc = encodePageInfo(USERNAME, LOC_HREF).encoded;
        // // cookie.SESSION_LAST_USER.set(lastUserEnc);
        // // check for 'resume=false' query string to prevent loading certain pages
        // if (getQueryStringValue('resume').toString() !== 'false') {
        //     cookie.SESSION_LAST_PAGE.set(lastPageEnc);
        // }
        // else {
        //     cookie.SESSION_LAST_PAGE.set('');
        // }
    });

    // attach event handler to elements with 'renew-session' class
    $(document).on('click', '.renew-session', function(e){
        e.preventDefault();
        timeout.renew();
    });


    // this script has loaded
    timeout.loaded = true;


    return XNAT.app.timeout = XNAT.app.timer = timer = timeout;


}));
