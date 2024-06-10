package org.nrg.xnat.services.archive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xnat.entities.ResourceSurveyRequest;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

@Value
@Accessors(prefix = "_")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Slf4j
public class ResourceSurveyReport implements Serializable {
    private static final long serialVersionUID = -2066472029149661423L;

    private static final Function<Map<String, Map<String, Map<File, String>>>, Integer> COUNT_DUPLICATES          = map -> map.values().stream().mapToInt(Map::size).sum();
    private static final Function<Map<String, Map<String, Map<File, String>>>, Integer> COUNT_FILES_IN_DUPLICATES = map -> map.values().stream().map(Map::values).flatMap(Collection::stream).mapToInt(Map::size).sum();

    @Builder
    public ResourceSurveyReport(final long resourceSurveyRequestId, final Date surveyDate, final int totalEntries, final Map<String, Set<String>> uids, final List<File> badFiles, final Map<File, String> mismatchedFiles, final Map<String, Map<String, Map<File, String>>> duplicates, final Map<String, Map<String, Map<File, String>>> nonActionableDuplicates) {
        this(resourceSurveyRequestId, surveyDate, totalEntries, -1, -1, -1, -1, -1, -1, -1, uids, badFiles, mismatchedFiles, duplicates, nonActionableDuplicates);
    }

    @JsonCreator
    public ResourceSurveyReport(final @JsonProperty("resourceSurveyRequestId") long resourceSurveyRequestId,
                                final @JsonProperty("surveyDate") Date surveyDate,
                                final @JsonProperty("totalEntries") int totalEntries,
                                final @JsonProperty("totalUids") int totalUids,
                                final @JsonProperty("totalBadFiles") int totalBadFiles,
                                final @JsonProperty("totalMismatchedFiles") int totalMismatchedFiles,
                                final @JsonProperty("totalDuplicates") int totalDuplicates,
                                final @JsonProperty("totalFilesInDuplicates") int totalFilesInDuplicates,
                                final @JsonProperty("totalNonActionableDuplicates") int totalNonActionableDuplicates,
                                final @JsonProperty("totalFilesInNonActionableDuplicates") int totalFilesInNonActionableDuplicates,
                                final @JsonProperty("uids") Map<String, Set<String>> uids,
                                final @JsonProperty("badFiles") List<File> badFiles,
                                final @JsonProperty("mismatchedFiles") Map<File, String> mismatchedFiles,
                                final @JsonProperty("duplicates") Map<String, Map<String, Map<File, String>>> duplicates,
                                final @JsonProperty("nonActionableDuplicates") Map<String, Map<String, Map<File, String>>> nonActionableDuplicates) {
        Validate.isTrue(resourceSurveyRequestId > 0, "You must specify a valid resource survey request ID for each report");
        _resourceSurveyRequestId             = resourceSurveyRequestId;
        _surveyDate                          = Optional.ofNullable(surveyDate).orElseGet(Date::new);
        _totalEntries                        = totalEntries;
        _uids                                = Optional.ofNullable(uids).orElseGet(Collections::emptyMap);
        _badFiles                            = Optional.ofNullable(badFiles).orElseGet(Collections::emptyList);
        _mismatchedFiles                     = Optional.ofNullable(mismatchedFiles).orElseGet(Collections::emptyMap);
        _duplicates                          = Optional.ofNullable(duplicates).orElseGet(Collections::emptyMap);
        _nonActionableDuplicates             = Optional.ofNullable(nonActionableDuplicates).orElseGet(Collections::emptyMap);
        _totalUids                           = totalUids == -1 ? _uids.values().stream().mapToInt(Set::size).sum() : totalUids;
        _totalBadFiles                       = totalBadFiles == -1 ? _badFiles.size() : totalBadFiles;
        _totalMismatchedFiles                = totalMismatchedFiles == -1 ? _mismatchedFiles.size() : totalMismatchedFiles;
        _totalDuplicates                     = totalDuplicates == -1 ? COUNT_DUPLICATES.apply(_duplicates) : totalDuplicates;
        _totalFilesInDuplicates              = totalFilesInDuplicates == -1 ? COUNT_FILES_IN_DUPLICATES.apply(_duplicates) : totalFilesInDuplicates;
        _totalNonActionableDuplicates        = totalNonActionableDuplicates == -1 ? COUNT_DUPLICATES.apply(_nonActionableDuplicates) : totalNonActionableDuplicates;
        _totalFilesInNonActionableDuplicates = totalFilesInNonActionableDuplicates == -1 ? COUNT_FILES_IN_DUPLICATES.apply(_nonActionableDuplicates) : totalFilesInNonActionableDuplicates;
    }

