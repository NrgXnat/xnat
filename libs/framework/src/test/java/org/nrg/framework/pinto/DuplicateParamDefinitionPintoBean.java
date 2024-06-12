/*
 * framework: org.nrg.framework.pinto.DuplicateParamDefinitionPintoBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.pinto;

public class DuplicateParamDefinitionPintoBean extends AbstractPintoBean {

    public DuplicateParamDefinitionPintoBean(Object parent, String[] arguments) throws PintoException {
        super(parent, arguments);
    }

    /**
     * Provides an opportunity for subclasses to validate the processed parameters and their arguments.
     *
     * @throws PintoException
     *
     */
    @Override
    public void validate() throws PintoException {
        // Nothing here.
    }

    @Parameter(value = "h", longOption = "help", argCount = ArgCount.StandAlone, help = "This should conflict with the base help parameter and cause an exception.")
    public void setHelp(boolean help) {
        _help = help;
    }

    @Value("h")
    public boolean getHelp() {
        return _help;
    }

    private boolean _help;
}
