package org.collectionspace.services.IntegrationTests.xmlreplay;

import org.apache.commons.cli.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.jexl2.JexlEngine;
import org.collectionspace.services.common.api.FileTools;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.*;

/**  This class is used to replay a request to the Services layer, by sending the XML payload
 *   in an appropriate Multipart request.
 *   See example usage in calling class XmlReplayTest in services/IntegrationTests, and also in main() in this class.
 *   @author Laramie Crocker
 */
public class XmlReplay {

    public XmlReplay(String basedir, String reportsDir){
        this.basedir = basedir;
        this.serviceResultsMap = createResultsMap();
        this.reportsList = new ArrayList<String>();
        this.reportsDir = reportsDir;
    }

    public static final String DEFAULT_CONTROL = "xml-replay-control.xml";
    public static final String DEFAULT_MASTER_CONTROL = "xml-replay-master.xml";
    public static final String DEFAULT_DEV_MASTER_CONTROL = "dev-master.xml";

    private String reportsDir = "";
    public String getReportsDir(){
        return reportsDir;
    }
    private String basedir = ".";  //set from constructor.
    public String getBaseDir(){
        return basedir;
    }
    
    private String controlFileName = DEFAULT_CONTROL;
    public String getControlFileName() {
        return controlFileName;
    }
    public void setControlFileName(String controlFileName) {
        this.controlFileName = controlFileName;
    }

    private String protoHostPort = "";
    public String getProtoHostPort() {
        return protoHostPort;
    }
    public void setProtoHostPort(String protoHostPort) {
        this.protoHostPort = protoHostPort;
    }

    private boolean autoDeletePOSTS = true;
    public boolean isAutoDeletePOSTS() {
        return autoDeletePOSTS;
    }
    public void setAutoDeletePOSTS(boolean autoDeletePOSTS) {
        this.autoDeletePOSTS = autoDeletePOSTS;
    }

    private Dump dump;
    public Dump getDump() {
        return dump;
    }
    public void setDump(Dump dump) {
        this.dump = dump;
    }

    AuthsMap defaultAuthsMap;
    public AuthsMap getDefaultAuthsMap(){
        return defaultAuthsMap;
    }
    public void setDefaultAuthsMap(AuthsMap authsMap){
        defaultAuthsMap = authsMap;
    }

    private Map<String, ServiceResult> serviceResultsMap;
    public Map<String, ServiceResult> getServiceResultsMap(){
        return serviceResultsMap;
    }
    public static Map<String, ServiceResult> createResultsMap(){
        return new HashMap<String, ServiceResult>();
    }

    private List<String> reportsList;
    public  List<String> getReportsList(){
        return reportsList;
    }

    public String toString(){
        return "XmlReplay{"+this.basedir+", "+this.defaultAuthsMap+", "+this.dump+", "+this.reportsDir+'}';
    }

    // ============== METHODS ===========================================================

    /** Optional information method: call this method after instantiating this class using the constructor XmlReplay(String), which sets the basedir.  Then you
     *   pass in your relative masterFilename to that basedir to this method, which will return true if the file is readable, valid xml, etc.
     *   Do this in preference to  just seeing if File.exists(), because there are rules to finding the file relative to the maven test dir, yada, yada.
     *   This method makes it easy to have a development test file that you don't check in, so that dev tests can be missing gracefully, etc.
     */
    public boolean masterConfigFileExists(String masterFilename){
        try {
            org.dom4j.Document doc = openMasterConfigFile(masterFilename);
            if (doc == null){
                return false;
            }
            return true;
        } catch (Throwable t){
            return false;
        }
    }

    public org.dom4j.Document openMasterConfigFile(String masterFilename) throws FileNotFoundException {
        String fullPath = Tools.glue(basedir, "/", masterFilename);
        File f = new File(fullPath);
        if (!f.exists()){
            return null;
        }
        org.dom4j.Document document = getDocument(fullPath); //will check full path first, then checks relative to PWD.
        if (document == null){
            throw new FileNotFoundException("XmlReplay master control file ("+masterFilename+") not found in basedir: "+basedir+". Exiting test.");
        }
        return document;
    }

    /** specify the master config file, relative to getBaseDir(), but ignore any tests or testGroups in the master.
     *  @return a Document object, which you don't need to use: all options will be stored in XmlReplay instance.
     */
    public org.dom4j.Document readOptionsFromMasterConfigFile(String masterFilename) throws FileNotFoundException {
        org.dom4j.Document document = openMasterConfigFile(masterFilename);
        if (document == null){
            throw new FileNotFoundException(masterFilename);
        }
        protoHostPort = document.selectSingleNode("/xmlReplayMaster/protoHostPort").getText().trim();
        AuthsMap authsMap = readAuths(document);
        setDefaultAuthsMap(authsMap);
        Dump dump = XmlReplay.readDumpOptions(document);
        setDump(dump);
        return document;
    }

