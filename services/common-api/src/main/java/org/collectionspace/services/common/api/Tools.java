/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.collectionspace.services.common.api;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import  java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;


import java.util.regex.Matcher;

/** General utility methods.
 *   @author Laramie Crocker
 * v.1.4
 */
public class Tools {    
    private static final String PROPERTY_VAR_REGEX = "\\$\\{([A-Za-z0-9_\\.]+)\\}";
    
    /** @return first glued to second with the separator string, at most one time - useful for appending paths.
     */
    public static String glue(String first, String separator, String second){
        if (first==null) { first = ""; }
        if (second==null) { second = ""; }
        if (separator==null) { separator = ""; }
        if (first.startsWith(separator) && second.startsWith(separator)){
            return first.substring(0, first.length()-separator.length()) + second;
        }
        if (first.endsWith(separator) || second.startsWith(separator)){
            return first+second;
        }
        return first+separator+second;
    }

    /** Remove all whitespace from a String.  */
    public static String squeeze(String s) {
        return s.replaceAll("\\s+", "");
    }

    /** Milliseconds from start time as defined by the Date class. */
    public static Long now(){
        return new Long((new java.util.Date()).getTime());
    }

     public static String nowLocale(){
        java.util.Date date = new java.util.Date();
        String result = java.text.DateFormat.getDateTimeInstance().format(date);
        date = null;
        return result;
    }

    /** Handles null strings as empty.  */
    public static boolean isEmpty(String str){
        return !notEmpty(str);
    }

    /** nulls, empty strings, and empty after trim() are considered blank. */
    public static boolean isBlank(String str){
        return !notBlank(str);
    }

    /** Handles null strings as empty.  */
    public static boolean notEmpty(String str){
        if (str==null) return false;
        if (str.length()==0) return false;
        return true;
    }
    public static boolean notBlank(String str){
        if (str==null) return false;
        if (str.length()==0) return false;
        if (str.trim().length()==0){
            return false;
        }
        return true;
    }

    /** Handles null strings as false.  */
    public static boolean isTrue(String test) {
        return notEmpty(test) && (new Boolean(test)).booleanValue();
    }
    
    /** Handles null value with 'true' result.  */
    public static boolean isFalse(String test) {
        if (test == null) {
            return true;
        }
        
        return (new Boolean(test)).booleanValue() == false;
    }    

    public static String searchAndReplace(String source, String find, String replace){
        Pattern pattern = Pattern.compile(find);
        Matcher matcher = pattern.matcher(source);
        String output = matcher.replaceAll(replace);
        return output;
    }
    
    public static String searchAndReplaceWithQuoteReplacement(String source, String find, String replace){
        Pattern pattern = Pattern.compile(find);
        Matcher matcher = pattern.matcher(source);
        String output = matcher.replaceAll(Matcher.quoteReplacement(replace));
        return output;
    }

    static boolean m_fileSystemIsDOS = "\\".equals(File.separator);
    static boolean m_fileSystemIsMac = ":".equals(File.separator);
    
    public final static String FILE_EXTENSION_SEPARATOR = ".";
    public final static String OPTIONAL_VALUE_SUFFIX = "_OPT";

    public static boolean fileSystemIsDOS(){return m_fileSystemIsDOS;}
    public static boolean fileSystemIsMac(){return m_fileSystemIsMac;}

    public static String fixFilename(String filename){
        if ( m_fileSystemIsDOS ) {
            return filename.replace('/', '\\');
        }
        if ( m_fileSystemIsMac ) {
            String t = filename.replace('/', ':');
            t = t.replace('\\', ':');
            return t;
        }
        return filename.replace('\\','/');
    }

    public static String join(String dir, String file){
        if ( dir.length() == 0 ) {
            return file;
        }
        dir = Tools.fixFilename(dir);
        file = Tools.fixFilename(file);
        if ( ! dir.endsWith(File.separator) ) {
            dir += File.separator;
        }
        if ( file.startsWith(File.separator) ) {
            file = file.substring(1);
        }
        return dir + file;
    }
    
