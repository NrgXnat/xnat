/*
 * web: timeLeft.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */

// set up the XNAT.app.timeout object
if (typeof XNAT == 'undefined'){ XNAT={} }
if (typeof XNAT.app == 'undefined'){ XNAT.app={} }
if (typeof XNAT.app.timeout == 'undefined'){ XNAT.app.timeout={} }

// wrap it in an IIFE and pass the 'XNAT.app.timeout' object as the argument.
// Is this the best way to do this? I don't know.
$(function(){

    var timeout = XNAT.app.timeout;

    /*
     * The SESSION_EXPIRATION_TIME cookie returned from the server is double quoted for some reason
     * so unquote it before parsing it out.
     */
    timeout.pattern = new RegExp("\"", "g");

    timeout.parseExpirationTimeTuple = function(tuple) {
        var values = tuple.replace(timeout.pattern, "").split(",");
        var ret = {};
        if (values.length == 2) {
            ret.flag = values[0];
            ret.maxIdleTime = parseInt(values[1]);
        }
        return ret;
    };



    timeout.warningDialog = (function() {

        // var timeLeft = timeout.settings.expirationTime.timeLeft;

        // dialog.find('.body.content > .inner').html("Your "+XNAT.app.siteId+" session will expire in:</br></br>&nbsp;&nbsp;&nbsp; " + timeLeft.hoursPart + " hours "
        //     + timeout.zeroPad(timeLeft.minutesPart) + " minutes " + +timeout.zeroPad(timeLeft.secondsPart) + ' seconds.' +
        //     '</br></br>Click "Renew" to reset session timer.');

        var z = 99999;

        var dialog = xmodal.open({
            id: 'session-timeout-warning',
            classes: 'keep static',
            width: 320,
            height: 200,
            title: false,
            content: 'Your ' + XNAT.app.siteId + ' session will expire in: <br><br>' +
            '<span class="mono timeout-hours"></span> hours ' +
            '<span class="mono timeout-minutes"></span> minutes ' +
            '<span class="mono timeout-seconds"></span> seconds.' +
            '</br></br>Click "Renew" to reset session timer.',
            okLabel: 'Renew',
            okAction: function(obj){
                timeout.handleOk();
                obj.$modal.hide();
            },
            okClose: false
        });

        dialog.$mask.hide().css('z-index', z-1);
        dialog.$modal.hide().css('z-index', z);

        dialog.hours   = dialog.$modal.find('span.timeout-hours');
        dialog.minutes = dialog.$modal.find('span.timeout-minutes');
        dialog.seconds = dialog.$modal.find('span.timeout-seconds');

        return dialog;

    })();

    timeout.warningDialog.show = function(){
        timeout.warningDialog.$mask.show();
        timeout.warningDialog.$modal.show();
    };

    timeout.warningDialog.hide = function(){
        timeout.warningDialog.$mask.hide();
        timeout.warningDialog.$modal.hide();
    };

    /**
     * These cookies are available across tabs and windows:
     * - expirationTime : Stores a tuple that is (0|1, maximum idle time)
     *                    Each time the server responds the flag is toggled from 0 to 1 or vice versa.
     *                    This way we know that the cookie has changed.
     *                    The server cannot just send the new session expiration time because the system clock
     *                    on the client and the server may be out of sync. The client must keep track of the
     *                    amount of time left in the session solely using its own clock.
     * - dialogDisplay : Boolean flag that indicates if the warning dialog is currently being displayed
     *                   Used to synchronize the hiding of the warning dialog.
     * - sessionTimeout : A boolean flag that indicates whether this session has timed out. Used to synchronize
     *                    redirecting to the login page.
     *
     * The dialogDisplay and sessionTimeout need some explanation. Given the scenario where there are two session tabs A and B
     * (1) Currently a warning dialog pops up only once per session per tab. If a user extends a session in tab A
     * , tab B has no way of knowing this without the dialogDisplay cookie.
     * (2) If A times out the page which then redirects to the login page, when that login page is loaded the server
     * updates the SESSION_EXPIRATION_TIME cookie to a new value even though the session has expired. This happens
     * because the redirect to the login page counts as a request. At this point the SESSION_EXPIRATION_TIME is wrong
     * and if a user were to refresh tab B they would be redirected to the login page. 'sessionTimeout' is used to
     * broadcast to all tabs and windows that the session has indeed expired and they should take some action.
     */
    timeout.cookieOptions = {path: '/'};
    timeout.synchronizingCookies = {
        get: function (cookieName) {
            if (!!Cookies.get(cookieName)) {
                return Cookies.get(cookieName);
            }
            else {
                return null;
            }
        },
        expirationTime: {
            name: "SESSION_EXPIRATION_TIME",
            // no set function, there is no reason to ever change its value.
            get: function () {
                var name = timeout.synchronizingCookies.expirationTime.name;
                if (!!Cookies.get(name)) {
                    return timeout.parseExpirationTimeTuple(Cookies.get(name));
                }
                else {
                    return null;
                }
            }
        },
        dialogDisplay: {
            name: "SESSION_EXPIRATION_TIME_DIALOG_DISPLAYING",
            set: function (status) {
                Cookies.set(timeout.synchronizingCookies.dialogDisplay.name, status, timeout.cookieOptions);
            },
            get: function () {
                return timeout.synchronizingCookies.get(timeout.synchronizingCookies.dialogDisplay.name);
            }
        },
        sessionTimeout: {
            name: "SESSION_EXPIRATION_TIMEOUT",
            set: function (status) {
                Cookies.set(timeout.synchronizingCookies.sessionTimeout.name, status, timeout.cookieOptions);
            },
            get: function () {
                return timeout.synchronizingCookies.get(timeout.synchronizingCookies.sessionTimeout.name);
            }
        },
        hasRedirected: {
            name: "SESSION_LOGOUT_HAS_REDIRECTED",
            set: function (context, status) {
                Cookies.set(timeout.synchronizingCookies.hasRedirected.name, ('{' + context + ':' + status + '}'), timeout.cookieOptions);
            },
            get: function (context) {
                if (!!Cookies.get(timeout.synchronizingCookies.hasRedirected.name)) {
                    return Cookies.getJSON(timeout.synchronizingCookies.hasRedirected.name, timeout.cookieOptions)[context];
                }
                return null;
            },
            clear: function () {
                Cookies.set(timeout.synchronizingCookies.hasRedirected.name, '{cleared:true}', timeout.cookieOptions);
            }
        }
    };

    /**
     * Local variables, think of them as thread-local variables.
     * warningDisplayedOnce - indicates if the warning dialog has been displayed already for this
     *                        session
     * timerInterval - the interval in milliseconds at which to update the state of the session counter,
     *                 the warning dialog and the various synchronizing cookies. This value is immutable.
     * popupTime - The time in seconds in before the end of the session at which to popup a warning dialog.
     *             Should be less than that length of the session.
     * expirationTime - A local copy of the time this session will expire. It is periodically synchronized with
     *                  timeout.synchronizingCookies.expirationTime if it has changed. If it has the session has been extended
     * waitOneMoreCycle - This is an ugly, ugly hack.
     *               IE 8 and under seem to try and refresh the page before the session runs out. So when we get to the
     *               point where we think we are about to redirect to the login page and the user is running IE we wait
     *               one extra time interval just to make sure.
     */
    timeout.settings = {
        warningDisplayedOnce: false,
        timerInterval: 1000, // milliseconds
        popupTime: 59, // seconds (default is 59)
        expirationTime: { // see the comments on timeout.synchronizingCookies.expirationTime for an explanation on what this object represents
            flag: "-1",
            timeLeft: -1 // milliseconds
        },
        waitOneMoreCycle: false
    };

    /**
     * Set the synchronizing cookies to their base state.
     * The starting state is:
     * 1. A fresh expiration time is set
     * 2. No pop-ups are being displayed on any tabs or windows
     * 3. A fresh session is started on all tabs and windows
     * 4. No tabs or windows have timed out
     */
    timeout.refreshSynchronizingCookies = function() {
        timeout.synchronizingCookies.dialogDisplay.set("false");
        timeout.synchronizingCookies.sessionTimeout.set("false");
        timeout.synchronizingCookies.hasRedirected.clear();
    };

    // timeout.disableButtons = function(dialog) {
    //     var buttons = dialog.getButtons();
    //     for (var i = 0; i < buttons.length; i++) {
    //         buttons[i].set('disabled', true);
    //     }
    // };
    //
    // timeout.enableButtons = function(dialog) {
    //     var buttons = dialog.getButtons();
    //     for (var i = 0; i < buttons.length; i++) {
    //         buttons[i].set('disabled', false);
    //     }
    // };

    /**
     * If a user double-clicks a button in YUI's SimpleDialog
     * the callback associated with that button is run twice.
     * So we have to disable the buttons after each click and
     * enable them again when the dialog is shown.
     */
    timeout.hideWarningDialog = function() {
        // timeout.disableButtons(dialog);
        timeout.synchronizingCookies.dialogDisplay.set("false");
        timeout.warningDialog.hide();
    };

    timeout.showWarningDialog = function() {
        // timeout.enableButtons();
        timeout.synchronizingCookies.dialogDisplay.set("true");
        timeout.warningDialog.show();
    };

    /**
     * If the user wants to extend the session, hide the dialog and "touch" the server
     */
    timeout.handleOk = function () {
        // timeout.hideWarningDialog();
        timeout.touchCallback.startTime = new Date().getTime();
        XNAT.xhr.get(XNAT.url.restUrl('/xapi/siteConfig/buildInfo'), timeout.touchCallback);
    };

    /**
     * If the user does not want to extend the session broadcast their choice
     * to all tabs and ensure that they all close their dialogs.
     */
    timeout.handleCancel = function () {
        timeout.hideWarningDialog();
        // timeout.settings.warningDisplayedOnce = true;
        // don't make it any more complicated than necessary - just show the thing
    };

    /**
     * After touching the server, reset the synchronizing cookies and local variables
     */
    timeout.touchCallback = {
        success: function (data, status, res) {
            var sessionExpired = res.responseText.indexOf("<HTML>") != -1;
            if (sessionExpired) {
                timeout.redirectToLogin();
            }
            else {
                timeout.refreshSynchronizingCookies();
                // timeout.settings.warningDisplayedOnce = false;
            }
        },
        failure: function () {
        }
    };

    /**
     * Used to zero pad the time displays
     */
    timeout.zeroPad = function (x) {
        var y = parseInt(x,10) ; // make sure it's a number
        return (y < 10) ? '0'+y : ''+y ;
    };

    // /**
    //  * The warning dialog
    //  */
    // timeout.warningDialog = new YAHOO.widget.SimpleDialog("session_timeout_dialog", {
    //     width: "300px",
    //     close: true,
    //     fixedcenter: true,
    //     // z-index is manhandled in xnat.css
    //     // but we need to set it here as a base z-index for the other YUI dialogs
    //     zIndex: 5001,
    //     constraintoviewport: true,
    //     modal: true,
    //     icon: YAHOO.widget.SimpleDialog.ICON_WARN,
    //     visible: true,
    //     draggable: true,
    //     hideAfterSubmit: true,
    //     buttons: [
    //         { text: 'Renew', handler: timeout.handleOk, isDefault: true },
    //         { text: 'Close', handler: timeout.handleCancel }
    //     ]
    // });

    timeout.initWarningDialog = function(dialog) {
        // dialog.manager = this;
        // dialog.render(document.body);
        // dialog.setHeader("Session Timeout Warning");
        // dialog.setBody("");
        // dialog.bringToTop();
        // dialog.hide();
    };

    /**
     * Return the timestamp as hours, minutes and seconds. Used to update the session counter
     * and the warning dialog.
     *
     * Also hold onto the number of milliseconds left and the expiration time.
     * This value is used to determine when to redirect the page to the login page.
     */
    timeout.parseTimestamp = function(time) {
        var millisecondsLeft = time - (new Date().getTime());
        var secondsLeft = Math.floor(millisecondsLeft / 1000);
        var secondsPart = secondsLeft;
        var minutesPart = Math.floor(secondsPart / 60);
        secondsPart = secondsPart % 60;
        var hoursPart = Math.floor(minutesPart / 60);
        minutesPart = minutesPart % 60;
        return {
            time: time,
            millisecondsLeft: millisecondsLeft,
            secondsLeft: secondsLeft,
            secondsPart: secondsPart,
            minutesPart: minutesPart,
            hoursPart: hoursPart
        };
    };

    /**
     * See the comments for "timeout.settings" to see why this function necessary
     */
    timeout.checkIfFinalCycle = function() {
        if (timeout.settings.waitOneMoreCycle) {
            timeout.redirectToLogin();
        }
    };

    /**
     * Check if the global cookie's flag is different from what is stored locally.
     * If it is the user has extended the session from this or some other tab and
     * we reset the expiration time of the current tab.
     *
     * If it hasn't we recalculate the amount of time left based on the old time.
     *
     */
    timeout.syncSessionExpirationCookieWithLocal = function() {
        var cookieExpirationTime = timeout.synchronizingCookies.expirationTime.get();
        if (timeout.settings.expirationTime.flag !== cookieExpirationTime.flag) {
            timeout.settings.warningDisplayedOnce = false;
            timeout.settings.expirationTime.flag = cookieExpirationTime.flag;
            timeout.settings.expirationTime.timeLeft = timeout.parseTimestamp((new Date().getTime()) + cookieExpirationTime.maxIdleTime);
        }
        else {
            var oldTime = timeout.settings.expirationTime.timeLeft.time;
            timeout.settings.expirationTime.timeLeft = timeout.parseTimestamp(oldTime);
        }
    };

    /**
     * Determine whether to show the popup and update it or hide it.
     * There are 4 possibilities:
     * 1. The dialog needs to be displayed and this tab is currently displaying so
     * update the message with the session timer countdown.
     * 2. The dialog needs to be displayed and this tab is not currently displaying it so toggle
     * the local display variable, show the dialog and recurse so that case (1) is executed.
     * 3. The dialog should not be displayed and it is currently being shown so hide it.
     * 4. The dialog should not be displayed and is not displayed. Hide the dialog anyway
     * in case another tab as been opened while the popup was open in this one.
     */
    timeout.updateMessageOrHide = function() {
        if (timeout.synchronizingCookies.dialogDisplay.get() === "true") {
            // var timeLeft = timeout.settings.expirationTime.timeLeft;
            // dialog.hours = timeLeft.hours
            timeout.showWarningDialog();
            // timeout.updateMessageOrHide();
        // }
        // else if (timeout.synchronizingCookies.dialogDisplay.get() === "true" && !timeout.settings.warningDisplayedOnce) {
            // timeout.settings.warningDisplayedOnce = true;
            // timeout.showWarningDialog();
            // timeout.updateMessageOrHide();
        }
        // else if (timeout.synchronizingCookies.dialogDisplay.get() === "false" && timeout.settings.warningDisplayedOnce) {
        //     timeout.hideWarningDialog();
        // }
        else if (timeout.synchronizingCookies.dialogDisplay.get() === "false") {
            timeout.hideWarningDialog();
        }
    };

    /**
     * If the session has expired just refreshing the page should redirect to the login page.
     */
    timeout.redirectToLogin = function() {
        var windowName = window.name;
        if (!windowName) {
            windowName = "default";
        }
        var hasRedirected = timeout.synchronizingCookies.hasRedirected.get(windowName);
        if (!hasRedirected) {
            timeout.synchronizingCookies.hasRedirected.set(windowName, "true");
            Cookies.set('WARNING_BAR', 'OPEN', timeout.cookieOptions);
            Cookies.set('guest', 'true', timeout.cookieOptions);
            timeout.synchronizingCookies.sessionTimeout.set("true");
            var currTime = (new Date()).getTime();
            Cookies.set('SESSION_TIMEOUT_TIME', currTime, timeout.cookieOptions);
            window.location.reload();
        }
    };

    /**
     * Control the display of the amount of time left in the session and determine when
     * the session has timed out.
     *
     * If it is time to warn the user and we haven't already broadcast tell the
     * other tabs and windows that they need to open the popup dialog
     *
     * If this session will expire before we come back around to this function stop
     * displaying a timer.
     *
     * If this or some other tab has timed out first make sure to tell the other
     * tabs and then redirect to the login page
     *
     */
    timeout.sessionCountdown = function() {

        var dialog = timeout.warningDialog;
        var timeLeft = timeout.settings.expirationTime.timeLeft;
        var $timeLeft = $('#timeLeft');

        var hours = timeLeft.hoursPart;
        var mins  = timeout.zeroPad(timeLeft.minutesPart);
        var secs  = timeout.zeroPad(timeLeft.secondsPart);

        $timeLeft.text(hours + ":" + mins + ":" + secs);

        // Update the text in the dialog too so it's always in synch
        dialog.hours.text(hours);
        dialog.minutes.text(mins);
        dialog.seconds.text(secs);

        if ((timeLeft.secondsLeft < timeout.settings.popupTime)) {
            timeout.warningDialog.show();
            timeout.synchronizingCookies.dialogDisplay.set("true");
        }

        if (timeLeft.millisecondsLeft <= timeout.settings.timerInterval) {
            timeout.synchronizingCookies.dialogDisplay.set("false");
            $timeLeft.text("Time Left: Session Expired.");
        }

        if (timeout.synchronizingCookies.sessionTimeout.get() === "true" ||
            timeLeft.millisecondsLeft <= 0 ||
            timeLeft.millisecondsLeft == undefined) {
            timeout.redirectToLogin();
        }
    };


    /**
     * Initialize the synchronizing cookies and warning dialog and kick off the
     * counter.
     */
    XNAT.app.timeout.refreshSynchronizingCookies();
    // XNAT.app.timeout.initWarningDialog();
    // only run the timer if *not* a guest user (if an authenticated user)
    if ((!!Cookies.get('guest')) && (Cookies.get('guest') === 'false')) {
        setInterval(
            function(){
                XNAT.app.timeout.syncSessionExpirationCookieWithLocal();
                XNAT.app.timeout.updateMessageOrHide();
                XNAT.app.timeout.sessionCountdown();
            },
            XNAT.app.timeout.settings.timerInterval
        );
    }

    // attach event handler to elements with 'renew-session' class
    $('body').on('click', '#timeLeftRenew, .renew-session', function(){
        timeout.handleOk();
    });

    // (function(){
    //
    //     var hash = window.location.hash.toLowerCase();
    //
    //     // force debug mode to 'stick' if set explicitly 'on' or 'off'
    //     var debugOn = /(debug=on|debug=true)/.test(hash.toLowerCase());
    //     var debugOff = /(debug=off|debug=false)/.test(hash.toLowerCase());
    //
    //     if (debugOn) { Cookies.set('debug','on') }
    //     else if (debugOff) { Cookies.remove('debug') }
    //
    //     // if debugging, reset the timer every 2 minutes
    //     if (debugOn || window.debug || isFalse(getQueryStringValue('timeout')) || /(on|true)/.test(Cookies.get('debug'))) {
    //         setInterval(XNAT.app.timeout.handleOk, 120*1000);
    //     }
    //
    // })();

});
