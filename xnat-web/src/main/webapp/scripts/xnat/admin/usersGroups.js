/*!
 * JS for Users & Groups admin page
 * /page/admin/users/
 *
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

console.log('usersGroups.js');

var XNAT = getObject(XNAT);

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

    var undefined, usersGroups, newUser = '',
        activeUsers = XNAT.data['/xapi/users/active'];

    XNAT.admin = getObject(XNAT.admin || {});

    XNAT.admin.usersGroups = usersGroups =
        getObject(XNAT.admin.usersGroups || {});


    function setupTabs(){

        function tabs(){
            return {
                kind: 'tabs',
                layout: 'top', // trying a top tabs layout
                contents: {
                    usersTab: usersTab(),
                    groupsTab: groupsTab()
                }
            }
        }

        function usersTab(){
            return {
                kind: 'tab',
                name: 'users',
                label: 'Users',
                active: true,
                contents: usersTabContents()
            }
        }

        function usersTabContents(){
            return {
                tag: 'div.manage-users',
                contents: {
                    style: {
                        tag: 'style',
                        content: '#user-profiles .new-user { background: #ffc }'
                    },
                    // actions: {
                    //     kind: 'panel',
                    //     label: 'Actions',
                    //     footer: false,
                    //     contents: {
                    //         viewActiveUsers: {
                    //             tag: 'button#view-active-users.btn1',
                    //             element: {
                    //                 type: 'button',
                    //                 html: 'View Active Users'
                    //             }
                    //         }//,
                    //         // things: {
                    //         //     kind: 'html',
                    //         //     content: '' +
                    //         //     '<div class="active-users">' +
                    //         //     '<button type="button" id="view-active-users" class="btn1">View Active Users</button>' +
                    //         //     '</div>'//,
                    //             // element: spawn('div.active-users', {
                    //             //     style: { marginBottom: '20px' }
                    //             // }, [['a#view-active-users|href=#!', 'View Active Users']])
                    //         // }
                    //     }
                    // },
                    userActions: {
                        tag: 'div.user-actions',
                        element: { style: {
                            marginBottom: '30px',
                            paddingTop: '30px',
                            borderTop: '1px solid #c8c8c8'
                        }},
                        contents: {
                            createUserButton: createUserButton()
                        }
                    },
                    usersTablePanel: {
                        kind: 'panel',
                        label: 'User Accounts',
                        footer: false,
                        contents: {
                            tableContainer: {
                                tag: 'div#user-table-container',
                                contents: {
                                    usersTable: usersTable()
                                }
                            }
                        }
                    }
                }
            }
        }

        function createUserButton(){
            return {
                tag: 'button#create-new-user',
                element: {
                    html: 'Create New User',
                    on: {
                        click: function(e){
                            console.log('clicked');
                            newUserDialog();
                        }
                    }
                }
            }
        }

        XNAT.tabs.container.on('click', '#view-active-users', function(){
            // var getActiveUsers = XNAT.xhr.get(XNAT.url.restUrl('/xapi/users/active'));
            getActiveUsers().done(function(data){
                var dataArray = [];
                forOwn(data, function(prop, obj){
                    // only add users with listed sessions
                    if (obj.sessions && obj.sessions.length) {
                        dataArray.push({
                            username: prop,
                            userdata: obj
                        })
                    }
                });
                xmodal.message({
                    width: 600,
                    height: 400,
                    maximize: true,
                    title: 'Active Users',
                    content: '<div class="active-users"></div>',
                    okLabel: 'Close',
                    beforeShow: function(){
                        var activeUsersTable = XNAT.table.dataTable(dataArray, {
                            items: {
                                username: 'User',
                                actions: {
                                    label: 'Actions',
                                    td: { className: 'actions center' },
                                    call: function(){
                                        var self          = this;
                                        var closeSessions = spawn('button.close-sessions', {
                                            type: 'button',
                                            title: this.username,
                                            html: 'Deactivate Sessions',
                                            on: { click: function(){
                                                    xmodal.confirm({
                                                        title: 'Close sessions?',
                                                        content: 'Deactivate all open sessions for <b>' + self.username + '</b>?',
                                                        okLabel: 'Deactivate Sessions',
                                                        okAction: function(){
                                                            XNAT.xhr.delete({
                                                                url: XNAT.url.rootUrl('/xapi/users/active/' + self.username),
                                                                success: function(){
                                                                    XNAT.ui.banner.top(2000, 'Sessions deactivated', 'success')
                                                                }
                                                            })
                                                        }
                                                    })
                                                }}
                                        });
                                        return spawn('div.user-actions', [
                                            closeSessions
                                        ])
                                    }
                                }//,
                                //userdata: {
                                //    label: 'Data',
                                //    call: function(data){
                                //        return '' +
                                //                '<textarea class="mono" style="width:100%" rows="8">' +
                                //                    JSON.stringify(data, null, 4) +
                                //                '</textarea>';
                                //    }
                                //}
                            }
                        });
                        this.__modal.find('div.active-users').append(activeUsersTable);
                    }
                });
            })
        });

        function userSwitch(className, value){
            var item = this;
            var _checked = !!realValue(value);
            return XNAT.ui.input.switchbox({
                // 'element' applies to inner checkbox
                // disabled: _checked,
                element: {
                    name: className,
                    className: 'user-' + className,
                    checked: _checked,
                    title: item.username + ':' + className
                },
                onText: '',
                offText: ''
            })
        }

        function selectAllUsers(){
            var $this   = $(this);
            var $table  = $this.closest('table');
            var $inputs = $table.find('input.select-user');
            if ($this.hasClass('selected')) {
                $inputs.prop('checked', false);
                $this.removeClass('selected');
            }
            else {
                $inputs.prop('checked', true);
                $this.addClass('selected');
            }
        }

        function userAccountForm(data){
            var _load = data ? '/xapi/users/' + data.username : false;
            data = data || {};
            var form = {
                kind: 'panel.form',
                label: 'Account Information',
                footer: false,
                validate: true,
                method: _load ? 'PUT' : 'POST',
                contentType: 'json',
                load: _load,
                refresh: false,
                action: '/xapi/users' + (_load ? '/' + data.username : ''),
                contents: {
                    // details: {
                    //     kind: 'panel.subhead',
                    //     label: 'User Details'
                    // },
                    username: {
                        kind: 'panel.input.text',
                        label: 'Username',
                        validate: 'alpha-num-safe required',
                        value: data.username || ''
                    },
                    password: {
                        kind: 'panel.input.password',
                        label: 'Password',
                        validate: 'allow-empty alpha-num-dash'//,
                        //value: data.password || ''
                    },
                    firstName: {
                        kind: 'panel.input.text',
                        label: 'First Name',
                        validate: 'required',
                        value: data.firstName || ''
                    },
                    lastName: {
                        kind: 'panel.input.text',
                        label: 'Last Name',
                        validate: 'required',
                        value: data.lastName || ''
                    },
                    email: {
                        kind: 'panel.input.email',
                        label: 'Email',
                        validate: 'email required',
                        value: data.email || ''
                    },
                    verified: {
                        kind: 'panel.input.switchbox',
                        label: 'Verified',
                        value: data.verified !== undefined ? data.verified+'' : 'false',
                        element: {
                            //disabled: !!_load,
                            title: data.username + ':verified'//,
                            //on: { click: _load ? setVerified : diddly }
                        }
                    },
                    enabled: {
                        kind: 'panel.input.switchbox',
                        label: 'Enabled',
                        value: data.enabled !== undefined ? data.enabled+'' : 'false',
                        element: {
                            //disabled: !!_load,
                            title: data.username + ':enabled'//,
                            //on: { click: _load ? setEnabled : diddly }
                        }
                    }
                }
            };

            return form;
        }

        // use this to spawn the user account form separately
        function renderUserAccountForm(data, container){
            return XNAT.spawner.spawn({
                userAccountForm: userAccountForm(data)
            }).render(container)
        }

        function saveUserData(form, opts){
            var $form = $$(form);
            var username = $form.find('input#username').val();
            opts = cloneObject(opts);
            var doSubmit = $form.submitJSON(opts);
            doSubmit.done(function(){
                XNAT.ui.banner.top(2000, 'User info saved.', 'success')
            });
            return doSubmit;
        }

        // open a dialog for creating a new user
        function newUserDialog(){
            return xmodal.open({
                width: 600,
                height: 500,
                title: 'Create New User',
                content: '<div class="new-user-form"></div>',
                beforeShow: function(){
                    var _container = this.$modal.find('div.new-user-form');
                    renderUserAccountForm(null, _container)
                },
                okLabel: 'Save',
                okClose: 'false',
                okAction: function(obj){
                    var $form = obj.$modal.find('form#user-account-form-panel');
                    var doSave = saveUserData($form);
                    doSave.done(function(){
                        obj.close();
                    });
                },
                onClose: function(){
                    updateUsersTable();
                }
            })
        }

        // define the user properties dialog
        function editUserDialog(data, onclose){
            if (data && !data.username) {
                return xmodal.message('Error', 'An error occurred displaying user data.');
            }
            // define the <form> Spawner element
            function userForm(){
                return {
                    userForm: {
                        tag: 'div.user-account-info',
                        //kind: 'panel.multiForm',
                        //classes: 'user-details',
                        // label: 'User Details',
                        //header: false,
                        //footer: false,
                        contents: {
                            userAccountForm: userAccountForm(data)//,
                            //userProjects: userProjects(),
                            //userSecurity: userSecurity()
                        }
                    }
                }
            }
            function userProjects(){
                return {
                    kind: 'panel.form',
                    title: 'Project Membership & Roles',
                    contents: {
                        projectMembership: {
                            tag: 'div.project-membership',
                            content: '<i>project membership and role menus go here</i>'
                        }
                    }
                }
            }
            function userSecurity(){
                return {
                    kind: 'panel.form',
                    name: 'securitySettings',
                    title: 'Security Settings',
                    // action: '#!',
                    // action: '/xapi/users/' + data.username + '/roles',
                    // action: '/data/user/' + data.username + '/roles',
                    // _action: '/app/action/ModifyUserGroups',
                    contents: {
                        systemRolesSubhead: {
                            kind: 'panel.subhead',
                            label: 'System Roles'
                        },
                        csrfToken: {
                            kind: 'input.hidden',
                            name: 'XNAT_CSRF',
                            value: csrfToken
                        },
                        siteManager: {
                            kind: 'panel.input.switchbox',
                            label: 'Site Manager',
                            id: 'custom-role-administrator',
                            name: 'custom_role',
                            value: 'Administrator',
                            element: {
                                checked: (data.roles.indexOf('Administrator') > -1)
                            },
                            description:
                                '<p>This allows users to access the Administrative pages of the web interface.</p>' +
                                '<div class="warning">' +
                                '<b>WARNING:</b> Granting administrative privileges allows this user great power ' +
                                'over the entire site.' +
                                '</div>'
                        },
                        nonExpiring: {
                            kind: 'panel.input.switchbox',
                            label: 'Non-Expiring',
                            id: 'custom-role-non-expiring',
                            name: 'custom_role',
                            value: 'non_expiring',
                            element: {
                                checked: (data.roles.indexOf('non_expiring') > -1)
                            },
                            description:
                                '<p>This prevents this accounts password from expiring.</p>' +
                                '<div class="warning">' +
                                '<b>WARNING:</b> Granting a user account a non-expiring password is a security risk ' +
                                'and should be limited to accounts that perform automated system tasks. In addition, ' +
                                'if any users are designated as non-expiring access to the user list should be ' +
                                'restricted to administrators.' +
                                '</div>'
                        },
                        allDataSubhead: {
                            kind: 'panel.subhead',
                            label: 'Allow All Data Access'
                        },
                        allDataRadios: {
                            tag: 'div.all-data-radios',
                            contents: {
                                noRadio: {
                                    kind: 'input.radio',
                                    id: 'data_none',
                                    name: 'xdat:user.groups.groupID[0].groupID',
                                    label: 'No',
                                    value: 'NULL'//,
                                    // afterElement: 'No'
                                },
                                readOnlyRadio: {
                                    kind: 'input.radio',
                                    id: 'data_access',
                                    name: 'xdat:user.groups.groupID[0].groupID',
                                    label: 'Read Only',
                                    value: 'ALL_DATA_ACCESS'
                                },
                                readEditDeleteRadio: {
                                    kind: 'input.radio',
                                    id: 'data_admin',
                                    name: 'xdat:user.groups.groupID[0].groupID',
                                    label: 'Read, Edit, Delete',
                                    value: 'ALL_DATA_ADMIN'
                                }
                            }
                        },
                        allDataWarning: {
                            tag: 'div.warning',
                            content:
                            'WARNING: Allowing full access to data will allow this user to see ALL data ' +
                            'stored in this system. It supersedes project membership. Most accounts on your server ' +
                            'should NOT have All Data Access allowed.'
                        }
                    }
                }
            }
            return xmodal.open({
                width: 600,
                height: 500,
                title: 'User Properties for <b>' + data.username + '</b>',
                content: '<div class="user-data"></div>',
                beforeShow: function(obj){
                    var _userForm = XNAT.spawner.spawn(userForm());
                    obj.$modal.find('div.user-data').append(_userForm.get())
                },
                okLabel: 'Save',
                okClose: false,
                okAction: function(obj){
                    var $form = obj.$modal.find('form#user-account-form-panel');
                    var doSave = saveUserData($form);
                    doSave.done(function(){
                        obj.close();
                    });
                },
                onClose: function(obj){
                    renderUsersTable();
                    if (typeof onclose === 'function'){
                        onclose(obj)
                    }
                }
            })
        }

        // get user data and return AJAX promise object
        function getUserData(username){
            var _url = XNAT.url.restUrl('/xapi/users/' + username);
            return XNAT.xhr.get(_url)
        }

        // get user roles and return AJAX promise object
        function getUserRoles(username){
            var _url = XNAT.url.restUrl('/xapi/users/' + username + '/roles');
            return XNAT.xhr.get(_url);
        }

        function getActiveUsers(success, failure){
            return XNAT.xhr.get({
                url: XNAT.url.restUrl('/xapi/users/active'),
                success: function(data){
                    XNAT.data['/xapi/users/active'] = data;
                    if (isFunction(success)) {
                        success(data);
                    }
                }
            })
        }

        // open a dialog to edit user properties
        function editUser(e, onclose){
            e.preventDefault();
            var username = (this.innerText||'').trim();
            getUserData(username).done(function(data){
                getUserRoles(username).done(function(roles){
                    data.roles = roles;
                    // save the data to namespaced object before opening dialog
                    XNAT.data['/xapi/users/' + username] = data;
                    editUserDialog(data, onclose);
                })
            });
        }
        XNAT.admin.usersGroups.editUser = editUser;

        // immediately toggles user's "Verified" status
        function setVerified(){
            var username = this.title.split(':')[0];
            var flag     = this.checked;
            XNAT.xhr.put({
                url: XNAT.url.rootUrl('/xapi/users/' + username + '/verified/' + flag),
                success: function(){
                    XNAT.ui.banner.top(2000, 'User has been set to ' + (flag ? '"verified"' : '"unverified"') + '.', 'success')
                },
                error: function(){
                    XNAT.ui.banner.top(3000, 'An error occurred setting "verified" status.', 'error')
                }
            })
        }

        // immediately toggles user's "Enabled" status
        function setEnabled(){
            var username = this.title.split(':')[0];
            var flag     = this.checked;
            XNAT.xhr.put({
                url: XNAT.url.rootUrl('/xapi/users/' + username + '/enabled/' + flag),
                success: function(){
                    XNAT.ui.banner.top(2000, 'User status has been set to ' + (flag ? '"enabled"' : '"disabled"') + '.', 'success')
                },
                error: function(){
                    XNAT.ui.banner.top(3000, 'An error occurred setting "enabled" status.', 'error')
                }
            })
        }

        // kill all active sessions for the specified user
        function killActiveSessions(username){
            return xmodal.confirm({
                title: 'Kill Active Sessions?',
                content: 'Kill all active sessions for <b>' + username + '</b>?',
                okClose: false,
                okAction: function(obj){
                    XNAT.xhr.delete({
                        url: XNAT.url.rootUrl('/xapi/users/active/' + username),
                        success: function(){
                            obj.close();
                            XNAT.ui.banner.top(2000, 'Sessions closed', 'success');
                            updateUsersTable();
                        }
                    })
                }
            });
        }

        // TODO: view active sessions
        function viewSessionInfo(e){
            e.preventDefault();
            var username = this.title;
        }

        function userProjectsAndSecurity(){
            var _username = $(this).data('username');
            var _url = XNAT.url.rootUrl('/app/action/DisplayItemAction/search_value/' + _username + '/search_element/xdat:user/search_field/xdat:user.login/popup/true');
            return xmodal.iframe(_url, {
                width: 800,
                height: '100%',
                title: 'Edit User Info',
                // footer: false,
                okLabel: 'Close',
                cancel: false,
                onClose: function(){
                    updateUsersTable();
                }
            })
        }

        function goToEmail(){
            var _email = $(this).text();
            var _url = XNAT.url.rootUrl('/app/template/XDATScreen_email.vm/emailTo/');
            window.location.href = _url + _email;
        }

        // userProjectsAndSecurity

        // Spawner element config for the users list table
        function usersTable(){
            //var _data = XNAT.xapi.users.profiles || XNAT.data['/xapi/users/profiles'];
            return {
                kind: 'table.dataTable',
                name: 'userProfiles',
                id: 'user-profiles',
                load: '/xapi/users/profiles',
                element: {
                    on: [
                        ['click', 'a.select-all', selectAllUsers],
                        ['click', 'a.username', editUser],
                        ['click', 'a.full-name', userProjectsAndSecurity],
                        ['click', 'a.send-email', goToEmail],
                        ['change', 'input.user-verified', setVerified],
                        ['change', 'input.user-enabled', setEnabled],
                        ['click', 'a.session-info', viewSessionInfo]
                    ]
                },
                //data: _data,
                sortable: 'username, fullName, email',
                filter: 'fullName, email',
                items: {
                    _id: '~data-id',
                    _username: '~data-username',
                    // _select: {
                    //     label: 'Select',
                    //     th: { html: '<a href="#!" class="select-all link">Select</a>' },
                    //     td: { className: 'centered' },
                    //     call: function(){
                    //         return spawn('input.select-user', {
                    //             type: 'checkbox',
                    //             checked: false,
                    //             title: this.username + ':select'
                    //         });
                    //         //return '<a href="#!" class="username link">' + this.username + '</a>'
                    //     }
                    // },
                    //
                    username: {
                        label: 'Username',
                        filter: true, // add filter: true to individual items to add a filter
                        call: function(username, tr){
                            //console.log(tr);
                            return '<a href="#!" title="edit user details" class="username link">' + username + '</a>'
                        }
                    },
                    fullName: {
                        label: 'Name',
                        call: function(){
                            return spawn('a.full-name.link', {
                                href: '#!',
                                title: 'edit user project and security settings',
                                html: this.lastName + ', ' + this.firstName,
                                data: { username: this.username }
                            });
                            //return this.lastName + ', ' + this.firstName
                        }
                    },
                    email: {
                        label: 'Email',
                        content: '<a href="#!" title="Send email" class="send-email link">__VALUE__</a>'
                    },
                    verified: {
                        label: 'Verified',
                        td: { className: 'verified center' },
                        call: function(value){
                            return userSwitch.call(this, 'verified', value);
                        }
                    },
                    enabled: {
                        label: 'Enabled',
                        td: { className: 'enabled center' },
                        call: function(value){
                            return userSwitch.call(this, 'enabled', value);
                        }
                    },
                    // by convention, name 'custom' columns with ALL CAPS
                    // 'custom' columns do not correspond directly with
                    // a data item
                    ACTIVE: {
                        label: 'Active',
                        sort: true,
                        td: { className: 'active center' },
                        call: function(){
                            var username = this.username;
                            var sessionCount = 0;
                            if (username && activeUsers[username] && activeUsers[username].sessions.length) {
                                sessionCount = activeUsers[username].sessions.length
                            }
                            if (sessionCount) {
                                return spawn('div', [
                                    ['i.hidden', -sessionCount],
                                    ['a.active-user', {
                                        title: 'click to kill ' + sessionCount + ' active session(s)',
                                        href: '#!',
                                        style: { display: 'block', padding: '2px' },
                                        on: { click: function(e){
                                            e.preventDefault();
                                            killActiveSessions(username);
                                        }}
                                    }, [['img', { src: XNAT.url.rootUrl('/images/cg.gif') }]]]
                                ])
                            }
                            else {
                                return '<i class="hidden">9</i>&mdash;'
                            }
                        }
                    }//,
                    // _active: {
                    //     label: 'Active',
                    //     td: { className: 'active center' },
                    //     call: function(){
                    //         var item = this;
                    //         var username = item.username;
                    //         var sessions = activeUsers[username] && activeUsers[username].sessions.length;
                    //         return XNAT.ui.input.switchbox({
                    //             disabled: !sessions,
                    //             // 'element' applies to inner checkbox
                    //             element: {
                    //                 className: !!sessions ? 'active' : 'inactive disabled',
                    //                 disabled: !sessions,
                    //                 checked: !!sessions,
                    //                 title: !!sessions ? 'active' : 'inactive',
                    //                 on: [
                    //                     ['change', function(){
                    //                         var element = this;
                    //                         var setActive = element.checked;
                    //                         if (!!sessions && !setActive) {
                    //                             XNAT.xhr.delete({
                    //                                 url: XNAT.url.rootUrl('/xapi/users/active/' + username),
                    //                                 success: function(){
                    //                                     element.title = 'inactive';
                    //                                     element.disabled = true;
                    //                                     $(element).addClass('disabled');
                    //                                     XNAT.ui.banner.top(2000, 'All sessions for <b>' + username + '</b> have been deactivated.', 'success')
                    //                                 }
                    //                             })
                    //                         }
                    //                     }]
                    //                 ]
                    //             },
                    //             onText: '',
                    //             offText: ''
                    //         });
                    //
                    //         //
                    //
                    //         // return userSwitch.call(this, 'enabled', value);
                    //     }
                    // },
                    // sessions: {
                    //     label: 'Session Info',
                    //     td: { className: 'sessions center' },
                    //     call: function(){
                    //         return spawn('a.session-info.link', {
                    //             href: '#!',
                    //             title: this.username
                    //         }, 'session info')
                    //     }
                    // }
                }
            }
        }

        // render or update the users table
        function renderUsersTable(container){
            var $container = $$(container || '#user-table-container').empty();
            return XNAT.spawner.spawn({
                usersTable: usersTable()
            }).render($container);
        }

        function updateUsersTable(){
            return XNAT.xhr.get({
                url: XNAT.url.restUrl('/xapi/users/profiles'),
                success: function(data){
                    XNAT.data['/xapi/users/profiles'] = data;
                    console.log(data);
                    getActiveUsers(function(){
                        renderUsersTable();
                    });
                }
            })
        }

        function groupsTab(){
            return {
                kind: 'tab',
                name: 'groups',
                label: 'Groups',
                active: false,
                contents: {
                    temp: {
                        tag: 'i',
                        content: '(groups will show up here)'
                    }
                }
            }
        }

        return {
            //tabs: tabs()
            usersTabContents: usersTabContents()
        }

    }

    var tabsConfig = setupTabs();

    XNAT.spawner.spawn(tabsConfig).render(XNAT.tabs.container);


    // this script has loaded
    usersGroups.loaded = true;

    return XNAT.admin.usersGroups = usersGroups;

}));
