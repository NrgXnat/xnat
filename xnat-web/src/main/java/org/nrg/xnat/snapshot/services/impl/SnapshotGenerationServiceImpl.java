package org.nrg.xnat.snapshot.services.impl;

import java.io.File;
import org.nrg.action.ClientException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.XnatImagescandataBean;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.ResourceData;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.snapshot.convert.SnapshotDicomConvertImage;
import org.nrg.xnat.snapshot.services.SnapshotGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pradeep.d
 *
 */
@Service
@Slf4j
public class SnapshotGenerationServiceImpl implements SnapshotGenerationService {

	/**
	 * @param catalogService
	 */
	@Autowired
	public SnapshotGenerationServiceImpl(final CatalogService catalogService) {
		_catalogService = catalogService;
	}

	@Override
	public String generateSnapshot(String projectID, String sessionIdentifier, String scanIdentifier, String gridView) {
		String path = null;
		try {
			_log.debug("SnapshotServiceImpl  generateSnapshot method start ");
			boolean verifySnapshot = verifySnapshots(sessionIdentifier, scanIdentifier);
			if (verifySnapshot) {
				path = getSnapshotsImage(sessionIdentifier, scanIdentifier, gridView);
			} else if (!verifySnapshot) {
				_message = "Snapshots Folder does not exist";
				_log.error("Snapshots Folder does not exist");
				return _message;
			} else {
				_message = "Snapshots- images does not exist";
				_log.error(_message);
				return _message;
			}
			_log.debug("generateSnapshot()- Snapshot path ::  " + path);
		} catch (Exception ex) {
			_log.error("SnapshotServiceImpl  generateSnapshot method error -" + ex.getMessage());
		}
		return path;
	}

	/**
	 * @param accessionId
	 * @param scanIdentifier
	 * @param gridView
	 * @return
	 * @throws Exception
	 */
	private String getSnapshotsImage(String accessionId, String scanIdentifier, String gridView) throws Exception {
		_log.debug("Snapshots verifyImage() ");
		boolean imageVerify = false;
		String path = null;
		String parentUri = ROOT_URI + accessionId + "/scans/" + scanIdentifier + SNAPSHOTS_RESOURCE;
		this.imageName = accessionId + "_" + scanIdentifier;
		if (gridView.equals("notApplicable")) {
			this.imageName = this.imageName + ".gif";
		} else {
			this.imageName = this.imageName + "_" + gridView.toUpperCase() + ".gif";
		}
		try {
			ResourceData resourceData = _catalogService.getResourceDataFromUri(parentUri);
			XnatResourcecatalog xnatResourcecatalog = resourceData.getCatalogResource();
			path = new File(xnatResourcecatalog.getUri()).getParent();
			File snapshotFile = new File(path);
			String[] fileNames = snapshotFile.list();
			if (fileNames != null && fileNames.length > 0) {
				for (String fileNm : fileNames) {
					if (fileNm.equals(this.imageName)) {
						imageVerify = true; // snapshots image exist
						_log.debug("Exist snapshot image ");
						path = path + "/" + this.imageName;
					}
				}
			}
			if (!imageVerify) {
				path = imageUpload(accessionId, scanIdentifier, gridView);
			}

		} catch (ClientException e) {
			_log.error(String.format(e.getMessage(), parentUri));
		} catch (Exception e) {
			_log.error(" Snapshots image error:: " + e.getMessage());
		}
		return path;
	}

	/**
	 * @param accessionId
	 * @param scanIdentifier
	 * @return
	 * @throws Exception
	 */
	private boolean verifySnapshots(String accessionId, String scanIdentifier) throws Exception {
		boolean folderCreate = false;
		try {
			String parentUri = ROOT_URI + accessionId + "/scans/" + scanIdentifier + "/resources/SNAPSHOTS";
			ResourceData resourceData = _catalogService.getResourceDataFromUri(parentUri, true);
			XnatResourcecatalog xnatResourcecatalog = resourceData.getCatalogResource();
			if(xnatResourcecatalog == null) {
				folderCreate = snapshotsFolder(accessionId, scanIdentifier);
			}
			folderCreate = snapshotsFolder(accessionId, scanIdentifier);
		} catch (Exception ex) {
			_log.error("Error : Verify snapshots folder");
			ex.printStackTrace();
		}
		return folderCreate;
	}

