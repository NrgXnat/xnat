package org.nrg.xnat.tracking.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.NotNull;
import org.nrg.xnat.event.archive.ArchiveEventI;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonInclude
public class EventTrackingLog {
    private List<MessageEntry> entryList = new ArrayList<>();

    public EventTrackingLog() {}

    public EventTrackingLog(List<MessageEntry> entryList) {
        this.entryList = entryList;
    }

    public List<MessageEntry> getEntryList() {
        return entryList;
    }

    public void setEntryList(List<MessageEntry> entryList) {
        this.entryList = entryList;
    }

    public void addToEntryList(MessageEntry entry) {
        this.entryList.add(entry);
    }

    public void sortEntryList(){
        Collections.sort(this.entryList);
    }

    @JsonInclude
    public static class MessageEntry implements Comparable<MessageEntry> {
        private ArchiveEventI.Status status;
        private long eventTime;
        @Nullable private String message;

        public MessageEntry() {}

        public MessageEntry(ArchiveEventI.Status status, long eventTime, @Nullable String message) {
            this.status = status;
            this.eventTime = eventTime;
            this.message = message;
        }

        public ArchiveEventI.Status getStatus() {
            return status;
        }

        public void setStatus(ArchiveEventI.Status status) {
            this.status = status;
        }

        @Nullable
        public String getMessage() {
            return message;
        }

        public void setMessage(@Nullable String message) {
            this.message = message;
        }

        public long getEventTime() {
            return eventTime;
        }

        public void setEventTime(long eventTime) {
            this.eventTime = eventTime;
        }

        @Override
        public int compareTo(@NotNull EventTrackingLog.MessageEntry o) {
            return Long.compare(this.eventTime, o.eventTime);
        }
    }
}
