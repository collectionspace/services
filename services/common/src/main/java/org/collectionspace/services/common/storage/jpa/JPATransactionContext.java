package org.collectionspace.services.common.storage.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.storage.TransactionContext;

@SuppressWarnings("rawtypes")
public class JPATransactionContext extends TransactionContext {
	EntityManagerFactory emf;
	EntityManager em;
	
	@SuppressWarnings("unused")
	private JPATransactionContext() {
		// Don't allow anyone to create an empty instance
	}
	
	public JPATransactionContext(ServiceContext ctx) {
        emf = JpaStorageUtils.getEntityManagerFactory();            
        em = emf.createEntityManager();
        this.ctx = ctx;
	}

	protected EntityManagerFactory getEntityManagerFactory() {
		return emf;
	}
	
	protected EntityManager getEntityManager() {
		return em;
	}
	
	@Override
	public ServiceContext getServiceContext() {
		return ctx;
	}
	
	@Override
	public void markForRollback() {
		em.getTransaction().setRollbackOnly();
	}
	
	@Override
	public void close() throws TransactionException  {
		if (em.getTransaction().isActive() == true && em.getTransaction().getRollbackOnly() == true) {
			em.getTransaction().rollback();
    	} else if (em.getTransaction().isActive() == true) {
    		throw new JPATransactionException("There is an active transaction.  You must commit the active transaction prior to calling this close method.");
    	}
    	
		em.close();
        JpaStorageUtils.releaseEntityManagerFactory(emf);
	}

	@Override
	public void beginTransaction() {
        em.getTransaction().begin();    
	}

	@Override
	public void commitTransaction() {
        em.getTransaction().commit();
	}

	@Override
	public boolean isTransactionActive() {
		return em.getTransaction().isActive();
	}
}