    public List<List<ServiceResult>> runMaster(String masterFilename) throws Exception {
        return runMaster(masterFilename, true);
    }

    /** Creates new instances of XmlReplay, one for each controlFile specified in the master,
     *  and setting defaults from this instance, but not sharing ServiceResult objects or maps. */
    public List<List<ServiceResult>> runMaster(String masterFilename, boolean readOptionsFromMaster) throws Exception {
        List<List<ServiceResult>> list = new ArrayList<List<ServiceResult>>();
        org.dom4j.Document document;
        if (readOptionsFromMaster){
            document = readOptionsFromMasterConfigFile(masterFilename);
        } else {
            document = openMasterConfigFile(masterFilename);
        }
        if (document==null){
            throw new FileNotFoundException(masterFilename);
        }
        String controlFile, testGroup, test;
        List<Node> runNodes;
        runNodes = document.selectNodes("/xmlReplayMaster/run");
        for (Node runNode : runNodes) {
            controlFile = runNode.valueOf("@controlFile");
            testGroup = runNode.valueOf("@testGroup");
            test = runNode.valueOf("@test"); //may be empty

            //Create a new instance and clone only config values, not any results maps.
            XmlReplay replay = new XmlReplay(basedir, this.reportsDir);
            replay.setControlFileName(controlFile);
            replay.setProtoHostPort(protoHostPort);
            replay.setAutoDeletePOSTS(isAutoDeletePOSTS());
            replay.setDump(dump);
            replay.setDefaultAuthsMap(getDefaultAuthsMap());

            //Now run *that* instance.
            List<ServiceResult> results = replay.runTests(testGroup, test);
            list.add(results);
            this.reportsList.addAll(replay.getReportsList());   //Add all the reports from the inner replay, to our master replay's reportsList, to generate the index.html file.
        }
        XmlReplayReport.saveIndexForMaster(basedir, reportsDir, masterFilename, this.reportsList);
        return list;
    }

    /** Use this if you wish to run named tests within a testGroup, otherwise call runTestGroup(). */
    public List<ServiceResult>  runTests(String testGroupID, String testID) throws Exception {
        List<ServiceResult> result = runXmlReplayFile(this.basedir,
                                this.controlFileName,
                                testGroupID,
                                testID,
                                this.serviceResultsMap,
                                this.autoDeletePOSTS,
                                dump,
                                this.protoHostPort,
                                this.defaultAuthsMap,
                                this.reportsList,
                                this.reportsDir);
        return result;
    }

    /** Use this if you wish to specify just ONE test to run within a testGroup, otherwise call runTestGroup(). */
    public ServiceResult  runTest(String testGroupID, String testID) throws Exception {
        List<ServiceResult> result = runXmlReplayFile(this.basedir,
                                this.controlFileName,
                                testGroupID,
                                testID,
                                this.serviceResultsMap,
                                this.autoDeletePOSTS,
                                dump,
                                this.protoHostPort,
                                this.defaultAuthsMap,
                                this.reportsList,
                                this.reportsDir);
        if (result.size()>1){
            throw new IndexOutOfBoundsException("Multiple ("+result.size()+") tests with ID='"+testID+"' were found within test group '"+testGroupID+"', but there should only be one test per ID attribute.");
        }
        return result.get(0);
    }

    /** Use this if you wish to run all tests within a testGroup.*/
    public List<ServiceResult> runTestGroup(String testGroupID) throws Exception {
        //NOTE: calling runTest with empty testID runs all tests in a test group, but don't expose this fact.
        // Expose this method (runTestGroup) instead.
        return runTests(testGroupID, "");
    }

    public List<ServiceResult>  autoDelete(String logName){
        return autoDelete(this.serviceResultsMap, logName);
    }

