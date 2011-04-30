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
package org.collectionspace.services.IntegrationTests.test;

import org.collectionspace.services.IntegrationTests.xmlreplay.TreeWalkResults;
import org.collectionspace.services.IntegrationTests.xmlreplay.XmlCompareJdom;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class XmlCompareJdomTest {


    private void testBanner(String msg){
        String BANNER ="-------------------------------------------------------";
        System.out.println(BANNER+"\r\n"+this.getClass().getName()+"\r\n"+msg+"\r\n"+BANNER);
    }
    public static void printTreeWalkResults(TreeWalkResults list){
        for (TreeWalkResults.TreeWalkEntry entry : list){
            System.out.println(entry.toString());
        }
    }

    static void assertTrue(boolean stmt, String msg, TreeWalkResults results){
        if (!stmt){
            System.out.println("=====> Assertion Failed: "+msg);
            printTreeWalkResults(results);
        }
        Assert.assertTrue(stmt, msg);
    }
    static void assertEquals(Object o1, Object o2, String msg, TreeWalkResults results){
        if ( ! o1.equals(o2)) {
            System.out.println("=====> Assertion Equals Failed: "+" o1: {"+o1+"} o2: {"+o2+"}"+"\r\n        "+msg);
            printTreeWalkResults(results);
        }
       Assert.assertEquals(o1, o2, msg);
    }

    public  static void assertTreeWalkResults(TreeWalkResults results,
                                                                      int addedRight,
                                                                      int missingRight,
                                                                      int textMismatches,
                                                                      boolean strictMatch,
                                                                      TreeWalkResults.MatchSpec matchSpec){
        int addedr = results.countFor(TreeWalkResults.TreeWalkEntry.STATUS.R_ADDED);
        int missingr = results.countFor(TreeWalkResults.TreeWalkEntry.STATUS.R_MISSING);
        int tdiff = results.countFor(TreeWalkResults.TreeWalkEntry.STATUS.TEXT_DIFFERENT);
        int badCount = results.getMismatchCount();
        boolean strict = results.isStrictMatch();
        boolean treeOK = results.treesMatch(matchSpec);

        String expected = "\r\n        expected: addedRight:"+addedRight+",missingRight:"+missingRight+",textMismatches:"+textMismatches
                              +",strictMatch:"+strictMatch+",matchSpec:"+matchSpec;

        String actual   = "\r\n        actual:   addedRight:"+addedr+",missingRight:"+missingr+",textMismatches:"+tdiff
                              +",strictMatch:"+strict+",matchSpec:"+matchSpec;
        String exp_act = expected +"\r\n"+actual+"\r\n";
        boolean done = false;
        try {
           assertEquals(addedr, addedRight, "assertTreeWalkResults:R_ADDED mismatch." + exp_act, results);
            assertEquals(missingr, missingRight, "assertTreeWalkResults:R_MISSING mismatch." + exp_act, results);
            assertEquals(tdiff, textMismatches, "assertTreeWalkResults:TEXT_DIFFERENT mismatch." + exp_act, results);
            assertTrue((strict == strictMatch), "assertTreeWalkResults:strictMatch mismatch." + exp_act, results);
            assertTrue((treeOK), "assertTreeWalkResults:treesMatch("+matchSpec+") returned false."+exp_act, results);
            //System.out.println("SUCCESS: assertTreeWalkResults done.\r\n");
            done = true;
        } finally {
            if (!done) System.out.println("FAILURE: assertTreeWalkResults failed an assertion. See surefire report.\r\n");
        }
    }

    @Test
    public void testXmlCompareJdom(){
        testBanner("testXmlCompareJdom");
        TreeWalkResults.MatchSpec matchSpec = TreeWalkResults.MatchSpec.createDefault();
        TreeWalkResults results =
                    XmlCompareJdom.compareParts(expectedPartContent,
                                        "expected",
                                        partFromServer,
                                        "from-server",
                                        exPARTNAME,
                                        matchSpec);
        assertTreeWalkResults(results,0,0,0,true,matchSpec);
                                   // addedRight,missingRight,textMismatches,strictMatch,treesMatch
    }

    @Test
    public void testTextContentDifferent(){
        testBanner("testTextContentDifferent");
        TreeWalkResults.MatchSpec matchSpec = TreeWalkResults.MatchSpec.createDefault();
        matchSpec.removeErrorFromSpec(TreeWalkResults.TreeWalkEntry.STATUS.TEXT_DIFFERENT);
        TreeWalkResults results =
            XmlCompareJdom.compareParts(expectedPartContent,
                                        "expected",
                                        srvHEAD+srvEN2+srvDEPOSITOR+srvFOOT,
                                        "from-server",
                                        exPARTNAME,
                                        matchSpec);
        assertTreeWalkResults(results,0,0,1,false,matchSpec);
                                   // addedRight,missingRight,textMismatches,strictMatch,treesMatch
    }


    @Test
    public void testAddedR(){
        testBanner("testAddedR");
        TreeWalkResults.MatchSpec matchSpec = TreeWalkResults.MatchSpec.createDefault();
        TreeWalkResults results =
            XmlCompareJdom.compareParts(expectedPartContent,
                                        "expected",
                                        srvHEAD+srvEN+exNEWTREE+srvDEPOSITOR+exNEW+srvFOOT,
                                        "from-server",
                                        exPARTNAME,
                                        matchSpec);
        assertTreeWalkResults(results,2,0,0,false,matchSpec);
                                   // addedRight,missingRight,textMismatches,strictMatch,treesMatch

    }

    @Test
    public void testAddedL(){
        testBanner("testAddedL");
        TreeWalkResults.MatchSpec matchSpec = TreeWalkResults.MatchSpec.createDefault();
        matchSpec.removeErrorFromSpec(TreeWalkResults.TreeWalkEntry.STATUS.R_MISSING);

        TreeWalkResults results =
            XmlCompareJdom.compareParts(exHEAD + exEN_WCH + exNEWTREE + exDEP  + exNEW + exFOOT,
                                    "expected",
                                    partFromServer,
                                    "from-server",
                                    exPARTNAME,
                                    matchSpec);
        assertTreeWalkResults(results,0,3,0,false,matchSpec);
                                   // addedRight,missingRight,textMismatches,strictMatch,treesMatch
    }

    @Test
    public void testChildrenReordered(){
        testBanner("testChildrenReordered");
        TreeWalkResults.MatchSpec matchSpec = TreeWalkResults.MatchSpec.createDefault();
        TreeWalkResults results =
            XmlCompareJdom.compareParts(exHEAD  + exDEP + exEN + exFOOT,
                                    "expected",
                                    partFromServer,
                                    "from-server",
                                    exPARTNAME,
                                    matchSpec);
        assertTreeWalkResults(results,0,0,0,true,matchSpec);
                                   // addedRight,missingRight,textMismatches,strictMatch,treesMatch
    }

    // ============  expected part, will be used as LEFT tree ==========================================================
    private static String exPARTNAME = "objectexit_common";

    private static String exHEAD    ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                                    +"<document name=\"objectexit\">"
                                    +"<ns2:objectexit_common \r\n"
                                    +"    xmlns:ns2=\"http://collectionspace.org/services/objectexit\" \r\n"
                                    +"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \r\n"
                                    +"    xsi:schemaLocation=\"http://collectionspace.org/services/objectexit http://services.collectionspace.org/objectexit/objectexit_common.xsd\">\r\n";
    private static String exEN      =" <exitNumber>objectexitNumber-1290026472360</exitNumber>\r\n";
    private static String exEN_WCH  =" <exitNumber>objectexitNumber-1290026472360\r\n"
                                    +"    <enChild>\r\n"
                                    +"        enChild content\r\n"
                                    +"    </enChild>\r\n"
                                    +" </exitNumber>\r\n";
    private static String exNEWTREE =" <first>\r\n"
                                    +"    <second>\r\n"
                                    +"        second content\r\n"
                                    +"    </second>\r\n"
                                    +" </first>\r\n";
    private static String exDEP     =" <depositor>urn:cspace:org.collectionspace.demo:orgauthority:name(TestOrgAuth):organization:name(Northern Climes Museum)'Northern Climes Museum'</depositor>\r\n";
    private static String exNEW     =" <newField>objectexitNumber-1290026472360</newField>\r\n";
    private static String exFOOT    ="</ns2:objectexit_common>"
                                                     +"</document>";

    private static String expectedPartContent = exHEAD + exEN + exDEP  + exFOOT;


    // ============  from-server part, will be used as RIGHT tree ==========================================================

    private static String srvHEAD =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                                    +"<document name=\"objectexit\">"
                                    +"<ns2:objectexit_common xmlns:ns2=\"http://collectionspace.org/services/objectexit\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://collectionspace.org/services/objectexit http://services.collectionspace.org/objectexit/objectexit_common.xsd\">\r\n";

    private static String srvEN    = "<exitNumber>objectexitNumber-1290026472360</exitNumber>\r\n";
    private static String srvEN2   = "<exitNumber>objectexitNumber-9999999999999</exitNumber>\r\n";
    private static String srvDEPOSITOR  = "<depositor>urn:cspace:org.collectionspace.demo:orgauthority:name(TestOrgAuth):organization:name(Northern Climes Museum)'Northern Climes Museum'</depositor>\r\n";
    private static String srvFOOT =  "</ns2:objectexit_common>\r\n"
                                                       +"</document>";

    private static String partFromServer = srvHEAD+srvEN+srvDEPOSITOR+srvFOOT;



}
