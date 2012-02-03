package org.nrg.xdat.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
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

	/* (non-Javadoc)
	 * User is authorized if:
	 *  the datatype is specifically marked as unsecured
	 *  the user is an administrator
	 *  or the data type is otherwise secured.
	 * @see org.nrg.xdat.security.AuthorizerI#authorizeSave(org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement, org.nrg.xft.security.UserI)
	 * 
	 */
	public void authorizeRead(final XFTItem e, final UserI user) throws Exception {
		authorize(READ,e,user);
	}

	private static final Map<String,List<String>> protectedNamespace= 
		new HashMap<String,List<String>>(){{
			put(READ,Arrays.asList(new String[]{XFT.PREFIX}));
			put(SAVE,Arrays.asList(new String[]{XFT.PREFIX,"arc"}));
		}};

		private static final Map<String,List<String>> unsecured= 
			new HashMap<String,List<String>>(){{
				put(READ,Arrays.asList("wrk:workflowData",XdatStoredSearch.SCHEMA_ELEMENT_NAME));
				put(SAVE,Arrays.asList("wrk:workflowData",XdatStoredSearch.SCHEMA_ELEMENT_NAME));
			}};

	
	/* (non-Javadoc)
	 * User is authorized if:
	 *  the datatype is specificalloy marked as unsecured
	 *  the user is an administrator
	 *  or the data type is otherwise secured.
	 * @see org.nrg.xdat.security.AuthorizerI#authorizeSave(org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement, org.nrg.xft.security.UserI)
	 * 
	 */
	public void authorizeSave(final XFTItem e, final UserI user) throws Exception{
		authorize(SAVE,e,user);
	}
	
	public void authorize(String action,final GenericWrapperElement e, final UserI user) throws Exception{
		if (!unsecured.get(action).contains(e.getXSIType()) && (user==null || !((XDATUser)user).checkRole("Administrator")))
        {
			if(protectedNamespace.get(action).contains(e.getType().getForeignPrefix())){
	    		AdminUtils.sendAdminEmail(user,"Unauthorized Admin Data Access Attempt", "Unauthorized access of '" + e.getXSIType() + "' by '" + ((user==null)?null:user.getUsername()) + "' prevented.");
	    		throw new InvalidPermissionException("Only site administrators can read core documents.");
	        }else if(!ElementSecurity.IsSecureElement(e.getXSIType())){
	        	AdminUtils.sendAdminEmail(user,"Unauthorized Data Access Attempt", "Unauthorized access of '" + e.getXSIType() + "' by '" + ((user==null)?null:user.getUsername()) + "' prevented.");
	    		throw new InvalidPermissionException("Unsecured data access attempt");
	        }
        }
	}
	
	public void authorize(String action,final XFTItem e, final UserI user) throws Exception{
		if (!unsecured.get(action).contains(e.getXSIType()) && (user==null || !((XDATUser)user).checkRole("Administrator")))
        {
			authorize(action,e.getGenericSchemaElement(),user);
			if(ElementSecurity.IsSecureElement(e.getXSIType())){
	        	if(!user.canEdit(e)){
	        		AdminUtils.sendAdminEmail(user,"Unauthorized Data Retrieval Attempt", "Unauthorized access of '" + e.getXSIType() + "' by '" + ((user==null)?null:user.getUsername()) + "' prevented.");
		    		throw new InvalidPermissionException("Unsecured data Access attempt");
		        }
	        }
        }
	}

	/* (non-Javadoc)
	 * User is authorized if:
	 *  the datatype is specificalloy marked as unsecured
	 *  the user is an administrator
	 *  or the data type is otherwise secured.
	 * @see org.nrg.xdat.security.AuthorizerI#authorizeSave(org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement, org.nrg.xft.security.UserI)
	 * 
	 */
	public void authorizeDelete(final XFTItem e, final UserI user) throws Exception{
		authorize(SAVE,e,user);
	}

	public void authorizeRead(GenericWrapperElement e, UserI user)
			throws Exception {
		authorize(READ,e,user);
	}

	public void authorizeSave(GenericWrapperElement e, UserI user)
			throws Exception {
		authorize(SAVE,e,user);
	}

	public void authorizeDelete(GenericWrapperElement e, UserI user)
			throws Exception {
		authorize(SAVE,e,user);
	}
	
}
