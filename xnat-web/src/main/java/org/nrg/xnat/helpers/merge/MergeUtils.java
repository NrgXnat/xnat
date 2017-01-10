/*
 * web: org.nrg.xnat.helpers.merge.MergeUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatResourceI;
import org.nrg.xdat.model.XnatResourceseriesI;

import java.util.List;
import java.util.NoSuchElementException;

public class MergeUtils {

	public static boolean compareResources(final XnatAbstractresourceI src, final XnatAbstractresourceI dest){
		if(src instanceof XnatResourceseriesI){
			return (((XnatResourceseriesI)src).getPath()+((XnatResourceseriesI)src).getPattern()).equals(((XnatResourceseriesI)src).getPath()+((XnatResourceseriesI)src).getPattern());
		}else{
			return ((XnatResourceI)src).getUri().equals(((XnatResourceI)dest).getUri());
		}
	}

	public static XnatImagescandataI getMatchingScanById(final String id, final List<XnatImagescandataI> list){
		try {
		return Iterables.find(list,new Predicate<XnatImagescandataI>(){
			@Override
			public boolean apply(XnatImagescandataI scan2) {
					return StringUtils.equals(id, scan2.getId());
			}}
		);
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public static XnatImagescandataI getMatchingScan(final XnatImagescandataI scan, final List<XnatImagescandataI> list){
		try {
		return Iterables.find(list,new Predicate<XnatImagescandataI>(){
			@Override
			public boolean apply(XnatImagescandataI scan2) {
					return StringUtils.equals(scan.getId(), scan2.getId());
			}}
		);
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public static XnatImagescandataI getMatchingScanByUID(final XnatImagescandataI scan, final List<XnatImagescandataI> list){
		try {
		return Iterables.find(list,new Predicate<XnatImagescandataI>(){
			@Override
			public boolean apply(XnatImagescandataI scan2) {
					return StringUtils.equals(scan.getUid(), scan2.getUid());
			}}
		);
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public static XnatAbstractresourceI getMatchingResource(final XnatAbstractresourceI res, List<XnatAbstractresourceI> list){
		try {
		return Iterables.find(list,new Predicate<XnatAbstractresourceI>(){
			@Override
			public boolean apply(XnatAbstractresourceI res2) {
					return StringUtils.equals(res.getLabel(),res2.getLabel());
			}}
		);
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public static XnatAbstractresourceI getMatchingResourceByLabel(final String label, List<XnatAbstractresourceI> list){
		try {
		return Iterables.find(list,new Predicate<XnatAbstractresourceI>(){
			@Override
			public boolean apply(XnatAbstractresourceI res2) {
					return StringUtils.equals(label,res2.getLabel());
			}}
		);
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	
}
