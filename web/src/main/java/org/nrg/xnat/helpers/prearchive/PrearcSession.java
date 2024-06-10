package org.nrg.xnat.helpers.prearchive;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
@Accessors(prefix = "_")
public class PrearcSession {
    public PrearcSession(final File sessionDir, final UserI user) throws Exception {
        this(PrearcTableBuilder.parseSession(new File(StringUtils.appendIfMissing(sessionDir.getAbsolutePath(), ".xml"))).getProject(), sessionDir.getParentFile().getName(), sessionDir.getName(), Collections.<String, Object>emptyMap(), user);
    }

    public PrearcSession(final URIManager.PrearchiveURI parsedURI, final Map<String, Object> additionalValues, final UserI user) throws Exception {
        this((String) parsedURI.getProps().get(URIManager.PROJECT_ID),
             (String) parsedURI.getProps().get(PrearcUtils.PREARC_TIMESTAMP),
             (String) parsedURI.getProps().get(PrearcUtils.PREARC_SESSION_FOLDER), additionalValues, user);
    }

    public PrearcSession(final PrearchiveOperationRequest request, final UserI user) throws Exception {
        this(request.getSessionData().getProject(), request.getSessionData().getTimestamp(), request.getSessionData().getFolderName(), request.getParameters(), user);
    }

    public PrearcSession(final String project, final String timestamp, final String folderName, final Map<String, Object> properties, final UserI user) throws Exception {
        if (folderName == null || timestamp == null) {
            throw new IllegalArgumentException();
        }
        _folderName = folderName;
        _project = project;
        _timestamp = timestamp;
        _additionalValues = null != properties ? new HashMap<>(properties) : new HashMap<String, Object>();
        _sessionDir = PrearcUtils.getPrearcSessionDir(user, project, timestamp, folderName, true);
    }

    @Override
    public String toString() {
        return StringUtils.joinWith("/", _project, _timestamp, _folderName);
    }

    public String getUrl() {
        return PrearcUtils.buildURI(_project, _timestamp, _folderName);
    }

    public SessionData getSessionData() throws Exception {
        return PrearcDatabase.getSessionIfExists(_folderName, _timestamp, _project);
    }

    public String getSource() throws Exception {
        final SessionData sessionData = getSessionData();
        return sessionData != null ? sessionData.getSource() : null;
    }

    public boolean isAutoArchive() throws Exception {
        return isAutoArchive(null);
    }

    public boolean isAutoArchive(final URIManager.DataURIA destination) throws Exception {
        //determine auto-archive setting
        if (StringUtils.isBlank(getProject())) {
            return setArchiveReason(false);
        }

        final SessionData sessionData = getSessionData();
        if (sessionData != null) {
            final PrearchiveCode sessionAutoArcSetting = sessionData.getAutoArchive();
            if (sessionAutoArcSetting == PrearchiveCode.AutoArchive || sessionAutoArcSetting == PrearchiveCode.AutoArchiveOverwrite) {
                return setArchiveReason(true);
            }
        }
        
        if (destination instanceof URIManager.ArchiveURI) {
            setArchiveReason(false);
            return true;
        }

        // If the user has specified auto-archive, override the project setting.
        final Boolean userArchiveSetting = hasAutoArchiveProperty();
        if (null != userArchiveSetting) {
            return setArchiveReason(userArchiveSetting);
        }

        final Integer code = ArcSpecManager.GetInstance().getPrearchiveCodeForProject(getProject());
        if (code != null && code >= 4) {
            return setArchiveReason(true);
        }

        return setArchiveReason(false);
    }

    public boolean isOverwriteFiles() throws Exception {
        //determine overwrite_files setting
        if (StringUtils.isBlank(getProject())) {
            return false;
        }

        if (hasOverwriteFilesProperty()) {
            return true;
        }

        final SessionData sessionData = getSessionData();
        if (sessionData != null) {
            final PrearchiveCode sessionAutoArcSetting = sessionData.getAutoArchive();
            if (sessionAutoArcSetting == PrearchiveCode.AutoArchiveOverwrite) {
                return true;
            }
        }

        final Integer code = ArcSpecManager.GetInstance().getPrearchiveCodeForProject(getProject());
        return code != null && code.equals(PrearchiveCode.AutoArchiveOverwrite.getCode());

    }

    public boolean setArchiveReason(final boolean autoArchive) {
        if (autoArchive) {
            if (!getAdditionalValues().containsKey(EventUtils.EVENT_REASON)) {
                getAdditionalValues().put(EventUtils.EVENT_REASON, "auto-archive");
            }
        } else {
            if (!getAdditionalValues().containsKey(EventUtils.EVENT_REASON)) {
                getAdditionalValues().put(EventUtils.EVENT_REASON, "standard upload");
            }
        }

        return autoArchive;
    }

    public void populateAdditionalFields(final UserI user) throws ActionException {
        //prepare params by removing non xml path names
        final Map<String, Object> cleaned = XMLPathShortcuts.identifyUsableFields(getAdditionalValues(), XMLPathShortcuts.EXPERIMENT_DATA, false);

        if (cleaned.size() > 0) {
            final SAXReader reader = new SAXReader(user);
            final File      xml    = new File(getSessionDir().getParentFile(), getSessionDir().getName() + ".xml");

            try {
                XFTItem item = reader.parse(xml.getAbsolutePath());

                try {
                    item.setProperties(cleaned, true);
                } catch (Exception e) {
                    throw new ClientException("unable to map parameters to valid xml path: ", e);
                }

                try (final FileWriter writer = new FileWriter(xml)) {
                    item.toXML(writer, false);
                } catch (IllegalArgumentException | IOException | SAXException e) {
                    throw new ServerException(e);
                }
            } catch (IOException | SAXException e1) {
                throw new ServerException(e1);
            }
        }
    }

    private boolean hasOverwriteFilesProperty() {
        return StringUtils.equalsIgnoreCase((String) _additionalValues.get(RequestUtil.OVERWRITE_FILES), RequestUtil.TRUE);
    }

    private Boolean hasAutoArchiveProperty() {
        final Optional<String> key = Iterables.tryFind(_additionalValues.keySet(), Predicates.contains(AUTO_ARCHIVE_KEYS));
        return key.isPresent() ? BooleanUtils.toBooleanObject((String) _additionalValues.get(key.get())) : null;
    }

    private static final Pattern AUTO_ARCHIVE_KEYS = Pattern.compile("(" + RequestUtil.AA + "|" + RequestUtil.AUTO_ARCHIVE + ")");

    private final File                _sessionDir;
    private final String              _project;
    private final String              _timestamp;
    private final String              _folderName;
    private final Map<String, Object> _additionalValues;
}
