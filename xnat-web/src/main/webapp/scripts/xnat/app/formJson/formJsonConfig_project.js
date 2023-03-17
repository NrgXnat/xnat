/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2022, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * @author: Mohana Ramaratnam (mohana@radiologics.com)
 * @since: 07-03-2021
 */
console.log('IN formJsonConfig_project.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin =
    getObject(XNAT.plugin || {});

(function(factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        return factory();
    }
}(function() {

    var projectFormManager, undefined, undef,
        csrfUrl = XNAT.url.csrfUrl;

    XNAT.app =
        getObject(XNAT.app || {});

    XNAT.customFormManager.protocolManager = getObject(XNAT.customFormManager.protocolManager || {});
    XNAT.customFormManager.datatypeManager = getObject(XNAT.customFormManager.datatypeManager || {});

    XNAT.customFormManager.projectOwner =
        getObject(XNAT.customFormManager.projectOwner || {});

    XNAT.customFormManager.projectOwner.projectFormManager = projectFormManager =
        getObject(XNAT.customFormManager.projectOwner.projectFormManager || {});

    projectFormManager.projectdefinitions = [];
    var addNewBuilderObj = {};


    var projectDataTypeSingularName = XNAT.app.displayNames.singular.project;


    projectFormManager.siteHasProtocolsPluginDeployed = false;
    projectFormManager.isProjectOwnerFormCreationEnabled = false;
    projectFormManager.isCustomVariableMigrationEnabled = false;

    const PRIMARY_KEY_FIELDNAME = "idCustomVariableFormAppliesTo";

    var onAddNewBuilderChange = function (build) {
        try {
            let modalId = 'addNewModal';
            let button_id = modalId + '-save-button';
            let saveBtn = document.getElementById(button_id);
            saveBtn.removeAttribute('disabled');
            saveBtn.classList.remove("disabled");
        }catch(err) {console.log(err);}
        addNewBuilderObj.builderSchema = build;
    };

    function setupBuilder(builder) {
        builder.on('change',onAddNewBuilderChange);
        Formio.Builders.addBuilder("addNew",builder);
        let formComponentDivs = document.getElementsByClassName('formcomponents');
        let formAreaDivs = document.getElementsByClassName('formarea');
        try {
            let formComponentDiv = formComponentDivs[0];
            let formAreaDiv = formAreaDivs[0];
            formComponentDiv.setAttribute('style','height:65vh; overflow-y:scroll');
            formAreaDiv.setAttribute('style','height:60vh; overflow-y:scroll');
        }catch(err){console.log("Could not add scroll bar");}

    }

    function spacer(width) {
        return spawn('i.spacer', {
            style: {
                display: 'inline-block',
                width: width + 'px'
            }
        })
    }

    function errorHandler(e, title, closeAll) {
        console.log(e);
        title = (title) ? 'Error Found: ' + title : 'Error';
        closeAll = (closeAll === undefined) ? true : closeAll;
        let errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + ': ' + e.statusText + '</strong></p><p>' + e.responseText + '</p>' : e;
        XNAT.dialog.open({
            width: 450,
            title: title,
            content: errormsg,
            buttons: [{
                label: 'OK',
                isDefault: true,
                close: true,
                action: function() {
                    if (closeAll) {
                        xmodal.closeAll();
                    }
                }
            }]
        });
    }

    function customFormUrl(append, paramsForMe) {
        var params = paramsForMe || {};
        return XNAT.url.restUrl('xapi/customforms/' + append, params, false, true);
    }

    function getProjectId() {
        if (XNAT.data.context.projectID.length > 0) return XNAT.data.context.projectID;
    }

    projectFormManager.getBuilderConfiguration = function () {
        return XNAT.customFormManager.builderConfigManager.getBuilderConfig();
    }

    function initBuilder(form) {
        let formType = "form";
        let formTitle = form.data.formTitle;
        let formBuilderElement = document.getElementById("form-builder");
        let builderConfig = projectFormManager.getBuilderConfiguration();
        if (jQuery.isEmptyObject(addNewBuilderObj) || !addNewBuilderObj.hasOwnProperty('builderSchema')) {
            Formio.builder(formBuilderElement, {
                display: formType,
                title: formTitle,
                components: [],
                settings: {}
            }, {
                noDefaultSubmitButton: true,
                builder: builderConfig
            }).then((builder) => {
                setupBuilder(builder);
            });
        }else {
            let builderSchema = addNewBuilderObj.builderSchema;
            Formio.builder(formBuilderElement, builderSchema, {
                noDefaultSubmitButton: true,
                builder: builderConfig
            }).then((builder) => {
                setupBuilder(builder);
            });
        }
    }


    // get the list of Site wide Configs
    projectFormManager.getCustomFormConfigs = projectFormManager.getAllCustomFormConfigs = function (callback) {
        callback = isFunction(callback) ? callback : function () {};
        let projId = getProjectId();
        return XNAT.xhr.get({
            url: restUrl('xapi/customforms',['projectId='+projId], false),
            dataType: 'json',
            async: false,
            success: function (data) {
                projectFormManager.projectdefinitions = [];
                data.forEach(function (item) {
                    projectFormManager.projectdefinitions.push(item);
                });
                projectFormManager.projectdefinitions.sort(function (a, b) {
                    return (a.path > b.path) ? 1 : -1;
                });
                callback.apply(this, arguments);
            },
            fail: function (e) {
                errorHandler(e, 'Could not retrieve forms');
            }

        });
    };

    projectFormManager.builderDialog = function (configDefinition) {
        let configDefinitionObj = JSON.parse(configDefinition['contents']);
        let builderConfig = projectFormManager.getBuilderConfiguration();
        Formio.builder(document.getElementById("formio-project-builder"), configDefinitionObj, {
            noDefaultSubmitButton: true,
            builder: builderConfig
        }).then((form) => {
            Formio.Builders.addBuilder("wysiwyg", form);
        });
    };

    projectFormManager.warnUserDataExists = function () {
        XNAT.dialog.open({
            width: 450,
            title: "Form deletion not possible",
            content: "Form has been disabled as data has been acquired using the form.",
            buttons: [{
                label: 'OK',
                isDefault: true,
                close: true,
                action: function () {
                    xmodal.closeAll();
                }
            }]
        });
    }

    projectFormManager.warnUserDataLossOnClosing = function () {
        XNAT.dialog.open({
            width: 450,
            title: "Are you sure you want to abandon form creation?",
            content: "You you sure you want to abandon form creation? All data will be lost.",
            buttons: [{
                label: 'OK',
                isDefault: true,
                close: true
            }]
        });
    }


    function filterDisplayOrderInput(textbox, originalFormOrder) {
      ["input", "keydown", "keyup", "select", "contextmenu", "drop"].forEach(function(event) {
        textbox.addEventListener(event, function() {
            let value = this.value;
            let test = /^-?\d*$/.test(this.value);
            if (/^-?\d*$/.test(this.value)) {
                this.oldValue = this.value;
            } else {
                if (this.hasOwnProperty("oldValue")) {
                    this.value = this.oldValue;
                } else {
                    this.value = originalFormOrder;
                }
            }
        });
      });
    }

    modifyDisplayOrder = function (configDefinition) {
        let formOrder = configDefinition['formDisplayOrder'];
        let itemObj = JSON.parse(configDefinition['contents']);
        const title = itemObj['title'] || '';
        const dateCreated = new Date(configDefinition['dateCreated']);
        const formId = configDefinition['formUUID'];
        var info_button = '<div class="info">Relative form order is a preference set via integer values, where lower numbers reflect higher positions. If multiple forms have the same value, creation date is used as a tie breaker.</div>';
        xmodal.open({
            title: 'Change Form Order for ' + title,
            content: info_button + '<br><br>Current Form Order: ' + formOrder + '<br><br> Creation Date: ' + dateCreated + '<br><br> New Form Order: <input id="formOrderTxt" value="'+ formOrder + '">',
            width: 500,
            height: 350,
            overflow: 'auto',
            afterShow: function () {
                filterDisplayOrderInput(document.getElementById("formOrderTxt"), formOrder)
            },
            buttons: {
                ok: {
                    label: 'Ok',
                    isDefault: true,
                    action: function () {
                        let desiredFormOrder = document.getElementById('formOrderTxt').value;
                        let desiredFormOrderInt = parseInt(desiredFormOrder);
                        if (desiredFormOrderInt > 1000000 || desiredFormOrderInt < -1000000) {
                            XNAT.dialog.open({
                                width: 450,
                                title: "Invalid Value for Form Order",
                                content: "Form Order cannot exceed 1000000.",
                                buttons: [{
                                    label: 'OK',
                                    isDefault: true,
                                    close: true
                                }]
                            });
                            return;
                        }
                        let url = customFormUrl( 'formId/' + formId + '?displayOrder=' + desiredFormOrder );
                        XNAT.xhr.post({
                            url: url,
                            success: function () {
                                xmodal.closeAll();
                                XNAT.ui.banner.top(2000, 'Form Order updated.', 'success');
                                projectFormManager.refreshTable();
                            },
                            fail: function (e) {
                                errorHandler(e, 'Could not update Form Order ' + title);
                            }
                        });
                    }
                },
                close: {
                    label: 'Close'
                }
            }
        });
    }


    function extractParts(path, partIndex) {
        let pathParts = path.split('/');
        if (partIndex > pathParts.length) {
            return '--';
        } else {
            return pathParts[partIndex];
        }
    }

    projectFormManager.addNewBtn = function (container, callback) {
        return spawn('button.btn1.btn-sm', {
            onclick: function (e) {
                e.preventDefault();
                xmodal.open({
                    id: 'addNewModal',
                    title: 'Custom Form Generation',
                    classes: 'xnat-bootstrap',
                    template: $('#addFormVariable'),
                    width: 1600,
                    height: 2400,
                    closeBtn: false,
                    scroll: false,
                    beforeShow: function (obj) {
                        displayFormWizard();
                    },
                    buttons: {
                        save: {
                            label: 'Save',
                            isDefault: true,
                            disabled: true,
                            action: function () {
                                saveConfiguration();
                            }
                        },
                        cancel: {
                            label: 'Cancel',
                            close: false,
                            action: function(obj) {
                                let $thisModal = obj.$modal;
                                xmodal.confirm({
                                    content: 'Are you sure you want to abandon?',
                                    okAction: function(){
                                        // close 'parent' modal
                                        xmodal.close($thisModal);
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }, 'Add New');
    }

    function displayFormWizard() {
        addNewBuilderObj = {};
        projectFormManager.getWizard();
    }


    // prepare to display the wizard for form creation
    projectFormManager.getWizard =  function (callback) {
        callback = isFunction(callback) ? callback : function () {};
        let url = XNAT.url.scriptUrl('/xnat/app/formJson/formManagerWizard_project.json');
        if (projectFormManager.siteHasProtocolsPluginDeployed == true) {
            url = XNAT.url.scriptUrl('/xnat/app/formJson/formManagerWizard_protocol_project.json');
        }
        let formWizardJson = undefined;
        XNAT.xhr.get({
            url: url,
            dataType: 'json',
            async: false,
            success: function (data) {
                formWizardJson = data;
            }
        });
        Formio.createForm(document.getElementById('formio'), formWizardJson, {
            breadcrumbSettings: {clickable:false},
            buttonSettings: {
                showCancel: false,
                showSubmit: false
            }
        }).then(function (wizard) {
            addNewBuilderObj.submission = wizard.submission;
            // Prevent the submission from going to the form.io server.
            wizard.nosubmit = true;
            wizard.on('nextPage', function (page) {
                if (projectFormManager.siteHasProtocolsPluginDeployed == true) {
                    if (page.page === 2) {
                        initBuilder(wizard);
                    }
                } else {
                    if (page.page === 1) {
                        initBuilder(wizard);
                    }
                }
            });
        });
    };

    function saveConfiguration() {
        let builder = Formio.Builders.getBuilder("addNew");
        let builderJson = builder.schema;
        let submission = addNewBuilderObj.submission;

        let submissionJson = {};
        let projectId = getProjectId();
        let xnatProject = {};
        xnatProject['label'] = projectId ;
        xnatProject['value'] = projectId;
        submission['data']['xnatProject'] = [];
        submission['data']['xnatProject'].push(xnatProject);
        submission['data']['isThisASiteWideConfiguration'] = "no";

        submissionJson['submission'] = submission;
        submissionJson['builder'] = builderJson;

        var url = restUrl('xapi/customforms/save', {}, false, true);

        XNAT.xhr.put({
            url: url,
            contentType: 'application/json',
            data: JSON.stringify(submissionJson),
            success: function () {
                xmodal.closeAll();
                XNAT.ui.banner.top(2000, 'Configuration saved.', 'success');
                projectFormManager.refreshTable();
            },
            fail: function (e) {
                errorHandler(e, 'Could Not save the form', false);
            }
        });
    }

    function saveWYSIWYGContent(itemObj) {
        let wysiygBuilder = Formio.Builders.getBuilder("wysiwyg");
        let editorContent = wysiygBuilder.schema;

        let submissionJson = getSubmissionObjectForRow(itemObj);
        submissionJson['builder'] = editorContent;

        let url = restUrl('xapi/customforms/save', {}, false, true);

        XNAT.xhr.put({
            url: url,
            contentType: 'application/json',
            data: JSON.stringify(submissionJson),
            success: function () {
                xmodal.closeAll();
                XNAT.ui.banner.top(2000, 'Form  updated.', 'success');
                projectFormManager.refreshTable();
            },
            fail: function (e) {
                errorHandler(e, 'Could Not save the form', false);
            }
        });
    }

    function getSubmissionObjectForRow(configItem) {
        let dbRowId = getPK(configItem);
        let path = configItem.path;
        let zIndex = configItem.formDisplayOrder;
        let submissionJson = {};
        let submissionObj = {};
        let submissionDataObj = {};
        if (dbRowId == undef) {
            submissionDataObj[PRIMARY_KEY_FIELDNAME] = '-1_-1';
        }else {
            submissionDataObj[PRIMARY_KEY_FIELDNAME] = dbRowId;
        }
        submissionDataObj['formType'] = "form";
        submissionDataObj['formTitle'] = configItem['contents']['title'];
        let dataXsiType = extractParts(path, 1);
        let dataSingular = XNAT.customFormManager.datatypeManager.getDatatypeByXsiType(dataXsiType).label;
        let xnatDataTypeObj = {};
        xnatDataTypeObj['label'] = dataSingular;
        xnatDataTypeObj['value'] = dataXsiType;
        submissionDataObj['xnatDatatype'] = xnatDataTypeObj;
        submissionDataObj['xnatProject'] = [];
        submissionDataObj['xnatProtocol'] = [];
        submissionDataObj['xnatVisit'] = [];
        submissionDataObj['xnatSubtype'] = [];

        let project = getProjectId();
        submissionDataObj.zIndex = zIndex;
        let rowProtocol = extractParts(configItem['path'], 3);
        submissionDataObj['isThisASiteWideConfiguration'] = 'no';
        var xnatProject = {};
        if (rowProtocol === '--') {
            xnatProject['label'] = project;
            xnatProject['value'] = project;
        } else {
            xnatProject['label'] = project + "[" + rowProtocol + "]";
            xnatProject['value'] = rowProtocol + ":" + project;
        }
        submissionDataObj['xnatProject'].push(xnatProject);
        if (rowProtocol != '--') {
            let xnatProtocol = {};
            xnatProtocol['label'] = rowProtocol;
            xnatProtocol['value'] = rowProtocol;
            submissionDataObj['xnatProtocol'].push(xnatProtocol);
            let visit = extractParts(configItem['path'], 5);
            if (visit != '--') {
                let xnatVisit = {};
                xnatVisit['label'] = rowProtocol + ":" + visit;
                xnatVisit['value'] = rowProtocol + ":" + visit;
                submissionDataObj['xnatVisit'].push(xnatVisit);
                let subType = extractParts(configItem['path'], 7);
                if (subType != '--') {
                    let xnatSubType = {};
                    xnatSubType['label'] = rowProtocol + ":" + visit + ":" + subType;
                    xnatSubType['value'] = rowProtocol + ":" + visit + ":" + subType;
                    submissionDataObj['xnatSubtype'].push(xnatSubType);
                }
            }
        }
        submissionObj['data'] = submissionDataObj;
        submissionObj['state'] = 'submitted';
        submissionJson['submission'] = submissionObj;
        return submissionJson;
    }

    function waitForElementInDOM(selector) {
        return new Promise(resolve => {
            if (document.querySelector(selector)) {
                return resolve(document.querySelector(selector));
            }

            const observer = new MutationObserver(mutations => {
                if (document.querySelector(selector)) {
                    resolve(document.querySelector(selector));
                    observer.disconnect();
                }
            });

            observer.observe(document.body, {
                childList: true,
                subtree: true
            });
        });
    }

    function editButton(itemObj) {
        return spawn('button.btn.btn-sm.edit', {
            onclick: function (e) {
                e.preventDefault();
                xmodal.open({
                    title: 'Edit Custom Form',
                    classes: 'xnat-bootstrap',
                    template: $('#editForm'),
                    width: 1600,
                    height: 1600,
                    scroll: false,
                    closeBtn: false,
                    beforeShow: function (obj) {
                        var has_data = false;
                        XNAT.xhr.get({
                            url: customFormUrl( 'hasdata/' + itemObj['appliesToList'][0]['idCustomVariableFormAppliesTo']),
                            dataType: 'json',
                            async: false,
                            success: function (data) {
                                has_data = data;
                            }
                        });
                        if (has_data) {
                            let $thisModal = obj.$modal;
                            xmodal.confirm({
                                content: 'This form already has data associated with it. Use caution when editing this form as it may affect the functioning of the previously collected data.',
                                okAction: function(){
                                },
                                cancelAction: function() {
                                   // close 'parent' modal
                                   xmodal.close($thisModal);
                                }
                            });
                        }
                        projectFormManager.builderDialog(itemObj);
                    },
                    afterShow: function(o) {
                        waitForElementInDOM('.formcomponents').then((formComponentDiv) => {
                            formComponentDiv.setAttribute('style','height:75vh; overflow-y:scroll');
                        });

                        waitForElementInDOM('.formarea').then((formAreaDiv) => {
                            formAreaDiv.setAttribute('style','height:80vh; overflow-y:scroll');
                        });

                    },
                    buttons: {
                        update: {
                            label: 'Save',
                            isDefault: true,
                            action: function () {
                                saveWYSIWYGContent(itemObj);
                            }
                        },
                        cancel: {
                            label: 'Cancel',
                            close: false,
                            action: function(obj) {
                                let $thisModal = obj.$modal;
                                xmodal.confirm({
                                    content: 'Are you sure you want to abandon?',
                                    okAction: function(){
                                        // close 'parent' modal
                                        xmodal.close($thisModal);
                                    }
                                });
                            }
                        }
                    }
                });
            },
            title: "Edit the form definition"
        }, [ spawn('i.fa.fa-pencil') ]);
    }

    function deleteButton(itemObj, isDataPresent) {
        return spawn('button.btn.btn-sm.edit', {
            onclick: function(e) {
                e.preventDefault();
                if (itemObj) {
                   projectFormManager.deleteForm(itemObj);
                }
            },
            disabled: isDataPresent,
            title: "Delete the form"
        }, [ spawn('i.fa.fa-trash') ]);
    }

    projectFormManager.deleteForm = function (configDefinition, title) {
        let appliesTo = configDefinition['appliesToList'];
        xmodal.open({
            title: 'Delete form?',
            content: 'Are you sure you want to delete the form? <br><br><p>In case data has been acquired using the form, form will be disabled. This allows access to data in the future.</p>',
            width: 300,
            height: 300,
            overflow: 'auto',
            buttons: {
                ok: {
                    label: 'Proceed',
                    isDefault: true,
                    action: function () {
                        let url = customFormUrl('');
                        XNAT.xhr.delete({
                            url: url,
                            contentType: 'application/json',
                            data: JSON.stringify(appliesTo),
                            success: function (resp) {
                                xmodal.closeAll();
                                const regex = /\: disabled$/;
                                const found = resp.match(regex);
                                if (found != null) {
                                    projectFormManager.warnUserDataExists();
                                } else {
                                    XNAT.ui.banner.top(4000, resp, 'success');
                                }
                                projectFormManager.refreshTable();
                            },
                            fail: function (e) {
                                errorHandler(e, 'Could not delete form ' + title);
                            }
                        });
                    }
                },
                close: {
                    label: 'Cancel'
                }
            }
        });
   }

    function displayOrderButton(itemObj) {
        return spawn('button.btn.btn-sm.edit', {
            onclick: function (e) {
                e.preventDefault();
                if (itemObj) {
                    modifyDisplayOrder(itemObj);
                }
            },
            title: "Change the order of the form relative to others"
        }, [ spawn('i.fa.fa-exchange') ]);
    }


    function optOutButton(itemObj, title) {
        let selectedProjects = [];
        selectedProjects.push(getProjectId());
        return spawn('button.btn.btn-sm.edit', {
            onclick: function(e) {
                e.preventDefault();
                xmodal.open({
                    title: 'Opt Out?',
                    content: 'Are you sure you want to opt out of the site wide form?',
                    width: 200,
                    height: 200,
                    overflow: 'auto',
                    buttons: {
                        ok: {
                            label: 'Ok',
                            isDefault: true,
                            action: function () {
                                if (itemObj) {
                                    let rowId = getPK(itemObj);
                                    let title = itemObj.title || '';
                                    XNAT.xhr.post({
                                        url: customFormUrl('/optout/' + rowId),
                                        contentType: 'application/json',
                                        async: false,
                                        data: JSON.stringify(selectedProjects),
                                        success: function() {
                                            xmodal.closeAll();
                                            XNAT.ui.banner.top(2000, projectDataTypeSingularName + ' ' + getProjectId() + ' opted out of form ' + title, 'success');
                                            projectFormManager.refreshTable();
                                        },
                                        fail: function(e) {
                                            errorHandler(e, 'Could not assign form for ' + projectDataTypeSingularName, true);
                                        }
                                    });
                                }
                            }
                        },
                        close: {
                            label: 'Close'
                        }
                    }
                });
            }
        }, 'OptOut');
    }

    function optInButton(itemObj, title) {
        return spawn('button.btn.btn-sm.edit', {
            onclick: function(e) {
                e.preventDefault();
                if (itemObj) {
                    projectFormManager.optIn(itemObj);
                }
            }
        }, 'OptIn');
    }

    projectFormManager.optIn = function (configDefinition, title) {
        let selectedProjects = [];
        selectedProjects.push(getProjectId());
        let rowId = getPK(configDefinition);
        xmodal.open({
            title: 'Confirm opt in',
            width: 300,
            height: 400,
            content: 'Are you sure you want to opt in? ',
            overflow: 'auto',
            buttons: {
                ok: {
                    label: 'Ok',
                    isDefault: true,
                    action: function () {
                        let url = customFormUrl('/optin/' + rowId);
                        XNAT.xhr.post({
                            url: url,
                            contentType: 'application/json',
                            data: JSON.stringify(selectedProjects),
                            success: function () {
                                xmodal.closeAll();
                                XNAT.ui.banner.top(2000, 'Form opted in', 'success');
                                projectFormManager.refreshTable();
                            },
                            fail: function (e) {
                                errorHandler(e, 'Could not opt in ' + title);
                            }
                        });
                    }
                },
                close: {
                    label: 'Close'
                }
            }
        });

    }


    // table cell formatting
    function truncCell(val, truncClass) {
        let elClass = truncClass ? 'truncate ' + truncClass : 'truncate';
        return spawn('span', {
            className: elClass,
            title: val,
            html: val
        });
    }

    // create table for FormIO JSON Forms for a project
    projectFormManager.table = function(container, callback) {
        let tableData = [];
        let definitions = projectFormManager.projectdefinitions;
        let DATA_FIELDS = 'title, datatype, formCreator, status';
        for (let k = 0; k < definitions.length; k++) {
            let item = definitions[k];
            let itemObj = JSON.parse(item['contents']);
            let title = itemObj.title || '';
            let tableDataRow = {};
            tableDataRow['title'] = title;
            tableDataRow['datatype'] = XNAT.customFormManager.datatypeManager.getDatatypeByXsiType(extractParts(item['path'], 1)).label;
            if (projectFormManager.siteHasProtocolsPluginDeployed) {
                tableDataRow['protocol'] = extractParts(item['path'], 3);
                tableDataRow['visit'] = extractParts(item['path'], 5);
                tableDataRow['subtype'] = extractParts(item['path'], 7);
            }
            tableDataRow['formCreator'] = projectFormManager.prettyPrint(item['username']);
            tableDataRow['status'] = projectFormManager.prettyPrint(getStatus(item['appliesToList']));
            tableDataRow['actions'] = item;
            tableData.push(tableDataRow);
        }
        let columns = {
            title: {
                label: 'Title'
            },
            datatype: {
                label: 'Datatype',
                sortable: true
            },
            formCreator: {
                label: 'Form Creator',
                sortable: true
            }
        };
        if (projectFormManager.siteHasProtocolsPluginDeployed) {
            columns['protocol'] = {
                label: 'Protocol',
                sortable: true
            };
            columns['visit'] = {
                label: 'Visit',
                sortable: true
            };
            columns['subtype'] = {
                label: 'Subtype',
                sortable: true
            };
        }
        columns['status'] = {
            label: 'Status',
            sortable: true
        };
        columns['actions'] = {
            label: 'Actions'
        };


        let columnsInTable = {
            title: {
                label: 'Title',
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function (title) {
                    return truncCell.call(this, title, '');
                }
            },
            datatype: {
                label: 'Datatype',
                sortable: true,
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function (datatype) {
                    return truncCell.call(this, datatype, '');
                }
            }
        };
        columnsInTable['formCreator']= {
            label: 'Form Creator',
                td: {
                style: {
                    verticalAlign: 'middle'
                }
            },
            apply: function (formCreator) {
                return truncCell.call(this, formCreator, '');
            }
        };
        if (projectFormManager.siteHasProtocolsPluginDeployed) {
            columnsInTable['protocol'] = {
                label: 'Protocol',
                sortable: true,
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function (protocol) {
                    return truncCell.call(this, protocol, '');
                }
            };
            columnsInTable['visit'] = {
                label: 'Visit',
                sortable: true,
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function (visit) {
                    return truncCell.call(this, visit, '');
                }
            };
            columnsInTable['subtype'] = {
                label: 'Subtype',
                sortable: true,
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function (subtype) {
                    return truncCell.call(this, subtype, '');
                }
            };

        }
        columnsInTable['status']= {
            label: 'Status',
            td: {
                style: {
                    verticalAlign: 'middle'
                }
            },
            apply: function (status) {
                return truncCell.call(this, status, '');
            }
        };
        columnsInTable['actions']= {
            label: 'Actions',
            td: {
                style: {
                    verticalAlign: 'middle'
                }
            },
            apply: function (actions) {
                return projectFormManager.getActionButtons(actions);
            }
        };

        let projectFormsTable = XNAT.table.dataTable(tableData, {
            container: container,
            header: true,
            sortable: DATA_FIELDS,
            filter: DATA_FIELDS,
            height: 'auto',
            overflowY: 'scroll',
            table: {
                className: 'project-formjson xnat-table selectable scrollable-table',
                style: {
                    width: '100%',
                    marginTop: '15px',
                    marginBottom: '15px'
                }
            },
            columns: columnsInTable
        });


        projectFormManager.$table = $(projectFormsTable.table);

        return projectFormsTable.table;
    };

    projectFormManager.prettyPrint = function(statusStr) {
        return statusStr[0].toUpperCase() + statusStr.slice(1, statusStr.length);
    }

    function getPK(item) {
        console.log(item['appliesToList']);
        return item['appliesToList'][0][PRIMARY_KEY_FIELDNAME];
    }

    function getStatus(appliesToList) {
        if (appliesToList.length === 1) {
            return appliesToList[0]['status'];
        } else {
            return appliesToList[1]['status'];
        }
    }

    projectFormManager.getActionButtons = function(item) {
        let isProjectSpecific = !(item.scope === 'Site');
        let isFormSharedBetweenProjects = item['doProjectsShareForm'];
        let title = item['title'] || '';
        let status = getStatus(item['appliesToList']);
        let isDisabled = false;
        let optedOut = false;
        let actions = [];
        if (status === 'optedout') {
            actions = [optInButton(item, title)];
        }else {
            if (isProjectSpecific && !isFormSharedBetweenProjects) {
                actions = [editButton(item),spacer(4), displayOrderButton(item, title), spacer(4), deleteButton(item, item['hasData'])];
            } else {
                actions = [optOutButton(item, title)];
            }

        }
        return actions;
    }

    // Is Visit and Protocols Plugin Installed?
    projectFormManager.getDeploymentEnvironment = function(callback) {
        callback = isFunction(callback) ? callback : function() {};
        let projId = getProjectId();
        return XNAT.xhr.get({
            url: restUrl('xapi/customforms/env',['projectId='+projId], false),
            dataType: 'json',
            async: false,
            success: function(data) {
                projectFormManager.siteHasProtocolsPluginDeployed = data.siteHasProtocolsPluginDeployed;
                let features = data.features || {};
                projectFormManager.isProjectOwnerFormCreationEnabled = features.isProjectOwnerFormCreationEnabled;
                projectFormManager.isCustomVariableMigrationEnabled = features.isCustomVariableMigrationEnabled;
                callback.apply(this, arguments);
            }
        });
    };


    projectFormManager.init = function(container) {
        projectFormManager.getDeploymentEnvironment();
        XNAT.customFormManager.datatypeManager.init();

        if (projectFormManager.siteHasProtocolsPluginDeployed) {
            XNAT.customFormManager.protocolManager.init();
        }

        projectFormManager.getAllCustomFormConfigs();
        let $manager = $$(container || 'div#form-project-json-container');

        projectFormManager.$container = $manager;

        $manager.append(projectFormManager.table());
        if (projectFormManager.isProjectOwnerFormCreationEnabled) {
            $manager.append(projectFormManager.addNewBtn());
        }

        $('div.filter-submit').remove();

        return {
            element: $manager[0],
            spawned: $manager[0],
            get: function() {
                return $manager[0]
            }
        };


    };


    projectFormManager.refresh = projectFormManager.refreshTable = function() {
        projectFormManager.getAllCustomFormConfigs();
        if (typeof projectFormManager.$table != 'undefined') {
            projectFormManager.$table.remove();
        }
        let $manager = $('div#form-project-json-container');
        $manager.prepend(projectFormManager.table());
    };

    projectFormManager.init();

    //We need the following code snippet to force the xnat-bootstrap onto the
    //dynamically added div elements of the form builder
    var observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            for(let node of mutation.addedNodes) {
                if(!(node instanceof HTMLElement)) continue;
                // check the inserted element for being a code snippets
                if(node.matches('div')) {
                    node.classList.add("xnat-bootstrap");
                }
            }
        });
    });

    observer.observe(document.getElementById('page_body'), { childList: true });



    return XNAT.customFormManager.projectOwner.projectFormManager = projectFormManager;

}));