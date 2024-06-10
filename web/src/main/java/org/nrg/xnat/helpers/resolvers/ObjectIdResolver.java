package org.nrg.xnat.helpers.resolvers;

import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xft.ItemI;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;

/**
 * Resolvers provide a generic solution to resolving the ID of a particular object in XFT. It's meant to wrap a query
 * that can locate the ID based on the values stored in the embedded parameters object.
 *
 * @param <P> An object that builds an instance of <b>SqlParameterSource</b> that feeds the resolution.
 */
public interface ObjectIdResolver<P extends ObjectIdResolver.Parameters> {
    /**
     * Provides the query parameters for the SQL query run by the {@link #resolve(Parameters)} method.
     */
    interface Parameters {
        SqlParameterSource getParameterSource();
    }

    /**
     * Indicates the types of objects the resolver can identify.
     *
     * @return A list of classes representing the types of objects the resolver can identify.
     */
    List<Class<? extends ItemI>> identifies();

    /**
     * Indicates the types of parameters that can be submitted together to identify an object. For example, a resolver
     * might identify a subject with the following criteria:
     *
     * <ul>
     *     <li>xnat:projectData/ID, xnat:subjectData/ID</li>
     *     <li>xnat:projectData/ID, xnat:subjectData/label</li>
     *     <li>xnat:subjectData/ID</li>
     * </ul>
     *
     * @return A list containing combinations of accepted parameters
     */
    List<List<String>> accepts();

    /**
     * Uses the values in <b>parameters</b> to try and resolve an XFT object ID.
     *
     * @param parameters An instance of the associated {@link ObjectIdResolver.Parameters} interface implementation.
     *
     * @return The resolved object ID.
     *
     * @throws DataFormatException When not enough context is provided to resolve the object ID/
     * @throws NotFoundException When no existing objects match the specified parameters.
     */
    String resolve(final P parameters) throws DataFormatException, NotFoundException;
}
