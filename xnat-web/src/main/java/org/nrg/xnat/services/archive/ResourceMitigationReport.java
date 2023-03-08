package org.nrg.xnat.services.archive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Value
@Accessors(prefix = "_")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Slf4j
public class ResourceMitigationReport {
    public static final String KEY_FILE    = "file";
    public static final String KEY_MESSAGE = "message";

    @Builder
    public ResourceMitigationReport(final long resourceSurveyRequestId, final Path cachePath, final Map<File, File> movedFiles, final Map<File, File> removedFiles,
                                    final Set<File> retainedFiles, final Map<File, Map<String, String>> backupErrors, final Map<File, Map<String, String>> moveErrors,
                                    final Map<File, String> deleteErrors, final String catalogWriteError, final String resourceSaveError) {
        this(resourceSurveyRequestId, cachePath.toAbsolutePath().toString(), movedFiles, removedFiles, retainedFiles, backupErrors, moveErrors, deleteErrors, catalogWriteError, resourceSaveError, -1, -1, -1);
    }

    @JsonCreator
    public ResourceMitigationReport(final @JsonProperty("resourceSurveyRequestId") long resourceSurveyRequestId,
                                    final @JsonProperty("cachePath") @Nonnull String cachePath,
                                    final @JsonProperty("movedFiles") Map<File, File> movedFiles,
                                    final @JsonProperty("removedFiles") Map<File, File> removedFiles,
                                    final @JsonProperty("retainedFiles") Set<File> retainedFiles,
                                    final @JsonProperty("backupErrors") Map<File, Map<String, String>> backupErrors,
                                    final @JsonProperty("moveErrors") Map<File, Map<String, String>> moveErrors,
                                    final @JsonProperty("deleteErrors") Map<File, String> deleteErrors,
                                    final @JsonProperty("catalogWriteError") String catalogWriteError,
                                    final @JsonProperty("resourceSaveError") String resourceSaveError,
                                    final @JsonProperty("totalMovedFiles") int totalMovedFiles,
                                    final @JsonProperty("totalRemovedFiles") int totalRemovedFiles,
                                    final @JsonProperty("totalFileErrors") int totalFileErrors) {
        _resourceSurveyRequestId = resourceSurveyRequestId;
        _cachePath               = cachePath;
        _movedFiles              = Optional.ofNullable(movedFiles).orElseGet(Collections::emptyMap);
        _removedFiles            = Optional.ofNullable(removedFiles).orElseGet(Collections::emptyMap);
        _retainedFiles           = Optional.ofNullable(retainedFiles).orElseGet(Collections::emptySet);
        _backupErrors            = Optional.ofNullable(backupErrors).orElseGet(Collections::emptyMap);
        _moveErrors              = Optional.ofNullable(moveErrors).orElseGet(Collections::emptyMap);
        _deleteErrors            = Optional.ofNullable(deleteErrors).orElseGet(Collections::emptyMap);
        _catalogWriteError       = catalogWriteError;
        _resourceSaveError       = resourceSaveError;
        _totalMovedFiles         = totalMovedFiles == -1 ? _movedFiles.size() : totalMovedFiles;
        _totalRemovedFiles       = totalRemovedFiles == -1 ? _removedFiles.size() : totalRemovedFiles;
        _totalFileErrors         = totalFileErrors == -1 ? _backupErrors.size() + _moveErrors.size() + _deleteErrors.size() : totalFileErrors;
    }

    public static Map<String, String> mapFileError(final File file, final String message) {
        final Map<String, String> map = new HashMap<>();
        map.put(ResourceMitigationReport.KEY_FILE, file.getAbsolutePath());
        map.put(ResourceMitigationReport.KEY_MESSAGE, message);
        return map;
    }

    public boolean hasErrors() {
        return StringUtils.isNotBlank(_catalogWriteError) || StringUtils.isNotBlank(_resourceSaveError) || _totalFileErrors > 0;
    }

    @Positive
    long _resourceSurveyRequestId;

    @NonNull
    @NotNull
    String _cachePath;
    Map<File, File>                _movedFiles;
    Map<File, File>                _removedFiles;
    Set<File>                      _retainedFiles;
    Map<File, Map<String, String>> _backupErrors;
    Map<File, Map<String, String>> _moveErrors;
    Map<File, String>              _deleteErrors;
    String                         _catalogWriteError;
    String                         _resourceSaveError;
    int                            _totalMovedFiles;
    int                            _totalRemovedFiles;
    int                            _totalFileErrors;
}
