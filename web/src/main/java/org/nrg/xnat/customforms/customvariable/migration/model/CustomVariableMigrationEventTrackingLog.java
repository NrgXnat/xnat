package org.nrg.xnat.customforms.customvariable.migration.model;

import org.nrg.xnat.customforms.customvariable.migration.event.CustomVariableMigrationEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonInclude
public class CustomVariableMigrationEventTrackingLog {
    private List<CustomVariableMigrationEventTrackingLog.MessageEntry> entryList = new ArrayList<>();

    public CustomVariableMigrationEventTrackingLog() {}

    public CustomVariableMigrationEventTrackingLog(List<CustomVariableMigrationEventTrackingLog.MessageEntry> entryList) {
        this.entryList = entryList;
    }

    public List<CustomVariableMigrationEventTrackingLog.MessageEntry> getEntryList() {
        return entryList;
    }

    public void setEntryList(List<CustomVariableMigrationEventTrackingLog.MessageEntry> entryList) {
        this.entryList = entryList;
    }

    public void addToEntryList(CustomVariableMigrationEventTrackingLog.MessageEntry entry) {
        this.entryList.add(entry);
    }

    public void sortEntryList(){
        Collections.sort(this.entryList);
    }

    @JsonInclude
    public static class MessageEntry implements Comparable<CustomVariableMigrationEventTrackingLog.MessageEntry> {
        private CustomVariableMigrationEvent.Status status;
        private long eventTime;
        @Nullable
        private String message;

        public MessageEntry() {}

        public MessageEntry(CustomVariableMigrationEvent.Status status, long eventTime, @Nullable String message) {
            this.status = status;
            this.eventTime = eventTime;
            this.message = message;
        }

        public CustomVariableMigrationEvent.Status getStatus() {
            return status;
        }

        public void setStatus(CustomVariableMigrationEvent.Status status) {
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
        public int compareTo(@NotNull CustomVariableMigrationEventTrackingLog.MessageEntry o) {
            return Long.compare(this.eventTime, o.eventTime);
        }
    }
}
