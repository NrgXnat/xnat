/*
 * web: settingsTabMgmt.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
/*******************************************************************************
 * Set of functions to facilitate settings management via AJAX
 */
var sessExpMsgT = 'WARNING: Your session has expired.<br/><br/>You will need to re-login and navigate to the content.';
function configurationIndexChanged() {
    var activeIndex = this.get("activeIndex");
    YAHOO.util.Cookie.set("configuration.tab.index", activeIndex);
}

function fullConfigHandler() {
    if (!document.getElementById('siteId').value) {
        xmodal.message('Site ID Required','You must specify a value for the site ID!','OK');
        return;
    }

    this.fullConfigCallback = {
        success : function() {
            // Reset buttons to use standard save mechanism.  The system is initialized after the first attempted additional saves will fail if they use the initialize method in fullConfigHandler
            // this SHOULD be safe.  The ArcSpec.isComplete() is the method the Restlet uses to see if the arc spec is built.  All the properties that isComplete() checks are populated by default except SITE_ID.  But site_id is checked at the beginning of this method.
            document.getElementById('siteInfo_save_button').onclick = saveSettings;
            document.getElementById('security_save_button').onclick = saveSettings;
            document.getElementById('fileSystem_save_button').onclick = saveSettings;
            document.getElementById('registration_save_button').onclick = saveSettings;
            document.getElementById('notifications_save_button').onclick = saveSettings;
            document.getElementById('anonymization_save_button').onclick = saveSettings;
            document.getElementById('seriesImportFilter_save_button').onclick = saveSettings;
            document.getElementById('dicomReceiver_save_button').onclick = saveSettings;

            xmodal.loading.close();
            xmodal.message('Welcome!','Your settings were saved. You will now be redirected to the main XNAT page.','OK');

            var destination;
            if (serverRoot) {
                destination = serverRoot;
            } else {
                destination = "/";
            }
            window.location.replace(destination);
        },
        failure : function(o) {
            xmodal.message(
                'Error',
                'Your settings were not successfully saved: ' + o.responseText,
                'OK'
            );
        },
        cache : false, // Turn off caching for IE
        scope : this
    };

    var data = buildSettingsUpdateRequestBody([
        'siteId','uiDebugExtensionPoints', 'siteDescriptionType', 'siteDescriptionText', 'siteDescriptionPage', 'siteUrl', 'siteAdminEmail', 'siteLoginLanding', 'siteLandingLayout', 'siteHome', 'siteHomeLayout'
        , 'enableCsrfToken', 'enableCsrfEmail', 'restrictUserListAccessToAdmins', 'uiAllowNonAdminProjectCreation', 'requireSaltedPasswords', 'passwordExpirationType', 'passwordExpirationInterval', 'passwordExpirationDate'
        , 'archivePath', 'checksums', 'prearchivePath', 'cachePath', 'ftpPath', 'buildPath', 'pipelinePath'
        , 'requireLogin', 'enableNewRegistrations', 'emailVerification'
        , 'error', 'issue', 'newUser', 'update', 'emailAllowNonuserSubscribers', 'smt.enabled'
        , 'anonScript', 'anonEnabled', 'dcmPort', 'dcmAe', 'enableDicomReceiver'
    ]);

    xmodal.loading.open({title:'Please wait...'});
    var putUrl = serverRoot + '/data/services/settings/initialize?XNAT_CSRF=' + window.csrfToken + '&stamp=' + (new Date()).getTime();
    YAHOO.util.Connect.asyncRequest('PUT', putUrl, this.fullConfigCallback, data, this);
}

function saveSettings(){
    window.siteInfoManager.saveTabSettings();
}

function configurationTabManagerInit() {
    window.configurationTabView = new YAHOO.widget.TabView('configurationTabs');
    window.configuration_tabs_module = new YAHOO.widget.Module("configuration_tabs_module", {visible:false, zIndex:5});
    window.configuration_tabs_module.show();
    window.configurationTabView.subscribe("activeTabChange", configurationIndexChanged);
    if (window.initializing) {
        // If we're initializing, divert all of the save handlers to centralized handling.
        document.getElementById('siteInfo_save_button').onclick = fullConfigHandler;
        document.getElementById('security_save_button').onclick = fullConfigHandler;
        document.getElementById('fileSystem_save_button').onclick = fullConfigHandler;
        document.getElementById('registration_save_button').onclick = fullConfigHandler;
        document.getElementById('notifications_save_button').onclick = fullConfigHandler;
        document.getElementById('anonymization_save_button').onclick = fullConfigHandler;
        document.getElementById('seriesImportFilter_save_button').onclick = fullConfigHandler;
        document.getElementById('dicomReceiver_save_button').onclick = fullConfigHandler;
    }
}

var configurationControls = {};

function putConfigurationControls(key, controls) {
    configurationControls[key] = controls;
}

