package org.nrg.xdat.security;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.nrg.xdat.security.group.exceptions.GroupFieldMappingException;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.security.UserI;

/**
 * @author Tim Olsen <tim@deck5consulting.com>
 *
 * User Group Service
 *
 * Added to allow different implementations of User Group activities
 */
public interface UserGroupServiceI {
	public class InvalidValueException extends Exception {

	}
	
	/**
	 * Get a UserGroupI by the group ID.
	 * @param id
	 * @return
	 */
	public UserGroupI getGroup(String id);
	
	/**
	 * Get the UserGroup that are currently assigned to a user.
	 * @param user
	 * @return
	 */
	public Map<String, UserGroupI> getGroupsForUser(UserI user);
	
	/**
	 * Get the group IDs currently assigned to a user.
	 * @param user
	 * @return
	 */
	public List<String> getGroupIdsForUser(UserI user);
	
	/**
	 * Is this user currently assigned to the group with this ID?
	 * 
	 * @param user
	 * @param groupId
	 * @return
	 */
	public boolean isMember(UserI user, String groupId);

	/**
	 * return all UserGroups on this server.  (this may be an expensive operation on larger servers)  We might want to get rid of this.
	 * @return
	 */
	public List<UserGroupI> getAllGroups();
	
	/**
	 * Get groups that have the specified tag.
	 * @param tag
	 * @return
	 * @throws Exception
	 */
	public List<UserGroupI> getGroupsByTag(String tag) throws Exception;
	
	/**
	 * Delete the groups (and user-group mappings) associated with this tag.
	 * @param tag
	 * @param user
	 * @param ci
	 * @throws Exception
	 */
	public void deleteGroupsByTag(String tag, UserI user, EventMetaI ci) throws Exception;
	
	/**
	 * add this group for the specified user (locally).   This will not update the database.  It will add the user to this group in local memory.
	 * 
	 * Sometimes, a user group is associated with a user, before the user group is physically created in the database.  This method can be used to do that.
	 * 
	 * To specifically add the user to the group permanently, use the AddUserToGroup method
	 * @param user
	 * @param groupId
	 * @param group
	 */
	public void updateUserForGroup(UserI user, String groupId, UserGroupI group);
	
	/**
	 * Remove user from the group (including updating database if necessary)
	 * 
	 * @param user
	 * @param groupId
	 * @param ci
	 * @throws SQLException
	 * @throws Exception
	 */
	public void removeUserFromGroup(UserI user, String groupId, EventMetaI ci) throws SQLException, Exception ;
	
	/**
	 * Refresh the user group for this user (this updates any local copies of the group for this user).  This should be eliminated by a more clear caching mechanism.
	 * 
	 * @param user
	 * @param groupId
	 */
	public void reloadGroupForUser(UserI user, String groupId);
	
	/**
	 * Refresh all of the user groups for this user.
	 * 
	 * @param user
	 */
	public void reloadGroupsForUser(UserI user);
	
	/**
	 * Create user group using the defined permissions.
	 * 
	 * @param id : String ID to use for the group ID
	 * @param displayName
	 * @param create : true if members should be able to create the data types in the List<ElementSecurity> ess, else false
	 * @param read : true if members should be able to read the data types in the List<ElementSecurity> ess, else false
	 * @param delete : true if members should be able to delete the data types in the List<ElementSecurity> ess, else false
	 * @param edit : true if members should be able to edit the data types in the List<ElementSecurity> ess, else false
	 * @param activate : true if members should be able to activate the data types in the List<ElementSecurity> ess, else false
	 * @param activateChanges : Should the permissions be activated upon creation (or wait for approval later)
	 * @param ess : List of data types that this group should have permissions for
	 * @param tag : Tag for permissions to key of off (typically the project ID)
	 * @param authenticatedUser
	 * @return
	 */
	public UserGroupI createGroup(final String id, final String displayName, Boolean create,Boolean read,Boolean delete,Boolean edit,Boolean activate,boolean activateChanges,List<ElementSecurity> ess, String tag, UserI authenticatedUser);
	
	/**
	 * create or update a group using the defined permissions.  If the group doesn't exist, a new one will be created.  If it does, the permissions will be updated according to the passed parameters
	 * 
	 * @param id : String ID to use for the group ID
	 * @param displayName
	 * @param create : true if members should be able to create the data types in the List<ElementSecurity> ess, else false
	 * @param read : true if members should be able to read the data types in the List<ElementSecurity> ess, else false
	 * @param delete : true if members should be able to delete the data types in the List<ElementSecurity> ess, else false
	 * @param edit : true if members should be able to edit the data types in the List<ElementSecurity> ess, else false
	 * @param activate : true if members should be able to activate the data types in the List<ElementSecurity> ess, else false
	 * @param activateChanges : Should the permissions be activated upon creation (or wait for approval later)
	 * @param ess : List of data types that this group should have permissions for
	 * @param tag : Tag for permissions to key of off (typically the project ID)
	 * @param authenticatedUser
	 * @return
	 * @throws Exception
	 */
	public UserGroupI createOrUpdateGroup(final String id, final String displayName, Boolean create,Boolean read,Boolean delete,Boolean edit,Boolean activate,boolean activateChanges,List<ElementSecurity> ess, String tag, UserI authenticatedUser) throws Exception;
	
	/**
	 * Add user to the group (includes potential modification to the database).
	 * 
	 * @param group_id
	 * @param newUser
	 * @param authenticatedUser
	 * @param ci
	 * @return
	 * @throws Exception
	 */
	public UserGroupI addUserToGroup(String group_id, UserI newUser,UserI authenticatedUser, EventMetaI ci) throws Exception;
	
	/**
	 * The data type or classification identifier used to identify the group data type.  
	 * 
	 * This will be used in workflow events that are created to track modifications to user groups.  
	 * @return
	 */
	public String getGroupDatatype();

	
    /**
     * Return a freshly created group object populated with the passed parameters.
     * 
     * Object may or may not already exist in the database.
     * 
     * @return
     * @throws GroupFieldMappingException 
     */
	public UserGroupI createGroup(Map<String, ? extends Object> params) throws GroupFieldMappingException;

	/**
	 * @param g
	 * @param user
	 * @param eventDetails
	 */
	public void deleteGroup(UserGroupI g, UserI user, EventMetaI ci);

	/**
	 * @param gID
	 * @return
	 */
	public UserGroupI getGroupByPK(Object gID);

	/**
	 * @param pID
	 * @param gID
	 * @return
	 */
	public UserGroupI getGroupByTagAndName(String pID, String gID);

	/**
	 * @param tempGroup
	 * @param user
	 * @param meta
	 * @throws Exception 
	 * @throws InvalidValueException 
	 */
	public void save(UserGroupI tempGroup, UserI user, EventMetaI meta) throws InvalidValueException, Exception;

}