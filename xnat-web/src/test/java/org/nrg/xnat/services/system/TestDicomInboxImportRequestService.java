package org.nrg.xnat.services.system;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.services.SerializerService;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.HashMap;
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
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("one", "1");
        parameters.put("two", "2");
        parameters.put("three", "3");

        final DicomInboxImportRequest request = DicomInboxImportRequest.builder().username("foo").sessionPath("bar").parameters(parameters).build();

        _service.create(request);

        final DicomInboxImportRequest queued = _service.get(request.getId());

        assertThat(queued).isNotNull();
        assertThat(queued.getStatus()).isEqualTo(Queued);
        assertThat(queued).isEqualToIgnoringGivenFields(request, "_created", "_timestamp", "_disabled");

        _service.setToProcessed(request);

        final DicomInboxImportRequest processed = _service.get(request.getId());

        assertThat(processed).isNotNull();
        assertThat(processed.getStatus()).isEqualTo(Processed);
        assertThat(processed).isEqualToIgnoringGivenFields(request, "_created", "_timestamp", "_disabled");

        _service.setToCompleted(request);

        final DicomInboxImportRequest completed = _service.get(request.getId());

        assertThat(completed).isNotNull();
        assertThat(completed.getStatus()).isEqualTo(Completed);
        assertThat(completed).isEqualToIgnoringGivenFields(request, "_created", "_timestamp", "_disabled");
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
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("one", "1");
        parameters.put("two", "2");
        parameters.put("three", "3");

        final DicomInboxImportRequest request = new DicomInboxImportRequest();
        request.setUsername("foo");
        request.setSessionPath("bar");
        request.setParameters(parameters);

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
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("one", "1");
        parameters.put("two", "2");
        parameters.put("three", "3");

        final DicomInboxImportRequest request = DicomInboxImportRequest.builder().username("foo").sessionPath("bar").parameters(parameters).build();

        _service.create(request);

        final String json = _serializer.toJson(request);
        assertThat(json).isNotBlank();

        final DicomInboxImportRequest deserialized = _serializer.deserializeJson(json, DicomInboxImportRequest.class);

        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getStatus()).isEqualTo(Queued);
        assertThat(deserialized).isEqualToIgnoringGivenFields(request, "_created", "_timestamp", "_disabled");

    }

    private DicomInboxImportRequestService _service;
    private SerializerService              _serializer;
}

