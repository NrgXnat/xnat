/*
 * web: activityTab.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Monitor async processing
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

    //TODO right now, this only monitors compressed uploader, but it should work for any async process, we just need
    // a unified way to poll progress

    let activityTab, cookieTag;
    const prearchiveUrl = '<a target="_blank" href="' +
        XNAT.url.fullUrl('/app/template/XDATScreen_prearchives.vm') +
        '">prearchive</a>';

    XNAT.app = getObject(XNAT.app || {});

    XNAT.app.activityTab = activityTab =
        getObject(XNAT.app.activityTab || {});

    XNAT.app.activityTab.cookieTag = cookieTag = 'activities';

    activityTab.pollers = [];

    activityTab.init = function() {
        var activities = JSON.parse(XNAT.cookie.get(cookieTag) || "{}");
        $.each(activities, function(key, item) {
            activityTab.startPoll(item, key);
        });
    };

    activityTab.start = function(title, statusListenerId) {
        var activities = JSON.parse(XNAT.cookie.get(cookieTag) || "{}"),
            key = (new Date()).toISOString().replace(/[^\w]/gi, '');
        var item = {
            title: title,
            statusListenerId: statusListenerId,
            divId: 'key' + key,
            detailsTag: 'div#activity-details-' + key
        };
        activities[key] = item;
        XNAT.cookie.set(cookieTag, activities, {});
        activityTab.startPoll(item, key);
    };

    activityTab.startPoll = function(item, key) {
        var $tab = $('#activity-tab');
        createEntry(item, key, $tab);
        var $info = $tab.find(item.divId);
        activityTab.pollers[key] = true;
        checkImageArchivalProgress(item.statusListenerId, $info, key, item.detailsTag, 0);
        $tab.css('visibility', 'visible');
    };

    activityTab.stopPoll = function(key, succeeded) {
        delete activityTab.pollers[key];

        var $info = $('#activity-tab #key' + key);
        if (succeeded) {
            $info.addClass('text-success').prepend('<i class="fa fa-check" style="margin-right:3px"></i>');
        } else {
            $info.addClass('text-error').prepend('<i class="fa fa-minus-circle" style="margin-right:3px"></i>');
        }
        $info.find('#close' + key).show();
        $info.find('#details' + key).html('<i class="fa fa-expand"></i>');
    };

    activityTab.cancel = function(key, $row) {
        var activities = {};
        if (key) {
            activities = JSON.parse(XNAT.cookie.get(cookieTag) || "{}");
            delete activities[key];
            if ($row) {
                $row.parents('div.item').remove();
            }
        }
        if ($.isEmptyObject(activities)) {
            XNAT.cookie.remove(cookieTag);
            const tab = $('#activity-tab');
            tab.css('visibility', 'hidden');
            tab.find('div.item').remove();
        } else {
            XNAT.cookie.set(cookieTag, activities, {});
        }
    };

    function createEntry(item, key, parent) {
        parent.find('.panel-body').append('<div id="' + item.divId + '" class="item">' + item.title +
            '<div class="actions">' +
            '<a id="close' + key + '" class="icn close"><i class="fa fa-close"></i></a>' +
            '<a id="details' + key + '" class="icn details"><i class="fa fa-cog fa-spin"></i></a>' +
            '</div></div>');

        var details = XNAT.ui.dialog.init({
            title: item.title,
            content: spawn(item.detailsTag),
            destroyOnClose: false,
            protected: true,
            buttons: [
                {
                    label: 'Close',
                    isDefault: true,
                    close: true
                }
            ]
        });

        $(document).on('click', 'a#details' + key, function() {
            details.show();
        });
        $(document).on('click', 'a#close' + key, function() {
            activityTab.cancel(key, $(this));
        });
    }

    function checkImageArchivalProgress(statusListenerId, div, key, detailsTag, errCnt, lastProgressIdx = -1) {
        if (! (activityTab.pollers.hasOwnProperty(key) && activityTab.pollers[key])) {
            return;
        }
        XNAT.xhr.getJSON({
            url: XNAT.url.restUrl('/xapi/event_tracking/' + statusListenerId),
            success: function(respDat) {
                var succeeded = null;
                try {
                    [succeeded, lastProgressIdx] = populateDetails(div, detailsTag, respDat, lastProgressIdx);
                } catch (e) {
                    console.log(e);
                    processError(statusListenerId, div, key, detailsTag, errCnt,
                        e.name + ' (js): ' + e.message, lastProgressIdx);
                    return;
                }

                if (succeeded !== null) {
                    activityTab.stopPoll(key, succeeded);
                } else {
                    checkImageArchivalProgress(statusListenerId, div, key, detailsTag, 0, lastProgressIdx);
                }
            },
            error: function(xhr) {
                processError(statusListenerId, div, key, detailsTag, errCnt,
                    xhr.responseText ? ': ' + xhr.responseText : '', lastProgressIdx);
            }
        });
    }

    function processError(statusListenerId, div, key, detailsTag, errCnt, errDetails, lastProgressIdx) {
        if (errCnt < 2) {
            setTimeout(function() {
                checkImageArchivalProgress(statusListenerId, div, key, detailsTag, ++errCnt, lastProgressIdx);
            }, 2000);
        } else {
            var msg = 'Issue polling archival progress' + errDetails + '. Refresh the page to try again or ' +
                'visit the prearchive to check for your session.';
            $(detailsTag).append('<div class="prog error">' + msg + '</div>');
            activityTab.stopPoll(key, false);
        }
    }

    function populateDetails(div, detailsTag, jsonobj, lastProgressIdx) {
        let messages = "";
        const succeeded = jsonobj['succeeded'];
        const payload = JSON.parse(jsonobj['payload']);
        let entryList = payload['entryList'] || [];
        if (entryList.length === 0 && succeeded == null) {
            return [null, lastProgressIdx];
        }
        entryList.forEach(function(e, i) {
            if (i <= lastProgressIdx) {
                return;
            }
            let level = e.status;
            let message = e.message.charAt(0).toUpperCase() + e.message.substr(1);
            let clazz;
            switch (level) {
                case 'Waiting':
                case 'InProgress':
                    clazz = 'info';
                    break;
                case 'Warning':
                    clazz = 'warning';
                    break;
                case 'Failed':
                    clazz = 'error';
                    break;
                case 'Completed':
                    clazz = 'success';
                    break;
            }
            messages += '<div class="prog ' + clazz + '">' + message + '</div>';
            lastProgressIdx = i;
        });
        if (succeeded != null) {
            messages += parseFinalMessage(jsonobj['finalMessage'], succeeded)
        }
        if (messages) {
            $(detailsTag).append(messages);
        }
        return [succeeded, lastProgressIdx];
    }

    function parseFinalMessage(message, succeeded) {
        if (succeeded) {
            const dest = message.replace(/:.*/, '');
            const urls = message.replace(/^.*:/, '').split(';');
            let urlsHtml;
            if (dest.toLowerCase().includes('prearchive')) {
                urlsHtml = 'Visit the ' + prearchiveUrl + ' to review.';
            } else {
                urlsHtml = $.map(urls, function (url) {
                    var id = url.replace(/.*\//, '');
                    return '<a target="_blank" href="/data' + url + '">' + id + '</a>'
                }).join(', ');
            }
            return '<div class="prog success">' + urls.length +
                ' session(s) successfully uploaded to ' + dest + ': ' + urlsHtml + '</div>';
        } else {
            return '<div class="prog error">Extraction/Review failed: ' + message + '</div>' +
                '<div class="warning">Check the ' + prearchiveUrl +
                ', your data may be available there for manual review.</div>';
        }
    }

    function errorHandler(e, base) {
        var info = e.responseText ? base + ': ' + e.responseText : base;
        var details = spawn('p',[info]);
        console.log(e);
        xmodal.alert({
            title: 'Error',
            content: '<p><strong>Error ' + e.status + ': '+ e.statusText+'</strong></p>' + details.html,
            okAction: function () {
                xmodal.closeAll();
            }
        });
    }

    // don't init until the page is finished loading
    $(window).on('load', function(){
        activityTab.init();
        $(document).on('click', '#activity-tab a.activity-min', function() {
            $('#activity-tab .panel-body').hide();
            $('#activity-tab .panel-header a.activity-min').hide();
            $('#activity-tab .panel-header a.activity-max').show();
        });
        $(document).on('click', '#activity-tab a.activity-max', function() {
            $('#activity-tab .panel-body').show();
            $('#activity-tab .panel-header a.activity-max').hide();
            $('#activity-tab .panel-header a.activity-min').show();
        });
        $(document).on('click', '#activity-tab a.activity-close', function() {
            activityTab.pollers = {};
            activityTab.cancel();
        });
    });

    return XNAT.app.activityTab = activityTab;
}));
