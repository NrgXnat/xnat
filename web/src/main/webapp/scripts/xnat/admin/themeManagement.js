/*
 * web: themeManagement.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

var XNAT = getObject(XNAT);

(function($, console){

    var themeMgr;

    console.log('themeManagement.js');

    XNAT.admin =
        getObject(XNAT.admin||{});

    XNAT.admin.themeManager = themeMgr =
        getObject(XNAT.admin.themeManager||{});

    XNAT.theme =
        getObject(XNAT.theme||{});

    var themeName = XNAT.themeName || XNAT.theme.name || '';

    var themeUrl = XNAT.url.rootUrl('/xapi/themes');
    var s = '/', q = '?', a = '&';
    var csrf = {XNAT_CSRF: window.csrfToken};
    // ??
    // $('#titleAppName').text(XNAT.app.siteId);
    var currentTheme = $('#current-theme');
    var themeSelector = $('#theme-selection');
    var themeUploadForm = document.getElementById('xnat-theme-upload-form');
    var themeUploader = document.getElementById('xnat-theme-upload-input');
    var themeUploadSubmit = document.getElementById('xnat-theme-upload-submit');
    var selectedTheme = null;

    menuInit(themeSelector, null, '230px');

    function setupThemeUrl(path, parameters) {
        // Empty string and / both mean no path below root.
        var isRootPath = path === '' || path === '/';

        // If this is the case, they passed nothing in, including no parameters.
        // Pass back theme URL solo.
        if (!isRootPath && !path) {
            return themeUrl;
        }

        // If they passed in an empty path or the top-level path, then the URL is
        // the base theme URL. If they passed in a path, then append the path to
        // the base theme URL.
        var url = isRootPath ? themeUrl : themeUrl + '/' + encodeURI(path);

        // If they passed in parameters, then convert those to a querystring and
        // append that to the calculated URL.
        return parameters ? url + '?' + $.param(parameters) : url;
    }

    function isDefaultTheme(name){
        var _theme = name || themeName;
        return /^(xnat-default|xnat-sample|none)$/i.test(_theme);
    }

    function addThemeOptions(themeList, selected){
        var options = '';
        var hasNone = false;
        themeSelector.empty();
        if (Array.isArray(themeList)) {
            forEach(themeList, function(opt){
                // skip bogus options
                if (/^(\.|__MACOSX)/.test(opt.label)) return;
                if (opt.value === 'none' && hasNone) return;
                if (/^None$/i.test(opt.label)) {
                    // add 'none' value to 'None' option
                    opt.value = 'none';
                    hasNone = true;
                }
                options += '<option';
                options += ' value="' + opt.value + '"';
                if ((selected+'').toLowerCase() === (opt.value+'').toLowerCase()) {
                    options += ' selected';
                }
                options += '>';
                options += opt.label;
                options += '</option>';
            });
        }
        // add all <option> elements at once
        themeSelector.append(options);
        menuUpdate(themeSelector);
    }

    function getAvailableThemes(selected){
        return XNAT.xhr.getJSON(setupThemeUrl(), function(data){
            // themeSelector.empty();
            addThemeOptions(data, selected);
        });
    }
    themeMgr.getAvailableThemes = getAvailableThemes;

    // returns object for active theme
    function getActiveTheme(callback){
        var role = 'current';
        return XNAT.xhr.getText(setupThemeUrl(role), function(name){
            // themeSelector.empty();
            selectedTheme = name ? name : 'None';
            currentTheme.text(selectedTheme);
            if (typeof callback === 'function') {
                callback(name);
            }
        });
    }
    themeMgr.getActiveTheme = getActiveTheme;

    function populateThemesMenu(){
        getActiveTheme().done(function(name){
            getAvailableThemes(name)
        });
    }

    // function selectTheme(themeToSelect){
    //     if (themeToSelect && typeof themeToSelect === 'string') {
    //         themeSelector.val(themeToSelect);
    //     }
    // }

    function setTheme(name, callback, successText){
        var URL = setupThemeUrl('theme/' + name, csrf);
        // don't try to set 'None' or an empty theme name
        if (!name) {
            return false;
        }
        if (/^none$/i.test(name)){
            XNAT.xhr.put(URL).done(function(){
                XNAT.ui.banner.top(2000, 'Themes are now disabled.');
                populateThemesMenu();
            });
            return true;
        }
        callback = callback || diddly;
        XNAT.ui.dialog.confirm({
            width: 450,
            title: false,
            content: '' +
            (successText ? successText + '<br><br>' : '') +
            'Would you like to set the active theme to <b>"' + name + '"</b>?' +
            '<br><br>' +
            'Theme appearances should take effect immediately, but in some cases ' +
            'the change may not fully take effect until users log out, ' +
            'clear their browser cache and log back in.' +
            '',
            cancelLabel: 'Not Now',
            okLabel: 'Set Theme',
            okClose: false,
            okAction: function(dlg){
                XNAT.xhr.put(URL).done(function(){
                    dlg.close(true);
                    XNAT.ui.banner.top(2000, 'Theme set to "' + name + '".', 'success');
                })
            },
            onClose: function(){
                callback.call(this)
            }
        })
    }

    // this should override default Spawner form submission
    var themeSelectionForm = themeSelector.closest('form');
    themeSelectionForm.off('submit').on('submit', function(ev){
        ev.preventDefault();
        ev.stopImmediatePropagation();
        setTheme(themeSelector.val(), populateThemesMenu);
    });

    function removeTheme(e){
        e.preventDefault();
        var _themeName = themeSelector.val();
        // don't allow deletion of the default theme
        if (isDefaultTheme(_themeName)) {
            XNAT.ui.dialog.message(false, 'The <b>' + _themeName + '</b> theme may not be deleted.');
            return false;
        }
        XNAT.ui.dialog.confirm({
            width: 450,
            title: false,
            content: '' +
                'Are you sure you wish to permanently delete the ' +
                '<b>' + _themeName + '</b> theme? ' +
                'This cannot be undone. If the theme is currently active, it will be disabled immediately.',
            okLabel: 'Delete Theme',
            okClose: false,
            okAction: function(dlg){
                XNAT.xhr.delete(setupThemeUrl('theme/' + _themeName, csrf), function(data){
                    console.log(data);
                    // setTheme('none', populateThemesMenu);
                }).always(function(){
                    populateThemesMenu();
                    dlg.close(true);
                });
            },
            cancelLabel: 'Cancel'
        });
    }

    $('#remove-theme').on('click', removeTheme);

    /*** Theme Package Upload Functions ***/
    themeUploadForm.action = setupThemeUrl();
    // $(themeUploadForm).parent().parent().css('position', 'relative');
    // $(themeUploadForm).parent().parent().css('top', '-30px');
    themeUploadForm.onsubmit = function(event){
        event.preventDefault();
        $(themeUploadSubmit).text('Uploading...');
        $(themeUploadSubmit).attr('disabled', 'disabled');
        var files = themeUploader.files;
        var formData = new FormData();
        var uploaded = false;
        for (var i = 0; i < files.length; i++) {
            var file = files[i];
            if (isDefaultTheme(file.name)) {
                XNAT.ui.dialog.message(false, 'The <b>' + file.name + '</b> theme may not be overwritten.');
                return false;
            }
            if (!file.type.match('zip.*')) {
                continue;
            }
            formData.append('themePackage', file, file.name); // formData.append('themes[]', file, file.name);
            var XHR = new XMLHttpRequest();
            XHR.open('POST', themeUploadForm.action, true);
            XHR.onload = function(){
                if (XHR.status !== 200) {
                    console.log(XHR.statusText);
                    console.log(XHR.responseText);
                    XNAT.ui.dialog.message(false, 'Upload Error', 'There was a problem uploading your theme package.<br>Server responded with: ' + xhr.statusText);
                }
                $(themeUploadSubmit).text('Upload');
                $(themeUploadSubmit).removeAttr('disabled');
                var newThemeOptions = $.parseJSON(XHR.responseText);
                var selected;
                if (newThemeOptions[0]) {
                    selected = newThemeOptions[0].value;
                }
                // selected = null; // don't change the menu?
                addThemeOptions(newThemeOptions, selected);
                // prompt to set the newly uploaded theme
                setTheme(selected, null, 'Theme uploaded successfully.');
            };
            XHR.send(formData);
            uploaded = true;
        }
        if (!uploaded) {
            XNAT.ui.dialog.message(false, 'Nothing Uploaded', 'No valid theme package files were selected for upload.<br><br>Click the "Choose Files" button below to browse for a theme package.');
            $(themeUploadSubmit).text('Upload');
            $(themeUploadSubmit).removeAttr('disabled');
        }
        else {
            //XNAT.ui.banner.top(2000, 'Theme uploaded.', 'success');
        }
        return false;
    };

    // $('body').on('change', '#theme-selection', function(){
    //     var THEME = this.value;
    //     var URL = XNAT.url.csrfUrl(setupThemeUrl(THEME));
    //     XNAT.ui.dialog.confirm({
    //         content: 'Would you like to change the active theme to "' + THEME + '"?',
    //         okLabel: 'Set Theme',
    //         okAction: function(){
    //             XNAT.xhr.put(URL).done(function(){
    //                 XNAT.ui.banner.top(2000, 'Theme set to "' + THEME + '".', 'success');
    //             })
    //         }
    //     })
    // });

    $(populateThemesMenu);  // ...called once DOM is fully loaded "ready"

})(window.jQuery, window.console);