    /** Use this method to clean up resources created on the server that returned CSIDs, if you have
     *  specified autoDeletePOSTS==false, which means you are managing the cleanup yourself.
     * @param serviceResultsMap a Map of ServiceResult objects, which will contain ServiceResult.deleteURL.
     * @return a List<String> of debug info about which URLs could not be deleted.
     */
    public static List<ServiceResult> autoDelete(Map<String, ServiceResult> serviceResultsMap, String logName){
        List<ServiceResult> results = new ArrayList<ServiceResult>();
        for (ServiceResult pr : serviceResultsMap.values()){
            try {
                if (Tools.notEmpty(pr.deleteURL)){
                    ServiceResult deleteResult = XmlReplayTransport.doDELETE(pr.deleteURL, pr.auth, pr.testID, "[autodelete:"+logName+"]");
                    results.add(deleteResult);
                } else {
                    ServiceResult errorResult = new ServiceResult();
                    errorResult.fullURL = pr.fullURL;
                    errorResult.testGroupID = pr.testGroupID;
                    errorResult.fromTestID = pr.fromTestID;
                    errorResult.overrideGotExpectedResult();
                    results.add(errorResult);
                }
            } catch (Throwable t){
                String s = (pr!=null) ? "ERROR while cleaning up ServiceResult map: "+pr+" for "+pr.deleteURL+" :: "+t
                                      : "ERROR while cleaning up ServiceResult map (null ServiceResult): "+t;
                System.err.println(s);
                ServiceResult errorResult = new ServiceResult();
                errorResult.fullURL = pr.fullURL;
                errorResult.testGroupID = pr.testGroupID;
                errorResult.fromTestID = pr.fromTestID;
                errorResult.error = s;
                results.add(errorResult);
            }
        }
        return results;
    }

    public static class AuthsMap {
        Map<String,String> map;
        String defaultID="";
        public String getDefaultAuth(){
            return map.get(defaultID);
        }
        public String toString(){
            return "AuthsMap: {default='"+defaultID+"'; "+map.keySet()+'}';
        }
    }

    public static AuthsMap readAuths(org.dom4j.Document document){
    Map<String, String> map = new HashMap<String, String>();
        List<Node> authNodes = document.selectNodes("//auths/auth");
        for (Node auth : authNodes) {
            map.put(auth.valueOf("@ID"), auth.getStringValue());
        }
        AuthsMap authsMap = new AuthsMap();
        Node auths = document.selectSingleNode("//auths");
        String defaultID = "";
        if (auths != null){
            defaultID = auths.valueOf("@default");
        }
        authsMap.map = map;
        authsMap.defaultID = defaultID;
        return authsMap;
    }

    public static class Dump {
        public boolean payloads = false;
        //public static final ServiceResult.DUMP_OPTIONS dumpServiceResultOptions = ServiceResult.DUMP_OPTIONS;
        public ServiceResult.DUMP_OPTIONS dumpServiceResult = ServiceResult.DUMP_OPTIONS.minimal;
        public String toString(){
            return "payloads: "+payloads+" dumpServiceResult: "+dumpServiceResult;
        }
    }

    public static Dump getDumpConfig(){
        return new Dump();
    }

    public static Dump readDumpOptions(org.dom4j.Document document){
        Dump dump = getDumpConfig();
        Node dumpNode = document.selectSingleNode("//dump");
        if (dumpNode != null){
            dump.payloads = Tools.isTrue(dumpNode.valueOf("@payloads"));
            String dumpServiceResultStr = dumpNode.valueOf("@dumpServiceResult");
            if (Tools.notEmpty(dumpServiceResultStr)){
                dump.dumpServiceResult = ServiceResult.DUMP_OPTIONS.valueOf(dumpServiceResultStr);
            }
        }
        return dump;
    }

    private static class PartsStruct {
        public List<Map<String,String>> varsList = new ArrayList<Map<String,String>>();
        String responseFilename = "";
        String overrideTestID = "";
        String startElement = "";
        String label = "";

        public static PartsStruct readParts(Node testNode, final String testID, String xmlReplayBaseDir){
            PartsStruct resultPartsStruct = new PartsStruct();
            resultPartsStruct.responseFilename = testNode.valueOf("filename");
            resultPartsStruct.startElement = testNode.valueOf("startElement");
            resultPartsStruct.label = testNode.valueOf("label");
            String responseFilename = testNode.valueOf("filename");
            if (Tools.notEmpty(responseFilename)){
                resultPartsStruct.responseFilename = xmlReplayBaseDir + '/' + responseFilename;
                List<Node> varNodes = testNode.selectNodes("vars/var");
                readVars(testNode, varNodes, resultPartsStruct);
            }
            return resultPartsStruct;
        }

        private static void readVars(Node testNode, List<Node> varNodes, PartsStruct resultPartsStruct){
            Map<String,String> vars = new HashMap<String,String>();
            resultPartsStruct.varsList.add(vars);
            //System.out.println("### vars: "+vars.size()+" ########");
            for (Node var: varNodes){
                String ID = var.valueOf("@ID");
                String value = var.getText();
                //System.out.println("ID: "+ID+" value: "+value);
                vars.put(ID, value); //vars is already part of resultPartsStruct.varsList
            }
            //System.out.println("### end-vars ########");
        }
    }



