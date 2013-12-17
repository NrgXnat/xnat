package org.nrg.mail.services.impl;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.nrg.mail.services.EmailRequestLogService;
import org.springframework.stereotype.Service;

@Service
public class DefaultEmailRequestLogService implements EmailRequestLogService {
	
	private final HashMap<String, LinkedList<Date>> log = new HashMap<String, LinkedList<Date>>();
	private final int  MAX_REQUESTS = 5;
	private final long BAN_DURATION = MILLISECONDS.convert(60, MINUTES); // 1 hour in milliseconds

	/**
	 * Function stores the email request. 
	 * @param String e - The email.
	 * @param Date t - The time the email was requested. 
	 */
	@Override
	public void logEmailRequest(String e, Date t) throws Exception{
		if((e == null || e.equals("")) || t == null){ throw new Exception("Unable to store email request. Email or Date is null.");}
		
		// If the key exists get it's list, else create a new list. 
		LinkedList<Date> requests = (log.containsKey(e)) ? log.get(e) : new LinkedList<Date>();
		
		requests.addLast(t);
		log.put(e, requests);
	}

	/**
	 * Function determines weather an email has been blocked from requesting
	 * more emails. 
	 * @param String e - the email we are interested in. 
	 */
	@Override
	public boolean isEmailBlocked(String e) {
		//If the log doesn't contain the email then it's not blocked.
		if(!log.containsKey(e)){ return false; }
		
		// The email is blocked if the maximum number of requests have been made.
		return (log.get(e).size() == MAX_REQUESTS);
	}
	
	/**
	 * Function removes an email from the block list.
	 * @param e - the email we are interested in.
	 */
	public void unblockEmail(String e){
		if(log.containsKey(e)){
			log.remove(e);
		}
	}

	/**
	 * Function clears the log of email requests that have expired.
	 * The request is expired if a certain amount of time (BAN_DURATION) has passed.
	 */
	@Override
	public void clearLogs() {
		Date now = new Date();
		LinkedList<Date> times = null;
		Map.Entry<String, LinkedList<Date>> entry = null;
		Iterator<Map.Entry<String, LinkedList<Date>>> it = log.entrySet().iterator();
		while(it.hasNext()){
			entry = it.next();
			times = entry.getValue();
			for(int i = times.size()-1; i >= 0; i--){
				// Determine if the request has expired. 
				if(now.getTime() - times.get(i).getTime() >= BAN_DURATION){
					// Since the times are in order, we can assume the rest have also expired.
					times.subList(0, i+1).clear();
					if(times.size() == 0){ 
						it.remove();   // Remove the email if there are no more times. 
					}else{
						entry.setValue(times);
					}
					break;
				}
			}
		}
	}
}