var XNAT = getObject(XNAT);
XNAT.app = getObject(XNAT.app || {});
XNAT.app.upload = getObject(XNAT.app.upload || {});
XNAT.app.upload.imageUploader = getObject(XNAT.app.upload.imageUploader || {});
XNAT.app.upload.datatypeHandlerMap = getObject(XNAT.app.upload.datatypeHandlerMap || {});
XNAT.app.upload.projectHandlerMap = getObject(XNAT.app.upload.projectHandlerMap || {});
XNAT.app.upload.defaultStr = "DEFAULT";

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
    let uploadName,
        fNameReplace = 'XNAME',
        usrResPath,
        uploaderUrl,
        archiverUrl = XNAT.url.csrfUrl('/data/services/import'),
        id = 'projuploader-modal',
        abuId = 'projuploader-modal-abu',
        interval;

    XNAT.app.upload.imageUploader.openUploadModal = function(config) {
        uploadName = 'upload' + getDateBasedId();
        usrResPath = '/user/cache/resources/' + uploadName + '/files/' + fNameReplace;
        uploaderUrl = XNAT.url.csrfUrl('/data' + usrResPath).replace(fNameReplace, '##FILENAME_REPLACE##');

        let loc;
        if (config.session) {
            loc = 'session: ' + config.session;
        } else if (config.subject) {
            loc = 'subject: ' + config.subject;
        } else {
            loc = 'project: ' + config.project;
        }

        let isDicomOrEcat = true;
        let importHandler = 'DICOM or ECAT';
        if (config['import-handler']) {
            importHandler = config['import-handler'];
            isDicomOrEcat = false;
        }
        const messages = [
            spawn('p', ['Upload compressed ' + importHandler + ' image files to your ' +
                loc.replace(/:.*/,'')])
        ];
        if (isDicomOrEcat) {
            messages.push(spawn('p', ['Review ', spawn('a|href="' +
                XNAT.url.fullUrl('/app/template/UploadOptions.vm') +
                '"', {}, 'alternative upload options'), '.']));
        }
        messages.push(spawn('div#' + abuId));

        function cancel() {
            abu._imageUploader.cancelUploads();
            if (interval) {
                window.clearInterval(interval);
                interval = null;
            }
        }
        function cancelAndClose() {
            cancel();
            xmodal.close(id);
        }

        xmodal.open({
            id: id,
            kind: 'dialog',
            title: 'Upload images to ' + loc,
            content: spawn('div', messages).outerHTML,
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
                        // only prompt
                        if ($('#' + id + '-process-button').prop('disabled')) {
                            cancelAndClose();
                        } else {
                            XNAT.ui.dialog.confirm('Confirm close',
                                'Are you sure you wish to close? Nothing will be submitted for archival.',
                                {
                                    buttons: [
                                        {
                                            label: 'Yes',
                                            close: true,
                                            isDefault: true,
                                            action: cancelAndClose
                                        },
                                        {
                                            label: 'No',
                                            close: true,
                                            isDefault: false,
                                            action: function () {}
                                        }
                                    ]
                                }
                            );
                        }
                    }
                },
                cancel: {
                    label: 'Cancel',
                    close: true,
                    action: cancel
                }
            }
        });

        abu.initializeImageUploader({
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
                if ($('#' + id + ' .abu-upload-complete-text').length === 0) {
                    $('#' + id + '-process-button').prop('disabled', true);
                } else {
                    if (!interval) {
                        XNAT.app.abu.runFunctionIfIdle(60000, function() {
                            $('#' + id + '-process-button').click();
                        });
                    }
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

        abu._imageUploader.buildUploaderDiv();
        abu._imageUploader._currentAction = uploaderUrl;
    };

    function errorHandler(e, base){
        const info = e.responseText ? base + ': ' + e.responseText : base;
        const details = spawn('p',[info]);
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
        $('#' + abuId + ' #file-upload-input').prop('disabled', true).addClass('disabled');
        $('#' + id + '-process-button').prop('disabled', true);
        const $statusDiv = $('#' + abuId + ' .abu-status');

        if ($statusDiv.length === 0 && interval) {
            // cancelled
            window.clearInterval(interval);
            interval = null;
            return;
        }

        const uploadInProg = $statusDiv.children().length === 0 || abu._imageUploader.uploadsInProgress > 0 ||
            abu._imageUploader.currentUploads > 0;
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

        const $targetFiles = $('#' + abuId + ' .abu-upload-filename');
        const nfiles = $targetFiles.length;
        let canCloseUploadDialog = true;
        let timeout = 0;
        $targetFiles.each(function(index) {
            const fname = $(this).text();
            const $parent = $(this).parent();

            const uploadFailed = $parent.find('.abu-upload-fail').length > 0;
            if (uploadFailed) {
                canCloseUploadDialog = false;
                return true;
            }
            window.setTimeout(function () {
                const uploadId = getDateBasedId();
                const formDataArchive = new FormData();
                formDataArchive.append("src", usrResPath.replace(fNameReplace, fname));
                formDataArchive.append("http-session-listener", uploadId);

                for (let key of Object.keys(config)) {
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
            timeout = 200; // space out archival requests a tad
        });
    }

    function getDateBasedId() {
        return (new Date()).toISOString().replace(/[^\w]/gi,'');
    }

    XNAT.app.upload.imageUploader.openUploadViaDesktopClient = function(config) {
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
                const token = (!isObject(data)) ? JSON.parse(data) : data;
                const prms = new URLSearchParams();
                prms.append('a', token.alias);
                prms.append('s', token.secret);
                for (let key of Object.keys(config)) {
                    if (config[key]) {
                        prms.append(key, config[key]);
                    }
                }
                const url = XNAT.url.xnatUrl('/upload?' + prms.toString());
                window.location.assign(url);
                const warning = XNAT.ui.dialog.message({
                    title: 'XNAT Desktop Client',
                    content: 'If nothing prompts from browser, ' +
                        '<a href="https://download.xnat.org/desktop-client" target="_blank">' +
                        'download and install XNAT Desktop Client' +
                        '</a> and try again.'
                });
                setTimeout(function(){
                    XNAT.ui.dialog.close(warning, true);
                }, 5000);
            }
        });
    };

    XNAT.app.upload.imageUploader.getProjectConfig = function(project, callbackSuccess, callbackError) {
        $.ajax({
            url: XNAT.url.rootUrl('/data/projects/' + project + '/config/upload/handlers?contents=true'),
            success: function(data) {
                XNAT.app.upload.projectHandlerMap[project] = JSON.parse(data);
                callbackSuccess();
            },
            error: function(xhr) {
                if (xhr.status === 404) {
                    XNAT.app.upload.projectHandlerMap[project] = XNAT.app.upload.defaultStr;
                    callbackSuccess();
                } else {
                    callbackError();
                    XNAT.ui.dialog.message("Unable to determine project upload handlers",
                        "Unable to retrieve project upload handlers from config service: " + xhr.responseText)
                }
            }
        });
    }

    function findHandlerForDataTypeByProject(project, datatype) {
        const projectUploaders = XNAT.app.upload.projectHandlerMap[project];
        if (projectUploaders === XNAT.app.upload.defaultStr) {
            return null;
        }
        let handler = null;
        $.each(projectUploaders, function(key, value) {
            if (value === datatype) {
                handler = key;
                return false;
            }
        });
        return handler;
    }

    function addHandlerToConfigAndRunCallback(config, callback) {
        let handler = findHandlerForDataTypeByProject(config.project, config.datatype);
        if (!handler) {
            handler = XNAT.app.upload.datatypeHandlerMap[config.datatype];
        }
        if (handler) {
            config['import-handler'] = handler;
        }
        callback(config);
    }

    XNAT.app.upload.imageUploader.determineHandler = function(config, callback) {
        if (XNAT.app.upload.projectHandlerMap.hasOwnProperty(config.project)) {
            addHandlerToConfigAndRunCallback(config, callback);
        } else {
            const waitDialog = XNAT.ui.dialog.static.wait('Determining handler...');
            XNAT.app.upload.imageUploader.getProjectConfig(config.project, function() {
                addHandlerToConfigAndRunCallback(config, callback);
                waitDialog.close();
            }, function() {waitDialog.close()})
        }
    }

    XNAT.app.upload.imageUploader.uploadImages = function(config) {
        // If import handler is not specified, let's see if we have a mapping for the datatype
        if (!config['import-handler'] && config.datatype ) {
            XNAT.app.upload.imageUploader.determineHandler(config, XNAT.app.upload.imageUploader.openAppropriateDialog);
            return;
        }
        XNAT.app.upload.imageUploader.openAppropriateDialog(config);
    };

    XNAT.app.upload.imageUploader.openAppropriateDialog = function(config) {
        // If no import handler and config.modal undefined or false, open Desktop Client
        if (!config['import-handler'] && (!config.modal || config.modal === "false")) {
            XNAT.app.upload.imageUploader.openUploadViaDesktopClient(config);
        } else {
            XNAT.app.upload.imageUploader.openUploadModal(config);
        }
    };

    $(document).on('click', 'a#uploadImages, a.uploadImages', function() {
        // DEVELOPER TAKE NOTE: this will pass data-my-key=value to /data/services/import as my-key=value
        let config = {};
        $.each($(this).data(), function(k,v) {
            // Convert from camel case (myKey) back to dash case (my-key)
            let newKey = k.replace(/([a-zA-Z])(?=[A-Z])/g, '$1-').toLowerCase();
            config[newKey] = v;
        });
        XNAT.app.upload.imageUploader.uploadImages(config);
    });
}));
