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

import org.collectionspace.services.common.Tools;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jaxen.XPath;
import org.jaxen.jdom.JDOMXPath;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.IntegrationTests.xmlreplay.TreeWalkResults.TreeWalkEntry;
import org.jdom.output.XMLOutputter;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class XmlCompareJdom {

private static final String DEFAULT_SAX_DRIVER_CLASS = "org.apache.xerces.parsers.SAXParser";

    public static org.jdom.Document getDocumentFromContent(String source) throws IOException, JDOMException {
        org.jdom.Document doc;
        SAXBuilder builder;
        builder = new SAXBuilder();
        builder.setValidation(false); //has no effect, I think.
        doc = builder.build(new StringReader(source));
        return doc;
    }

    public static TreeWalkResults compareParts(String expectedContent, String leftID, String actualPartContent, String rightID){
        TreeWalkResults list = new TreeWalkResults();
        try {

            list.leftID = leftID;
            list.rightID = rightID;
            TreeWalkResults.TreeWalkEntry infoentry = new TreeWalkResults.TreeWalkEntry();
            infoentry.status = TreeWalkResults.TreeWalkEntry.STATUS.INFO;
            infoentry.message = "\r\n    LEFT file: "+leftID+"\r\n    RIGHT file: "+rightID;
            list.add(infoentry);
            if (Tools.isEmpty(expectedContent)){
                TreeWalkEntry entry = new TreeWalkEntry();
                entry.status = TreeWalkEntry.STATUS.DOC_ERROR;
                entry.errmessage = "L dom was empty.";
                list.add(entry);
            } else if (Tools.isEmpty(actualPartContent)){
                TreeWalkEntry entry = new TreeWalkEntry();
                entry.errmessage = "R dom was empty.";
                entry.status = TreeWalkEntry.STATUS.DOC_ERROR;
                list.add(entry);
            } else {
                Document expected = getDocumentFromContent(expectedContent);
                Document actual = getDocumentFromContent(actualPartContent);
                treeWalk(expected, actual, list);
            }
        } catch (Throwable t){
            String msg = "ERROR in XmlReplay.compareParts(): "+t;
            System.out.println(msg);
            TreeWalkEntry entry = new TreeWalkEntry();
                entry.status = TreeWalkEntry.STATUS.DOC_ERROR;
                entry.errmessage = msg;
                list.add(entry);
        }
        return list;
    }

    public static List select(Element element, String xpathExpression) throws Exception {
        XPath xpath = new JDOMXPath(xpathExpression);
        return xpath.selectNodes(element);
    }

    public static Object selectSingleNode(Element element, String xpathExpression) throws Exception {
        XPath xpath = new JDOMXPath(xpathExpression);
        return xpath.selectSingleNode(element);
    }




    public static boolean treeWalk(Document left, Document right, TreeWalkResults list) throws Exception {
        boolean res = treeWalk(left.getRootElement(), right.getRootElement(), "/", list);
        return res;
    }

    public static boolean treeWalk(Element left, Element right, String parentPath, TreeWalkResults msgList) throws Exception {
        String SPACE = "     ";
        if (left == null && right == null){
            return true;
        }
        if (left == null){
            return false;
        }
        if (right == null){
            return false;
        }
        List l = left.getChildren();
        Map foundRightMap = new HashMap();
        List<String> foundRepeatingList = new ArrayList<String>();
        boolean result = true;
        for (Object o : l) {
            if (!(o instanceof Element)){
                continue;
            }
            Element leftChild = (Element)o;
            String leftChildName = leftChild.getName();
            if (Tools.isEmpty(leftChildName)){
                continue;
            }
            String leftChildPath = Tools.glue(parentPath, "/", leftChildName);

            if (foundRepeatingList.indexOf(leftChildPath)>=0){
                continue;
            }
            List leftlist = select(left, leftChildName);
            if (leftlist != null && leftlist.size() > 1){
                //System.out.println("-----------------doRepeating------"+leftChildPath);
                foundRepeatingList.add(leftChildPath);
                boolean repeatingIdentical =
                    doRepeatingFieldComparison(leftlist, leftChildPath, leftChildName, left, right, msgList) ; //todo: deal with foundRightMap in this repeating field block.
                if ( ! repeatingIdentical ){
                    //System.out.println("\r\n\r\n\r\n*****************************\r\nOne repeating field failed: "+msgList);
                    return false;
                }
                foundRightMap.put(leftChildName, "OK");
            } else {
                Element rightChild  = (Element)selectSingleNode(right,leftChildName);
                if (rightChild == null){
                    TreeWalkEntry entry = new TreeWalkEntry();
                    entry.lpath = leftChildPath;
                    entry.status = TreeWalkEntry.STATUS.R_MISSING;
                    msgList.add(entry);
                    continue;
                }
                foundRightMap.put(leftChildName, "OK");
                String leftChildTextTrim = leftChild.getText().trim();
                String rightChildTextTrim = rightChild.getText().trim();
                TreeWalkEntry entry = new TreeWalkEntry();
                entry.ltextTrimmed = leftChildTextTrim;
                entry.rtextTrimmed = rightChildTextTrim;
                entry.lpath = leftChildPath;
                entry.rpath = leftChildPath; //same

                if (leftChildTextTrim.equals(rightChildTextTrim)){
                    entry.status = TreeWalkEntry.STATUS.MATCHED;
                    msgList.add(entry);
                } else {
                    entry.status = TreeWalkEntry.STATUS.TEXT_DIFFERENT;
                    msgList.add(entry);
                }
                //============ DIVE !! =====================================================
                result = result && treeWalk( leftChild, rightChild, leftChildPath, msgList);
            }
        }
        for (Object r : right.getChildren()){
            if (!(r instanceof Element)){
                continue;
            }
            Element rightChild = (Element)r;
            String rname = rightChild.getName();
            if (null==foundRightMap.get(rname)){
                String rightChildPath = Tools.glue(parentPath, "/", rname);

                TreeWalkEntry entry = new TreeWalkEntry();
                entry.rpath = rightChildPath;
                entry.status = TreeWalkEntry.STATUS.R_ADDED;
                msgList.add(entry);
            }
        }
        return true;
    }

    private static void dumpXML_OUT(Element el) throws Exception {
        XMLOutputter outputter = new XMLOutputter();
        outputter.output(el, System.out);
    }
    private static String dumpXML(Element el) throws Exception {
        XMLOutputter outputter = new XMLOutputter();
        return outputter.outputString(el);
    }

    public static boolean doRepeatingFieldComparison(List leftList, String leftChildPath, String leftChildName, Element left, Element right, TreeWalkResults msgList)
    throws Exception {
        //todo: deal with foundRightMap in this repeating field block.
        List rightList = select(right, leftChildName);
        if (rightList == null || rightList.size() == 0 || rightList.size() < leftList.size()){
            TreeWalkEntry twe = new TreeWalkEntry();
            twe.lpath = leftChildPath;
            twe.status = TreeWalkEntry.STATUS.R_MISSING;
            String rmsg = (rightList == null)
                    ? " Right: 0"
                    : " Right: "+rightList.size();
            twe.message = "Repeating field count not matched. Field: "+leftChildPath+" Left: "+leftList.size()+rmsg;
            msgList.add(twe);
            return false;
        }
        if (rightList.size() > leftList.size()){
            TreeWalkEntry twe = new TreeWalkEntry();
            twe.lpath = leftChildPath;
            twe.status = TreeWalkEntry.STATUS.R_ADDED;
            twe.message = "Repeating field count not matched. Field: "+leftChildPath+" Left: "+leftList.size()+" Right: "+rightList.size();
            msgList.add(twe);
            return false;
        }

        for (Object le : leftList){
            boolean found = false;
            Element leftEl = (Element)le;
            //pl("left", leftEl);
            for(Object re : rightList){
                Element rightEl = (Element)re;
                //pl("right", rightEl);
                TreeWalkResults msgListInner = new TreeWalkResults();
                treeWalk(leftEl, rightEl, leftChildPath, msgListInner);
                if (msgListInner.isStrictMatch()){
                    found = true;
                    TreeWalkEntry twe = new TreeWalkEntry();
                    twe.lpath = leftChildPath;
                    twe.status = TreeWalkEntry.STATUS.MATCHED;
                    msgList.add(twe);
                    //System.out.println("===========================\r\nfound match for "+leftEl+"\r\n===========================\r\n");
                    rightList.remove(re); //found it, don't need to inspect this element again.  Since we are breaking from loop, removing element won't mess up iterator--we get a new one on the next loop.
                    break;
                }
            }
            if ( ! found){
                TreeWalkEntry twe = new TreeWalkEntry();
                twe.lpath = leftChildPath;
                twe.status = TreeWalkEntry.STATUS.R_MISSING;
                twe.message = "Repeating field not matched. Source: {"+dumpXML(leftEl)+"}";
                msgList.add(twe);
                return false;
            }
        }
        return true;
    }

    private static void pl(String name, Element el) throws Exception {
        Object lobid = selectSingleNode(el, "@ID");
        String lid = "";
        if (lobid!=null){
            lid = lobid.toString();
        }

        System.out.println(name+": "+lid);
        dumpXML_OUT(el);
        System.out.println();

    }
}
