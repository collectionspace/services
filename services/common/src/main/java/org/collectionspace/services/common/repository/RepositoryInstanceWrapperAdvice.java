package org.collectionspace.services.common.repository;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;


public class RepositoryInstanceWrapperAdvice implements MethodInterceptor {

	public RepositoryInstanceWrapperAdvice() {
		// TODO Auto-generated constructor stub
	}

	//@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		try {
			// proceed to original method call
			Object result = methodInvocation.proceed(); 
			return result;
		} catch (IllegalArgumentException e) {
			throw e;
		}
	}
}