    public static String getFilenameExtension(String filename) {
        int dot = filename.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        return (dot>=0)?filename.substring(dot + 1):null;
        }

    public static String getFilenameBase(String filename) {
        int dot = filename.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        if(dot<0)
            dot = filename.length();
        int sep = filename.lastIndexOf(File.separator); // Note: if -1, then sep+1=0, which is right
        return filename.substring(sep + 1, dot);
        }

    public static String getStackTrace(Throwable e){
        return getStackTrace(e, -1);
    }
    
    public static String implode(String strings[], String sep) {
        String implodedString;
        if (strings.length == 0) {
            implodedString = "";
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append(strings[0]);
            for (int i = 1; i < strings.length; i++) {
                if (strings[i] != null && !strings[i].trim().isEmpty()) {
                    sb.append(sep);
                    sb.append(strings[i]);
                }
            }
            implodedString = sb.toString();
        }
        return implodedString;
    }
        



    /** @param includeLines if zero, returns all lines */
    public static String getStackTrace(Throwable e, int includeLines){
        if (e==null){
            return "";
        }
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(bos);
        e.printStackTrace(ps);
        String result = bos.toString();
        try {
            if(bos!=null)bos.reset();
            else System.out.println("bos was null, not closing");
        } catch (Exception e2)  {System.out.println("ERROR: couldn't reset() bos in Tools "+e2);}

        if (includeLines == 0){
            return result;   //return all.
        }
        StringBuffer sb = new StringBuffer();
        int i = 0;
        // BUG - \n is not the separator on all systems. Have to use the system line separator.
        String[] foo = result.split("\n");
        for (String line: foo){
            i++;
            if (i>includeLines){
                sb.append("  ...first "+i+" lines. "+(foo.length-i)+" more.\r\n");
                return sb.toString();
            }
            sb.append(line).append("\r\n");
        }
        return sb.toString();
    }

    public static String errorToString(Throwable e, boolean stackTraceOnException){
        return errorToString(e, stackTraceOnException, 0);
    }

    /** Takes an Exception object and formats a message that provides more debug information
      * suitable for developers for printing to System.out or for logging.  Not suitable for
      * presentation of error messages to clients.
     * @param includeLines if zero, return all lines of stack trace, otherwise return number of lines from top.
      */
    public static String errorToString(Throwable e, boolean stackTraceOnException, int includeLines) {
        if (e == null) {
            return "";
        }
        String s = "\r\n  -- Exception: " + e.getClass().getCanonicalName() + "\r\n  -- Message: " + e.getMessage();

        StringBuffer causeBuffer = new StringBuffer();
        Throwable cause = e.getCause();
        while (cause != null) {
            causeBuffer.append(cause.getClass().getName() + "::" + cause.getMessage() + "\r\n");
            cause = cause.getCause();
        }
        if (causeBuffer.length() > 0) {
            s = s + "\r\n  -- Causes: " + causeBuffer.toString();
        }

        s = s + "\r\n  -- Stack Trace: \r\n  --      " + getStackTrace(e, includeLines);
        return s;
    }

