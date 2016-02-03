/*
 * org.nrg.xnat.restlet.servlet.XNATRestletServlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/11/13 3:33 PM
 */
package org.nrg.xnat.restlet.servlet;

import com.noelios.restlet.ext.servlet.ServerServlet;
import org.apache.commons.io.FileUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.dcm.DicomSCPManager;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xft.event.EventListener;
import org.nrg.xft.event.EventManager;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.editscript.DicomEdit;
import org.nrg.xnat.helpers.merge.AnonUtils;
import org.nrg.xnat.helpers.prearchive.PrearcConfig;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.security.XnatPasswordEncrypter;
import org.nrg.xnat.services.PETTracerUtils;
import org.nrg.xnat.workflow.WorkflowSaveHandlerAbst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.authority.AuthorityUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class XNATRestletServlet extends ServerServlet {
    private static final long serialVersionUID = -4149339105144231596L;

    public static ServletConfig REST_CONFIG=null;

    private final Logger logger = LoggerFactory.getLogger(XNATRestletServlet.class);

    /**
     * Get the username of the site administrator. If there are multiple
     * site admins, just get the first one. If none are found, return null.
     * @return The name of the admin user.
     */
    @SuppressWarnings("unchecked")
    private String getAdminUser() throws Exception {
        for (String login : Users.getAllLogins()) {
            final UserI user = Users.getUser(login);
            if (Roles.isSiteAdmin(user)) {
                return login;
            }
        }
        return null;
    }

    @Override
    public void init() throws ServletException {
        super.init();

        updateAuthTable();

        XNATRestletServlet.REST_CONFIG=this.getServletConfig();
        try {
            String path = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.SITE_WIDE, "");
            Configuration init_config = AnonUtils.getService().getScript(path, null);
            if (init_config == null) {
                logger.info("Creating Script Table.");
                String site_wide = FileUtils.readFileToString(AnonUtils.getDefaultScript());
                String adminUser = this.getAdminUser();
                if (adminUser != null) {
                    AnonUtils.getService().setSiteWideScript(adminUser, path,site_wide);
                } else {
                    throw new Exception("Site administrator not found.");
                }
            }
            // there is a default site-wide script, so nothing to do here for the else.
        } catch (Throwable e){
            logger.error("Unable to either find or initialize script database", e);
        }

        // blatant copy of how we initialize the anon script
        try {
            String path = PETTracerUtils.buildScriptPath(PETTracerUtils.ResourceScope.SITE_WIDE, "");
            Configuration init_config = PETTracerUtils.getService().getTracerList(path, null);
            if (init_config == null) {
                logger.info("Creating PET Tracer List.");
                String site_wide = FileUtils.readFileToString(PETTracerUtils.getDefaultTracerList());
                String adminUser = this.getAdminUser();
                if (adminUser != null) {
                    PETTracerUtils.getService().setSiteWideTracerList(adminUser, path, site_wide);
                } else {
                    throw new Exception("Site administrator not found.");
                }
            }
            // there is a default site-wide tracer list, so nothing to do here for the else.
        } catch (Throwable e){
            logger.error("Unable to either find or initialize the PET tracer list.", e);
        }

        PrearcConfig prearcConfig = XDAT.getContextService().getBean(PrearcConfig.class);
        try {
            PrearcDatabase.initDatabase(prearcConfig.isReloadPrearcDatabaseOnApplicationStartup());
        } catch (Throwable e) {
            logger.error("Unable to initialize prearchive database", e);
        }

        XnatPasswordEncrypter.execute();

        addWorkflowListeners();

        XDAT.getContextService().getBean(DicomSCPManager.class).startOrStopDicomSCPAsDictatedByConfiguration();
    }

    private void addWorkflowListeners(){
    	try {
			List<Class<?>> classes = Reflection.getClassesForPackage("org.nrg.xnat.workflow.listeners");

			if(classes!=null && classes.size()>0){
				 for(Class<?> clazz: classes){
					 if(WorkflowSaveHandlerAbst.class.isAssignableFrom(clazz)){
						EventManager.AddListener(WrkWorkflowdata.SCHEMA_ELEMENT_NAME,(EventListener)clazz.newInstance());
					 }
				 }
			 }
		} catch (Exception e) {
			logger.error("",e);
		}
    }

    /**
     * Adds users from /old xdat_user table to new user authentication table if they are not already there. New local database users now get added to both automatically, but this is necessary
     * so that those who upgrade from an earlier version will still have their users be able to log in. Password expiry times are also added so that pre-existing users still have their passwords expire.
     */
    private void updateAuthTable(){
        JdbcTemplate template = new JdbcTemplate(XDAT.getDataSource());
        List<XdatUserAuth> unmapped = template.query("SELECT login, enabled FROM xdat_user WHERE login NOT IN (SELECT xdat_username FROM xhbm_xdat_user_auth)", new RowMapper<XdatUserAuth>() {
            @Override
            public XdatUserAuth mapRow(final ResultSet resultSet, final int i) throws SQLException {
                final String login = resultSet.getString("login");
                final boolean enabled = resultSet.getInt("enabled") == 1;
                return new XdatUserAuth(login, XdatUserAuthService.LOCALDB, enabled, true, true, true, AuthorityUtils.NO_AUTHORITIES, login,0);
            }
        });
        for (XdatUserAuth userAuth : unmapped) {
            XDAT.getXdatUserAuthService().create(userAuth);
        }
        template.execute("UPDATE xhbm_xdat_user_auth SET password_updated=current_timestamp WHERE auth_method='"+XdatUserAuthService.LOCALDB+"' AND password_updated IS NULL");
    }

    @Override
    public void destroy() {
        XDAT.getContextService().getBean(DicomSCPManager.class).stopDicomSCPs();
    }
}
