package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.rest.users.DataAccessApi;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.XFTManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class InitializeXftElementsTask extends AbstractInitializingTask {
    @Autowired
    public InitializeXftElementsTask(final DataAccessApi dataAccessApi) {
        _dataAccessApi = dataAccessApi;
    }

    @Override
    public String getTaskName() {
        return "Initialize the XFT elements for the data access API.";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        if (!XFTManager.isComplete()) {
            throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "XFTManager has not yet completed initialization. Deferring execution.");
        }
        try {
            final List<GenericWrapperElement> elements = GenericWrapperElement.GetAllElements(false);
            if (elements.isEmpty()) {
                throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "No elements available yet. Deferring execution.");
            }
            _dataAccessApi.initialize();
        } catch (XFTInitException | ElementNotFoundException e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "XFT threw an error. Please check that out. This will block use of the data access API.", e);
        }
    }

    private final DataAccessApi _dataAccessApi;
}
