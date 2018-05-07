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
    var csrfUrl = XNAT.url.csrfUrl;

    var x2js = new X2JS();

    /* ====================== *
     * Site Admin UI Controls *
     * ====================== */

    var undefined, storedSearches;

    var userTableContainer = 'div#ss-table-container';

    XNAT.admin = getObject(XNAT.admin || {});

    XNAT.admin.storedSearches = storedSearches =
        getObject(XNAT.admin.storedSearches || {});

    function getStoredSearchListUrl(){
        return rootUrl('/data/search/saved?all=true&format=json');
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
                return data;
                callback.apply(this,arguments);
            },
            fail: function(e){
                errorHandler(e,'Could not retrieve stored search XML');
            }
        })
    };

    /* --------------------------- *
     * Stored Search Display Table *
     * --------------------------- */

    var ssTable = function(searches){

        // sort search list
        searches = searches.sort(function(a,b){ return a['id'].toLowerCase().localeCompare(b['id'].toLowerCase()) });

        function editLink(id,content){
            content = content || id; 
            return spawn('a',{
                href: rootUrl('/app/action/DisplayItemAction/search_value/'+id+'/search_element/xdat:stored_search/search_field/xdat:stored_search.ID/popup/true'),
                className: 'popup'
            }, content);
        }
        function viewLink(id,content){
            content = content || id;
            return spawn('a',{
                href: rootUrl('/app/template/Search.vm/node/ss.'+id)
            }, content);
        }

        return {
            kind: 'table.dataTable',
            name: 'adminSSlist',
            id: 'adminSSlist',
            data: searches,
            table: { },
            trs: function(tr, data){
                tr.id = "tr-" + data.id;
                addDataAttrs(tr, { filter: '0', data: data.id })
            },
            sortable: 'id, brief_description, description, root_element_name, USERS',
            items: {
                id: {
                    label: 'ID',
                    filter: false,
                    apply: function(){
                        return spawn('b', [ viewLink(this.id, this.id) ])
                    }
                },
                brief_description: {
                    label: 'Label',
                    filter: true,
                    apply: function(){
                        return escapeHtml(this['brief_description'])
                    }
                },
                description: {
                    label: 'Description',
                    filter: true,
                    apply: function(){
                        return escapeHtml(this['description'])
                    }
                },
                root_element_name: {
                    label: 'Root Data Type',
                    filter: true
                },
                USERS: {
                    label: 'Users',
                    filter: true,
                    td: { className: 'right allowed-users' }
                },
                ACTIONS: {
                    label: 'Actions',
                    td: { className: 'center nowrap' },
                    apply: function(){
                        return spawn('!', [
                            editLink(this.id, [ spawn('button.btn2.btn-sm','Edit') ]),
                            spacer(10),
                            viewLink(this.id, [ spawn('button.btn2.btn-sm','View') ])
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

    function parseSearch(xml){
        var searchXml = (xml.documentElement) ? xml.documentElement : x2js.parseXmlString(xml);
        return x2js.xml2json(searchXml);
    }
    function showUserCount(userObj){
        if (isArray(userObj)) {
            return userObj.length
        }
        else {
            return (userObj.login.toString().length > 0) ? '1' : '0'
        }
    }
    
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

                    searches.forEach(function(search){
                        storedSearches.getStoredSearch(search.id).done(function(searchData){
                            var searchJson = parseSearch(searchData);

                            $(document).find('tr#tr-'+search.id).find('.allowed-users').empty().html( showUserCount(searchJson['allowed_user']) );
                        })
                    })
                });

            }
            else {
                return spawn('p', 'No stored searches to display');
            }
        });

    };

    return XNAT.admin.storedSearches = XNAT.storedSearches = storedSearches;

}));
