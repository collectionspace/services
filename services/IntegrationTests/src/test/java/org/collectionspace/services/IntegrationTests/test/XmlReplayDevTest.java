package org.collectionspace.services.IntegrationTests.test;

import org.collectionspace.services.IntegrationTests.xmlreplay.ServiceResult;
import org.collectionspace.services.IntegrationTests.xmlreplay.XmlReplay;
import org.collectionspace.services.IntegrationTests.xmlreplay.XmlReplayTest;
import org.collectionspace.services.common.api.Tools;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
        Maven surefire doesn't let you pass stuff on the command line
        unless you define -DforkMode=never inn the command-line args.
        So be sure to use a command-line like:
           mvn -e test -DxmlReplayMaster=dev-master.xml -DforkMode=never -Dtest=XmlReplayDevTest
 *
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class XmlReplayDevTest extends XmlReplayTest {

    @Test
    public void runMaster() throws Exception {
        String masterFile = System.getProperty("xmlReplayMaster");
        if (Tools.notEmpty(masterFile)){
            System.out.println("Using masterFile specified in System property: "+masterFile);
        } else {
            masterFile = XmlReplay.DEFAULT_DEV_MASTER_CONTROL;
            System.out.println("Using default masterFile: "+masterFile);
        }
        XmlReplay replay = createXmlReplayUsingIntegrationTestsModule("..");
        if (replay.masterConfigFileExists(masterFile)){           // CSPACE-4027
             List<List<ServiceResult>> list = replay.runMaster(masterFile);
             logTestForGroup(list, "XmlReplayMasterTest");
        } else {
            System.out.println("XmlReplayDevTest skipping local dev test "
            		+ masterFile+" because it doesn't exist in "
            		+ replay.getBaseDir()
            		+ ".  This is expected behavior on a production installation.");
        }
    }

    private void bigLoop(String masterFile){
        int MAXWORKERS = 20;
        long start = System.currentTimeMillis();
        for (int t=0; t<MAXWORKERS; t++){
            Worker w = new Worker(masterFile, "Worker"+t, this);
            synchronized (workers){
                workers.add(w);
            }
            w.start();
        }
        while (!quitNow) {
            Thread.yield();
        }
        System.out.println("DONE. workers: " + MAXWORKERS+" time: " + (System.currentTimeMillis()-start));
        for (Long l: stats){
            System.out.println(""+l);
        }
    }

    private List<Worker> workers = new ArrayList<Worker>();
    private List<Long> stats = new ArrayList<Long>();
    private volatile boolean quitNow = false;
    protected void finished(Worker worker, Long time){
        synchronized(workers){
            workers.remove(worker);
            stats.add(time);
            if (workers.size()==0){
                quitNow = true;
            }
            System.out.println("Workers left: "+workers.size());
            workers.notifyAll();
        }

    }

    public static class Worker extends Thread{
        public Worker(String masterFile, String ID, XmlReplayDevTest dtest){
            super(ID);
            this.masterFile = masterFile;
            this.dtest = dtest;
        }
        private String masterFile;
        private XmlReplayDevTest dtest;

        public void run(){
            try {
                long start = System.currentTimeMillis();
                System.out.println("RUNNING Master in Worker: "+this.getName());
                XmlReplay replay = createXmlReplayUsingIntegrationTestsModule("..");
                List<List<ServiceResult>> list = replay.runMaster(masterFile);
                logTestForGroup(list, "XmlReplayMasterTest");
                long stop = System.currentTimeMillis();
                dtest.finished(this, stop-start);
            } catch (Exception e){
                System.out.println("ERROR in Worker: "+e);
                return;
            }

        }
    }
}
