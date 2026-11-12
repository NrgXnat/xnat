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
    let uploaderUrl,
        id = 'projuploader-modal',
        abuId = 'projuploader-modal-abu';

    XNAT.app.upload.imageUploader.openUploadModal = function(config) {
        // Each file is sent with async=true: XNAT stores it, queues its import as soon as it has uploaded, and answers
        // with the import's tracking key
        const importParams = {async: 'true'};
        for (let key of Object.keys(config)) {
            if (config[key]) {
                importParams[key] = config[key];
            }
        }
        uploaderUrl = XNAT.url.csrfUrl('/data/services/import', importParams);

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
        }

        xmodal.open({
            id: id,
            kind: 'dialog',
            title: 'Upload images to ' + loc,
            content: spawn('div', messages).outerHTML,
            beforeShow: function(obj) {
                obj.$modal.find('#' + id + '-done-button').hide();
            },
            // Closing the dialog any way, with Cancel, Escape, or the title bar's X, cancels the files that haven't been
            // sent in full
            onClose: cancel,
            buttons: {
                done: {
                    label: 'Close',
                    isDefault: true,
                    close: true
                },
                cancel: {
                    label: 'Cancel',
                    close: true
                }
            }
        });

        // Uploads keep running after their dialog is closed, and can end after it has been opened again for other
        // files, so only the uploader this dialog was opened with changes it
        let uploader;
        abu.initializeImageUploader({
            element: $('#' + abuId),
            uploadStartedFunction: function(){
                if (abu._imageUploader !== uploader) {
                    return;
                }
                $('#' + id + '-cancel-button').show();
                $('#' + id + '-done-button').hide();
            },
            // Each upload answers with its import's tracking key, and the import has started, so track it right away:
            // the dialog may be closed before the other files have uploaded
            uploadSucceededFunction: function(key) {
                if (typeof key === 'string') {
                    XNAT.app.activityTab.start('Upload extraction/review for archival', key.trim());
                }
            },
            uploadCompletedFunction: function(anyFailedUploads) {
                if (abu._imageUploader !== uploader) {
                    return;
                }
                $('#' + id + '-cancel-button').hide();
                $('#' + id + '-done-button').show();
                if (!anyFailedUploads) {
                    xmodal.close(id);
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
        uploader = abu._imageUploader;

        abu._imageUploader.buildUploaderDiv();
        abu._imageUploader._currentAction = uploaderUrl;
    };

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
