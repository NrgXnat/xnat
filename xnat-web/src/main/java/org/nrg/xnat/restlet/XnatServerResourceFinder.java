package org.nrg.xnat.restlet;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

/**
 * Restlet 2.x {@link Finder} that instantiates resources via XNAT's legacy
 * {@code (Context, Request, Response)} constructor rather than the 2.x convention of a no-arg
 * constructor followed by {@code init()}.
 *
 * <p>In Restlet 1.1 the {@code Finder}/{@code Router} created resources through the three-argument
 * constructor; in 2.x the default {@code Finder} requires a public no-arg constructor. XNAT's ~45
 * {@code SecureResource} subclasses all use the 1.1-style constructor, so this finder preserves that
 * contract and lets them stay unchanged. The three-argument {@code SecureResource} constructor calls
 * {@code init(context, request, response)} itself; the standard {@code Finder.handle()} will call
 * {@code init()} once more, which is idempotent (XNAT resources override no {@code doInit()}).
 */
public class XnatServerResourceFinder extends Finder {

    public XnatServerResourceFinder(final Context context, final Class<? extends ServerResource> targetClass) {
        super(context, targetClass);
    }

    @Override
    public ServerResource find(final Request request, final Response response) {
        final Class<? extends ServerResource> targetClass = getTargetClass();
        try {
            return targetClass.getConstructor(Context.class, Request.class, Response.class)
                              .newInstance(getContext(), request, response);
        } catch (NoSuchMethodException e) {
            // Resource doesn't use the legacy constructor; fall back to the 2.x no-arg + init() path.
            return super.find(request, response);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // XNAT resources reject requests from the constructor by throwing ResourceException
            // with a meaningful status (403/400/...). Restlet 1.1 honored it; the 2.x Finder maps a
            // null find() result to 404, so propagate and let the status service answer with the
            // embedded status instead of rewriting the rejection to 404.
            if (e.getCause() instanceof org.restlet.resource.ResourceException) {
                throw (org.restlet.resource.ResourceException) e.getCause();
            }
            logMaskedFailure(targetClass, e.getCause());
            return null;
        } catch (Exception e) {
            logMaskedFailure(targetClass, e);
            return null;
        }
    }

    /**
     * Logs a resource-construction failure that {@link #find} is about to turn into a bare <b>404</b>.
     *
     * <p>Returning {@code null} from {@code find()} makes Restlet answer 404, so a resource that rejects a
     * request from its constructor reports "not found" instead of the real reason — which reads as a routing
     * or registration bug and sends investigation in the wrong direction. See status doc items 1-24 / 1-34,
     * where this masked a lost request body for weeks.
     *
     * <p>The inherited {@link #getLogger()} is a {@code java.util.logging} logger, so on Tomcat its output
     * lands in {@code localhost.<date>.log} — not {@code catalina.out} and not XNAT's own logs, so nobody
     * greps it. This mirrors the message onto the XNAT SLF4J pipeline where people actually look, and states
     * the consequence and the remedy explicitly.
     *
     * <p><b>Resource authors:</b> throw {@link org.restlet.resource.ResourceException} with a real status
     * (400/403/405/…) from a constructor — it is propagated untouched. Anything else becomes this 404.
     *
     * <p>The 404 itself is deliberately left as-is: changing an externally-observable error status is a
     * behavioral change the develop-calibrated REST suite asserts on, so it must be paired with test updates
     * rather than bundled into migration work.
     */
    private void logMaskedFailure(final Class<? extends ServerResource> targetClass, final Throwable cause) {
        getLogger().log(Level.WARNING, "Unable to instantiate resource " + targetClass.getName(), cause);
        log.error("Could not construct Restlet resource {} — the client will receive a misleading 404 "
                  + "(Restlet renders a null Finder result as \"not found\"). This is NOT a routing problem: the "
                  + "route matched and the resource was selected. Throw a ResourceException with a real status "
                  + "from the constructor to surface the actual reason instead.", targetClass.getName(), cause);
    }

    private static final Logger log = LoggerFactory.getLogger(XnatServerResourceFinder.class);
}
