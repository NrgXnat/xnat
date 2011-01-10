package org.nrg.xdat.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;

public class Authorizer implements AuthorizerI{

	private static final String READ = "READ";
	private static final String SAVE = "SAVE";

	private Authorizer(){}
	
	public static AuthorizerI getInstance(){
		return new Authorizer();
	}
	
	@Override
	public void authorizeRead(final GenericWrapperElement e, final UserI user) throws Exception {
		if(protectedNamespace.get(READ).contains(e.getType().getForeignPrefix())){
        	if (user!=null && !((XDATUser)user).checkRole("Administrator"))
            {
        		AdminUtils.sendAdminEmail(user,"Unauthorized Admin Data Retrieval Attempt", "Unauthorized retrieval of '" + e.getXSIType() + "' by '" + user.getUsername() + "' prevented.");
        		throw new InvalidPermissionException("Only site administrators can read core documents.");
            }
        }
	}

	private static final Map<String,List<String>> protectedNamespace= 
		new HashMap<String,List<String>>(){{
			put(READ,Arrays.asList(new String[]{XFT.PREFIX}));
			put(SAVE,Arrays.asList(new String[]{XFT.PREFIX,"arc"}));
		}};
	
	@Override
	public void authorizeSave(final GenericWrapperElement e, final UserI user) throws Exception{
		if(protectedNamespace.get(SAVE).contains(e.getType().getForeignPrefix())){
        	if (user!=null && !((XDATUser)user).checkRole("Administrator"))
            {
        		AdminUtils.sendAdminEmail(user,"Unauthorized Admin Modification Attempt", "Unauthorized modification of '" + e.getXSIType() + "' by '" + user.getUsername() + "' prevented.");
        		throw new InvalidPermissionException("Only site administrators can store core documents.");
            }
        }
	}
	
}
