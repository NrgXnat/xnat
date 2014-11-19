package org.nrg.xdat.security;

/**
 * @author Tim Olsen <tim@deck5consulting.com>
 *
 * Interface used to track individual permissions within XNAT.  
 * 
 * Schema element name identifies the data type for which acceess is being restricted/permitted.
 * 
 * Field is used to identify the relationship between the data object and the value being referenced.  (this is typically an xml path to a primary project field, or a xml path to a sharing/share field.
 * 
 * Field value is typically the project ID that is being secured.
 * 
 * So, the permissions could be ready, someone with this criteria can {ACTION} a {SCHEMA_ELEMENT_NAME} where {FIELD} is {FIELD_VALUE}.
 * example: can 'CREATE' a 'xnat:subjectData' where 'xnat:subjectData/project' is 'project_id'
 * example: can 'READ' a 'xnat:mrSessionData' where 'xnat:mrSessionData/sharing/share/project' is 'project_id2'
 */
public interface PermissionCriteriaI {

	/**
	 * @return
	 */
	public abstract String getElementName();

	/**
	 * @return
	 */
	public abstract String getField();

	/**
	 * @return
	 */
	public abstract Object getFieldValue();

	/**
	 * @return
	 */
	public abstract boolean getCreate();

	/**
	 * @return
	 */
	public abstract boolean getRead();

	/**
	 * @return
	 */
	public abstract boolean getEdit();

	/**
	 * @return
	 */
	public abstract boolean getDelete();

	/**
	 * @return
	 */
	public abstract boolean getActivate();

	/**
	 * Does this criteria object allow access to any of the field/value combinations in this list of values.
	 * 
	 * @param action : read, create, edit, delete, activate
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public abstract boolean canAccess(String action, SecurityValues values) throws Exception;

	/**
	 * @return
	 */
	public boolean isActive();
	
	/**
	 * @param action : read, create, edit, delete, activate
	 * @return
	 */
	public boolean getAction(String action);
}