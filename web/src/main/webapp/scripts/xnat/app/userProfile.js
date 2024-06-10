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
                userEmailPanel: {
                    kind: 'panel',
                    label: 'Email Management',
                    footer: false,
                    contents: {
                        userEmailContainer: {
                            tag: 'div#user-email-container'
                        }
                    }
                },
                userPasswordPanel: {
                    kind: 'panel',
                    label: 'Password Management',
                    footer: false,
                    contents: {
                        userPasswordContainer: {
                            tag: 'div#user-password-container'
                        }
                    }
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
        var userCacheTab = {
            kind: 'tab',
            label: 'Manage Cached Resources',
            group: 'General',
            contents: {
                userCachePanel: {
                    kind: 'panel',
                    label: 'Cache Settings',
                    footer: false,
                    contents: {
                        userCacheHeader: {
                            tag: 'div',
                            element: {
                                style: { margin: '0 0 2em' }
                            },
                            contents:
                            '<div class="message">' +
                            '<p>The XNAT datatype access cache stores lists of common items that users have access to that seldom change, such as project roles and browseable datatypes. ' +
                            'This increases site performance by reducing the number of server calls required to render the UI. When those items are updated, the cache should be automatically flushed. ' +
                            'However, users may run across times when cached data appears to be stale. In those instances (or whenever you wish), users can manually refresh their datatype access cache.</p> ' +
                            '<p>Additionally, you  may need to reset your display cache using this function if you are not seeing updates to custom form defintions.</p>' +
                            '</div>'
                        },
                        userCacheControl: {
                            tag: 'div#user-cache-control',
                            contents:
                            '<p><a class="btn1 flush-user-cache" href="#!">Reset Access Cache</a> <span id="user-cache-modified-status" class="pad10h"></span></p>'
                        },
                        userCacheContainer: {
                            tag: 'div#user-cache-container'
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
                aliasTokenTab: aliasTokenTab,
                userCacheTab: userCacheTab,
            }
        };

        userProfile.tabSet = XNAT.spawner.spawn({ userProfileSettings: userProfileTabSet });
        userProfile.tabSet.render($container);

        let emailForm = $('#user-change-email').detach();
        emailForm.removeClass('html-template').appendTo($('#user-email-container'));
        let username = window.username || XNAT.data.username || 'false';

        XNAT.xhr.get({
            url: XNAT.url.restUrl('xapi/users/authDetails/'+username),
            async: false,
            fail: function(e){
                errorHandler(e, 'Could not fetch authentication method');
            },
            success: function(data){
                const hasLocalDB = data.some(el => el.authMethod === 'localdb');
                let passwordForm = $('#user-change-password').detach();
                if (hasLocalDB) {
                    passwordForm.removeClass('html-template').appendTo($('#user-password-container'));
                }else {
                    passwordForm.hide();
                    $('#user-change-password-not-allowed').html('Configured authentication provider for this user account does not allow password change through this interface. Please contact your site administrator for assistance.').appendTo($('#user-password-container'));
                }
            }
        })

        XNAT.ui.tab.activate('user-profile-tab');
    };

    userProfile.init = function(container){
        container = container || $('#user-profile-manager');
        userProfile.showTabSet(container);
    };
    
    
}));