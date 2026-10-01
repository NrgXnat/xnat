/*
 * framework: org.nrg.framework.node.NodeLeader
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.node;

import java.time.Duration;

/**
 * This node's handle on a leader lock registered through
 * {@link NodeLockService#registerLeader(String, NodeLeaderListener)}.
 */
public interface NodeLeader {
    /**
     * The lock name this handle was registered for.
     *
     * @return The lock name.
     */
    String getName();

    /**
     * Indicates whether this node currently holds the leader lock. Periodic work should check this at the start of
     * every run, and long work should check it between steps, because leadership can be lost at any time.
     *
     * @return {@code true} when this node leads.
     */
    boolean isLeader();

    /**
     * Gives up the leader lock so that another node can take it, and doesn't try to take it again until the backoff
     * has passed. {@link NodeLeaderListener#onDemoted(String, String)} is called as for any other loss of
     * leadership. Use this when the leader can't do its work, for example because it can't reach a backend that
     * another node might.
     * <p>
     * If no other node has a lock connection there is nobody to hand over to, and stepping down would only delay
     * recovery by the backoff. In that case the lock is kept and this method returns {@code false}. It also returns
     * {@code false} when this node isn't the leader, or when the lock service is disabled.
     *
     * @param backoff How long to wait before trying to lead again.
     *
     * @return {@code true} when the lock was released.
     */
    boolean stepDown(Duration backoff);
}
