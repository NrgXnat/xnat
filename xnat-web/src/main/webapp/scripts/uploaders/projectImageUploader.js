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
    // NOTE: fileuploader.js expects the id variable below to be xmodal-abu,
    // but I don't want to use that bc I want to do my own button handling
    var uploadName = 'upload' + getDateBasedId(),
        fNameReplace = 'XNAME',
        usrResPath = '/user/cache/resources/' + uploadName + '/files/' + fNameReplace,
        uploaderUrl = XNAT.url.csrfUrl('/data' + usrResPath).replace(fNameReplace, '##FILENAME_REPLACE##'),
        archiverUrl = XNAT.url.csrfUrl('/data/services/import'),
        id = 'projuploader-modal',
        abuId = 'projuploader-modal-abu';

    function openUploadModal(project) {
        xmodal.open({
            id: id,
            kind: 'dialog',
            title: 'Upload Images to Project ' + project,
            content: spawn('div', [
                spawn('p', ['Upload zipped (.zip or .tar.gz) DICOM or ECAT image files to your project']),
                spawn('p', ['Review ', spawn('a|href="' + XNAT.url.fullUrl('/app/template/UploadOptions.vm')
                    + '"', {}, 'alternative upload options'), '.']),
                spawn('div#' + abuId)
            ]).outerHTML,
            beforeShow: function(obj) {
                obj.$modal.find('#' + id + '-process-button').prop('disabled','disabled');
                obj.$modal.find('#' + id + '-done-button').hide();
            },
            buttons: {
                process: {
                    label: 'Begin archival',
                    isDefault: true,
                    close: false,
                    action: function() {
                        submitForArchival(project);
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
                    action: function() {}
                }
            }
        });

        abu.initializeUploader({
            element: $('#' + abuId),
            uploadStartedFunction: function(){
                $('#' + id + '-process-button').prop('disabled', true);
                $('#' + id + '-cancel-button').show();
                $('#' + id + '-done-button').hide();
            },
            uploadCompletedFunction: function(anyFailedUploads) {
                $('#' + id + '-cancel-button').hide();
                $('#' + id + '-done-button').show();
                if ($('.abu-upload-complete-text').length === 0) {
                    $('#' + id + '-process-button').prop('disabled', true);
                } else {
                    $('#' + id + '-process-button').prop('disabled', false);
                }
            },
            processFunction: function(){},
            doneFunction: function(){},
            showEmailOption: false,
            showCloseOption: false,
            showExtractOption: false,
            showVerboseOption: false,
            showUpdateOption: false
        });

        abu._fileUploader.buildUploaderDiv();
        abu._fileUploader._currentAction = uploaderUrl;
    }

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

    function getDateBasedId() {
        return (new Date()).toISOString().replace(/[^\w]/gi,'');
    }

    function submitForArchival(project) {
        $('#file-upload-input').prop('disabled', true).addClass('disabled');
        $('#' + id + '-process-button').prop('disabled', true);

        var $targetFiles = $('.abu-upload-filename');
        var nfiles = $targetFiles.length;
        var canCloseUploadDialog = true;
        $targetFiles.each(function (index) {
            var timeout = index === 0 ? 0 : 1000;
            var $statusDiv = $(this).siblings('.abu-status');
            var fname = $(this).text();

            var uploadInProg = $statusDiv.children().length === 0 || abu._fileUploader.uploadsInProgress > 0 ||
                abu._fileUploader.currentUploads > 0;
            if (uploadInProg) {
                canCloseUploadDialog = false;
                // Shouldn't happen, but just in case...
                $('#' + id + '-process-button').prop('disabled', false);
                XNAT.ui.dialog.message(fname + ' upload still in progress, please request archival ' +
                    'when upload completes');
                return;
            }

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
                formDataArchive.append("project", project);
                formDataArchive.append("prearchive_code", "0");

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

    $(document).on('click', 'a#uploadImages', function() {
        openUploadModal($(this).data('project'));
    });
}));