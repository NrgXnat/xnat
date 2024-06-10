/*
 * web: directArhive.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
}(function() {
    const directArchiveTableContainerId = 'direct-archive-table'; // see XDATScreen_prearchives.vm
    let directArchiveTable = {};

    const labelMap = {
        project: {label: 'Project', column: 'project', show: true},
        subject: {label: 'Subject', column: 'subject', show: true},
        session: {label: 'Session', column: 'folderName', show: true},
        scanDate: {label: 'Scan Date', column: 'scanDate', show: true},
        uploadDate: {label: 'Upload Date', column: 'lastBuiltDate', show: true},
        status: {label: 'Status', column: 'status', show: true},
    };

    function directArchiveTableObject() {
        return {
            table: {
                classes: "compact fixed-header selectable scrollable-table",
                style: "width: auto;",
            },
            sortable: 'project, subject, session, scanDate, uploadDate, status',
            filter: 'project, subject, session',
            items: {
                project: {
                    th: {className: 'project'},
                    td: {className: 'project word-wrapped'},
                    label: labelMap.project.label,
                    apply: function () {
                        return this[labelMap.project.column];
                    }
                },
                subject: {
                    th: {className: 'subject'},
                    td: {className: 'subject word-wrapped'},
                    label: labelMap.subject.label,
                    apply: function () {
                        return this[labelMap.subject.column];
                    }
                },
                session: {
                    th: {className: 'session'},
                    td: {className: 'session word-wrapped'},
                    label: labelMap.session.label,
                    apply: function () {
                        return this[labelMap.session.column];
                    }
                },
                scanDate: {
                    th: {className: 'scanDate'},
                    td: {className: 'scanDate word-wrapped'},
                    label: labelMap.scanDate.label,
                    apply: function () {
                        let time = this['scan_date']; // differs between SessionData and DirectArchiveSession
                        return time ? new Date(time).toLocaleString() : 'N/A';
                    }
                },
                uploadDate: {
                    th: {className: 'uploadDate'},
                    td: {className: 'uploadDate word-wrapped'},
                    label: labelMap.uploadDate.label,
                    apply: function () {
                        let time = this[labelMap.uploadDate.column];
                        return time ? new Date(time).toLocaleString() : 'N/A';
                    }
                },
                status: {
                    th: {className: 'status'},
                    td: {className: 'status word-wrapped'},
                    label: labelMap.status.label,
                    apply: function () {
                        const status = this[labelMap.status.column];
                        if (status.match(/^error/i)) {
                            const message = this['message'];
                            const project = this[labelMap.project.column];
                            const id = this['id'];
                            const statusSpan = spawn('span.text-error|title="' + message + '"', status);
                            const statusActions = spawn("span.inline-actions", [
                                spawn('i.fa.fa-times.da-delete|title="Delete"|data-project="'+ project + '"|data-id="' + id + '"')
                            ]);
                            return spawn("span", [statusSpan, statusActions]);
                        } else {
                            return status;
                        }
                    }
                }
            }
        }
    }

    $(document).on('click', '.da-delete', function () {
        const data = $(this).data();
        XNAT.ui.dialog.open({
            title: 'Delete',
            content: 'Are you sure you want to delete this entry?',
            buttons: [
                {
                    label: 'Yes',
                    isDefault: true,
                    close: true,
                    action: function () {
                        XNAT.xhr.delete({
                            url: XNAT.url.restUrl('/xapi/direct-archive/' + data.id),
                            success: directArchiveTable.history.reload,
                            error: function (xhr) {
                                let message = xhr.responseText;
                                if (xhr.status === 403) {
                                    message = 'only admins or project owners can delete direct archive entries';
                                }
                                XNAT.ui.dialog.message('Error', xhr.statusText + (message ? ': ' + message : ''));
                            }
                        });
                    }
                },
                {
                    label: 'Cancel',
                    isDefault: false,
                    close: true
                }
            ]
        });
    });

    directArchiveTable.init = directArchiveTable.refresh = function() {
        // add a "find by ID" input field after the table renders
        let target = $('#'+directArchiveTableContainerId);
        target.empty();
        target.show();
        directArchiveTable.history = XNAT.ui.ajaxTable.AjaxTable(XNAT.url.restUrl('/xapi/direct-archive'),
            'direct-archive-history-table', directArchiveTableContainerId, 'Direct archive', 'Sessions',
            directArchiveTableObject(), null, null, null,
            null, labelMap);

        directArchiveTable.history.load();
    };

    $(document).ready(function() {
        XNAT.xhr.getJSON(XNAT.url.rootUrl('/xapi/dicomscp')).success(function(data) {
            if (data.length > 0 && data.some(dr => dr.directArchive)) {
                directArchiveTable.init();
            }
        });
    });
}));