    /**
     * Return a set of properties from a properties file.
     * 
     * @param clientPropertiesFilename
     * @return
     */
    static public Properties loadProperties(String clientPropertiesFilename) {
        Properties inProperties = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = null;

        try {
            is = cl.getResourceAsStream(clientPropertiesFilename);
            inProperties.load(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return inProperties;
    }
    
    static public Properties loadProperties(String clientPropertiesFilename, boolean filterPasswords) throws Exception {
        Properties result = loadProperties(clientPropertiesFilename);

        if (filterPasswords) {
            result = filterPropertiesWithEnvVars(result);
        }
        
        return result;
    }
    
    /**
     * Looks for property values if the form ${foo} and tries to find environment property "foo" value to replace with.
     * 
     * For example, a property value of "${foo}" would be replaced with the value of the environment variable "foo" if a
     * value for "foo" exists in the current environment.
     * 
     * @param inProperties
     * @return
     * @throws Exception
     */
    static public Properties filterPropertiesWithEnvVars(Properties inProperties) throws Exception {
        final String filteredFlag = "fe915b1b-7411-4aaa-887f";
        final String filteredKey = filteredFlag;        
        Properties result = inProperties;
        
        if (inProperties.containsKey(filteredKey) == false) {
            // Only process the properties once
            if (inProperties != null && inProperties.size() > 0) {
                for (String key : inProperties.stringPropertyNames()) {
                    String propertyValue = inProperties.getProperty(key);
                    String newPropertyValue = Tools.getValueFromEnv(propertyValue);
                    if (newPropertyValue != null) { // non-null result means the property value was the name of an environment variable
                        inProperties.setProperty(key, newPropertyValue);
                    }
                }
                inProperties.setProperty(filteredKey, filteredFlag); // set to indicated we've already process these properties
            }
        }
        
        return result;
    }
        
    static public boolean isOptional(String properyValue) {
        boolean result = false;
        
        result = properyValue.endsWith(OPTIONAL_VALUE_SUFFIX);
        
        return result;
    }
    
    /**
     * Try to find the value of a property variable in the system or JVM environment.  This code substitutes only property values formed
     * like ${cspace.password.mysecret} or ${cspace_password_mysecret_secret}.  The corresponding environment variables would
     * be "cspace.password.mysecret" and "cspace.password.mysecret.secret".
     * 
     * Returns null if the passed in property value is not a property variable -i.e., not something of the form {$cspace.password.foo}
     * 
     * Throws an exception if the passed in property value has a valid variable form but the corresponding environment variable is not
     * set.
     * 
     * @param propertyValue
     * @return
     * @throws Exception
     */
    static public String getValueFromEnv(String propertyValue) throws Exception {
        String result = null;
        //
        // Replace things like ${cspace.password.cow} with values from either the environment
        // or from the JVM system properties.
        //
        Pattern pattern = Pattern.compile(PROPERTY_VAR_REGEX);      // For example, "${cspace.password.mysecret}" or "${password_strong_longpassword}"
        Matcher matcher = pattern.matcher(propertyValue);
        String key = null;    
        if (matcher.find()) {
            key = matcher.group(1);  // Gets the string inside the ${} enclosure.  For example, gets "cspace.password.mysecret" from "${cspace.password.mysecret}"
            result = System.getenv(key);
            if (result == null || result.isEmpty()) {
                // If we couldn't find a value in the environment, check the JVM system properties
                result = System.getProperty(key);
            }

            if (result == null || result.isEmpty()) {
                String errMsg = String.format("Could find neither an environment variable nor a systen variable named '%s'", key);
                if (isOptional(key) == true) {
                    System.err.println(errMsg);
                } else {
                    throw new Exception(errMsg);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Test to see if 'propertyValue' is actually a property variable
     * @param propertyValue
     * @return
     */
    static public boolean isValuePropretyVar(String propertyValue) {
        boolean result = false;
        
        if (propertyValue != null) {
            Pattern pattern = Pattern.compile(PROPERTY_VAR_REGEX);      // For example, "${cspace.password.mysecret}" or "${password_strong_longpassword}"
            Matcher matcher = pattern.matcher(propertyValue);
            if (matcher.find()) {
                result = true;
            }
        }
        
        return result;
    }

    public static boolean isEmpty(List<?> theList) {
        if (theList != null && theList.size() > 0) {
            return false;
        } else {
            return true;
        }
    }
    
    static public boolean listContainsIgnoreCase(List<String> theList, String searchStr) {
    	boolean result = false;
    	
    	for (String listItem : theList) {
    		if (StringUtils.containsIgnoreCase(listItem, searchStr)) {
    			return true;
    		}
    	}
    	
    	return result;
    }
}
