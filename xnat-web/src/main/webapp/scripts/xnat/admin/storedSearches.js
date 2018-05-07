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

    storedSearches.ssTable = function(){
        // initialize the table;
        var ssTable = XNAT.table({
            addClass: 'xnat-table compact',
            style: {
                'width': '100%',
                'margin-top': '15px',
                'margin-bottom': '15px'
            }
        });

        // add table header row
        ssTable.tr()
            .th({ addClass: 'left' }, '<b>ID</b>')
            .th('<b>Label</b>')
            .th('<b>Description</b>')
            .th('<b>Root Data Type</b>')
            .th('<b>Users</b>')
            .th('<b>Actions</b>');

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

        storedSearches.getStoredSearches().done(function(data){
            var searches = data.ResultSet.Result;
            if (searches.length) {
                searches.sort(function(a,b){ return (a.id > b.id) ? 1 : -1 })
                searches.forEach(function(search){

                    storedSearches.getStoredSearch(search.id).done(function(searchData){
                        var searchJson = parseSearch(searchData);

                        ssTable.tr({ data: { id: search.id }})
                            .td({ addClass: 'primary-link' }, [ editLink(search.id) ])
                            .td(escapeHtml( search['brief_description'] ))
                            .td(escapeHtml( search.description ))
                            .td(escapeHtml( search['root_element_name'] ))
                            .td({ addClass: 'right' },[ showUserCount(searchJson['allowed_user']) ])
                            .td({ addClass: 'center nowrap' },[
                                editLink(search.id, [ spawn('button.btn2.btn-sm','Edit') ]),
                                spacer(10),
                                viewLink(search.id, [ spawn('button.btn2.btn-sm','View') ])
                            ]);
                    });

                });
            }
            else {
                ssTable.tr()
                    .td({colSpan: 5}, 'No stored searches to display');
            }
        });

        storedSearches.$table = $(ssTable.table);

        return ssTable.table;
    };

    $(document).on('click','a.popup', function(event){
        event.preventDefault();
        XNAT.dialog.iframe(this.href, 'Edit Stored Search', 900, 600, { onClose: function(){ XNAT.admin.storedSearches.refresh() }});
    });
    
    storedSearches.init = storedSearches.refresh = function(container){
        container = $(container || '#stored-searches-container');

        container.empty().append(storedSearches.ssTable());
    };

    return XNAT.admin.storedSearches = XNAT.storedSearches = storedSearches;

}));
