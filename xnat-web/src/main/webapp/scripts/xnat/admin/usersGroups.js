/*
 * web: usersGroups.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
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
        BASE_URL = '/xapi/users',
        passwordComplexity =
            XNAT.data.siteConfig.passwordComplexity
                .replace(/^\^*/, '^')
                .replace(/\$*$/, '$'),
        passwordComplexityMessage = XNAT.data.siteConfig.passwordComplexityMessage,
        activeUsers = XNAT.data['/xapi/users/active'];

    XNAT.admin = getObject(XNAT.admin || {});

    XNAT.admin.usersGroups = usersGroups =
        getObject(XNAT.admin.usersGroups || {});

    XNAT.usersGroups = usersGroups;

    usersGroups.showAdvanced = true;

    // return a url for doing a rest call
    function setUrl(part1, part2, part3){
        var fullUrl = BASE_URL +
                (part1 ? ('/' + part1) : '') +
                (part2 ? ('/' + part2) : '') +
                (part3 ? ('/' + part3) : '');
        return XNAT.url.rootUrl(fullUrl)
    }
    usersGroups.setUrl = setUrl;


    // create urls based on minimal input
    function usersUrl(user){

        // resolve url arguments
        function urlArgs(username, path, param){
            username = username || user || '';
            param = param || username || '';
            username = (username||'') === param ? user : username;
            return setUrl(username, path, param)
        }

        return {
            allUsers: function(){
                return setUrl()
            },
            userProfile: function(username){
                return setUrl(username||user)
            },
            allUserProfiles: function(){
                return setUrl('profiles')
            },
            activeUser: function(username){
                return setUrl('active', username||user)
            },
            allActiveUsers: function(){
                return setUrl('active')
            },
            enabledUser: function(username, flag){
                return urlArgs(username, 'enabled', flag)
            },
            // XNAT.admin.usersGroups.url('bob').groups()
            // XNAT.admin.usersGroups.url().groups('bob')
            // --> '/xapi/users/bob/groups'
            groups: function(username, group){
                return urlArgs(username, 'groups', group)
            },
            roles: function(username, role){
                return urlArgs(username, 'roles', role)
            },
            verified: function(username, flag){
                return urlArgs(username, 'verified', flag)
            }
        }
    }
    usersGroups.url = usersUrl;

    function xapiUsers(method, url, opts){
        method = method || 'get';
        var methodGET = /^get$/i.test(method);
        opts = cloneObject(opts);
        opts.url = opts.url || url;
        if (methodGET){
            // the 'restuUrl' method adds a cache-busting parameter
            opts.url = XNAT.url.restUrl(opts.url);
        }
        else {
            opts.url = XNAT.url.rootUrl(opts.url);
        }
        return XNAT.xhr[method](opts).done(function(data){
            // cache returned 'get' data at URL
            if (methodGET) XNAT.data[url] = data;
        });
    }

    // get or set data via REST
    usersGroups.userData = function(user){
        var methods = {
            // generic method for any kind of request
            request: function(method, path, data, opts){
                path = [].concat(path);
                opts = getObject(opts);
                opts.data = data || opts.data || '';
                return xapiUsers(method, setUrl.apply(null, path), opts)
            },
            userList: function(opts){
                return xapiUsers('get', setUrl(), opts);
            },
            allProfiles: function(opts){
                return xapiUsers('get', setUrl('profiles'), opts);
            },
            // XNAT.usersGroups.userData('bob').profile().done(someCallbackFunction)
            getProfile: function(opts){
                return xapiUsers('get', setUrl('profile/' + user), opts);
            },
            createProfile: function(data, opts){
                opts = getObject(opts);
                opts.data = data || opts.data || '';
                return xapiUsers('postJSON', setUrl(user), opts)
            },
            updateProfile: function(data, opts){
                opts = getObject(opts);
                opts.data = data || opts.data || '';
                return xapiUsers('putJSON', setUrl(user), opts)
            },
            activeUsers: function(opts){
                return xapiUsers('get', setUrl('active'), opts)
            }
        };
        methods.allUsers = methods.userList;
        methods.profiles = methods.allProfiles;
        methods.userProfiles = methods.allProfiles;
        methods.allUserProfiles = methods.allProfiles;

        // decide whether to GET or POST
        // based on presence of 'data' argument
        methods.profile = function(data, opts){
            var method = 'createProfile';
            if (!data && !opts) {
                method = 'getProfile'
            }
            return methods[method](data, opts);
        };
        methods.createUser = methods.createProfile;
        methods.updateUser = methods.updateProfile;

        return methods;

    };


    function setupTabs(){

        // console.log(setupTabs.name);

        var userTableContainer = 'div#user-table-container';

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
                        element: {
                            style: {
                                marginBottom: '30px',
                                paddingTop: '30px',
                                borderTop: '1px solid #c8c8c8'
                            }
                        },
                        contents: {
                            createUserButton: createUserButton(),
                            updateUserTableButton: updateUserTableButton()
                        }
                    },
                    usersTablePanel: {
                        // kind: 'panel',
                        // label: 'User Accounts',
                        // footer: false,
                        tag: 'div#user-accounts',
                        contents: {
                            tableContainer: {
                                tag: userTableContainer,
                                contents: {
                                    usersTable: spawnUsersTable()
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
                            // console.log('clicked');
                            newUserDialog();
                        }
                    }
                }
            }
        }

        function updateUserTableButton(){
            return {
                tag: 'button#refresh-user-list',
                element: {
                    html: 'Refresh User List',
                    style: {
                        marginLeft: '20px'
                    },
                    on: {
                        click: function(e){
                            // console.log('clicked');
                            updateUserData('profiles', 10);
                        }
                    }
                }
            }
        }

        // common config function for DOM and Spawner elements
        function userSwitchConfig(username, type, status){
            status = !!realValue(status);
            return {
                config: {
                    element: {
                        name: type,
                        className: 'user-' + type,
                        checked: status,
                        title: username + ':' + type
                    },
                    onText: '',
                    offText: ''
                }
            }
        }

        // RETURNS A DOM ELEMENT
        function userSwitch(type, status){
            var element = userSwitchConfig(this.username, type, status);
            return XNAT.ui.input.switchbox(element.config)
        }

        // spawns a status switchbox element
        // RETURNS A SPAWNER INSTANCE
        usersGroups.userSwitchElement = function(username, type, status){
            var element = userSwitchConfig(username, type, status);
            element.config.kind = 'input.switchbox'; // add 'kind' property for spawner function
            element.config.onText = 'Yes';
            element.config.offText = 'No';
            return XNAT.spawner.spawn(element);
        };

        function selectAllUsers(){
            var $this = $(this);
            var $table = $this.closest('table');
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

            var _load = data ? serverRoot + '/xapi/users/' + data.username : false;

            data = data || {};

            // username could be text or input element
            function usernameField(){
                var obj = {
                    label: 'Username'
                };
                if (data && data.username) {
                    obj.kind = 'panel.element';
                    obj.contents = {
                        usernameText: {
                            kind: 'html',
                            content: data.username
                        },
                        usernameInput: {
                            kind: 'input.hidden',
                            name: 'username',
                            value: data.username || ''
                        }
                    };
                }
                else {
                    obj.kind = 'panel.input.text';
                    obj.name = 'username';
                    obj.validate = 'alpha-num-safe required';
                    obj.value = '';
                }
                return obj;
            }

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
                    // id: {
                    //     kind: 'panel.input.hidden',
                    //     validate: _load ? 'number required' : 'allow-empty',
                    //     value: data.id || ''
                    // },
                    pad: {
                        kind: 'html',
                        content: '<br>'
                    },
                    usernameField: usernameField(),
                    password: {
                        kind: 'panel.input.password',
                        label: 'Password',
                        element: {
                            placeholder: '********',
                            data: { message: passwordComplexityMessage }
                        },
                        validate: 'allow-empty pattern:' + passwordComplexity + ' max-length:255'//,
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
                        value: data.verified !== undefined ? data.verified + '' : 'false',
                        element: {
                            //disabled: !!_load,
                            title: data.username + ':verified'//,
                            //on: { click: _load ? setVerified : diddly }
                        }
                    },
                    enabled: {
                        kind: 'panel.input.switchbox',
                        label: 'Enabled',
                        value: data.enabled !== undefined ? data.enabled + '' : 'false',
                        element: {
                            //disabled: !!_load,
                            title: data.username + ':enabled'//,
                            //on: { click: _load ? setEnabled : diddly }
                        }
                    }
                }
            };

            // add 'Advanced Settings' when editing existing user
            if (_load && usersGroups.showAdvanced) {
                form.contents.advancedSettings = {
                    kind: 'panel.element',
                    label: 'Advanced',
                    contents: {
                        advancedLink: {
                            tag: 'a.edit-advanced-settings.link',
                            element: {
                                href: '#!',
                                title: data.username + ':advanced',
                                on: {
                                    click: function(e){
                                        e.preventDefault();
                                        var modalId = $(this).closest('div.xmodal').attr('id');
                                        xmodal.modals[modalId].close();
                                        userProjectsAndSecurity(e, data.username);
                                    }
                                }
                            },
                            content: 'Edit Advanced Settings'
                        },
                        description: {
                            tag: 'div.description',
                            content: "Edit this user's project and security settings."
                        }
                    }
                }
            }

            usersGroups.showAdvanced = true;

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
            if (doSubmit.done) {
                doSubmit.done(function(){
                    // xmodal.loading.open();
                    XNAT.ui.banner.top(2000, 'User info saved.', 'success')
                });
            }
            return doSubmit;
        }

        // open a dialog for creating a new user
        function newUserDialog(){
            var updated = false;
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
                        updated =  true;
                        obj.close();
                    });
                },
                onClose: function(){
                    if (updated) {
                        updateUsersTable(true);
                    }
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

            // TODO: replace old 'advanced' project settings with this
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

            // TODO: replace old 'advanced' security settings with this
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
                            description: '<p>This allows users to access the Administrative pages of the web interface.</p>' +
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
                            description: '<p>This prevents this accounts password from expiring.</p>' +
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
                            content: 'WARNING: Allowing full access to data will allow this user to see ALL data ' +
                            'stored in this system. It supersedes project membership. Most accounts on your server ' +
                            'should NOT have All Data Access allowed.'
                        }
                    }
                }
            }
            var updated = false;
            return xmodal.open({
                width: 600,
                height: 600,
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
                        updated = true;
                        obj.close();
                    });
                },
                onClose: function(obj){
                    if (typeof onclose === 'function') {
                        onclose(obj)
                    }
                    if (updated) {
                        updateUserData(data.username);
                        // renderUsersTable();
                    }
                }
            })
        }

        // get user data and return AJAX promise object
        function getUserData(username){
            var _url = XNAT.url.restUrl('/xapi/users/' + username);
            return XNAT.xhr.get(_url)
        }

        // get profile data for ALL users
        function getUserProfiles(success, failure){
            return XNAT.xhr.get({
                url: XNAT.url.restUrl('/xapi/users/profiles'),
                success: function(data){
                    XNAT.data['/xapi/users/profiles'] = data;
                    if (isFunction(success)) {
                        success.apply(this, arguments);
                    }
                },
                failure: failure
            })
        }
        usersGroups.getUserProfiles = getUserProfiles;

        // get user roles and return AJAX promise object
        function getUserRoles(username){
            var _url = XNAT.url.restUrl('/xapi/users/' + username + '/roles');
            return XNAT.xhr.get(_url);
        }

        function getActiveUsers(success, failure){
            // console.log(getActiveUsers.name);
            return XNAT.xhr.get({
                url: XNAT.url.restUrl('/xapi/users/active'),
                success: function(data){
                    XNAT.data['/xapi/users/active'] = data;
                    if (isFunction(success)) {
                        success.apply(this, arguments);
                    }
                },
                failure: failure
            })
        }

        function setRowId(profile){
            return profile.username + '-' + profile.id;
        }

        // update ONLY the row of the edited user
        function updateUserRow(profile){

            var rowId = setRowId(profile);
            var $row  = $(document.getElementById(rowId));

            // full name column
            $row.find('a.full-name')
                .text(profile.lastName + ', ' + profile.firstName);

            // email column
            $row.find('a.send-email')
                .attr('title', profile.email + ': send email')
                .text(profile.email);

            // verified status column
            $row.find('td.verified')
                .empty()
                .append(userStatusInfo.call(profile, 'verified', 'unverified'));

            // enabled status column
            $row.find('td.enabled')
                .empty()
                .append(userStatusInfo.call(profile, 'enabled', 'disabled'));

            // active status column
            $row.find('td.active')
                .empty()
                .append(activeUserInfo.call(profile));

            // last login column
            $row.find('td.last-login')
                .empty()
                .append(lastLoginInfo(profile.lastSuccessfulLogin));

        }

        function updateUserData(username, delay){
            var USERNAME = typeof username === 'string' ? username : this.title.split(':')[0];
            var USER_URL = '/xapi/users/profile/' + USERNAME;
            return XNAT.xhr.get({
                url: XNAT.url.restUrl(USER_URL),
                success: function(DATA){
                    XNAT.data[USER_URL] = DATA;
                    window.setTimeout(function(){
                        getActiveUsers().done(function(ACTIVE){
                            XNAT.data['/xapi/users/active'] = ACTIVE;
                            forEach([].concat(DATA), function(profile){
                                updateUserRow(profile);
                            })
                        })
                    }, delay||100);
                }
            })
        }

        // open a dialog to edit user properties
        function editUser(e, onclose){
            e.preventDefault();
            var username =
                (this.title || '').split(':')[0] ||
                $(this).data('username') ||
                $(this).closest('tr').data('username') ||
                (this.innerText || '').trim();
            getUserData(username).done(function(data){
                getUserRoles(username).done(function(roles){
                    data.roles = roles;
                    // save the data to namespaced object before opening dialog
                    XNAT.data['/xapi/users/profile/' + username] = data;
                    editUserDialog(data, onclose);
                })
            });
        }
        usersGroups.editUser = editUser;

        // immediately toggles user's "Verified" status
        function setVerified(e, username, flag){
            username = username || this.title.split(':')[0];
            flag = flag || this.checked;
            return XNAT.xhr.put({
                url: XNAT.url.rootUrl('/xapi/users/' + username + '/verified/' + flag),
                success: function(){
                    XNAT.ui.banner.top(2000, 'User has been set to ' + (flag ? '"verified"' : '"unverified"') + '.', 'success')
                },
                error: function(){
                    XNAT.ui.banner.top(3000, 'An error occurred setting "verified" status.', 'error')
                }
            })
        }
        usersGroups.setVerified = setVerified;


        // immediately toggles user's "Enabled" status
        function setEnabled(e, username, flag){
            username = username || this.title.split(':')[0];
            flag = flag || this.checked;
            return XNAT.xhr.put({
                url: XNAT.url.rootUrl('/xapi/users/' + username + '/enabled/' + flag),
                success: function(){
                    XNAT.ui.banner.top(2000, 'User status has been set to ' + (flag ? '"enabled"' : '"disabled"') + '.', 'success')
                },
                error: function(){
                    XNAT.ui.banner.top(3000, 'An error occurred setting "enabled" status.', 'error')
                }
            })
        }
        usersGroups.setEnabled = setEnabled;


        // kill all active sessions for the specified user
        function killActiveSessions(e){
            e.preventDefault();
            var username = this.title.split(':')[0].trim();
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
                            // wait a few seconds before updating the row
                            updateUserData(username, 2000);
                            // updateUsersTable(true);
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

        function userProjectsAndSecurity(e, usr){
            var _username = usr || $(this).data('username');
            var _url = XNAT.url.rootUrl('/app/action/DisplayItemAction/search_value/' + _username + '/search_element/xdat:user/search_field/xdat:user.login/popup/true');
            return xmodal.iframe({
                src: _url,
                name: 'advanced-user-settings',
                width: 800,
                height: '100%',
                title: 'Edit User Info',
                // footer: false,
                okLabel: 'Close',
                cancel: false,
                onClose: function(){
                    updateUsersTable(true);
                }
            })
        }

        function goToEmail(){
            var _email = this.title.split(':')[0];
            var _url = XNAT.url.rootUrl('/app/template/XDATScreen_email.vm/emailTo/');
            window.location.href = _url + _email;
        }

        // set up custom filter menus
        function filterMenuElement(prop){
            if (!prop) return false;
            // call this function in context of the table
            var $userProfilesTable = $(this);
            var FILTERCLASS = 'filter-' + prop;
            // var FILTERCLASS = 'hidden';
            return {
                id: 'user-filter-select-' + prop,
                // style: { width: '100%' },
                on: {
                    change: function(){
                        var selectedValue = $(this).val();
                        // console.log(selectedValue);
                        $userProfilesTable.find('i.filtering.'+prop).each(function(){
                            var $row = $(this).closest('tr');
                            if (selectedValue === 'all') {
                                $row.removeClass(FILTERCLASS);
                                return;
                            }
                            $row.addClass(FILTERCLASS);
                            if (selectedValue == this.textContent) {
                                $row.removeClass(FILTERCLASS);
                            }
                            // if (!this.checked && selectedValue === notProp) {
                            //     $row.removeClass(FILTERCLASS);
                            // }
                        })
                    }
                }
            };
        }

        // renders cells for 'verified' and 'enabled' status
        function userStatusInfo(type, off){
            var username = this.username;
            var status = realValue(this[type]);
            var SORTER = status ? 1 : 0;
            var IMG = status ? '/images/cg.gif' : '/images/cr.gif';
            return spawn('!', [
                ['i.hidden.sorting.filtering.' + type, SORTER+''],
                ['a.user-' + type + '-status.edit-user', {
                    title: username + ': ' + (status ? type : off),
                    href: '#!',
                    style: { display: 'block', padding: '2px' }
                }, [['img', { src: XNAT.url.rootUrl(IMG) }]]]
            ]);
        }

        // renders cells for 'active' users column
        function activeUserInfo(){
            var username = this.username;
            var sessionCount = 0;
            activeUsers = XNAT.data['/xapi/users/active'];
            if (username && activeUsers && activeUsers[username] && activeUsers[username].sessions.length) {
                sessionCount = activeUsers[username].sessions.length
            }
            if (sessionCount) {
                return spawn('!', [
                    ['i.hidden.sorting', zeroPad(sessionCount, 6)],
                    ['a.active-user', {
                        title: username + ': kill ' + sessionCount + ' active session(s)',
                        href: '#!',
                        style: { display: 'block', padding: '2px' }
                    }, [['img', { src: XNAT.url.rootUrl('/images/cg.gif') }]]]
                ])
            }
            else {
                return '<i class="hidden">-1</i>&mdash;'
            }
        }

        // renders cells for last login column
        function lastLoginInfo(value){
            value = realValue(value) || 0;
            return spawn('!', [
                ['i.last-login.hidden.sorting', (9999999999999-value || '9999999999999')+''],
                ['input.hidden.last-login.timestamp.filtering|type=hidden', { value: value }],
                ['span.date-time', (value ? (new Date(value)).toLocaleString() : '&mdash;')]
            ])
        }


        // Spawner element config for the users list table
        function spawnUsersTable(){

            // console.log(spawnUsersTable.name);

            //var _data = XNAT.xapi.users.profiles || XNAT.data['/xapi/users/profiles'];
            var $dataRows = [];

            // TODO:
            // TODO: set min-width as well as max-width
            // TODO:

            var styles = {
                id: (60-24)+'px',
                username: (160-24)+'px',
                name: (280-24) + 'px',
                email: (222-24) + 'px',
                verified: (104-24) + 'px',
                enabled: (96-24) +'px',
                active: (92-24) + 'px',
                login: (118-24) + 'px'
            };
            // var altStyles = {};
            // forOwn(styles, function(name, val){
            //     altStyles[name] = (val * 0.8)
            // });
            return {
                kind: 'table.dataTable',
                name: 'userProfiles',
                id: 'user-profiles',
                load: '/xapi/users/profiles',
                before: {
                    filterCss: {
                        tag: 'style|type=text/css',
                        content: '\n' +
                        '#user-profiles td.user-id { width: ' + styles.id + '; } \n' +
                        '#user-profiles td.username .truncate { width: 120px; } \n' +
                        '#user-profiles td.fullName .truncate { width: 180px; } \n' +
                        '#user-profiles td.email .truncate { width: 220px; } \n' +
                        '#user-profiles td.verified { width: ' + styles.verified + '; } \n' +
                        '#user-profiles td.enabled { width: ' + styles.enabled + '; } \n' +
                        '#user-profiles td.ACTIVE { width: ' + styles.active + '; } \n' +
                        '#user-profiles td.lastSuccessfulLogin { width: ' + styles.login + '; } \n' +
                        '#user-profiles tr.filter-verified, \n' +
                        '#user-profiles tr.filter-enabled, \n' +
                        '#user-profiles tr.filter-active, \n' +
                        '#user-profiles tr.filter-login { display: none; } \n' +
                        '@media screen and (max-width: 1200px) { \n' +
                        '    #user-profiles td.username .truncate { width: 90px; } \n' +
                        '    #user-profiles td.fullName .truncate { width: 100px; } \n' +
                        '    #user-profiles td.email .truncate { width: 140px; } \n' +
                        '}'
                    }
                },
                onRender: showUsersTable,
                table: {
                    classes: 'highlight hidden',
                    on: [
                        ['click', 'a.user-id', updateUserData],
                        ['click', 'a.select-all', selectAllUsers],
                        ['click', 'a.edit-user', editUser],
                        // ['click', 'a.full-name', userProjectsAndSecurity],
                        ['click', 'a.send-email', goToEmail],
                        // ['change', 'input.user-verified', setVerified],
                        // ['change', 'input.user-enabled', setEnabled],
                        ['click', 'a.active-user', killActiveSessions],
                        ['click', 'a.session-info', viewSessionInfo]
                    ]
                },
                trs: function(tr, data){
                    // 'this' is the currently iterating <tr>
                    data.userId = setRowId(data);
                    tr.id = data.userId;
                    addDataAttrs(tr, { filter: '0' });
                },
                sortable: 'username, fullName, email',
                filter: 'fullName, email',
                items: {
                    // _id: '~data-id',
                    // _username: '~data-username',
                    // _select: {
                    //     label: 'Select',
                    //     th: { html: '<a href="#!" class="select-all link">Select</a>' },
                    //     td: { className: 'centered' },
                    //     apply: function(){
                    //         return spawn('input.select-user', {
                    //             type: 'checkbox',
                    //             checked: false,
                    //             title: this.username + ':select'
                    //         });
                    //         //return '<a href="#!" class="username link">' + this.username + '</a>'
                    //     }
                    // },
                    //
                    id: {
                        label: 'ID',
                        sort: true,
                        // th: { style: { width: styles.id }},
                        td: {
                            // style: { width: styles.id },
                            className: 'user-id center'
                        },
                        apply: function(id){
                            return spawn('a.user-id.link', {
                                href: '#!',
                                title: this.username + ': refresh'
                            }, [['i.hidden', zeroPad(id, 6)], id]);
                        }
                    },
                    username: {
                        label: 'Username',
                        filter: true, // add filter: true to individual items to add a filter
                        // th: { style: { width: styles.username }},
                        // td: { style: { width: styles.username }},
                        apply: function(username, tr){
                            //console.log(tr);
                            // var _username = truncateText(username);
                            return spawn('a.username.link.truncate.edit-user', {
                                href: '#!',
                                title: username + ': details',
                                // html: _username,
                                html: username//,
                                // style: { width: styles.username },
                                // data: { username: username }
                            });
                        }
                    },
                    fullName: {
                        label: 'Name',
                        // th: { style: { width: styles.name }},
                        // td: { style: { width: styles.name }},
                        apply: function(){
                            // var _fullName = truncateText(this.lastName + ', ' + this.firstName);
                            var _fullName = (this.lastName + ', ' + this.firstName);
                            return spawn('a.full-name.link.truncate.edit-user', {
                                href: '#!',
                                title: this.username + ': project and security settings',
                                html: _fullName//,
                                // style: { width: styles.name },
                                // data: { username: this.username }
                            });
                            //return this.lastName + ', ' + this.firstName
                        }
                    },
                    email: {
                        label: 'Email',
                        // th: { style: { width: styles.email }},
                        // td: { style: { width: styles.email }},
                        apply: function(email){
                            // var _email = truncateText(email);
                            return spawn('a.send-email.link.truncate.edit-user', {
                                href: '#!',
                                title: email + ': send email',
                                // style: { width: styles.email },
                                // title: 'Send email to: ' + email,
                                // html: _email
                                html: email
                            })
                        }
                    },
                    verified: {
                        label: 'Verified',
                        // th: { style: { width: styles.verified }},
                        td: {
                            // style: { width: styles.verified },
                            className: 'verified center'
                        },
                        // custom filter menu
                        filter: function(table){
                            return spawn('div.center', [XNAT.ui.select.menu({
                                value: 'all',
                                options: [
                                    { label: 'All', value: 'all' },
                                    { label: 'Verified', value: '1' },
                                    { label: 'Unverified', value: '0' }
                                ],
                                element: filterMenuElement.call(table, 'verified')
                            }).element])
                        },
                        apply: function(){
                            return userStatusInfo.call(this, 'verified', 'unverified');
                        }
                    },
                    enabled: {
                        label: 'Enabled',
                        // th: { style: { width: styles.enabled }},
                        td: {
                            // style: { width: styles.enabled },
                            className: 'enabled center'
                        },
                        filter: function(table){
                            return spawn('div.center', [XNAT.ui.select.menu({
                                value: 'all',
                                options: [
                                    { label: 'All', value: 'all' },
                                    { label: 'Enabled', value: '1' },
                                    { label: 'Disabled', value: '0' }
                                ],
                                element: filterMenuElement.call(table, 'enabled')
                            }).element])
                        },
                        apply: function(){
                            return userStatusInfo.call(this, 'enabled', 'disabled');
                        }
                    },
                    // by convention, name 'custom' columns with ALL CAPS
                    // 'custom' columns do not correspond directly with
                    // a data item
                    ACTIVE: {
                        label: 'Active',
                        sort: true,
                        // th: { style: { width: styles.active }},
                        td: {
                            // style: { width: styles.active },
                            className: 'active center'
                        },
                        filter: function(table){
                            var $table = $(table);
                            return spawn('div.center', [XNAT.ui.select.menu({
                                value: 'all',
                                options: {
                                    all: 'All',
                                    active: 'Active',
                                    inactive: 'Inactive'
                                },
                                element: {
                                    id: 'user-filter-select-active',
                                    on: {
                                        change: function(){
                                            var selectedValue = $(this).val();
                                            var $rows = $table.find('tbody').find('tr');
                                            var FILTERCLASS = 'filter-active';
                                            if (selectedValue === 'all') {
                                                $rows.removeClass(FILTERCLASS);
                                                return;
                                            }
                                            $rows.addClass(FILTERCLASS);
                                            $rows.each(function(){
                                                var $row = $(this);
                                                var isActive = $row.find('a.active-user').length > 0;
                                                if (isActive && selectedValue === 'active') {
                                                    $row.removeClass(FILTERCLASS);
                                                    return;
                                                }
                                                if (!isActive && selectedValue === 'inactive') {
                                                    $row.removeClass(FILTERCLASS);
                                                }
                                            });
                                        }
                                    }
                                }
                            }).element])
                        },
                        apply: activeUserInfo
                    },
                    lastSuccessfulLogin: {
                        label: 'Last Login',
                        sort: true,
                        th: { className: 'last-login center' },
                        td: { className: 'last-login center mono' },
                        filter: function(table){
                            var MIN = 60*1000;
                            var HOUR = 60*60*1000;
                            var X8HRS = HOUR*8;
                            var X24HRS = HOUR*24;
                            var X7DAYS = X24HRS*7;
                            var X30DAYS = X24HRS*30;
                            return spawn('div.center', [XNAT.ui.select.menu({
                                value: 0,
                                options: {
                                    all: {
                                        label: 'All',
                                        value: 0,
                                        selected: true
                                    },
                                    lastHour: {
                                        label: 'Last Hour',
                                        value: HOUR
                                    },
                                    last8hours: {
                                        label: 'Last 8 Hrs',
                                        value: X8HRS
                                    },
                                    last24hours: {
                                        label: 'Last 24 Hrs',
                                        value: X24HRS
                                    },
                                    lastWeek: {
                                        label: 'Last Week',
                                        value: X7DAYS
                                    },
                                    last30days: {
                                        label: 'Last 30 days',
                                        value: X30DAYS
                                    },
                                    never: {
                                        label: 'Never',
                                        value: -1
                                    }
                                },
                                element: {
                                    id: 'user-filter-select-last-login',
                                    on: {
                                        change: function(){
                                            var FILTERCLASS = 'filter-login';
                                            var selectedValue = parseInt(this.value, 10);
                                            var currentTime = Date.now();
                                            $dataRows = $dataRows.length ? $dataRows : $$(table).find('tbody').find('tr');
                                            if (selectedValue === 0) {
                                                $dataRows.removeClass(FILTERCLASS);
                                            }
                                            else {
                                                $dataRows.addClass(FILTERCLASS).filter(function(){
                                                    var timestamp = this.querySelector('input.last-login.timestamp');
                                                    var lastLogin = +(timestamp.value);
                                                    return selectedValue === lastLogin-1 || selectedValue > (currentTime - lastLogin);
                                                }).removeClass(FILTERCLASS);
                                            }
                                        }
                                    }
                                }
                            }).element])
                        },
                        apply: lastLoginInfo
                    }
                }
            }
        }

        function showUsersTable(){
            // console.log('showUsersTable');
            // $$($table).removeClass('hidden');
            // $$($table).show();
        }
        usersGroups.showUsersTable = showUsersTable;

        // render or update the users table
        function renderUsersTable(container){
            // console.log('renderUsersTable');
            var $container = container ? $$(container) : $(userTableContainer);
            var _usersTable;
            if ($container.length) {
                $container.html('loading...');
                // setTimeout(function(){
                    _usersTable = XNAT.spawner.spawn({
                        usersTable: spawnUsersTable()
                    });
                    _usersTable.render($container.empty(), 200, showUsersTable);
                // }, 200);
                // return _usersTable;
            }
        }
        usersGroups.renderUsersTable = renderUsersTable;


        // TODO: re-render *only* the table rows, not the whole table
        function updateUsersTable(refresh){
            if (!refresh && XNAT.data['/xapi/users/profiles']) {
                getActiveUsers(function(){
                    renderUsersTable();
                });
                return {
                    done: function(callback){
                        callback.call()
                    }
                }
            }
            return XNAT.xhr.get({
                url: XNAT.url.restUrl('/xapi/users/profiles'),
                success: function(data){
                    XNAT.data['/xapi/users/profiles'] = data;
                    // console.log(data);
                    getActiveUsers(function(){
                        renderUsersTable();
                    });
                }
            })
        }
        usersGroups.updateUsersTable = updateUsersTable;

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

    usersGroups.setupTabs = setupTabs;

    usersGroups.spawnTabs = function(container){
        // console.log('usersGroups.spawnTabs');
        var tabsConfig = setupTabs();
        var $container = $$(container || XNAT.tabs.container);
        $container.html('loading...');
        setTimeout(function(){
            usersGroups.tabs = XNAT.spawner.spawn(tabsConfig);
            usersGroups.tabs.render($container.empty(), 20);
        }, 200);
        // usersGroups.tabs.done(usersGroups.showUsersTable);
    };

    // only render tabs if XNAT.tabs.container is defined
    if (XNAT.tabs && XNAT.tabs.container) {
        usersGroups.spawnTabs();
    }

    // this script has loaded
    usersGroups.loaded = true;

    return XNAT.admin.usersGroups = XNAT.usersGroups = usersGroups;

}));
