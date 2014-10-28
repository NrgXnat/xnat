package org.nrg.xdat.services;

import java.util.List;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.entities.GroupFeature;

public interface GroupFeatureService extends BaseHibernateService<GroupFeature> {
    /**
     * Finds all features for the specified group
     *
     * @param groupId The group ID from the XdatUserGroup table.
     * @return An list of the {@link GroupFeature group features} issued to the indicated group.
     */
    abstract public List<GroupFeature> findFeaturesForGroup(String groupId);

    /**
     * Finds all features for the specified groups
     *
     * @param groupIds The group IDs from the XdatUserGroup table.
     * @return An list of the {@link GroupFeature group features} issued to the indicated group.
     */
    abstract public List<GroupFeature> findFeaturesForGroups(List<String> groupIds);
    
    abstract public List<GroupFeature> findFeaturesForTag(String tag);

    /**
     * Finds all groups for the specified feature.
     *
     * @param feature The feature to match.
     * @return An list of the {@link GroupFeature group features} issued to the indicated feature.
     */
    abstract public List<GroupFeature> findGroupsForFeature(String feature);

    /**
     * Finds all groups for the specified feature.
     *
     * @param feature The feature to match.
     * @return An list of the {@link GroupFeature group features} issued to the indicated feature.
     */
    abstract public List<GroupFeature> findGroupsForFeatures(List<String> feature);

    /**
     * Deletes the specified group feature combo.
     *
     * @param groupId The group ID to match.
     * @param feature    The feature to match.
     */
    abstract public void delete(final String groupId, final String feature);

    /**
     * Deletes the specified group.
     *
     * @param groupId The group ID to match.
     */
    abstract public void deleteByGroup(final String groupId);

    /**
     * Deletes the specified tag.
     *
     * @param tag The tag to match.
     */
    abstract public void deleteByTag(final String tag);

    /**
     * Creates the specified group feature combo.
     *
     * @param groupId The group ID.
     * @param tag     The tag.
     * @param feature    The feature.
     * @return created Groupfeature
     */
    abstract public GroupFeature addFeatureToGroup(final String groupId, final String tag, final String feature);

    /**
     * Finds all matching group features
     *
     * @param groupId The group ID.
     * @param feature    The feature.
     * @return The matched group feature
     */
    abstract public GroupFeature findGroupFeature(String groupId, String feature);

	/**
	 * Block feature for this group
	 * @param group
	 * @param feature
	 */
    public void blockFeatureForGroup(String id, String tag, String feature);

	/**
	 * get features with OnByDefault=true and tag=x
	 * @param group
	 * @param feature
	 */
    public List<GroupFeature> getEnabledByTag(String tag);

	/**
	 * get features with OnByDefault=true and tag=x
	 * @param group
	 * @param feature
	 */
    public List<GroupFeature> getBannedByTag(String tag);
}
