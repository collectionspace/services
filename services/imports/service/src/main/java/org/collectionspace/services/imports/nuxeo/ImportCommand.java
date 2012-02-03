package org.collectionspace.services.imports.nuxeo;

import java.io.File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.nuxeo.client.java.NuxeoClientEmbedded;
import org.collectionspace.services.nuxeo.client.java.NuxeoConnectorEmbedded;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
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
        } finally {
//            repository.close();
            client.releaseRepository(repoSession);
        }
    }

    String importTree(RepositoryInstance repository, File file, String toPath) throws Exception {
        DocumentReader reader = null;
        DocumentWriter writer = null;
        String dump = "NO RESULTS";
        try {
            System.out.println("importTree reading file: "+file+(file!=null ? " exists? "+file.exists() : " file param is null"));
            reader = new LoggedXMLDirectoryReader(file);  //our overload of XMLDirectoryReader.
            writer = new DocumentModelWriter(repository, toPath, 10);
            DocumentPipe pipe = new DocumentPipeImpl(10);
            // pipe.addTransformer(transformer);
            pipe.setReader(reader);
            pipe.setWriter(writer);
            pipe.run();
        } finally {
            if (reader != null) {
                reader.close();
                dump = ((LoggedXMLDirectoryReader)reader).report();
            }
            if (writer != null) {
                writer.close();
            }
        }
        return dump;
    }
}