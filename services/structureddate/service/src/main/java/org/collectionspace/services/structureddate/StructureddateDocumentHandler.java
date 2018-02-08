package org.collectionspace.services.structureddate;

import java.util.List;

import org.collectionspace.services.common.api.RefName.RefNameInterface;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.AbstractMultipartDocumentHandlerImpl;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.lifecycle.Lifecycle;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.nuxeo.ecm.core.api.DocumentModel;

/*
 * The StructedDate service uses non of these method.  It exists only because it is needed to create a proper ServiceContext instance.
 */
public class StructureddateDocumentHandler extends AbstractMultipartDocumentHandlerImpl<StructureddateCommon, List<StructureddateCommon>, StructureddateCommon, List<StructureddateCommon>> {

	@Override
	public Lifecycle getLifecycle() {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public Lifecycle getLifecycle(String serviceObjectName) {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void handleWorkflowTransition(ServiceContext ctx, DocumentWrapper<DocumentModel> wrapDoc,
			TransitionDef transitionDef) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void handleCreate(DocumentWrapper<StructureddateCommon> wrapDoc) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void handleUpdate(DocumentWrapper<StructureddateCommon> wrapDoc) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void handleGet(DocumentWrapper<StructureddateCommon> wrapDoc) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void handleGetAll(DocumentWrapper<List<StructureddateCommon>> wrapDoc) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void extractAllParts(DocumentWrapper<StructureddateCommon> wrapDoc) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void fillAllParts(DocumentWrapper<StructureddateCommon> wrapDoc,
			org.collectionspace.services.common.document.DocumentHandler.Action action) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public StructureddateCommon extractCommonPart(DocumentWrapper<StructureddateCommon> wrapDoc) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void fillCommonPart(StructureddateCommon obj, DocumentWrapper<StructureddateCommon> wrapDoc)
			throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public List<StructureddateCommon> extractCommonPartList(DocumentWrapper<List<StructureddateCommon>> wrapDoc)
			throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public List<StructureddateCommon> extractPagingInfo(List<StructureddateCommon> theCommonList,
			DocumentWrapper<List<StructureddateCommon>> wrapDoc) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public StructureddateCommon getCommonPart() {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void setCommonPart(StructureddateCommon obj) {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public List<StructureddateCommon> getCommonPartList() {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void setCommonPartList(List<StructureddateCommon> obj) {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public String getQProperty(String prop) throws DocumentException {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	protected String getRefnameDisplayName(DocumentWrapper<StructureddateCommon> docWrapper) {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	protected RefNameInterface getRefName(DocumentWrapper<StructureddateCommon> docWrapper, String tenantName,
			String serviceName) {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public DocumentFilter createDocumentFilter() {
		throw new RuntimeException("Unimplemented method.");
	}
}
