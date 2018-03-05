package org.collectionspace.services.aspect;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author remillet
 * 
 * This method intercepts the all calls to the the setCreatedAt() method of the AuthN/AuthZ JaxB classes.  These classes
 * are all derived from XML Schema by the HyperJaxB3 Maven build plugin, so we couldn't do this in classes themselved.
 * 
 * This method sets the timezone of the incoming XMLGregorianCalendar instance to the current JVM timezone and then
 * normalized the instance to UTC time.  Doing this results in the correct marshaling of the instance into the convention
 * used by the other CollectionSpace services.
 *
 */

@Aspect
public class JaxbXMLGregorianCalendarMarshal {

	@Around("setDateMethods()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
		Object result = null;
		
        try {
        	Object[] args = pjp.getArgs(); // gets us a read-only copy of the argument(s)
        	XMLGregorianCalendar toDate = (XMLGregorianCalendar)args[0]; // get the incoming date argument
        	if (toDate != null) {
	            toDate.setTimezone(new Date().getTimezoneOffset() * -1);	// set the incoming date's timezone to the current JVM timezone
	            toDate = toDate.normalize(); // normalize to UTC time
	            args[0] = toDate; // setup the new arguments
	            result = pjp.proceed(args);	// finish the call
        	} else {
	            result = pjp.proceed();	// finish the call
        	}
        } finally {
            // No cleanup needed.
        }
        
        return result;
    }

	/**
	 * Setup a pointcut for all CSpace classes with methods like setCreatedAt(javax.xml.datatype.XMLGregorianCalendar) and
	 * setUpdatedAt(javax.xml.datatype.XMLGregorianCalendar)
	 */
    @Pointcut("execution(void org.collectionspace.services..setCreatedAt(javax.xml.datatype.XMLGregorianCalendar))")
    public void setCreatedAtCutPoint() {}
    
    @Pointcut("execution(void org.collectionspace.services..setUpdatedAt(javax.xml.datatype.XMLGregorianCalendar))")
    public void setUpdateatedAtCutPoint() {}
    
    @Pointcut("setCreatedAtCutPoint() || setUpdateatedAtCutPoint()")
    public void setDateMethods() {}

}
