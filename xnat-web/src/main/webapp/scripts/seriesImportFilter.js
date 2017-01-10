/*
 * web: seriesImportFilter.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */

/**
 * These functions help to manage passing series import filters to the back-end.
 */
function getSeriesImportFilterData(mode, filters, enabled) {
    if (enabled == null) {
        enabled = true;
    }
    var data = {};
    if (/modalityMap/i.test(mode)) {
        data = jq.extend({ mode: 'modalityMap', enabled: enabled }, getModalityMap(filters));
    } else {
        data["mode"] = mode;
        data["enabled"] = enabled;
        data["list"] = filters;
    }
    return JSON.stringify(data);
}
function getModalityMap(filters) {
    var data = {};
    var modes = filters.match(/[^\r\n]+/g);
    for (var index = 0; index < modes.length; index++) {
        var entry = getKeyAndValue(modes[index]);
        data[entry[0]]= entry[1];
    }
    return data;
}
function getKeyAndValue(source) {
    var atoms = source.split(":");
    if (atoms.length == 2) {
        return [atoms[0], atoms[1].trim()];
    }
    var key = atoms[0];
    var value = "";
    for (var index = 1; index < atoms.length; index++) {
        var atom = atoms[index];
        if (index == 1) {
            atom = atom.trim();
        } else {
            value += ":";
        }
        value += atom;
    }
    return [key, value];
}
