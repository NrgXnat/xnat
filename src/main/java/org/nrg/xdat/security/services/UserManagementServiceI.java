/**
 * 
 */
package org.nrg.xdat.security.services;

import java.util.List;
import java.util.Map;

import org.nrg.xdat.security.Authenticator.Credentials;
import org.nrg.xdat.security.user.exceptions.PasswordAuthenticationException;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Tim Olsen <tim@deck5consulting.com>
 *
 * Interface used to manage particular implementations of user management.
 */
public interface UserManagementServiceI {
	/**
     * Return a freshly created (empty) user.
	 * @return
	 */
	public UserI createUser();
	
	/**
     * Return a User object for the referenced username.
	 * @param username
	 * @return
	 * @throws UserNotFoundException
	 * @throws UserInitException
	 */
	public UserI getUser(String username) throws UserInitException;
	
	/**
     * Return a User object for the referenced user id.
	 * @return
	 * @throws UserNotFoundException
	 * @throws UserInitException
	 */
	public UserI getUser(Integer user_id) throws UserNotFoundException, UserInitException;
	
	/**
     * Return the user objects with matching email addresses
	 * @param email
	 * @return
	 */
	public List<UserI> getUsersByEmail(String email);
	
	/**
	 * @return
	 * @throws UserNotFoundException
	 * @throws UserInitException
	 */
	public UserI getGuestUser() throws UserNotFoundException, UserInitException;
	
	/**
	 * Return a complete list of all the users in the database.
	 * @return
	 */
	public List<UserI> getUsers();
	
	/**
	 * Return a string identifying the type of user implementation that is being used (xdat:user)
	 * @return
	 */
	public String getUserDataType();
	
	/**
	 * Return a freshly created user object populated with the passed parameters.
     * 
     * Object may or may not already exist in the database.
     * 
     * @param params
	 * @return
	 * @throws UserFieldMappingException
	 * @throws UserInitException
	 */
	public UserI createUser(Map<String, ? extends Object> params) throws UserFieldMappingException, UserInitException;
	
	/**
	 * clear any objects that might be cached for this user
	 * @param user
	 */
	public void clearCache(UserI user);
	
	/**
	 * Save the user object
	 * 
	 * @param user
	 * @param authenticatedUser
	 * @param overrideSecurity : whether to check if this user can modify this user object (should be false if authenticatedUser is null)
	 * @param c
	 * @throws Exception
	 */
	public void save(UserI user, UserI authenticatedUser, boolean overrideSecurity, EventMetaI c) throws Exception;
	
	/**
	 * Save the user object
	 * 
	 * @param user
	 * @param authenticatedUser
	 * @param overrideSecurity : whether to check if this user can modify this user object (should be false if authenticatedUser is null)
	 * @param c
	 * @throws Exception
	 */
	public void save(UserI user, UserI authenticatedUser, boolean overrideSecurity, EventDetails c) throws Exception;
	
	/**
	 * Validate the user object and see if it meets whatever requirements have been met by the system
	 * 
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public ValidationResultsI validate(UserI user) throws Exception;
	
	/**
	 * Enable the user account
	 * 
	 * @param user
	 * @param authenticatedUser
	 * @param ci
	 * @throws InvalidPermissionException
	 * @throws Exception
	 */
	public void enableUser(UserI user ,UserI authenticatedUser, EventDetails ci) throws InvalidPermissionException, Exception;
	
	/**
	 * Disable the user account
	 * 
	 * @param user
	 * @param authenticatedUser
	 * @param ci
	 * @throws InvalidPermissionException
	 * @throws Exception
	 */
	public void disableUser(UserI user ,UserI authenticatedUser, EventDetails ci) throws InvalidPermissionException, Exception;
	
	/**
	 * See whether the passed user can authenticate using the passed credentials
	 * 
	 * @param u
	 * @param cred
	 * @return
	 * @throws PasswordAuthenticationException
	 * @throws Exception
	 */
	public boolean authenticate(UserI u, Credentials cred)throws PasswordAuthenticationException, Exception;
	
}
