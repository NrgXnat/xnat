// interractions with 'Site Info' section of admin ui
(function(){

    var sdtPage, sdtText;

    setTimeout(function(){
        sdtPage = $('#siteDescriptionTypePage');
        sdtText = $('#siteDescriptionTypeText');
        sdtPage.click(changeSiteDescriptionType);
        sdtText.click(changeSiteDescriptionType);
        changeSiteDescriptionType(XNAT.data.siteConfig.siteDescriptionType);
    }, 100);

    function changeSiteDescriptionType(eventOrValue){

        var value = eventOrValue;

        if (typeof eventOrValue === 'object') {
            if (eventOrValue.target.id == "siteDescriptionTypeText") {
                value = 'Text';
            }
            else {
                value = 'Page';
            }
        }

        sdtText.val(value);
        sdtPage.val(value);

        var text = $('div.input-bundle.text');
        var page = $('div.input-bundle.page');

        if (value == 'Text') {
            sdtText.prop('checked', true);
            text.show();
            page.hide();
        }
        else {
            sdtPage.prop('checked', true);
            page.show();
            text.hide();
        }
    }

})();
