/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2022, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * @author: Mohana Ramaratnam (mohanakannan9@gmail.com)
 * @since: 07-03-2021
 */
console.log('IN assignProject.js');

var XNAT = getObject(XNAT || {});

(function(factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        return factory();
    }
}(function() {

   XNAT.customFormManager =
        getObject(XNAT.customFormManager || {});


    XNAT.customFormManager.xnatFormManager = getObject(XNAT.customFormManager.xnatFormManager || {});


    XNAT.customFormManager.assignDialog = launcher =
        getObject(XNAT.customFormManager.launcher || {});

    var projectsList = [];
    var hasAccessToProjects = false;
    var projectDataTypeSingularName = XNAT.app.displayNames.singular.project;
    var projectDataTypePluralName = XNAT.app.displayNames.plural.project;

    function errorHandler(e) {
        console.log(e);
        xmodal.alert({
            title: 'Error',
            content: '<p><strong>Error ' + e.status + ': ' + e.statusText + '</strong></p>',
            okAction: function() {
                xmodal.closeAll();
            }
        });
    }

    function getJsonSubmissionObject(configDefinition) {
        var formIOContents = configDefinition['configData']['contents'];
        var submissionJson = {};
        var submission = {};
        submission.data = {};
        submission.data.formType = "form";
        submission.data.formTitle = formIOContents.title;
        submission.data.xnatDatatype = {
            label: '',
            value: ''
        };
        submission.data.isThisASiteWideConfiguration = 'no';
        submissionJson['submission'] = submission;
        submissionJson['builder'] = formIOContents;
    }

    function updateFormAssociations(urlToSubmit, selectedProjects) {
        XNAT.xhr.post({
            url: urlToSubmit,
            contentType: 'application/json',
            async: false,
            data: JSON.stringify(selectedProjects),
            success: function() {
                xmodal.closeAll();
                refreshMainTable = true;
                XNAT.ui.banner.top(2000, 'Form ' + projectDataTypeSingularName +  ' associations successfully updated. ', 'success');
            },
            fail: function(e) {
                errorHandler(e, 'Could not assign form for ' + projectDataTypePluralName , false);
            }
        });
    }

    function promoteFormUrl() {
        return XNAT.customFormManager.xnatFormManager.customFormUrl('promote');
    }

    function promoteButton(itemObj, title) {
        let projects = [];
        itemObj['appliesToList'].forEach(function (item) {
            projects.push(item['entityId']);
        });
        return spawn('button.btn.btn-sm.edit', {
            style: {
                padding: '6px 12px !important',
                background: '#f8f8f8 linear-gradient( #ffffff, #f0f0f0 )',
                border: '1px solid #a0a0a0'
            },
            onclick: function (e) {
                e.preventDefault();
                if (itemObj) {
                    promote(itemObj, title);
                }
            }
        }, 'Apply by default to all ' + projectDataTypePluralName);
    }

    promote = function (configDefinition, title) {
        let appliesTo = configDefinition['appliesToList'];
        xmodal.open({
            title: 'Confirm form promotion',
            content: 'Are you sure you want to promote form to site? <br> <br> <p> Promoting a form would make it available to the entire site.</p>',
            width: 300,
            height: 400,
            overflow: 'auto',
            buttons: {
                ok: {
                    label: 'Ok',
                    isDefault: true,
                    action: function () {
                        let url = promoteFormUrl();
                        XNAT.xhr.post({
                            url: url,
                            contentType: 'application/json',
                            data: JSON.stringify(appliesTo),
                            success: function () {
                                xmodal.closeAll();
                                XNAT.ui.banner.top(2000, 'Form promoted to site', 'success');
                                XNAT.customFormManager.xnatFormManager.refreshTable();
                            },
                            fail: function (e) {
                                errorHandler(e, 'Could not promote form ' + title);
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

    launcher.populateForm = function($form, siteWide, projectsAlreadyAssigned = []) {
        if (typeof launcher.$table != 'undefined') {
            projectsList = [];
            launcher.$table.remove();
        }

        let projectsUrl = XNAT.url.restUrl('xapi/role/projects',{},false,false);
        let columnIds = ["assign", "name", "id", "investigator"];
        let labelMap = {
            assign: {
                label: "Select",
                checkboxes: true,
                id: "Assign"
            },
            name: {
                label: projectDataTypeSingularName + " Name",
                checkboxes: false,
                id: projectDataTypeSingularName + " Name"
            },
            id: {
                label: projectDataTypeSingularName + " ID",
                checkboxes: false,
                id: projectDataTypeSingularName + " ID"
            },
            investigator: {
                label: "Primary Investigator",
                checkboxes: false,
                id: "Primary Investigator"
            }

        };
        hasAccessToProjects = false;
            XNAT.xhr.get({
                url: projectsUrl,
                async: false,
                dataType: 'json',
                success: function(data) {
                    hasAccessToProjects = true;
                    projectsList = data;
                },
                error: function(e) {
                    errorHandler(e);
                }
            });

        var projectsTable = XNAT.table({
            className: 'projects-table xnat-table data-table clean fixed-header selectable scrollable-table',
            style: {
                width: 'auto'
            }
        });
        var $dataRows = [];
        var dataRows = [];

        function cacheRows() {
            if ($dataRows.length === 0 || $dataRows.length !== dataRows.length) {
                $dataRows = dataRows.length ?
                    $(dataRows) :
                    launcher.container.find('.table-body').find('tr');
            }
            return $dataRows;
        }

        function filterRows(val, name) {
            if (!val) {
                return false
            }
            val = val.toLowerCase();
            var filterClass = 'filter-' + name;
            // cache the rows if not cached yet
            cacheRows();
            $dataRows.addClass(filterClass).filter(function() {
                return $(this).find('td.' + name).containsNC(val).length
            }).removeClass(filterClass);
            launcher.$table.find('.selectable-select-all').each(function() {
                setIndeterminate($(this), $(this).data('id'), $(this).prop('checked'));
            });
        }

        function selectProjectCheckbox(projectId, checked) {
            var ckbox = spawn('input', {
                type: 'checkbox',
                checked: checked,
                disabled: false,
                value: projectId,
                id: 'assign-' + projectId,
                classes: projectId
            });

            return spawn('div.center', [ckbox]);
        }


        projectsTable.thead().tr();
        $.each(columnIds, function(i, c) {
            projectsTable.th('<b>' + labelMap[c].label + '</b>');
        });
        // add check-all header row
        projectsTable.tr({
            classes: 'filter'
        });

        const projectSelectCheckBox = spawn('input.project-select', {
            type: 'checkbox',
            name: 'project-select',
            checked: false,
            onclick: function () {
                if ($(this).is(':checked')) {
                    markChecked(true);
                } else {
                    markChecked(false);
                }
            }
        });

        function markChecked(checkedStatus) {
            $.each(projectsList, function(i, project) {
                let checkBoxElt = document.getElementById('assign-' + project.id);
                checkBoxElt.checked = checkedStatus;
            });
        }

        $.each(columnIds, function(i, c) {
            if (labelMap[c].checkboxes) {
                projectsTable.td("", spawn('div.center', [projectSelectCheckBox]));
            } else {
                document.head.appendChild(spawn('style|type=text/css', 'tr.filter-' + c + '{display:none;}'));
                var $filterInput = $.spawn('input.filter-data', {
                    type: 'text',
                    title: c + ':filter',
                    placeholder: 'Filter by ' + c,
                    style: 'width: 90%;'
                });
                $filterInput.on('focus', function() {
                    $(this).select();
                    cacheRows();
                });
                $filterInput.on('keyup', function(e) {
                    var val = this.value;
                    var key = e.which;
                    // don't do anything on 'tab' keyup
                    if (key == 9) return false;
                    if (key == 27) { // key 27 = 'esc'
                        this.value = val = '';
                    }
                    if (!val || key == 8) {
                        $dataRows.removeClass('filter-' + c);
                    }
                    if (!val) {
                        // no value, no filter
                        return false;
                    }
                    filterRows(val, c);
                });
                projectsTable.td({
                    classes: 'filter'
                }, $filterInput[0]);
            }
        });
        projectsTable.tbody({
            classes: 'table-body'
        });

        $.each(projectsList, function(i, e) {
            projectsTable.tr();
            projectsTable.td([selectProjectCheckbox(e.id, siteWide != projectsAlreadyAssigned.includes(e.id))]);
            projectsTable.td({
                classes: columnIds[1]
            }, e.name);
            projectsTable.td({
                classes: columnIds[2]
            }, e.id);
            if (e.investigator){
                projectsTable.td({
                    classes: columnIds[3]
                }, e.investigator);
            }
        });
        $form.empty().prepend(projectsTable.table);
        launcher.container = $form;
        launcher.$table = $(projectsTable.table);

    }

    launcher.assignProject = function(configDefinition, title,  rowId, siteWide,  projectsAlreadyAssigned) {
        let myArgumentCount = arguments.length;
        let projectSelectorContent = spawn('div.panel', [
            spawn('p', 'Please select ' + projectDataTypePluralName),
            spawn('div.standard-settings'),
            spawn('div.advanced-settings-container.hidden', [
                spawn('div.advanced-settings-toggle'),
                spawn('div.advanced-settings')
            ])
        ]);
        let selectedProjects = [];

        var modal = {
            title: 'Select ' + projectDataTypePluralName + ' for ' + title,
            content: projectSelectorContent,
            width: 550,
            scroll: true,
            beforeShow: function(obj) {
                xmodal.loading.open({
                    title: 'Fetching available ' + projectDataTypePluralName
                });
                let $panel = obj.$modal.find('.panel');
                let $standardInputContainer = $panel.find('.standard-settings');
                let $advancedInputContainer = $panel.find('.advanced-settings');
                launcher.populateForm($panel, siteWide, projectsAlreadyAssigned);
            },
            afterShow: function(obj) {
                xmodal.loading.close();
                if (hasAccessToProjects  && projectsList.length === 0) {
                    obj.close();
                    xmodal.alert({
                        content: '<p><strong>There are no ' + projectDataTypePluralName + ' left to include.</strong></p>',
                        okAction: function() {
                            xmodal.closeAll();
                        }
                    });
                }else if (!hasAccessToProjects) {
                    obj.close();
                    xmodal.alert({
                        content: '<p><strong>You do not have access to any ' + projectDataTypePluralName + '.</strong></p>',
                        okAction: function() {
                            xmodal.closeAll();
                        }
                    });
                }
            },
            buttons: [{
                label: 'Save',
                isDefault: false,
                close: true,
                action: function(obj) {
                    let $panel = obj.$modal.find('.panel'),
                        targetData = {};
                    let refreshMainTable = false;
                    var additions, subtractions;
                    var oneChecked = false;
                    if (siteWide) {
                        $.each(projectsList, function(i, project) {
                            var checkBoxElt = document.getElementById('assign-' + project.id);
                            if (!checkBoxElt.checked) {
                                selectedProjects.push(project.id);
                            } else {
                                oneChecked = true;
                            }
                        });
                        subtractions = selectedProjects.filter(x => !projectsAlreadyAssigned.includes(x));
                        additions = projectsAlreadyAssigned.filter(x => !selectedProjects.includes(x));
                    } else {
                        $.each(projectsList, function(i, project) {
                            var checkBoxElt = document.getElementById('assign-' + project.id);
                            if (checkBoxElt.checked) {
                                selectedProjects.push(project.id);
                                oneChecked = true;
                            }
                        });
                        additions = selectedProjects.filter(x => !projectsAlreadyAssigned.includes(x));
                        subtractions = projectsAlreadyAssigned.filter(x => !selectedProjects.includes(x));
                    }

                    if (additions.length === 0 && subtractions.length === 0) {
                        xmodal.alert({
                            title: 'Select ' + projectDataTypeSingularName,
                            content: '<p><strong>Please select at least one ' + projectDataTypeSingularName + ' to add or remove</strong></p>',
                            okAction: function() {
                                xmodal.closeAll();
                            }
                        });
                    } else if(oneChecked === false) {
                        xmodal.alert({
                            title: 'No ' + projectDataTypePluralName + ' associated',
                            content: '<p><strong>Performing this action would mean no ' + projectDataTypePluralName + ' would be associated with this form. Try disabling the form to make the form inactive for all ' +projectDataTypePluralName + ' instead.</strong></p>',
                            okAction: function() {
                                xmodal.closeAll();
                            }
                        });
                    }else {
                        //Save the current rows contents into the new path for the selected project
                        refreshMainTable = true;
                        if (additions.length > 0) {
                            updateFormAssociations(XNAT.customFormManager.xnatFormManager.customFormUrl('/optin/' + rowId), additions);
                        }
                        if (subtractions.length > 0) {
                            updateFormAssociations(XNAT.customFormManager.xnatFormManager.customFormUrl('/optout/' + rowId), subtractions);
                        }

                        if (refreshMainTable) {
                            XNAT.customFormManager.xnatFormManager.refreshTable();
                        }
                    }

                }
            },
            {
                label: 'Cancel',
                isDefault: false,
                close: true
            }
            ]
        };

        if (!siteWide) {
            modal.footerContent = promoteButton(configDefinition, title);
        }

        XNAT.ui.dialog.open(modal);
    }
}));