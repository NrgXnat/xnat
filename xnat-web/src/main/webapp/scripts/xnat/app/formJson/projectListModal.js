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
console.log('IN projectListModal.js');

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


    XNAT.customFormManager.projectListModalManager = projectListLauncher =
        getObject(XNAT.customFormManager.projectListLauncher || {});

    var projectDataTypeSingularName = XNAT.app.displayNames.singular.project;



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


    projectListLauncher.populateForm = function($form, projectsList) {
        if (typeof projectListLauncher.$table != 'undefined') {
            projectListLauncher.$table.remove();
        }
        let projectsUrl = XNAT.url.restUrl('xapi/role/projectsById',{},false,false);
        let columnIds = ["name", "id", "investigator"];
        let labelMap = {
            name: {
                label: projectDataTypeSingularName + " Name",
                checkboxes: false,
                id: "Project Name"
            },
            id: {
                label: projectDataTypeSingularName + " ID",
                checkboxes: false,
                id: "Project ID"
            },
            investigator: {
                label: "Primary Investigator",
                checkboxes: false,
                id: "Primary Investigator"
            }
        };

        var projectsFullData;

        XNAT.xhr.post({
            url: projectsUrl,
            async: false,
            contentType: 'text/plain;charset=UTF-8',
            data: projectsList.toString(),
            success: function (data) {
                projectsFullData = data;
            },
            fail: function (e) {
                errorHandler(e);
            }
        });

        if (projectsFullData.length === 0) {
            return;
        }

        let projectsTable = XNAT.table({
            className: 'projects-table xnat-table data-table clean fixed-header selectable scrollable-table',
            style: {
                width: 'auto'
            }
        });
        let $dataRows = [];
        let dataRows = [];

        function cacheRows() {
            if ($dataRows.length === 0 || $dataRows.length !== dataRows.length) {
                $dataRows = dataRows.length ?
                    $(dataRows) :
                    projectListLauncher.container.find('.table-body').find('tr');
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
            projectListLauncher.$table.find('.selectable-select-all').each(function() {
                setIndeterminate($(this), $(this).data('id'), $(this).prop('checked'));
            });
        }

        projectsTable.thead().tr();
        $.each(columnIds, function(i, c) {
            projectsTable.th('<b>' + labelMap[c].label + '</b>');
        });
        // add check-all header row
        projectsTable.tr({
            classes: 'filter'
        });

        $.each(columnIds, function(i, c) {
            if (labelMap[c].checkboxes) {
                projectsTable.td("", "");
            } else {
                document.head.appendChild(spawn('style|type=text/css', 'tr.filter-' + c + '{display:none;}'));
                let $filterInput = $.spawn('input.filter-data', {
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
                    let val = this.value;
                    let key = e.which;
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

        $.each(projectsFullData, function(i, e) {
            projectsTable.tr();
            projectsTable.td({
                classes: columnIds[0]
            }, e.name);
            projectsTable.td({
                classes: columnIds[1]
            }, e.id);
            if (e.investigator){
                projectsTable.td({
                    classes: columnIds[2]
                }, e.investigator);
            }
        });
        $form.empty().prepend(projectsTable.table);
        projectListLauncher.container = $form;
        projectListLauncher.$table = $(projectsTable.table);

    }

    projectListLauncher.show = function(projectsList, formtitle) {

        let projectSelectorContent = spawn('div.panel', [
            spawn('p', 'Following ' + XNAT.app.displayNames.plural.project + ' are associated'),
            spawn('div.standard-settings'),
            spawn('div.advanced-settings-container.hidden', [
                spawn('div.advanced-settings-toggle'),
                spawn('div.advanced-settings')
            ])
        ]);


        XNAT.ui.dialog.open({
            title: XNAT.app.displayNames.plural.project + ' ' + formtitle,
            content: projectSelectorContent,
            width: 500,
            scroll: true,
            beforeShow: function(obj) {
                var $panel = obj.$modal.find('.panel');
                var $standardInputContainer = $panel.find('.standard-settings');
                var $advancedInputContainer = $panel.find('.advanced-settings');
                projectListLauncher.populateForm($panel, projectsList);
            },
            afterShow: function(obj) {
                if (projectsList.length === 0) {
                    var $panel = obj.$modal.find('.panel');
                    $panel.empty().prepend(spawn('div.warning', {
                        style: {
                            'margin-bottom': '1em'
                        }
                    }, 'No ' + XNAT.app.displayNames.plural.project +  ' to display '));
                }
            },
            buttons: [
                {
                    label: 'Ok',
                    isDefault: false,
                    close: true
                }
            ]
        });
    }

}));