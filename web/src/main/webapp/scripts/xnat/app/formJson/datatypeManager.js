/*
 * Copyright (c) 2021, Radiologics Inc
 * Author: Mohana Ramaratnam (mohana@radiologics.com)
 */

/*!
 * Methods for accessing protocols for XNAT
 */
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


    XNAT.customFormManager.datatypeManager = datatypeManager = getObject(XNAT.customFormManager.datatypeManager || {});
    XNAT.customFormManager.datatypeManager.createableElements = [];
    var createableDataTypeURL = XNAT.url.csrfUrl('xapi/role/displays/createable');

    datatypeManager.init = function() {
        manager = new DataTypeManager();
        return manager;
    }

    function DataTypeManager() {
        XNAT.xhr.get({
            url: createableDataTypeURL,
            dataType: 'json',
            async: false,
            success: function(data) {
                data.forEach(function(item) {
                    if (item['imageAssessor'] === true || item['imageSession'] === true || item['subjectAssessor'] === true || item['elementName'] === "xnat:subjectData" || item['elementName'] === "xnat:projectData" ) {
                        var o = {};
                        o['label'] = item.singular;
                        o['value'] = item.elementName;
                        XNAT.customFormManager.datatypeManager.createableElements.push(o);
                    }
                });
                XNAT.customFormManager.datatypeManager.createableElements.sort(
                    function(a, b){
                        let x = a.label.toLowerCase();
                        let y = b.label.toLowerCase();
                        if (x < y) {return -1;}
                        if (x > y) {return 1;}
                        return 0;
                    }
                );
            }
        });
        return this;
    }

    datatypeManager.getDatatypeByXsiType = function(xsiType) {
        var dataType = {};
        dataType.label = undefined;
        dataType.value = xsiType;
        if (XNAT.customFormManager.datatypeManager.createableElements.length > 0) {
            XNAT.customFormManager.datatypeManager.createableElements.forEach(function(dType) {
                if (dType['value'] === xsiType) {
                    dataType.label = dType.label;
                }
            });
        } else {
            dataType.label = xsiType;
        }
        return dataType;
    }


}));