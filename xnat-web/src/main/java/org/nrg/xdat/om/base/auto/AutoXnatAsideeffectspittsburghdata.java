/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:09 UTC 2016
 *
 */
package org.nrg.xdat.om.base.auto;
import org.nrg.xft.*;
import org.nrg.xft.security.UserI;
import org.nrg.xdat.om.*;
import org.nrg.xft.utils.ResourceFile;
import org.nrg.xft.exception.*;

import java.util.*;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoXnatAsideeffectspittsburghdata extends XnatSubjectassessordata implements org.nrg.xdat.model.XnatAsideeffectspittsburghdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatAsideeffectspittsburghdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat_a:sideEffectsPittsburghData";

	public AutoXnatAsideeffectspittsburghdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatAsideeffectspittsburghdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatAsideeffectspittsburghdata(UserI user)
	 **/
	public AutoXnatAsideeffectspittsburghdata(){}

	public AutoXnatAsideeffectspittsburghdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat_a:sideEffectsPittsburghData";
	}
	 private org.nrg.xdat.om.XnatSubjectassessordata _Subjectassessordata =null;

	/**
	 * subjectAssessorData
	 * @return org.nrg.xdat.om.XnatSubjectassessordata
	 */
	public org.nrg.xdat.om.XnatSubjectassessordata getSubjectassessordata() {
		try{
			if (_Subjectassessordata==null){
				_Subjectassessordata=((XnatSubjectassessordata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("subjectAssessorData")));
				return _Subjectassessordata;
			}else {
				return _Subjectassessordata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for subjectAssessorData.
	 * @param v Value to Set.
	 */
	public void setSubjectassessordata(ItemI v) throws Exception{
		_Subjectassessordata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/subjectAssessorData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/subjectAssessorData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * subjectAssessorData
	 * set org.nrg.xdat.model.XnatSubjectassessordataI
	 */
	public <A extends org.nrg.xdat.model.XnatSubjectassessordataI> void setSubjectassessordata(A item) throws Exception{
	setSubjectassessordata((ItemI)item);
	}

	/**
	 * Removes the subjectAssessorData.
	 * */
	public void removeSubjectassessordata() {
		_Subjectassessordata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/subjectAssessorData",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Motortics=null;

	/**
	 * @return Returns the motorTics.
	 */
	public Integer getMotortics() {
		try{
			if (_Motortics==null){
				_Motortics=getIntegerProperty("motorTics");
				return _Motortics;
			}else {
				return _Motortics;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for motorTics.
	 * @param v Value to Set.
	 */
	public void setMotortics(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/motorTics",v);
		_Motortics=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _BuccalLingualmovements=null;

	/**
	 * @return Returns the buccal_lingualMovements.
	 */
	public Integer getBuccalLingualmovements() {
		try{
			if (_BuccalLingualmovements==null){
				_BuccalLingualmovements=getIntegerProperty("buccal_lingualMovements");
				return _BuccalLingualmovements;
			}else {
				return _BuccalLingualmovements;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for buccal_lingualMovements.
	 * @param v Value to Set.
	 */
	public void setBuccalLingualmovements(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/buccal_lingualMovements",v);
		_BuccalLingualmovements=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _PickingSkinFingersNailsLip=null;

	/**
	 * @return Returns the picking_skin_fingers_nails_lip.
	 */
	public Integer getPickingSkinFingersNailsLip() {
		try{
			if (_PickingSkinFingersNailsLip==null){
				_PickingSkinFingersNailsLip=getIntegerProperty("picking_skin_fingers_nails_lip");
				return _PickingSkinFingersNailsLip;
			}else {
				return _PickingSkinFingersNailsLip;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for picking_skin_fingers_nails_lip.
	 * @param v Value to Set.
	 */
	public void setPickingSkinFingersNailsLip(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/picking_skin_fingers_nails_lip",v);
		_PickingSkinFingersNailsLip=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _WorriedAnxious=null;

	/**
	 * @return Returns the worried_anxious.
	 */
	public Integer getWorriedAnxious() {
		try{
			if (_WorriedAnxious==null){
				_WorriedAnxious=getIntegerProperty("worried_anxious");
				return _WorriedAnxious;
			}else {
				return _WorriedAnxious;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for worried_anxious.
	 * @param v Value to Set.
	 */
	public void setWorriedAnxious(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/worried_anxious",v);
		_WorriedAnxious=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _DullTiredListless=null;

	/**
	 * @return Returns the dull_tired_listless.
	 */
	public Integer getDullTiredListless() {
		try{
			if (_DullTiredListless==null){
				_DullTiredListless=getIntegerProperty("dull_tired_listless");
				return _DullTiredListless;
			}else {
				return _DullTiredListless;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dull_tired_listless.
	 * @param v Value to Set.
	 */
	public void setDullTiredListless(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dull_tired_listless",v);
		_DullTiredListless=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Sedation=null;

	/**
	 * @return Returns the sedation.
	 */
	public Integer getSedation() {
		try{
			if (_Sedation==null){
				_Sedation=getIntegerProperty("sedation");
				return _Sedation;
			}else {
				return _Sedation;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for sedation.
	 * @param v Value to Set.
	 */
	public void setSedation(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/sedation",v);
		_Sedation=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Stomachache=null;

	/**
	 * @return Returns the stomachache.
	 */
	public Integer getStomachache() {
		try{
			if (_Stomachache==null){
				_Stomachache=getIntegerProperty("stomachache");
				return _Stomachache;
			}else {
				return _Stomachache;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for stomachache.
	 * @param v Value to Set.
	 */
	public void setStomachache(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/stomachache",v);
		_Stomachache=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _NauseaVomiting=null;

	/**
	 * @return Returns the nausea_vomiting.
	 */
	public Integer getNauseaVomiting() {
		try{
			if (_NauseaVomiting==null){
				_NauseaVomiting=getIntegerProperty("nausea_vomiting");
				return _NauseaVomiting;
			}else {
				return _NauseaVomiting;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for nausea_vomiting.
	 * @param v Value to Set.
	 */
	public void setNauseaVomiting(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/nausea_vomiting",v);
		_NauseaVomiting=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _CrabbyIrritable=null;

	/**
	 * @return Returns the crabby_irritable.
	 */
	public Integer getCrabbyIrritable() {
		try{
			if (_CrabbyIrritable==null){
				_CrabbyIrritable=getIntegerProperty("crabby_irritable");
				return _CrabbyIrritable;
			}else {
				return _CrabbyIrritable;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for crabby_irritable.
	 * @param v Value to Set.
	 */
	public void setCrabbyIrritable(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/crabby_irritable",v);
		_CrabbyIrritable=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _TearfulSadDepressed=null;

	/**
	 * @return Returns the tearful_sad_depressed.
	 */
	public Integer getTearfulSadDepressed() {
		try{
			if (_TearfulSadDepressed==null){
				_TearfulSadDepressed=getIntegerProperty("tearful_sad_depressed");
				return _TearfulSadDepressed;
			}else {
				return _TearfulSadDepressed;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tearful_sad_depressed.
	 * @param v Value to Set.
	 */
	public void setTearfulSadDepressed(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tearful_sad_depressed",v);
		_TearfulSadDepressed=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Hallucinations=null;

	/**
	 * @return Returns the hallucinations.
	 */
	public Integer getHallucinations() {
		try{
			if (_Hallucinations==null){
				_Hallucinations=getIntegerProperty("hallucinations");
				return _Hallucinations;
			}else {
				return _Hallucinations;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for hallucinations.
	 * @param v Value to Set.
	 */
	public void setHallucinations(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/hallucinations",v);
		_Hallucinations=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Troublesleeping=null;

	/**
	 * @return Returns the troubleSleeping.
	 */
	public Integer getTroublesleeping() {
		try{
			if (_Troublesleeping==null){
				_Troublesleeping=getIntegerProperty("troubleSleeping");
				return _Troublesleeping;
			}else {
				return _Troublesleeping;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for troubleSleeping.
	 * @param v Value to Set.
	 */
	public void setTroublesleeping(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/troubleSleeping",v);
		_Troublesleeping=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Lossofappetite=null;

	/**
	 * @return Returns the lossOfAppetite.
	 */
	public Integer getLossofappetite() {
		try{
			if (_Lossofappetite==null){
				_Lossofappetite=getIntegerProperty("lossOfAppetite");
				return _Lossofappetite;
			}else {
				return _Lossofappetite;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for lossOfAppetite.
	 * @param v Value to Set.
	 */
	public void setLossofappetite(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/lossOfAppetite",v);
		_Lossofappetite=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _TroubleconcentratingDistractible=null;

	/**
	 * @return Returns the troubleConcentrating_distractible.
	 */
	public Integer getTroubleconcentratingDistractible() {
		try{
			if (_TroubleconcentratingDistractible==null){
				_TroubleconcentratingDistractible=getIntegerProperty("troubleConcentrating_distractible");
				return _TroubleconcentratingDistractible;
			}else {
				return _TroubleconcentratingDistractible;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for troubleConcentrating_distractible.
	 * @param v Value to Set.
	 */
	public void setTroubleconcentratingDistractible(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/troubleConcentrating_distractible",v);
		_TroubleconcentratingDistractible=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _DizzinessLightheadedness=null;

	/**
	 * @return Returns the dizziness_lightheadedness.
	 */
	public Integer getDizzinessLightheadedness() {
		try{
			if (_DizzinessLightheadedness==null){
				_DizzinessLightheadedness=getIntegerProperty("dizziness_lightheadedness");
				return _DizzinessLightheadedness;
			}else {
				return _DizzinessLightheadedness;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dizziness_lightheadedness.
	 * @param v Value to Set.
	 */
	public void setDizzinessLightheadedness(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dizziness_lightheadedness",v);
		_DizzinessLightheadedness=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Headaches=null;

	/**
	 * @return Returns the headaches.
	 */
	public Integer getHeadaches() {
		try{
			if (_Headaches==null){
				_Headaches=getIntegerProperty("headaches");
				return _Headaches;
			}else {
				return _Headaches;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for headaches.
	 * @param v Value to Set.
	 */
	public void setHeadaches(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/headaches",v);
		_Headaches=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Socialwithdrawal=null;

	/**
	 * @return Returns the socialWithdrawal.
	 */
	public Integer getSocialwithdrawal() {
		try{
			if (_Socialwithdrawal==null){
				_Socialwithdrawal=getIntegerProperty("socialWithdrawal");
				return _Socialwithdrawal;
			}else {
				return _Socialwithdrawal;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for socialWithdrawal.
	 * @param v Value to Set.
	 */
	public void setSocialwithdrawal(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/socialWithdrawal",v);
		_Socialwithdrawal=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Drymouth=null;

	/**
	 * @return Returns the dryMouth.
	 */
	public Integer getDrymouth() {
		try{
			if (_Drymouth==null){
				_Drymouth=getIntegerProperty("dryMouth");
				return _Drymouth;
			}else {
				return _Drymouth;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dryMouth.
	 * @param v Value to Set.
	 */
	public void setDrymouth(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dryMouth",v);
		_Drymouth=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Palpitations=null;

	/**
	 * @return Returns the palpitations.
	 */
	public Integer getPalpitations() {
		try{
			if (_Palpitations==null){
				_Palpitations=getIntegerProperty("palpitations");
				return _Palpitations;
			}else {
				return _Palpitations;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for palpitations.
	 * @param v Value to Set.
	 */
	public void setPalpitations(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/palpitations",v);
		_Palpitations=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatAsideeffectspittsburghdata> getAllXnatAsideeffectspittsburghdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAsideeffectspittsburghdata> al = new ArrayList<org.nrg.xdat.om.XnatAsideeffectspittsburghdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAsideeffectspittsburghdata> getXnatAsideeffectspittsburghdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAsideeffectspittsburghdata> al = new ArrayList<org.nrg.xdat.om.XnatAsideeffectspittsburghdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAsideeffectspittsburghdata> getXnatAsideeffectspittsburghdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAsideeffectspittsburghdata> al = new ArrayList<org.nrg.xdat.om.XnatAsideeffectspittsburghdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatAsideeffectspittsburghdata getXnatAsideeffectspittsburghdatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat_a:sideEffectsPittsburghData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatAsideeffectspittsburghdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static ArrayList wrapItems(ArrayList items)
	{
		ArrayList al = new ArrayList();
		al = org.nrg.xdat.base.BaseElement.WrapItems(items);
		al.trimToSize();
		return al;
	}

	public static ArrayList wrapItems(org.nrg.xft.collections.ItemCollection items)
	{
		return wrapItems(items.getItems());
	}

	public org.w3c.dom.Document toJoinedXML() throws Exception
	{
		ArrayList al = new ArrayList();
		al.add(this.getItem());
		al.add(org.nrg.xft.search.ItemSearch.GetItem("xnat:subjectData.ID",this.getItem().getProperty("xnat:mrSessionData.subject_ID"),getItem().getUser(),false));
		al.trimToSize();
		return org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter.ItemListToDOM(al);
	}
	public ArrayList<ResourceFile> getFileResources(String rootPath, boolean preventLoop){
ArrayList<ResourceFile> _return = new ArrayList<ResourceFile>();
	 boolean localLoop = preventLoop;
	        localLoop = preventLoop;
	
	        //subjectAssessorData
	        XnatSubjectassessordata childSubjectassessordata = (XnatSubjectassessordata)this.getSubjectassessordata();
	            if (childSubjectassessordata!=null){
	              for(ResourceFile rf: ((XnatSubjectassessordata)childSubjectassessordata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("subjectAssessorData[" + ((XnatSubjectassessordata)childSubjectassessordata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("subjectAssessorData/" + ((XnatSubjectassessordata)childSubjectassessordata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
