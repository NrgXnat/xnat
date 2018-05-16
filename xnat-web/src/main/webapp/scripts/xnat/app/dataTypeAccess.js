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
    var displayItems = [];
    var displayName = 'browseable';
    var SITE_ROOT = XNAT.url.rootUrl();
    var USERNAME = window.username || 'xnat';

    XNAT.app =
        getObject(XNAT.app || {});

    XNAT.app.dataTypeAccess = dataTypeAccess =
        getObject(XNAT.app.dataTypeAccess || {});

    dataTypeAccess.isReady = false;

    // save the url with the site context prepended using XNAT.url.rootUrl()
    dataTypeAccess.url = XNAT.url.rootUrl('/xapi/access/displays');

    // setup a url for /xapi/access/displays requests
    function dataTypeAccessUrl(part){
        return dataTypeAccess.url + (part ? ('/' + part) : '')
    }

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

    // by default, don't update the datatypes
    dataTypeAccess.needsUpdate = false;

    // use the existing default 'userData' storage instance
    var userData = XNAT.storage.userData;

    // retrieve the *stored* value for last modified for comparison
    var modifiedValue = userData.data['accessDisplaysModified'];

    // if there's no currently stored 'modified' value...
    // ...or if the stored value is different than the current value...
    // ...update the 'modified' value in the data store
    if (!modifiedValue || modifiedValue !== dataTypeAccess.modified) {
        modifiedValue = dataTypeAccess.modified;
        dataTypeAccess.needsUpdate = true;
    }

    // always save the 'modified' value in the browser's localStorage
    userData.setValue('accessDisplaysModified', modifiedValue);

    // force an update by adding ?updateAccess=true to the URL query string
    dataTypeAccess.needsUpdate = /true|all/i.test(getQueryStringValue('updateAccess')) || dataTypeAccess.needsUpdate;

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
    userData.setValue('accessDisplaysList', dataTypeAccess.displays);

    // save existing values or an empty object to 'accessDisplays'
    userData.setValue('accessDisplays', userData.data.accessDisplays || {});

    // initialize the 'loading' dialog...
    // var cacheLoadingMessage = XNAT.dialog.init({
    //     width: 300,
    //     // title: 'Please wait...',
    //     header: false,
    //     padding: 0,
    //     top: '80px',
    //     footer: false,
    //     mask: false,
    //     content: '<div class="message waiting md">&nbsp; Refreshing data type cache...</div>'
    // });
    // ...and open it (maybe)
    // window.setTimeout(function(){
    //     cacheLoadingMessage.open();
    // }, 300);

    dataTypeAccess.reqCount = 0;

    var getFreshData = dataTypeAccess.needsUpdate || false;

    // force reloading of display elements
    dataTypeAccess.getElements = function(type, opts){
        // return existing function if it already exists
        if (isFunction(dataTypeAccess.getElements[type])) {
            console.log("exists: datatTypeAccess.getElements['" + type + "']");
            return dataTypeAccess.getElements[type];
        }
        var accessTypeKey = 'accessDisplays.' + type;
        getFreshData = getFreshData || userData.getValue(accessTypeKey) === undef;
        var getElementDisplays = function(){};
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
                    var elements = [];
                    var elementNames = [];
                    forEach(datatypes, function(element){
                        // map to old names for compatibility
                        element.elementName =
                            element.element_name =
                                element.elementName;
                        // only add unique elements
                        if (elementNames.indexOf(element.elementName) === -1) {
                            element.plural =
                                element.plural ||
                                element.singular ||
                                element.properName ||
                                element.elementName.split(':')[1] ||
                                element.elementName;
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
                    userData.setValue(accessTypeKey, sortObjects(elements, 'plural'));
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
            ready: function(success, failure){
                if (getFreshData) {
                    getElementDisplays.done(success || diddly);
                    getElementDisplays.fail(failure || diddly);
                }
                else {
                    dataTypeAccess.reqCount++;
                    try {
                        if (isFunction(success)) {
                            success(userData.getValue(accessTypeKey));
                        }
                    }
                    catch(e) {
                        if (isFunction(failure)) {
                            failure(e);
                        }
                        console.warn(e)
                    }
                }
                return this;
            }
        }
    };

    // only load data types on non-login-type pages
    if (!window.isLoginPage && !/(Login\.vm|Register\.vm|VerifyEmail\.vm|XDATRegisterUser)/.test(window.location.href)) {
        // this will be called for each item in the 'displays' array
        forEach(dataTypeAccess.displays, function(type, i){
            dataTypeAccess.getElements[type] = dataTypeAccess.getElements[type] || dataTypeAccess.getElements(type).ready(function(){
                console.log('ready: ' + type);
                if (dataTypeAccess.displays[dataTypeAccess.displays.length - 1] === type) {
                    // poll every 10ms to check for completion of all data retrieval
                    waitForIt(
                        10,
                        function(){
                            console.log('refresh: ' + getFreshData);
                            return !getFreshData || dataTypeAccess.reqCount === dataTypeAccess.displays.length;
                        },
                        function(){
                            console.log('ALL LOADED');
                            if (getFreshData) {
                                // cacheLoadingMessage.destroy();
                                // window.setTimeout(function(){
                                //     window.location.reload(true);
                                // }, 500);
                            }
                            else {
                                // cacheLoadingMessage.destroy();
                            }
                        }
                    );
                }
            });
        });
    }

    return (XNAT.app.dataTypeAccess = dataTypeAccess);

}));
