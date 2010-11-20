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
    
}
