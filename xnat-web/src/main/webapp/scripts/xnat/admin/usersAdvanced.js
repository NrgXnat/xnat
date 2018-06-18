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


    var listedProjects = {};
    usersAdvanced.listedProjects = listedProjects;

    // keep track of projects the user doesn't belong to yet
    usersAdvanced.availableProjects = {};


    // get project ID from the group_id
    function extractProjectId(str){
        return str.replace(/(_owner|_member|_collaborator)$/, '');
    }

    // get gorup/role label from group_id
    function extractRoleLabel(group){
        if (/_owner$/i.test(group)) {
            return 'Owners'
        }
        if (/_member$/i.test(group)) {
            return 'Members'
        }
        if (/_collaborator$/i.test(group)) {
            return 'Collaborators'
        }
    }

    // gets list of ALL projects on the system
    function getAllProjects(success, failure){
        return XNAT.xhr.getJSON({
            url: XNAT.url.restUrl('/data/projects', ['format=json']),
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

    // gets list of all projects then removes user projects
    function getAvailableProjects(callback){
        getAllProjects().done(function(allProjects){
            getUserProjects().done(function(userProjects){
                var jqXHR = this;
                var availableProjects = [];
                var availableProjectIds = [];
                var userProjectIds = userProjects.map(function(userProj){
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
                usersAdvanced.listedProjects = userProjects;
                if (isFunction(callback)) {
                    callback.call(jqXHR, availableProjects, availableProjectIds)
                }
            })
        })
    }


    usersAdvanced.tableContainer = $$(usersAdvanced.tableContainer || '#user-group-membership-table');


    function linkToProject(project, text){
        return spawn('a', {
            href: project.URI,
            target: '_blank',
            title: project.name
        }, escapeHtml(text))
    }

    function addNewRole(user){
        getAvailableProjects(function(availableProj){

            var sortedProj = sortObjects(availableProj, 'secondary_ID');

            var addRoleDialog = XNAT.dialog.open({
                width: 500,
                minHeight: 250,
                padding: 30,
                title: 'Add User to Project Group',
                content: (function(){

                    var addRolePanel = XNAT.spawner.spawn({
                        addRolePanel: {
                            kind: 'panel',
                            header: false,
                            footer: false,
                            border: false,
                            padding: 0,
                            contents: {
                                projectSelect: {
                                    kind: 'panel.select.menu',
                                    id: '',
                                    name: 'project',
                                    label: 'Select Project',
                                    options: sortedProj.map(function(prj){
                                        return {
                                            label: prj.secondary_ID,
                                            value: prj.ID
                                        }
                                    })
                                },
                                roleSelect: {
                                    kind: 'panel.select.menu',
                                    id: '',
                                    name: 'role',
                                    label: 'Select Group',
                                    options: [
                                        { label: 'Owners', value: '_owner' },
                                        { label: 'Members', value: '_member' },
                                        { label: 'Collaborators', value: '_collaborator' }
                                    ]
                                }
                            }
                        }
                    });

                    return addRolePanel.get();

                })(),
                beforeShow: function(){
                    // all of this for the fancy menus...
                    this.dialog$.css({ overflow: 'visible' });
                    this.dialogBody$.css({ overflow: 'visible' });
                    this.content$.css({ overflow: 'visible' });
                    menuInit(this.dialogBody$.find('select[name="project"]'), {}, 250);
                    menuInit(this.dialogBody$.find('select[name="role"]'), {}, 200);
                },
                buttons: [
                    {
                        label: 'Save',
                        isDefault: true,
                        close: false,
                        action: function(dlgObj){
                            var dlg$ = dlgObj.dialog$;
                            var projMenu = dlg$.find('select[name="project"]');
                            var roleMenu = dlg$.find('select[name="role"]');
                            XNAT.xhr.put({
                                url: XNAT.url.rootUrl('/xapi/users/' + editUser + '/groups/' + projMenu.val() + roleMenu.val())
                            }).done(function doneFn(){
                                // update the list after saving
                                window.setTimeout(function(){
                                    userGroupsTable();
                                    addRoleDialog.close();
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

    function confirmChange(user, groupId){
        var newRole = extractRoleLabel(groupId);
        var projectId = extractProjectId(groupId);
        var proj = listedProjects[projectId];
        var confirmChangeDialog = XNAT.dialog.confirm({
            // width: 500,
            // padding: 30,
            title: 'Change Role?',
            content: 'Change role of <b>"' + user + '"</b> for project <b>"' + proj.secondary_ID + '"</b> to <b>"' + newRole.toLowerCase().slice(0, -1) + '"</b>?',
            okLabel: 'Change',
            okClose: false,
            okAction: function(obj){
                // do we need to delete old role first?
                // XNAT.xhr['delete']({
                //     url: '/xapi/users/' + user + '/groups/' + proj.group_id
                // }).done(function(){})
                XNAT.xhr.put({
                    url: '/xapi/users/' + user + '/groups/' + groupId
                }).done(function(){
                    XNAT.ui.banner.top(2000, 'Role changed.', 'success');
                    userGroupsTable();
                }).fail(function(){
                    XNAT.ui.banner.top(3000, 'An error occured when attempting to set the user role.', 'error');
                }).always(function(){
                    confirmChangeDialog.close();
                })
            }
        })
    }

    function confirmRemove(user, groupId){
        var projectId = extractProjectId(groupId);
        var proj = listedProjects[projectId];
        var confirmRemoveDialog = XNAT.dialog.confirm({
            // width: 500,
            // padding: 30,
            title: 'Remove Role?',
            content: 'Remove all access to project <b>"' + proj.secondary_ID + '"</b> for user <b>"' + user + '"</b>?',
            okLabel: 'Remove',
            okClose: false,
            okAction: function(obj){
                XNAT.xhr['delete']({
                    url: '/xapi/users/' + user + '/groups/' + proj.group_id
                }).done(function(){
                    XNAT.ui.banner.top(2000, 'Project access removed.', 'success');
                    userGroupsTable();
                }).fail(function(){
                    XNAT.ui.banner.top(3000, 'An error occured when attempting to remove project access.', 'error');
                }).always(function(){
                    confirmRemoveDialog.close();
                })
            }
        })
    }


    // renders the table for listing and assigning group membership
    function userGroupsTable(container, username){

        var tableContainer = usersAdvanced.tableContainer = $$(container || usersAdvanced.tableContainer);

        getUserProjects().done(function(projects){

            if (!projects || !projects.length) {
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
            forEach(projects, function(project){
                listedProjects[project.ID] = project;
            });

            var rolesTable = XNAT.table.dataTable(projects, {
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
                    // _ID: '~data-project-id',
                    // _group_id: '~data-group-id',
                    secondary_ID: {
                        label: 'Project Label',
                        sort: true,
                        filter: true,
                        apply: function(){
                            return linkToProject(this, this.secondary_ID)
                        }
                    },
                    ID: {
                        label: 'Project ID',
                        sort: true,
                        filter: true,
                        apply: function(){
                            return linkToProject(this, this.ID)
                        }
                    },
                    role: {
                        label: 'Group',
                        th: { style: { width: '160px' }},
                        td: { style: { width: '160px' }},
                        apply: function(){
                            var project = this;
                            var roleMenu = XNAT.ui.select.menu({
                                element: {
                                    classes: 'select-user-role',
                                    // data: {
                                    //     group: project.group_id,
                                    //     project: project.ID
                                    // },
                                    title: 'Select: ' + editUser + ' | ' + project.role + ' | ' + project.ID
                                },
                                options: [
                                    { label: 'Owners', value: project.ID + '_owner' },
                                    { label: 'Members', value: project.ID + '_member' },
                                    { label: 'Collaborators', value: project.ID + '_collaborator' }
                                ],
                                value: project.group_id
                            });
                            menuInit(roleMenu.element, null, 140);
                            return spawn('div.center', [roleMenu.spawned])
                        }
                    },
                    REMOVE: {
                        label: 'Remove',
                        th: { style: { width: '80px' }},
                        td: { style: { width: '80px' }},
                        apply: function(){
                            var project = this;
                            var btn = spawn('a.remove-user-role.nolink.btn-hover', {
                                href: '#!remove=' + project.group_id,
                                title: 'Remove: ' + editUser + ' | ' + project.role + ' | ' + project.ID
                            }, '<b class="x">&times;</b>');
                            return spawn('div.center', [btn]);
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
            tableContainer.empty().append(rolesTable.spawned);

        });

        // event listeners for editing and deleting - remove and re-add
        // every time the table renders to prevent multiple listeners
        tableContainer.off('change.select-role').on('change.select-role', '.select-user-role', function(e){
            e.preventDefault();
            confirmChange(editUser, this.value);
        });

        tableContainer.off('click.remove-role').on('click.remove-role', '.remove-user-role', function(e){
            e.preventDefault();
            console.log('deleting role');
            console.log(this.title);
            var groupId = this.getAttribute('href').split('#!remove=')[1];
            confirmRemove(editUser, groupId);
        });

    }
    usersAdvanced.userGroupsTable = userGroupsTable;

    // render the table when the script loads (or call it from the page?)
    userGroupsTable();


    $(document).on('click', '#user-add-group', function userAddGroup(){
        addNewRole()
    });


    function updateUserRoles(form$){

        var userform$ = $$(form$ || '#userform');
        var accessInputs$ = userform$.find('input.access');

        var username = userform$.find('[name="xdat:user.login"]').val();

        function roleUrl(part){
            return XNAT.url.rootUrl('/xapi/users/' + username + part);
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
                    // ...submit the form
                    userform$.submit();
                }, 500);
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
