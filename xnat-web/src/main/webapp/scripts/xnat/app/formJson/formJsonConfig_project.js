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

    var projectDataTypeSingularName = XNAT.app.displayNames.singular.project;


    projectFormManager.siteHasProtocolsPluginDeployed = false;
    projectFormManager.isProjectOwnerFormCreationEnabled = false;
    projectFormManager.isCustomVariableMigrationEnabled = false;

    const PRIMARY_KEY_FIELDNAME = "idCustomVariableFormAppliesTo";

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

    // get the list of Site wide Configs
    projectFormManager.getCustomFormConfigs = projectFormManager.getAllCustomFormConfigs = function (callback) {
        callback = isFunction(callback) ? callback : function () {};
        let projId = getProjectId();
        return XNAT.xhr.get({
            url: restUrl('xapi/customforms',['projectId='+projId], false),
            dataType: 'json',
            async: false,
            success: function (data) {
                closeModalPanel("cfConfigModal");
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
                closeModalPanel("cfConfigModal");
                errorHandler(e, 'Could not retrieve forms');
            }

        });
    };

    function extractParts(path, partIndex) {
        let pathParts = path.split('/');
        if (partIndex > pathParts.length) {
            return '--';
        } else {
            return pathParts[partIndex];
        }
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
                    return spawn('!',[
                        spawn('strong',[
                            truncCell.call(this, title, '')
                        ]),
                        '<br>',
                        spawn('small',{style: {color: "#a0a0a0" }},[
                            truncCell.call(this, this.actions.formUUID, '')
                        ])
                    ])
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
                    verticalAlign: 'middle',
                    width: '130px'
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
            if (!isProjectSpecific || isFormSharedBetweenProjects) {
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