    private static String fixupFullURL(String fullURL, String protoHostPort, String uri){
        if ( ! uri.startsWith(protoHostPort)){
            fullURL = Tools.glue(protoHostPort, "/", uri);
        } else {
            fullURL = uri;
        }
        return fullURL;
    }

    private static String fromTestID(String fullURL, Node testNode, Map<String, ServiceResult> serviceResultsMap){
        String fromTestID = testNode.valueOf("fromTestID");
        if (Tools.notEmpty(fromTestID)){
            ServiceResult getPR = serviceResultsMap.get(fromTestID);
            if (getPR != null){
                fullURL = Tools.glue(fullURL, "/", getPR.location);
            }
        }
        return fullURL;
    }

    private static String CSIDfromTestID(Node testNode, Map<String, ServiceResult> serviceResultsMap){
        String result = "";
        String fromTestID = testNode.valueOf("fromTestID");
        if (Tools.notEmpty(fromTestID)){
            ServiceResult getPR = serviceResultsMap.get(fromTestID);
            if (getPR != null){
                result = getPR.location;
            }
        }
        return result;
    }

    public static org.dom4j.Document getDocument(String xmlFileName) {
        org.dom4j.Document document = null;
        SAXReader reader = new SAXReader();
        try {
            document = reader.read(xmlFileName);
        } catch (DocumentException e) {
            System.out.println("ERROR reading document: "+e);
            //e.printStackTrace();
        }
        return document;
    }

    protected static String validateResponseSinglePayload(ServiceResult serviceResult,
                                                 Map<String, ServiceResult> serviceResultsMap,
                                                 PartsStruct expectedResponseParts,
                                                 XmlReplayEval evalStruct)
    throws Exception {
        String OK = "";
        byte[] b = FileUtils.readFileToByteArray(new File(expectedResponseParts.responseFilename));
        String expectedPartContent = new String(b);
        Map<String,String> vars = expectedResponseParts.varsList.get(0);  //just one part, so just one varsList.  Must be there, even if empty.
        expectedPartContent = evalStruct.eval(expectedPartContent, serviceResultsMap, vars, evalStruct.jexl, evalStruct.jc);
        serviceResult.expectedContentExpanded = expectedPartContent;
        String label = "NOLABEL";
        String leftID  = "{from expected part, label:"+label+" filename: "+expectedResponseParts.responseFilename+"}";
        String rightID = "{from server, label:"+label
                            +" fromTestID: "+serviceResult.fromTestID
                            +" URL: "+serviceResult.fullURL
                            +"}";
        String startElement = expectedResponseParts.startElement;
        String partLabel = expectedResponseParts.label;
        if (Tools.isBlank(startElement)){
            if (Tools.notBlank(partLabel))
            startElement = "/document/*[local-name()='"+partLabel+"']";
        }
        TreeWalkResults.MatchSpec matchSpec = TreeWalkResults.MatchSpec.createDefault();
        TreeWalkResults list =
            XmlCompareJdom.compareParts(expectedPartContent,
                                        leftID,
                                        serviceResult.result,
                                        rightID,
                                        startElement,
                                        matchSpec);
        serviceResult.addPartSummary(label, list);
        return OK;
    }

    protected static String validateResponse(ServiceResult serviceResult,
                                             Map<String, ServiceResult> serviceResultsMap,
                                             PartsStruct expectedResponseParts,
                                             XmlReplayEval evalStruct){
        String OK = "";
        if (expectedResponseParts == null) return OK;
        if (serviceResult == null) return OK;
        if (serviceResult.result.length() == 0) return OK;
        try {
            return validateResponseSinglePayload(serviceResult, serviceResultsMap, expectedResponseParts, evalStruct);
        } catch (Exception e){
            String err = "ERROR in XmlReplay.validateResponse() : "+e;
            return err  ;
        }
    }

    //================= runXmlReplayFile ======================================================

