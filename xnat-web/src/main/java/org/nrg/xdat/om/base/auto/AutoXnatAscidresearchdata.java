/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:10 UTC 2016
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
public abstract class AutoXnatAscidresearchdata extends XnatSubjectassessordata implements org.nrg.xdat.model.XnatAscidresearchdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatAscidresearchdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat_a:scidResearchData";

	public AutoXnatAscidresearchdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatAscidresearchdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatAscidresearchdata(UserI user)
	 **/
	public AutoXnatAscidresearchdata(){}

	public AutoXnatAscidresearchdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat_a:scidResearchData";
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

	private String _Administrator=null;

	/**
	 * @return Returns the administrator.
	 */
	public String getAdministrator(){
		try{
			if (_Administrator==null){
				_Administrator=getStringProperty("administrator");
				return _Administrator;
			}else {
				return _Administrator;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for administrator.
	 * @param v Value to Set.
	 */
	public void setAdministrator(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/administrator",v);
		_Administrator=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Moodepisodes_currentmajordepressiveepisode=null;

	/**
	 * @return Returns the moodEpisodes/currentMajorDepressiveEpisode.
	 */
	public Integer getMoodepisodes_currentmajordepressiveepisode() {
		try{
			if (_Moodepisodes_currentmajordepressiveepisode==null){
				_Moodepisodes_currentmajordepressiveepisode=getIntegerProperty("moodEpisodes/currentMajorDepressiveEpisode");
				return _Moodepisodes_currentmajordepressiveepisode;
			}else {
				return _Moodepisodes_currentmajordepressiveepisode;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodEpisodes/currentMajorDepressiveEpisode.
	 * @param v Value to Set.
	 */
	public void setMoodepisodes_currentmajordepressiveepisode(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodEpisodes/currentMajorDepressiveEpisode",v);
		_Moodepisodes_currentmajordepressiveepisode=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Moodepisodes_pastmajordepressiveepisode=null;

	/**
	 * @return Returns the moodEpisodes/pastMajorDepressiveEpisode.
	 */
	public Integer getMoodepisodes_pastmajordepressiveepisode() {
		try{
			if (_Moodepisodes_pastmajordepressiveepisode==null){
				_Moodepisodes_pastmajordepressiveepisode=getIntegerProperty("moodEpisodes/pastMajorDepressiveEpisode");
				return _Moodepisodes_pastmajordepressiveepisode;
			}else {
				return _Moodepisodes_pastmajordepressiveepisode;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodEpisodes/pastMajorDepressiveEpisode.
	 * @param v Value to Set.
	 */
	public void setMoodepisodes_pastmajordepressiveepisode(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodEpisodes/pastMajorDepressiveEpisode",v);
		_Moodepisodes_pastmajordepressiveepisode=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Moodepisodes_currentmanicepisode=null;

	/**
	 * @return Returns the moodEpisodes/currentManicEpisode.
	 */
	public Integer getMoodepisodes_currentmanicepisode() {
		try{
			if (_Moodepisodes_currentmanicepisode==null){
				_Moodepisodes_currentmanicepisode=getIntegerProperty("moodEpisodes/currentManicEpisode");
				return _Moodepisodes_currentmanicepisode;
			}else {
				return _Moodepisodes_currentmanicepisode;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodEpisodes/currentManicEpisode.
	 * @param v Value to Set.
	 */
	public void setMoodepisodes_currentmanicepisode(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodEpisodes/currentManicEpisode",v);
		_Moodepisodes_currentmanicepisode=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Moodepisodes_pastmanicepisode=null;

	/**
	 * @return Returns the moodEpisodes/pastManicEpisode.
	 */
	public Integer getMoodepisodes_pastmanicepisode() {
		try{
			if (_Moodepisodes_pastmanicepisode==null){
				_Moodepisodes_pastmanicepisode=getIntegerProperty("moodEpisodes/pastManicEpisode");
				return _Moodepisodes_pastmanicepisode;
			}else {
				return _Moodepisodes_pastmanicepisode;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodEpisodes/pastManicEpisode.
	 * @param v Value to Set.
	 */
	public void setMoodepisodes_pastmanicepisode(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodEpisodes/pastManicEpisode",v);
		_Moodepisodes_pastmanicepisode=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Moodepisodes_currenthypomanicepisode=null;

	/**
	 * @return Returns the moodEpisodes/currentHypomanicEpisode.
	 */
	public Integer getMoodepisodes_currenthypomanicepisode() {
		try{
			if (_Moodepisodes_currenthypomanicepisode==null){
				_Moodepisodes_currenthypomanicepisode=getIntegerProperty("moodEpisodes/currentHypomanicEpisode");
				return _Moodepisodes_currenthypomanicepisode;
			}else {
				return _Moodepisodes_currenthypomanicepisode;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodEpisodes/currentHypomanicEpisode.
	 * @param v Value to Set.
	 */
	public void setMoodepisodes_currenthypomanicepisode(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodEpisodes/currentHypomanicEpisode",v);
		_Moodepisodes_currenthypomanicepisode=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Moodepisodes_pasthypomanicepisode=null;

	/**
	 * @return Returns the moodEpisodes/pastHypomanicEpisode.
	 */
	public Integer getMoodepisodes_pasthypomanicepisode() {
		try{
			if (_Moodepisodes_pasthypomanicepisode==null){
				_Moodepisodes_pasthypomanicepisode=getIntegerProperty("moodEpisodes/pastHypomanicEpisode");
				return _Moodepisodes_pasthypomanicepisode;
			}else {
				return _Moodepisodes_pasthypomanicepisode;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodEpisodes/pastHypomanicEpisode.
	 * @param v Value to Set.
	 */
	public void setMoodepisodes_pasthypomanicepisode(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodEpisodes/pastHypomanicEpisode",v);
		_Moodepisodes_pasthypomanicepisode=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Moodepisodes_currentdysthmicepisode=null;

	/**
	 * @return Returns the moodEpisodes/currentDysthmicEpisode.
	 */
	public Integer getMoodepisodes_currentdysthmicepisode() {
		try{
			if (_Moodepisodes_currentdysthmicepisode==null){
				_Moodepisodes_currentdysthmicepisode=getIntegerProperty("moodEpisodes/currentDysthmicEpisode");
				return _Moodepisodes_currentdysthmicepisode;
			}else {
				return _Moodepisodes_currentdysthmicepisode;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodEpisodes/currentDysthmicEpisode.
	 * @param v Value to Set.
	 */
	public void setMoodepisodes_currentdysthmicepisode(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodEpisodes/currentDysthmicEpisode",v);
		_Moodepisodes_currentdysthmicepisode=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Moodepisodes_currentmooddisorderduetomedicalcondition=null;

	/**
	 * @return Returns the moodEpisodes/currentMoodDisorderDueToMedicalCondition.
	 */
	public Integer getMoodepisodes_currentmooddisorderduetomedicalcondition() {
		try{
			if (_Moodepisodes_currentmooddisorderduetomedicalcondition==null){
				_Moodepisodes_currentmooddisorderduetomedicalcondition=getIntegerProperty("moodEpisodes/currentMoodDisorderDueToMedicalCondition");
				return _Moodepisodes_currentmooddisorderduetomedicalcondition;
			}else {
				return _Moodepisodes_currentmooddisorderduetomedicalcondition;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodEpisodes/currentMoodDisorderDueToMedicalCondition.
	 * @param v Value to Set.
	 */
	public void setMoodepisodes_currentmooddisorderduetomedicalcondition(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodEpisodes/currentMoodDisorderDueToMedicalCondition",v);
		_Moodepisodes_currentmooddisorderduetomedicalcondition=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Moodepisodes_pastmooddisorderduetomedicalcondition=null;

	/**
	 * @return Returns the moodEpisodes/pastMoodDisorderDueToMedicalCondition.
	 */
	public Integer getMoodepisodes_pastmooddisorderduetomedicalcondition() {
		try{
			if (_Moodepisodes_pastmooddisorderduetomedicalcondition==null){
				_Moodepisodes_pastmooddisorderduetomedicalcondition=getIntegerProperty("moodEpisodes/pastMoodDisorderDueToMedicalCondition");
				return _Moodepisodes_pastmooddisorderduetomedicalcondition;
			}else {
				return _Moodepisodes_pastmooddisorderduetomedicalcondition;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodEpisodes/pastMoodDisorderDueToMedicalCondition.
	 * @param v Value to Set.
	 */
	public void setMoodepisodes_pastmooddisorderduetomedicalcondition(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodEpisodes/pastMoodDisorderDueToMedicalCondition",v);
		_Moodepisodes_pastmooddisorderduetomedicalcondition=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Moodepisodes_currentsubstanceinducedmooddisorder=null;

	/**
	 * @return Returns the moodEpisodes/currentSubstanceInducedMoodDisorder.
	 */
	public Integer getMoodepisodes_currentsubstanceinducedmooddisorder() {
		try{
			if (_Moodepisodes_currentsubstanceinducedmooddisorder==null){
				_Moodepisodes_currentsubstanceinducedmooddisorder=getIntegerProperty("moodEpisodes/currentSubstanceInducedMoodDisorder");
				return _Moodepisodes_currentsubstanceinducedmooddisorder;
			}else {
				return _Moodepisodes_currentsubstanceinducedmooddisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodEpisodes/currentSubstanceInducedMoodDisorder.
	 * @param v Value to Set.
	 */
	public void setMoodepisodes_currentsubstanceinducedmooddisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodEpisodes/currentSubstanceInducedMoodDisorder",v);
		_Moodepisodes_currentsubstanceinducedmooddisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Moodepisodes_pastsubstanceinducedmooddisorder=null;

	/**
	 * @return Returns the moodEpisodes/pastSubstanceInducedMoodDisorder.
	 */
	public Integer getMoodepisodes_pastsubstanceinducedmooddisorder() {
		try{
			if (_Moodepisodes_pastsubstanceinducedmooddisorder==null){
				_Moodepisodes_pastsubstanceinducedmooddisorder=getIntegerProperty("moodEpisodes/pastSubstanceInducedMoodDisorder");
				return _Moodepisodes_pastsubstanceinducedmooddisorder;
			}else {
				return _Moodepisodes_pastsubstanceinducedmooddisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodEpisodes/pastSubstanceInducedMoodDisorder.
	 * @param v Value to Set.
	 */
	public void setMoodepisodes_pastsubstanceinducedmooddisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodEpisodes/pastSubstanceInducedMoodDisorder",v);
		_Moodepisodes_pastsubstanceinducedmooddisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticsymptoms_currentdelusions=null;

	/**
	 * @return Returns the psychoticSymptoms/currentDelusions.
	 */
	public Integer getPsychoticsymptoms_currentdelusions() {
		try{
			if (_Psychoticsymptoms_currentdelusions==null){
				_Psychoticsymptoms_currentdelusions=getIntegerProperty("psychoticSymptoms/currentDelusions");
				return _Psychoticsymptoms_currentdelusions;
			}else {
				return _Psychoticsymptoms_currentdelusions;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticSymptoms/currentDelusions.
	 * @param v Value to Set.
	 */
	public void setPsychoticsymptoms_currentdelusions(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticSymptoms/currentDelusions",v);
		_Psychoticsymptoms_currentdelusions=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticsymptoms_pastdelusions=null;

	/**
	 * @return Returns the psychoticSymptoms/pastDelusions.
	 */
	public Integer getPsychoticsymptoms_pastdelusions() {
		try{
			if (_Psychoticsymptoms_pastdelusions==null){
				_Psychoticsymptoms_pastdelusions=getIntegerProperty("psychoticSymptoms/pastDelusions");
				return _Psychoticsymptoms_pastdelusions;
			}else {
				return _Psychoticsymptoms_pastdelusions;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticSymptoms/pastDelusions.
	 * @param v Value to Set.
	 */
	public void setPsychoticsymptoms_pastdelusions(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticSymptoms/pastDelusions",v);
		_Psychoticsymptoms_pastdelusions=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticsymptoms_currenthallucinations=null;

	/**
	 * @return Returns the psychoticSymptoms/currentHallucinations.
	 */
	public Integer getPsychoticsymptoms_currenthallucinations() {
		try{
			if (_Psychoticsymptoms_currenthallucinations==null){
				_Psychoticsymptoms_currenthallucinations=getIntegerProperty("psychoticSymptoms/currentHallucinations");
				return _Psychoticsymptoms_currenthallucinations;
			}else {
				return _Psychoticsymptoms_currenthallucinations;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticSymptoms/currentHallucinations.
	 * @param v Value to Set.
	 */
	public void setPsychoticsymptoms_currenthallucinations(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticSymptoms/currentHallucinations",v);
		_Psychoticsymptoms_currenthallucinations=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticsymptoms_pasthallucinations=null;

	/**
	 * @return Returns the psychoticSymptoms/pastHallucinations.
	 */
	public Integer getPsychoticsymptoms_pasthallucinations() {
		try{
			if (_Psychoticsymptoms_pasthallucinations==null){
				_Psychoticsymptoms_pasthallucinations=getIntegerProperty("psychoticSymptoms/pastHallucinations");
				return _Psychoticsymptoms_pasthallucinations;
			}else {
				return _Psychoticsymptoms_pasthallucinations;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticSymptoms/pastHallucinations.
	 * @param v Value to Set.
	 */
	public void setPsychoticsymptoms_pasthallucinations(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticSymptoms/pastHallucinations",v);
		_Psychoticsymptoms_pasthallucinations=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticsymptoms_currentdisorganizedspeechbehavior=null;

	/**
	 * @return Returns the psychoticSymptoms/currentDisorganizedSpeechBehavior.
	 */
	public Integer getPsychoticsymptoms_currentdisorganizedspeechbehavior() {
		try{
			if (_Psychoticsymptoms_currentdisorganizedspeechbehavior==null){
				_Psychoticsymptoms_currentdisorganizedspeechbehavior=getIntegerProperty("psychoticSymptoms/currentDisorganizedSpeechBehavior");
				return _Psychoticsymptoms_currentdisorganizedspeechbehavior;
			}else {
				return _Psychoticsymptoms_currentdisorganizedspeechbehavior;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticSymptoms/currentDisorganizedSpeechBehavior.
	 * @param v Value to Set.
	 */
	public void setPsychoticsymptoms_currentdisorganizedspeechbehavior(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticSymptoms/currentDisorganizedSpeechBehavior",v);
		_Psychoticsymptoms_currentdisorganizedspeechbehavior=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticsymptoms_pastdisorganizedspeechbehavior=null;

	/**
	 * @return Returns the psychoticSymptoms/pastDisorganizedSpeechBehavior.
	 */
	public Integer getPsychoticsymptoms_pastdisorganizedspeechbehavior() {
		try{
			if (_Psychoticsymptoms_pastdisorganizedspeechbehavior==null){
				_Psychoticsymptoms_pastdisorganizedspeechbehavior=getIntegerProperty("psychoticSymptoms/pastDisorganizedSpeechBehavior");
				return _Psychoticsymptoms_pastdisorganizedspeechbehavior;
			}else {
				return _Psychoticsymptoms_pastdisorganizedspeechbehavior;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticSymptoms/pastDisorganizedSpeechBehavior.
	 * @param v Value to Set.
	 */
	public void setPsychoticsymptoms_pastdisorganizedspeechbehavior(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticSymptoms/pastDisorganizedSpeechBehavior",v);
		_Psychoticsymptoms_pastdisorganizedspeechbehavior=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticsymptoms_currentcatatonicbehavior=null;

	/**
	 * @return Returns the psychoticSymptoms/currentCatatonicBehavior.
	 */
	public Integer getPsychoticsymptoms_currentcatatonicbehavior() {
		try{
			if (_Psychoticsymptoms_currentcatatonicbehavior==null){
				_Psychoticsymptoms_currentcatatonicbehavior=getIntegerProperty("psychoticSymptoms/currentCatatonicBehavior");
				return _Psychoticsymptoms_currentcatatonicbehavior;
			}else {
				return _Psychoticsymptoms_currentcatatonicbehavior;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticSymptoms/currentCatatonicBehavior.
	 * @param v Value to Set.
	 */
	public void setPsychoticsymptoms_currentcatatonicbehavior(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticSymptoms/currentCatatonicBehavior",v);
		_Psychoticsymptoms_currentcatatonicbehavior=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticsymptoms_pastcatatonicbehavior=null;

	/**
	 * @return Returns the psychoticSymptoms/pastCatatonicBehavior.
	 */
	public Integer getPsychoticsymptoms_pastcatatonicbehavior() {
		try{
			if (_Psychoticsymptoms_pastcatatonicbehavior==null){
				_Psychoticsymptoms_pastcatatonicbehavior=getIntegerProperty("psychoticSymptoms/pastCatatonicBehavior");
				return _Psychoticsymptoms_pastcatatonicbehavior;
			}else {
				return _Psychoticsymptoms_pastcatatonicbehavior;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticSymptoms/pastCatatonicBehavior.
	 * @param v Value to Set.
	 */
	public void setPsychoticsymptoms_pastcatatonicbehavior(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticSymptoms/pastCatatonicBehavior",v);
		_Psychoticsymptoms_pastcatatonicbehavior=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticsymptoms_currentnegativesymptoms=null;

	/**
	 * @return Returns the psychoticSymptoms/currentNegativeSymptoms.
	 */
	public Integer getPsychoticsymptoms_currentnegativesymptoms() {
		try{
			if (_Psychoticsymptoms_currentnegativesymptoms==null){
				_Psychoticsymptoms_currentnegativesymptoms=getIntegerProperty("psychoticSymptoms/currentNegativeSymptoms");
				return _Psychoticsymptoms_currentnegativesymptoms;
			}else {
				return _Psychoticsymptoms_currentnegativesymptoms;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticSymptoms/currentNegativeSymptoms.
	 * @param v Value to Set.
	 */
	public void setPsychoticsymptoms_currentnegativesymptoms(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticSymptoms/currentNegativeSymptoms",v);
		_Psychoticsymptoms_currentnegativesymptoms=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticsymptoms_pastnegativesymptoms=null;

	/**
	 * @return Returns the psychoticSymptoms/pastNegativeSymptoms.
	 */
	public Integer getPsychoticsymptoms_pastnegativesymptoms() {
		try{
			if (_Psychoticsymptoms_pastnegativesymptoms==null){
				_Psychoticsymptoms_pastnegativesymptoms=getIntegerProperty("psychoticSymptoms/pastNegativeSymptoms");
				return _Psychoticsymptoms_pastnegativesymptoms;
			}else {
				return _Psychoticsymptoms_pastnegativesymptoms;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticSymptoms/pastNegativeSymptoms.
	 * @param v Value to Set.
	 */
	public void setPsychoticsymptoms_pastnegativesymptoms(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticSymptoms/pastNegativeSymptoms",v);
		_Psychoticsymptoms_pastnegativesymptoms=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_currentschizophrenia=null;

	/**
	 * @return Returns the psychoticDisorders/currentSchizophrenia.
	 */
	public Integer getPsychoticdisorders_currentschizophrenia() {
		try{
			if (_Psychoticdisorders_currentschizophrenia==null){
				_Psychoticdisorders_currentschizophrenia=getIntegerProperty("psychoticDisorders/currentSchizophrenia");
				return _Psychoticdisorders_currentschizophrenia;
			}else {
				return _Psychoticdisorders_currentschizophrenia;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/currentSchizophrenia.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_currentschizophrenia(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/currentSchizophrenia",v);
		_Psychoticdisorders_currentschizophrenia=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_pastschizophrenia=null;

	/**
	 * @return Returns the psychoticDisorders/pastSchizophrenia.
	 */
	public Integer getPsychoticdisorders_pastschizophrenia() {
		try{
			if (_Psychoticdisorders_pastschizophrenia==null){
				_Psychoticdisorders_pastschizophrenia=getIntegerProperty("psychoticDisorders/pastSchizophrenia");
				return _Psychoticdisorders_pastschizophrenia;
			}else {
				return _Psychoticdisorders_pastschizophrenia;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/pastSchizophrenia.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_pastschizophrenia(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/pastSchizophrenia",v);
		_Psychoticdisorders_pastschizophrenia=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_currentparanoidtype=null;

	/**
	 * @return Returns the psychoticDisorders/currentParanoidType.
	 */
	public Integer getPsychoticdisorders_currentparanoidtype() {
		try{
			if (_Psychoticdisorders_currentparanoidtype==null){
				_Psychoticdisorders_currentparanoidtype=getIntegerProperty("psychoticDisorders/currentParanoidType");
				return _Psychoticdisorders_currentparanoidtype;
			}else {
				return _Psychoticdisorders_currentparanoidtype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/currentParanoidType.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_currentparanoidtype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/currentParanoidType",v);
		_Psychoticdisorders_currentparanoidtype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_pastparanoidtype=null;

	/**
	 * @return Returns the psychoticDisorders/pastParanoidType.
	 */
	public Integer getPsychoticdisorders_pastparanoidtype() {
		try{
			if (_Psychoticdisorders_pastparanoidtype==null){
				_Psychoticdisorders_pastparanoidtype=getIntegerProperty("psychoticDisorders/pastParanoidType");
				return _Psychoticdisorders_pastparanoidtype;
			}else {
				return _Psychoticdisorders_pastparanoidtype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/pastParanoidType.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_pastparanoidtype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/pastParanoidType",v);
		_Psychoticdisorders_pastparanoidtype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_currentcatatonictype=null;

	/**
	 * @return Returns the psychoticDisorders/currentCatatonicType.
	 */
	public Integer getPsychoticdisorders_currentcatatonictype() {
		try{
			if (_Psychoticdisorders_currentcatatonictype==null){
				_Psychoticdisorders_currentcatatonictype=getIntegerProperty("psychoticDisorders/currentCatatonicType");
				return _Psychoticdisorders_currentcatatonictype;
			}else {
				return _Psychoticdisorders_currentcatatonictype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/currentCatatonicType.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_currentcatatonictype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/currentCatatonicType",v);
		_Psychoticdisorders_currentcatatonictype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_pastcatatonictype=null;

	/**
	 * @return Returns the psychoticDisorders/pastCatatonicType.
	 */
	public Integer getPsychoticdisorders_pastcatatonictype() {
		try{
			if (_Psychoticdisorders_pastcatatonictype==null){
				_Psychoticdisorders_pastcatatonictype=getIntegerProperty("psychoticDisorders/pastCatatonicType");
				return _Psychoticdisorders_pastcatatonictype;
			}else {
				return _Psychoticdisorders_pastcatatonictype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/pastCatatonicType.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_pastcatatonictype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/pastCatatonicType",v);
		_Psychoticdisorders_pastcatatonictype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_currentdisorganizedtype=null;

	/**
	 * @return Returns the psychoticDisorders/currentDisorganizedType.
	 */
	public Integer getPsychoticdisorders_currentdisorganizedtype() {
		try{
			if (_Psychoticdisorders_currentdisorganizedtype==null){
				_Psychoticdisorders_currentdisorganizedtype=getIntegerProperty("psychoticDisorders/currentDisorganizedType");
				return _Psychoticdisorders_currentdisorganizedtype;
			}else {
				return _Psychoticdisorders_currentdisorganizedtype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/currentDisorganizedType.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_currentdisorganizedtype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/currentDisorganizedType",v);
		_Psychoticdisorders_currentdisorganizedtype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_pastdisorganizedtype=null;

	/**
	 * @return Returns the psychoticDisorders/pastDisorganizedType.
	 */
	public Integer getPsychoticdisorders_pastdisorganizedtype() {
		try{
			if (_Psychoticdisorders_pastdisorganizedtype==null){
				_Psychoticdisorders_pastdisorganizedtype=getIntegerProperty("psychoticDisorders/pastDisorganizedType");
				return _Psychoticdisorders_pastdisorganizedtype;
			}else {
				return _Psychoticdisorders_pastdisorganizedtype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/pastDisorganizedType.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_pastdisorganizedtype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/pastDisorganizedType",v);
		_Psychoticdisorders_pastdisorganizedtype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_currentundifferentiatedtype=null;

	/**
	 * @return Returns the psychoticDisorders/currentUndifferentiatedType.
	 */
	public Integer getPsychoticdisorders_currentundifferentiatedtype() {
		try{
			if (_Psychoticdisorders_currentundifferentiatedtype==null){
				_Psychoticdisorders_currentundifferentiatedtype=getIntegerProperty("psychoticDisorders/currentUndifferentiatedType");
				return _Psychoticdisorders_currentundifferentiatedtype;
			}else {
				return _Psychoticdisorders_currentundifferentiatedtype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/currentUndifferentiatedType.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_currentundifferentiatedtype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/currentUndifferentiatedType",v);
		_Psychoticdisorders_currentundifferentiatedtype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_pastundifferentiatedtype=null;

	/**
	 * @return Returns the psychoticDisorders/pastUndifferentiatedType.
	 */
	public Integer getPsychoticdisorders_pastundifferentiatedtype() {
		try{
			if (_Psychoticdisorders_pastundifferentiatedtype==null){
				_Psychoticdisorders_pastundifferentiatedtype=getIntegerProperty("psychoticDisorders/pastUndifferentiatedType");
				return _Psychoticdisorders_pastundifferentiatedtype;
			}else {
				return _Psychoticdisorders_pastundifferentiatedtype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/pastUndifferentiatedType.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_pastundifferentiatedtype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/pastUndifferentiatedType",v);
		_Psychoticdisorders_pastundifferentiatedtype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_currentresidualtype=null;

	/**
	 * @return Returns the psychoticDisorders/currentResidualType.
	 */
	public Integer getPsychoticdisorders_currentresidualtype() {
		try{
			if (_Psychoticdisorders_currentresidualtype==null){
				_Psychoticdisorders_currentresidualtype=getIntegerProperty("psychoticDisorders/currentResidualType");
				return _Psychoticdisorders_currentresidualtype;
			}else {
				return _Psychoticdisorders_currentresidualtype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/currentResidualType.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_currentresidualtype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/currentResidualType",v);
		_Psychoticdisorders_currentresidualtype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_pastresidualtype=null;

	/**
	 * @return Returns the psychoticDisorders/pastResidualType.
	 */
	public Integer getPsychoticdisorders_pastresidualtype() {
		try{
			if (_Psychoticdisorders_pastresidualtype==null){
				_Psychoticdisorders_pastresidualtype=getIntegerProperty("psychoticDisorders/pastResidualType");
				return _Psychoticdisorders_pastresidualtype;
			}else {
				return _Psychoticdisorders_pastresidualtype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/pastResidualType.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_pastresidualtype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/pastResidualType",v);
		_Psychoticdisorders_pastresidualtype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_currentschizophreniformdisorder=null;

	/**
	 * @return Returns the psychoticDisorders/currentSchizophreniformDisorder.
	 */
	public Integer getPsychoticdisorders_currentschizophreniformdisorder() {
		try{
			if (_Psychoticdisorders_currentschizophreniformdisorder==null){
				_Psychoticdisorders_currentschizophreniformdisorder=getIntegerProperty("psychoticDisorders/currentSchizophreniformDisorder");
				return _Psychoticdisorders_currentschizophreniformdisorder;
			}else {
				return _Psychoticdisorders_currentschizophreniformdisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/currentSchizophreniformDisorder.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_currentschizophreniformdisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/currentSchizophreniformDisorder",v);
		_Psychoticdisorders_currentschizophreniformdisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_pastschizophreniformdisorder=null;

	/**
	 * @return Returns the psychoticDisorders/pastSchizophreniformDisorder.
	 */
	public Integer getPsychoticdisorders_pastschizophreniformdisorder() {
		try{
			if (_Psychoticdisorders_pastschizophreniformdisorder==null){
				_Psychoticdisorders_pastschizophreniformdisorder=getIntegerProperty("psychoticDisorders/pastSchizophreniformDisorder");
				return _Psychoticdisorders_pastschizophreniformdisorder;
			}else {
				return _Psychoticdisorders_pastschizophreniformdisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/pastSchizophreniformDisorder.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_pastschizophreniformdisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/pastSchizophreniformDisorder",v);
		_Psychoticdisorders_pastschizophreniformdisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_currentschizoaffectivedisorder=null;

	/**
	 * @return Returns the psychoticDisorders/currentSchizoaffectiveDisorder.
	 */
	public Integer getPsychoticdisorders_currentschizoaffectivedisorder() {
		try{
			if (_Psychoticdisorders_currentschizoaffectivedisorder==null){
				_Psychoticdisorders_currentschizoaffectivedisorder=getIntegerProperty("psychoticDisorders/currentSchizoaffectiveDisorder");
				return _Psychoticdisorders_currentschizoaffectivedisorder;
			}else {
				return _Psychoticdisorders_currentschizoaffectivedisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/currentSchizoaffectiveDisorder.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_currentschizoaffectivedisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/currentSchizoaffectiveDisorder",v);
		_Psychoticdisorders_currentschizoaffectivedisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_pastschizoaffectivedisorder=null;

	/**
	 * @return Returns the psychoticDisorders/pastSchizoaffectiveDisorder.
	 */
	public Integer getPsychoticdisorders_pastschizoaffectivedisorder() {
		try{
			if (_Psychoticdisorders_pastschizoaffectivedisorder==null){
				_Psychoticdisorders_pastschizoaffectivedisorder=getIntegerProperty("psychoticDisorders/pastSchizoaffectiveDisorder");
				return _Psychoticdisorders_pastschizoaffectivedisorder;
			}else {
				return _Psychoticdisorders_pastschizoaffectivedisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/pastSchizoaffectiveDisorder.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_pastschizoaffectivedisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/pastSchizoaffectiveDisorder",v);
		_Psychoticdisorders_pastschizoaffectivedisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_currentdelusionaldisorder=null;

	/**
	 * @return Returns the psychoticDisorders/currentDelusionalDisorder.
	 */
	public Integer getPsychoticdisorders_currentdelusionaldisorder() {
		try{
			if (_Psychoticdisorders_currentdelusionaldisorder==null){
				_Psychoticdisorders_currentdelusionaldisorder=getIntegerProperty("psychoticDisorders/currentDelusionalDisorder");
				return _Psychoticdisorders_currentdelusionaldisorder;
			}else {
				return _Psychoticdisorders_currentdelusionaldisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/currentDelusionalDisorder.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_currentdelusionaldisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/currentDelusionalDisorder",v);
		_Psychoticdisorders_currentdelusionaldisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_pastdelusionaldisorder=null;

	/**
	 * @return Returns the psychoticDisorders/pastDelusionalDisorder.
	 */
	public Integer getPsychoticdisorders_pastdelusionaldisorder() {
		try{
			if (_Psychoticdisorders_pastdelusionaldisorder==null){
				_Psychoticdisorders_pastdelusionaldisorder=getIntegerProperty("psychoticDisorders/pastDelusionalDisorder");
				return _Psychoticdisorders_pastdelusionaldisorder;
			}else {
				return _Psychoticdisorders_pastdelusionaldisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/pastDelusionalDisorder.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_pastdelusionaldisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/pastDelusionalDisorder",v);
		_Psychoticdisorders_pastdelusionaldisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_currentbriefpsychoticdisorder=null;

	/**
	 * @return Returns the psychoticDisorders/currentBriefPsychoticDisorder.
	 */
	public Integer getPsychoticdisorders_currentbriefpsychoticdisorder() {
		try{
			if (_Psychoticdisorders_currentbriefpsychoticdisorder==null){
				_Psychoticdisorders_currentbriefpsychoticdisorder=getIntegerProperty("psychoticDisorders/currentBriefPsychoticDisorder");
				return _Psychoticdisorders_currentbriefpsychoticdisorder;
			}else {
				return _Psychoticdisorders_currentbriefpsychoticdisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/currentBriefPsychoticDisorder.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_currentbriefpsychoticdisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/currentBriefPsychoticDisorder",v);
		_Psychoticdisorders_currentbriefpsychoticdisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_pastbriefpsychoticdisorder=null;

	/**
	 * @return Returns the psychoticDisorders/pastBriefPsychoticDisorder.
	 */
	public Integer getPsychoticdisorders_pastbriefpsychoticdisorder() {
		try{
			if (_Psychoticdisorders_pastbriefpsychoticdisorder==null){
				_Psychoticdisorders_pastbriefpsychoticdisorder=getIntegerProperty("psychoticDisorders/pastBriefPsychoticDisorder");
				return _Psychoticdisorders_pastbriefpsychoticdisorder;
			}else {
				return _Psychoticdisorders_pastbriefpsychoticdisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/pastBriefPsychoticDisorder.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_pastbriefpsychoticdisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/pastBriefPsychoticDisorder",v);
		_Psychoticdisorders_pastbriefpsychoticdisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_currentpsychoticdisorderduetomedicalcondition=null;

	/**
	 * @return Returns the psychoticDisorders/currentPsychoticDisorderDueToMedicalCondition.
	 */
	public Integer getPsychoticdisorders_currentpsychoticdisorderduetomedicalcondition() {
		try{
			if (_Psychoticdisorders_currentpsychoticdisorderduetomedicalcondition==null){
				_Psychoticdisorders_currentpsychoticdisorderduetomedicalcondition=getIntegerProperty("psychoticDisorders/currentPsychoticDisorderDueToMedicalCondition");
				return _Psychoticdisorders_currentpsychoticdisorderduetomedicalcondition;
			}else {
				return _Psychoticdisorders_currentpsychoticdisorderduetomedicalcondition;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/currentPsychoticDisorderDueToMedicalCondition.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_currentpsychoticdisorderduetomedicalcondition(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/currentPsychoticDisorderDueToMedicalCondition",v);
		_Psychoticdisorders_currentpsychoticdisorderduetomedicalcondition=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_pastpsychoticdisorderduetomedicalcondition=null;

	/**
	 * @return Returns the psychoticDisorders/pastPsychoticDisorderDueToMedicalCondition.
	 */
	public Integer getPsychoticdisorders_pastpsychoticdisorderduetomedicalcondition() {
		try{
			if (_Psychoticdisorders_pastpsychoticdisorderduetomedicalcondition==null){
				_Psychoticdisorders_pastpsychoticdisorderduetomedicalcondition=getIntegerProperty("psychoticDisorders/pastPsychoticDisorderDueToMedicalCondition");
				return _Psychoticdisorders_pastpsychoticdisorderduetomedicalcondition;
			}else {
				return _Psychoticdisorders_pastpsychoticdisorderduetomedicalcondition;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/pastPsychoticDisorderDueToMedicalCondition.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_pastpsychoticdisorderduetomedicalcondition(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/pastPsychoticDisorderDueToMedicalCondition",v);
		_Psychoticdisorders_pastpsychoticdisorderduetomedicalcondition=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_currentsubstanceinducedpsychoticdisorder=null;

	/**
	 * @return Returns the psychoticDisorders/currentSubstanceInducedPsychoticDisorder.
	 */
	public Integer getPsychoticdisorders_currentsubstanceinducedpsychoticdisorder() {
		try{
			if (_Psychoticdisorders_currentsubstanceinducedpsychoticdisorder==null){
				_Psychoticdisorders_currentsubstanceinducedpsychoticdisorder=getIntegerProperty("psychoticDisorders/currentSubstanceInducedPsychoticDisorder");
				return _Psychoticdisorders_currentsubstanceinducedpsychoticdisorder;
			}else {
				return _Psychoticdisorders_currentsubstanceinducedpsychoticdisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/currentSubstanceInducedPsychoticDisorder.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_currentsubstanceinducedpsychoticdisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/currentSubstanceInducedPsychoticDisorder",v);
		_Psychoticdisorders_currentsubstanceinducedpsychoticdisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_pastsubstanceinducedpsychoticdisorder=null;

	/**
	 * @return Returns the psychoticDisorders/pastSubstanceInducedPsychoticDisorder.
	 */
	public Integer getPsychoticdisorders_pastsubstanceinducedpsychoticdisorder() {
		try{
			if (_Psychoticdisorders_pastsubstanceinducedpsychoticdisorder==null){
				_Psychoticdisorders_pastsubstanceinducedpsychoticdisorder=getIntegerProperty("psychoticDisorders/pastSubstanceInducedPsychoticDisorder");
				return _Psychoticdisorders_pastsubstanceinducedpsychoticdisorder;
			}else {
				return _Psychoticdisorders_pastsubstanceinducedpsychoticdisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/pastSubstanceInducedPsychoticDisorder.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_pastsubstanceinducedpsychoticdisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/pastSubstanceInducedPsychoticDisorder",v);
		_Psychoticdisorders_pastsubstanceinducedpsychoticdisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_currentpsychoticdisordernos=null;

	/**
	 * @return Returns the psychoticDisorders/currentPsychoticDisorderNOS.
	 */
	public Integer getPsychoticdisorders_currentpsychoticdisordernos() {
		try{
			if (_Psychoticdisorders_currentpsychoticdisordernos==null){
				_Psychoticdisorders_currentpsychoticdisordernos=getIntegerProperty("psychoticDisorders/currentPsychoticDisorderNOS");
				return _Psychoticdisorders_currentpsychoticdisordernos;
			}else {
				return _Psychoticdisorders_currentpsychoticdisordernos;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/currentPsychoticDisorderNOS.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_currentpsychoticdisordernos(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/currentPsychoticDisorderNOS",v);
		_Psychoticdisorders_currentpsychoticdisordernos=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Psychoticdisorders_pastpsychoticdisordernos=null;

	/**
	 * @return Returns the psychoticDisorders/pastPsychoticDisorderNOS.
	 */
	public Integer getPsychoticdisorders_pastpsychoticdisordernos() {
		try{
			if (_Psychoticdisorders_pastpsychoticdisordernos==null){
				_Psychoticdisorders_pastpsychoticdisordernos=getIntegerProperty("psychoticDisorders/pastPsychoticDisorderNOS");
				return _Psychoticdisorders_pastpsychoticdisordernos;
			}else {
				return _Psychoticdisorders_pastpsychoticdisordernos;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for psychoticDisorders/pastPsychoticDisorderNOS.
	 * @param v Value to Set.
	 */
	public void setPsychoticdisorders_pastpsychoticdisordernos(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/psychoticDisorders/pastPsychoticDisorderNOS",v);
		_Psychoticdisorders_pastpsychoticdisordernos=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Mooddisorders_currentbipolar1disorder=null;

	/**
	 * @return Returns the moodDisorders/currentBipolar1Disorder.
	 */
	public Integer getMooddisorders_currentbipolar1disorder() {
		try{
			if (_Mooddisorders_currentbipolar1disorder==null){
				_Mooddisorders_currentbipolar1disorder=getIntegerProperty("moodDisorders/currentBipolar1Disorder");
				return _Mooddisorders_currentbipolar1disorder;
			}else {
				return _Mooddisorders_currentbipolar1disorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodDisorders/currentBipolar1Disorder.
	 * @param v Value to Set.
	 */
	public void setMooddisorders_currentbipolar1disorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodDisorders/currentBipolar1Disorder",v);
		_Mooddisorders_currentbipolar1disorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Mooddisorders_pastbipolar1disorder=null;

	/**
	 * @return Returns the moodDisorders/pastBipolar1Disorder.
	 */
	public Integer getMooddisorders_pastbipolar1disorder() {
		try{
			if (_Mooddisorders_pastbipolar1disorder==null){
				_Mooddisorders_pastbipolar1disorder=getIntegerProperty("moodDisorders/pastBipolar1Disorder");
				return _Mooddisorders_pastbipolar1disorder;
			}else {
				return _Mooddisorders_pastbipolar1disorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodDisorders/pastBipolar1Disorder.
	 * @param v Value to Set.
	 */
	public void setMooddisorders_pastbipolar1disorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodDisorders/pastBipolar1Disorder",v);
		_Mooddisorders_pastbipolar1disorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Mooddisorders_currentbipolar2disorder=null;

	/**
	 * @return Returns the moodDisorders/currentBipolar2Disorder.
	 */
	public Integer getMooddisorders_currentbipolar2disorder() {
		try{
			if (_Mooddisorders_currentbipolar2disorder==null){
				_Mooddisorders_currentbipolar2disorder=getIntegerProperty("moodDisorders/currentBipolar2Disorder");
				return _Mooddisorders_currentbipolar2disorder;
			}else {
				return _Mooddisorders_currentbipolar2disorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodDisorders/currentBipolar2Disorder.
	 * @param v Value to Set.
	 */
	public void setMooddisorders_currentbipolar2disorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodDisorders/currentBipolar2Disorder",v);
		_Mooddisorders_currentbipolar2disorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Mooddisorders_pastbipolar2disorder=null;

	/**
	 * @return Returns the moodDisorders/pastBipolar2Disorder.
	 */
	public Integer getMooddisorders_pastbipolar2disorder() {
		try{
			if (_Mooddisorders_pastbipolar2disorder==null){
				_Mooddisorders_pastbipolar2disorder=getIntegerProperty("moodDisorders/pastBipolar2Disorder");
				return _Mooddisorders_pastbipolar2disorder;
			}else {
				return _Mooddisorders_pastbipolar2disorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodDisorders/pastBipolar2Disorder.
	 * @param v Value to Set.
	 */
	public void setMooddisorders_pastbipolar2disorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodDisorders/pastBipolar2Disorder",v);
		_Mooddisorders_pastbipolar2disorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Mooddisorders_currentotherbipolardisorder=null;

	/**
	 * @return Returns the moodDisorders/currentOtherBipolarDisorder.
	 */
	public Integer getMooddisorders_currentotherbipolardisorder() {
		try{
			if (_Mooddisorders_currentotherbipolardisorder==null){
				_Mooddisorders_currentotherbipolardisorder=getIntegerProperty("moodDisorders/currentOtherBipolarDisorder");
				return _Mooddisorders_currentotherbipolardisorder;
			}else {
				return _Mooddisorders_currentotherbipolardisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodDisorders/currentOtherBipolarDisorder.
	 * @param v Value to Set.
	 */
	public void setMooddisorders_currentotherbipolardisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodDisorders/currentOtherBipolarDisorder",v);
		_Mooddisorders_currentotherbipolardisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Mooddisorders_pastotherbipolardisorder=null;

	/**
	 * @return Returns the moodDisorders/pastOtherBipolarDisorder.
	 */
	public Integer getMooddisorders_pastotherbipolardisorder() {
		try{
			if (_Mooddisorders_pastotherbipolardisorder==null){
				_Mooddisorders_pastotherbipolardisorder=getIntegerProperty("moodDisorders/pastOtherBipolarDisorder");
				return _Mooddisorders_pastotherbipolardisorder;
			}else {
				return _Mooddisorders_pastotherbipolardisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodDisorders/pastOtherBipolarDisorder.
	 * @param v Value to Set.
	 */
	public void setMooddisorders_pastotherbipolardisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodDisorders/pastOtherBipolarDisorder",v);
		_Mooddisorders_pastotherbipolardisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Mooddisorders_currentmajordepressivedisorder=null;

	/**
	 * @return Returns the moodDisorders/currentMajorDepressiveDisorder.
	 */
	public Integer getMooddisorders_currentmajordepressivedisorder() {
		try{
			if (_Mooddisorders_currentmajordepressivedisorder==null){
				_Mooddisorders_currentmajordepressivedisorder=getIntegerProperty("moodDisorders/currentMajorDepressiveDisorder");
				return _Mooddisorders_currentmajordepressivedisorder;
			}else {
				return _Mooddisorders_currentmajordepressivedisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodDisorders/currentMajorDepressiveDisorder.
	 * @param v Value to Set.
	 */
	public void setMooddisorders_currentmajordepressivedisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodDisorders/currentMajorDepressiveDisorder",v);
		_Mooddisorders_currentmajordepressivedisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Mooddisorders_pastmajordepressivedisorder=null;

	/**
	 * @return Returns the moodDisorders/pastMajorDepressiveDisorder.
	 */
	public Integer getMooddisorders_pastmajordepressivedisorder() {
		try{
			if (_Mooddisorders_pastmajordepressivedisorder==null){
				_Mooddisorders_pastmajordepressivedisorder=getIntegerProperty("moodDisorders/pastMajorDepressiveDisorder");
				return _Mooddisorders_pastmajordepressivedisorder;
			}else {
				return _Mooddisorders_pastmajordepressivedisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodDisorders/pastMajorDepressiveDisorder.
	 * @param v Value to Set.
	 */
	public void setMooddisorders_pastmajordepressivedisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodDisorders/pastMajorDepressiveDisorder",v);
		_Mooddisorders_pastmajordepressivedisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Mooddisorders_currentdepressivedisordernos=null;

	/**
	 * @return Returns the moodDisorders/currentDepressiveDisorderNOS.
	 */
	public Integer getMooddisorders_currentdepressivedisordernos() {
		try{
			if (_Mooddisorders_currentdepressivedisordernos==null){
				_Mooddisorders_currentdepressivedisordernos=getIntegerProperty("moodDisorders/currentDepressiveDisorderNOS");
				return _Mooddisorders_currentdepressivedisordernos;
			}else {
				return _Mooddisorders_currentdepressivedisordernos;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodDisorders/currentDepressiveDisorderNOS.
	 * @param v Value to Set.
	 */
	public void setMooddisorders_currentdepressivedisordernos(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodDisorders/currentDepressiveDisorderNOS",v);
		_Mooddisorders_currentdepressivedisordernos=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Mooddisorders_pastdepressivedisordernos=null;

	/**
	 * @return Returns the moodDisorders/pastDepressiveDisorderNOS.
	 */
	public Integer getMooddisorders_pastdepressivedisordernos() {
		try{
			if (_Mooddisorders_pastdepressivedisordernos==null){
				_Mooddisorders_pastdepressivedisordernos=getIntegerProperty("moodDisorders/pastDepressiveDisorderNOS");
				return _Mooddisorders_pastdepressivedisordernos;
			}else {
				return _Mooddisorders_pastdepressivedisordernos;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for moodDisorders/pastDepressiveDisorderNOS.
	 * @param v Value to Set.
	 */
	public void setMooddisorders_pastdepressivedisordernos(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/moodDisorders/pastDepressiveDisorderNOS",v);
		_Mooddisorders_pastdepressivedisordernos=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentalcoholdependence=null;

	/**
	 * @return Returns the substanceUseDisorders/currentAlcoholDependence.
	 */
	public Integer getSubstanceusedisorders_currentalcoholdependence() {
		try{
			if (_Substanceusedisorders_currentalcoholdependence==null){
				_Substanceusedisorders_currentalcoholdependence=getIntegerProperty("substanceUseDisorders/currentAlcoholDependence");
				return _Substanceusedisorders_currentalcoholdependence;
			}else {
				return _Substanceusedisorders_currentalcoholdependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentAlcoholDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentalcoholdependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentAlcoholDependence",v);
		_Substanceusedisorders_currentalcoholdependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastalcoholdependence=null;

	/**
	 * @return Returns the substanceUseDisorders/pastAlcoholDependence.
	 */
	public Integer getSubstanceusedisorders_pastalcoholdependence() {
		try{
			if (_Substanceusedisorders_pastalcoholdependence==null){
				_Substanceusedisorders_pastalcoholdependence=getIntegerProperty("substanceUseDisorders/pastAlcoholDependence");
				return _Substanceusedisorders_pastalcoholdependence;
			}else {
				return _Substanceusedisorders_pastalcoholdependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastAlcoholDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastalcoholdependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastAlcoholDependence",v);
		_Substanceusedisorders_pastalcoholdependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentalcoholabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/currentAlcoholAbuse.
	 */
	public Integer getSubstanceusedisorders_currentalcoholabuse() {
		try{
			if (_Substanceusedisorders_currentalcoholabuse==null){
				_Substanceusedisorders_currentalcoholabuse=getIntegerProperty("substanceUseDisorders/currentAlcoholAbuse");
				return _Substanceusedisorders_currentalcoholabuse;
			}else {
				return _Substanceusedisorders_currentalcoholabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentAlcoholAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentalcoholabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentAlcoholAbuse",v);
		_Substanceusedisorders_currentalcoholabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastalcoholabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/pastAlcoholAbuse.
	 */
	public Integer getSubstanceusedisorders_pastalcoholabuse() {
		try{
			if (_Substanceusedisorders_pastalcoholabuse==null){
				_Substanceusedisorders_pastalcoholabuse=getIntegerProperty("substanceUseDisorders/pastAlcoholAbuse");
				return _Substanceusedisorders_pastalcoholabuse;
			}else {
				return _Substanceusedisorders_pastalcoholabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastAlcoholAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastalcoholabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastAlcoholAbuse",v);
		_Substanceusedisorders_pastalcoholabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentamphetaminedependence=null;

	/**
	 * @return Returns the substanceUseDisorders/currentAmphetamineDependence.
	 */
	public Integer getSubstanceusedisorders_currentamphetaminedependence() {
		try{
			if (_Substanceusedisorders_currentamphetaminedependence==null){
				_Substanceusedisorders_currentamphetaminedependence=getIntegerProperty("substanceUseDisorders/currentAmphetamineDependence");
				return _Substanceusedisorders_currentamphetaminedependence;
			}else {
				return _Substanceusedisorders_currentamphetaminedependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentAmphetamineDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentamphetaminedependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentAmphetamineDependence",v);
		_Substanceusedisorders_currentamphetaminedependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastamphetaminedependence=null;

	/**
	 * @return Returns the substanceUseDisorders/pastAmphetamineDependence.
	 */
	public Integer getSubstanceusedisorders_pastamphetaminedependence() {
		try{
			if (_Substanceusedisorders_pastamphetaminedependence==null){
				_Substanceusedisorders_pastamphetaminedependence=getIntegerProperty("substanceUseDisorders/pastAmphetamineDependence");
				return _Substanceusedisorders_pastamphetaminedependence;
			}else {
				return _Substanceusedisorders_pastamphetaminedependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastAmphetamineDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastamphetaminedependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastAmphetamineDependence",v);
		_Substanceusedisorders_pastamphetaminedependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentamphetamineabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/currentAmphetamineAbuse.
	 */
	public Integer getSubstanceusedisorders_currentamphetamineabuse() {
		try{
			if (_Substanceusedisorders_currentamphetamineabuse==null){
				_Substanceusedisorders_currentamphetamineabuse=getIntegerProperty("substanceUseDisorders/currentAmphetamineAbuse");
				return _Substanceusedisorders_currentamphetamineabuse;
			}else {
				return _Substanceusedisorders_currentamphetamineabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentAmphetamineAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentamphetamineabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentAmphetamineAbuse",v);
		_Substanceusedisorders_currentamphetamineabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastamphetamineabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/pastAmphetamineAbuse.
	 */
	public Integer getSubstanceusedisorders_pastamphetamineabuse() {
		try{
			if (_Substanceusedisorders_pastamphetamineabuse==null){
				_Substanceusedisorders_pastamphetamineabuse=getIntegerProperty("substanceUseDisorders/pastAmphetamineAbuse");
				return _Substanceusedisorders_pastamphetamineabuse;
			}else {
				return _Substanceusedisorders_pastamphetamineabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastAmphetamineAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastamphetamineabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastAmphetamineAbuse",v);
		_Substanceusedisorders_pastamphetamineabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentcannabisdependence=null;

	/**
	 * @return Returns the substanceUseDisorders/currentCannabisDependence.
	 */
	public Integer getSubstanceusedisorders_currentcannabisdependence() {
		try{
			if (_Substanceusedisorders_currentcannabisdependence==null){
				_Substanceusedisorders_currentcannabisdependence=getIntegerProperty("substanceUseDisorders/currentCannabisDependence");
				return _Substanceusedisorders_currentcannabisdependence;
			}else {
				return _Substanceusedisorders_currentcannabisdependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentCannabisDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentcannabisdependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentCannabisDependence",v);
		_Substanceusedisorders_currentcannabisdependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastcannabisdependence=null;

	/**
	 * @return Returns the substanceUseDisorders/pastCannabisDependence.
	 */
	public Integer getSubstanceusedisorders_pastcannabisdependence() {
		try{
			if (_Substanceusedisorders_pastcannabisdependence==null){
				_Substanceusedisorders_pastcannabisdependence=getIntegerProperty("substanceUseDisorders/pastCannabisDependence");
				return _Substanceusedisorders_pastcannabisdependence;
			}else {
				return _Substanceusedisorders_pastcannabisdependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastCannabisDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastcannabisdependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastCannabisDependence",v);
		_Substanceusedisorders_pastcannabisdependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentcannabisabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/currentCannabisAbuse.
	 */
	public Integer getSubstanceusedisorders_currentcannabisabuse() {
		try{
			if (_Substanceusedisorders_currentcannabisabuse==null){
				_Substanceusedisorders_currentcannabisabuse=getIntegerProperty("substanceUseDisorders/currentCannabisAbuse");
				return _Substanceusedisorders_currentcannabisabuse;
			}else {
				return _Substanceusedisorders_currentcannabisabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentCannabisAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentcannabisabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentCannabisAbuse",v);
		_Substanceusedisorders_currentcannabisabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastcannabisabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/pastCannabisAbuse.
	 */
	public Integer getSubstanceusedisorders_pastcannabisabuse() {
		try{
			if (_Substanceusedisorders_pastcannabisabuse==null){
				_Substanceusedisorders_pastcannabisabuse=getIntegerProperty("substanceUseDisorders/pastCannabisAbuse");
				return _Substanceusedisorders_pastcannabisabuse;
			}else {
				return _Substanceusedisorders_pastcannabisabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastCannabisAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastcannabisabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastCannabisAbuse",v);
		_Substanceusedisorders_pastcannabisabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentcocainedependence=null;

	/**
	 * @return Returns the substanceUseDisorders/currentCocaineDependence.
	 */
	public Integer getSubstanceusedisorders_currentcocainedependence() {
		try{
			if (_Substanceusedisorders_currentcocainedependence==null){
				_Substanceusedisorders_currentcocainedependence=getIntegerProperty("substanceUseDisorders/currentCocaineDependence");
				return _Substanceusedisorders_currentcocainedependence;
			}else {
				return _Substanceusedisorders_currentcocainedependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentCocaineDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentcocainedependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentCocaineDependence",v);
		_Substanceusedisorders_currentcocainedependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastcocainedependence=null;

	/**
	 * @return Returns the substanceUseDisorders/pastCocaineDependence.
	 */
	public Integer getSubstanceusedisorders_pastcocainedependence() {
		try{
			if (_Substanceusedisorders_pastcocainedependence==null){
				_Substanceusedisorders_pastcocainedependence=getIntegerProperty("substanceUseDisorders/pastCocaineDependence");
				return _Substanceusedisorders_pastcocainedependence;
			}else {
				return _Substanceusedisorders_pastcocainedependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastCocaineDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastcocainedependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastCocaineDependence",v);
		_Substanceusedisorders_pastcocainedependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentcocaineabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/currentCocaineAbuse.
	 */
	public Integer getSubstanceusedisorders_currentcocaineabuse() {
		try{
			if (_Substanceusedisorders_currentcocaineabuse==null){
				_Substanceusedisorders_currentcocaineabuse=getIntegerProperty("substanceUseDisorders/currentCocaineAbuse");
				return _Substanceusedisorders_currentcocaineabuse;
			}else {
				return _Substanceusedisorders_currentcocaineabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentCocaineAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentcocaineabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentCocaineAbuse",v);
		_Substanceusedisorders_currentcocaineabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastcocaineabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/pastCocaineAbuse.
	 */
	public Integer getSubstanceusedisorders_pastcocaineabuse() {
		try{
			if (_Substanceusedisorders_pastcocaineabuse==null){
				_Substanceusedisorders_pastcocaineabuse=getIntegerProperty("substanceUseDisorders/pastCocaineAbuse");
				return _Substanceusedisorders_pastcocaineabuse;
			}else {
				return _Substanceusedisorders_pastcocaineabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastCocaineAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastcocaineabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastCocaineAbuse",v);
		_Substanceusedisorders_pastcocaineabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currenthallucinogendependence=null;

	/**
	 * @return Returns the substanceUseDisorders/currentHallucinogenDependence.
	 */
	public Integer getSubstanceusedisorders_currenthallucinogendependence() {
		try{
			if (_Substanceusedisorders_currenthallucinogendependence==null){
				_Substanceusedisorders_currenthallucinogendependence=getIntegerProperty("substanceUseDisorders/currentHallucinogenDependence");
				return _Substanceusedisorders_currenthallucinogendependence;
			}else {
				return _Substanceusedisorders_currenthallucinogendependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentHallucinogenDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currenthallucinogendependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentHallucinogenDependence",v);
		_Substanceusedisorders_currenthallucinogendependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pasthallucinogendependence=null;

	/**
	 * @return Returns the substanceUseDisorders/pastHallucinogenDependence.
	 */
	public Integer getSubstanceusedisorders_pasthallucinogendependence() {
		try{
			if (_Substanceusedisorders_pasthallucinogendependence==null){
				_Substanceusedisorders_pasthallucinogendependence=getIntegerProperty("substanceUseDisorders/pastHallucinogenDependence");
				return _Substanceusedisorders_pasthallucinogendependence;
			}else {
				return _Substanceusedisorders_pasthallucinogendependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastHallucinogenDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pasthallucinogendependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastHallucinogenDependence",v);
		_Substanceusedisorders_pasthallucinogendependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currenthallucinogenabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/currentHallucinogenAbuse.
	 */
	public Integer getSubstanceusedisorders_currenthallucinogenabuse() {
		try{
			if (_Substanceusedisorders_currenthallucinogenabuse==null){
				_Substanceusedisorders_currenthallucinogenabuse=getIntegerProperty("substanceUseDisorders/currentHallucinogenAbuse");
				return _Substanceusedisorders_currenthallucinogenabuse;
			}else {
				return _Substanceusedisorders_currenthallucinogenabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentHallucinogenAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currenthallucinogenabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentHallucinogenAbuse",v);
		_Substanceusedisorders_currenthallucinogenabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pasthallucinogenabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/pastHallucinogenAbuse.
	 */
	public Integer getSubstanceusedisorders_pasthallucinogenabuse() {
		try{
			if (_Substanceusedisorders_pasthallucinogenabuse==null){
				_Substanceusedisorders_pasthallucinogenabuse=getIntegerProperty("substanceUseDisorders/pastHallucinogenAbuse");
				return _Substanceusedisorders_pasthallucinogenabuse;
			}else {
				return _Substanceusedisorders_pasthallucinogenabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastHallucinogenAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pasthallucinogenabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastHallucinogenAbuse",v);
		_Substanceusedisorders_pasthallucinogenabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentopioiddependence=null;

	/**
	 * @return Returns the substanceUseDisorders/currentOpioidDependence.
	 */
	public Integer getSubstanceusedisorders_currentopioiddependence() {
		try{
			if (_Substanceusedisorders_currentopioiddependence==null){
				_Substanceusedisorders_currentopioiddependence=getIntegerProperty("substanceUseDisorders/currentOpioidDependence");
				return _Substanceusedisorders_currentopioiddependence;
			}else {
				return _Substanceusedisorders_currentopioiddependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentOpioidDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentopioiddependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentOpioidDependence",v);
		_Substanceusedisorders_currentopioiddependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastopioiddependence=null;

	/**
	 * @return Returns the substanceUseDisorders/pastOpioidDependence.
	 */
	public Integer getSubstanceusedisorders_pastopioiddependence() {
		try{
			if (_Substanceusedisorders_pastopioiddependence==null){
				_Substanceusedisorders_pastopioiddependence=getIntegerProperty("substanceUseDisorders/pastOpioidDependence");
				return _Substanceusedisorders_pastopioiddependence;
			}else {
				return _Substanceusedisorders_pastopioiddependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastOpioidDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastopioiddependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastOpioidDependence",v);
		_Substanceusedisorders_pastopioiddependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentopioidabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/currentOpioidAbuse.
	 */
	public Integer getSubstanceusedisorders_currentopioidabuse() {
		try{
			if (_Substanceusedisorders_currentopioidabuse==null){
				_Substanceusedisorders_currentopioidabuse=getIntegerProperty("substanceUseDisorders/currentOpioidAbuse");
				return _Substanceusedisorders_currentopioidabuse;
			}else {
				return _Substanceusedisorders_currentopioidabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentOpioidAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentopioidabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentOpioidAbuse",v);
		_Substanceusedisorders_currentopioidabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastopioidabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/pastOpioidAbuse.
	 */
	public Integer getSubstanceusedisorders_pastopioidabuse() {
		try{
			if (_Substanceusedisorders_pastopioidabuse==null){
				_Substanceusedisorders_pastopioidabuse=getIntegerProperty("substanceUseDisorders/pastOpioidAbuse");
				return _Substanceusedisorders_pastopioidabuse;
			}else {
				return _Substanceusedisorders_pastopioidabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastOpioidAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastopioidabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastOpioidAbuse",v);
		_Substanceusedisorders_pastopioidabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentphencyclidinedependence=null;

	/**
	 * @return Returns the substanceUseDisorders/currentPhencyclidineDependence.
	 */
	public Integer getSubstanceusedisorders_currentphencyclidinedependence() {
		try{
			if (_Substanceusedisorders_currentphencyclidinedependence==null){
				_Substanceusedisorders_currentphencyclidinedependence=getIntegerProperty("substanceUseDisorders/currentPhencyclidineDependence");
				return _Substanceusedisorders_currentphencyclidinedependence;
			}else {
				return _Substanceusedisorders_currentphencyclidinedependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentPhencyclidineDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentphencyclidinedependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentPhencyclidineDependence",v);
		_Substanceusedisorders_currentphencyclidinedependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastphencyclidinedependence=null;

	/**
	 * @return Returns the substanceUseDisorders/pastPhencyclidineDependence.
	 */
	public Integer getSubstanceusedisorders_pastphencyclidinedependence() {
		try{
			if (_Substanceusedisorders_pastphencyclidinedependence==null){
				_Substanceusedisorders_pastphencyclidinedependence=getIntegerProperty("substanceUseDisorders/pastPhencyclidineDependence");
				return _Substanceusedisorders_pastphencyclidinedependence;
			}else {
				return _Substanceusedisorders_pastphencyclidinedependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastPhencyclidineDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastphencyclidinedependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastPhencyclidineDependence",v);
		_Substanceusedisorders_pastphencyclidinedependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentphencyclidineabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/currentPhencyclidineAbuse.
	 */
	public Integer getSubstanceusedisorders_currentphencyclidineabuse() {
		try{
			if (_Substanceusedisorders_currentphencyclidineabuse==null){
				_Substanceusedisorders_currentphencyclidineabuse=getIntegerProperty("substanceUseDisorders/currentPhencyclidineAbuse");
				return _Substanceusedisorders_currentphencyclidineabuse;
			}else {
				return _Substanceusedisorders_currentphencyclidineabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentPhencyclidineAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentphencyclidineabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentPhencyclidineAbuse",v);
		_Substanceusedisorders_currentphencyclidineabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastphencyclidineabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/pastPhencyclidineAbuse.
	 */
	public Integer getSubstanceusedisorders_pastphencyclidineabuse() {
		try{
			if (_Substanceusedisorders_pastphencyclidineabuse==null){
				_Substanceusedisorders_pastphencyclidineabuse=getIntegerProperty("substanceUseDisorders/pastPhencyclidineAbuse");
				return _Substanceusedisorders_pastphencyclidineabuse;
			}else {
				return _Substanceusedisorders_pastphencyclidineabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastPhencyclidineAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastphencyclidineabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastPhencyclidineAbuse",v);
		_Substanceusedisorders_pastphencyclidineabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentsedativehypnoticanxiolyticdependence=null;

	/**
	 * @return Returns the substanceUseDisorders/currentSedativeHypnoticAnxiolyticDependence.
	 */
	public Integer getSubstanceusedisorders_currentsedativehypnoticanxiolyticdependence() {
		try{
			if (_Substanceusedisorders_currentsedativehypnoticanxiolyticdependence==null){
				_Substanceusedisorders_currentsedativehypnoticanxiolyticdependence=getIntegerProperty("substanceUseDisorders/currentSedativeHypnoticAnxiolyticDependence");
				return _Substanceusedisorders_currentsedativehypnoticanxiolyticdependence;
			}else {
				return _Substanceusedisorders_currentsedativehypnoticanxiolyticdependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentSedativeHypnoticAnxiolyticDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentsedativehypnoticanxiolyticdependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentSedativeHypnoticAnxiolyticDependence",v);
		_Substanceusedisorders_currentsedativehypnoticanxiolyticdependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastsedativehypnoticanxiolyticdependence=null;

	/**
	 * @return Returns the substanceUseDisorders/pastSedativeHypnoticAnxiolyticDependence.
	 */
	public Integer getSubstanceusedisorders_pastsedativehypnoticanxiolyticdependence() {
		try{
			if (_Substanceusedisorders_pastsedativehypnoticanxiolyticdependence==null){
				_Substanceusedisorders_pastsedativehypnoticanxiolyticdependence=getIntegerProperty("substanceUseDisorders/pastSedativeHypnoticAnxiolyticDependence");
				return _Substanceusedisorders_pastsedativehypnoticanxiolyticdependence;
			}else {
				return _Substanceusedisorders_pastsedativehypnoticanxiolyticdependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastSedativeHypnoticAnxiolyticDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastsedativehypnoticanxiolyticdependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastSedativeHypnoticAnxiolyticDependence",v);
		_Substanceusedisorders_pastsedativehypnoticanxiolyticdependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentsedativehypnoticanxiolyticabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/currentSedativeHypnoticAnxiolyticAbuse.
	 */
	public Integer getSubstanceusedisorders_currentsedativehypnoticanxiolyticabuse() {
		try{
			if (_Substanceusedisorders_currentsedativehypnoticanxiolyticabuse==null){
				_Substanceusedisorders_currentsedativehypnoticanxiolyticabuse=getIntegerProperty("substanceUseDisorders/currentSedativeHypnoticAnxiolyticAbuse");
				return _Substanceusedisorders_currentsedativehypnoticanxiolyticabuse;
			}else {
				return _Substanceusedisorders_currentsedativehypnoticanxiolyticabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentSedativeHypnoticAnxiolyticAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentsedativehypnoticanxiolyticabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentSedativeHypnoticAnxiolyticAbuse",v);
		_Substanceusedisorders_currentsedativehypnoticanxiolyticabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastsedativehypnoticanxiolyticabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/pastSedativeHypnoticAnxiolyticAbuse.
	 */
	public Integer getSubstanceusedisorders_pastsedativehypnoticanxiolyticabuse() {
		try{
			if (_Substanceusedisorders_pastsedativehypnoticanxiolyticabuse==null){
				_Substanceusedisorders_pastsedativehypnoticanxiolyticabuse=getIntegerProperty("substanceUseDisorders/pastSedativeHypnoticAnxiolyticAbuse");
				return _Substanceusedisorders_pastsedativehypnoticanxiolyticabuse;
			}else {
				return _Substanceusedisorders_pastsedativehypnoticanxiolyticabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastSedativeHypnoticAnxiolyticAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastsedativehypnoticanxiolyticabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastSedativeHypnoticAnxiolyticAbuse",v);
		_Substanceusedisorders_pastsedativehypnoticanxiolyticabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentpolysubstancedependence=null;

	/**
	 * @return Returns the substanceUseDisorders/currentPolysubstanceDependence.
	 */
	public Integer getSubstanceusedisorders_currentpolysubstancedependence() {
		try{
			if (_Substanceusedisorders_currentpolysubstancedependence==null){
				_Substanceusedisorders_currentpolysubstancedependence=getIntegerProperty("substanceUseDisorders/currentPolysubstanceDependence");
				return _Substanceusedisorders_currentpolysubstancedependence;
			}else {
				return _Substanceusedisorders_currentpolysubstancedependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentPolysubstanceDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentpolysubstancedependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentPolysubstanceDependence",v);
		_Substanceusedisorders_currentpolysubstancedependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastpolysubstancedependence=null;

	/**
	 * @return Returns the substanceUseDisorders/pastPolysubstanceDependence.
	 */
	public Integer getSubstanceusedisorders_pastpolysubstancedependence() {
		try{
			if (_Substanceusedisorders_pastpolysubstancedependence==null){
				_Substanceusedisorders_pastpolysubstancedependence=getIntegerProperty("substanceUseDisorders/pastPolysubstanceDependence");
				return _Substanceusedisorders_pastpolysubstancedependence;
			}else {
				return _Substanceusedisorders_pastpolysubstancedependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastPolysubstanceDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastpolysubstancedependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastPolysubstanceDependence",v);
		_Substanceusedisorders_pastpolysubstancedependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentotherorunknowndependence=null;

	/**
	 * @return Returns the substanceUseDisorders/currentOtherOrUnknownDependence.
	 */
	public Integer getSubstanceusedisorders_currentotherorunknowndependence() {
		try{
			if (_Substanceusedisorders_currentotherorunknowndependence==null){
				_Substanceusedisorders_currentotherorunknowndependence=getIntegerProperty("substanceUseDisorders/currentOtherOrUnknownDependence");
				return _Substanceusedisorders_currentotherorunknowndependence;
			}else {
				return _Substanceusedisorders_currentotherorunknowndependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentOtherOrUnknownDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentotherorunknowndependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentOtherOrUnknownDependence",v);
		_Substanceusedisorders_currentotherorunknowndependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastotherorunknowndependence=null;

	/**
	 * @return Returns the substanceUseDisorders/pastOtherOrUnknownDependence.
	 */
	public Integer getSubstanceusedisorders_pastotherorunknowndependence() {
		try{
			if (_Substanceusedisorders_pastotherorunknowndependence==null){
				_Substanceusedisorders_pastotherorunknowndependence=getIntegerProperty("substanceUseDisorders/pastOtherOrUnknownDependence");
				return _Substanceusedisorders_pastotherorunknowndependence;
			}else {
				return _Substanceusedisorders_pastotherorunknowndependence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastOtherOrUnknownDependence.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastotherorunknowndependence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastOtherOrUnknownDependence",v);
		_Substanceusedisorders_pastotherorunknowndependence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_currentotherorunknownabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/currentOtherOrUnknownAbuse.
	 */
	public Integer getSubstanceusedisorders_currentotherorunknownabuse() {
		try{
			if (_Substanceusedisorders_currentotherorunknownabuse==null){
				_Substanceusedisorders_currentotherorunknownabuse=getIntegerProperty("substanceUseDisorders/currentOtherOrUnknownAbuse");
				return _Substanceusedisorders_currentotherorunknownabuse;
			}else {
				return _Substanceusedisorders_currentotherorunknownabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/currentOtherOrUnknownAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_currentotherorunknownabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/currentOtherOrUnknownAbuse",v);
		_Substanceusedisorders_currentotherorunknownabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Substanceusedisorders_pastotherorunknownabuse=null;

	/**
	 * @return Returns the substanceUseDisorders/pastOtherOrUnknownAbuse.
	 */
	public Integer getSubstanceusedisorders_pastotherorunknownabuse() {
		try{
			if (_Substanceusedisorders_pastotherorunknownabuse==null){
				_Substanceusedisorders_pastotherorunknownabuse=getIntegerProperty("substanceUseDisorders/pastOtherOrUnknownAbuse");
				return _Substanceusedisorders_pastotherorunknownabuse;
			}else {
				return _Substanceusedisorders_pastotherorunknownabuse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for substanceUseDisorders/pastOtherOrUnknownAbuse.
	 * @param v Value to Set.
	 */
	public void setSubstanceusedisorders_pastotherorunknownabuse(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/substanceUseDisorders/pastOtherOrUnknownAbuse",v);
		_Substanceusedisorders_pastotherorunknownabuse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_currentpanicwithagoraphobia=null;

	/**
	 * @return Returns the anxietyDisorders/currentPanicWithAgoraphobia.
	 */
	public Integer getAnxietydisorders_currentpanicwithagoraphobia() {
		try{
			if (_Anxietydisorders_currentpanicwithagoraphobia==null){
				_Anxietydisorders_currentpanicwithagoraphobia=getIntegerProperty("anxietyDisorders/currentPanicWithAgoraphobia");
				return _Anxietydisorders_currentpanicwithagoraphobia;
			}else {
				return _Anxietydisorders_currentpanicwithagoraphobia;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/currentPanicWithAgoraphobia.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_currentpanicwithagoraphobia(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/currentPanicWithAgoraphobia",v);
		_Anxietydisorders_currentpanicwithagoraphobia=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_pastpanicwithagoraphobia=null;

	/**
	 * @return Returns the anxietyDisorders/pastPanicWithAgoraphobia.
	 */
	public Integer getAnxietydisorders_pastpanicwithagoraphobia() {
		try{
			if (_Anxietydisorders_pastpanicwithagoraphobia==null){
				_Anxietydisorders_pastpanicwithagoraphobia=getIntegerProperty("anxietyDisorders/pastPanicWithAgoraphobia");
				return _Anxietydisorders_pastpanicwithagoraphobia;
			}else {
				return _Anxietydisorders_pastpanicwithagoraphobia;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/pastPanicWithAgoraphobia.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_pastpanicwithagoraphobia(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/pastPanicWithAgoraphobia",v);
		_Anxietydisorders_pastpanicwithagoraphobia=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_currentpanicwithoutagoraphobia=null;

	/**
	 * @return Returns the anxietyDisorders/currentPanicWithoutAgoraphobia.
	 */
	public Integer getAnxietydisorders_currentpanicwithoutagoraphobia() {
		try{
			if (_Anxietydisorders_currentpanicwithoutagoraphobia==null){
				_Anxietydisorders_currentpanicwithoutagoraphobia=getIntegerProperty("anxietyDisorders/currentPanicWithoutAgoraphobia");
				return _Anxietydisorders_currentpanicwithoutagoraphobia;
			}else {
				return _Anxietydisorders_currentpanicwithoutagoraphobia;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/currentPanicWithoutAgoraphobia.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_currentpanicwithoutagoraphobia(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/currentPanicWithoutAgoraphobia",v);
		_Anxietydisorders_currentpanicwithoutagoraphobia=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_pastpanicwithoutagoraphobia=null;

	/**
	 * @return Returns the anxietyDisorders/pastPanicWithoutAgoraphobia.
	 */
	public Integer getAnxietydisorders_pastpanicwithoutagoraphobia() {
		try{
			if (_Anxietydisorders_pastpanicwithoutagoraphobia==null){
				_Anxietydisorders_pastpanicwithoutagoraphobia=getIntegerProperty("anxietyDisorders/pastPanicWithoutAgoraphobia");
				return _Anxietydisorders_pastpanicwithoutagoraphobia;
			}else {
				return _Anxietydisorders_pastpanicwithoutagoraphobia;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/pastPanicWithoutAgoraphobia.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_pastpanicwithoutagoraphobia(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/pastPanicWithoutAgoraphobia",v);
		_Anxietydisorders_pastpanicwithoutagoraphobia=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_currentagoraphobiawithoutpanichx=null;

	/**
	 * @return Returns the anxietyDisorders/currentAgoraphobiaWithoutPanicHx.
	 */
	public Integer getAnxietydisorders_currentagoraphobiawithoutpanichx() {
		try{
			if (_Anxietydisorders_currentagoraphobiawithoutpanichx==null){
				_Anxietydisorders_currentagoraphobiawithoutpanichx=getIntegerProperty("anxietyDisorders/currentAgoraphobiaWithoutPanicHx");
				return _Anxietydisorders_currentagoraphobiawithoutpanichx;
			}else {
				return _Anxietydisorders_currentagoraphobiawithoutpanichx;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/currentAgoraphobiaWithoutPanicHx.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_currentagoraphobiawithoutpanichx(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/currentAgoraphobiaWithoutPanicHx",v);
		_Anxietydisorders_currentagoraphobiawithoutpanichx=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_pastagoraphobiawithoutpanichx=null;

	/**
	 * @return Returns the anxietyDisorders/pastAgoraphobiaWithoutPanicHx.
	 */
	public Integer getAnxietydisorders_pastagoraphobiawithoutpanichx() {
		try{
			if (_Anxietydisorders_pastagoraphobiawithoutpanichx==null){
				_Anxietydisorders_pastagoraphobiawithoutpanichx=getIntegerProperty("anxietyDisorders/pastAgoraphobiaWithoutPanicHx");
				return _Anxietydisorders_pastagoraphobiawithoutpanichx;
			}else {
				return _Anxietydisorders_pastagoraphobiawithoutpanichx;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/pastAgoraphobiaWithoutPanicHx.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_pastagoraphobiawithoutpanichx(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/pastAgoraphobiaWithoutPanicHx",v);
		_Anxietydisorders_pastagoraphobiawithoutpanichx=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_currentsocialphobia=null;

	/**
	 * @return Returns the anxietyDisorders/currentSocialPhobia.
	 */
	public Integer getAnxietydisorders_currentsocialphobia() {
		try{
			if (_Anxietydisorders_currentsocialphobia==null){
				_Anxietydisorders_currentsocialphobia=getIntegerProperty("anxietyDisorders/currentSocialPhobia");
				return _Anxietydisorders_currentsocialphobia;
			}else {
				return _Anxietydisorders_currentsocialphobia;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/currentSocialPhobia.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_currentsocialphobia(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/currentSocialPhobia",v);
		_Anxietydisorders_currentsocialphobia=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_pastsocialphobia=null;

	/**
	 * @return Returns the anxietyDisorders/pastSocialPhobia.
	 */
	public Integer getAnxietydisorders_pastsocialphobia() {
		try{
			if (_Anxietydisorders_pastsocialphobia==null){
				_Anxietydisorders_pastsocialphobia=getIntegerProperty("anxietyDisorders/pastSocialPhobia");
				return _Anxietydisorders_pastsocialphobia;
			}else {
				return _Anxietydisorders_pastsocialphobia;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/pastSocialPhobia.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_pastsocialphobia(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/pastSocialPhobia",v);
		_Anxietydisorders_pastsocialphobia=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_currentspecificphobia=null;

	/**
	 * @return Returns the anxietyDisorders/currentSpecificPhobia.
	 */
	public Integer getAnxietydisorders_currentspecificphobia() {
		try{
			if (_Anxietydisorders_currentspecificphobia==null){
				_Anxietydisorders_currentspecificphobia=getIntegerProperty("anxietyDisorders/currentSpecificPhobia");
				return _Anxietydisorders_currentspecificphobia;
			}else {
				return _Anxietydisorders_currentspecificphobia;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/currentSpecificPhobia.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_currentspecificphobia(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/currentSpecificPhobia",v);
		_Anxietydisorders_currentspecificphobia=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_pastspecificphobia=null;

	/**
	 * @return Returns the anxietyDisorders/pastSpecificPhobia.
	 */
	public Integer getAnxietydisorders_pastspecificphobia() {
		try{
			if (_Anxietydisorders_pastspecificphobia==null){
				_Anxietydisorders_pastspecificphobia=getIntegerProperty("anxietyDisorders/pastSpecificPhobia");
				return _Anxietydisorders_pastspecificphobia;
			}else {
				return _Anxietydisorders_pastspecificphobia;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/pastSpecificPhobia.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_pastspecificphobia(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/pastSpecificPhobia",v);
		_Anxietydisorders_pastspecificphobia=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_currentocd=null;

	/**
	 * @return Returns the anxietyDisorders/currentOCD.
	 */
	public Integer getAnxietydisorders_currentocd() {
		try{
			if (_Anxietydisorders_currentocd==null){
				_Anxietydisorders_currentocd=getIntegerProperty("anxietyDisorders/currentOCD");
				return _Anxietydisorders_currentocd;
			}else {
				return _Anxietydisorders_currentocd;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/currentOCD.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_currentocd(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/currentOCD",v);
		_Anxietydisorders_currentocd=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_pastocd=null;

	/**
	 * @return Returns the anxietyDisorders/pastOCD.
	 */
	public Integer getAnxietydisorders_pastocd() {
		try{
			if (_Anxietydisorders_pastocd==null){
				_Anxietydisorders_pastocd=getIntegerProperty("anxietyDisorders/pastOCD");
				return _Anxietydisorders_pastocd;
			}else {
				return _Anxietydisorders_pastocd;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/pastOCD.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_pastocd(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/pastOCD",v);
		_Anxietydisorders_pastocd=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_currentptsd=null;

	/**
	 * @return Returns the anxietyDisorders/currentPTSD.
	 */
	public Integer getAnxietydisorders_currentptsd() {
		try{
			if (_Anxietydisorders_currentptsd==null){
				_Anxietydisorders_currentptsd=getIntegerProperty("anxietyDisorders/currentPTSD");
				return _Anxietydisorders_currentptsd;
			}else {
				return _Anxietydisorders_currentptsd;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/currentPTSD.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_currentptsd(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/currentPTSD",v);
		_Anxietydisorders_currentptsd=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_pastptsd=null;

	/**
	 * @return Returns the anxietyDisorders/pastPTSD.
	 */
	public Integer getAnxietydisorders_pastptsd() {
		try{
			if (_Anxietydisorders_pastptsd==null){
				_Anxietydisorders_pastptsd=getIntegerProperty("anxietyDisorders/pastPTSD");
				return _Anxietydisorders_pastptsd;
			}else {
				return _Anxietydisorders_pastptsd;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/pastPTSD.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_pastptsd(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/pastPTSD",v);
		_Anxietydisorders_pastptsd=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_currentgeneralizedanxietydisorder=null;

	/**
	 * @return Returns the anxietyDisorders/currentGeneralizedAnxietyDisorder.
	 */
	public Integer getAnxietydisorders_currentgeneralizedanxietydisorder() {
		try{
			if (_Anxietydisorders_currentgeneralizedanxietydisorder==null){
				_Anxietydisorders_currentgeneralizedanxietydisorder=getIntegerProperty("anxietyDisorders/currentGeneralizedAnxietyDisorder");
				return _Anxietydisorders_currentgeneralizedanxietydisorder;
			}else {
				return _Anxietydisorders_currentgeneralizedanxietydisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/currentGeneralizedAnxietyDisorder.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_currentgeneralizedanxietydisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/currentGeneralizedAnxietyDisorder",v);
		_Anxietydisorders_currentgeneralizedanxietydisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_currentanxietyduetomedicalcondition=null;

	/**
	 * @return Returns the anxietyDisorders/currentAnxietyDueToMedicalCondition.
	 */
	public Integer getAnxietydisorders_currentanxietyduetomedicalcondition() {
		try{
			if (_Anxietydisorders_currentanxietyduetomedicalcondition==null){
				_Anxietydisorders_currentanxietyduetomedicalcondition=getIntegerProperty("anxietyDisorders/currentAnxietyDueToMedicalCondition");
				return _Anxietydisorders_currentanxietyduetomedicalcondition;
			}else {
				return _Anxietydisorders_currentanxietyduetomedicalcondition;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/currentAnxietyDueToMedicalCondition.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_currentanxietyduetomedicalcondition(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/currentAnxietyDueToMedicalCondition",v);
		_Anxietydisorders_currentanxietyduetomedicalcondition=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_pastanxietyduetomedicalcondition=null;

	/**
	 * @return Returns the anxietyDisorders/pastAnxietyDueToMedicalCondition.
	 */
	public Integer getAnxietydisorders_pastanxietyduetomedicalcondition() {
		try{
			if (_Anxietydisorders_pastanxietyduetomedicalcondition==null){
				_Anxietydisorders_pastanxietyduetomedicalcondition=getIntegerProperty("anxietyDisorders/pastAnxietyDueToMedicalCondition");
				return _Anxietydisorders_pastanxietyduetomedicalcondition;
			}else {
				return _Anxietydisorders_pastanxietyduetomedicalcondition;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/pastAnxietyDueToMedicalCondition.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_pastanxietyduetomedicalcondition(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/pastAnxietyDueToMedicalCondition",v);
		_Anxietydisorders_pastanxietyduetomedicalcondition=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_currentsubstanceinducedanxietydisorder=null;

	/**
	 * @return Returns the anxietyDisorders/currentSubstanceInducedAnxietyDisorder.
	 */
	public Integer getAnxietydisorders_currentsubstanceinducedanxietydisorder() {
		try{
			if (_Anxietydisorders_currentsubstanceinducedanxietydisorder==null){
				_Anxietydisorders_currentsubstanceinducedanxietydisorder=getIntegerProperty("anxietyDisorders/currentSubstanceInducedAnxietyDisorder");
				return _Anxietydisorders_currentsubstanceinducedanxietydisorder;
			}else {
				return _Anxietydisorders_currentsubstanceinducedanxietydisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/currentSubstanceInducedAnxietyDisorder.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_currentsubstanceinducedanxietydisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/currentSubstanceInducedAnxietyDisorder",v);
		_Anxietydisorders_currentsubstanceinducedanxietydisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_pastsubstanceinducedanxietydisorder=null;

	/**
	 * @return Returns the anxietyDisorders/pastSubstanceInducedAnxietyDisorder.
	 */
	public Integer getAnxietydisorders_pastsubstanceinducedanxietydisorder() {
		try{
			if (_Anxietydisorders_pastsubstanceinducedanxietydisorder==null){
				_Anxietydisorders_pastsubstanceinducedanxietydisorder=getIntegerProperty("anxietyDisorders/pastSubstanceInducedAnxietyDisorder");
				return _Anxietydisorders_pastsubstanceinducedanxietydisorder;
			}else {
				return _Anxietydisorders_pastsubstanceinducedanxietydisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/pastSubstanceInducedAnxietyDisorder.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_pastsubstanceinducedanxietydisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/pastSubstanceInducedAnxietyDisorder",v);
		_Anxietydisorders_pastsubstanceinducedanxietydisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_currentanxietydisordernos=null;

	/**
	 * @return Returns the anxietyDisorders/currentAnxietyDisorderNOS.
	 */
	public Integer getAnxietydisorders_currentanxietydisordernos() {
		try{
			if (_Anxietydisorders_currentanxietydisordernos==null){
				_Anxietydisorders_currentanxietydisordernos=getIntegerProperty("anxietyDisorders/currentAnxietyDisorderNOS");
				return _Anxietydisorders_currentanxietydisordernos;
			}else {
				return _Anxietydisorders_currentanxietydisordernos;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/currentAnxietyDisorderNOS.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_currentanxietydisordernos(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/currentAnxietyDisorderNOS",v);
		_Anxietydisorders_currentanxietydisordernos=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Anxietydisorders_pastanxietydisordernos=null;

	/**
	 * @return Returns the anxietyDisorders/pastAnxietyDisorderNOS.
	 */
	public Integer getAnxietydisorders_pastanxietydisordernos() {
		try{
			if (_Anxietydisorders_pastanxietydisordernos==null){
				_Anxietydisorders_pastanxietydisordernos=getIntegerProperty("anxietyDisorders/pastAnxietyDisorderNOS");
				return _Anxietydisorders_pastanxietydisordernos;
			}else {
				return _Anxietydisorders_pastanxietydisordernos;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for anxietyDisorders/pastAnxietyDisorderNOS.
	 * @param v Value to Set.
	 */
	public void setAnxietydisorders_pastanxietydisordernos(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/anxietyDisorders/pastAnxietyDisorderNOS",v);
		_Anxietydisorders_pastanxietydisordernos=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Somatoformdisorders_somatizationdisorder=null;

	/**
	 * @return Returns the somatoformDisorders/somatizationDisorder.
	 */
	public Integer getSomatoformdisorders_somatizationdisorder() {
		try{
			if (_Somatoformdisorders_somatizationdisorder==null){
				_Somatoformdisorders_somatizationdisorder=getIntegerProperty("somatoformDisorders/somatizationDisorder");
				return _Somatoformdisorders_somatizationdisorder;
			}else {
				return _Somatoformdisorders_somatizationdisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for somatoformDisorders/somatizationDisorder.
	 * @param v Value to Set.
	 */
	public void setSomatoformdisorders_somatizationdisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/somatoformDisorders/somatizationDisorder",v);
		_Somatoformdisorders_somatizationdisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Somatoformdisorders_undifferentiatedsomatformdisorder=null;

	/**
	 * @return Returns the somatoformDisorders/undifferentiatedSomatformDisorder.
	 */
	public Integer getSomatoformdisorders_undifferentiatedsomatformdisorder() {
		try{
			if (_Somatoformdisorders_undifferentiatedsomatformdisorder==null){
				_Somatoformdisorders_undifferentiatedsomatformdisorder=getIntegerProperty("somatoformDisorders/undifferentiatedSomatformDisorder");
				return _Somatoformdisorders_undifferentiatedsomatformdisorder;
			}else {
				return _Somatoformdisorders_undifferentiatedsomatformdisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for somatoformDisorders/undifferentiatedSomatformDisorder.
	 * @param v Value to Set.
	 */
	public void setSomatoformdisorders_undifferentiatedsomatformdisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/somatoformDisorders/undifferentiatedSomatformDisorder",v);
		_Somatoformdisorders_undifferentiatedsomatformdisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Somatoformdisorders_paindisorder=null;

	/**
	 * @return Returns the somatoformDisorders/painDisorder.
	 */
	public Integer getSomatoformdisorders_paindisorder() {
		try{
			if (_Somatoformdisorders_paindisorder==null){
				_Somatoformdisorders_paindisorder=getIntegerProperty("somatoformDisorders/painDisorder");
				return _Somatoformdisorders_paindisorder;
			}else {
				return _Somatoformdisorders_paindisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for somatoformDisorders/painDisorder.
	 * @param v Value to Set.
	 */
	public void setSomatoformdisorders_paindisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/somatoformDisorders/painDisorder",v);
		_Somatoformdisorders_paindisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Somatoformdisorders_hypochondriasis=null;

	/**
	 * @return Returns the somatoformDisorders/hypochondriasis.
	 */
	public Integer getSomatoformdisorders_hypochondriasis() {
		try{
			if (_Somatoformdisorders_hypochondriasis==null){
				_Somatoformdisorders_hypochondriasis=getIntegerProperty("somatoformDisorders/hypochondriasis");
				return _Somatoformdisorders_hypochondriasis;
			}else {
				return _Somatoformdisorders_hypochondriasis;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for somatoformDisorders/hypochondriasis.
	 * @param v Value to Set.
	 */
	public void setSomatoformdisorders_hypochondriasis(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/somatoformDisorders/hypochondriasis",v);
		_Somatoformdisorders_hypochondriasis=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Somatoformdisorders_bodydysmorphicdisorder=null;

	/**
	 * @return Returns the somatoformDisorders/bodyDysmorphicDisorder.
	 */
	public Integer getSomatoformdisorders_bodydysmorphicdisorder() {
		try{
			if (_Somatoformdisorders_bodydysmorphicdisorder==null){
				_Somatoformdisorders_bodydysmorphicdisorder=getIntegerProperty("somatoformDisorders/bodyDysmorphicDisorder");
				return _Somatoformdisorders_bodydysmorphicdisorder;
			}else {
				return _Somatoformdisorders_bodydysmorphicdisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for somatoformDisorders/bodyDysmorphicDisorder.
	 * @param v Value to Set.
	 */
	public void setSomatoformdisorders_bodydysmorphicdisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/somatoformDisorders/bodyDysmorphicDisorder",v);
		_Somatoformdisorders_bodydysmorphicdisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Eatingdisorders_currentanorexianervosa=null;

	/**
	 * @return Returns the eatingDisorders/currentAnorexiaNervosa.
	 */
	public Integer getEatingdisorders_currentanorexianervosa() {
		try{
			if (_Eatingdisorders_currentanorexianervosa==null){
				_Eatingdisorders_currentanorexianervosa=getIntegerProperty("eatingDisorders/currentAnorexiaNervosa");
				return _Eatingdisorders_currentanorexianervosa;
			}else {
				return _Eatingdisorders_currentanorexianervosa;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for eatingDisorders/currentAnorexiaNervosa.
	 * @param v Value to Set.
	 */
	public void setEatingdisorders_currentanorexianervosa(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/eatingDisorders/currentAnorexiaNervosa",v);
		_Eatingdisorders_currentanorexianervosa=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Eatingdisorders_pastanorexianervosa=null;

	/**
	 * @return Returns the eatingDisorders/pastAnorexiaNervosa.
	 */
	public Integer getEatingdisorders_pastanorexianervosa() {
		try{
			if (_Eatingdisorders_pastanorexianervosa==null){
				_Eatingdisorders_pastanorexianervosa=getIntegerProperty("eatingDisorders/pastAnorexiaNervosa");
				return _Eatingdisorders_pastanorexianervosa;
			}else {
				return _Eatingdisorders_pastanorexianervosa;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for eatingDisorders/pastAnorexiaNervosa.
	 * @param v Value to Set.
	 */
	public void setEatingdisorders_pastanorexianervosa(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/eatingDisorders/pastAnorexiaNervosa",v);
		_Eatingdisorders_pastanorexianervosa=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Eatingdisorders_currentbulimianervosa=null;

	/**
	 * @return Returns the eatingDisorders/currentBulimiaNervosa.
	 */
	public Integer getEatingdisorders_currentbulimianervosa() {
		try{
			if (_Eatingdisorders_currentbulimianervosa==null){
				_Eatingdisorders_currentbulimianervosa=getIntegerProperty("eatingDisorders/currentBulimiaNervosa");
				return _Eatingdisorders_currentbulimianervosa;
			}else {
				return _Eatingdisorders_currentbulimianervosa;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for eatingDisorders/currentBulimiaNervosa.
	 * @param v Value to Set.
	 */
	public void setEatingdisorders_currentbulimianervosa(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/eatingDisorders/currentBulimiaNervosa",v);
		_Eatingdisorders_currentbulimianervosa=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Eatingdisorders_pastbulimianervosa=null;

	/**
	 * @return Returns the eatingDisorders/pastBulimiaNervosa.
	 */
	public Integer getEatingdisorders_pastbulimianervosa() {
		try{
			if (_Eatingdisorders_pastbulimianervosa==null){
				_Eatingdisorders_pastbulimianervosa=getIntegerProperty("eatingDisorders/pastBulimiaNervosa");
				return _Eatingdisorders_pastbulimianervosa;
			}else {
				return _Eatingdisorders_pastbulimianervosa;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for eatingDisorders/pastBulimiaNervosa.
	 * @param v Value to Set.
	 */
	public void setEatingdisorders_pastbulimianervosa(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/eatingDisorders/pastBulimiaNervosa",v);
		_Eatingdisorders_pastbulimianervosa=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Eatingdisorders_currentbingeeatingdisorder=null;

	/**
	 * @return Returns the eatingDisorders/currentBingeEatingDisorder.
	 */
	public Integer getEatingdisorders_currentbingeeatingdisorder() {
		try{
			if (_Eatingdisorders_currentbingeeatingdisorder==null){
				_Eatingdisorders_currentbingeeatingdisorder=getIntegerProperty("eatingDisorders/currentBingeEatingDisorder");
				return _Eatingdisorders_currentbingeeatingdisorder;
			}else {
				return _Eatingdisorders_currentbingeeatingdisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for eatingDisorders/currentBingeEatingDisorder.
	 * @param v Value to Set.
	 */
	public void setEatingdisorders_currentbingeeatingdisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/eatingDisorders/currentBingeEatingDisorder",v);
		_Eatingdisorders_currentbingeeatingdisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Eatingdisorders_pastbingeeatingdisorder=null;

	/**
	 * @return Returns the eatingDisorders/pastBingeEatingDisorder.
	 */
	public Integer getEatingdisorders_pastbingeeatingdisorder() {
		try{
			if (_Eatingdisorders_pastbingeeatingdisorder==null){
				_Eatingdisorders_pastbingeeatingdisorder=getIntegerProperty("eatingDisorders/pastBingeEatingDisorder");
				return _Eatingdisorders_pastbingeeatingdisorder;
			}else {
				return _Eatingdisorders_pastbingeeatingdisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for eatingDisorders/pastBingeEatingDisorder.
	 * @param v Value to Set.
	 */
	public void setEatingdisorders_pastbingeeatingdisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/eatingDisorders/pastBingeEatingDisorder",v);
		_Eatingdisorders_pastbingeeatingdisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Adjustmentdisorder=null;

	/**
	 * @return Returns the adjustmentDisorder.
	 */
	public Integer getAdjustmentdisorder() {
		try{
			if (_Adjustmentdisorder==null){
				_Adjustmentdisorder=getIntegerProperty("adjustmentDisorder");
				return _Adjustmentdisorder;
			}else {
				return _Adjustmentdisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for adjustmentDisorder.
	 * @param v Value to Set.
	 */
	public void setAdjustmentdisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/adjustmentDisorder",v);
		_Adjustmentdisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Optional_currentacutestressdisorder=null;

	/**
	 * @return Returns the optional/currentAcuteStressDisorder.
	 */
	public Integer getOptional_currentacutestressdisorder() {
		try{
			if (_Optional_currentacutestressdisorder==null){
				_Optional_currentacutestressdisorder=getIntegerProperty("optional/currentAcuteStressDisorder");
				return _Optional_currentacutestressdisorder;
			}else {
				return _Optional_currentacutestressdisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for optional/currentAcuteStressDisorder.
	 * @param v Value to Set.
	 */
	public void setOptional_currentacutestressdisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/optional/currentAcuteStressDisorder",v);
		_Optional_currentacutestressdisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Optional_pastacutestressdisorder=null;

	/**
	 * @return Returns the optional/pastAcuteStressDisorder.
	 */
	public Integer getOptional_pastacutestressdisorder() {
		try{
			if (_Optional_pastacutestressdisorder==null){
				_Optional_pastacutestressdisorder=getIntegerProperty("optional/pastAcuteStressDisorder");
				return _Optional_pastacutestressdisorder;
			}else {
				return _Optional_pastacutestressdisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for optional/pastAcuteStressDisorder.
	 * @param v Value to Set.
	 */
	public void setOptional_pastacutestressdisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/optional/pastAcuteStressDisorder",v);
		_Optional_pastacutestressdisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Optional_currentminordepressivedisorder=null;

	/**
	 * @return Returns the optional/currentMinorDepressiveDisorder.
	 */
	public Integer getOptional_currentminordepressivedisorder() {
		try{
			if (_Optional_currentminordepressivedisorder==null){
				_Optional_currentminordepressivedisorder=getIntegerProperty("optional/currentMinorDepressiveDisorder");
				return _Optional_currentminordepressivedisorder;
			}else {
				return _Optional_currentminordepressivedisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for optional/currentMinorDepressiveDisorder.
	 * @param v Value to Set.
	 */
	public void setOptional_currentminordepressivedisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/optional/currentMinorDepressiveDisorder",v);
		_Optional_currentminordepressivedisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Optional_pastminordepressivedisorder=null;

	/**
	 * @return Returns the optional/pastMinorDepressiveDisorder.
	 */
	public Integer getOptional_pastminordepressivedisorder() {
		try{
			if (_Optional_pastminordepressivedisorder==null){
				_Optional_pastminordepressivedisorder=getIntegerProperty("optional/pastMinorDepressiveDisorder");
				return _Optional_pastminordepressivedisorder;
			}else {
				return _Optional_pastminordepressivedisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for optional/pastMinorDepressiveDisorder.
	 * @param v Value to Set.
	 */
	public void setOptional_pastminordepressivedisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/optional/pastMinorDepressiveDisorder",v);
		_Optional_pastminordepressivedisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Optional_currentmixedanxietydepressivedisorder=null;

	/**
	 * @return Returns the optional/currentMixedAnxietyDepressiveDisorder.
	 */
	public Integer getOptional_currentmixedanxietydepressivedisorder() {
		try{
			if (_Optional_currentmixedanxietydepressivedisorder==null){
				_Optional_currentmixedanxietydepressivedisorder=getIntegerProperty("optional/currentMixedAnxietyDepressiveDisorder");
				return _Optional_currentmixedanxietydepressivedisorder;
			}else {
				return _Optional_currentmixedanxietydepressivedisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for optional/currentMixedAnxietyDepressiveDisorder.
	 * @param v Value to Set.
	 */
	public void setOptional_currentmixedanxietydepressivedisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/optional/currentMixedAnxietyDepressiveDisorder",v);
		_Optional_currentmixedanxietydepressivedisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Optional_pastmixedanxietydepressivedisorder=null;

	/**
	 * @return Returns the optional/pastMixedAnxietyDepressiveDisorder.
	 */
	public Integer getOptional_pastmixedanxietydepressivedisorder() {
		try{
			if (_Optional_pastmixedanxietydepressivedisorder==null){
				_Optional_pastmixedanxietydepressivedisorder=getIntegerProperty("optional/pastMixedAnxietyDepressiveDisorder");
				return _Optional_pastmixedanxietydepressivedisorder;
			}else {
				return _Optional_pastmixedanxietydepressivedisorder;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for optional/pastMixedAnxietyDepressiveDisorder.
	 * @param v Value to Set.
	 */
	public void setOptional_pastmixedanxietydepressivedisorder(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/optional/pastMixedAnxietyDepressiveDisorder",v);
		_Optional_pastmixedanxietydepressivedisorder=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Optional_pastsympomaticdetails=null;

	/**
	 * @return Returns the optional/pastSympomaticDetails.
	 */
	public String getOptional_pastsympomaticdetails(){
		try{
			if (_Optional_pastsympomaticdetails==null){
				_Optional_pastsympomaticdetails=getStringProperty("optional/pastSympomaticDetails");
				return _Optional_pastsympomaticdetails;
			}else {
				return _Optional_pastsympomaticdetails;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for optional/pastSympomaticDetails.
	 * @param v Value to Set.
	 */
	public void setOptional_pastsympomaticdetails(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/optional/pastSympomaticDetails",v);
		_Optional_pastsympomaticdetails=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatAscidresearchdata> getAllXnatAscidresearchdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAscidresearchdata> al = new ArrayList<org.nrg.xdat.om.XnatAscidresearchdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAscidresearchdata> getXnatAscidresearchdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAscidresearchdata> al = new ArrayList<org.nrg.xdat.om.XnatAscidresearchdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAscidresearchdata> getXnatAscidresearchdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAscidresearchdata> al = new ArrayList<org.nrg.xdat.om.XnatAscidresearchdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatAscidresearchdata getXnatAscidresearchdatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat_a:scidResearchData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatAscidresearchdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
