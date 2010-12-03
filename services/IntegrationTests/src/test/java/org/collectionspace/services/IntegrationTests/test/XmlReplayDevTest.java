package org.collectionspace.services.IntegrationTests.test;

import org.collectionspace.services.IntegrationTests.xmlreplay.ServiceResult;
import org.collectionspace.services.IntegrationTests.xmlreplay.Tools;
import org.collectionspace.services.IntegrationTests.xmlreplay.XmlReplay;
import org.collectionspace.services.IntegrationTests.xmlreplay.XmlReplayTest;
import org.testng.annotations.Test;

import java.util.List;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class XmlReplayDevTest extends XmlReplayTest {

    @Test
    public void runMaster() throws Exception {
        XmlReplay replay = createXmlReplayUsingIntegrationTestsModule("..");
        List<List<ServiceResult>> list = replay.runMaster(XmlReplay.DEFAULT_DEV_MASTER_CONTROL);
        logTestForGroup(list, "XmlReplayMasterTest");

        /*
        Maven surefire doesn't let you pass stuff on the command line
        unless you define it in command args in the pom.xml file.
        So this doesn't work, because -D defines don't get passed through
        when maven execs surefire.

        String masterFile = System.getProperty("xmlReplayMaster");
        if (Tools.notEmpty(masterFile)){
            System.out.println("Using masterFile specified in System property: "+masterFile);
            XmlReplay replay = createXmlReplayUsingIntegrationTestsModule("..");
            List<List<ServiceResult>> list = replay.runMaster(masterFile);
            logTestForGroup(list, "XmlReplayMasterTest");
        } */
    }
}
