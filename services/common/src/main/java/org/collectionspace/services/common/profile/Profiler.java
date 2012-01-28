/**
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
 * Profiler.java
 *
 * Simple utility to store and report performance metrics.
 *
 * Records start and stop times, and elapsed and cumulative timings;
 * writes performance-related log times.
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 */
public class Profiler {

    /** The level of indentation. */
    private int messageIndent = 0;
    /** The indentation character(s) */
    private static String INDENT = "\t";
    /** The message prefix. */
    private String messagePrefix = "";
    /** The separator between the message prefix and the message. **/
    final static String PREFIX_SEPARATOR = ":";
    /** The start time. */
    private long startTime = 0;
    /** The stop time. */
    private long stopTime = 0;
    /** The segment time. */
    private long elapsedTime = 0;
    /** The cumulative time. */
    private long cumulativeTime = 0;
    /** The profiler logger. */
    private static String DEFAULT_LOG4J_CATEGORY = "perf.collectionspace";
    private static Logger profileLogger = LoggerFactory.getLogger(DEFAULT_LOG4J_CATEGORY);

    /**
     * private default constructor.
     */
    private Profiler() {
        //empty default constructor
    }

    /**
     * Instantiates a new profiler.
     *
     * @param theObject a class whose name is to be logged.
     */
    public Profiler(Object theObject) {
        this(theObject, 0);
    }

    /**
     * Instantiates a new profiler.
     *
     * @param theObject a class whose name is to be logged.
     * @param messageIndent the level of indentation for log messages.
     */
    public Profiler(Object theObject, int messageIndent) {
        init(objectName(theObject), messageIndent);
    }

    /**
     * Instantiates a new profiler.
     *
     * @param messagePrefix the message prefix to appear in log messages.
     * @param messageIndent the level of indentation for log messages.
     */
    public Profiler(String messagePrefix, int messageIndent) {
        init(messagePrefix, messageIndent);
    }

    private void init(String messagePrefix, int messageIndent) {
        this.setMessagePrefix(messagePrefix);
        this.setMessageIndent(messageIndent);
    }

    /**
     * Stores and logs the start time.
     *
     * Logs using a default message.
     */
    public void start() {
        start(defaultStartMessage());
    }

    /**
     * Stores and logs the start time.
     *
     * @param msg the message to log.
     */
    public void start(String msg) {
        log(msg);
        setStartTime(currentTime());
    }

    private void stopTimer() {
        setStopTime(currentTime());
        setElapsedTime(getStopTime() - getStartTime());
        addToCumulativeTime(getElapsedTime());
    }
    
    /**
     * Stores and logs the stop time, and the elapsed and cumulative timings
     * from one or more cycles of start and stop.
     *
     * Logs using a default message.
     */
    public void stop() {
    	stopTimer();
        log(defaultStopMessage());
    }

    /**
     * Stores and logs the stop time, and the elapsed and cumulative timings,
     * from one or more cycles of start and stop.
     *
     * @param msg the message to log.
     */
    public void stop(String msg) {
    	stopTimer();
        log(msg);
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getStopTime() {
        return this.stopTime;
    }

    public long getElapsedTime() {
        return this.elapsedTime;
    }

    public long getCumulativeTime() {
        return this.cumulativeTime;
    }

    /**
     * Resets the start and stop time, and the elapsed and cumulative timings.
     */
    public void reset() {
        setStartTime(0);
        setStopTime(0);
        setElapsedTime(0);
        setCumulativeTime(0);
    }

    /**
     * Writes a message to a log entry.  The message will
     * be formatted, using current settings for indentation,
     * prefix, etc. before being written.
     *
     * @param msg the message to be written to a log entry.
     */
    public void log(String msg) {
        if (getLogger().isTraceEnabled()) {
            getLogger().trace(formatLogMessage(msg));
        }        
    }

    /**
     * Writes a message to a log entry,
     *
     * @param msg the message to write to a log entry.
     * @param formatMsg true if the message is to be formatted;
     *                  false if it is not to be formatted.
     */
    public void log(String msg, boolean formatMsg) {
        if (getLogger().isTraceEnabled()) {
            if (formatMsg) {
                getLogger().trace(formatLogMessage(msg));
            } else {
                getLogger().trace(msg);
            }
        }
    }

    /**
     * Gets the logger.
     *
     * @return the logger
     */
    protected Logger getLogger() {
        return this.profileLogger;
    }
    
    private void setMessagePrefix(String prefix) {
        this.messagePrefix = prefix;
    }

    private String getMessagePrefix() {
        return this.messagePrefix;
    }

    private int getMessageIndent() {
        return this.messageIndent;
    }

    private void setMessageIndent(int indent) {
        this.messageIndent = indent;
    }

    private void setStartTime(long start) {
        this.startTime = start;
    }

    private void setStopTime(long stop) {
        this.stopTime = stop;
    }

    private void setElapsedTime(long segment) {
        this.elapsedTime = segment;
    }

    private void setCumulativeTime(long cumulative) {
        this.cumulativeTime = cumulative;
    }

    private void addToCumulativeTime(long cumulative) {
        this.cumulativeTime += cumulative;
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }

    private long elapsedTime() {
        return (currentTime() - getStartTime());
    }

    private String objectName(Object theObject) {
        if (theObject != null) {
            return (theObject.getClass().getSimpleName());
        } else {
            return "";
        }
    }

    /**
     * Returns a formatted log message, after adding indentation and prefix, if any.
     *
     * @param msg the message to log.
     * @return a formatted log message, including indentation and prefix, if any.
     */
    private String formatLogMessage(String msg) {
        StringBuffer logMsg = new StringBuffer();
        for (int i = 0; i < getMessageIndent(); i++) {
            logMsg.append(INDENT);
        }
        String prefix = getMessagePrefix();
        if (prefix != null && !prefix.trim().isEmpty()) {
            logMsg.append(prefix);
            logMsg.append(PREFIX_SEPARATOR);
        }
        if (msg != null) {
            logMsg.append(msg);
        }
        return logMsg.toString();
    }

    private String defaultStartMessage() {
        StringBuffer defaultStartMessage = new StringBuffer();
        defaultStartMessage.append(">>>> Start >>>>");
        return defaultStartMessage.toString();
    }

    private String defaultStopMessage() {
        StringBuffer defaultStopMessage = new StringBuffer();
        defaultStopMessage.append("<<<< Stopped <<<<");
        defaultStopMessage.append(" [" + getElapsedTime() + " ms]");
        if (getCumulativeTime() > getElapsedTime()) {
            defaultStopMessage.append(" (cumulative [" + getCumulativeTime() + " ms])");
        }
        return defaultStopMessage.toString();
    }

}
