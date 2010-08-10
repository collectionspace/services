/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common.datetime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
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

    final static String UTC_TIMEZONE_IDENTIFIER = "UTC";
    final static String ISO_8601_UTC_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

   /**
    * Returns the UTC time zone.
    *
    * @return The UTC time zone.  Defaults to the closely-related GMT time zone,
    *         if for some reason the UTC time zone identifier cannot be understood.
    */
    public static TimeZone UTCTimeZone() {
        return TimeZone.getTimeZone(UTC_TIMEZONE_IDENTIFIER);
    }

   /**
    * Returns a calendar date, representing the current date and time instance
    * in the UTC time zone.
    *
    * @return The current date and time instance in the UTC time zone.
    */
    public static GregorianCalendar currentDateAndTimeUTC() {
        return currentDateAndTime(UTCTimeZone());
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
    * Returns a String representing the current date and time instance.
    * in the UTC time zone, formatted as an ISO 8601 timestamp.
    *
    * @return A String representing the current date and time instance.
    */
    public static String timestampUTC() {
        return formatAsISO8601Timestamp(currentDateAndTime(UTCTimeZone()));
    }

   /**
    * Returns a representation of a calendar date and time instance,
    * as an ISO 8601-formatted timestamp in the UTC time zone.
    *
    * @param cal A calendar date and time instance
    *
    * @return    A representation of that calendar date and time instance,
    *            as an ISO 8601-formatted timestamp in the UTC time zone.
    */
    public static String formatAsISO8601Timestamp(GregorianCalendar cal) {
        return formatCalendarDate(cal, UTCTimeZone(), ISO8601TimestampFormatter());
    }

   /**
    * Formats a provided calendar date using a provided date formatter,
    * in the default system time zone.
    *
    * @param date  A calendar date to format.
    * @param df    A date formatter to apply.
    *
    * @return      A formatted date string, or the empty string
    *              if one or more of the parameter values were invalid.
    */
    public static String formatCalendarDate(GregorianCalendar gcal, DateFormat df) {
        return formatCalendarDate(gcal, TimeZone.getDefault(), df);
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
    public static String formatCalendarDate(GregorianCalendar gcal, TimeZone tz, DateFormat df) {
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

   /**
    * Returns a date formatter for an ISO 8601 timestamp pattern.
    *
    * @return  A date formatter for an ISO 8601 timestamp pattern.
    *          This pattern is specified as a class constant above.
    */
    public static DateFormat ISO8601TimestampFormatter() {
        return getDateFormatter(ISO_8601_UTC_TIMESTAMP_PATTERN);
    }

   /**
    * Returns a date formatter for a provided date or date/time pattern.
    *
    * @param pattern  A date or date/time pattern.
    *
    * @return         A date formatter using that pattern, or null
    *                 if the pattern was null, empty, or invalid.
    */
    public static DateFormat getDateFormatter(String pattern) {
        DateFormat df = null;
        if (pattern == null || pattern.trim().isEmpty()) {
            logger.warn("Null or empty date pattern string was provided " +
                "when a non-null, non-empty date pattern string was required.");
            return df;
        }
        try {
            df = new SimpleDateFormat(pattern);
        } catch (IllegalArgumentException iae) {
            logger.warn("Invalid date pattern string: " + pattern);
        }
        return df;
    }

}
