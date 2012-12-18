/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright © 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common.api;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * GregorianCalendarDateTimeUtils.java
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 */
public class GregorianCalendarDateTimeUtils {

    private static final Logger logger = LoggerFactory.getLogger(GregorianCalendarDateTimeUtils.class);

    /**
     * Returns a String representing the current date and time instance.
     * in the UTC time zone, formatted as an ISO 8601 timestamp.
     *
     * @return A String representing the current date and time instance.
     */
     public static String timestampUTC() {
         return formatAsISO8601Timestamp(currentDateAndTime(DateUtils.UTCTimeZone()));
     }
     
    /**
     * Returns a String representing the current date and time instance.
     * in the UTC time zone, formatted as an ISO 8601 date.
     *
     * @return A String representing the current date and time instance.
     */
     public static String currentDateUTC() {
         return formatAsISO8601Date(currentDateAndTime(DateUtils.UTCTimeZone()));
     }
    
   /**
    * Returns a calendar date, representing the current date and time instance
    * in the UTC time zone.
    *
    * @return The current date and time instance in the UTC time zone.
    */
    public static GregorianCalendar currentDateAndTimeUTC() {
        return currentDateAndTime(DateUtils.UTCTimeZone());
    }

   /**
    * Returns a calendar date, representing the current date and time instance
    * in the specified time zone.
    *
    * @return The current date and time instance in the specified time zone.
    *         If the time zone is null, will return the current time and
    *         date in the time zone intrinsic to a new Calendar instance.
    */
    public static GregorianCalendar currentDateAndTime(TimeZone tz) {
        GregorianCalendar gcal = new GregorianCalendar();
        if (tz != null) {
            gcal.setTimeZone(tz);
        }
        Date now = new Date();
        gcal.setTime(now);
        return gcal;
    }

    
    /**
     * Returns a representation of a calendar date and time instance,
     * as an ISO 8601-formatted timestamp in the UTC time zone.
     *
     * @param cal a calendar date and time instance.
     *
     * @return    a representation of that calendar date and time instance,
     *            as an ISO 8601-formatted timestamp in the UTC time zone.
     */
    public static String formatAsISO8601Timestamp(GregorianCalendar cal) {
        return formatGregorianCalendarDate(cal, DateUtils.UTCTimeZone(),
        		DateUtils.getDateFormatter(DateUtils.ISO_8601_UTC_TIMESTAMP_PATTERN));
    }
    
    /**
     * Returns a representation of a calendar date and time instance,
     * as an ISO 8601-formatted date.
     *
     * @param cal a calendar date and time instance.
     *
     * @return    a representation of that calendar date and time instance,
     *            as an ISO 8601-formatted date.
     */
    public static String formatAsISO8601Date(GregorianCalendar cal) {
        return formatGregorianCalendarDate(cal, DateUtils.UTCTimeZone(),
        		DateUtils.getDateFormatter(DateUtils.ISO_8601_DATE_PATTERN));
    }

    /**
     * Formats a provided calendar date using a supplied date formatter,
     * in the default system time zone.
     *
     * @param date  A calendar date to format.
     * @param df    A date formatter to apply.
     *
     * @return      A formatted date string, or the empty string
     *              if one or more of the parameter values were invalid.
     */
    public static String formatGregorianCalendarDate(GregorianCalendar gcal, DateFormat df) {
        return formatGregorianCalendarDate(gcal, TimeZone.getDefault(), df);
    }

    /**
     * Formats a provided calendar date using a provided date formatter,
     * in a provided time zone.
     *
     * @param date  A calendar date to format.
     * @param tz    The time zone qualifier for the calendar date to format.
     * @param df    A date formatter to apply.
     *
     * @return      A formatted date string, or the empty string
     *              if one or more of the parameter values were invalid.
     */
    public static String formatGregorianCalendarDate(GregorianCalendar gcal, TimeZone tz, DateFormat df) {
        String formattedDate = "";
        if (gcal == null) {
            logger.warn("Null calendar date was provided when a non-null calendar date was required.");
            return formattedDate;
        }
        if (tz == null) {
            logger.warn("Null time zone was provided when a non-null time zone was required.");
            return formattedDate;
        }
        if (df == null) {
            logger.warn("Null date formatter was provided when a non-null date formatter was required.");
            return formattedDate;
        }
        gcal.setTimeZone(tz);
        Date date = gcal.getTime();
        df.setTimeZone(tz);
        formattedDate = df.format(date);
        return formattedDate;
    }    
}
