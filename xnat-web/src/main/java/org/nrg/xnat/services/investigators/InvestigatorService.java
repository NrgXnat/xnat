package org.nrg.xnat.services.investigators;

import org.nrg.xapi.exceptions.*;
import org.nrg.xapi.model.xft.Investigator;
import org.nrg.xft.exception.XftItemException;
import org.nrg.xft.security.UserI;

import java.util.List;

/**
 * Manages {@link Investigator investigator instances} in XNAT.
 */
public interface InvestigatorService {
    /**
     * Creates an investigator in the system from the submitted instance.
     *
     * @param investigator The investigator to be created.
     * @param user         The user creating the investigator.
     *
     * @return The newly created investigator.
     *
     * @throws XftItemException               When an error occurs trying to create the investigator.
     * @throws ResourceAlreadyExistsException If an investigator with the same attributes already exists.
     */
    Investigator createInvestigator(Investigator investigator, UserI user) throws ResourceAlreadyExistsException, DataFormatException, XftItemException;

    /**
     * Tests whether an investigator with the specified ID exists.
     *
     * @param investigatorId The ID of the investigator to test.
     *
     * @return Returns <b>true</b> if an investigator with the specified ID exists, <b>false</b> otherwise.
     */
    boolean exists(final int investigatorId);

    /**
     * Tests whether an investigator with the specified first and last names exists.
     *
     * @param firstName The first name of the investigator to test.
     * @param lastName  The last name of the investigator to test.
     *
     * @return Returns <b>true</b> if an investigator with the specified first and last names exists, <b>false</b> otherwise.
     */
    boolean exists(final String firstName, final String lastName);

    /**
     * Gets the investigator with the submitted ID.
     *
     * @param investigatorId The ID of the investigator to retrieve.
     *
     * @return The requested investigator.
     *
     * @throws NotFoundException If no investigator with the indicated ID exists on the system.
     */
    Investigator getInvestigator(int investigatorId) throws NotFoundException;

    /**
     * Gets the investigator with the submitted first and last names.
     *
     * @param firstName The first name of the investigator to retrieve.
     * @param lastName  The last name of the investigator to retrieve.
     *
     * @return The requested investigator.
     *
     * @throws NotFoundException If no investigator with the indicated first and last names exists on the system.
     */
    Investigator getInvestigator(String firstName, String lastName) throws NotFoundException;

    /**
     * Gets a list of all of the investigators on the system.
     *
     * @return A list of all of the currently configured investigators on the system.
     */
    List<Investigator> getInvestigators();

    /**
     * Updates the investigator with the submitted ID using the data in the investigator object. Note that any fields that contain nulls are not considered. This means you can change just the
     * first name of the investigator by setting a value for that property then leaving (or setting) the other fields to null. If the investigator is not modified (i.e. there are no changes in the
     * properties associated with the investigator), this method returns null. Otherwise, it returns the updated investigator object.
     *
     * @param investigatorId The ID of the investigator to update.
     * @param investigator   The investigator object with the properties to be set.
     * @param user           The user requesting the changes to the investigator.
     *
     * @return The updated investigator object if modified, null otherwise.
     *
     * @throws NotFoundException       When an investigator with the indicated ID can't be found.
     * @throws InitializationException When the updated investigator failed to save without throwing an exception.
     * @throws XftItemException        When an error occurs trying to update the investigator.
     */
    Investigator updateInvestigator(int investigatorId, Investigator investigator, UserI user) throws NotFoundException, InitializationException, XftItemException;

    /**
     * Deletes the investigator with the submitted ID.
     *
     * @param investigatorId The ID of the investigator to delete.
     * @param user           The user requesting the changes to the investigator.
     *
     * @throws InsufficientPrivilegesException If the user isn't an administrator.
     * @throws NotFoundException               When an investigator with the indicated ID can't be found.
     * @throws XftItemException                When an error occurs trying to update the investigator.
     */
    void deleteInvestigator(int investigatorId, UserI user) throws InsufficientPrivilegesException, NotFoundException, XftItemException;
}
