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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.config.TenantBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.tenant.TenantBindingType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * DateTimeFormatUtils.java
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 *
 */
public class DateTimeFormatUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateTimeFormatUtils.class);
    final static String DATE_FORMAT_PATTERN_PROPERTY_NAME = "datePattern";
    final static String ISO_8601_UTC_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    // FIXME:
    // - Add a method to return the default set of ISO 8601-based date patterns,
    //   irrespective of per-tenant configuration.
    // - Consider cacheing the lists of per-tenant date format patterns
    //   and refresh the cached copies whenever tenant bindings are read.
    // - Methods below related to per-tenant configuration of date formats might
    //   be moved to their own class.

    /**
     * Returns a list of the date format patterns permitted in a service context.
     * These patterns must conform to the format for date and time pattern strings
     * specified in the Javadocs for the Java language class, java.text.SimpleDateFormat.
     *
     * @param ctx a service context.
     *
     * @return    a list of date format patterns permitted in the service context.
     *            Returns an empty list of patterns if the service context is null.
     */
    public static List<String> getDateFormatPatternsForTenant(ServiceContext ctx) {
        if (ctx == null) {
            return new ArrayList<String>();
        }
        return getDateFormatPatternsForTenant(ctx.getTenantId());
    }

    /**
     * Returns a list of the date format patterns permitted for a tenant, specified
     * by tenant ID.
     *
     * These patterns must conform to the format for date and time pattern strings
     * specified in the Javadocs for the Java language class, java.text.SimpleDateFormat.
     *
     * @param tenantId  a tenant ID.
     *
     * @return          a list of date format patterns permitted for the tenant.
     */
    public static List<String> getDateFormatPatternsForTenant(String tenantId) {
        List<String> patterns = new ArrayList<String>();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            return patterns;
        }
        TenantBindingConfigReaderImpl tReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        TenantBindingType tenantBinding = tReader.getTenantBinding(tenantId);
        patterns = TenantBindingUtils.getPropertyValues(tenantBinding,
                DATE_FORMAT_PATTERN_PROPERTY_NAME);
        return validatePatterns(patterns);
    }

    public static List<String> validatePatterns(List<String> patterns) {
        if (patterns == null) {
            return new ArrayList<String>();
        }
        List<String> validPatterns = new ArrayList<String>();
        for (String pattern : patterns) {
            try {
                DateFormat df = getDateFormatter(pattern);
                validPatterns.add(pattern);
            } catch (IllegalArgumentException iae) {
                logger.warn("Invalid " + DATE_FORMAT_PATTERN_PROPERTY_NAME + " property: " + pattern);
            }
        }
        return validPatterns;
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
        return formatGregorianCalendarDate(cal, GregorianCalendarDateTimeUtils.UTCTimeZone(),
                getDateFormatter(ISO_8601_UTC_TIMESTAMP_PATTERN));
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

    /**
     * Identifies whether a presumptive date or date/time can be parsed
     * by a date parser, using a specified format pattern.
     *
     * @param str      a String, possibly a date or date/time String.
     *
     * @param pattern  A date or date/time pattern.
     *
     * @return         true, if the String can be parsed, using the pattern;
     *                 false if the String cannot be parsed by the pattern,
     *                 or if the String or pattern are null.
     */
    public static boolean isParseableByDatePattern(String str, String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return false;
        }
        DateFormat df = null;
        try {
            df = new SimpleDateFormat(pattern);
            df.parse(str);
        } catch (ParseException pe) {
            return false;
        } catch (IllegalArgumentException iae) {
            return false;
        }
        return true;
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
            logger.warn("Null or empty date pattern string was provided when getting date formatter.");
            return df;
        }
        try {
            df = new SimpleDateFormat(pattern);
        } catch (IllegalArgumentException iae) {
            logger.warn("Invalid date pattern string '" + pattern + "': " + iae.getMessage());
        }
        return df;
    }
}
