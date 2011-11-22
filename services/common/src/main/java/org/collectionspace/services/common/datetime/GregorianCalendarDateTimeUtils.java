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
package org.collectionspace.services.common.datetime;

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
        return DateTimeFormatUtils.formatAsISO8601Timestamp(currentDateAndTime(UTCTimeZone()));
    }
    
   /**
    * Returns a String representing the current date and time instance.
    * in the UTC time zone, formatted as an ISO 8601 date.
    *
    * @return A String representing the current date and time instance.
    */
    public static String currentDateUTC() {
        return DateTimeFormatUtils.formatAsISO8601Date(currentDateAndTime(UTCTimeZone()));
    }

}