    public static List<ServiceResult> runXmlReplayFile(String xmlReplayBaseDir,
                                          String controlFileName,
                                          String testGroupID,
                                          String oneTestID,
                                          Map<String, ServiceResult> serviceResultsMap,
                                          boolean param_autoDeletePOSTS,
                                          Dump dump,
                                          String protoHostPortParam,
                                          AuthsMap defaultAuths,
                                          List<String> reportsList,
                                          String reportsDir)
                                          throws Exception {
        //Internally, we maintain two collections of ServiceResult:
        //  the first is the return value of this method.
        //  the second is the serviceResultsMap, which is used for keeping track of CSIDs created by POSTs, for later reference by DELETE, etc.
        List<ServiceResult> results = new ArrayList<ServiceResult>();

        XmlReplayReport report = new XmlReplayReport(reportsDir);

        String controlFile = Tools.glue(xmlReplayBaseDir, "/", controlFileName);
        org.dom4j.Document document;
        document = getDocument(controlFile); //will check full path first, then checks relative to PWD.
        if (document==null){
            throw new FileNotFoundException("XmlReplay control file ("+controlFileName+") not found in basedir: "+xmlReplayBaseDir+" Exiting test.");
        }
        String protoHostPort;
        if (Tools.isEmpty(protoHostPortParam)){
            protoHostPort = document.selectSingleNode("/xmlReplay/protoHostPort").getText().trim();
            System.out.println("DEPRECATED: Using protoHostPort ('"+protoHostPort+"') from xmlReplay file ('"+controlFile+"'), not master.");
        } else {
            protoHostPort = protoHostPortParam;
        }
        if (Tools.isEmpty(protoHostPort)){
            throw new Exception("XmlReplay control file must have a protoHostPort element");
        }

        String authsMapINFO;
        AuthsMap authsMap = readAuths(document);
        if (authsMap.map.size()==0){
            authsMap = defaultAuths;
            authsMapINFO = "Using defaultAuths from master file: "+defaultAuths;
        } else {
            authsMapINFO = "Using AuthsMap from control file: "+authsMap;
        }

        report.addTestGroup(testGroupID, controlFileName);   //controlFileName is just the short name, without the full path.
        String xmlReplayHeader = "========================================================================"
                          +"\r\nXmlReplay running:"
                          +"\r\n   controlFile: "+ (new File(controlFile).getCanonicalPath())
                          +"\r\n   protoHostPort: "+protoHostPort
                          +"\r\n   testGroup: "+testGroupID
                          + (Tools.notEmpty(oneTestID) ? "\r\n   oneTestID: "+oneTestID : "")
                          +"\r\n   AuthsMap: "+authsMapINFO
                          +"\r\n   param_autoDeletePOSTS: "+param_autoDeletePOSTS
                          +"\r\n   Dump info: "+dump
                          +"\r\n========================================================================"
                          +"\r\n";
        report.addRunInfo(xmlReplayHeader);

        System.out.println(xmlReplayHeader);

        String autoDeletePOSTS = "";
        List<Node> testgroupNodes;
        if (Tools.notEmpty(testGroupID)){
            testgroupNodes = document.selectNodes("//testGroup[@ID='"+testGroupID+"']");
        } else {
            testgroupNodes = document.selectNodes("//testGroup");
        }

        JexlEngine jexl = new JexlEngine();   // Used for expression language expansion from uri field.
        XmlReplayEval evalStruct = new XmlReplayEval();
        evalStruct.serviceResultsMap = serviceResultsMap;
        evalStruct.jexl = jexl;

        for (Node testgroup : testgroupNodes) {

            XmlReplayEval.MapContextWKeys jc = new XmlReplayEval.MapContextWKeys();//MapContext();  //Get a new JexlContext for each test group.
            evalStruct.jc = jc;

            autoDeletePOSTS = testgroup.valueOf("@autoDeletePOSTS");
            List<Node> tests;
            if (Tools.notEmpty(oneTestID)){
                tests = testgroup.selectNodes("test[@ID='"+oneTestID+"']");
            } else {
                tests = testgroup.selectNodes("test");
            }
            String authForTest = "";
            int testElementIndex = -1;

            for (Node testNode : tests) {
                long startTime = System.currentTimeMillis();
                try {
                    testElementIndex++;
                    String testID = testNode.valueOf("@ID");
                    String testIDLabel = Tools.notEmpty(testID) ? (testGroupID+'.'+testID) : (testGroupID+'.'+testElementIndex);
                    String method = testNode.valueOf("method");
                    String uri = testNode.valueOf("uri");
                    String fullURL = Tools.glue(protoHostPort, "/", uri);

                    String authIDForTest = testNode.valueOf("@auth");
                    String currentAuthForTest = authsMap.map.get(authIDForTest);
                    if (Tools.notEmpty(currentAuthForTest)){
                        authForTest = currentAuthForTest; //else just run with current from last loop;
                    }
                    if (Tools.isEmpty(authForTest)){
                        authForTest = defaultAuths.getDefaultAuth();
                    }

                    if (uri.indexOf("$")>-1){
                        uri = evalStruct.eval(uri, serviceResultsMap, null, jexl, jc);
                    }
                    fullURL = fixupFullURL(fullURL, protoHostPort, uri);

                    List<Integer> expectedCodes = new ArrayList<Integer>();
                    String expectedCodesStr = testNode.valueOf("expectedCodes");
                    if (Tools.notEmpty(expectedCodesStr)){
                         String[] codesArray = expectedCodesStr.split(",");
                         for (String code : codesArray){
                             expectedCodes.add(new Integer(code.trim()));
                         }
                    }

                    Node responseNode = testNode.selectSingleNode("response");
                    PartsStruct expectedResponseParts = null;
                    if (responseNode!=null){
                        expectedResponseParts = PartsStruct.readParts(responseNode, testID, xmlReplayBaseDir);
                        //System.out.println("reponse parts: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+expectedResponseParts);
                    }

                    ServiceResult serviceResult;
                    boolean isPOST = method.equalsIgnoreCase("POST");
                    boolean isPUT =  method.equalsIgnoreCase("PUT");
                    if ( isPOST || isPUT ) {
                        PartsStruct parts = PartsStruct.readParts(testNode, testID, xmlReplayBaseDir);
                        if (Tools.notEmpty(parts.overrideTestID)) {
                            testID = parts.overrideTestID;
                        }
                        if (isPOST){
                            String csid = CSIDfromTestID(testNode, serviceResultsMap);
                            if (Tools.notEmpty(csid)) uri = Tools.glue(uri, "/", csid+"/items/");
                        } else if (isPUT) {
                            uri = fromTestID(uri, testNode, serviceResultsMap);
                        }
                        //vars only make sense in two contexts: POST/PUT, because you are submitting another file with internal expressions,
                        // and in <response> nodes. For GET, DELETE, there is no payload, so all the URLs with potential expressions are right there in the testNode.
                        Map<String,String> vars = null;
                        if (parts.varsList.size()>0){
                            vars = parts.varsList.get(0);
                        }
                        serviceResult = XmlReplayTransport.doPOST_PUTFromXML(parts.responseFilename, vars, protoHostPort, uri, method, XmlReplayTransport.APPLICATION_XML, evalStruct, authForTest, testIDLabel);
                        if (vars!=null) {
                            serviceResult.addVars(vars);
                        }
                        results.add(serviceResult);
                        //if (isPOST){
                            serviceResultsMap.put(testID, serviceResult);      //PUTs do not return a Location, so don't add PUTs to serviceResultsMap.
                        //}
                        fullURL = fixupFullURL(fullURL, protoHostPort, uri);
                    } else if (method.equalsIgnoreCase("DELETE")){
                        String fromTestID = testNode.valueOf("fromTestID");
                        ServiceResult pr = serviceResultsMap.get(fromTestID);
                        if (pr!=null){
                            serviceResult = XmlReplayTransport.doDELETE(pr.deleteURL, authForTest, testIDLabel, fromTestID);
                            serviceResult.fromTestID = fromTestID;
                            if (expectedCodes.size()>0){
                                serviceResult.expectedCodes = expectedCodes;
                            }
                            results.add(serviceResult);
                            if (serviceResult.codeInSuccessRange(serviceResult.responseCode)){  //gotExpectedResult depends on serviceResult.expectedCodes.
                                serviceResultsMap.remove(fromTestID);
                            }
                        } else {
                            if (Tools.notEmpty(fromTestID)){
                                serviceResult = new ServiceResult();
                                serviceResult.responseCode = 0;
                                serviceResult.error = "ID not found in element fromTestID: "+fromTestID;
                                System.err.println("****\r\nServiceResult: "+serviceResult.error+". SKIPPING TEST. Full URL: "+fullURL);
                            } else {
                                serviceResult = XmlReplayTransport.doDELETE(fullURL, authForTest, testID, fromTestID);
                            }
                            serviceResult.fromTestID = fromTestID;
                            results.add(serviceResult);
                        }
                    } else if (method.equalsIgnoreCase("GET")){
                        fullURL = fromTestID(fullURL, testNode, serviceResultsMap);
                        serviceResult = XmlReplayTransport.doGET(fullURL, authForTest, testIDLabel);
                        results.add(serviceResult);
                        serviceResultsMap.put(testID, serviceResult);
                    } else if (method.equalsIgnoreCase("LIST")){
                        fullURL = fixupFullURL(fullURL, protoHostPort, uri);
                        String listQueryParams = ""; //TODO: empty for now, later may pick up from XML control file.
                        serviceResult = XmlReplayTransport.doLIST(fullURL, listQueryParams, authForTest, testIDLabel);
                        results.add(serviceResult);
                        serviceResultsMap.put(testID, serviceResult);
                    } else {
                        throw new Exception("HTTP method not supported by XmlReplay: "+method);
                    }

                    serviceResult.testID = testID;
                    serviceResult.fullURL = fullURL;
                    serviceResult.auth = authForTest;
                    serviceResult.method = method;
                    if (expectedCodes.size()>0){
                        serviceResult.expectedCodes = expectedCodes;
                    }
                    if (Tools.isEmpty(serviceResult.testID)) serviceResult.testID = testIDLabel;
                    if (Tools.isEmpty(serviceResult.testGroupID)) serviceResult.testGroupID = testGroupID;

                    Node expectedLevel = testNode.selectSingleNode("response/expected");
                    if (expectedLevel!=null){
                        String level = expectedLevel.valueOf("@level");
                        serviceResult.payloadStrictness = level;
                    }
                    //=====================================================
                    //  ALL VALIDATION FOR ALL REQUESTS IS DONE HERE:
                    //=====================================================
                    boolean hasError = false;
                    String vError = validateResponse(serviceResult, serviceResultsMap, expectedResponseParts, evalStruct);
                    if (Tools.notEmpty(vError)){
                        serviceResult.error = vError;
                        serviceResult.failureReason = " : VALIDATION ERROR; ";
                        hasError = true;
                    }
                    if (hasError == false){
                        hasError = ! serviceResult.gotExpectedResult();
                    }

                    boolean doingAuto = (dump.dumpServiceResult == ServiceResult.DUMP_OPTIONS.auto);
                    String serviceResultRow = serviceResult.dump(dump.dumpServiceResult, hasError)+"; time:"+(System.currentTimeMillis()-startTime);
                    String leader = (dump.dumpServiceResult == ServiceResult.DUMP_OPTIONS.detailed) ? "XmlReplay:"+testIDLabel+": ": "";

                    report.addTestResult(serviceResult);

                    if (   (dump.dumpServiceResult == ServiceResult.DUMP_OPTIONS.detailed)
                        || (dump.dumpServiceResult == ServiceResult.DUMP_OPTIONS.full)         ){
                        System.out.println("\r\n#---------------------#");
                    }
                    System.out.println(timeString()+" "+leader+serviceResultRow+"\r\n");
                    if (dump.payloads || (doingAuto&&hasError) ) {
                        if (Tools.notBlank(serviceResult.requestPayload)){
                            System.out.println("\r\n========== request payload ===============");
                            System.out.println(serviceResult.requestPayload);
                            System.out.println("==========================================\r\n");
                        }
                    }
                    if (dump.payloads || (doingAuto&&hasError)) {
                        if (Tools.notBlank(serviceResult.result)){
                            System.out.println("\r\n========== response payload ==============");
                            System.out.println(serviceResult.result);
                            System.out.println("==========================================\r\n");
                        }
                    }
                } catch (Throwable t) {
                    String msg = "ERROR: XmlReplay experienced an error in a test node: "+testNode+" Throwable: "+t;
                    System.out.println(msg);
                    System.out.println(Tools.getStackTrace(t));
                    ServiceResult serviceResult = new ServiceResult();
                    serviceResult.error = msg;
                    serviceResult.failureReason = " : SYSTEM ERROR; ";
                    results.add(serviceResult);
                }
            }
            if (Tools.isTrue(autoDeletePOSTS)&&param_autoDeletePOSTS){
                autoDelete(serviceResultsMap, "default");
            }
        }

        //=== Now spit out the HTML report file ===
        File m = new File(controlFileName);
        String localName = m.getName();//don't instantiate, just use File to extract file name without directory.
        String reportName = localName+'-'+testGroupID+".html";

        File resultFile = report.saveReport(xmlReplayBaseDir, reportsDir, reportName);
        if (resultFile!=null) {
            String toc = report.getTOC(reportName);
            reportsList.add(toc);
        }
        //================================

        return results;
    }

