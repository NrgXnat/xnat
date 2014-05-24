//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.presentation;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.nrg.xft.presentation.FlattenedItemA.FileSummary;

public interface FlattenedItemI {
	
	public abstract List<FileSummary> getMisc();
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#isDeleted()
	 */
	public abstract boolean isDeleted();
	
	
	public abstract Number getCreate_event_id();

	public abstract Number getModified_event_id();

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getItemObject()
	 */
	public abstract FlattenedItemA.ItemObject getItemObject();

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getChange_date()
	 */
	public abstract Date getChange_date();

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getFields()
	 */
	public abstract FlattenedItemA.FieldTracker getFields();

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#isHistory()
	 */
	public abstract boolean isHistory();

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getLast_modified()
	 */
	public abstract Date getLast_modified();

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getInsert_date()
	 */
	public abstract Date getInsert_date();

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getXSIType()
	 */
	public abstract String getXSIType();

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getPrevious_change_date()
	 */
	public abstract Date getPrevious_change_date();

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getChildCollections()
	 */
	public abstract Map<String, FlattenedItemA.ChildCollection> getChildCollections();

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getHistory()
	 */
	public abstract List<FlattenedItemI> getHistory();

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getTotalChildren()
	 */
	public abstract int getTotalChildren();

	public abstract List<String> getPks();

	public abstract Date getEndDate();

	public abstract Date getStartDate();

	public abstract Integer getId();

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getParents()
	 */
	public abstract List<FlattenedItemA.ItemObject> getParents();

	public <S extends FlattenedItemI>S filteredCopy(FlattenedItemA.FilterI fi) throws Exception;
	
	public void setChildCollections(Map<String, FlattenedItemA.ChildCollection> children);
	
	public void addChildCollection(String path,String display, List<FlattenedItemI> items);
	
	public void setHistory(List<FlattenedItemI> history);
	
	public Number getCreateEventId();
	public Number getModifiedEventId();
	
	public String getCreateUsername();
	public String getModifiedUsername();
}
