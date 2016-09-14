package org.nrg.xdat.security;

import org.nrg.xdat.security.group.exceptions.GroupFieldMappingException;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;

import java.util.List;
import java.util.Map;

/**
 * @author Tim Olsen &lt;tim@deck5consulting.com&gt;
 *
 * User Group Service
 *
 * Added to allow different implementations of User Group activities
 */
public interface UserGroupServiceI {
	class InvalidValueException extends Exception {

	}
	
	/**
	 * Get a UserGroupI by the group ID.
	 * @param id    The ID of the group to retrieve.
	 * @return The group with the indicated ID.
	 */
	UserGroupI getGroup(String id);
	
	/**
	 * Get the groups that are currently assigned to a user.
	 * @param user    The user to retrieve groups for.
	 * @return The groups associated with the user.
	 */
	Map<String, UserGroupI> getGroupsForUser(UserI user);
	
	/**
	 * Get the group IDs currently assigned to a user.
	 * @param user    The user to retrieve groups for.
	 * @return The group IDs associated with the user.
	 */
	List<String> getGroupIdsForUser(UserI user);
	
	/**
	 * Is this user currently assigned to the group with this ID?
	 * 
	 * @param user       The user to test.
	 * @param groupId    The group ID to test for.
	 * @return Returns true if the user is a member of the group with the indicated ID, false otherwise.
	 */
	boolean isMember(UserI user, String groupId);

	/**
	 * return all UserGroups on this server.  (this may be an expensive operation on larger servers)  We might want to get rid of this.
	 * @return A list of all of the groups on the server.
	 */
	List<UserGroupI> getAllGroups();
	
	/**
	 * Get groups that have the specified tag.
	 * @param tag    The tag to search for.
	 * @return All groups with the indicated tag.
	 * @throws Exception When something goes wrong.
	 */
	List<UserGroupI> getGroupsByTag(String tag) throws Exception;
	
	/**
	 * Delete the groups (and user-group mappings) associated with this tag.
	 * @param tag     The tag to search for.
	 * @param user    The user running the search.
	 * @param ci      Event metadata for the operation.
	 * @throws Exception When something goes wrong.
	 */
	void deleteGroupsByTag(String tag, UserI user, EventMetaI ci) throws Exception;
	
	/**
	 * add this group for the specified user (locally).   This will not update the database.  It will add the user to this group in local memory.
	 * 
	 * Sometimes, a user group is associated with a user, before the user group is physically created in the database.  This method can be used to do that.
	 * 
	 * To specifically add the user to the group permanently, use the AddUserToGroup method
	 * @param user       The user to add to the group.
	 * @param groupId    The ID of the group to add.
	 * @param group      The user group.
	 */
	void updateUserForGroup(UserI user, String groupId, UserGroupI group);

	/**
	 * Remove user from the group (including updating database if necessary)
	 *
	 * @param user       		The user to remove from the group.
	 * @param authenticatedUser The user requesting the removal.
	 * @param groupId    		The ID of the group to remove the user from.
	 * @param ci         		Event metadata for the operation.
	 * @throws Exception 		When something goes wrong.
	 */
	void removeUserFromGroup(UserI user, UserI authenticatedUser, String groupId, EventMetaI ci) throws Exception;
	
	/**
	 * Refresh the user group for this user (this updates any local copies of the group for this user).  This should be eliminated by a more clear caching mechanism.
	 * 
	 * @param user       The user to refresh.
	 * @param groupId    The group to refresh.
	 */
	void reloadGroupForUser(UserI user, String groupId);
	
	/**
	 * Refresh all of the user groups for this user.
	 * 
	 * @param user    The user to refresh.
	 */
	void reloadGroupsForUser(UserI user);
	
