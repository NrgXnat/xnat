package org.nrg.xnat.snapshot.services;

/**
 * @author pradeep.d
 *
 */
public interface SnapshotGenerationService {

	/**
	 * @param projectID
	 * @param sessionIdentifier
	 * @param scanIdentifier
	 * @param gridView
	 * @return
	 */
	public String generateSnapshot(String projectID, String sessionIdentifier, String scanIdentifier, String gridView);
}
