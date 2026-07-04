package org.nrg.xnat.restlet;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

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
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Unable to instantiate resource " + targetClass.getName(), e);
            return null;
        }
    }
}
