
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
}(function() {
    XNAT.app = getObject(XNAT.app || {});
    XNAT.app.context = getObject(XNAT.app.context || {});
    const scanContrastManager = XNAT.app.context.scanContrastManager = getObject(XNAT.app.context.scanContrastManager || {});
    var restUrl = XNAT.url.restUrl;
    scanContrastManager.contrasts = [];
    scanContrastManager.currentContrast;
    var acceptedDataTypes = ["xnat:mrSessionData", "xnat:ctSessionData", "xnat:xaSessionData"]

    scanContrastManager.rmvIngredient = function (child){
        scanContrastManager.currentContrast.ingredients.splice(child-1, 1);
        scanContrastManager.refreshIngredientsTable();
    }

    scanContrastManager.rmvAdmin = function (child){
        scanContrastManager.currentContrast.administrations.splice(child-1, 1);
        scanContrastManager.refreshAdministrationsTable();
    }

    scanContrastManager.addEntry = function(){
        var scanId = $("#scd-scan-selector").find(":selected").val();
        if (scanId == "") {
            return;
        }
        var newContrast = {};
        newContrast.scanId = scanId;
        var addElement = document.getElementById('scan-contrast-add');
        addElement.remove();
        scanContrastManager.currentContrast = newContrast;
        renderContrastForm(scanContrastManager.currentContrast);
    }

    scanContrastManager.getAddNewContrastButton = function() {
        return spawn('div',[
            spawn('button.btn.btn-sm.edit', {
                onclick: function (e) {
                    scanContrastManager.addContrastModal();
                },
                title: "Add New Contrast"
            }, '<i class="fa fa-plus" style="margin-right: 4px"></i> Add Contrast'),
            spacer(10),
            spawn('button.btn.btn-sm.refresh-contrast', {
                onclick: function(e) {
                    scanContrastManager.refreshContrastAction();
                },
                title: "Refresh Contrast Data"
            }, '<i class="fa fa-refresh" style="margin-right: 4px"></i> Refresh Contrast Data')
        ]);
    }

    scanContrastManager.addContrastModal = function(){
        const tmpl = spawn('div#scan-contrast-details', [
            spawn('div#scan-contrast-add', [
                "Add new contrast record: ",
                spawn('select#scd-scan-selector'),
                spawn('button.btn.btn-sm.edit', {
                    onclick: function (e) {
                        e.preventDefault();
                        scanContrastManager.addEntry(this);
                    },
                }, 'Add')
            ]),
            spawn('div#scan-contrast-details-body')
        ]);

        this.dialog=XNAT.ui.dialog.open({
            title: 'Add New Contrast Bolus',
            width: 720,
            content:tmpl,
            isDraggable: true,
            mask: false,
            esc: true,
            buttons: [
                {
                    label: 'Save and Close',
                    isDefault: true,
                    close: false,
                    action: function() {
                        if (document.getElementById('scan-contrast-add') != null) {
                            XNAT.ui.banner.top(2000, 'Contrast not created as no scan was selected.', 'error');
                            return;
                        }
                        let responseErrorMessage = saveChangeToContrasts(true);
                        if (!responseErrorMessage) {
                            scanContrastManager.refreshTable();
                            XNAT.ui.dialog.closeAll();
                            XNAT.ui.banner.top(2000, "Contrast created successfully", 'success');
                            XNAT.ui.dialog.closeAll();
                        }
                        else {
                            XNAT.ui.banner.top(2000, 'Failed to create contrast.', 'error');
                        }
                    }
                },
                {
                    label: 'Save',
                    isDefault: false,
                    close: false,
                    action: function() {
                        if (document.getElementById('scan-contrast-add') != null) {
                            XNAT.ui.banner.top(2000, 'You must choose a scan for the contrast.', 'error');
                            return;
                        }
                        let responseErrorMessage = saveChangeToContrasts(true);
                        if (!responseErrorMessage) {
                            XNAT.app.context.scanContrastManager.refreshTable();
                            XNAT.ui.banner.top(2000, "Contrast created successfully", 'success');
                        }
                        else {
                            XNAT.ui.banner.top(2000, 'Failed to create contrast.', 'error');
                        }
                    }
                },
                {
                    label: 'Close',
                    isDefault: false,
                    close: true
                }
            ],
            afterShow: function() {
                renderAddContrastOptions();
            }
        });
    }

    scanContrastManager.refreshContrastAction = function(){
        XNAT.dialog.open({
            title: 'Refresh DICOM Contrast Data',
            content: 'If the contrast data shown here appears out of sync or incomplete, this function will reexamine the DICOM headers and repopulate contrast data for this experiment. Note that any contrast entries that were added manually will not be affected by this operation.',
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    action: function(){
                        XNAT.xhr.put({
                            url: XNAT.url.csrfUrl('/data/experiments/'+XNAT.data.context.ID+'?pullDataFromHeaders=true'),
                            success: function(){
                                XNAT.ui.banner.top(2000,'Refreshed contrast data','success');
                                scanContrastManager.init();
                            },
                            fail: function (e) {
                                errorHandler(e, 'Could not refresh contrast data.');
                            }
                        });
                    }
                },
                {
                    label: 'Cancel',
                    close: true
                }
            ]
        })
    }

    scanContrastManager.getViewButton = function(contrastItem) {
        return spawn('button.btn.btn-sm.edit', {
            onclick: function (e) {
                e.preventDefault();
                scanContrastManager.viewModal(contrastItem);
            },
            title: "View contrast information"
        }, 'View');
    }

    scanContrastManager.viewModal = function(contrastItem) {
        const tmpl = spawn('div#scan-contrast-details', [
            spawn('div#scan-contrast-details-body', [
                "Loading..."
                ])
        ]);

        this.dialog=XNAT.ui.dialog.open({
            title: 'Contrast Bolus Details',
            width: 720,
            content:tmpl,
            isDraggable: true,
            mask: false,
            esc: true,
            buttons: [
                {
                    label: 'Save and Close',
                    isDefault: true,
                    close: false,
                    action: function() {
                        let responseErrorMessage = saveChangeToContrasts(false);
                        if (!responseErrorMessage) {
                            scanContrastManager.refreshTable();
                            XNAT.ui.dialog.closeAll();
                            XNAT.ui.banner.top(2000, "Contrast updated successfully", 'success');
                            XNAT.ui.dialog.closeAll();
                        }
                        else {
                            XNAT.ui.banner.top(2000, 'Failed to create contrast.', 'error');
                        }
                    }
                },
                {
                    label: 'Save',
                    isDefault: false,
                    close: false,
                    action: function() {
                        let responseErrorMessage = saveChangeToContrasts(false);
                        if (!responseErrorMessage) {
                            scanContrastManager.refreshTable();
                            XNAT.ui.banner.top(2000, "Contrast updated successfully", 'success');
                        }
                        else {
                            XNAT.ui.banner.top(2000, 'Failed to create contrast.', 'error');
                        }
                    }
                },
                {
                    label: 'Close',
                    isDefault: false,
                    close: true
                }
            ],
            afterShow: function () {
                scanContrastManager.currentContrast = contrastItem;
                renderContrastForm(contrastItem);
                document.getElementById("agent_input").readOnly = true;
            }
        });
    }

    scanContrastManager.getDeleteButton = function(contrastItem) {
        return spawn('button.btn.btn-sm.edit', {
            onclick: function (e) {
                e.preventDefault();
                scanContrastManager.deleteContrast(contrastItem)
            },
            title: "Delete contrast bolus"
        }, 'Delete');
    }

    scanContrastManager.deleteContrast = function(contrastItem) {
        xmodal.open({
            title: 'Confirm Contrast Deletion',
            content: 'Are you sure you want to delete the contrast bolus element? This operation cannot be undone.',
            width: 400,
            height: 200,
            overflow: 'auto',
            buttons: {
                ok: {
                    label: 'Proceed',
                    isDefault: true,
                    action: function () {
                        XNAT.xhr.delete({
                            url: restUrl('/xapi/contrasts/delete/' + scanContrastManager.sessionId),
                            async: false,
                            contentType: 'application/json',
                            data: JSON.stringify(contrastItem),
                            success: function () {
                                xmodal.closeAll();
                                XNAT.ui.banner.top(2000, "Contrast deleted", 'success');
                                scanContrastManager.refreshTable();
                            },
                            fail: function (e) {
                                errorHandler(e, 'Could not delete the contrast element.');
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

    scanContrastManager.getSessionContrasts = function() {
        XNAT.xhr.get({
            url: restUrl('/xapi/contrasts/' + scanContrastManager.sessionId),
            async: false,
            success: function (data) {
                scanContrastManager.contrasts = []
                data.forEach(function (item) {
                    scanContrastManager.contrasts.push(item);
                });
            },
            fail: function (e) {
                errorHandler(e, 'Could not retrieve contrast data for ' + scanContrastManager.sessionId);
            }
        });
    }

    scanContrastManager.addIngredientModal = function(addOrEdit, existingIngredient, position) {
        let tmpl =  spawn('div.element-wrapper', [
            createInputElement('Name', 'ingredient', 'add_ingredient_input', existingIngredient.name, 'text', null, {width: '30%'})
        ]);

        this.dialog=XNAT.ui.dialog.open({
            title: addOrEdit + ' New Ingredient',
            width: 720,
            content:tmpl,
            isDraggable: true,
            mask: false,
            esc: true,
            buttons: [
                {
                    label: 'Save',
                    isDefault: true,
                    close: true,
                    action: function() {
                        let ingredientInput = document.getElementById('add_ingredient_input').value;
                        const newIngredient = {}
                        newIngredient.name = ingredientInput;
                        if (addOrEdit==='Add') {
                            scanContrastManager.currentContrast.ingredients.push(newIngredient);
                        } else {
                            scanContrastManager.currentContrast.ingredients.splice(position, 1, newIngredient);
                        }
                        scanContrastManager.refreshIngredientsTable();
                    }
                },
                {
                    label: 'Close',
                    isDefault: false,
                    close: true
                }
            ]
        });
    }

    scanContrastManager.refreshIngredientsTable = function() {
        if (typeof scanContrastManager.$ingredientsTable != 'undefined') {
            scanContrastManager.$ingredientsTable.remove();
        }
        let $contrastDiv = $('div#ingredients_table');
        $contrastDiv.prepend(scanContrastManager.ingredientsTable(scanContrastManager.currentContrast.ingredients));
    };

    scanContrastManager.ingredientsTable = function(ingredients) {
        let tableData = [];
        for (let k = 0; k < ingredients.length; k++) {
            let ingredient = ingredients[k].name;
            let tableDataRow = {};
            tableDataRow['ingredient'] = ingredient;
            tableDataRow['actions'] = k+1;
            tableData.push(tableDataRow);
        }

        let columnsInTable = {
            ingredient: {
                label: 'Ingredient',
                sortable: true,
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function (ingredient) {
                    return spawn('span.ingredient_name_span', {
                        title: ingredient,
                        html: ingredient
                    });
                }
            },
            actions: {
                label: 'Actions',
                td: {
                    style: {
                        verticalAlign: 'middle',
                        width: '150px'
                    }
                },
                apply: function (actions) {
                    return[
                        spawn('button.btn.btn-sm.edit', {
                            onclick: function (e) {
                                e.preventDefault();
                                XNAT.app.context.scanContrastManager.addIngredientModal("Edit", XNAT.app.context.scanContrastManager.currentContrast.ingredients[actions-1], actions-1);
                            },
                            title: "Edit Ingredient"
                        }, 'Edit'),
                        spacer(4),
                        spawn('button.btn.btn-sm.edit', {
                            onclick: function (e) {
                                e.preventDefault();
                                scanContrastManager.rmvIngredient(actions);
                            },
                            title: "Remove Ingredient"
                        }, 'Remove')
                    ]
                }
            }
        };

        let inTable = XNAT.table.dataTable(tableData, {
            header: true,
            sortable: "ingredient",
            filter: "ingredient",
            height: 'auto',
            table: {
                className: 'session-contrast-table xnat-table selectable',
                style: {
                    width: '50%',
                    marginTop: '15px',
                    marginBottom: '15px',
                    border: '1px solid #aaa'
                }
            },
            columns: columnsInTable
        });

        scanContrastManager.$ingredientsTable = $(inTable.table);
        return inTable.table;
    }

    scanContrastManager.addAdministrationModal = function(addOrEdit, existingAdministration, position) {
        let formattedStartTime = handleTimeElement(existingAdministration.startTime);
        let formattedEndTime = handleTimeElement(existingAdministration.endTime);
        let tmpl =  spawn('div.element-wrapper', [
            createInputElement('Volume', 'administration', 'admin_volume_input', existingAdministration.volume, 'text', null, {width: '20%'}),
            createInputElement('Start Time', 'administration', 'admin_start_time_input', formattedStartTime, 'time', null, {width: '20%'}),
            createInputElement('End Time', 'administration', 'admin_end_time_input', formattedEndTime, 'time', null, {width: '20%'}),
            createInputElement('Duration', 'administration', 'admin_duration_input', existingAdministration.duration, 'text', null, {width: '20%'}),
            createInputElement('Flow Rate', 'administration', 'admin_flow_rate_input', existingAdministration.flowRate, 'text', null, {width: '20%'})
        ]);

        this.dialog=XNAT.ui.dialog.open({
            title: addOrEdit + ' Administration',
            width: 720,
            content:tmpl,
            isDraggable: true,
            mask: false,
            esc: true,
            buttons: [
                {
                    label: 'Save',
                    isDefault: true,
                    close: true,
                    action: function() {
                        let adminVolumeInput = document.getElementById('admin_volume_input').value;
                        let adminStartTimeInput = document.getElementById('admin_start_time_input').value;
                        let adminEndTimeInput = document.getElementById('admin_end_time_input').value;
                        let adminDurationInput = document.getElementById('admin_duration_input').value;
                        let adminFlowRateInput = document.getElementById('admin_flow_rate_input').value;
                        const newAdministration = {}
                        newAdministration.volume = adminVolumeInput;
                        newAdministration.startTime = adminStartTimeInput;
                        newAdministration.endTime = adminEndTimeInput;
                        newAdministration.duration = adminDurationInput;
                        newAdministration.flowRate = adminFlowRateInput;
                        if (addOrEdit==='Add') {
                            XNAT.app.context.scanContrastManager.currentContrast.administrations.push(newAdministration);
                        } else {
                            XNAT.app.context.scanContrastManager.currentContrast.administrations.splice(position, 1, newAdministration);
                        }
                        XNAT.app.context.scanContrastManager.refreshAdministrationsTable();
                    }
                },
                {
                    label: 'Close',
                    isDefault: false,
                    close: true
                }
            ]
        });
    }

    scanContrastManager.refreshAdministrationsTable = function() {
        if (typeof scanContrastManager.$administrationTable != 'undefined') {
            scanContrastManager.$administrationTable.remove();
        }
        let $contrastDiv = $('div#administrations_table');
        $contrastDiv.prepend(scanContrastManager.administrationTable(scanContrastManager.currentContrast.administrations));
    };

    scanContrastManager.administrationTable = function(administrations) {
        let tableData = [];
        DATA_FIELDS = "adminVolume, adminStartTime, adminEndTime, adminDuration, adminFlowRate"
        for (let k = 0; k < administrations.length; k++) {
            let administration = administrations[k];
            let tableDataRow = {};
            tableDataRow['adminVolume'] = administration['volume'];
            tableDataRow['adminStartTime'] = administration['startTime'];
            tableDataRow['adminEndTime'] = administration['endTime'];
            tableDataRow['adminDuration'] = administration['duration'];
            tableDataRow['adminFlowRate'] = administration['flowRate'];
            tableDataRow['actions'] = k+1;
            tableData.push(tableDataRow);
        }

        let columnsInTable = {
            adminVolume: {
                label: 'Volume',
                sortable: true,
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function (adminVolume) {
                    return spawn('span.admin_volume_span', {
                        title: adminVolume,
                        html: adminVolume
                    });
                }
            },
            adminStartTime: {
                label: 'Start Time',
                sortable: true,
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function (adminStartTime) {
                    let formattedTime = handleTimeElement(adminStartTime)
                    return spawn('span.admin_start_time_span', {
                        title: formattedTime,
                        html: formattedTime
                    });
                }
            },
            adminEndTime: {
                label: 'End Time',
                sortable: true,
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function (adminEndTime) {
                    let formattedTime = handleTimeElement(adminEndTime)
                    return spawn('span.admin_end_time_span', {
                        title: formattedTime,
                        html: formattedTime
                    });
                }
            },
            adminDuration: {
                label: 'Duration',
                sortable: true,
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function (adminDuration) {
                    return spawn('span.admin_duration_span', {
                        title: adminDuration,
                        html: adminDuration
                    });
                }
            },
            adminFlowRate: {
                label: 'Flow Rate',
                sortable: true,
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function (adminFlowRate) {
                    return spawn('span.admin_flow_rate_span', {
                        title: adminFlowRate,
                        html: adminFlowRate
                    });
                }
            },
            actions: {
                label: 'Actions',
                td: {
                    style: {
                        verticalAlign: 'middle',
                        width: '150px'
                    }
                },
                apply: function (actions) {
                    return[
                        spawn('button.btn.btn-sm.edit', {
                            onclick: function (e) {
                                e.preventDefault();
                                XNAT.app.context.scanContrastManager.addAdministrationModal("Edit", XNAT.app.context.scanContrastManager.currentContrast.administrations[actions-1], actions-1);
                            },
                            title: "Edit Administration"
                        }, 'Edit'),
                        spacer(4),
                        spawn('button.btn.btn-sm.edit', {
                            onclick: function (e) {
                                e.preventDefault();
                                scanContrastManager.rmvAdmin(actions);
                            },
                            title: "Remove Administration"
                        }, 'Remove')
                    ]
                }
            }
        };

        let adminTable = XNAT.table.dataTable(tableData, {
            header: true,
            sortable: DATA_FIELDS,
            filter: DATA_FIELDS,
            height: 'auto',
            table: {
                className: 'session-contrast-table xnat-table selectable',
                style: {
                    width: '100%',
                    marginTop: '15px',
                    marginBottom: '15px',
                    border: '1px solid #aaa'
                }
            },
            columns: columnsInTable
        });

        scanContrastManager.$administrationTable = $(adminTable.table);
        return adminTable.table;
    }

    scanContrastManager.refreshTable = function() {
        if (typeof scanContrastManager.$table != 'undefined') {
            scanContrastManager.$table.remove();
        }
        let $contrastDiv = $('div#session_contrast_section');
        $contrastDiv.prepend(scanContrastManager.table());
    };

    scanContrastManager.table = function() {
        scanContrastManager.getSessionContrasts();
        let tableData = [];
        let idToScanId = scanContrastManager.scanIdToId;
        let contrasts = scanContrastManager.contrasts;
        DATA_FIELDS = "agent, scan"
        for (let k = 0; k < contrasts.length; k++) {
            let contrast = contrasts[k];
            let tableDataRow = {};
            tableDataRow['agent'] = contrast['agent'];
            tableDataRow['scan'] = scanContrastManager.scanIdToId[contrast['scanId']];
            tableDataRow['actions'] = contrast;
            tableData.push(tableDataRow);
        }

        let columnsInTable = {
            scan: {
                label: 'Scan',
                sortable: true,
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function (scan) {
                    return spawn('span', {
                        title: scan,
                        html: scan
                    });
                }
            },
            agent: {
                label: 'Agent',
                sortable: true,
                td: {
                    style: {
                        verticalAlign: 'middle'
                    }
                },
                apply: function (agent) {
                    return spawn('span', {
                        title: agent,
                        html: agent
                    });
                }
            },
            actions: {
                label: 'Actions',
                td: {
                    style: {
                        verticalAlign: 'middle',
                        width: '135px'
                    }
                },
                apply: function (actions) {
                    return [scanContrastManager.getViewButton(actions), spacer(4), scanContrastManager.getDeleteButton(actions)]
                }
            }
        };

        let contrastTable = XNAT.table.dataTable(tableData, {
            header: true,
            sortable: DATA_FIELDS,
            filter: DATA_FIELDS,
            height: 'auto',
            table: {
                className: 'session-contrast-table xnat-table selectable',
                style: {
                    width: '40%',
                    marginTop: '15px',
                    marginBottom: '15px',
                    border: '1px solid #aaa'
                }
            },
            columns: columnsInTable
        });

        scanContrastManager.$table = $(contrastTable.table);
        return contrastTable.table;
    }

    scanContrastManager.init = async function() {
        await setupDataForTable();
        if(XNAT.app.context.scanContrastManager.contrastFeatureFlag && acceptedDataTypes.includes(XNAT.app.context.scanContrastManager.sessionType)) {
            let $contrastHeader = $('div#session_contrast_header');
            let $contrastDiv = $('div#session_contrast_section');
            $contrastHeader.empty().append(spawn('h3', "Contrasts"));
            $contrastDiv.empty().append(scanContrastManager.table());
            $contrastDiv.append(scanContrastManager.getAddNewContrastButton());
        } else {
            $('div#session_contrast_section').remove();
        }
    }
    scanContrastManager.init();
}));

function spacer(width) {
    return spawn('i.spacer', {
        style: {
            display: 'inline-block',
            width: width + 'px'
        }
    })
}

function handleTimeElement(timeElement) {
    if (timeElement && !timeElement.includes(":")) {
        if (timeElement < 4) {
            return timeElement;
        } else if (timeElement.length === 4) {
            return timeElement.substring(0,2) + ":" + timeElement.substring(2,4);
        } else if (timeElement.length > 4){
            return timeElement.substring(0,2) + ":" + timeElement.substring(2,4) + ":" + timeElement.substring(4, 6);
        }
    } else {
        return timeElement;
    }
}

 function renderAddContrastOptions(){
    // Populate the select box
    $.each(XNAT.app.context.scanContrastManager.scanIds, function(index, itemS) {
        $("#scd-scan-selector").append($("<option></option>").attr("value", itemS).text(XNAT.app.context.scanContrastManager.scanDescs[index]));
    });
}

function createInputElement(label, classTag, id, value, inputType, tooltipTitle, elementStyle) {
    let elements = [
        spawn('label.element-label|class=contrast', label),
        spawn('input', {
            addClass: classTag,
            type: inputType,
            id: id,
            value: value ?? ''
        })
    ];
    if (elementStyle) {
        for(var s in elementStyle) {
            elements[1].style[s] = elementStyle[s];
    }
    }
    if (tooltipTitle) {
        elements.push(spawn('a.infoLink#render-resources-info', {
            title: tooltipTitle,
            html: '<i class="fa fa-question-circle"></i>',
            style: {position: 'relative', top: '0px', left: '5px'},
        }));
    }
    return spawn('div.element-wrapper', elements);
}

// Function to render form fields for each object in JSON data
function renderContrastForm(item) {
    const container = document.getElementById('scan-contrast-details-body');
    container.innerHTML = '';
    const form = document.createElement('form');
    form.id = 'form';
    form.scanId = ""+item.scanId;
    var scanDesc = XNAT.app.context.scanContrastManager.scanDescs[XNAT.app.context.scanContrastManager.scanIds.indexOf(form.scanId)];

    item.ingredients = (item.ingredients==undefined)? new Array():item.ingredients;
    item.administrations = (item.administrations==undefined)? new Array():item.administrations;

    form.appendChild(spawn('h3', scanDesc))
    form.appendChild(spawn('div|class="warning"',{'style': {visibility: 'hidden'}, 'id': 'warning'}));
    form.appendChild(createInputElement('Agent', 'agent required', 'agent_input', item.agent, 'text', 'Contrast or bolus agent administered.'));
    form.appendChild(createInputElement('Agent Number', 'agent', 'agent_number_input', item.agentNumber, 'text', 'Identifying number of the agent administered. The number shall be 1 for the first Item and increase by 1 for each subsequent Item.'));
    form.appendChild(createInputElement('Route', 'agent', 'route_input', item.route, 'text', 'The route of administration of contrast agent.'));
    form.appendChild(createInputElement('Ingredient Concentration', 'agent', 'concentration_input', item.concentration, 'text', 'Milligrams of active ingredient per milliliter of agent.'));
    form.appendChild(createInputElement('Percentage By Volume', 'agent', 'percentageByVolume_input', item.percentageByVolume, 'text', 'Percentage by volume of active ingredient in the total volume.'));
    form.appendChild(createInputElement('Ingredient Opaque', 'agent', 'ingredientOpaque_input', item.ingredientOpaque, 'text', 'Absorption of the ingredient greater than the absorption of water (tissue). Enumerated values: YES, NO'));
    if (XNAT.app.context.scanContrastManager.sessionType === "xnat:ctSessionData") {
        form.appendChild(createInputElement('T1 Relaxivity', 'agent', 't1Relaxivity_input', item.t1Relaxivity, 'text', 'T1 Relaxivity of the MR Contrast/Bolus used specified in s-1*mmol-1 specified at body temperature in blood plasma.'));
        form.appendChild(createInputElement('T2 Relaxivity', 'agent', 't2Relaxivity_input', item.t2Relaxivity, 'text', 'T2 Relaxivity of the MR Contrast/Bolus used specified in s-1*mmol-1 specified at body temperature in blood plasma.', {marginBottom: '25px'}));
    }
    form.appendChild(spawn('h3', "Ingredients"))
    let ingredientsTableDiv = spawn('div', {'id': 'ingredients_table'});
    ingredientsTableDiv.appendChild(XNAT.app.context.scanContrastManager.ingredientsTable(item.ingredients));
    form.appendChild(ingredientsTableDiv);
    let addIngredientButton = spawn('button.btn.btn-sm.edit', {
        onclick: function (e) {
            e.preventDefault();
            XNAT.app.context.scanContrastManager.addIngredientModal("Add", new Array(), null);
        },
        title: "Add New Ingredient",
        style: {
                marginBottom: '25px'
        },

        id: 'add_ingredient_button'
    }, 'Add Ingredient');
    form.appendChild(addIngredientButton);
    form.appendChild(spawn('h3', "Administrations"))
    let administrationsTableDiv = spawn('div', {'id': 'administrations_table'});
    administrationsTableDiv.appendChild(XNAT.app.context.scanContrastManager.administrationTable(item.administrations));
    form.appendChild(administrationsTableDiv);
    let addAdministrationButton = spawn('button.btn.btn-sm.edit', {
        onclick: function (e) {
            e.preventDefault();
            XNAT.app.context.scanContrastManager.addAdministrationModal("Add", new Array(), null);
        },
        title: "Add New Administration",
        style: {
                marginBottom: '25px'
        },

        id: 'add_administration_button'
    }, 'Add Administration');

    form.appendChild(addAdministrationButton);
    container.appendChild(form);
}

// Function to save changes made in the form back to the JSON data
function saveChangeToContrasts(isNewContrast) {
    let errorMessage = ''
    XNAT.app.context.validation="";
    const jsonData = initializeJSONFromContrastForm();
    if(XNAT.app.context.validation!=""){
        $("#warning").html(XNAT.app.context.validation);
        $("#warning").css('visibility','visible');
        errorMessage = XNAT.app.context.validation;
        return errorMessage;
    }else{
        $("#warning").html("");
        $("#warning").css('visibility','hidden');
    }

    let apiUrl = serverRoot + '/xapi/contrasts/';
    if (isNewContrast) {
        apiUrl = apiUrl + 'create';
    } else {
        apiUrl = apiUrl + 'save';
    }
    apiUrl = apiUrl + '/' + XNAT.app.context.scanContrastManager.sessionId;
    openModalPanel("CONTRAST_L","Saving contrast data");
    const scdBody = $("#scan-contrast-details-body");
    $.ajax({
        url: apiUrl,
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(jsonData),
        success: function(response) {
            closeModalPanel("CONTRAST_L");
            XNAT.app.context.scanContrastManager.refreshTable();
            console.log('Data submitted successfully', response);
        },
        error: function(error) {
            closeModalPanel("CONTRAST_L");
            console.error('Error submitting data', error);
            errorMessage=error;
        }
    });
    return errorMessage;
}

// Function to save changes made in the form back to the JSON data
function initializeJSONFromContrastForm(){
    const form = document.getElementById('form');
    const formData = new FormData(form);

    XNAT.app.context.valid = true;
    const jsonData = XNAT.app.context.scanContrastManager.currentContrast;

    jsonData.agent = XNAT.app.context.validate($('#agent_input'),"Agent Name", "required text", 'input');
    jsonData.agentNumber = XNAT.app.context.validate($('#agent_number_input'),"Agent Number", "integer", 'input');
    jsonData.concentration = XNAT.app.context.validate($('#concentration_input'),"Concentration", "float", 'input');
    jsonData.route = XNAT.app.context.validate($('#route_input'),"Route", "text", 'input');
    jsonData.percentageByVolume = XNAT.app.context.validate($('#percentageByVolume_input'),"Percentage By Volume", "float", 'input');
    jsonData.ingredientOpaque = XNAT.app.context.validate($('#ingredientOpaque_input'),"Opaque", "text", 'input');
    if (XNAT.app.context.scanContrastManager.sessionType === "xnat:ctSessionData") {
        jsonData.t1Relaxivity = XNAT.app.context.validate($('#t1Relaxivity_input'),"T1 Relaxivity", "float", 'input');
        jsonData.t2Relaxivity = XNAT.app.context.validate($('#t2Relaxivity_input'),"T2 Relaxivity", "float", 'input');
    } else {
        jsonData.t1Relaxivity = "";
        jsonData.t2Relaxivity = ""
    }

    let ingredientNameValues = $(".ingredient_name_span");
    for (let i = 0; i < ingredientNameValues.length; i++) {
        if(jsonData.ingredients[i]==undefined)jsonData.ingredients[i]={};
        jsonData.ingredients[i].name = XNAT.app.context.validate(ingredientNameValues.get(i), "Ingredient Name in row " + i, "required text", 'span');
    }

    //remove the ones that aren't there anymore
    for(let i = ingredientNameValues.length; i < jsonData.ingredients.length; i++) {
        jsonData.ingredients.splice(i);
    }

    let adminValues = $(".admin_volume_span");
    let adminStartTimes = $(".admin_start_time_span");
    let adminEndTimes = $(".admin_end_time_span");
    let adminDurations = $(".admin_duration_span");
    let adminFlowRates = $(".admin_flow_rate_span");

    for (let i = 0; i < adminValues.length; i++) {
        if(jsonData.administrations[i]==undefined)jsonData.administrations[i]={};
        jsonData.administrations[i].volume=XNAT.app.context.validate(adminValues.get(i),"Administration Volume in row " + i , "float", 'span')
        jsonData.administrations[i].startTime=XNAT.app.context.validate(adminStartTimes.get(i),"Administration Start Time in row " + i, " time", 'span');
        jsonData.administrations[i].endTime=XNAT.app.context.validate(adminEndTimes.get(i),"Administration End Time in row " + i, " time", 'span');
        jsonData.administrations[i].duration=XNAT.app.context.validate(adminDurations.get(i),"Administration Duration in row " + i, " float", 'span');
        jsonData.administrations[i].flowRate=XNAT.app.context.validate(adminFlowRates.get(i),"Administration Flow Rate in row " + i, " float", 'span');
    }

    //remove the ones that aren't there anymore
    for(let i = adminValues.length; i < jsonData.ingredients.length; i++) {
        jsonData.administrations.splice(i);
    }

    console.log('Updated JSON Data:', JSON.stringify(jsonData, null, 2));
    return jsonData;
}

function textContains(string, substring){
    return (string.indexOf(substring) !== -1);
}

XNAT.app.context.validate=function(element, name, validators, elementType){
    elementValue = '';
    if (elementType === "input") {
        elementValue = element.val();
    }
    if (elementType === "span") {
        elementValue = element.innerText;
    }

    var valid = true;
    if(textContains(validators,"required")){
        if(elementValue.trim()===''){
            XNAT.app.context.validation+=name + " is a required field.</br>";
            valid=false;
        }
    }
    if(textContains(validators,"float")){
        var temp=elementValue.trim().replace(/\s+/g,"");
        if ( temp !== "" && !Number.isFinite(Number(temp))) {
            XNAT.app.context.validation+=name + " must be a valid float.</br>";
            valid=false;
        }
    }
    if(textContains(validators,"text")){
        elementValue = elementValue.trim();
        if(elementValue !== "" && !elementValue.match('^[A-Za-z0-9 ,.]+$')){
            valid=false;
        }
    }
    if(textContains(validators,"integer")){
         var temp = elementValue.trim().replace(/\s+/g, "");
         if (temp !== "" && !Number.isInteger(Number(temp))) {
             XNAT.app.context.validation += name + " must be a valid integer.</br>";
             valid = false;
         }
    }

    if (elementType === "input") {
        if(valid){
            element.css("border-color", "");
        }else{
            element.css("border-color", "red");
        }
    }

    return elementValue;
}