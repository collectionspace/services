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
package org.collectionspace.services.IntegrationTests.xmlreplay;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class XmlReplayEval {
    public Map<String, ServiceResult> serviceResultsMap;
    public JexlEngine jexl;
    public JexlContext jc;

    /**
     * You may pass in a Jexl 2 expression, e.g. ${foo.bar} and it will be eval'd for you.
     * We are looking at some URI like so: ${newOrgAuthority.CSID}
     * The idea here is that the XML control file may bind to this namespace, and
     * this module may find those values and any future extensions, specifically
     * when someone says "I want to bind to ${CSID} and ${SUBRESOURCE.CSID}
     * The code here is easy to extend, but the test cases build up, so you don't
     * want to break all the config files by not being backward compatible.  Binding
     * to context variables like this makes it easy.
     * EXAMPLE USAGE: <br />
     * String uri = "/cspace-services/orgauthorities/${OrgAuth1.CSID}/items/${Org1.CSID}";   <br />
     * uri = eval(uri, serviceResultsMap, jexl, jc);  <br />
     * RESULT:    "/cspace-services/orgauthorities/43a2739c-4f40-49c8-a6d5/items/"
     */
     public static String eval(String inputJexlExpression, Map<String, ServiceResult> serviceResultsMap, Map<String,String> vars, JexlEngine jexl, JexlContext jc) {
        //System.out.println("\r\n---- REPLACE.init-uri:        "+inputJexlExpression);
        String result;
        try {
             jc.set("itemCSID", "${itemCSID}"); //noiseless passthru.
            //System.out.println("eval :: serviceResultsMap "+serviceResultsMap.size());
            for (ServiceResult serviceResult : serviceResultsMap.values()) {
                jc.set(serviceResult.testID, serviceResult);
                //System.out.println("eval :: "+serviceResult.testID+"==>"+serviceResult.minimal());
            }
            if (vars!=null){
                for (Map.Entry<String,String> entry: vars.entrySet()) {
                    String value = entry.getValue();
                    String key = entry.getKey();
                    try {
                        value = parse(value, jexl, jc);
                        vars.put(key, value); //replace template value with actual value.
                    } catch (Exception e){
                        value = "ERROR: "+e;
                    }
                    jc.set(key, value);
                }
            }
            result = parse(inputJexlExpression, jexl, jc);
        } catch (Throwable t) {
            System.err.println("ERROR: " + t);
            result = "ERROR";
        }
        //System.out.println("---- REPLACE.uri:        "+result+"\r\n");
        return result;
    }

    private static String parse(String in, JexlEngine jexl, JexlContext jc) {
        StringBuffer result = new StringBuffer();
        String s = in;
        String var = "";
        int start, end, len;
        len = in.length();
        start = 0;
        int cursor = 0;
        String front = "";
        while (start < len) {
            end = in.indexOf("}", start);
            start = in.indexOf("${", start);
            if (start < 0) {
                String tail = in.substring(cursor);
                result.append(tail);
                break;
            }
            if (end < 0) {
                return "ERROR: unbalanced ${} braces";
            }
            front = in.substring(cursor, start);
            result.append(front);
            cursor = end + 1;                   //bump past close brace
            var = in.substring(start + 2, end);  //+2 bump past open brace ${ and then "end" is indexed just before the close brace }
            //s   = s.substring(end+1);         //bump past close brace
            start = cursor;

            Expression expr = jexl.createExpression(var);
            Object resultObj = expr.evaluate(jc);
            String resultStr;
            if (null == resultObj){
                //debug: System.out.println("null found while evaluationg variable: '"+var+"' Jexl context: "+dumpContext(jc));
                resultStr = "${"+var+"}";
            } else {
                resultStr = resultObj.toString();

            }
            result.append(resultStr);
        }
        return result.toString();
    }

    protected static String dumpContext(JexlContext jc){
        String result = "";
        if (jc instanceof MapContextWKeys){
            Set keys = ((MapContextWKeys)jc).getKeys();
            result = keys.toString();
        }  else {
            result = jc.toString();
        }
        return result;
    }

    public static class MapContextWKeys extends MapContext implements JexlContext {
        private Map<String,Object> map = new HashMap();
        public Set getKeys(){
            return this.map.keySet();
        }
    }


}
