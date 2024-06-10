/*
 * web: storedSearches.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

console.log('storedSearches.js');

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

    /* ================ *
     * GLOBAL FUNCTIONS *
     * ================ */

    function spacer(width){
        return spawn('i.spacer', {
            style: {
                display: 'inline-block',
                width: width + 'px'
            }
        })
    }

    function errorHandler(e, title, closeAll){
        console.log(e);
        title = (title) ? 'Error Found: '+ title : 'Error';
        closeAll = (closeAll === undefined) ? true : closeAll;
        var errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + ': '+ e.statusText+'</strong></p><p>' + e.responseText + '</p>' : e;
        XNAT.dialog.open({
            width: 450,
            title: title,
            content: errormsg,
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    action: function(){
                        if (closeAll) {
                            xmodal.closeAll();

                        }
                    }
                }
            ]
        });
    }

    var rootUrl = XNAT.url.rootUrl;
    var restUrl = XNAT.url.restUrl;
    var csrfUrl = XNAT.url.csrfUrl;

    /* ====================== *
     * Site Admin UI Controls *
     * ====================== */

    var undefined, storedSearches;

    var userTableContainer = 'div#ss-table-container';

    XNAT.admin = getObject(XNAT.admin || {});

    XNAT.admin.storedSearches = storedSearches =
        getObject(XNAT.admin.storedSearches || {});

    function getStoredSearchListUrl(){
        return restUrl('/data/search/saved', ['all=true', 'format=json']);
    }
    function getStoredSearchUrl(id){
        if (!id) {
            console.log('No id, cannot retrieve search');
            return false;
        }
        return csrfUrl('/data/search/saved/'+id);
    }

    storedSearches.getStoredSearches = function(callback){
        callback = isFunction(callback) ? callback : function(){};
        return XNAT.xhr.getJSON({
            url: getStoredSearchListUrl(),
            success: function(data){
                if (data) {
                    return data;
                } else {
                    errorHandler(data,'Stored search list is empty');
                }
                callback.apply(this, arguments);
            },
            fail: function(e){
                errorHandler(e,'Could not retrieve list of stored searches');
            }
        })
    };

    storedSearches.getStoredSearch = function(id,callback){
        if (!id) {
            errorHandler({},'Could not retrieve stored search');
            return false;
        }
        callback = (isFunction(callback)) ? callback : function(){};
        return XNAT.xhr.get({
            url: getStoredSearchUrl(id),
            success: function(data){
                callback.apply(this, arguments);
                return data;
            },
            fail: function(e){
                console.error(e);
            }
        })
    };

    /* --------------------------- *
     * Stored Search Display Table *
     * --------------------------- */

    var ssTable = function(searches){

        // sort search list
        searches = searches.sort(function(a,b){ return a['id'].toLowerCase().localeCompare(b['id'].toLowerCase()) });

        function parseUsers(userList){
            // user list is returned as a string formatted as "{username,username}" or "{NULL}" if no users have been defined in the stored search.
            // parse this list and return either an array or a FALSE state
            if (userList.length < 2) return false;

            userList = userList.substr(1,userList.length-2).split(",");
            if (userList.length && userList[0] !== "NULL") {
                return userList;
            }
            else {
                return false;
            }
        }
        function userCount(userList){
            var users = parseUsers(userList), numToDisplay;
            if (users) {
                numToDisplay = users.length;
            }
            else {
                numToDisplay = "0";
            }

            return spawn('!',[
                // HACK: force numeric sorting by generating hidden values with leading zeros
                spawn('i.hidden.sorting',zeroPad(numToDisplay,6,'0')),
                spawn('span',numToDisplay)
            ]);
        }

        function editLink(id, content){
            content = content || id;
            return spawn('a',{
                href: rootUrl('/app/action/DisplayItemAction/search_value/'+id+'/search_element/xdat:stored_search/search_field/xdat:stored_search.ID/popup/true'),
                className: 'popup'
            }, content);
        }
        function viewLink(id,userList){
            var users = parseUsers(userList), buttonStyle = ".btn2.btn-sm.ss-view-button", buttonProp = {};
            var url = '#!';
            if (users && users.indexOf(window.username) >= 0) {
                url = rootUrl('/app/template/Search.vm/node/ss.'+id);
            }
            else {
                buttonStyle += '.disabled';
                buttonProp = {
                    disabled: true,
                    title: 'To view this stored search, you will need to grant yourself access in the "Allowed User" definition of the search.'
                };
            }
            return spawn('a',{
                href: url,
            }, [ spawn('button'+buttonStyle,buttonProp,'View') ]);
        }

        return {
            kind: 'table.dataTable',
            name: 'adminSSlist',
            id: 'adminSSlist',
            data: searches,
            table: { },
            before: {
                filterCss: {
                    tag: 'style|type=text/css',
                    content: '\n' +
                        'td[class*="break-word-"] { max-width: 150px; word-wrap: break-word; } \n' +
                        'td.align-top { vertical-align: top } \n'
                }
            },
            trs: function(tr, data){
                tr.id = "tr-" + data.id;
                addDataAttrs(tr, { filter: '0', data: data.id })
            },
            sortable: 'id, brief_description, description, root_element_name, USERS',
            items: {
                id: {
                    label: 'ID',
                    filter: false,
                    td: { className: 'id break-word-id align-top' },
                    apply: function(){
                        return spawn('b', [editLink(this.id, this.id)])
                    }
                },
                brief_description: {
                    label: 'Label',
                    filter: true,
                    td: { className: 'brief_description break-word-label align-top' },
                    apply: function(){
                        return escapeHtml(this['brief_description'])
                    }
                },
                description: {
                    label: 'Description',
                    filter: true,
                    td: { className: 'description break-word-desc align-top' },
                    apply: function(){
                        return escapeHtml(this['description'])
                    }
                },
                root_element_name: {
                    label: 'Root Data Type',
                    className: 'root_element_name align-top',
                    filter: true
                },
                USERS: {
                    label: 'Users',
                    filter: false,
                    className: 'USERS right allowed-users align-top',
                    apply: function(){
                        return userCount(this['users'])
                    }
                },
                ACTIONS: {
                    label: 'Actions',
                    className: 'ACTIONS center nowrap',
                    apply: function(){
                        return spawn('!', [
                            editLink(this.id, [spawn('button.btn2.btn-sm', 'Edit')]),
                            spacer(10),
                            viewLink(this.id, this['users'])
                        ])
                    }
                }
            }
        }
    };

    $(document).on('click','a.popup', function(event){
        event.preventDefault();
        XNAT.dialog.iframe(this.href, 'Edit Stored Search', 900, 600, { onClose: function(){ XNAT.admin.storedSearches.refresh() }});
    });

    storedSearches.init = storedSearches.refresh = function(container){
        var _ssTable;
        container = $(container || '#stored-searches-container');

        storedSearches.getStoredSearches().done(function(data){
            var searches = data.ResultSet.Result;
            if (searches.length) {

                _ssTable = XNAT.spawner.spawn({
                    historyTable: ssTable(searches)
                });
                _ssTable.done(function(){
                    container.empty();
                    this.render(container);
                });

            }
            else {
                container.empty().append(spawn('div.message', 'No stored searches to display'));
            }
        });

    };

    return XNAT.admin.storedSearches = XNAT.storedSearches = storedSearches;

}));
