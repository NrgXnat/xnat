/*
 * web: CreateClassMapping.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2018, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

// Dynamically loads 'generated' JavaScript files by name

function ClassMapping(){

    this.newInstance = function(name) {

        XNAT.data.dataTypes = XNAT.data.datatypes =
            getObject(firstDefined(XNAT.data.dataTypes, XNAT.data.datatypes, {}));

        var undef;

        var namesList = XNAT.data.dataTypes.namesList;

        // store map of possible name options to the js file name base
        var nameMap = getObject(XNAT.data.dataTypes.nameMap || {});

        // special awesome hack for lovely custom variables
        if (/^xnat[:_]FieldDefinitionSet$/i.test(name)) {
            nameMap[name] = 'xnat_fieldDefinitionGroup';
        }
        else {
            if (namesList === undef) {
                XNAT.xhr.get({
                    url: XNAT.url.restUrl('/xapi/schemas/datatypes/names/all'),
                    async: false,
                    success: function(json){
                        XNAT.data['/xapi/schemas/datatypes/names/all'] = json;
                        namesList = json;
                    }
                });
            }
            // save the timestamp...
            XNAT.data.dataTypes.modified = XNAT.data.dataTypes.modified || namesList.timestamp;
            // ...then delete it
            delete namesList.timestamp;
            // iterate names list to map to js/sql name
            forOwn(namesList, function(jsName, names){
                forEach(names, function(key){
                    nameMap[key] = jsName;
                });
            });
        }

        var jsName = nameMap[name];
        var Fn = function(){};

        if (jsName !== undef) {
            if (window[jsName] === undef) {
                dynamicJSLoad(jsName, 'generated/' + jsName + '.js');
            }
            Fn = window[jsName];
        }

        debugLog(nameMap);
        debugLog(jsName);

        XNAT.data.dataTypes.namesList = namesList;
        XNAT.data.dataTypes.nameMap = nameMap;

        return new Fn();

    }
}
