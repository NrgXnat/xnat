package org.nrg.xdat.security;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.security.Authenticator.Credentials;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.PasswordAuthenticationException;
import org.nrg.xdat.security.user.exceptions.PasswordComplexityException;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
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
import org.nrg.xft.utils.StringUtils;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import com.google.common.collect.Lists;

public class XDATUserMgmtServiceImpl  implements UserManagementServiceI{
    static Logger logger = Logger.getLogger(XDATUserMgmtServiceImpl.class);

	@Override
	public UserI createUser() {
		return new XDATUser();
	}

	@Override
	public UserI getUser(String username) throws UserNotFoundException, UserInitException {
		return new XDATUser(username);
	}

	@Override
	public UserI getUser(Integer user_id) throws UserNotFoundException, UserInitException {
		XdatUser u=XdatUser.getXdatUsersByXdatUserId(user_id, null, false);
		if(u==null){
			return new XDATUser(u.getItem());
		}else{
			throw new UserNotFoundException(user_id.toString());
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
	public UserI getGuestUser() throws UserNotFoundException, UserInitException {
		String guestName;
		try {
			guestName = XDAT.getSiteConfigurationProperty("security.user.guestName", "guest");
		} catch (ConfigServiceException e) {
			logger.error("",e);
			guestName="guest";
		}
		return getUser(guestName);
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
	public UserI createUser(Map<String, ? extends Object> params) throws UserFieldMappingException, UserInitException{
        try {
			PopulateItem populater = new PopulateItem(params,null,org.nrg.xft.XFT.PREFIX + ":user",true);
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
	public void save(UserI user, UserI authenticatedUser, boolean overrideSecurity, EventDetails ci) throws InvalidPermissionException, Exception{
		//this calls the other save method, but also takes care of creating the workflow entry for this change.
		if(user.getLogin()==null){
			throw new NullPointerException();
		}
		
		Object id;
		UserI existing;
    	try {
			if(user.getID()==null){
				id=user.getID();
				existing=Users.getUser(user.getID());
			}else{
				id=user.getLogin();
				existing=Users.getUser(user.getLogin());
			}			
		} catch (Exception e1) {
			id=user.getLogin();
	    }
		
    	PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, authenticatedUser, Users.getUserDataType(),id.toString(),PersistentWorkflowUtils.getExternalId(user), ci);
         
    	try {
	    	save(authenticatedUser,user,overrideSecurity,wrk.buildEvent());
	    	 
	    	if(id.equals(user.getLogin())) {
	    		//this was a new user or didn't include the user's id.
	    		//before we save the workflow entry, we should replace the login with the ID.
                Integer userId = Users.getUserid(user.getLogin());
                if (userId!=null) {
                	wrk.setId(user.getID().toString());
                }
	    	}else{
                throw new Exception("Couldn't find a user for the indicated login: " + user.getLogin());
            }
	    	
			PersistentWorkflowUtils.complete(wrk,wrk.buildEvent());
		} catch (Exception e) {
			PersistentWorkflowUtils.fail(wrk,wrk.buildEvent());
			throw e;
		}
	}

	@Override
	public void save(UserI user, UserI authenticatedUser,boolean overrideSecurity, EventMetaI c)  throws Exception{
		if(user.getLogin()==null){
			throw new NullPointerException();
		}
		
		UserI existing=Users.getUser(user.getLogin());
		if (existing == null) {
			 // NEW USER
		    if (overrideSecurity || authenticatedUser.checkRole("Administrator")) {
		        String tempPass = user.getPassword();
		        if (!StringUtils.IsEmpty(tempPass)){
		        	PasswordValidatorChain validator = XDAT.getContextService().getBean(PasswordValidatorChain.class);
		        	if(validator.isValid(tempPass, null)){
		        		//this is set to null instead of authenticatedUser because new users should be able to use any password even those that have recently been used by other users.
                        String salt = Users.createNewSalt();
		        		user.setPassword(new ShaPasswordEncoder(256).encodePassword(tempPass, salt));
                        user.setSalt(salt);
		        	} else {
		        		throw new PasswordComplexityException(validator.getMessage());
		        	}
		        }
		        // newUser.initializePermissions();
		        SaveItemHelper.authorizedSave(((XDATUser)user), authenticatedUser, true, false,c);
		        XdatUserAuth newUserAuth = new XdatUserAuth(user.getLogin(), XdatUserAuthService.LOCALDB);
		        XDAT.getXdatUserAuthService().create(newUserAuth);
		    } else {
		        throw new InvalidPermissionException("Unauthorized user modification attempt");
		    }
		} else {
		    // OLD USER
		    String tempPass = user.getPassword();
		    String savedPass = existing.getPassword();
		    if (StringUtils.IsEmpty(tempPass) && StringUtils.IsEmpty(savedPass)) {

		    } else if (StringUtils.IsEmpty(tempPass)) {

		    } else {
		        if (!tempPass.equals(savedPass)){
		        	PasswordValidatorChain validator = XDAT.getContextService().getBean(PasswordValidatorChain.class);
		        	if(validator.isValid(tempPass, authenticatedUser)){
                        String salt = Users.createNewSalt();
		                user.setPassword(new ShaPasswordEncoder(256).encodePassword(tempPass, salt));
		                user.setSalt(salt);
		        	} else {
		        		throw new PasswordComplexityException(validator.getMessage());
		        	}
		        }
		    }

		    if (authenticatedUser.checkRole("Administrator") || overrideSecurity) {
		        SaveItemHelper.authorizedSave(((XDATUser)user), authenticatedUser, false, false,c);
		    } else if (user.getLogin().equals(authenticatedUser.getLogin())) {
		    	//not-admin user is modifying his own account.
		    	//we only allow him to modify specific fields.
		    	UserI toSave=Users.createUser();
		        toSave.setLogin(authenticatedUser.getLogin());
		        toSave.setPassword(user.getPassword());
                toSave.setSalt(user.getSalt());
		        toSave.setEmail(user.getEmail());
		        SaveItemHelper.authorizedSave(((XDATUser)toSave), authenticatedUser, false, false,c);

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
	public void enableUser(UserI user, UserI authenticatedUser, EventDetails ci)
			throws InvalidPermissionException, Exception {
		user.setEnabled(true);
		Users.save(user, authenticatedUser,false, ci);
	}

	@Override
	public void disableUser(UserI user, UserI authenticatedUser, EventDetails ci)
			throws InvalidPermissionException, Exception {
		user.setEnabled(false);
		Users.save(user, authenticatedUser,false, ci);
	}

	@Override
	public boolean authenticate(UserI u, Credentials cred) throws PasswordAuthenticationException, Exception {
        return ((XDATUser)u).login(cred.password);
	}
}