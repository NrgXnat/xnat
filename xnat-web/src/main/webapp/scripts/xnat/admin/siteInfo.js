/*
 * web: siteInfo.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

// interractions with 'Site Info' section of admin ui

console.log('siteInfo.js');

(function(){

    var $container = $('[data-name="siteDescriptionType"]');
    var $bundles = $container.find('div.input-bundle');

    $container.find('input[name="siteDescriptionType"]').on('change', function(){
        changeSiteDescriptionType(this.value);
    });

    changeSiteDescriptionType(XNAT.data.siteConfig.siteDescriptionType);

    function changeSiteDescriptionType(value){

        value = (value || 'page').toLowerCase();

        $bundles.hide();
        $bundles.filter('.' + value).show();

    }

    // XNAT-5533 remove trailing slash(es) from siteUrl, if it was added
    $(document).on('change', 'input[name="siteUrl"]', function(){
        var siteUrl = (this.value + '').trim();
        var endSlash = /\/+$/;
        if (endSlash.test(siteUrl)) {
            this.value = siteUrl.replace(endSlash,'');
            XNAT.ui.banner.top(2000, 'Site URL should not have trailing slashes', 'info');
        }
    });

})();
