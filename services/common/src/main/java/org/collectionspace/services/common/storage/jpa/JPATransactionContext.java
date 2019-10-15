package org.collectionspace.services.common.storage.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.InconsistentStateException;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.storage.TransactionContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class JPATransactionContext extends TransactionContext {
    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(JPATransactionContext.class);

	private int transactionRefCount = 0;
	private boolean aclTablesUpdatedFlag = false;
	
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
		
	/**
	 * Set to 'true' if (and only if) a change has been made AND successfully committed to the Spring Security tables.
	 * 
	 * Since we can't include Spring Security table changes and JPA changes in a single transaction, we
	 * keep track of changes to the Spring Security tables here.  We'll use this flag to log a critical error if
	 * we think there is a chance the JPA tables and Spring Security tables get out of sync.
	 * 
	 * @param flag
	 */
	public void setAclTablesUpdateFlag(boolean flag) {
		aclTablesUpdatedFlag = flag;
	}
	
	protected boolean getAclTablesUpdateFlag() {
		return aclTablesUpdatedFlag;
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
			if (getAclTablesUpdateFlag() == false) {
				//
				// Since there were no changes committed to the Spring Security tables, we can just rollback and continue
				//
				em.getTransaction().rollback();
			} else {
				String msg = handleInconsistentState();
				throw new InconsistentStateException(msg);
			}
    	} else if (em.getTransaction().isActive() == true) {
    		markForRollback();
    		close(); // NOTE: Recursive call.
    		throw new JPATransactionException("There was an active transaction.  You must commit the active transaction prior to calling this close method.");
    	}
		
		em.close();
        JpaStorageUtils.releaseEntityManagerFactory(emf);		
	}
	
	private String handleInconsistentState() throws InconsistentStateException {
		//
		// If we've modified the Spring Tables and need to rollback this JPA transaction, we now have a potentially critical inconsistent state in the system
		//
		String msg = "\n#\n# CRITICAL: The Spring Security tables just became inconsistent with CollectionSpace JPA AuthN and AuthZ tables.  Contact your CollectionSpace administrator immediately.\n#";

		//
		// Finish by rolling back the JPA transaction, closing the connection, and throwing an exception
		//
		logger.error(msg);
		em.getTransaction().rollback();
		em.close();
        JpaStorageUtils.releaseEntityManagerFactory(emf);
        
        return msg;
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
	public Object merge(Object entity) {
		return em.merge(entity);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object find(Class entityClass, Object primaryKey) {
		return em.find(entityClass, primaryKey);
	}
	
	@SuppressWarnings("unchecked")
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
	public boolean isTransactionActive() {
		return em.getTransaction().isActive();
	}
	
	@Override
	public void flush() {
		em.flush();
	}

	@Override
	public void commitTransaction() throws TransactionException {
		if (transactionRefCount == 0) {
    		throw new JPATransactionException("There is no active transaction to commit.");
		}
		if (--transactionRefCount == 0) {
			em.getTransaction().commit();
		}
	}
}
