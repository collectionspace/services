package org.collectionspace.services.common.api;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    final static String ISO_8601_DATE_PATTERN = "yyyy-MM-dd";
    final static String UTC_TIMEZONE_IDENTIFIER = "UTC";
    final static String ISO_8601_UTC_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    final static Locale NULL_LOCALE = null;
    public final static List<String> isoLanguageCodes = new ArrayList(Arrays.asList(Locale.getISOLanguages()));    

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
          if (!isoLanguageCodes.contains(lang.trim())) {
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
     
}
