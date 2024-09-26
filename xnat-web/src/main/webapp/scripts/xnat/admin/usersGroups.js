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

    var userTableContainer = 'div#user-table-container';

    XNAT.admin = getObject(XNAT.admin || {});

    XNAT.admin.usersGroups = usersGroups =
        getObject(XNAT.admin.usersGroups || {});

    XNAT.usersGroups = extend(true, usersGroups, XNAT.usersGroups);

    var userAdminPage = (XNAT.page && XNAT.page.userAdminPage) || false;

    // usersGroups.showAdvanced = true;
    usersGroups.showAdvanced = userAdminPage;
    usersGroups.adminControls = userAdminPage;
    usersGroups.roles = ['Administrator','DataManager','SiteUser'];

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
        methods.usernames = methods.userList;
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


    // RETURNS A DOM ELEMENT
    usersGroups.userSwitchElement = function(username, type, status){
        return XNAT.ui.input.switchbox({
            value: firstDefined(status, '') + '',
            element: {
                name: type,
                className: 'user-' + type,
                checked: status,
                title: username + ':' + type
            },
            onText: 'Yes',
            offText: 'No'
        }).get();
    };


    // use this to spawn the user account form separately
    function renderUserAccountForm(data, container){
        return XNAT.spawner.spawn({
            userAccountForm: userAccountForm(data)
        }).render(container)
    }

    function saveUserData(form, opts){
        var $form = $$(form);
        // var username = $form.find('input#username').val();

        opts = cloneObject(opts);
        opts.cache = false;
        var successMsg = opts.msg || 'User info saved.';

        var doSubmit = $form.submitJSON(opts);

        if (doSubmit.done) {
            doSubmit.done(function(){
                // xmodal.loading.open();
                XNAT.ui.banner.top(2000, successMsg, 'success')
            });
        }
        if (doSubmit.fail) {
            doSubmit.fail(function(e){
                XNAT.ui.dialog.alert('' +
                    'An error occurred saving user data.' +
                    '<br>' +
                    '<div class="error">' + e + '</div>' +
                    '');
            });
        }
        return doSubmit;
    }


    // define the user properties dialog
    function editUserDialog(data, onclose){
        if (data && !data.username) {
            return XNAT.dialog.message('Error', 'An error occurred displaying user data.');
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
                        'over the entire site, <b>including access to all data</b>.' +
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
        return XNAT.dialog.open({
            width: 600,
            // height: 600,
            speed: 200,
            esc: true,
            enter: false,
            title: 'User Properties for <b>' + data.username + '</b>',
            content: XNAT.spawner.spawn(userForm()).get(),
            beforeShow: function(obj){
                if (usersGroups.adminUsers.indexOf(data.username) >= 0 && usersGroups.adminUsers.length === 1){
                    // special handling when editing the only active site administrator
                    obj.$dialog.find('.user-account-info').prepend(
                        spawn(
                            'div.warning',{ style: {'margin-bottom':'1em'}},
                            'You are editing the only active administrative user account. Certain features are restricted to ensure you do not leave this XNAT in an unmanaged state.'
                            )
                    );

                    // obj.$dialog.find('div[data-name=verified]').addClass('disabled')
                    //     .find('label.switchbox').addClass('disabled');
                    //
                    // obj.$dialog.find('div[data-name=enabled]').addClass('disabled')
                    //     .find('label.switchbox').addClass('disabled');

                    obj.$dialog.find('div[data-name=verified]').addClass('disabled').empty()
                        .append(spawn('!',[
                            spawn('div.element-label','Verified'),
                            spawn('div.element-wrapper','<i class="fa fa-check-circle-o"></i>')
                        ]));

                    obj.$dialog.find('div[data-name=enabled]').empty()
                        .append(spawn('!',[
                            spawn('div.element-label','Enabled'),
                            spawn('div.element-wrapper','<i class="fa fa-check-circle-o"></i>')
                        ]));
                }
            },
            buttons: [
                {
                    label: 'Save',
                    close: false,
                    isDefault: true,
                    action: function(obj){
                        var $form = obj.$modal.find('form');
                        if (XNAT.validate.form($form)){
                            var doSave = saveUserData($form);
                            doSave.done(function(){
                                updated = true;
                                obj.close();
                            });
                        }
                    }
                },
                {
                    label: 'Cancel',
                    close: true
                }
            ],
            onClose: function(obj){
                if (typeof onclose === 'function') {
                    onclose(obj)
                }
                if (updated) {
                    updateUserData(data.username);
                    updateUsersTable(true);
                    // renderUsersTable();
                }
            }
        })
    }


    // get user data and return AJAX promise object
    function getUserData(username){
        var _url = XNAT.url.restUrl('/xapi/users/profile/' + username);
        delete XNAT.data['/xapi/users/profile/' + username];
        delete XNAT.data['/xapi/users/profiles'];
        delete XNAT.data['/xapi/users/current'];
        delete XNAT.data['/xapi/users/' + username];
        return XNAT.xhr.get(_url)
    }


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

    function showUsersTable(){
        // console.log('showUsersTable');
        // $$($table).removeClass('hidden');
        // $$($table).show();
    }
    usersGroups.showUsersTable = showUsersTable;


    // render or update the users table
    function renderUsersTable(container, url){

        var $container = container ? $$(container) : $(userTableContainer);
        if ($container.length) {
            setTimeout(function(){
                $container.html('loading...');

                // begin by getting an updated user role map
                XNAT.xhr.get({
                    url: XNAT.url.restUrl('/xapi/users/rolemap'),
                    success: function(data){
                        XNAT.data['/xapi/users/rolemap'] = data;
                        XNAT.usersGroups.roles = Object.keys(data);
                        XNAT.usersGroups.adminUsers = data['Administrator'] || []; // Add special tracking for admin users

                        // proceed with rendering the table
                        setTimeout(function(){
                            XNAT.dialog.loadingBar.show();
                            XNAT.spawner.spawn({
                                usersTable: usersGroups.spawnUsersTable(url)
                            }).render($container.empty(), function(){
                                XNAT.dialog.loadingBar.hide();
                            });
                        }, 10);

                    },
                    failure: function(e) {
                        XNAT.dialog.message('Could not load user role map, which is required to display the user table');
                        console.warn(e);
                    }
                });
            }, 1);
            // return _usersTable;
        }
    }
    usersGroups.renderUsersTable = renderUsersTable;


    // TODO: re-render *only* the table rows, not the whole table
    function updateUsersTable(refresh, all){
        var URL = all ? '/xapi/users/profiles' : '/xapi/users/current';
        if (!refresh && XNAT.data[URL]) {
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
            url: XNAT.url.restUrl(URL),
            success: function(data){
                XNAT.data[URL] = data;
                // console.log(data);
                getActiveUsers(function(){
                    renderUsersTable();
                });
            }
        })
    }
    usersGroups.updateUsersTable = updateUsersTable;


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


    // get profile data for users considered 'current'
    function getCurrentUsers(success, failure){
        return XNAT.xhr.get({
            url: XNAT.url.restUrl('/xapi/users/current'),
            success: function(data){
                XNAT.data['/xapi/users/current'] = data;
                if (isFunction(success)) {
                    success.apply(this, arguments);
                }
            },
            failure: failure
        })
    }
    usersGroups.getCurrentUsers = getCurrentUsers;


    // renders cells for 'verified' and 'enabled' status
    function userStatusInfo(type, off){
        var username = escapeHtml(this.username || '');
        var status = realValue(this[type]);
        var SORTER = status ? 1 : 0;
        var iconClass = status ? '.fa-check' : '';
        return spawn('!', [
            ['i.hidden.sorting.filtering.' + type, SORTER+''],
            ['a.user-' + type + '-status.edit-user', {
                title: username + ': ' + (status ? type : off),
                href: '#!',
                style: { display: 'block', padding: '2px' }
            }, [['i.fa' + iconClass ]]]
        ]);
    }


    // renders cells for 'active' users column
    function activeUserInfo(){
        var username = escapeHtml(this.username || '');
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
                }, [['i.fa.fa-user-circle']]]
            ])
        }
    }


    // renders cells for last login column
    function lastLoginInfo(value){
        value = realValue(value) || 0;
        return spawn('!', [
            ['i.last-login.hidden.sorting', (9999999999999-value || '9999999999999')+''],
            ['input.hidden.last-login.timestamp.filtering|type=hidden', { value: value }],
            ['span.date-time', (value ? (new Date(value)).toLocaleString() : '')]
        ])
    }


    // set unique id attribute for <tr> elements
    // by combining username and user id
    function setRowId(profile){
        return profile.username + '-' + profile.id;
    }


    // update ONLY the row of the edited user
    function updateUserRow(profile){

        var rowId = setRowId(profile);
        var $row  = $(document.getElementById(rowId));

        // full name column
        $row.find('a.full-name')
            .html(escapeHtml(profile.lastName + ', ' + profile.firstName));

        // email column
        $row.find('a.send-email')
            .attr('title', profile.email + ': send email')
            .html(escapeHtml(profile.email));

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
        var USER_URL = USERNAME === 'profiles' ? '/xapi/users/profiles' : '/xapi/users/profile/' + USERNAME;
        return XNAT.xhr.get({
            url: XNAT.url.restUrl(USER_URL),
            success: function(DATA){
                // XNAT.data[USER_URL] = DATA;
                if (userAdminPage) {
                    window.setTimeout(function(){
                        getActiveUsers().done(function(ACTIVE){
                            XNAT.data['/xapi/users/active'] = ACTIVE;
                            forEach([].concat(DATA), function(profile, i){
                                updateUserRow(profile);
                            });
                        })
                    }, delay||100);
                }
            }
        })
    }

    usersGroups.changePasswordDialog = function(data){
        function changePasswordForm(){
            var form = {
                kind: 'panel.form',
                name: 'changeUserPasswordForm',
                id: 'change-user-password-form',
                label: 'Change Password',
                footer: false,
                validate: true,
                method: 'PUT',
                contentType: 'json',
                refresh: false,
                reload: true,
                action: '~/xapi/users/' + data.username,
                element: {
                    autocomplete: 'off'
                },
                contents: {
                    username: {
                        kind: 'panel.input.hidden',
                        value: data.username
                    },
                    password: {
                        kind: 'panel.input.password',
                        label: 'Password',
                        element: {
                            placeholder: '*****',
                            autocomplete: 'off',
                            data: { message: passwordComplexityMessage }
                        },
                        validate: 'required pattern:'+passwordComplexity+' max-length:255'
                    },
                    confirmPassword: {
                        kind: 'panel.input.password',
                        label: 'Confirm Password',
                        element: {
                            placeholder: '*****',
                            autocomplete: 'off',
                            data: { message: 'Password fields must match' }
                        },
                        validate: 'matches:[name=password]'
                    }
                }
            };
            return form;
        }

        let cpForm$ = null;
        let updated = false;
        let formContainer$ = null;
        XNAT.dialog.open({
            title: 'Change Password for '+data.username,
            width: 500,
            content: '<div id="change-password-form"></div>',
            beforeShow: function(obj){
                formContainer$ = obj.dialog$.find('#change-password-form');
                XNAT.spawner.spawn({
                    changePasswordForm: changePasswordForm()
                }).render(formContainer$);
            },
            afterShow: function(){
                cpForm$ = formContainer$.find('form');
            },
            buttons: [
                {
                    label: 'Update Password',
                    close: false,
                    isDefault: true,
                    action: function(obj){
                        var doSave = saveUserData(cpForm$, { msg: 'User password updated successfully' });
                        doSave.done(function(){
                            updated = true;
                            obj.close();
                        });
                    }
                },
                {
                    label: 'Cancel',
                    close: true
                }
            ]
        });
    };


    function userAccountForm(data){

        data = data || {};

        var usernameEsc = data.username = escapeHtml(data.username || '');

        var _load = data ? XNAT.url.restUrl('/xapi/users/profile/' + data.username) : false;

        var doEdit = _load && data.username;

        function checkUserAccountIsManagedInternally() {
            if (data.username === "") {
                return false;
            }
            let isUserAccountManagedInternally = false;
            XNAT.xhr.get({
                url: XNAT.url.restUrl('xapi/users/authDetails/'+data.username),
                async: false,
                fail: function(e){
                    errorHandler(e, 'Could not fetch authentication method');
                },
                success: function(userAuthData){
                    isUserAccountManagedInternally = userAuthData.some(el => el.authMethod === 'localdb');
                }
            });
            return isUserAccountManagedInternally;
        }


        // username could be text or input element
        function usernameField(username){
            var obj = {
                label: 'Username'
            };
            if (data && data.username) {
                obj.kind = 'panel.element';
                obj.contents = {
                    usernameText: {
                        kind: 'html',
                        content: usernameEsc
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
                obj.validate = 'username required';
                obj.value = '';
            }
            return obj;
        }

        function passwordField(){
            var obj = {
                label: 'Password'
            };
            let isUserAccountManagedInternally = checkUserAccountIsManagedInternally();
            if (data && data.username) {
                let disabledBtn = isUserAccountManagedInternally?'':' disabled ';
                obj.kind = 'panel.element';
                obj.contents = {
                    changePasswordLink: {
                        kind: 'html',
                        content:
                            '<a href="#!" class="change-password" style="display: inline-block; margin: -4px 0 4px;" data-username="'+data.username+'">' +
                            '<button class="btn btn-sm" ' + disabledBtn + '>Change User Password</button></a>'
                    },
                    description: {
                        tag: 'div.description',
                        content: isUserAccountManagedInternally?'':'Configured authentication provider for this user account does not allow password change through this interface. Please contact your site administrator for assistance.'
                    }
                }
            }
            else {
                obj.kind = 'panel.input.password';
                obj.element = {
                    placeholder: '*****',
                    autocomplete: 'off',
                    data: { message: passwordComplexityMessage }
                };
                obj.validate = 'allow-empty pattern:'+passwordComplexity+' max-length:255'
            }
            return obj;
        }

        function confirmPasswordField(){
            if (data && data.username) {
                return false;
            }
            else {
                return {
                    kind: 'panel.input.password',
                    label: 'Confirm Password',
                    element: {
                        placeholder: '*****',
                        autocomplete: 'off',
                        data: { message: 'Password fields must match' }
                    },
                    validate: 'matches:[name=password]'
                }
            }
        }

        var userVerified = data.verified || 'false';
        var userEnabled = data.enabled || 'false';

        var form = {
            kind: 'panel.form',
            name: 'userAccountForm',
            id: 'user-account-form',
            label: 'Account Information',
            footer: false,
            validate: true,
            method: doEdit ? 'PUT' : 'POST',
            contentType: 'json',
            load: doEdit ? '$? *' + _load : '',
            refresh: true,
            reload: true,
            action: '~/xapi/users' + (doEdit ? ('/' + data.username) : ''),
            element: {
                autocomplete: 'off'
            },
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
                // pad: {
                //     html: '<br>'
                // },
                usernameField: usernameField(),
                password: passwordField(),
                confirmPassword: confirmPasswordField(),
                firstName: {
                    kind: 'panel.input.text',
                    label: 'First Name',
                    validate: 'name-safe required',
                    value: data.firstName || ''
                },
                lastName: {
                    kind: 'panel.input.text',
                    label: 'Last Name',
                    validate: 'name-safe required',
                    value: data.lastName || ''
                },
                email: {
                    kind: 'panel.input.email',
                    label: 'Email',
                    validate: 'email required',
                    value: data.email || ''
                }
            }
        };

        // only show admin controls if this form is being shown from within the Admin Users UI
        if (usersGroups.adminControls) {
            form.contents.verified = {
                kind: 'panel.input.switchbox',
                    label: 'Verified',
                    options: 'true|false',
                    value: userVerified,
                    // checked: !/false/i.test(userVerified)//,
                    element: {
                    // disabled: !!_load,
                    checked: !/false/i.test(userVerified)//,
                    // title: username + ':verified'//,
                    // on: { click: _load ? setVerified : diddly }
                }
            };
            form.contents.enabled = {
                kind: 'panel.input.switchbox',
                label: 'Enabled',
                options: 'true|false',
                value: userEnabled,
                // checked: !/false/i.test(userEnabled)//,
                element: {
                    //disabled: !!_load,
                    checked: !/false/i.test(userEnabled)//,
                    //title: username + ':enabled'//,
                    //on: { click: _load ? setEnabled : diddly }
                }
            };

            if (doEdit) {
                form.contents.authId = {
                    kind: 'panel.element',
                    label: 'Authentication',
                    contents: {
                        link: {
                            tag: 'a.view-auth-details.link',
                            element: {
                                href: '#!',
                                on: {
                                    click: function (e) {
                                        e.preventDefault();
                                        const $waitElement = $('<span class="spinner text-info"><i class="fa fa-spinner fa-spin"></i> Loading...</span>');
                                        $(this).after($waitElement);
                                        XNAT.xhr.getJSON({
                                            url: restUrl('/xapi/users/authDetails/' + data.username),
                                            error: function (e) {
                                                $waitElement.remove();
                                                console.warn(e);
                                                XNAT.ui.dialog.message('Unable to retrieve user auth details');
                                            },
                                            success: function (auth) {
                                                function formatDate(value) {
                                                    return value ? (new Date(value)).toLocaleString() : '';
                                                }

                                                let table = {
                                                    kind: 'table.dataTable',
                                                    name: 'containerHistoryTable',
                                                    id: 'container-history-table',
                                                    data: auth,
                                                    table: {
                                                        classes: 'highlight hidden'
                                                    },
                                                    items: {
                                                        authMethod: {
                                                            label: 'Auth method',
                                                            filter: true,
                                                            sort: true,
                                                            apply: function () {
                                                                return this['authMethod'];
                                                            }
                                                        },
                                                        authMethodId: {
                                                            label: 'Auth method id',
                                                            filter: true,
                                                            sort: true,
                                                            apply: function () {
                                                                return this['authMethodId'];
                                                            }
                                                        },
                                                        authUser: {
                                                            label: 'Username (remote)',
                                                            filter: true,
                                                            sort: true,
                                                            apply: function () {
                                                                return this['authUser'];
                                                            }
                                                        },
                                                        lastLoginAttempt: {
                                                            label: 'Last login attempt',
                                                            sort: true,
                                                            apply: function () {
                                                                return formatDate(this['lastLoginAttempt']);
                                                            }
                                                        },
                                                        lastSuccessfulLogin: {
                                                            label: 'Last successful login',
                                                            sort: true,
                                                            apply: function () {
                                                                return formatDate(this['lastSuccessfulLogin']);
                                                            }
                                                        },
                                                        failedLoginAttempts: {
                                                            label: 'Failed login attempts',
                                                            sort: true,
                                                            apply: function () {
                                                                return this['failedLoginAttempts'];
                                                            }
                                                        }
                                                    }
                                                };
                                                XNAT.ui.dialog.open({
                                                    title: 'Authentication details for ' + data.username,
                                                    width: 850,
                                                    header: true,
                                                    maxBtn: true,
                                                    content: XNAT.spawner.spawn({authTable: table}).get(),
                                                    afterShow: function () {
                                                        $waitElement.remove();
                                                    },
                                                    buttons: [
                                                        {
                                                            label: 'Close',
                                                            isDefault: true,
                                                            close: true
                                                        }
                                                    ]
                                                });
                                            }
                                        });
                                    }
                                }
                            },
                            content: 'View Authentication Details'
                        }
                    }
                };
            }
        }

        // add 'Advanced Settings' when editing existing user
        if (doEdit && usersGroups.showAdvanced) {
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
                                    var modalId = $(this).closest('div.xnat-dialog').attr('data-dialog');
                                    XNAT.dialog.getDialog(modalId).close();
                                    window.top.usernameForUserThatIsCurrentlyBeingEdited = data.username;
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

        // Add a submit button if this form is being displayed outside the Admin Users UI
        if (doEdit && !usersGroups.adminControls) {
            form.contents.submitButton = {
                kind: 'panel.element',
                contents: {
                    submitUserFormButton: {
                        kind: 'html',
                        content: '<button class="btn primary" id="save-user-profile">Update Profile</button>'
                    }
                }
            }
        }

        usersGroups.showAdvanced = true;

        return form;

    }
    usersGroups.userAccountForm = userAccountForm; 

    // open a separate dialog to edit user password
    $(document).on('click','.change-password',function(){
        var username = $(this).data('username');
        usersGroups.changePasswordDialog({ username: username });
    });

    // external user profile form validation and submission
    $(document).on('click','#save-user-profile',function(){
        var $form = $(this).parents('form'), updated;
        if (XNAT.validate.form($form)){
            var doSave = saveUserData($form);
            doSave.done(function(){
                updated = true;
            });
        }
    });

    // open a dialog to edit user properties
    function editUser(e, onclose){
        e.preventDefault();
        var username =
                (this.title || '').split(':')[0] ||
                $(this).data('username') ||
                $(this).closest('tr').data('username') ||
                (this.innerText || '').trim();
        window.top.usernameForUserThatIsCurrentlyBeingEdited = username;
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


    function userProjectsAndSecurity(e, usr){
        var _username = usr || $(this).data('username');
        var _url = XNAT.url.rootUrl('/app/action/DisplayItemAction/search_value/' + _username + '/search_element/xdat:user/search_field/xdat:user.login/popup/true');
        return xmodal.iframe({
            src: _url,
            name: 'advanced-user-settings',
            width: 850,
            height: '80%',
            title: 'Edit User Info',
            // footer: false,
            okLabel: 'Close',
            cancel: false,
            beforeShow: function(){
                XNAT.dialog.close(XNAT.dialog.topUID, true);
            },
            onClose: function(){
                var editedUser = window.top.usernameForUserThatIsCurrentlyBeingEdited;
                if (!window.top.XNAT.page.userAdminPage) {
                    return false;
                }
                if (editedUser){
                    usersGroups.userData(editedUser).getProfile().done(function(profileData){
                        updateUserRow(profileData)
                    });
                }
                else {
                    updateUsersTable(true);
                }
            }
        })
    }


    // open a dialog for creating a new user
    function newUserDialog(){
        var updated = false;
        var formContainer$ = null;
        return XNAT.dialog.open({
            width: 600,
            // height: 500,
            speed: 200,
            title: 'Create New User',
            content: (function(){
                formContainer$ = $.spawn('div.new-user-form');
                renderUserAccountForm(null, formContainer$);
                return formContainer$[0];
            })(),
            buttons: [
                {
                    label: 'Save',
                    close: false,
                    isDefault: true,
                    action: function(obj){
                        // userAccountForm
                        var userForm$ = formContainer$.find('form');
                        var _username = userForm$.find('input[name="username"]').val();
                        // make sure new username is not a duplicate
                        var getUserList = usersGroups.userData().usernames();
                        getUserList.done(function(users){
                            if (users.indexOf(_username) > -1) {
                                XNAT.ui.dialog.alert('The username ' +
                                    '<b>' + _username + '</b> is already in use. ' +
                                    'Please enter a different username value and ' +
                                    'try again.');
                                return false;
                            }

                            var doSave = saveUserData(userForm$);
                            doSave.done(function(){
                                updated = true;
                                obj.close();
                            });
                        });
                    }
                }
            ],
            onClose: function(){
                // always update the whole table when adding a user.
                // usersGroups.spawnTabs();
                // renderUsersTable();
                if (updated) {
                    updateUsersTable(true);
                }
            }
        })
    }


    function setupTabs(){

        // console.log(setupTabs.name);

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

        function reloadUserListButton(){
            return {
                tag: 'button#reload-user-list',
                element: {
                    html: 'Reload User List',
                    style: {
                        marginLeft: '20px'
                    },
                    on: {
                        click: function(e){
                            // console.log('clicked');
                            // updateUserData('profiles', 10);
                            // var $container = $('#user-table-container');
                            // $container.empty().html('loading...');
                            // spawnUsersTable().render($container.empty());
                            // usersGroups.spawnTabs();
                            setTimeout(function(){
                                XNAT.dialog.loadingBar.show();
                                updateUsersTable(true);
                            }, 1);
                        }
                    }
                }
            }
        }

        function loadAllUsersButton(){
            return {
                tag: 'button#load-all-users',
                element: {
                    html: 'Load All Users',
                    style: {
                        marginLeft: '10px'
                    },
                    on: {
                        click: function(e){
                            setTimeout(function(){
                                XNAT.dialog.loadingBar.show();
                                renderUsersTable($(userTableContainer), '/xapi/users/profiles');
                            }, 1);
                        }
                    }
                }
            }
        }

        function allUsersButtonInfoText(){
            return {
                tag: 'span.tip.shadowed',
                element: {
                    style: {
                        width: '350px',
                        left: '-385px',
                        top: '-75px',
                        // backgroundColor: '#ffc',
                        zIndex: 10000
                    }
                },
                content: [
                    'By default, the table below only shows "current" users: users that are <b>enabled</b>, or who ' +
                    'may be <b>disabled</b> but have logged in during the past year, or have an unused account created less ' +
                    'than a year ago.' +
                    '<br><br>' +
                    'Click the <b>"Load All Users"</b> button to view <i>all</i> users that have accounts on this system, ' +
                    'even if their account is not "current."'
                ],
                filler: null
            }
        }

        // var alternateInfoText = '' +
        //
        //     'By default, the table below only shows "current" users: users that are <b>enabled</b>, users that have ' +
        //     'logged in during the past year but are <b>disabled</b>, or users with accounts that were ' +
        //     'created created in the past year but never used.' +
        //     '<br><br>' +
        //     'Click the <b>"Load All Users"</b> button to view <i>all</i> users that have accounts on this system, ' +
        //     'even if their account is not "current."' +
        //
        //     '';

        function allUsersButtonInfo(){
            return {
                tag: 'span.tip_icon',
                element: {
                    style: {
                        marginRight: '3px',
                        left: '2px',
                        top: '3px'
                    }
                },
                contents: {
                    allUsersButtonInfoText: allUsersButtonInfoText()
                }
            }
        }

        function allUsersButtonElements(){
            return {
                tag: 'div',
                element: {
                    style: {
                        float: 'right'
                    }
                },
                contents: {
                    allUsersButtonInfo: allUsersButtonInfo(),
                    loadAllUsersButton: loadAllUsersButton()
                }
            }
        }

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
                                position: 'relative',
                                marginBottom: '30px',
                                paddingTop: '30px',
                                borderTop: '1px solid #c8c8c8'
                            }
                        },
                        contents: {
                            createUserButton: createUserButton(),
                            reloadUserListButton: reloadUserListButton(),
                            allUsersButtonElements: allUsersButtonElements()
                        }
                    },
                    usersTablePanel: {
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

        // kill all active sessions for the specified user
        function killActiveSessions(e){
            e.preventDefault();
            var username = this.title.split(':')[0].trim();
            return XNAT.dialog.confirm({
                title: 'Kill Active Sessions?',
                content: 'Kill all active sessions for <b>' + username + '</b>?',
                okClose: false,
                okAction: function(obj){
                    XNAT.xhr['delete']({
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

        // Spawner element config for the users list table
        function spawnUsersTable(url){

            // console.log(spawnUsersTable.name);

            //var _data = XNAT.xapi.users.profiles || XNAT.data['/xapi/users/profiles'];
            var $dataRows = [];

            // load 'current' users initially
            var URL = XNAT.url.restUrl(url || '/xapi/users/current');

            // TODO:
            // TODO: set min-width as well as max-width
            // TODO:

            var styles = {
                id: (60-24)+'px',
                username: (120-24)+'px',
                name: (240-24) + 'px',
                email: (182-24) + 'px',
                roles: (120-24) + 'px',
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
                load: URL,
                before: {
                    filterCss: {
                        tag: 'style|type=text/css',
                        content: '\n' +
                        '#user-profiles td.user-id { width: ' + styles.id + '; } \n' +
                        '#user-profiles td.username .truncate { width: 100px; } \n' +
                        '#user-profiles td.fullName .truncate { width: 160px; } \n' +
                        '#user-profiles td.email .truncate { width: 180px; } \n' +
                        '#user-profiles td.roles .truncate { width: 100px; } \n' +
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
                        '    #user-profiles td.roles .truncate { width: 70px; } \n' +
                        '} \n' +
                        '#user-account-form, #user-account-form-panel { border: none; margin: 0; }'
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
                filter: 'fullName, email, roles',
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
                        apply: function(){
                            // var _username = truncateText(username);
                            return spawn('a.username.link.truncate.edit-user', {
                                href: '#!',
                                title: this.username + ': details',
                                html: escapeHtml(this.username)//,
                                // data: { username: username }
                            });
                        }
                    },
                    fullName: {
                        label: 'Name',
                        apply: function(){
                            // var _fullName = truncateText(this.lastName + ', ' + this.firstName);
                            return spawn('a.full-name.link.truncate.edit-user', {
                                href: '#!',
                                title: this.username + ': project and security settings',
                                html: escapeHtml(this.lastName + ', ' + this.firstName)//,
                                // data: { username: this.username }
                            });
                            //return this.lastName + ', ' + this.firstName
                        }
                    },
                    email: {
                        label: 'Email',
                        apply: function(){
                            return spawn('a.send-email.link.truncate.edit-user', {
                                href: '#!',
                                title: this.email + ': send email',
                                // title: 'Send email to: ' + email,
                                // html: _email
                                html: escapeHtml(this.email)
                            })
                        }
                    },
                    roles: {
                        label: 'Roles',
                        filter: true,
                        apply: function(){
                            if (XNAT.data['/xapi/users/rolemap'] === undefined) return false;

                            var userRoles = [], username=this.username;
                            XNAT.usersGroups.roles.forEach(function(role){
                                if (role === 'SiteUser' || role === 'DataManager') return false; // ignore this bit of XNAT cruft
                                if (XNAT.data['/xapi/users/rolemap'][role].indexOf(username) >= 0) userRoles.push(role);
                            });
                            var userRoleTxt = userRoles.length ? userRoles.join('<br>') : '';
                            return spawn('span.truncate',{
                                style: { 'font-size':'82.5%', 'text-transform':'uppercase' },
                                title: userRoleTxt
                            }, userRoleTxt);
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
                            className: 'active center mono'
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
                            var HOUR = MIN*60;
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
                                                    if (selectedValue === -1) {
                                                        // special handling for users that have never logged in
                                                        return lastLogin === 0;
                                                    }
                                                    else {
                                                        return selectedValue === lastLogin-1 || selectedValue > (currentTime - lastLogin);
                                                    }
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
        usersGroups.spawnUsersTable = spawnUsersTable;

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

        // begin by initializing the rolemap
        XNAT.xhr.get({
            url: XNAT.url.restUrl('/xapi/users/rolemap'),
            success: function (data) {
                XNAT.data['/xapi/users/rolemap'] = data;
                XNAT.usersGroups.roles = Object.keys(data);
                XNAT.usersGroups.adminUsers = data['Administrator'] || []; // Add special tracking for admin users

                // proceed with rendering the table

                setTimeout(function () {
                    XNAT.dialog.loadingBar.show();
                    usersGroups.tabs = XNAT.spawner.spawn(tabsConfig);
                    usersGroups.tabs.render($container.empty(), function () {
                        XNAT.dialog.loadingBar.hide()
                    });
                }, 1);
                // usersGroups.tabs.done(usersGroups.showUsersTable);
            },
            failure: function (e) {
                XNAT.dialog.message('Could not load user role map, which is required to display the user table');
                console.warn(e);
            }
        });
    };

    // only render tabs if XNAT.tabs.container is defined
    if (XNAT.tabs && XNAT.tabs.container) {
        usersGroups.spawnTabs();
    }

    // this script has loaded
    usersGroups.loaded = true;

    return XNAT.admin.usersGroups = XNAT.usersGroups = usersGroups;

}));
//# sourceURL=browsertools://scripts/xnat/admin/usersGroups.js
