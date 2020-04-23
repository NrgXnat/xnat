var XNAT = getObject(XNAT);
XNAT.app = getObject(XNAT.app || {});
XNAT.app.imageUploader = getObject(XNAT.app.imageUploader || {});

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
    // NOTE: fileuploader.js expects the id variable below to be xmodal-abu,
    // but I don't want to use that bc I want to do my own button handling
    var uploadName,
        fNameReplace = 'XNAME',
        usrResPath,
        uploaderUrl,
        archiverUrl = XNAT.url.csrfUrl('/data/services/import'),
        id = 'projuploader-modal',
        abuId = 'projuploader-modal-abu',
        interval;

    XNAT.app.imageUploader.openUploadModal = function(config) {
        uploadName = 'upload' + getDateBasedId();
        usrResPath = '/user/cache/resources/' + uploadName + '/files/' + fNameReplace;
        uploaderUrl = XNAT.url.csrfUrl('/data' + usrResPath).replace(fNameReplace, '##FILENAME_REPLACE##');

        var loc;
        if (config.session) {
            loc = 'session: ' + config.session;
        } else if (config.subject) {
            loc = 'subject: ' + config.subject;
        } else {
            loc = 'project: ' + config.project;
        }
        var importHandler = 'DICOM or ECAT';
        if (config.importhandler) {
            importHandler = config.importhandler;
        }

        xmodal.open({
            id: id,
            kind: 'dialog',
            title: 'Upload images to ' + loc,
            content: spawn('div', [
                spawn('p', ['Upload zipped (.zip or .tar.gz) ' + importHandler + ' image files to your ' +
                    loc.replace(/:.*/,'')]),
                spawn('p', ['Review ', spawn('a|href="' + XNAT.url.fullUrl('/app/template/UploadOptions.vm')
                    + '"', {}, 'alternative upload options'), '.']),
                spawn('div#' + abuId)
            ]).outerHTML,
            beforeShow: function(obj) {
                obj.$modal.find('#' + id + '-done-button').hide();
                obj.$modal.find('#' + id + '-process-button').prop('disabled', true);
            },
            buttons: {
                process: {
                    label: 'Request archival',
                    isDefault: true,
                    close: false,
                    action: function() {
                        submitForArchival(config);
                    }
                },
                done: {
                    label: 'Close',
                    isDefault: false,
                    close: false,
                    action: function() {
                        XNAT.ui.dialog.confirm('Confirm close',
                            'Are you sure you wish to close? Nothing will be submitted for archival.',
                            {
                                buttons: [
                                    {
                                        label: 'No',
                                        close: true,
                                        isDefault: false,
                                        action: function() {}
                                    },
                                    {
                                        label: 'Yes',
                                        close: true,
                                        isDefault: true,
                                        action: function() {
                                            if (interval) {
                                                window.clearInterval(interval);
                                                interval = null;
                                            }
                                            xmodal.close(id);
                                        }
                                    }
                                ]
                            }
                        );
                    }
                },
                cancel: {
                    label: 'Cancel',
                    close: true,
                    action: function() {
                        if (interval) {
                            window.clearInterval(interval);
                            interval = null;
                        }
                    }
                }
            }
        });

        abu.initializeUploader({
            element: $('#' + abuId),
            uploadStartedFunction: function(){
                $('#' + id + '-cancel-button').show();
                $('#' + id + '-done-button').hide();
                if (!interval) {
                    $('#' + id + '-process-button').prop('disabled', false);
                }
            },
            uploadCompletedFunction: function(anyFailedUploads) {
                $('#' + id + '-cancel-button').hide();
                $('#' + id + '-done-button').show();
                if ($('.abu-upload-complete-text').length === 0) {
                    $('#' + id + '-process-button').prop('disabled', true);
                }
            },
            processFunction: function(){},
            doneFunction: function(){},
            showEmailOption: false,
            showCloseOption: false,
            showExtractOption: false,
            showVerboseOption: false,
            showUpdateOption: false,
            acceptFilePattern: 'application/zip, application/x-gzip, application/x-tgz'
        });

        abu._fileUploader.buildUploaderDiv();
        abu._fileUploader._currentAction = uploaderUrl;
    };

    function errorHandler(e, base){
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

    function submitForArchival(config) {
        $('#file-upload-input').prop('disabled', true).addClass('disabled');
        $('#' + id + '-process-button').prop('disabled', true);
        var $statusDiv = $('#' + abuId + ' .abu-status');

        if ($statusDiv.length === 0 && interval) {
            // cancelled
            window.clearInterval(interval);
            interval = null;
            return;
        }

        var uploadInProg = $statusDiv.children().length === 0 || abu._fileUploader.uploadsInProgress > 0 ||
            abu._fileUploader.currentUploads > 0;
        if (uploadInProg) {
            if (!interval) {
                XNAT.ui.dialog.message('Archival requested!', 'Archival will begin automatically when all uploads complete.');
                interval = window.setInterval(function() {
                    submitForArchival(config);
                }, 2000);
            }
            return;
        } else if (interval) {
            window.clearInterval(interval);
            interval = null;
        }

        var $targetFiles = $('.abu-upload-filename');
        var nfiles = $targetFiles.length;
        var canCloseUploadDialog = true;
        $targetFiles.each(function (index) {
            var timeout = index === 0 ? 0 : 1000;
            var fname = $(this).text();

            var uploadFailed = $statusDiv.find('.abu-upload-fail').length > 0;
            if (uploadFailed) {
                canCloseUploadDialog = false;
                return;
            }

            var $parent = $(this).parent();
            window.setTimeout(function () {
                var uploadId = getDateBasedId();
                var formDataArchive = new FormData();
                formDataArchive.append("src", usrResPath.replace(fNameReplace, fname));
                formDataArchive.append("http-session-listener", uploadId);
                formDataArchive.append("prearchive_code", "0");

                for (var key of Object.keys(config)) {
                    if (config[key]) {
                        formDataArchive.append(key, config[key]);
                    }
                }

                $.ajax({
                    method: 'POST',
                    contentType: false,
                    url: archiverUrl,
                    data: formDataArchive,
                    processData: false,
                    cache: false,
                    beforeSend: function () {
                        XNAT.app.timeout.maintainLogin = true;
                    },
                    success: function () {
                        $parent.remove();
                        XNAT.app.activityTab.start('Upload extraction/review for archival', uploadId);
                    },
                    error: function (xhr) {
                        canCloseUploadDialog = false;
                        errorHandler(xhr, 'Issue requesting archival of ' + fname);
                    },
                    complete: function () {
                        if (index === nfiles-1 && canCloseUploadDialog) {
                            xmodal.close(id);
                        }
                        XNAT.app.timeout.maintainLogin = false;
                    }
                });
            }, timeout);
        });
    }

    function getDateBasedId() {
        return (new Date()).toISOString().replace(/[^\w]/gi,'');
    }

    XNAT.app.imageUploader.openUploadViaDesktopClient = function(config) {
        XNAT.xhr.get({
            url: XNAT.url.rootUrl('/data/services/tokens/issue'),
            fail: function(e){
                console.log(e);
                XNAT.ui.dialog.message({
                    title: 'Unexpected error',
                    content: 'Could not issue user token for XNAT Desktop Client.'
                });
            },
            success: function(data){
                var token = JSON.parse(data);
                const prms = new URLSearchParams();
                prms.append('a', token.alias);
                prms.append('s', token.secret);
                for (var key of Object.keys(config)) {
                    if (config[key]) {
                        prms.append(key, config[key]);
                    }
                }
                var url = XNAT.url.xnatUrl('/upload?' + prms.toString());
                window.location.assign(url);
                XNAT.ui.dialog.closeAll();
            }
        })
    };

    $(document).on('click', 'a#uploadImages, a.uploadImages', function() {
        if ($(this).data('modal') === 'true') {
            XNAT.app.imageUploader.openUploadModal($(this).data());
        } else {
            XNAT.app.imageUploader.openUploadViaDesktopClient($(this).data());
        }
    });
}));
