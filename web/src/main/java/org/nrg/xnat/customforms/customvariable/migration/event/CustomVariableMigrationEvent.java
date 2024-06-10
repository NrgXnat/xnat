package org.nrg.xnat.customforms.customvariable.migration.event;

import org.nrg.xdat.XDAT;
import org.nrg.xnat.customforms.customvariable.migration.model.CustomVariableMigrationEventTrackingLog;
import org.nrg.xnat.tracking.model.TrackableEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.io.IOException;

@Data
@AllArgsConstructor
@Builder
@Slf4j
public class CustomVariableMigrationEvent implements TrackableEvent {
    public enum Status { Waiting, InProgress, Warning, Completed, Failed; }

    @NotNull
    @Override
    public String getTrackingId() {
        return trackingId;
    }

    @NotNull
    @Override
    public Integer getUserId() {
        return userId;
    }

    @Override
    public boolean isSuccess() {
        return Status.Completed == status;
    }

    @Override
    public boolean isCompleted() { return progress == 100; }

    @Nullable
    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String updateTrackingPayload(@Nullable String currentPayload) throws IOException {
        CustomVariableMigrationEventTrackingLog statusLog;
        if (currentPayload != null) {
            statusLog = XDAT.getSerializerService().getObjectMapper()
                    .readValue(currentPayload, CustomVariableMigrationEventTrackingLog.class);
        } else {
            statusLog = new CustomVariableMigrationEventTrackingLog();
        }
        statusLog.addToEntryList(new CustomVariableMigrationEventTrackingLog.MessageEntry(status, eventTime, message));
        statusLog.sortEntryList();
        return XDAT.getSerializerService().getObjectMapper().writeValueAsString(statusLog);
    }

    @Override
    public String toString() {
        return getTrackingId() + ": Custom Variable Migration : " + status.toString() ;
    }

    public static CustomVariableMigrationEvent progress(final Integer userId,  final String trackingId, final String message) {
        return builder().status(Status.InProgress)
                .trackingId(trackingId)
                .progress(0)
                .message(message)
                .eventTime(System.nanoTime())
                .userId(userId)
                .build();
    }

    public static CustomVariableMigrationEvent complete(final Integer userId, final String trackingId, final String message) {
        return builder().status(Status.Completed)
                .trackingId(trackingId)
                .progress(100)
                .message(message)
                .eventTime(System.nanoTime())
                .userId(userId)
                .build();
    }

    public static CustomVariableMigrationEvent warn(final Integer userId,  final String trackingId, final String message) {
        return builder().status(Status.Warning)
                .trackingId(trackingId)
                .progress(0)
                .message(message)
                .eventTime(System.nanoTime())
                .userId(userId)
                .build();
    }

    public static CustomVariableMigrationEvent fail(final Integer userId,  final String trackingId, final String message) {
        return builder().status(Status.Failed)
                .trackingId(trackingId)
                .progress(0)
                .message(message)
                .eventTime(System.nanoTime())
                .userId(userId)
                .build();
    }

    public static CustomVariableMigrationEvent waiting(final Integer userId, final String trackingId, final String message) {
        return builder().status(Status.Waiting)
                .trackingId(trackingId)
                .progress(0)
                .message(message)
                .eventTime(System.nanoTime())
                .userId(userId)
                .build();
    }

    private int           progress;
    private String        trackingId;
    private String        message;
    private long          eventTime;
    private Integer       userId;
    private Status        status;
}
