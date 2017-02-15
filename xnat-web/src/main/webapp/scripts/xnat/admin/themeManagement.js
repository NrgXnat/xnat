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
    
    var themeUrl = XNAT.url.rootUrl('/xapi/theme');
    var s = '/', q = '?', a = '&';
    var csrf = 'XNAT_CSRF=' + window.csrfToken;
    // ??
    // $('#titleAppName').text(XNAT.app.siteId);
    var currentTheme = $('#current-theme');
    var themeSelector = $('#theme-selection');
    var themeUploadForm = document.getElementById('xnat-theme-upload-form');
    var themeUploader = document.getElementById('xnat-theme-upload-input');
    var themeUploadSubmit = document.getElementById('xnat-theme-upload-submit');
    var selectedTheme = null;

    menuInit(themeSelector, null, '230px');
    
    function populateThemes(){
        getActiveTheme(getAvailableThemes);
    }

    // returns object for active theme
    function getActiveTheme(callback){
        var role = 'global';
        return XNAT.xhr.get(themeUrl + s + role, function(data){
            themeSelector.empty();
            selectedTheme = data.name ? data.name : 'None';
            currentTheme.text(selectedTheme);
            if (typeof callback === 'function') {
                callback(data.name);
            }
        });
    }
    themeMgr.getActiveTheme = getActiveTheme;
    
    function getAvailableThemes(selected){
        return XNAT.xhr.getJSON(themeUrl, function(data){
            themeSelector.empty();
            addThemeOptions(data, selected);
        });
    }
    themeMgr.getAvailableThemes = getAvailableThemes;

    function addThemeOptions(themeList, selected){
        var options = '';
        if (Array.isArray(themeList)) {
            forEach(themeList, function(opt){
                options += '<option';
                options += ' value="' + opt.value + '"';
                if (selected == opt.value) {
                    options += ' selected';
                }
                options += '>';
                options += opt.label;
                options += '</option>';
            });
        }
        // add all <option> elements at once
        themeSelector.html(options);
        menuUpdate(themeSelector);
    }
    
    // function selectTheme(themeToSelect){
    //     if (themeToSelect && typeof themeToSelect === 'string') {
    //         themeSelector.val(themeToSelect);
    //     }
    // }

    function setTheme(name, callback){
        var URL = XNAT.url.csrfUrl('/xapi/theme/' + encodeURI(name));
        callback = callback || diddly;
        xmodal.confirm({
            content: '' +
            'Would you like to change the active theme to "' + name + '"?' +
            '<br><br>' +
            'Theme theme appearances may not fully take effect until users log out, ' +
            'clear their browser cache and log back in.' +
            '',
            cancelLabel: 'Not Now',
            okLabel: 'Set Theme',
            okAction: function(){
                XNAT.xhr.put(URL).done(function(){
                    callback.call(this);
                    XNAT.ui.banner.top(2000, 'Theme set to "' + name + '".', 'success');
                })
            }
        })
    }

    // this should override default Spawner form submission
    var themeSelectionForm = themeSelector.closest('form');
    themeSelectionForm.off('submit').on('submit', function(ev){
        ev.preventDefault();
        ev.stopImmediatePropagation();
        setTheme(themeSelector.val(), populateThemes);
    });
    
    function removeTheme(e){
        e.preventDefault();
        xmodal.confirm({
            content: 'Are you sure you wish to delete the selected theme?',
            action: function(){
                XNAT.xhr.delete(themeUrl + s + encodeURI(themeSelector.val()) + q + csrf, function(data){
                    console.log(data);
                    populateThemes();
                });
            }
        });
    }
    
    $('#remove-theme').on('click', removeTheme);

    /*** Theme Package Upload Functions ***/
    themeUploadForm.action = themeUrl + q + csrf;
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
                    xmodal.message('Upload Error', 'There was a problem uploading your theme package.<br>Server responded with: ' + xhr.statusText);
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
                setTheme(selected);
            };
            XHR.send(formData);
            uploaded = true;
        }
        if (!uploaded) {
            xmodal.message('Nothing Uploaded', 'No valid theme package files were selected for upload.<br><br>Click the "Choose Files" button below to browse for a theme package.');
            $(themeUploadSubmit).text('Upload');
            $(themeUploadSubmit).removeAttr('disabled');
        }
        else {
            XNAT.ui.banner.top(2000, 'Theme uploaded.', 'success');
        }
        return false;
    };

    // $('body').on('change', '#theme-selection', function(){
    //     var THEME = this.value;
    //     var URL = XNAT.url.csrfUrl('/xapi/theme/' + THEME);
    //     xmodal.confirm({
    //         content: 'Would you like to change the active theme to "' + THEME + '"?',
    //         okLabel: 'Set Theme',
    //         okAction: function(){
    //             XNAT.xhr.put(URL).done(function(){
    //                 XNAT.ui.banner.top(2000, 'Theme set to "' + THEME + '".', 'success');
    //             })
    //         }
    //     })
    // });

    $(populateThemes);  // ...called once DOM is fully loaded "ready"
    
})(window.jQuery, window.console);

