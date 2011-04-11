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

import org.collectionspace.services.common.api.Tools;
import org.testng.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Subclass this test to programmatically control XmlReplay from a surefire test.  See example in IntegrationTests :: XmlReplaySelfTest
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class XmlReplayTest {

    public static final String XMLREPLAY_REL_DIR_TO_MODULE = "/src/test/resources/test-data/xmlreplay";

    /** To use this method, you should have a test repository of xml files in the path
     *  defined by XMLREPLAY_REL_DIR_TO_MODULE, relative to your pom.xml file, but normally
     *  you would use the central repository of tests, which live in services/IntegrationTests,
     *  and which you can use by calling createXmlReplay() which calls createXmlReplayUsingIntegrationTestsModule() for you.
     */
    public static XmlReplay createXmlReplayForModule() throws Exception {
        String pwd = (new File(".")).getCanonicalPath();
        System.out.println("createXmlReplayForModule.pwd: "+pwd);
        XmlReplay replay = new XmlReplay(pwd+XMLREPLAY_REL_DIR_TO_MODULE);
        System.out.println("XmlReplay: "+replay);
        return replay;
    }

    /** Use this method if your test xml files are stored in the central repository,
     *   which is "services/IntegrationTests" + XMLREPLAY_REL_DIR_TO_MODULE
     */
    public static XmlReplay createXmlReplay() throws Exception {
        return createXmlReplayUsingIntegrationTestsModule("../..");
    }

    /**
     * @param relToServicesRoot is a Unix-like path from the calling module to the services root,
     *        so if  if you are in services/dimension/client/
     *        then relToServicesRoot is "../.." which is how most of the client tests are set up, or if you
     *        are setting up your test repository relative to the main service, e.g. you are in
     *        services/dimension/, then relToServicesRoot is ".."
     */
    public static XmlReplay createXmlReplayUsingIntegrationTestsModule(String relToServicesRoot) throws Exception {
        String thisDir = Tools.glue(relToServicesRoot, "/", "IntegrationTests");
        String pwd = (new File(thisDir)).getCanonicalPath();
        System.out.println("createXmlReplayUsingIntegrationTestsModule.pwd: "+pwd);
        XmlReplay replay = new XmlReplay(pwd+XMLREPLAY_REL_DIR_TO_MODULE);
        System.out.println("XmlReplay: "+replay);
        return replay;
    }

    public static void logTest(ServiceResult sresult, String testname){
        ResultSummary summary = resultSummary(sresult);
        org.testng.Reporter.log(summary.table);
        Assert.assertEquals(summary.oks, summary.total, "Expected all "+summary.total+ " XmlReplay tests to pass.  See Output from test '"+testname+"'.");
    }

    public static void logTest(List<ServiceResult> list, String testname){
        ResultSummary summary = resultSummary(list);
        org.testng.Reporter.log(summary.table);
        Assert.assertEquals(summary.oks, summary.total, "Expected all "+summary.total+ " XmlReplay tests to pass.  See Output from test '"+testname+"'.");
    }

    public static void logTestForGroup(List<List<ServiceResult>> list, String testname){
        ResultSummary summary = resultSummaryForGroup(list);
        org.testng.Reporter.log(summary.table);
        Assert.assertEquals(summary.oks, summary.total, "Expected all "+summary.total+ " XmlReplay tests to pass.  See Output from test '"+testname+"'.");
    }


    //============== HELPERS AND FORMATTING =====================================================

    private static final String TBLSTART = "<table border='1'>";
    private static final String ROWSTART = "<tr><td bgcolor='white'>";
    private static final String ROWSTARTRED = "<tr><td bgcolor='red'><b>";
    private static final String SEP = "</td><td>";
    private static final String ROWEND = "</td></tr>";
    private static final String ROWENDRED = "</b></td></tr>";
    private static final String TBLEND = "</table>";

    public static class ResultSummary {
        public long oks = 0;
        public long total = 0;
        public String table = "";
        public List<String> groups = new ArrayList<String>();
    }

    public static ResultSummary resultSummaryForGroup(List<List<ServiceResult>> list){
        ResultSummary summary = new ResultSummary();
        summary.oks = 0;
        summary.total = 0;
        StringBuffer buff = new StringBuffer();
        buff.append(TBLSTART);
        for (List<ServiceResult> serviceResults : list){
            String groupID = "";
            if (serviceResults.size()>0){
                groupID = serviceResults.get(0).testGroupID;
                summary.groups.add(groupID);
            }
            buff.append(ROWSTART+"XmlReplay testGroup "+groupID+ROWEND);
            for (ServiceResult serviceResult : serviceResults){
                summary.total++;
                if (serviceResult.gotExpectedResult()){
                    summary.oks++;
                    buff.append(ROWSTART+serviceResult.minimal()+ROWEND);
                } else {
                    buff.append(ROWSTARTRED+serviceResult.minimal()+ROWENDRED);
                }
            }
        }
        buff.append(TBLEND);
        summary.table = buff.toString();
        return summary;
    }

    public static ResultSummary resultSummary(List<ServiceResult> serviceResults){
        ResultSummary summary = new ResultSummary();
        summary.oks = 0;
        summary.total = 0;
        StringBuffer buff = new StringBuffer();
        buff.append(TBLSTART);
        for (ServiceResult serviceResult : serviceResults){
            summary.total++;
            if (serviceResult.gotExpectedResult()){
                summary.oks++;
                buff.append(ROWSTART+serviceResult.minimal()+ROWEND);
            } else {
                buff.append(ROWSTARTRED+serviceResult.minimal()+ROWENDRED);
            }
        }
        buff.append(TBLEND);
        summary.table = buff.toString();
        return summary;
    }

    public static ResultSummary resultSummary(ServiceResult serviceResult){
        ResultSummary summary = new ResultSummary();
        summary.oks = 0;
        summary.total = 1;
        StringBuffer buff = new StringBuffer();
        buff.append(TBLSTART);
        if (serviceResult.gotExpectedResult()){
            summary.oks = 1;
            buff.append(ROWSTART+serviceResult.minimal()+ROWEND);
        } else {
            buff.append(ROWSTARTRED+serviceResult.minimal()+ROWENDRED);
        }
        buff.append(TBLEND);
        summary.table = buff.toString();
        return summary;
    }

}
