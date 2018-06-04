console.log('xnat/admin/userProfile.js');

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

    /* ================= *
     * USER PROFILE MGMT *
     * ================= */

    var userProfile, undef;

    XNAT.app = getObject(XNAT.app || {});
    XNAT.app.userProfile = userProfile =
        getObject(XNAT.app.userProfile || {});

    userProfile.showTabSet = function(rootDiv){
        var $container = $(rootDiv || '#event-service-admin-tabs');
        $container.empty();

        var userProfileTab =  {
            kind: 'tab',
            label: 'Edit User Profile',
            group: 'General',
            active: true,
            contents: {
                userProfileContainer: {
                    tag: 'div#user-profile-container'
                }
            }
        };
        var aliasTokenTab = {
            kind: 'tab',
            label: 'Manage Alias Tokens',
            group: 'General',
            contents: {
                aliasTokenPanel: {
                    kind: 'panel',
                    label: 'Alias Tokens',
                    footer: false,
                    contents: {
                        aliasTokenHeader: {
                            tag: 'div',
                            element: {
                                style: { margin: '0 0 2em' }
                            },
                            contents:
                                '<div class="message">Alias Tokens can be generated and used by external applications to authenticate a session using your user credentials. Any activity performed during that session will be logged as having been performed by your user account.</div>' +
                                '<p><button id="create-alias-token" class="btn primary">Create Alias Token</button></p>'
                        },
                        aliasTokenContainer: {
                            tag: 'div#alias-token-table-container'
                        }
                    }
                }
            }
        };
        var userProfileTabSet = {
            kind: 'tabs',
            name: 'userProfileSettings',
            label: 'Event Service Administration',
            contents: {
                userProfileTab: userProfileTab,
                aliasTokenTab: aliasTokenTab
            }
        };

        userProfile.tabSet = XNAT.spawner.spawn({ userProfileSettings: userProfileTabSet });
        userProfile.tabSet.render($container);

        // userProfile.showSubscriptionList();

        XNAT.ui.tab.activate('user-profile-tab');
    };

    userProfile.init = function(container){
        container = container || $('#user-profile-manager');
        userProfile.showTabSet(container);
    };
    
    
}));