package org.collectionspace.services.IntegrationTests.test;

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
public class XmlReplayMasterTest  extends XmlReplayTest {

    @Test
    public void runMaster() throws Exception {
        XmlReplay replay = createXmlReplayUsingIntegrationTestsModule("..");
        List<List<ServiceResult>> list = replay.runMaster(XmlReplay.DEFAULT_MASTER_CONTROL);
        logTestForGroup(list, "XmlReplayMasterTest");
    }
}
