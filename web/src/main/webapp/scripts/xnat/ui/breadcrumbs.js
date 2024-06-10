/*
 * web: breadcrumbs.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Breadcrumb navigation
 */

var XNAT = getObject(XNAT||{});

(function(XNAT){

    var breadcrumbs;

    XNAT.ui = getObject(XNAT.ui||{});
    XNAT.ui.breadcrumbs = breadcrumbs = getObject(XNAT.ui.breadcrumbs||{});

    breadcrumbs.render = function(parent, crumbArray){

        // maybe don't even render a breadcrumb for the project page?
        if (window.isProjectPage){
            return false;
        }

        var output = '',
            len = crumbArray.length,
            i = -1,
            crumb = {},
            html = '',
            last = false;

        while (++i < len){

            crumb = crumbArray[i];

            html = '<span class="crumb">';

            html += '<a href="' + (crumb.link || '#') + '"';

            if (i === len-1){
                last = true;
                html += ' class="last"'
            }

            if (crumb.id){
                html += ' id="breadcrumb-' + crumb.id + '"';
                html += ' data-id="' + crumb.id + '"';
            }

            if (crumb.type && !last){
                html += ' data-type="' + crumb.type + '"';
                html += '>' + crumb.type + ': ';
            }
            else {
                html += '>';
            }

            html += (crumb.label || '');
            html += '</a>';

            if (i < len-1){
                html += '&nbsp; &gt; &nbsp;';
            }

            html += '</span>';

            output += html;
        }

        $(parent).html(output);

    };

})(XNAT);
