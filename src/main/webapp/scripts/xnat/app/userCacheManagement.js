console.log('xnat/admin/userCacheManagement.js');

var XNAT = getObject(XNAT || {});

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
    /* ================ *
     * GLOBAL FUNCTIONS *
     * ================ */

    function spacer(width){
        return spawn('i.spacer', {
            style: {
                display: 'inline-block',
                width: width + 'px'
            }
        })
    }

    function errorHandler(e, title, closeAll){
        console.log(e);
        title = (title) ? 'Error Found: '+ title : 'Error';
        closeAll = (closeAll === undefined) ? true : closeAll;
        var errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + ': '+ e.statusText+'</strong></p><p>' + e.responseText + '</p>' : e;
        XNAT.dialog.open({
            width: 450,
            title: title,
            content: errormsg,
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    action: function(){
                        if (closeAll) {
                            xmodal.closeAll();

                        }
                    }
                }
            ]
        });
    }

    /* =============== *
     * USER CACHE MGMT *
     * =============== */

    var userCache, checkCacheUrl, flushCacheUrl, undef;

    XNAT.app = getObject(XNAT.app || {});
    XNAT.app.userCache = userCache =
        getObject(XNAT.app.userCache || {});

    userCache.checkCacheUrl = checkCacheUrl = function(){
        return XNAT.url.restUrl('/xapi/access/displays/modified');
    };

    userCache.flushCacheUrl = flushCacheUrl = function(username){
        username = (username) ? '/'+username : '';
        return XNAT.url.csrfUrl('/xapi/access/cache/flush'+username);
    };
    userCache.customFieldUrl = customFieldUrl = function () {
        return XNAT.url.restUrl('/xapi/customforms/displayfields/reload');
    };

    $(document).on('click','.flush-user-cache',function(e){
        e.preventDefault();

        var username = $(this).data('username') || false;

        xmodal.loading.open({ title: 'Flushing Cache'});
        XNAT.xhr.ajax({
            url: XNAT.app.userCache.flushCacheUrl(username),
            method: 'DELETE',
            fail: function(e){
                xmodal.loading.close();
                errorHandler(e, 'Could Not Flush User Cache');
            },
            success: function(data){
                xmodal.loading.close();
                xmodal.loading.open({ title: 'Resetting Custom Form Display Docs'});
                console.log('Successfully updated User Cache', data);

                // add a call to reset the custom form display field cache
                XNAT.xhr.post({
                    url: XNAT.app.userCache.customFieldUrl(),
                    success: function(data){
                        xmodal.loading.close();
                        XNAT.ui.banner.top(3000,'Successfully updated cached data','success');
                        XNAT.app.userCache.refresh();
                    },
                    fail: function(e){
                        xmodal.loading.close();
                        errorHandler(e, 'Could Not Reset Custom Form Display Docs');
                    }
                });
            }
        })
    });

    userCache.init = userCache.refresh = function(){

        XNAT.xhr.get({
            url: checkCacheUrl(),
            fail: function(e){
                errorHandler(e, 'Could not check cache status');
            },
            success: function(data){
                // a single datestamp is returned
                var d = new Date(data).toLocaleString(); 
                $('#user-cache-modified-status').empty().html('Cache last updated on '+d);
            }
        })

    }

}));