/*
 * web: org.nrg.xnat.restlet.resources.ProjectMemberResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import com.noelios.restlet.ext.servlet.ServletCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.nrg.action.ActionException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ProjectMemberResource extends SecureResource {
	XnatProjectdata proj=null;
	UserGroupI group=null;
	ArrayList<UserI> newUsers= new ArrayList<>();
	ArrayList<String> unknown= new ArrayList<>();
	String gID=null;
    boolean displayHiddenUsers = false;

	public ProjectMemberResource(Context context, Request request, Response response) {
		super(context, request, response);

		this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
		this.getVariants().add(new Variant(MediaType.TEXT_HTML));
		this.getVariants().add(new Variant(MediaType.TEXT_XML));

		final UserI user = getUser();

		String pID = getUrlEncodedParameter(request, "PROJECT_ID");
		if (pID != null) {
			proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);
		}

		gID = getUrlEncodedParameter(request, "GROUP_ID");

		group = Groups.getGroup(gID);

		if (group == null) {
			group = Groups.getGroup(pID + "_" + gID);
		}

		if (group == null) {
			try {
				for (UserGroupI gp : Groups.getGroupsByTag(pID)) {
					if (StringUtils.equals(gID, gp.getDisplayname())) {
						group = gp;
						break;
					}
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}


		String tempValue = (String) getParameter(request, "USER_ID");
		try {
			String[] ids;
			if (tempValue.contains(",")) {
				ids = tempValue.split(",");
			} else {
				ids = new String[] {tempValue};
			}

			for (final String id : ids) {
				String  uID          = id.trim();
				Integer xdat_user_id = null;
				try {
					xdat_user_id = Integer.parseInt(uID);
				} catch (NumberFormatException ignored) {

				}


				if (xdat_user_id == null) {
					//login or email
					UserI newUser = null;
					try {
						newUser = Users.getUser(uID);
					} catch (UserNotFoundException ignored) {
					}
					if (newUser == null) {
						//by email
						List<? extends UserI> items = Users.getUsersByEmail(uID);
						if (items.size() > 0) {
							newUsers.addAll(items);
						} else {
							unknown.add(uID);
						}
					} else {
						newUsers.add(newUser);
					}
				} else {
					UserI tempUser = Users.getUser(xdat_user_id);
					if (tempUser != null) {
						newUsers.add(tempUser);
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		displayHiddenUsers = Boolean.parseBoolean((String) getParameter(request, "DISPLAY_HIDDEN_USERS"));

	}

	@Override
	public boolean allowPut() {
		return true;
	}

	@Override
	public boolean allowDelete() {
		return true;
	}
	
	@Override
	public void handleDelete() {
		if(proj==null || group==null || newUsers.size()==0){
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}else{
			final UserI user = getUser();
			try {
				if(Permissions.canDelete(user,proj)){
					try {
						for(UserI newUser: newUsers){
						    proj.removeGroupMember(group.getId(), newUser, user,newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.REMOVE_USER_FROM_PROJECT + " (" + newUser.getLogin() + ")"));
						}
					} catch (Exception e) {
						logger.error("",e);
					}
				}else{
					getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
				}
			} catch (Exception e) {
				logger.error("",e);
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		}
		returnDefaultRepresentation();
	}

	@Override
	public void handlePut() {
		HttpServletRequest request = ServletCall.getRequest(getRequest());
		if(proj==null || group==null){
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}else{
			try {
				final UserI user = getUser();
				if(Permissions.canDelete(user,proj)){
					if (unknown.size() > 0) {
						//NEW USER
						try {
							for (String uID : unknown) {
								VelocityContext context = new VelocityContext();
								context.put("user", user);
								context.put("server", TurbineUtils.GetFullServerPath(request));
								context.put("siteLogoPath", XDAT.getSiteLogoPath());
								context.put("process", "Transfer to the archive.");
								context.put("system", TurbineUtils.GetSystemName());
								context.put("access_level", gID);
								context.put("admin_email", XDAT.getSiteConfigPreferences().getAdminEmail());
								context.put("projectOM", proj);
								//SEND email to user
								final PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, proj.getId(), proj.getId(), newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.INVITE_USER_TO_PROJECT + " (" + uID + ")"));
								try {
									ProjectAccessRequest.InviteUser(context, uID, user, user.getFirstname() + " " + user.getLastname() + " has invited you to join the " + proj.getName() + " " + DisplayManager.GetInstance().getSingularDisplayNameForProject().toLowerCase() + ".");
									WorkflowUtils.complete(wrk, wrk.buildEvent());
								} catch (Exception e) {
									WorkflowUtils.fail(wrk, wrk.buildEvent());
									logger.error("", e);
								}
							}
						} catch (Throwable e) {
							logger.error("", e);
						}
					}

					if (newUsers.size() > 0) {
						//CURRENT USER

						String email = (this.isQueryVariableTrue("sendemail")) ? "true" : "false";


						boolean sendmail = Boolean.parseBoolean(email);

						for (UserI newUser : newUsers) {
							if(newUser!=null && newUser.getID().equals(Users.getGuest().getID())){
								getResponse().setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED);
							} else {
								final PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, Users.getUserDataType(), newUser.getID().toString(), proj.getId(), newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.ADD_USER_TO_PROJECT));
								EventMetaI c = wrk.buildEvent();

								proj.addGroupMember(group.getId(), newUser, user, WorkflowUtils.setStep(wrk, "Add " + newUser.getLogin()));
								WorkflowUtils.complete(wrk, c);

								if (sendmail) {
									try {
										VelocityContext context = new VelocityContext();

										context.put("user", user);
										context.put("server", TurbineUtils.GetFullServerPath(request));
										context.put("siteLogoPath", XDAT.getSiteLogoPath());
										context.put("process", "Transfer to the archive.");
										context.put("system", TurbineUtils.GetSystemName());
										context.put("access_level", "member");
										context.put("admin_email", XDAT.getSiteConfigPreferences().getAdminEmail());
										context.put("projectOM", proj);
										org.nrg.xnat.turbine.modules.actions.ProcessAccessRequest.SendAccessApprovalEmail(context, newUser.getEmail(), user, TurbineUtils.GetSystemName() + " Access Granted for " + proj.getName());
									} catch (Throwable e) {
										logger.error("", e);
									}
								}
							}
						}
					}
				}else{
					getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
				}
			} catch (InvalidItemException e) {
				logger.error("",e);
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			} catch (ActionException e) {
				getResponse().setStatus(e.getStatus());
				return;
			} catch (Exception e) {
				logger.error("",e);
					getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			}
			returnDefaultRepresentation();
		}
	}

	@Override
	public Representation represent(Variant variant) {
		XFTTable table=null;
		if(proj!=null){
			try {
                StringBuilder query = new StringBuilder("SELECT g.id AS \"GROUP_ID\", displayname,login,firstname,lastname,email FROM xdat_userGroup g RIGHT JOIN xdat_user_Groupid map ON g.id=map.groupid RIGHT JOIN xdat_user u ON map.groups_groupid_xdat_user_xdat_user_id=u.xdat_user_id WHERE tag='").append(proj.getId()).append("' ");
                if(!displayHiddenUsers){
                    query.append(" and enabled = 1 ");
                }
                query.append(" ORDER BY g.id DESC;");
				final UserI user = getUser();
                table = XFTTable.Execute(query.toString(), user.getDBName(), user.getLogin());
			} catch (SQLException | DBPoolException e) {
				logger.error("",e);
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		}
		
		Hashtable<String,Object> params=new Hashtable<>();
		params.put("title", "Projects");

		MediaType mt = overrideVariant(variant);
		
		if(table!=null)params.put("totalRecords", table.size());
		return this.representTable(table, mt, params);
	}
}
