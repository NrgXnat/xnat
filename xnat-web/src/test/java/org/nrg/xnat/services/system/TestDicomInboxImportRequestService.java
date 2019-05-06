package org.nrg.xnat.services.system;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.services.SerializerService;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest.Status.*;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestDicomInboxImportRequestServiceConfig.class)
public class TestDicomInboxImportRequestService {
    @Autowired
    public void setDicomInboxImportRequestService(final DicomInboxImportRequestService service) {
        _service = service;
    }

    @Autowired
    public void setSerializerService(final SerializerService serializer) {
        _serializer = serializer;
    }

    @Test
    public void testConfigLoads() {
        log.info("This test method just validates that the configuration for the test class loaded properly. Small victories.");
    }

    @Test
    public void testDicomInboxImportRequestOperations() throws NotFoundException {
        final DicomInboxImportRequest request = DicomInboxImportRequest.builder().username("foo").sessionPath("bar").parameters(PARAMETERS).cleanupAfterImport(false).build();

        assertThat(request.getUsername()).isNotBlank().isEqualTo("foo");
        assertThat(request.getSessionPath()).isNotBlank().isEqualTo("bar");
        assertThat(request.getStatus()).isEqualTo(Queued);
        assertThat(request.getCleanupAfterImport()).isEqualTo(false);
        assertThat(request.getResolution()).isNull();
        assertThat(request.getParameters()).isNotNull().isNotEmpty().containsOnlyKeys(PARAMETERS.keySet().toArray(new String[0])).containsAllEntriesOf(PARAMETERS);

        _service.create(request);

        final DicomInboxImportRequest queued = _service.get(request.getId());

        assertThat(queued).isNotNull();
        assertThat(queued.getUsername()).isNotBlank().isEqualTo("foo");
        assertThat(queued.getSessionPath()).isNotBlank().isEqualTo("bar");
        assertThat(queued.getStatus()).isEqualTo(Queued);
        assertThat(queued.getCleanupAfterImport()).isEqualTo(false);
        assertThat(queued.getResolution()).isNull();
        assertThat(queued.getParameters()).isNotNull().isNotEmpty().containsOnlyKeys(PARAMETERS.keySet().toArray(new String[0])).containsAllEntriesOf(PARAMETERS);
        assertThat(queued).isEqualToIgnoringGivenFields(request, "_created", "_timestamp", "_disabled");

        _service.setToProcessed(queued);

        final DicomInboxImportRequest processed = _service.get(request.getId());

        assertThat(processed).isNotNull();
        assertThat(processed.getUsername()).isNotBlank().isEqualTo("foo");
        assertThat(processed.getSessionPath()).isNotBlank().isEqualTo("bar");
        assertThat(processed.getStatus()).isEqualTo(Processed);
        assertThat(processed.getCleanupAfterImport()).isEqualTo(false);
        assertThat(processed.getResolution()).isNull();
        assertThat(processed.getParameters()).isNotNull().isNotEmpty().containsOnlyKeys(PARAMETERS.keySet().toArray(new String[0])).containsAllEntriesOf(PARAMETERS);
        assertThat(processed).isEqualToIgnoringGivenFields(request, "_created", "_timestamp", "_disabled", "status");

        _service.complete(processed);

        final DicomInboxImportRequest completed = _service.get(request.getId());

        assertThat(completed).isNotNull();
        assertThat(completed.getUsername()).isNotBlank().isEqualTo("foo");
        assertThat(completed.getSessionPath()).isNotBlank().isEqualTo("bar");
        assertThat(completed.getStatus()).isEqualTo(Completed);
        assertThat(completed.getCleanupAfterImport()).isEqualTo(false);
        assertThat(completed.getResolution()).isNull();
        assertThat(completed.getParameters()).isNotNull().isNotEmpty().containsOnlyKeys(PARAMETERS.keySet().toArray(new String[0])).containsAllEntriesOf(PARAMETERS);
        assertThat(completed).isEqualToIgnoringGivenFields(request, "_created", "_timestamp", "_disabled", "status");
    }

    /**
     * There's a known issue with Lombok where fields are initialized differently through the builder and default
     * constructor (fields that are set to a value and annotated with @Builder.Default are set to null in the generated
     * no-args constructor so that they can then be initialized by the builder). This tests that the request class
     * works the same with the builder and default constructor.
     *
     * @throws NotFoundException Thrown if the request object isn't found.
     */
    @Test
    public void testDicomInboxImportRequestOperationWithDefaultConstructor() throws NotFoundException {
        final DicomInboxImportRequest request = new DicomInboxImportRequest();

        // Validate all default values.
        assertThat(request.getUsername()).isNull();
        assertThat(request.getSessionPath()).isNull();
        assertThat(request.getStatus()).isEqualTo(Queued);
        assertThat(request.getCleanupAfterImport()).isEqualTo(true);
        assertThat(request.getResolution()).isNull();
        assertThat(request.getParameters()).isNotNull().isEmpty();

        request.setUsername("foo");
        request.setSessionPath("bar");
        request.setParameters(PARAMETERS);

        _service.create(request);

        final DicomInboxImportRequest queued = _service.get(request.getId());

        assertThat(queued).isNotNull();
        assertThat(queued.getStatus()).isEqualTo(Queued);
        assertThat(queued).isEqualToIgnoringGivenFields(request, "_created", "_timestamp", "_disabled");
    }

    /**
     * There's a known issue with Lombok where fields are initialized differently through the builder and default
     * constructor (fields that are set to a value and annotated with @Builder.Default are set to null in the generated
     * no-args constructor so that they can then be initialized by the builder). This tests that the request class
     * works the same with the builder and default constructor.
     *
     * @throws IOException Thrown if an error occurs during serialization.
     */
    @Test
    public void testSerializationAndDeserialization() throws IOException {
        final DicomInboxImportRequest request = DicomInboxImportRequest.builder().username("foo").sessionPath("bar").parameters(PARAMETERS).build();

        assertThat(request.getUsername()).isNotBlank().isEqualTo("foo");
        assertThat(request.getSessionPath()).isNotBlank().isEqualTo("bar");
        assertThat(request.getStatus()).isEqualTo(Queued);
        assertThat(request.getCleanupAfterImport()).isEqualTo(true);
        assertThat(request.getResolution()).isNull();
        assertThat(request.getParameters()).isNotNull().isNotEmpty().containsOnlyKeys(PARAMETERS.keySet().toArray(new String[0])).containsAllEntriesOf(PARAMETERS);

        _service.create(request);

        final String json = _serializer.toJson(request);
        assertThat(json).isNotBlank();

        final DicomInboxImportRequest deserialized = _serializer.deserializeJson(json, DicomInboxImportRequest.class);

        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getUsername()).isNotBlank().isEqualTo("foo");
        assertThat(deserialized.getSessionPath()).isNotBlank().isEqualTo("bar");
        assertThat(deserialized.getStatus()).isEqualTo(Queued);
        assertThat(deserialized.getCleanupAfterImport()).isEqualTo(true);
        assertThat(deserialized.getResolution()).isNull();
        assertThat(deserialized.getParameters()).isNotNull().isNotEmpty().containsOnlyKeys(PARAMETERS.keySet().toArray(new String[0])).containsAllEntriesOf(PARAMETERS);
        assertThat(deserialized).isEqualToIgnoringGivenFields(request, "_created", "_timestamp", "_disabled");
    }

    private static final Map<String, String> PARAMETERS = ImmutableMap.of("one", "1", "two", "2", "three", "3");

    private DicomInboxImportRequestService _service;
    private SerializerService              _serializer;
}

