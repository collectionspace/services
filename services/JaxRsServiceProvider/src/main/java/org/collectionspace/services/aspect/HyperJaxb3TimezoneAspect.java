/**
 * An AOP (AspectJ) aspect to resolve the timezone related issue https://issues.collectionspace.org/browse/DRYD-182.
 * 
 * See related config in files: src/main/resources/META-INF/aop.xml, src/main/webapp/WEB-INF/applicationContext-security.xml
 * 
 */
package org.collectionspace.services.aspect;

import java.util.Date;
import javax.xml.datatype.XMLGregorianCalendar;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class HyperJaxb3TimezoneAspect {

	@Around("methodsToBeProfiled()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        try {
        	Date fromDate = (Date)pjp.getArgs()[0];
        	XMLGregorianCalendar toDate = (XMLGregorianCalendar)pjp.getArgs()[1];
                        
            Object result = pjp.proceed();
            //
            // Marshal the timezone info from the 'fromDate' Date instance into the XMLGregorianCalendar 'toDate' instance
            //
            toDate.setTimezone(fromDate.getTimezoneOffset());

            return result;
        } finally {
            // No cleanup needed.
        }
    }

    /**
     * Intercept all calls to the createCalendar() method of the XMLGregorianCalendarAsDateTime class.  This is how HyperJaxb3 marshals datetime info from Hibernate/JPA into
     * out AuthN/AuthZ class instances.
     */
    @Pointcut("execution(* org.jvnet.hyperjaxb3.xml.bind.annotation.adapters.XMLGregorianCalendarAsDateTime.createCalendar(java.util.Date, javax.xml.datatype.XMLGregorianCalendar))")
    public void methodsToBeProfiled() {}
}