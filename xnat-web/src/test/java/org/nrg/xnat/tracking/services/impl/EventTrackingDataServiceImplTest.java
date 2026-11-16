package org.nrg.xnat.tracking.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.preferences.AsyncOperationsPreferences;
import org.nrg.xnat.tracking.entities.EventTrackingData;
import org.nrg.xnat.tracking.services.EventTrackingDataHibernateService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventTrackingDataServiceImplTest {
    @Test
    public void recordingABulkLaunchStoresItsSizeOnItsKey() throws Exception {
        final EventTrackingDataHibernateService storage = mock(EventTrackingDataHibernateService.class);
        final EventTrackingData record = new EventTrackingData("bulk-admin1", 7);
        when(storage.createOrRestartWithKey("bulk-admin1", 7)).thenReturn(record);
        final UserI user = mock(UserI.class);
        when(user.getID()).thenReturn(7);
        final ObjectMapper mapper = new ObjectMapper();

        new EventTrackingDataServiceImpl(storage, mock(AsyncOperationsPreferences.class), null, mapper).recordBulkLaunch("bulk-admin1", user, 5, 2);

        verify(storage).update(record);
        assertThat(mapper.readTree(record.getPayload()).path("total").asInt()).isEqualTo(5);
        assertThat(mapper.readTree(record.getPayload()).path("steps").asInt()).isEqualTo(2);
    }
}
