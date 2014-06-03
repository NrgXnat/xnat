package org.nrg.xdat.security;

import java.util.List;

import org.nrg.xft.ItemWrapper;

public interface PermissionSetI {

	/**
	 * @return
	 */
	public abstract String getSchemaElementName();

	/**
	 * @param access
	 * @param row
	 * @return
	 * @throws ItemWrapper.FieldEmptyException
	 * @throws Exception
	 */
	public abstract boolean canAccess(String access, SecurityValues row) throws ItemWrapper.FieldEmptyException,Exception;

	/**
	 * @return
	 */
	public abstract boolean canReadAny();

	/**
	 * @return
	 */
	public abstract boolean canCreateAny();

	/**
	 * @return
	 */
	public abstract boolean canEditAny();
	
	/**
	 * @return
	 */
	public boolean isActive();
	
	/**
	 * @return
	 */
	public String getMethod();
	
	/**
     * @return the permCriteria
     */
    public List<PermissionCriteriaI> getAllCriteria();
	
	/**
     * @return the permCriteria
     */
    public List<PermissionCriteriaI> getPermCriteria();
    
    /**
     * @return the permSet
     */
    public List<PermissionSetI> getPermSets();
    
    /**
     * @param fieldName
     * @param value
     * @return
     * @throws Exception
     */
    public PermissionCriteriaI getMatchingPermissions(String fieldName, Object value) throws Exception;
}