/*
 * ecat4xnat: org.nrg.ecat.StaticSubheaderFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.nrg.ecat.Header.Type;

public class StaticSubheaderFactory implements SubheaderFactory {
	private static final SubheaderFactory factory = new StaticSubheaderFactory();
	
	public static SubheaderFactory getInstance() { return factory; }
	
	private StaticSubheaderFactory() { }
	
	private static final Map<Integer,Type> fileTypes = new HashMap<Integer,Type>();
	static {
		fileTypes.put(6,Type.IMAGE);    // Volume 8
		fileTypes.put(7,Type.IMAGE);    // Volume 16
	}

	private static final class Subheader extends AbstractHeader implements Header {
		Subheader(final int fileType) {
			super(fileTypes.get(fileType));
		}
		
		protected void startXML(final PrintStream out) {
			out.println(String.format("<subheader type=\"%1$s\">", getType()));
		}
		
		protected void endXML(final PrintStream out) {
			out.println("</subheader>");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.ecat.SubheaderFactory#create(int)
	 */
	public Header create(int fileType) {
		return new Subheader(fileType);
	}
}