	/**
	 * Create user group using the defined permissions.
	 * 
	 * @param id : String ID to use for the group ID
	 * @param displayName          The display name for the user group.
	 * @param create : true if members should be able to create the data types in the List&lt;ElementSecurity&gt; ess, else false
	 * @param read : true if members should be able to read the data types in the List&lt;ElementSecurity&gt; ess, else false
	 * @param delete : true if members should be able to delete the data types in the List&lt;ElementSecurity&gt; ess, else false
	 * @param edit : true if members should be able to edit the data types in the List&lt;ElementSecurity&gt; ess, else false
	 * @param activate : true if members should be able to activate the data types in the List&lt;ElementSecurity&gt; ess, else false
	 * @param activateChanges : Should the permissions be activated upon creation (or wait for approval later)
	 * @param ess : List of data types that this group should have permissions for
	 * @param tag : Tag for permissions to key of off (typically the project ID)
	 * @param authenticatedUser    The user performing the operation.
	 * @return The newly created group.
	 */
	UserGroupI createGroup(final String id, final String displayName, Boolean create,Boolean read,Boolean delete,Boolean edit,Boolean activate,boolean activateChanges,List<ElementSecurity> ess, String tag, UserI authenticatedUser);
	
	/**
	 * create or update a group using the defined permissions.  If the group doesn't exist, a new one will be created.  If it does, the permissions will be updated according to the passed parameters
	 * 
	 * @param id : String ID to use for the group ID
	 * @param displayName          The display name for the user group.
	 * @param create : true if members should be able to create the data types in the List&lt;ElementSecurity&gt; ess, else false
	 * @param read : true if members should be able to read the data types in the List&lt;ElementSecurity&gt; ess, else false
	 * @param delete : true if members should be able to delete the data types in the List&lt;ElementSecurity&gt; ess, else false
	 * @param edit : true if members should be able to edit the data types in the List&lt;ElementSecurity&gt; ess, else false
	 * @param activate : true if members should be able to activate the data types in the List&lt;ElementSecurity&gt; ess, else false
	 * @param activateChanges : Should the permissions be activated upon creation (or wait for approval later)
	 * @param ess : List of data types that this group should have permissions for
	 * @param tag : Tag for permissions to key of off (typically the project ID)
	 * @param authenticatedUser    The user performing the operation.
	 * @return The created or updated group.
	 * @throws Exception When something goes wrong.
	 */
	UserGroupI createOrUpdateGroup(final String id, final String displayName, Boolean create,Boolean read,Boolean delete,Boolean edit,Boolean activate,boolean activateChanges,List<ElementSecurity> ess, String tag, UserI authenticatedUser) throws Exception;
	
	/**
	 * Add user to the group (includes potential modification to the database).
	 * 
	 * @param group_id             The ID of the group to which the user should be added.
	 * @param newUser              The user to add to the group.
	 * @param authenticatedUser    The user performing the operation.
	 * @param ci                   Event metadata for the operation.
	 * @return The group with the newly added user.
	 * @throws Exception When something goes wrong.
	 */
	UserGroupI addUserToGroup(String group_id, UserI newUser,UserI authenticatedUser, EventMetaI ci) throws Exception;
	
	/**
	 * The data type or classification identifier used to identify the group data type.  
	 * 
	 * This will be used in workflow events that are created to track modifications to user groups.  
	 * @return The data type identifier.
	 */
	String getGroupDatatype();

	
    /**
     * Return a freshly created group object populated with the passed parameters.
     * 
     * Object may or may not already exist in the database.
     * 
     * @return The newly created group.
     * @throws GroupFieldMappingException When an error occurs mapping the parameters to the group.
     */
	UserGroupI createGroup(Map<String, ?> params) throws GroupFieldMappingException;

	/**
	 * @param group    The group to delete.
	 * @param user     The user running the operation.
	 * @param ci       Event metadata for the operation.
	 */
	void deleteGroup(UserGroupI group, UserI user, EventMetaI ci);

	/**
	 * @param gID    The primary key of the group to retrieve.
	 * @return The group if found.
	 */
	UserGroupI getGroupByPK(Object gID);

	/**
	 * @param pID    The tag ID.
	 * @param gID    The group ID.
	 * @return The group if found.
	 */
	UserGroupI getGroupByTagAndName(String pID, String gID);

	/**
	 * @param tempGroup    The in-memory group.
	 * @param user         The user running the operation.
	 * @param meta         Event metadata for the operation.
	 * @throws Exception When something goes wrong.
	 */
	void save(UserGroupI tempGroup, UserI user, EventMetaI meta) throws Exception;
}