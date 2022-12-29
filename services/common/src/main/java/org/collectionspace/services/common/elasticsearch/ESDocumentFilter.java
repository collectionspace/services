package org.collectionspace.services.common.elasticsearch;

import org.collectionspace.services.audit.AuditCommon;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;

public class ESDocumentFilter extends DocumentFilter {
	public ESDocumentFilter(ServiceContext<AuditCommon, AuditCommon> ctx) {
		super(ctx);
	}

	public ESDocumentFilter(String whereClause, int theStartPage, int thePageSize) {
		super(whereClause, theStartPage, thePageSize);
	}

	@Override
	public boolean getPageSizeDirty() {
		// TODO Auto-generated method stub
		return false;
	}

}