		private static String timeString() {
			java.util.Date date= new java.util.Date();
	 		java.sql.Timestamp ts = new java.sql.Timestamp(date.getTime());
			return ts.toString();
		}
		

    //======================== MAIN ===================================================================

    private static Options createOptions() {
        Options options = new Options();
        options.addOption("xmlReplayBaseDir", true, "default/basedir");
        return options;
    }

    public static String usage(){
        String result = "org.collectionspace.services.IntegrationTests.xmlreplay.XmlReplay {args}\r\n"
                        +"  -xmlReplayBaseDir <dir> \r\n"
                        +" You may also override these with system args, e.g.: \r\n"
                        +"   -DxmlReplayBaseDir=/path/to/dir \r\n"
                        +" These may also be passed in via the POM.\r\n"
                        +" You can also set these system args, e.g.: \r\n"
                        +"  -DtestGroupID=<oneID> \r\n"
                        +"  -DtestID=<one TestGroup ID>"
                        +"  -DautoDeletePOSTS=<true|false> \r\n"
                        +"    (note: -DautoDeletePOSTS won't force deletion if set to false in control file.";
        return result;
    }

    private static String opt(CommandLine line, String option){
        String result;
        String fromProps = System.getProperty(option);
        if (Tools.notEmpty(fromProps)){
            return fromProps;
        }
        if (line==null){
            return "";
        }
        result = line.getOptionValue(option);
        if (result == null){
            result = "";
        }
        return result;
    }

