/*
 * core: org.nrg.xdat.security.services.UserManagementServiceI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.services;

import org.nrg.xdat.security.Authenticator.Credentials;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;

import java.util.List;
import java.util.Map;

/**
 * @author Tim Olsen &lt;tim@deck5consulting.com&gt;
 *
 * Interface used to manage particular implementations of user management.
 */
public interface UserManagementServiceI {
	/**
     * Return a freshly created (empty) user.
	 * @return The new user object.
	 */
	UserI createUser();
	
	/**
     * Return a User object for the referenced username.
	 * @param username    The name of the user to retrieve.
	 * @return The user if found.
	 * @throws UserNotFoundException If the user isn't found.
	 * @throws UserInitException If something goes wrong creating the user.
	 */
	UserI getUser(String username) throws UserInitException, UserNotFoundException;
	
	/**
     * Return a User object for the referenced user id.
	 * @param userId The object ID for the user to retrieve.
	 * @return The user if found.
	 * @throws UserNotFoundException If the user isn't found.
	 * @throws UserInitException If something goes wrong creating the user.
	 */
	UserI getUser(Integer userId) throws UserNotFoundException, UserInitException;
	
	/**
     * Return the user objects with matching email addresses.
	 * @param email    The email of the user(s) to retrieve.
	 * @return Any users with the indicated email address.
	 */
	List<UserI> getUsersByEmail(String email);
	
	/**
	 * Gets the guest user object.
	 * @return The guest user object.
	 * @throws UserNotFoundException If the user isn't found.
	 * @throws UserInitException If something goes wrong creating the user.
	 */
	UserI getGuestUser() throws UserNotFoundException, UserInitException;
	
	/**
	 * Return a complete list of all the users in the database.
	 * @return A list of all the users in the database.
	 */
	List<UserI> getUsers();
	
	/**
	 * Return a string identifying the type of user implementation that is being used (xdat:user)
	 * @return The user implementation or data type.
	 */
	String getUserDataType();
	
	/**
	 * Return a freshly created user object populated with the passed parameters.
     * 
     * Object may or may not already exist in the database.
     * 
     * @param properties    The properties for the user to create or retrieve.
	 * @return The existing user if properties match or a newly created user object with the indicated properties.
	 * @throws UserInitException If something goes wrong creating the user.
	 * @throws UserFieldMappingException An error occurred mapping the submitted properties to the user object.
	 */
	UserI createUser(Map<String, ?> properties) throws UserFieldMappingException, UserInitException;
	
	/**
	 * Clear any objects that might be cached for this user
	 * @param user    The user to clear.
	 */
    void clearCache(UserI user);
	
	/**
	 * Save the user object.
	 * 
	 * @param user                 The user to save.
	 * @param authenticatedUser    The user actually performing the save operation.
	 * @param overrideSecurity     Whether to check if this user can modify this user object (should be false if authenticatedUser is null)
	 * @param event                The event metadata for the save operation.
	 * @throws Exception When something goes wrong.
	 */
    void save(UserI user, UserI authenticatedUser, boolean overrideSecurity, EventMetaI event) throws Exception;
	
	/**
	 * Save the user object.
	 * 
	 * @param user                 The user to save.
	 * @param authenticatedUser    The user actually performing the save operation.
	 * @param overrideSecurity     Whether to check if this user can modify this user object (should be false if authenticatedUser is null).
	 * @param event                The event data for the save operation.
	 * @throws Exception When something goes wrong.
	 */
    void save(UserI user, UserI authenticatedUser, boolean overrideSecurity, EventDetails event) throws Exception;
	
	/**
	 * Validate the user object and see if it meets whatever requirements have been met by the system.
	 * 
	 * @param user    The user to validate.
	 * @return The results of the validation.
     * @throws Exception When something goes wrong.
	 */
    ValidationResultsI validate(UserI user) throws Exception;
	
	/**
	 * Enable the user account.
	 * 
	 * @param user                 The user account to be enabled.
	 * @param authenticatedUser    The user actually performing the enable operation.
     * @param event                The event data for the save operation.
     * @throws Exception When something goes wrong.
	 */
    void enableUser(UserI user, UserI authenticatedUser, EventDetails event) throws Exception;
	
	/**
	 * Disable the user account.
	 *
     * @param user                 The user account to be enabled.
     * @param authenticatedUser    The user actually performing the enable operation.
     * @param event                The event data for the save operation.
     * @throws Exception When something goes wrong.
	 */
    void disableUser(UserI user, UserI authenticatedUser, EventDetails event) throws Exception;
	
	/**
	 * See whether the passed user can authenticate using the passed credentials
	 * 
	 * @param user           The user to authenticate.
	 * @param credentials    The credentials with which to authenticate.
	 * @return True if the credentials are valid for the user, false otherwise.
     * @throws Exception When something goes wrong.
	 */
    boolean authenticate(UserI user, Credentials credentials) throws Exception;
}
