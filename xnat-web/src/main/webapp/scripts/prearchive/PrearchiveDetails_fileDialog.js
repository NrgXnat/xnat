XNAT.app.fileDialog = XNAT.app.fileDialog || {};

XNAT.app.fileDialog.loadScan = function( url, title ){

    xmodal.closeAll(); // get rid of any lingering modals

    xmodal.loading.open();

    var modalOpts={};
    modalOpts.width = 900;
    modalOpts.height = 600;
    modalOpts.maximize = true;
    modalOpts.buttons = {
        close: {
            label: 'Close',
            //close: true, // naming this button 'close' adds the 'close' class
            isDefault: true
        }
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