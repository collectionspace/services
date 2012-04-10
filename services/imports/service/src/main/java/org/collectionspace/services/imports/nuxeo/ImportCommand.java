package org.collectionspace.services.imports.nuxeo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.nuxeo.client.java.NuxeoClientEmbedded;
import org.collectionspace.services.nuxeo.client.java.NuxeoConnectorEmbedded;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentTranslationMap;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentModelWriter;
// we use our own override of this: import org.nuxeo.ecm.core.io.impl.plugins.XMLDirectoryReader;

// based loosely on package org.nuxeo.ecm.shell.commands.io.ImportCommand;
public class ImportCommand {
    private static final Log logger = LogFactory.getLog(ImportCommand.class);

    public String run(String src, String dest) throws Exception {
        File file = new File(src);
        ///cspace way of configuring client and auth:
        NuxeoClientEmbedded client = NuxeoConnectorEmbedded.getInstance().getClient();
        RepositoryInstance  repoSession = client.openRepository();
        try {
            return importTree(repoSession, file, dest);
        } catch (Exception e) {
            throw e;
        } finally {
//            repository.close();
            client.releaseRepository(repoSession);
        }
    }

    String importTree(RepositoryInstance repoSession, File file, String toPath) throws Exception {
        DocumentReader reader = null;
        DocumentWriter writer = null;
        DocumentModel docModel = null;
        DocumentRef keyDocRef, valueDocRef;
        String docType;
        StringBuffer dump = new StringBuffer();
        Map<String,Integer> recordsImportedForDocType = new HashMap<String,Integer>();
        Integer numRecordsImportedForDocType = new Integer(0);
        int totalRecordsImported = 0;
        try {
            System.out.println("importTree reading file: "+file+(file!=null ? " exists? "+file.exists() : " file param is null"));
            reader = new LoggedXMLDirectoryReader(file);  //our overload of XMLDirectoryReader.
            writer = new DocumentModelWriter(repoSession, toPath, 10);
            DocumentPipe pipe = new DocumentPipeImpl(10);
            // pipe.addTransformer(transformer);
            pipe.setReader(reader);
            pipe.setWriter(writer);
            DocumentTranslationMap dtm = pipe.run();
            Map<DocumentRef,DocumentRef> documentRefs = dtm.getDocRefMap();
            dump.append("<importedRecords>");
            for (Map.Entry entry: documentRefs.entrySet()) {
                keyDocRef = (DocumentRef) entry.getKey();
                valueDocRef = (DocumentRef) entry.getValue();
                if (keyDocRef == null || valueDocRef == null) {
                    continue;
                }
                dump.append("<importedRecord>");
                docModel = repoSession.getDocument(valueDocRef);
                docType = docModel.getDocumentType().getName();
                dump.append("<doctype>"+docType+"</doctype>");
                dump.append("<csid>"+keyDocRef.toString()+"</csid>");
                dump.append("</importedRecord>");
                if (recordsImportedForDocType.containsKey(docType)) {
                    numRecordsImportedForDocType = (Integer) recordsImportedForDocType.get(docType);
                    numRecordsImportedForDocType = Integer.valueOf(numRecordsImportedForDocType.intValue() + 1);
                    recordsImportedForDocType.put(docType, numRecordsImportedForDocType);
                } else {
                    recordsImportedForDocType.put(docType, 1);
                }
                totalRecordsImported++;
            }
            dump.append("</importedRecords>");
        } catch (Exception e) {
            throw e;
        } finally {
            dump.append("<totalRecordsImported>"+totalRecordsImported+"</totalRecordsImported>");
            dump.append("<numRecordsImportedByDocType>");
            TreeSet<String> keys = new TreeSet<String>(recordsImportedForDocType.keySet());
            for (String key : keys) {
                dump.append("<numRecordsImported>");
                dump.append("<docType>"+key+"</docType>");
                dump.append("<numRecords>"+recordsImportedForDocType.get(key).intValue()+"</numRecords>");
                dump.append("</numRecordsImported>");
            }
            dump.append("</numRecordsImportedByDocType>");
            if (reader != null) {
                dump.append("<report>"+(((LoggedXMLDirectoryReader)reader).report())+"</report>");
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
        return dump.toString();
    }
}