
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
        currAccessibility = $('#current_accessibility').val(),
        allUsers,
        availableUsers,
        projectUsers,
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
                    message = (opts.notificationMessage) ? opts.notificationMessage : 'Access level updated for <b>' + user + '</b>.';
                    XNAT.ui.banner.top(2000, message, 'success');
                }
            },
            fail: function(e){
                errorHandler(e);
            }
        })
    };
    
    XNAT.projectAccess.removeUser = function(user,group){
        if (!user || !group) return false;
        XNAT.xhr.delete({
            url: removeUserUrl(user,group),
            success: function(){
                XNAT.ui.banner.top(2000, '<b>' + user + '</b> removed from project.', 'success');
                XNAT.projectAccess.renderUsersTable();
            },
            fail: function(e){
                errorHandler(e);
            }
        })
    };


    /*
     * populate user tables
     */

    // table cell formatting
    function truncCell(val,truncClass) {
        return '<span class="truncate '+ truncClass +'" title="'+ val +'">'+ val +'</span>';
    }

    function groupSelect(login,groupSelection){
        // if a user already belongs to this project, a group selection will be specified. This changes the behavior of the select.
        groupSelection = groupSelection || '';
        var selector = (groupSelection)
            ? '<select onchange="XNAT.projectAccess.setUserAccess(\''+login+'\', this.value)">'
            : '<select><option value selected></option>';

        XNAT.projectAccess.groups.forEach(function(group){
            var groupSelected = (groupSelection === group.id) ? ' selected' : '';
            selector += '<option value="'+group.id+'" '+ groupSelected +'>'+ group.displayname + '</option>';
        });
        selector += '</select>';
        return selector;
    }

    function removeUserButton(login,group){
        var button = '<div class="centered"><button class="btn sm" onclick="XNAT.projectAccess.removeUser(\''+login+'\',\''+group+'\')">x</button></div> ';
        return button;
    }


    // display list of users with access to project
    var spawnUserTable = XNAT.projectAccess.spawnUserTable = function(showDisabled){
        var URL = (showDisabled) ? projectUsersUrl(true) : projectUsersUrl();

        var colWidths = {
            narrow: '125px',
            email: '175px',
            group: '180px'
        };

        return {
            kind: 'table.dataTable',
            name: 'project-users',
            id: 'project-users',
            className: 'compact',
            header: true,
            sortable: false,
            load: URL,
            items: {
                login: {
                    label: 'Username',
                    td: { style: { width: colWidths.narrow } },
                    apply: function(login){
                        return truncCell.call(this, login, 'truncateCellNarrow');
                    }
                },
                firstname: {
                    label: 'First Name',
                    td: { style: { width: colWidths.narrow } },
                    apply: function(firstname){
                        return truncCell.call(this, firstname, 'truncateCellNarrow');
                    }
                },
                lastname: {
                    label: 'Last Name',
                    td: { style: { width: colWidths.narrow } },
                    apply: function(lastname){
                        return truncCell.call(this, lastname, 'truncateCellNarrow');
                    }
                },
                email: {
                    label: 'Email',
                    td: { style: { width: colWidths.email } },
                    apply: function(email){
                        return truncCell.call(this, email, 'truncateCell');
                    }
                },
                GROUP_ID: {
                    label: 'Group',
                    td: { style: { width: colWidths.group } },
                    apply: function(GROUP_ID){
                        return groupSelect.call(this, this.login, GROUP_ID );
                    }
                },
                remove: {
                    label: 'Remove',
                    td: {},
                    apply: function(){
                        return removeUserButton.call(this, this.login, this.GROUP_ID);
                    }
                }

            }
        };
    };


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
                xmodal.alert({
                    message: 'This user already exists in this project. To modify, please use the table above.'
                });
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
            xmodal.alert({
                message: 'This username was not found. To invite a new user, please input that user\'s email address.'
            });
            return false;
        }

        if (newUser === undefined && !isEmail) {
            xmodal.confirm({
                title: 'Confirm Invite',
                message: 'Are you sure '+ user +' is a valid username in XNAT?',
                okAction: function(){
                    XNAT.projectAccess.setUserAccess(user,group,{ sendEmail: true });
                    XNAT.projectAccess.renderUsersTable();
                    $('#invite_user').val('');
                }
            });
            return true;
        }

        if (newUser === undefined && isEmail) {
            // attempt to add new user
            XNAT.projectAccess.setUserAccess(user,group,{ sendEmail: true, hideNotification: true });
            xmodal.alert({
                message: '<b>'+user+'</b> has been invited to join your project, and an email notification has been sent.'
            });
            $('#invite_user').val('');
            XNAT.projectAccess.renderUsersTable();
            XNAT.projectAccess.initPars('project');
            return true;
        }

        if (newUser && isEmail) {
            XNAT.projectAccess.setUserAccess(user,group,{ sendEmail: true, hideNotification: true });
            xmodal.alert({
                message: 'An email invitation has been sent to <b>'+user+'</b> to register an account with XNAT and join your project.'
            });
            $('#invite_user').val('');
            XNAT.projectAccess.initPars('project');
            return true;
        }

        if (newUser === false) {
            XNAT.projectAccess.setUserAccess(user,group,{ sendEmail: true });
            XNAT.projectAccess.renderUsersTable();
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

        var availableUsers = [];

        allUsers.forEach(function(userObj){
            var available = (userObj.login !== 'guest'); // don't pass the guest user as an available user to be added to a project.  
            for (var i=0, j=projectUsers.length; i<j; i++) {
                if (userObj.login === projectUsers[i].login) {
                    // don't add users if they already exist in the project, or the "guest" user
                    available=false;
                    break;
                }
            }
            if (available) availableUsers.push(userObj);
        });

        return availableUsers;
    };

    // display list of users available in XNAT
    var showAvailableUsers = XNAT.projectAccess.showAvailableUsers = function(container){

        allUsers = XNAT.projectAccess.allUsers;
        XNAT.projectAccess.availableUsers = availableUsers = getAvailableUsers();
        $(container).empty();

        var colWidths = {
            narrow: '125px',
            email: '175px',
            group: '180px'
        };

        var availableUserTable = XNAT.table.dataTable(availableUsers, {
            className: 'xnat-table compact',
            header: true,
            sortable: false,
            items: {
                login: {
                    label: 'Username',
                    td: { style: { width: colWidths.narrow } },
                    apply: function(login){
                        return truncCell.call(this, login, 'truncateCellNarrow');
                    }
                },
                firstname: {
                    label: 'First Name',
                    td: { style: { width: colWidths.narrow } },
                    apply: function(firstname){
                        return truncCell.call(this, firstname, 'truncateCellNarrow');
                    }
                },
                lastname: {
                    label: 'Last Name',
                    td: { style: { width: colWidths.narrow } },
                    apply: function(lastname){
                        return truncCell.call(this, lastname, 'truncateCellNarrow');
                    }
                },
                email: {
                    label: 'Email',
                    td: { style: { width: colWidths.email } },
                    apply: function(email){
                        return truncCell.call(this, email, 'truncateCell');
                    }
                },
                group: {
                    label: 'Group',
                    td: { style: { width: colWidths.group } },
                    apply: function(){
                        return groupSelect.call(this, this.login );
                    }
                }
            }
        });

        $(container).append(availableUserTable.spawned);
    };

    // open dialog
    XNAT.projectAccess.inviteUserFromList = function(container){
        container = container || '#availableUserList';

        var modalSearch = '<input type="text" class="modalSearch" placeholder="Find User" onkeyup="XNAT.projectAccess.searchAvailableUsers(this,this.value)">&nbsp;';
        modalSearch += '<a href="#!" onclick="XNAT.projectAccess.clearSearch(this)">Clear</a>';

        showAvailableUsers(container);

        xmodal.open({
            title: 'Add Users From List',
            template: container,
            width: 730,
            height: 400,
            okClose: false,
            okLabel: 'Invite Users',
            okAction: function(obj){
                var listObj = obj.$modal.find('table'),
                    usersToAdd = inviteUsersFromTable(listObj);

                if (usersToAdd.length > 0) {
                    xmodal.confirm({
                        content: "<strong>Success!</strong> You have added "+usersToAdd.length+" users to this project. Do you want to send confirmation emails to each user?",
                        okLabel: "Invite and Send Emails",
                        okAction: function(){
                            usersToAdd.forEach(function(user){
                                XNAT.projectAccess.setUserAccess(user.login,user.group,{ sendEmail: true, notificationMessage: user.login + ' added to project.' });
                            });
                            XNAT.projectAccess.renderUsersTable();
                            xmodal.closeAll();
                        },
                        cancelLabel: "Invite Only",
                        cancelAction: function(){
                            usersToAdd.forEach(function(user){
                                XNAT.projectAccess.setUserAccess(user.login,user.group,{ sendEmail: false,  notificationMessage: user.login + ' added to project.' });
                            });
                            XNAT.projectAccess.renderUsersTable();
                            xmodal.closeAll();
                        }
                    });

                } else {
                    xmodal.alert('You have not selected a project group for any users.');
                }
            },
            footer: {
                content: modalSearch
            }
        });
    };

    // search dialog
    XNAT.projectAccess.searchAvailableUsers = function(input,term){
        term = term.toLowerCase();
        var table = $(input).parents('div.xmodal').find('tbody');
        table.find('tr').each(function(){
            $(this).addClass('hidden');
            var rowArray = '';
            $(this).find('td').not('.group').each(function(){
                rowArray += ' '+$(this).find('span').html().toLowerCase();
            });
            if (rowArray.search(term) >= 0) $(this).removeClass('hidden');
        });
    };
    XNAT.projectAccess.clearSearch = function(item){
        var table = $(item).parents('div.xmodal').find('tbody');
        table.find('tr').removeClass('hidden');
        var searchField = $(item).prev('input');
        $(searchField).val('');
    };

    // render or update the users table
    var renderUsersTable = XNAT.projectAccess.renderUsersTable = function(showDisabled){
        showDisabled = showDisabled || $(showDeactivatedUsersCheck).is(':checked');
        var container = userTableContainer;
        XNAT.xhr.getJSON({
            url: projectUsersUrl(),
            success: function(data){
                XNAT.projectAccess.projectUsers = projectUsers = data.ResultSet.Result;
                var _usersTable = XNAT.spawner.spawn({
                    usersTable: spawnUserTable(showDisabled)
                });
                _usersTable.done(function(){
                    this.render($(container).empty(), 20);
                });
            },
            fail: function(e){
                errorHandler(e);
            }
        });
    };

    /*
     * Manage Groups
     */
    function groupCell(group,groupId){
        return (group === 'Owners' || group === 'Members' || group === 'Collaborators') ?
            group :
            group + '<span class="pull-right"><a href="#!" onclick="XNAT.projectAccess.modifyGroup(\''+groupId+'\', \''+projectId+'\')">Edit</a></span>';
    }

    XNAT.projectAccess.showGroups = function(){
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
            width: 300,
            nuke: false,
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
                container.append(spawn('p',''));
                container.append(
                    spawn('p', [
                        spawn('a', {
                        href: '/app/template/XDATScreen_edit_xdat_userGroup.vm/tag/' + projectId + '/src/project',
                        }, [
                            spawn('button',{
                                className: 'btn',
                                html: 'Create Custom Group'
                            })
                        ])
                    ])
                );
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
            },
            fail: function(e){
                errorHandler(e)
            }
        })
    };
    
    $(document).on('change','input[name=accessibility]',function(){
        // only enable accessibility change if a new value has been selected
        if ($(this).val() !== currAccessibility) {
            $('#accessibility_save').prop('disabled',false)
        } else {
            $('#accessibility_save').prop('disabled','disabled')
        }
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
                setTimeout(function () {
                    userTableContainer.css('min-height', userTableContainer.height());
                }, 1000);

                // enable invite UI
                groups.forEach(function (group) {
                    $('#invite_access_level').append('<option value="' + group.id + '">' + group.displayname + '</option>')
                });
                $('#popup_all_users_button').prop('disabled', false);
            }
        });

    };

    XNAT.projectAccess.init();

}));