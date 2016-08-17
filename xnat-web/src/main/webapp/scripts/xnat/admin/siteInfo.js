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

})();
