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

    function downloadFileInPage(URL) {
        var link = document.createElement('a');
        link.href = URL;
        link.download = '';
        link.click();
    }

    // list of project data is build on the download page
    // XDATScreen_download_sessions.vm
    // XNAT.app.projectDownload.items
    ITEMS = projectDownload.items;

    // general function to generate listing of data items
    function spawnDataList(type){

        if (ITEMS[type].list && !ITEMS[type].list.length) {
            return {
                get: function(){ return '<p class="none"><i>(none)</i></p>' }
            };
        }

        var typeDashed = toDashed(type);

        // store a reference to all
        // checkboxes for checking/unchecking
        var itemCheckboxes = [];
        var $itemCheckboxes = null;

        var CKBX_WIDTH = '50px';

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
            checked: true,
            style: { 'width':'auto' }
        });

        var selectAllLabel = spawn('label', {
            className: 'no-wrap',
            title: type + ': select all',
            attr: { 'for': SELECT_ALL_ID },
            style: {
                display: 'block',
                width: CKBX_WIDTH
            }
        }, [selectAllCheckbox, ' <span style="text-decoration:underline;">All</span>']);

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
            var checkbox = spawn('input', {
                type: 'checkbox',
                id: item.newId,
                className: 'select-item select-' + typeDashed,
                value: window.unescapeHTML(item.value),
                name: type + '[]',
                title: type + ': ' + item.name,
                checked: true
            });
            itemCheckboxes.push(checkbox);
            return checkbox;
        }

        var itemTableConfig = {
            classes: typeDashed,
            header: false,
            sortable: false,
            table: {
                style: { tableLayout: 'fixed' },
                on: [
                    ['change', 'input.select-all', toggleAllItems],
                    ['change', 'input.select-item', toggleSelectAllCheckbox]
                ]
            },
            columns: {
                _name: '~data-id'//,
                // _id: function(){
                //     // this.uid = randomID('i$', false);
                //     // saves a 'newId' property back to the data object
                //     this.newId = typeDashed + '-' + toDashed(this.name);
                //     return false;
                // }
            }
        };

        // checkbox column
        itemTableConfig.columns[typeDashed + '-ckbx'] = {
            // label: '&nbsp;',
            // sort: false,
            td: {
                // className: 'center',
                style: { width: CKBX_WIDTH }
            },
            th: {
                // className: 'center',
                style: { width: CKBX_WIDTH }
            },
            filter: function(){
                // renders a menu in the filter row
                return spawn('div', [selectAllLabel]);
            },
            apply: function(){
                // this.uid = randomID('i$', false);
                // saves a 'newId' property back to the data object
                this.newId = typeDashed + '-' + toDashed(this.name);
                // renders the actual checkbox cell
                return itemCheckbox(this);
            }
        };


        // item column
        itemTableConfig.columns[typeDashed + '-label'] = {
            // label: 'Session ID',
            // sort: true,
            filter: true,
            apply: function(){
                var item = this;
                var hasType = item.type !== UNDEF;
                var hasCount = item.count !== UNDEF;
                var _label = spawn('label.truncate.pull-left', {
                    title: item.name,
                    style: { width: hasType || hasCount ? '74%' : '98%' },
                    attr: { 'for': item.newId },
                    // data: { uid: item.uid },
                    html: item.label
                });
                var _type = hasType ?
                    spawn('span.item-type.mono.pull-right.text-right', {
                        style: { width: '24%' },
                        html: item.type.trim() ? ' (' + item.type + ')' : ''
                    }) :
                    '';
                var _count = hasCount ?
                    spawn('span.item-count.mono.pull-right.text-right', {
                        style: { width: '24%' },
                        html: ' (' + item.count + ')'
                    }) :
                    '';
                return spawn('!', [_label, _count || _type]);
            }
        };

        var itemDataTable = XNAT.table.dataTable(ITEMS[type].data, itemTableConfig);

        // cache checkboxes so they're available for filtering
        $itemCheckboxes = $(itemCheckboxes);

        return itemDataTable;

    }

    $('#project-sessions').find('section.list').empty().append(spawnDataList('sessions').get());

    var projectImagingData$ = $('#project-imaging-data');

    forEach(['scan_formats', 'scan_types', 'resources', 'reconstructions', 'assessors'],
        function(dataType){
            projectImagingData$.find('section.' + toDashed(dataType) + '.list')
                               .each(function(){
                                   if (ITEMS[dataType].list && ITEMS[dataType].list.length) {
                                       $(this).empty().append(spawnDataList(dataType).table);
                                   }
                                   else {
                                       $(this).find('> .none').hidden(false);
                                   }
                               });
        }
    );
    function formatBytes(a,b){if(0==a)return"0 Bytes";var c=1024,d=b||2,e=["Bytes","KB","MB","GB","TB","PB","EB","ZB","YB"],f=Math.floor(Math.log(a)/Math.log(c));return parseFloat((a/Math.pow(c,f)).toFixed(d))+" "+e[f]}

    // future high-speed selector syntax with lightweight event handling
    // XNAT.dom('#/download-form').on('submit', function(e){
    //     e.preventDefault();
    // });

    // wait for the DOM to load before attaching the form handler
    // (in case there's another conflicting handler, which will be removed)
    $(function(){

        // the 'off' method should clear out any existing event handlers
        $('#download-form').off('submit').on('submit', function(e){
            e.preventDefault();
            e.stopImmediatePropagation();
            var $form = $(this).removeClass('invalid');
            var type = $form.find('.download-type:checked').val();
            var getZip = (type === 'zip');
            var msg = [];
            var preparingDownload = XNAT.dialog.open({
                width: 400,
                header: false,
                content: spawn('div.message', 'Preparing download. Please wait...'),
                footer: false
            });
            $form.find('[name="XNAT_CSRF"]').remove();
            $form.submitJSON({
                // dataType: 'text/plain',
                stringValues: ['projectIds[]'],
                validate: function(){
                    var errorMsg = '';
                    var sessionSelected, countChecked;
                    sessionSelected = $form.find('input.select-sessions:checked').length;
                    if (!sessionSelected) {
                        errorMsg = 'You must select at least one session.';
                    }
                    else {
                        countChecked = $form.find('input.select-item:checked').not('.select-sessions').length;
                        if (!countChecked) {
                            errorMsg = 'You must select at least one item to download.';
                        }
                    }
                    if (errorMsg) {
                        XNAT.ui.dialog.message({
                            title: false,
                            content: '' +
                            '<div class="warning" style="font-size:14px;">' +
                            errorMsg +
                            '</div>'
                        });
                        preparingDownload.fadeOut(100, function(){
                            this.destroy();
                        });
                        return false;
                    }
                    return true;
                },
                failure: function(){
                    // var i = -1;
                    // while (++i < arguments.length){
                    //     console.log(arguments[i]);
                    // }
                },
                complete: function(data){

                    preparingDownload.fadeOut(100, function(){
                        this.destroy();
                    });
                    var downJSON = JSON.parse(data.responseText);
                    var ID = downJSON.id;
                    var SIZE = downJSON.size;
                    var UNKNOWN_SIZE_COUNT = downJSON.resourcesOfUnknownSize;
                    var URL = XNAT.url.rootUrl('/xapi/archive/download/' + ID + '/' + (getZip ? 'zip' : 'xml'));
                    var buttons = [];
                    if (getZip) {
                        msg.push('' +
                            'Click "Download ZIP" to start a zip download. ' +
                            'After the download begins, you may navigate away from this page &ndash; the download will continue in the background.<br><br>');
                        buttons.push(
                            {
                                label: 'Download ZIP',
                                isDefault: true,
                                close: true,
                                action: function(){
                                    // *DON'T* tickle the server every minute to keep the session alive
                                    // window.setInterval(XNAT.app.timer.touch, 60*1000);
                                    downloadFileInPage(URL);
                                }
                            }
                        )
                    }
                    else if (type === 'client') {
                        msg.push('Click "Download Via App" to open the XNAT Desktop Client and begin downloading your files to a specified location on your local system.');
                        msg.push('The download will happen behind the scenes, and you will not need to stay logged into XNAT in your browser.<br><br>');
                        msg.push('<div class="warning">The XNAT Desktop Client must be installed to handle the "xnat:" download protocol.</div><br>');
                        buttons.push(
                            {
                                label: 'Download Via App',
                                isDefault: true,
                                close: false,
                                action: function(obj){
                                    // get an alias token and send the user to the download protocol
                                    XNAT.xhr.get({
                                        url: XNAT.url.rootUrl('/data/services/tokens/issue'),
                                        fail: function(e){
                                            console.log(e);
                                            XNAT.ui.dialog.message({
                                                title: 'Unexpected error',
                                                content: 'Could not issue user token for download application.'
                                            });
                                        },
                                        success: function(data){
                                            var token = (!isObject(data)) ? JSON.parse(data) : data;
                                            var url = XNAT.url.xnatUrl('/download/'+ID+'.xml?a='+token.alias+'&s='+token.secret);
                                            window.location.assign(url);
                                            XNAT.ui.dialog.closeAll();
                                        }
                                    })
                                }
                            }
                        )
                    }
                    else {
                        msg.push('Click "Download XML" to download the catalog XML.<br><br>');
                        buttons.push(
                            {
                                label: 'Download XML',
                                isDefault: true,
                                close: true,
                                action: function(){
                                    downloadFileInPage(URL);
                                }
                            }
                        )
                    }
                    if(UNKNOWN_SIZE_COUNT>0) {
                        msg.push('The download id is <b>' + ID + '</b>. Of the resources you are trying to download, <b>' + UNKNOWN_SIZE_COUNT + '</b> '+((UNKNOWN_SIZE_COUNT==1)? 'has':'have')+' unknown size. The total size of the rest of the files to be downloaded (before compression) is <b>' + formatBytes(SIZE) + '</b>.');
                    }
                    else {
                        msg.push('The download id is <b>' + ID + '</b>. The total size of the files to be downloaded (before compression) is <b>' + formatBytes(SIZE) + '</b>.');
                    }

                    buttons.push({
                        label: 'Cancel',
                        close: true
                    });

                    XNAT.ui.dialog.open({
                        title: false,
                        content: msg.join(' '),
                        width: 450,
                        buttons: buttons
                    });
                }
            });
            return false;
        });
    });

    // this script has loaded
    projectDownload.loaded = true;

    return XNAT.app.projectDownload = projectDownload;

}));

