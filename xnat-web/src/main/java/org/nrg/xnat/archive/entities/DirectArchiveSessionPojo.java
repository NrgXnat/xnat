package org.nrg.xnat.archive.entities;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;

import java.util.Date;

@Slf4j
public class DirectArchiveSessionPojo {
    private int id;
    private String subject, session, tag, visit, protocol, timeZone, location;
    private Date uploadDate;
    private Date lastBuiltDate;
    private PrearcUtils.PrearcStatus status;
    private Date scanDate;

    public DirectArchiveSessionPojo(int id, String subject, String session, String tag, String visit, String protocol,
                                    String timeZone, String location, Date uploadDate, Date lastBuiltDate,
                                    PrearcUtils.PrearcStatus status, Date scanDate) {
        this(subject, session, tag, visit, protocol, timeZone, location, uploadDate, lastBuiltDate, status, scanDate);
        this.id = id;
    }

    public DirectArchiveSessionPojo(String subject, String session, String tag, String visit, String protocol,
                                    String timeZone, String location, Date uploadDate, Date lastBuiltDate,
                                    PrearcUtils.PrearcStatus status, Date scanDate) {
        this.subject = subject;
        this.session = session;
        this.tag = tag;
        this.visit = visit;
        this.protocol = protocol;
        this.timeZone = timeZone;
        this.location = location;
        this.uploadDate = uploadDate;
        this.lastBuiltDate = lastBuiltDate;
        this.status = status;
        this.scanDate = scanDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getVisit() {
        return visit;
    }

    public void setVisit(String visit) {
        this.visit = visit;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Date getLastBuiltDate() {
        return lastBuiltDate;
    }

    public void setLastBuiltDate(Date lastBuiltDate) {
        this.lastBuiltDate = lastBuiltDate;
    }

    public PrearcUtils.PrearcStatus getStatus() {
        return status;
    }

    public void setStatus(PrearcUtils.PrearcStatus status) {
        this.status = status;
    }

    public Date getScanDate() {
        return scanDate;
    }

    public void setScanDate(Date scanDate) {
        this.scanDate = scanDate;
    }
}
