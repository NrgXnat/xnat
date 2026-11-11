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
    let activityTab, storageTag;
    const minimized = 'minimized', 
        processes = 'processes',
        queuedTimeout = 10 * 60 * 1000;

    XNAT.app = getObject(XNAT.app || {});

    XNAT.app.activityTab = activityTab =
        getObject(XNAT.app.activityTab || {});

    XNAT.app.activityTab.storageTag = storageTag = 'activities' + XNAT.sub64.dlxEnc(window.username).encoded;

    activityTab.pollers = [];

    function getActivities() {
        return JSON.parse(localStorage.getItem(storageTag) || '{"' + minimized + '": false, "' + processes + '": {}}');
    }

    function saveActivities(activities) {
        localStorage.setItem(storageTag, JSON.stringify(activities));
    }

    activityTab.init = function() {
        let activities = getActivities();
        $.each(activities[processes], function(key, item) {
            activityTab.startPoll(item, key);
        });
        setMinimized(activities[minimized]);
    };

    activityTab.start = function (title, statusListenerId, callbackPath, timeout) {
        if (!callbackPath) {
            callbackPath = 'XNAT.app.activityTab.populateArchivalDetails';
        }
        if (!timeout) {
            timeout = 500;
        }

        let activities = getActivities(),
            key = (new Date()).toISOString().replace(/[^\w]/gi, '');
        const item = {
            title: title,
            statusListenerId: statusListenerId,
            divId: 'key' + key,
            detailsTag: 'div#activity-details-' + key,
            callbackPath: callbackPath,
            timeout: timeout
        };
        activities[processes][key] = item;
        saveActivities(activities);
        activityTab.startPoll(item, key);
    };

    activityTab.startPoll = function(item, key) {
        let $tab = $('#activity-tab');
        createEntry(item, key, $tab);
        // details div is not in the DOM yet, see XNAT-6387
        waitForElement(200, item.detailsTag, function(){
            activityTab.pollers[key] = true;
            checkProgress(item, key, 0);
            $tab.css('visibility', 'visible');
        });
    };

    activityTab.stopPoll = function(key, succeeded) {
        delete activityTab.pollers[key];

        let $info = $('#activity-tab #key' + key);
        if (succeeded === null) {
            $info.addClass('text-warning').prepend('<i class="fa fa-exclamation-triangle" style="margin-right:3px"></i>');
        } else if (succeeded) {
            $info.addClass('text-success').prepend('<i class="fa fa-check" style="margin-right:3px"></i>');
        } else {
            $info.addClass('text-error').prepend('<i class="fa fa-minus-circle" style="margin-right:3px"></i>');
        }
        $info.find('#close' + key).show();
        $info.find('#details' + key).html('<i class="fa fa-expand"></i>');
    };

    activityTab.cancel = function(key, $row) {
        let activities = {};
        if (key) {
            activities = getActivities();
            delete activities[processes][key];
            if ($row) {
                $row.parents('div.item').remove();
            }
        }
        if ($.isEmptyObject(activities[processes])) {
            localStorage.removeItem(storageTag);
            const tab = $('#activity-tab');
            tab.css('visibility', 'hidden');
            tab.find('div.item').remove();
        } else {
            saveActivities(activities);
        }
    };

    function createEntry(item, key, parent) {
        parent.find('.panel-body').append('<div id="' + item.divId + '" class="item">' + item.title +
            '<div class="actions">' +
            '<a id="close' + key + '" class="icn close"><i class="fa fa-close"></i></a>' +
            '<a id="details' + key + '" class="icn details"><i class="fa fa-cog fa-spin"></i></a>' +
            '</div></div>');

        const details = XNAT.ui.dialog.init({
            title: item.title,
            content: spawn(item.detailsTag),
            destroyOnClose: false,
            protected: true,
            scroll: true,
            maxBtn: true,
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

    function checkProgress(item, key, errCnt, lastProgressIdx, queuedSince) {
        if (!lastProgressIdx) {
            lastProgressIdx = -1;
        }
        if (! (activityTab.pollers.hasOwnProperty(key) && activityTab.pollers[key])) {
            return;
        }
        const statusListenerId = item.statusListenerId,
            detailsTag = item.detailsTag,
            itemDivId = '#' + item.divId;
        XNAT.xhr.getJSON({
            url: XNAT.url.restUrl('/xapi/event_tracking/' + statusListenerId),
            success: function(respDat) {
                var succeeded = null;
                var queued = false;
                try {
                    const callback = getCallbackForItem(item);
                    const rtn = callback(itemDivId, detailsTag, respDat, lastProgressIdx);
                    succeeded = rtn.succeeded;
                    lastProgressIdx = rtn.lastProgressIdx;
                    queued = rtn.queued === true;
                } catch (e) {
                    console.log(e);
                    processError(item, key, errCnt, e.name + ' (js): ' + e.message, lastProgressIdx);
                    return;
                }

                if (succeeded !== null) {
                    activityTab.stopPoll(key, succeeded);
                } else if (queued) {
                    // The callback says nothing has reported yet: the import is still queued, or was lost, as in a
                    // restart. Check less often the longer it waits, and stop after a while with a note that
                    // refreshing the page resumes tracking.
                    queuedSince = queuedSince || Date.now();
                    const waited = Date.now() - queuedSince;
                    if (waited > queuedTimeout) {
                        stopWithQueuedWarning(item, key);
                    } else {
                        window.setTimeout(function() {
                            checkProgress(item, key, 0, lastProgressIdx, queuedSince);
                        }, Math.min(30000, Math.max(2000, waited / 2)));
                    }
                } else {
                    window.setTimeout(function() {
                        checkProgress(item, key, 0, lastProgressIdx);
                    }, item.timeout);
                }
            },
            error: function(xhr) {
                processError(item, key, errCnt,
                    xhr.responseText ? ': ' + xhr.responseText : '', lastProgressIdx, xhr.status, queuedSince);
            }
        });
    }

    function processError(item, key, errCnt, errDetails, lastProgressIdx, status = -1, queuedSince) {
        const maxErrCnt = status === 404 ? 15 : 3;
        if (errCnt < maxErrCnt) {
            setTimeout(function() {
                checkProgress(item, key, ++errCnt, lastProgressIdx, queuedSince);
            }, 2000);
        } else if (status === 404) {
            stopWithQueuedWarning(item, key);
        } else {
            $(item.detailsTag).append('<div class="prog error">Issue polling event progress: ' + errDetails +
                '. Refresh the page to try again.</div>');
            activityTab.stopPoll(key, false);
        }
    }

    function stopWithQueuedWarning(item, key) {
        // Event tracking can take some time to start if no consumers/threads are available to start the processing
        $(item.detailsTag).append('<div class="warning">No event tracking data yet. This likely means tasks are queued, ' +
            'but could be indicative of an issue. You will need to refresh the page to resume tracking this event. ' +
            'If there is no progress for quite some time, you may wish to contact your administrator.</div>');
        activityTab.stopPoll(key, null);
    }

    activityTab.populateArchivalDetails = function(itemDivId, detailsTag, jsonobj, lastProgressIdx) {
        const succeeded = jsonobj['succeeded'];
        const payload = JSON.parse(jsonobj['payload']);
        let messages = "";
        let entryList = payload ? (payload['entryList'] || []) : [];
        if (entryList.length === 0 && succeeded == null) {
            // The record was created when the import was queued, and nothing has reported yet
            return {succeeded: null, lastProgressIdx: lastProgressIdx, queued: true};
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
        return {succeeded: succeeded, lastProgressIdx: lastProgressIdx};
    };

    function parseFinalMessage(message, succeeded) {
       return parseSiMessage(message, succeeded);
    }

    function parseSiMessage(message, succeeded) {
        if (succeeded) {
            // The DICOM importers name the sessions they made as "Prearchive:" or "Archive:" and their URIs. Other
            // importers word it their own way, so their message is shown as it is.
            const named = /^(prearchive|archive):(.*)$/i.exec(message || '');
            if (!named) {
                return '<div class="prog success">Import complete' +
                    (message ? ': ' + linkArchived(escapeHtml(message)) : '') + '</div>';
            }
            const dest = named[1];
            const urls = named[2].split(';').filter(Boolean);
            let urlsHtml;
            if (dest.toLowerCase().includes('prearchive')) {
                urlsHtml = 'Visit the ' + getPrearcLink() + ' to review.';
            } else {
                urlsHtml = $.map(urls, function (url) {
                    var id = url.replace(/.*\//, '');
                    return '<a target="_blank" href="' + XNAT.url.rootUrl('/data' + url) + '">' + id + '</a>'
                }).join(', ');
            }
            return '<div class="prog success">' + urls.length +
                ' session(s) successfully uploaded to ' + dest + ': ' + urlsHtml + '</div>';
        } else {
            // A failed upload names any sessions it archived before the failure: link them as a success does
            const details = linkArchived(escapeHtml(message));
            return '<div class="prog error">Extraction/Review failed: ' + details + '</div>' +
                '<div class="warning">Check the ' + getPrearcLink() +
                ', DICOM/ECAT data may be available there for manual review.</div>';
        }
    }

    // Links each archived session a message names
    function linkArchived(html) {
        return html.replace(/\/archive\/projects\/[^\s;<]+/g, function (url) {
            const id = url.replace(/.*\//, '');
            return '<a target="_blank" href="' + XNAT.url.rootUrl('/data' + url) + '">' + id + '</a>';
        });
    }

    function getPrearcLink() {
        return '<a target="_blank" href="' +
        XNAT.url.rootUrl('/app/template/XDATScreen_prearchives.vm') +
        '">prearchive</a> ';
    }

    function getCallbackForItem(item) {
        const parts = item.callbackPath.split('.');
        let callback = parts.shift() === 'XNAT' ? XNAT : window;
        parts.forEach(function(p) {
           callback = callback[p];
        });
        return callback;
    }

    function setMinimized(isMinimized, updateStorage) {
        let activities = getActivities();
        if (updateStorage) {
            activities[minimized] = isMinimized;
            saveActivities(activities);
        }
        if (isMinimized) {
            $('#activity-tab .panel-header span.count').text('(' + Object.keys(activities[processes]).length + ')');
            $('#activity-tab .panel-body').hide();
            $('#activity-tab .panel-header a.activity-min').hide();
            $('#activity-tab .panel-header a.activity-max').show();
        } else {
            $('#activity-tab .panel-header span.count').text('');
            $('#activity-tab .panel-body').show();
            $('#activity-tab .panel-header a.activity-max').hide();
            $('#activity-tab .panel-header a.activity-min').show();
        }
    }

    $(document).on('click', '#activity-tab a.activity-min', function() {
        setMinimized(true, true);
    });
    $(document).on('click', '#activity-tab a.activity-max', function() {
        setMinimized(false, true);
    });
    $(document).on('click', '#activity-tab a.activity-close', function() {
        activityTab.pollers = {};
        activityTab.cancel();
    });

    $(document).ready(function(){
        activityTab.init();
    });

    return XNAT.app.activityTab = activityTab;
}));
