/*
 * web: org.nrg.xnat.archive.PrearcSessionValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.merge.MergePrearcToArchiveSession;
import org.nrg.xnat.helpers.merge.MergeSessionsA.SaveHandlerI;
import org.nrg.xnat.helpers.merge.MergeUtils;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PrearcSessionValidator extends PrearcSessionArchiver  {
	@SuppressWarnings("unused")
	protected PrearcSessionValidator(final XnatImagesessiondata src, final PrearcSession prearcSession, final UserI user, final String project, final Map<String,Object> params) {
		super(src,prearcSession,user,project,params,false,true,false,false);
	}

	@SuppressWarnings("unused")
	public PrearcSessionValidator(final PrearcSession session, final UserI user, final Map<String,Object> params) throws IOException, SAXException {
		super(session,user,params,false,true,false,false);
	}

	/**
	 * This method overwrites the one in archiver so that multiple exceptions can be recorded, rather than just the first one.
	 */
	public void checkForConflicts(final XnatImagesessiondata src, final File srcDIR, final XnatImagesessiondata existing, final File destDIR) {
		if(existing!=null){
			//it already exists
			conflict(1,PRE_EXISTS);

			//check if this would change the label (not allowed)
			if(!StringUtils.equals(src.getLabel(),existing.getLabel())){
				this.fail(2,LABEL_MOD);
			}

			//check if this would change the project (not allowed)
			if(!StringUtils.equals(existing.getProject(),src.getProject())){
				fail(3,PROJ_MOD);
			}

			//check if this would change the subject (not allowed)
			if(!StringUtils.equals(existing.getSubjectId(),src.getSubjectId())){
				String subjectId = existing.getLabel();
				String newError = SUBJECT_MOD + ": " + subjectId + " Already Exists for another Subject";
				fail(4,newError);
			}

			//check if the UIDs match
			if(StringUtils.isNotEmpty(existing.getUid()) && StringUtils.isNotEmpty(src.getUid())){
				if(!StringUtils.equals(existing.getUid(), src.getUid())){
					conflict(5,UID_MOD);
				}
			}

			//check if the XSI types match
			if(!StringUtils.equals(existing.getXSIType(), src.getXSIType())){
				fail(19,MODALITY_MOD);
			}

			for(final XnatImagescandataI newScan : src.getScans_scan()){
				XnatImagescandataI match=MergeUtils.getMatchingScanById(newScan.getId(), existing.getScans_scan());//match by ID
				if(match!=null){
					if(StringUtils.equals(match.getUid(),newScan.getUid())){
						conflict(16,"Session already contains a scan (" + match.getId() +") with the same UID and number.");
					}else{
						conflict(17,"Session already contains a scan (" + match.getId() +") with the same number, but a different UID. - New scan will be renamed to " + match.getId() + "_1.");
					}
				}

				XnatImagescandataI match2=MergeUtils.getMatchingScanByUID(newScan, existing.getScans_scan());//match by UID
				if(match2!=null){
					if(match==null || !StringUtils.equals(match.getId(),newScan.getId())){
						conflict(18,"Session already contains a scan (" + match2.getId() +") with the same UID, but a different number.");
					}
				}
			}
		}
	}

	public String call(){
		return null;
	}

	/**
	 * Mimics the behavior of PrearcSessionArchiver.call(), but tracks the exceptions, rather then failing on them.
	 * @return A list of validator notices.
	 */
	public List<? extends Notice> validate() throws ClientException   {
		if (StringUtils.isEmpty(project)) {
			fail(6, "unable to identify destination project");
		}

		try {
			populateAdditionalFields();
		} catch (ClientException e) {
			fail(7, e.getMessage());//this is a processing exception
		}

		try {
			fixSessionLabel();
		} catch (ClientException e) {
			fail(8, e.getMessage());//this means the code couldn't identify the session label.
		}

		final XnatImagesessiondata existing = retrieveExistingExpt();

		if (existing == null) {
			try {
				if (StringUtils.isBlank(src.getId())) {
					src.setId(XnatExperimentdata.CreateNewID());
				}
			} catch (Exception e) {
				fail(9, "unable to create new session ID");
			}
		} else {
			src.setId(existing.getId());
			try {
				preventConcurrentArchiving(existing.getId(), user);
			} catch (ClientException e) {
				conflict(10, e.getMessage());//this means there is an open workflow entry
			}
		}

		try {
			fixSubject(EventUtils.TEST_EVENT(user),false);
		} catch(Throwable e) {
			try {
				try {
					Thread.sleep(10000);
				}catch(InterruptedException ignored){
				}
				fixSubject(EventUtils.TEST_EVENT(user),false);
			} catch (ClientException e1) {
				fail(11, e1.getMessage());//this means the action couldn't identify the subject
			} catch (ServerException e1) {
				warn(12, e1.getMessage());//this just means the action was going to create a new subject
			}
		}

		try {
			validateSession();
		} catch (ServerException e) {
			fail(13, e.getMessage());//this is some sort of schema validation exception
		}

		final File arcSessionDir;
		try {
			arcSessionDir = getArcSessionDir();
		} catch (Exception e) {
			return notices;
		}

		if (existing != null) {
			checkForConflicts(src, prearcSession.getSessionDir(), existing, arcSessionDir);
		}

		final SaveHandlerI<XnatImagesessiondata> saveImpl = new SaveHandlerI<XnatImagesessiondata>() {
			public void save(final XnatImagesessiondata merged) {
				// Do nothing.
			}
		};

		final MergePrearcToArchiveSession merger = new MergePrearcToArchiveSession(src.getPrearchivePath(),
																				   prearcSession,
																				   src,
																				   src.getPrearchivepath(),
																				   arcSessionDir,
																				   existing,
																				   arcSessionDir.getAbsolutePath(),
																				   true,
																				   false,
																				   saveImpl, user, EventUtils.TEST_EVENT(user));

		try {
			merger.checkForConflict();
		} catch (ClientException e) {
			conflict(14, e.getMessage());
		} catch (ServerException e) {
			fail(15, e.getMessage());
		}

		//validate files to confirm DICOM contents
		validateDicomFiles();

		//verify compliance with DICOM whitelist/blacklist
		verifyCompliance();

		return notices;
	}

	public interface Notice extends Comparable<Notice> {
		int getCode();

		String getMessage();

		String getType();
	}

	@Getter
	@Accessors(prefix = "_")
	@AllArgsConstructor
	public static abstract class AbstractNotice implements Notice {
		@Override
		public int compareTo(@NotNull final Notice notice) {
			return getCode() - notice.getCode();
		}

		private final int    _code;
		private final String _message;
		private final String _type;
	}

	public static class Warning extends AbstractNotice {
		public Warning(final int code, final String message) {
			super(code, message, "WARN");
		}
	}

	public static class Failure extends AbstractNotice {
		public Failure(final int code, final String message) {
			super(code, message, "FAIL");
		}
	}

	public static class Conflict extends AbstractNotice {
		public Conflict(final int code, final String message) {
			super(code, message, "CONFLICT");
		}
	}

	//override implementations from PrearcSessionArchiver
	//prearcSessionArchiver will fail (throw exception) on the first issue it finds
	//validator should collect a list of all failures

	/**
	 * Adds a failure code and message to the list of notices for the current validation operation.
	 *
	 * @param code    The failure code.
	 * @param message A message explaining the failure in more detail.
	 */
	@Override
	protected void fail(final int code, final String message) {
		notices.add(new Failure(code, message));
	}

	/**
	 * Adds a warning code and message to the list of notices for the current validation operation.
	 *
	 * @param code    The warning code.
	 * @param message A message explaining the warning in more detail.
	 */
	@Override
	protected void warn(final int code, final String message) {
		notices.add(new Warning(code, message));
	}

	/**
	 * Adds a conflict code and message to the list of notices for the current validation operation.
	 *
	 * @param code    The conflict code.
	 * @param message A message explaining the conflict in more detail.
	 */
	@Override
	protected void conflict(final int code, final String message) {
		notices.add(new Conflict(code, message));
	}

	private final List<Notice> notices = new ArrayList<>();
}