    /**
     * Flattens the duplicates map to use a compound key composed of SOP class and instance UID.
     *
     * @param duplicates The map of duplicated files keyed by SOP class and instance UID
     *
     * @return The flattened map.
     */
    public static Map<Pair<String, String>, Map<File, String>> flattenDuplicateMaps(final Map<String, Map<String, Map<File, String>>> duplicates) {
        final Map<Pair<String, String>, Map<File, String>> flattened = new HashMap<>();
        for (final String classUid : duplicates.keySet()) {
            final Map<String, Map<File, String>> instances = duplicates.get(classUid);
            for (final String instanceUid : instances.keySet()) {
                flattened.put(Pair.of(classUid, instanceUid), instances.get(instanceUid));
            }
        }
        return flattened;
    }

    /**
     * Expands the duplicates map to use nested keys composted of SOP class and instance UID. This method is the inverse
     * operation of {@link #flattenDuplicateMaps(Map)}.
     *
     * @param duplicates The map of duplicated files keyed by a pair of SOP class and instance UID.
     *
     * @return The unflattened map.
     */
    public static Map<String, Map<String, Map<File, String>>> unflattenDuplicateMaps(final Map<Pair<String, String>, Map<File, String>> duplicates) {
        final Map<String, Map<String, Map<File, String>>> unflattened = new HashMap<>();
        for (final Pair<String, String> classAndInstanceUid : duplicates.keySet()) {
            final String                         classUid  = classAndInstanceUid.getKey();
            final Map<String, Map<File, String>> instances = unflattened.computeIfAbsent(classUid, s -> new HashMap<>());
            instances.put(classAndInstanceUid.getValue(), duplicates.get(classAndInstanceUid));
        }
        return unflattened;
    }

    long _resourceSurveyRequestId;

    Date _surveyDate;

    int _totalEntries;

    int _totalUids;

    int _totalBadFiles;

    int _totalMismatchedFiles;

    int _totalDuplicates;

    int _totalFilesInDuplicates;

    int _totalNonActionableDuplicates;

    int _totalFilesInNonActionableDuplicates;

    Map<String, Set<String>> _uids;

    /**
     * Contains a list of files that couldn't be parsed as DICOM objects.
     */
    List<File> _badFiles;

    /**
     * Contains a map of files whose file names don't match the name generated from the DICOM metadata. The value in the
     * map is the generated file name. There may be other files where the file name doesn't match the generated file name,
     * but that also represent a duplicated UID, so it's presumed one of the other files for that UID does match the
     * generated file name.
     */
    Map<File, String> _mismatchedFiles;

    /**
     * Contains a map of SOP class and instance UIDs to a map of files and generated file names that are identified with
     * each UID. Each UID and file name reference a list of multiple files that contain the same DICOM data. These files
     * are candidates for normalization and culling.
     */
    Map<String, Map<String, Map<File, String>>> _duplicates;

    /**
     * Contains a map of SOP class and instance UIDs to a map of files and generated file names that are identified with
     * each UID. Each UID and file name reference a list of multiple files that contain the same DICOM data. These differ
     * from {@link #getDuplicates()} because the generated file names don't match, meaning the "duplicate" DICOM
     * instances only duplicate their SOP class and instance UIDs, but differ in some other way, usually having
     * different instance numbers. Requests with no duplicates or mismatched files and one or more non-actionable
     * duplicates are given the status {@link ResourceSurveyRequest.Status#NONCOMPLIANT}. Requests with one or more
     * duplicates or mismatched files and one or more non-actionable duplicates are given the status
     * {@link ResourceSurveyRequest.Status#DIVERGENT} but are marked {@link ResourceSurveyRequest.Status#NONCOMPLIANT}
     * once mitigation is completed.
     */
    Map<String, Map<String, Map<File, String>>> _nonActionableDuplicates;
}
