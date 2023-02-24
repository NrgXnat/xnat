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
console.log('IN MigrateLegacyCustomVariable.vm');

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

    var xnatCustomVariableMigratorManager, undefined, undef,
        restUrl = XNAT.url.restUrl;

    XNAT.customVariableMigrator =
        getObject(XNAT.customVariableMigrator || {});

    XNAT.customFormManager =
        getObject(XNAT.customFormManager || {});


    XNAT.customFormManager.projectListModalManager = getObject(XNAT.customFormManager.projectListModalManager || {});
    XNAT.customFormManager.datatypeManager = getObject(XNAT.customFormManager.datatypeManager || {});

    XNAT.customVariableMigrator.xnatCustomVariableMigratorManager = xnatCustomVariableMigratorManager =
        getObject(XNAT.customVariableMigrator.xnatCustomVariableMigratorManager || {});

    xnatCustomVariableMigratorManager.customVariables = [];


    function errorHandler(e, title, closeAll) {
        console.log(e);
        title = (title) ? 'Error Found: ' + title : 'Error';
        closeAll = (closeAll === undefined) ? true : closeAll;
        var errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + ': ' + e.statusText + '</strong></p><p>' + e.responseText + '</p>' : e;
        XNAT.dialog.open({
            width: 450,
            title: title,
            content: errormsg,
            buttons: [{
                label: 'OK',
                isDefault: true,
                close: true,
                action: function () {
                    if (closeAll) {
                        xmodal.closeAll();
                    }
                }
            }]
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

    function truncCellWithDescription(val, description, truncClass) {
        let elClass = truncClass ? 'truncate ' + truncClass : 'truncate';
        let htmlVal = description ? val + '<br>' + description + '' : val;
        return spawn('span', {
            className: elClass,
            title: val,
            html: htmlVal
        });
    }

    function reloadBtn(){
        return {
            tag: 'button#reload-custom-variable-list',
            element: {
                html: 'Reload',
                style: {
                    marginLeft: '20px'
                },
                on: {
                    click: function(e){
                        location.reload();
                    }
                }
            }
        }
    }


    function migrateButton(itemObj, title) {
        let isSharedBetweenProjects = itemObj['project_specific'];
        return spawn('button.btn.btn-sm.edit', {
            onclick: function(e) {
                e.preventDefault();
                xnatCustomVariableMigratorManager.migrateAction(itemObj, this);
            }
        }, 'Migrate');
    }

    xnatCustomVariableMigratorManager.migrateAction = function(item, btnObj) {
        xmodal.open({
            title: 'Migrate Custom Variable to Custom Form',
            content: 'Are you sure you want to migrate to custom forms?' +
                '<br><br><p>As part of the migration, the following steps would be undertaken:' +
                '<ul>' +
                '<li>Custom form would be generated from the custom variable definition</li>' +
                '<li>The custom form would be associated to ALL projects using the custom variable</li>' +
                '<li>Data would be moved from custom variable to custom_fields</li>' +
                '<li>Any stored search that references a custom variable would contain references to custom fields</li>' +
                '<li>Association between Project and the Custom Variable Definition would be removed</li>' +
                '</ul>' +
                '</p>',
            width: 400,
            height: 400,
            overflow: 'auto',
            buttons: {
                ok: {
                    label: 'Proceed',
                    isDefault: true,
                    action: function() {
                        btnObj.innerHtml = "Migration Queued";
                        let fieldId = item['fieldDefinitionGroupId'];
                        let trackingId = "LegacyCustom_" + fieldId +"_"+ Date.now();
                        let url = restUrl('xapi/legacycustomvariable/migratetoformio/'+ fieldId +"?trackingId="+trackingId);
                        XNAT.xhr.post({
                            url: url,
                            success: function() {
                                xmodal.closeAll();
                                XNAT.ui.banner.top(2000, 'Custom Variable Migration request generated', 'success');
                                XNAT.app.activityTab.start('Custom Variable Migration (' + item['id'] + ")",
                                    trackingId, 'XNAT.customVariableMigrator.updateMigrationProgress');
                            },
                            fail: function(e) {
                                errorHandler(e, 'Could not migrate the custom variable ');
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

    function getProject(itemObj) {
        let projects = [];
        itemObj['projectIds'].forEach(function (proj) {
            projects.push(proj);
        });
        return projects.sort();
    }

    function spawnProjects(item) {
        let projectsArray = getProject(item);
        if (typeof projectsArray === "string") {
            return projectsArray;
        }
        if (projectsArray.length < 4) {
            return projectsArray.join(", ")
        } else {
            return spawn('button.btn.btn-sm.edit', {
                onclick: function (e) {
                    e.preventDefault();
                    XNAT.customFormManager.projectListModalManager.show(projectsArray, "associated with: " + item.id);
                }
            }, 'View');
        }
    }




    // create table for FormIO JSON Forms for site
    xnatCustomVariableMigratorManager.table = function(container, callback) {
        let tableData = [];
        let definitions = xnatCustomVariableMigratorManager.customVariables;
        let DATA_FIELDS = 'id, datatype';
        for (let k = 0; k < definitions.length; k++) {
            let item = definitions[k];
            let projectIds = [];
            let id = item.id || '';
            let datatype = item.dataType;
            let tableDataRow = {};
            tableDataRow['id'] = id;
            tableDataRow['datatype'] = datatype;
            tableDataRow['description'] = item.description;
            tableDataRow['project_specific'] = item.project_specific;
            tableDataRow['projectIds'] = item;
            tableDataRow['fieldDefinitionGroupId'] = item['fieldDefinitionGroupId'];
            tableData.push(tableDataRow);
        }


        let columns = {
            id: {
                label: 'Id',
                sortable: true
            },
            datatype: {
                label: 'Datatype',
                sortable: true
            },
            projectIds: {
                label: 'Project Ids'
            },
            project_specific: {
                label: 'Project Specific'
            }
        };


        let columnsInTable = {
            id: {
                label: 'Id',
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function(id) {
                    return truncCellWithDescription.call(this, id, this.description, '');
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
                apply: function(datatype) {
                    let dataSingular = XNAT.customFormManager.datatypeManager.getDatatypeByXsiType(datatype).label;
                    return truncCell(dataSingular, '');
                }
            },
            projectIds: {
                label: XNAT.app.displayNames.singular.project,
                sortable: true,
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function(projectIds) {
                    return spawnProjects.call(this, projectIds, '');
                }
            },
            project_specific: {
                label: XNAT.app.displayNames.singular.project + ' Specific',
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function(project_specific) {
                    let humanReadableProjectSpecific = 'No';
                    if (project_specific === 1) {
                        humanReadableProjectSpecific = 'Yes';
                    }
                    return truncCell(humanReadableProjectSpecific, '');;
                }
            },
            actions: {
                label: 'Actions',
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function(actions) {
                    return [migrateButton(this)];
                }
            }
        };

        let legacyCustomVariableTable = XNAT.table.dataTable(tableData, {
            container: container,
            header: true,
            sortable: DATA_FIELDS,
            filter: DATA_FIELDS,
            height: 'auto',
            overflowY: 'scroll',
            table: {
                className: 'xnat-table selectable scrollable-table',
                style: {
                    width: '100%',
                    marginTop: '15px',
                    marginBottom: '15px'
                }
            },
            columns: columnsInTable
        });

        xnatCustomVariableMigratorManager.$table = $(legacyCustomVariableTable.table);

        return legacyCustomVariableTable.table;
    };

    xnatCustomVariableMigratorManager.getCustomVariableDefinitions = function(callback) {
        callback = isFunction(callback) ? callback : function() {};
        return XNAT.xhr.get({
            url: restUrl('xapi/legacycustomvariable'),
            dataType: 'json',
            async: false,
            success: function(data) {
                xnatCustomVariableMigratorManager.customVariables = [];
                data.forEach(function(item) {
                    xnatCustomVariableMigratorManager.customVariables.push(item);
                });
                xnatCustomVariableMigratorManager.customVariables.sort(function(a, b) {
                    return (a.path > b.path) ? 1 : -1;
                });
                callback.apply(this, arguments);
            },
            fail: function(e) {
                errorHandler(e, 'Could not retrieve custom variable definitions');
            }

        });
    };


    xnatCustomVariableMigratorManager.init = function(container) {
        XNAT.customFormManager.datatypeManager.init();
        xnatCustomVariableMigratorManager.getCustomVariableDefinitions();

        let $manager = $$(container || 'div#custom-variable-container');

        xnatCustomVariableMigratorManager.$container = $manager;

        $manager.append(xnatCustomVariableMigratorManager.table());
        return {
            element: $manager[0],
            spawned: $manager[0],
            get: function() {
                return $manager[0]
            }
        };
    };

    xnatCustomVariableMigratorManager.refresh = xnatCustomVariableMigratorManager.refreshTable = function() {
        xnatCustomVariableMigratorManager.getCustomVariableDefinitions();
        if (typeof xnatCustomVariableMigratorManager.$table != 'undefined') {
            xnatCustomVariableMigratorManager.$table.remove();
        }
        let $manager = $('div#custom-variable-container');
        $manager.prepend(xnatCustomVariableMigratorManager.table());
    };


    xnatCustomVariableMigratorManager.init();

    return XNAT.customVariableMigrator.xnatCustomVariableMigratorManager = xnatCustomVariableMigratorManager;
}));