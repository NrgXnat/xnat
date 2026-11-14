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
 * The event tracking service builds a batch's log from its workflows each time it's read. With the size recorded at
 * launch, the total is the number of targets and the steps are each target's commands. Without it, the total is the
 * number of workflows saved so far, so a batch polled before its first workflow is saved has a total of 0.
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

    @Test
    public void anOrchestrationBetweenStepsIsNotComplete() {
        final BulkLaunchLog batch = orchestrationLogOf(2, 2, workflow(1, "SESSION_1", COMPLETE), workflow(2, "SESSION_2", COMPLETE));

        assertThat(batch.bulkLaunchComplete()).isFalse();
        assertThat(batch.bulkLaunchSuccess()).isFalse();
    }

    @Test
    public void anOrchestrationWhoseTargetsRanEveryStepSucceeded() {
        final BulkLaunchLog batch = orchestrationLogOf(2, 2, workflow(1, "SESSION_1", COMPLETE), workflow(2, "SESSION_2", COMPLETE),
                                                       workflow(3, "SESSION_1", COMPLETE), workflow(4, "SESSION_2", COMPLETE));

        assertThat(batch.bulkLaunchComplete()).isTrue();
        assertThat(batch.bulkLaunchSuccess()).isTrue();
        assertThat(batch.bulkLaunchMessage()).isEqualTo("All jobs succeeded");
    }

    @Test
    public void anOrchestrationTargetThatFailedAStepIsDone() {
        final BulkLaunchLog batch = orchestrationLogOf(2, 2, workflow(1, "SESSION_1", FAILED), workflow(2, "SESSION_2", COMPLETE),
                                                       workflow(3, "SESSION_2", COMPLETE));

        assertThat(batch.bulkLaunchComplete()).isTrue();
        assertThat(batch.bulkLaunchSuccess()).isFalse();
        assertThat(batch.bulkLaunchMessage()).isEqualTo("1 jobs succeed / 1 jobs failed");
    }

    @Test
    public void aBatchWithTargetsNotYetSavedIsNotComplete() {
        final BulkLaunchLog batch = orchestrationLogOf(3, 1, workflow(1, "SESSION_1", COMPLETE), workflow(2, "SESSION_2", COMPLETE));

        assertThat(batch.bulkLaunchComplete()).isFalse();
        assertThat(batch.bulkLaunchSuccess()).isFalse();
    }

    // Builds the log as EventTrackingDataServiceImpl does with a recorded size: set the steps, add each saved workflow,
    // then set the total to the number of targets.
    private static BulkLaunchLog orchestrationLogOf(final int targets, final int steps, final PersistentWorkflowI... workflows) {
        final BulkLaunchLog batch = new BulkLaunchLog();
        batch.setSteps(steps);
        for (final PersistentWorkflowI workflow : workflows) {
            batch.addOrUpdateWorkflow(workflow);
        }
        batch.setTotal(targets);
        return batch;
    }

    private static PersistentWorkflowI workflow(final int id, final String itemId, final String status) {
        final PersistentWorkflowI workflow = mock(PersistentWorkflowI.class);
        when(workflow.getWorkflowId()).thenReturn(id);
        when(workflow.getId()).thenReturn(itemId);
        when(workflow.getStatus()).thenReturn(status);
        return workflow;
    }

    // Builds the log as EventTrackingDataServiceImpl does: add each saved workflow, then set the total to the count.
    private static BulkLaunchLog logOf(final String... statuses) {
        final BulkLaunchLog batch = new BulkLaunchLog();
        for (int index = 0; index < statuses.length; index++) {
            batch.addOrUpdateWorkflow(workflow(index + 1, "SESSION_" + (index + 1), statuses[index]));
        }
        batch.setTotal(statuses.length);
        return batch;
    }
}
