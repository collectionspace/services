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
import org.collectionspace.services.common.api.DateUtils;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
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
                    locale = DateUtils.getLocale(languageCode);
                    for (String pattern : patterns) {
                        df = DateUtils.getDateFormatter(pattern, locale);
                        if (df != null)  {
                            formatters.add(df);
                        }
                    }
                }
            }
        } else {
            for (String pattern : patterns) {
                df = DateUtils.getDateFormatter(pattern, locale);
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
                df = DateUtils.getDateFormatter(pattern);
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
            if (code != null && DateUtils.isoLanguageCodes.contains(code.trim())) {
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
            date = DateUtils.parseDate(dateStr, formatter);
            if (date != null) {
                break;
            }
        }
        if (date == null) {
            return null;
        } else {
            GregorianCalendar gcal = new GregorianCalendar();
            gcal.setTime(date);
            String isoStr = GregorianCalendarDateTimeUtils.formatAsISO8601Timestamp(gcal);
            return isoStr;
        }
    }
}
