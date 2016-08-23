package org.nrg.xdat.security;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.security.Authenticator.Credentials;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.*;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class XDATUserMgmtServiceImpl implements UserManagementServiceI{
    private static final Logger logger = Logger.getLogger(XDATUserMgmtServiceImpl.class);

	@Override
	public UserI createUser() {
		return new XDATUser();
	}

	@Override
	public UserI getUser(String username) throws UserNotFoundException, UserInitException {
		return new XDATUser(username);
	}

	@Override
	public UserI getUser(Integer userId) throws UserNotFoundException, UserInitException {
		XdatUser u=XdatUser.getXdatUsersByXdatUserId(userId, null, false);
		if(u!=null){
			return new XDATUser(u.getItem());
		}else{
			throw new UserNotFoundException(userId);
		}
	}
    
	public List<UserI> getUsersByEmail(String email){
		List<UserI> _return=Lists.newArrayList();
		List<XdatUser> al = XdatUser.getXdatUsersByField("xdat:user.email", email, null, true);
		for(XdatUser u:al){
			try {
				_return.add(new XDATUser(u.getItem()));
			} catch (Exception e) {
				logger.error("",e);
			}
		}
		return _return;
	}

	@Override
	@Nonnull
	public UserI getGuestUser() throws UserNotFoundException, UserInitException {
		try {
			return getUser(XDAT.getSiteConfigurationProperty("security.user.guestName", "guest"));
		} catch (ConfigServiceException e) {
			logger.error("",e);
			return getUser("guest");
		}
	}

	@Override
	public List<UserI> getUsers() {
		List<UserI> allUsers = Lists.newArrayList();
		for(XdatUser u:XdatUser.getAllXdatUsers(null,false)){
            try {
				allUsers.add(new XDATUser((u).getItem()));
			} catch (UserInitException e) {
				logger.error("",e);
			}
		}
        return allUsers;
	}

	@Override
	public String getUserDataType() {
		return XdatUser.SCHEMA_ELEMENT_NAME;
	}

	@Override
	public UserI createUser(Map<String, ?> properties) throws UserFieldMappingException, UserInitException{
        try {
			PopulateItem populater = new PopulateItem(properties, null, org.nrg.xft.XFT.PREFIX + ":user", true);
			ItemI found = populater.getItem();
			return new XDATUser(found);
		} catch (Exception e) {
			throw new UserFieldMappingException(e);
		}
	}

	@Override
	public void clearCache(UserI user) {
		if(user instanceof XDATUser){
			((XDATUser)user).clearLocalCache();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.services.UserManagementServiceI#save(org.nrg.xft.security.UserI, org.nrg.xft.security.UserI, boolean, org.nrg.xft.event.EventDetails)
	 */
	public void save(UserI user, UserI authenticatedUser, boolean overrideSecurity, EventDetails event) throws Exception {
		//this calls the other save method, but also takes care of creating the workflow entry for this change.
		if(user.getLogin()==null){
			throw new NullPointerException();
		}

		final Object id = user.getID() != null ? user.getID() : user.getLogin();

		PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, authenticatedUser, Users.getUserDataType(), id.toString(), PersistentWorkflowUtils.getExternalId(user), event);

		try {
	    	save(user,authenticatedUser,overrideSecurity,wrk.buildEvent());
	    	 
	    	if(id.equals(user.getLogin())) {
	    		//this was a new user or didn't include the user's id.
	    		//before we save the workflow entry, we should replace the login with the ID.
                Integer userId = Users.getUserid(user.getLogin());
                if (userId!=null) {
                	wrk.setId(user.getID().toString());
                }
                else{
                    throw new Exception("Couldn't find a user for the indicated login: " + user.getLogin());
                }
	    	}
	    	
			PersistentWorkflowUtils.complete(wrk,wrk.buildEvent());
		} catch (Exception e) {
			PersistentWorkflowUtils.fail(wrk,wrk.buildEvent());
			throw e;
		}
	}

	@Override
	public void save(UserI user, UserI authenticatedUser,boolean overrideSecurity, EventMetaI event) throws Exception{
		if(user.getLogin()==null){
			throw new NullPointerException();
		}
		
		UserI existing=null;
		try {
			existing = Users.getUser(user.getLogin());
		} catch (Exception ignored) {
		}
		
		if (existing == null) {
			 // NEW USER
		    if (overrideSecurity || Roles.isSiteAdmin(authenticatedUser)) {
		        String tempPass = user.getPassword();
		        if (StringUtils.isNotBlank(tempPass)){
		        	PasswordValidatorChain validator = XDAT.getContextService().getBean(PasswordValidatorChain.class);
		        	if(validator.isValid(tempPass, null)){
		        		//this is set to null instead of authenticatedUser because new users should be able to use any password even those that have recently been used by other users.
                        String salt = user.getSalt();
                        if(salt==null){
                            salt = Users.createNewSalt();
                            user.setSalt(salt);
                        }
                        if(user.getPassword()==null || user.getPassword().length()!=64) {
                            user.setPassword(new ShaPasswordEncoder(256).encodePassword(tempPass, salt));
                        }

		        	} else {
		        		throw new PasswordComplexityException(validator.getMessage());
		        	}
		        }
		        // newUser.initializePermissions();
		        SaveItemHelper.authorizedSave(((XDATUser)user), authenticatedUser, true, false, event);
		        XdatUserAuth newUserAuth = new XdatUserAuth(user.getLogin(), XdatUserAuthService.LOCALDB);
		        XDAT.getXdatUserAuthService().create(newUserAuth);
		    } else {
		        throw new InvalidPermissionException("Unauthorized user modification attempt");
		    }
		} else {
		    // OLD USER
			String tempPass = user.getPassword();
			String savedPass = existing.getPassword();
			String savedSalt = existing.getSalt();
			// check if the password is being updated (also do this if password remains the same but salt is empty)
			if ( (StringUtils.isNotBlank(tempPass) && tempPass.length()!=64) && (user.getSalt()==null || (!StringUtils.equals(tempPass, savedPass) && !StringUtils.equals(new ShaPasswordEncoder(256).encodePassword(tempPass, user.getSalt()), savedPass)))) {
				String encrypted=(new ShaPasswordEncoder(256).encodePassword(tempPass, existing.getSalt()));
				PasswordValidatorChain validator = XDAT.getContextService().getBean(PasswordValidatorChain.class);
				if(validator.isValid(tempPass, user)){
					String salt = Users.createNewSalt();
					user.setPassword(new ShaPasswordEncoder(256).encodePassword(tempPass, salt));
					user.setSalt(salt);

					XdatUserAuth auth = XDAT.getXdatUserAuthService().getUserByNameAndAuth(user.getLogin(), XdatUserAuthService.LOCALDB, "");
					auth.setPasswordUpdated(new java.util.Date());
					auth.setFailedLoginAttempts(0);
					XDAT.getXdatUserAuthService().update(auth);
				} else {
					throw new PasswordComplexityException(validator.getMessage());
				}
	        }
            else {
            	user.setPassword(savedPass);
				if(StringUtils.isNotBlank(savedSalt)){
					user.setSalt(savedSalt);
				}
            }

		    if (Roles.isSiteAdmin(authenticatedUser) || overrideSecurity) {
		        SaveItemHelper.authorizedSave(((XDATUser)user), authenticatedUser, false, false, event);
		    } else if (user.getLogin().equals(authenticatedUser.getLogin())) {
		    	//not-admin user is modifying his own account.
		    	//we only allow him to modify specific fields.
		    	XDATUser toSave=(XDATUser)createUser();
		        toSave.setLogin(authenticatedUser.getLogin());
		        toSave.setPassword(user.getPassword());
                toSave.setSalt(user.getSalt());
		        toSave.setEmail(user.getEmail());
		        
		        if(!user.isEnabled()){
		        	//allowed to disable his own account (but not enable)
		        	toSave.setEnabled(false);
		        }
		        
		        if(!user.isVerified()){
		        	//allowed to un-verify his own account (but not verify)
		        	toSave.setVerified(false);
		        }
		        
		        SaveItemHelper.authorizedSave(toSave, authenticatedUser, false, false, event);

		        authenticatedUser.setPassword(user.getPassword());
		        authenticatedUser.setSalt(user.getSalt());
		        authenticatedUser.setEmail(user.getEmail());
		        
		        if(user.isVerified()!=null){
		        	authenticatedUser.setVerified(user.isVerified());
		        }
		        
		        if(user.isEnabled()){
		        	authenticatedUser.setEnabled(Boolean.TRUE);
		        }
		    } else {
		        throw new InvalidPermissionException("Unauthorized user modification attempt");
		    }
		}
	}

	

	@Override
	public ValidationResultsI validate(UserI user) throws Exception{
		return ((XDATUser)user).validate();
	}


	@Override
	public void enableUser(UserI user, UserI authenticatedUser, EventDetails event) throws Exception {
		user.setEnabled(true);
		Users.save(user, authenticatedUser, false, event);
	}

	@Override
	public void disableUser(UserI user, UserI authenticatedUser, EventDetails event) throws Exception {
		user.setEnabled(false);
		Users.save(user, authenticatedUser, false, event);
	}

	@Override
	public boolean authenticate(UserI user, Credentials credentials) throws Exception {
        return ((XDATUser) user).login(credentials.password);
	}
}