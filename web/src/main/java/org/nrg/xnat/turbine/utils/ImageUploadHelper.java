/*
 * web: org.nrg.xnat.turbine.utils.ImageUploadHelper
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.utils;

import lombok.extern.slf4j.Slf4j;
import org.nrg.PrearcImporter;
import org.nrg.framework.status.StatusListenerI;
import org.nrg.framework.status.StatusMessage;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.PrearcImporterFactory;
import org.nrg.xnat.event.archive.ArchiveStatusProducer;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileLock;
import java.util.*;
import java.util.concurrent.Callable;

@Slf4j
public class ImageUploadHelper extends ArchiveStatusProducer implements Callable<List<File>> {
	private final String           project;

	private final File src;
	private final File dest;

	private Map<String,Object> additionalValues;

	public ImageUploadHelper(final Object uID,final UserI user,final String project,final File src, final File dest,final Map<String,Object> additionalVariables) {
		super(uID, user);
		this.project = project;
		this.src=src;
		this.dest=dest;
		this.additionalValues=additionalVariables;
	}

	public List<File> call() {
		final PrearcImporter pw = PrearcImporterFactory.getFactory().getPrearcImporter(project, dest, src);
		pw.setAdditionalValues(additionalValues);
		for(final StatusListenerI listener: this.getListeners()){
			pw.addStatusListener(listener);
		}

		final LocalListener localListener = new LocalListener();

		pw.addStatusListener(localListener);

		pw.run();

		final Collection<File> sessions=pw.getSessions();
		final List<File> response= new ArrayList<>();

		for(final File f : sessions){
			response.add(f);

			if (f.isDirectory())
			{
				final String s = f.getAbsolutePath() + ".xml";
				final File xml = new File(s);
				if (xml.exists())
				{
					if(this.additionalValues!=null && this.additionalValues.size()>0){
						processing("Setting additional values for '" + f.getName() + "'");
						final SAXReader reader = new SAXReader(null);
						try {   
							final org.nrg.xft.XFTItem item = reader.parse(s);
							for (Map.Entry<String, Object> entry :additionalValues.entrySet())
							{
								String key=entry.getKey();
								if(key.startsWith("onBuild:")){
									key=key.substring(8);
								}
								
								Object value=entry.getValue();
								if(value.toString().startsWith("format:")){
									value=value.toString().substring(7);
									String [] chunks=value.toString().split(",");
									String format=chunks[0];
									
									Object [] formatValues=new Object[(chunks.length-1)];
									for(int i=1;i<chunks.length;i++){
										String chunk=chunks[i];
										if(chunk.startsWith("d:")){
											//special handling for dates
											Date d=item.getDateProperty(chunk.substring(2));
											formatValues[(i-1)]=d;
										}else{
											String v=item.getStringProperty(chunk);
											formatValues[(i-1)]=v;
										}
									}
									
									value=String.format(format, formatValues);
								}

								try {
									item.setProperty(key, value);
								} catch (FieldNotFoundException e) {
									String message = "Couldn't set field " + e.FIELD + " on item of type '" +
											item.getXSIType() + "' to value '" + value + "' (field not found): " + e.MESSAGE;
									log.warn(message);
									warning(message);
								} catch (InvalidValueException e) {
									String message = "Couldn't set field " + key + " on item of type '" +
											item.getXSIType() + "' to value '" + value + "' (invalid value): " + e.getMessage();
									log.warn(message);
									warning(message);
								} catch (Throwable e) {
									final String message = "Failed to set appropriate field for '" + f.getName() +
											"'. Data may be publicly accessible until archived.";
									log.error(message, e);
									failed(message);
								}
							}

							try (final FileOutputStream fos = new FileOutputStream(xml)) {
								final FileLock fl = fos.getChannel().lock();
								try {
									item.toXML(fos, false);
								} finally {
									fl.release();
								}
							}
						} catch (Throwable e) {
							log.error("Failed to parse " + s, e);
							failed("failed to set appropriate field(s) for '" + f.getName() + "': " + e.getMessage() +
									". Data may be publicly accessible until archived.");
						}
					}
				} else {
					failed("failed to load generated xml file ('" + xml.getName() + "').  Data may be publicly accessible until archived.");
				}
			}
		}


		for (final StatusMessage s : localListener.getCachedMessages()){
			if (s.getStatus().equals(StatusMessage.Status.FAILED)){
				log.error(s.getMessage());
			}else{
				log.info(s.getMessage());
			}
		}

		return response;
	}

	@SuppressWarnings("unused")
	public class PrearcListener implements StatusListenerI{
		final HttpSession session;
		final String sessionAttribute;
		final ArrayList<String[]> messages = new ArrayList<>();

		public PrearcListener(HttpSession s, String sa)
		{
			session = s;
			sessionAttribute = sa;
		}

		public void notify(StatusMessage sm){
			//            Object src = sm.getSource();
			String message = sm.getMessage();
			StatusMessage.Status status = sm.getStatus();

			String text = "";
			//            if (src instanceof File){
			//                text += ((File)src).getName() + " ";
			//            }
			text +=message;

			if (status.equals(StatusMessage.Status.COMPLETED)){
				messages.add(new String[]{"COMPLETED",text});
			}else if (status.equals(StatusMessage.Status.PROCESSING)){
				messages.add(new String[]{"PROCESSING",text});
			}else if (status.equals(StatusMessage.Status.WARNING)){
				messages.add(new String[]{"WARNING",text});
			}else if (status.equals(StatusMessage.Status.FAILED)){
				messages.add(new String[]{"FAILED",text});
			}else{
				messages.add(new String[]{"UNKNOWN",text});
			}

			if (session !=null){
				session.setAttribute(sessionAttribute, messages);
			}


		}

		public ArrayList<String[]> getMessages(){
			return messages;
		}

		public void addMessage(String level, String message){
			messages.add(new String[]{level,message});
		}
	}

	public class LocalListener implements StatusListenerI{
		private Collection<StatusMessage> cache= new ArrayList<>();
		public void notify(StatusMessage message) {
			cache.add(message);
			publish(message);
		}

		public Collection<StatusMessage> getCachedMessages(){
			return cache;
		}
	}
}
