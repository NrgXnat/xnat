
/*
 * web: parManager.js
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
}(function() {
	var projectAccess,
		pars,
		undefined,
		parContainer = $('#user_mgmt_div'),
		rootUrl = XNAT.url.rootUrl,
        csrfUrl = XNAT.url.csrfUrl,
		projectId = XNAT.data.context.projectID;
	
	projectParUrl = function(){
		return rootUrl('/REST/projects/'+projectId+'/pars')
	};
    siteParUrl = function(){
        return rootUrl('/REST/pars');
    };
    parActionUrl = function(parId,action){
        return csrfUrl('/REST/pars/'+parId+'?'+action+'=true&format=json')
    };

	XNAT.projectAccess = projectAccess =
		getObject(XNAT.projectAccess || {});

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

	/*
	 * Project PAR table
	 */
	function parDate(dateString){
		var d = new Date(dateString);
		return d.toLocaleString();
	}
	function parStatus(approved){
		switch (approved) {
			case 'false':
				return 'Declined';
				break;
			case 'true':
				return 'Accepted';
				break;
			default:
				return '';
		}
	}
    function parLevel(groupId){
        // split the project ID from the group ID and return just the group label
        var g = groupId.split('_');
        return g[g.length-1];
    }

	XNAT.projectAccess.showParsInProject = function(pars){
		var parContainer = $('#invite_container');
		if (pars.length) {
			parContainer.empty().append(
				XNAT.table.dataTable(pars,{
					className: 'clean compact',
					items: {
						create_date: {
							label: 'Date Created',
							td: { className: 'left' },
                            th: { className: 'left' },
							apply: function(create_date){
								return parDate.call(this, create_date);
							}
						},
						email: {
							label: 'Invited User',
                            td: { className: 'left' },
                            th: { className: 'left' }
						},
                        level: {
                            label: 'Project Role',
                            td: { className: 'left' },
                            th: { className: 'left' },
                            apply: function(level){
                                return parLevel.call(this, level);
                            }
                        },
						approved: {
							label: 'Status',
                            td: { className: 'left' },
                            th: { className: 'left' },
							apply: function(approved){
								return parStatus.call(this, approved);
							}
                        }
					}
				}).element
			);
		} else {
			parContainer.append('<p>No outstanding invitations to this project</p>');
		}
	};

	/*
	 * PAR notice on home page
	 */
    function parActions(parId,project){
        var acceptButton = '<a href="javascript:XNAT.projectAccess.acceptPar(\''+parId+'\',\''+project.id+'\',\''+project.name+'\')"><button class="btn">Accept</button></a>';
        var declineButton = '<a href="javascript:XNAT.projectAccess.declinePar(\''+parId+'\',\''+project.id+'\',\''+project.name+'\')"><button class="btn">Decline</button></a>';
        return acceptButton + '&nbsp;' + declineButton;
    }

	XNAT.projectAccess.showParsAtLogin = function(pars){
		XNAT.ui.dialog.open({
			title: 'Project Invitations',
            width: 500,
            nuke: true,
            content: '<div class="panel"></div>',
            beforeShow: function(obj){
                var parContainer = obj.$modal.find('.panel');
                if (pars.length > 0) {
                    parContainer.append(
                        XNAT.table.dataTable(pars, {
                            items: {
                                secondary_id: {
                                    label: 'Project',
                                    td: { className: 'left' },
                                    th: { className: 'left' }
                                },
                                level: {
                                    label: 'Role',
                                    td: { className: 'left' },
                                    th: { className: 'left' },
                                    apply: function(level){
                                        return parLevel.call(this, level);
                                    }
                                },
                                buttons: {
                                    label: 'Action',
                                    th: { className: 'left' },
                                    apply: function(){
                                        var project = { id: this.proj_id, name: this.name };
                                        return parActions.call(this, this.par_id, project);
                                    }
                                }

                            }
                        }).element
                    );
                } else {
                    parContainer.append('<p>You have no outstanding Project Access Requests.</p>');
                }
            },
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    action: function(){
                        if (pars.length === 0) {
                            $('#login_par_message').slideUp();
                        }
                    }
                }
            ]
		});
	};

    /*
     * User-initiated actions -- request to join project, accept or decline invitation
     */
    XNAT.projectAccess.requestAccess = function(reqProjectId){
        if (!reqProjectId) {
            errorHandler({
                statusText: 'Function: requestAccess',
                responseText: 'Cannot request access, no project ID specified.'
            });
            return false;
        }
        XNAT.xhr.getJSON({
            url: rootUrl('/REST/projects/'+reqProjectId+'/groups?format=json'),
            fail: function(e){
                errorHandler(e);
            },
            success: function(data){
                var groups = data.ResultSet.Result;
                if (groups.length > 0) {

                    var groupSelectOptions = [];

                    groups.forEach(function(group){
                        // split the group ID name (e.g. "project_id_owners") and use just the last element.
                        var id = group.id.split('_');
                        groupSelectOptions.push({
                            value: id[id.length - 1],
                            label: group.displayname
                        })
                    });

                    // generate the form first
                    var requestForm$ = $.spawn('div.panel').spawn('form', {
                        method: 'post',
                        action: rootUrl('/app/action/RequestAccess')
                    }, [

                        // by putting these elements in an array inside the spawned
                        // 'form' element, they will be automatically appended
                        spawn('p', 'Upon submission of this form an email will be sent to the project manager. The manager will be asked to give you access to this project. Once the manager approves or denies your access, an email will be sent to you.'),

                        XNAT.ui.input.hidden({
                            name: 'XNAT_CSRF',
                            value: window.csrfToken
                        }).get(),

                        XNAT.ui.input.hidden({
                            name: 'project',
                            value: reqProjectId
                        }).get(),

                        XNAT.ui.panel.select.single({
                            name: 'access_level',
                            options: groupSelectOptions,
                            label: 'Access Level',
                            validation: 'required not-empty'
                        }).get(),

                        XNAT.ui.panel.textarea({
                            name: 'comments',
                            label: 'Comments',
                            code: 'text',
                            description: 'Comments will be included in the <br> email to the project manager.'
                        }).get()

                    ]);

                    XNAT.ui.dialog.open({
                        title: 'Project Access Request Form',
                        width: 580,
                        padding: 30,
                        content: requestForm$,
                        buttons: [
                            {
                                label: 'Submit Request',
                                isDefault: true,
                                close: false,
                                action: function(dialog){
                                    // validate role selection
                                    var form$ = dialog.body$.find('form');
                                    if (XNAT.validate(form$.find('.required')).all('required').check()) {
                                        form$.submit();
                                        xmodal.loading.open('Submitting Request');
                                    }
                                    else {
                                        XNAT.ui.banner.top(2500, '<b>Error:</b> Please Select A Role.', 'alert');
                                    }
                                }
                            },
                            {
                                label: 'Cancel',
                                isDefault: false,
                                close: true
                            }
                        ]

                    });
                }
                else {
                    errorHandler({
                        statusText: 'No groups found',
                        responseText: 'Could not request access to this project.'
                    });
                    return false;
                }

            }
        });

    };

    XNAT.projectAccess.acceptPar = function(parId,projectId,projectName){
        if (!parId) {
            errorHandler({
                statusText: 'Function: acceptPar',
                responseText: 'Could not accept access request. No PAR ID found.'
            });
            return false;
        }
        XNAT.xhr.putJSON({
            url: parActionUrl(parId,'accept'),
            success: function(){
                XNAT.ui.banner.top(2000, '<b>Accepted Invitation</b> to <a href="/REST/projects/'+projectId+'">'+projectName+'</a>.', 'success');
                XNAT.ui.dialog.closeAll();
                XNAT.projectAccess.initPars('site');
            },
            fail: function(e){
                errorHandler(e);
            }
        });
    };

    XNAT.projectAccess.declinePar = function(parId,projectId,projectName){
        if (!parId) {
            errorHandler({
                statusText: 'Function: declinePar',
                responseText: 'Could not decline access request. No PAR ID found.'
            });
            return false;
        }
        XNAT.xhr.putJSON({
            url: parActionUrl(parId,'decline'),
            success: function(){
                XNAT.ui.banner.top(2000, '<b>Declined Invitation</b> to '+projectName+'.', 'note');
                XNAT.ui.dialog.closeAll();
                XNAT.projectAccess.initPars('site');
            },
            fail: function(e){
                errorHandler(e);
            }
        });
    };
	
	/*
	 * Init
	 */
	XNAT.projectAccess.initPars = function(config){
		switch (config) {
            case 'project':
                XNAT.xhr.getJSON({
                    url: projectParUrl(),
                    success: function(data){
                        var rawPars = data.ResultSet.Result, pars = [];
                        rawPars.forEach(function(par){
                            if (par.email) pars.push(par); // weed out improperly captured PARs.
                        });
                        pars.sort(function(a,b){
                            var aTimestamp = new Date(a.create_date),
                                bTimestamp = new Date(b.create_date);
                            return (bTimestamp - aTimestamp) / Math.abs(bTimestamp - aTimestamp);
                        });
                        XNAT.projectAccess.showParsInProject(pars);
                    },
                    fail: function(e){
                        errorHandler(e);
                    }
                });
                break;
            case 'site':
                XNAT.xhr.getJSON({
                    url: siteParUrl(),
                    success: function(data){
                        pars = data.ResultSet.Result;
                        XNAT.projectAccess.showParsAtLogin(pars);
                    },
                    fail: function(e){
                        errorHandler(e);
                    }
                });
                break;
            default:
                console.log('Sorry, I don\'t know what PAR action to take.');
                return false;
        }
	};

}));

