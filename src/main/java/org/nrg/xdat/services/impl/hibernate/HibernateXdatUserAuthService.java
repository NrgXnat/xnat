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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.daos.XdatUserAuthDAO;
import org.nrg.xdat.entities.XDATUserDetails;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.daos.XftFieldExclusionDAO;
import org.nrg.xft.entities.XftFieldExclusion;
import org.nrg.xft.entities.XftFieldExclusionScope;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.services.XftFieldExclusionService;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HibernateXdatUserAuthService extends AbstractHibernateEntityService<XdatUserAuth> implements XdatUserAuthService {

    private static final String[] EXCLUSION_PROPERTIES = new String[] {"xdatUsername", "id", "enabled", "created", "timestamp", "disabled" };

    protected final Log logger = LogFactory.getLog(getClass());
    
    @Override
    public XdatUserAuth newEntity() {
        return new XdatUserAuth();
    }

	@Override
	@Transactional
	public XdatUserAuth getUserByNameAndAuth(String user, String auth) {
		XdatUserAuth example = new XdatUserAuth();
	        example.setAuthUser(user);
	        example.setAuthMethod(auth);
	        List<XdatUserAuth> auths =  _dao.findByExample(example, EXCLUSION_PROPERTIES);
	        if(auths==null || auths.size()==0){
	        	return null;
	        }
	        return auths.get(0);
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
	public XDATUserDetails getUserDetailsByNameAndAuth(String user,
			String auth) {
		return getUserDetailsByNameAndAuth(user, auth, null);
	}
    
	@Override
	@Transactional
	public XDATUserDetails getUserDetailsByNameAndAuth(String username, String auth, String email) {
		List<UserDetails> users = loadUsersByUsername(username, auth);

        if (users.size() == 0 || users.get(0)==null) {
        	if(auth.equals("ldap") && !isLDAPUserDisabled(username)){
    			logger.debug("Adding LDAP user '" + username + "' to database.");

	        	try{
	        		Map<String, String> newUserPrperties = new HashMap<String, String>();
	        		newUserPrperties.put(org.nrg.xft.XFT.PREFIX + ":user.login", username);
	        		newUserPrperties.put(org.nrg.xft.XFT.PREFIX + ":user.email", email);
	        		newUserPrperties.put(org.nrg.xft.XFT.PREFIX + ":user.primary_password", null);
	        		newUserPrperties.put(org.nrg.xft.XFT.PREFIX + ":user.lastname", null);
	        		newUserPrperties.put(org.nrg.xft.XFT.PREFIX + ":user.firstname", null);
	        		newUserPrperties.put(org.nrg.xft.XFT.PREFIX + ":user.primary_password.encrypt", "true");
	        		newUserPrperties.put(org.nrg.xft.XFT.PREFIX + ":user.enabled", "1");
	        		
	        		PopulateItem populater = new PopulateItem(newUserPrperties,null,org.nrg.xft.XFT.PREFIX + ":user",true);
	            	ItemI item = populater.getItem();
	                
	                item.setProperty("xdat:user.assigned_roles.assigned_role[0].role_name","SiteUser");
	                item.setProperty("xdat:user.assigned_roles.assigned_role[1].role_name","DataManager");
	                
	                XDATUser newUser = new XDATUser(item);
	                
	                newUser.save(XDAT.getUserDetails(),true,false,true,false); 
	                XDAT.setUserDetails(new XDATUserDetails(newUser));
	                
	                if(users.size() == 0){
	                	users.add(new XDATUserDetails(newUser));
	                }
	                else{
		                users.set(0, new XDATUserDetails(newUser));
	                }
	                
	                XdatUserAuth newUserAuth = new XdatUserAuth(username, "ldap");
	                XDAT.getXdatUserAuthService().create(newUserAuth);
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

        return createUserDetails(username, user, dbAuths, auth);
	}

    

	public String getUsersByUsernameQuery() {
		return "select xhbm_xdat_user_auth.auth_user,xhbm_xdat_user_auth.auth_method,xhbm_xdat_user_auth.xdat_username,xhbm_xdat_user_auth.enabled from xhbm_xdat_user_auth JOIN xdat_user ON xhbm_xdat_user_auth.auth_user=xdat_user.login where xdat_user.enabled=1 and xhbm_xdat_user_auth.enabled=TRUE and xhbm_xdat_user_auth.auth_user = ? and xhbm_xdat_user_auth.auth_method = ?";
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
			List<Boolean> enabled = (new JdbcTemplate(_datasource)).query("SELECT enabled FROM xhbm_xdat_user_auth WHERE auth_user = ? AND auth_method = 'ldap'", new String[] {username}, new RowMapper<Boolean>() {
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
			String auth) {
    	XDATUserDetails u = null;
    	try {
	    	XdatUserAuth userAuth = XDAT.getXdatUserAuthService().getUserByNameAndAuth(username, auth);
			u = new XDATUserDetails(userAuth.getXdatUsername());
		} catch (Exception e) {
			logger.error(e);
		}
    	return u;
	}

	protected List<UserDetails> loadUsersByUsername(String username, String auth) {
		List<UserDetails> u = 
        (new JdbcTemplate(_datasource)).query(getUsersByUsernameQuery(), new String[] {username,auth}, new RowMapper<UserDetails>() {
            public UserDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
                String username = rs.getString(1);
                String method = rs.getString(2);
                String xdatUsername = rs.getString(3);
                boolean enabled = rs.getBoolean(4);
                XdatUserAuth u = new XdatUserAuth(username, method, enabled, true, true, true, AuthorityUtils.NO_AUTHORITIES, xdatUsername);
                XDATUserDetails xdat;
                xdat = null;
				try {
					xdat = new XDATUserDetails(u.getXdatUsername());
				} catch (Exception e) {
					logger.error(e);
				}
                return xdat;
            }
        });
		return u;
	}
	
	
}
