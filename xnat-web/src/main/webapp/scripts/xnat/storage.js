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
        var siteRoot = XNAT.url.rootUrl().replace(/^\/*|\/*$/g, '');
        name = name || USERNAME;
        return (siteRoot && siteRoot !== '/') ? (siteRoot + '+' + name) : name;
    }

    // set the root property name to use for browsers' localStorage
    storage.setName = function(name){
        return (storage.name = storage.dataStore = dataStoreName(name));
    };

    function dataStoreNameEnc(name){
        return XNAT.util.sub64.dlxEnc(dataStoreName(name)).encoded;
    }

    storage.setNameEnc = function(name){
        storage.nameEnc = dataStoreNameEnc(name);
        return storage.setName(storage.nameEnc);
    };

    storage.getName = function(){
        return storage.name;
    };

    function dataStoreNameDec(name){
        return XNAT.util.sub64.dlxDec(name || storage.nameEnc).decoded;
    }

    storage.getNameDec = function(name){
        return dataStoreNameDec(name);
    };

    function getDescendantProp(obj, desc) {
        var arr = desc.split('.');
        var part;
        while (arr.length) {
            part = arr.shift();
            obj = obj.hasOwnProperty(part) ? obj[part] : {};
        }
        // return undefined if object is empty (no stored value)
        return isEmptyObject(obj) ? undef : obj;
    }

    function setDescendantProp(obj, desc, value) {
        var arr = desc.split('.');
        var tmp, prop;
        while (arr.length > 1) {
            tmp = arr.shift();
            if (obj[tmp] === undef) { obj[tmp] = {} }
            obj = obj[tmp];
        }
        prop = arr[0];
        // set [value] to '@DELETE' or '{DELETE}' to delete the item
        if (/^([@{]DELETE[}]*)$/i.test(value)) {
            try {
                delete obj[prop];
            }
            catch (e) {
                console.error(e);
            }
            return null;
        }
        else {
            return (obj[prop] = value);
        }
    }


    /**
     *
     * @param [dataStore]
     * @param [key]
     * @param [data]
     * @returns {BrowserStorage}
     * @constructor BrowserStorage
     */
    function BrowserStorage(dataStore, key, data){
        if (dataStore instanceof BrowserStorage) {
            return dataStore;
        }
        this.dataStore = dataStore || storage.dataStore || storage.setNameEnc();
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
     * @param {*} [data] - data object or string to save
     * @returns {BrowserStorage}
     */
    BrowserStorage.fn.save = function(data){
        var DATA = data && isString(data) ? JSON.parse(data) : data || null;
        // this.data = DATA || this.data || this.getAll() || '';
        // do we need to use extend() to prevent overwriting of values?
        // probably.
        this.data = extend(true, {}, this.getAll(), this.data, DATA);
        localStorage.setItem(this.dataStore, JSON.stringify(this.data));
        return this;
    };


    /**
     * Get the whole 'data' object
     * @returns {*}
     */
    BrowserStorage.fn.getData = function(){
        this.getAll();
        return !isEmpty(this.data) ? this.data : undef;
    };


    /**
     * Get the value of a specific property
     * @param {String} objPath - string representing path to object property
     * @returns {*}
     */
    BrowserStorage.fn.getValue = function(objPath){
        // tolerate root-level properties
        if (this.data.hasOwnProperty(objPath)) {
            return this.data[objPath];
        }
        else {
            return getDescendantProp(this.getAll(), objPath);
        }
    };


    /**
     * Set a new value for an item and save it back to localStorage
     * @param objPath
     * @param newValue
     * @returns {BrowserStorage}
     * @example xnatStorage.setValue('foo.bar.baz', 'abc-xyz')
     */
    BrowserStorage.fn.setValue = function(objPath, newValue){
        // calling #getAll() sets the value of #data from the localStorage datastore
        this.getAll();
        // are we deleting a property entirely?
        var doDelete = /@DELETE/i.test(newValue);
        // tolerate root-level properties
        if (objPath.indexOf('.') === -1) {
            if (doDelete) {
                delete this.data[objPath];
            }
            else {
                this.data[objPath] = newValue;
            }
        }
        else {
            setDescendantProp(this.data, objPath, newValue);
        }
        localStorage.setItem(this.dataStore, JSON.stringify(this.data));
        return this;
    };


    /**
     * Delete the value at the specified path
     * @param {String|Array} objPath(s) - a single path or an array of paths to delete
     * @returns {*}
     */
    BrowserStorage.fn['delete'] = function(objPath){
        [].concat(objPath).forEach(function(path){
            this.setValue(path, '@DELETE');
        }, this);
        return this;
    };
    BrowserStorage.fn.remove = BrowserStorage.fn['delete'];


    /**
     * Reset/replace ALL data for this storage instance
     * @param {*} [data] - optional replacement data (replaces ALL data in the storage instance)
     * @returns {*}
     */
    BrowserStorage.fn.reset = function(data){
        this.data = data || '';
        localStorage.setItem(this.dataStore, JSON.stringify(this.data));
        return this;
    };


    /**
     * Initialize an XNAT.storage instance with a specified 'dataStore'
     * @param {String} [dataStore] - name of localStorage item
     * @param {String} [rootKey] - name of localStorage key
     * @param {*} [data] - initial data to store
     */
    storage.init = function(dataStore, rootKey, data){
        return (new BrowserStorage(dataStore, rootKey, data)).save();
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

    // initialize a default 'userData' data store
    // XNAT.storage.userData.setValue('foo', 'bar');
    storage.userData = storage.init(storage.setNameEnc());

    // initialize a 'site' data store for site-level storage
    storage.siteData = storage.init('siteData');

    return XNAT.storage = storage;

}));
