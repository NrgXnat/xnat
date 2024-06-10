/*!
 * Functions for "Advanced" user settings
 */

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

    var undef, usersAdvanced, projectAccess;

    XNAT.admin =
        getObject(XNAT.admin || {});

    XNAT.admin.usersAdvanced = usersAdvanced =
        getObject(XNAT.admin.usersAdvanced || {});

    XNAT.data =
        getObject(XNAT.data || {});

    XNAT.data.projectAccess = projectAccess =
        getObject(XNAT.data.projectAccess || {});


    // which user are we working with?
    usersAdvanced.user =
        usersAdvanced.user ||
        projectAccess.user ||
        window.editUser ||
        window.username;


    var editUser = usersAdvanced.user;

    usersAdvanced.listedProjects = {};

    // keep track of projects the user doesn't belong to yet
    usersAdvanced.availableProjects = {};

    // save a map of ALL group names with id as the key
    usersAdvanced.groupNamesMap = {};

    // save a map of arrays of project groups with project ID as the key
    usersAdvanced.projectGroupsMap = {};

    usersAdvanced.groupsList = [];

    // get all groups for specified project
    function getProjectGroups(projId, success, failure){
        return XNAT.xhr.getJSON({
            url: XNAT.url.restUrl('/data/projects/' + projId + '/groups', ['format=json']),
            success: function(json){
                var jqXHR = this;
                // pull the data from the ResultSet.Result
                // for 'success' callback (argument)
                var projGroups = json.ResultSet.Result;
                if (isFunction(success)) {
                    success.call(jqXHR, projGroups);
                }
            },
            error: failure
        })
    }

    // gets list of ALL projects on the system
    function getAllProjects(success, failure){
        return XNAT.xhr.getJSON({
            url: XNAT.url.restUrl('/data/projects', ['allDataOverride=true','format=json']),
            success: success,
            error: failure
        })
    }

    // gets project data that specified user has
    // access to, including their role/group
    function getUserProjects(user, success, failure){
        var USER = user || editUser;
        return XNAT.xhr.getJSON({
            url: XNAT.url.restUrl('/xapi/access/' + USER + '/projects'),
            success: success,
            error: failure
        })
    }

    function removeSiteRoles(projGroupsList){
        return projGroupsList.filter(function(proj){
            return !/^(ALL_DATA_ACCESS|ALL_DATA_ADMIN)$/.test(proj.group_id)
        })
    }

    // gets list of all projects then removes user projects
    function getAvailableProjects(callback){
        getAllProjects().done(function(allProjects){
            getUserProjects().done(function(userProjects){
                var jqXHR = this;
                var filteredProjects = removeSiteRoles(userProjects);
                var availableProjects = [];
                var availableProjectIds = [];
                var userProjectIds = filteredProjects.map(function(userProj){
                    return userProj.ID
                });
                forEach(allProjects.ResultSet.Result, function(proj){
                    if (userProjectIds.indexOf(proj.ID) === -1) {
                        availableProjects.push(proj);
                        availableProjectIds.push(proj.ID);
                    }
                });
                // update namespaced vars
                usersAdvanced.availableProjects = availableProjects;
                usersAdvanced.listedProjects = filteredProjects;
                usersAdvanced.listedProjectsMap = {};
                forEach(filteredProjects, function(proj){
                    usersAdvanced.listedProjectsMap[proj.ID] = proj;
                });
                if (isFunction(callback)) {
                    callback.call(jqXHR, availableProjects, availableProjectIds)
                }
            })
        })
    }


    usersAdvanced.tableContainer = $$(usersAdvanced.tableContainer || '#user-group-membership-table');


    function linkToProject(project, text){
        return spawn('a.nowrap.truncate', {
            href: XNAT.url.rootUrl(project.URI),
            target: '_blank',
            title: project.name
        }, escapeHtml(text))
    }

    function setGroupInfo(data){
        var group = cloneObject(data);
        var groupId = group.ID = group.groupId = group.id;
        var projectId = group.project_id = group.projectId = group.tag;
        group.displayName = group.displayname;
        usersAdvanced.groupsList.push(group);
        usersAdvanced.groupNamesMap[groupId] = group;
        usersAdvanced.projectGroupsMap[projectId] = [];
        forOwn(usersAdvanced.groupNamesMap, function(id, obj){
            usersAdvanced.projectGroupsMap[projectId].push(obj);
        });
        return group;
    }

    // if the group isn't in groupNamesMap, get the data, then do something
    function getGroupInfo(projId, groupId, callback){
        if (!projId) return;
        getProjectGroups(projId,
            function(projGroups){
                forEach(projGroups, function(projGroup){
                    setGroupInfo(projGroup);
                });
                // use [groupId] group for callback
                if (isFunction(callback)) {
                    callback.call(usersAdvanced.groupNamesMap, usersAdvanced.groupNamesMap[groupId], projGroups);
                }
            },
            function(){
                console.warn(arguments);
            }
        )
    }


    function setupGroupsMenu(menu, projId, groupId){
        getGroupInfo(projId, groupId, function(group, projGroups){
            menu.innerHTML = '<option value="">&nbsp;</option>'; // reset the menu
            forEach(projGroups, function(group, i){
                var option = spawn('option', {
                    value: group.id,
                    html: group.displayname
                });
                if (groupId === group.id) {
                    option.selected = true;
                }
                menu.appendChild(option);
            });
            console.log('project groups ready');
            // menuInit(menu, {}, 200);
            menuUpdate(menu);
            // $(menu).trigger('chosen:updated');
            // menu$.trigger('chosen:open');
        });
    }


    function setUserRole(user, groupId, projectId){
        getAvailableProjects(function(availableProj){

            var sortedProj = sortObjects(availableProj, 'secondary_ID');

            var projectIdMap = {};

            forEach(sortedProj, function(proj, i){
                projectIdMap[proj.ID] = proj;
            });

            var editGroupDialog = XNAT.dialog.open({
                width: 500,
                minHeight: 250,
                padding: 30,
                title: 'Set User Group',
                content: (function(){

                    var rolePanelConfig = {
                        kind: 'panel',
                        header: false,
                        footer: false,
                        border: false,
                        padding: 0,
                        contents: {
                            // child elements defined below
                        }
                    };

                    // is the 'Project' element going to be text or a <select> menu?
                    if (projectId) {
                        rolePanelConfig.contents.projectDisplay = {
                            kind: 'panel.element',
                            id: false,
                            label: 'Project',
                            contents: {
                                projectName: {
                                    tag: 'b',
                                    content: escapeHtml(usersAdvanced.listedProjectsMap[projectId].secondary_ID)
                                },
                                projectId: {
                                    kind: 'input.hidden',
                                    name: 'project',
                                    value: projectId
                                }
                            }
                        }
                    }
                    else {
                        rolePanelConfig.contents.projectSelect = {
                            kind: 'panel.select.menu',
                            id: false,
                            name: 'project',
                            label: 'Select Project',
                            options: [{
                                label: '&nbsp;',
                                value: '',
                                selected: true
                            }].concat(sortedProj.map(function(prj){
                                return {
                                    label: escapeHtml(prj.secondary_ID),
                                    value: prj.ID
                                }
                            }))
                        };
                    }

                    // add the 'Groups' (roles) menu
                    rolePanelConfig.contents.groupSelect = {
                        kind: 'panel.select.menu',
                        id: false,
                        name: 'group',
                        label: 'Select Group',
                        options: [{
                            label: '&nbsp;',
                            value: '',
                            selected: true
                        }]
                        // options added later
                    };


                    var rolePanel = XNAT.spawner.spawn({
                        rolePanel: rolePanelConfig
                    });

                    // var projMenu$ = $(addRolePanel.spawned).find('select[name="project"]');
                    // var groupMenu$ = $(addRolePanel.spawned).find('select[name="group"]');

                    // if there's a project id, populate the 'groups' menu immediately
                    // if (projectId) {
                    //     projMenu$.changeVal(projectId)
                    // }

                    return rolePanel.spawned;

                })(),
                beforeShow: function(dlgObj){
                    // all of this for the fancy menus...
                    this.dialog$.css({ overflow: 'visible' });
                    this.dialogBody$.css({ overflow: 'visible' });
                    this.content$.css({ overflow: 'visible' });
                    var dlg$ = dlgObj.dialog$;
                    var groupMenu$ = dlg$.find('select[name="group"]');
                    menuInit(groupMenu$, {}, 200);
                    // set the initial value for a selected group
                    if (projectId && groupId) {
                        setupGroupsMenu(groupMenu$[0], projectId, groupId);
                    }
                    var projMenu$ = dlg$.find('select[name="project"]');
                    menuInit(projMenu$, {}, 250);
                    // update role menu after selecting a project
                    projMenu$.on('change', function(e){
                        var projId = this.value;
                        setupGroupsMenu(groupMenu$[0], projId);
                    });
                },
                buttons: [
                    {
                        label: 'Save',
                        isDefault: true,
                        close: false,
                        action: function(dlgObj){
                            var dlg$ = dlgObj.dialog$;
                            var proj$ = dlg$.find('[name="project"]');
                            var group$ = dlg$.find('[name="group"]');
                            if (!proj$.val()) {
                                XNAT.dialog.message(false, 'Please select a project.');
                                return;
                            }
                            if (!group$.val()) {
                                XNAT.dialog.message(false, 'Please select a group');
                                return;
                            }
                            XNAT.xhr.put({
                                url: XNAT.url.rootUrl('/xapi/users/' + editUser + '/groups/' + group$.val())
                            }).done(function doneFn(){
                                // update the list after saving
                                window.setTimeout(function(){
                                    renderUserGroupsTable();
                                    editGroupDialog.close();
                                }, 100)
                            }).fail(function failFn(jqXHR, textStatus, errorThrown){
                                XNAT.dialog.message('Error', 'An error occurred adding user role.<br><br>' + errorThrown)
                            })
                        }
                    },
                    {
                        label: 'Cancel',
                        close: true
                    }
                ]
            });
        })
    }

    function confirmChange(user, projId, groupId){
        getGroupInfo(projId, groupId, function(group){
            user = user || editUser;
            var newRole = escapeHtml(group.displayname);
            var projectId = escapeHtml(group.tag);
            var proj = usersAdvanced.listedProjects[projectId];
            var projLabel = escapeHtml(proj.secondary_ID);

            var confirmation = '' +
                'Move user ' + '<b>"' + user + '"</b>' +
                ' to group ' + '<b>"' + newRole + '"</b>' +
                ' in project ' + '<b>"' + projLabel + '"</b>' + '?';

            var confirmChangeDialog = XNAT.dialog.confirm({
                width: 500,
                padding: 30,
                title: 'Change Role?',
                content: confirmation,
                okLabel: 'Change',
                okClose: false,
                okAction: function(obj){
                    // do we need to delete old role first?
                    // XNAT.xhr['delete']({
                    //     url: '/xapi/users/' + user + '/groups/' + proj.group_id
                    // }).done(function(){})
                    XNAT.xhr.put({
                        url: XNAT.url.rootUrl('/xapi/users/' + user + '/groups/' + groupId)
                    }).done(function(){
                        XNAT.ui.banner.top(2000, 'Role changed.', 'success');
                        // renderUserGroupsTable();
                    }).fail(function(){
                        XNAT.ui.banner.top(5000, 'An error occured when attempting to set the user role.', 'error');
                    }).always(function(){
                        confirmChangeDialog.close();
                    })
                }
            })
        })
    }


    function confirmRemove(user, groupId, projLabel){
        user = user || editUser;
        var confirmRemoveDialog = XNAT.dialog.confirm({
            width: 500,
            padding: 30,
            title: 'Remove Project Access?',
            content: 'Remove all access to project <b class="nowrap">"' + escapeHtml(projLabel) + '"</b> for user <b class="nowrap">"' + escapeHtml(user) + '"</b>?',
            okLabel: 'Remove',
            okClose: false,
            okAction: function(obj){
                XNAT.xhr['delete']({
                    url: XNAT.url.rootUrl('/xapi/users/' + user + '/groups/' + groupId)
                }).done(function(){
                    XNAT.ui.banner.top(2000, 'Project access removed.', 'success');
                    renderUserGroupsTable();
                }).fail(function(){
                    XNAT.ui.banner.top(5000, 'An error occured when attempting to remove project access.', 'error');
                }).always(function(){
                    confirmRemoveDialog.close();
                })
            }
        })
    }


    // renders the table for listing and assigning group membership
    function renderUserGroupsTable(container, username){

        var tableContainer = usersAdvanced.tableContainer = $$(container || usersAdvanced.tableContainer);

        username = username || editUser;

        getUserProjects().done(function(userProjects){

            var filteredProjects = removeSiteRoles(userProjects || []);

            if (!userProjects || !filteredProjects.length) {
                tableContainer.empty().spawn(
                    // element
                    'div.message',
                    // content
                    "This user does not currently belong to any project groups. " +
                    "Click the 'Add Role' button below to configure project access."
                );
                return this;
            }

            // save an object map for each project keyed off the ID
            forEach(filteredProjects, function(project){
                usersAdvanced.listedProjects[project.ID] = project;
            });

            var btnCSS = spawn('style|type=text/css', [
                '\n' +
                'td.MODIFY a { width: 30px; height: 28px; margin: 0 2px; position: relative; font-size: 15px; } \n' +
                'td.MODIFY a i { position: relative; } \n' +
                'td.MODIFY a.edit-user-role i { top: 2px; } \n' +
                'td.MODIFY a.remove-user-role i { top: 1px; color: #c00; } \n' +
                ' '
            ]);

            var rolesTable = XNAT.table.dataTable(filteredProjects, {
                table: {
                    classes: 'compact rows-only highlight',
                    id: 'user-project-roles'
                },
                // load: '/xapi/access/' + (username || editUser) + '/projects',
                // load: '',
                // messages: {
                //     noData: '<div class="message">' +
                //     "This user does not currently belong to any project groups. " +
                //     "Click the 'Add Role' button below to configure project access." +
                //     '</div>'
                // },
                items: {
                    _ID: '~data-project-id',
                    _group_id: '~data-group-id',
                    _secondary_ID: '~data-project-label',
                    secondary_ID: {
                        label: 'Project Label',
                        sort: true,
                        filter: filteredProjects.length > 6,
                        th: { style: { width: '35%' } },
                        td: { style: { width: '35%' } },
                        apply: function(){
                            return linkToProject(this, this.secondary_ID)
                        }
                    },
                    ID: {
                        label: 'Project ID',
                        sort: true,
                        filter: filteredProjects.length > 6,
                        th: { style: { width: '30%' } },
                        td: { style: { width: '30%' } },
                        apply: function(){
                            return linkToProject(this, this.ID)
                        }
                    },
                    role: {
                        label: 'Group',
                        sort: true,
                        th: { style: { width: '25%' } },
                        td: { style: { width: '25%' } },
                        apply: function(){
                            var project = this;
                            var editLink = spawn('a.edit-user-role', {
                                href: '#!edit=' + project.group_id,
                                title: 'Edit: ' + username + ' | ' + project.group_id + ' | ' + project.ID
                            }, '' + project.role + '');

                            return spawn('div.nowrap.truncate', [editLink])
                        }
                    },
                    MODIFY: {
                        label: '&nbsp;',
                        th: { style: { width: '10%' } },
                        td: { style: { width: '10%' } },
                        apply: function(){
                            var project = this;
                            var editBtn = spawn('a.edit-user-role.nolink.btn-hover', {
                                href: '#!edit=' + project.group_id,
                                title: 'Edit: ' + username + ' | ' + project.group_id + ' | ' + project.ID
                            }, [['i.fa.fa-edit']]);
                            var removeBtn = spawn('a.remove-user-role.nolink.btn-hover', {
                                href: '#!remove=' + project.group_id,
                                title: 'Remove: ' + username + ' | ' + project.group_id + ' | ' + project.secondary_ID
                            }, [['i.fa.fa-times']]);
                            return spawn('div.center.nowrap', [editBtn, removeBtn]);
                        }
                    }
                }
            });

            // need to set overflow to 'visible' so Chosen menus
            // don't get cut off or cause weird scrolling behavior
            rolesTable.wrapper.style.overflowX = 'visible';
            rolesTable.wrapper.style.overflowY = 'visible';

            // rolesTable.render(tableContainer.empty());
            // choosing to manually 'append' the spawned elements
            tableContainer.empty().append([btnCSS, rolesTable.spawned]);

        });

        // event listeners for editing and deleting - remove and re-add
        // every time the table renders to prevent multiple listeners
        tableContainer.off('click.edit-role').on('click.edit-role', '.edit-user-role', function(e){
            e.preventDefault();
            var rowData = $(this).closest('tr').data();
            setUserRole(editUser, rowData.groupId, rowData.projectId);
            // confirmChange(editUser, rowData.groupId, this.value);
        });

        tableContainer.off('click.remove-role').on('click.remove-role', '.remove-user-role', function(e){
            e.preventDefault();
            console.log('deleting role');
            var rowData = $(this).closest('tr').data();
            confirmRemove(editUser, rowData.groupId, rowData.projectLabel);
        });

    }
    usersAdvanced.renderUserGroupsTable = renderUserGroupsTable;

    // render the table when the script loads (or call it from the page?)
    renderUserGroupsTable();


    $(document).off('click.set-role').on('click.set-role', '#user-add-group', function userAddGroup(){
        setUserRole()
    });


    function updateUserRoles(form$){

        var userform$ = $$(form$ || '#userform');
        var accessInputs$ = userform$.find('input.access');

        function roleUrl(part){
            return XNAT.url.rootUrl('/xapi/users/' + editUser + part);
        }

        // collect request info
        var requests = [];

        // ALWAYS delete ALL_DATA_ACCESS and ALL_DATA_ADMIN
        // so they can be set to the ONE proper value
        requests.push({
            method: 'DELETE',
            url: roleUrl('/groups/ALL_DATA_ACCESS')
        });
        requests.push({
            method: 'DELETE',
            url: roleUrl('/groups/ALL_DATA_ADMIN')
        });

        accessInputs$.each(function(){

            var NAME = this.name;
            var VALUE = this.value;
            var CHECKED = this.checked;

            if (/custom_role/i.test(NAME)) {
                requests.unshift({
                    method: CHECKED ? 'PUT' : 'DELETE',
                    url: roleUrl('/roles/' + VALUE)
                });
            }
            else if (/xdat:user\.groups/i.test(NAME)) {
                if (CHECKED && VALUE !== 'NULL') {
                    requests.push({
                        method: 'PUT',
                        url: roleUrl('/groups/' + VALUE)
                    });
                }
            }

        });

        function doUpdate(i){

            // after the last request returns...
            if (i === (requests.length)) {
                window.setTimeout(function(){
                    window.location.reload(true);
                    // DON'T submit the form
                    //     userform$.submit();
                }, 100);
                return;
            }

            var req = requests[i];
            // req.async = false;
            req.success = function(){
                // doUpdate(++i)
            };
            req.error = function(){
                console.error(arguments);
            };
            req.always = function(){
                // keep going even if there's an error
                doUpdate(++i)
            };

            debugLog(req);

            XNAT.xhr.request(req);
        }

        doUpdate(0);

    }
    usersAdvanced.updateUserRoles = updateUserRoles;

    $(document).ready(function(){
        var userform$ = $('#userform');
        usersAdvanced.form$ = userform$;
        $('#update-user-roles').on('click', function(e){
            e.preventDefault();
            xmodal.loading.open();
            updateUserRoles(userform$)
        });
    });


    //////////////////////////////////////////////////


    // this script has loaded
    usersAdvanced.loaded = true;

    return (XNAT.admin.usersAdvanced = usersAdvanced);

}));
