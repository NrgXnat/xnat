/*
 * web: EventServiceHistory.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * History Table Generator for Container Services
 */

console.log('EventServiceHistory.js');

var XNAT = getObject(XNAT || {});
XNAT.admin = getObject(XNAT.admin || {});

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
}(function() {

    /* ================ *
     * GLOBAL FUNCTIONS *
     * ================ */

    var eventServicePanel,
        undefined,
        rootUrl = XNAT.url.rootUrl,
        restUrl = XNAT.url.restUrl,
        csrfUrl = XNAT.url.csrfUrl;

    XNAT.admin.eventServicePanel = eventServicePanel =
        getObject(XNAT.admin.eventServicePanel || {});

    function spacer(width) {
        return spawn('i.spacer', {
            style: {
                display: 'inline-block',
                width: width + 'px'
            }
        })
    }

    function errorHandler(e, title, closeAll) {
        console.log(e);
        title = (title) ? 'Error: ' + title : 'Error';
        closeAll = (closeAll === undefined) ? true : closeAll;
        var errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + ': ' + e.statusText + '</strong></p><p>' + e.responseText + '</p>' : e;
        XNAT.dialog.open({
            width: 450,
            title: title,
            content: errormsg,
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    action: function () {
                        if (closeAll) {
                            xmodal.closeAll();
                            XNAT.ui.dialog.closeAll();
                        }
                    }
                }
            ]
        });
    }

    /* =============== *
     * Command History *
     * =============== */

    var historyTable, historyData;

    XNAT.admin.eventServicePanel.historyTable = historyTable =
        getObject(XNAT.admin.eventServicePanel.historyTable || {});

    XNAT.admin.eventServicePanel.historyData = historyData =
        getObject(XNAT.admin.eventServicePanel.historyData || {});

    const historyTableContainerId = 'history-table-container';

    function getHistoryUrl(project,sub){
        var params = [];
        if (project) params.push('project='+project);
        if (sub) params.push('subscriptionid='+sub);
        var appended = (params.length) ? '?'+params.join('&') : '';
        return XNAT.url.restUrl('/xapi/events/delivered' + appended);
    }

    function viewHistoryDialog(e, onclose) {
        e.preventDefault();
        var historyId = $(this).data('id') || $(this).closest('tr').prop('title');
        XNAT.admin.eventServicePanel.historyTable.viewHistory(historyId);
    }

    const labelMap = {
        id:             {label: 'ID', op: 'eq', type: 'number', show: true},
        DATE:           {label: 'Date', column: 'timestamp', show: true},
        subscription:   {label: 'Subscription Name', column: 'subscription-name', show: true},
        eventtype:      {label: 'Event Type', column: 'event-name', show: true},
        user:           {label: 'Run As User', column: 'user', show: true},
        status:         {label: 'Status', column: 'status', show: true},
        project:        {label: 'Project', column: 'project', show: true}
    };

    function historyTableObject() {
        return {
            table: {
                classes: "clean fixed-header selectable scrollable-table",
                style: "width: auto;",
                on: [
                    ['click', 'a.view-event-history', viewHistoryDialog]
                ]
            },
            before: {
                filterCss: {
                    tag: 'style|type=text/css',
                    content:
                        '#' + historyTableContainerId + ' .id { max-width: 80px; } \n' +
                        '#' + historyTableContainerId + ' .timestamp { max-width: 150px; word-wrap: break-word; overflow-wrap: break-word; }  \n' +
                        '#' + historyTableContainerId + ' .subscription { max-width: 200px; word-wrap: break-word; overflow-wrap: break-word; }  \n' +
                        '#' + historyTableContainerId + ' .event-type { max-width: 120px; word-wrap: break-word; overflow-wrap: break-word; }  \n' +
                        '#' + historyTableContainerId + ' .user { max-width: 120px; word-wrap: break-word; overflow-wrap: break-word; }  \n' +
                        '#' + historyTableContainerId + ' .status { max-width: 150px; word-wrap: break-word; overflow-wrap: break-word; }  \n' +
                        '#' + historyTableContainerId + ' .project { max-width: 150px; word-wrap: break-word; overflow-wrap: break-word; }  \n'
                }
            },
            sortable: 'id, user, DATE, status',
            filter: 'id, user, status, project, subscription, eventtype',
            items: {
                id: {
                    th: {className: 'id'},
                    label: labelMap.id['label'],
                    apply: function(){
                        return this.id.toString();
                    }
                },
                DATE: {
                    label: labelMap.DATE['label'],
                    th: {className: 'DATE'},
                    apply: function () {
                        let timestamp = this['timestamp'];
                        let dateString = '';
                        if (timestamp) {
                            timestamp = timestamp.replace(/-/g, '/'); // include date format hack for Safari
                            if (timestamp.indexOf('UTC') < 0) {
                                timestamp = timestamp.trim() + ' UTC';
                            }
                            dateString = (new Date(timestamp)).toLocaleString();
                            // dateString = timestamp.toISOString().replace('T',' ').replace('Z',' ').split('.')[0];

                        } else {
                            dateString = 'N/A';
                        }
                        return dateString;
                    }
                },
                subscription: {
                    th: {className: 'subscription'},
                    label: labelMap.subscription['label'],
                    apply: function () {
                        var message = '';
                        if (this['trigger-label']) {
                            message = message + '<br>Trigger: ' + source['trigger-label'];
                        }

                        return spawn('!',[
                            spawn('a.view-container-history', {
                                href: '#!',
                                title: 'View command history and logs',
                                data: {'id': this.id},
                                style: { wordWrap: 'break-word' },
                                html: this['subscription-name']
                            }),
                            message
                        ]);
                    }
                },
                eventtype: {
                    th: { className: 'event-type' },
                    label: labelMap.eventtype['label'],
                    apply: function(){
                        return this['event-name']
                    }
                },
                user: {
                    th: {className: 'user'},
                    label: labelMap.user['label'],
                    apply: function () {
                        return this['user-id']
                    }
                },
                status: {
                    th: {className: 'status'},
                    label: labelMap.status['label'],
                    apply: function(){
                        return this['status'];
                    }
                },
                project: {
                    th: {className: 'project'},
                    label: labelMap.project['label'],
                    apply: function(){
                        return this['project'];
                    }
                }
            }
        }
    }

    historyTable.$loadAllBtn = false;
    historyTable.workflowModal = function(workflowIdOrEvent) {
        var workflowId;
        if (workflowIdOrEvent.hasOwnProperty("data")) {
            // this is an event
            workflowId = workflowIdOrEvent.data.wfid;
        } else {
            workflowId = workflowIdOrEvent;
        }
        // rptModal in xdat.js
        rptModal.call(this, workflowId, "wrk:workflowData", "wrk:workflowData.wrk_workflowData_id");
    };

    historyTable.viewHistoryEntry = function(historyEntry) {
        var historyDialogButtons = [
            {
                label: 'Done',
                isDefault: true,
                close: true
            }
        ];

        // build nice-looking history entry table
        var pheTable = XNAT.table({
            className: 'xnat-table compact',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        });

        var allTables = [spawn('h3', 'Event Details'), pheTable.table];

        for (var key in historyEntry) {
            var val = historyEntry[key], formattedVal = '', putInTable = true;

            if (Array.isArray(val) && val.length > 0) {
                // Display a table
                var columns = [];
                val.forEach(function (item) {
                    if (typeof item === 'object') {
                        Object.keys(item).forEach(function(itemKey){
                            if(columns.indexOf(itemKey)===-1){
                                columns.push(itemKey);
                            }
                        });
                    }
                });


                formattedVal="<table class='xnat-table'>";
                if (columns.length > 0) {
                    formattedVal+="<tr>";
                    columns.forEach(function(colName){
                        formattedVal+="<th>"+colName+"</th>";
                    });
                    formattedVal+="</tr>";

                    val.sort(function(obj1,obj2){
                        // Sort by time recorded (if we have it)
                        var date1 = Date.parse(obj1["time-recorded"]), date2 = Date.parse(obj2["time-recorded"]);
                        return date1 - date2;
                    });
                } else {
                    // skip header if we just have one column
                    // sort alphabetically
                    val.sort()
                }

                val.forEach(function (item) {
                    formattedVal+="<tr>";
                    if (typeof item === 'object') {
                        columns.forEach(function (itemKey) {
                            formattedVal += "<td nowrap>";
                            var temp = item[itemKey];
                            if (typeof temp === 'object') temp = JSON.stringify(temp);
                            formattedVal += temp;
                            formattedVal += "</td>";
                        });
                    } else {
                        formattedVal += "<td nowrap>";
                        formattedVal += item;
                        formattedVal += "</td>";
                    }
                    formattedVal+="</tr>";
                });
                formattedVal+="</table>";
                putInTable = false;
            } else if (typeof val === 'object') {
                formattedVal = spawn('code', JSON.stringify(val));
            } else if (!val) {
                formattedVal = spawn('code', 'false');
            } else if (key === 'workflow-id') {
                // Allow pulling up detailed workflow info (can contain addl info in details field)
                var curid = '#wfmodal' + val;
                formattedVal = spawn('a' + curid, {}, val);
                $(document).on('click', curid, {wfid: val}, historyTable.workflowModal);
            } else {
                formattedVal = spawn('code', val);
            }

            if (putInTable) {
                pheTable.tr()
                    .td('<b>' + key + '</b>')
                    .td([spawn('div', {style: {'word-break': 'break-all', 'max-width': '600px', 'overflow':'auto'}}, formattedVal)]);
            } else {
                allTables.push(
                    spawn('div', {style: {'word-break': 'break-all', 'overflow':'auto', 'margin-bottom': '10px', 'max-width': 'max-content'}},
                        [spawn('div.data-table-actionsrow', {}, spawn('strong', {class: "textlink-sm data-table-action"},
                            'Container ' + key)), formattedVal])
                );
            }
        }

        // display history
        XNAT.ui.dialog.open({
            title: historyEntry.subscription.name + ': ' +historyEntry.subscription.created,
            width: 800,
            scroll: true,
            content: spawn('div', allTables),
            buttons: historyDialogButtons,
            header: true,
            maxBtn: true
        });
    };

    historyTable.viewHistory = function (id) {
        if (XNAT.admin.eventServicePanel.historyData.hasOwnProperty(id)) {
            historyTable.viewHistoryEntry(XNAT.admin.eventServicePanel.historyData[id]);
        } else {
            console.log(id);
            XNAT.ui.dialog.open({
                content: 'Sorry, could not display this history item.',
                buttons: [
                    {
                        label: 'OK',
                        isDefault: true,
                        close: true
                    }
                ]
            });
        }
    };

    historyTable.init = historyTable.refresh = function (context) {
        if (context) {
            historyTable.context = context;
        }
        function setupParams() {
            if (context) {
                XNAT.ui.ajaxTable.filters = XNAT.ui.ajaxTable.filters || {};
                // XNAT.ui.ajaxTable.filters['project'] = {operator: 'eq', value: context, backend: 'hibernate'};
            }
        }

        $('#' + historyTableContainerId).empty();
        XNAT.admin.eventServicePanel.historyTable.eventHistory = XNAT.ui.ajaxTable.AjaxTable(getHistoryUrl(),
            'event-history-table', historyTableContainerId, 'History', 'All Events',
            historyTableObject(), setupParams, null, dataLoadCallback, null, labelMap);

        XNAT.admin.eventServicePanel.historyTable.eventHistory.load();

    };

    function dataLoadCallback(data) {
        data.forEach(function (historyEntry) {
            // data.filter(function(entry){ return entry.id === historyEntry.id })[0].context = historyTable.context;
            historyEntry.context = historyTable.context;
            historyData[historyEntry.id] = historyEntry;
        });
    }
}));