    public static void main(String[]args) throws Exception {
        Options options = createOptions();
        //System.out.println("System CLASSPATH: "+prop.getProperty("java.class.path", null));
        CommandLineParser parser = new GnuParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            String xmlReplayBaseDir = opt(line, "xmlReplayBaseDir");
            String reportsDir = opt(line, "reportsDir");
            String testGroupID      = opt(line, "testGroupID");
            String testID           = opt(line, "testID");
            String autoDeletePOSTS  = opt(line, "autoDeletePOSTS");
            String dumpResults      = opt(line, "dumpResults");
            String controlFilename   = opt(line, "controlFilename");
            String xmlReplayMaster  = opt(line, "xmlReplayMaster");

            if (Tools.isBlank(reportsDir)){
                reportsDir = xmlReplayBaseDir + XmlReplayTest.REPORTS_DIRNAME;
            }
            reportsDir = Tools.fixFilename(reportsDir);
            xmlReplayBaseDir = Tools.fixFilename(xmlReplayBaseDir);
            controlFilename = Tools.fixFilename(controlFilename);

            boolean bAutoDeletePOSTS = true;
            if (Tools.notEmpty(autoDeletePOSTS)) {
                bAutoDeletePOSTS = Tools.isTrue(autoDeletePOSTS);
            }
            boolean bDumpResults = false;
            if (Tools.notEmpty(dumpResults)) {
                bDumpResults = Tools.isTrue(autoDeletePOSTS);
            }
            if (Tools.isEmpty(xmlReplayBaseDir)){
                System.err.println("ERROR: xmlReplayBaseDir was not specified.");
                return;
            }
            File f = new File(Tools.glue(xmlReplayBaseDir, "/", controlFilename));
            if (Tools.isEmpty(xmlReplayMaster) && !f.exists()){
                System.err.println("Control file not found: "+f.getCanonicalPath());
                return;
            }
            File fMaster = new File(Tools.glue(xmlReplayBaseDir, "/", xmlReplayMaster));
            if (Tools.notEmpty(xmlReplayMaster)  && !fMaster.exists()){
                System.err.println("Master file not found: "+fMaster.getCanonicalPath());
                return;
            }

            String xmlReplayBaseDirResolved = (new File(xmlReplayBaseDir)).getCanonicalPath();
            System.out.println("XmlReplay ::"
                            + "\r\n    xmlReplayBaseDir: "+xmlReplayBaseDir
                            + "\r\n    xmlReplayBaseDir(resolved): "+xmlReplayBaseDirResolved
                            + "\r\n    controlFilename: "+controlFilename
                            + "\r\n    xmlReplayMaster: "+xmlReplayMaster
                            + "\r\n    testGroupID: "+testGroupID
                            + "\r\n    testID: "+testID
                            + "\r\n    autoDeletePOSTS: "+bAutoDeletePOSTS
                            + (Tools.notEmpty(xmlReplayMaster)
                                       ? ("\r\n    will use master file: "+fMaster.getCanonicalPath())
                                       : ("\r\n    will use control file: "+f.getCanonicalPath()) )
                             );
            
            if (Tools.notEmpty(xmlReplayMaster)){
                if (Tools.notEmpty(controlFilename)){
                    System.out.println("WARN: controlFilename: "+controlFilename+" will not be used because master was specified.  Running master: "+xmlReplayMaster);
                }
                XmlReplay replay = new XmlReplay(xmlReplayBaseDirResolved, reportsDir);
                replay.readOptionsFromMasterConfigFile(xmlReplayMaster);
                replay.setAutoDeletePOSTS(bAutoDeletePOSTS);
                Dump dumpFromMaster = replay.getDump();
                dumpFromMaster.payloads = Tools.isTrue(dumpResults);
                replay.setDump(dumpFromMaster);
                replay.runMaster(xmlReplayMaster, false); //false, because we already just read the options, and override a few.
            } else {
                Dump dump = getDumpConfig();
                dump.payloads = Tools.isTrue(dumpResults);
                List<String> reportsList = new ArrayList<String>();
                runXmlReplayFile(xmlReplayBaseDirResolved, controlFilename, testGroupID, testID, createResultsMap(), bAutoDeletePOSTS, dump, "", null, reportsList, reportsDir);
                System.out.println("DEPRECATED: reportsList is generated, but not dumped: "+reportsList.toString());
            }
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Cmd-line parsing failed.  Reason: " + exp.getMessage());
            System.err.println(usage());
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            e.printStackTrace();
        }
    }

}
