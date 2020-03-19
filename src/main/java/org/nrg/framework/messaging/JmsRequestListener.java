package org.nrg.framework.messaging;

/**
 * Declares this class to be a listener for JMS requests. The parameterized type <b>&lt;T&gt;</b> indicates
 * the class containing the JMS request that is passed to the {@link #onRequest(Object)} method.
 *
 * @param <T> The class containing the JMS request that is handled by the {@link #onRequest(Object)} method.
 */
public interface JmsRequestListener<T> {
    /**
     * Handles incoming JMS requests of type <b>T</b>.
     *
     * @param request The request object.
     *
     * @throws Exception When an error occurs processing the request.
     */
    @SuppressWarnings("unused")
    void onRequest(final T request) throws Exception;
}
