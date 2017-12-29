package org.collectionspace.services.common.storage.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.storage.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class JPATransactionContext extends TransactionContext {
    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(TransactionContext.class);

	private int transactionRefCount = 0;
	private Boolean commitSuccessful = null;
	
	EntityManagerFactory emf;
	EntityManager em;
	
	public JPATransactionContext(ServiceContext ctx) {
        emf = JpaStorageUtils.getEntityManagerFactory();            
        em = emf.createEntityManager();
        this.ctx = ctx;
	}
	
	public JPATransactionContext() {
        emf = JpaStorageUtils.getEntityManagerFactory();            
        em = emf.createEntityManager();
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
		if (em.getTransaction().isActive() == true) {
			em.getTransaction().setRollbackOnly();
		} else {
			String msg = "Attemped to mark an inactive transaction for rollback.";
			logger.warn(msg);
		}
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
	synchronized public void beginTransaction() {
		if (transactionRefCount == 0) {
			em.getTransaction().begin();
		}
        transactionRefCount++;
	}
	
	@Override
	public void persist(Object entity) {
		em.persist(entity);
	}
	
	@Override
	public Object find(Class entityClass, Object primaryKey) {
		return em.find(entityClass, primaryKey);
	}
	
	@Override
	public Object find(Class entityClass, String id) {
		return em.find(entityClass, id);
	}
	
	@Override
	public Query createQuery(String qlString) {
		return em.createQuery(qlString);
	}
	
	@Override
    public void remove(Object entity) {
		em.remove(entity);
	}

	@Override
	public void commitTransaction() throws TransactionException {
		if (transactionRefCount == 0) {
    		throw new JPATransactionException("There is no active transaction to commit.");
		}
		if (--transactionRefCount == 0) {
			em.getTransaction().commit();
			commitSuccessful = Boolean.TRUE;
		}
	}

	@Override
	public boolean isTransactionActive() {
		return em.getTransaction().isActive();
	}
}
