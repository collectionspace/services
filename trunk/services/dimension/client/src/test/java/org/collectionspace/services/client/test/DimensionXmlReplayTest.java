package org.collectionspace.services.client.test;

import org.collectionspace.services.IntegrationTests.xmlreplay.ServiceResult;
import org.collectionspace.services.IntegrationTests.xmlreplay.XmlReplay;
import org.collectionspace.services.IntegrationTests.xmlreplay.XmlReplayTest;
import org.testng.annotations.Test;

import java.util.List;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class DimensionXmlReplayTest extends XmlReplayTest { 

    //@Test
    public void runMaster() throws Exception {
        XmlReplay replay = createXmlReplayUsingIntegrationTestsModule("../../");
        List<List<ServiceResult>> list = replay.runMaster("dimension-master.xml");
        logTestForGroup(list, "runMaster");
    }

    //@Test
    public void runOneTest() throws Exception {
        XmlReplay replay = createXmlReplayForModule();
        replay.readOptionsFromMasterConfigFile("dimension-master.xml");
        replay.setControlFileName("dimension.xml");

        ServiceResult res = replay.runTest("dimensionTestGroup", "dimension1");
        logTest(res, "runOneTest");
    }

}
