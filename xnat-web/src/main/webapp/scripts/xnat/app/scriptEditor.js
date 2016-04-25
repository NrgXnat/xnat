/**
 * functions for XNAT script editor
 * xnat-templates/screens/Scripts.vm
 */

var XNAT = getObject(XNAT||{});

(function(XNAT){

    var app, scriptEditor,
        xhr = XNAT.xhr;

    XNAT.app =
        app = getObject(XNAT.app||{});

    XNAT.app.scriptEditor =
        scriptEditor = getObject(XNAT.app.scriptEditor||{});

    scriptEditor.scriptIds = [];
    scriptEditor.languages = scriptEditor.runners = [];

    // sample of output from REST query
    // that retrieves script info
    var sampleScriptJSON = {
        "content": "// script content goes here",
        "enabled": true,
        "id": 1,
        //"asProperties" : {
        //    "script" : "// script content goes here",
        //    "languageVersion" : "2.3.6",
        //    "language" : "groovy",
        //    "description" : "First script",
        //    "scriptId" : "script1"
        //},
        "language": "groovy",
        "created": 1421265306548,
        "description": "First script",
        "scriptId": "script1",
        "disabled": 0,
        "timestamp": 1421265306548
    };

    // return script url with common parts pre-defined
    function scriptURL( scriptId, params ){
        return XNAT.url.restUrl('/data/automation/scripts/' + scriptId, params);
    }

    // return script url with common parts pre-defined
    function scriptVersionsURL( scriptId, params ){
        return XNAT.url.restUrl('/data/automation/scriptVersions/' + scriptId, params);
    }

    function langMenuOptions(langs){

        langs = langs || scriptEditor.languages;

        var langOptions = '<option value="!">Select a Language</option>';

        if (langs.length){
            forEach(langs, function(lang){
                langOptions +=
                    '<option value="' + lang + '">' + lang + '</option>';
            });
        }

        return langOptions;

    }

    scriptEditor.loadScripts = function(){

        var rowTemplate = $('#script-row-template').find('> tbody').html(),
            scriptsTable = $('#scripts-table'),
            noScripts = $('#no-scripts-installed');

        xhr.getJSON(XNAT.url.restUrl('/data/automation/scripts'), function(json){

            var scripts = json.ResultSet.Result,
                list = '';

            // reset id array
            scriptEditor.scriptIds = [];

            if (scripts.length){
                $.each(scripts, function(i, script){
                    scriptEditor.scriptIds.push(script['Script ID']);
                    list +=
                        rowTemplate.
                        replace(/__SCRIPT_ID__/g, script['Script ID']).
                        replace(/__SCRIPT_DESCRIPTION__/g, XNAT.utils.escapeXML(script['Description']));
                });
                scriptsTable.find('> tbody').html(list);
                scriptsTable.show();
                noScripts.hide();
            }
            else {
                scriptsTable.hide().find('> tbody').empty();
                noScripts.show();
            }

        });

    };

    function saveScript( scriptId, $dialog, editor_id ){

        var $scriptIdInput = $dialog.find('.script-id-input');

        if ( !scriptId ) {
            xmodal.message('Error: Empty Script ID', 'Please give your script an ID before saving.');
        }
        // check for valid script ID
        else if (!/^[a-zA-Z][a-zA-Z0-9_]*$/.test(scriptId)){
            xmodal.message({
                title: 'Error: Invalid Characters in Script ID',
                message: 'Script ID must start with a letter and can only consist of letters, numbers, and the underscore "_" character.',
                action: function(){
                    setTimeout(function(){
                        $scriptIdInput.focus().select();
                    },10);
                }
            });
        }
        // check to see if script already exists
        else if ( scriptEditor.scriptIds.indexOf($scriptIdInput.val()) > -1 ) {
            // TODO: (maybe) get list via REST instead of when the page loads
            xmodal.message({
                title: 'Error: Script ID Already Exists',
                message: 'A script with that ID already exists. Please enter a new ID and try again.',
                action: function(){
                    setTimeout(function(){
                        $scriptIdInput.focus().select();
                    },10);
                }
            });
        }
        else {
            editor_id =
                editor_id ||
                scriptEditor.editor_id ||
                $dialog.find('.editor-content').attr('id');

            var data = {
                scriptLabel: $dialog.find('.scriptLabel').val(),
                content: ace.edit(editor_id).getSession().getValue(),
                description: $dialog.find('.script-description').val(),
                scriptVersion: $dialog.find('.script-version').val(),
                language: $dialog.find('input.language').val() || ''
            };

            if (!data.content || /^\s+$/.test(data.content)){
                xmodal.message('No Content', 'Please add script content and try again.');
            }
            else {
                var csrfParam = {
                    XNAT_CSRF: csrfToken
                };
                xhr.put(scriptURL(scriptId, csrfParam), data, {
                    success: function(){
                        xmodal.message('Success', 'Your script was successfully saved.', {
                            action: function(){
                                scriptEditor.loadScripts();
                                xmodal.closeAll();
                            }
                        });
                        //xmodal.close($dialog);
                    },
                    error: function( request, status, error ){
                        xmodal.message('Error', 'An error occurred: [' + status + '] ' + error);
                    }
                });
            }
        }
    }

    var counter = 0;
    // open xmodal dialog for script editing
    function renderEditor( json ){
        //var fullJson = json || {};
        //json = {};
        //var largestVersion = -1;
        //var arrayLength = fullJson.length;
        //for (var i = 0; i < arrayLength; i++) {
        //    if(fullJson[i].scriptVersion>largestVersion){
        //        largestVersion = fullJson[i].scriptVersion;
        //        json = fullJson[i];
        //    }
        //}

        var scriptId = json.scriptId || '';
        var lang = json.language || 'groovy';
        var time = json.timestamp || '';

        var opts = {};
        opts.width = 880;
        opts.height = 695;
        //opts.top = 100;
        opts.scroll = false;
        opts.template = $('#script-editor-template');
        opts.title = 'XNAT Script Editor';
        opts.title += (lang) ? ' - ' + lang : '';
        opts.esc = false; // prevent closing on 'esc'
        opts.enter = false; // prevents modal closing on 'enter' keypress
        opts.footerContent = '<span style="color:#555;">';
        if (time){
            opts.footerContent +=
                'last modified: ' + (new Date(time)).toString();
        }
        opts.footerContent += '</span>';
        opts.buttons = {
            save: {
                label: 'Save and Close',
                action: function( obj ){
                    scriptId =
                        obj.$modal.find('.scriptId').val() ||
                        obj.$modal.find('.script-id-input').val();
                    saveScript(scriptId, obj.$modal)
                },
                isDefault: true,
                close: false
            },
            close: {
                label: 'Cancel'
            }
        };
        opts.beforeShow = function( obj ){

            var $dialog = obj.$modal;

            $dialog.find('.id').val(json.id || '');
            $dialog.find('.scriptId').val(scriptId);
            $dialog.find('.scriptLabel').val(json.scriptLabel || '');
            $dialog.find('.language').val(lang);
            $dialog.find('.timestamp').val(time);
            $dialog.find('.script-description').val(json.description || '');

            var currScriptVersion = 1;
            if(scriptId) {
                //xhr.getJSON(XNAT.url.restUrl('/data/automation/scriptVersions/' + scriptId), function (jsonVersions) {
                //    for(var key in jsonVersions){
                //        if(key!="contains") {
                //            var currScript = jsonVersions[key];
                //            currScriptVersion = currScript.scriptVersion;
                //            $('#script-version')
                //                .append($("<option></option>")
                //                    .attr("value", currScriptVersion)
                //                    .text(currScriptVersion));
                //        }
                //    }
                //    $('#script-version').val(currScriptVersion);
                //});
                xhr.getJSON(XNAT.url.restUrl('/data/automation/scriptVersions/' + scriptId), function (versionsList) {
                    var versCounter = 1;
                    for(var vers in versionsList){
                        if(vers!="contains") {
                            currScriptVersion = versionsList[vers];
                            $('#script-version')
                                .append($("<option></option>")
                                    .attr("value", currScriptVersion)
                                    .text(versCounter));
                            versCounter = versCounter+1;
                        }
                    }
                    $('#script-version').val(currScriptVersion);
                });
            }
            else{
                $('#script-version')
                    .append($("<option></option>")
                        .attr("value", "1")
                        .attr("selected","selected")
                        .text("1"));
            }
            //$("div.script-version select").val(currScriptVersion);
            $('#script-version').change(function(){
                var selectedVersion = $('#script-version')[0].value;
                xhr.getJSON(XNAT.url.restUrl('/data/automation/scripts/' + scriptId + '/'+ selectedVersion), function (versionObj) {
                    json = versionObj;
                    var $dialog = obj.$modal;

                    //$dialog.find('.id').val(json.id || '');
                    $dialog.find('.scriptId').val(scriptId || '');
                    $dialog.find('.scriptLabel').val(json.scriptLabel || '');
                    $dialog.find('.language').val(json.language || '');
                    $dialog.find('.timestamp').val(json.timestamp || '');
                    $dialog.find('.script-description').val(json.description || '');
                    $dialog.find('.script-version').val(selectedVersion || '');

                    var $wrapper = $dialog.find('.editor-wrapper');

                    // make sure the editor wrapper is empty
                    $wrapper.empty();

                    // create an entirely new editor div
                    var _editor = document.createElement('div');
                    _editor.id = 'script-' + (scriptId || (json.id||++counter)) + '-content';
                    _editor.className = 'editor-content';
                    _editor.innerHTML = XNAT.utils.escapeXML(json.content) || '';
                    _editor.style = 'position:absolute;top:0;right:0;bottom:0;left:0;border: 1px solid #ccc';

                    // put the new editor div in the wrapper
                    $wrapper.append(_editor);

                    // save the id to outer scope for other functions
                    scriptEditor.editor_id = _editor.id;

                    var aceEditor = ace.edit(_editor);
                    aceEditor.setTheme("ace/theme/eclipse");
                    aceEditor.getSession().setMode("ace/mode/" + stringLower(lang));
                });

            });

            if (scriptId){
                $dialog.find('.script-id-text').html(scriptId);
                $dialog.find('.script-id-input').remove();
                //$dialog.find('.script-id-input').val(scriptId);
            }

            var $wrapper = $dialog.find('.editor-wrapper');

            // make sure the editor wrapper is empty
            $wrapper.empty();

            // create an entirely new editor div
            var _editor = document.createElement('div');
            _editor.id = 'script-' + (scriptId || (json.id||++counter)) + '-content';
            _editor.className = 'editor-content';
            _editor.innerHTML = XNAT.utils.escapeXML(json.content) || '';
            _editor.style = 'position:absolute;top:0;right:0;bottom:0;left:0;border: 1px solid #ccc';

            // put the new editor div in the wrapper
            $wrapper.append(_editor);

            // save the id to outer scope for other functions
            scriptEditor.editor_id = _editor.id;

            var aceEditor = ace.edit(_editor);
            aceEditor.setTheme("ace/theme/eclipse");
            aceEditor.getSession().setMode("ace/mode/" + stringLower(lang));

        };
        opts.afterShow = function(obj){
            if (!scriptId){
                obj.$modal.find('.script-id-input').focus().select();
            }
        };
        xmodal.open(opts);
    }

    // open dialog to choose language
    function selectLanguage(){

        function langDialog(langs){

            xmodal.confirm({
                title: 'Select a Language',
                content: '<p>Please select a language:</p>' +
                '<select class="language">' + langMenuOptions(langs) + '</select>',
                //top: 120,
                beforeShow: function(obj){
                    //console.log(obj.content);
                },
                okLabel: 'Add Script',
                okClose: false,
                okAction: function(obj){
                    var $langSelect = obj.$modal.find('select.language'),
                        lang = $langSelect.val();
                    if (lang === '!'){
                        xmodal.message('Please select a language before proceeding.',{
                            //top: 140,
                            width: 350,
                            height: 150
                        });
                    }
                    else {
                        renderEditor({
                            language: lang
                        });
                        xmodal.close(obj.$modal);
                    }
                },
                cancelAction: function(obj){
                    xmodal.close(obj.$modal);
                }
            });
        }

        // can we assume the language choices will be
        // up-to-date when the dialog opens?
        if (scriptEditor.languages.length){
            langDialog(scriptEditor.languages);
        }
        // otherwise get the runners (again?)
        else {
            xhr.getJSON(XNAT.url.restUrl('/data/automation/runners'), function(json){
                langDialog(json);
            });
        }

    }

    scriptEditor.editScript = function( scriptId ){
        xhr.getJSON(scriptURL(scriptId), function( json ){
            renderEditor(json);
        });
    };

    scriptEditor.duplicateScript = function( scriptId ){
        xhr.getJSON(scriptURL(scriptId), function( json ){
            var data = {
                //scriptId: json.scriptId + '_copy',
                scriptLabel: json.scriptLabel,
                description: json.description,
                content: json.content,
                language: json.language,
                copyOf: json.scriptId
            };
            renderEditor(data);
        });
    };

    scriptEditor.addScript = function(){
        //window.location = url;
        var $langMenu = $('#add-script-language'),
            _lang = $langMenu.val();
        if (_lang === '!'){
            selectLanguage();
        }
        else {
            renderEditor({
                language: _lang
            });
        }
    };

    function doDelete( scriptId ){

        var successDialog = {
            title: 'Success',
            content: 'The script was successfully deleted.',
            action: function(){
                scriptEditor.loadScripts();
                XNAT.app.siteEventsManager.initHandlersTable();
                xmodal.closeAll();
            }
        };
        var csrfParam = {
            XNAT_CSRF: csrfToken
        };

        xhr.delete(scriptURL(scriptId, csrfParam), {
            success: function(){
                xmodal.message(successDialog);
            },
            error: function( request, status, error ){
                xmodal.message('Error', 'An error occurred: [' + status + '] ' + error);
            }
        });
    }

    scriptEditor.deleteScript = function( scriptId ){
        xmodal.confirm({
            width: 450,
            title: 'Delete Script?',
            content: 'Are you sure you want to delete the script <b>"' + scriptId + '"</b>?<br/><br/>Any events or triggers associated with this script will also be deleted.',
            okLabel: 'Delete',
            cancelLabel: 'Cancel',
            okAction: function(){
                doDelete(scriptId);
            },
            cancelAction: function(){
                //xmodal.message('Delete cancelled', 'The script was not deleted.');
            }
        });
    };

    // ???
    $(function(){

        $('body').addClass('administer configuration no_left_bar');

        scriptEditor.loadScripts();

        $('#scripts-table').on('click', '[data-action]', function(){
            var action = $(this).data('action');
            var script = $(this).closest('tr').data('script-id');
            scriptEditor[action](script);
        });

        $('#add-script-button').click(scriptEditor.addScript);

        xhr.getJSON(XNAT.url.restUrl('/data/automation/runners'), function(json){
            scriptEditor.languages = scriptEditor.runners = json;
            $('#add-script-language').html(langMenuOptions(json));
        });

    });

})(XNAT);
