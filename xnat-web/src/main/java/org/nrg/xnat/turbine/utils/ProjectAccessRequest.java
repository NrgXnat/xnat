/*
 * web: org.nrg.xnat.turbine.utils.ProjectAccessRequest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.utils;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.ItemAccessHistory;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.XFTItemNotFoundException;
import org.nrg.xnat.exceptions.XNATException;
import org.nrg.xnat.utils.WorkflowUtils;

import javax.mail.MessagingException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nrg.xdat.om.base.BaseXnatProjectdata.getProjectByIDorAlias;
import static org.nrg.xdat.om.base.auto.AutoXnatProjectdata.*;
import static org.nrg.xft.event.XftItemEventI.OPERATION;

public class ProjectAccessRequest {
    public static final String PAR_BY_ID = " par_id=%s";
    public static final String PAR_BY_GUID = " guid='%s'";
    public static final String PAR_BY_USER_AND_PROJ_ID = " user_id=%s AND proj_id='%s'";
    public static final String PAR_BY_EMAIL = " xs_par_table.email='%s' AND approval_date IS NULL";

    public static final Map<String, Pattern> PAR_PATTERNS = new HashMap<String, Pattern>() {{
        put("ID %s", Pattern.compile(String.format(PAR_BY_ID, "(.*?)")));
        put("GUID %s", Pattern.compile(String.format(PAR_BY_GUID, "(.*?)")));
        put("ID %s for project %s", Pattern.compile(String.format(PAR_BY_USER_AND_PROJ_ID, "(.*?)", "(.*?)")));
        put("Email %s", Pattern.compile(String.format(PAR_BY_EMAIL, "(.*?)")));
    }};

    public ProjectAccessRequest(final String query, final UserI user) throws XNATException, SQLException, DBPoolException {
        XFTTable table = runInitQuery(query, user);
        table.resetRowCursor();
        int numRows = table.getNumRows();
        if (numRows == 0) {
            throw new XFTItemNotFoundException("Found no results for project access request search, should return one and only one.", user) {{
                setQuery(query);
            }};
        } else if (numRows > 1) {
            if (logger.isInfoEnabled()) {
                logger.info("Found " + numRows + " results for project access request search, should return one and only one for: " + getIdentifierFromQuery(query) + "\n" + query);
            }
        }
        initializeFromDataRow(table.nextRow());
    }

    @SuppressWarnings("RedundantThrows")
    public ProjectAccessRequest(Object[] row) throws SQLException, DBPoolException {
        initializeFromDataRow(row);
    }

    /**
     * @return the _userString
     */
    @SuppressWarnings("unused")
    public String getUserString() {
        return _userString;
    }
    /**
     * @param userString the user string to set
     */
    public void setUserString(String userString) {
        _userString = userString;
    }
    /**
     * @return The user ID for the request approver
     */
    @SuppressWarnings("unused")
    public Integer getApproverUserId() {
        return _approverUserId;
    }
    /**
     * @param approverUserId    The user ID to set for the request approver
     */
    public void setApproverUserId(Integer approverUserId) {
        _approverUserId = approverUserId;
    }
    /**
     * @return the username
     */
    public Integer getUserId() {
        return _userId;
    }

    /**
     * @param userId    The ID of the user
     */
    public void setUserId(Integer userId) {
        _userId = userId;
    }

    /**
     * Gets the email associated with this request.
     * @return A string containing the email associated with this request.
     */
    public String getEmail() {
		return _email;
	}

    /**
     * Sets the email associated with this request.
     * @param email    A string containing the email associated with this request.
     */
	public void setEmail(String email) {
		PoolDBUtils.CheckSpecialSQLChars(email);
		_email = email;
	}

	/**
     * Gets the email associated with this request.
     * @return A string containing the email associated with this request.
     */
    public String getHashedEmail() {
        return DigestUtils.md5Hex(_email);
    }

    /**
     * Gets the GUID for this request.
     * @return A string containing the GUID for this request.
     */
    public String getGuid() {
		return _guid;
	}

    /**
     * Sets the GUID for this request.
     * @param  guid    A string containing the GUID for this request.
     */
	public void setGuid(String guid) {
		PoolDBUtils.CheckSpecialSQLChars(guid);
		_guid = guid;
	}

	/**
     * @return the approvalDate
     */
    public Date getApprovalDate() {
        return _approvalDate;
    }
    /**
     * @param approvalDate the approvalDate to set
     */
    public void setApprovalDate(Date approvalDate) {
        _approvalDate = approvalDate;
    }
    /**
     * @return the createDate
     */
    public Date getCreateDate() {
        return _createDate;
    }
    /**
     * @param createDate the createDate to set
     */
    public void setCreateDate(Date createDate) {
        _createDate = createDate;
    }
    /**
     * @return the level
     */
    public String getLevel() {
        return _level;
    }
    /**
     * @param level the level to set
     */
    public void setLevel(String level) {
		PoolDBUtils.CheckSpecialSQLChars(level);
        _level = level;
    }
    /**
     * @return the _projectId
     */
    public String getProjectId() {
        return _projectId;
    }
    /**
     * @param projectId the _projectId to set
     */
    public void setProjectId(String projectId) {
		PoolDBUtils.CheckSpecialSQLChars(projectId);
        _projectId = projectId;
    }

    /**
     * @return the _requestId
     */
    public Integer getRequestId() {
        return _requestId;
    }

    /**
     * @param requestId the _requestId to set
     */
    public void setRequestId(Integer requestId) {
        _requestId = requestId;
    }

    /**
     * @return the approved
     */
    public Boolean getApproved(){
        return _approved;
    }

    /**
     * @param approved the approved to set
     */
    public void setApproved(boolean approved) {
        _approved = approved;
    }

    /**
     * Save the project access request with the <b>user</b> as the request approver.
     * @param user    The user to specify as the request approver.
     * @throws Exception When an unexpected error occurs.
     */
    public void save(UserI user) throws Exception{
        if (!CREATED_PAR_TABLE.get()) {
            CreatePARTable();
        }

        if (_requestId == null) {
            // New request
            StringBuilder query = new StringBuilder("INSERT INTO xs_par_table (proj_id, level, user_id");
            if (_approved !=null) {
                query.append(", approval_date, approved, approver_id");
            }
            query.append(")");
            query.append(" VALUES ('").append(_projectId).append("', '").append(_level).append("', ");
            query.append(_userId != null ? _userId : "NULL");
            if (_approved != null) {
                if (_approvalDate == null) {
                    query.append(", NOW()");
                } else {
                    query.append(", '").append(_approvalDate.toString()).append("'");
                }
                query.append(",").append(_approved).append(",").append(user.getID());
            }
            query.append(");");
            PoolDBUtils.ExecuteNonSelectQuery(query.toString(), user.getDBName(), user.getLogin());
        } else {
            // Existing request
            StringBuilder query = new StringBuilder("UPDATE xs_par_table SET proj_id='");
            query.append(_projectId).append("', level='").append(_level).append("'");

            if(_userId != null) {
                query.append(", user_id=").append(_userId);
            }
            if(_email != null) {
                query.append(", email='").append(_email).append("'");
            }
            if (_approved != null) {
                if (_approvalDate == null) {
                    query.append(", approval_date=NOW()");
                } else {
                    query.append(", approval_date='").append(_approvalDate.toString()).append("'");
                }
                query.append(" ,approved=").append(_approved);
            }

            query.append(" WHERE par_id=").append(_requestId).append(";");

            PoolDBUtils.ExecuteNonSelectQuery(query.toString(), user.getDBName(), user.getLogin());
        }
    }

    /**
     * Processes the project access request, setting the <b>user</b> as the accepting or declining user. Note that this
     * will also process all other requests associated with the same email as this request. This does <i>not</i> find
     * all requests with the same email as the specified user, just those with the same email as the initial request.
     * However, all processed requests are set to the email of the actual user object as they're processed.
     * @param user         The user who accepts or declines the request.
     * @param accept       Whether the request was accepted or declined.
     * @param eventType    A system event for tracking requests.
     * @param reason       The reason for the operation.
     * @param comment      Any related comments.
     * @return A list of all of the project access requests associated with the original email and accepted or declined.
     * @throws Exception When an unexpected error occurs.
     */
    public List<String> process(UserI user, boolean accept, EventUtils.TYPE eventType, String reason, String comment) throws Exception {
        return process(user, accept, eventType, reason, comment, true);
    }

    public static boolean isParTableCreated() {
        return CREATED_PAR_TABLE.get();
    }

    public static ProjectAccessRequest RequestPARById(Integer id, UserI user) {
        return RequestPAR(String.format(PAR_BY_ID, id.toString()), user);
    }

    public static ProjectAccessRequest RequestPARByGUID(String guid, UserI user) {
        return RequestPAR(String.format(PAR_BY_GUID, guid), user);
    }

    public static ProjectAccessRequest RequestPARByUserProject(Integer userId, String projectId, UserI user) {
        return RequestPAR(String.format(PAR_BY_USER_AND_PROJ_ID, userId.toString(), projectId), user);
    }

    public static List<ProjectAccessRequest> RequestPARsByUserEmail(String email, UserI user) {
        return RequestPARs(String.format(PAR_BY_EMAIL, email), user);
    }

    public static ProjectAccessRequest RequestPAR(String where, UserI user) {
        try {
            String query = getPARQuery(where);
            return new ProjectAccessRequest(query, user);
        } catch (Exception exception) {
            _logger.error("Error occurred while requesting project access request for user [" + user.getUsername() + "]: " + where, exception);
            return null;
        }
    }

    public static List<ProjectAccessRequest> RequestPARs(String where, UserI user) {
        final List<ProjectAccessRequest> PARs = new ArrayList<>();
        try {
            String query = getPARQuery(where);
            XFTTable table = runInitQuery(query, user);
            table.resetRowCursor();
            while (table.hasMoreRows()) {
                ProjectAccessRequest par = new ProjectAccessRequest(query, user);
                par.initializeFromDataRow(table.nextRow());
                PARs.add(par);
            }
        } catch (Exception exception) {
            final String identifier = user != null ? user.getUsername() : getIdentifierFromWhere(where);
            _logger.error("Error occurred while requesting project access requests for user [" + identifier + "]: " + where, exception);
        }
        return PARs;
    }

    private static String getIdentifierFromQuery(final String query) {
        if (query.contains("where ")) {
            return query.substring(query.indexOf("where ") + "where ".length());
        }
        return query;
    }

    private static String getIdentifierFromWhere(final String where) {
        for (final Map.Entry<String, Pattern> entry: PAR_PATTERNS.entrySet()) {
            Pattern pattern = entry.getValue();
            Matcher matcher = pattern.matcher(where);
            if (matcher.matches()) {
                if (matcher.groupCount() == 2) {
                    return String.format(entry.getKey(), matcher.group(1), matcher.group(2));
                } else {
                    return String.format(entry.getKey(), matcher.group(1));
                }
            }
        }
        return null;
    }

    public static void CreatePAR(String pID, String level, UserI user){
        try {
            if (!CREATED_PAR_TABLE.get()) {
                CreatePARTable();
            }

            String query = String.format("INSERT INTO xs_par_table (proj_id,user_id,level) VALUES ('%s', %d, '%s');", pID, user.getID(), StringUtils.remove(level, '\''));
            PoolDBUtils.ExecuteNonSelectQuery(query, user.getDBName(), user.getLogin());
        } catch (Exception e) {
            _logger.error("", e);
        }
    }

    public static synchronized void CreatePARTable() {
        try {
            if (!CREATED_PAR_TABLE.get()) {

                String query = "SELECT relname FROM pg_catalog.pg_class WHERE relname = LOWER('xs_par_table');";
                String exists = (String) PoolDBUtils.ReturnStatisticQuery(query, "relname", PoolDBUtils.getDefaultDBName(), null);

                if (exists != null) {
                    //check for email column (added 6/2/08)
                    boolean containsEmail = false, containsGuid = false;
                    PoolDBUtils con = new PoolDBUtils();
                    XFTTable t = con.executeSelectQuery("select LOWER(attname) as col_name,typname, attnotnull from pg_attribute, pg_class,pg_type where attrelid = pg_class.oid AND atttypid=pg_type.oid AND attnum>0 and LOWER(relname) = 'xs_par_table';", PoolDBUtils.getDefaultDBName(), null);
        			while (t.hasMoreRows()) {
        				t.nextRow();
                        String colName = t.getCellValue("col_name").toString();
                        if (colName.equals("email")) {
        					containsEmail =true;
                            if (containsGuid) {
        					break;
        				}
                        } else if (colName.equals("guid")) {
                            containsGuid = true;
                            if (containsEmail) {
                                break;
                            }
                        }
        			}

        			if (!containsEmail) {
        				query = "ALTER TABLE xs_par_table ADD COLUMN email VARCHAR(255);";
        				PoolDBUtils.ExecuteNonSelectQuery(query, PoolDBUtils.getDefaultDBName(), null);
        			}
                    if (!containsGuid) {
                        query = "ALTER TABLE xs_par_table ADD COLUMN guid VARCHAR(64);";
                        PoolDBUtils.ExecuteNonSelectQuery(query, PoolDBUtils.getDefaultDBName(), null);
                    }
                } else {
                    query = "CREATE TABLE xs_par_table"+
                    "\n("+
                    "\n  par_id SERIAL,"+
                    "\n  proj_id VARCHAR(255),"+
                    "\n  level VARCHAR(255),"+
                    "\n  create_date timestamp DEFAULT now(),"+
                    "\n  user_id integer,"+
                    "\n  approval_date timestamp ,"+
                    "\n  approved boolean,"+
                    "\n  approver_id integer," +
                    "\n  email VARCHAR(255)," +
                            "\n  guid VARCHAR(64)," +
                            "\n  CONSTRAINT xs_par_table_pkey PRIMARY KEY (par_id),"+
                            "\n  CONSTRAINT xs_par_table_guid_key UNIQUE (guid)"+
                    "\n)";

                    PoolDBUtils.ExecuteNonSelectQuery(query, PoolDBUtils.getDefaultDBName(), null);
                }
                CREATED_PAR_TABLE.set(true);
            }
        } catch (Exception exception) {
            _logger.error("Error occurred while creating PAR table", exception);
        }
    }

    public static void InviteUser(XnatProjectdata project, String invitee, UserI user, String subject, String access_level) throws Exception {
    	ProjectAccessRequest request = null;
		try {
            if (!CREATED_PAR_TABLE.get()) {
	             CreatePARTable();
	         }

	         invitee = StringUtils.remove(invitee, '\'');
            String guid = UUID.randomUUID().toString();

            PoolDBUtils.ExecuteNonSelectQuery("INSERT INTO xs_par_table (email, guid, proj_id, approver_id, level) VALUES ('" + invitee + "', '" + guid + "', '" + project.getId() + "', " + user.getID() + ", '" + access_level + "');", user.getDBName(), user.getLogin());

            request = RequestPAR("xs_par_table.email = '" + invitee + "' AND guid = '" + guid + "' AND proj_id = '" + project.getId() + "' AND approved IS NULL", user);
	    } catch (SQLException exception) {
	         _logger.error("Error occurred while running SQL query", exception);
	    } catch (Exception exception) {
	         _logger.error("General error occurred when inviting user " + invitee, exception);
	    }

        String bcc = null;
        if (ArcSpecManager.GetInstance().getEmailspecifications_projectAccess()) {
	        bcc = XDAT.getSiteConfigPreferences().getAdminEmail();
        }

        String from = XDAT.getSiteConfigPreferences().getAdminEmail();

        if(XDAT.getNotificationsPreferences().getSmtpEnabled()) {
            String body = XDAT.getNotificationsPreferences().getEmailMessageInviteProjectAccess();

            body = body.replaceAll("PROJECT_NAME", project.getName());
            body = body.replaceAll("SITE_NAME", TurbineUtils.GetSystemName());
            body = body.replaceAll("USER_FIRSTNAME", user.getFirstname());
            body = body.replaceAll("USER_LASTNAME", user.getLastname());
            body = body.replaceAll("SITE_URL",TurbineUtils.GetFullServerPath());
            String adminEmail = "<a href=\"mailto:" + XDAT.getSiteConfigPreferences().getAdminEmail() + "\">" + XDAT.getSiteConfigPreferences().getAdminEmail() + "</a>";

            body = body.replaceAll("ADMIN_MAIL", adminEmail);

            String acceptURL = TurbineUtils.GetFullServerPath() + "/app/action/AcceptProjectAccess/par/" + request._guid + "?hash=" + request.getHashedEmail();

            String acceptLink = "<a href=\"" + acceptURL + "\">" + acceptURL + ".</a>";

            body = body.replaceAll("ACCEPT_URL", acceptLink);

            try {
                XDAT.getMailService().sendHtmlMessage(from, invitee, user.getEmail(), bcc, subject, body);
            } catch (MessagingException exception) {
                _logger.error("Unable to send mail", exception);
                throw exception;
            }
        }


    }

    @SuppressWarnings({"unused", "RedundantThrows"})
    private ProjectAccessRequest() throws SQLException, DBPoolException {
        // Here for creation of objects from initialization functions.
    }

    private List<String> process(final UserI user, final boolean accept, final EventUtils.TYPE eventType, final String reason, final String comment, final boolean processRelated) throws Exception {

        final String parEmail = getEmail();

    	setUserId(user.getID());
		setApproved(accept);
		setApprovalDate(Calendar.getInstance().getTime());
        if (!StringUtils.equalsIgnoreCase(parEmail, user.getEmail())) {
            setEmail(user.getEmail());
        }
        save(user);

		if (accept) {

            final XnatProjectdata     project   = getXnatProjectdatasById(_projectId, null, false);
            final PersistentWorkflowI workflow  = WorkflowUtils.getOrCreateWorkflowData(null, user, SCHEMA_ELEMENT_NAME, _projectId, _projectId, EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, eventType, EventUtils.ADD_USER_TO_PROJECT, reason, comment));
            final EventMetaI          eventInfo = workflow.buildEvent();

			try {
                for (final UserGroupI group : Groups.getGroupsForUser(user).values()) {
                    if (StringUtils.equals(group.getTag(), _projectId) && !UserHelper.getUserHelperService(user).isOwner(_projectId)) {
                        Groups.removeUserFromGroup(user, user, group.getId(), eventInfo);
                    }
                }

				if (!_level.startsWith(project.getId())) {
					_level = project.getId() + "_" + _level;
				}

				final UserGroupI group = Groups.addUserToGroup(_level, user, user, eventInfo);
                Groups.updateUserForGroup(user, group.getId(), group);

                XDAT.triggerXftItemEvent(project, XftItemEventI.UPDATE, ImmutableMap.of(OPERATION, Groups.OPERATION_ADD_USERS, Groups.USERS, Collections.singleton(user.getUsername())));
				WorkflowUtils.complete(workflow, eventInfo);

				try {
					ItemAccessHistory.LogAccess(user, project.getItem(), "report");
				} catch (Throwable e) {
					_logger.error("", e);
				}
			} catch (Exception e) {
				WorkflowUtils.fail(workflow, eventInfo);
				throw e;
			}
        }

        List<String> processedProjects = new ArrayList<String>() {{
            add(getProjectByIDorAlias(getProjectId(), user, false).getDisplayName());
        }};
        if (processRelated) {
            processedProjects.addAll(processRelatedPARs(parEmail, user, accept, eventType, reason, comment));
        }
        return processedProjects;
    }

    private static XFTTable runInitQuery(final String query, final UserI user) throws SQLException, DBPoolException {
        if (!CREATED_PAR_TABLE.get()) {
            CreatePARTable();
        }

        final String dbName = user != null ? user.getDBName() : PoolDBUtils.getDefaultDBName();
        final String login = user != null ? user.getLogin() : null;

        return XFTTable.Execute(query, dbName, login);
    }

    private void initializeFromDataRow(final Object[] row) {
        if (row[0] != null) {
            setRequestId((Integer) row[0]);
        }

        if (row[1] != null) {
            setProjectId((String) row[1]);
        }

        if (row[2] != null) {
            setLevel((String) row[2]);
        }

        if (row[3] != null) {
            setCreateDate((Date) row[3]);
        }

        if (row[4] != null) {
            setUserId((Integer) row[4]);
        }

        if (row[5] != null) {
            setApprovalDate((Date) row[5]);
        }

        if (row[6] != null) {
            setApproved((Boolean) row[6]);
        }

        if (row[7] != null) {
            setApproverUserId((Integer) row[7]);
        }

        if (row[8] != null) {
            setUserString((String) row[8]);
        }

        if (row[9] != null) {
            setEmail((String) row[9]);
        }

        if (row[10] != null) {
            setGuid((String) row[10]);
        }
    }

    private List<String> processRelatedPARs(String parEmail, UserI user, boolean accept, EventUtils.TYPE eventType, String reason, String comment) {
        final List<String> processedProjects = new ArrayList<>();
        final List<ProjectAccessRequest> parsForEmail = RequestPARsByUserEmail(parEmail, user);
        for(final ProjectAccessRequest par : parsForEmail) {
            try {
                processedProjects.addAll(par.process(user, accept, eventType, reason, comment, false));
            } catch (Exception exception) {
                _logger.error("Error occurred trying to process project access request " + par.getRequestId());
            }
        }
        return processedProjects;
    }

    private static String getPARQuery(String where) {
        return String.format(QUERY_GET_PAR, where);
    }

    private static final Logger _logger = Logger.getLogger(ProjectAccessRequest.class);
    private static final String QUERY_GET_PAR = "SELECT par_id, proj_id, level, create_date, user_id, approval_date, approved, approver_id, firstname || ' ' || lastname || '(' || xdat_user.email || ')' as user_string, xs_par_table.email, guid FROM xs_par_table LEFT JOIN xdat_user ON xs_par_table.user_id = xdat_user.xdat_user_id WHERE %s;";
    private static final AtomicBoolean CREATED_PAR_TABLE = new AtomicBoolean();

    private Integer _requestId = null;
    private String _projectId = null;
    private String _level = null;
    private String _email = null;
    private String _guid = null;
    private Integer _userId = null;
    private Date _createDate = null;
    private Date _approvalDate = null;
    private Integer _approverUserId = null;
    private Boolean _approved = null;
    private String _userString = null;
}
