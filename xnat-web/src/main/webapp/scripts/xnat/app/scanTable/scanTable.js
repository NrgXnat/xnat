/**
 * Functionality for scan tables on image session pages
 */

console.log('scanTable.js');

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
}(function() {

    XNAT.app = getObject(XNAT.app || {});
    const scanTable = XNAT.app.scanTable = getObject(XNAT.app.scanTable || {});

    let projectId = XNAT.data.context.project;
    let subjectId = XNAT.data.context.subjectID;
    let exptId    = XNAT.data.context.ID;

    // const htmlUrlExample = '/data/experiments/XNAT_E01124/scans/0,1,2/files?format=html';
    // const zipUrlExample  = '/data/experiments/XNAT_E01124/scans/0,1,2/files?format=zip';

    scanTable.container$ = $('#selectable-table-scans');
    scanTable.dataTable$ = $('#scan-data-table');

    function scanUrl(part){
        return XNAT.url.rootUrl(
            '/REST' +
            '/projects/' + projectId +
            '/subjects/' + subjectId +
            '/experiments/' + exptId +
            (part || '')
        )
    }

    // extract scan id from a.href
    function getScanId(a){
        return $(a).data('id').toString();
    }

    // create then destroy iframe to initiate download
    function downloadIframe(url){
        var iframedId = randomID('dli', false);

        var downloadFrame$ = $.spawn('iframe.download.hidden', {
            id: iframedId,
            attr: { src: url }
        });

        $('body').append(downloadFrame$);

        // remove the iframe after 5 seconds
        window.setTimeout(function(){
            downloadFrame$.remove();
        }, 5000);
    }

    const grids = {
        "Original": "",
        "1x1 Montage":"1X1",
        "1x2 Montage":"1X2",
        "1x3 Montage":"1X3",
        "2x2 Montage":"2X2",
        "2x3 Montage":"2X3",
        "3x3 Montage":"3X3"
    };

    function getElementHtmlForSrc(src,thumb) {
        if (thumb === undefined) thumb = src.replace('/snapshot','/thumbnail');
        return '<a target="_blank" class="scan-original-link" href="' + src + '">' +
               '   <img class="scan-snapshot" src="' + thumb + '"/>' +
               '</a>';
    }

    function loadSnapshotImageNoBlocking(scanID, view) {
        const element = $(".span-scan" + scanID + "snapshot");
        const exprId = element ? element.data('expt-id') : null;
        const elementLoaded = element ? element.data('loaded') : false;
        if (exprId) {
            const src = serverRoot +
                '/xapi/projects/' + projectId + '/experiments/' + exprId + '/scan/' + scanID + '/snapshot' +
                (view ? '/' + view : '');

            if (elementLoaded && !view) {
                element.html(getElementHtmlForSrc(src));
                return true;
            }

            element.html('Generating and loading ' + (view ? view : 'snapshot'));
            $.ajax({
                url: XNAT.url.restUrl(src),
                type: 'HEAD',
                success: function() {
                    element.data('loaded', true);
                    element.html(getElementHtmlForSrc(src));
                    if (!elementLoaded) {
                        let montageSel = $('<select class="select-montage" name='+scanID+'></select>');
                        $.each(grids, function(key, value) {
                            montageSel.append($('<option>', {
                                value: value,
                                text: key
                            }));
                        });
                        element.after($('<div></div>').append(montageSel));
                    }
                },
                error: function() {
                    element.html('No snapshot available');
                }
            });
        } else if (element) {
            element.html('No snapshot available');
        }

        return true;
    }
    
    // inline scan table functions
    scanTable.displayScanDetails = function(scanId){

        if (!scanId) return false;
        
        // If the details modal for this scan is open, don't open it again.
        if(scanTable.scanDetailsOpen.includes(scanId)) return false;
        
        scanTable.scanDetailsOpen.push(scanId);
        var tmpl = $('#scan-' + scanId + '-details-template').html();

        XNAT.ui.dialog.message({
            title: 'Scan ' + scanId,
            width: 720,
            content: tmpl,
            isDraggable: true,
            mask: false,
            esc: true,
            okLabel: 'Close',
            afterShow: function() {
                loadSnapshotImageNoBlocking(scanId);
            },
            afterClose: function(){
                delete scanTable.scanDetailsOpen[scanTable.scanDetailsOpen.indexOf(scanId)];
            },
            footer: {
                content: 'Click in the header to move this dialog around the page'
            }
        });

    };

    scanTable.displayScanDetailsGrid = function(scanId, view){
        if (!scanId) {
            return false;
        }
        loadSnapshotImageNoBlocking(scanId, view);
     }
     
    // download all selected scans
    function downloadSelectedScans(){
        var selectedScans = [];
        scanTable.dataTable$.find('input.selectable-select-one:checked').each(function(){
            selectedScans.push(this.value);
        });
        var url = scanUrl('/scans/' + selectedScans + '/files?format=zip');
        downloadIframe(url);
    }


    // download a *single* scan
    function downloadScan(scanId){
        scanId = scanId || getScanId(this);
        var url = scanUrl('/scans/' + scanId + '/files?format=zip');
        downloadIframe(url);
    }

    function removeScanRow(scanId){
        // remove scan row
        scanTable.dataTable$.find('tr[data-scan="' + scanId + '"]').remove();
    }

    function deleteScan(scanId){
        scanId = scanId || getScanId(this);
        XNAT.ui.dialog.open({
            title: 'Delete Scan ' + scanId,
            width: 480,
            nuke: true, // destroys the modal on close, rather than preserving its state
            content: XNAT.spawner.spawn({
                panel: {
                    tag: 'div.panel',
                    contents: {
                        msg: {
                            tag: 'div',
                            content: '' +
                                '<p>This will delete scan <b>' + scanId + '</b> from this image session.</p>' +
                                '<div class="warning">This deletion is permanent and cannot be undone.</div>' +
                                '<br>'
                        },
                        delete_files: {
                            kind: 'panel.input.switchbox',
                            name: 'delete_files',
                            id: 'delete_files',
                            value: true,
                            values: 'true|false',
                            checked: true,
                            label: 'Delete Scan Files',
                            onText: 'Yes (Default)',
                            offText: 'No',
                            description: 'If selected, all scan files will be <br>deleted from the filesystem'
                        }
                    }
                }
            }).get(),
            buttons: [
                {
                    label: 'Delete Scan',
                    isDefault: true,
                    close: false,
                    action: function (obj) {
                        XNAT.app.requestJustificationDialog(function(event_reason){
                            var reasonStr = event_reason ? "event_reason=" + encodeURIComponent(event_reason) : "";
                            var deleteUrl   = '/REST/experiments/' + XNAT.data.context.ID + '/scans/' + scanId;
                            var deleteFiles = obj.dialogBody$.find('#delete_files').is(':checked');
                            var params = [
                                'format=json',
                                'event_action=Removed scan',
                                'event_type=WEB_FORM',
                                reasonStr
                            ];

                            if (deleteFiles) {
                                params.push('removeFiles=true');
                            }
                            
                            XNAT.xhr.delete({
                                url: XNAT.url.csrfUrl(deleteUrl, params),
                                success: function(data){
                                    var msg = 'Scan deleted';
                                    msg += (deleteFiles) ? ' and scan files permanently removed from file system.' : '.';

                                    XNAT.ui.dialog.open({
                                        title: 'Success',
                                        width: 360,
                                        content: '<p>'+msg+' Page will reload.</p>',
                                        buttons: [
                                            {
                                                label: 'OK',
                                                isDefault: true,
                                                close: true,
                                                action: function(){
                                                    XNAT.ui.dialog.closeAll();
                                                    window.location.reload();
                                                }
                                            }
                                        ]
                                    })
                                },
                                fail: function(e){
                                    XNAT.ui.dialog.open({
                                        title: 'Error',
                                        width: 360,
                                        content: '<p><strong>Error ' + e.status + '</strong></p><p>' + e.statusText + '</p>',
                                        buttons: [
                                            {
                                                label: 'OK',
                                                isDefault: true,
                                                close: true
                                            }
                                        ]
                                    })
                                }
                            }) // end XNAT.xhr.delete
                        }) // end justification
                    } // end action function
                },
                {
                    label: 'Cancel',
                    isDefault: false,
                    close: true
                }
            ]
        });
    }

    // bulk-delete selected scans
    // function deleteScans(scanIds){
    // // no back-end support for this yet
    // }

    function editScanNote(e) {

        var this$ = $(this);
        var scanId = this$.data('scanId');
        var xsiType = XNAT.data.context.xsiType;
        var noteText = $('#scan-' + scanId + '-note').find('.scan-note-content > span').html().trim();
        var noteEditor = spawn('textarea.note-editor', {
            name: xsiType + '/scans/scan[ID=' + scanId + ']/note',
            value: noteText,
            attr: {rows: 10, maxlength: 2500},
            style: {width: '100%'}
        });

        var editorForm$ = $.spawn('form.scan-note-editor', [noteEditor]);

        XNAT.ui.dialog.open({
            title: 'Edit Note for Scan ' + scanId,
            width: 480,
            destroyOnClose: true,
            content: editorForm$[0],
            afterShow: function () {
                $(noteEditor).focus();
            },
            buttons: [
                {
                    label: 'Update Note',
                    isDefault: true,
                    close: false,
                    action: function (obj) {
                        XNAT.app.requestJustificationDialog(function(event_reason){
                            var reasonStr = event_reason ? "event_reason=" + encodeURIComponent(event_reason) : "";
                            
                            var updateNoteUrl = scanUrl('?req_format=form&' + reasonStr, projectId, subjectId);
                            if (!noteEditor.value) {
                                noteEditor.value = ' '
                            }
                            var updateNoteString = editorForm$.serialize() || '';

                            // force the note field to save as empty
                            if (updateNoteString.length === 0) updateNoteString = 'NULL';

                            XNAT.xhr.put({
                                url: XNAT.url.csrfUrl(updateNoteUrl),
                                data: updateNoteString,
                                success: function (data) {
                                    XNAT.ui.dialog.message({
                                        title: 'Success',
                                        width: 360,
                                        content: '<p>Scan note updated. Page will reload.</p>',
                                        enter: true,
                                        buttons: [
                                            {
                                                label: 'OK',
                                                isDefault: true,
                                                close: true,
                                                action: function () {
                                                    XNAT.ui.dialog.closeAll();
                                                    window.location.reload();
                                                }
                                            }
                                        ]
                                    })
                                },
                                fail: function (e) {
                                    XNAT.ui.dialog.message({
                                        title: 'Error',
                                        width: 360,
                                        content: '<p><strong>Error ' + e.status + '</strong></p><p>' + e.statusText + '</p>',
                                        enter: true,
                                        esc: true,
                                        buttons: [
                                            {
                                                label: 'OK',
                                                isDefault: true,
                                                close: true
                                            }
                                        ]
                                    })
                                }
                            }) // end XNAT.xhr.put
                        }) //end justification
                    }
                },
                {
                    label: 'Cancel',
                    close: true
                }
            ]
        });

    }

// init function for XNAT.app.scanTable
    scanTable.init = function(){
        projectId = XNAT.data.context.project;
        subjectId = XNAT.data.context.subjectID;
        exptId    = XNAT.data.context.ID;

        var scanTableContainer$ = scanTable.container$ = $('#selectable-table-scans');

        // get this once and cache for later use
        var scanDataTable$ = scanTable.dataTable$ = $('#scan-data-table');

        //
        scanTableContainer$.on('click.view-details', 'a[href^="#!details"]', function viewDetailsFn(e){
            e.preventDefault();
            displayScanDetails.call(this, getScanId(this));
        });

        // Download a *single* scan (?)
        scanTableContainer$.on('click.download-scan', 'a[href^="#!download"]', function downloadScanFn(e){
            e.preventDefault();
            downloadScan.call(this, getScanId(this));
        });

        //
        scanTableContainer$.on('click.delete-scan', 'a[href^="#!delete"]', function deleteScanFn(e){
            e.preventDefault();
            deleteScan.call(this, getScanId(this));
        });

        // Download *all* selected scans
        scanTableContainer$.on('click.download-scans', 'button.do-scan-download:not(.disabled)', function downloadSelectedFn(e){
            e.preventDefault();
            downloadSelectedScans.call(this);
        });

        //
        scanTableContainer$.on('click', '.edit-scan-note', editScanNote);
    };

    $(document).ready(scanTable.init);

    $(document).on('click', 'table.scan-details a.view-dicom-headers', function(e){
        e.preventDefault();
        XNAT.dialog.load($(this).attr('href') + ' #layout_content table.dump', { minWidth: 800, width: '80%', esc: true, enter: true });
    });
    $(document).on('click', '.view-scan-details', function(){
        var scanId = $(this).data('id').toString();
        if (scanId) { scanTable.displayScanDetails(scanId) }
        else { console.log('No Scan ID found') }
    });

    $(document).on('change', '.select-montage', function(){
        var gridVal = $(this).children("option:selected").val();
        var scanId = $(this).attr('name').toString();
        if (scanId ) {
            scanTable.displayScanDetailsGrid(scanId,gridVal);
        }
        else { console.log('No Scan ID found') }
    });
      
   // Array that keeps track of which scan details modals are open.
    scanTable.scanDetailsOpen = [];

    // this script has loaded
    scanTable.loaded = true;
    return XNAT.app.scanTable = scanTable;
}));
