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
import  java.util.regex.Pattern;
import java.util.regex.Matcher;

/** General utility methods.
 *   @author Laramie Crocker
 * v.1.4
 */
public class Tools {
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
    public static boolean isTrue(String test){
        return notEmpty(test) && (new Boolean(test)).booleanValue();
    }

                    /*  Example usage of searchAndReplace:
                        for (Map.Entry<String,String> entry : variablesMap.entrySet()){
                            String key = entry.getKey();
                            String replace = entry.getValue();
                            String find = "\\$\\{"+key+"\\}";   //must add expression escapes
                                                                //because $ and braces are "special", and we want to find "${object.CSID}"
                            uri = Tools.searchAndReplace(uri, find, replace);
                            System.out.println("---- REPLACE.uri:        "+initURI);
                            System.out.println("---- REPLACE.find:       "+find);
                            System.out.println("---- REPLACE.replace:    "+replace);
                            System.out.println("---- REPLACE.uri result: "+uri);
                        }
                    */
    public static String  searchAndReplace(String source, String find, String replace){
        Pattern pattern = Pattern.compile(find);
        Matcher matcher = pattern.matcher(source);
        String output = matcher.replaceAll(replace);
        return output;
    }

    static boolean m_fileSystemIsDOS = "\\".equals(File.separator);
    static boolean m_fileSystemIsMac = ":".equals(File.separator);

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

    public static String getStackTrace(Throwable e){
        return getStackTrace(e, -1);
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
    public static String errorToString(Throwable e, boolean stackTraceOnException, int includeLines){
        if (e==null){
            return "";
        }
        String s = e.toString() + "\r\n  -- message: " + e.getMessage();

        StringBuffer causeBuffer = new StringBuffer();
        Throwable cause = e.getCause();
        while (cause != null){
            causeBuffer.append(cause.getClass().getName()+"::"+cause.getMessage()+"\r\n");
            cause = cause.getCause();
        }
        if (causeBuffer.length()>0) s = s + "\r\n  -- Causes: "+causeBuffer.toString();


        s = s + "\r\n  -- Stack Trace: \r\n  --      " + getStackTrace(e, includeLines);
        return s;
    }


}
