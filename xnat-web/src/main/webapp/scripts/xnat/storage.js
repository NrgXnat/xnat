/*!
 * Helper methods for working with browser localStorage.
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

    var undef, storage;

    function noop(){}

    XNAT.data =
        getObject(XNAT.data || {});

    XNAT.storage = storage =
        getObject(XNAT.storage || {});

    var USERNAME = window.username || XNAT.data.username || 'xnat';

    // append non-root site context if applicable
    function dataStoreName(name){
        var siteRoot = XNAT.url.rootUrl().replace(/^\//, '');
        name = name || USERNAME;
        return (siteRoot && siteRoot !== '/') ? (name + '@' + siteRoot) : name;
    }

    // set the root property name to use for browsers' localStorage
    storage.setName = function(name){
        return (storage.dataStore = dataStoreName(name));
    };

    function getDescendantProp(obj, desc) {
        var arr = desc.split('.');
        while (arr.length) {
            obj = obj[arr.shift()];
        }
        return obj;
    }

    function setDescendantProp(obj, desc, value) {
        var arr = desc.split('.');
        while (arr.length > 1) {
            obj = obj[arr.shift()];
        }
        // set [value] to '@DELETE' or '{DELETE}' to delete the item
        if (/^([@{]DELETE[}]*)$/i.test(value)) {
            try {
                delete obj[arr[0]];
            }
            catch (e) {
                console.error(e);
            }
            return null;
        }
        else {
            return obj[arr[0]] = value;
        }
    }

    function BrowserStorage(dataStore, key, data){
        if (dataStore instanceof BrowserStorage) {
            return dataStore;
        }
        this.dataStore = dataStore || storage.dataStore || dataStoreName();
        this.key = key || 'data';
        this.data = data || '';
    }

    BrowserStorage.fn = BrowserStorage.prototype;


    /**
     * Get the whole localStorage data object
     * @param {Function} [callback]
     * @returns {*}
     */
    BrowserStorage.fn.getAll = function(callback){
        this.data = JSON.parse(localStorage.getItem(this.dataStore)) || {};
        try {
            (callback || noop).call(this, this.data);
            return this.data;
        }
        catch(e){
            console.error(e);
            return null;
        }
    };


    /**
     * Save all data to the 'dataStore'
     * @param {*} data - data object or string to save
     * @returns {BrowserStorage}
     */
    BrowserStorage.fn.save = function(data){
        var DATA = data && isString(data) ? JSON.parse(data) : data || '';
        this.data = DATA || this.data || {};
        localStorage.setItem(this.dataStore, JSON.stringify(this.data));
        return this;
    };


    /**
     * Get the value of a specific property
     * @param {String} objPath - string representing path to object property
     * @returns {*}
     */
    BrowserStorage.fn.getValue = function(objPath){
        return getDescendantProp(this.getAll(), objPath);
    };


    /**
     * Set a new value for an item and save it back to localStorage
     * @param objPath
     * @param newValue
     * @returns {BrowserStorage}
     * @example xnatStorage.setValue('foo.bar.baz', 'abc-xyz')
     */
    BrowserStorage.fn.setValue = function(objPath, newValue){
        setDescendantProp(this.getAll(), objPath, newValue);
        this.save(this.data);
        return this;
    };


    BrowserStorage.fn['delete'] = function(objPath){
        this.setValue(objPath, '@DELETE');
        return this;
    };


    /**
     * Initialize an XNAT.storage instance with a specified 'dataStore'
     * @param {String} [dataStore] - name of localStorage item
     * @param {String} [rootKey] - name of localStorage key
     * @param {*} [data] - initial data to store
     */
    storage.init = function(dataStore, rootKey, data){
        return new BrowserStorage(dataStore, rootKey, data);
    };

    storage.getAll = function(dataStore){
        return storage.init(dataStore).getAll();
    };

    storage.getValue = function(dataStore, objPath){
        var tmpStorage = storage.init(dataStore);
        return objPath ? tmpStorage.getValue(objPath) : tmpStorage.getAll();
    };

    storage.setValue = function(dataStore, objPath, value){
        return storage.init(dataStore).setValue(objPath, value).getAll();
    };

    storage['delete'] = function(dataStore, objPath){
        return storage.init(dataStore).delete(objPath).getAll();
    };


    storage.userData = storage.init();

    return XNAT.storage = storage;

}));
