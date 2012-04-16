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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.config.TenantBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.config.tenant.TenantBindingType;

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
    final static String LOCALE_LANGUAGE_CODE_PROPERTY_NAME = "localeLanguage";
    final static Locale NULL_LOCALE = null;
    final static List<String> isoLanguageCodes = new ArrayList(Arrays.asList(Locale.getISOLanguages()));
    final static String ISO_8601_DATE_PATTERN = "yyyy-MM-dd";
    final static String ISO_8601_UTC_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    static Map<String,List<DateFormat>> dateFormatters = new HashMap<String,List<DateFormat>>();
    static Map<String,List<String>> datePatterns = new HashMap<String,List<String>>();
    static Map<String,List<String>> localeLanguageCodes = new HashMap<String,List<String>>();


    // FIXME:
    // - Add a method to return the default set of ISO 8601-based date patterns,
    //   irrespective of per-tenant configuration.
    // - Methods below related to per-tenant configuration of date formats might
    //   be moved to their own class.
    // - Investigate whether the map of per-tenant date formatters might best be
    //   maintained within a singleton class.

    /**
     * Returns a list of the date formatters permitted in a service context.
     *
     * @param ctx a service context.
     *
     * @return    a list of date formatters permitted in the service context.
     *            Returns an empty list of date formatters if the service context is null.
     */
    public static List<DateFormat> getDateFormattersForTenant(ServiceContext ctx) {
        List<DateFormat> formatters = new ArrayList<DateFormat>();
        if (ctx == null) {
            return formatters;
        }
        return getDateFormattersForTenant(ctx.getTenantId());
    }

    /**
     * Returns a list of the date formatters permitted for a tenant, specified
     * by tenant ID.
     *
     * @param tenantId  a tenant ID.
     *
     * @return    a list of date formatters permitted for the tenant.
     *            Returns an empty list of date formatters if the tenant ID is null or empty.
     */
    public static List<DateFormat> getDateFormattersForTenant(String tenantId) {
        List<DateFormat> formatters = new ArrayList<DateFormat>();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            return formatters;
        }
        // If a list of date formatters for this tenant already exists, return it.
        if (dateFormatters != null && dateFormatters.containsKey(tenantId)) {
            formatters = dateFormatters.get(tenantId);
            if (formatters != null && formatters.size() > 0) {
                return formatters;
            }
        }
        // Otherwise, generate that list and cache it for re-use.
        List<String> patterns = getDateFormatPatternsForTenant(tenantId);
        List<String> languageCodes = getLanguageCodesForTenant(tenantId);
        Locale locale = null;
        DateFormat df = null;
        boolean hasLanguageCodes = languageCodes != null && languageCodes.size() > 0;
        // FIXME: this code pairs every locale language code with every date or
        // date/time pattern.  This is a quick and dirty expedient, and must
        // necessarily be replaced by date pattern/language code pairs.
        if (hasLanguageCodes) {
            for (String languageCode : languageCodes) {
                if (languageCode != null && ! languageCode.trim().isEmpty()) {
                    locale = getLocale(languageCode);
                    for (String pattern : patterns) {
                        df = getDateFormatter(pattern, locale);
                        if (df != null)  {
                            formatters.add(df);
                        }
                    }
                }
            }
        } else {
            for (String pattern : patterns) {
                df = getDateFormatter(pattern, locale);
                if (df != null)  {
                    formatters.add(df);
                }
            }
        }
        if (dateFormatters != null) {
            dateFormatters.put(tenantId, formatters);
        }
        return formatters;
    }

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
     *                  Returns an empty list of patterns if the tenant ID is null or empty.
     */
    public static List<String> getDateFormatPatternsForTenant(String tenantId) {
        List<String> patterns = new ArrayList<String>();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            return patterns;
        }
        // If a list of date patterns for this tenant already exists, return it.
        if (datePatterns != null && datePatterns.containsKey(tenantId)) {
            patterns = datePatterns.get(tenantId);
            if (patterns != null && patterns.size() > 0) {
                return patterns;
            }
        }
        // Otherwise, generate that list and cache it for re-use.
        TenantBindingConfigReaderImpl tReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        TenantBindingType tenantBinding = tReader.getTenantBinding(tenantId);
        patterns = TenantBindingUtils.getPropertyValues(tenantBinding,
                DATE_FORMAT_PATTERN_PROPERTY_NAME);
        patterns = validatePatterns(patterns);
        if (datePatterns != null) {
            datePatterns.put(tenantId, patterns);
        }
        return patterns;
    }

    /**
     * Validates a list of date or date/time patterns, checking each pattern
     * to determine whether it can be used to instantiate a date formatter.
     *
     * These patterns must conform to the format for date and time pattern strings
     * specified in the Javadocs for the Java language class, java.text.SimpleDateFormat.
     *
     * @param patterns  a list of date or date/time patterns.
     *
     * @return          a list of valid patterns, excluding any patterns
     *                  that could not be used to instantiate a date formatter.
     */
    public static List<String> validatePatterns(List<String> patterns) {
        if (patterns == null) {
            return new ArrayList<String>();
        }
        DateFormat df = new SimpleDateFormat();
        List<String> validPatterns = new ArrayList<String>();
        for (String pattern : patterns) {
            try {
                df = getDateFormatter(pattern);
                validPatterns.add(pattern);
            } catch (IllegalArgumentException iae) {
                logger.warn("Invalid " + DATE_FORMAT_PATTERN_PROPERTY_NAME + " property: " + pattern);
            }
        }
        return validPatterns;
    }

    // FIXME: Routines specific to Locales, including their constituent language
    // and country codes, should be moved to their own utility class, and likely
    // to their own common package.

    /**
     * Returns a list of the locale language codes permitted in a service context.
     *
     * @param ctx a service context.
     *
     * @return    a list of locale language codes permitted in the service context.
     *            Returns an empty list of language codes if the service context is null.
     */
    public static List<String> getLanguageCodesForTenant(ServiceContext ctx) {
        if (ctx == null) {
            return new ArrayList<String>();
        }
        return getLanguageCodesForTenant(ctx.getTenantId());
    }

    /**
     * Returns a list of the locale language codes permitted for a tenant, specified
     * by tenant ID.
     *
     * The values of these codes must be valid ISO 639-1 language codes.
     *
     * @param tenantId  a tenant ID.
     *
     * @return          a list of locale language codes permitted for the tenant.
     *                  Returns an empty list of language codes if the tenant ID is null or empty.
     */
    public static List<String> getLanguageCodesForTenant(String tenantId) {
        List<String> languageCodes = new ArrayList<String>();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            return languageCodes;
        }
        // If a list of language codes for this tenant already exists, return it.
        if (localeLanguageCodes != null && localeLanguageCodes.containsKey(tenantId)) {
            languageCodes = localeLanguageCodes.get(tenantId);
            if (languageCodes != null && languageCodes.size() > 0) {
                return languageCodes;
            }
        }
        // Otherwise, generate that list and cache it for re-use.
        TenantBindingConfigReaderImpl tReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        TenantBindingType tenantBinding = tReader.getTenantBinding(tenantId);
        languageCodes = TenantBindingUtils.getPropertyValues(tenantBinding,
                LOCALE_LANGUAGE_CODE_PROPERTY_NAME);
        languageCodes = validateLanguageCodes(languageCodes);
        if (localeLanguageCodes != null) {
            localeLanguageCodes.put(tenantId, languageCodes);
        }
        return languageCodes;
    }

    /**
     * Validates a list of language codes, verifying codes against a
     * list of valid ISO 639-1 language codes.
     *
     * @param patterns  a list of language codes.
     *
     * @return          a list of valid language codes, excluding any codes
     *                  that are not valid ISO 639-1 language codes.
     */
    public static List<String> validateLanguageCodes(List<String> languageCodes) {
        if (languageCodes == null) {
            return new ArrayList<String>();
        }
        List<String> validLanguageCodes = new ArrayList<String>();
        for (String code : languageCodes) {
            if (code != null && isoLanguageCodes.contains(code.trim())) {
                validLanguageCodes.add(code);
            }
        }
        return validLanguageCodes;
    }

    /**
     * Returns an ISO 8601 timestamp representation of a presumptive date or
     * date/time string.  Applies the set of date formatters for a supplied tenant,
     * in sequence, to attempt to parse the string, and returns the timestamp
     * resulting from the first successful parse attempt.
     *
     * @param str       a String, possibly a date or date/time String.
     * @param tenantId  a tenant ID.
     *
     * @return          an ISO 8601 timestamp representation of that String.
     *                  If the String cannot be parsed by the date formatters
     *                  for the supplied tenant, return null;
     */
    public static String toIso8601Timestamp(String dateStr, String tenantId) {
        Date date = null;
        List<DateFormat> formatters = getDateFormattersForTenant(tenantId);
        for (DateFormat formatter : formatters) {
            date = parseDate(dateStr, formatter);
            if (date != null) {
                break;
            }
        }
        if (date == null) {
            return null;
        } else {
            GregorianCalendar gcal = new GregorianCalendar();
            gcal.setTime(date);
            String isoStr = formatAsISO8601Timestamp(gcal);
            return isoStr;
        }
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
     * Returns a representation of a calendar date and time instance,
     * as an ISO 8601-formatted date.
     *
     * @param cal a calendar date and time instance.
     *
     * @return    a representation of that calendar date and time instance,
     *            as an ISO 8601-formatted date.
     */
    public static String formatAsISO8601Date(GregorianCalendar cal) {
        return formatGregorianCalendarDate(cal, GregorianCalendarDateTimeUtils.UTCTimeZone(),
                getDateFormatter(ISO_8601_DATE_PATTERN));
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

    /**
     * Identifies whether a presumptive date or date/time can be parsed
     * by a date parser, using a supplied format pattern.
     *
     * @param str      a String, possibly a date or date/time String.
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
        if (str == null || str.trim().isEmpty()) {
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
     * Parses a presumptive date or date/time, using a supplied format pattern.
     *
     * @param str      a String, possibly a date or date/time String.
     * @param pattern  A date or date/time pattern.
     *
     * @return         A date value, resulting from parsing the String using the
     *                 supplied pattern.  Returns null if the parsing attempt fails.
     */
    public static Date parseDate(String str, String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return null;
        }
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        DateFormat df = null;
        Date date = null;
        try {
            df = new SimpleDateFormat(pattern);
            date = parseDate(str, df);
        } catch (IllegalArgumentException iae) {
            return null;
        }
        return date;
    }

       /**
     * Parses a presumptive date or date/time, using a supplied format pattern.
     *
     * @param str      a String, possibly a date or date/time String.
     * @param df       A date formatter.
     *
     * @return         A date value, resulting from parsing the String using the
     *                 supplied formatter.  Returns null if the parsing attempt fails.
     */
    public static Date parseDate(String str, DateFormat df) {
        if (df == null) {
            return null;
        }
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        Date date = null;
        try {
            df.setLenient(false);
            date = df.parse(str);
        } catch (ParseException pe) {
            return null;
        }
        return date;
    }

    /**
     * Returns the locale associated with a supplied ISO 639-1 language code.
     *
     * @param lang     A language code.
     *
     * @return         A locale based on that language code; or null
     *                 if the code was null, empty, or invalid.
     */
    public static Locale getLocale(String lang) {
        if (lang == null || lang.trim().isEmpty()) {
            logger.warn("Null or empty date language code was provided when getting locale.");
            return NULL_LOCALE;
        }
        if (! isoLanguageCodes.contains(lang.trim())) {
            logger.warn("Invalid language code '" + lang + "'");
            return NULL_LOCALE;
        }
        return new Locale(lang);
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
        return getDateFormatter(pattern, NULL_LOCALE);
    }

    /**
     * Returns a date formatter for a supplied date or date/time pattern,
     * in the supplied locale (if any).
     *
     * @param pattern  A date or date/time pattern.
     * @param locale   A locale.
     *
     * @return         A date formatter using that pattern and locale (if any), or null
     *                 if the pattern was null, empty, or invalid.
     */
    public static DateFormat getDateFormatter(String pattern, Locale locale) {
        DateFormat df = null;
        if (pattern == null || pattern.trim().isEmpty()) {
            logger.warn("Null or empty date pattern string was provided when getting date formatter.");
            return df;
        }
        try {
            if (locale == null) {
                df = new SimpleDateFormat(pattern);
            } else {
                df = new SimpleDateFormat(pattern, locale);
            }
            df.setLenient(false);
        } catch (IllegalArgumentException iae) {
            logger.warn("Invalid date pattern string '" + pattern + "': " + iae.getMessage());
        }
        return df;
    }

}
