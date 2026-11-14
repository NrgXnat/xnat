package org.nrg.xnat.event.model;

import org.junit.Test;
import org.nrg.xft.event.persist.PersistentWorkflowI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.nrg.xft.event.persist.PersistentWorkflowUtils.COMPLETE;
import static org.nrg.xft.event.persist.PersistentWorkflowUtils.FAILED;
import static org.nrg.xft.event.persist.PersistentWorkflowUtils.RUNNING;

/**
 * The event tracking service builds a batch's log from the workflows saved so far and sets the total to the number it
 * found, so a batch polled before its first workflow is saved has a total of 0.
 */
public class BulkLaunchLogTest {
    @Test
    public void aBatchWithNoWorkflowsYetIsNotComplete() {
        final BulkLaunchLog batch = logOf();

        assertThat(batch.bulkLaunchComplete()).isFalse();
        assertThat(batch.bulkLaunchSuccess()).isFalse();
        assertThat(batch.bulkLaunchMessage()).isNull();
    }

    @Test
    public void aBatchWithoutATotalIsNotComplete() {
        final BulkLaunchLog batch = new BulkLaunchLog();

        assertThat(batch.bulkLaunchComplete()).isFalse();
        assertThat(batch.bulkLaunchSuccess()).isFalse();
        assertThat(batch.bulkLaunchMessage()).isNull();
    }

    @Test
    public void aBatchWithAWorkflowStillRunningIsNotComplete() {
        final BulkLaunchLog batch = logOf(COMPLETE, RUNNING);

        assertThat(batch.bulkLaunchComplete()).isFalse();
        assertThat(batch.bulkLaunchSuccess()).isFalse();
    }

    @Test
    public void aBatchWhoseWorkflowsAllCompletedSucceeded() {
        final BulkLaunchLog batch = logOf(COMPLETE, COMPLETE);

        assertThat(batch.bulkLaunchComplete()).isTrue();
        assertThat(batch.bulkLaunchSuccess()).isTrue();
        assertThat(batch.bulkLaunchMessage()).isEqualTo("All jobs succeeded");
    }

    @Test
    public void aBatchWhoseWorkflowsAllFailedFailed() {
        final BulkLaunchLog batch = logOf(FAILED, FAILED + " (Staging)");

        assertThat(batch.bulkLaunchComplete()).isTrue();
        assertThat(batch.bulkLaunchSuccess()).isFalse();
        assertThat(batch.bulkLaunchMessage()).isEqualTo("All jobs failed");
    }

    @Test
    public void aBatchWithSuccessesAndFailuresFailed() {
        final BulkLaunchLog batch = logOf(COMPLETE, FAILED);

        assertThat(batch.bulkLaunchComplete()).isTrue();
        assertThat(batch.bulkLaunchSuccess()).isFalse();
        assertThat(batch.bulkLaunchMessage()).isEqualTo("1 jobs succeed / 1 jobs failed");
    }

    // Builds the log as EventTrackingDataServiceImpl does: add each saved workflow, then set the total to the count.
    private static BulkLaunchLog logOf(final String... statuses) {
        final BulkLaunchLog batch = new BulkLaunchLog();
        for (int index = 0; index < statuses.length; index++) {
            final PersistentWorkflowI workflow = mock(PersistentWorkflowI.class);
            when(workflow.getWorkflowId()).thenReturn(index + 1);
            when(workflow.getId()).thenReturn("SESSION_" + (index + 1));
            when(workflow.getStatus()).thenReturn(statuses[index]);
            batch.addOrUpdateWorkflow(workflow);
        }
        batch.setTotal(statuses.length);
        return batch;
    }
}