	/**
	 * @param accessionNo
	 * @param scanIdentifier
	 * @return
	 */
	private boolean snapshotsFolder(String accessionNo, String scanIdentifier) {
		String parentUri = ROOT_URI + accessionNo + "/scans/" + scanIdentifier + SNAPSHOTS_RESOURCE;
		_log.debug(" Snapshots directory generation ");
		String createdUri = null;
		try {
			final UserI userI = XDAT.getUserDetails();
			String[] tags = { "" };
			final XnatResourcecatalog resourcecatalog = _catalogService.createAndInsertResourceCatalog(userI, parentUri,
					1, SNAPSHOTS, "Snapshots Desc", "GIF", SNAPSHOTS, tags);
			createdUri = UriParserUtils.getArchiveUri(resourcecatalog);
			_log.debug(" Snapshots directory generation-  createdUri :: " + createdUri);
		} catch (ClientException e) {
			_log.error(String.format(": " + e.getMessage(), parentUri));
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			_log.error("Snapshot folder not generated- error", e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @param accessionId
	 * @param scanIdentifier
	 * @param gridView
	 * @return
	 * @throws Exception
	 */
	private String imageUpload(String accessionId, String scanIdentifier, String gridView) throws Exception {
		String parentUri = ROOT_URI + accessionId + "/scans/" + scanIdentifier;
		String path = null;
		try {
			final UserI userI = XDAT.getUserDetails();
			ResourceData resourceData = _catalogService.getResourceDataFromUri(parentUri + DICOM_RESOURCE, true);
			XnatResourcecatalog xnatResourcecatalog = resourceData.getCatalogResource();
			File dicomFile = new File(xnatResourcecatalog.getUri());
			String dicompath = dicomFile.getParent();
			String tempImagePath = new File(dicompath).getParent();
			SnapshotDicomConvertImage dcm = new SnapshotDicomConvertImage(dicompath);
			XnatImagescandataBean scan = new XnatImagescandataBean();
			scan.setId(scanIdentifier);
			scan.setImageSessionId(accessionId);
			File file = null;
			if (!gridView.equals("notApplicable")) {
				_log.debug("Create Montage- GridView MXN image ");
				file = dcm.createSnapshotImage(scan, tempImagePath, true, gridView);
			} else {
				file = dcm.createSnapshotImage(scan, tempImagePath, false, "");
			}
			String[] tags = { "" };
			_log.debug("Upload image into Snapshots folder ");
			_catalogService.insertResources(userI, parentUri + SNAPSHOTS_RESOURCE, file, SNAPSHOTS, null, "GIF",
					"ORIGINAL", tags);
			dcm.deleteFile(file);
			resourceData = _catalogService.getResourceDataFromUri(parentUri + SNAPSHOTS_RESOURCE);
			xnatResourcecatalog = resourceData.getCatalogResource();
			path = new File(xnatResourcecatalog.getUri()).getParent();
			File snapshotFile = new File(path);
			String[] fileNames = snapshotFile.list();
			if (fileNames != null && fileNames.length > 0) {
				for (String fileNm : fileNames) {
					if (fileNm.equals(this.imageName)) {
						_log.debug("Path and name of generated snapshot image ");
						path = path + "/" + this.imageName;
					}
				}
			}

		} catch (ClientException e) {
			_log.error(String.format(e.getMessage(), parentUri));
			e.printStackTrace();
		} catch (Exception e) {
			_log.error(" Snapshots image error:: " + e.getMessage());
			e.printStackTrace();
		}
		return path;
	}

	private String imageName = null;
	private String _message = "";
	private final String DICOM_RESOURCE = "/resources/DICOM/files";
	private final String SNAPSHOTS_RESOURCE = "/resources/SNAPSHOTS/files";
	private final String ROOT_URI = "/archive/experiments/";
	private final String SNAPSHOTS = "SNAPSHOTS";
	private final CatalogService _catalogService;
	private static final Logger _log = LoggerFactory.getLogger(SnapshotGenerationServiceImpl.class);
}
