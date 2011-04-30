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
import org.collectionspace.services.common.api.FileTools;
import org.collectionspace.services.common.api.Tools;
import org.testng.annotations.Test;

import java.io.File;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class XmlCompareJdomRepeatingTest {

    private static String getDirectory(){
        String dataDir = "src/test/resources/test-data/xmlreplay/XmlCompareJdom";   // this dir lives under service/IntegrationTests
        String pwd = ".";
        try {
            pwd = (new File(".")).getCanonicalPath();
        } catch (Exception e){
            System.err.println("Error trying to find current working directory: "+e);
        }
        String thisDir = Tools.glue(pwd, "/", dataDir);
        return thisDir;
    }

    private void testBanner(String msg){
        String BANNER ="-------------------------------------------------------";
        String R = "\r\n";
        System.out.println(BANNER
                                     + R +" TEST CLASS: "+this.getClass().getName()
                                     + R +" TEST NAME: "+msg
                                     + R +" TEST DATA DIR: "+getDirectory()
                                     + R
                                     +BANNER);
    }

    @Test
    public void testLeftAndRightSame(){
        testBanner("testLeftAndRightSame");
        String dir = getDirectory();
        String expectedPartContent = FileTools.readFile(dir, "1-left.xml");
        String fromServerContent = FileTools.readFile(dir, "1-right.xml");
        String startPath = "/document/*[local-name()='relations-common-list']";
        TreeWalkResults.MatchSpec matchSpec = TreeWalkResults.MatchSpec.createDefault();
        TreeWalkResults results =
            XmlCompareJdom.compareParts(expectedPartContent,
                                        "expected",
                                        fromServerContent,
                                        "from-server",
                                        startPath,
                                        matchSpec);
        XmlCompareJdomTest.assertTreeWalkResults(results,1,0,0,false, matchSpec);
                                   // addedRight,missingRight,textMismatches,strictMatch,treesMatch
    }

    @Test
    public void testLeftAndRightSameNoStartElement(){
        testBanner("testLeftAndRightSameNoStartElement");
        String dir = getDirectory();
         String expectedPartContent = FileTools.readFile(dir, "2-left.xml");
        String fromServerContent = FileTools.readFile(dir, "2-right.xml");
        String startPath = "/document";
        TreeWalkResults.MatchSpec matchSpec = TreeWalkResults.MatchSpec.createDefault();
        TreeWalkResults results =
            XmlCompareJdom.compareParts(expectedPartContent,
                                        "expected",
                                        fromServerContent,
                                        "from-server",
                                        startPath,
                                        matchSpec);
        XmlCompareJdomTest.assertTreeWalkResults(results,0,0,0,true,matchSpec);
                                   // addedRight,missingRight,textMismatches,strictMatch,treesMatch
    }


}
