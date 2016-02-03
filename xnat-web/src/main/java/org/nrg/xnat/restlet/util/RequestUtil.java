/*
 * org.nrg.xnat.restlet.util.RequestUtil
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.util;

import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.resource.Representation;

import com.noelios.restlet.ext.servlet.ServletCall;

public class RequestUtil {
	public static final String DEST = "dest";
	public static final String AUTO_ARCHIVE = "auto-archive";
	public static final String AA = "AA";
	public static final String OVERWRITE_FILES = "overwrite_files";
	public static final String TRUE = "true";

	public HttpServletRequest getHttpServletRequest(Request request) {
		return ServletCall.getRequest(request);
	}

	private static Map<MediaType, String> supported_upload_types = new Hashtable<MediaType, String>() {{
		put(MediaType.TEXT_ALL, ".txt");
		put(MediaType.APPLICATION_ALL,"");
        put(MediaType.TEXT_PLAIN, ".txt");
        put(MediaType.TEXT_XML,".xml");
        put(SecureResource.TEXT_CSV, ".csv");
        put(SecureResource.APPLICATION_DICOM, ".dcm");
        put(MediaType.APPLICATION_ZIP,".zip");
        put(SecureResource.APPLICATION_XAR,".xar");
        put(MediaType.APPLICATION_GNU_TAR,".tar.gz");
        put(MediaType.APPLICATION_GNU_ZIP,".gzip");
        put(MediaType.APPLICATION_OCTET_STREAM,"");
        put(MediaType.APPLICATION_PDF,".pdf");
        put(MediaType.APPLICATION_EXCEL,".xls");
        put(MediaType.APPLICATION_POWERPOINT,".ppt");
        put(MediaType.APPLICATION_TAR,".tar");
        put(MediaType.APPLICATION_WORD,".doc");
        put(MediaType.IMAGE_ALL,".img");
        put(MediaType.IMAGE_BMP,".bmp");
        put(MediaType.IMAGE_GIF,".gif");
        put(MediaType.IMAGE_ICON,".icon");
        put(MediaType.IMAGE_JPEG,".jpeg");
        put(MediaType.IMAGE_PNG,".png");
        put(MediaType.IMAGE_SVG,".svg");
        put(MediaType.IMAGE_TIFF,".tiff");
		put(MediaType.VIDEO_ALL,".video");
		put(MediaType.VIDEO_AVI,".avi");
		put(MediaType.VIDEO_MP4,".mp4");
		put(MediaType.VIDEO_MPEG,".mpg");
		put(MediaType.VIDEO_QUICKTIME,".mov");
		put(MediaType.VIDEO_WMV,".wmv");
    }};
	
	// method with boolean flag to allow curl processing where media types are not specified
	public static String deriveFileName(final String prefix,final Representation entity, boolean returnNullForUnsupported){
		if(!(entity==null || entity.getMediaType()==null)){
			if(supported_upload_types.containsKey(entity.getMediaType())){
				return prefix+supported_upload_types.get(entity.getMediaType());
			}
		}
		if (returnNullForUnsupported) {
			return null;
		} else {
			return prefix+".unsupported";
		}
	}
	
	// original deriveFileName method returned null filename for null/unsupported types
	public static String deriveFileName(final String prefix,final Representation entity){
		return deriveFileName(prefix,entity,true);
	}
	
	public static boolean isFileInBody(Representation entity){
		if(entity==null || entity.getMediaType()==null){
			return false;
		}
		
		final MediaType mt=entity.getMediaType();
		if(supported_upload_types.containsKey(mt))
		{
			return true;
		}else{
			return false;
		}
	}

	public static <T extends MediaType> boolean compareMediaType(Representation entity, T... mts){
		if(entity==null || entity.getMediaType()==null){
			return false;
		}
		
		for(MediaType mt: mts){
			if(StringUtils.equals(mt.getName(), entity.getMediaType().getName())){
				return true;
			}
		}

		return false;
	}
	
	public static boolean hasContent(Representation entity){
		return !(entity == null || (entity.getSize() == 0) || (entity.getSize() == -1));
	}
	
	public  static boolean isMultiPartFormData(Representation entity){
		if(entity==null || entity.getMediaType()==null){
			return false;
		}
		
		final MediaType mt=entity.getMediaType();
		if(mt.equals(MediaType.MULTIPART_ALL))
		{
			return true;
		}else if(mt.equals(MediaType.MULTIPART_FORM_DATA))
		{
			return true;
		}else if(mt.equals(MediaType.APPLICATION_WWW_FORM))
		{
			return true;
		// check string values of main/subtypes where doesn't resolve to one of the above media types.
		// Happens for some Curl requests
		}else if (mt.getMainType().equalsIgnoreCase("multipart") && mt.getSubType().equalsIgnoreCase("form-data")){
			return true;
		}else{
			return false;
		}
	}
}
