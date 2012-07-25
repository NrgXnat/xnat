package org.nrg.xdat.services.impl.hibernate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.daos.XdatUserAuthDAO;
import org.nrg.xdat.entities.XDATUserDetails;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.utils.AuthUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HibernateXdatUserAuthService extends AbstractHibernateEntityService<XdatUserAuth> implements XdatUserAuthService {

    private static final String[] EXCLUSION_PROPERTIES = new String[] {"xdatUsername", "id", "enabled", "created", "timestamp", "disabled","failedLoginAttempts" };
    private static final String[] EXCLUSION_PROPERTIES_USERNAME = new String[] {"xdatUsername", "id", "enabled", "created", "timestamp", "disabled","authMethodId","failedLoginAttempts"};

    protected final Log logger = LogFactory.getLog(getClass());
    
    @Override
    public XdatUserAuth newEntity() {
        return new XdatUserAuth();
    }

	@Transactional
	public XdatUserAuth getUserByNameAndAuth(String user, String auth, String id) {
		XdatUserAuth example = new XdatUserAuth();
	        example.setAuthUser(user);
	        example.setAuthMethod(auth);
	        if(!id.equals("")){
	        	example.setAuthMethodId(id);
	        }
	        List<XdatUserAuth> auths =  _dao.findByExample(example, EXCLUSION_PROPERTIES);
	        if(auths==null || auths.size()==0){
	        	return null;
	        }
	        return auths.get(0);
	}

	@Transactional
	public List<XdatUserAuth> getUsersByName(String user) {
		XdatUserAuth example = new XdatUserAuth();
	        example.setAuthUser(user);
	        return  _dao.findByExample(example, EXCLUSION_PROPERTIES_USERNAME);
	}
	
    @Override
    protected XdatUserAuthDAO getDao() {
        return _dao;
    }
    
    @Inject
    private XdatUserAuthDAO _dao;

    @Inject
    private DataSource _datasource;
    
    @Override
	@Transactional
	public XDATUserDetails getUserDetailsByNameAndAuth(String user, String auth) {
		return getUserDetailsByNameAndAuth(user, auth, "", null);
	}
    
	@Override
	@Transactional
	public XDATUserDetails getUserDetailsByNameAndAuth(String user, String auth, String id) {
		return getUserDetailsByNameAndAuth(user, auth, id, null);
	}
    
	@Override
	@Transactional
	public XDATUserDetails getUserDetailsByNameAndAuth(String username, String auth, String id, String email) {
		List<UserDetails> users = loadUsersByUsernameAndAuth(username, auth, id);
		
		XDATUserDetails userDetails = null;

        if (users.size() == 0 || users.get(0)==null) {
        	if(auth.equals(XdatUserAuthService.LDAP) && !isLDAPUserDisabled(username, id) &&!isLDAPUserLocked(username,id)){
	        	try{
	        		String ldapUsername = username;
	        		username = findUnusedLocalUsernameForNewLDAPUser(ldapUsername);
	    			logger.debug("Adding LDAP user '" + username + "' to database.");

	        		Map<String, String> newUserPrperties = new HashMap<String, String>();
	        		newUserPrperties.put(org.nrg.xft.XFT.PREFIX + ":user.login", username);
	        		newUserPrperties.put(org.nrg.xft.XFT.PREFIX + ":user.email", email);
	        		newUserPrperties.put(org.nrg.xft.XFT.PREFIX + ":user.primary_password", null);
	        		newUserPrperties.put(org.nrg.xft.XFT.PREFIX + ":user.lastname", null);
	        		newUserPrperties.put(org.nrg.xft.XFT.PREFIX + ":user.firstname", null);
	        		newUserPrperties.put(org.nrg.xft.XFT.PREFIX + ":user.primary_password.encrypt", "true");
	        		newUserPrperties.put(org.nrg.xft.XFT.PREFIX + ":user.enabled", newUserAccountsAreAutoEnabled().toString());
	        		
	        		PopulateItem populater = new PopulateItem(newUserPrperties,null,org.nrg.xft.XFT.PREFIX + ":user",true);
	            	ItemI item = populater.getItem();
	                
	                item.setProperty("xdat:user.assigned_roles.assigned_role[0].role_name","SiteUser");
	                item.setProperty("xdat:user.assigned_roles.assigned_role[1].role_name","DataManager");
	                
	                XDATUser newUser = new XDATUser(item);
	                
	                SaveItemHelper.authorizedSave(newUser,XDAT.getUserDetails(),true,false,true,false,EventUtils.ADMIN_EVENT(newUser));
	                XdatUserAuth newUserAuth = new XdatUserAuth(ldapUsername, XdatUserAuthService.LDAP, id, username, true, 0);
	                XDAT.getXdatUserAuthService().create(newUserAuth);

	                // <HACK_ALERT>
	                /*
	                 * We must save enabled flag to DB as true above, because the administrator code for enabling a user account does not flip this flag
	                 * (no time to mess with that now).
	                 * But for purposes of determining whether or not the user can log in right now after we've just created their account,
	                 * we use the system-wide auto-enable config setting.
	                 * Must clone a new object to return, rather than modifying the existing, so that Hibernate still saves the desired values to the DB.
	                 */
	                newUserAuth = new XdatUserAuth(newUserAuth);
	                newUserAuth.setEnabled(newUserAccountsAreAutoEnabled());
	                // </HACK_ALERT>
	                
	                userDetails = new XDATUserDetails(newUser);
	                userDetails.setAuthorization(newUserAuth);

	                XDAT.setUserDetails(userDetails);
	                
	                if(users.size() == 0){
	                	users.add(userDetails);
	                }
	                else{
		                users.set(0, userDetails);
	                }
	        	}
	        	catch(Exception e){
	        		logger.error(e);
	        	}
        	}
        	else{
	            logger.debug("Query returned no results for user '" + username + "'");
	
	            throw new UsernameNotFoundException(
	            		SpringSecurityMessageSource.getAccessor().getMessage("JdbcDaoImpl.notFound", new Object[]{username}, "Username {0} not found"), username);
        	}
        }

        UserDetails user = users.get(0); // contains no GrantedAuthority[]

        Set<GrantedAuthority> dbAuthsSet = new HashSet<GrantedAuthority>();

        dbAuthsSet.addAll(loadUserAuthorities(user.getUsername()));

        List<GrantedAuthority> dbAuths = new ArrayList<GrantedAuthority>(dbAuthsSet);

        if (dbAuths.size() == 0) {
            logger.debug("User '" + username + "' has no authorities and will be treated as 'not found'");

            throw new UsernameNotFoundException(
            		SpringSecurityMessageSource.getAccessor().getMessage("JdbcDaoImpl.noAuthority",
                            new Object[] {username}, "User {0} has no GrantedAuthority"), username);
        }

        if( userDetails == null )
        {
        	// If we just created a new user account above, the user_auth DB record won't yet be committed at this point.  
        	// So we'll just return the object that was already created.
        	// For subsequent logins, this code here will pull the auth record and set it.
        	userDetails = createUserDetails(username, user, dbAuths, auth, id);
        }
        
        return userDetails; 
	}

	public String getUsersByUsernameQuery() {
		if(AuthUtils.MAX_FAILED_LOGIN_ATTEMPTS>-1){
			return "select xhbm_xdat_user_auth.auth_user,xhbm_xdat_user_auth.auth_method,xhbm_xdat_user_auth.xdat_username,xhbm_xdat_user_auth.enabled,xhbm_xdat_user_auth.failed_login_attempts,xhbm_xdat_user_auth.auth_method_id from xhbm_xdat_user_auth JOIN xdat_user ON xhbm_xdat_user_auth.xdat_username=xdat_user.login where xdat_user.enabled=1 and xhbm_xdat_user_auth.enabled=TRUE and xhbm_xdat_user_auth.failed_login_attempts<"+ AuthUtils.MAX_FAILED_LOGIN_ATTEMPTS+"  and xhbm_xdat_user_auth.auth_user = ? and xhbm_xdat_user_auth.auth_method = ? and COALESCE(xhbm_xdat_user_auth.auth_method_id, '') = ?";
		}else{
			return "select xhbm_xdat_user_auth.auth_user,xhbm_xdat_user_auth.auth_method,xhbm_xdat_user_auth.xdat_username,xhbm_xdat_user_auth.enabled,xhbm_xdat_user_auth.failed_login_attempts,xhbm_xdat_user_auth.auth_method_id from xhbm_xdat_user_auth JOIN xdat_user ON xhbm_xdat_user_auth.xdat_username=xdat_user.login where xdat_user.enabled=1 and xhbm_xdat_user_auth.enabled=TRUE and xhbm_xdat_user_auth.auth_user = ? and xhbm_xdat_user_auth.auth_method = ? and COALESCE(xhbm_xdat_user_auth.auth_method_id, '') = ?";
		}
		
    }
	

	public List<GrantedAuthority> loadUserAuthorities(String username) {
        return (new JdbcTemplate(_datasource)).query("SELECT login as username, 'ROLE_USER' as authority FROM xdat_user WHERE login = ?", new String[] {username}, new RowMapper<GrantedAuthority>() {
            public GrantedAuthority mapRow(ResultSet rs, int rowNum) throws SQLException {
                String roleName = rs.getString(2);
                GrantedAuthorityImpl authority = new GrantedAuthorityImpl(roleName);

                return authority;
            }
        });
    }

	public boolean isLDAPUserDisabled(String username) {
		boolean isDisabled = false;
		try{
			List<Boolean> enabled = (new JdbcTemplate(_datasource)).query("SELECT enabled FROM xhbm_xdat_user_auth WHERE auth_user = ? AND auth_method = '" + XdatUserAuthService.LDAP+"'", new String[] {username}, new RowMapper<Boolean>() {
            public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                boolean enabled = rs.getBoolean(1);
                return enabled;
            }
        });
			if(enabled.get(0).equals(false)){
				isDisabled=true;
			}
		}
		catch(Exception e){
			
		}
		return isDisabled;
    }
	
	private boolean isLDAPUserLocked(String username, String id) {
		boolean isLocked = false;
		try{
			List<Integer> count = (new JdbcTemplate(_datasource)).query("SELECT failed_login_attempts FROM xhbm_xdat_user_auth WHERE auth_user = ? AND auth_method = '" + XdatUserAuthService.LDAP+"' AND auth_method_id = ?", new String[] {username, id}, new RowMapper<Integer>() {
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            	Integer count = rs.getInt(1);
                return count;
            }
        });
			if(count.get(0)>=AuthUtils.MAX_FAILED_LOGIN_ATTEMPTS){
				isLocked=true;
			}
		}
		catch(Exception e){
			
		}
		return isLocked;
	}
	
	public boolean isLDAPUserDisabled(String username, String id) {
		boolean isDisabled = false;
		try{
			List<Boolean> enabled = (new JdbcTemplate(_datasource)).query("SELECT enabled FROM xhbm_xdat_user_auth WHERE auth_user = ? AND auth_method = '" + XdatUserAuthService.LDAP+"' AND auth_method_id = ?", new String[] {username, id}, new RowMapper<Boolean>() {
            public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                boolean enabled = rs.getBoolean(1);
                return enabled;
            }
        });
			if(enabled.get(0).equals(false)){
				isDisabled=true;
			}
		}
		catch(Exception e){
			
		}
		return isDisabled;
    }

	protected XDATUserDetails createUserDetails(String username,
			UserDetails userFromUserQuery,
			List<GrantedAuthority> combinedAuthorities,
			String auth, String id) {
    	XDATUserDetails u = null;
    	try {
	    	XdatUserAuth userAuth = getUserByNameAndAuth(username, auth, id);
			u = new XDATUserDetails(userAuth.getXdatUsername());
			u.setAuthorization(userAuth);
		} catch (Exception e) {
			logger.error(e);
		}
    	return u;
	}

	protected List<UserDetails> loadUsersByUsernameAndAuth(String username, String auth, String id) {
		id = (id == null) ? "" : id;
		List<UserDetails> u = 
        (new JdbcTemplate(_datasource)).query(getUsersByUsernameQuery(), new String[] {username,auth,id}, new RowMapper<UserDetails>() {
            public UserDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
                String username = rs.getString(1);
                String method = rs.getString(2);
                String xdatUsername = rs.getString(3);
                boolean enabled = rs.getBoolean(4);
                Integer failedLoginAttempts = rs.getInt(5);
                String methodId = rs.getString(6);
                XdatUserAuth u = new XdatUserAuth(username, method, methodId, enabled, true, true, true, AuthorityUtils.NO_AUTHORITIES, xdatUsername,failedLoginAttempts);
                XDATUserDetails xdat = null;
				try {
					xdat = new XDATUserDetails(u.getXdatUsername());
					xdat.setAuthorization(u);
				} catch (Exception e) {
					logger.error(e);
				}
                return xdat;
            }
        });
		return u;
	}
	
	private String findUnusedLocalUsernameForNewLDAPUser( String ldapUsername )
	{
		// we will punt on this for now and just create a new user account if their is already a local account
		// the Cadillac solution would be to link the two (assuming the user proves that they own the local account also)
		
		String usernameToTest = ldapUsername;
		int testCount = -1;
		List<String> existingLocalUsernames;
		
		do
		{
			if ( ++testCount > 0 )
			{
				usernameToTest = ldapUsername + "_" + String.format( "%02d", testCount );
			}
			else if ( testCount > 99 )
			{
				throw new RuntimeException( "Ran out of possible XNAT user ids to check (last one checked was " + usernameToTest + ")");
			}
			
			existingLocalUsernames = (new JdbcTemplate(_datasource)).query("SELECT login FROM xdat_user WHERE login = ?", new String[] {usernameToTest}, new RowMapper<String>() 
			{
	            public String mapRow(ResultSet rs, int rowNum) throws SQLException 
	            {
	                return rs.getString(1);
	            }
		    });
			
		} while ( existingLocalUsernames.size() > 0 );
		
		return usernameToTest;
	}
	
    public Boolean newUserAccountsAreAutoEnabled()
    {
    	return XFT.GetUserRegistration();
    }
}
