XNAT.app.headerDialog = XNAT.app.headerDialog || {};

XNAT.app.headerDialog.load = function( url, title ){

    //openModalPanel("resource_loading","Loading");
    xmodal.loading.open();

    //var csvURL = url.split('format=html')[0] + 'format=csv';

    var modalOpts={};
    modalOpts.width = 700;
    modalOpts.height = 500;
    modalOpts.maximize = true;
    modalOpts.buttons = {
        close: {
            label: 'Close',
            close: true,
            isDefault: true
        }//,
        // letting this linger in case it can be used in the future
//        csv: {
//            label: 'Download CSV',
//            close: false,
//            //link: true, // this would make it look like a regular link
//            action: function(){
//                window.location.href = csvURL;
//            }
//        }
    };
    modalOpts.beforeShow = function(){
        xmodal.loading.close();
    };

    var getData = $.ajax({
        type: 'GET',
        url: url,
        cache: false ,
        dataType: 'html'
    });

    getData.done(function(data){
        modalOpts.title = title;
        modalOpts.content = data;
        xmodal.open(modalOpts);
    });

    getData.fail(function(jqXHR, textStatus, errorThrown){
        modalOpts.title = 'Error - ' + title;
        modalOpts.content = 'Error: ' + textStatus;
        xmodal.open(modalOpts);
    });

};
