/*
 * core: org.nrg.xdat.turbine.utils.TurbineUtilsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.utils;

import org.junit.Test;
import static org.junit.Assert.fail;


public class TurbineUtilsTest {

	private void doEscapeFail(String s){
		String s2=TurbineUtils.escapeParam(s);
		if(s.equals(s2)){
			fail("Not properly escaped");
		}else{
			System.out.println(s+":"+s2);
		}
	}

	private void doEscapeSuccess(String s){
		String s2=TurbineUtils.escapeParam(s);
		if(!s.equals(s2)){
			fail("Unexpectedly escaped "+s+":"+s2);
		}
	}
	
	@Test
	public void testEscapeParamSingleQuote() {
		String s="sdfsf'sdfs";
		doEscapeFail(s);
	}
	
	@Test
	public void testEscapeParam1() {
		String s="sdfsf:sdfs";
		doEscapeSuccess(s);
	}
	
	@Test
	public void testEscapeParam2() {
		String s="sdfsf;sdfs";
		doEscapeSuccess(s);
	}
	
	@Test
	public void testEscapeParam3() {
		String s="sdfsf?sdfs";
		doEscapeSuccess(s);
	}
	
	@Test
	public void testEscapeParam4() {
		String s="sdfsf/sdfs";
		doEscapeSuccess(s);
	}
	
	@Test
	public void testEscapeParam5() {
		String s="sdfsf\\sdfs";
		doEscapeSuccess(s);
	}
	
	@Test
	public void testEscapeParam6() {
		String s="sdfsf sdfs";
		doEscapeSuccess(s);
	}
	
	@Test
	public void testEscapeParam7() {
		String s="sdfsf+sdfs";
		doEscapeSuccess(s);
	}
	
	@Test
	public void testEscapeParam8() {
		String s="sdfsf-sdfs";
		doEscapeSuccess(s);
	}
	
	@Test
	public void testEscapeParamDoubleQuote() {
		String s="sdfsf\"sdfs";
		doEscapeFail(s);
	}
	
	@Test
	public void testEscapeParamClose() {
		String s="sdfsf<sdfs";
		doEscapeFail(s);
	}
	
	@Test
	public void testEscapeParamOpen() {
		String s="sdfsf>sdfs";
		doEscapeFail(s);
	}
	
	@Test
	public void testEscapeParamAnd() {
		String s="sdfsf&sdfs";
		doEscapeFail(s);
	}
}
