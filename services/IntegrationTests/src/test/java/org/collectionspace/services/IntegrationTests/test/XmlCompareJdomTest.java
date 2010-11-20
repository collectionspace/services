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
    private void printTreeWalkResults(TreeWalkResults list){
        for (TreeWalkResults.TreeWalkEntry entry : list){
            System.out.println(entry.toString());
        }
    }

    private void assertTreeWalkResults(TreeWalkResults results,
                                       int addedRight,
                                       int missingRight,
                                       int textMismatches,
                                       boolean strictMatch,
                                       boolean treesMatch){
        System.out.println("assertTreeWalkResults: ");

        int addedr = results.countFor(TreeWalkResults.TreeWalkEntry.STATUS.R_ADDED);
        int missingr = results.countFor(TreeWalkResults.TreeWalkEntry.STATUS.R_MISSING);
        int tdiff = results.countFor(TreeWalkResults.TreeWalkEntry.STATUS.TEXT_DIFFERENT);
        int badCount = results.getMismatchCount();
        boolean strict = results.isStrictMatch();
        boolean treeOK = results.treesMatch();

        String expected = "    expected: addedRight:"+addedRight+",missingRight:"+missingRight+",textMismatches:"+textMismatches
                              +",strictMatch:"+strictMatch+",treesMatch:"+treesMatch;

        String actual   = "    actual:   addedRight:"+addedr+",missingRight:"+missingr+",textMismatches:"+tdiff
                              +",strictMatch:"+strict+",treesMatch:"+treeOK;

        String exp_act = expected +"\r\n"+actual+"\r\n";
        System.out.print(exp_act);

        printTreeWalkResults(results);


        boolean done = false;
        try {
            Assert.assertEquals(addedr, addedRight, "assertTreeWalkResults:R_ADDED mismatch."+exp_act);

            Assert.assertEquals(missingr, missingRight, "assertTreeWalkResults:R_MISSING mismatch."+exp_act);

            Assert.assertEquals(tdiff, textMismatches, "assertTreeWalkResults:TEXT_DIFFERENT mismatch."+exp_act);


            Assert.assertTrue((strict==strictMatch), "assertTreeWalkResults:strictMatch mismatch."+exp_act);

            Assert.assertTrue((treeOK==treesMatch), "assertTreeWalkResults:treesMatch mismatch."+exp_act);

            System.out.println("SUCCESS: assertTreeWalkResults done.\r\n");
            done = true;
        } finally {
            if (!done) System.out.println("FAILURE: assertTreeWalkResults failed an assertion. See surefire report.\r\n");
        }
    }

    @Test
    public void testXmlCompareJdom(){
        testBanner("testXmlCompareJdom");
        TreeWalkResults results =
                    XmlCompareJdom.compareParts(expectedPartContent,
                                        "expected",
                                        partFromServer,
                                        "from-server");
        assertTreeWalkResults(results,0,0,0,true,true);
                                   // addedRight,missingRight,textMismatches,strictMatch,treesMatch
    }

    @Test
    public void testTextContentDifferent(){
        testBanner("testTextContentDifferent");
        TreeWalkResults results =
            XmlCompareJdom.compareParts(expectedPartContent,
                                        "expected",
                                        srvHEAD+srvEN2+srvDEPOSITOR+srvFOOT,
                                        "from-server");
        assertTreeWalkResults(results,0,0,1,false,true);
                                   // addedRight,missingRight,textMismatches,strictMatch,treesMatch
    }


    @Test
    public void testAddedR(){
        testBanner("testAddedR");
        TreeWalkResults results =
            XmlCompareJdom.compareParts(expectedPartContent,
                                        "expected",
                                        srvHEAD+srvEN+exNEWTREE+srvDEPOSITOR+exNEW+srvFOOT,
                                        "from-server");
        assertTreeWalkResults(results,2,0,0,false,false);
                                   // addedRight,missingRight,textMismatches,strictMatch,treesMatch

    }

    @Test
    public void testAddedL(){
        testBanner("testAddedL");
        TreeWalkResults results =
            XmlCompareJdom.compareParts(exHEAD + exEN_WCH + exNEWTREE + exDEP  + exNEW + exFOOT,
                                    "expected",
                                    partFromServer,
                                    "from-server");
        assertTreeWalkResults(results,0,3,0,false,false);
                                   // addedRight,missingRight,textMismatches,strictMatch,treesMatch
    }

    @Test
    public void testChildrenReordered(){
        testBanner("testChildrenReordered");
        TreeWalkResults results =
            XmlCompareJdom.compareParts(exHEAD  + exDEP + exEN + exFOOT,
                                    "expected",
                                    partFromServer,
                                    "from-server");
        assertTreeWalkResults(results,0,0,0,true,true);
                                   // addedRight,missingRight,textMismatches,strictMatch,treesMatch
    }


    // ============  expected part, will be used as LEFT tree ==========================================================
    private static String exHEAD    ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
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
    private static String exFOOT    ="</ns2:objectexit_common>";

    private static String expectedPartContent = exHEAD + exEN + exDEP  + exFOOT;


    // ============  from-server part, will be used as RIGHT tree ==========================================================

    private static String srvHEAD =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                                    +"<ns2:objectexit_common xmlns:ns2=\"http://collectionspace.org/services/objectexit\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://collectionspace.org/services/objectexit http://services.collectionspace.org/objectexit/objectexit_common.xsd\">\r\n";
    private static String srvEN    = "<exitNumber>objectexitNumber-1290026472360</exitNumber>\r\n";
    private static String srvEN2   = "<exitNumber>objectexitNumber-9999999999999</exitNumber>\r\n";
    private static String srvDEPOSITOR  = "<depositor>urn:cspace:org.collectionspace.demo:orgauthority:name(TestOrgAuth):organization:name(Northern Climes Museum)'Northern Climes Museum'</depositor>\r\n";
    private static String srvFOOT =  "</ns2:objectexit_common>\r\n";

    private static String partFromServer = srvHEAD+srvEN+srvDEPOSITOR+srvFOOT;



}
