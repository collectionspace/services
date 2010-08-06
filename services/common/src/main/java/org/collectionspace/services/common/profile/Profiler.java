/**	
 * Profiler.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common.profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class Profiler.
 */
public class Profiler {
	
	/** The start time. */
	private long startTime = 0;
	
	/** The stop time. */
	private long stopTime = 0;
	
	/** The final time. */
	private long finalTime = 0;
	
	/** The message prefix. */
	private String messagePrefix = "Default profiler prefix:";

    /** The profiler logger. */
	private static String LOG4J_CATEGORY = "perf.collectionspace";
    private static Logger profilerLogger = LoggerFactory.getLogger(LOG4J_CATEGORY);
	
    /**
     * private default constructor.
     */
    private Profiler() {
    	//empty default constructor
    }
    
    /**
     * Sets the message prefix.
     *
     * @param theMessagePrefix the new message prefix
     */
    protected void setMessagePrefix(String theMessagePrefix) {
    	messagePrefix = theMessagePrefix + ":";
    }
    
    protected StringBuffer getMessagePrefix() {
    	return new StringBuffer(messagePrefix);
    }
    
    /**
     * Instantiates a new profiler.
     * @param theObject 
     *
     * @param theClass the the class
     */
    public Profiler(Object theObject) {
    	if (theObject != null) {
    		this.setMessagePrefix(theObject.getClass().getSimpleName());
    	}
    }
	/**
	 * Instantiates a new profiler.
	 *
	 * @param theMessagePrefix the the message prefix
	 */
	public Profiler(String theMessagePrefix) {
		this.setMessagePrefix(theMessagePrefix);
	}
	
	/*
	 * For some reason, we need to call this method "early" at startup time for our
	 * Logger instance to get created correctly.  FIXME: REM - Can we figure this out and fix it?
	 */
	/**
	 * Setup.
	 */
	public static void setup() {
		//do nothing
	}
	
	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	protected Logger getLogger() {
		return profilerLogger;
	}
	
	/**
	 * Private log.
	 *
	 * @param inMessage the in message
	 */
	public void log(String inMessage) {
		if (getLogger().isDebugEnabled() == true) {
			StringBuffer finalMessage = getMessagePrefix();
			if (inMessage != null) {
				finalMessage.append(inMessage);
			}
			getLogger().debug(finalMessage.toString());
		}
	}
	
	/**
	 * Start
	 */
	public void start() {
		if (getLogger().isDebugEnabled() == true) {
			StringBuffer message = getMessagePrefix();
			message.append(">>>> Start >>>>");
			getLogger().debug(message.toString());
		}
		startTime = System.currentTimeMillis();
	}
	
	/**
	 * Stop.
	 */
	public void stop() {
		stopTime = System.currentTimeMillis();
		finalTime = finalTime + (stopTime - startTime);
		if (getLogger().isDebugEnabled() == true) {
			StringBuffer message = getMessagePrefix();
			message.append("<<<< Stopped <<<< [");
			message.append(finalTime);
			message.append("ms]");
			message.append('\n');
			getLogger().debug(message.toString());
		}		
	}
}
