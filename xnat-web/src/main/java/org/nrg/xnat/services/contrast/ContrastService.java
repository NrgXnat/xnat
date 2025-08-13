package org.nrg.xnat.services.contrast;

import org.nrg.xnat.contrast.model.ContrastBolus;
import org.nrg.xft.security.UserI;

import java.util.List;

public interface ContrastService {
    public void save(final ContrastBolus contrast, UserI user, final boolean isNewContrast) throws Exception;
    public void delete(final ContrastBolus contrast, UserI user) throws Exception;
    public List<ContrastBolus> findContrast(final String sessionId, UserI user) throws Exception;
}
