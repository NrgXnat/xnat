
/*
 * web: userAccessMgmt.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

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
    var projectAccess,
        undefined,
        userTableContainer = $('#user_mgmt_div'),
        rootUrl = XNAT.url.rootUrl,
        csrfUrl = XNAT.url.csrfUrl,
        projectId = XNAT.data.context.projectID,
        siteName = XNAT.data.context.siteName,
        currAccessibility = $('#current_accessibility').val(),
        allUsers,
        allUsernames,
        availableUsers,
        projectUsers,
        projectUsernames,
        groups;

    XNAT.projectAccess = projectAccess =
        getObject(XNAT.projectAccess || {});

    XNAT.projectAccess.allUsers = allUsers = [];
    XNAT.projectAccess.availableUsers = availableUsers = [];
    XNAT.projectAccess.projectUsers = projectUsers = [];
    XNAT.projectAccess.groups = groups = [];

    function errorHandler(e){
        console.log(e);
        e.status = e.status || '';
        XNAT.ui.dialog.open({
            title: 'Error',
            content: '<p><strong>Error ' + e.status + ': '+ e.statusText+'</strong></p><p>' + e.responseText + '</p>',
            buttons: [{
                    label: 'OK',
                    action: function () { XNAT.ui.dialog.closeAll(); }
            }]
        });
    }

    var projectGroupsUrl = function(){
        return rootUrl('/REST/projects/'+projectId+'/groups');
    };
    var projectUsersUrl = function(showDeactivatedUsers){
        showDeactivatedUsers = (showDeactivatedUsers) ? '/true' : '';
        return csrfUrl('/REST/projects/'+projectId+'/users'+ showDeactivatedUsers );
    };
    var xnatUsersUrl = function(userId){
        return (userId) ? rootUrl('/REST/users/'+userId+'?format=json') : rootUrl('/REST/users?format=json');
    };
    var addUserRoleUrl = function(user,group,sendemail){
        if (!user || !group) return false;
        sendemail = (sendemail) ? '&sendemail=true' : '';
        return csrfUrl('/REST/projects/'+projectId+'/users/'+group+'/'+user+'?format=json'+sendemail);
    };
    var removeUserUrl = function(user,group){
        if (!user || !group) return false;
        return csrfUrl('/REST/projects/'+projectId+'/users/' + group + '/' + user);
    };

    /*
     * functions to add, change or remove user
     */
    XNAT.projectAccess.setUserAccess = function(user,group,opts){ // opts: { sendEmail, hideNotification, notificationMessage }
        if (!user || !group.length) return false;
        opts = opts || { hideNotification: false };
        var sendemail = (opts) ? opts.sendEmail : false;
        XNAT.xhr.putJSON({
            url: addUserRoleUrl(user,group,sendemail),
            success: function(){
                if (!opts.hideNotification) {
                    var message = (opts.notificationMessage) ? opts.notificationMessage : 'Access level updated for <b>' + user + '</b>.';
                    XNAT.ui.banner.top(2000, message, 'success');
                }
                if(user == username){
                    // If the user changes their own access level, we need to reload
                    location.reload();
                }
            },
            fail: function(e){
                errorHandler(e);
            },
            always: function(){
                renderUsersTable();
            }
        })
    };

    XNAT.projectAccess.removeUser = function(user,group){
        if (!user || !group) return false;
        XNAT.xhr.delete({
            url: removeUserUrl(user,group),
            success: function(){
                XNAT.ui.banner.top(2000, '<b>' + user + '</b> removed from project.', 'success');
                if(user == username){
                    // If the user removes themselves, we need to reload the page
                    location.reload();
                }
            },
            fail: function(e){
                errorHandler(e);
            },
            always: function(){
                renderUsersTable();
            }
        })
    };

    XNAT.projectAccess.updateProjectUser = function(user,group){
        if (!user || !group) {
            errorHandler({
                statusText: 'Function: updateProjectUser',
                responseText: 'Cannot add user ('+user+') to group ('+group+')'
            });
            return false;
        }
        XNAT.ui.dialog.confirm({
            title: false,
            content: "<p>You have updated the user role for <b>" + user + "</b> for this project. Do you want to send a confirmation email?</p>",
            okLabel: "Send Email",
            okAction: function(){
                XNAT.projectAccess.setUserAccess(user, group, { sendEmail: true, notificationMessage: 'Email sent to ' + user + '.' });
                XNAT.ui.dialog.closeAll();
                xmodal.closeAll();
            },
            cancelLabel: "No",
            cancelAction: function(){
                XNAT.projectAccess.setUserAccess(user, group, { sendEmail: false, hideNotification: true });
                XNAT.ui.dialog.closeAll();
                xmodal.closeAll();
            }
        });
    };


    /*
     * populate user tables
     */

    // table cell formatting
    function truncCell(val, truncClass) {
        var elClass = truncClass ? 'truncate ' + truncClass : 'truncate';
        var valEsc = escapeHtml(val);
        return spawn('span', {
            className: elClass,
            title: val,
            html: valEsc
        });
    }

    function selectGroup(e){
        e.preventDefault();
        XNAT.projectAccess.updateProjectUser(this.title, this.value);
    }

    function groupsMenu(login, selected){
        selected = selected || '';
        // set to empty array if something is selected
        // this prevents the empty option from being rendered
        // since an empty array would get concatenated below
        var options = selected ? [] : [{
            value: "",
            html: "Select..."
        }];
        return XNAT.ui.select.menu({
            value: selected,
            element: {
                className: 'select-group',
                title: login,
                style: { width: '100%' }
            },
            options: options.concat(XNAT.projectAccess.groups.map(function(group){
                return {
                    value: group.id,
                    html: escapeHtml(group.displayname)
                }
            }))
        }).get();
    }

    // function groupSelect(login, groupSelection){
    //     // if a user already belongs to this project, a group selection will be specified. This changes the behavior of the select.
    //     groupSelection = groupSelection || '';
    //     var selector = (groupSelection)
    //         ? '<select class="select-group" onchange="XNAT.projectAccess.updateProjectUser(\''+login+'\', this.value)">'
    //         : '<select><option value selected></option>';
    //
    //     XNAT.projectAccess.groups.forEach(function(group){
    //         var groupSelected = (groupSelection === group.id) ? ' selected' : '';
    //         selector += '<option value="'+group.id+'" '+ groupSelected +'>'+ group.displayname + '</option>';
    //     });
    //     selector += '</select>';
    //     return selector;
    // }

    function removeUser(e){
        e.preventDefault();
        var userParts = (this.title.split(':')[1]||'').trim().split('|');
        var _login = userParts[0];
        var _group = userParts[1];
        console.log('remove user: ' + _login);
        projectAccess.removeUser(_login, _group);
    }

    // all users get the same menu options,
    // so just create them once (as an HTML string)
    // this will be injected into individual <select>
    // elements then the value will be set
    var groupOptions = XNAT.projectAccess.groups.map(function(group){
        return spawn.html('option', {
            value: group.id,
            innerHTML: group.displayname
        })
    }).join('');

    // display list of users with access to project
    function spawnUserTable(showDisabled){

        var URL = (showDisabled) ? projectUsersUrl(true) : projectUsersUrl();

        var colWidths = {
            login: '16%',
            firstname: '16%',
            lastname: '16%',
            email: '26%',
            group: '16%',
            remove: '10%'
        };

        var projectUsersContainer = spawn('div#project-user-list.table-group-container');

        var DATA_FIELDS = 'login, firstname, lastname, email';

        XNAT.table.dataTable([], {
            container: projectUsersContainer,
            body: false,
            sortable: DATA_FIELDS,
            filter: DATA_FIELDS,
            overflowY: 'scroll',
            table: {
                classes: 'table-group-member table-header',
                style: { tableLayout: 'fixed' }
            },
            columns: {
                login: {
                    label: 'Username',
                    // sortable: true,
                    th: { style: { width: colWidths.login } }
                },
                firstname: {
                    label: 'First Name',
                    // sortable: true,
                    th: { style: { width: colWidths.firstname } }
                },
                lastname: {
                    label: 'Last Name',
                    // sortable: true,
                    th: { style: { width: colWidths.lastname } }
                },
                email: {
                    label: 'Email',
                    // sortable: true,
                    th: { style: { width: colWidths.email } }
                },
                GROUP_ID: {
                    label: 'Group',
                    // sortable: true,
                    th: { style: { width: colWidths.group } }
                },
                remove: {
                    label: 'Remove',
                    th: { style: { width: colWidths.remove } }
                }
            }
        });

        XNAT.table.dataTable(projectAccess.projectUsers, {
            // kind: 'table.dataTable',
            container: projectUsersContainer,
            load: URL, // this should only do a REST call if the cached data is not available
            header: false,
            // sortable: DATA_FIELDS,
            height: projectAccess.projectUsers.length < 15 ? 'auto' : '592px',
            overflowY: 'scroll',
            table: {
                name: 'project-users',
                id: 'project-users',
                classes: 'table-group-member table-data compact rows-only highlight',
                style: { tableLayout: 'fixed' },
                on: [
                    ['click', '.remove-user', removeUser],
                    ['change', '.select-group', selectGroup]
                ]
            },
            columns: {
                login: {
                    label: 'Username',
                    // sortable: true,
                    td: { style: {
                        verticalAlign: 'middle',
                        width: colWidths.login
                    }},
                    apply: function(login){
                        return truncCell.call(this, login, '');
                    }
                },
                firstname: {
                    label: 'First Name',
                    // sortable: true,
                    td: { style: {
                        verticalAlign: 'middle',
                        width: colWidths.firstname
                    }},
                    apply: function(firstname){
                        return truncCell.call(this, firstname, '');
                    }
                },
                lastname: {
                    label: 'Last Name',
                    // sortable: true,
                    td: { style: {
                        verticalAlign: 'middle',
                        width: colWidths.lastname
                    }},
                    apply: function(lastname){
                        return truncCell.call(this, lastname, '');
                    }
                },
                email: {
                    label: 'Email',
                    // sortable: true,
                    td: { style: {
                        verticalAlign: 'middle',
                        width: colWidths.email
                    }},
                    apply: function(email){
                        return truncCell.call(this, email, '');
                    }
                },
                GROUP_ID: {
                    label: 'Group',
                    // sortable: true,
                    td: {
                        classes: 'center',
                        style: {
                            verticalAlign: 'middle',
                            width: colWidths.group
                        }
                    },
                    apply: function(GROUP_ID, data){
                        var groupEsc = escapeHtml(GROUP_ID);
                        var loginEsc = escapeHtml(data.login);
                        return [
                            '<span class="hidden">' + groupEsc + '</span>',
                            groupsMenu(escapeHtml(loginEsc), groupEsc)
                        ];
                        // return groupSelect.call(this, this.login, GROUP_ID);
                    }
                },
                remove: {
                    label: 'Remove',
                    td: {
                        classes: 'center',
                        style: {
                            verticalAlign: 'middle',
                            width: colWidths.remove
                        }
                    },
                    apply: function(NULL, data){
                        return spawn('div.center', [
                                spawn('a.remove-user.nolink.btn-hover', {
                                    href: '#!',
                                    title: 'Remove: ' + data.login + '|' + data.GROUP_ID,
                                    html: '<b class="x">&times;</b>'
                                })
                            ]
                        )
                    }
                    // html: '<button type="button" class="remove-user btn btn-sm">&times;</button>'//,
                    // apply: function(){
                    //     return removeUserButton.call(this, this.login, this.GROUP_ID);
                    // }
                }

            }
        });

        return projectUsersContainer;

    }
    XNAT.projectAccess.spawnUserTable = spawnUserTable;


    /*
     * User Invite Methods
     */
    XNAT.projectAccess.inviteMultipleUsersFromForm = function(){
        // parse list of 1-n users. Can be separated by spaces, line breaks or commas.
        var invitedUsers = $('#invite_user').val().trim(), invitedUserList;

        if (invitedUsers.indexOf(' ') > 0) { // if separated by spaces
            invitedUserList = invitedUsers.split(' ');
        } else if (RegExp('\\s').test(invitedUsers)) { // if separated by line breaks
            invitedUserList = invitedUsers.replace(/\r\n/g, "\n").split("\n"); // normalize line break and split
        } else { // separate by commas
            invitedUserList = invitedUsers.split(',');
        }

        invitedUserList.forEach(function(user){
            XNAT.projectAccess.inviteUserFromForm(user);
        });
    };

    XNAT.projectAccess.inviteUserFromForm = function(user){
        var group = $('#invite_access_level').find('option:selected').val();
        if (!user || !group) {
            errorHandler({
                statusText: 'Function: inviteUserFromForm',
                responseText: 'Please be sure a user and role have been specified.'
            });
            return false;
        }

        var newUser = undefined;

        // determine if input is username or email.
        var isEmail = XNAT.validation.regex.email.test(user);

        // determine whether user is already in project
        for (var i=0, j=projectUsers.length; i<j; i++) {
            if (projectUsers[i].login === user || projectUsers[i].email === user) {
                XNAT.ui.dialog.alert('This user already exists in this project. To modify, please use the table above.');
                return false;
            }
        }

        if (allUsers.length > 0) {
            // if this user can view the full user's list, we can check whether this invite is for a new user
            // determine whether to invite new user or existing user
            newUser = true;

            for (var i=0, j=allUsers.length; i<j; i++) {
                if (allUsers[i].login === user || allUsers[i].email === user) {

                    // if input was an email, convert it to the appropriate username.
                    if (isEmail) {
                        user = allUsers[i].login;
                    }
                    newUser = false;
                    break;
                }
            }
        }

        if (newUser && !isEmail) {
            XNAT.ui.dialog.alert("This username was not found. " + (XNAT.projectAccess.securityExternalUserParDisabled ? "" : " To invite a new user, please input that user's email address."));
            return false;
        }

        if (newUser === undefined && !isEmail) {
            XNAT.ui.dialog.confirm({
                title: 'Confirm Invite',
                content: 'Are you sure '+ user +' is a valid username in ' + siteName + '?',
                okAction: function(){
                    XNAT.projectAccess.setUserAccess(user,group,{ sendEmail: true });
                    $('#invite_user').val('');
                }
            });
            return true;
        }

        if (newUser === undefined && isEmail) {
            // attempt to add new user
            XNAT.projectAccess.setUserAccess(user,group,{ sendEmail: true, hideNotification: true });
            XNAT.ui.dialog.alert('<b>'+user+'</b> has been invited to join your project, and an email notification has been sent.');
            $('#invite_user').val('');
            XNAT.projectAccess.initPars('project');
            return true;
        }


       if (newUser && isEmail) {
          if(XNAT.projectAccess.securityExternalUserParDisabled){
              XNAT.ui.dialog.alert('Unable to invite ' + user + '. Inviting unregistered users is not allowed.');
              return true;
          }else{
               XNAT.projectAccess.setUserAccess(user,group,{ sendEmail: true, hideNotification: true });
               XNAT.ui.dialog.alert('An email invitation has been sent to <b>'+user+'</b> to register an account with ' + siteName + ' and join your project.');
               $('#invite_user').val('');
               XNAT.projectAccess.initPars('project');
               return true;
           }
       }


        if (newUser === false) {
            XNAT.projectAccess.setUserAccess(user,group,{ sendEmail: true });
            $('#invite_user').val('');
            return true;
        }

    };

    function inviteUsersFromTable(listObj){
        var usersToAdd = [];
        $(listObj).find('option:selected').each(function(){
            if ($(this).val().length > 0) {
                var group = $(this).val();
                var user = $(this).parents('tr').find('.login').find('span').html();
                usersToAdd.push({ login: user, group:group });
            }
        });
        return usersToAdd; // return list for notification
    }

    var getAvailableUsers = XNAT.projectAccess.getAvailableUsers = function(){

        if (!allUsers.length) return false;

        // collect list of usernames in the project
        projectUsernames = projectUsers.map(function(userObj){
            return userObj.login;
        });

        // // collect list of ALL usernames (minus 'guest')
        // allUsernames = allUsers.map(function(userObj){
        //     return userObj.login;
        // }).filter(function(username){
        //     return username !== 'guest'
        // });

        // collect list of users NOT in the project
        availableUsers = [];

        allUsers.forEach(function(userObj){
            var userLogin = userObj.login;
            // don't pass the guest user as an available user to be added to a project.
            if (userLogin === 'guest') return;
            if (projectUsernames.indexOf(userLogin) === -1) {
                availableUsers.push(userObj)
            }
        });

        return availableUsers;
    };


    // display list of users available in XNAT
    function availableUsersTable(){

        allUsers = XNAT.projectAccess.allUsers;
        XNAT.projectAccess.availableUsers = availableUsers = getAvailableUsers();

        var colWidths = {
            narrow: '18%',
            email: '30%',
            group: '16%'
        };

        var shortList = availableUsers.length < 15;

        var usersTableContainer = $.spawn('div#available-user-list.table-group-container');

        XNAT.table.dataTable([], {
            container: usersTableContainer,
            body: false,
            sortable: 'login, firstname, lastname, email',
            // filter: 'login, firstname, lastname, email',
            overflowY: shortList ? 'auto' : 'scroll',
            table: {
                classes: 'table-group-member table-header',
                style: { tableLayout: 'fixed' }
            },
            columns: {
                login: {
                    label: 'Username',
                    th: { style: { width: colWidths.narrow } }
                },
                firstname: {
                    label: 'First Name',
                    th: { style: { width: colWidths.narrow } }
                },
                lastname: {
                    label: 'Last Name',
                    th: { style: { width: colWidths.narrow } }
                },
                email: {
                    label: 'Email',
                    th: { style: { width: colWidths.email } }
                },
                group: {
                    label: 'Group',
                    th: { style: { width: colWidths.group } }
                }
            }
        });

        XNAT.table.dataTable(availableUsers, {
            container: usersTableContainer,
            header: false,
            maxHeight: '480px',
            overflowY: shortList ? 'auto' : 'scroll',
            table: {
                className: 'table-group-member table-data rows-only highlight user-list',
                style: { tableLayout: 'fixed' }
            },
            columns: {
                login: {
                    td: { style: { width: colWidths.narrow } },
                    apply: function(login){
                        return truncCell.call(this, login, '');
                    }
                },
                firstname: {
                    td: { style: { width: colWidths.narrow } },
                    apply: function(firstname){
                        return truncCell.call(this, firstname, '');
                    }
                },
                lastname: {
                    td: { style: { width: colWidths.narrow } },
                    apply: function(lastname){
                        return truncCell.call(this, lastname, '');
                    }
                },
                email: {
                    td: { style: { width: colWidths.email } },
                    apply: function(email){
                        return truncCell.call(this, email, '');
                    }
                },
                group: {
                    td: { style: { width: colWidths.group } },
                    apply: function(){
                        return groupsMenu(this.login, null);
                        // return groupSelect.call(this, this.login );
                    }
                }
            }
        });

        return usersTableContainer;

    }
    XNAT.projectAccess.availableUsersTable = availableUsersTable;

    // search dialog
    XNAT.projectAccess.searchAvailableUsers = function(input, term, table){
        term = (term ? term+'' : '').toLowerCase();
        $$(table).find('tr').each(function(){
            var $tr = $(this);
            var rowText = '';
            $tr.find('td').not('.group').each(function(){
                rowText += ' ' + this.textContent.toLowerCase();
            });
            $tr.hidden(rowText.search(term) === -1);
        });
    };
    XNAT.projectAccess.clearSearch = function(input, table){
        $$(table).find('tr').hidden(false);
        $$(input).val('').focus();
    };

    // open dialog
    XNAT.projectAccess.inviteUserFromList = function(){

        var availableUsersTable$ = availableUsersTable();

        var userList$ = availableUsersTable$.find('table.user-list');

        // search <input>
        var searchInput$ = $.spawn('input', {
            type: 'text',
            placeholder: 'Find User'
        }).on('keyup', function(e){

            XNAT.projectAccess.searchAvailableUsers(this, this.value, userList$)
        });

        // 'Clear' link
        var clearSearch = spawn('a', {
            href: '#!',
            html: '&nbsp; Clear &nbsp;',
            on: {
                click: function(e) {
                    e.preventDefault();
                    XNAT.projectAccess.clearSearch(searchInput$, userList$);
                }
            }
        });

        XNAT.ui.dialog.open({
            title: 'Add Users From List',
            content: availableUsersTable$,
            width: 800,
            // height: 400,
            // nuke: true,
            buttons: [
                {
                    label: 'Add Users',
                    close: false,
                    isDefault: true,
                    action: function(obj){
                        var listObj = obj.$modal.find('table'),
                            usersToAdd = inviteUsersFromTable(listObj);
                        if (usersToAdd.length > 0) {
                            XNAT.ui.dialog.confirm({
                                title: 'Success!',
                                content: "You have added "+usersToAdd.length+" users to this project. Do you want to send confirmation emails to each user?",
                                okLabel: "Add and Send Email",
                                okAction: function(){
                                    usersToAdd.forEach(function(user){
                                        XNAT.projectAccess.setUserAccess(user.login,user.group,{ sendEmail: true, notificationMessage: user.login + ' added to project.' });
                                    });
                                    XNAT.ui.dialog.closeAll(true);
                                },
                                cancelLabel: "Add Only",
                                cancelAction: function(){
                                    usersToAdd.forEach(function(user){
                                        XNAT.projectAccess.setUserAccess(user.login,user.group,{ sendEmail: false,  notificationMessage: user.login + ' added to project.' });
                                    });
                                    XNAT.ui.dialog.closeAll(true);
                                }
                            });

                        } else {
                            XNAT.ui.dialog.alert('You have not selected a project group for any users.');
                            return false;
                        }
                    }
                },
                {
                    label: 'Cancel',
                    close: true
                }
            ],
            footerContent: [searchInput$[0], clearSearch]
        });
    };

    // render or update the users table
    function renderUsersTable(showDisabled){
        showDisabled = showDisabled || $('#showDeactivatedUsersCheck').is(':checked');
        var container = userTableContainer;
        var URL = projectUsersUrl(showDisabled);
        return XNAT.xhr.getJSON({
            url: URL,
            success: function(data){
                projectAccess.projectUsers = projectUsers = data.ResultSet.Result;
                $$(container).empty().append(spawnUserTable(showDisabled));
            },
            fail: function(e){
                errorHandler(e);
            }
        });
    }
    XNAT.projectAccess.renderUsersTable = renderUsersTable;

    /*
     * Manage Groups
     */
    function groupCell(group, groupId){
        return (group === 'Owners' || group === 'Members' || group === 'Collaborators') ?
            group :
            group + '<span class="pull-right"><a href="#!" onclick="XNAT.projectAccess.modifyGroup(\''+groupId+'\', \''+projectId+'\')">Edit</a></span>';
    }

    XNAT.projectAccess.showGroups = function(canCreateGroups){
        if (!XNAT.projectAccess.groups.length) {
            errorHandler({
                statusText: 'Function: showGroups',
                responseText: 'Could not load groups.'
            });
            return false;
        }
        var groups = XNAT.projectAccess.groups;
        XNAT.ui.dialog.open({
            title: 'Manage Groups',
            width: 400,
            // nuke: false,
            content: '<div class="panel"></div>',
            beforeShow: function(obj){
                var container = obj.$modal.find('.panel');
                container.append(
                    XNAT.table.dataTable(groups,{
                        className: 'xnat-table compact',
                        header: true,
                        sortable: false,
                        items: {
                            displayname: {
                                label: 'Group',
                                td: { className: 'left' },
                                apply: function(displayname){
                                    return groupCell.call(this, displayname, this.id);
                                }
                            },
                            users: {
                                label: 'Users',
                                td: { className: 'right' }
                            }
                        }
                    }).element
                );
                container.spawn('p');
                if (canCreateGroups){
                    container.spawn('a', {
                        href: rootUrl('/app/template/XDATScreen_edit_xdat_userGroup.vm/tag/' + projectId + '/src/project')
                    }, '<button class="btn" type="button">Create Custom Group</button>');
                }
                else {
                    container.spawn('button.btn.disabled',{title: "Not allowed for this user on this project"},"Create Custom Group");
                }
            },
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true
                }
            ]
        });
    };

    XNAT.projectAccess.modifyGroup = function(group,projectId){
        window.location.href = rootUrl('/app/template/XDATScreen_edit_xdat_userGroup.vm/tag/' + projectId + '/src/project/search_element/xdat:userGroup/search_field/xdat:userGroup.ID/search_value/'+group);
    };

    /*
     * Project Accessibility
     */
    XNAT.projectAccess.setAccessibility = function(){
        var accessibility = $('input[name=accessibility]:checked').val();
        if (!accessibility) {
            errorHandler({
                statusText: 'Function: setAccessibility',
                responseText: 'No accessibility setting found. Please select one.'
            });
            return false;
        }
        XNAT.xhr.putJSON({
            url: csrfUrl('/REST/projects/'+projectId+'/accessibility/'+accessibility),
            success: function(){
                XNAT.ui.banner.top(2000,'<b>Success.</b> Project accessibility set to '+accessibility+'.', 'success');
                $('#accessibility_save').prop('disabled', true);
                currAccessibility = accessibility;
            },
            fail: function(e){
                errorHandler(e)
            }
        })
    };

    $(document).on('change','input[name=accessibility]',function(){
        // only enable accessibility change if a new value has been selected
        var _disabled = $(this).val() === currAccessibility;
        $('#accessibility_save').prop('disabled', _disabled);
    });

    /*
     * Init
     */

    XNAT.projectAccess.initSiteUsers = function() {
        XNAT.xhr.getJSON({
            url: xnatUsersUrl(),
            fail: function (e) {
                errorHandler(e);
            },
            success: function(data) {
                allUsers = XNAT.projectAccess.allUsers = data.ResultSet.Result;
            }
        });
    };

    XNAT.projectAccess.init = function() {
        // get project access groups, then initialize user table


        XNAT.xhr.getJSON({
            url: projectGroupsUrl(),
            fail: function (e) {
                errorHandler(e);
            },
            success: function (data) {
                var groups = XNAT.projectAccess.groups = data.ResultSet.Result;
                renderUsersTable();

                // set a standard height for the user table to stop page flickering on table refresh.
                window.setTimeout(function () {
                    userTableContainer.css('min-height', userTableContainer.height());
                }, 1);

                // enable invite UI
                groups.forEach(function (group) {
                    $('#invite_access_level').append('<option value="' + group.id + '">' + group.displayname + '</option>')
                });
                $('#popup_all_users_button').prop('disabled', false);
            }
        });

    };

    XNAT.projectAccess.init();

    return XNAT.projectAccess = projectAccess;

}));