/*
 * web: projectDownload.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * Download project data
 */

var XNAT = getObject(XNAT);

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
}(function(){

    var UNDEF, projectDownload,
        ITEMS;

    XNAT.app = getObject(XNAT.app || {});

    XNAT.app.projectDownload = projectDownload =
        getObject(XNAT.app.projectDownload || {});

    function getById(id){
        return document.getElementById(id);
    }

    // list of project data is build on the download page
    // XDATScreen_download_sessions.vm
    // XNAT.app.projectDownload.items
    ITEMS = projectDownload.items;

    // general function to generate listing of data items
    function spawnDataList(type){

        var typeDashed = toDashed(type);

        // store a reference to all
        // checkboxes for checking/unchecking
        var itemCheckboxes = [];
        var $itemCheckboxes = null;

        var CKBX_WIDTH = '60px';

        // keeping track of the select-all toggle
        // in the DOM is a nightmare
        // true -> select all
        // false -> select none
        // null -> indeterminate
        var selectAll = true;

        // cache the ID string for the 'select all' menu/checkbox
        var SELECT_ALL_ID = 'select-all-' + typeDashed;

        var selectAllMenu = XNAT.ui.select.menu({
            value: 'all',
            options: {
                select: 'Select...',
                all: 'All',
                none: 'None'
            },
            element: {
                id: SELECT_ALL_ID,
                className: 'ignore',
                style: { width: CKBX_WIDTH },
                on: [['change', function(){
                    var menu = this;
                    if ($itemCheckboxes && menu.value !== 'select') {
                        $itemCheckboxes.prop('checked', false).filter(':visible').each(function(){
                            this.checked = menu.value === 'all';
                        });
                    }
                }]]
            }
        }).element;

        var $selectAllMenu = $(selectAllMenu);

        function toggleSelectAllMenu(){
            $selectAllMenu.changeVal('select');
        }

        var selectAllCheckbox = spawn('input|checked', {
            type: 'checkbox',
            id: SELECT_ALL_ID,
            className: 'ignore checkbox select-all ' + typeDashed,
            title: type + ': select all',
            checked: true
        });

        var selectAllLabel = spawn('label', {
            className: 'no-wrap',
            title: type + ': select all',
            attr: { 'for': SELECT_ALL_ID },
            style: {
                display: 'block',
                width: CKBX_WIDTH
            }
        }, [selectAllCheckbox, '&nbsp;', '<span style="text-decoration:underline;">All</span>']);

        function toggleAllItems(){
            selectAll = (selectAll === false || selectAll === null);
            $('.select-' + typeDashed).prop('checked', false).filter(':visible').each(function(){
                this.checked = selectAll;
            });
        }

        function toggleSelectAllCheckbox(){
            var selectAllCkbx = getById(SELECT_ALL_ID);
            selectAll = null;
            selectAllCkbx.checked = false;
            selectAllCkbx.indeterminate = true;
        }

        function itemCheckbox(item){
            var checkbox = spawn('input|checked', {
                type: 'checkbox',
                id: item.newId,
                className: 'select-item select-' + typeDashed,
                value: item.value,
                name: type + '[]',
                title: type + ': ' + item.name,
                checked: true
            });
            itemCheckboxes.push(checkbox);
            return checkbox;
        }

        var itemDataTable = XNAT.table.dataTable(ITEMS[type].data, {
            classes: typeDashed,
            header: false,
            sortable: false,
            table: {
                on: [
                    ['change', 'input.select-all', toggleAllItems],
                    ['change', 'input.select-item', toggleSelectAllCheckbox]
                ]
            },
            items: {
                _name: '~data-id',
                CHECKBOX: {
                    // label: '&nbsp;',
                    // sort: false,
                    td: {
                        style: { width: CKBX_WIDTH },
                        className: 'center'
                    },
                    th: {
                        style: { width: CKBX_WIDTH },
                        className: 'center'
                    },
                    filter: function(){
                        // renders a menu in the filter row
                        return spawn('div.center', [selectAllLabel]);
                    },
                    call: function(){
                        // renders the actual checkbox cell
                        // this.uid = randomID('i$', false);
                        this.newId = typeDashed + '-' + toDashed(this.name);
                        return itemCheckbox(this);
                    }
                },
                label: {
                    // label: 'Session ID',
                    // sort: true,
                    filter: true,
                    call: function(){
                        var item = this;
                        return spawn('label', {
                            title: item.name,
                            attr: { 'for': item.newId },
                            // data: { uid: item.uid },
                            html: item.label + (item.count !== UNDEF ? ' (' + item.count + ')' : '')
                        })
                    }
                }
            }
        });

        // cache checkboxes so they're available for filtering
        $itemCheckboxes = $(itemCheckboxes);

        return itemDataTable;

    }

    $('#project-sessions').find('section.list').empty().append(spawnDataList('sessions').get());

    var projectImagingData$ = $('#project-imaging-data');

    forEach(['scan_formats', 'scan_types', 'resources', 'reconstructions', 'assessors'],
        function(dataType){
            projectImagingData$.find('section.' + toDashed(dataType) + '.list')
                .empty()
                .append(spawnDataList(dataType).get());

        }
    );

    // future high-speed selector syntax with lightweight event handling
    // XNAT.dom('#/download-form').on('submit', function(e){
    //     e.preventDefault();
    // });

    // wait for the DOM to load before attaching the form handler
    // (in case there's another conflicting handler, which will be removed)
    $(function(){

        function setupDownloadUrl(id, zip){
            var base = XNAT.url.rootUrl('/xapi/archive/download/' + id);
            return zip ? (base + '/zip') : (base + '/xml');
        }

        // the 'off' method should clear out any existing event handlers
        $('#download-form').off('submit').on('submit', function(e){
            e.preventDefault();
            e.stopImmediatePropagation();
            var $form = $(this);
            var type = $form.find('.download-type:checked').val();
            var getZip = (type === 'direct');
            var msg = [];
            $form.find('[name="XNAT_CSRF"]').remove();
            var $formSubmit = $form.submitJSON();
            $formSubmit.always(function(data){
                var ID = data.responseText;
                var URL = setupDownloadUrl(ID, getZip);
                if (getZip) {
                    msg.push('Click "Download" to start the zip download.');
                }
                else {
                    msg.push('Click "Download" to download the catalog XML.');
                }
                msg.push('<br><br>');
                msg.push('The download id is <b>' + ID + '</b>.');

                xmodal.confirm({
                    content: msg.join(' '),
                    okLabel: 'Download',
                    okAction: function(){
                        window.open(URL)
                    }
                })
            });
            $formSubmit.done(function(id){
                console.log(id);
                // xmodal.message('Catalog ID', data);
            });
            return false;
        });
    });

    // this script has loaded
    projectDownload.loaded = true;

    return XNAT.app.projectDownload = projectDownload;

}));

