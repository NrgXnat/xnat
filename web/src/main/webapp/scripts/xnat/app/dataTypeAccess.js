/*!
 * Get datatype objects depending on access type
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

    var undef, dataTypeAccess;
    var tmp = {};
    var displayItems = [];
    var displayName = 'browseable';
    var SITE_ROOT = XNAT.url.rootUrl();
    var USERNAME = window.username || 'xnat';

    XNAT.app =
        getObject(XNAT.app || {});

    XNAT.app.dataTypeAccess = dataTypeAccess =
        getObject(XNAT.app.dataTypeAccess || {});

    dataTypeAccess.isReady = false;

    // Regex list of pages which should NOT load data types
    dataTypeAccess.skipPages = /Login|Register|VerifyEmail|ForgotLogin|RegisterUser|ModifyPassword|Verification/;

    dataTypeAccess.loadDataTypes = window.loadDataTypes =
        window.loadDataTypes ||
        // (window.isLoginPage !== undef ?  !window.isLoginPage : false) ||  // login page test seems unreliable
        !dataTypeAccess.skipPages.test(window.location.href);

    // setup a url for /xapi/access/displays requests
    function dataTypeAccessUrl(part, cacheParam){
        return XNAT.url.restUrl('/xapi/access/displays' + (part ? ('/' + part) : ''), {}, cacheParam);
    }

    // save the url with the site context prepended using XNAT.url.restUrl()
    dataTypeAccess.url = dataTypeAccessUrl('', false);

    // add above function to the global object
    dataTypeAccess.setUrl = dataTypeAccessUrl;

    // 'cacheLastModified' should be defined in Velocity or JSP before this JS file loads
    // but if it's not, do a synchronous request to set the variable
    if (!firstDefined(XNAT['cacheLastModified'], window['cacheLastModified'], false)) {
        XNAT.xhr.get({
            url: dataTypeAccessUrl('modified'),
            async: false,
            success: function(value){
                XNAT['cacheLastModified'] = window['cacheLastModified'] = value;
            }
        })
    }

    // add the 'modified' value to the XNAT.app.dataTypeAccess object
    dataTypeAccess.modified = XNAT['cacheLastModified'] = XNAT['cacheLastModified'] || window['cacheLastModified'];


    // use the existing default 'userData' storage instance
    var userData = XNAT.storage.userData;

    // retrieve the *stored* value for last modified for comparison
    var modifiedValue = userData.data['accessDisplaysModified'];
    
    var getFreshData = false; 

    // if there's no currently stored 'modified' value...
    // ...or if the stored value is different than the current value...
    // ...update the 'modified' value in the data store
    if (!modifiedValue || modifiedValue !== dataTypeAccess.modified) {
        modifiedValue = dataTypeAccess.modified;
        getFreshData = true;
    }
    //

    // always save the 'modified' value in the browser's localStorage
    userData.setValue('accessDisplaysModified', modifiedValue);

    // save a list of timestamps with page urls for cache updates
    if (window.jsdebug) {

        var modifiedValuesList = (userData.getValue('accessDisplaysModifiedList') || []).slice(0, 8).concat(modifiedValue);
        userData.setValue('accessDisplaysModifiedList', modifiedValuesList);

        // retrieve existing history if available in the browser's local storage
        tmp.oldmod = userData.getValue('accessDisplaysModifiedHistory') || {};
        tmp.newmod = {};
        // get only the last 8 'old' values
        Object.keys(tmp.oldmod).sort().reverse().slice(0, 8).forEach(function(key, i){
            tmp.newmod[key] = tmp.oldmod[key];
        });
        // add this one
        tmp.newmod[modifiedValue + ''] = (new Date(Date.now())).toLocaleString() + ' >> ' + XNAT.sub64.dlxEnc(window.location.pathname || window.location.href).encoded;
        userData.setValue('accessDisplaysModifiedHistory', tmp.newmod);
        tmp = {};
    }
    else {
        // userData.remove([
        //     'accessDisplaysModifiedList',
        //     'accessDisplaysModifiedHistory'
        // ]);
    }

    // force an update by adding ?updateAccess=true to the URL query string
    var updateAccess = getQueryStringValue('updateAccess');
    if ((typeof updateAccess !== undefined) && (updateAccess !== null) ) {
      getFreshData = /true|all/i.test(updateAccess) || getFreshData;
    }


    // list of display types (this doesn't change)
    dataTypeAccess.displays = [
        'browseable',
        'browseableCreateable',
        'createable',
        'searchable',
        'searchableByDesc',
        'searchableByPluralDesc'
    ];

    // save the display type list to the user data store
    userData.setValue('accessDisplayTypes', dataTypeAccess.displays);

    // save existing values or an empty object to 'accessDisplays'
    userData.setValue('accessDisplays', userData.data.accessDisplays || {});

    

    // initialize the 'loading' dialog...
    var cacheLoadingMessage = XNAT.dialog.init({
        width: 300,
        // title: 'Please wait...',
        header: false,
        footer: false,
        mask: false,
        padding: 0,
        top: '80px',
        content: '<div class="message waiting md">&nbsp; Refreshing data type cache...</div>',
        beforeShow: function(){
            // if this is false it should suppress the display of the dialog.
            return getFreshData;
        }
    });

    dataTypeAccess.reqCount = 0;

    var loadingTimer = (function(){
        if (getFreshData && window.loadDataTypes) {
            return window.setTimeout(function(){
                cacheLoadingMessage.fadeIn(100);
            }, 2000);
        }
        else {
            cacheLoadingMessage.destroy();
            return null;
        }
    })();

    // make sure there aren't duplicate data type elements
    function collectDataTypes(datatypes){
        var elements = [];
        var elementNames = [];
        var elementMap = {};
        forEach(datatypes, function(element){
            // map to old names for compatibility
            var elementName =
                element.elementName =
                    element.element_name =
                        element.elementName;
            elementMap[elementName] = element;
            // only add unique elements
            if (elementNames.indexOf(element.elementName) === -1) {
                element.plural =
                    element.plural ||
                    element.singular ||
                    element.properName ||
                    elementName.split(':')[1] ||
                    elementName;
                element.isExperiment = element.experiment;
                element.isSubjectAssessor = element.subjectAssessor;
                element.isImageAssessor = element.imageAssessor;
                element.isImageSession = element.imageSession;
                element.isImageScan = element.imageScan;
                // element.lbg = '#f0f0f0';
                // element.dbg = '#505050';
                elementNames.push(element.elementName);
                elements.push(element);
            }
        });
        elements.getByName = function(name){
            return elementMap[name];
        };
        return {
            elements: elements,
            sortedElements: sortObjects(elements, 'plural'),
            elementNames: elementNames,
            elementMap: elementMap
        }
    }

    function updateDataTypeCache(collected, paths){
        delete collected.elements.getByName;
        delete collected.sortedElements.getByName;
        forEach([].concat(paths), function(pathInfo, i){
            var storageKey = pathInfo[0];
            var dataKey = pathInfo[1];
            userData.setValue(storageKey, collected[dataKey])
        });
        return collected;
    }

    // force reloading of display elements
    dataTypeAccess.getElements = function(type, opts){

        // return existing function if it already exists
        if (isFunction(dataTypeAccess.getElements[type])) {
            console.log("exists: datatTypeAccess.getElements['" + type + "']");
            return dataTypeAccess.getElements[type];
        }

        // localStorage property names
        var accessDisplaysKey = 'accessDisplays.' + type;
        var accessDisplaysMapKey = 'accessDisplaysMap.' + type;

        // refresh if timestamp has changed or if data object doesn't exist yet
        getFreshData = getFreshData || userData.getValue(accessDisplaysKey) === undef;

        function getElementDisplays(){}

        getElementDisplays.done = function(){};
        getElementDisplays.fail = function(){};

        if (getFreshData) {
            getElementDisplays = XNAT.xhr.get(extend({
                url: dataTypeAccessUrl(type),
                dataType: 'json',
                // async: type !== 'browseable',  // get 'browseable' elements synchronously
                async: true,
                // beforeSend: function(){
                //     cacheLoadingMessage.open();
                // },
                success: function(datatypes){
                    var collected = collectDataTypes(datatypes);
                    updateDataTypeCache(collected, [
                        [accessDisplaysKey, 'sortedElements'],
                        [accessDisplaysMapKey, 'elementMap']
                    ]);
                },
                failure: function(){
                    console.warn(arguments);
                },
                always: function(){
                    dataTypeAccess.reqCount++;
                }
            }, opts));
        }
        return {
            ready: function(doneFn, failFn){

                //
                // return existing function if it already exists
                // if (dataTypeAccess.getElements[type] && dataTypeAccess.getElements[type].ready) {
                //     console.log("exists: datatTypeAccess.getElements['" + type + "'].ready");
                //     return dataTypeAccess.getElements[type].ready;
                // }

                dataTypeAccess.reqCount++;

                try {

                    var dataTypeListCache = userData.getValue(accessDisplaysKey);

                    // go ahead and execute `doneFn` if there's data in the browser's localStorage
                    if (dataTypeListCache) {
                        try {
                            if (isFunction(doneFn)) {
                                doneFn.call(this, collectDataTypes(dataTypeListCache));
                            }
                        }
                        catch(e) {
                            if (isFunction(failFn)) {
                                failFn.call(this, e);
                            }
                            console.warn(e)
                        }
                    }

                    // we'll run `doneFn` *AGAIN* if we need to get fresh data
                    if (getFreshData) {
                        if (isFunction(doneFn)) {
                            getElementDisplays.done(function(datatypes){
                                var collected = collectDataTypes(datatypes);
                                updateDataTypeCache(collected, [
                                    [accessDisplaysKey, 'sortedElements'],
                                    [accessDisplaysMapKey, 'elementMap']
                                ]);
                                doneFn.call(this, collected);
                            });
                        }
                        if (isFunction(failFn)) {
                            getElementDisplays.fail(function(){
                                failFn.apply(this, arguments);
                            });
                        }
                    }

                }
                catch(e) {
                    console.warn(e);
                }

                return this;
            }
        }
    };

    // only load data types on non-login-type pages
    if (!window.isLoginPage && window.loadDataTypes) {
        // this will be called for each item in the 'displays' array
        forEach(dataTypeAccess.displays, function(type, i){
            dataTypeAccess.getElements[type] = dataTypeAccess.getElements[type] || dataTypeAccess.getElements(type).ready(function(){
                console.log('ready: ' + type);
                if (dataTypeAccess.displays[dataTypeAccess.displays.length - 1] === type) {
                    // poll every 5 ms to check for completion of all data retrieval
                    waitForIt(
                        5,
                        function(){
                            console.log('refresh: ' + getFreshData);
                            return !getFreshData || dataTypeAccess.reqCount >= (dataTypeAccess.displays.length + 1);
                        },
                        function(){
                            console.log('ALL LOADED');
                            if (loadingTimer) {
                                window.clearTimeout(loadingTimer);
                                cacheLoadingMessage.dialog$.fadeOut(100, function(){
                                    cacheLoadingMessage.destroy();
                                });
                            }
                            // if (getFreshData) {
                            //     window.setTimeout(function(){
                            //         cacheLoadingMessage.dialog$.fadeOut(50, function(){
                            //             cacheLoadingMessage.destroy()
                            //         });
                            //         // window.location.reload(true);
                            //     }, 10);
                            // }
                            // else {
                            //     // make sure the loading dialog closes
                            //     window.setTimeout(function(){
                            //         cacheLoadingMessage.destroy()
                            //     }, 10);
                            // }
                        }
                    );
                }
            });
        });
    }

    return (XNAT.app.dataTypeAccess = dataTypeAccess);

}));
