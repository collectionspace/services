package org.collectionspace.services.export;

import java.util.Iterator;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.invocable.InvocationContext;

public class DocumentsByGroupIterator implements Iterator<PoxPayloadOut> {
  private RelationObjectsByQueryIterator relationsIterator;

  DocumentsByGroupIterator(
    ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
    String csid) throws Exception {

    InvocationContext.Query query = new InvocationContext.Query();

    query.setSbjType("Group");
    query.setSbj(csid);
    query.setPrd("affects");
    query.setWfDeleted(false);

    relationsIterator = new RelationObjectsByQueryIterator(serviceContext, RelationClient.SERVICE_DOC_TYPE, null, query);
  }

  @Override
  public boolean hasNext() {
    return (
      relationsIterator != null
      && relationsIterator.hasNext()
    );
  }

  @Override
  public PoxPayloadOut next() {
    return relationsIterator.next();
  }
}
