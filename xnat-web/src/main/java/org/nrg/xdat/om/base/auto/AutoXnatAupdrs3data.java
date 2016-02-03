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
public abstract class AutoXnatAupdrs3data extends XnatSubjectassessordata implements org.nrg.xdat.model.XnatAupdrs3dataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatAupdrs3data.class);
	public static String SCHEMA_ELEMENT_NAME="xnat_a:updrs3Data";

	public AutoXnatAupdrs3data(ItemI item)
	{
		super(item);
	}

	public AutoXnatAupdrs3data(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatAupdrs3data(UserI user)
	 **/
	public AutoXnatAupdrs3data(){}

	public AutoXnatAupdrs3data(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat_a:updrs3Data";
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

	private Boolean _Inscanner=null;

	/**
	 * @return Returns the inScanner.
	 */
	public Boolean getInscanner() {
		try{
			if (_Inscanner==null){
				_Inscanner=getBooleanProperty("inScanner");
				return _Inscanner;
			}else {
				return _Inscanner;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for inScanner.
	 * @param v Value to Set.
	 */
	public void setInscanner(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/inScanner",v);
		_Inscanner=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Clicker_right=null;

	/**
	 * @return Returns the clicker/right.
	 */
	public Integer getClicker_right() {
		try{
			if (_Clicker_right==null){
				_Clicker_right=getIntegerProperty("clicker/right");
				return _Clicker_right;
			}else {
				return _Clicker_right;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for clicker/right.
	 * @param v Value to Set.
	 */
	public void setClicker_right(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/clicker/right",v);
		_Clicker_right=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Clicker_left=null;

	/**
	 * @return Returns the clicker/left.
	 */
	public Integer getClicker_left() {
		try{
			if (_Clicker_left==null){
				_Clicker_left=getIntegerProperty("clicker/left");
				return _Clicker_left;
			}else {
				return _Clicker_left;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for clicker/left.
	 * @param v Value to Set.
	 */
	public void setClicker_left(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/clicker/left",v);
		_Clicker_left=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Clicker_interval=null;

	/**
	 * @return Returns the clicker/interval.
	 */
	public Integer getClicker_interval() {
		try{
			if (_Clicker_interval==null){
				_Clicker_interval=getIntegerProperty("clicker/interval");
				return _Clicker_interval;
			}else {
				return _Clicker_interval;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for clicker/interval.
	 * @param v Value to Set.
	 */
	public void setClicker_interval(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/clicker/interval",v);
		_Clicker_interval=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Tremorrest_face=null;

	/**
	 * @return Returns the tremorRest/face.
	 */
	public String getTremorrest_face(){
		try{
			if (_Tremorrest_face==null){
				_Tremorrest_face=getStringProperty("tremorRest/face");
				return _Tremorrest_face;
			}else {
				return _Tremorrest_face;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tremorRest/face.
	 * @param v Value to Set.
	 */
	public void setTremorrest_face(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tremorRest/face",v);
		_Tremorrest_face=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Tremorrest_rue=null;

	/**
	 * @return Returns the tremorRest/rue.
	 */
	public String getTremorrest_rue(){
		try{
			if (_Tremorrest_rue==null){
				_Tremorrest_rue=getStringProperty("tremorRest/rue");
				return _Tremorrest_rue;
			}else {
				return _Tremorrest_rue;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tremorRest/rue.
	 * @param v Value to Set.
	 */
	public void setTremorrest_rue(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tremorRest/rue",v);
		_Tremorrest_rue=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Tremorrest_lue=null;

	/**
	 * @return Returns the tremorRest/lue.
	 */
	public String getTremorrest_lue(){
		try{
			if (_Tremorrest_lue==null){
				_Tremorrest_lue=getStringProperty("tremorRest/lue");
				return _Tremorrest_lue;
			}else {
				return _Tremorrest_lue;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tremorRest/lue.
	 * @param v Value to Set.
	 */
	public void setTremorrest_lue(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tremorRest/lue",v);
		_Tremorrest_lue=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Tremorrest_rle=null;

	/**
	 * @return Returns the tremorRest/rle.
	 */
	public String getTremorrest_rle(){
		try{
			if (_Tremorrest_rle==null){
				_Tremorrest_rle=getStringProperty("tremorRest/rle");
				return _Tremorrest_rle;
			}else {
				return _Tremorrest_rle;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tremorRest/rle.
	 * @param v Value to Set.
	 */
	public void setTremorrest_rle(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tremorRest/rle",v);
		_Tremorrest_rle=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Tremorrest_lle=null;

	/**
	 * @return Returns the tremorRest/lle.
	 */
	public String getTremorrest_lle(){
		try{
			if (_Tremorrest_lle==null){
				_Tremorrest_lle=getStringProperty("tremorRest/lle");
				return _Tremorrest_lle;
			}else {
				return _Tremorrest_lle;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tremorRest/lle.
	 * @param v Value to Set.
	 */
	public void setTremorrest_lle(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tremorRest/lle",v);
		_Tremorrest_lle=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Rigidity_neck=null;

	/**
	 * @return Returns the rigidity/neck.
	 */
	public String getRigidity_neck(){
		try{
			if (_Rigidity_neck==null){
				_Rigidity_neck=getStringProperty("rigidity/neck");
				return _Rigidity_neck;
			}else {
				return _Rigidity_neck;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for rigidity/neck.
	 * @param v Value to Set.
	 */
	public void setRigidity_neck(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/rigidity/neck",v);
		_Rigidity_neck=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Rigidity_rue=null;

	/**
	 * @return Returns the rigidity/rue.
	 */
	public String getRigidity_rue(){
		try{
			if (_Rigidity_rue==null){
				_Rigidity_rue=getStringProperty("rigidity/rue");
				return _Rigidity_rue;
			}else {
				return _Rigidity_rue;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for rigidity/rue.
	 * @param v Value to Set.
	 */
	public void setRigidity_rue(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/rigidity/rue",v);
		_Rigidity_rue=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Rigidity_lue=null;

	/**
	 * @return Returns the rigidity/lue.
	 */
	public String getRigidity_lue(){
		try{
			if (_Rigidity_lue==null){
				_Rigidity_lue=getStringProperty("rigidity/lue");
				return _Rigidity_lue;
			}else {
				return _Rigidity_lue;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for rigidity/lue.
	 * @param v Value to Set.
	 */
	public void setRigidity_lue(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/rigidity/lue",v);
		_Rigidity_lue=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Rigidity_rle=null;

	/**
	 * @return Returns the rigidity/rle.
	 */
	public String getRigidity_rle(){
		try{
			if (_Rigidity_rle==null){
				_Rigidity_rle=getStringProperty("rigidity/rle");
				return _Rigidity_rle;
			}else {
				return _Rigidity_rle;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for rigidity/rle.
	 * @param v Value to Set.
	 */
	public void setRigidity_rle(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/rigidity/rle",v);
		_Rigidity_rle=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Rigidity_lle=null;

	/**
	 * @return Returns the rigidity/lle.
	 */
	public String getRigidity_lle(){
		try{
			if (_Rigidity_lle==null){
				_Rigidity_lle=getStringProperty("rigidity/lle");
				return _Rigidity_lle;
			}else {
				return _Rigidity_lle;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for rigidity/lle.
	 * @param v Value to Set.
	 */
	public void setRigidity_lle(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/rigidity/lle",v);
		_Rigidity_lle=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Handmovementsgrip_right=null;

	/**
	 * @return Returns the handMovementsGrip/right.
	 */
	public Integer getHandmovementsgrip_right() {
		try{
			if (_Handmovementsgrip_right==null){
				_Handmovementsgrip_right=getIntegerProperty("handMovementsGrip/right");
				return _Handmovementsgrip_right;
			}else {
				return _Handmovementsgrip_right;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for handMovementsGrip/right.
	 * @param v Value to Set.
	 */
	public void setHandmovementsgrip_right(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/handMovementsGrip/right",v);
		_Handmovementsgrip_right=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Handmovementsgrip_left=null;

	/**
	 * @return Returns the handMovementsGrip/left.
	 */
	public Integer getHandmovementsgrip_left() {
		try{
			if (_Handmovementsgrip_left==null){
				_Handmovementsgrip_left=getIntegerProperty("handMovementsGrip/left");
				return _Handmovementsgrip_left;
			}else {
				return _Handmovementsgrip_left;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for handMovementsGrip/left.
	 * @param v Value to Set.
	 */
	public void setHandmovementsgrip_left(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/handMovementsGrip/left",v);
		_Handmovementsgrip_left=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Facialexpression=null;

	/**
	 * @return Returns the facialExpression.
	 */
	public Integer getFacialexpression() {
		try{
			if (_Facialexpression==null){
				_Facialexpression=getIntegerProperty("facialExpression");
				return _Facialexpression;
			}else {
				return _Facialexpression;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for facialExpression.
	 * @param v Value to Set.
	 */
	public void setFacialexpression(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/facialExpression",v);
		_Facialexpression=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Gait=null;

	/**
	 * @return Returns the gait.
	 */
	public Integer getGait() {
		try{
			if (_Gait==null){
				_Gait=getIntegerProperty("gait");
				return _Gait;
			}else {
				return _Gait;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for gait.
	 * @param v Value to Set.
	 */
	public void setGait(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/gait",v);
		_Gait=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Speech=null;

	/**
	 * @return Returns the speech.
	 */
	public Integer getSpeech() {
		try{
			if (_Speech==null){
				_Speech=getIntegerProperty("speech");
				return _Speech;
			}else {
				return _Speech;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for speech.
	 * @param v Value to Set.
	 */
	public void setSpeech(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/speech",v);
		_Speech=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Writing=null;

	/**
	 * @return Returns the writing.
	 */
	public Integer getWriting() {
		try{
			if (_Writing==null){
				_Writing=getIntegerProperty("writing");
				return _Writing;
			}else {
				return _Writing;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for writing.
	 * @param v Value to Set.
	 */
	public void setWriting(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/writing",v);
		_Writing=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Arisefromchair=null;

	/**
	 * @return Returns the ariseFromChair.
	 */
	public Integer getArisefromchair() {
		try{
			if (_Arisefromchair==null){
				_Arisefromchair=getIntegerProperty("ariseFromChair");
				return _Arisefromchair;
			}else {
				return _Arisefromchair;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for ariseFromChair.
	 * @param v Value to Set.
	 */
	public void setArisefromchair(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/ariseFromChair",v);
		_Arisefromchair=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Actionposturaltremor_right=null;

	/**
	 * @return Returns the actionPosturalTremor/right.
	 */
	public Integer getActionposturaltremor_right() {
		try{
			if (_Actionposturaltremor_right==null){
				_Actionposturaltremor_right=getIntegerProperty("actionPosturalTremor/right");
				return _Actionposturaltremor_right;
			}else {
				return _Actionposturaltremor_right;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for actionPosturalTremor/right.
	 * @param v Value to Set.
	 */
	public void setActionposturaltremor_right(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/actionPosturalTremor/right",v);
		_Actionposturaltremor_right=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Actionposturaltremor_left=null;

	/**
	 * @return Returns the actionPosturalTremor/left.
	 */
	public Integer getActionposturaltremor_left() {
		try{
			if (_Actionposturaltremor_left==null){
				_Actionposturaltremor_left=getIntegerProperty("actionPosturalTremor/left");
				return _Actionposturaltremor_left;
			}else {
				return _Actionposturaltremor_left;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for actionPosturalTremor/left.
	 * @param v Value to Set.
	 */
	public void setActionposturaltremor_left(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/actionPosturalTremor/left",v);
		_Actionposturaltremor_left=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Handsram_right=null;

	/**
	 * @return Returns the handsRAM/right.
	 */
	public Integer getHandsram_right() {
		try{
			if (_Handsram_right==null){
				_Handsram_right=getIntegerProperty("handsRAM/right");
				return _Handsram_right;
			}else {
				return _Handsram_right;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for handsRAM/right.
	 * @param v Value to Set.
	 */
	public void setHandsram_right(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/handsRAM/right",v);
		_Handsram_right=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Handsram_left=null;

	/**
	 * @return Returns the handsRAM/left.
	 */
	public Integer getHandsram_left() {
		try{
			if (_Handsram_left==null){
				_Handsram_left=getIntegerProperty("handsRAM/left");
				return _Handsram_left;
			}else {
				return _Handsram_left;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for handsRAM/left.
	 * @param v Value to Set.
	 */
	public void setHandsram_left(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/handsRAM/left",v);
		_Handsram_left=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Fingertaps_right=null;

	/**
	 * @return Returns the fingerTaps/right.
	 */
	public Integer getFingertaps_right() {
		try{
			if (_Fingertaps_right==null){
				_Fingertaps_right=getIntegerProperty("fingerTaps/right");
				return _Fingertaps_right;
			}else {
				return _Fingertaps_right;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for fingerTaps/right.
	 * @param v Value to Set.
	 */
	public void setFingertaps_right(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/fingerTaps/right",v);
		_Fingertaps_right=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Fingertaps_left=null;

	/**
	 * @return Returns the fingerTaps/left.
	 */
	public Integer getFingertaps_left() {
		try{
			if (_Fingertaps_left==null){
				_Fingertaps_left=getIntegerProperty("fingerTaps/left");
				return _Fingertaps_left;
			}else {
				return _Fingertaps_left;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for fingerTaps/left.
	 * @param v Value to Set.
	 */
	public void setFingertaps_left(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/fingerTaps/left",v);
		_Fingertaps_left=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Posture=null;

	/**
	 * @return Returns the posture.
	 */
	public Integer getPosture() {
		try{
			if (_Posture==null){
				_Posture=getIntegerProperty("posture");
				return _Posture;
			}else {
				return _Posture;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for posture.
	 * @param v Value to Set.
	 */
	public void setPosture(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/posture",v);
		_Posture=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Posturalstability=null;

	/**
	 * @return Returns the posturalStability.
	 */
	public Integer getPosturalstability() {
		try{
			if (_Posturalstability==null){
				_Posturalstability=getIntegerProperty("posturalStability");
				return _Posturalstability;
			}else {
				return _Posturalstability;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for posturalStability.
	 * @param v Value to Set.
	 */
	public void setPosturalstability(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/posturalStability",v);
		_Posturalstability=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Foottaps_right=null;

	/**
	 * @return Returns the footTaps/right.
	 */
	public Integer getFoottaps_right() {
		try{
			if (_Foottaps_right==null){
				_Foottaps_right=getIntegerProperty("footTaps/right");
				return _Foottaps_right;
			}else {
				return _Foottaps_right;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for footTaps/right.
	 * @param v Value to Set.
	 */
	public void setFoottaps_right(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/footTaps/right",v);
		_Foottaps_right=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Foottaps_left=null;

	/**
	 * @return Returns the footTaps/left.
	 */
	public Integer getFoottaps_left() {
		try{
			if (_Foottaps_left==null){
				_Foottaps_left=getIntegerProperty("footTaps/left");
				return _Foottaps_left;
			}else {
				return _Foottaps_left;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for footTaps/left.
	 * @param v Value to Set.
	 */
	public void setFoottaps_left(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/footTaps/left",v);
		_Foottaps_left=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Bodybradykinesiahypokinesia=null;

	/**
	 * @return Returns the bodyBradykinesiaHypokinesia.
	 */
	public Integer getBodybradykinesiahypokinesia() {
		try{
			if (_Bodybradykinesiahypokinesia==null){
				_Bodybradykinesiahypokinesia=getIntegerProperty("bodyBradykinesiaHypokinesia");
				return _Bodybradykinesiahypokinesia;
			}else {
				return _Bodybradykinesiahypokinesia;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for bodyBradykinesiaHypokinesia.
	 * @param v Value to Set.
	 */
	public void setBodybradykinesiahypokinesia(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/bodyBradykinesiaHypokinesia",v);
		_Bodybradykinesiahypokinesia=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Problem=null;

	/**
	 * @return Returns the problem.
	 */
	public Boolean getProblem() {
		try{
			if (_Problem==null){
				_Problem=getBooleanProperty("problem");
				return _Problem;
			}else {
				return _Problem;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for problem.
	 * @param v Value to Set.
	 */
	public void setProblem(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/problem",v);
		_Problem=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatAupdrs3data> getAllXnatAupdrs3datas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAupdrs3data> al = new ArrayList<org.nrg.xdat.om.XnatAupdrs3data>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAupdrs3data> getXnatAupdrs3datasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAupdrs3data> al = new ArrayList<org.nrg.xdat.om.XnatAupdrs3data>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAupdrs3data> getXnatAupdrs3datasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAupdrs3data> al = new ArrayList<org.nrg.xdat.om.XnatAupdrs3data>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatAupdrs3data getXnatAupdrs3datasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat_a:updrs3Data/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatAupdrs3data) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