function getConfigurationControls(key) {
    return configurationControls[key];
}

var configurationControlValueHandlers = {};

function putConfigurationControlValueHandler(key, handler) {
    configurationControlValueHandlers[key] = handler;
}

function hasConfigurationControlValueHandler(key) {
    return key in configurationControlValueHandlers;
}

function invokeConfigurationControlValueHandler(key, value) {
    return configurationControlValueHandlers[key](value);
}

function SettingsTabManager(settingsTabDivId, settings, postLoad) {

    this.controls = [];
    this.settings = getConfigurationControls(settings);
    this.settings_tab_mgmt_div = document.getElementById(settingsTabDivId);
    this.settings_svc_url = serverRoot + '/data/services/settings/';

    this.dirtyFlag = false;

    if (postLoad) {
        this.postLoad = postLoad;
    } else {
        this.postLoad = null;
    }

    var resetButtons = '#siteInfo_reset_button, ' +
            '#security_reset_button, ' +
            '#fileSystem_reset_button, ' +
            '#registration_reset_button, ' +
            '#notifications_reset_button, ' +
            '#anonymization_reset_button, ' +
            '#seriesImportFilter_reset_button, ' +
            '#dicomReceiver_reset_button'
        ;

    this.enableResetButtons = function() {
        $(resetButtons).prop('disabled',false);
    };
    this.disableResetButtons = function() {
        $(resetButtons).prop('disabled',true);
    };
    this.disableResetButtons();

    this.init = function() {
        if (window.configurationData) {
            this.processData();
        } else {
            xmodal.loading.open({title:'Loading site information...'});

            // load from settings data from server
            this.initCallback = {
                success : this.completeInit,
                failure : this.initFailure,
                cache : false, // Turn off caching for IE
                scope : this
            };

            var getUrl = this.settings_svc_url + '?XNAT_CSRF=' + window.csrfToken + '&format=json&stamp=' + (new Date()).getTime();
            YAHOO.util.Connect.asyncRequest('GET', getUrl, this.initCallback, null, this);
        }
    };

    this.completeInit = function(o) {
        try {
            this.processData(o.responseText);
        } catch (e) {
            this.displayError("[ERROR " + o.status + "] Failed to parse site information: [" + e.name + "] " + e.message);
        }
        xmodal.loading.close();
        if (window.initializing) {
            xmodal.message('Welcome', 'Your XNAT installation has not yet been initialized. Please review each panel on this configuration screen before saving the system settings.', 'OK');
        }
    };

    this.processData = function(data) {
        // If we got new data, cache that.
        if (data) {
            window.configurationData = data;
        } else {
            // But if not, use the cached data.
            data = window.configurationData;
        }

        this.controls.length = 0;
        var resultSet = eval("(" + data + ")");
        for (var index = 0; index < this.settings.length; index++) {
            var setting = this.settings[index];
            var control = document.getElementById(setting);
            if(!control){ //handle radio button sets
                control = document.getElementsByName(setting);
                this.controls.push(setting);
                for (var j = 0; j < control.length; j++) {
                    if(control[j].value == eval('resultSet.ResultSet.Result["' + setting+'"]')){
                        $(control[j]).trigger("click");
                    }
                }
            } else {
                control.defaultValue = eval('resultSet.ResultSet.Result["' + setting+'"]');
                if(control.tagName.toLowerCase()=='select'){
                    control.value = control.defaultValue;
                }
                this.controls.push(control);
                if (!this.firstControl) {
                    this.firstControl = control;
                }
            }
        }
        this.render();
        if (this.postLoad != null) {
            this.postLoad();
        }
    };

    this.initFailure = function(o) {
        this.displayError("ERROR " + o.status + ": Failed to load site information.");
        if (o.status == 401) {
            xmodal.message('Session Expired', sessExpMsgT);
            window.location = serverRoot + "/app/template/Login.vm";
        }
    };

    this.render = function() {
        this.resetForm();
    };

    this.setFormDisabled = function(value) {

        var $settings_div = $(this.settings_tab_mgmt_div);
        $settings_div.find(':input').prop('disabled',value);
        // the ':input' selector ^ grabs all input, textarea, select, and button elements
        // http://api.jquery.com/input-selector/

        window.toggleControlsOnAnonEnabled();
        window.toggleControlsOnFilterEnabled();

    };

    this.resetForm = function() {
        for (var index = 0; index < this.controls.length; index++) {
            var control = this.controls[index];
            if (control.type == 'text' || control.type == 'textarea') {
                control.value = control.defaultValue;
            } else if (control.type == 'checkbox') {
                control.checked = control.defaultValue.toLowerCase() === 'true';
            } else if (control.type == 'select-one') {
                $(control).val(control.defaultValue);
            } else if (control.type == 'hidden') {
                control.value = control.defaultValue;
                jq('#' + control.id + 'Label').html(control.defaultValue);
            } else if (typeof control == 'string') { //handle radio buttons
                var defaults = eval("("+window.configurationData+")");
                var def = eval('defaults.ResultSet.Result["'+control+'"]');
                control = $('[name="' + control + '"]');
                for (var j = 0; j < control.length; j++) {
                    if(control[j].value == def){
                        $(control[j]).trigger("click");
                    }
                }
            }
        }
        this.setFormDisabled(false);
        this.dirtyFlag = false;
        this.disableResetButtons();
        if (this.firstControl) {
            this.firstControl.focus();
        }
    };

    this.dirtyForm = function() {
        this.dirtyFlag = true;
        this.enableResetButtons();
    };

    this.saveTabSettings = function() {
        if (this.validateSettings()) {
            if (this.isDirty()) {
                this.setFormDisabled(true);
                this.updateCallback = {
                    success : this.completeSave,
                    failure : this.saveFailure,
                    cache : false, // Turn off caching for IE
                    scope : this
                };
                xmodal.loading.open({title:'Please wait...'});
                var data = buildSettingsUpdateRequestBody(this.controls);
                YAHOO.util.Connect.asyncRequest('POST', this.settings_svc_url + '?XNAT_CSRF=' + window.csrfToken, this.updateCallback, data, this);
            } else {
                xmodal.message('Message','None of the site information appears to have changed.','OK');
            }
        } else {
            xmodal.message('Note','You need to enter a value into all of the site information settings boxes to save the site settings.','OK');
        }
    };

    this.validateSettings = function() {
        for (var index = 0; index < this.controls.length; index++) {
            var control = this.controls[index];
            if (control.type == 'text') {
                if (!control.value) {
                    return false;
                }
            }
        }
        return true;
    };

    /**
     * Combines the wasDirty() and hasBeenDirtied() calls to tell you if the resource had been dirtied previously or is
     * in a dirty state currently. Note that this can return true even if the form isn't truly dirty, i.e. it was
     * changed then reverted manually.
     * @returns True if previously or currently dirty.
     */
    this.isDirty = function() {
        return this.wasDirty() || this.hasBeenDirtied();
    };

    /**
     * Indicates whether the resource was ever dirtied. Note that this can return true even if the form isn't truly
     * dirty, i.e. it was changed then reverted manually. The only thing that should clear this flag is the reset
     * button.
     * @returns True if the resource was previously dirtied.
     */
    this.wasDirty = function() {
        return this.dirtyFlag;
    };

    /**
     * Indicates whether the form is currently dirtied.
     * @returns {boolean}
     */
    this.hasBeenDirtied = function() {
        for (var index = 0; index < this.controls.length; index++) {
            var control = this.controls[index];
            if (control.type === 'text' || control.type === 'textarea') {
                if (control.value != control.defaultValue) {
                    return true;
                }
            } else if (control.type === 'checkbox') {
                if (control.checked != control.defaultValue.toLowerCase()) {
                    return true;
                }
            }
        }
        return false;
    };

    this.completeSave = function(o) {
        this.processData(o.responseText);
        xmodal.loading.close();
        xmodal.message('Success','Your settings have been successfully updated.','OK');
        this.setFormDisabled(false);
    };

    this.saveFailure = function(o) {
        if (o.status == 401) {
            xmodal.message('Session Expired', sessExpMsgT);
            window.location.reload();
        }
        xmodal.loading.close();
        this.displayError('There was an error saving your notification settings. Please check that all of the configured usernames and addresses map to valid enabled users on your XNAT system.</p><p><b>Error code:</b> ' + o.status + ' ' + o.statusText);
        this.setFormDisabled(false);
    };

    this.displayError = function(errorMsg) {
        xmodal.message(
            'Error',
            errorMsg,
            'OK'
        );
    };

    this.init();
}

function buildSettingsUpdateRequestBody(controls) {
    var data = '';
    for (var index = 0; index < controls.length; index++) {
        var control;
        if('string' == typeof controls[index]) {
            control = document.getElementById(controls[index]);
            if(!control){ //handle radio button sets
                control = document.getElementsByName(controls[index]);
                for (var j = 0; j < control.length; j++) {
                    if(control[j].checked){
                        control={id: controls[index], value:control[j].value};
                        break;
                    }
                }
            }
        }
        else {
            control = controls[index];
        }
        // Filter out null control: happens when some text controls have no content.
        if (control != null) {
            if (data) {
                data += '&';
            }
            var value = (control.type == 'checkbox' ? control.checked : control.value);
            if (hasConfigurationControlValueHandler(control.id)) {
                value = invokeConfigurationControlValueHandler(control.id, value);
            }
            data += control.id + '=' + encodeURIComponent(value);
        }
    }

    return data;
}
